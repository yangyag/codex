import { useQuery, useMutation } from '@tanstack/react-query';
import { useMemo, useState } from 'react';
import { createPost, fetchBoards, fetchPosts, PostSummary } from '../utils/api';

type Props = {
  token: string;
};

export default function BoardReaderPage({ token }: Props) {
  const [boardPage, setBoardPage] = useState(0);
  const [boardQuery, setBoardQuery] = useState('');
  const [selectedBoardId, setSelectedBoardId] = useState<string | null>(null);
  const [postPage, setPostPage] = useState(0);
  const [selectedPost, setSelectedPost] = useState<PostSummary | null>(null);
  const [titleInput, setTitleInput] = useState('');
  const [contentInput, setContentInput] = useState('');
  const [statusFilter, setStatusFilter] = useState('PUBLISHED');
  const [feedback, setFeedback] = useState<string | null>(null);

  const boardsQuery = useQuery({
    queryKey: ['boards-public', boardPage, boardQuery],
    queryFn: () => fetchBoards(token, boardPage, 10, boardQuery, 'PUBLIC', 'ACTIVE'),
    keepPreviousData: true,
  });

  const postsQuery = useQuery({
    queryKey: ['posts-public', selectedBoardId, postPage, statusFilter],
    enabled: !!selectedBoardId,
    queryFn: () => fetchPosts(token, selectedBoardId!, postPage, 10, statusFilter, undefined),
    keepPreviousData: true,
  });

  const createPostMutation = useMutation({
    mutationFn: () => {
      if (!selectedBoardId) throw new Error('게시판을 먼저 선택하세요.');
      return createPost(token, selectedBoardId, titleInput.trim(), contentInput.trim(), 'PUBLISHED');
    },
    onSuccess: (post) => {
      setTitleInput('');
      setContentInput('');
      setSelectedPost(post);
      setFeedback('게시글이 작성되었습니다.');
      postsQuery.refetch();
    },
    onError: (err) => setFeedback(err instanceof Error ? err.message : '작성에 실패했습니다.'),
  });

  const boardList = useMemo(() => boardsQuery.data?.content ?? [], [boardsQuery.data]);
  const postList = useMemo(() => postsQuery.data?.content ?? [], [postsQuery.data]);

  return (
    <div className="card">
      <div className="row">
        <h2>게시판</h2>
        <div className="row gap">
          <input
            className="input"
            placeholder="게시판 검색"
            value={boardQuery}
            onChange={(e) => {
              setBoardQuery(e.target.value);
              setBoardPage(0);
            }}
          />
          <button className="btn" onClick={() => boardsQuery.refetch()}>
            새로고침
          </button>
        </div>
      </div>

      {boardsQuery.isLoading && <div>게시판 불러오는 중...</div>}
      {boardsQuery.isError && <div className="error">게시판을 불러오지 못했습니다.</div>}
      {boardList.length > 0 && (
        <div className="table-wrapper">
          {boardsQuery.isFetching && <div className="loading-overlay">불러오는 중...</div>}
          <table className="table">
            <thead>
              <tr>
                <th>이름</th>
                <th>공개범위</th>
                <th>상태</th>
                <th>생성일</th>
              </tr>
            </thead>
            <tbody>
              {boardList.map((board) => (
                <tr
                  key={board.id}
                  className={selectedBoardId === board.id ? 'active' : ''}
                  onClick={() => {
                    setSelectedBoardId(board.id);
                    setPostPage(0);
                    setSelectedPost(null);
                    setFeedback(null);
                  }}
                >
                  <td>{board.name}</td>
                  <td>{board.visibility}</td>
                  <td>{board.status}</td>
                  <td>{new Date(board.createdAt).toLocaleString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {boardsQuery.data && (
        <div className="pagination">
          <button className="btn" disabled={boardPage === 0} onClick={() => setBoardPage(0)}>
            처음
          </button>
          <button
            className="btn"
            disabled={boardPage === 0}
            onClick={() => setBoardPage((p) => Math.max(0, p - 1))}
          >
            이전
          </button>
          <span className="page-info">
            {boardsQuery.data.page + 1} / {Math.max(1, boardsQuery.data.totalPages)}
          </span>
          <button
            className="btn"
            disabled={boardPage + 1 >= boardsQuery.data.totalPages}
            onClick={() => setBoardPage((p) => p + 1)}
          >
            다음
          </button>
          <button
            className="btn"
            disabled={boardPage + 1 >= boardsQuery.data.totalPages}
            onClick={() => setBoardPage(Math.max(0, boardsQuery.data.totalPages - 1))}
          >
            끝
          </button>
        </div>
      )}

      {selectedBoardId && (
        <div className="card" style={{ marginTop: '1rem' }}>
          <div className="row">
            <h3>게시글</h3>
            <div className="row gap">
              <select
                className="input"
                value={statusFilter}
                onChange={(e) => {
                  setStatusFilter(e.target.value);
                  setPostPage(0);
                }}
              >
                <option value="">상태 전체</option>
                <option value="PUBLISHED">PUBLISHED</option>
                <option value="DRAFT">DRAFT</option>
                <option value="ARCHIVED">ARCHIVED</option>
              </select>
              <button className="btn" onClick={() => postsQuery.refetch()}>
                새로고침
              </button>
            </div>
          </div>

          <div className="row gap">
            <input
              className="input"
              placeholder="제목"
              value={titleInput}
              onChange={(e) => setTitleInput(e.target.value)}
            />
            <input
              className="input"
              placeholder="내용"
              value={contentInput}
              onChange={(e) => setContentInput(e.target.value)}
            />
            <button
              className="btn primary"
              disabled={!titleInput.trim() || !contentInput.trim() || createPostMutation.isPending}
              onClick={() => createPostMutation.mutate()}
            >
              {createPostMutation.isPending ? '작성 중...' : '게시글 작성'}
            </button>
          </div>

          {postsQuery.isLoading && <div>게시글 불러오는 중...</div>}
          {postsQuery.isError && <div className="error">게시글을 불러오지 못했습니다.</div>}
          {postList.length > 0 && (
            <div className="table-wrapper">
              {postsQuery.isFetching && <div className="loading-overlay">불러오는 중...</div>}
              <table className="table">
                <thead>
                  <tr>
                    <th>제목</th>
                    <th>작성자</th>
                    <th>상태</th>
                    <th>작성일</th>
                  </tr>
                </thead>
                <tbody>
                  {postList.map((post) => (
                    <tr
                      key={post.id}
                      className={selectedPost?.id === post.id ? 'active' : ''}
                      onClick={() => setSelectedPost(post)}
                    >
                      <td>{post.title}</td>
                      <td>{post.authorEmail}</td>
                      <td>{post.status}</td>
                      <td>{new Date(post.createdAt).toLocaleString()}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          {postsQuery.data && (
            <div className="pagination">
              <button className="btn" disabled={postPage === 0} onClick={() => setPostPage(0)}>
                처음
              </button>
              <button
                className="btn"
                disabled={postPage === 0}
                onClick={() => setPostPage((p) => Math.max(0, p - 1))}
              >
                이전
              </button>
              <span className="page-info">
                {postsQuery.data.page + 1} / {Math.max(1, postsQuery.data.totalPages)}
              </span>
              <button
                className="btn"
                disabled={postPage + 1 >= postsQuery.data.totalPages}
                onClick={() => setPostPage((p) => p + 1)}
              >
                다음
              </button>
              <button
                className="btn"
                disabled={postPage + 1 >= postsQuery.data.totalPages}
                onClick={() => setPostPage(Math.max(0, postsQuery.data.totalPages - 1))}
              >
                끝
              </button>
            </div>
          )}

          {selectedPost && (
            <div className="detail-panel">
              <div className="detail-title">{selectedPost.title}</div>
              <div className="detail-subtitle">작성자: {selectedPost.authorEmail}</div>
              <div className="detail-value" style={{ whiteSpace: 'pre-wrap' }}>
                {selectedPost.content}
              </div>
            </div>
          )}

          {feedback && <div className="hint">{feedback}</div>}
        </div>
      )}
    </div>
  );
}
