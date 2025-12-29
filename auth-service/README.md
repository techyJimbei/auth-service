# Auth Service (Oppex AI) - Backend

A robust, high-performance authentication microservice built for the Oppex AI ecosystem using **Quarkus**. This service handles user registration, persistence via Supabase, and secure token issuance.

## 🚀 Features

* **Secure User Registration**: Password hashing using **BCrypt** with constant-time verification.
* **JWT Authentication**: Stateless authentication using **SmallRye JWT** with **HS256 (Symmetric)** signing.
* **Email Infrastructure**: API-based delivery using **Resend**, bypassing cloud SMTP port restrictions for reliable delivery.
* **Cloud Persistence**: Optimized for **PostgreSQL** (Supabase) using Hibernate ORM with Panache.

## 🏗️ Architecture

This service is part of a three-tier system:
1.  **Frontend**: React SPA (Vercel).
2.  **Middleware**: Node.js Proxy (Render) - Manages stateful sessions.
3.  **Backend (This Service)**: Quarkus API (Render) - Handles logic and DB.



## 🛠 Technology Stack

* **Framework**: Quarkus (v3.30.5)
* **Security**: SmallRye JWT Build (for token generation)
* **Database**: PostgreSQL / Supabase
* **JSON Handling**: Jackson (RESTEasy Reactive)

## ⚙️ Configuration & Environment

For production on **Render**, the following environment variables are required:

| Variable | Description |
| :--- | :--- |
| `DATABASE_URL` | Supabase JDBC URL (use Port 5432 for Session Pooling) |
| `JWT_SIGNING_KEY` | 32+ character secret string for HS256 signing |
| `RESEND_API_KEY` | API Key for email delivery |
| `QUARKUS_PROFILE` | Set to `prod` to enable production configurations |

### JWT Configuration Note
To resolve the `SRJWT05009` signature error, the service is configured to use symmetric signing:
```properties
smallrye.jwt.new-token.signature-algorithm=HS256
smallrye.jwt.sign.key=${JWT_SIGNING_KEY}

```

## 🔌 API Endpoints

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/auth/signup` | Validates and persists new users; triggers Resend email |
| `POST` | `/api/auth/login` | Returns a signed JWT and user metadata |
| `GET` | `/api/auth/verify` | Updates verification status in Supabase |

## 🏃 Local Development

Run the application in development mode with live coding:

```shell script
./mvnw quarkus:dev

```

Access the **Dev UI** at [http://localhost:8080/q/dev/](http://localhost:8080/q/dev/) to inspect beans and JWT configurations.

## 👤 Author

Shruti - [GitHub Profile](https://github.com/techyJimbei)

## 🔗 Related Repositories

* [Frontend & Middleware Proxy](https://github.com/techyJimbei/auth-frontend)


```
