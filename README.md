# SmartCity360

A Jakarta EE 10 smart-city management platform (zones, energy meters, citizen complaints,
streetlights and city policies), packaged as a multi-module Maven EAR. Each functional area is
built around one design pattern from the *Object Oriented Design Patterns II* assessment.

| Part | Area | Pattern | Where |
|------|------|---------|-------|
| A | Energy data streams | Decorator | `pattern/decorator`, `EnergyService` |
| B | City hierarchy | Composite | `pattern/composite`, `CityService` |
| C | Complaint escalation | Chain of Responsibility | `pattern/chain`, `ComplaintService` |
| D | Streetlights | Flyweight | `pattern/flyweight`, `StreetlightService` |
| E | City policies | Interpreter | `pattern/interpreter`, `PolicyService` |

## Stack

Java 17 · Jakarta EE 10 (EJB 4, JPA 3, CDI 4, JSF 4, Bean Validation 3) · Payara 6 · MySQL 8 · Maven 3.9

## Layout

```
smartcity360/
├── pom.xml                  parent POM (versions, plugin management)
├── smartcity-ejb/           entities, stateless services, pattern implementations, unit tests
├── smartcity-web/           JSF pages (one Facelets template) and CDI backing beans
├── smartcity-ear/           packages the EJB jar + WAR into SmartCity360.ear
├── database/smartcity360.sql  schema and sample data
├── docs/                    project report and UML (PlantUML)
└── docker-compose.yml       local MySQL with the schema pre-loaded
```

## Build and test

```bash
mvn clean verify
```

Produces `smartcity-ear/target/SmartCity360.ear` and runs the JUnit 5 tests (patterns, crypto
decorators, policy parser). The tests need no application server or database.

## Run locally

1. **Database**: `docker compose up -d` (loads `database/smartcity360.sql`), or run the script
   against your own MySQL 8 server. Default docker credentials: `smartcity` / `smartcity`.
2. **MySQL driver**: copy `mysql-connector-j-*.jar` into `<payara>/glassfish/domains/domain1/lib`
   and restart the domain. The driver is deliberately *not* bundled in the EAR.
3. **Payara resources** (`asadmin`):
   ```bash
   asadmin create-jdbc-connection-pool --restype javax.sql.DataSource \
     --datasourceclassname com.mysql.cj.jdbc.MysqlDataSource \
     --property serverName=localhost:portNumber=3306:databaseName=smartcity360:user=smartcity:password=smartcity:useSSL=false:allowPublicKeyRetrieval=true \
     SmartCityPool
   asadmin ping-connection-pool SmartCityPool
   asadmin create-jdbc-resource --connectionpoolid SmartCityPool jdbc/SmartCityDS
   ```
4. **Stream key (recommended)**: the energy page encrypts with AES-256-GCM. Provide a key with
   `export SMARTCITY_STREAM_KEY=$(openssl rand -base64 32)` before starting Payara (or the JVM
   option `-Dsmartcity.stream.key=...`). Without it a fixed *development* key is used and a
   warning is logged.
5. **Deploy** `smartcity-ear/target/SmartCity360.ear`, then open
   <http://localhost:8080/smartcity360/> (redirects to `signin.html` when unauthenticated).

### Preloaded Test Accounts

The database seed (`database/smartcity360.sql`) provides pre-configured accounts for testing each role:

| Username | Password | Role | Permitted Access |
|----------|----------|------|------------------|
| `admin` | `admin123` | `ADMIN` | All modules, system administration |
| `officer` | `officer123` | `OFFICER` | Dashboard, complaints, zones, energy, streetlights, policies |
| `citizen` | `citizen123` | `CITIZEN` | Dashboard, citizen complaints submission |

## Security and Role-Based Access Control (RBAC)

- **Privilege Escalation Vulnerability Fix**: `signup.html` previously allowed arbitrary role
  selection, enabling self-registration as `ADMIN`. This security hole has been eliminated:
  the role selector was removed, self-registration is strictly hardcoded to `CITIZEN` across
  the web and business tiers (`AuthBean.signUp`, `UserService.registerCitizen`), and
  `OFFICER` / `ADMIN` roles can only be granted internally or through database seeding.
- **User Entity & Persistence**: `User` entity mapped to `app_users` with unique constraints on
  `username` and `email`. Passwords are protected using `PasswordHasher` (PBKDF2 with HMAC-SHA256,
  10,000 iterations, 128-bit cryptographically secure per-user salt, and constant-time hash comparison).
- **Servlet Filter (`AuthFilter`)**: Intercepts all requests across the web application:
  - *Public pages*: `/signin.html`, `/signup.html`, `/access-denied.html`, and static assets (`/resources/*`, `/jakarta.faces.resource/*`).
  - *Authentication check*: Redirects unauthenticated requests targeting protected pages to `/signin.html` (including AJAX partial-response redirects).
  - *Role authorization*: Restricts administrative modules (`/zones.html`, `/energy.html`, `/streetlights.html`, `/policies.html`) to `OFFICER` and `ADMIN`. If a `CITIZEN` user attempts to access these pages, `AuthFilter` blocks the request with HTTP 403 Forbidden and renders `access-denied.html`.
- **Role-Aware Presentation**: Navigation links in `layout.html` and module cards in `dashboard.html`
  dynamically adapt to the signed-in user's role.

## Behaviour worth knowing

- **Complaints**: severity 1–2 → local staff, 3–4 → supervisor, 5 → city manager. Resolving a
  complaint removes it from the dashboard's pending count.
- **Zones**: a city has no parent; district → city; street → district; building → street. The
  service rejects anything else. "Broadcast a command" builds a Composite tree from the stored
  rows and runs the command on a zone and everything under it.
- **Policies**: comparisons (`> >= < <= == !=`) joined by `AND` / `OR` (`AND` binds tighter, no
  parentheses). Saving validates the expression; unknown variables are errors, not zero.
  Example: `noiseDb > 60 AND hour >= 22`.
- **Streetlights**: model, wattage and colour are pooled in `LightTypeFactory`; "Show flyweight
  pool" reports how many shared type objects back all stored lights. The pool is per-JVM.
- **Energy protection**: payload → HMAC-SHA256 tag → gzip → AES-256-GCM (random IV per message).

## Not included (yet)

OAuth2/OIDC single sign-on, multi-factor authentication (MFA), centralized audit logging, pagination, and database-backed integration tests (e.g. Testcontainers). Authentication, PBKDF2 password hashing, `User` entity persistence, servlet filter protection (`AuthFilter`), and role-based access control (RBAC) are fully implemented.
