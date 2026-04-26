// ── Confirmation modal component ──────────────────────────────────────────
//
// Drop-in replacement for window.confirm() with proper styling, keyboard
// support, and async/await ergonomics.
//
// Public API:
//   await confirmDialog({ title, message, confirmText, cancelText, danger })
//
// Resolves to true when the user confirms, false otherwise. Escape and
// clicking the backdrop cancel.

(function attachConfirm(global) {
  let activeDialog = null;

  function ensureRoot() {
    let root = document.getElementById("confirmRoot");
    if (!root) {
      root = document.createElement("div");
      root.id = "confirmRoot";
      document.body.appendChild(root);
    }
    return root;
  }

  function cleanup(dialog, result, resolve) {
    if (!dialog || dialog.dataset.closing === "true") return;
    dialog.dataset.closing = "true";
    document.removeEventListener("keydown", dialog._onKey, true);
    dialog.classList.remove("confirm--visible");
    setTimeout(() => {
      dialog.remove();
      activeDialog = null;
      resolve(result);
    }, 160);
  }

  function open(options) {
    const opts = options || {};
    const title = opts.title || "Confirm";
    const message = opts.message || "Are you sure?";
    const confirmText = opts.confirmText || "Confirm";
    const cancelText = opts.cancelText || "Cancel";
    const danger = Boolean(opts.danger);

    if (activeDialog) {
      cleanup(activeDialog, false, () => {});
    }

    return new Promise((resolve) => {
      const root = ensureRoot();
      const dialog = document.createElement("div");
      dialog.className = "confirm-backdrop";
      dialog.innerHTML = `
        <div class="confirm-panel" role="dialog" aria-modal="true" aria-labelledby="confirmTitle">
          <h3 id="confirmTitle" class="confirm-title"></h3>
          <p class="confirm-message"></p>
          <div class="confirm-actions">
            <button type="button" class="btn-sm btn-secondary confirm-cancel"></button>
            <button type="button" class="btn-sm confirm-ok"></button>
          </div>
        </div>
      `;
      dialog.querySelector(".confirm-title").textContent = title;
      dialog.querySelector(".confirm-message").textContent = message;
      const cancelBtn = dialog.querySelector(".confirm-cancel");
      const okBtn = dialog.querySelector(".confirm-ok");
      cancelBtn.textContent = cancelText;
      okBtn.textContent = confirmText;
      okBtn.classList.add(danger ? "btn-danger" : "btn-primary");

      cancelBtn.addEventListener("click", () => cleanup(dialog, false, resolve));
      okBtn.addEventListener("click", () => cleanup(dialog, true, resolve));
      dialog.addEventListener("click", (event) => {
        if (event.target === dialog) cleanup(dialog, false, resolve);
      });

      dialog._onKey = (event) => {
        if (event.key === "Escape") {
          event.preventDefault();
          cleanup(dialog, false, resolve);
        }
        if (event.key === "Enter") {
          event.preventDefault();
          cleanup(dialog, true, resolve);
        }
      };
      document.addEventListener("keydown", dialog._onKey, true);

      root.appendChild(dialog);
      activeDialog = dialog;
      requestAnimationFrame(() => {
        dialog.classList.add("confirm--visible");
        okBtn.focus();
      });
    });
  }

  global.confirmDialog = open;
})(window);
