// ── Toast component ───────────────────────────────────────────────────────
//
// Reusable, queued, dismissible toast notifications. Replaces the single-slot
// #globalStatus banner with stackable notifications and per-toast timeouts.
//
// Public API:
//   toast.show(message, { type, duration })  → returns the toast element
//   toast.info(message, opts)
//   toast.success(message, opts)
//   toast.warning(message, opts)
//   toast.error(message, opts)
//   toast.dismissAll()
//
// `type` ∈ { "info", "success", "warning", "error" } (default "info").
// `duration` is in ms; pass 0 to keep the toast visible until dismissed.

(function attachToast(global) {
  const DEFAULT_DURATION = 3500;
  const ERROR_DURATION = 6000;
  const MAX_TOASTS = 5;

  function ensureContainer() {
    let container = document.getElementById("toastContainer");
    if (!container) {
      container = document.createElement("div");
      container.id = "toastContainer";
      container.className = "toast-container";
      document.body.appendChild(container);
    }
    return container;
  }

  function iconFor(type) {
    switch (type) {
      case "success": return "✓";
      case "warning": return "!";
      case "error":   return "✕";
      default:        return "i";
    }
  }

  function defaultDuration(type) {
    return type === "error" ? ERROR_DURATION : DEFAULT_DURATION;
  }

  function dismiss(element) {
    if (!element || element.dataset.dismissing === "true") return;
    element.dataset.dismissing = "true";
    element.classList.add("toast--leaving");
    element.addEventListener("transitionend", () => element.remove(), { once: true });
    setTimeout(() => element.remove(), 400);
  }

  function trimQueue(container) {
    const existing = container.querySelectorAll(".toast");
    if (existing.length <= MAX_TOASTS) return;
    for (let i = 0; i < existing.length - MAX_TOASTS; i += 1) {
      dismiss(existing[i]);
    }
  }

  function show(message, options) {
    const opts = options || {};
    const type = opts.type || "info";
    const duration = typeof opts.duration === "number" ? opts.duration : defaultDuration(type);

    const container = ensureContainer();
    const element = document.createElement("div");
    element.className = `toast toast--${type}`;
    element.setAttribute("role", type === "error" ? "alert" : "status");
    element.innerHTML = `
      <span class="toast__icon" aria-hidden="true">${iconFor(type)}</span>
      <span class="toast__message"></span>
      <button class="toast__close" aria-label="Dismiss">×</button>
    `;
    element.querySelector(".toast__message").textContent = String(message);
    element.querySelector(".toast__close").addEventListener("click", () => dismiss(element));
    container.appendChild(element);

    requestAnimationFrame(() => element.classList.add("toast--visible"));
    trimQueue(container);

    if (duration > 0) {
      setTimeout(() => dismiss(element), duration);
    }
    return element;
  }

  const api = {
    show,
    info:    (msg, opts) => show(msg, { ...(opts || {}), type: "info" }),
    success: (msg, opts) => show(msg, { ...(opts || {}), type: "success" }),
    warning: (msg, opts) => show(msg, { ...(opts || {}), type: "warning" }),
    error:   (msg, opts) => show(msg, { ...(opts || {}), type: "error" }),
    dismissAll: () => {
      const container = document.getElementById("toastContainer");
      if (!container) return;
      container.querySelectorAll(".toast").forEach(dismiss);
    }
  };

  global.toast = api;
})(window);
