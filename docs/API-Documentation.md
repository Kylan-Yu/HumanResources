# HRMS API Documentation

## Overview

The HRMS (Human Resource Management System) API is a RESTful API built with Spring Boot microservices architecture. It provides comprehensive endpoints for managing human resources, employee data, organizational structure, payroll, recruitment, and more.

## Base URL

```
Production: https://api.hrms.com
Development: http://localhost:8080
```

## Authentication

All API requests (except authentication endpoints) require a valid JWT token in the Authorization header:

```
Authorization: Bearer <jwt_token>
```

## API Response Format

All API responses follow a consistent format:

### Success Response
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    // Response data
  },
  "timestamp": "2024-01-01T00:00:00Z"
}
```

### Error Response
```json
{
  "code": 400,
  "message": "Bad Request",
  "error": "Detailed error message",
  "timestamp": "2024-01-01T00:00:00Z"
}
```

## Core Services

### 1. Authentication Service (hrms-auth)

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "password"
}
```

**Response:**
```json
{
  "code": 200,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "refresh_token_here",
    "expiresIn": 86400,
    "user": {
      "id": 1,
      "username": "admin",
      "email": "admin@hrms.com",
      "roles": ["ADMIN"],
      "permissions": ["USER_READ", "USER_WRITE", "SYSTEM_ADMIN"]
    }
  }
}
```

#### Refresh Token
```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "refresh_token_here"
}
```

#### Logout
```http
POST /api/auth/logout
Authorization: Bearer <jwt_token>
```

#### Get Current User
```http
GET /api/auth/me
Authorization: Bearer <jwt_token>
```

### 2. Employee Service (hrms-employee)

#### Get All Employees
```http
GET /api/employees?page=1&size=20&search=john&department=IT
Authorization: Bearer <jwt_token>
```

**Response:**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "content": [
      {
        "id": 1,
        "employeeId": "EMP001",
        "firstName": "John",
        "lastName": "Doe",
        "email": "john.doe@hrms.com",
        "phone": "+1234567890",
        "department": {
          "id": 1,
          "name": "IT",
          "code": "IT"
        },
        "position": "Senior Developer",
        "status": "ACTIVE",
        "hireDate": "2023-01-15",
        "manager": {
          "id": 2,
          "name": "Jane Smith"
        }
      }
    ],
    "totalElements": 100,
    "totalPages": 5,
    "size": 20,
    "number": 0
  }
}
```

#### Get Employee by ID
```http
GET /api/employees/{id}
Authorization: Bearer <jwt_token>
```

#### Create Employee
```http
POST /api/employees
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "employeeId": "EMP002",
  "firstName": "Jane",
  "lastName": "Smith",
  "email": "jane.smith@hrms.com",
  "phone": "+1234567891",
  "departmentId": 1,
  "position": "Project Manager",
  "hireDate": "2024-01-01",
  "managerId": 1,
  "salary": 80000,
  "address": {
    "street": "123 Main St",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001",
    "country": "USA"
  }
}
```

#### Update Employee
```http
PUT /api/employees/{id}
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "firstName": "Jane",
  "lastName": "Johnson",
  "email": "jane.johnson@hrms.com",
  "position": "Senior Project Manager"
}
```

#### Delete Employee
```http
DELETE /api/employees/{id}
Authorization: Bearer <jwt_token>
```

#### Get Employee Profile
```http
GET /api/employees/{id}/profile
Authorization: Bearer <jwt_token>
```

#### Upload Employee Document
```http
POST /api/employees/{id}/documents
Authorization: Bearer <jwt_token>
Content-Type: multipart/form-data

file: <file>
documentType: "CONTRACT"
description: "Employment Contract"
```

### 3. Organization Service (hrms-org)

#### Get All Departments
```http
GET /api/organizations/departments
Authorization: Bearer <jwt_token>
```

**Response:**
```json
{
  "code": 200,
  "message": "Success",
  "data": [
    {
      "id": 1,
      "name": "Engineering",
      "code": "ENG",
      "description": "Software development team",
      "parentId": null,
      "managerId": 1,
      "employeeCount": 25,
      "children": [
        {
          "id": 2,
          "name": "Frontend Team",
          "code": "FE",
          "parentId": 1,
          "employeeCount": 8
        }
      ]
    }
  ]
}
```

#### Create Department
```http
POST /api/organizations/departments
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "name": "Marketing",
  "code": "MKT",
  "description": "Marketing and sales team",
  "parentId": null,
  "managerId": 2
}
```

#### Get Organization Chart
```http
GET /api/organizations/chart
Authorization: Bearer <jwt_token>
```

#### Get Positions
```http
GET /api/organizations/positions?departmentId=1
Authorization: Bearer <jwt_token>
```

### 4. Payroll Service (hrms-payroll)

#### Get Payroll History
```http
GET /api/payroll/history?employeeId=1&year=2024&month=1
Authorization: Bearer <jwt_token>
```

**Response:**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": 1,
    "employeeId": 1,
    "payPeriod": "2024-01",
    "basicSalary": 5000.00,
    "overtimePay": 500.00,
    "bonuses": 1000.00,
    "deductions": {
      "tax": 800.00,
      "insurance": 200.00,
      "other": 50.00
    },
    "netSalary": 5450.00,
    "status": "PAID",
    "paymentDate": "2024-01-31"
  }
}
```

#### Process Payroll
```http
POST /api/payroll/process
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "payPeriod": "2024-02",
  "employeeIds": [1, 2, 3],
  "processType": "MONTHLY"
}
```

#### Get Salary Structure
```http
GET /api/payroll/salary-structure?positionId=1
Authorization: Bearer <jwt_token>
```

### 5. Recruitment Service (hrms-recruit)

#### Get Job Postings
```http
GET /api/recruitments/jobs?status=ACTIVE&page=1&size=20
Authorization: Bearer <jwt_token>
```

**Response:**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "content": [
      {
        "id": 1,
        "title": "Senior Java Developer",
        "description": "We are looking for an experienced Java developer...",
        "department": "Engineering",
        "location": "New York",
        "employmentType": "FULL_TIME",
        "salaryRange": {
          "min": 80000,
          "max": 120000,
          "currency": "USD"
        },
        "requirements": [
          "5+ years of Java experience",
          "Spring Boot expertise",
          "Microservices experience"
        ],
        "status": "ACTIVE",
        "postedDate": "2024-01-01",
        "applicationCount": 15
      }
    ]
  }
}
```

#### Create Job Posting
```http
POST /api/recruitments/jobs
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "title": "Frontend Developer",
  "description": "We are looking for a skilled frontend developer...",
  "departmentId": 1,
  "location": "Remote",
  "employmentType": "FULL_TIME",
  "salaryRange": {
    "min": 70000,
    "max": 100000,
    "currency": "USD"
  },
  "requirements": [
    "3+ years of React experience",
    "TypeScript expertise",
    "UI/UX knowledge"
  ]
}
```

#### Get Applicants
```http
GET /api/recruitments/applicants?jobId=1&status=SCREENING
Authorization: Bearer <jwt_token>
```

#### Update Application Status
```http
PUT /api/recruitments/applications/{id}/status
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "status": "INTERVIEW_SCHEDULED",
  "notes": "Passed initial screening",
  "interviewDate": "2024-02-01T10:00:00Z"
}
```

### 6. Performance Service (hrms-performance)

#### Get Performance Reviews
```http
GET /api/performance/reviews?employeeId=1&year=2024
Authorization: Bearer <jwt_token>
```

#### Create Performance Review
```http
POST /api/performance/reviews
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "employeeId": 1,
  "reviewerId": 2,
  "reviewPeriod": "2024-Q1",
  "goals": [
    {
      "id": 1,
      "title": "Complete Project X",
      "description": "Finish the development of Project X",
      "weight": 40,
      "achievement": 85
    }
  ],
  "competencies": [
    {
      "name": "Technical Skills",
      "rating": 4,
      "comments": "Excellent technical knowledge"
    }
  ],
  "overallRating": 4,
  "comments": "Great performance this quarter"
}
```

#### Get Goals
```http
GET /api/performance/goals?employeeId=1&status=ACTIVE
Authorization: Bearer <jwt_token>
```

## Error Codes

| Code | Description |
|------|-------------|
| 200 | Success |
| 201 | Created |
| 400 | Bad Request |
| 401 | Unauthorized |
| 403 | Forbidden |
| 404 | Not Found |
| 409 | Conflict |
| 422 | Validation Error |
| 500 | Internal Server Error |

## Rate Limiting

API requests are limited to:
- 1000 requests per hour per IP address
- 100 requests per minute per user

## Pagination

List endpoints support pagination with these parameters:
- `page`: Page number (default: 1)
- `size`: Page size (default: 20, max: 100)
- `sort`: Sort field and direction (e.g., `createdAt,desc`)

## Search and Filtering

Most list endpoints support:
- `search`: Text search across multiple fields
- Field-specific filters (e.g., `department=IT`, `status=ACTIVE`)

## Webhooks

HRMS supports webhooks for real-time notifications:

### Configure Webhook
```http
POST /api/webhooks
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "url": "https://your-app.com/webhook",
  "events": ["employee.created", "employee.updated", "payroll.processed"],
  "secret": "webhook_secret"
}
```

### Webhook Events
- `employee.created`
- `employee.updated`
- `employee.deleted`
- `payroll.processed`
- `recruitment.application_received`
- `performance.review_completed`

## SDKs and Libraries

### JavaScript/TypeScript
```bash
npm install hrms-sdk
```

```javascript
import { HRMSClient } from 'hrms-sdk';

const client = new HRMSClient({
  baseURL: 'http://localhost:8080',
  token: 'your-jwt-token'
});

const employees = await client.employees.list();
```

### Python
```bash
pip install hrms-python-sdk
```

```python
from hrms_sdk import HRMSClient

client = HRMSClient(
    base_url='http://localhost:8080',
    token='your-jwt-token'
)

employees = client.employees.list()
```

## Testing

Use our Postman collection to test the API:
[Download Postman Collection](./hrms-api.postman_collection.json)

## Support

For API support and questions:
- Email: api-support@hrms.com
- Documentation: https://docs.hrms.com
- Status Page: https://status.hrms.com
