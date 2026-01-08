import { useState } from 'react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './LoginPage';
import UsersPage from './UsersPage';

const queryClient = new QueryClient();

export default function App() {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem('token'));
  const [email, setEmail] = useState<string | null>(() => localStorage.getItem('email'));

  const handleLogin = (nextToken: string, nextEmail: string) => {
    localStorage.setItem('token', nextToken);
    localStorage.setItem('email', nextEmail);
    setToken(nextToken);
    setEmail(nextEmail);
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('email');
    setToken(null);
    setEmail(null);
  };

  return (
    <QueryClientProvider client={queryClient}>
      <Routes>
        <Route
          path="/login"
          element={
            token ? (
              <Navigate to="/" replace />
            ) : (
              <div className="app-shell">
                <main className="app-main">
                  <LoginPage onLogin={handleLogin} allowSignup />
                </main>
              </div>
            )
          }
        />
        <Route
          path="/admin"
          element={
            <div className="app-shell">
              <header className="app-header">
                <div className="logo">관리자</div>
                <div className="spacer" />
                {email && <div className="user">환영합니다, {email}</div>}
                {token && (
                  <button className="btn" onClick={handleLogout}>
                    로그아웃
                  </button>
                )}
              </header>
              <main className="app-main">
                {token ? (
                  <UsersPage token={token} />
                ) : (
                  <LoginPage
                    onLogin={handleLogin}
                    defaultEmail="admin"
                    defaultPassword="yangyag1!"
                    allowSignup={false}
                  />
                )}
              </main>
            </div>
          }
        />
        <Route path="*" element={<Navigate to="/admin" replace />} />
      </Routes>
    </QueryClientProvider>
  );
}
