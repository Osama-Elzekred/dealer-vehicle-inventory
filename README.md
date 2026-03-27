# Dealer & Vehicle Inventory Module

A multi-tenant inventory management system built with Spring Boot 3.2.x, implementing clean architecture principles with row-level tenant isolation.

## Quick Start

### Prerequisites
- Java 21+
- Maven 3.9+

### Run Application
```bash
mvn spring-boot:run
```

- **API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs
- **H2 Console**: http://localhost:8080/h2-console (JDBC: `jdbc:h2:mem:inventorydb`, User: `sa`)

## ✅ Requirements Completion

| Requirement | Status | Details |
|---|---|---|
| Data Model | ✅ | Dealer & Vehicle entities with all required fields |
| Multi-Tenancy | ✅ | Row-level tenant isolation with `tenant_id` column |
| X-Tenant-Id Header | ✅ | Required for all endpoints, returns **400** if missing |
| Cross-Tenant Access | ✅ | Blocked with **403** response |
| CRUD Endpoints | ✅ | All dealer & vehicle operations implemented |
| Pagination & Sorting | ✅ | Spring Data Pageable support |
| Vehicle Filters | ✅ | model, status, priceMin, priceMax, subscription |
| Subscription Filter | ✅ | `?subscription=PREMIUM` returns tenant-scoped PREMIUM dealer vehicles |
| Admin Endpoint | ✅ | `/admin/dealers/countBySubscription` requires GLOBAL_ADMIN |
| Admin Scoping | ✅ | System-wide counts OR per-tenant with `scope=tenant` parameter |

### Acceptance Checks
- ✅ Missing X-Tenant-Id → **400 Bad Request**
- ✅ Cross-tenant access → **403 Forbidden**
- ✅ Subscription filter → Tenant-scoped, PREMIUM dealers only
- ✅ Admin authorization → GLOBAL_ADMIN role required

## Authentication

Generate JWT tokens via `/auth/login`:

```bash
# Tenant user
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "user1", "tenantId": "tenant-1", "role": "TENANT_USER"}'

# Admin
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "tenantId": "system", "role": "GLOBAL_ADMIN"}'
```

### Required Headers
```bash
-H "Authorization: Bearer <jwt-token>"
-H "X-Tenant-Id: tenant-1"
```

**Note**: Both headers must match, otherwise request returns **403 Forbidden**.

## API Endpoints

### Dealers
- `POST /dealers` - Create dealer
- `GET /dealers/{id}` - Get dealer by ID
- `GET /dealers` - List dealers (pagination/sort)
- `PATCH /dealers/{id}` - Update dealer
- `DELETE /dealers/{id}` - Delete dealer

### Vehicles
- `POST /vehicles` - Create vehicle
- `GET /vehicles/{id}` - Get vehicle by ID
- `GET /vehicles` - List vehicles with filters (pagination/sort)
- `PATCH /vehicles/{id}` - Update vehicle
- `DELETE /vehicles/{id}` - Delete vehicle

**Filters**: `model`, `status`, `priceMin`, `priceMax`, `subscription`

Example: `GET /vehicles?subscription=PREMIUM&status=AVAILABLE&priceMin=50000`

### Admin (GLOBAL_ADMIN only)
- `GET /admin/dealers/countBySubscription` - Count dealers by subscription
  - System-wide by default
  - Add `?scope=tenant` + `X-Tenant-Id` header for per-tenant counts

## Secure Multi-Tenant Architecture

**Dual-Layer Protection:**
1. JWT token contains tenant ID (prevents tampering)
2. X-Tenant-Id header must match JWT tenant
3. All queries automatically scoped by tenant

**Security Features:**
- ✅ Row-level tenant isolation
- ✅ Cross-tenant access prevention (403)
- ✅ Role-based access control (TENANT_USER, GLOBAL_ADMIN)
- ✅ Automatic audit timestamps (created_at, updated_at)
- ✅ Stateless JWT authentication

## Tech Stack

- Java 21
- Spring Boot 3.2.x
- Spring Security + JWT
- Spring Data JPA + H2
- SpringDoc OpenAPI
- Lombok, MapStruct

## Project Structure

```
src/main/java/com/inventory/
├── common/              # Shared infrastructure
│   ├── config/         # Security, JPA, OpenAPI
│   ├── exception/      # Error handling
│   └── security/       # JWT, filters, tenant context
├── auth/               # Authentication module
├── dealer/             # Dealer CRUD + admin analytics
└── vehicle/            # Vehicle CRUD + advanced filters
```

## Testing

Run all tests:
```bash
mvn test
```

Run acceptance tests only:
```bash
mvn -Dtest=InventoryAcceptanceIntegrationTest test
```

**Test Coverage:**
- ✅ 16 total tests (all passing)
- ✅ 8 acceptance integration tests
- ✅ Missing X-Tenant-Id header validation
- ✅ Cross-tenant access blocking
- ✅ Subscription filtering
- ✅ Admin authorization
- ✅ Full CRUD operations
- ✅ Pagination & sorting

See `SUBMISSION_TEST_EVIDENCE.md` for detailed test results.

## Clean Architecture

**Controller** → Request handling & validation
**Service** → Business logic & tenant enforcement
**Repository** → Data access with tenant scoping
**Entity** → JPA models with audit support
**Security** → JWT + X-Tenant-Id validation

## Submission

**To**: mytask@dealersautocenter.com
**Subject**: `BackEnd Task / Osama [Last Name]`

**Include:**
- Source code
- `SUBMISSION_TEST_EVIDENCE.md`
- Updated resume (PDF)
- How to run/test instructions

