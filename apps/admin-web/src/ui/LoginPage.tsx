import { FormEvent, useState } from 'react';
import { login } from '../utils/api';

type Props = {
  onLogin: (token: string, email: string) => void;
  defaultEmail?: string;
  defaultPassword?: string;
};

export default function LoginPage({ onLogin, defaultEmail = '', defaultPassword = '' }: Props) {
  const [email, setEmail] = useState(defaultEmail);
  const [password, setPassword] = useState(defaultPassword);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const res = await login(email, password);
      onLogin(res.token, res.email);
    } catch (err) {
      setError('로그인에 실패했습니다. 아이디/비밀번호를 확인하세요.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="card">
      <h2>관리자 로그인</h2>
      <form onSubmit={handleSubmit} className="form">
        <label>
          아이디
          <input value={email} onChange={(e) => setEmail(e.target.value)} placeholder="admin" />
        </label>
        <label>
          비밀번호
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="••••••••"
          />
        </label>
        {error && <div className="error">{error}</div>}
        <button className="btn primary" type="submit" disabled={loading}>
          {loading ? '로그인 중...' : '로그인'}
        </button>
      </form>
    </div>
  );
}
