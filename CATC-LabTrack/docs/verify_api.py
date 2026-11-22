"""Run the SD1 guide's ten checks against a fresh local LabTrack instance."""
import base64
import http.cookiejar
import json
import sys
import urllib.error
import urllib.request

BASE = sys.argv[1] if len(sys.argv) > 1 else "http://127.0.0.1:8080"
EQUIPMENT = "/api/equipment"


class Client:
    def __init__(self, credentials=None):
        self.opener = urllib.request.build_opener(
            urllib.request.HTTPCookieProcessor(http.cookiejar.CookieJar())
        )
        self.headers = {}
        if credentials:
            encoded = base64.b64encode(credentials.encode()).decode()
            self.headers["Authorization"] = "Basic " + encoded

    def request(self, method, path, expected, body=None):
        headers = self.headers.copy()
        data = None
        if body is not None:
            data = json.dumps(body).encode()
            headers["Content-Type"] = "application/json"
        request = urllib.request.Request(BASE + path, data, headers, method=method)
        try:
            response = self.opener.open(request, timeout=10)
        except urllib.error.HTTPError as error:
            response = error
        with response:
            actual = response.code
            raw = response.read()
            if actual != expected:
                raise AssertionError(f"{method} {path}: expected {expected}, got {actual}: {raw!r}")
            print(f"  {method} {path} — expected {expected}, got {actual}", flush=True)
            result = json.loads(raw) if raw else None
            if path != "/api/csrf" and "--evidence" in sys.argv:
                if body is not None:
                    print("    Request body:", json.dumps(body))
                print("    Response body:", json.dumps(result) if raw else "(empty)")
            return result

    def csrf(self):
        response = self.request("GET", "/api/csrf", 200)
        self.headers[response["header"]] = response["token"]


def verify():
    user = Client("user:user-demo")
    admin = Client("admin:admin-demo")
    user.csrf()
    admin.csrf()
    item = {
        "assetCode": "EE-MM-001",
        "name": "Digital Multimeter",
        "location": "EE Laboratory",
        "status": "AVAILABLE",
    }

    print("1. Create valid equipment")
    created = user.request("POST", EQUIPMENT, 201, item)
    assert isinstance(created["id"], int)
    path = f"{EQUIPMENT}/{created['id']}"

    print("2. List, retrieve and filter")
    assert created in user.request("GET", EQUIPMENT, 200)
    assert user.request("GET", path, 200) == created
    assert created in user.request("GET", EQUIPMENT + "?location=ee", 200)
    assert user.request("GET", EQUIPMENT + "?location=no-match", 200) == []

    print("3. Update with unchanged asset code")
    update = dict(item, status="BORROWED")
    updated = user.request("PUT", path, 200, update)
    assert all(updated[key] == value for key, value in dict(update, id=created["id"]).items())
    assert user.request("GET", path, 200) == updated

    print("4. Delete as ADMIN")
    disposable = admin.request("POST", EQUIPMENT, 201, dict(item, assetCode="EE-MM-002"))
    disposable_path = f"{EQUIPMENT}/{disposable['id']}"
    admin.request("DELETE", disposable_path, 204)
    admin.request("GET", disposable_path, 404)

    print("5. Invalid input and invalid enum")
    assert "name" in user.request("POST", EQUIPMENT, 400, dict(item, name=""))["field"]
    assert "message" in user.request("POST", EQUIPMENT, 400, dict(item, status="unknown"))
    assert "message" in user.request("POST", EQUIPMENT, 400, dict(item, status=0))

    print("6. Missing item")
    assert "message" in user.request("GET", EQUIPMENT + "/999999", 404)

    user.request("PUT", EQUIPMENT + "/999999", 404, item)
    admin.request("DELETE", EQUIPMENT + "/999999", 404)

    print("7. Duplicate code on create and update")
    user.request("POST", EQUIPMENT, 409, dict(item, assetCode="ee-mm-001"))
    other = user.request("POST", EQUIPMENT, 201, dict(item, assetCode="EE-MM-003"))
    other_path = f"{EQUIPMENT}/{other['id']}"
    user.request("PUT", other_path, 409, item)
    assert user.request("GET", other_path, 200) == other
    assert user.request("GET", path, 200) == updated

    print("8. Missing and invalid credentials")
    Client().request("GET", EQUIPMENT, 401)
    Client("user:wrong-password").request("GET", EQUIPMENT, 401)

    print("9. Deny USER delete with a valid token")
    user.request("DELETE", path, 403)
    assert user.request("GET", path, 200) == updated

    print("10. Retrieve a new record in a separate request")
    saved = user.request("POST", EQUIPMENT, 201, dict(item, assetCode="EE-MM-004"))
    assert admin.request("GET", f"{EQUIPMENT}/{saved['id']}", 200) == saved
    print("All ten checks passed.")


if __name__ == "__main__":
    verify()
