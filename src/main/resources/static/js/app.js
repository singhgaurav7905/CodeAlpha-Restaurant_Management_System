const API = "/api";

let fullMenu = [];
let activeCategory = "ALL";
let cart = []; // { menuItemId, name, price, quantity }
let tablesCache = [];

/* ---------------- helpers ---------------- */

function money(n) {
  return "$" + Number(n).toFixed(2);
}

async function api(path, options) {
  const res = await fetch(API + path, {
    headers: { "Content-Type": "application/json" },
    ...options,
  });
  const body = await res.json();
  if (!res.ok || body.success === false) {
    throw new Error(body.message || "Request failed");
  }
  return body.data;
}

/* ---------------- hero specials ---------------- */

function renderSpecials(items) {
  const board = document.getElementById("specials-list");
  const picks = items.filter((i) => i.available).slice(0, 5);
  if (picks.length === 0) {
    board.innerHTML = '<li><span class="desc">No specials posted yet.</span></li>';
    return;
  }
  board.innerHTML = picks
    .map(
      (i) => `
    <li>
      <div>
        <span class="name">${escapeHtml(i.name)}</span>
        <span class="desc">${escapeHtml(i.category.replace("_", " "))}</span>
      </div>
      <span class="price">${money(i.price)}</span>
    </li>`
    )
    .join("");
}

function escapeHtml(str) {
  const div = document.createElement("div");
  div.textContent = str ?? "";
  return div.innerHTML;
}

/* ---------------- menu ---------------- */

async function loadMenu() {
  try {
    fullMenu = await api("/menu");
    document.getElementById("stat-items").textContent = fullMenu.filter((m) => m.available).length;
    renderSpecials(fullMenu);
    renderTabs();
    renderMenu();
  } catch (e) {
    document.getElementById("menu-grid").innerHTML = `<div class="loading">Couldn't load the menu: ${escapeHtml(e.message)}</div>`;
  }
}

function renderTabs() {
  const categories = ["ALL", ...new Set(fullMenu.map((m) => m.category))];
  const tabsEl = document.getElementById("menu-tabs");
  tabsEl.innerHTML = categories
    .map(
      (c) =>
        `<button class="tab ${c === activeCategory ? "active" : ""}" data-cat="${c}">${c === "ALL" ? "All dishes" : c.replace("_", " ").toLowerCase()}</button>`
    )
    .join("");
  tabsEl.querySelectorAll(".tab").forEach((btn) => {
    btn.addEventListener("click", () => {
      activeCategory = btn.dataset.cat;
      renderTabs();
      renderMenu();
    });
  });
}

function renderMenu() {
  const list = activeCategory === "ALL" ? fullMenu : fullMenu.filter((m) => m.category === activeCategory);
  const grid = document.getElementById("menu-grid");
  if (list.length === 0) {
    grid.innerHTML = '<div class="loading">Nothing in this category right now.</div>';
    return;
  }
  grid.innerHTML = list
    .map(
      (item) => `
    <div class="menu-card">
      <div class="menu-card-top">
        <h4>${escapeHtml(item.name)}</h4>
        <span class="menu-card-price">${money(item.price)}</span>
      </div>
      <p class="menu-card-desc">${escapeHtml(item.description || "")}</p>
      <div class="menu-card-foot">
        <span class="tag ${item.available ? "" : "unavailable"}">${item.available ? (item.vegetarian ? "Vegetarian" : "Available") : "86'd tonight"}</span>
        <button class="btn btn-small ${item.available ? "btn-primary" : "btn-ghost"}" ${item.available ? "" : "disabled"} onclick="addToCart(${item.id})">Add</button>
      </div>
    </div>`
    )
    .join("");
}

/* ---------------- cart ---------------- */

function addToCart(menuItemId) {
  const item = fullMenu.find((m) => m.id === menuItemId);
  if (!item) return;
  const line = cart.find((l) => l.menuItemId === menuItemId);
  if (line) {
    line.quantity += 1;
  } else {
    cart.push({ menuItemId, name: item.name, price: item.price, quantity: 1 });
  }
  renderCart();
  toggleCart(true);
}

function removeFromCart(menuItemId) {
  cart = cart.filter((l) => l.menuItemId !== menuItemId);
  renderCart();
}

function renderCart() {
  const body = document.getElementById("cart-body");
  const count = cart.reduce((sum, l) => sum + l.quantity, 0);
  document.getElementById("cart-count").textContent = count;

  if (cart.length === 0) {
    body.innerHTML = '<div class="cart-empty">Nothing yet &mdash; add a dish from the menu.</div>';
    document.getElementById("cart-total").textContent = money(0);
    return;
  }

  body.innerHTML = cart
    .map(
      (l) => `
    <div class="cart-line">
      <div>
        <div class="cart-line-name">${escapeHtml(l.name)}</div>
        <div class="cart-line-qty">x${l.quantity}</div>
        <button onclick="removeFromCart(${l.menuItemId})">Remove</button>
      </div>
      <div class="cart-line-price">${money(l.price * l.quantity)}</div>
    </div>`
    )
    .join("");

  const total = cart.reduce((sum, l) => sum + l.price * l.quantity, 0);
  document.getElementById("cart-total").textContent = money(total);
}

function toggleCart(open) {
  document.getElementById("cart-drawer").classList.toggle("open", open);
  document.getElementById("overlay").classList.toggle("open", open);
}

document.getElementById("o-type").addEventListener("change", (e) => {
  document.getElementById("o-table-field").style.display = e.target.value === "DINE_IN" ? "block" : "none";
});

async function placeOrder() {
  const msg = document.getElementById("order-msg");
  msg.className = "form-msg";
  if (cart.length === 0) {
    msg.textContent = "Add at least one dish first.";
    msg.classList.add("show", "err");
    return;
  }
  const orderType = document.getElementById("o-type").value;
  const tableId = document.getElementById("o-table").value;
  if (orderType === "DINE_IN" && !tableId) {
    msg.textContent = "Pick a table for a dine-in order.";
    msg.classList.add("show", "err");
    return;
  }

  const payload = {
    orderType,
    tableId: orderType === "DINE_IN" && tableId ? Number(tableId) : null,
    customerName: document.getElementById("o-name").value || "Guest",
    customerPhone: document.getElementById("o-phone").value || null,
    items: cart.map((l) => ({ menuItemId: l.menuItemId, quantity: l.quantity })),
  };

  try {
    const order = await api("/orders", { method: "POST", body: JSON.stringify(payload) });
    msg.textContent = `Order #${order.id} placed — total ${money(order.totalAmount)}. The kitchen has it.`;
    msg.classList.add("show", "ok");
    cart = [];
    renderCart();
    loadTables();
  } catch (e) {
    msg.textContent = e.message;
    msg.classList.add("show", "err");
  }
}

/* ---------------- floor plan / tables ---------------- */

async function loadTables() {
  try {
    tablesCache = await api("/tables");
    document.getElementById("stat-tables").textContent = tablesCache.filter((t) => t.status === "AVAILABLE").length;
    renderFloorPlan();
    renderTableSelect();
  } catch (e) {
    document.getElementById("floor-plan").innerHTML = `<div class="loading">Couldn't load tables: ${escapeHtml(e.message)}</div>`;
  }
}

function renderFloorPlan() {
  const el = document.getElementById("floor-plan");
  el.innerHTML = tablesCache
    .map(
      (t) => `
    <div class="table-tile">
      <div class="t-num">${escapeHtml(t.tableNumber)}</div>
      <div class="t-cap">${t.capacity} seats · ${escapeHtml(t.location || "")}</div>
      <div class="t-status status-${t.status}"><i class="dot status-${t.status}"></i>${t.status.toLowerCase()}</div>
    </div>`
    )
    .join("");
}

function renderTableSelect() {
  const select = document.getElementById("o-table");
  const available = tablesCache.filter((t) => t.status === "AVAILABLE");
  if (available.length === 0) {
    select.innerHTML = '<option value="">No tables free right now</option>';
    return;
  }
  select.innerHTML = available
    .map((t) => `<option value="${t.id}">${escapeHtml(t.tableNumber)} — ${t.capacity} seats (${escapeHtml(t.location || "")})</option>`)
    .join("");
}

/* ---------------- reservations ---------------- */

document.getElementById("reservation-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  const msg = document.getElementById("reservation-msg");
  msg.className = "form-msg";

  const payload = {
    guestName: document.getElementById("r-name").value,
    guestPhone: document.getElementById("r-phone").value,
    partySize: Number(document.getElementById("r-party").value),
    reservationTime: document.getElementById("r-time").value,
    specialRequests: document.getElementById("r-requests").value || null,
  };

  try {
    const reservation = await api("/reservations", { method: "POST", body: JSON.stringify(payload) });
    msg.textContent = `Table ${reservation.table.tableNumber} is booked for you at ${new Date(reservation.reservationTime).toLocaleString()}.`;
    msg.classList.add("show", "ok");
    e.target.reset();
    document.getElementById("r-party").value = 2;
    loadTables();
  } catch (err) {
    msg.textContent = err.message;
    msg.classList.add("show", "err");
  }
});

/* ---------------- init ---------------- */

loadMenu();
loadTables();
setInterval(loadTables, 20000);
