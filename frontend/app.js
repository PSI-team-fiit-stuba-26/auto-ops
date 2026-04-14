// ── State ──────────────────────────────────────────────────────────────────
const state = {
  apiBase: localStorage.getItem("autoopsApiBase") || "http://localhost:8080",
  token: localStorage.getItem("autoopsToken") || null,
  currentUser: JSON.parse(localStorage.getItem("autoopsUser") || "null"),
  orders: [],
  customers: [],
  vehicles: [],
  mechanics: [],
  inventory: [],
  operations: [],
  notifications: [],
  history: [],
  currentSection: "dashboard"
};

// ── Helpers ────────────────────────────────────────────────────────────────
const $ = (sel) => document.querySelector(sel);
const esc = (v) => String(v ?? "").replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;");
const money = (v) => v != null ? `€${Number(v).toFixed(2)}` : "—";
const fmtDate = (s) => s ? new Date(s).toLocaleString("sk-SK", { dateStyle: "short", timeStyle: "short" }) : "—";

function statusBadge(status) {
  const map = {
    SCHEDULED: "badge-blue", IN_PROGRESS: "badge-yellow", WAITING_FOR_PARTS: "badge-yellow",
    READY_FOR_PAYMENT: "badge-green", COMPLETED: "badge-gray", CANCELLED: "badge-red", NEW: "badge-gray"
  };
  return `<span class="badge ${map[status] || "badge-gray"}">${esc(status)}</span>`;
}
function invBadge(status) {
  const map = { AVAILABLE: "badge-green", LOW_STOCK: "badge-yellow", RESERVED: "badge-blue", OUT_OF_STOCK: "badge-red" };
  return `<span class="badge ${map[status] || "badge-gray"}">${esc(status)}</span>`;
}

function setStatus(msg, type = "info") {
  const el = $("#globalStatus");
  el.textContent = msg;
  el.className = `global-status ${type}`;
  if (type !== "error") setTimeout(() => { el.className = "global-status"; }, 3500);
}

// ── API ────────────────────────────────────────────────────────────────────
async function api(path, options = {}) {
  const headers = { "Content-Type": "application/json", ...(options.headers || {}) };
  if (state.token) {
    headers["Authorization"] = `Bearer ${state.token}`;
  }
  const res = await fetch(`${state.apiBase}${path}`, {
    headers,
    ...options
  });
  const text = await res.text();
  const data = text ? JSON.parse(text) : null;
  if (res.status === 401 || res.status === 403) {
    if (res.status === 401) {
      logout();
    }
    throw new Error(data?.message || res.statusText || "Unauthorized");
  }
  if (!res.ok) throw new Error(data?.message || res.statusText);
  return data;
}

// ── Auth ──────────────────────────────────────────────────────────────────
function checkAuth() {
  if (!state.token || !state.currentUser) {
    $("#loginScreenContainer").classList.remove("hidden");
    return false;
  }
  $("#loginScreenContainer").classList.add("hidden");
  $("#currentUserLabel").textContent = `${state.currentUser.name} (${state.currentUser.role})`;
  applyRoleUI();
  return true;
}

document.querySelectorAll(".btn-login-option").forEach((btn) => {
  btn.addEventListener("click", async () => {
    const email = btn.dataset.email;
    try {
      const user = await api("/api/auth/login", {
        method: "POST",
        body: JSON.stringify({ email })
      });
      state.token = user.id;
      state.currentUser = user;
      localStorage.setItem("autoopsToken", state.token);
      localStorage.setItem("autoopsUser", JSON.stringify(user));
      checkAuth();
      loadAll();
    } catch (err) {
      alert("Login failed: " + err.message);
    }
  });
});

$("#logoutBtn").addEventListener("click", logout);

function logout() {
  state.token = null;
  state.currentUser = null;
  localStorage.removeItem("autoopsToken");
  localStorage.removeItem("autoopsUser");
  checkAuth();
}

function applyRoleUI() {
  const role = state.currentUser?.role;
  const isClient = role === "CUSTOMER";
  const isMechanic = role === "MECHANIC";

  // Hide parts of UI based on roles
  const hideIf = (sel, cond) => {
    document.querySelectorAll(sel).forEach(el => {
      if (cond) el.style.display = "none";
      else el.style.display = "";
    });
  };

  hideIf(".nav-item[data-section='customers']", isClient || isMechanic);
  hideIf(".nav-item[data-section='mechanics']", isClient);
  hideIf(".nav-item[data-section='inventory']", isClient);
  hideIf(".nav-item[data-section='services']", isClient);
  hideIf(".section-toolbar", isClient); // Clients usually can't create things here
  hideIf(".action-delete", isClient || isMechanic);
  hideIf(".action-complete", isClient);
  hideIf(".action-pay", isMechanic);
}

// ── Load all data ──────────────────────────────────────────────────────────
async function loadAll() {
  if (!checkAuth()) return;
  try {
    const [orders, customers, vehicles, mechanics, inventory, operations, notifications, history] = await Promise.all([
      api("/api/repair-orders"),
      api("/api/planning/customers"),
      api("/api/planning/vehicles"),
      api("/api/mechanics"),
      api("/api/inventory"),
      api("/api/operations"),
      api("/api/notifications"),
      api("/api/service-history")
    ]);
    state.orders = orders;
    state.customers = customers;
    state.vehicles = vehicles;
    state.mechanics = mechanics;
    state.inventory = inventory;
    state.operations = operations;
    state.notifications = notifications;
    state.history = history;
    renderCurrentSection();
    setStatus("Connected to backend.", "ok");
  } catch (e) {
    setStatus(`Backend error: ${e.message}`, "error");
  }
}

function renderCurrentSection() {
  const s = state.currentSection;
  if (s === "dashboard") renderDashboard();
  else if (s === "repair-orders") renderOrders();
  else if (s === "customers") renderCustomers();
  else if (s === "vehicles") renderVehicles();
  else if (s === "mechanics") renderMechanics();
  else if (s === "inventory") renderInventory();
  else if (s === "services") renderServices();
  else if (s === "history") renderHistory();
  else if (s === "notifications") renderNotifications();
}

// ── Navigation ────────────────────────────────────────────────────────────
const TITLES = {
  dashboard: "Dashboard", "repair-orders": "Repair Orders", customers: "Customers",
  vehicles: "Vehicles", mechanics: "Mechanics", inventory: "Inventory",
  services: "Services & Pricing", history: "Service History", notifications: "Notifications"
};

document.querySelectorAll(".nav-item").forEach((btn) => {
  btn.addEventListener("click", () => {
    const sec = btn.dataset.section;
    document.querySelectorAll(".nav-item").forEach((b) => b.classList.remove("is-active"));
    document.querySelectorAll(".section").forEach((s) => s.classList.remove("is-active"));
    btn.classList.add("is-active");
    $(`#sec-${sec}`).classList.add("is-active");
    $("#pageTitle").textContent = TITLES[sec] || sec;
    state.currentSection = sec;
    renderCurrentSection();
  });
});

// ── API base save ──────────────────────────────────────────────────────────
// $("#saveApi").addEventListener("click", () => {
//   state.apiBase = $("#apiBase").value.replace(/\/$/, "");
//   localStorage.setItem("autoopsApiBase", state.apiBase);
//   loadAll();
// });

// ── MODAL ─────────────────────────────────────────────────────────────────
function openModal(title, html, onSubmit) {
  $("#modalTitle").textContent = title;
  $("#modalBody").innerHTML = html;
  $("#modalOverlay").classList.remove("hidden");
  const form = $("#modalBody form");
  if (form && onSubmit) {
    form.addEventListener("submit", async (e) => {
      e.preventDefault();
      try {
        await onSubmit(new FormData(form), form);
        closeModal();
      } catch (err) {
        setStatus(err.message, "error");
      }
    });
  }
}
function closeModal() { $("#modalOverlay").classList.add("hidden"); }
$("#modalClose").addEventListener("click", closeModal);
$("#modalOverlay").addEventListener("click", (e) => { if (e.target === $("#modalOverlay")) closeModal(); });

// ══════════════════════════════════════════════════════════════════════════
// DASHBOARD
// ══════════════════════════════════════════════════════════════════════════
function renderDashboard() {
  const activeOrders = state.orders.filter((o) => !["COMPLETED", "CANCELLED"].includes(o.status));
  const lowStock = state.inventory.filter((i) => i.status === "LOW_STOCK" || i.status === "OUT_OF_STOCK");
  $("#stat-orders").textContent = activeOrders.length;
  $("#stat-customers").textContent = state.customers.length;
  $("#stat-mechanics").textContent = state.mechanics.length;
  $("#stat-lowstock").textContent = lowStock.length;

  const ordersEl = $("#dash-orders");
  const recent = state.orders.slice(-5).reverse();
  ordersEl.innerHTML = recent.length ? recent.map((o) => `
    <div class="list-item">
      <div class="list-item-head">
        <span class="list-item-title">${esc(o.problemDescription)}</span>
        ${statusBadge(o.status)}
      </div>
      <div class="list-item-meta">${fmtDate(o.plannedStart)} · Tasks ${o.tasks.filter((t) => t.status === "DONE").length}/${o.tasks.length}</div>
    </div>
  `).join("") : '<div class="empty-state">No repair orders yet.</div>';

  const notifEl = $("#dash-notifications");
  const recent5 = state.notifications.slice(-5).reverse();
  notifEl.innerHTML = recent5.length ? recent5.map((n) => `
    <div class="list-item">
      <div class="list-item-head">
        <span class="list-item-title">${esc(n.type || "Notification")}</span>
        <span class="badge badge-blue">${esc(n.type)}</span>
      </div>
      <div class="list-item-meta">${esc(n.message)}</div>
    </div>
  `).join("") : '<div class="empty-state">No notifications yet.</div>';
}

// ══════════════════════════════════════════════════════════════════════════
// REPAIR ORDERS
// ══════════════════════════════════════════════════════════════════════════
function mechanicName(id) {
  const m = state.mechanics.find((m) => m.id === id);
  return m ? m.name : id ? id.slice(0, 8) + "…" : "—";
}

function renderOrders() {
  const tbody = $("#ordersTbody");
  if (!state.orders.length) {
    tbody.innerHTML = '<tr><td colspan="6" class="empty-state">No repair orders found.</td></tr>';
    return;
  }
  tbody.innerHTML = state.orders.map((o) => `
    <tr>
      <td><strong>${esc(o.problemDescription)}</strong></td>
      <td>${statusBadge(o.status)}</td>
      <td>${esc(mechanicName(o.mechanicId))}</td>
      <td>${fmtDate(o.plannedStart)}</td>
      <td>${o.tasks.filter((t) => t.status === "DONE").length}/${o.tasks.length}</td>
      <td>
        <div class="table-actions">
          ${o.status !== "READY_FOR_PAYMENT" && o.status !== "COMPLETED" ? `<button class="btn-sm btn-primary action-complete" onclick="openCompleteModal('${o.id}')">Complete</button>` : ""}
          ${o.status === "READY_FOR_PAYMENT" ? `<button class="btn-sm btn-primary action-pay" onclick="payRepairOrder('${o.id}')">Pay</button>` : ""}
          <button class="btn-sm btn-danger action-delete" onclick="deleteOrder('${o.id}')">Delete</button>
        </div>
      </td>
    </tr>
  `).join("");
}

$("#newOrderBtn").addEventListener("click", () => {
  const mechOptions = state.mechanics.map((m) => `<option value="${m.id}">${esc(m.name)} (${m.specialties.join(", ")})</option>`).join("");
  const now = new Date(Date.now() + 2 * 3600000);
  const end = new Date(Date.now() + 5 * 3600000);
  const fmt = (d) => d.toISOString().slice(0, 16);
  openModal("New Repair Order", `
    <form class="form-grid" id="newOrderForm">
      <label>Customer Name<input name="customerName" required placeholder="Peter Novak"></label>
      <label>Customer Email<input name="customerEmail" type="email" required placeholder="peter@example.com"></label>
      <label>Customer Phone<input name="customerPhone" placeholder="+421900111222"></label>
      <label>VIN<input name="vin" required placeholder="WVWZZZ1KZ6W000001"></label>
      <label>License Plate<input name="licensePlate" required placeholder="BA-123AB"></label>
      <label>Brand<input name="brand" placeholder="Volkswagen"></label>
      <label>Model<input name="model" placeholder="Golf"></label>
      <label>Year<input name="year" type="number" value="2020"></label>
      <label>Mileage<input name="mileage" type="number" value="80000"></label>
      <label>Max Price (€)<input name="maxPrice" type="number" value="400" min="0" step="0.01"></label>
      <label>Mechanic<select name="mechanicId">${mechOptions}</select></label>
      <label>Planned Start<input name="plannedStart" type="datetime-local" value="${fmt(now)}" required></label>
      <label>Planned Finish<input name="plannedCompletionDate" type="datetime-local" value="${fmt(end)}" required></label>
      <label class="full">Problem Description<textarea name="problemDescription" rows="2" required placeholder="Describe the issue…"></textarea></label>
      <label class="full">Tasks (one per line)<textarea name="taskNames" rows="3" placeholder="Diagnose issue&#10;Repair&#10;Road test"></textarea></label>
      <div class="form-actions"><button type="submit" class="btn-primary">Create Repair Order</button></div>
    </form>
  `, async (fd) => {
    const payload = {
      customerName: fd.get("customerName"), customerEmail: fd.get("customerEmail"),
      customerPhone: fd.get("customerPhone"), vin: fd.get("vin"),
      licensePlate: fd.get("licensePlate"), brand: fd.get("brand"), model: fd.get("model"),
      year: Number(fd.get("year")), mileage: Number(fd.get("mileage")),
      problemDescription: fd.get("problemDescription"),
      maxPrice: String(fd.get("maxPrice")),
      mechanicId: fd.get("mechanicId"),
      plannedStart: fd.get("plannedStart"),
      plannedCompletionDate: fd.get("plannedCompletionDate"),
      taskNames: String(fd.get("taskNames") || "").split("\n").map((s) => s.trim()).filter(Boolean)
    };
    await api("/api/repair-orders", { method: "POST", body: JSON.stringify(payload) });
    state.orders = await api("/api/repair-orders");
    renderOrders();
    setStatus("Repair order created.", "ok");
  });
});

$("#refreshOrdersBtn").addEventListener("click", async () => {
  state.orders = await api("/api/repair-orders");
  renderOrders();
  setStatus("Refreshed.", "ok");
});

async function deleteOrder(id) {
  if (!confirm("Delete this repair order?")) return;
  await api(`/api/repair-orders/${id}`, { method: "DELETE" });
  state.orders = state.orders.filter((o) => o.id !== id);
  renderOrders();
  setStatus("Repair order deleted.", "ok");
}
window.deleteOrder = deleteOrder;

async function payRepairOrder(id) {
  if (!confirm("Mark this invoice as paid?")) return;
  try {
    await api(`/api/repairs/${id}/pay`, { method: "POST" });
    state.orders = await api("/api/repair-orders");
    renderOrders();
    setStatus("Payment received. Repair order marked as completed.", "ok");
  } catch (err) {
    setStatus(err.message, "error");
  }
}
window.payRepairOrder = payRepairOrder;

// ── Complete repair modal ──────────────────────────────────────────────────
function openCompleteModal(orderId) {
  const order = state.orders.find((o) => o.id === orderId);
  if (!order) return;
  const form = $("#completeRepairForm");
  form.querySelector("[name=repairOrderId]").value = orderId;
  $("#invoiceResult").className = "invoice-output hidden";
  $("#invoiceResult").textContent = "";
  $("#completeOverlay").classList.remove("hidden");
}
window.openCompleteModal = openCompleteModal;

$("#completeClose").addEventListener("click", () => $("#completeOverlay").classList.add("hidden"));
$("#completeOverlay").addEventListener("click", (e) => { if (e.target === $("#completeOverlay")) $("#completeOverlay").classList.add("hidden"); });

$("#completeRepairForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const fd = new FormData(e.currentTarget);
  const orderId = fd.get("repairOrderId");
  const payload = {
    actualWorkHours: Number(fd.get("actualWorkHours")),
    note: fd.get("note"),
    markAllTasksDone: fd.get("markAllTasksDone") === "on"
  };
  try {
    const result = await api(`/api/repairs/${orderId}/complete`, { method: "POST", body: JSON.stringify(payload) });
    const out = $("#invoiceResult");
    out.textContent = JSON.stringify(result.invoice, null, 2);
    out.className = "invoice-output";
    state.orders = await api("/api/repair-orders");
    renderOrders();
    setStatus(`Invoice ${result.invoice.invoiceNumber} issued.`, "ok");
  } catch (err) {
    setStatus(err.message, "error");
  }
});

// ══════════════════════════════════════════════════════════════════════════
// CUSTOMERS
// ══════════════════════════════════════════════════════════════════════════
function renderCustomers(filter = "") {
  const tbody = $("#customersTbody");
  const list = filter ? state.customers.filter((c) =>
    c.name.toLowerCase().includes(filter) || c.email.toLowerCase().includes(filter)
  ) : state.customers;
  if (!list.length) {
    tbody.innerHTML = '<tr><td colspan="4" class="empty-state">No customers found.</td></tr>';
    return;
  }
  tbody.innerHTML = list.map((c) => `
    <tr>
      <td><strong>${esc(c.name)}</strong></td>
      <td>${esc(c.email)}</td>
      <td>${esc(c.phone)}</td>
      <td>
        <div class="table-actions">
          <button class="btn-sm btn-secondary" onclick="editCustomer('${c.id}')">Edit</button>
          <button class="btn-sm btn-danger" onclick="deleteCustomer('${c.id}')">Delete</button>
        </div>
      </td>
    </tr>
  `).join("");
}

$("#customerSearch").addEventListener("input", (e) => renderCustomers(e.target.value.toLowerCase()));

$("#newCustomerBtn").addEventListener("click", () => {
  openModal("New Customer", `
    <form class="form-grid">
      <label class="full">Full Name<input name="name" required placeholder="Peter Novak"></label>
      <label>Email<input name="email" type="email" required placeholder="peter@example.com"></label>
      <label>Phone<input name="phone" placeholder="+421900111222"></label>
      <div class="form-actions"><button type="submit" class="btn-primary">Create Customer</button></div>
    </form>
  `, async (fd) => {
    const c = await api("/api/planning/customers", {
      method: "POST",
      body: JSON.stringify({ name: fd.get("name"), email: fd.get("email"), phone: fd.get("phone") })
    });
    state.customers.push(c);
    renderCustomers();
    setStatus("Customer created.", "ok");
  });
});

function editCustomer(id) {
  const c = state.customers.find((x) => x.id === id);
  if (!c) return;
  openModal("Edit Customer", `
    <form class="form-grid">
      <label class="full">Full Name<input name="name" value="${esc(c.name)}" required></label>
      <label>Email<input name="email" type="email" value="${esc(c.email)}" required></label>
      <label>Phone<input name="phone" value="${esc(c.phone)}"></label>
      <div class="form-actions"><button type="submit" class="btn-primary">Save Changes</button></div>
    </form>
  `, async (fd) => {
    const updated = await api(`/api/planning/customers/${id}`, {
      method: "PUT",
      body: JSON.stringify({ name: fd.get("name"), email: fd.get("email"), phone: fd.get("phone") })
    });
    const idx = state.customers.findIndex((x) => x.id === id);
    if (idx >= 0) state.customers[idx] = updated;
    renderCustomers();
    setStatus("Customer updated.", "ok");
  });
}
window.editCustomer = editCustomer;

async function deleteCustomer(id) {
  if (!confirm("Delete this customer?")) return;
  await api(`/api/planning/customers/${id}`, { method: "DELETE" });
  state.customers = state.customers.filter((c) => c.id !== id);
  renderCustomers();
  setStatus("Customer deleted.", "ok");
}
window.deleteCustomer = deleteCustomer;

// ══════════════════════════════════════════════════════════════════════════
// VEHICLES
// ══════════════════════════════════════════════════════════════════════════
function renderVehicles(filter = "") {
  const tbody = $("#vehiclesTbody");
  const list = filter ? state.vehicles.filter((v) =>
    v.vin.toLowerCase().includes(filter) ||
    v.licensePlate.toLowerCase().includes(filter) ||
    `${v.brand} ${v.model}`.toLowerCase().includes(filter)
  ) : state.vehicles;
  if (!list.length) {
    tbody.innerHTML = '<tr><td colspan="6" class="empty-state">No vehicles found.</td></tr>';
    return;
  }
  tbody.innerHTML = list.map((v) => `
    <tr>
      <td><strong>${esc(v.licensePlate)}</strong></td>
      <td>${esc(v.brand)} ${esc(v.model)}</td>
      <td><code style="font-size:11px">${esc(v.vin)}</code></td>
      <td>${v.year}</td>
      <td>${v.mileage.toLocaleString()} km</td>
      <td>
        <div class="table-actions">
          <button class="btn-sm btn-secondary" onclick="editVehicle('${v.id}')">Edit</button>
          <button class="btn-sm btn-danger" onclick="deleteVehicle('${v.id}')">Delete</button>
        </div>
      </td>
    </tr>
  `).join("");
}

$("#vehicleSearch").addEventListener("input", (e) => renderVehicles(e.target.value.toLowerCase()));

$("#newVehicleBtn").addEventListener("click", () => {
  const custOptions = state.customers.map((c) => `<option value="${c.id}">${esc(c.name)} (${esc(c.email)})</option>`).join("");
  openModal("New Vehicle", `
    <form class="form-grid">
      <label class="full">Customer<select name="customerId">${custOptions}</select></label>
      <label>VIN<input name="vin" required placeholder="WVWZZZ1KZ6W000001"></label>
      <label>License Plate<input name="licensePlate" required placeholder="BA-123AB"></label>
      <label>Brand<input name="brand" placeholder="Volkswagen"></label>
      <label>Model<input name="model" placeholder="Golf"></label>
      <label>Year<input name="year" type="number" value="2020"></label>
      <label>Mileage (km)<input name="mileage" type="number" value="0"></label>
      <div class="form-actions"><button type="submit" class="btn-primary">Create Vehicle</button></div>
    </form>
  `, async (fd) => {
    const v = await api("/api/planning/vehicles", {
      method: "POST",
      body: JSON.stringify({
        vin: fd.get("vin"), licensePlate: fd.get("licensePlate"),
        brand: fd.get("brand"), model: fd.get("model"),
        year: Number(fd.get("year")), mileage: Number(fd.get("mileage")),
        customerId: fd.get("customerId")
      })
    });
    state.vehicles.push(v);
    renderVehicles();
    setStatus("Vehicle created.", "ok");
  });
});

function editVehicle(id) {
  const v = state.vehicles.find((x) => x.id === id);
  if (!v) return;
  openModal("Edit Vehicle", `
    <form class="form-grid">
      <label>License Plate<input name="licensePlate" value="${esc(v.licensePlate)}"></label>
      <label>Brand<input name="brand" value="${esc(v.brand)}"></label>
      <label>Model<input name="model" value="${esc(v.model)}"></label>
      <label>Year<input name="year" type="number" value="${v.year}"></label>
      <label>Mileage (km)<input name="mileage" type="number" value="${v.mileage}"></label>
      <div class="form-actions"><button type="submit" class="btn-primary">Save Changes</button></div>
    </form>
  `, async (fd) => {
    const updated = await api(`/api/planning/vehicles/${id}`, {
      method: "PUT",
      body: JSON.stringify({
        licensePlate: fd.get("licensePlate"), brand: fd.get("brand"), model: fd.get("model"),
        year: Number(fd.get("year")), mileage: Number(fd.get("mileage"))
      })
    });
    const idx = state.vehicles.findIndex((x) => x.id === id);
    if (idx >= 0) state.vehicles[idx] = updated;
    renderVehicles();
    setStatus("Vehicle updated.", "ok");
  });
}
window.editVehicle = editVehicle;

async function deleteVehicle(id) {
  if (!confirm("Delete this vehicle?")) return;
  await api(`/api/planning/vehicles/${id}`, { method: "DELETE" });
  state.vehicles = state.vehicles.filter((v) => v.id !== id);
  renderVehicles();
  setStatus("Vehicle deleted.", "ok");
}
window.deleteVehicle = deleteVehicle;

// ══════════════════════════════════════════════════════════════════════════
// MECHANICS
// ══════════════════════════════════════════════════════════════════════════
const SPECIALTIES = ["GENERAL", "ENGINE", "ELECTRICAL", "BODYWORK", "DIAGNOSTICS", "TIRES"];

function specialtyCheckboxes(selected = []) {
  return SPECIALTIES.map((s) =>
    `<label class="check-label" style="display:inline-flex;gap:6px;margin-right:12px">
      <input type="checkbox" name="specialties" value="${s}" ${selected.includes(s) ? "checked" : ""}> ${s}
    </label>`
  ).join("");
}

function renderMechanics() {
  const tbody = $("#mechanicsTbody");
  if (!state.mechanics.length) {
    tbody.innerHTML = '<tr><td colspan="5" class="empty-state">No mechanics found.</td></tr>';
    return;
  }
  tbody.innerHTML = state.mechanics.map((m) => `
    <tr>
      <td><strong>${esc(m.name)}</strong></td>
      <td>${m.specialties.map((s) => `<span class="badge badge-blue" style="margin:1px">${s}</span>`).join(" ")}</td>
      <td>${money(m.wage)}/hr</td>
      <td style="color:var(--muted);font-size:12px">${esc(m.workLimitations)}</td>
      <td>
        <div class="table-actions">
          <button class="btn-sm btn-secondary" onclick="editMechanic('${m.id}')">Edit</button>
          <button class="btn-sm btn-danger" onclick="deleteMechanic('${m.id}')">Delete</button>
        </div>
      </td>
    </tr>
  `).join("");
}

$("#newMechanicBtn").addEventListener("click", () => {
  openModal("New Mechanic", `
    <form class="form-grid">
      <label class="full">Full Name<input name="name" required placeholder="Marek Novák"></label>
      <label>Hourly Wage (€)<input name="wage" type="number" value="35.00" min="0" step="0.5"></label>
      <label>Work Limitations<input name="workLimitations" placeholder="No limitations"></label>
      <label class="full">Specialties<div style="padding-top:6px">${specialtyCheckboxes()}</div></label>
      <div class="form-actions"><button type="submit" class="btn-primary">Create Mechanic</button></div>
    </form>
  `, async (fd, form) => {
    const checked = [...form.querySelectorAll("[name=specialties]:checked")].map((el) => el.value);
    const m = await api("/api/mechanics", {
      method: "POST",
      body: JSON.stringify({ name: fd.get("name"), specialties: checked, workLimitations: fd.get("workLimitations"), wage: String(fd.get("wage")) })
    });
    state.mechanics.push(m);
    renderMechanics();
    setStatus("Mechanic created.", "ok");
  });
});

function editMechanic(id) {
  const m = state.mechanics.find((x) => x.id === id);
  if (!m) return;
  openModal("Edit Mechanic", `
    <form class="form-grid">
      <label class="full">Full Name<input name="name" value="${esc(m.name)}" required></label>
      <label>Hourly Wage (€)<input name="wage" type="number" value="${m.wage}" min="0" step="0.5"></label>
      <label>Work Limitations<input name="workLimitations" value="${esc(m.workLimitations)}"></label>
      <label class="full">Specialties<div style="padding-top:6px">${specialtyCheckboxes(m.specialties)}</div></label>
      <div class="form-actions"><button type="submit" class="btn-primary">Save Changes</button></div>
    </form>
  `, async (fd, form) => {
    const checked = [...form.querySelectorAll("[name=specialties]:checked")].map((el) => el.value);
    const updated = await api(`/api/mechanics/${id}`, {
      method: "PUT",
      body: JSON.stringify({ name: fd.get("name"), specialties: checked, workLimitations: fd.get("workLimitations"), wage: String(fd.get("wage")) })
    });
    const idx = state.mechanics.findIndex((x) => x.id === id);
    if (idx >= 0) state.mechanics[idx] = updated;
    renderMechanics();
    setStatus("Mechanic updated.", "ok");
  });
}
window.editMechanic = editMechanic;

async function deleteMechanic(id) {
  if (!confirm("Delete this mechanic?")) return;
  await api(`/api/mechanics/${id}`, { method: "DELETE" });
  state.mechanics = state.mechanics.filter((m) => m.id !== id);
  renderMechanics();
  setStatus("Mechanic deleted.", "ok");
}
window.deleteMechanic = deleteMechanic;

// ══════════════════════════════════════════════════════════════════════════
// INVENTORY
// ══════════════════════════════════════════════════════════════════════════
function renderInventory(filter = "") {
  const tbody = $("#inventoryTbody");
  const list = filter ? state.inventory.filter((i) =>
    i.code.toLowerCase().includes(filter) || i.name.toLowerCase().includes(filter)
  ) : state.inventory;
  if (!list.length) {
    tbody.innerHTML = '<tr><td colspan="8" class="empty-state">No inventory items found.</td></tr>';
  } else {
    tbody.innerHTML = list.map((i) => `
      <tr>
        <td><code style="font-size:12px">${esc(i.code)}</code></td>
        <td><strong>${esc(i.name)}</strong></td>
        <td>${i.count}</td>
        <td>${i.reservedCount}</td>
        <td>${esc(i.location)}</td>
        <td>${money(i.price)}</td>
        <td>${invBadge(i.status)}</td>
        <td>
          <div class="table-actions">
            <button class="btn-sm btn-secondary" onclick="editItem('${i.id}')">Edit</button>
            <button class="btn-sm btn-danger" onclick="deleteItem('${i.id}')">Delete</button>
          </div>
        </td>
      </tr>
    `).join("");
  }

  // Populate use/reserve selects
  const orderSel = $("#useRepairSelect");
  orderSel.innerHTML = state.orders.map((o) =>
    `<option value="${o.id}">${esc(o.problemDescription)} (${o.status})</option>`
  ).join("") || "<option>No open orders</option>";

  const itemSel = $("#useItemSelect");
  itemSel.innerHTML = state.inventory.map((i) =>
    `<option value="${i.id}">${esc(i.code)} – ${esc(i.name)} (${i.count} in stock)</option>`
  ).join("") || "<option>No items</option>";
}

$("#inventorySearch2").addEventListener("input", (e) => renderInventory(e.target.value.toLowerCase()));

$("#newItemBtn").addEventListener("click", () => {
  openModal("Add Inventory Item", `
    <form class="form-grid">
      <label>Part Code<input name="code" required placeholder="BRK-PAD-ATE"></label>
      <label>Name<input name="name" required placeholder="ATE brake pads"></label>
      <label>Initial Stock<input name="count" type="number" value="0" min="0"></label>
      <label>Location<input name="location" placeholder="A1"></label>
      <label>Unit Price (€)<input name="price" type="number" value="0.00" min="0" step="0.01"></label>
      <div class="form-actions"><button type="submit" class="btn-primary">Add Item</button></div>
    </form>
  `, async (fd) => {
    const item = await api("/api/inventory", {
      method: "POST",
      body: JSON.stringify({
        code: fd.get("code"), name: fd.get("name"),
        count: Number(fd.get("count")), location: fd.get("location"),
        price: String(fd.get("price"))
      })
    });
    state.inventory.push(item);
    renderInventory();
    setStatus("Inventory item added.", "ok");
  });
});

function editItem(id) {
  const i = state.inventory.find((x) => x.id === id);
  if (!i) return;
  openModal("Edit Inventory Item", `
    <form class="form-grid">
      <label class="full">Name<input name="name" value="${esc(i.name)}" required></label>
      <label>Stock Count<input name="count" type="number" value="${i.count}" min="0"></label>
      <label>Location<input name="location" value="${esc(i.location)}"></label>
      <label>Unit Price (€)<input name="price" type="number" value="${i.price}" min="0" step="0.01"></label>
      <div class="form-actions"><button type="submit" class="btn-primary">Save Changes</button></div>
    </form>
  `, async (fd) => {
    const updated = await api(`/api/inventory/${id}`, {
      method: "PUT",
      body: JSON.stringify({
        name: fd.get("name"), count: Number(fd.get("count")),
        location: fd.get("location"), price: String(fd.get("price"))
      })
    });
    const idx = state.inventory.findIndex((x) => x.id === id);
    if (idx >= 0) state.inventory[idx] = updated;
    renderInventory();
    setStatus("Item updated.", "ok");
  });
}
window.editItem = editItem;

async function deleteItem(id) {
  if (!confirm("Delete this inventory item?")) return;
  await api(`/api/inventory/${id}`, { method: "DELETE" });
  state.inventory = state.inventory.filter((i) => i.id !== id);
  renderInventory();
  setStatus("Item deleted.", "ok");
}
window.deleteItem = deleteItem;

$("#useReserveForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const action = e.submitter.value;
  const fd = new FormData(e.currentTarget);
  const payload = {
    repairJobId: fd.get("repairJobId"),
    itemId: fd.get("itemId"),
    amount: Number(fd.get("amount"))
  };
  const endpoint = action === "reserve" ? "/api/inventory/reserve" : "/api/inventory/use";
  await api(endpoint, { method: "POST", body: JSON.stringify(payload) });
  state.inventory = await api("/api/inventory");
  state.orders = await api("/api/repair-orders");
  renderInventory();
  setStatus(action === "reserve" ? "Part reserved." : "Part used and recorded.", "ok");
});

// ══════════════════════════════════════════════════════════════════════════
// SERVICES / OPERATIONS
// ══════════════════════════════════════════════════════════════════════════
function renderServices() {
  const tbody = $("#servicesTbody");
  if (!state.operations.length) {
    tbody.innerHTML = '<tr><td colspan="5" class="empty-state">No services found.</td></tr>';
  } else {
    tbody.innerHTML = state.operations.map((op) => `
      <tr>
        <td><strong>${esc(op.name)}</strong></td>
        <td style="color:var(--muted);font-size:12px">${esc(op.description)}</td>
        <td><span class="badge badge-green">${money(op.currentPrice)}</span></td>
        <td style="color:var(--muted);font-size:12px">${esc(op.limitations)}</td>
        <td>
          <div class="table-actions">
            <button class="btn-sm btn-secondary" onclick="editService('${op.id}')">Edit Price</button>
          </div>
        </td>
      </tr>
    `).join("");
  }

  // Price change log
  api("/api/operations/price-changes").then((logs) => {
    const el = $("#priceChangesList");
    if (!logs.length) {
      el.innerHTML = '<div class="empty-state">No price changes recorded.</div>';
      return;
    }
    el.innerHTML = logs.slice().reverse().map((l) => `
      <div class="list-item">
        <div class="list-item-head">
          <span class="list-item-title">Operation price updated</span>
          <span class="badge badge-yellow">${money(l.oldPrice)} → ${money(l.newPrice)}</span>
        </div>
        <div class="list-item-meta">${esc(l.reason)} · ${fmtDate(l.changedAt)}</div>
      </div>
    `).join("");
  }).catch(() => {});
}

$("#newServiceBtn").addEventListener("click", () => {
  const mechOptions = state.mechanics.map((m) =>
    `<option value="${m.id}">${esc(m.name)}</option>`
  ).join("");
  openModal("New Service / Operation", `
    <form class="form-grid">
      <label class="full">Service Name<input name="name" required placeholder="Oil change"></label>
      <label>Price (€)<input name="price" type="number" value="0.00" min="0" step="0.01" required></label>
      <label>Limitations<input name="limitationsDesc" placeholder="No limitations"></label>
      <label class="full">Description<textarea name="description" rows="2" placeholder="Describe the service…"></textarea></label>
      <div class="form-actions"><button type="submit" class="btn-primary">Create Service</button></div>
    </form>
  `, async (fd) => {
    const op = await api("/api/operations", {
      method: "POST",
      body: JSON.stringify({
        name: fd.get("name"), description: fd.get("description"),
        price: String(fd.get("price")), limitationsDesc: fd.get("limitationsDesc"),
        eligibleWorkers: []
      })
    });
    state.operations.push(op);
    renderServices();
    setStatus("Service created.", "ok");
  });
});

function editService(id) {
  const op = state.operations.find((x) => x.id === id);
  if (!op) return;
  openModal("Update Service Price", `
    <form class="form-grid">
      <label class="full" style="color:var(--ink);font-size:14px;font-weight:700">${esc(op.name)}</label>
      <label>Current Price<input disabled value="${money(op.currentPrice)}"></label>
      <label>New Price (€)<input name="newPrice" type="number" value="${op.currentPrice}" min="0" step="0.01" required></label>
      <label>New Description<input name="description" value="${esc(op.description)}"></label>
      <label>Limitations<input name="limitationsDesc" value="${esc(op.limitations)}"></label>
      <label class="full">Reason for change<textarea name="reason" rows="2" placeholder="Explain the price change…" required></textarea></label>
      <div class="form-actions"><button type="submit" class="btn-primary">Update Price</button></div>
    </form>
  `, async (fd) => {
    const updated = await api(`/api/operations/${id}`, {
      method: "PUT",
      body: JSON.stringify({
        newPrice: String(fd.get("newPrice")), description: fd.get("description"),
        limitationsDesc: fd.get("limitationsDesc"), reason: fd.get("reason"),
        changedBy: "00000000-0000-0000-0000-000000000001"
      })
    });
    const idx = state.operations.findIndex((x) => x.id === id);
    if (idx >= 0) state.operations[idx] = updated;
    renderServices();
    setStatus("Service price updated.", "ok");
  });
}
window.editService = editService;

// ══════════════════════════════════════════════════════════════════════════
// NOTIFICATIONS
// ══════════════════════════════════════════════════════════════════════════

async function renderNotifications() {
  const el = $("#notificationsList");
  try {
    const notifications = await api("/api/notifications");
    state.notifications = notifications;
  } catch (e) {
    console.error("Failed to fetch notifications", e);
  }

  if (!state.notifications.length) {
    el.innerHTML = '<div class="empty-state">No notifications.</div>';
    return;
  }
  
  el.innerHTML = state.notifications.slice().reverse().map((n) => `
    <div class="list-item">
      <div class="list-item-head">
        <span class="list-item-title">${esc(n.type || "Notification")}</span>
        <span class="badge badge-blue">${esc(n.type)}</span>
      </div>
      <div class="list-item-meta">${esc(n.message)}</div>
    </div>
  `).join("");
}

// ── Bootstrap ──────────────────────────────────────────────────────────────
if (checkAuth()) {
  loadAll();
}
