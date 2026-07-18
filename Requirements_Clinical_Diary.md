# Clinical Diary Web Application - Requirements Specification

## 1. Overview

The Clinical Diary is a secure web application that enables patients to maintain a personal health record, allowing them to track illnesses, vaccinations, medications, and other medical events. Certified doctors can review, confirm, and annotate patient records, including assigning ICD-10 diagnostic codes.

---

## 2. Actors

| Actor | Description |
|-----------------|--------------------------------------------------|
| **Patient** | Registers and manages personal health records |
| **Doctor** | Confirms patient records, assigns ICD-10 codes |
| **Superuser** | System administrator who registers doctors |

---

## 3. Functional Requirements

### 3.1 Patient Module

#### 3.1.1 Registration

- **FR-P-01:** The system shall allow a patient to self-register.
- **FR-P-02:** Registration shall require the following fields:
  - Social Security Number (SSN) — used as unique identifier
  - Full name (first name, last name)
  - Date of birth
  - E-mail address
- **FR-P-03:** The system shall validate the SSN format and uniqueness.
- **FR-P-04:** The system shall verify the e-mail address via a confirmation link.
- **FR-P-05:** The system shall enforce password complexity requirements at registration.
- **FR-P-06:** Duplicate SSN or e-mail registrations shall be rejected with a meaningful error message.

#### 3.1.2 Authentication & Session

- **FR-P-07:** Patients shall log in using their e-mail address and password.
- **FR-P-08:** The system shall support session timeout after a configurable period of inactivity.
- **FR-P-09:** Patients shall be able to log out explicitly.
- **FR-P-10:** Patients shall be able to change their password (requiring the current password for verification).

#### 3.1.3 Health Record Management

- **FR-P-11:** A patient shall be able to create a new health record entry.
- **FR-P-12:** The system shall support the following record categories:
  - **Illness** — symptoms, onset date, end date, severity, notes
  - **Vaccination** — vaccine name, date administered, dose number, batch number, administering institution
  - **Medication** — medication name, dosage, frequency, prescribing doctor, start date, end date
  - **Other** — free-text category for other health-related events
- **FR-P-13:** Each record entry shall store a timestamp of creation and last modification.
- **FR-P-14:** A patient shall be able to view, edit, and delete their own unconfirmed records.
- **FR-P-15:** Records confirmed by a doctor shall only be editable or deletable by a doctor.
- **FR-P-16:** The system shall display a chronological timeline view of all records.
- **FR-P-17:** A patient shall be able to attach one or more files (e.g., lab results, scans) to a health record entry.
- **FR-P-18:** The system shall support file uploads in common formats (PDF, JPEG, PNG) with a configurable maximum file size.
- **FR-P-19:** The patient or their doctor shall be able to view and download attached files from a record.
- **FR-P-20:** The patient shall be able to generate and download a PDF report containing all of their health records.

#### 3.1.4 Doctor Assignment

- **FR-P-21:** A patient shall be able to assign themselves to one or more doctors.
- **FR-P-22:** The system shall provide a search/lookup functionality for doctors (by name, specialization, or medical license number).
- **FR-P-23:** A patient shall be able to view a list of their currently assigned doctors.
- **FR-P-24:** A patient shall be able to remove a doctor from their assigned doctors list.

---

### 3.2 Doctor Module

#### 3.2.1 Authentication

- **FR-D-01:** Doctors shall log in using credentials assigned by the superuser.
- **FR-D-02:** The system shall differentiate the doctor role from patient and superuser roles.
- **FR-D-03:** Doctors shall be able to change their password (requiring the current password for verification).

#### 3.2.2 Patient Record Confirmation

- **FR-D-04:** A doctor shall be able to view records of patients under their care.
- **FR-D-05:** A doctor shall be able to confirm a patient's record entry.
- **FR-D-06:** A confirmed record shall display the confirming doctor's name, medical license number, and the confirmation timestamp.
- **FR-D-07:** A doctor shall be able to add a comment or annotation when confirming a record.

#### 3.2.3 ICD-10 Code Assignment

- **FR-D-08:** A doctor shall be able to assign one or more ICD-10 codes to a confirmed record (where applicable).
- **FR-D-09:** The system shall provide an ICD-10 code search/lookup functionality (by code or description).
- **FR-D-10:** The system shall display the ICD-10 code and its short description alongside the record.
- **FR-D-11:** A doctor shall be able to modify or remove previously assigned ICD-10 codes.

---

### 3.3 Superuser Module

#### 3.3.1 Doctor Registration

- **FR-S-01:** The superuser shall be able to register a new doctor in the system.
- **FR-S-02:** Doctor registration shall require:
  - Full name
  - Medical license number (unique)
  - Specialization
  - E-mail address
- **FR-S-03:** The system shall generate a temporary password and send it to the doctor's e-mail.
- **FR-S-04:** The superuser shall be able to view a list of all registered doctors.
- **FR-S-05:** The superuser shall be able to deactivate a doctor's account.

#### 3.3.2 System Administration

- **FR-S-06:** The superuser shall have access to a dashboard showing system usage statistics.
- **FR-S-07:** The superuser shall be able to manage user accounts (activate/deactivate).
- **FR-S-08:** The superuser shall be able to change their password.

---

## 4. Non-Functional Requirements

### 4.1 Security

- **NFR-01:** All communication between client and server shall be encrypted using TLS 1.2+.
- **NFR-02:** Passwords shall be stored using a strong, salted hashing algorithm (e.g., bcrypt, Argon2).
- **NFR-03:** The application shall comply with applicable data protection regulations (e.g., GDPR).
- **NFR-04:** Patient health data shall be accessible only to the patient themselves and their assigned doctor(s).
- **NFR-05:** The system shall maintain an audit log of all record modifications and confirmations.
- **NFR-06:** The system shall implement role-based access control (RBAC).

### 4.2 Availability & Performance

- **NFR-07:** The system shall be available 99.5% of the time (scheduled maintenance excluded).
- **NFR-08:** Page load time shall not exceed 3 seconds under normal load.
- **NFR-09:** The system shall support at least 100 concurrent users.

### 4.3 Data Integrity

- **NFR-10:** Confirmed records shall be immutable to patients; only a doctor may modify or delete them.
- **NFR-11:** The system shall perform regular automated backups (daily minimum).
- **NFR-12:** The database shall enforce referential integrity constraints.

### 4.4 Usability

- **NFR-13:** The application shall be responsive and usable on desktop, tablet, and mobile devices.
- **NFR-14:** The application shall provide a user-friendly interface accessible to non-technical users.
- **NFR-15:** Error messages shall be clear and actionable.

### 4.5 Compatibility

- **NFR-16:** The application shall support the two latest major versions of Chrome, Firefox, Safari, and Edge.
- **NFR-17:** The system shall provide a RESTful API for potential future integrations.

---


## 5. Assumptions & Constraints

1. The SSN format follows national regulations of Austria.
2. ICD-10 code data is maintained as a reference dataset within the application.
3. Each patient may be assigned to one or more doctors.
4. Only confirmed records can have ICD-10 codes assigned.
5. The superuser is a predefined account created during initial system deployment.

---

