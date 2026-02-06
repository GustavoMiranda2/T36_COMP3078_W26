from datetime import datetime, time, timedelta

from django.db import IntegrityError
from django.utils import timezone
from rest_framework import generics, status
from rest_framework.exceptions import ValidationError
from rest_framework.permissions import AllowAny, IsAuthenticated
from rest_framework.response import Response
from rest_framework.views import APIView
from rest_framework_simplejwt.views import TokenObtainPairView

from .models import Appointment, AppointmentStatus, Service
from .serializers import (
    AppointmentCreateSerializer,
    AppointmentSerializer,
    AppointmentUpdateSerializer,
    CustomTokenObtainPairSerializer,
    RegisterSerializer,
    ServiceSerializer,
    UserSerializer,
)

BUSINESS_START = time(10, 0)
BUSINESS_END = time(19, 0)
SLOT_MINUTES = 15


def _parse_date_param(value: str | None):
    if not value:
        raise ValidationError({"date": "date query parameter is required (YYYY-MM-DD)."})
    try:
        return datetime.strptime(value, "%Y-%m-%d").date()
    except ValueError as exc:
        raise ValidationError({"date": "date must be in YYYY-MM-DD format."}) from exc


def _is_aligned_to_grid(value: time, minutes: int = SLOT_MINUTES) -> bool:
    return value.minute % minutes == 0 and value.second == 0 and value.microsecond == 0


def _ensure_aligned(value: time):
    if not _is_aligned_to_grid(value):
        raise ValidationError({"start_time": "start_time must align to a 15-minute grid."})


def _ensure_within_business_hours(start_dt: datetime, end_dt: datetime):
    day_start = datetime.combine(start_dt.date(), BUSINESS_START, tzinfo=start_dt.tzinfo)
    day_end = datetime.combine(start_dt.date(), BUSINESS_END, tzinfo=start_dt.tzinfo)
    if start_dt < day_start or end_dt > day_end:
        raise ValidationError(
            {"start_time": "start_time must be within business hours (10:00-19:00)."}
        )


def _get_active_service(service_id):
    service = Service.objects.filter(id=service_id, is_active=True).first()
    if not service:
        raise ValidationError({"service_id": "Service not found or inactive."})
    return service


def _ensure_no_conflict(start_dt: datetime, end_dt: datetime, exclude_id=None):
    qs = Appointment.objects.exclude(status=AppointmentStatus.CANCELLED)
    if exclude_id:
        qs = qs.exclude(id=exclude_id)
    conflict = qs.filter(start_time__lt=end_dt, end_time__gt=start_dt).exists()
    if conflict:
        raise ValidationError({"detail": "Requested time overlaps an existing appointment."})


class RegisterView(APIView):
    authentication_classes = []
    permission_classes = [AllowAny]

    def post(self, request):
        serializer = RegisterSerializer(data=request.data)
        serializer.is_valid(raise_exception=True)
        try:
            user = serializer.save()
        except IntegrityError:
            raise ValidationError({"email": "Email already registered."})
        return Response(UserSerializer(user).data, status=status.HTTP_201_CREATED)


class LoginView(TokenObtainPairView):
    serializer_class = CustomTokenObtainPairSerializer
    permission_classes = [AllowAny]


class ServiceListView(generics.ListAPIView):
    serializer_class = ServiceSerializer
    permission_classes = [AllowAny]

    def get_queryset(self):
        return Service.objects.filter(is_active=True).order_by("name")


class AvailabilityView(APIView):
    authentication_classes = []
    permission_classes = [AllowAny]

    def get(self, request):
        target_date = _parse_date_param(request.query_params.get("date"))

        tz = timezone.get_current_timezone()
        day_start = datetime.combine(target_date, BUSINESS_START)
        day_end = datetime.combine(target_date, BUSINESS_END)

        slots = []
        current = day_start
        while current + timedelta(minutes=SLOT_MINUTES) <= day_end:
            slots.append(current)
            current += timedelta(minutes=SLOT_MINUTES)

        slot_windows = [
            (
                timezone.make_aware(slot, tz),
                timezone.make_aware(slot + timedelta(minutes=SLOT_MINUTES), tz),
            )
            for slot in slots
        ]

        appointments = Appointment.objects.exclude(
            status=AppointmentStatus.CANCELLED
        ).filter(
            start_time__lt=timezone.make_aware(day_end, tz),
            end_time__gt=timezone.make_aware(day_start, tz),
        )

        available_slots = []
        for slot_start, slot_end in slot_windows:
            conflict = appointments.filter(
                start_time__lt=slot_end, end_time__gt=slot_start
            ).exists()
            if not conflict:
                available_slots.append(slot_start.strftime("%H:%M"))

        return Response(
            {"date": target_date.isoformat(), "slots": available_slots},
            status=status.HTTP_200_OK,
        )


class AppointmentListCreateView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        if request.query_params.get("me") != "true":
            raise ValidationError({"me": "Query parameter me=true is required."})
        appointments = (
            Appointment.objects.filter(user=request.user)
            .select_related("service")
            .order_by("-start_time")
        )
        return Response(
            AppointmentSerializer(appointments, many=True).data,
            status=status.HTTP_200_OK,
        )

    def post(self, request):
        serializer = AppointmentCreateSerializer(data=request.data)
        serializer.is_valid(raise_exception=True)

        service = _get_active_service(serializer.validated_data["service_id"])

        tz = timezone.get_current_timezone()
        date_value = serializer.validated_data["date"]
        time_value = serializer.validated_data["start_time"]

        _ensure_aligned(time_value)
        start_dt = timezone.make_aware(datetime.combine(date_value, time_value), tz)
        end_dt = start_dt + timedelta(minutes=service.duration_minutes)
        _ensure_within_business_hours(start_dt, end_dt)
        _ensure_no_conflict(start_dt, end_dt)

        appointment = Appointment.objects.create(
            user=request.user,
            service=service,
            start_time=start_dt,
            end_time=end_dt,
            status=AppointmentStatus.CONFIRMED,
        )
        return Response(
            AppointmentSerializer(appointment).data,
            status=status.HTTP_201_CREATED,
        )


class AppointmentUpdateView(APIView):
    permission_classes = [IsAuthenticated]

    def patch(self, request, appointment_id):
        serializer = AppointmentUpdateSerializer(data=request.data)
        serializer.is_valid(raise_exception=True)

        appointment = Appointment.objects.filter(
            id=appointment_id, user=request.user
        ).select_related("service").first()
        if not appointment:
            return Response(status=status.HTTP_404_NOT_FOUND)

        action = serializer.validated_data["action"]
        if action == "cancel":
            appointment.status = AppointmentStatus.CANCELLED
            appointment.save(update_fields=["status"])
            return Response(
                AppointmentSerializer(appointment).data,
                status=status.HTTP_200_OK,
            )

        tz = timezone.get_current_timezone()
        date_value = serializer.validated_data["date"]
        time_value = serializer.validated_data["start_time"]

        _ensure_aligned(time_value)
        start_dt = timezone.make_aware(datetime.combine(date_value, time_value), tz)
        end_dt = start_dt + timedelta(minutes=appointment.service.duration_minutes)
        _ensure_within_business_hours(start_dt, end_dt)
        _ensure_no_conflict(start_dt, end_dt, exclude_id=appointment.id)

        appointment.start_time = start_dt
        appointment.end_time = end_dt
        appointment.status = AppointmentStatus.CONFIRMED
        appointment.save(update_fields=["start_time", "end_time", "status"])

        return Response(
            AppointmentSerializer(appointment).data,
            status=status.HTTP_200_OK,
        )
