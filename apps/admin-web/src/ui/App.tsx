import { useState } from 'react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './LoginPage';
import UsersPage from './UsersPage';
import BoardsPage from './BoardsPage';
import BoardReaderPage from './BoardReaderPage';

const queryClient = new QueryClient();

export default function App() {
  const [userToken, setUserToken] = useState<string | null>(
    () => localStorage.getItem('userToken') ?? localStorage.getItem('token')
  );
  const [userEmail, setUserEmail] = useState<string | null>(
    () => localStorage.getItem('userEmail') ?? localStorage.getItem('email')
  );
  const [adminToken, setAdminToken] = useState<string | null>(
    () => localStorage.getItem('adminToken') ?? null
  );
  const [adminEmail, setAdminEmail] = useState<string | null>(
    () => localStorage.getItem('adminEmail') ?? null
  );
  const [adminRole, setAdminRole] = useState<string | null>(
    () => localStorage.getItem('adminRole') ?? null
  );
  const [activeAdminTab, setActiveAdminTab] = useState<'users' | 'boards' | 'reader'>('users');

  const handleUserLogin = (nextToken: string, nextEmail: string, _nextRole: string) => {
    localStorage.setItem('userToken', nextToken);
    localStorage.setItem('userEmail', nextEmail);
    setUserToken(nextToken);
    setUserEmail(nextEmail);
  };

  const handleAdminLogin = (nextToken: string, nextEmail: string, nextRole: string) => {
    localStorage.setItem('adminToken', nextToken);
    localStorage.setItem('adminEmail', nextEmail);
    localStorage.setItem('adminRole', nextRole);
    setAdminToken(nextToken);
    setAdminEmail(nextEmail);
    setAdminRole(nextRole);
  };

  const handleUserLogout = () => {
    localStorage.removeItem('userToken');
    localStorage.removeItem('userEmail');
    setUserToken(null);
    setUserEmail(null);
  };

  const handleAdminLogout = () => {
    localStorage.removeItem('adminToken');
    localStorage.removeItem('adminEmail');
    localStorage.removeItem('adminRole');
    setAdminToken(null);
    setAdminEmail(null);
    setAdminRole(null);
  };

  return (
    <QueryClientProvider client={queryClient}>
      <Routes>
        <Route
          path="/login"
          element={
            userToken ? (
              <Navigate to="/" replace />
            ) : (
              <div className="app-shell">
                <main className="app-main">
                  <LoginPage onLogin={handleUserLogin} allowSignup />
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
                {adminEmail && <div className="user">환영합니다, {adminEmail}</div>}
                {adminToken && (
                  <button className="btn" onClick={handleAdminLogout}>
                    로그아웃
                  </button>
                )}
              </header>
              <main className="app-main">
                {adminToken && adminRole === 'ADMIN' ? (
                  <div className="admin-body">
                    <nav className="admin-tabs">
                      <button
                        className={`tab ${activeAdminTab === 'users' ? 'active' : ''}`}
                        onClick={() => setActiveAdminTab('users')}
                      >
                        회원 관리
                      </button>
                      <button
                        className={`tab ${activeAdminTab === 'boards' ? 'active' : ''}`}
                        onClick={() => setActiveAdminTab('boards')}
                      >
                        게시판 관리
                      </button>
                      <button
                        className={`tab ${activeAdminTab === 'reader' ? 'active' : ''}`}
                        onClick={() => setActiveAdminTab('reader')}
                      >
                        게시판 보기
                      </button>
                    </nav>
                    {activeAdminTab === 'users' ? (
                      <UsersPage token={adminToken} />
                    ) : activeAdminTab === 'boards' ? (
                      <BoardsPage token={adminToken} />
                    ) : (
                      <BoardReaderPage token={adminToken} />
                    )}
                  </div>
                ) : (
                  <LoginPage
                    onLogin={handleAdminLogin}
                    defaultEmail="admin"
                    defaultPassword="yangyag1!"
                    allowSignup={false}
                    allowAdminId
                  />
                )}
              </main>
            </div>
          }
        />
        <Route
          path="/"
          element={
            userToken ? (
              <div className="app-shell">
                <header className="app-header">
                  <div className="logo">홈</div>
                  <div className="spacer" />
                  {userEmail && <div className="user">{userEmail}</div>}
                  <button className="btn" onClick={handleUserLogout}>
                    로그아웃
                  </button>
                </header>
                <main className="app-main">
                  <div className="card">
                    <p>로그인이 완료되었습니다. 상단 버튼으로 로그아웃할 수 있습니다.</p>
                  </div>
                </main>
              </div>
            ) : (
              <Navigate to="/login" replace />
            )
          }
        />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </QueryClientProvider>
  );
}
