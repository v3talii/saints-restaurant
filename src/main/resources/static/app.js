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

        const price = document.createElement("div");
        price.className = "price";
        price.textContent = "€" + Number(item.price).toFixed(2);

        row.appendChild(left);
        row.appendChild(price);
        container.appendChild(row);
      }
    });
  } catch (error) {
    container.textContent = "Sorry, the menu could not be loaded. Please try again later.";
    console.error(error);
  }
}

loadMenu();

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

// Booking form
const form = document.getElementById("booking-form");
const message = document.getElementById("booking-message");
const hint = document.getElementById("booking-hint");
let openingHours = [];

function showMessage(text, type) {
  message.className = type || "";
  message.textContent = text;
}

function hoursFor(dateValue) {
  const day = new Date(dateValue + "T00:00:00").getDay();
  return openingHours.find((h) => h.jsDay === day);
}

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

// Block past dates
form.elements.date.min = new Date().toISOString().split("T")[0];

// Allow only open days and limit time to that day's hours
form.elements.date.addEventListener("change", () => {
  const value = form.elements.date.value;
  const time = form.elements.time;
  if (!value || openingHours.length === 0) return;

  const hours = hoursFor(value);
  if (!hours) {
    showMessage("We take bookings on Friday, Saturday and Sunday only.", "error");
    form.elements.date.value = "";
    time.min = "";
    time.max = "";
    return;
  }

  showMessage("");
  time.min = hours.open;
  time.max = hours.lastBooking;
  time.step = 900; // 15 minutes
  if (time.value && (time.value < hours.open || time.value > hours.lastBooking)) {
    time.value = "";
  }
});

form.addEventListener("submit", async (event) => {
  event.preventDefault();

  const data = Object.fromEntries(new FormData(form));
  data.guests = Number(data.guests);

  // Client-side check (server checks again)
  if (data.date && openingHours.length > 0) {
    const hours = hoursFor(data.date);
    if (!hours) {
      showMessage("We take bookings on Friday, Saturday and Sunday only.", "error");
      return;
    }
    if (data.time && (data.time < hours.open || data.time > hours.lastBooking)) {
      showMessage("On " + hours.day + " we take bookings from " + hours.open + " to " + hours.lastBooking + ".", "error");
      return;
    }
  }

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