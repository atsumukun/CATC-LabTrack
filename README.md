# CATC LabTrack

LabTrack is CAT College’s laboratory equipment register. Students and instructors search inventory by lab and reserve available units. Custodians maintain equipment records, confirm pickups, and record returns.

Each equipment entry has a unique asset code, name, location, status and unit counts. The shared register covers Computer Engineering, Electrical Engineering, Computer Science, Engineering Technology and Information Technology. A single list helps users find available items and prevents a second entry from taking the same asset code.

## Run locally

Start in the repository root, the folder containing this README. The Maven project starts at the [root pom.xml](pom.xml). Use **JDK 25** and **Maven 3.6.3 or later**. Check that `mvn -v` reports Java 25.

```sh
mvn clean package
java -jar target/labtrack-0.1.0.jar
```

Alternatively, open the linked POM in IntelliJ, set the project SDK and Maven runner to JDK 25, reload Maven, and run `LabTrackApplication.main`. If IntelliJ reports JVM target 5, reimport the linked POM and check the SDK and Maven runner settings.

Once the console shows `Started LabTrackApplication`, open the [local login page](http://127.0.0.1:8080/login).

| Demo role | Username | Password | Landing page |
|---|---|---|---|
| USER | user | user-demo | /user |
| ADMIN | admin | admin-demo | /admin |

The server listens on loopback. Startup creates both accounts and seeds six equipment records. H2 stores data in memory, so restarting restores the sample inventory and clears reservations and edits.

## Test the HTTP requests in Postman Desktop

1. Leave LabTrack running in IntelliJ. In the extracted project, open the `docs` folder and locate `CATC-LabTrack.postman_collection.json`. If you are viewing the project on GitHub, open the collection file and click **Download raw file** (the down arrow). [Open the collection file](docs/CATC-LabTrack.postman_collection.json).
2. Open **Postman Desktop → Import** and drag the downloaded JSON file into the import window. Under **Collections**, open **CATC LabTrack · Ten HTTP checks**.
3. Open **Setup · user session** and click **Send**. Expect **200 OK**. Do the same for **Setup · admin session**. These requests save each account’s session and CSRF token automatically.
4. Open **1 · Create meter** and click **Send**. Expect **201 Created** and a numeric `id` in the response body. The collection saves that ID for the next requests.
5. Continue down the collection in its displayed order, clicking **Send** once per request. Some course checks have several requests. The ADMIN deletion runs last so the meter is available for the earlier checks.

Open **Test Results** after each response. For example, the USER delete request should return **403 Forbidden**, and the next GET should return **200 OK** for the same meter. Capture the request name, status and test result for your evidence. Keep session cookies and token values out of screenshots.

### Browser demonstration

For the browser demo, sign in as USER, search for a clamp meter, reserve an available unit, and find its reference under **My Reservations**. Sign out, sign in as ADMIN, and use that reference to confirm pickup and record the return. The planned return date is three days after the borrow date. Late fees are informational.

## How an equipment request works

`EquipmentController → EquipmentService → EquipmentRepository → H2`

For a create request, Spring Security authenticates the caller and checks the CSRF token. The controller validates the request DTO. Its constructor-injected service normalizes the asset code, checks uniqueness, and saves the entity through JPA. The controller returns a response DTO with the generated ID and HTTP 201.

| Equipment API operation | Allowed roles | Successful result |
|---|---|---|
| GET /api/equipment | USER, ADMIN | 200 with the inventory. Use `?location=ee` to filter by lab location. |
| GET /api/equipment/{id} | USER, ADMIN | 200 with one equipment record |
| POST /api/equipment | USER, ADMIN | 201 with the created record |
| PUT /api/equipment/{id} | USER, ADMIN | 200 with the updated record |
| DELETE /api/equipment/{id} | ADMIN | 204 with an empty body |

API clients can use HTTP Basic. Passwords are stored as BCrypt hashes. For writes, fetch `GET /api/csrf`, retain the session cookie, and send the returned token using the returned header name. Only ADMIN can change a reservation’s status.

## First write request in Postman Desktop

1. With the app running, send **GET** to `http://127.0.0.1:8080/api/csrf`. Under **Authorization → Basic Auth**, enter the USER credentials above. Leave the body empty.
2. Expect **200**. Copy the returned header name and token value. Keep Postman’s session cookie and use the same host for every request.
3. Open a second request with the same Basic Auth. Select **POST** and enter `http://127.0.0.1:8080/api/equipment`. Under **Headers**, use the returned header name as the key and the token as its value. Under **Body → raw → JSON**, paste the example below.
4. Send it once. Expect **201** and a generated ID. Send a separate authenticated **GET** to `/api/equipment/{id}` using that ID. Expect **200** with the same asset code.

```json
{
  "assetCode": "PM-EE-001",
  "name": "Digital Multimeter",
  "location": "EE Laboratory",
  "status": "AVAILABLE"
}
```

For the role demonstration, send **DELETE** to the equipment ID as USER with the valid token. Expect **403** and verify the record still exists. Clear cookies for the host, switch both requests to ADMIN credentials, and fetch a fresh token before repeating DELETE. Expect **204**, then **404** on GET. For the unauthenticated check, clear cookies and select **No Auth**. A **405** on the token URL means you should check that the method is **GET**.

## Chapter 4 source map

Java paths below start at `src/main/java/ph/edu/catc/labtrack/`. Resource paths start at `src/main/resources/`.

| Topic | Where to look |
|---|---|
| 4.1 Boot and injection | `LabTrackApplication.java`, root `pom.xml`, and resource `application.properties`. Spring supplies the service to the controller constructor. |
| 4.2 REST | `api/EquipmentController.java` maps the equipment URLs and HTTP methods. |
| 4.3 DTOs | `equipment/dto/EquipmentRequest.java` and `EquipmentResponse.java` define the JSON boundary. The server generates the ID. |
| 4.4 JPA | `equipment/model/Equipment.java` maps stored records. `equipment/repository/EquipmentRepository.java` derives the case-insensitive location query. |
| 4.5 Security | `security/SecurityConfig.java`, `setup/DemoData.java` and resource `schema.sql` implement database-backed demo login and role checks. |
| 4.6 Layers | `equipment/service/EquipmentService.java` checks asset codes on create and update. Keeping the current code succeeds. A collision returns **409**. |
| 4.7 Validation | The controller applies `@Valid` to the request. `api/ApiErrors.java` formats validation, missing-record and conflict responses. |

## Team rehearsal and submission

| Owner | Checks to demonstrate |
|---|---|
| 1 · Integration | Run from a fresh extraction using this README. **10** Create a record and retrieve it through a second authenticated client. |
| 2 · Equipment and JPA | **3** Update while keeping the same asset code. **7** Reject duplicate codes on create and update, then verify both records. |
| 3 · REST and validation | **1** Create a record and show its generated ID. **5** Show invalid input returning 400. **6** Show a missing ID returning 404. |
| 4 · Security and admin | **4** Delete as ADMIN and verify the item is gone. **8** Show 401 for missing or wrong credentials. **9** Show 403 for USER deletion with a valid CSRF token and verify the item still exists. |
| 5 · Student interface | **2** List, retrieve, and filter equipment through the API. Demonstrate search, booking, and reservation history in the browser. |


### Folder ownership

| Suggested owner | Folders or files to manage |
|---|---|
| 1 · Team leader | Root `pom.xml`, `README.md`, `docs/`, Java `setup/`, and resource `application.properties` |
| 2 · Equipment and JPA | Java `equipment/model/`, `equipment/repository/`, `equipment/service/` and reservation data and service code under `reservation/` |
| 3 · API and validation | Java `api/`, `equipment/dto/` and `reservation/dto/` |
| 4 · Security and admin | Java `security/`, resource `schema.sql` and `templates/admin/` |
| 5 · Student dashboard | Java `web/` and `profile/`, resource `templates/user/`, `templates/shared/` and `static/` |

### Submission materials

- Package the source with the root POM, README, configuration and docs. Open the extracted POM in IntelliJ to check the submitted copy.
- Review the [editable report](docs/week3/LabTrack-Week3.docx) and its [PDF](docs/week3/LabTrack-Week3.pdf). The guide asks for about 4–6 pages excluding the cover and evidence. Each member should enter their actual contribution and personal checks, then export the updated PDF.
- Use the [saved Postman requests](docs/CATC-LabTrack.postman_collection.json) and [ten-check transcript](docs/week3/api-evidence.txt). For each check record the input, expected result, actual result and pass/fail, with a saved response or screenshot.
- Follow the [five-member rehearsal](docs/week3/rehearsal.md) for the 10–15 minute demonstration. Each member explains one contribution and traces a request through the controller, service, repository and database.

**AI-use note.** ChatGPT/Codex assisted with implementation, test preparation and documentation. The team should describe what it reviewed, tested and corrected in the submitted report.
