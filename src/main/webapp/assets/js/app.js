document.addEventListener("DOMContentLoaded", () => {
  document.querySelectorAll("form[data-validate]").forEach(form => {
    form.addEventListener("submit", event => {
      if (!form.checkValidity()) { event.preventDefault(); form.reportValidity(); }
    });
  });
  const send = document.getElementById("sendOtp");
  if (send) {
    send.addEventListener("click", async () => {
      const email = document.getElementById("employeeEmail");
      const message = document.getElementById("otpMessage");
      if (!email.reportValidity()) return;
      send.disabled = true;
      send.textContent = "Sending…";
      message.textContent = "";
      try {
        const form = document.getElementById("employeeForm");
        const token = form.querySelector('[name="csrfToken"]').value;
        const data = new URLSearchParams({ email: email.value.trim(), csrfToken: token });
        const response = await fetch(send.dataset.url, { method: "POST", headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" }, body: data });
        const text = await response.text();
        message.textContent = text;
        message.className = "helper span-2 " + (response.ok ? "helper-success" : "helper-error");
      } catch (e) {
        message.textContent = "Could not contact the server. Please try again.";
        message.className = "helper span-2 helper-error";
      } finally {
        send.disabled = false;
        send.textContent = "Send OTP";
      }
    });
  }
});
