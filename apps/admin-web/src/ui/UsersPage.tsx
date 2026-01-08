import { useQuery } from '@tanstack/react-query';
import { fetchUsers } from '../utils/api';
import { useState } from 'react';

type Props = {
  token: string;
};

export default function UsersPage({ token }: Props) {
  const [page, setPage] = useState(0);
  const pageSize = 10;
  const { data, isLoading, isError, refetch } = useQuery({
    queryKey: ['users', page],
    queryFn: () => fetchUsers(token, page, pageSize),
    keepPreviousData: true
  });

  return (
    <div className="card">
      <div className="row">
        <h2>회원 관리</h2>
        <button className="btn" onClick={() => refetch()}>
          새로고침
        </button>
      </div>
      {isLoading && <div>불러오는 중...</div>}
      {isError && <div className="error">목록을 불러오지 못했습니다.</div>}
      {data && (
        <>
          <table className="table">
            <thead>
              <tr>
                <th>Email</th>
                <th>Role</th>
                <th>Status</th>
                <th>Created</th>
              </tr>
            </thead>
            <tbody>
              {data.content.map((user) => (
                <tr key={user.id}>
                  <td>{user.email}</td>
                  <td>{user.role}</td>
                  <td>{user.status}</td>
                  <td>{new Date(user.createdAt).toLocaleString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
          <div className="pagination">
            <button className="btn" disabled={page === 0} onClick={() => setPage((p) => Math.max(0, p - 1))}>
              이전
            </button>
            <span>
              {data.page + 1} / {Math.max(1, data.totalPages)}
            </span>
            <button
              className="btn"
              disabled={page + 1 >= data.totalPages}
              onClick={() => setPage((p) => p + 1)}
            >
              다음
            </button>
          </div>
        </>
      )}
    </div>
  );
}
