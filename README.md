# Dealer & Vehicle Inventory Module

A multi-tenant inventory management system built with Spring Boot, implementing clean architecture principles.

## Tech Stack

- **Java 21**
- **Spring Boot 3.2.x**
- **Spring Security** with JWT authentication
- **Spring Data JPA** with H2 database
- **SpringDoc OpenAPI** for API documentation
- **Lombok** for reducing boilerplate code

## Features

- Multi-tenant architecture with row-level tenant isolation
- JWT-based authentication with role support (TENANT_USER, GLOBAL_ADMIN)
- RESTful CRUD operations for Dealers and Vehicles
- Filtering, pagination, and sorting
- Admin analytics endpoints

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

## Authentication

### Generating JWT Tokens

Use the `/auth/login` endpoint to generate JWT tokens:

```bash
# Generate TENANT_USER token
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "user1", "tenantId": "tenant-1", "role": "TENANT_USER"}'

# Generate GLOBAL_ADMIN token
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "tenantId": "admin", "role": "GLOBAL_ADMIN"}'
```

### Using Tokens

Include the token in the `Authorization` header:

```
Authorization: Bearer <your-jwt-token>
```

## API Endpoints

### Dealers

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/dealers` | Create a new dealer |
| GET | `/dealers/{id}` | Get dealer by ID |
| GET | `/dealers` | List dealers (pagination/sort) |
| PATCH | `/dealers/{id}` | Update dealer |
| DELETE | `/dealers/{id}` | Delete dealer |

### Vehicles

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/vehicles` | Create a new vehicle |
| GET | `/vehicles/{id}` | Get vehicle by ID |
| GET | `/vehicles` | List vehicles with filters |
| PATCH | `/vehicles/{id}` | Update vehicle |
| DELETE | `/vehicles/{id}` | Delete vehicle |

**Vehicle Filters:**
- `model` - Filter by model name (partial match)
- `status` - Filter by status (AVAILABLE, SOLD)
- `priceMin` - Minimum price
- `priceMax` - Maximum price
- `subscription` - Filter by dealer subscription (BASIC, PREMIUM)

### Admin (GLOBAL_ADMIN only)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/admin/dealers/countBySubscription` | Count dealers by subscription type |

**Query Parameters:**
- `scope=tenant` - Returns per-tenant counts (requires X-Tenant-Id header)
- No scope - Returns system-wide counts across all tenants

## Multi-Tenant Architecture

### Tenant Identification

Every request to protected endpoints must include the `X-Tenant-Id` header:

```
X-Tenant-Id: tenant-1
```

### Tenant Isolation

- **Row-level security**: All data includes `tenant_id` column
- **Service layer validation**: Cross-tenant access is blocked with 403
- **GLOBAL_ADMIN exception**: Admin users can access admin endpoints without tenant restrictions

### Error Responses

| Scenario | HTTP Status | Response |
|----------|-------------|----------|
| Missing X-Tenant-Id | 400 | `{"error": "Missing required header: X-Tenant-Id"}` |
| Cross-tenant access | 403 | `{"error": "Access denied: Cannot access resources of different tenant"}` |
| Resource not found | 404 | `{"error": "Dealer not found with id: {id}"}` |

## Sample Data

The application includes sample data for testing:

**Tenant-1 Dealers:**
- Premium Auto Sales (PREMIUM)
- Basic Motors (BASIC)
- Elite Vehicles (PREMIUM)

**Tenant-2 Dealers:**
- City Auto Group (PREMIUM)
- Budget Cars (BASIC)

## Testing Examples

### 1. Get JWT Token

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "user1", "tenantId": "tenant-1", "role": "TENANT_USER"}' | jq -r '.token')
```

### 2. Create a Dealer

```bash
curl -X POST http://localhost:8080/dealers \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-Id: tenant-1" \
  -d '{"name": "New Dealer", "email": "new@dealer.com", "subscriptionType": "PREMIUM"}'
```

### 3. List Vehicles with Filters

```bash
curl -X GET "http://localhost:8080/vehicles?subscription=PREMIUM&status=AVAILABLE&priceMin=50000" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-Id: tenant-1"
```

### 4. Admin: Count by Subscription

```bash
# Get admin token first
ADMIN_TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "tenantId": "admin", "role": "GLOBAL_ADMIN"}' | jq -r '.token')

# System-wide count
curl -X GET http://localhost:8080/admin/dealers/countBySubscription \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Per-tenant count
curl -X GET "http://localhost:8080/admin/dealers/countBySubscription?scope=tenant" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "X-Tenant-Id: tenant-1"
```

## Acceptance Checks

| Requirement | Implementation |
|-------------|----------------|
| Missing X-Tenant-Id → 400 | TenantFilter returns 400 Bad Request |
| Cross-tenant access blocked → 403 | TenantFilter validates header matches token tenant |
| subscription=PREMIUM filter | VehicleRepository joins with Dealer and filters by subscriptionType |
| Admin counts (GLOBAL_ADMIN only) | DealerAdminController with role check in TenantFilter |

## Project Structure

```
src/main/java/com/inventory/
├── InventoryApplication.java
├── common/                    # Shared infrastructure
│   ├── config/               # Security, JPA, OpenAPI config
│   ├── exception/            # Exception handling
│   ├── security/             # JWT, Tenant filters
│   └── web/                  # Common DTOs
├── auth/                      # Authentication module
│   ├── controller/
│   └── dto/
├── dealer/                    # Dealer module
│   ├── controller/
│   ├── dto/
│   ├── entity/
│   ├── mapper/
│   ├── repository/
│   └── service/
└── vehicle/                   # Vehicle module
    ├── controller/
    ├── dto/
    ├── entity/
    ├── mapper/
    ├── repository/
    └── service/
```

## Clean Architecture Layers

1. **Controller Layer** - REST API endpoints, request/response handling
2. **Service Layer** - Business logic, tenant validation
3. **Repository Layer** - Data access with tenant-scoped queries
4. **Entity Layer** - JPA entities with proper relationships
5. **Security Layer** - JWT authentication, tenant filtering

## Author

Osama - Backend Developer Assignment
