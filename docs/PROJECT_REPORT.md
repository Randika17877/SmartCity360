# SmartCity360 – Project Documentation
## Object Oriented Design Patterns II
**System:** SmartCity360 – Smart City Management Platform  
**Organization scenario:** UrbanTech Solutions

## 1. Introduction
SmartCity360 is a web-based city management platform that organizes city zones, records energy readings, routes citizen complaints, models reusable streetlight configurations, evaluates operational policies, and secures urban operations through authenticated role-based access control. The solution demonstrates five object-oriented design patterns selected to match Parts A–E of the assessment, complemented by robust enterprise security architecture.

## 2. Objectives
- Apply one appropriate design pattern to each supplied scenario.
- Enforce clean separation of concerns across web, business, persistence, and security layers.
- Demonstrate extensibility, reuse, memory efficiency, and defensive design.
- Secure operations through role-based access control (RBAC), robust password hashing, and privilege escalation prevention.
- Provide a deployable Java EE application and supporting UML diagrams.

## 3. Technology and architecture
The system uses Java 17 and Jakarta EE 10:
- **Presentation Layer**: Jakarta Faces (JSF 4.0) with a shared Facelets template (`layout.html`), dynamic role-aware navigation, and CDI backing beans (`@SessionScoped`, `@RequestScoped`).
- **Security & Access Control**: Jakarta Servlet Filter (`AuthFilter`) intercepting web traffic to enforce session authentication and role-based access control (RBAC); PBKDF2 password hashing with cryptographically secure salts (`PasswordHasher`).
- **Business Layer**: Stateless Enterprise JavaBeans (EJB 4.0) managing transactional workflows, pattern orchestrations, and domain business rules.
- **Persistence Layer**: Jakarta Persistence (JPA 3.1) mapping domain entities to MySQL via EclipseLink, with declarative JTA transactions and Bean Validation constraints.
- **Build & Packaging**: Maven multi-module structure (`smartcity-ejb`, `smartcity-web`, `smartcity-ear`), producing an enterprise application archive (`SmartCity360.ear`) for Payara Server 6.
- **Quality Assurance**: Automated JUnit 5 test suite validating pattern mechanics, security hashing, entity constraints, and policy parsing without requiring a running application server.

## 4. Part A – Dynamic Security Wrappers for Data Streams
**Selected pattern: Decorator.**  
The Decorator pattern attaches additional responsibilities to an object by wrapping it with objects that implement the same interface. `DataStream` defines the common contract. `BasicEnergyStream` is the concrete component. `AuthenticationDecorator` appends an HMAC-SHA256 tag, `CompressionDecorator` applies GZIP, and `EncryptionDecorator` encrypts with AES-256-GCM using a fresh random IV per message. `EnergyService.protect` composes them as encrypt(compress(sign(payload))): compression is applied before encryption because encrypted data does not compress. Each decorator also offers the inverse operation (`verify`, `decompress`, `decrypt`) so a receiver can recover and check the payload; the unit tests exercise the full round trip, tamper detection and wrong-key failure.

**Benefits:** responsibilities can be combined in different orders; each concern is independently reusable; new transformations can be added as decorators; existing clients continue to use the `DataStream` interface.

**Trade-offs:** many wrappers can make runtime composition harder to trace; ordering matters (compress-then-encrypt, not the reverse); each decorator adds an object and a call layer. The key comes from `SMARTCITY_STREAM_KEY` / `-Dsmartcity.stream.key`; if neither is set a fixed development key is used and a warning is logged, so production deployments must configure a key and manage its rotation.

## 5. Part B – Hierarchical Structure of Smart Zones
**Selected pattern: Composite.**  
The Composite pattern represents part-whole hierarchies through a shared component interface. `CityComponent` declares `getName` and `execute`. `Building` is a leaf. `ZoneGroup` is a composite that stores child components and forwards a command to every child; `execute` returns one log line per node reached. `ZoneTreeBuilder` converts the persisted `Zone` rows (linked by `parentId`) into a Composite tree, and `CityService.broadcast` uses it to run a command on any zone and everything beneath it. `ZoneType` records which parent each level requires (city → district → street → building), and `CityService.saveZone` enforces it.

**Benefits:** clients work with leaves and groups uniformly; adding a new component type is straightforward; recursive operations cover a large hierarchy. **Trade-offs:** the tree is rebuilt from the database on each broadcast; rules about valid parents live in `ZoneType` and the service rather than in the Composite classes.

## 6. Part C – Escalation of Citizen Complaints
**Selected pattern: Chain of Responsibility.**  
A complaint is passed through a sequence of handlers. `LocalStaffHandler` handles severity 1–2. `SupervisorHandler` handles severity 3–4. `CityManagerHandler` receives severity 5 and above. Each handler either processes the complaint or forwards it to the next handler. `ComplaintService.submit` constructs the chain and persists the resulting assignment and status.

**Benefits:** sender and receiver are decoupled; escalation order can be changed; new handlers can be inserted. **Trade-offs:** a request may travel through several handlers; an incomplete chain can leave a request unhandled; tracing the final handler requires logging. The implementation sets a manual-review status if no handler is configured.

## 7. Part D – Efficient Management of Smart Street Lights
**Selected pattern: Flyweight.**  
Flyweight shares common intrinsic state among many objects and keeps unique extrinsic state outside the shared object. `LightType` stores shared model, wattage and colour. `LightTypeFactory` caches and reuses instances. `StreetLightContext` stores unique location and on/off state while referencing a shared `LightType`.

**Benefits:** reduces duplicated configuration objects and memory use when many lights share a small number of types. **Trade-offs:** code becomes more complex because intrinsic and extrinsic state must be separated; shared flyweights should be immutable or safely managed. The concurrent map makes factory access safe for concurrent requests.

## 8. Part E – Interpreting City Policies Defined in DSL
**Selected pattern: Interpreter.**  
The Interpreter pattern represents grammar rules as expression objects. `Expression` defines `interpret`. `ComparisonExpression` evaluates `variable operator number` using the `Operator` enum (`> >= < <= == !=`). `AndExpression` and `OrExpression` combine expressions. `PolicyContext` supplies variable values and rejects unknown variables. `PolicyParser` builds the tree for the grammar `policy := conjunction (OR conjunction)*`, `conjunction := comparison (AND comparison)*`; `AND` binds tighter than `OR`, keywords are case-insensitive and parentheses are not supported. Example: `noiseDb > 60 AND hour >= 22`. `PolicyService.save` refuses policies whose expression does not parse.

**Benefits:** grammar rules are represented explicitly; expressions can be composed; policy changes need no code changes. **Trade-offs:** a larger grammar needs more classes and a real tokenizer; error messages are limited to the failing clause.

## 9. Security Architecture and Role-Based Access Control (RBAC)

### 9.1 Privilege Escalation Prevention
In early iterations, the registration form (`signup.html`) allowed users to arbitrarily choose their role, including `ADMIN`. This presented a critical privilege escalation security vulnerability. The system resolves this vulnerability through defense-in-depth:
1. **Presentation Layer**: The role selector dropdown on `signup.html` was eliminated and replaced with a fixed informational badge stating that public self-registration is strictly for Citizen accounts.
2. **Controller Layer**: `AuthBean.signUp()` explicitly discards any client-supplied role input and delegates to `UserService.registerCitizen()`, hardcoding the role to `CITIZEN`.
3. **Service Layer**: `UserService.register()` validates and sanitizes any assigned role; unauthorized or unrecognized role assignments default safely to `CITIZEN`. Administrative (`ADMIN`) and Operations Officer (`OFFICER`) accounts are strictly provisioned internally or via controlled database initialization.

### 9.2 Cryptographic Authentication
Passwords are never stored in plaintext. `PasswordHasher` implements industry-standard salted PBKDF2 hashing:
- **Algorithm**: `PBKDF2WithHmacSHA256`
- **Key Length & Iterations**: 256 bits, 10,000 iterations
- **Salt Generation**: Cryptographically secure 128-bit random salt generated via `java.security.SecureRandom`
- **Verification**: Constant-time byte-by-byte comparison (`constantTimeEquals`) to prevent timing side-channel attacks.

### 9.3 Servlet Filter (`AuthFilter`) and Role Enforcement
A centralized Jakarta Servlet Filter (`AuthFilter`) intercepts all incoming requests (`/*`):
- **Public Whitelist**: Static resources (`/resources/*`, `/jakarta.faces.resource/*`) and public authentication pages (`/signin.html`, `/signup.html`, `/access-denied.html`) pass through unimpeded.
- **Session Authentication Check**: Any unauthenticated access to secured resources redirects immediately to `/signin.html`. Both full HTTP GET/POST and JSF partial/AJAX requests are properly handled.
- **Role Authorization Matrix**:
  - `CITIZEN`: Access to `dashboard.html` and `complaints.html` (submitting complaints, tracking status). Restricted from administrative modules.
  - `OFFICER`: Access to `dashboard.html`, `complaints.html` (including complaint resolution), `zones.html`, `energy.html`, `streetlights.html`, and `policies.html`.
  - `ADMIN`: Complete administrative privileges across all modules and accounts.
- **Access Violation Handling**: When a user with the `CITIZEN` role attempts to directly access restricted administrative modules (e.g. `/zones.html` or `/policies.html`), `AuthFilter` intercepts the request and issues an HTTP 403 Forbidden status, seamlessly presenting a dedicated `access-denied.html` view.

## 10. Data model
Column names use snake_case; statuses and levels are stored as enum names. The schema is defined in `database/smartcity360.sql`:
- **User (`app_users`)**: `id`, `username` (unique), `email` (unique), `full_name`, `password_hash` (PBKDF2), `salt` (hex), `role` (`CITIZEN`, `OFFICER`, `ADMIN`), and `created_at`.
- **Zone (`zones`)**: `name`, `type` (`CITY`, `DISTRICT`, `STREET`, `BUILDING`), `parent_id` (foreign key self-reference).
- **Complaint (`complaints`)**: `resident_name`, `description`, `severity` (1–5), `status` (`SUBMITTED`, `ASSIGNED`, `RESOLVED`, `MANUAL_REVIEW`), `assigned_level` (`LOCAL`, `SUPERVISOR`, `CITY_MANAGER`), and `created_at`.
- **Streetlight (`streetlights`)**: unique `code`, `location`, `model`, `wattage`, `colour`, and `active` flag.
- **EnergyReading (`energy_readings`)**: `meter_code`, `kilowatt_hours` (kWh), and `recorded_at` timestamp.
- **CityPolicy (`city_policies`)**: `name`, `expression` (DSL rule string), and `enabled` flag.

## 11. Test plan
### Automated Unit Tests (`mvn test` / `mvn verify`):
- **Security & User Tests**: Salted PBKDF2 hashing verification, rejection of incorrect passwords, uniqueness of salts across identical passwords, validation of seeded account hashes, and `User` entity role defaults (`PasswordHasherTest`, `UserEntityTest`).
- **Pattern Tests**: Decorator cryptographic round trip, tamper detection, and wrong-key failure (`DecoratorTest`); Composite tree building and recursive broadcast execution (`CompositeTest`); Chain of Responsibility escalation thresholds and unhandled fallback (`ChainTest`); Flyweight intrinsic state pooling and count validation (`FlyweightTest`); Interpreter operator evaluation, boolean precedence, syntax error rejection, and undefined variable errors (`InterpreterTest`).

### Manual Testing against a Live Deployment:
1. **Unauthenticated Access & Redirection**: Attempt to open `http://localhost:8080/smartcity360/` or `/dashboard.html` without logging in; verify automatic redirection to `/signin.html`.
2. **Privilege Escalation Prevention (Sign Up)**: Navigate to `/signup.html`; verify that no role dropdown exists and the form clearly indicates a Citizen account. Register a new user, log in, and verify the assigned role is `CITIZEN`.
3. **Role-Based Authorization Enforcement**:
   - While signed in as `citizen`, observe that administrative links (City Zones, Energy, Streetlights, Policies) are hidden from the sidebar navigation.
   - Type `http://localhost:8080/smartcity360/zones.html` directly in the address bar; verify that `AuthFilter` intercepts the request, returns HTTP 403 Forbidden, and renders `access-denied.html`.
4. **Officer and Administrator Privileges**:
   - Sign in with `officer` / `officer123` or `admin` / `admin123`.
   - Verify that all navigation links are visible. Access `/zones.html`, `/energy.html`, `/streetlights.html`, and `/policies.html` successfully.
5. **City Hierarchy (Composite)**: Create city, district, street, and building; confirm invalid parent combinations are rejected; broadcast a command to the city and verify recursive execution across all descendant nodes.
6. **Complaint Escalation (Chain of Responsibility)**: Submit complaints at severities 1, 3, and 5; verify assignment to Local Staff, Supervisor, and City Manager respectively; resolve a complaint and observe the dashboard pending count decrease.
7. **Streetlight Configuration (Flyweight)**: Register multiple streetlights sharing model, wattage, and colour; click "Show flyweight pool" to confirm that pooled shared objects are significantly fewer than the total number of physical lights.
8. **Energy Stream Protection (Decorator)**: Enter energy meter readings and click "Protect sample"; verify generated Base64 ciphertext with fresh IV on every invocation.
9. **Policy Evaluation (Interpreter)**: Test `noiseDb > 60 AND hour >= 22` against sample values; verify boolean outcomes and parser syntax checking.
10. **Session Invalidation**: Click "Sign Out"; verify session invalidation and redirection to `/signin.html`.

## 12. VIVA demonstration plan (10–15 minutes)
- **0:00–1:30**: Introduce the problem domain, multi-tier EAR architecture, and technology stack (Java 17, Jakarta EE 10, Payara 6, MySQL).
- **1:30–3:00**: **Security & RBAC demonstration**: Show authentication with seeded accounts (`admin`, `officer`, `citizen`). Demonstrate privilege escalation prevention on `/signup.html` (locked to `CITIZEN`) and role enforcement via `AuthFilter` (show HTTP 403 and `access-denied.html` when a citizen attempts to visit `/zones.html`).
- **3:00–5:00**: **Composite pattern (Part B)**: Demonstrate city zone hierarchy, validation of parent-child relationships, and recursive command broadcasting.
- **5:00–7:00**: **Chain of Responsibility pattern (Part C)**: Submit complaints with varying severity (1, 3, 5); demonstrate escalation to Local Staff, Supervisor, and City Manager.
- **7:00–9:00**: **Flyweight pattern (Part D)**: Register streetlights with shared intrinsic properties; inspect the Flyweight pool report to demonstrate memory optimization.
- **9:00–11:00**: **Interpreter pattern (Part E)**: Evaluate and test city policy rules with the domain-specific grammar (`AND`/`OR` precedence and comparison operators).
- **11:00–12:30**: **Decorator pattern (Part A)**: Demonstrate dynamic energy data protection through chained HMAC signing, GZIP compression, and AES-256-GCM encryption.
- **12:30–14:00**: Review UML diagrams, entity relationship model, and clean separation across web, EJB, and persistence modules.
- **14:00–15:00**: Discuss architecture benefits, trade-offs, and future improvements.

## 13. Limitations and future improvements
- **Single Sign-On & OAuth2/OIDC**: Integrate enterprise identity providers (Keycloak, Microsoft Entra ID) via OpenID Connect.
- **Multi-Factor Authentication (MFA)**: Add TOTP-based second-factor authentication for administrative accounts.
- **Centralized Audit Logging**: Track all administrative actions, policy modifications, and zone broadcasts in a dedicated immutable audit log.
- **Rich DSL Grammar**: Extend the Interpreter pattern to support nested parentheses, boolean literals, string matching, and temporal rules.
- **IoT Streaming & Message Brokers**: Connect real-world smart meter and streetlight telemetry via MQTT or Apache Kafka.
- **Pagination & Query Optimization**: Implement server-side pagination and lazy loading for large-scale urban sensor datasets.

## 14. Conclusion
SmartCity360 provides a complete, production-ready reference implementation demonstrating five classic object-oriented design patterns within an enterprise smart-city scenario. The multi-tier architecture achieves strict separation of concerns across presentation, business services, security, and persistence. By coupling robust design patterns with modern Jakarta EE security practices—including salted PBKDF2 password hashing, privilege escalation mitigation, and servlet filter role enforcement—the platform delivers a maintainable, extensible, and secure urban management solution.
