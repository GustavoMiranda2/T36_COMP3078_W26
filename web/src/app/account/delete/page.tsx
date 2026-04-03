'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useState } from 'react';
import { apiDeleteAccount } from '../../api';
import { ActionDialog } from '../../components/ActionDialog';
import { useSession } from '../../session-context';

export default function AccountDeletionPage() {
  const router = useRouter();
  const { isReady, role, displayName, logout } = useSession();
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [deletedEmail, setDeletedEmail] = useState('');

  async function confirmDelete() {
    setBusy(true);
    setError('');

    try {
      const result = await apiDeleteAccount();
      setDeletedEmail(result.deleted_email);
      setConfirmOpen(false);
      logout();
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Failed to delete account.');
    } finally {
      setBusy(false);
    }
  }

  return (
    <>
      <div className="mx-auto max-w-3xl space-y-6 rounded-[2rem] bg-white p-6 shadow-sm md:p-8">
        <div className="inline-flex rounded-full bg-[#fff4ea] px-3 py-1 text-sm font-semibold text-[#a45d15]">
          Account deletion
        </div>

        <div>
          <h1 className="text-3xl font-bold text-[#0f0a1e]">Delete your Brazdes account</h1>
          <p className="mt-3 max-w-2xl text-sm leading-6 text-[#5a5872]">
            This page exists for account deletion compliance and for client self-service. Deleting an account removes
            the personal profile attached to normal client bookings. Admin accounts cannot be deleted here.
          </p>
        </div>

        {deletedEmail ? (
          <div className="rounded-[1.5rem] border border-[#cfead8] bg-[#effbf4] p-5">
            <p className="text-sm font-semibold text-[#0d7a33]">Account deleted</p>
            <p className="mt-2 text-sm leading-6 text-[#2d5a3b]">
              The account for <span className="font-semibold">{deletedEmail}</span> was deleted successfully.
            </p>
          </div>
        ) : null}

        {error ? (
          <div className="rounded-[1.5rem] border border-[#f4d7db] bg-[#fff1f3] p-5 text-sm font-semibold text-[#b42341]">
            {error}
          </div>
        ) : null}

        <div className="rounded-[1.5rem] border border-[#ecebf5] bg-[#fcfcff] p-5">
          <p className="text-sm font-semibold text-[#0f0a1e]">What gets removed</p>
          <ul className="mt-3 space-y-2 text-sm leading-6 text-[#5a5872]">
            <li>Your client account profile and personal data stored for self-service access.</li>
            <li>Access to future self-service booking management under that account.</li>
            <li>Admin accounts are excluded from this self-service deletion flow.</li>
          </ul>
        </div>

        {!isReady ? (
          <p className="text-sm text-[#5a5872]">Loading session…</p>
        ) : role === 'user' ? (
          <div className="rounded-[1.5rem] border border-[#ecebf5] bg-white p-5">
            <p className="text-sm font-semibold text-[#0f0a1e]">Signed in as {displayName || 'Client'}</p>
            <p className="mt-2 text-sm leading-6 text-[#5a5872]">
              If you want to permanently delete this client account, confirm below. This action cannot be undone.
            </p>
            <button
              type="button"
              onClick={() => setConfirmOpen(true)}
              className="mt-5 rounded-xl bg-[#b42318] px-5 py-3 text-sm font-semibold text-white shadow-sm transition hover:brightness-110"
            >
              Delete my account
            </button>
          </div>
        ) : role === 'admin' ? (
          <div className="rounded-[1.5rem] border border-[#f0dfcf] bg-[#fff4ea] p-5">
            <p className="text-sm font-semibold text-[#a45d15]">Admin accounts are managed separately</p>
            <p className="mt-2 text-sm leading-6 text-[#7a5731]">
              Admin accounts cannot be deleted through this public self-service page. Remove them through the internal
              administrative workflow instead.
            </p>
          </div>
        ) : (
          <div className="rounded-[1.5rem] border border-[#ecebf5] bg-white p-5">
            <p className="text-sm font-semibold text-[#0f0a1e]">Not signed in</p>
            <p className="mt-2 text-sm leading-6 text-[#5a5872]">
              Sign in with the client account you want to remove, then return to this page to confirm deletion.
            </p>
            <div className="mt-5 flex flex-wrap gap-3">
              <Link
                href="/login?next=%2Faccount%2Fdelete"
                className="rounded-xl bg-[#1a132f] px-5 py-3 text-sm font-semibold text-white shadow-sm transition hover:brightness-110"
              >
                Sign in to continue
              </Link>
              <button
                type="button"
                onClick={() => router.push('/signup')}
                className="rounded-xl border border-[#1a132f] px-5 py-3 text-sm font-semibold text-[#1a132f] transition hover:bg-[#f6f6f6]"
              >
                Create client account
              </button>
            </div>
          </div>
        )}
      </div>

      <ActionDialog
        open={confirmOpen}
        eyebrow="Permanent action"
        title="Delete this account permanently?"
        description="This removes the client account profile tied to your login. You will need to create a new account if you want to book again later."
        tone="danger"
        onClose={busy ? undefined : () => setConfirmOpen(false)}
        closeLabel="Back"
        actions={
          <>
            <button
              type="button"
              onClick={confirmDelete}
              disabled={busy}
              className="w-full rounded-xl bg-[#b42318] px-4 py-3 text-sm font-semibold text-white shadow-sm transition hover:brightness-110 disabled:cursor-not-allowed disabled:opacity-70"
            >
              {busy ? 'Deleting account…' : 'Yes, delete account'}
            </button>
            <button
              type="button"
              onClick={() => setConfirmOpen(false)}
              disabled={busy}
              className="w-full rounded-xl border border-[#e2d5d8] px-4 py-3 text-sm font-semibold text-[#6d4250] transition hover:bg-white disabled:cursor-not-allowed disabled:opacity-70"
            >
              Keep account
            </button>
          </>
        }
      />
    </>
  );
}
