import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { updateIdentityUserStatus, updateMemberStatus } from './api';

const API_BASE =
  process.env.VITE_MEMBER_API_BASE ||
  (typeof window !== 'undefined'
    ? `${window.location.protocol}//${window.location.hostname}:8082`
    : 'http://127.0.0.1:8082');
const ID_API_BASE =
  process.env.VITE_API_BASE ||
  (typeof window !== 'undefined'
    ? `${window.location.protocol}//${window.location.hostname}:8081`
    : 'http://127.0.0.1:8081');

describe('api client', () => {
  const originalFetch = global.fetch;

  beforeEach(() => {
    vi.resetAllMocks();
  });

  afterEach(() => {
    global.fetch = originalFetch;
  });

  it('calls status update endpoint with token and payload', async () => {
    const fakeResponse = {
      id: '123',
      email: 'test@example.com',
      name: 'tester',
      role: 'USER',
      status: 'ACTIVE',
      createdAt: new Date().toISOString(),
    };
    const mockFetch = vi.fn().mockResolvedValue(
      new Response(JSON.stringify(fakeResponse), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      })
    );
    global.fetch = mockFetch;

    const result = await updateMemberStatus('token-abc', '123', 'BLOCKED');

    expect(mockFetch).toHaveBeenCalledTimes(1);
    const [url, init] = mockFetch.mock.calls[0];
    expect(url).toBe(`${API_BASE}/api/v1/members/123/status`);
    expect(init?.method).toBe('PATCH');
    expect(init?.headers).toMatchObject({
      Authorization: 'Bearer token-abc',
      'Content-Type': 'application/json',
    });
    expect(init?.body).toBe(JSON.stringify({ status: 'BLOCKED' }));
    expect(result.id).toBe('123');
  });

  it('calls identity status update endpoint with token and payload', async () => {
    const mockFetch = vi.fn().mockResolvedValue(
      new Response(JSON.stringify({}), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      })
    );
    global.fetch = mockFetch;

    await updateIdentityUserStatus('token-xyz', 'admin', 'BLOCKED');

    expect(mockFetch).toHaveBeenCalledTimes(1);
    const [url, init] = mockFetch.mock.calls[0];
    expect(url).toBe(`${ID_API_BASE}/api/v1/admin/users/admin/status`);
    expect(init?.method).toBe('PATCH');
    expect(init?.headers).toMatchObject({
      Authorization: 'Bearer token-xyz',
      'Content-Type': 'application/json',
    });
    expect(init?.body).toBe(JSON.stringify({ status: 'BLOCKED' }));
  });
});
