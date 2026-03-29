# HRMS (Spring Cloud + React) - Portfolio Project

## 1. Project Overview
This repository is a personal full-stack HRMS project built to demonstrate practical Java backend and React engineering skills.

The backend is organized as a Spring Cloud multi-module project, and the frontend is a React + TypeScript + Ant Design admin/self-service application.  
Current implementation is functional for core HR operations, attendance, and workflow-driven leave/patch/overtime approvals.

## 2. Why I Built This Project
I built this project as a realistic system-level portfolio rather than isolated CRUD demos. The goal is to show:

- service decomposition with Spring Boot/Spring Cloud modules
- authentication and RBAC in a real menu-driven UI
- domain modeling for HR data (org, employee, attendance, payroll, recruitment, contract)
- workflow template configuration plus runtime approval execution

## 3. My Role
I am the primary builder of this repository. I implemented and integrated:

- backend service modules, SQL schema, and API endpoints
- React admin/ESS pages and route-level permission handling
- JWT authentication flow and role/menu-based access control
- workflow template management and approval runtime for leave/patch/overtime

## 4. Current Implementation Status

### Implemented
- Authentication: `hrms-auth` provides login/refresh/logout/user-info with JWT.
- Authorization: role/menu/permission model based on `sys_user`, `sys_role`, `sys_menu`, `sys_user_role`, `sys_role_menu`.
- Main business APIs: `hrms-system` contains working controllers for users, roles, menus, org/dept/position/rank, employee archive/change, notice, attendance, payroll standard, recruit requirement, contract, profile, and workflow.
- Employee self-service: profile edit, personal attendance query, leave/patch/overtime application submit and withdraw, "my applications" with progress.
- Team management: team members, team attendance summary (role-restricted), department notices.
- Workflow:
  - template list/create/edit/save/publish/duplicate/delete
  - template version history and restore
  - node approver/CC persistence tables
  - runtime instance/task/record generation for leave/patch/overtime
  - todo approval actions (approve/reject/return)
- Frontend: route-based pages for system, org, employee, recruit, payroll, contract, attendance, notice, workflow, ESS, and team modules.

### Partially Implemented
- Microservice split is partial:
  - `hrms-auth`, `hrms-system`, `hrms-gateway` are the main runtime path.
  - `hrms-org`, `hrms-employee`, `hrms-recruit`, `hrms-payroll`, `hrms-contract` have separate service code but are not the default frontend target in local dev.
- Workflow designer UI supports richer graph editing (including branch/parallel node types), but workflow execution is still centered on linear approval progression (`ANY`/`ALL`/`SEQUENTIAL`) with approval/CC runtime nodes.
- Infrastructure Docker compose exists for MySQL/Redis/Nacos/MinIO, but full app-container orchestration is not complete.

### Planned / Roadmap
- move more domain logic out of `hrms-system` into dedicated services
- complete placeholder service modules and unify routing through gateway
- improve test coverage (unit + integration + frontend tests)
- harden deployment packaging and environment consistency

## 5. Architecture Overview
Current codebase architecture is microservice-oriented but runtime-concentrated:

- Frontend (`frontend`) calls `/api/**`
- Vite proxy sends `/api/auth/**` to `hrms-auth` (`8081`)
- other `/api/**` requests go to `hrms-system` (`8082`)
- `hrms-gateway` (`8080`) exists with static routes to auth/system
- MySQL is the primary data store (`database/hrms_full_complete.sql`)

Data model evidence includes 48 tables across system and HR domains, including workflow template/runtime tables:

- `hr_workflow_template`, `hr_workflow_template_node`
- `hr_workflow_template_node_approver`, `hr_workflow_template_node_cc`
- `hr_workflow_instance`, `hr_workflow_task`, `hr_workflow_record`

## 6. Implemented Services / Modules
| Module | Status | Notes |
|---|---|---|
| `backend/hrms-auth` | Implemented | JWT auth APIs and security filter chain |
| `backend/hrms-system` | Implemented (main business service) | Most current business endpoints live here |
| `backend/hrms-gateway` | Implemented (basic) | Route forwarding to auth/system |
| `backend/hrms-org` | Partial | Service code exists; not default frontend target |
| `backend/hrms-employee` | Partial | Service code exists; not default frontend target |
| `backend/hrms-recruit` | Partial | Service code exists; overlap with system APIs |
| `backend/hrms-payroll` | Partial | Service code exists; overlap with system APIs |
| `backend/hrms-contract` | Partial | Service code exists; overlap with system APIs |
| `backend/hrms-attendance` | Scaffold | Module exists, no main Java implementation |
| `backend/hrms-workflow` | Scaffold | Module exists, no main Java implementation |
| `backend/hrms-training` | Scaffold | Module exists, no main Java implementation |
| `backend/hrms-performance` | Scaffold | Module exists, no main Java implementation |
| `backend/hrms-file` | Scaffold | Module exists, no main Java implementation |
| `backend/hrms-message` | Scaffold | Module exists, no main Java implementation |
| `backend/hrms-report` | Scaffold | Module exists, no main Java implementation |
| `backend/hrms-certificate` | Scaffold | Module exists, no main Java implementation |
| `backend/hrms-schedule` | Scaffold | Module exists, no main Java implementation |

## 7. Frontend Scope
Major frontend route groups currently present:

- dashboard and system management (user/role/menu/dict)
- organization management (org/dept/position/rank)
- employee archive and change records
- recruit requirement
- payroll standard
- contract management and expiry warning
- attendance management (shift/record/appeal/statistics)
- notice center and department notice
- workflow center (todo, template list, visual designer, history)
- employee self-service (profile, attendance, leave/patch/overtime, my applications)
- team management (team members, team attendance)

## 8. Tech Stack
### Backend
- Java 17, Spring Boot 3.0.13
- Spring Cloud 2022.0.4, Spring Cloud Alibaba 2022.0.0.0
- Spring Security + JWT (`jjwt`)
- MyBatis-Plus + `NamedParameterJdbcTemplate`
- MySQL, Redis, Nacos, MinIO

### Frontend
- React 18 + TypeScript
- Ant Design 5
- React Router 6, Zustand, Axios
- Vite
- `@xyflow/react` for workflow designer canvas

## 9. Local Setup
### Prerequisites
- JDK 17+
- Maven 3.8+
- Node.js 16+
- Docker (recommended for infra)

### 1) Start infrastructure
```bash
docker compose -f docker-compose-infra.yml up -d
```

### 2) Initialize database
```bash
mysql -uroot -proot < database/hrms_full_complete.sql
```

Optional phase scripts are available in `database/` for staged workflow/employee-team updates.

### 3) Start backend (minimum working set)
```bash
# terminal 1
cd backend/hrms-auth
mvn spring-boot:run

# terminal 2
cd backend/hrms-system
mvn spring-boot:run
```

Optional:
```bash
cd backend/hrms-gateway
mvn spring-boot:run
```

### 4) Start frontend
```bash
cd frontend
npm install
npm run dev
```

### 5) Access
- Frontend: `http://localhost:3000`
- Auth service: `http://localhost:8081`
- System service: `http://localhost:8082`
- Gateway (optional path): `http://localhost:8080`

## 10. Key Engineering Decisions
- **Unified API envelope**: backend endpoints return `Result<T>` and paginated `PageResult<T>` for predictable frontend integration.
- **Menu-driven RBAC**: backend permission data drives frontend menu visibility and route checks (`/menus/current/tree` + client-side route guard).
- **Workflow persistence strategy**: template snapshot JSON is stored for designer replay/versioning, while runtime tables store approver/CC execution data.
- **Pragmatic service delivery**: core flows were first consolidated in `hrms-system` to keep end-to-end features working before full service split.
- **Soft-delete convention**: many tables use `deleted` flags with audit fields for reversible operations and historical safety.

## 11. Current Limitations
- Service decomposition is incomplete; many domains still execute through `hrms-system`.
- Some service configs are environment-specific/inconsistent (for example, mixed localhost and LAN DB/Nacos settings in different modules).
- Automated tests are currently minimal in this repository.
- No confirmed message-queue/event-driven pipeline in current implementation.
- Documentation under `docs/` is partly generic and not always aligned with current code.
- Docker support is partial (infra compose exists; full app containerization is not fully wired).

## 12. Roadmap
- complete module-by-module extraction from `hrms-system` to dedicated services
- unify environment/config conventions across all services
- add repeatable test suites (backend + frontend)
- tighten API and schema documentation to match code continuously
- improve deployment packaging for one-command local startup
