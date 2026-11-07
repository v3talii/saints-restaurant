# Saints Bar & Restaurant: website with table booking and takeaway orders

A small full-stack web app I built for a real local bar and restaurant. It shows the menu (with allergens), takes table bookings and takeaway orders, and sends the staff an email for each one.

**Live demo:** https://saints-restaurant.onrender.com
(It runs on a free tier, so the first load after a quiet period can take about a minute while the server wakes up.)

> Not an official site. I built it as a trial version for the restaurant and for my portfolio. Name, menu and branding belong to the restaurant and are used with their knowledge.

## Why I made this

I found out the restaurant had no website, only Instagram and Facebook posts with photos of the menu. Guests had to phone to book a table.

Instead of just suggesting it, I offered to build a minimal, working version that they could actually try. The idea was simple: if people use it, they can decide whether a better version (and a proper domain such as a `.com`) is worth paying for. If not, it cost them nothing. For me it was a real project with a real user, which is very different from a lab exercise.

The staff have seen it and the reaction was positive, so the next step is deciding what to keep.

## What it does

- **Menu:** loaded from the database through a REST API. Categories, prices in euro, allergen numbers and extras like "Add chicken +€6".
- **Table booking:** form with validation. Only Friday, Saturday and Sunday can be booked, and the time has to fit that day's opening hours. The server checks everything again, not only the browser.
- **Takeaway:** guests tap `+` on dishes, change quantities, and send an order with a pickup time. **The server calculates the prices from the database**, so a customer cannot change a price in the browser.
- **Emails to staff:** subjects start with `BOOKING:` or `TAKEAWAY:` so they are easy to tell apart.
- **Mobile friendly:** most guests will open it from a phone.

## Tech stack

| Part | What I used |
|---|---|
| Backend | Java 25, Spring Boot (Web, Data JPA, Validation) |
| Database | H2 in memory for local work, PostgreSQL (Neon) in production |
| Frontend | Plain HTML, CSS and JavaScript (no framework), served by Spring Boot |
| Email | Resend HTTP API |
| Hosting | Render (Docker), Neon for the database, GitHub for the code |

## How I built it

I started from the structure and went step by step instead of jumping into the design:

1. Talked to the restaurant and wrote down what they need: menu categories, what to show for each dish, opening hours, how bookings work now.
2. Chose the stack. I picked Java and Spring Boot because I am learning Java at university and wanted to use it on something real.
3. Data model first: `Category`, `MenuItem`, `Reservation`, later `TakeawayOrder`.
4. REST API second: `GET /api/menu`, `POST /api/reservations`, `POST /api/takeaway`. I tested these in the browser before writing any page.
5. Frontend last: the page just calls the API with `fetch()` and draws the result.
6. Then validation, opening hours, notifications, and finally deployment.

## Project structure

```
src/main/java/ie/saints/saints_backend/
├── menu/          Category, MenuItem, repositories, MenuController, DataSeeder
├── reservation/   Reservation, validation, opening hours, notifiers
└── takeaway/      TakeawayOrder, OrderLine, request validation, controller
src/main/resources/static/   index.html, style.css, app.js
```

Notifications use a small `BookingNotifier` interface. Locally it just prints to the console (`LogNotifier`), and in production it sends email (`EmailNotifier`). I can switch with one setting and the rest of the code does not change.

## Run it locally

You need Java 21 or newer.

```
./mvnw spring-boot:run
```

Open http://localhost:8080. Without any settings it uses the in-memory H2 database, fills in the menu automatically, and prints bookings and orders to the console instead of sending email.

## Environment variables

No secrets are stored in the code. They are set on the hosting platform.

| Variable | Purpose |
|---|---|
| `DB_URL` | JDBC URL, for example `jdbc:postgresql://HOST/DB?sslmode=require` |
| `DB_USER` | Database user |
| `DB_PASSWORD` | Database password |
| `EMAIL_ENABLED` | `true` to send real emails |
| `EMAIL_TO` | Where booking and order emails go |
| `RESEND_API_KEY` | API key for Resend |
| `EMAIL_FROM` | Optional sender address |

## Problems I ran into (and what I learned)

- **Wrong JDBC URL on the first deploy.** The app crashed on start because the Neon connection string starts with `postgresql://` but Java needs `jdbc:postgresql://`. I learned to read the first `Caused by:` line in a stack trace, not the whole thing.
- **Emails worked locally but not on Render.** The free tier blocks outgoing SMTP, so Gmail timed out. I moved to an HTTP email API instead. I also added longer timeouts and one retry, because the free server is slow right after waking up.
- **The booking is never lost if the email fails.** The order is saved first, and sending the email is wrapped so an error is only logged.
- **Browser cache.** The new JavaScript did not show up on the deployed site until I added `?v=` to the file links.
- **Never trusting the browser.** Prices and opening hours are checked on the server.
- **Keeping secrets out of Git.** I used environment variables and checked the Git history before making the repo public.
- A date input overflowing its card on iPhone, `BigDecimal` for money, JPA relations, and how Docker builds a Spring Boot app.

## How I used AI

I used an AI assistant (Claude) throughout the project: to plan the steps, to explain things I did not know yet, to suggest code, and to help me read errors when a deploy failed. I typed, ran and tested everything myself, fixed the mistakes, and made the decisions about what the restaurant needs. I am not hiding it, it is part of how I learn, but I also try to make sure I can explain every file in this repo.

## Known limitations

- Menu changes currently need a database edit, because the seed data only runs on an empty database.
- No admin page yet.
- Anyone can submit the forms, so there is no bot protection.
- Takeaway extras (like adding chicken) are written in the notes field and are not added to the total.
- Payment is on collection, there is no online payment.
- The free hosting sleeps when nobody visits.
- No automated tests yet.
- Emails currently go to one address only (Resend limit without my own domain).

## Ideas for next steps

- Admin page so the staff can change prices and hide dishes that ran out.
- Own domain (`.com` or `.ie`) and a proper restaurant email address.
- Rate limiting or a captcha on the forms.
- Automated tests (JUnit, MockMvc).
- Send emails in the background so the form answers faster.
- Database migrations with Flyway.
- Confirmation email to the guest.

## Author

Second-year student at TU Dublin. Feedback is welcome.
