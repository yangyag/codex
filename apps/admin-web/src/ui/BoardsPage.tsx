import { useMutation, useQuery } from '@tanstack/react-query';
import {
  archivePost,
  BoardSummary,
  createBoard,
  createPost,
  fetchBoards,
  fetchPosts,
  PostSummary,
  updateBoard,
  updatePost,
} from '../utils/api';
import { useMemo, useState } from 'react';

type Props = {
  token: string;
};

export default function BoardsPage({ token }: Props) {
  const [boardPage, setBoardPage] = useState(0);
  const [boardQuery, setBoardQuery] = useState('');
  const [visibility, setVisibility] = useState('');
  const [boardStatus, setBoardStatus] = useState('');
  const [selectedBoard, setSelectedBoard] = useState<BoardSummary | null>(null);
  const [newBoardName, setNewBoardName] = useState('');
  const [newBoardVisibility, setNewBoardVisibility] = useState('PUBLIC');
  const [boardFeedback, setBoardFeedback] = useState<string | null>(null);

  const [postPage, setPostPage] = useState(0);
  const [postStatus, setPostStatus] = useState('');
  const [postAuthor, setPostAuthor] = useState('');
  const [selectedPost, setSelectedPost] = useState<PostSummary | null>(null);
  const [postTitle, setPostTitle] = useState('');
  const [postContent, setPostContent] = useState('');
  const [postStatusInput, setPostStatusInput] = useState('DRAFT');
  const [postFeedback, setPostFeedback] = useState<string | null>(null);

  const boardsQuery = useQuery({
    queryKey: ['boards', boardPage, boardQuery, visibility, boardStatus],
    queryFn: () =>
      fetchBoards(token, boardPage, 10, boardQuery, visibility || undefined, boardStatus || undefined),
    keepPreviousData: true,
  });

  const postsQuery = useQuery({
    queryKey: ['posts', selectedBoard?.id, postPage, postStatus, postAuthor],
    enabled: !!selectedBoard,
    queryFn: () =>
      fetchPosts(
        token,
        selectedBoard!.id,
        postPage,
        10,
        postStatus || undefined,
        postAuthor || undefined
      ),
    keepPreviousData: true,
  });

  const createBoardMutation = useMutation({
    mutationFn: () => createBoard(token, newBoardName.trim(), newBoardVisibility),
    onSuccess: () => {
      setNewBoardName('');
      setBoardFeedback('게시판이 생성되었습니다.');
      boardsQuery.refetch();
    },
    onError: (err) => setBoardFeedback(err instanceof Error ? err.message : '생성에 실패했습니다.'),
  });

  const updateBoardMutation = useMutation({
    mutationFn: (payload: { status?: string }) => {
      if (!selectedBoard) throw new Error('게시판을 선택하세요.');
      return updateBoard(token, selectedBoard.id, payload);
    },
    onSuccess: (updated) => {
      setSelectedBoard(updated);
      setBoardFeedback('게시판이 수정되었습니다.');
      boardsQuery.refetch();
    },
    onError: (err) => setBoardFeedback(err instanceof Error ? err.message : '수정에 실패했습니다.'),
  });

  const createPostMutation = useMutation({
    mutationFn: () => {
      if (!selectedBoard) throw new Error('게시판을 먼저 선택하세요.');
      return createPost(token, selectedBoard.id, postTitle.trim(), postContent.trim(), postStatusInput);
    },
    onSuccess: (post) => {
      setSelectedPost(post);
      setPostTitle('');
      setPostContent('');
      setPostFeedback('게시글이 작성되었습니다.');
      postsQuery.refetch();
    },
    onError: (err) => setPostFeedback(err instanceof Error ? err.message : '작성에 실패했습니다.'),
  });

  const updatePostMutation = useMutation({
    mutationFn: (payload: { status?: string }) => {
      if (!selectedBoard || !selectedPost) throw new Error('게시글을 선택하세요.');
      return updatePost(token, selectedBoard.id, selectedPost.id, payload);
    },
    onSuccess: (post) => {
      setSelectedPost(post);
      setPostFeedback('게시글이 수정되었습니다.');
      postsQuery.refetch();
    },
    onError: (err) => setPostFeedback(err instanceof Error ? err.message : '수정에 실패했습니다.'),
  });

  const archivePostMutation = useMutation({
    mutationFn: () => {
      if (!selectedBoard || !selectedPost) throw new Error('게시글을 선택하세요.');
      return archivePost(token, selectedBoard.id, selectedPost.id);
    },
    onSuccess: () => {
      setPostFeedback('게시글이 삭제(ARCHIVED)되었습니다.');
      postsQuery.refetch();
    },
    onError: (err) => setPostFeedback(err instanceof Error ? err.message : '삭제에 실패했습니다.'),
  });

  const boardList = useMemo(() => boardsQuery.data?.content ?? [], [boardsQuery.data]);
  const postList = useMemo(() => postsQuery.data?.content ?? [], [postsQuery.data]);

  return (
    <div className="card">
      <div className="row">
        <h2>게시판 관리</h2>
        <div className="row gap">
          <input
            className="input"
            placeholder="게시판 이름 검색"
            value={boardQuery}
            onChange={(e) => {
              setBoardQuery(e.target.value);
              setBoardPage(0);
            }}
          />
          <select
            className="input"
            value={visibility}
            onChange={(e) => {
              setVisibility(e.target.value);
              setBoardPage(0);
            }}
          >
            <option value="">공개범위 전체</option>
            <option value="PUBLIC">PUBLIC</option>
            <option value="PRIVATE">PRIVATE</option>
          </select>
          <select
            className="input"
            value={boardStatus}
            onChange={(e) => {
              setBoardStatus(e.target.value);
              setBoardPage(0);
            }}
          >
            <option value="">상태 전체</option>
            <option value="ACTIVE">ACTIVE</option>
            <option value="INACTIVE">INACTIVE</option>
          </select>
          <button className="btn" onClick={() => boardsQuery.refetch()}>
            새로고침
          </button>
        </div>
      </div>

      <div className="row gap">
        <input
          className="input"
          placeholder="새 게시판 이름"
          value={newBoardName}
          onChange={(e) => setNewBoardName(e.target.value)}
        />
        <select
          className="input"
          value={newBoardVisibility}
          onChange={(e) => setNewBoardVisibility(e.target.value)}
        >
          <option value="PUBLIC">PUBLIC</option>
          <option value="PRIVATE">PRIVATE</option>
        </select>
        <button
          className="btn primary"
          disabled={!newBoardName.trim() || createBoardMutation.isPending}
          onClick={() => createBoardMutation.mutate()}
        >
          {createBoardMutation.isPending ? '생성 중...' : '게시판 생성'}
        </button>
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
                  className={selectedBoard?.id === board.id ? 'active' : ''}
                  onClick={() => {
                    setSelectedBoard(board);
                    setPostPage(0);
                    setSelectedPost(null);
                    setPostFeedback(null);
                  }}
                >
                  <td>{board.name}</td>
                  <td>{board.visibility}</td>
                  <td>
                    <span className={`status-pill ${board.status.toLowerCase()}`}>{board.status}</span>
                  </td>
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

      {selectedBoard && (
        <div className="detail-panel">
          <div className="detail-header">
            <div>
              <div className="detail-title">{selectedBoard.name}</div>
              <div className="detail-subtitle">공개: {selectedBoard.visibility}</div>
            </div>
            <span className={`status-pill ${selectedBoard.status.toLowerCase()}`}>
              {selectedBoard.status}
            </span>
          </div>
          <div className="detail-grid">
            <div>
              <label>상태 변경</label>
              <div className="detail-row">
                <select
                  className="input"
                  value={selectedBoard.status}
                  onChange={(e) => updateBoardMutation.mutate({ status: e.target.value })}
                  disabled={updateBoardMutation.isPending}
                >
                  <option value="ACTIVE">ACTIVE</option>
                  <option value="INACTIVE">INACTIVE</option>
                </select>
              </div>
            </div>
          </div>
          {boardFeedback && <div className="hint">{boardFeedback}</div>}
        </div>
      )}

      {selectedBoard && (
        <div className="card" style={{ marginTop: '1rem' }}>
          <div className="row">
            <h3>게시글</h3>
            <div className="row gap">
              <select
                className="input"
                value={postStatus}
                onChange={(e) => {
                  setPostStatus(e.target.value);
                  setPostPage(0);
                }}
              >
                <option value="">상태 전체</option>
                <option value="DRAFT">DRAFT</option>
                <option value="PUBLISHED">PUBLISHED</option>
                <option value="ARCHIVED">ARCHIVED</option>
              </select>
              <input
                className="input"
                placeholder="작성자 이메일"
                value={postAuthor}
                onChange={(e) => {
                  setPostAuthor(e.target.value);
                  setPostPage(0);
                }}
              />
              <button className="btn" onClick={() => postsQuery.refetch()}>
                새로고침
              </button>
            </div>
          </div>

          <div className="row gap">
            <input
              className="input"
              placeholder="제목"
              value={postTitle}
              onChange={(e) => setPostTitle(e.target.value)}
            />
            <input
              className="input"
              placeholder="본문"
              value={postContent}
              onChange={(e) => setPostContent(e.target.value)}
            />
            <select
              className="input"
              value={postStatusInput}
              onChange={(e) => setPostStatusInput(e.target.value)}
            >
              <option value="DRAFT">DRAFT</option>
              <option value="PUBLISHED">PUBLISHED</option>
            </select>
            <button
              className="btn primary"
              disabled={!postTitle.trim() || !postContent.trim() || createPostMutation.isPending}
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
                    <th>생성일</th>
                  </tr>
                </thead>
                <tbody>
                  {postList.map((post) => (
                    <tr
                      key={post.id}
                      className={selectedPost?.id === post.id ? 'active' : ''}
                      onClick={() => {
                        setSelectedPost(post);
                        setPostStatusInput(post.status);
                        setPostTitle(post.title);
                        setPostContent(post.content);
                        setPostFeedback(null);
                      }}
                    >
                      <td>{post.title}</td>
                      <td>{post.authorEmail}</td>
                      <td>
                        <span className={`status-pill ${post.status.toLowerCase()}`}>{post.status}</span>
                      </td>
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
              <div className="detail-header">
                <div>
                  <div className="detail-title">{selectedPost.title}</div>
                  <div className="detail-subtitle">작성자: {selectedPost.authorEmail}</div>
                </div>
                <span className={`status-pill ${selectedPost.status.toLowerCase()}`}>
                  {selectedPost.status}
                </span>
              </div>
              <div className="detail-grid">
                <div>
                  <label>제목</label>
                  <input
                    className="input"
                    value={postTitle}
                    onChange={(e) => setPostTitle(e.target.value)}
                  />
                </div>
                <div>
                  <label>본문</label>
                  <textarea
                    className="input"
                    value={postContent}
                    onChange={(e) => setPostContent(e.target.value)}
                    rows={3}
                  />
                </div>
                <div>
                  <label>상태</label>
                  <select
                    className="input"
                    value={postStatusInput}
                    onChange={(e) => setPostStatusInput(e.target.value)}
                  >
                    <option value="DRAFT">DRAFT</option>
                    <option value="PUBLISHED">PUBLISHED</option>
                    <option value="ARCHIVED">ARCHIVED</option>
                  </select>
                </div>
              </div>
              <div className="row gap">
                <button
                  className="btn primary"
                  disabled={updatePostMutation.isPending}
                  onClick={() => updatePostMutation.mutate({ status: postStatusInput })}
                >
                  {updatePostMutation.isPending ? '저장 중...' : '저장'}
                </button>
                <button
                  className="btn"
                  disabled={archivePostMutation.isPending}
                  onClick={() => archivePostMutation.mutate()}
                >
                  {archivePostMutation.isPending ? '처리 중...' : '삭제(ARCHIVE)'}
                </button>
              </div>
              {postFeedback && <div className="hint">{postFeedback}</div>}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
