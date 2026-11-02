const money = (n) => "€" + Number(n).toFixed(2);

// Takeaway cart: id -> { id, name, price, qty }
const cart = new Map();
const redraws = new Set();

const cartBar = document.getElementById("cart-bar");
const orderDialog = document.getElementById("order-dialog");

function cartCount() {
  let n = 0;
  cart.forEach((l) => (n += l.qty));
  return n;
}

function cartTotal() {
  let t = 0;
  cart.forEach((l) => (t += l.qty * Number(l.price)));
  return t;
}

function setQty(item, qty) {
  if (qty <= 0) cart.delete(item.id);
  else cart.set(item.id, { id: item.id, name: item.name, price: item.price, qty: Math.min(qty, 20) });
  redraws.forEach((fn) => fn());
  updateCartUi();
}

function updateCartUi() {
  document.getElementById("cart-count").textContent = cartCount();
  document.getElementById("cart-total").textContent = money(cartTotal());
  cartBar.hidden = cartCount() === 0 || orderDialog.open;
  renderOrderLines();
}

function makeControls(item) {
  const box = document.createElement("div");
  box.className = "qty";

  const draw = () => {
    box.textContent = "";
    const qty = cart.get(item.id)?.qty || 0;

    if (qty === 0) {
      const add = document.createElement("button");
      add.type = "button";
      add.className = "add-btn";
      add.setAttribute("aria-label", "Add " + item.name + " to order");
      add.addEventListener("click", () => setQty(item, 1));
      box.appendChild(add);
      return;
    }

    const minus = document.createElement("button");
    minus.type = "button";
    minus.textContent = "−";
    minus.setAttribute("aria-label", "Remove one " + item.name);
    minus.addEventListener("click", () => setQty(item, qty - 1));

    const count = document.createElement("span");
    count.textContent = qty;

    const plus = document.createElement("button");
    plus.type = "button";
    plus.textContent = "+";
    plus.setAttribute("aria-label", "Add one " + item.name);
    plus.addEventListener("click", () => setQty(item, qty + 1));

    box.append(minus, count, plus);
  };

  redraws.add(draw);
  draw();
  return box;
}

async function loadMenu() {
  const container = document.getElementById("menu-container");

  try {
    const response = await fetch("/api/menu");
    if (!response.ok) throw new Error("Server error " + response.status);
    const categories = await response.json();

    container.textContent = ""; // remove "Loading..."

    // Category quick links
    const tabs = document.createElement("div");
    tabs.className = "menu-tabs";
    container.appendChild(tabs);

    categories.forEach((category, index) => {
      const id = "cat-" + index;

      const tab = document.createElement("button");
      tab.type = "button";
      tab.textContent = category.name;
      tab.addEventListener("click", () =>
        document.getElementById(id).scrollIntoView({ behavior: "smooth" }));
      tabs.appendChild(tab);

      const title = document.createElement("h3");
      title.id = id;
      title.textContent = category.name;
      container.appendChild(title);

      for (const item of category.items) {
        const row = document.createElement("div");
        row.className = "item";

        const left = document.createElement("div");

        const name = document.createElement("div");
        name.className = "item-name";
        name.textContent = item.name;
        left.appendChild(name);

        if (item.description) {
          const desc = document.createElement("div");
          desc.className = "item-desc";
          desc.textContent = item.description;
          left.appendChild(desc);
        }

        if (item.note) {
          for (const line of item.note.split("|")) {
            const extra = document.createElement("div");
            extra.className = "item-extra";
            extra.textContent = line.trim();
            left.appendChild(extra);
          }
        }

        if (item.allergens) {
          const allergens = document.createElement("div");
          allergens.className = "allergens";
          allergens.textContent = "Allergens: " + item.allergens.split(",").join(", ");
          left.appendChild(allergens);
        }

        const side = document.createElement("div");
        side.className = "item-side";

        const price = document.createElement("div");
        price.className = "price";
        price.textContent = money(item.price);

        side.append(price, makeControls(item));
        row.append(left, side);
        container.appendChild(row);
      }
    });
  } catch (error) {
    container.textContent = "Sorry, the menu could not be loaded. Please try again later.";
    console.error(error);
  }
}

// Fade-in sections on scroll
const observer = new IntersectionObserver((entries) => {
  entries.forEach((entry) => {
    if (entry.isIntersecting) {
      entry.target.classList.add("visible");
      observer.unobserve(entry.target);
    }
  });
}, { threshold: 0.08 });
document.querySelectorAll(".reveal").forEach((el) => observer.observe(el));

// Opening hours (shared by booking and takeaway)
let openingHours = [];

function hoursFor(dateValue) {
  const day = new Date(dateValue + "T00:00:00").getDay();
  return openingHours.find((h) => h.jsDay === day);
}

function todayString() {
  return new Date().toISOString().split("T")[0];
}

// Limit a date/time pair of inputs to open days and that day's hours
function bindDateTime(form, timeLimitKey, closedText, setMessage) {
  const dateInput = form.elements.date;
  const timeInput = form.elements.time;
  dateInput.min = todayString();

  dateInput.addEventListener("change", () => {
    if (!dateInput.value || openingHours.length === 0) return;
    const hours = hoursFor(dateInput.value);
    if (!hours) {
      setMessage(closedText, "error");
      dateInput.value = "";
      timeInput.min = "";
      timeInput.max = "";
      return;
    }
    setMessage("");
    timeInput.min = hours.open;
    timeInput.max = hours[timeLimitKey];
    timeInput.step = 900; // 15 minutes
    if (timeInput.value && (timeInput.value < hours.open || timeInput.value > hours[timeLimitKey])) {
      timeInput.value = "";
    }
  });
}

function checkTimes(data, timeLimitKey, label, closedText, setMessage) {
  if (!data.date || openingHours.length === 0) return true;
  const hours = hoursFor(data.date);
  if (!hours) {
    setMessage(closedText, "error");
    return false;
  }
  if (data.time && (data.time < hours.open || data.time > hours[timeLimitKey])) {
    setMessage("On " + hours.day + " " + label + " " + hours.open + " to " + hours[timeLimitKey] + ".", "error");
    return false;
  }
  return true;
}

// Booking form
const form = document.getElementById("booking-form");
const message = document.getElementById("booking-message");
const hint = document.getElementById("booking-hint");

function showMessage(text, type) {
  message.className = type || "";
  message.textContent = text;
}

const BOOKING_CLOSED = "We take bookings on Friday, Saturday and Sunday only.";

bindDateTime(form, "lastBooking", BOOKING_CLOSED, showMessage);

form.addEventListener("submit", async (event) => {
  event.preventDefault();

  const data = Object.fromEntries(new FormData(form));
  data.guests = Number(data.guests);

  if (!checkTimes(data, "lastBooking", "we take bookings from", BOOKING_CLOSED, showMessage)) return;

  showMessage("Sending...");
  form.querySelector("button").disabled = true;

  try {
    const response = await fetch("/api/reservations", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data),
    });
    const result = await response.json();

    if (response.ok) {
      showMessage(result.message, "ok");
      form.reset();
      form.elements.guests.value = 2;
    } else {
      showMessage(Object.values(result).join(". "), "error");
    }
  } catch (error) {
    showMessage("Could not send the request. Please call us instead.", "error");
  } finally {
    form.querySelector("button").disabled = false;
  }
});

// Takeaway order
const orderForm = document.getElementById("order-form");
const orderMessage = document.getElementById("order-message");
const orderLinesBox = document.getElementById("order-lines");

function showOrderMessage(text, type) {
  orderMessage.className = type || "";
  orderMessage.textContent = text;
}

const ORDER_CLOSED = "We take takeaway orders on Friday, Saturday and Sunday only.";

bindDateTime(orderForm, "lastPickup", ORDER_CLOSED, showOrderMessage);

function renderOrderLines() {
  orderLinesBox.textContent = "";
  document.getElementById("order-total").textContent = money(cartTotal());

  if (cart.size === 0) {
    const empty = document.createElement("p");
    empty.className = "note";
    empty.textContent = "Your order is empty. Close this window and tap Add on the menu.";
    orderLinesBox.appendChild(empty);
    return;
  }

  cart.forEach((line) => {
    const row = document.createElement("div");
    row.className = "order-line";

    const info = document.createElement("div");
    const name = document.createElement("div");
    name.className = "order-line-name";
    name.textContent = line.name;
    const sum = document.createElement("div");
    sum.className = "order-line-sum";
    sum.textContent = line.qty + " × " + money(line.price) + " = " + money(line.qty * Number(line.price));
    info.append(name, sum);

    const controls = document.createElement("div");
    controls.className = "qty";
    const minus = document.createElement("button");
    minus.type = "button";
    minus.textContent = "−";
    minus.addEventListener("click", () => setQty(line, line.qty - 1));
    const count = document.createElement("span");
    count.textContent = line.qty;
    const plus = document.createElement("button");
    plus.type = "button";
    plus.textContent = "+";
    plus.addEventListener("click", () => setQty(line, line.qty + 1));
    controls.append(minus, count, plus);

    row.append(info, controls);
    orderLinesBox.appendChild(row);
  });
}

document.getElementById("cart-open").addEventListener("click", () => {
  showOrderMessage("");
  orderDialog.showModal();
  updateCartUi();
});

document.getElementById("order-close").addEventListener("click", () => orderDialog.close());
orderDialog.addEventListener("close", updateCartUi);

orderForm.addEventListener("submit", async (event) => {
  event.preventDefault();

  if (cart.size === 0) {
    showOrderMessage("Your order is empty.", "error");
    return;
  }

  const data = Object.fromEntries(new FormData(orderForm));
  data.items = [...cart.values()].map((l) => ({ menuItemId: l.id, quantity: l.qty }));

  if (!checkTimes(data, "lastPickup", "pickup is from", ORDER_CLOSED, showOrderMessage)) return;

  showOrderMessage("Sending...");
  const submit = orderForm.querySelector("button[type=submit]");
  submit.disabled = true;

  try {
    const response = await fetch("/api/takeaway", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data),
    });
    const result = await response.json();

    if (response.ok) {
      cart.clear();
      redraws.forEach((fn) => fn());
      updateCartUi();
      orderForm.reset();
      showOrderMessage(result.message, "ok");
    } else {
      showOrderMessage(Object.values(result).join(". "), "error");
    }
  } catch (error) {
    showOrderMessage("Could not send the order. Please call us instead.", "error");
  } finally {
    submit.disabled = false;
  }
});

async function loadOpeningHours() {
  try {
    const response = await fetch("/api/opening-hours");
    openingHours = await response.json();
    hint.textContent = "Bookings: " + openingHours
      .map((h) => h.day + " " + h.open + "–" + h.lastBooking)
      .join(" · ");
  } catch (error) {
    console.error(error);
  }
}

loadOpeningHours();
loadMenu();
updateCartUi();