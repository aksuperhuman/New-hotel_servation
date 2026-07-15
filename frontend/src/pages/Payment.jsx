import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import Navbar from "../components/Navbar";
import "../css/shared.css";
import { chargePayment } from "../api/paymentApi";
import { getReservation } from "../api/reservationApi";
import { extractErrorMessage } from "../api/client";
import { formatMoney, formatDate } from "../utils/format";

export default function Payment() {
  const { state } = useLocation();
  const navigate = useNavigate();

  const [reservation, setReservation] = useState(state?.reservation || null);
  const [cardNumber, setCardNumber] = useState("");
  const [cardHolderName, setCardHolderName] = useState("");
  const [expiry, setExpiry] = useState("");
  const [cvv, setCvv] = useState("");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (!reservation && state?.reservationId) {
      getReservation(state.reservationId).then(setReservation).catch(() => {});
    }
  }, [reservation, state]);

  if (!reservation) {
    return (
      <div>
        <Navbar />
        <div className="page-wrap">
          <div className="alert-error">
            No reservation to pay for. Please book a room first from{" "}
            <span style={{ textDecoration: "underline", cursor: "pointer" }} onClick={() => navigate("/search")}>
              Search Hotels
            </span>.
          </div>
        </div>
      </div>
    );
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setSubmitting(true);
    try {
      const response = await chargePayment({
        reservationId: reservation.id,
        amount: reservation.totalPrice,
        cardNumber,
        cardHolderName,
      });
      navigate("/confirmation", { state: { reservationId: reservation.id, payment: response } });
    } catch (err) {
      setError(extractErrorMessage(err, "Payment failed. Please check your card details and try again."));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div>
      <Navbar />
      <div className="page-wrap">
        <h1>Payment</h1>
        <p className="page-subtitle">Your room is held while you complete payment.</p>

        {error && <div className="alert-error">{error}</div>}

        <div className="card">
          <h3>{reservation.hotelName}</h3>
          <div className="summary-row">
            <span>Room</span>
            <strong>{reservation.roomTypeName} (Room {reservation.roomNumber})</strong>
          </div>
          <div className="summary-row">
            <span>Dates</span>
            <strong>{formatDate(reservation.checkInDate)} &rarr; {formatDate(reservation.checkOutDate)}</strong>
          </div>
          <div className="summary-row">
            <span>Total Due</span>
            <strong>{formatMoney(reservation.totalPrice)}</strong>
          </div>
        </div>

        <div className="card">
          <h3>Card Details</h3>
          <form onSubmit={handleSubmit}>
            <div className="form-row">
              <div className="form-field">
                <label>Cardholder Name</label>
                <input
                  type="text"
                  value={cardHolderName}
                  onChange={(e) => setCardHolderName(e.target.value)}
                  required
                />
              </div>
            </div>
            <div className="form-row">
              <div className="form-field">
                <label>Card Number</label>
                <input
                  type="text"
                  placeholder="4111 1111 1111 1111"
                  value={cardNumber}
                  onChange={(e) => setCardNumber(e.target.value)}
                  required
                />
              </div>
              <div className="form-field">
                <label>Expiry</label>
                <input
                  type="text"
                  placeholder="MM/YY"
                  value={expiry}
                  onChange={(e) => setExpiry(e.target.value)}
                  required
                />
              </div>
              <div className="form-field">
                <label>CVV</label>
                <input
                  type="password"
                  maxLength={4}
                  value={cvv}
                  onChange={(e) => setCvv(e.target.value)}
                  required
                />
              </div>
            </div>

            <button type="submit" className="btn-primary" disabled={submitting}>
              {submitting ? "Processing payment..." : `Pay ${formatMoney(reservation.totalPrice)}`}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
