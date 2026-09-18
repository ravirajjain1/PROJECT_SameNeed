# PROJECT_SameNeed

A Java web application that lets multiple customers with the same service need pool together into a group and collectively negotiate with a service provider for a shared visit.

## Overview

SameNeed addresses a common problem: when several people in a locality need the same service (e.g., washing machine repair), each pays a solo call-out fee. The platform lets those customers find each other, form a group for a single visit, and negotiate a per-member price with a verified provider — reducing cost for customers and increasing job value for providers.

## Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Web Framework | Jakarta Servlet 6.0 (annotation-driven, no XML servlet declarations) |
| Persistence | JPA 3.1 with Hibernate ORM 6.4.4.Final |
| Database | MySQL 8 |
| JDBC Driver | MySQL Connector/J 8.3.0 |
| JSON | Jackson Databind 2.17.0 + JSR-310 module |
| Password Hashing | jBCrypt 0.4 |
| Real-time Chat | Jakarta WebSocket 2.1.0 |
| Build Tool | Apache Maven 3 |
| Packaging | WAR (deployed to Apache Tomcat 10.1+) |
| Frontend | Plain HTML, CSS, JavaScript (no frontend framework) |

> Tomcat 10.1 or later is required because the project uses the `jakarta.*` namespace introduced in Jakarta EE 9.

## Prerequisites

- JDK 17
- Apache Tomcat 10.1+
- MySQL 8.0+
- Apache Maven 3.8+

## Database Setup

1. Create a schema named `sameneed` in MySQL.
2. Run `database.sql` against that schema. The script drops and recreates all tables and inserts seed data (service categories, services, one sample provider, and sample service requests).

```sql
mysql -u root -p sameneed < database.sql
```

## Configuration

Database credentials are stored in two places (both must match):

**`src/main/resources/db.properties`** — used by the raw JDBC layer (`DatabaseConfig`).

**`src/main/resources/META-INF/persistence.xml`** — used by the JPA/Hibernate layer for the three JPA-managed entities (`ServiceCategory`, `Service`, `ProviderProfile`).

Update the `db.url`, `db.username`, and `db.password` values in both files before building.

## Build and Deploy

```bash
mvn clean package
copy target\SameNeed-1.0.0.war <TOMCAT_HOME>\webapps\ROOT.war
```

Start Tomcat and navigate to `http://localhost:8080`.

## Project Structure

```
SameNeed/
├── database.sql                          Schema and seed data
├── pom.xml
└── src/main/
    ├── java/com/sameneed/
    │   ├── AppLifecycleListener.java     ServletContextListener (startup/shutdown)
    │   ├── config/                       DatabaseConfig, JpaConfig
    │   ├── controller/                   15 HttpServlet classes (REST API)
    │   ├── dao/                          9 DAO classes (JDBC + JPA)
    │   ├── enums/                        6 enum types
    │   ├── exception/                    Custom exception classes
    │   ├── filter/                       AuthFilter, RoleFilter
    │   ├── model/                        17 entity/model classes
    │   ├── service/                      10 service classes
    │   ├── thread/                       NotificationDispatcher, OfferExpiryWorker
    │   ├── util/                         JsonUtil, PasswordUtil, DateUtil
    │   └── websocket/                    GroupChatEndpoint, HttpSessionConfigurator
    ├── resources/
    │   ├── db.properties
    │   └── META-INF/persistence.xml
    └── webapp/
        ├── index.html                    Landing page (public)
        ├── login.html / register.html    Auth pages (public)
        ├── dashboard.html                Customer dashboard
        ├── create-request.html           New service request form
        ├── requests.html                 Browse open requests
        ├── request-detail.html           Group detail, offers, chat
        ├── bookings.html                 Bookings list
        ├── provider-dashboard.html       Provider dashboard
        ├── provider-requests.html        Requests eligible for provider
        ├── profile.html                  User profile
        ├── notifications.html            Notification inbox
        ├── reviews.html                  Review submission
        ├── admin/                        Admin sub-portal (6 pages)
        ├── css/style.css                 Single stylesheet for all pages
        └── js/                           api.js, requests.js, offers.js, group.js, admin.js
```

## API Endpoints

All API endpoints are prefixed with `/api/` and return JSON.

| Servlet | URL Pattern | Methods | Purpose |
|---|---|---|---|
| AuthServlet | `/api/auth/*` | POST | Login, register, logout |
| RequestServlet | `/api/requests/*` | GET, POST, PUT | Service requests, matching |
| MemberServlet | `/api/members/*` | GET, POST, DELETE | Group membership |
| OfferServlet | `/api/offers/*` | GET, POST | Submit offers, negotiate, accept/reject |
| BookingServlet | `/api/bookings/*` | GET, POST, PUT | Booking management |
| ReviewServlet | `/api/reviews/*` | GET, POST | Reviews |
| NotificationServlet | `/api/notifications/*` | GET, POST | Notifications |
| ChatServlet | `/api/chat/*` | GET | Fetch chat history |
| UserServlet | `/api/users/*` | GET, PUT | User profile |
| MetaServlet | `/api/meta/*` | GET | App metadata |
| CategoryServlet | `/api/categories/*` | GET, POST, PUT, DELETE | Service categories |
| ServiceServlet | `/api/services/*` | GET | Services list |
| AdminServlet | `/api/admin/*` | GET, POST | Admin operations |
| ReportServlet | `/api/reports/*` | GET, POST | Abuse reports |
| ExportServlet | `/api/export/*` | GET | CSV export of bookings |

WebSocket endpoint: `ws://<host>/ws/chat/{requestId}` — real-time group chat per service request.

## User Roles

- **CUSTOMER** — creates service requests, joins groups, negotiates offers, books services, leaves reviews.
- **SERVICE_PROVIDER** — views eligible group requests, submits offers, manages bookings.
- **ADMIN** — manages users, providers, service categories, reports via the `/admin/` sub-portal.

## Session and Security

- Sessions are server-side HTTP sessions managed by Tomcat (60-minute timeout, HttpOnly cookies).
- `AuthFilter` protects all non-public paths. Unauthenticated API calls receive HTTP 401; unauthenticated page requests redirect to `/login.html`.
- `RoleFilter` enforces role-based access on role-restricted paths.
- Passwords are hashed with BCrypt via jBCrypt.

## Background Threads

Two daemon threads start at application startup via `AppLifecycleListener`:

- **OfferExpiryWorker** — checks every 60 seconds for offers whose `valid_until` has passed and marks them `EXPIRED`.
- **NotificationDispatcher** — processes a notification queue and persists notifications to the `notifications` table.

## Database Schema Summary

| Table | Purpose |
|---|---|
| `users` | All users (customers, providers, admins) |
| `service_providers` | Provider profile extension (1-to-1 with users) |
| `service_categories` | Top-level categories |
| `services` | Individual services under each category |
| `service_requests` | Customer service requests with group-forming state |
| `request_members` | Members of each request group (with anonymous alias) |
| `provider_services` | Services a provider offers |
| `provider_availability` | Provider weekly schedule |
| `offers` | Provider offers for a request group |
| `offer_negotiations` | Per-round negotiation history on an offer |
| `offer_responses` | Individual member responses to an accepted offer |
| `bookings` | Confirmed bookings |
| `chat_messages` | Group chat messages per request |
| `reviews` | Post-booking reviews |
| `notifications` | In-app notifications per user |
| `reports` | Abuse reports |
| `disputes` | Disputes filed against a booking |

## Known Limitations

- The JPA persistence unit only manages three entities (`ServiceCategory`, `Service`, `ProviderProfile`). All other DB operations use raw JDBC.
- There is no email verification flow for new registrations.
- Provider availability is stored in the database but there is no frontend UI to view or edit it.
- The CSV export endpoint exists in `ExportServlet` but is not yet wired to a button in the admin frontend.
