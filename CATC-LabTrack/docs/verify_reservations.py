"""Run against the local demo with Python 3 after starting LabTrack."""
from concurrent.futures import ThreadPoolExecutor
from datetime import date, timedelta
from uuid import uuid4
import urllib.error

from verify_api import Client, EQUIPMENT


def reserve(client, payload):
    # Capture the actual status for concurrent requests, whose order varies.
    import json
    import urllib.request
    headers = dict(client.headers, **{"Content-Type": "application/json"})
    from verify_api import BASE
    request = urllib.request.Request(BASE + "/api/reservations", json.dumps(payload).encode(), headers)
    try:
        response = client.opener.open(request, timeout=10)
    except urllib.error.HTTPError as error:
        response = error
    with response:
        return response.code


def verify():
    users = [Client("user:user-demo") for _ in range(2)]
    admin = Client("admin:admin-demo")
    for client in [*users, admin]:
        client.csrf()
    item = dict(assetCode="TEST-" + uuid4().hex[:8], name="Test meter", location="Test lab",
                status="AVAILABLE", totalUnits=1)
    created = admin.request("POST", EQUIPMENT, 201, item)
    path = EQUIPMENT + "/" + str(created["id"])
    request = dict(equipmentId=created["id"], quantity=1, borrowerName="Demo Student",
                   yearLevel="1st Year", course="Engineering", subject="Circuits",
                   useDate=str(date.today() + timedelta(days=1)))
    users[0].request("POST", "/api/reservations", 400, dict(request, quantity=0))
    users[0].request("POST", "/api/reservations", 400, dict(request, quantity=4))
    with ThreadPoolExecutor(max_workers=2) as pool:
        results = list(pool.map(lambda user: reserve(user, request), users))
    assert sorted(results) == [201, 409], results
    stock = admin.request("GET", path, 200)
    assert stock["availableUnits"] == 0 and stock["status"] == "RESERVED"
    mine = users[0].request("GET", "/api/reservations/mine", 200)
    assert sum(row["equipmentId"] == created["id"] for row in mine) == 1
    assert all(row["equipmentId"] != created["id"] for row in admin.request("GET", "/api/reservations/mine", 200))
    saved = next(row for row in mine if row["equipmentId"] == created["id"])
    detail = "/api/reservations/" + str(saved["id"])
    assert users[0].request("GET", detail, 200) == saved
    admin.request("GET", detail, 404)
    admin.request("DELETE", path, 409)
    print("PASS: quantity limits, competing requests for the final unit, stock and per-account history.")


if __name__ == "__main__":
    verify()
