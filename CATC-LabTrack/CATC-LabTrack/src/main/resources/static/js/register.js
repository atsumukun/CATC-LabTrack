const csrf = document.querySelector("#create-form").dataset;

document.addEventListener("submit", async event => {
  const form = event.target;
  if (!form.matches(".equipment-action")) return;
  event.preventDefault();
  const method = form.dataset.method;
  if (method === "DELETE" && !window.confirm(`Delete equipment ${form.dataset.code}?`)) return;
  const button = form.querySelector("button");
  const feedback = form.querySelector("output");
  button.disabled = true;
  try {
    const response = await fetch(form.dataset.url, {
      method,
      headers: {"Content-Type": "application/json", [csrf.header]: csrf.token},
      body: method === "DELETE" ? undefined : JSON.stringify(Object.fromEntries(new FormData(form)))
    });
    if (response.ok) {
      window.location.reload();
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
