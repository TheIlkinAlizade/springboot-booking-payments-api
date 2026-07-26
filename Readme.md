# Booking & Payments API

Backend for a booking and appointments system with real payments. Built with Spring Boot 3, Spring Security (JWT), PostgreSQL, and Stripe Checkout + webhooks (test mode).

**Live demo:** _coming soon_
**Frontend repo:** [nextjs-booking-payments-client](https://github.com/TheIlkinAlizade/nextjs-booking-payments-client)
**API docs (Swagger):** _coming soon_
**Postman collection:** [`/postman`](./postman)

---

## What it does

- Users register/login and get a JWT
- Admins create bookable time slots (title, time window, price, currency)
- Anyone can browse available slots
- A logged-in user books a slot, which creates a Stripe Checkout session and redirects them to pay
- A Stripe webhook confirms the payment server-side and marks the booking `CONFIRMED`
- If a user starts checkout and never pays, a scheduled job expires the booking after a configurable timeout and releases the slot back to `AVAILABLE`
- Slots can't be double-booked: enforced with optimistic locking (`@Version`) at the app level, and a partial unique index at the database level

## Tech stack

| Layer | Technology |
|---|---|
| Language / Framework | Java 21, Spring Boot 3 (Web, Data JPA, Security, Validation) |
| Auth | JWT, BCrypt password hashing, roles (`USER` / `ADMIN`) |
| Database | PostgreSQL (Neon in production, Docker locally) |
| Payments | Stripe Checkout Sessions + signed webhooks |
| API docs | springdoc-openapi (Swagger UI) |
| Containerization | Docker, multi-stage build, Docker Compose for local dev |
| Deployment | Render (backend), Neon (database) |

## API endpoints

| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/auth/register` | Public |
| POST | `/api/auth/login` | Public |
| GET | `/api/slots` | Public |
| GET | `/api/slots/{id}` | Public |
| POST | `/api/slots` | Admin |
| DELETE | `/api/slots/{id}` | Admin |
| POST | `/api/bookings` | Authenticated |
| GET | `/api/bookings/me` | Authenticated |
| GET | `/api/bookings/{id}` | Authenticated (owner or admin) |
| GET | `/api/payments/{bookingId}` | Authenticated (owner or admin) |
| POST | `/api/webhooks/stripe` | Stripe signature only, no JWT |

Full interactive docs at `/swagger-ui.html` once the app is running.

## Prerequisites

- Java 21
- Docker Desktop
- A free [Stripe](https://dashboard.stripe.com) account (sandbox / test mode — no real business or card details required)

## Setup

### 1. Clone and set environment variables

```bash
git clone https://github.com/TheIlkinAlizade/springboot-booking-payments-api.git
cd springboot-booking-payments-api
cp .env.example .env
```

Open `.env` and set your Stripe **test mode** secret key. Everything else has a working local default.

### 2. Start PostgreSQL

```bash
docker compose up -d
```

### 3. Run the app

```bash
./mvnw spring-boot:run
```

API runs at `http://localhost:8080`. Swagger UI at `http://localhost:8080/swagger-ui.html`.

### 4. Forward Stripe webhooks to your local machine

Stripe can't call `localhost` directly, so events need to be tunneled in with the Stripe CLI. No local install needed — run it via Docker:

```bash
docker run --rm -it -v stripe-config:/root/.config/stripe stripe/stripe-cli:latest login
```

This prints a pairing code and a URL — open it in your browser and approve.

```bash
docker run --rm -it -v stripe-config:/root/.config/stripe stripe/stripe-cli:latest listen --forward-to host.docker.internal:8080/api/webhooks/stripe
```

This prints a webhook signing secret (`whsec_...`). Put it in `.env` as `STRIPE_WEBHOOK_SECRET` and restart the app.

**Keep this listener running any time you're testing a real payment locally.** Without it, Stripe has no way to notify your backend that a payment succeeded, and the booking will stay `PENDING` until the expiry job marks it `EXPIRED`.

### 5. Test the full flow

Import the Postman collection + environment from [`/postman`](./postman), or use Swagger UI directly.

1. Register a user
2. Register a second user, then promote it to admin directly in the database:
   ```sql
   UPDATE users SET role = 'ADMIN' WHERE email = 'your-admin-email';
   ```
3. Log in as admin, create a slot
4. Log in as the regular user, book that slot — response includes a Stripe Checkout URL
5. Open that URL, pay with the test card `4242 4242 4242 4242`, any future expiry date, any CVC
6. Check the `stripe listen` terminal for a `200` on `checkout.session.completed`
7. Call `GET /api/bookings/me` — the booking is now `CONFIRMED`

## Environment variables

See [`.env.example`](./.env.example) for the full list. Nothing sensitive is committed; every variable has a safe local default, and real values are provided via environment variables in any deployed environment.

## Related repos

- Frontend: [nextjs-booking-payments-client](https://github.com/TheIlkinAlizade/nextjs-booking-payments-client)