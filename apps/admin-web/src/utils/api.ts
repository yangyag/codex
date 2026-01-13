const API_BASE =
  import.meta.env.VITE_API_BASE ||
  (typeof window !== 'undefined'
    ? `${window.location.protocol}//${window.location.hostname}:8083`
    : 'http://127.0.0.1:8083');

const MEMBER_API_BASE =
  import.meta.env.VITE_MEMBER_API_BASE ||
  (typeof window !== 'undefined'
    ? `${window.location.protocol}//${window.location.hostname}:8083`
    : 'http://127.0.0.1:8083');

const BOARD_API_BASE =
  import.meta.env.VITE_BOARD_API_BASE ||
  (typeof window !== 'undefined'
    ? `${window.location.protocol}//${window.location.hostname}:8083`
    : 'http://127.0.0.1:8083');

export type AuthResponse = {
  token: string;
  email: string;
  role: string;
};

export type PageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type UserSummary = {
  id: string;
  email: string;
  name: string;
  role: string;
  status: string;
  createdAt: string;
};

export type BoardSummary = {
  id: string;
  name: string;
  visibility: string;
  status: string;
  createdAt: string;
  updatedAt: string;
};

export type PostSummary = {
  id: string;
  boardId: string;
  authorEmail: string;
  title: string;
  content: string;
  status: string;
  createdAt: string;
  updatedAt: string;
};

export async function login(email: string, password: string): Promise<AuthResponse> {
  const res = await fetch(`${API_BASE}/api/v1/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password }),
  });
  return handleJson(res, '로그인에 실패했습니다.');
}

export type SignupResponse = {
  id: string;
  email: string;
  role: string;
  status: string;
  createdAt: string;
};

export async function signup(email: string, password: string): Promise<SignupResponse> {
  const res = await fetch(`${API_BASE}/api/v1/auth/signup`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password }),
  });
  return handleJson(res, '회원가입에 실패했습니다.');
}

export async function fetchUsers(
  token: string,
  page: number,
  size: number,
  query?: string
): Promise<PageResponse<UserSummary>> {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  });
  if (query && query.trim().length > 0) {
    params.set('q', query.trim());
  }
  const res = await fetch(`${MEMBER_API_BASE}/api/v1/members?${params.toString()}`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
  return handleJson(res, '회원 목록을 불러오지 못했습니다.');
}

export async function updateMemberStatus(
  token: string,
  memberId: string,
  nextStatus: string
): Promise<UserSummary> {
  const res = await fetch(`${MEMBER_API_BASE}/api/v1/members/${memberId}/status`, {
    method: 'PATCH',
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ status: nextStatus }),
  });
  return handleJson(res, '회원 상태를 변경하지 못했습니다.');
}

export async function updateIdentityUserStatus(
  token: string,
  email: string,
  nextStatus: string
): Promise<void> {
  const res = await fetch(`${API_BASE}/api/v1/admin/users/${encodeURIComponent(email)}/status`, {
    method: 'PATCH',
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ status: nextStatus }),
  });
  await handleJson(res, '로그인 시스템 상태 동기화에 실패했습니다.');
}

export async function fetchBoards(
  token: string,
  page: number,
  size: number,
  query?: string,
  visibility?: string,
  status?: string
): Promise<PageResponse<BoardSummary>> {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  });
  if (query && query.trim()) {
    params.set('q', query.trim());
  }
  if (visibility) {
    params.append('visibility', visibility);
  }
  if (status) {
    params.append('status', status);
  }
  const res = await fetch(`${BOARD_API_BASE}/api/v1/boards?${params.toString()}`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
  return handleJson(res, '게시판 목록을 불러오지 못했습니다.');
}

export async function createBoard(token: string, name: string, visibility: string): Promise<BoardSummary> {
  const res = await fetch(`${BOARD_API_BASE}/api/v1/boards`, {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ name, visibility }),
  });
  return handleJson(res, '게시판 생성에 실패했습니다.');
}

export async function updateBoard(
  token: string,
  id: string,
  payload: Partial<{ name: string; visibility: string; status: string }>
): Promise<BoardSummary> {
  const res = await fetch(`${BOARD_API_BASE}/api/v1/boards/${id}`, {
    method: 'PATCH',
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(payload),
  });
  return handleJson(res, '게시판 수정에 실패했습니다.');
}

export async function fetchPosts(
  token: string,
  boardId: string,
  page: number,
  size: number,
  status?: string,
  author?: string
): Promise<PageResponse<PostSummary>> {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  });
  if (status) {
    params.append('status', status);
  }
  if (author && author.trim()) {
    params.set('author', author.trim());
  }
  const res = await fetch(
    `${BOARD_API_BASE}/api/v1/boards/${boardId}/posts?${params.toString()}`,
    {
      headers: { Authorization: `Bearer ${token}` },
    }
  );
  return handleJson(res, '게시글 목록을 불러오지 못했습니다.');
}

export async function createPost(
  token: string,
  boardId: string,
  title: string,
  content: string,
  status?: string
): Promise<PostSummary> {
  const res = await fetch(`${BOARD_API_BASE}/api/v1/boards/${boardId}/posts`, {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      title,
      content,
      status,
    }),
  });
  return handleJson(res, '게시글 작성에 실패했습니다.');
}

export async function updatePost(
  token: string,
  boardId: string,
  postId: string,
  payload: Partial<{ title: string; content: string; status: string }>
): Promise<PostSummary> {
  const res = await fetch(`${BOARD_API_BASE}/api/v1/boards/${boardId}/posts/${postId}`, {
    method: 'PATCH',
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(payload),
  });
  return handleJson(res, '게시글 수정에 실패했습니다.');
}

export async function archivePost(token: string, boardId: string, postId: string): Promise<void> {
  const res = await fetch(`${BOARD_API_BASE}/api/v1/boards/${boardId}/posts/${postId}`, {
    method: 'DELETE',
    headers: { Authorization: `Bearer ${token}` },
  });
  await handleJson(res, '게시글 삭제에 실패했습니다.');
}

async function handleJson<T>(res: Response, defaultMessage: string): Promise<T> {
  if (!res.ok) {
    try {
      const data = await res.json();
      const message = data?.message || defaultMessage;
      throw new Error(message);
    } catch (e) {
      if (e instanceof Error && e.message !== defaultMessage) {
        throw e;
      }
      throw new Error(defaultMessage);
    }
  }
  return res.json();
}
