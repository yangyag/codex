import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import {
  archivePost,
  createBoard,
  createPost,
  fetchBoards,
  fetchPosts,
  updateBoard,
  updateIdentityUserStatus,
  updateMemberStatus,
  updatePost,
} from './api';

const API_BASE =
  process.env.VITE_MEMBER_API_BASE ||
  (typeof window !== 'undefined'
    ? `${window.location.protocol}//${window.location.hostname}:8083`
    : 'http://127.0.0.1:8083');
const ID_API_BASE =
  process.env.VITE_API_BASE ||
  (typeof window !== 'undefined'
    ? `${window.location.protocol}//${window.location.hostname}:8083`
    : 'http://127.0.0.1:8083');
const BOARD_API_BASE =
  process.env.VITE_BOARD_API_BASE ||
  (typeof window !== 'undefined'
    ? `${window.location.protocol}//${window.location.hostname}:8083`
    : 'http://127.0.0.1:8083');

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

  it('fetches boards with filters', async () => {
    const mockFetch = vi.fn().mockResolvedValue(
      new Response(JSON.stringify({ content: [], page: 0, size: 10, totalPages: 0, totalElements: 0 }), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      })
    );
    global.fetch = mockFetch;

    await fetchBoards('token-1', 0, 10, 'ann', 'PUBLIC', 'ACTIVE');

    expect(mockFetch).toHaveBeenCalledTimes(1);
    const [url] = mockFetch.mock.calls[0];
    expect(url).toBe(
      `${BOARD_API_BASE}/api/v1/boards?page=0&size=10&q=ann&visibility=PUBLIC&status=ACTIVE`
    );
  });

  it('creates and updates board with auth header', async () => {
    const responses = [
      new Response(JSON.stringify({ id: '1', name: 'x' }), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      }),
      new Response(JSON.stringify({ id: '1', name: 'x', status: 'INACTIVE' }), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      }),
    ];
    const mockFetch = vi.fn().mockImplementation(() => responses.shift()!);
    global.fetch = mockFetch;

    await createBoard('t', 'News', 'PUBLIC');
    await updateBoard('t', '1', { status: 'INACTIVE' });

    expect(mockFetch).toHaveBeenCalledTimes(2);
    const [createUrl, createInit] = mockFetch.mock.calls[0];
    expect(createUrl).toBe(`${BOARD_API_BASE}/api/v1/boards`);
    expect(createInit?.headers).toMatchObject({ Authorization: 'Bearer t' });
    const [updateUrl, updateInit] = mockFetch.mock.calls[1];
    expect(updateUrl).toBe(`${BOARD_API_BASE}/api/v1/boards/1`);
    expect(updateInit?.headers).toMatchObject({ Authorization: 'Bearer t' });
    expect(updateInit?.method).toBe('PATCH');
  });

  it('handles post CRUD endpoints', async () => {
    const responses = [
      new Response(JSON.stringify({ content: [] }), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      }),
      new Response(JSON.stringify({ id: 'p1', title: 'title', content: 'content' }), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      }),
      new Response(JSON.stringify({ id: 'p1', status: 'ARCHIVED' }), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      }),
      new Response(JSON.stringify({}), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      }),
    ];
    const mockFetch = vi.fn().mockImplementation(() => responses.shift()!);
    global.fetch = mockFetch;

    await fetchPosts('t', 'b1', 0, 10, 'PUBLISHED', 'author');
    await createPost('t', 'b1', 'title', 'content', 'PUBLISHED');
    await updatePost('t', 'b1', 'p1', { status: 'ARCHIVED' });
    await archivePost('t', 'b1', 'p1');

    expect(mockFetch).toHaveBeenCalledTimes(4);
    expect(mockFetch.mock.calls[0][0]).toBe(
      `${BOARD_API_BASE}/api/v1/boards/b1/posts?page=0&size=10&status=PUBLISHED&author=author`
    );
    expect(mockFetch.mock.calls[1][0]).toBe(`${BOARD_API_BASE}/api/v1/boards/b1/posts`);
    expect(mockFetch.mock.calls[2][0]).toBe(`${BOARD_API_BASE}/api/v1/boards/b1/posts/p1`);
    expect(mockFetch.mock.calls[3][0]).toBe(`${BOARD_API_BASE}/api/v1/boards/b1/posts/p1`);
    expect((mockFetch.mock.calls[3][1] as RequestInit | undefined)?.method).toBe('DELETE');
  });
});
