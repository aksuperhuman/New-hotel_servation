import { useEffect, useState } from "react";
import { useLocation, useNavigate, Link } from "react-router-dom";
import Navbar from "../components/Navbar";
import "../css/shared.css";
import { FaCheckCircle } from "react-icons/fa";
import { getReservation } from "../api/reservationApi";
import { formatMoney, formatDate } from "../utils/format";

export default function BookingConfirmation() {
  const { state } = useLocation();
  const navigate = useNavigate();
  const [reservation, setReservation] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!state?.reservationId) return;
    getReservation(state.reservationId)
      .then(setReservation)
      .catch(() => setError("Could not load your confirmed reservation."));
  }, [state]);

  if (!state?.reservationId) {
    return (
      <div>
        <Navbar />
        <div className="page-wrap">
          <div className="alert-error">
            Nothing to confirm yet. Head to{" "}
            <span style={{ textDecoration: "underline", cursor: "pointer" }} onClick={() => navigate("/search")}>
              Search Hotels
            </span>{" "}
            to make a booking.
          </div>
        </div>
      </div>
    );
  }

  return (
    <div>
      <Navbar />
      <div className="page-wrap" style={{ textAlign: "center" }}>
        <FaCheckCircle size={56} color="#1e7b3b" style={{ marginBottom: 12 }} />
        <h1>Booking Confirmed!</h1>
        <p className="page-subtitle">A confirmation has been recorded for your stay.</p>

        {error && <div className="alert-error">{error}</div>}

        {reservation && (
          <div className="card" style={{ textAlign: "left", maxWidth: 480, margin: "0 auto" }}>
            <div className="summary-row">
              <span>Confirmation #</span>
              <strong>{reservation.id}</strong>
            </div>
            <div className="summary-row">
              <span>Hotel</span>
              <strong>{reservation.hotelName}</strong>
            </div>
            <div className="summary-row">
              <span>Room</span>
              <strong>{reservation.roomTypeName} (Room {reservation.roomNumber})</strong>
            </div>
            <div className="summary-row">
              <span>Guest</span>
              <strong>{reservation.guestName}</strong>
            </div>
            <div className="summary-row">
              <span>Dates</span>
              <strong>{formatDate(reservation.checkInDate)} &rarr; {formatDate(reservation.checkOutDate)}</strong>
            </div>
            <div className="summary-row">
              <span>Total Paid</span>
              <strong>{formatMoney(reservation.totalPrice)}</strong>
            </div>
            <div className="summary-row">
              <span>Status</span>
              <strong>{reservation.status}</strong>
            </div>
          </div>
        )}

        <div style={{ marginTop: 24 }}>
          <Link to="/my-bookings" className="btn-primary" style={{ textDecoration: "none", padding: "12px 22px" }}>
            View My Bookings
          </Link>
        </div>
      </div>
    </div>
  );
}
