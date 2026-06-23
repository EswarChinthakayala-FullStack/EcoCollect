# 🔒 EcoCollect: Security & Access Control Specifications

This document outlines the security architecture, encryption standards, authentication models, and data protection mechanisms enforced across EcoCollect.

---

## 🔑 Authentication & Token Management

EcoCollect uses stateless Token-based Authentication to verify requests:

```
+-------------+                    +-------------+                    +-------------+
|   Client    |                    |  API Router |                    |  Controller |
+-------------+                    +-------------+                    +-------------+
       |                                  |                                  |
       | POST /auth/login                 |                                  |
       |--------------------------------->|                                  |
       |                                  | Authenticates credentials        |
       |                                  | Hashing match check              |
       |                                  |---------------------------\      |
       |                                  |<--------------------------/      |
       | Returns JWT Token                |                                  |
       |<---------------------------------|                                  |
       |                                  |                                  |
       | GET /profile (Bearer JWT)        |                                  |
       |--------------------------------->|                                  |
       |                                  | Verifies signature, role         |
       |                                  |--------------------------------->|
       |                                  |                                  | Returns Protected Data
       |                                  |<---------------------------------|
       | Returns Profile Data             |                                  |
       |<---------------------------------|                                  |
```

### 1. JSON Web Tokens (JWT)
* **Algorithm**: `HS256` (HMAC using SHA-256 hash algorithm).
* **Payload Claims**:
  * `sub` (Subject): The user's registered email address.
  * `role`: User authorization level (`citizen`, `supervisor`, `admin`).
  * `exp` (Expiration): Set to 7 days (`60 * 24 * 7` minutes) from the time of issue.
* **Storage**:
  * **Web Client**: Stored locally in `localStorage` for session persistence.
  * **Android Client**: Persisted in encrypted local state handlers.

### 2. Password Hashing & Salt Rounds
* **Library**: `bcrypt` (compiled wrapper).
* **Work Factor**: 12 rounds of salting, protecting against brute-force and dictionary attacks.
* **Storage**: Under no circumstances are raw passwords logged or stored. The database only tracks the `password_hash` string.

---

## 🛡️ Role-Based Access Control (RBAC)

API routes and client interface directories are isolated into three permissions groups:

### 1. Backend Endpoint Guards
FastAPI router endpoints utilize dependency injection to enforce role criteria:
* **Citizen Routes** (`/api/citizen/*`): Requires token payload role to be `citizen`.
* **Supervisor Routes** (`/api/supervisor/*`): Requires token payload role to be `supervisor`.
* **Admin Routes** (`/api/admin/*`): Requires token payload role to be `admin`.

If a client attempts to reach an endpoint outside their scope, the backend raises `403 Forbidden` or `401 Unauthorized` HTTP exceptions.

### 2. Frontend Navigation Guards
* **React Web Portal**: Wrap routes in a `<ProtectedRoute allowedRoles={['admin']}>` wrapper. If a user lacks the matching role, they are redirected back to their respective portal gateway.
* **Android Kotlin Client**: Screens check local `TokenManager.userRole` definitions. Navigation bars adapt options dynamically based on whether a Citizen or Supervisor is authenticated.

---

## 📧 Secure OTP Verification Flow

Password recovery utilizes a secure one-time passcode (OTP) flow:

```
[ForgotPassword Request] ──> Generate 6-digit random code ──> Hash and store with expires_at (10 min)
                                                                   │
                                                                   v
                                                            Send code via SMTP
                                                                   │
                                                                   v
[VerifyOTP Request] <── User enters code from email <──────────────┘
         │
         ├── Code mismatch / Expired ──> Throw 400 Bad Request
         └── Code matches ─────────────> Mark OTP is_verified = True in DB
                                               │
                                               v
[ResetPassword Request] <── User submits new password + matching code
         │
         ├── Checks is_verified is True ──> Hashes password & updates User record
         └── Otherwise ───────────────────> Throw 400 Bad Request
```

1. **Generation**: The server generates a high-entropy 6-digit numeric string.
2. **Expiration**: Saved in the `otps` table with an expiration timestamp set to 10 minutes from creation.
3. **Consumption**: Once verified and used to reset a password, the OTP is deleted or cleared to prevent replay attacks.

---

## 💾 Injection & Sanitation Defenses

* **SQL Injection (SQLi)**: All database interactions utilize SQLAlchemy's query constructor or parameterized values. Raw statements (such as text filters) compile safely via bind parameters.
* **Input Schema Sanitation**: API payloads are strictly validated against **Pydantic** models (e.g. `UserResponse`, `IssueReportCreatePayload`). Payloads that contain non-conforming parameters or additional keys are rejected with `422 Unprocessable Entity`.
* **CORS Settings**: Cross-Origin Resource Sharing is locked down to prevent scripts on foreign sites from issuing requests against the database on behalf of users.
