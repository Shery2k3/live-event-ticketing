// End-to-end saga test, driven through the API gateway as a black box.
// Node 18+, no dependencies. Run: node scripts/e2e.mjs

const BASE = process.env.BASE_URL || "http://localhost:8080";
const DECLINE_ABOVE = Number(process.env.DECLINE_ABOVE || 1000); // payment.auto-decline-above
const TICKET_PRICE = 300;
const POLL_TIMEOUT_MS = 25000;
const POLL_INTERVAL_MS = 1000;

let passed = 0;
let failed = 0;

function log(msg) { process.stdout.write(msg + "\n"); }
function section(title) { log("\n=== " + title + " ==="); }

function check(label, condition) {
    if (condition) { passed++; log("  PASS  " + label); }
    else { failed++; log("  FAIL  " + label); }
}

async function http(method, path, { token, body } = {}) {
    const headers = { "Content-Type": "application/json" };
    if (token) headers["Authorization"] = "Bearer " + token;
    const res = await fetch(BASE + path, {
        method,
        headers,
        body: body ? JSON.stringify(body) : undefined,
    });
    let json = null;
    const text = await res.text();
    if (text) { try { json = JSON.parse(text); } catch { json = text; } }
    return { status: res.status, body: json };
}

async function login(username, password) {
    const r = await http("POST", "/auth/login", { body: { username, password } });
    if (r.status !== 200) throw new Error(`login failed for ${username}: HTTP ${r.status}`);
    return r.body.token;
}

async function pollBookingStatus(ref, expected, token) {
    const deadline = Date.now() + POLL_TIMEOUT_MS;
    let last = null;
    while (Date.now() < deadline) {
        const r = await http("GET", `/api/bookings/${ref}`, { token });
        last = r.body && r.body.status;
        if (last === expected) return r.body;
        if (last === "CONFIRMED" || last === "CANCELLED") return r.body; // terminal
        await new Promise((res) => setTimeout(res, POLL_INTERVAL_MS));
    }
    return { status: last, timedOut: true };
}

async function main() {
    log(`Target gateway: ${BASE}`);
    log(`Ticket price: ${TICKET_PRICE}, decline-above: ${DECLINE_ABOVE}`);

    // --- Auth -----------------------------------------------------------------
    section("Authentication");
    const badLogin = await http("POST", "/auth/login", { body: { username: "admin", password: "wrong" } });
    check("bad credentials rejected with 401", badLogin.status === 401);

    const adminToken = await login("admin", "admin123");
    const userToken = await login("user", "user123");
    check("admin login returns a token", !!adminToken);
    check("user login returns a token", !!userToken);

    // --- Authorization boundaries --------------------------------------------
    section("Authorization boundaries");
    const noToken = await http("POST", "/api/bookings", { body: { userId: 1, eventId: 1, seatIds: [1] } });
    check("booking without a token is 401", noToken.status === 401);

    const userMakesEvent = await http("POST", "/api/events", {
        token: userToken,
        body: { name: "x", description: "x", venue: "x", startsAt: "2030-01-01T20:00:00Z", totalCapacity: 1, ticketPrice: 10 },
    });
    check("USER cannot create an event (403)", userMakesEvent.status === 403);

    // --- Admin seeds an event + seats ----------------------------------------
    section("Admin seeds event and seats");
    const eventRes = await http("POST", "/api/events", {
        token: adminToken,
        body: {
            name: "Saga Demo Concert",
            description: "E2E seed",
            venue: "Demo Arena",
            startsAt: "2030-01-01T20:00:00Z",
            totalCapacity: 100,
            ticketPrice: TICKET_PRICE,
        },
    });
    check("event created (201)", eventRes.status === 201);
    const eventId = eventRes.body.id;
    log(`  eventId = ${eventId}`);

    const seatsRes = await http("POST", "/api/inventory/seats", {
        token: adminToken,
        body: { eventId, seatNumbers: ["A1", "A2", "A3", "A4", "A5", "A6", "A7", "A8"] },
    });
    check("8 seats created (201)", seatsRes.status === 201 && seatsRes.body.length === 8);
    const seatIds = seatsRes.body.map((s) => s.id);
    log(`  seatIds = [${seatIds.join(", ")}]`);

    // --- Scenario 1: HAPPY PATH (approved -> CONFIRMED) ----------------------
    section("Scenario 1 - happy path (payment approved)");
    const happy = await http("POST", "/api/bookings", {
        token: userToken,
        body: { userId: 1, eventId, seatIds: [seatIds[0], seatIds[1]] }, // 2 x 300 = 600 < 1000
    });
    check("booking accepted with 202", happy.status === 202);
    check("initial status is PENDING_PAYMENT", happy.body.status === "PENDING_PAYMENT");
    const happyRef = happy.body.bookingReference;
    log(`  bookingReference = ${happyRef}, waiting for saga...`);

    const happyFinal = await pollBookingStatus(happyRef, "CONFIRMED", userToken);
    check("booking reaches CONFIRMED", happyFinal.status === "CONFIRMED");

    const happyPayment = await http("GET", `/api/payments/${happyRef}`, { token: userToken });
    check("payment status is COMPLETED", happyPayment.body && happyPayment.body.status === "COMPLETED");

    const seatsAfterHappy = await http("GET", `/api/inventory/events/${eventId}/seats`);
    const bookedSeats = seatsAfterHappy.body.filter((s) => [seatIds[0], seatIds[1]].includes(s.id));
    check("the 2 seats are now BOOKED", bookedSeats.every((s) => s.status === "BOOKED"));

    // --- Scenario 2: SAD PATH (declined -> CANCELLED + compensation) ---------
    section("Scenario 2 - compensation (payment declined)");
    const sadSeatIds = [seatIds[2], seatIds[3], seatIds[4], seatIds[5]]; // 4 x 300 = 1200 > 1000
    const sad = await http("POST", "/api/bookings", {
        token: userToken,
        body: { userId: 1, eventId, seatIds: sadSeatIds },
    });
    check("booking accepted with 202", sad.status === 202);
    const sadRef = sad.body.bookingReference;
    log(`  bookingReference = ${sadRef}, waiting for saga...`);

    const sadFinal = await pollBookingStatus(sadRef, "CANCELLED", userToken);
    check("booking reaches CANCELLED", sadFinal.status === "CANCELLED");
    check("booking has a failureReason", !!sadFinal.failureReason);
    log(`  failureReason = ${sadFinal.failureReason}`);

    const sadPayment = await http("GET", `/api/payments/${sadRef}`, { token: userToken });
    check("payment status is FAILED", sadPayment.body && sadPayment.body.status === "FAILED");

    const seatsAfterSad = await http("GET", `/api/inventory/events/${eventId}/seats`);
    const releasedSeats = seatsAfterSad.body.filter((s) => sadSeatIds.includes(s.id));
    check("compensation released seats back to AVAILABLE",
        releasedSeats.every((s) => s.status === "AVAILABLE"));

    // --- Scenario 3: CONCURRENCY (two bookings race for one seat) ------------
    section("Scenario 3 - concurrency (double-book the same seat)");
    const raceSeat = [seatIds[6]];
    const raceBody = { token: userToken, body: { userId: 1, eventId, seatIds: raceSeat } };
    const [r1, r2] = await Promise.all([
        http("POST", "/api/bookings", raceBody),
        http("POST", "/api/bookings", raceBody),
    ]);
    log(`  request 1 -> HTTP ${r1.status}`);
    log(`  request 2 -> HTTP ${r2.status}`);
    const accepted = [r1, r2].filter((r) => r.status === 202).length;
    check("exactly one of the two requests is accepted", accepted === 1);

    // --- Summary --------------------------------------------------------------
    section("Summary");
    log(`  passed: ${passed}`);
    log(`  failed: ${failed}`);
    process.exit(failed === 0 ? 0 : 1);
}

main().catch((err) => {
    log("\nFATAL: " + err.message);
    process.exit(1);
});