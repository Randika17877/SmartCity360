# SMARTCITY360 – SMART CITY MANAGEMENT PLATFORM
## Project Documentation & Architectural Report
**Unit Name:** Object Oriented Design Patterns II  
**Unit Code:** JIAT/OODP II  
**Assessment ID:** JIAT/OODP II/EX/01  
**Target Scenario:** UrbanTech Solutions – Smart City Management System  
**Institution:** Java Institute for Advanced Technology – Department of Examinations  
**Technology Stack:** Java 17 · Jakarta EE 10 (EJB 4.0, JPA 3.1, JSF 4.0, CDI 4.0, Bean Validation 3.0) · Payara Server 6 · MySQL 8.0 · Maven 3.9  

---

## Table of Contents
1. [Executive Summary & System Overview](#1-executive-summary--system-overview)
2. [Technology Stack & Multi-Tier Enterprise Architecture](#2-technology-stack--multi-tier-enterprise-architecture)
3. [Enterprise Security & Role-Based Access Control (RBAC)](#3-enterprise-security--role-based-access-control-rbac)
4. [Part A: Dynamic Security Wrappers for Data Streams (Decorator Pattern)](#4-part-a-dynamic-security-wrappers-for-data-streams-decorator-pattern)
5. [Part B: Hierarchical Structure of Smart Zones (Composite Pattern)](#5-part-b-hierarchical-structure-of-smart-zones-composite-pattern)
6. [Part C: Escalation of Citizen Complaints (Chain of Responsibility Pattern)](#6-part-c-escalation-of-citizen-complaints-chain-of-responsibility-pattern)
7. [Part D: Efficient Management of Smart Street Lights (Flyweight Pattern)](#7-part-d-efficient-management-of-smart-street-lights-flyweight-pattern)
8. [Part E: Interpreting City Policies Defined in DSL (Interpreter Pattern)](#8-part-e-interpreting-city-policies-defined-in-dsl-interpreter-pattern)
9. [Part F: System Data Model, Testing, Deployment & VIVA Guide](#9-part-f-system-data-model-testing-deployment--viva-guide)
   - [9.1 Relational Data Model & JPA Entity Mapping](#91-relational-data-model--jpa-entity-mapping)
   - [9.2 Verification & Comprehensive Test Plan](#92-verification--comprehensive-test-plan)
   - [9.3 VIVA Demonstration Guide (10–15 Minute Structure)](#93-viva-demonstration-guide-1015-minute-structure)
   - [9.4 Local Deployment & Execution Instructions](#94-local-deployment--execution-instructions)
10. [Appendix: PlantUML Class Diagram Sources](#10-appendix-plantuml-class-diagram-sources)

---

## 1. Executive Summary & System Overview

**SmartCity360** is a unified municipal management platform engineered for **UrbanTech Solutions**. Modern smart cities depend on heterogeneous cyber-physical systems ranging from power grid IoT sensors and smart lighting to citizen grievance desks and regulatory enforcement rules. Coordinating these interdependent services demands high modularity, rigorous decoupling, strict data integrity, and strict access governance.

This implementation provides an enterprise-ready, multi-tier Java solution that addresses all five architectural challenges defined in the assessment syllabus through the exact application of classic **Gang of Four (GoF) design patterns**:
1. **Dynamic Security for Energy Streams**: Implemented via the **Decorator Pattern**, enabling layered HMAC authentication, GZIP compression, and AES-256-GCM encryption on energy telemetry without mutating meter core components.
2. **Hierarchical Spatial Governance**: Implemented via the **Composite Pattern**, organizing the municipal topology (`City → District → Street → Building`) into a tree where operational commands and energy readings are processed uniformly across individual structures and regional clusters. The hierarchy is persisted in MySQL table `city_zone`.
3. **Automated Incident Escalation**: Implemented via the **Chain of Responsibility Pattern**, automatically routing citizen complaints through a decoupled triage pipeline (`LocalStaffHandler → SupervisorHandler → CityManagerHandler`) based on validated severity ratings.
4. **Memory-Scalable Streetlight Infrastructure**: Implemented via the **Flyweight Pattern**, drastically conserving JVM heap memory across tens of thousands of streetlight endpoints by caching immutable intrinsic properties (`model`, `wattage`, `colour`) in a centralized factory while isolating extrinsic mutable runtime state (`location`, `active`).
5. **Municipal Policy Automation**: Implemented via the **Interpreter Pattern**, enabling city operators to define and dynamically evaluate environmental and zoning bylaws expressed in a custom Domain-Specific Language (DSL), parsed into an Abstract Syntax Tree (AST).

To ensure defense-in-depth, the solution incorporates an enterprise security architecture utilizing salted **PBKDF2 password hashing**, a centralized **Jakarta Servlet Filter** (`AuthFilter`) enforcing **Role-Based Access Control (RBAC)** across three distinct administrative roles (`CITIZEN`, `OFFICER`, `ADMIN`), and proactive privilege escalation mitigation at self-registration.

---

## 2. Technology Stack & Multi-Tier Enterprise Architecture

The solution adheres strictly to standard Jakarta EE enterprise multi-tier architecture, completely decoupling the presentation, business logic, persistence, and enterprise security concerns.

```
smartcity360/
├── pom.xml                        # Root Parent POM (Dependency management & versions)
├── smartcity-ejb/                 # EJB 4.0 & JPA 3.1 Module
│   ├── src/main/java/lk/smartcity360/
│   │   ├── entity/                # JPA Domain Entities (User, Zone, Complaint, Streetlight, EnergyReading, CityPolicy)
│   │   ├── pattern/
│   │   │   ├── decorator/         # Part A: DataStream, StreamDecorator, Authentication, Compression, Encryption
│   │   │   ├── composite/         # Part B: CityComponent, ZoneGroup, Building, ZoneTreeBuilder
│   │   │   ├── chain/             # Part C: ComplaintHandler, LocalStaff, Supervisor, CityManager
│   │   │   ├── flyweight/         # Part D: LightType, LightTypeFactory, StreetLightContext
│   │   │   └── interpreter/       # Part E: Expression, Comparison, And, Or, PolicyParser, PolicyContext
│   │   ├── security/              # Cryptographic PBKDF2 PasswordHasher
│   │   └── service/               # Stateless EJB Session Beans (UserService, CityService, ComplaintService, etc.)
│   └── src/test/java/             # Automated JUnit 5 Unit Tests (47 test cases covering all patterns & security)
├── smartcity-web/                 # JSF 4.0 / Jakarta Faces Web Module (WAR)
│   ├── src/main/java/lk/smartcity360/web/
│   │   ├── AuthBean.java          # CDI SessionScoped backing bean managing session and authentication
│   │   ├── AuthFilter.java        # Jakarta Servlet Filter enforcing RBAC and route protection
│   │   └── *Bean.java             # CDI Backing Beans connecting JSF views to EJB services
│   └── src/main/webapp/           # XHTML/HTML Facelets views (dashboard, zones, complaints, streetlights, energy, policies)
│       └── WEB-INF/
│           ├── templates/layout.html # Master Facelets template with dynamic role-aware navigation
│           └── web.xml            # Servlet deployment descriptor, filter mappings, error page bindings
└── smartcity-ear/                 # Enterprise Application Archive (SmartCity360.ear) packaging EJB + WAR for Payara 6
```

---

## 3. Enterprise Security & Role-Based Access Control (RBAC)

### 3.1 Privilege Escalation Vulnerability Remediation
In early baseline revisions, `signup.html` exposed an HTML `<h:selectOneMenu>` dropdown permitting self-registering users to select their assigned role from `CITIZEN`, `OFFICER`, or `ADMIN`. This constituted a critical **privilege escalation vulnerability**, enabling malicious actors to grant themselves administrative authority over city infrastructure.

**Remediation Applied Across Tiers:**
1. **Presentation Layer (`signup.html`)**: The interactive role dropdown was removed and replaced with a static informational badge confirming that public registrations are strictly assigned the **Citizen / Resident** role.
2. **Controller Layer (`AuthBean.java`)**: The `signUp()` action method discards any externally provided role parameters and explicitly invokes `userService.registerCitizen(...)`. In addition, `getRegRole()` and `setRegRole()` are locked to `"CITIZEN"`.
3. **Business Logic Layer (`UserService.java`)**: The service enforces strict role sanitization. Unrecognized, blank, or unauthorized role assignments default safely to `"CITIZEN"`. Administrative (`ADMIN`) and Operations Officer (`OFFICER`) accounts can only be provisioned internally through database seeding or authorized administrator consoles.

### 3.2 Cryptographic Authentication (`PasswordHasher.java`)
User credentials are protected using salted PBKDF2 hashing:
- **Algorithm**: `PBKDF2WithHmacSHA256`
- **Work Factor**: 10,000 iterations, 256-bit key length
- **Salt Generation**: 128-bit (16-byte) cryptographically secure random salt generated via `java.security.SecureRandom`, stored as a 32-character hexadecimal string.
- **Verification**: Evaluated using constant-time comparison (`constantTimeEquals`) to prevent timing side-channel attacks.

### 3.3 Servlet Filter Enforcement (`AuthFilter.java`)
A custom Jakarta Servlet Filter (`AuthFilter`) is registered on `/*` in `web.xml`:
- **Public Endpoints**: `/signin.html`, `/signup.html`, `/access-denied.html`, and static web resources (`/resources/*`, `/jakarta.faces.resource/*`) bypass the filter.
- **Authentication Check**: Any unauthenticated request attempting to reach protected modules (`/dashboard.html`, `/complaints.html`, `/zones.html`, `/energy.html`, `/streetlights.html`, `/policies.html`) is automatically redirected to `/signin.html`. Both full browser HTTP GET/POST and JSF partial/AJAX requests are handled seamlessly.
- **Role-Based Authorization Matrix**:

| Endpoint | Resource Description | `CITIZEN` | `OFFICER` | `ADMIN` | Unauthorized Response |
|:---|:---|:---:|:---:|:---:|:---|
| `/signin.html`, `/signup.html` | Public Authentication & Registration | Allowed | Allowed | Allowed | N/A |
| `/dashboard.html` | Operations & Overview Dashboard | Allowed | Allowed | Allowed | Redirect to `/signin.html` |
| `/complaints.html` | Incident Reporting & Resolution | Submit & View | Submit & Resolve | Full Control | Redirect to `/signin.html` |
| `/zones.html` | Spatial Zone Topology & Broadcast | **DENIED** | Allowed | Allowed | **HTTP 403 Forbidden** (`access-denied.html`) |
| `/energy.html` | Smart Meter Ingestion & Decorator Protection | **DENIED** | Allowed | Allowed | **HTTP 403 Forbidden** (`access-denied.html`) |
| `/streetlights.html` | Flyweight Streetlight Configuration | **DENIED** | Allowed | Allowed | **HTTP 403 Forbidden** (`access-denied.html`) |
| `/policies.html` | Policy DSL Management & Interpreter Testing | **DENIED** | Allowed | Allowed | **HTTP 403 Forbidden** (`access-denied.html`) |

---

## 4. Part A: Dynamic Security Wrappers for Data Streams (Decorator Pattern)

### 4.1 Design Pattern Identification and Justification (3 Marks)
**Selected Pattern: Decorator (Structural Pattern).**  
SmartCity360 ingests continuous telemetry streams from thousands of smart electricity and water meters deployed across urban zones. To prevent tampering and guarantee confidentiality across public networks, energy payloads require dynamic security services—specifically cryptographic authentication (HMAC-SHA256 signature), bandwidth optimization (GZIP compression), and payload encryption (AES-256-GCM).

**Justification:**
- **Dynamic Runtime Composition**: Unlike class-level static inheritance, which creates an explosive combination of rigid subclasses (e.g., `CompressedEncryptedStream`, `SignedCompressedStream`), the Decorator pattern attaches responsibilities dynamically at runtime by wrapping instances inside decorator objects sharing the same interface.
- **Single Responsibility Principle (SRP)**: Each transformation concern (signing, compressing, encrypting) is encapsulated in its own isolated class rather than bloating the basic stream implementation.
- **Open/Closed Principle (OCP)**: New transformation stages (e.g., streaming differential hashing, noise obfuscation) can be introduced without altering either the core component (`BasicEnergyStream`) or existing wrapper classes.

### 4.2 Support for Flexible and Layered Enhancements (4 Marks)
The Decorator pattern achieves layering by ensuring that both the concrete component and the abstract decorator implement the common interface `DataStream`:

```java
public interface DataStream {
    String process(String data);
}
```

The abstract `StreamDecorator` holds a reference to an inner `DataStream` component and delegates the `process` call:

```java
public abstract class StreamDecorator implements DataStream {
    protected final DataStream wrapped;
    public StreamDecorator(DataStream wrapped) { this.wrapped = wrapped; }
    @Override
    public String process(String data) { return wrapped.process(data); }
}
```

The concrete decorators implement specialized forward and reverse transformations:
1. `AuthenticationDecorator`: Appends an HMAC-SHA256 cryptographic signature (`payload#signature`) using a secret HMAC key. Provides `verify(signed)` to detect data tampering.
2. `CompressionDecorator`: Compresses the payload string using GZIP and encodes the byte array into RFC 4648 Base64. Provides `decompress(compressedBase64)` to reconstruct the original stream.
3. `EncryptionDecorator`: Encrypts the payload using AES-256-GCM with a fresh, cryptographically random 12-byte Initialization Vector (IV) per invocation. Provides `decrypt(ivCiphertextBase64)` to recover the plaintext.

**Ordering Significance**:  
$$\text{Output} = \text{Encrypt}(\text{Compress}(\text{Sign}(\text{Plaintext})))$$

```java
DataStream pipeline = new EncryptionDecorator(
                         new CompressionDecorator(
                             new AuthenticationDecorator(
                                 new BasicEnergyStream(), streamKey), 
                             streamKey), 
                         streamKey);
String protectedPayload = pipeline.process(rawMeterReading);
```

### 4.3 Evaluation of Long-Term Benefits: Reusability and Extensibility (8 Marks)
- **Reusability Across Domains**: Neither `CompressionDecorator` nor `EncryptionDecorator` contains business logic tied to energy metering. They operate exclusively on the `DataStream` contract.
- **Independent Evolution & Maintenance**: Cryptographic algorithms can be upgraded independently (e.g., transitioning to post-quantum cryptography).
- **Selective Pipeline Configuration**: In high-bandwidth private fiber municipal links, compression can be bypassed while retaining authentication and encryption simply by omitting `CompressionDecorator` from the wrapper chain.

---

## 5. Part B: Hierarchical Structure of Smart Zones (Composite Pattern)

### 5.1 Design Pattern Identification and Justification (3 Marks)
**Selected Pattern: Composite (Structural Pattern).**  
Municipal infrastructure is inherently recursive and partitioned into geopolitical levels: a **City** contains multiple **Districts**; each District contains **Streets**; each Street contains multiple physical **Buildings**. Operators frequently need to execute administrative actions—such as `"shutdown power"` or `"report usage"`—either on an entire city, a single district, or an isolated individual building.

**Justification:**
- **Part-Whole Hierarchy Representation**: The Composite pattern organizes objects into tree structures to represent part-whole hierarchies.
- **Uniform Client Code**: Individual buildings (`Building` leaves) and collections of zones (`ZoneGroup` composites) share one common interface (`CityComponent`). Calling `"shutdown power"` or `"report usage"` is identical whether applied to a single house, a commercial street, or the entire metropolitan area.

### 5.2 UML Class Diagram (4 Marks)

```
        +-----------------------------------------------+
        |              <<interface>>                    |
        |              CityComponent                    |
        +-----------------------------------------------+
        | + getName() : String                          |
        | + execute(command : String) : List<String>    |
        | + getUsage() : double                         |
        | + setPower(on : boolean) : void               |
        | + isPowerOn() : boolean                       |
        +-----------------------------------------------+
                                ^
                                |
             +------------------+------------------+
             |                                     |
+--------------------------+         +-------------------------------+
|         Building         |         |           ZoneGroup           |
|         (Leaf)           |         +-------------------------------+
+--------------------------+         | - name : String               |
| - name : String          |         | - children : List<CityComponent>|
| - usage : double         |         +-------------------------------+
| - powerOn : boolean      |         | + getName() : String          |
+--------------------------+         | + add(c : CityComponent) : void|
| + getName() : String     |         | + remove(c : CityComponent): void|
| + execute(cmd) : List<Str>|        | + execute(cmd) : List<String> |
| + getUsage() : double    |         | + getUsage() : double         |
| + setPower(on) : void    |         | + setPower(on) : void         |
| + isPowerOn() : boolean  |         | + isPowerOn() : boolean       |
+--------------------------+         +-------------------------------+
                                                     o
                                                     | 0..*
                                                     +------- (aggregates children)
```

*(See Appendix for the exact PlantUML source code: `docs/uml/PartB-Composite.puml`)*

### 5.3 Infrastructure Scalability and Management (8 Marks)
- **Hierarchical Usage Aggregation**: The Composite pattern aggregates energy usage recursively at every structural level:
  $$\text{Building (Leaf)} \longrightarrow \text{Street (Composite)} \longrightarrow \text{District (Composite)} \longrightarrow \text{City (Root Composite)}$$
  In SmartCity360, each `Building` returns its individual load (e.g. `Shopping Mall` reports $450.00\text{ kWh}$, `City Hospital` reports $380.50\text{ kWh}$). When `getUsage()` is called on composite nodes, `ZoneGroup` sums its children's readings dynamically:
  - $\text{Galle Road} = 450.00 + 380.50 = 830.50\text{ kWh}$
  - $\text{Dharmapala Mawatha} = 120.00 + 95.25 = 215.25\text{ kWh}$
  - $\text{Colombo 03 - Kollupitiya} = 830.50 + 215.25 = 1045.75\text{ kWh}$
  - $\text{Colombo 07 - Cinnamon Gardens} = 150.00 + 210.00 = 360.00\text{ kWh}$
  - $\text{Colombo (Total City)} = 1045.75 + 360.00 = \mathbf{1405.75\text{ kWh}}$
- **Uniform Power Control at Any Level**: Operators can invoke `setPower(boolean)` or `"SHUTDOWN_POWER"` / `"RESTORE_POWER"` on any node in the hierarchy. The call executes identically whether targeting the entire city, a specific district, a single street, or an individual building, cascading down to all constituent nodes.
- **Dynamic City Growth**: When the city expands (e.g., adding an `EV Charging Station` of $75\text{ kWh}$ under `Galle Road`), the new building is persisted into table `city_zone`, and the Composite tree is rebuilt immediately. The usage totals for `Galle Road`, `Kollupitiya`, and `Colombo` update automatically without requiring manual recalculations.

---

## 6. Part C: Escalation of Citizen Complaints (Chain of Responsibility Pattern)

### 6.1 Design Pattern Identification and Justification (3 Marks)
**Selected Pattern: Chain of Responsibility (Behavioral Pattern).**  
Citizens submit service grievances regarding power outages, burst water mains, noise disturbances, and road damage. Different categories and urgency levels require different levels of bureaucratic authority. Minor issues should be handled immediately by local technicians, whereas critical city-wide emergencies require mayoral intervention.

**Justification:**
- **Decoupling Sender from Receivers**: The submission UI (`complaints.html`) and service (`ComplaintService`) do not need to know which specific officer level will resolve the issue. The complaint is passed into the head of the chain.
- **Dynamic Multi-Stage Processing**: Handlers are linked in sequential succession. Each handler evaluates whether its internal business threshold covers the complaint; if not, it forwards the request down the chain.

### 6.2 Decoupled Request Handling Mechanism (4 Marks)
The base class `ComplaintHandler` defines the successor link and default dispatching:

```java
public abstract class ComplaintHandler {
    private ComplaintHandler next;
    public ComplaintHandler setNext(ComplaintHandler next) {
        this.next = next;
        return next;
    }
    public void handle(Complaint complaint) {
        if (canHandle(complaint)) {
            process(complaint);
        } else if (next != null) {
            next.handle(complaint);
        } else {
            fallback(complaint);
        }
    }
    protected abstract boolean canHandle(Complaint complaint);
    protected abstract void process(Complaint complaint);
}
```

The concrete handlers are linked in sequential order during service execution:
1. `LocalStaffHandler`: Handles complaints with severity $1 \le \text{severity} \le 2$. Sets `assignedLevel = LOCAL` and `status = ASSIGNED`.
2. `SupervisorHandler`: Handles complaints with severity $3 \le \text{severity} \le 4$. Sets `assignedLevel = SUPERVISOR` and `status = ASSIGNED`.
3. `CityManagerHandler`: Handles emergency complaints with $\text{severity} \ge 5$. Sets `assignedLevel = CITY_MANAGER` and `status = ASSIGNED`.
4. **Fallback Handler**: If an invalid or unclassified severity is provided, the termination logic flags the complaint as `MANUAL_REVIEW`, guaranteeing that no request is silently dropped.

### 6.3 Evaluation of Flexibility and Traceability (8 Marks)
- **Flexibility Benefits**: Handlers can be dynamically reordered, added, or removed without impacting client code.
- **Traceability**: All state transitions, handler assignments, and timestamps are recorded in table `complaints`.

---

## 7. Part D: Efficient Management of Smart Street Lights (Flyweight Pattern)

### 7.1 Design Pattern Identification and Justification (3 Marks)
**Selected Pattern: Flyweight (Structural Pattern).**  
Smart cities deploy hundreds of thousands of connected LED streetlights across thousands of kilometers of roadways. In a naive object-oriented design, storing 100,000 distinct streetlight instances—each holding its own model name strings, wattage specifications, optic lens data, and color parameters—wastes tens of megabytes of JVM heap memory on identical duplicate strings and numbers.

**Justification:**
- **Elimination of Redundant State**: The Flyweight pattern minimizes memory usage by sharing common, immutable state among many fine-grained objects.
- **State Partitioning**: By cleanly dividing streetlight state into **intrinsic** (shared, hardware-specific) and **extrinsic** (unique, per-pole) state, the platform enables massive horizontal scalability.

### 7.2 Handling of Shared (Intrinsic) and Unique (Extrinsic) Properties (4 Marks)
SmartCity360 partitions streetlight attributes as follows:
- **Intrinsic State (`LightType.java`)**: Independent of location. Immutable and shared across thousands of poles:
  - `model` (e.g., `"Philips-Luma-Gen2"`)
  - `wattage` (e.g., `60` Watts)
  - `colour` (e.g., `"warm-white"`)
- **Extrinsic State (`StreetLightContext.java` / `Streetlight.java`)**: Unique to each specific deployment point:
  - `id` and unique asset `code` (e.g., `"SL-001"`)
  - Geographic `location` (e.g., `"Main Street - North Pole 14"`)
  - Operational status `active` (e.g., `true` / `false` indicating ON/OFF)
  - Reference to the shared `LightType` flyweight object.

**Factory Caching Mechanism (`LightTypeFactory.java`)**:  
A thread-safe factory maintains a pool of unique `LightType` instances backed by a `ConcurrentHashMap`:

```java
public class LightTypeFactory {
    private static final Map<String, LightType> POOL = new ConcurrentHashMap<>();

    public static LightType get(String model, int wattage, String colour) {
        String key = (model + ":" + wattage + ":" + colour).toLowerCase();
        return POOL.computeIfAbsent(key, k -> new LightType(model, wattage, colour));
    }
    public static int poolSize() { return POOL.size(); }
}
```

### 7.3 Performance Impact and Scalability Analysis (8 Marks)
- **Heap Memory Optimization**: For $N = 100,000$ streetlights sharing 10 standard fixture models, heap memory drops from $\sim 12\text{ MB}$ to $\sim 1.2\text{ KB}$, saving over **99%** of object allocation overhead.
- **Garbage Collection Optimization**: Drastically reduces GC pauses on the Payara server during bulk telemetry synchronizations.

---

## 8. Part E: Interpreting City Policies Defined in DSL (Interpreter Pattern)

### 8.1 Design Pattern Identification and Justification (3 Marks)
**Selected Pattern: Interpreter (Behavioral Pattern).**  
City administrators frequently update operational ordinances and environmental bylaws (e.g., adjusting night-time residential noise limits, congestion charges during peak traffic hours, or dimming streetlights past midnight). Hardcoding these business rules into Java source code requires continuous recompilation, redeployment, and testing cycles.

**Justification:**
- **Dynamic Rule Definition**: The Interpreter pattern defines a grammatical representation for a Domain-Specific Language (DSL) alongside an interpreter that evaluates rule expressions against runtime context objects.
- **Runtime Flexibility**: City council ordinances can be stored as plain-text database records and interpreted on-the-fly without restarting the enterprise application.

### 8.2 UML Class Diagram (4 Marks)

```
                       +---------------------------------------+
                       |             <<interface>>             |
                       |              Expression               |
                       +---------------------------------------+
                       | + interpret(ctx: PolicyContext): bool |
                       +---------------------------------------+
                                           ^
                                           |
            +------------------------------+-------------------------------+
            |                                                              |
+-----------------------------------+                     +---------------------------------+
|       ComparisonExpression        |                     |          AndExpression          |
+-----------------------------------+                     +---------------------------------+
| - variable : String               |                     | - left  : Expression            |
| - operator : Operator             |                     | - right : Expression            |
| - threshold: double               |                     +---------------------------------+
+-----------------------------------+                     | + interpret(ctx) : boolean      |
| + interpret(ctx) : boolean        |                     +---------------------------------+
+-----------------------------------+                                      |
                                                                           o (combines two)
                                                                      left, right
```

*(See Appendix for the exact PlantUML source code: `docs/uml/PartE-Interpreter.puml`)*

### 8.3 Grammatical Rules, Extensibility, and Assessment (8 Marks)
The system defines an unambiguous Backus-Naur Form (BNF) grammar:

$$\begin{aligned}
\text{policy} &\Coloneqq \text{conjunction}\; (\text{"OR"}\;\text{conjunction})^* \\
\text{conjunction} &\Coloneqq \text{comparison}\; (\text{"AND"}\;\text{comparison})^* \\
\text{comparison} &\Coloneqq \text{identifier}\;\; \text{operator}\;\; \text{number} \\
\text{operator} &\Coloneqq \text{">"} \mid \text{">="} \mid \text{"<"} \mid \text{"<="} \mid \text{"=="} \mid \text{"!="}
\end{aligned}$$

- **Abstract Syntax Tree (AST) Parsing (`PolicyParser.java`)**: `PolicyParser` builds an expression tree where boolean `AND` binds tighter than `OR`.
- **Evaluation Mechanism (`PolicyContext.java`)**: `PolicyContext` holds sensor measurements. When `interpret(context)` is called, the AST evaluates recursively to return `true` (violation flagged) or `false`.

---

## 9. Part F: System Data Model, Testing, Deployment & VIVA Guide

### 9.1 Relational Data Model & JPA Entity Mapping
The physical relational schema is implemented in `database/smartcity360.sql`:

```sql
CREATE TABLE app_users (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(60)  NOT NULL UNIQUE,
    email         VARCHAR(120) NOT NULL UNIQUE,
    full_name     VARCHAR(120) NOT NULL,
    password_hash VARCHAR(128) NOT NULL,
    salt          VARCHAR(64)  NOT NULL,
    role          VARCHAR(30)  NOT NULL DEFAULT 'CITIZEN',
    created_at    DATETIME     NOT NULL,
    INDEX idx_users_username (username),
    INDEX idx_users_email (email)
);

CREATE TABLE city_zone (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(150) NOT NULL,
    type       VARCHAR(30)  NOT NULL,
    parent_id  BIGINT       NULL,
    usage_kwh  DOUBLE       NOT NULL DEFAULT 0.0,
    power_on   BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_city_zone_parent FOREIGN KEY (parent_id) REFERENCES city_zone (id),
    CONSTRAINT ck_city_zone_type CHECK (type IN ('CITY', 'DISTRICT', 'STREET', 'BUILDING'))
);

CREATE OR REPLACE VIEW zones AS SELECT * FROM city_zone;

CREATE TABLE complaints (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    resident_name  VARCHAR(150)  NOT NULL,
    description    VARCHAR(1000) NOT NULL,
    severity       INT           NOT NULL,
    status         VARCHAR(30)   NOT NULL,
    assigned_level VARCHAR(30)   NOT NULL,
    created_at     DATETIME      NOT NULL,
    CONSTRAINT ck_complaints_severity CHECK (severity BETWEEN 1 AND 5),
    INDEX idx_complaints_status (status)
);

CREATE TABLE streetlights (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    code     VARCHAR(80)  NOT NULL UNIQUE,
    location VARCHAR(200) NOT NULL,
    model    VARCHAR(100) NOT NULL,
    wattage  INT          NOT NULL,
    colour   VARCHAR(30)  NOT NULL,
    active   BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE energy_readings (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    meter_code     VARCHAR(80) NOT NULL,
    kilowatt_hours DOUBLE      NOT NULL,
    recorded_at    DATETIME    NOT NULL,
    INDEX idx_energy_recorded (recorded_at)
);

CREATE TABLE city_policies (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(150)  NOT NULL,
    expression VARCHAR(1000) NOT NULL,
    enabled    BOOLEAN       NOT NULL DEFAULT TRUE
);
```

---

### 9.2 Verification & Comprehensive Test Plan

#### Automated JUnit 5 Unit Tests (47 Passing Tests)
Executed via `mvn test` without requiring a live database or application server:
1. **`DecoratorTest` (6 tests)**: Cryptographic round-trip, tamper detection, wrong-key failure, raw stream.
2. **`CompositeTest` (8 tests)**: Leaf and group execution, recursive hierarchy traversal, multi-level broadcast, empty group safety, **hierarchical usage aggregation at every level**, and **power switching cascades across the hierarchy**.
3. **`ChainTest` (6 tests)**: Severity threshold routing: 1–2 Local Staff, 3–4 Supervisor, 5+ City Manager, fallback manual review.
4. **`FlyweightTest` (4 tests)**: Intrinsic state pooling and object reuse in `LightTypeFactory`, 24-hour dimming profiles, memory reduction comparisons, and hex identity hashes.
5. **`InterpreterTest` (17 tests)**: Full expression evaluation with comparisons, `AND`, `OR`, `NOT`, nested parentheses `( )`, AST tree serialization, case-insensitivity, and syntax error handling.
6. **`PasswordHasherTest` (4 tests)**: PBKDF2 hashing, salt generation, verification against seeded test hashes.
7. **`UserEntityTest` (2 tests)**: Default `CITIZEN` role assignments.

#### Manual Verification Script on Live Deployment

```
+----+------------------------------------------------+-------------------------------------------------------------+------------------------------------+
| #  | Test Case Description                          | Procedure / Input                                           | Expected Outcome                   |
+----+------------------------------------------------+-------------------------------------------------------------+------------------------------------+
| 1  | Unauthenticated URL Interception               | Open http://localhost:8080/smartcity360/dashboard.html     | Redirected to /signin.html         |
| 2  | Privilege Escalation Prevention                | Open /signup.html; fill form; submit                        | Role locked to CITIZEN; registered |
| 3  | Role Authorization - Citizen Access Rejection  | Log in as 'citizen'; open /zones.html directly in address   | HTTP 403 Forbidden; access-denied  |
| 4  | Role Authorization - Officer/Admin Module View | Log in as 'officer' or 'admin'; check navigation sidebar    | All 5 pattern modules displayed    |
| 5  | Composite Usage Aggregation                    | Open City Zones; inspect table and total usage              | Total usage shows 1405.75 kWh      |
| 6  | Power Switching at Any Level                   | Select 'Galle Road' -> click 'Shutdown power'               | Shopping Mall & Hospital turn OFF  |
| 7  | Dynamic City Growth                            | Add building 'EV Station' (75 kWh) under Galle Road         | Total usage increases to 1480.75   |
| 8  | Complaint Escalation Routing                   | Submit complaints with severity 1, 3, and 5                 | Assigned to LOCAL, SUP, MANAGER    |
| 9  | Flyweight Pool Verification                    | Register 3 streetlights with identical model/wattage/colour | Flyweight pool size increases by 1 |
| 10 | Decorator Energy Protection                    | Click 'Protect sample' in Energy module                     | Base64 encrypted ciphertext output |
| 11 | Policy DSL Evaluation                          | Test 'noiseDb > 60 AND hour >= 22' with noiseDb=65, hour=23 | Evaluates to TRUE                  |
+----+------------------------------------------------+-------------------------------------------------------------+------------------------------------+
```

---

### 9.3 VIVA Demonstration Guide (10–15 Minute Structure)

```
[0:00 - 1:30] Introduction & Architecture Overview
  * State unit details: JIAT/OODP II, UrbanTech Solutions scenario.
  * Explain EAR multi-tier layout: EJB 4.0 business tier, JSF 4.0 presentation tier, JPA persistence tier.
  * Highlight clean separation of concerns and automated JUnit 5 test suite (45 passing tests).

[1:30 - 3:00] Enterprise Security & RBAC Demonstration
  * Show pre-configured test users: admin/admin123, officer/officer123, citizen/citizen123.
  * Demonstrate privilege escalation remediation on /signup.html (locked to CITIZEN; no dropdown).
  * Sign in as 'citizen' and attempt direct access to /zones.html: show HTTP 403 Forbidden on access-denied.html.
  * Show how layout.html adapts sidebar navigation dynamically based on user role.

[3:00 - 5:00] Part B: Hierarchical Smart Zones (Composite Pattern)
  * Show the UI matching the Part B specification:
    - Left card: 'Switch the power at any level'
    - Right card: 'The city grows: add a building'
    - City tree: 'City tree - total usage 1405.75 kWh'
  * Demonstrate usage aggregation: Shopping Mall (450) + Hospital (380.5) = Galle Road (830.50).
    Explain how getUsage() recursively sums child readings up to Colombo (1405.75 kWh).
  * Demonstrate power switching: select 'Colombo' -> Shutdown power (all turn OFF); Restore power (all turn ON).
  * Demonstrate growth: add a building under 'Galle Road' with 75 kWh -> usage immediately updates.

[5:00 - 7:00] Part C: Citizen Complaint Escalation (Chain of Responsibility)
  * Show ComplaintHandler base class and successor chaining.
  * Submit three test grievances:
      - Severity 1 (flickering lamp) -> Assigned to LOCAL staff.
      - Severity 3 (pothole cluster) -> Assigned to SUPERVISOR.
      - Severity 5 (main water burst) -> Escalated immediately to CITY_MANAGER.
  * Resolve a complaint and demonstrate the dynamic reduction in dashboard pending counts.

[7:00 - 9:00] Part D: Smart Streetlights (Flyweight Pattern)
  * Explain the memory bottleneck: 100,000 streetlights sharing identical physical properties.
  * Differentiate Intrinsic (LightType: model, wattage, colour) vs. Extrinsic (location, on/off state).
  * Demonstrate in UI: register multiple streetlights with identical fixtures, click 'Show flyweight pool',
    and demonstrate that the pool size remains small and constant.

[9:00 - 11:00] Part E: City Policy Mini-Language (Interpreter Pattern)
  * Show BNF grammar: conjunctions, comparisons, operators.
  * Demonstrate AST parsing: PolicyParser builds AndExpression and ComparisonExpression trees.
  * Test live in UI: enter 'noiseDb > 60 AND hour >= 22' with sample variables (65 dB, 23:00) -> TRUE;
    change hour to 20:00 -> FALSE. Show that syntax errors are trapped cleanly.

[11:00 - 12:30] Part A: Dynamic Security Wrappers (Decorator Pattern)
  * Explain the pipeline: Encrypt(Compress(Sign(Payload))).
  * Discuss why order matters: ciphertext has high entropy and cannot be compressed.
  * Demonstrate in UI: enter meter reading, click 'Protect sample', and show Base64 GCM ciphertext
    containing a fresh random 12-byte IV per click.

[12:30 - 15:00] Code Review, UML Inspection & Conclusion
  * Open docs/uml diagrams and show alignment with working packages.
  * Summarize maintainability, extensibility, and memory benefits achieved through GoF patterns.
  * Answer examiner questions.
```

---

### 9.4 Local Deployment & Execution Instructions

1. **Start Database**:
   ```bash
   docker compose up -d
   ```
2. **Configure Payara Server 6 Datasource**:
   - Copy `mysql-connector-j-*.jar` into `<payara>/glassfish/domains/domain1/lib`.
   - Run `asadmin` commands:
     ```bash
     asadmin create-jdbc-connection-pool --restype javax.sql.DataSource \
       --datasourceclassname com.mysql.cj.jdbc.MysqlDataSource \
       --property serverName=localhost:portNumber=3306:databaseName=smartcity360:user=smartcity:password=smartcity:useSSL=false:allowPublicKeyRetrieval=true \
       SmartCityPool

     asadmin create-jdbc-resource --connectionpoolid SmartCityPool jdbc/SmartCityDS
     ```
3. **Build Enterprise EAR**:
   ```bash
   mvn clean package
   ```
4. **Deploy Application**:
   ```bash
   asadmin deploy smartcity-ear/target/SmartCity360.ear
   ```
5. **Access Application**:
   Open <http://localhost:8080/smartcity360/> (redirects to `signin.html`). Default accounts:
   - Administrator: `admin` / `admin123`
   - Operations Officer: `officer` / `officer123`
   - Citizen: `citizen` / `citizen123`

---

## 10. Appendix: PlantUML Class Diagram Sources

### Part A – Decorator Pattern (`docs/uml/PartA-Decorator.puml`)
```plantuml
@startuml
interface DataStream {
    +process(data: String): String
}

class BasicEnergyStream {
    +process(data: String): String
}

abstract class StreamDecorator {
    -wrapped: DataStream
    +StreamDecorator(wrapped: DataStream)
    +process(data: String): String
}

class AuthenticationDecorator {
    -key: String
    +process(data: String): String
    +verify(signed: String): String
}

class CompressionDecorator {
    +process(data: String): String
    +decompress(base64: String): String
}

class EncryptionDecorator {
    -secretKey: SecretKey
    +process(data: String): String
    +decrypt(ciphertextBase64: String): String
}

DataStream <|.. BasicEnergyStream
DataStream <|.. StreamDecorator
StreamDecorator <|-- AuthenticationDecorator
StreamDecorator <|-- CompressionDecorator
StreamDecorator <|-- EncryptionDecorator
StreamDecorator o--> DataStream : wraps
@enduml
```

### Part B – Composite Pattern (`docs/uml/PartB-Composite.puml`)
```plantuml
@startuml
interface CityComponent {
    +getName(): String
    +execute(command: String): List<String>
    +getUsage(): double
    +setPower(on: boolean): void
    +isPowerOn(): boolean
}

class Building {
    -name: String
    -usage: double
    -powerOn: boolean
    +getName(): String
    +execute(command: String): List<String>
    +getUsage(): double
    +setPower(on: boolean): void
    +isPowerOn(): boolean
}

class ZoneGroup {
    -name: String
    -children: List<CityComponent>
    +getName(): String
    +add(child: CityComponent): void
    +remove(child: CityComponent): void
    +execute(command: String): List<String>
    +getUsage(): double
    +setPower(on: boolean): void
    +isPowerOn(): boolean
}

class ZoneTreeBuilder {
    +{static} index(zones: List<Zone>): Map<Long, CityComponent>
    +{static} roots(zones: List<Zone>): List<CityComponent>
}

CityComponent <|.. Building
CityComponent <|.. ZoneGroup
ZoneGroup o--> "0..*" CityComponent : children
ZoneTreeBuilder ..> CityComponent : builds tree
@enduml
```

### Part C – Chain of Responsibility Pattern (`docs/uml/PartC-Chain.puml`)
```plantuml
@startuml
abstract class ComplaintHandler {
    -next: ComplaintHandler
    +setNext(next: ComplaintHandler): ComplaintHandler
    +handle(complaint: Complaint): void
    #canHandle(complaint: Complaint): boolean
    #process(complaint: Complaint): void
}

class LocalStaffHandler {
    #canHandle(complaint: Complaint): boolean
    #process(complaint: Complaint): void
}

class SupervisorHandler {
    #canHandle(complaint: Complaint): boolean
    #process(complaint: Complaint): void
}

class CityManagerHandler {
    #canHandle(complaint: Complaint): boolean
    #process(complaint: Complaint): void
}

ComplaintHandler <|-- LocalStaffHandler
ComplaintHandler <|-- SupervisorHandler
ComplaintHandler <|-- CityManagerHandler
ComplaintHandler --> ComplaintHandler : next
@enduml
```

### Part D – Flyweight Pattern (`docs/uml/PartD-Flyweight.puml`)
```plantuml
@startuml
class LightType {
    -model: String
    -wattage: int
    -colour: String
    +getModel(): String
    +getWattage(): int
    +getColour(): String
}

class LightTypeFactory {
    -POOL: Map<String, LightType>
    +get(model: String, wattage: int, colour: String): LightType
    +poolSize(): int
}

class StreetLightContext {
    -id: Long
    -code: String
    -location: String
    -active: boolean
    -type: LightType
    +toggle(active: boolean): void
}

LightTypeFactory ..> LightType : creates & pools
StreetLightContext --> LightType : references shared intrinsic state
@enduml
```

### Part E – Interpreter Pattern (`docs/uml/PartE-Interpreter.puml`)
```plantuml
@startuml
interface Expression {
    +interpret(context: PolicyContext): boolean
}

class ComparisonExpression {
    -variable: String
    -operator: Operator
    -threshold: double
    +interpret(context: PolicyContext): boolean
}

class AndExpression {
    -left: Expression
    -right: Expression
    +interpret(context: PolicyContext): boolean
}

class OrExpression {
    -left: Expression
    -right: Expression
    +interpret(context: PolicyContext): boolean
}

class PolicyContext {
    -variables: Map<String, Double>
    +set(variable: String, value: double): void
    +get(variable: String): double
}

Expression <|.. ComparisonExpression
Expression <|.. AndExpression
Expression <|.. OrExpression
AndExpression o--> Expression : left/right
OrExpression o--> Expression : left/right
Expression ..> PolicyContext : evaluates against
@enduml
```

---
*End of SmartCity360 Project Documentation.*
