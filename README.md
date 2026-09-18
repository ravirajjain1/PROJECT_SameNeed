# SameNeed - Connecting People with Shared Service Needs

A Java web application that helps people who need the same local service come together, form a group, and collectively coordinate and negotiate with a service provider for a shared visit.

## Overview

SameNeed solves a simple but common problem: when multiple people in the same area need the same service, such as washing machine repair, each person usually has to arrange a separate service visit and may pay the full visit or service charge individually.

SameNeed allows customers with the same service requirement, even if their individual problems are different, to discover each other and form a group. Once a group is formed, they can communicate through a private group chat, connect with a suitable service provider, and negotiate a group-based price for servicing multiple customers in a single visit.

For example, one customer may have a washing machine that is not draining, while another has a leakage problem. Their problems are different, but both need washing machine repair, so they can be part of the same group.

The platform is designed to make local service coordination more organized and give customers an opportunity to negotiate collectively, while allowing service providers to handle multiple nearby service requests more efficiently.


## Features

### Customer Features

* User registration and secure login
* Create and manage service requests
* Browse and search open service requests
* Find customers looking for the same service
* Join and leave service groups
* Anonymous member identities during group formation
* Real-time group chat using WebSocket
* View provider offers
* Participate in group price negotiation
* Accept or decline offers
* Manage confirmed bookings
* Track service status
* Receive in-app notifications
* Submit reviews after completed services
* Manage personal profile
* Report issues and raise disputes

### Service Provider Features

* Provider registration and login
* Manage provider profile
* Add and manage offered services
* Define service areas
* Manage availability
* View suitable service groups
* View individual customer requirements within a group
* Submit group-based price offers
* Negotiate with the group coordinator
* Manage bookings
* Update service status
* View booking history
* Receive customer reviews

### Admin Features

* Admin authentication
* Dashboard with application statistics
* Manage customers and service providers
* Verify and manage service providers
* Manage service categories and services
* Monitor service requests and bookings
* Handle user reports and disputes
* Suspend or manage user accounts
* View application activity
* Export booking data as CSV

### Core Platform Features

* Same-service grouping even when customer problems are different
* Locality-based and availability-based request matching
* Configurable group size with a maximum of 10 members
* Request-specific anonymous identities
* Real-time WebSocket communication
* Provider offer and counter-offer system
* Offer expiry handling
* Role-based access control
* Session-based authentication
* BCrypt password hashing
* Input validation and error handling
* JDBC and JPA/Hibernate persistence
* MySQL database storage
* Java I/O based CSV export
* Java Reflection-based entity metadata utility
* Background processing for offer expiry and notifications


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

## Steps to Install & Run the Project

### 1. Install Prerequisites

Make sure the following software is installed on your system:

* JDK 17 or later
* Apache Maven 3.8+
* MySQL 8.0+
* Apache Tomcat 10.1+
* A web browser
* An IDE such as IntelliJ IDEA, Eclipse, or VS Code

Verify the installations:

```bash
java -version
mvn -version
mysql --version
```

### 2. Create the Database

Open MySQL and create the `sameneed` database:

```sql
CREATE DATABASE sameneed;
```

Select the database:

```sql
USE sameneed;
```

Run the provided `database.sql` file to create the required tables and insert the initial service categories, services, provider data, and sample service requests.

Alternatively, from the project directory:

```bash
mysql -u root -p sameneed < database.sql
```

### 3. Configure Database Connection

Open:

```text
src/main/resources/db.properties
```

Update the database details according to your MySQL installation:

```properties
db.url=jdbc:mysql://localhost:3306/sameneed
db.username=root
db.password=your_password
```

Update the corresponding database connection properties in:

```text
src/main/resources/META-INF/persistence.xml
```

Make sure both configurations point to the same MySQL database.

### 4. Build the Project

Open a terminal in the root directory of the project and run:

```bash
mvn clean package
```

Maven will compile the Java source files, resolve dependencies, run the configured build process, and generate the WAR file inside the `target` directory.

The generated file will be:

```text
target/SameNeed-1.0.0.war
```

### 5. Deploy to Apache Tomcat

Copy the generated WAR file into the Tomcat `webapps` directory.

For Windows:

```bash
copy target\SameNeed-1.0.0.war <TOMCAT_HOME>\webapps\ROOT.war
```

For Linux/macOS:

```bash
cp target/SameNeed-1.0.0.war <TOMCAT_HOME>/webapps/ROOT.war
```

Using `ROOT.war` makes the application available directly from the Tomcat root URL.

### 6. Start Tomcat

Start the Tomcat server.

On Windows:

```text
<TOMCAT_HOME>\bin\startup.bat
```

On Linux/macOS:

```bash
<TOMCAT_HOME>/bin/startup.sh
```

Wait for Tomcat to deploy the application successfully.

### 7. Open the Application

Open a browser and visit:

```text
http://localhost:8080
```

The SameNeed home page should appear.

### 8. Test the Application

The application can be tested using the seeded data or by creating new accounts.

#### Customer Testing

1. Register a customer account.
2. Log in to the customer dashboard.
3. Create a service request.
4. Select a service such as `Washing Machine Repair`.
5. Enter the problem, locality, preferred date, time, and budget.
6. Browse other open requests for the same service.
7. Join an existing group.
8. Open the group page and verify the anonymous member identities.
9. Send and receive messages through the group chat.
10. Check provider offers and negotiation details.
11. Accept or decline an offer.
12. Complete the booking workflow.
13. Submit a review after the service is completed.

#### Service Provider Testing

1. Register a service provider account.
2. Log in to the provider dashboard.
3. Complete the provider profile.
4. Add the services offered.
5. View eligible service groups.
6. Open a group to view its aggregated requirements.
7. Submit a group-based offer.
8. Send a counter-offer when required.
9. Accept a negotiated offer.
10. Update the booking status as the service progresses.

#### Admin Testing

1. Log in using an administrator account.
2. View dashboard statistics.
3. Manage customers and service providers.
4. Verify or manage providers.
5. Manage service categories and services.
6. Monitor service requests and bookings.
7. Review reports and disputes.
8. Suspend or manage accounts when required.
9. Test the CSV export functionality.
10. Access the restricted entity metadata functionality.

### 9. Verify WebSocket Chat

After joining a service group, open the same group from two authenticated browser sessions or accounts.

Send a message from one member and verify that it appears in the other active session without manually refreshing the page.

The WebSocket endpoint is:

```text
ws://localhost:8080/ws/chat/{requestId}
```

### 10. Stop the Application

When testing is complete, stop Tomcat using:

Windows:

```text
<TOMCAT_HOME>\bin\shutdown.bat
```

Linux/macOS:

```bash
<TOMCAT_HOME>/bin/shutdown.sh
```

### Troubleshooting

#### MySQL Connection Error

Check that:

* MySQL server is running.
* The `sameneed` database exists.
* The username and password are correct.
* The JDBC URL uses the correct port.

#### Application Does Not Start

Check the Tomcat console and logs for deployment or configuration errors.

Also verify that the project is being deployed to **Tomcat 10.1+**, because SameNeed uses the `jakarta.*` namespace.

#### WAR File Is Not Generated

Run:

```bash
mvn clean package
```

and check the Maven output for compilation or dependency errors.

#### Port 8080 Is Already in Use

Change the Tomcat HTTP connector port in:

```text
<TOMCAT_HOME>/conf/server.xml
```

Then access the application using the updated port.

For example:

```text
http://localhost:8081
```

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

## Instructions for Testing

SameNeed can be tested by running the application on Apache Tomcat and using separate browser sessions for different user roles.

### 1. Customer Registration and Login

1. Open the application at `http://localhost:8080`.
2. Open the registration page.
3. Create a new customer account with valid details.
4. Log in using the registered credentials.
5. Verify that the customer dashboard is displayed.
6. Test logout and confirm that protected pages cannot be accessed without authentication.

### 2. Create a Service Request

1. Log in as a customer.
2. Open **Create Service Request**.
3. Select a service category and service.
4. Enter the individual problem.
5. Enter the locality, preferred date, preferred time, and expected budget.
6. Set the required group size.
7. Submit the request.
8. Verify that the request appears in the list of available requests.

Example:

```text
Service: Washing Machine Repair
Problem: Machine is not draining
Locality: Kolar Road
Preferred Date: 25 September 2026
Preferred Time: 11:00 AM - 2:00 PM
Expected Budget: ₹700
```

### 3. Test Same-Service Grouping

1. Create another customer account using a separate browser or incognito window.
2. Log in as the second customer.
3. Create or browse a request for the same service.
4. Use a different problem description, such as `water leakage`.
5. Verify that the customer can discover the washing machine repair group.
6. Join the group.
7. Confirm that the group count is updated.

The test should demonstrate that customers are grouped by the **service they need**, not by having identical problem descriptions.

### 4. Test Anonymous Group Members

1. Open the group as a customer.
2. Verify that other members are displayed using anonymous identities such as:

```text
Member 01
Member 02
Member 03
```

3. Verify that members cannot see another member's private contact information during group formation.

### 5. Test Real-Time Group Chat

1. Open the same group using two authenticated browser sessions.
2. Send a message from the first customer.
3. Verify that the message appears in the second session without refreshing the page.
4. Send a reply from the second customer.
5. Verify that the first session receives the message.
6. Refresh the group and verify that previously sent messages remain available.

This tests both the WebSocket communication and database persistence of chat messages.

### 6. Test Service Provider Workflow

1. Register or log in as a service provider.
2. Complete the provider profile.
3. Add the relevant service, such as `Washing Machine Repair`.
4. Open the provider request/group section.
5. Verify that suitable service groups are displayed.
6. Open a group and verify that the provider can see the service, locality, preferred schedule, group size, and individual service issues.
7. Submit a group-based price offer.

### 7. Test Offer and Negotiation

Use a simple negotiation scenario:

```text
Provider Offer:       ₹800 per member
Coordinator Counter:  ₹600 per member
Provider Counter:     ₹700 per member
Coordinator Counter:  ₹650 per member
Provider Final Offer: ₹675 per member
```

Verify that:

* Each offer is stored in the database.
* Previous offers remain visible in the negotiation history.
* The latest offer has the correct status.
* Members can accept or decline the offer.
* An expired offer cannot be accepted after its validity period.
* A booking can be created only after the required offer conditions are satisfied.

### 8. Test Booking and Service Status

Verify the booking workflow:

```text
REQUESTED
    ↓
GROUP_FORMING
    ↓
PROVIDER_CONTACTED
    ↓
OFFER_RECEIVED
    ↓
NEGOTIATING
    ↓
OFFER_ACCEPTED
    ↓
BOOKED
    ↓
IN_PROGRESS
    ↓
COMPLETED
    ↓
REVIEWED
```

Verify that valid status changes are saved correctly and displayed to the relevant users.

### 9. Test Reviews

1. Complete a booking.
2. Open the review section.
3. Submit a rating and review.
4. Verify that the review is stored.
5. Verify that reviews are associated with the correct completed service.

### 10. Test Admin Functions

1. Log in using an administrator account.
2. Open the admin dashboard.
3. Verify application statistics.
4. Test customer and provider management.
5. Test provider verification.
6. Test service category and service management.
7. Review reports and disputes.
8. Test account suspension or management.
9. Test CSV export.
10. Test the restricted entity metadata feature.

### 11. Test Validation and Error Handling

Test invalid or incomplete inputs, including:

* Empty required fields
* Invalid email format
* Invalid password
* Invalid date or time
* Invalid budget
* Group size greater than the configured maximum
* Joining a full group
* Joining the same group twice
* Accessing another user's private data
* Accessing customer functionality as a provider
* Accessing admin functionality without admin privileges
* Submitting an offer for an unavailable request

The application should display an appropriate error response instead of failing unexpectedly.

### 12. Test Database Persistence

After performing important operations, verify that the corresponding records are stored in MySQL.

Test records should include:

* Users
* Service requests
* Request members
* Chat messages
* Offers
* Offer negotiations
* Offer responses
* Bookings
* Reviews
* Notifications
* Reports

Restart Tomcat and verify that persistent data is still available.

# Screenshots

### Homepage

![SameNeed Homepage](SameNeed/screenshots/homepage.png)

### Login Page

![SameNeed Login](SameNeed/screenshots/login.png)

### Register Page

![SameNeed Dashboard](SameNeed/screenshots/user_dashboard.png)

### User Dashboard

![SameNeed Dashboard](SameNeed/screenshots/user_dashboard.png)

### Service Provider Dashboard

![SameNeed Dashboard](SameNeed/screenshots/provider_dashboard.png)
