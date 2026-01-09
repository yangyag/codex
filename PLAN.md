# 프로젝트 계획 (모노레포)

MSA 관리자 프로젝트의 단계별 계획을 기록합니다. 진행 상황에 따라 체크박스를 업데이트하세요.

## 기본 스택/버전
- JDK: 21 (LTS)
- Spring Boot: 3.5.x (Gradle 8.x)
- DB: PostgreSQL 15 (도커 이미지 `postgres:15`)
- 프런트: Vite 5 + React 18 + TypeScript 5, Node.js 20 LTS (현재 로컬: 22.18.0도 사용 가능)
- 테스트: JUnit 5, Testcontainers
- 마이그레이션: Flyway

## 접속/시나리오 목표
- 모든 서비스는 `0.0.0.0`로 바인드.
- Windows 브라우저에서 `http://127.0.0.1:8080/admin` 접속 시 관리자 페이지 노출.
- 기본 관리자 계정: `admin` / `yangyag1!`.
- 관리자 페이지 초기 기능: 회원 관리 메뉴 → 등록된 사용자 목록 표시. 테스트용 사용자 100개 시드, 페이지당 10개 표시(페이징).

## 단계 0 - 설치/환경 준비
- [x] 필수: Docker 엔진 + Docker Compose 플러그인(또는 Docker Desktop) 설치 및 동작 확인. (확인: Docker 29.1.3, Compose v5.0.1)
- [x] JDK 21 설치 후 `java -version` 확인. (확인: OpenJDK 21.0.9)
- [x] Node.js 20 LTS 설치 후 `node -v`/`npm -v` 확인. (현재 22.18.0 설치 완료; 필요 시 nvm으로 20 전환 가능)
- 참고: Gradle Wrapper는 리포에서 제공 예정(Stage 1에서 처리).
- (선택) `psql` 클라이언트, `make`/`just` 등 작업 자동화 도구 설치.

## 단계 1 - 베이스 세팅
- [x] 리포 구조: `services/identity-service`, `services/member-service`, `services/board-service`, `gateway/api-gateway`, `apps/admin-web`, `infra/docker-compose.yml`, 결정 기록용 `adr/`.
- [x] 공통 도구: `.editorconfig`, `README.md`, ADR 템플릿 (Gradle wrapper는 서비스 스캐폴드 시 추가).
- [x] Dev/CI 기본: 웹용 Prettier/ESLint, 기본 CI 워크플로(빌드/테스트), git hooks(선택).
- [x] Docker Compose 베이스: Postgres 서비스, 공유 네트워크, 환경 변수 템플릿, 네임드 볼륨.

## 단계 2 - Identity 스켈레톤
- [x] Spring Boot 3 스캐폴드(Web, Validation, Data JPA, Flyway, PostgreSQL driver).
- [x] 도메인: `users` 집합(id, 이메일 고유, BCrypt 비밀번호 해시, role, status, 생성/수정 시각).
- [x] Flyway 마이그레이션으로 `users` 테이블 생성.
- [x] 회원가입 엔드포인트(요청 검증, 이메일 중복 방어, 비밀번호 해싱).
- [x] 테스트: 가입 서비스 단위 테스트, Testcontainers 통합 테스트(마이그레이션+리포지토리+가입 흐름).
- [x] Dockerfile 작성 및 compose 서비스 엔트리.

## 단계 3 - 인증 + 게이트웨이
- [x] 로그인 엔드포인트(이메일+비밀번호, JWT 액세스 토큰).
- [x] JWT 프로바이더(HS256 키, 만료 설정), 인증 오류 모델.
- [x] 시큐리티 설정: 패스워드 인코더, 인증 매니저, 보호 경로용 Bearer 필터.
- [x] Spring Cloud Gateway 스캐폴드: identity(및 향후 서비스) 라우팅, JWT 검증 필터, CORS 규칙.
- [x] Compose 업데이트: gateway ↔ identity ↔ Postgres 연동; 스모크 테스트 스크립트 또는 Postman 컬렉션. *(identity + admin-web + postgres 구성 완료)*
- [x] 기본 관리자 계정(admin/yangyag1!) 시드.
- [x] 서비스 바인드 주소를 `0.0.0.0`으로 노출.

## 단계 4 - Admin Web (초기)
- [x] Vite + React + TypeScript 스캐폴드, TanStack Query, React Router.
- [x] 인증 인지 API 클라이언트(fetch), 토큰 저장(localStorage).
- [x] 페이지: 회원가입, 로그인; 성공 시 토큰 저장 후 보호 영역으로 리다이렉트.
- [x] Admin 레이아웃 셸과 보호 라우트 가드(로그인 상태 전환).
- [x] 기본 테스트: 컴포넌트 테스트/린트. *(Vitest 단위 테스트 + lint/format 스크립트)*
- [x] 관리자 페이지 경로: `/admin` 노출, 서버 바인드 `0.0.0.0`.
- [x] 회원 관리 메뉴: 사용자 목록 조회(페이지당 10개), 100명 테스트 데이터 시드, 목록 확인 가능, 상세/상태 변경 UI 추가.
- [x] `http://127.0.0.1:8080/admin` 접속 스모크 확인(수동, 현재 compose 직결).
- [x] API BASE 자동 설정: 브라우저 호스트 기반(`hostname:8081`) 기본값 적용.
- [x] 회원가입 페이지 추가(관리자 계정 외 신규 가입) 및 성공 후 로그인/리다이렉트 플로우.
- [x] 회원가입 시 아이디(이메일) 중복 검사 및 이메일 형식 검증 반영.
- [x] 회원가입 시 member-service로 사용자 동기화 연계.
- [x] 라우팅 분리: `/admin`은 관리자 로그인+회원 관리 전용, `/login`은 로그인/회원가입 전용(로그인 성공 시 빈 화면/후속 페이지).
- [x] 코드 리팩터: 서비스 인터페이스/구현체 분리, 도메인 생성 팩토리 도입, 핵심 유스케이스를 커맨드/핸들러 구조로 정리(Identity/Member 적용 완료).

## 단계 5 - Member 서비스 + Admin UI
- [x] `member-service` 스캐폴드(Web, Validation, Data JPA, Flyway) + Postgres 공유 DB, 별도 Flyway 히스토리 테이블.
- [x] 도메인: 멤버 프로필/상태; 멤버 테이블 마이그레이션.
- [x] API: 멤버 목록/검색, 상태 업데이트, 외부 동기화 엔드포인트(`/api/v1/members/sync`).
- [ ] 보안: 게이트웨이 라우팅 + 역할 검증; gateway ↔ member-service 계약 테스트. *(게이트웨이 미도입, JWT 필터로 ADMIN 요구)*
- [x] Admin Web: 멤버 목록(검색/페이지네이션), 페이징 처음/끝 이동, 상세/상태 토글.

## 추가 진행 사항
- Identity 서비스: BLOCKED 상태 사용자는 로그인 차단.
- Admin Web: 상태 변경 시 identity/member 서비스 동기화.

## 단계 6 - Board 서비스 + Admin UI
- [ ] `board-service` 스캐폴드(Web, Validation, Data JPA, Flyway) + 별도 DB/스키마.
- [ ] 도메인: 게시판(이름, 공개 범위), 게시글(board_id, author_id, 제목, 본문, 상태, 타임스탬프).
- [ ] API: 게시판/게시글 CRUD, 페이지네이션/필터링, 소유/역할 검사.
- [ ] Admin Web: 게시판 관리, 게시글 목록, 작성/수정.

## 단계 7 - 품질 + 운영
- [ ] 가시성: 구조화 로깅, 요청 ID, Micrometer 메트릭, OpenTelemetry 트레이싱 훅.
- [ ] 오류 모델: 일관된 문제 응답, 글로벌 예외 핸들러.
- [ ] 보안 강화: 보안 헤더, 게이트웨이 레이트 리밋, 환경 변수 기반 시크릿 관리.
- [ ] CI/CD: 빌드/테스트, 도커 이미지 빌드/푸시, compose/k8s 스모크 잡; SBOM/스캔(선택).
- [ ] k8s 준비: minikube 매니페스트 또는 Helm 차트(서비스, 게이트웨이, Postgres).
- [ ] 런북: 기동/종료, 스모크 체크리스트, DB 백업/복구.
