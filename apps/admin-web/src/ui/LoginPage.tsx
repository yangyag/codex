import { FormEvent, useState } from 'react';
import { login, signup } from '../utils/api';

type Props = {
  onLogin: (token: string, email: string) => void;
  defaultEmail?: string;
  defaultPassword?: string;
};

export default function LoginPage({ onLogin, defaultEmail = '', defaultPassword = '' }: Props) {
  type Mode = 'login' | 'signup';
  const [mode, setMode] = useState<Mode>('login');
  const [email, setEmail] = useState(defaultEmail);
  const [password, setPassword] = useState(defaultPassword);
  const [confirm, setConfirm] = useState(defaultPassword);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const validate = () => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      setError('올바른 이메일 형식을 입력하세요.');
      return false;
    }
    if (password.length < 8) {
      setError('비밀번호는 8자 이상이어야 합니다.');
      return false;
    }
    if (mode === 'signup' && password !== confirm) {
      setError('비밀번호가 일치하지 않습니다.');
      return false;
    }
    return true;
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      if (!validate()) {
        setLoading(false);
        return;
      }
      if (mode === 'signup') {
        await signup(email, password);
      }
      const res = await login(email, password);
      onLogin(res.token, res.email);
    } catch (err) {
      const message = err instanceof Error ? err.message : null;
      setError(message || (mode === 'login' ? '로그인에 실패했습니다. 아이디/비밀번호를 확인하세요.' : '회원가입에 실패했습니다.'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="card">
      <h2>{mode === 'login' ? '로그인' : '회원가입'}</h2>
      <form onSubmit={handleSubmit} className="form">
        <label>
          아이디(이메일)
          <input value={email} onChange={(e) => setEmail(e.target.value)} placeholder="admin@example.com" />
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
        {mode === 'signup' && (
          <label>
            비밀번호 확인
            <input
              type="password"
              value={confirm}
              onChange={(e) => setConfirm(e.target.value)}
              placeholder="••••••••"
            />
          </label>
        )}
        {error && <div className="error">{error}</div>}
        <button className="btn primary" type="submit" disabled={loading}>
          {loading ? '처리 중...' : mode === 'login' ? '로그인' : '회원가입'}
        </button>
        <div className="toggle-row">
          <small className="hint">{mode === 'login' ? '계정이 없으신가요?' : '이미 계정이 있으신가요?'}</small>
          <button
            type="button"
            className="link-button"
            onClick={() => setMode(mode === 'login' ? 'signup' : 'login')}
          >
            {mode === 'login' ? '회원가입' : '로그인'}
          </button>
        </div>
      </form>
    </div>
  );
}
