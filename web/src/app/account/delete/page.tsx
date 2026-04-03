'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useState } from 'react';
import { apiDeleteAccount } from '../../api';
import { useSession } from '../../session-context';

export default function AccountDeletePage() {
  const router = useRouter();
  const { isReady, role, logout } = useSession();
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  async function handleDelete() {
    setLoading(true);
    setError('');
    setMessage('');

    try {
      const result = await apiDeleteAccount();
      logout();
      setMessage(`Account deleted for ${result.deleted_email}.`);
      router.replace('/login');
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Failed to delete account.');
    } finally {
      setLoading(false);
    }
  }

  if (!isReady) {
    return (
      <div className="mx-auto max-w-2xl rounded-3xl bg-white p-8 shadow-sm">
        <p className="text-sm text-[#5a5872]">Loading account options...</p>
      </div>
    );
  }

  if (role === 'guest') {
    return (
      <div className="mx-auto max-w-2xl space-y-5 rounded-3xl bg-white p-8 shadow-sm">
        <h1 className="text-3xl font-bold text-[#0f0a1e]">Delete your account</h1>
        <p className="text-sm leading-7 text-[#5a5872]">
          Sign in with the customer account you want to remove, then return to this page to complete deletion.
        </p>
        <Link
          href="/login?next=%2Faccount%2Fdelete"
          className="inline-flex rounded-xl bg-[#1a132f] px-4 py-3 font-semibold text-white"
        >
          Sign in to continue
        </Link>
      </div>
    );
  }

  if (role === 'admin') {
    return (
      <div className="mx-auto max-w-2xl space-y-5 rounded-3xl bg-white p-8 shadow-sm">
        <h1 className="text-3xl font-bold text-[#0f0a1e]">Delete your account</h1>
        <p className="text-sm leading-7 text-[#5a5872]">
          Admin accounts are managed separately and cannot be deleted from this self-service page.
        </p>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-2xl space-y-6 rounded-3xl bg-white p-8 shadow-sm">
      <h1 className="text-3xl font-bold text-[#0f0a1e]">Delete your account</h1>
      <p className="text-sm leading-7 text-[#5a5872]">
        This permanently removes your customer account, booking history, and testimonial submissions from the live
        booking system. This action cannot be undone.
      </p>
      {message && <p className="rounded-2xl bg-[#eef7ef] px-4 py-3 text-sm text-[#215c2c]">{message}</p>}
      {error && <p className="rounded-2xl bg-[#fff1f1] px-4 py-3 text-sm text-[#b3261e]">{error}</p>}
      <button
        type="button"
        onClick={handleDelete}
        disabled={loading}
        className="w-full rounded-xl bg-[#b3261e] px-4 py-3 font-semibold text-white disabled:opacity-60"
      >
        {loading ? 'Deleting account...' : 'Delete account permanently'}
      </button>
      <p className="text-xs leading-6 text-[#7b7794]">
        Need help instead? Email{' '}
        <a className="font-semibold text-[#5b4fe5] underline" href="mailto:admin@brazwebdes.com">
          admin@brazwebdes.com
        </a>
        .
      </p>
    </div>
  );
}
