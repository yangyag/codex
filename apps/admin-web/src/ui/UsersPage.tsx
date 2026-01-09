import { useQuery, useMutation } from '@tanstack/react-query';
import {
  fetchUsers,
  updateIdentityUserStatus,
  updateMemberStatus,
  UserSummary,
} from '../utils/api';
import { useState } from 'react';

type Props = {
  token: string;
};

export default function UsersPage({ token }: Props) {
  const [page, setPage] = useState(0);
  const [query, setQuery] = useState('');
  const [selected, setSelected] = useState<UserSummary | null>(null);
  const [statusInput, setStatusInput] = useState('ACTIVE');
  const [feedback, setFeedback] = useState<string | null>(null);
  const pageSize = 10;
  const { data, isLoading, isError, isFetching, refetch } = useQuery({
    queryKey: ['users', page, query],
    queryFn: () => fetchUsers(token, page, pageSize, query),
    keepPreviousData: true,
  });
  const mutation = useMutation({
    mutationFn: async (nextStatus: string) => {
      if (!selected) {
        throw new Error('선택된 회원이 없습니다.');
      }
      await updateIdentityUserStatus(token, selected.email, nextStatus);
      return updateMemberStatus(token, selected.id, nextStatus);
    },
    onSuccess: (updated) => {
      setSelected(updated);
      setFeedback('상태가 변경되었습니다.');
      refetch();
    },
    onError: (err) => {
      const msg = err instanceof Error ? err.message : '상태 변경에 실패했습니다.';
      setFeedback(msg);
    },
  });

  const handleSelect = (user: UserSummary) => {
    setSelected(user);
    setStatusInput(user.status);
    setFeedback(null);
  };

  return (
    <div className="card">
      <div className="row">
        <h2>회원 관리</h2>
        <div className="row gap">
          <input
            className="input"
            placeholder="이메일/이름 검색"
            value={query}
            onChange={(e) => {
              setQuery(e.target.value);
              setPage(0);
            }}
          />
          <button className="btn" onClick={() => refetch()}>
            새로고침
          </button>
        </div>
      </div>
      {isLoading && <div>불러오는 중...</div>}
      {isError && <div className="error">목록을 불러오지 못했습니다.</div>}
      {data && (
        <div className="table-wrapper">
          {isFetching && <div className="loading-overlay">불러오는 중...</div>}
          <table className="table">
            <thead>
              <tr>
                <th>이름</th>
                <th>Email</th>
                <th>Role</th>
                <th>Status</th>
                <th>Created</th>
              </tr>
            </thead>
            <tbody>
              {data.content.map((user) => (
                <tr
                  key={user.id}
                  className={selected?.id === user.id ? 'active' : ''}
                  onClick={() => handleSelect(user)}
                >
                  <td>{user.name || '-'}</td>
                  <td>{user.email}</td>
                  <td>{user.role}</td>
                  <td>
                    <span className={`status-pill ${user.status.toLowerCase()}`}>
                      {user.status}
                    </span>
                  </td>
                  <td>{new Date(user.createdAt).toLocaleString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      {data && (
        <div className="pagination">
          <button className="btn" disabled={page === 0} onClick={() => setPage(0)}>
            처음
          </button>
          <button
            className="btn"
            disabled={page === 0}
            onClick={() => setPage((p) => Math.max(0, p - 1))}
          >
            이전
          </button>
          <span className="page-info">
            {data.page + 1} / {Math.max(1, data.totalPages)}
          </span>
          <button
            className="btn"
            disabled={page + 1 >= data.totalPages}
            onClick={() => setPage((p) => p + 1)}
          >
            다음
          </button>
          <button
            className="btn"
            disabled={page + 1 >= data.totalPages}
            onClick={() => setPage(Math.max(0, data.totalPages - 1))}
          >
            끝
          </button>
        </div>
      )}
      {selected && (
        <div className="detail-panel">
          <div className="detail-header">
            <div>
              <div className="detail-title">{selected.name || selected.email}</div>
              <div className="detail-subtitle">{selected.email}</div>
            </div>
            <span className={`status-pill ${selected.status.toLowerCase()}`}>
              {selected.status}
            </span>
          </div>
          <div className="detail-grid">
            <div>
              <label>Role</label>
              <div className="detail-value">{selected.role}</div>
            </div>
            <div>
              <label>가입 일시</label>
              <div className="detail-value">{new Date(selected.createdAt).toLocaleString()}</div>
            </div>
            <div>
              <label>상태 변경</label>
              <div className="detail-row">
                <select
                  value={statusInput}
                  onChange={(e) => setStatusInput(e.target.value)}
                  className="input"
                  disabled={mutation.isPending}
                >
                  <option value="ACTIVE">ACTIVE</option>
                  <option value="BLOCKED">BLOCKED</option>
                </select>
                <button
                  className="btn primary"
                  onClick={() => mutation.mutate(statusInput)}
                  disabled={mutation.isPending}
                >
                  {mutation.isPending ? '변경 중...' : '적용'}
                </button>
              </div>
            </div>
          </div>
          {feedback && <div className="hint">{feedback}</div>}
        </div>
      )}
    </div>
  );
}
