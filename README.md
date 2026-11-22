# CATC LabTrack

**Laboratory Equipment Register Web Service · CAT College · Week 3 integration and verification**

A student looking for a multimeter can search by lab location, read its unique asset code and see its current status. The same register serves Computer Engineering, Electrical Engineering, Computer Science, Engineering Technology and Information Technology. Custodians and technicians use the ADMIN page. Instructors and students use the USER page.

## Run the packaged application

From the repository root, build the current checkout with Maven and JDK 25.

```sh
cd CATC-LabTrack
mvn clean package
java -jar target/labtrack-0.1.0.jar
```

Visit http://localhost:8080/login. Rebuild after editing the source.

## Start in IntelliJ IDEA

1. Extract the ZIP. In IntelliJ, choose **File → Open** and select the **pom.xml** inside **CATC-LabTrack**. Open it as a Maven project.
2. In **File → Project Structure → Project**, choose **JDK 25** as the SDK. Use **Download JDK** if needed. Set language level to **SDK default**. Under **Settings → Build Tools → Maven**, select bundled Maven. Under **Runner** and **Importing**, select JDK 25 where a JDK choice appears.
3. Open the Maven tool window and reload the project. Allow internet access for the first dependency download.
4. Open **src/main/java/ph/edu/catc/labtrack/LabTrackApplication.java**. Run its **main** method using the green arrow. Wait for the console to show **Started LabTrackApplication**.
5. Visit **http://localhost:8080/**. Sign in using the LabTrack login form. The home route takes you to the appropriate workspace.

| Role | Username | Local demo password | Landing page |
|---|---|---|---|
| ADMIN | admin | admin-demo | /admin |
| USER | user | user-demo | /user |

Click Sign out to switch accounts. HTTP Basic is available to API clients. The server binds to the loopback address. H2 runs in memory. Stopping and restarting clears edits and recreates both accounts and six sample equipment records. Each saved account contains a BCrypt hash.

**Terminal option.** From the extracted folder containing pom.xml, run `mvn -v` and confirm Java 25 and Maven 3.6.3 or later. Run `mvn spring-boot:run`. If Maven cannot find the spring-boot plugin, check that `dir pom.xml` finds this project's POM in your current folder. The IntelliJ main-method instructions work with bundled Maven. Spring Boot serves both pages and their CSS.

## If IntelliJ reports JVM target 5

Extract the updated ZIP into a fresh folder and open its pom.xml as a project. To keep local edits, copy just the updated pom.xml into your existing project and reload Maven.

1. In **File → Project Structure → Project**, select JDK 25 and set the language level to SDK default. Under **Modules → labtrack → Dependencies**, choose **Project SDK**. Under **Sources**, select **Project default** for language level.
2. In **Settings → Build, Execution, Deployment → Compiler → Java Compiler**, set the project bytecode version to **25** and remove any per-module override of **5** for labtrack.
3. In Maven settings, choose **JDK 25** for both the importer and runner. Reload all Maven projects. Run **Maven → Lifecycle → package**. If Java 25 is missing from the language-level choices after adding the SDK, use an IntelliJ IDEA release with Java 25 support.

## Try the first end-to-end feature

On the USER dashboard, open **My Profile** to check the seeded student identity. Choose an available item, enter the instructor, subject and borrow date, review the return date and accept the displayed terms. After confirming, open **My Reservations** to find the reference and status. The return date is three days after the borrow date. The late-fee text is informational. Sign in as ADMIN, find the reference in Reservations, confirm pickup and record the return. Cancelling a pending request also releases its units.

## Architecture design

One Spring Boot Maven module hosts the HTML interface and the JSON API. Both entry points use the same equipment service. A DTO is the data shape accepted or returned at the API boundary. The JPA entity represents the stored row.

```mermaid
flowchart TD
    B["Browser or API client"] --> S["Spring Security"]
    S --> P["Page controller"]
    S --> R["REST controller"]
    S --> A["Account lookup and BCrypt"]
    A --> H[("H2 database")]
    P --> V["Thymeleaf role templates"]
    P --> E["Equipment service"]
    R --> E
    E --> J["EquipmentRepository"]
    J --> H
```

**Example request.** An authenticated API client sends JSON and its session’s CSRF token to POST /api/equipment. Spring Security checks the credentials and token. EquipmentController validates EquipmentRequest. Spring injects EquipmentService into the controller through its constructor. The service normalizes the code, checks uniqueness and asks EquipmentRepository to save it. H2 also enforces uniqueness. The controller returns 201, a Location header and an EquipmentResponse. The client can retrieve the generated ID in a separate GET.

| Table | Fields and meaning |
|---|---|
| equipment | id (generated key), asset_code (unique item tag), name, location (lab or room), status (AVAILABLE, RESERVED or BORROWED) |
| app_user | username (key), password_hash, role (USER or ADMIN) |

A reservation holds units until an administrator cancels the pending request or records the return after pickup. Equipment with free units is AVAILABLE. Fully held equipment is BORROWED when every unit has been picked up and RESERVED otherwise.

## Five-member ownership

Paths beginning with **J/** below are relative to **src/main/java/ph/edu/catc/labtrack/**. **R/** means **src/main/resources/**. Member numbers are placeholders for your teammates' names.

| Member | Folders and files to manage | Individual deliverable and demo |
|---|---|---|
| 1 · Team leader | Root pom.xml, README.md, docs/, J/setup/, J/LabTrackApplication.java, R/application.properties | Maintain the JDK 25 build and sample setup. Integrate changes, coordinate the ten checks, and demonstrate a clean import and reset. |
| 2 · Equipment data and business rules | J/equipment/model/, J/equipment/repository/, J/equipment/service/ | Own the H2/JPA model, location query and normalized asset code. Implement update and delete service methods. Demonstrate an unchanged code succeeds on update and a conflicting code fails. |
| 3 · REST API and validation | J/api/, J/equipment/dto/ | Implement PUT and DELETE, coordinate their service signatures with Member 2, and add a helpful missing-ID response. Capture 201, 400, 404 and 409 examples. |
| 4 · Security and admin frontend | J/security/, R/schema.sql, R/templates/admin/ | Own database-backed login, CSRF and authorization. Add admin edit/delete controls when the endpoints are ready. Demonstrate ADMIN deletion and USER denial using valid tokens. |
| 5 · User frontend and shared UI | J/web/, R/templates/user/, R/templates/shared/, R/static/ | Own page routing, search, shared HTML/CSS and browser requests. Add user status editing for existing items. Demonstrate finding a breadboard and recording a reservation. |

**Working agreement.** Assign one feature branch per member. Members 2 and 3 agree on method signatures before implementing writes. Members 4 and 5 agree on shared form fields and error messages before changing templates. The leader reviews merges. Each member supplies their own test evidence and explains one request through the layers.

## API verification

Both roles can list, retrieve, create and update equipment. Only ADMIN can delete equipment and transition reservations. All writes require a valid CSRF token. Run these from the module folder with a freshly started application.

```sh
python docs/verify_api.py
python docs/verify_reservations.py
```

**Week 3 materials.** See [report, saved responses and five-member rehearsal notes](CATC-LabTrack/docs/week3/). To capture a fresh run, start the application with a clean in-memory database and run `python docs/verify_api.py http://127.0.0.1:8080 --evidence` from the Maven module. Use a fourth week for fixes if available.

**AI-use note.** ChatGPT assisted with this starter, its architecture and the suggested division of work. Each member should record the changes they make and the checks they personally run before submission.

For a repeatable create, fetch and filter demonstration on Windows, open PowerShell in the project root while Spring Boot is running and run `.\docs\try-api.ps1`. It fetches a CSRF token, retains the session cookie and uses a fresh asset code on each run.

## File I/O activity

From the repository root, run the standalone classroom exercise with JDK 25.

```sh
cd activities/file-io
java src/FileIOActivity.java
```

It creates or replaces `SD1.txt` in the current directory with the text `File was successfully created.`. LabTrack itself uses H2 for its equipment and reservation records.
