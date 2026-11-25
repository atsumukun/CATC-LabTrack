# CATC LabTrack

LabTrack is CAT College’s laboratory equipment register. Students and instructors search inventory by lab and reserve available units. Custodians maintain equipment records, confirm pickups, and record returns.

## Run locally

Start in the repository root, the folder containing this README. The application IntelliJ opens is defined by [the nested Maven project](CATC-LabTrack/CATC-LabTrack/pom.xml). Use **JDK 25** and **Maven 3.6.3 or later**. Check that `mvn -v` reports Java 25.

```powershell
mvn -f CATC-LabTrack/CATC-LabTrack/pom.xml clean package
java -jar CATC-LabTrack/CATC-LabTrack/target/labtrack-0.1.0.jar
```

Alternatively, open the linked POM in IntelliJ, set the project SDK and Maven runner to JDK 25, reload Maven, and run `LabTrackApplication.main`. If IntelliJ reports JVM target 5, reimport the linked POM and check the SDK and Maven runner settings.

Once the console shows `Started LabTrackApplication`, open the [local login page](http://127.0.0.1:8080/login).

| Demo role | Username | Password | Landing page |
|---|---|---|---|
| USER | user | user-demo | /user |
| ADMIN | admin | admin-demo | /admin |

The server listens on loopback. Startup creates both accounts and seeds six equipment records. H2 stores data in memory, so restarting restores the sample inventory and clears reservations and edits.

## Verify the application

Leave LabTrack running. In a second terminal at the repository root, use Python 3 to run the checks below. Start with a fresh application instance each time you run the ten equipment checks because the script reuses fixed asset codes.

```powershell
python CATC-LabTrack/docs/verify_api.py http://127.0.0.1:8080 --evidence
python CATC-LabTrack/docs/verify_reservations.py
```

The first script prints requests, expected and actual status codes, and response bodies. A successful run ends with `All ten checks passed.` The second checks reservation quantities, competing requests for the last unit, account history, pickup, return, and cancellation. For a short interactive API demo on Windows, run `.\CATC-LabTrack\docs\try-api.ps1` in PowerShell.

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

## Team rehearsal and submission

| Owner | Checks to demonstrate |
|---|---|
| 1 · Integration | Run from a fresh extraction using this README. **10** Create a record and retrieve it through a second authenticated client. |
| 2 · Equipment and JPA | **3** Update while keeping the same asset code. **7** Reject duplicate codes on create and update, then verify both records. |
| 3 · REST and validation | **1** Create a record and show its generated ID. **5** Show invalid input returning 400. **6** Show a missing ID returning 404. |
| 4 · Security and admin | **4** Delete as ADMIN and verify the item is gone. **8** Show 401 for missing or wrong credentials. **9** Show 403 for USER deletion with a valid CSRF token and verify the item still exists. |
| 5 · Student interface | **2** List, retrieve, and filter equipment through the API. Demonstrate search, booking, and reservation history in the browser. |
