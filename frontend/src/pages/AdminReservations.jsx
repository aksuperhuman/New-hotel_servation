import { useEffect, useMemo, useState } from "react";
import { Calendar, momentLocalizer } from "react-big-calendar";
import moment from "moment";
import "react-big-calendar/lib/css/react-big-calendar.css";
import "../css/shared.css";
import "../css/Dashboard.css";
import { useNavigate } from "react-router-dom";
import { FaArrowLeft } from "react-icons/fa";
import { getAllReservations, updateReservationStatus, cancelReservation } from "../api/reservationApi";
import { extractErrorMessage } from "../api/client";
import { statusBadgeClass, formatMoney, formatDate } from "../utils/format";

const localizer = momentLocalizer(moment);

const STATUS_COLORS = {
  HELD: "#f5a623",
  PAYMENT_PROCESSING: "#3f6fe0",
  CONFIRMED: "#1e9e5b",
  FAILED: "#c93a31",
  RELEASED: "#8a8f9c",
  CANCELLED: "#c93a31",
};

export default function AdminReservations() {
  const navigate = useNavigate();
  const [reservations, setReservations] = useState([]);
  const [selected, setSelected] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    load();
  }, []);

  async function load() {
    setLoading(true);
    setError("");
    try {
      const data = await getAllReservations();
      setReservations(data);
    } catch (err) {
      setError(extractErrorMessage(err, "Could not load reservations."));
    } finally {
      setLoading(false);
    }
  }

  const events = useMemo(
    () =>
      reservations.map((r) => ({
        id: r.id,
        title: `${r.guestName} - ${r.hotelName} (Room ${r.roomNumber})`,
        start: new Date(r.checkInDate),
        end: new Date(r.checkOutDate),
        allDay: true,
        resource: r,
      })),
    [reservations]
  );

  function eventStyleGetter(event) {
    const color = STATUS_COLORS[event.resource.status] || "#3f6fe0";
    return { style: { backgroundColor: color, borderRadius: 4, border: "none" } };
  }

  async function handleStatusChange(status) {
    if (!selected) return;
    setError("");
    try {
      if (status === "CANCELLED") {
        await cancelReservation(selected.id);
      } else {
        await updateReservationStatus(selected.id, status);
      }
      setSelected(null);
      load();
    } catch (err) {
      setError(extractErrorMessage(err, "Could not update reservation status."));
    }
  }

  return (
    <div className="page-wrap" style={{ maxWidth: 1200 }}>
      <button className="btn-secondary" onClick={() => navigate("/admin")} style={{ marginBottom: 16 }}>
        <FaArrowLeft /> Back to Dashboard
      </button>

      <h1>Reservation Management</h1>
      <p className="page-subtitle">Calendar view of every reservation across all hotels.</p>

      {error && <div className="alert-error">{error}</div>}

      <div className="card">
        {loading ? (
          <p>Loading calendar...</p>
        ) : (
          <Calendar
            localizer={localizer}
            events={events}
            startAccessor="start"
            endAccessor="end"
            style={{ height: 600 }}
            eventPropGetter={eventStyleGetter}
            onSelectEvent={(event) => setSelected(event.resource)}
          />
        )}
      </div>

      {selected && (
        <div className="card">
          <h3>Reservation #{selected.id}</h3>
          <div className="summary-row">
            <span>Guest</span>
            <strong>{selected.guestName}</strong>
          </div>
          <div className="summary-row">
            <span>Hotel / Room</span>
            <strong>{selected.hotelName} - Room {selected.roomNumber}</strong>
          </div>
          <div className="summary-row">
            <span>Dates</span>
            <strong>{formatDate(selected.checkInDate)} &rarr; {formatDate(selected.checkOutDate)}</strong>
          </div>
          <div className="summary-row">
            <span>Total</span>
            <strong>{formatMoney(selected.totalPrice)}</strong>
          </div>
          <div className="summary-row">
            <span>Status</span>
            <span className={statusBadgeClass(selected.status)}>{selected.status.replace(/_/g, " ")}</span>
          </div>

          <div style={{ display: "flex", gap: 10, marginTop: 16, flexWrap: "wrap" }}>
            {selected.status === "HELD" && (
              <button className="btn-primary" onClick={() => handleStatusChange("PAYMENT_PROCESSING")}>
                Move to Payment Processing
              </button>
            )}
            {selected.status === "PAYMENT_PROCESSING" && (
              <>
                <button className="btn-primary" onClick={() => handleStatusChange("CONFIRMED")}>
                  Mark Confirmed
                </button>
                <button className="btn-danger" onClick={() => handleStatusChange("FAILED")}>
                  Mark Failed
                </button>
              </>
            )}
            {(selected.status === "HELD" || selected.status === "CONFIRMED") && (
              <button className="btn-danger" onClick={() => handleStatusChange("CANCELLED")}>
                Cancel Reservation
              </button>
            )}
            <button className="btn-secondary" onClick={() => setSelected(null)}>Close</button>
          </div>
        </div>
      )}
    </div>
  );
}
