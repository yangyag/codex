# AGENTS 지침

- 아키텍처 스냅샷: Spring Boot 3.5.x (Gradle, Java 21), PostgreSQL 15(도커), Vite + React + TypeScript(노드 20 LTS 권장), Docker Compose 기반 로컬 실행.
- 리포 구조 계획: `services/identity-service`, `services/member-service`, `services/board-service`, `gateway/api-gateway`, `apps/admin-web`, `infra/docker-compose.yml`, `adr/`.
- 인증/인증 스택: JWT 기반, Spring Security + Spring Cloud Gateway 라우팅(차후 추가).
- 데이터: Flyway 마이그레이션, 서비스별 DB/스키마 분리 방향.
- Git 규칙: 커밋 메시지는 반드시 한글로 작성.
- 테스트 정책: JUnit 5 기반 TDD를 기본으로 하고, 통합 테스트는 Testcontainers(PostgreSQL) 사용. 가능하면 레드-그린-리팩터 순서로 진행하며, 목/스텁은 최소화.

## 현재 아키텍처/런타임 구성
- 인프라: `infra/docker-compose.yml`로 기동.
  - postgres: 포트 5432, 기본 DB/계정 `msa`/`msa-password`.
  - identity-service(8081): 0.0.0.0 바인드, JWT 인증, admin 시드 계정 `admin`/`yangyag1!`, 사용자 100명 시드, `/api/v1/auth/login`, `/api/v1/auth/signup`(이메일 중복 검사+해싱 후 member-service 동기화).
  - member-service(8082): 0.0.0.0 바인드, 멤버 100명 시드, 목록/검색/상태 변경 API(`GET /api/v1/members`, `PATCH /api/v1/members/{id}/status`), 외부 동기화(`POST /api/v1/members/sync`), JWT ADMIN 요구.
  - admin-web(8080): nginx로 `/admin`(관리자 로그인 전용)과 `/login`(로그인/회원가입) 경로 서비스. `/admin` 로그인 후 멤버 목록 검색/페이징(10개/페이지, 처음/끝 이동), `/login` 로그인 성공 시 빈 화면(추후 확장). 이메일·비밀번호 검증 포함. API_BASE는 브라우저 호스트 기준 8081/8082가 기본.
- 접속 경로: 브라우저 `http://127.0.0.1:8080/admin` → 로그인 `admin`/`yangyag1!` → 멤버 목록 확인. 일반 로그인/회원가입 진입점은 `http://127.0.0.1:8080/login`.
- 남은 작업 큰 줄기: Gateway 도입(라우팅/CORS 통합), admin-web 회원가입 UI/테스트, member 상세/상태 토글 UI, board 서비스 추가.
