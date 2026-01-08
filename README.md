# MSA 관리자 프로젝트

Spring Boot + React 기반의 관리자용 MSA 프로젝트입니다.

## 기본 스택
- 백엔드: Spring Boot 3.2.x, Gradle, Java 21
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
