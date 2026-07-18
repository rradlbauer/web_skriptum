# Clinical Diary Web Application

A secure web application for patients to maintain personal health records and for doctors to review, confirm, and annotate them.

## Prerequisites

- **Java 17+** (tested with Java 24)
- **Node.js 18+** and npm (for TypeScript compilation)
- **Maven 3.8+** (downloaded to `C:\tools\apache-maven-3.9.6` during setup)

## Quick Start

### 1. Compile TypeScript

```bash
cd frontend
npx tsc
```

This compiles `ts/app.ts` to `backend/src/main/resources/static/js/app.js`.

### 2. Build the Backend

```bash
cd backend
mvn package -q -DskipTests
```

### 3. Run

```bash
cd backend
java -jar target/clinical-diary-1.0.0.jar
```

The application starts at **http://localhost:8080**

### 4. Open in Browser

Navigate to **http://localhost:8080** and log in.

## Demo Accounts

| Role | Email | Password |
|------|-------|----------|
| Patient | anna.schmidt@email.at | password123 |
| Patient | max.muster@email.at | password123 |
| Patient | maria.weber@email.at | password123 |
| Doctor | dr.tandler@klinik.at | doctor123 |
| Doctor | dr.müller@klinik.at | doctor123 |
| Doctor | dr.bauer@klinik.at | doctor123 |
| Admin | admin@clinicaldiary.at | admin |

## Architecture

### Backend (Spring Boot)

```
backend/src/main/java/com/clinicaldiary/
├── ClinicalDiaryApplication.java    # Entry point
├── entity/                          # JPA entities (database tables)
│   ├── Patient.java
│   ├── Doctor.java
│   ├── HealthRecord.java
│   ├── Icd10Code.java
│   ├── DoctorPatient.java           # Doctor-Patient assignment
│   ├── RecordConfirmation.java      # Doctor confirms a record
│   └── RecordIcd10.java            # ICD-10 code on a record
├── repository/                      # Database access (Spring Data JPA)
├── security/                        # JWT authentication
│   ├── JwtTokenProvider.java        # Creates & validates JWT tokens
│   ├── JwtAuthenticationFilter.java # Intercepts requests, checks token
│   └── UserPrincipal.java          # Holds current user info
├── config/
│   ├── SecurityConfig.java         # Spring Security configuration
│   └── DataInitializer.java        # Loads sample data on startup
└── controller/                      # REST API endpoints
    ├── AuthController.java          # Login, registration
    ├── PatientController.java       # Patient operations
    ├── DoctorController.java        # Doctor operations
    └── SuperuserController.java     # Admin operations
```

### Frontend (Vanilla TypeScript)

```
frontend/
├── tsconfig.json                    # TypeScript configuration
└── ts/app.ts                       # Single-file SPA application

backend/src/main/resources/static/
├── index.html                       # Main HTML page
├── css/styles.css                  # Styling
└── js/app.js                       # Compiled TypeScript
```

The frontend is a **Single-Page Application (SPA)** built without any framework. It uses:
- **State management** via a global `state` object
- **DOM manipulation** for view rendering
- **Fetch API** for HTTP requests
- **localStorage** for session persistence

### Key Concepts

#### Authentication Flow
1. User submits credentials to `/api/auth/login`
2. Backend validates and returns a **JWT token**
3. Frontend stores token in `localStorage`
4. All subsequent requests include `Authorization: Bearer <token>` header
5. `JwtAuthenticationFilter` validates the token on each request

#### Role-Based Access Control (RBAC)
- **PATIENT**: Can manage own health records, assign doctors
- **DOCTOR**: Can view assigned patients, confirm records, assign ICD-10 codes
- **SUPERUSER**: Can register/deactivate doctors, view statistics

#### Health Record Lifecycle
1. Patient creates a record -> status: **Unconfirmed**
2. Patient can edit/delete unconfirmed records
3. Assigned doctor reviews and confirms -> status: **Confirmed**
4. Doctor can add ICD-10 diagnostic codes to confirmed records
5. Confirmed records become immutable for the patient

#### Doctor-Patient Assignment
- Patients can search for and assign themselves to doctors
- Doctors can then see the patient's records and confirm them
- A patient can have multiple doctors

## API Endpoints

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/login` | Login (returns JWT) |
| POST | `/api/auth/register/patient` | Register as patient |
| POST | `/api/auth/register/doctor` | Register doctor (superuser only) |

### Patient
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/patients/me` | Get own profile |
| GET | `/api/patients/me/records` | List own records |
| POST | `/api/patients/me/records` | Create record |
| PUT | `/api/patients/me/records/{id}` | Update record |
| DELETE | `/api/patients/me/records/{id}` | Delete record |
| GET | `/api/patients/me/doctors` | List assigned doctors |
| POST | `/api/patients/me/doctors/{doctorId}` | Assign doctor |
| DELETE | `/api/patients/me/doctors/{doctorId}` | Remove doctor |
| GET | `/api/patients/doctors/search?q=` | Search doctors |

### Doctor
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/doctors/me` | Get own profile |
| GET | `/api/doctors/me/patients` | List assigned patients |
| GET | `/api/doctors/me/patients/{id}/records` | View patient records |
| POST | `/api/doctors/records/{id}/confirm` | Confirm record |
| POST | `/api/doctors/records/{id}/icd10` | Assign ICD-10 code |
| DELETE | `/api/doctors/records/{id}/icd10/{icd10Id}` | Remove ICD-10 |
| GET | `/api/doctors/icd10/search?q=` | Search ICD-10 codes |

### Superuser
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/superuser/doctors` | List all doctors |
| POST | `/api/superuser/doctors` | Register new doctor |
| PUT | `/api/superuser/doctors/{id}/activate` | Activate doctor |
| PUT | `/api/superuser/doctors/{id}/deactivate` | Deactivate doctor |
| GET | `/api/superuser/stats` | System statistics |

## Technologies

| Component | Technology |
|-----------|-----------|
| Backend | Spring Boot 3.4, Spring Data JPA, Spring Security |
| Database | H2 (in-memory) |
| Authentication | JWT (JSON Web Tokens) via jjwt library |
| Frontend | Vanilla TypeScript, HTML5, CSS3 |
| Build | Maven |

## H2 Console

Access the H2 database console at **http://localhost:8080/h2-console**:
- JDBC URL: `jdbc:h2:mem:clinicaldiary`
- Username: `sa`
- Password: *(empty)*
