const state = {
  apiBase: localStorage.getItem("autoopsApiBase") || "http://localhost:8080",
  mechanics: [],
  orders: [],
  inventory: [],
  operations: []
};

const $ = (selector) => document.querySelector(selector);
const statusBox = $("#status");
const apiBaseInput = $("#apiBase");
apiBaseInput.value = state.apiBase;

function setStatus(message, type = "") {
  statusBox.className = `status ${type}`.trim();
  statusBox.textContent = message;
}

async function api(path, options = {}) {
  const response = await fetch(`${state.apiBase}${path}`, {
    headers: { "Content-Type": "application/json", ...(options.headers || {}) },
    ...options
  });
  const text = await response.text();
  const data = text ? JSON.parse(text) : null;
  if (!response.ok) {
    throw new Error(data?.message || response.statusText);
  }
  return data;
}

function formatDateInput(date) {
  const pad = (value) => String(value).padStart(2, "0");
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

function money(value) {
  if (value === undefined || value === null) return "-";
  return `${Number(value).toFixed(2)} EUR`;
}

function htmlEscape(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;");
}

function parseVehicleName(value) {
  const parts = String(value || "").split(" ");
  return {
    brand: parts[0] || "Unknown",
    model: parts.slice(1).join(" ") || "Model"
  };
}

function renderSelect(select, rows, getLabel) {
  select.innerHTML = rows.map((row) => `<option value="${row.id}">${htmlEscape(getLabel(row))}</option>`).join("");
}

function renderOrders() {
  const list = $("#ordersList");
  if (!state.orders.length) {
    list.innerHTML = '<div class="item">No repair orders.</div>';
  } else {
    list.innerHTML = state.orders.map((order) => `
      <article class="item">
        <div class="item-header">
          <p class="item-title">${htmlEscape(order.problemDescription)}</p>
          <span class="pill ${order.status === "READY_FOR_PAYMENT" ? "ok" : ""}">${order.status}</span>
        </div>
        <div class="meta">
          <span>${order.id}</span>
          <span>Tasks ${order.tasks.filter((task) => task.status === "DONE").length}/${order.tasks.length}</span>
          <span>Parts ${order.usedParts.length}</span>
          <span>Limit ${money(order.maxPrice)}</span>
        </div>
      </article>
    `).join("");
  }
  renderSelect($("#inventoryOrderSelect"), state.orders, (order) => `${order.problemDescription} (${order.status})`);
  renderSelect($("#completionOrderSelect"), state.orders, (order) => `${order.problemDescription} (${order.status})`);
}

function renderInventory() {
  const list = $("#inventoryList");
  list.innerHTML = state.inventory.map((item) => `
    <article class="item">
      <div class="item-header">
        <p class="item-title">${htmlEscape(item.name)}</p>
        <span class="pill ${item.count <= 2 ? "warn" : "ok"}">${item.status}</span>
      </div>
      <div class="meta">
        <span>${htmlEscape(item.code)}</span>
        <span>Stock ${item.count}</span>
        <span>Reserved ${item.reservedCount}</span>
        <span>${money(item.price)}</span>
        <span>${htmlEscape(item.location)}</span>
      </div>
    </article>
  `).join("");
  renderSelect($("#inventorySelect"), state.inventory, (item) => `${item.code} - ${item.name} (${item.count})`);
}

function renderOperations() {
  const list = $("#operationList");
  list.innerHTML = state.operations.map((operation) => `
    <article class="item">
      <div class="item-header">
        <p class="item-title">${htmlEscape(operation.name)}</p>
        <span class="pill ok">${money(operation.currentPrice)}</span>
      </div>
      <p>${htmlEscape(operation.description)}</p>
      <div class="meta">
        <span>${operation.id}</span>
        <span>${htmlEscape(operation.limitations)}</span>
      </div>
    </article>
  `).join("");
  renderSelect($("#operationSelect"), state.operations, (operation) => `${operation.name} - ${money(operation.currentPrice)}`);
}

async function loadAll() {
  try {
    const start = new Date(Date.now() + 2 * 60 * 60 * 1000);
    const end = new Date(Date.now() + 5 * 60 * 60 * 1000);
    $("#plannedStart").value ||= formatDateInput(start);
    $("#plannedEnd").value ||= formatDateInput(end);

    const [mechanics, orders, inventory, operations] = await Promise.all([
      api(`/api/planning/mechanics/available?start=${encodeURIComponent(start.toISOString().slice(0, 19))}&end=${encodeURIComponent(end.toISOString().slice(0, 19))}`),
      api("/api/repair-orders"),
      api("/api/inventory"),
      api("/api/operations")
    ]);
    state.mechanics = mechanics;
    state.orders = orders;
    state.inventory = inventory;
    state.operations = operations;
    renderSelect($("#mechanicSelect"), mechanics, (mechanic) => `${mechanic.name} - ${mechanic.specialties.join(", ")}`);
    renderOrders();
    renderInventory();
    renderOperations();
    setStatus("Connected to backend.", "ok");
  } catch (error) {
    setStatus(`Backend unavailable: ${error.message}`, "error");
  }
}

document.querySelectorAll(".tab").forEach((tab) => {
  tab.addEventListener("click", () => {
    document.querySelectorAll(".tab").forEach((item) => item.classList.remove("is-active"));
    document.querySelectorAll(".view").forEach((item) => item.classList.remove("is-active"));
    tab.classList.add("is-active");
    $(`#${tab.dataset.tab}`).classList.add("is-active");
  });
});

$("#saveApi").addEventListener("click", async () => {
  state.apiBase = apiBaseInput.value.replace(/\/$/, "");
  localStorage.setItem("autoopsApiBase", state.apiBase);
  await loadAll();
});

$("#refreshOrders").addEventListener("click", async () => {
  state.orders = await api("/api/repair-orders");
  renderOrders();
  setStatus("Repair orders refreshed.", "ok");
});

$("#inventorySearch").addEventListener("input", async (event) => {
  state.inventory = await api(`/api/inventory?query=${encodeURIComponent(event.target.value)}`);
  renderInventory();
});

$("#scheduleForm").addEventListener("submit", async (event) => {
  event.preventDefault();
  const form = new FormData(event.currentTarget);
  const vehicleName = parseVehicleName(form.get("vehicleName"));
  const payload = {
    customerName: form.get("customerName"),
    customerEmail: form.get("customerEmail"),
    customerPhone: form.get("customerPhone"),
    vin: form.get("vin"),
    licensePlate: form.get("licensePlate"),
    brand: vehicleName.brand,
    model: vehicleName.model,
    year: Number(form.get("year")),
    mileage: Number(form.get("mileage")),
    problemDescription: form.get("problemDescription"),
    maxPrice: String(form.get("maxPrice")),
    mechanicId: form.get("mechanicId"),
    plannedStart: form.get("plannedStart"),
    plannedCompletionDate: form.get("plannedCompletionDate"),
    taskNames: String(form.get("taskNames")).split("\n").map((item) => item.trim()).filter(Boolean)
  };
  try {
    const result = await api("/api/repair-orders", { method: "POST", body: JSON.stringify(payload) });
    state.orders = await api("/api/repair-orders");
    renderOrders();
    setStatus(`Repair order ${result.repairOrder.id} created.`, "ok");
  } catch (error) {
    setStatus(error.message, "error");
  }
});

$("#inventoryForm").addEventListener("submit", async (event) => {
  event.preventDefault();
  const submitter = event.submitter;
  const form = new FormData(event.currentTarget);
  const payload = {
    repairJobId: form.get("repairJobId"),
    itemId: form.get("itemId"),
    amount: Number(form.get("amount"))
  };
  try {
    const endpoint = submitter.value === "reserve" ? "/api/inventory/reserve" : "/api/inventory/use";
    await api(endpoint, { method: "POST", body: JSON.stringify(payload) });
    state.inventory = await api("/api/inventory");
    state.orders = await api("/api/repair-orders");
    renderInventory();
    renderOrders();
    setStatus(submitter.value === "reserve" ? "Part reserved." : "Part used and attached to repair order.", "ok");
  } catch (error) {
    setStatus(error.message, "error");
  }
});

$("#operationForm").addEventListener("submit", async (event) => {
  event.preventDefault();
  const form = new FormData(event.currentTarget);
  const operationId = form.get("operationId");
  const payload = {
    newPrice: String(form.get("newPrice")),
    description: form.get("description"),
    reason: form.get("reason"),
    changedBy: "00000000-0000-0000-0000-000000000001"
  };
  try {
    await api(`/api/operations/${operationId}`, { method: "PUT", body: JSON.stringify(payload) });
    state.operations = await api("/api/operations");
    renderOperations();
    setStatus("Operation price updated and notification created.", "ok");
  } catch (error) {
    setStatus(error.message, "error");
  }
});

$("#completionForm").addEventListener("submit", async (event) => {
  event.preventDefault();
  const form = new FormData(event.currentTarget);
  const orderId = form.get("repairOrderId");
  const payload = {
    actualWorkHours: Number(form.get("actualWorkHours")),
    note: form.get("note"),
    markAllTasksDone: form.get("markAllTasksDone") === "on"
  };
  try {
    const result = await api(`/api/repairs/${orderId}/complete`, { method: "POST", body: JSON.stringify(payload) });
    $("#invoiceOutput").textContent = JSON.stringify(result.invoice, null, 2);
    state.orders = await api("/api/repair-orders");
    renderOrders();
    setStatus(`Invoice ${result.invoice.invoiceNumber} created with payment status ${result.paymentStatus}.`, "ok");
  } catch (error) {
    setStatus(error.message, "error");
  }
});

loadAll();
