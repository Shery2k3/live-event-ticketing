#!/usr/bin/env bash
#
# End-to-end demo of the booking -> payment saga, driven entirely through the
# API gateway as a black box. It shows the saga completing on an approved
# payment and compensating (releasing the held seats) on a declined payment.
#
# Prerequisites: the full stack is running (config-server, discovery-server,
# api-gateway, event, inventory, booking, payment) with Postgres and Kafka up.
# Tools required on the machine running this script: curl and jq.
#
# Usage:
#   ./scripts/demo-saga.sh
#   BASE_URL=http://localhost:8080 ./scripts/demo-saga.sh
#
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
USER_ID="${USER_ID:-1}"
TICKET_PRICE="${TICKET_PRICE:-300.00}"
DECLINE_ABOVE="${DECLINE_ABOVE:-1000.00}"   # keep in sync with payment.auto-decline-above
POLL_TIMEOUT="${POLL_TIMEOUT:-25}"

require() { command -v "$1" >/dev/null 2>&1 || { echo "Missing required tool: $1" >&2; exit 1; }; }
require curl
require jq

step() { printf '\n== %s\n' "$1"; }
info() { printf '   %s\n' "$1"; }
fail() { printf '   FAILED: %s\n' "$1" >&2; exit 1; }

api() {
  local method="$1" path="$2" body="${3:-}"
  if [[ -n "$body" ]]; then
    curl -sS -X "$method" "$BASE_URL$path" -H 'Content-Type: application/json' -d "$body"
  else
    curl -sS -X "$method" "$BASE_URL$path"
  fi
}

# POST that captures only the HTTP status code, for the concurrency test.
api_code() {
  local method="$1" path="$2" body="$3" out="$4"
  curl -sS -o "$out" -w '%{http_code}' -X "$method" "$BASE_URL$path" \
    -H 'Content-Type: application/json' -d "$body"
}

wait_for_booking() {
  local ref="$1" expected="$2" waited=0 status
  while (( waited < POLL_TIMEOUT )); do
    status="$(api GET "/api/bookings/$ref" | jq -r '.status')"
    if [[ "$status" == "$expected" ]]; then return 0; fi
    sleep 1; (( waited++ )) || true
  done
  return 1
}

# ---------------------------------------------------------------------------
step "Seeding an event"
EVENT_BODY="$(jq -n --arg price "$TICKET_PRICE" '{
  name: "Saga Demo Concert",
  description: "Seed data for the saga demo",
  venue: "Demo Arena",
  startsAt: "2030-01-01T20:00:00Z",
  totalCapacity: 100,
  ticketPrice: ($price | tonumber)
}')"
EVENT_ID="$(api POST /api/events "$EVENT_BODY" | jq -r '.id')"
[[ "$EVENT_ID" != "null" && -n "$EVENT_ID" ]] || fail "could not create event"
info "eventId=$EVENT_ID  ticketPrice=$TICKET_PRICE"

step "Seeding 8 seats (A1..A8)"
SEATS_BODY="$(jq -n --argjson eid "$EVENT_ID" '{
  eventId: $eid,
  seatNumbers: ["A1","A2","A3","A4","A5","A6","A7","A8"]
}')"
SEAT_IDS="$(api POST /api/inventory/seats "$SEATS_BODY" | jq -c '[.[].id]')"
info "seatIds=$SEAT_IDS"
id_at() { echo "$SEAT_IDS" | jq ".[$1]"; }

# ---------------------------------------------------------------------------
step "Scenario 1 - happy path (payment approved, saga completes)"
HAPPY_SEATS="$(jq -c -n --argjson a "$(id_at 0)" --argjson b "$(id_at 1)" '[$a,$b]')"
info "booking seats $HAPPY_SEATS (total = $TICKET_PRICE x 2, under the $DECLINE_ABOVE limit)"
BODY="$(jq -n --argjson uid "$USER_ID" --argjson eid "$EVENT_ID" --argjson seats "$HAPPY_SEATS" '{userId:$uid, eventId:$eid, seatIds:$seats}')"
REF="$(api POST /api/bookings "$BODY" | jq -r '.bookingReference')"
info "booking accepted: $REF (status PENDING_PAYMENT), waiting for the saga..."
if wait_for_booking "$REF" "CONFIRMED"; then
  info "booking is CONFIRMED"
  info "payment status: $(api GET "/api/payments/$REF" | jq -r '.status')"
  info "seat availability now:"
  api GET "/api/inventory/events/$EVENT_ID/availability" | jq '.'
else
  fail "booking $REF did not reach CONFIRMED within ${POLL_TIMEOUT}s"
fi

# ---------------------------------------------------------------------------
step "Scenario 2 - compensation (payment declined, seats released)"
COMP_SEATS="$(jq -c -n --argjson a "$(id_at 2)" --argjson b "$(id_at 3)" --argjson c "$(id_at 4)" --argjson d "$(id_at 5)" '[$a,$b,$c,$d]')"
info "booking seats $COMP_SEATS (total = $TICKET_PRICE x 4, over the $DECLINE_ABOVE limit -> will be declined)"
BODY="$(jq -n --argjson uid "$USER_ID" --argjson eid "$EVENT_ID" --argjson seats "$COMP_SEATS" '{userId:$uid, eventId:$eid, seatIds:$seats}')"
REF="$(api POST /api/bookings "$BODY" | jq -r '.bookingReference')"
info "booking accepted: $REF (status PENDING_PAYMENT), waiting for the saga..."
if wait_for_booking "$REF" "CANCELLED"; then
  info "booking is CANCELLED (compensated)"
  info "failure reason: $(api GET "/api/bookings/$REF" | jq -r '.failureReason')"
  info "payment status: $(api GET "/api/payments/$REF" | jq -r '.status')"
  info "seat availability now (the 4 held seats should be back to available):"
  api GET "/api/inventory/events/$EVENT_ID/availability" | jq '.'
else
  fail "booking $REF did not reach CANCELLED within ${POLL_TIMEOUT}s"
fi

# ---------------------------------------------------------------------------
step "Scenario 3 - concurrency (two bookings race for the same seat)"
RACE_SEAT="$(jq -c -n --argjson a "$(id_at 6)" '[$a]')"
info "firing two simultaneous bookings for seat $RACE_SEAT"
BODY="$(jq -n --argjson uid "$USER_ID" --argjson eid "$EVENT_ID" --argjson seats "$RACE_SEAT" '{userId:$uid, eventId:$eid, seatIds:$seats}')"
R1="$(mktemp)"; R2="$(mktemp)"; C1="$(mktemp)"; C2="$(mktemp)"
api_code POST /api/bookings "$BODY" "$R1" > "$C1" &
api_code POST /api/bookings "$BODY" "$R2" > "$C2" &
wait
info "request 1 -> HTTP $(cat "$C1")"
info "request 2 -> HTTP $(cat "$C2")"
info "exactly one should be 202 (accepted); the other should be rejected because the seat is already held"
rm -f "$R1" "$R2" "$C1" "$C2"

step "Demo complete"
