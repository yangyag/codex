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
  if (!res.ok) {
    throw new Error('login failed');
  }
  return res.json();
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
  if (!res.ok) {
    throw new Error('signup failed');
  }
  return res.json();
}

export async function fetchUsers(token: string, page: number, size: number): Promise<PageResponse<UserSummary>> {
  const res = await fetch(`${MEMBER_API_BASE}/api/v1/members?page=${page}&size=${size}`, {
    headers: {
      Authorization: `Bearer ${token}`
    }
  });
  if (!res.ok) {
    throw new Error('fetch users failed');
  }
  return res.json();
}
