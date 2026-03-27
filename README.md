# Dealer & Vehicle Inventory Module

A multi-tenant inventory management system built with Spring Boot, implementing clean architecture principles.

## 🎯 Requirements Compliance

### ✅ All Requirements Met

| Requirement | Status | Implementation |
|-------------|--------|----------------|
| **Data Model** | ✅ | Dealer & Vehicle entities with all required fields + audit timestamps |
| **Multi-Tenancy** | ✅ | Row-level tenant isolation with `tenant_id` in all tables |
| **X-Tenant-Id Header** | ✅ | Required for all endpoints, returns **400** if missing |
| **Cross-Tenant Access** | ✅ | Blocked with **403** response |
| **All CRUD Endpoints** | ✅ | Complete REST API for Dealers & Vehicles |
| **Pagination & Sorting** | ✅ | Implemented with Spring Data Pageable |
| **Vehicle Filters** | ✅ | model, status, priceMin, priceMax, subscription |
| **Subscription Filter** | ✅ | `?subscription=PREMIUM` returns PREMIUM dealer vehicles only (tenant-scoped) |
| **Admin Endpoint** | ✅ | `/admin/dealers/countBySubscription` requires GLOBAL_ADMIN |
| **Admin Count Scope** | ✅ | Returns system-wide counts (all tenants) OR per-tenant with `X-Tenant-Id` header |
| **Clean Architecture** | ✅ | Clear separation: controller → service → repository → entity |

### 🔒 Acceptance Checks

- ✅ **Missing X-Tenant-Id → 400**: Filter returns `400 Bad Request` with error message
- ✅ **Cross-tenant access → 403**: Attempting to access another tenant's data returns `403 Forbidden`
- ✅ **Subscription filter**: `?subscription=PREMIUM` only returns vehicles from PREMIUM dealers within caller's tenant
- ✅ **Admin authorization**: `/admin/**` endpoints require GLOBAL_ADMIN role, return `403` otherwise

## Tech Stack

- **Java 21**
- **Spring Boot 3.2.x**
- **Spring Security** with JWT authentication
- **Spring Data JPA** with H2 database and automatic auditing
- **SpringDoc OpenAPI** for API documentation
- **Lombok** for reducing boilerplate code

## Features

- ✨ **Multi-tenant architecture** with row-level tenant isolation
- 🔐 **JWT-based authentication** with embedded tenant ID
- 🛡️ **Required X-Tenant-Id header** validation (400 if missing)
- 🚫 **Cross-tenant access prevention** (403 if attempted)
- 👤 **Role-based access control** (TENANT_USER, GLOBAL_ADMIN)
- 📊 **RESTful CRUD operations** for Dealers and Vehicles
- 🔍 **Advanced filtering**, pagination, and sorting
- 👑 **Admin analytics** endpoints with optional tenant override
- ⏰ **Automatic audit timestamps** (`created_at`, `updated_at`) managed by JPA

## Quick Start

### Prerequisites

- Java 21 or higher
- Maven 3.9.x or higher

### Running the Application

```bash
# Clone/download the project
cd dealer-vehicle-inventory

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start at `http://localhost:8080`

### API Documentation

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

### H2 Console

- **URL**: http://localhost:8080/h2-console
- **JDBC URL**: `jdbc:h2:mem:inventorydb`
- **Username**: `sa`
- **Password**: (leave empty)

## Authentication & Multi-Tenancy

### Security Model - Dual Layer Protection

The application uses **two layers** of tenant security:

1. **JWT Token** - Contains tenant ID (prevents tampering)
2. **X-Tenant-Id Header** - Required for all requests (explicit tenant declaration)

Both must match for the request to succeed. This provides:
- ✅ Protection against tenant ID tampering
- ✅ Explicit tenant declaration in API calls
- ✅ Easy debugging and request tracing

### Generating JWT Tokens

Use the `/auth/login` endpoint to generate JWT tokens:

```bash
# Generate TENANT_USER token (tenant-1)
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "user1", "tenantId": "tenant-1", "role": "TENANT_USER"}'

# Generate TENANT_USER token (tenant-2)
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "user2", "tenantId": "tenant-2", "role": "TENANT_USER"}'

# Generate GLOBAL_ADMIN token
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "tenantId": "system", "role": "GLOBAL_ADMIN"}'
```

### Using Tokens

**IMPORTANT**: You must include **both** headers:

```bash
curl -X GET http://localhost:8080/dealers \
  -H "Authorization: Bearer <your-jwt-token>" \
  -H "X-Tenant-Id: tenant-1"
```

**Missing X-Tenant-Id header returns 400**:
```json
{
  "status": 400,
  "message": "Missing required header: X-Tenant-Id"
}
```

**Mismatched tenant ID returns 403**:
```json
{
  "status": 403,
  "message": "Access denied: X-Tenant-Id does not match authenticated user's tenant"
}
```

## API Endpoints

### Dealers

| Method | Endpoint | Description | Required Headers |
|--------|----------|-------------|------------------|
| POST | `/dealers` | Create a new dealer | Authorization, X-Tenant-Id |
| GET | `/dealers/{id}` | Get dealer by ID | Authorization, X-Tenant-Id |
| GET | `/dealers` | List dealers (pagination/sort) | Authorization, X-Tenant-Id |
| PATCH | `/dealers/{id}` | Update dealer | Authorization, X-Tenant-Id |
| DELETE | `/dealers/{id}` | Delete dealer | Authorization, X-Tenant-Id |

### Vehicles

| Method | Endpoint | Description | Required Headers |
|--------|----------|-------------|------------------|
| POST | `/vehicles` | Create a new vehicle | Authorization, X-Tenant-Id |
| GET | `/vehicles/{id}` | Get vehicle by ID | Authorization, X-Tenant-Id |
| GET | `/vehicles` | List vehicles with filters | Authorization, X-Tenant-Id |
| PATCH | `/vehicles/{id}` | Update vehicle | Authorization, X-Tenant-Id |
| DELETE | `/vehicles/{id}` | Delete vehicle | Authorization, X-Tenant-Id |

**Vehicle Filters** (all optional):
- `model` - Filter by model name (partial match, case-insensitive)
- `status` - Filter by status (AVAILABLE, SOLD)
- `priceMin` - Minimum price filter
- `priceMax` - Maximum price filter
- `subscription` - Filter by dealer subscription type (BASIC, PREMIUM)

**Example**: `GET /vehicles?subscription=PREMIUM&status=AVAILABLE&priceMin=50000`
- Returns only vehicles from PREMIUM dealers
- **Tenant-scoped**: Only returns vehicles within the caller's tenant
- Combines with other filters (status, price range, etc.)

### Admin Endpoints (GLOBAL_ADMIN only)

| Method | Endpoint | Description | Required Headers |
|--------|----------|-------------|------------------|
| GET | `/admin/dealers/countBySubscription` | Count dealers by subscription | Authorization only |

**Count Scope Behavior**:
- **Default (no params)**: Returns **system-wide counts** across all tenants
  ```json
  { "BASIC": 15, "PREMIUM": 23 }
  ```
- **With `scope=tenant` + `X-Tenant-Id` header**: Returns **per-tenant counts**
  ```json
  { "BASIC": 3, "PREMIUM": 5 }
  ```

**Note**: Only GLOBAL_ADMIN can access admin endpoints. Regular users get `403 Forbidden`.

## Multi-Tenant Architecture

### Tenant Identification & Validation

1. **X-Tenant-Id Header** (required) - Explicitly declares which tenant the request is for
2. **JWT Token** (required) - Contains embedded tenant ID for validation
3. **Validation** - Both must match, otherwise request is rejected with 403

### Tenant Isolation Guarantees

✅ **Row-level security**: All tables include `tenant_id` column
✅ **Query scoping**: All repository methods automatically filter by tenant
✅ **Service validation**: Services verify tenant context before operations
✅ **Cross-tenant prevention**: Attempting to access another tenant's data returns 403
✅ **Admin exception**: GLOBAL_ADMIN can query across tenants for analytics

### Error Responses

| Scenario | HTTP Status | Response |
|----------|-------------|----------|
| Missing X-Tenant-Id header | 400 | `{"status": 400, "message": "Missing required header: X-Tenant-Id"}` |
| X-Tenant-Id doesn't match token | 403 | `{"status": 403, "message": "Access denied: X-Tenant-Id does not match authenticated user's tenant"}` |
| Missing/Invalid token | 401 | `{"status": 401, "message": "Authentication required"}` |
| Non-admin accessing /admin | 403 | `{"status": 403, "message": "Access denied: GLOBAL_ADMIN role required"}` |
| Resource not found in tenant | 404 | `{"status": 404, "message": "Dealer not found with id: {id}"}` |

## Testing Examples

### 1. Get JWT Token

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "user1", "tenantId": "tenant-1", "role": "TENANT_USER"}' | jq -r '.token')
```

### 2. Create a Dealer (with X-Tenant-Id)

```bash
curl -X POST http://localhost:8080/dealers \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-Id: tenant-1" \
  -d '{"name": "Premium Motors", "email": "contact@premium.com", "subscriptionType": "PREMIUM"}'
```

### 3. List Vehicles with Subscription Filter

```bash
# Get only PREMIUM dealer vehicles (tenant-scoped)
curl -X GET "http://localhost:8080/vehicles?subscription=PREMIUM&status=AVAILABLE&priceMin=50000" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-Id: tenant-1"
```

### 4. Test Missing X-Tenant-Id (Returns 400)

```bash
curl -X GET http://localhost:8080/dealers \
  -H "Authorization: Bearer $TOKEN"

# Response: {"status":400,"message":"Missing required header: X-Tenant-Id"}
```

### 5. Test Tenant Mismatch (Returns 403)

```bash
# Token is for tenant-1, but header says tenant-2
curl -X GET http://localhost:8080/dealers \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-Id: tenant-2"

# Response: {"status":403,"message":"Access denied: X-Tenant-Id does not match authenticated user's tenant"}
```

### 6. Admin: Count by Subscription

```bash
# Get admin token
ADMIN_TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "tenantId": "system", "role": "GLOBAL_ADMIN"}' | jq -r '.token')

# System-wide count (all tenants)
curl -X GET http://localhost:8080/admin/dealers/countBySubscription \
  -H "Authorization: Bearer $ADMIN_TOKEN"
# Response: {"BASIC": 15, "PREMIUM": 23}

# Per-tenant count (admin can use X-Tenant-Id header)
curl -X GET "http://localhost:8080/admin/dealers/countBySubscription?scope=tenant" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "X-Tenant-Id: tenant-1"
# Response: {"BASIC": 3, "PREMIUM": 5}
```

### 7. Verify Cross-Tenant Isolation (Returns 404)

```bash
# Get token for tenant-1
TOKEN_T1=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "user1", "tenantId": "tenant-1", "role": "TENANT_USER"}' | jq -r '.token')

# Create dealer in tenant-1
DEALER_ID=$(curl -s -X POST http://localhost:8080/dealers \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN_T1" \
  -H "X-Tenant-Id: tenant-1" \
  -d '{"name": "Tenant1 Dealer", "email": "t1@dealer.com", "subscriptionType": "BASIC"}' | jq -r '.id')

# Get token for tenant-2
TOKEN_T2=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "user2", "tenantId": "tenant-2", "role": "TENANT_USER"}' | jq -r '.token')

# Try to access tenant-1's dealer from tenant-2
# This will fail because dealer doesn't exist in tenant-2's scope
curl -X GET "http://localhost:8080/dealers/$DEALER_ID" \
  -H "Authorization: Bearer $TOKEN_T2" \
  -H "X-Tenant-Id: tenant-2"

# Response: {"status":404,"message":"Dealer not found with id: ..."}
# Even though the dealer exists, it's not visible to tenant-2
```

## Project Structure

```
src/main/java/com/inventory/
├── InventoryApplication.java          # Main application entry point
├── common/                            # Shared infrastructure components
│   ├── config/
│   │   ├── JpaAuditingConfig.java    # JPA auditing configuration (@CreatedDate, @LastModifiedDate)
│   │   ├── OpenApiConfig.java         # Swagger/OpenAPI configuration
│   │   └── SecurityConfig.java        # Spring Security configuration
│   ├── entity/
│   │   └── Auditable.java            # Embeddable component for audit timestamps (created_at, updated_at)
│   ├── exception/
│   │   ├── ErrorResponse.java         # Standard error response DTO
│   │   ├── GlobalExceptionHandler.java # Centralized exception handling
│   │   ├── ResourceNotFoundException.java
│   │   └── TenantAccessDeniedException.java
│   ├── security/
│   │   ├── JwtAuthenticationFilter.java   # JWT token validation & tenant extraction
│   │   ├── JwtTokenProvider.java          # JWT token generation & parsing
│   │   ├── RoleConstants.java             # Role definitions (TENANT_USER, GLOBAL_ADMIN)
│   │   ├── TenantContext.java             # ThreadLocal tenant context holder
│   │   └── TenantFilter.java              # X-Tenant-Id validation (400 if missing, 403 if mismatch)
│   └── web/
│       └── PageResponse.java              # Generic pagination response wrapper
├── auth/                              # Authentication module
│   ├── controller/
│   │   └── AuthController.java        # Login endpoint for JWT generation
│   └── dto/
│       ├── LoginRequest.java
│       └── JwtResponse.java
├── dealer/                            # Dealer module (clean architecture)
│   ├── controller/
│   │   ├── DealerController.java      # REST endpoints for dealer CRUD
│   │   └── DealerAdminController.java # Admin analytics endpoint
│   ├── dto/
│   │   ├── CreateDealerRequest.java
│   │   ├── UpdateDealerRequest.java
│   │   └── DealerResponse.java
│   ├── entity/
│   │   ├── Dealer.java                # JPA entity with tenant_id, audit fields
│   │   └── SubscriptionType.java      # Enum: BASIC, PREMIUM
│   ├── mapper/
│   │   └── DealerMapper.java          # Entity ↔ DTO conversion
│   ├── repository/
│   │   └── DealerRepository.java      # Tenant-scoped queries
│   └── service/
│       ├── DealerService.java
│       └── DealerServiceImpl.java     # Business logic with tenant validation
└── vehicle/                           # Vehicle module (clean architecture)
    ├── controller/
    │   └── VehicleController.java     # REST endpoints with filters
    ├── dto/
    │   ├── CreateVehicleRequest.java
    │   ├── UpdateVehicleRequest.java
    │   ├── VehicleFilterRequest.java  # Filters: model, status, price, subscription
    │   └── VehicleResponse.java
    ├── entity/
    │   ├── Vehicle.java               # JPA entity with dealer FK, tenant_id
    │   └── VehicleStatus.java         # Enum: AVAILABLE, SOLD
    ├── mapper/
    │   └── VehicleMapper.java
    ├── repository/
    │   └── VehicleRepository.java     # Complex query with subscription filter
    └── service/
        ├── VehicleService.java
        └── VehicleServiceImpl.java    # Business logic with tenant validation
```

## Clean Architecture Layers

### 1. Controller Layer
- REST API endpoints with OpenAPI documentation
- Request validation (Jakarta Validation)
- HTTP response handling
- **No business logic**

### 2. Service Layer
- Business logic implementation
- Tenant context validation
- Transaction management (@Transactional)
- Cross-entity operations (e.g., verify dealer exists before creating vehicle)

### 3. Repository Layer
- Data access with Spring Data JPA
- Tenant-scoped queries (`findByTenantId`, `findByIdAndTenantId`)
- Complex filters (JPQL with JOIN for subscription filter)

### 4. Entity Layer
- JPA entities with proper relationships
- Audit timestamps via embedded `Auditable` component
- Enum types for `SubscriptionType` and `VehicleStatus`

### 5. Security Layer
- JWT authentication (JwtAuthenticationFilter)
- X-Tenant-Id header validation (TenantFilter)
- Role-based access control (SecurityConfig)
- Tenant context management (TenantContext)

## Implementation Highlights

### 🎯 Automatic Audit Timestamps

All entities use an **embeddable** `Auditable` component for automatic timestamp management:

```java
@Embeddable
@Getter
public class Auditable {
    @CreatedDate
    private LocalDateTime createdAt;  // Automatically set on INSERT

    @LastModifiedDate
    private LocalDateTime updatedAt;  // Automatically updated on UPDATE
}

@Entity
@EntityListeners(AuditingEntityListener.class)
public class Dealer {
    @Embedded
    @Builder.Default
    private Auditable auditable = new Auditable();

    // Convenience methods for clean API
    public LocalDateTime getCreatedAt() {
        return auditable.getCreatedAt();
    }

    public LocalDateTime getUpdatedAt() {
        return auditable.getUpdatedAt();
    }
}
```

**Benefits**:
- No manual timestamp management
- Consistent across all entities
- Spring Data JPA handles updates automatically
- **Preserves inheritance slot** - entities can extend other classes if needed
- Uses **composition over inheritance** - better OOP design

### 🔐 Tenant Security - Defense in Depth

**Layer 1: TenantFilter**
- Validates `X-Tenant-Id` header is present (400 if missing)
- Validates header matches JWT token tenant (403 if mismatch)
- Sets tenant context for request

**Layer 2: Service Layer**
- Explicitly validates `TenantContext.getTenantId()` before operations
- Throws `TenantAccessDeniedException` if context missing

**Layer 3: Repository Layer**
- All queries include `tenantId` parameter
- Uses `findByIdAndTenantId` pattern to prevent cross-tenant access

### 🔍 Subscription Filter Implementation

The subscription filter is tenant-scoped and uses JPQL JOIN:

```java
@Query("SELECT v FROM Vehicle v JOIN v.dealer d WHERE v.tenantId = :tenantId " +
       "AND (:subscription IS NULL OR d.subscriptionType = :subscription)")
Page<Vehicle> findByFilters(..., SubscriptionType subscription, Pageable pageable);
```

This ensures:
- ✅ Only vehicles from the caller's tenant are returned
- ✅ JOIN with dealer table filters by `subscriptionType`
- ✅ Combines with other filters (model, status, price)
- ✅ Supports pagination and sorting

## Security Highlights

- ✅ **Dual validation**: JWT token + X-Tenant-Id header must match
- ✅ **Stateless authentication**: No server-side session storage
- ✅ **Role-based access**: TENANT_USER vs GLOBAL_ADMIN
- ✅ **Automatic scoping**: All queries filtered by tenant
- ✅ **Tamper-proof**: JWT signature prevents token modification
- ✅ **Cross-tenant prevention**: 403 response for unauthorized access
- ✅ **Audit trail**: Automatic timestamps on all entities

## Assignment Submission

**To**: mytask@dealersautocenter.com
**Subject**: BackEnd Task / [Your First Name] [Your Last Name]

## Author

Osama - Backend Developer Assignment
