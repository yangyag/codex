# AGENTS 지침

- 아키텍처 스냅샷: Spring Boot 3.2.x (Gradle, Java 21), PostgreSQL 15(도커), Vite + React + TypeScript(노드 20 LTS 권장), Docker Compose 기반 로컬 실행.
- 리포 구조 계획: `services/identity-service`, `services/member-service`, `services/board-service`, `gateway/api-gateway`, `apps/admin-web`, `infra/docker-compose.yml`, `adr/`.
- 인증/인증 스택: JWT 기반, Spring Security + Spring Cloud Gateway 라우팅(차후 추가).
- 데이터: Flyway 마이그레이션, 서비스별 DB/스키마 분리 방향.
- Git 규칙: 커밋 메시지는 반드시 한글로 작성.
