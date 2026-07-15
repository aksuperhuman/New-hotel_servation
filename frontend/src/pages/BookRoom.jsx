import { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import Navbar from "../components/Navbar";
import "../css/BookRoom.css";
import "../css/shared.css";
import { useAuth } from "../context/AuthContext";
import { createReservation } from "../api/reservationApi";
import { extractErrorMessage } from "../api/client";
import { formatMoney, nightsBetween } from "../utils/format";

function BookRoom() {
  const { state } = useLocation();
  const navigate = useNavigate();
  const { user, isAuthenticated } = useAuth();

  const [checkInDate, setCheckInDate] = useState(state?.checkInDate || "");
  const [checkOutDate, setCheckOutDate] = useState(state?.checkOutDate || "");
  const [guestName, setGuestName] = useState(user?.name || "");
  const [numberOfGuests, setNumberOfGuests] = useState(1);
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  if (!state?.roomId) {
    return (
      <div>
        <Navbar />
        <div className="page-wrap">
          <div className="alert-error">
            No room selected. Please start from{" "}
            <span style={{ textDecoration: "underline", cursor: "pointer" }} onClick={() => navigate("/search")}>
              Search Hotels
            </span>.
          </div>
        </div>
      </div>
    );
  }

  const nights = nightsBetween(checkInDate, checkOutDate);
  const estimatedTotal = nights * (state.basePrice || 0);

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");

    if (!isAuthenticated) {
      navigate("/login", { state: { from: "/book" } });
      return;
    }

    setSubmitting(true);
    try {
      const reservation = await createReservation({
        roomId: state.roomId,
        guestName,
        checkInDate,
        checkOutDate,
        numberOfGuests: Number(numberOfGuests),
      });
      navigate("/payment", { state: { reservation } });
    } catch (err) {
      setError(extractErrorMessage(err, "Could not create reservation."));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div>
      <Navbar />
      <div className="page-wrap">
        <h1>Book Your Room</h1>
        <p className="page-subtitle">
          {state.hotelName} &middot; {state.roomTypeName}
        </p>

        {error && <div className="alert-error">{error}</div>}

        <div className="card">
          <form onSubmit={handleSubmit}>
            <div className="form-row">
              <div className="form-field">
                <label>Guest Name</label>
                <input
                  type="text"
                  value={guestName}
                  onChange={(e) => setGuestName(e.target.value)}
                  required
                />
              </div>
              <div className="form-field">
                <label>Number of Guests</label>
                <input
                  type="number"
                  min={1}
                  value={numberOfGuests}
                  onChange={(e) => setNumberOfGuests(e.target.value)}
                  required
                />
              </div>
            </div>
            <div className="form-row">
              <div className="form-field">
                <label>Check In</label>
                <input
                  type="date"
                  value={checkInDate}
                  onChange={(e) => setCheckInDate(e.target.value)}
                  required
                />
              </div>
              <div className="form-field">
                <label>Check Out</label>
                <input
                  type="date"
                  value={checkOutDate}
                  min={checkInDate}
                  onChange={(e) => setCheckOutDate(e.target.value)}
                  required
                />
              </div>
            </div>

            <div className="summary-row">
              <span>Rate</span>
              <strong>{formatMoney(state.basePrice)} / night</strong>
            </div>
            <div className="summary-row">
              <span>Nights</span>
              <strong>{nights}</strong>
            </div>
            <div className="summary-row">
              <span>Estimated Total</span>
              <strong>{formatMoney(estimatedTotal)}</strong>
            </div>

            <p style={{ color: "#889", fontSize: "0.85rem", margin: "12px 0" }}>
              This room will be held for you for 5 minutes while you complete payment.
            </p>

            <button type="submit" className="btn-primary" disabled={submitting || nights === 0}>
              {submitting ? "Reserving..." : "Reserve & Continue to Payment"}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}

export default BookRoom;
