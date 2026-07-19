# Clinical Diary - Skriptum

Ein Skriptum zur Entwicklung einer Web-Applikation mit Spring Boot (REST API), JPA, Spring Security (JWT), TypeScript und client-seitigem Rendering.

## Voraussetzungen

Dieses Skriptum richtet sich an Studierende mit folgenden Vorkenntnissen:

- **Java & OOP**: Klassen, Vererbung, Interfaces, Generics
- **Grundlagen JPA**: Entitäten, Annotationen, Persistierung
- **Spring Boot mit Thymeleaf**: Controller, Repositories, serverseitiges Rendering
- **HTML & CSS**: Semantisches HTML, Layout mit CSS

Neu in diesem Skriptum sind die **client-seitige Darstellung** mit TypeScript und die Kommunikation über eine **REST API** mit **JWT-basierter Authentifizierung**.

## Projektstruktur

```
web_skriptum/
├── backend/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/clinicaldiary/
│       │   ├── ClinicalDiaryApplication.java  # Einstiegspunkt
│       │   ├── entity/            # JPA-Entitäten (Patient, Doctor, ...)
│       │   ├── repository/        # Spring Data Repositories
│       │   ├── security/          # JWT & Security-Konfiguration
│       │   ├── config/            # SecurityConfig, DataInitializer
│       │   └── controller/        # REST-Controller
│       └── resources/
│           ├── application.properties
│           └── static/            # Frontend-Dateien
│               ├── index.html
│               ├── css/styles.css
│               └── js/app.js      # Kompiliertes TypeScript
├── frontend/
│   ├── tsconfig.json
│   └── ts/app.ts                  # TypeScript-Quellcode
└── Skriptum/                      # Dieses Skriptum
```

## Konventionen

| Thema | Konvention |
|-------|-----------|
| **Packages** | `com.clinicaldiary.<layer>` (controller, entity, repository, config, security) |
| **Klassennamen** | PascalCase: `HealthRecord`, `PatientController` |
| **Variablennamen** | camelCase: `patientName`, `isActive` |
| **REST-Endpoints** | Plural, kebab-case: `/api/health-records`, `/api/patients` |
| **HTTP-Methoden** | GET = lesen, POST = erstellen, PUT = aktualisieren, DELETE = löschen |
| **JSON-Felder** | camelCase: `firstName`, `icdCode` |
| **TypeScript** | Interfaces für DTOs, async/await für API-Aufrufe |
| **Commits** | Conventional Commits: `feat:`, `fix:`, `docs:` |

## Kapitelübersicht

Nr. | Kapitel | Übungen
---|---------|--------
00.0 | [Über dieses Skriptum](skriptum/00.0_UeberDiesesSkriptum.md) | –
01.0 | [Überblick über die Applikation](skriptum/01.0_Ueberblick.md) | –
01.1 | [Technologien im Überblick](skriptum/01.1_Technologien.md) | –
02.0 | [Spring Boot Projektstruktur](skriptum/02.0_SpringBootProjekt.md) | [Übungen](uebungen/UE_02.0_SpringBootProjekt.md)
02.1 | [REST API mit Spring Boot](skriptum/02.1_REST_API.md) | [Übungen](uebungen/UE_02.1_REST_API.md)
03.0 | [JPA Entitäten & Spring Data Repositories](skriptum/03.0_JPA_Entitaeten.md) | [Übungen](uebungen/UE_03.0_JPA_Entitaeten.md)
03.1 | [JPA Relationen & Abfragen](skriptum/03.1_JPA_Relationen.md) | [Übungen](uebungen/UE_03.1_JPA_Relationen.md)
04.0 | [Spring Security mit JWT - Grundlagen](skriptum/04.0_SpringSecurity.md) | [Übungen](uebungen/UE_04.0_SpringSecurity.md)
04.1 | [JWT Token erzeugen & validieren](skriptum/04.1_JWT.md) | [Übungen](uebungen/UE_04.1_JWT.md)
04.2 | [Security Filter Chain konfigurieren](skriptum/04.2_SecurityFilter.md) | [Übungen](uebungen/UE_04.2_SecurityFilter.md)
05.0 | [TypeScript Grundlagen](skriptum/05.0_TypeScript.md) | [Übungen](uebungen/UE_05.0_TypeScript.md)
05.1 | [Single-Page Application mit TypeScript](skriptum/05.1_SPA_mit_TypeScript.md) | [Übungen](uebungen/UE_05.1_SPA_mit_TypeScript.md)
06.0 | [Frontend-Backend Integration](skriptum/06.0_FrontendBackend.md) | [Übungen](uebungen/UE_06.0_FrontendBackend.md)
06.1 | [Datei-Upload & File-Handling](skriptum/06.1_DateiUpload.md) | [Übungen](uebungen/UE_06.1_DateiUpload.md)
07.0 | [Daten initialisieren & Beispieldaten](skriptum/07.0_DatenInitialisieren.md) | [Übungen](uebungen/UE_07.0_DatenInitialisieren.md)
