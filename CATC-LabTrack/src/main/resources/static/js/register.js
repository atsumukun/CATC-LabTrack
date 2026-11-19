const form = document.querySelector("#create-form");
form.addEventListener("submit", async event => {
  event.preventDefault();
  const button = form.querySelector("button");
  const feedback = form.querySelector("output");
  button.disabled = true;
  try {
    const response = await fetch("/api/equipment", {
      method: "POST",
      headers: {"Content-Type": "application/json", [form.dataset.header]: form.dataset.token},
      body: JSON.stringify(Object.fromEntries(new FormData(form)))
    });
    if (response.ok) {
      window.location.assign(window.location.pathname);
      return;
    }
    const error = await response.json().catch(() => ({}));
    feedback.textContent = error.message || `Request failed (${response.status}). Refresh and try again.`;
  } catch {
    feedback.textContent = "Could not reach LabTrack. Check the server and try again.";
  } finally {
    button.disabled = false;
  }
});
