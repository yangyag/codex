const API_BASE =
  import.meta.env.VITE_API_BASE ||
  (typeof window !== 'undefined'
    ? `${window.location.protocol}//${window.location.hostname}:8081`
    : 'http://127.0.0.1:8081');

const MEMBER_API_BASE =
  import.meta.env.VITE_MEMBER_API_BASE ||
  (typeof window !== 'undefined'
    ? `${window.location.protocol}//${window.location.hostname}:8082`
    : 'http://127.0.0.1:8082');

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
  role: string;
  status: string;
  createdAt: string;
};

export async function login(email: string, password: string): Promise<AuthResponse> {
  const res = await fetch(`${API_BASE}/api/v1/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password })
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
    body: JSON.stringify({ email, password })
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
    size: String(size)
  });
  if (query && query.trim().length > 0) {
    params.set('q', query.trim());
  }
  const res = await fetch(`${MEMBER_API_BASE}/api/v1/members?${params.toString()}`, {
    headers: {
      Authorization: `Bearer ${token}`
    }
  });
  return handleJson(res, '회원 목록을 불러오지 못했습니다.');
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
