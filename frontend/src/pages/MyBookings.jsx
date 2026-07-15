import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Navbar from "../components/Navbar";
import "../css/shared.css";
import { useAuth } from "../context/AuthContext";
import { getReservationsByUser, cancelReservation } from "../api/reservationApi";
import { extractErrorMessage } from "../api/client";
import { statusBadgeClass, formatMoney, formatDate } from "../utils/format";

export default function MyBookings() {
  const { user } = useAuth();
  const navigate = useNavigate();

  const [reservations, setReservations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    load();
  }, []);

  async function load() {
    setLoading(true);
    setError("");
    try {
      const data = await getReservationsByUser(user.userId);
      data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
      setReservations(data);
    } catch (err) {
      setError(extractErrorMessage(err, "Could not load your bookings."));
    } finally {
      setLoading(false);
    }
  }

  async function handleCancel(id) {
    if (!window.confirm("Cancel this reservation?")) return;
    try {
      await cancelReservation(id);
      load();
    } catch (err) {
      setError(extractErrorMessage(err, "Could not cancel reservation."));
    }
  }

  function handlePay(reservation) {
    navigate("/payment", { state: { reservation } });
  }

  return (
    <div>
      <Navbar />
      <div className="page-wrap">
        <h1>My Bookings</h1>
        <p className="page-subtitle">Everything you've reserved with us.</p>

        {error && <div className="alert-error">{error}</div>}

        {loading ? (
          <p>Loading...</p>
        ) : reservations.length === 0 ? (
          <p>You don't have any bookings yet.</p>
        ) : (
          <div className="reservation-list">
            {reservations.map((r) => (
              <div key={r.id} className="card reservation-item">
                <div>
                  <strong>{r.hotelName}</strong> &middot; {r.roomTypeName} (Room {r.roomNumber})
                  <div style={{ color: "#667", fontSize: "0.9rem", marginTop: 4 }}>
                    {formatDate(r.checkInDate)} &rarr; {formatDate(r.checkOutDate)} &middot; {formatMoney(r.totalPrice)}
                  </div>
                  <span className={statusBadgeClass(r.status)} style={{ marginTop: 8, display: "inline-block" }}>
                    {r.status.replace(/_/g, " ")}
                  </span>
                </div>
                <div style={{ display: "flex", gap: 8 }}>
                  {r.status === "HELD" && (
                    <button className="btn-primary" onClick={() => handlePay(r)}>Pay Now</button>
                  )}
                  {(r.status === "HELD" || r.status === "CONFIRMED") && (
                    <button className="btn-danger" onClick={() => handleCancel(r.id)}>Cancel</button>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
