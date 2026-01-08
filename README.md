# MSA 관리자 프로젝트

Spring Boot + React 기반의 관리자용 MSA 프로젝트입니다.

## 기본 스택
- 백엔드: Spring Boot 3.5.x, Gradle, Java 21
- DB: PostgreSQL 15 (Docker)
- 프런트: Vite 5 + React 18 + TypeScript 5 (Node.js 20 LTS 권장)
- 인프라: Docker Compose (로컬 실행)

## 리포 구조
- `services/identity-service` — 인증/가입
- `services/member-service` — 회원 관리(예정)
- `services/board-service` — 게시판/게시글(예정)
- `gateway/api-gateway` — 라우팅·인증(예정)
- `apps/admin-web` — 관리자 웹 프런트
- `infra` — docker-compose 등 인프라 스크립트
- `adr` — Architecture Decision Record

## 진행 단계
세부 단계와 체크리스트는 `PLAN.md`를 참고하세요.

## 개발 준비
- Docker + Docker Compose 사용 가능해야 합니다.
- JDK 21, Node.js 20 LTS 권장(현재 Node 22도 사용 가능).
- 커밋 메시지는 한글로 작성합니다.

## 현재 상태
- Identity 서비스: 회원가입 API 구현(Flyway 마이그레이션, JPA, 입력 검증, 이메일 중복 처리), 단위/통합 테스트(Testcontainers) 포함.
- `infra/docker-compose.yml`에 Postgres 15 + identity-service 빌드 항목 추가.
- 통합 테스트는 Docker 접근 가능 시 실행되며, Docker가 없으면 자동으로 건너뜁니다.
