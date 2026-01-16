### Device Management API

A Spring Boot 4.0.1 REST API for managing devices with full CRUD support, pagination, filtering, and state-based business rules.
The API is fully documented using OpenAPI (Swagger) and designed with testability and clean architecture principles in mind.

### Features

* Create, fetch, update, and delete devices
* Full (PUT) and partial (PATCH) updates with strict business rules
* Pagination, sorting, and filtering
* Optimistic locking
* OpenAPI / Swagger documentation
* H2 support for tests, PostgreSQL support for production
* Clean separation of concerns (Controller / Use Case / Domain / Persistence)

### Tech Stack

* Java 21
* Spring Boot 4.0.1
* Spring Web (REST)
* Spring Data JPA
* PostgreSQL (production) and H2 (tests)
* Springdoc OpenAPI (Swagger)
* JUnit 5 & Mockito

### Domain Overview

| Field        | Type    | Description                 |
| ------------ | ------- |-----------------------------|
| id           | UUID    | Unique identifier (PK)      |
| name         | String  | Device name                 |
| brand        | String  | Device brand                |
| state        | Enum    | AVAILABLE, IN_USE, INACTIVE |
| version      | Long    | Optimistic locking          |
| creationTime | Instant | Creation timestamp          |


### Business Rules/Considerations

#### PUT (Full Update)

* Either all fields change or no fields change
* Name and brand cannot be changed if device is IN_USE

#### PATCH (Partial Update)

* Only provided fields are updated
* Name and brand cannot be changed if device is IN_USE
* State-only changes are allowed

### Running the Application

#### Prerequisites

* Java 21+
* Maven 3.9+
* PostgreSQL 13+ (tested on PostgreSQL 16)

Steps to build and execute the application
* Ensure the postgres image is pulled and is running locally or on docker.
* Make sure the port for PostgreSQL is 5432.
* The above steps are required if the application is run as standalone using
`
  mvn clean spring-boot:run
`
* Build the project using the maven command 
`
mvn clean install -U
`
* This step creates a executable Jar like deviceManagement-1.0.0.jar in the target folder.
* The project has Docker file to containerize the spring boot app and build an image.
* The docker compose is configured to build and run the application and other dependencies like postgres.
* Navigate to the root folder of the project ~/DeviceManagement.
* To run the application use the commands.

`
docker-compose down -v
`

`
docker-compose up -d
`
* The application starts at http://localhost:8080

### API Documentation (Swagger)

The API is fully documented using Springdoc OpenAPI.

#### Swagger UI
http://localhost:8080/swagger-ui/index.html

#### OpenAPI JSON
http://localhost:8080/v3/api-docs

Swagger includes:
* Request/response schemas
* Enum values
* Query parameter descriptions
* Pagination & sorting parameters

### API Endpoints

#### Create Device

```
POST /devices
```
Body 
```
{
  "name": "Device A",
  "brand": "Samsung",
  "state": "AVAILABLE"
}
```

#### Get Device

```
GET /devices/{id}

```

#### List Devices

```
GET /devices?brand=Samsung&state=AVAILABLE&page=0&size=20&sort=creationTime,desc

```

#### Full Update (PUT)

```
PUT /devices/{id}

```
Body

```
{
  "name": "New Name",
  "brand": "New Brand",
  "state": "AVAILABLE"
}
```

#### Partial Update (PATCH)

```
PUT /devices/{id}

```
Body

```
{
  "name" : "Update Name", (Optional)
  "brand": "Update Brand", (Optional)
  "state": "IN_USE"
}
```

#### Delete Device
```
DELETE /devices/{id}
```

### Architecture
```
controller
    └── usecase
        └── domain
            └── persistence
```

* Controller: HTTP layer only
* Use Case: Business logic and rules
* Domain: Core entities
* Persistence: JPA repositories

### Design Decisions

* Strict separation between PUT and PATCH semantics
* Business rules enforced in the service layer
* Swagger annotations for API clarity, not logic
* Optimistic locking to avoid lost updates
* Pageable abstraction to keep API stable

