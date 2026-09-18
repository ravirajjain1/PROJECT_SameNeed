# SameNeed — A Community Service Pooling Platform

## Background

When a household appliance breaks down or a plumbing issue arises, a customer typically contacts a service technician individually. In many cases, the technician charges a visit or service fee for travelling to the customer's location. At the same time, other customers in the same locality may have similar service requirements around the same period, but there is usually no simple way for them to coordinate their requests and approach a service provider together.

As a result, customers generally arrange separate service visits, while service providers may need to make multiple trips to the same locality for individual customers.

## Problem Statement

Existing service-booking platforms generally handle customer requests independently. They do not provide a dedicated mechanism for customers who need the **same service** in a compatible locality and time period to form a group, coordinate their requirements, and collectively negotiate with a service provider.

For example, one customer may need washing machine repair because the machine is not draining, while another may need repair because of water leakage. Although their individual problems are different, both customers require the same service: **Washing Machine Repair**.

The problem is therefore to design a platform that can identify compatible customers based on their common service requirement and allow them to coordinate and negotiate collectively with a service provider.

## Proposed Solution

**SameNeed** is a Java-based web application that implements a group-service-pooling workflow:

1. A customer creates a service request specifying the service, locality, preferred date and time, individual problem, and optional budget expectation.

2. Other customers looking for the **same service** in a compatible locality and time period can discover the request and join its group.

3. The group maintains anonymous member identities during the group-formation stage to protect members' personal information.

4. A service provider can view eligible groups and their aggregated service requirements, including the different individual problems reported by members.

5. The provider can submit a per-member price offer to the group.

6. The group coordinator can negotiate through counter-offers, with each offer and negotiation step stored as part of the negotiation history.

7. After the coordinator accepts an offer, individual group members can confirm or decline their participation.

8. When the required participation conditions are satisfied, a booking is created and the relevant users are notified.

9. A real-time group chat using WebSocket allows members to communicate while coordinating the service.

The platform does not guarantee a discount. Instead, it provides a structured way for customers to **pool compatible service requirements and negotiate collectively**, which may create an opportunity for a group-based price.

## Objectives

* Implement multi-role user management for **Customer, Service Provider, and Admin** roles.
* Provide session-based authentication with BCrypt password hashing.
* Implement service-based request matching using service type, locality, preferred date, and compatible time requirements.
* Allow customers with different individual problems to join the same group when they require the same service.
* Implement request-specific anonymous identities for group members.
* Provide a structured offer and counter-offer negotiation system with persistent negotiation history.
* Provide real-time group communication using Jakarta WebSocket.
* Implement booking creation and service-status tracking.
* Automatically expire offers after their validity period using background processing.
* Provide in-app notifications for important group, offer, booking, and service events.
* Provide an admin sub-portal for managing users, providers, services, requests, reports, and disputes.
* Demonstrate Java concepts including OOP, inheritance, polymorphism, collections, exception handling, multithreading, JDBC, JPA/Hibernate, Java I/O, WebSocket communication, and Reflection.

## Scope of the Project

The scope of SameNeed includes:

* Customer registration, authentication, and profile management.
* Service provider registration, authentication, profile management, and verification.
* Creation and management of local service requests.
* Discovery of customers requiring the same service.
* Formation of request-specific service groups.
* Matching based on service, locality, and compatible schedule.
* Anonymous identities for group members during group formation.
* Real-time group communication using WebSocket.
* Provider offers and counter-offer negotiation.
* Individual member confirmation or rejection of an accepted offer.
* Booking creation and service-status tracking.
* In-app notifications.
* Customer reviews after completed services.
* User reports and dispute management.
* Administrative management of users, providers, services, requests, and bookings.
* CSV export of booking information.
* Java Reflection-based entity metadata functionality for administrative use.

The project does not include online payment processing or email delivery. Actual payment between customers and service providers is assumed to happen outside the platform.

## Target Users

### 1. Customers

Individuals or households who require local services and want to find other nearby customers requiring the same service.

Examples:

* Customers requiring washing machine repair.
* Customers requiring AC servicing.
* Customers requiring plumbing services.
* Customers requiring electrical repairs.
* Customers requiring other supported local services.

### 2. Service Providers

Technicians, repair professionals, and local service businesses who provide supported services and want to handle multiple compatible service requests in a locality.

### 3. Administrators

Authorized administrators responsible for maintaining the platform, managing users and service providers, monitoring requests and bookings, managing services, and handling reports or disputes.

## High-Level Features

### Customer Features

* Registration and login
* Service request creation
* Browse and search service requests
* Same-service group discovery
* Join and leave groups
* Anonymous group-member identities
* Real-time group chat
* Provider offer viewing
* Price negotiation
* Offer acceptance or rejection
* Booking management
* Service-status tracking
* Notifications
* Reviews and ratings
* Reports and disputes
* Profile management

### Service Provider Features

* Registration and login
* Provider profile management
* Service management
* Service-area management
* View eligible service groups
* View individual customer requirements
* Submit group-based offers
* Counter-offer negotiation
* Booking management
* Service-status updates
* Service history
* Customer reviews

### Admin Features

* Admin authentication
* Dashboard and application statistics
* Customer management
* Service-provider management
* Provider verification
* Service category and service management
* Request and booking monitoring
* Reports and dispute management
* Account management
* CSV booking export
* Entity metadata inspection using Java Reflection

### Core Platform Features

* Same-service grouping even when customer problems are different
* Locality and schedule-based matching
* Configurable group capacity
* Request-specific anonymous identities
* Real-time WebSocket communication
* Structured offer and counter-offer negotiation
* Offer expiry handling
* Session-based authentication
* BCrypt password hashing
* Role-based access control
* MySQL data persistence
* JDBC and JPA/Hibernate integration
* Background processing
* Java I/O based CSV generation
