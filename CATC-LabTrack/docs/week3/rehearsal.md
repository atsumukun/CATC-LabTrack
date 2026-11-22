# Five-member rehearsal • 12 minutes

Start LabTrackApplication in IntelliJ with JDK 25. Use the module folder containing pom.xml for terminal commands. This demo uses invented data on localhost. Restart the application before each full rehearsal to recreate the six sample records.

| Time | Suggested speaker | Show and explain |
|---|---|---|
| 0–2 min | Leader | Introduce the five labs and the missing inventory list. Show the running app and the H2 seed. Trace GET /api/equipment from controller through service and repository to H2. |
| 2–4 min | Equipment and data | Run the filter. Use checks 3 and 7 to explain why updating the same item succeeds and assigning its code to another fails. Show EquipmentService and the unique column in Equipment. |
| 4–6 min | REST and validation | Run checks 1 and 5. Point out the generated ID, request record, @Valid and field-specific error. Explain where the invalid request stops. |
| 6–8 min | Security and admin | Use checks 8 and 9. Compare the anonymous GET with the authenticated USER delete. Show the token request, cookie handling and ADMIN-only matcher. Use check 4 to show successful deletion. |
| 8–12 min | User interface, then admin speaker | Log in as user, search for Clamp, reserve one unit, fill in the instructor and subject, review the date, accept terms and confirm. Find the same reference in My Reservations. As admin, confirm pickup and record return. Trace the write from ReservationController to ReservationService, the repositories and H2. |

Run the saved examples before presenting. From the module, use `py docs/verify_api.py http://127.0.0.1:8080 --evidence` on Windows. The numbered output lets each speaker find their example. Use `py docs/verify_reservations.py` for the reservation checks. For manual requests use `docs/try-api.ps1` to see how to retain a session and supply a CSRF token.

Each member should add their name, a specific personal change, and a test they personally ran to the editable report. Practice one request through all layers and explain one failure. Save the edited report as PDF again before submission.
