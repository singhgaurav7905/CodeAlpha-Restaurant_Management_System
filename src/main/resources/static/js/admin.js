const API = "/api";

function money(n) { return "$" + Number(n).toFixed(2); }
function escapeHtml(str) { const d = document.createElement("div"); d.textContent = str ?? ""; return d.innerHTML; }

async function api(path, options) {
  const res = await fetch(API + path, { headers: { "Content-Type": "application/json" }, ...options });
  const body = await res.json();
  if (!res.ok || body.success === false) throw new Error(body.message || "Request failed");
  return body.data;
}

/* ---------------- nav / view switching ---------------- */

const viewLoaders = {
  overview: loadOverview,
  orders: loadOrders,
  tables: loadTablesAdmin,
  reservations: loadReservations,
  inventory: loadInventory,
  menu: loadMenuAdmin,
};

document.querySelectorAll(".admin-nav-item").forEach((btn) => {
  btn.addEventListener("click", () => {
    document.querySelectorAll(".admin-nav-item").forEach((b) => b.classList.remove("active"));
    document.querySelectorAll(".admin-view").forEach((v) => v.classList.remove("active"));
    btn.classList.add("active");
    const view = btn.dataset.view;
    document.getElementById("view-" + view).classList.add("active");
    viewLoaders[view]();
  });
});

/* ---------------- overview ---------------- */

async function loadOverview() {
  const today = new Date().toISOString().slice(0, 10);
  document.getElementById("overview-date").textContent = new Date().toLocaleDateString(undefined, { weekday: "long", month: "long", day: "numeric" });
  try {
    const report = await api(`/reports/daily-sales?date=${today}`);
    document.getElementById("s-orders").textContent = report.totalOrders;
    document.getElementById("s-completed").textContent = report.completedOrders;
    document.getElementById("s-cancelled").textContent = report.cancelledOrders;
    document.getElementById("s-revenue").textContent = money(report.totalRevenue);
    document.getElementById("s-avg").textContent = money(report.averageOrderValue);

    const topEl = document.getElementById("top-sellers");
    const entries = Object.entries(report.topSellingItems || {});
    topEl.innerHTML = entries.length
      ? entries.map(([name, qty]) => `<div class="mini-row"><span>${escapeHtml(name)}</span><span class="qty">x${qty}</span></div>`).join("")
      : '<div class="mini-empty">No orders yet today.</div>';

    const alertsEl = document.getElementById("stock-alerts");
    alertsEl.innerHTML = report.lowStockAlerts && report.lowStockAlerts.length
      ? report.lowStockAlerts.map((a) => `<div class="alert-row">${escapeHtml(a)}</div>`).join("")
      : '<div class="mini-empty">Everything is well stocked.</div>';
  } catch (e) {
    document.getElementById("top-sellers").innerHTML = `<div class="mini-empty">${escapeHtml(e.message)}</div>`;
  }
}

/* ---------------- orders ---------------- */

const ORDER_STATUSES = ["PLACED", "CONFIRMED", "PREPARING", "READY", "SERVED", "COMPLETED", "CANCELLED"];

async function loadOrders() {
  const tbody = document.querySelector("#orders-table tbody");
  tbody.innerHTML = '<tr><td colspan="8" class="loading">Loading orders&hellip;</td></tr>';
  try {
    const filter = document.getElementById("order-filter").value;
    const orders = await api(filter ? `/orders?status=${filter}` : "/orders");
    if (orders.length === 0) {
      tbody.innerHTML = '<tr><td colspan="8" class="loading">No orders yet.</td></tr>';
      return;
    }
    tbody.innerHTML = orders
      .slice()
      .reverse()
      .map((o) => {
        const itemSummary = o.items.map((i) => `${i.quantity}x ${i.menuItem.name}`).join(", ");
        const isTerminal = o.status === "COMPLETED" || o.status === "CANCELLED";
        return `
        <tr>
          <td>#${o.id}</td>
          <td>${o.orderType.replace("_", " ")}</td>
          <td>${o.table ? escapeHtml(o.table.tableNumber) : "&mdash;"}</td>
          <td title="${escapeHtml(itemSummary)}">${escapeHtml(itemSummary.length > 40 ? itemSummary.slice(0, 40) + "…" : itemSummary)}</td>
          <td>${money(o.totalAmount)}</td>
          <td><span class="status-pill ${o.status}">${o.status}</span></td>
          <td>${new Date(o.createdAt).toLocaleTimeString()}</td>
          <td>
            ${isTerminal ? "" : `
            <select class="row-select" onchange="changeOrderStatus(${o.id}, this.value)">
              <option value="">Set status&hellip;</option>
              ${ORDER_STATUSES.filter((s) => s !== o.status).map((s) => `<option value="${s}">${s}</option>`).join("")}
            </select>`}
          </td>
        </tr>`;
      })
      .join("");
  } catch (e) {
    tbody.innerHTML = `<tr><td colspan="8" class="loading">${escapeHtml(e.message)}</td></tr>`;
  }
}

async function changeOrderStatus(id, status) {
  if (!status) return;
  try {
    await api(`/orders/${id}/status?status=${status}`, { method: "PATCH" });
    loadOrders();
  } catch (e) {
    alert(e.message);
  }
}

document.getElementById("order-filter").addEventListener("change", loadOrders);

/* ---------------- tables ---------------- */

const TABLE_STATUSES = ["AVAILABLE", "OCCUPIED", "RESERVED", "CLEANING"];

async function loadTablesAdmin() {
  const tbody = document.querySelector("#tables-table tbody");
  tbody.innerHTML = '<tr><td colspan="5" class="loading">Loading tables&hellip;</td></tr>';
  try {
    const tables = await api("/tables");
    tbody.innerHTML = tables
      .map(
        (t) => `
      <tr>
        <td>${escapeHtml(t.tableNumber)}</td>
        <td>${escapeHtml(t.location || "&mdash;")}</td>
        <td>${t.capacity}</td>
        <td><span class="status-pill ${t.status}">${t.status}</span></td>
        <td>
          <select class="row-select" onchange="changeTableStatus(${t.id}, this.value)">
            <option value="">Set status&hellip;</option>
            ${TABLE_STATUSES.filter((s) => s !== t.status).map((s) => `<option value="${s}">${s}</option>`).join("")}
          </select>
        </td>
      </tr>`
      )
      .join("");
  } catch (e) {
    tbody.innerHTML = `<tr><td colspan="5" class="loading">${escapeHtml(e.message)}</td></tr>`;
  }
}

async function changeTableStatus(id, status) {
  if (!status) return;
  try {
    await api(`/tables/${id}/status?status=${status}`, { method: "PATCH" });
    loadTablesAdmin();
  } catch (e) {
    alert(e.message);
  }
}

/* ---------------- reservations ---------------- */

async function loadReservations() {
  const tbody = document.querySelector("#reservations-table tbody");
  tbody.innerHTML = '<tr><td colspan="6" class="loading">Loading reservations&hellip;</td></tr>';
  try {
    const reservations = await api("/reservations");
    if (reservations.length === 0) {
      tbody.innerHTML = '<tr><td colspan="6" class="loading">No reservations yet.</td></tr>';
      return;
    }
    tbody.innerHTML = reservations
      .slice()
      .reverse()
      .map((r) => {
        const isTerminal = ["COMPLETED", "CANCELLED", "NO_SHOW"].includes(r.status);
        return `
        <tr>
          <td>${escapeHtml(r.guestName)}<br><span style="color:rgba(27,33,29,0.45);font-size:12px">${escapeHtml(r.guestPhone)}</span></td>
          <td>${r.partySize}</td>
          <td>${escapeHtml(r.table.tableNumber)}</td>
          <td>${new Date(r.reservationTime).toLocaleString()}</td>
          <td><span class="status-pill ${r.status}">${r.status}</span></td>
          <td>
            ${isTerminal ? "" : `
            <div class="row-actions">
              ${r.status !== "SEATED" ? `<button class="btn btn-small btn-ghost" onclick="seatReservation(${r.id})">Seat</button>` : `<button class="btn btn-small btn-ghost" onclick="completeReservation(${r.id})">Complete</button>`}
              <button class="btn btn-small btn-ghost" onclick="cancelReservation(${r.id})">Cancel</button>
            </div>`}
          </td>
        </tr>`;
      })
      .join("");
  } catch (e) {
    tbody.innerHTML = `<tr><td colspan="6" class="loading">${escapeHtml(e.message)}</td></tr>`;
  }
}

async function seatReservation(id) {
  try { await api(`/reservations/${id}/seat`, { method: "POST" }); loadReservations(); } catch (e) { alert(e.message); }
}
async function completeReservation(id) {
  try { await api(`/reservations/${id}/complete`, { method: "POST" }); loadReservations(); } catch (e) { alert(e.message); }
}
async function cancelReservation(id) {
  try { await api(`/reservations/${id}/cancel`, { method: "POST" }); loadReservations(); } catch (e) { alert(e.message); }
}

/* ---------------- inventory ---------------- */

async function loadInventory() {
  const tbody = document.querySelector("#inventory-table tbody");
  tbody.innerHTML = '<tr><td colspan="5" class="loading">Loading inventory&hellip;</td></tr>';
  try {
    const lowOnly = document.getElementById("inv-low-only").checked;
    const items = await api(`/inventory${lowOnly ? "?lowStockOnly=true" : ""}`);
    if (items.length === 0) {
      tbody.innerHTML = '<tr><td colspan="5" class="loading">Nothing to show.</td></tr>';
      return;
    }
    tbody.innerHTML = items
      .map(
        (i) => `
      <tr>
        <td>${escapeHtml(i.name)}</td>
        <td>${i.quantityInStock} ${escapeHtml(i.unit)}</td>
        <td>${i.reorderThreshold} ${escapeHtml(i.unit)}</td>
        <td><span class="status-pill ${i.quantityInStock <= i.reorderThreshold ? "CANCELLED" : "AVAILABLE"}">${i.quantityInStock <= i.reorderThreshold ? "Low stock" : "OK"}</span></td>
        <td>
          <div class="row-actions">
            <input type="number" min="0" step="0.5" class="mini-input" id="restock-${i.id}" placeholder="qty">
            <button class="btn btn-small btn-ghost" onclick="restock(${i.id})">Add</button>
          </div>
        </td>
      </tr>`
      )
      .join("");
  } catch (e) {
    tbody.innerHTML = `<tr><td colspan="5" class="loading">${escapeHtml(e.message)}</td></tr>`;
  }
}

async function restock(id) {
  const input = document.getElementById(`restock-${id}`);
  const qty = Number(input.value);
  if (!qty || qty <= 0) { alert("Enter a quantity greater than zero."); return; }
  try {
    await api(`/inventory/${id}/restock`, { method: "PATCH", body: JSON.stringify({ quantity: qty }) });
    loadInventory();
  } catch (e) {
    alert(e.message);
  }
}

/* ---------------- menu ---------------- */

async function loadMenuAdmin() {
  const tbody = document.querySelector("#menu-table tbody");
  tbody.innerHTML = '<tr><td colspan="5" class="loading">Loading menu&hellip;</td></tr>';
  try {
    const items = await api("/menu");
    tbody.innerHTML = items
      .map(
        (m) => `
      <tr>
        <td>${escapeHtml(m.name)}</td>
        <td>${escapeHtml(m.category.replace("_", " "))}</td>
        <td>${money(m.price)}</td>
        <td><span class="status-pill ${m.available ? "AVAILABLE" : "CANCELLED"}">${m.available ? "Available" : "86'd"}</span></td>
        <td><button class="btn btn-small btn-ghost" onclick="toggleAvailability(${m.id}, ${!m.available})">${m.available ? "Mark 86'd" : "Bring back"}</button></td>
      </tr>`
      )
      .join("");
  } catch (e) {
    tbody.innerHTML = `<tr><td colspan="5" class="loading">${escapeHtml(e.message)}</td></tr>`;
  }
}

async function toggleAvailability(id, available) {
  try {
    await api(`/menu/${id}/availability?available=${available}`, { method: "PATCH" });
    loadMenuAdmin();
  } catch (e) {
    alert(e.message);
  }
}

/* ---------------- change password ---------------- */

document.getElementById("change-password-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  const msg = document.getElementById("cp-msg");
  msg.className = "form-msg";
  const currentPassword = document.getElementById("cp-current").value;
  const newPassword = document.getElementById("cp-new").value;
  try {
    await api("/staff/change-password", { method: "PATCH", body: JSON.stringify({ currentPassword, newPassword }) });
    msg.textContent = "Password updated. Use the new one next time you log in.";
    msg.classList.add("show", "ok");
    e.target.reset();
  } catch (err) {
    msg.textContent = err.message;
    msg.classList.add("show", "err");
  }
});

/* ---------------- init ---------------- */

loadOverview();
