import { useEffect, useState } from "react";
import "../css/Dashboard.css";
import "../css/shared.css";
import { useNavigate } from "react-router-dom";
import {
  FaHotel,
  FaHome,
  FaBed,
  FaClipboardList,
  FaUsers,
  FaBell,
  FaUserCircle,
  FaBars,
  FaArrowRight,
  FaDoorOpen,
  FaCheckCircle,
  FaCalendarCheck,
  FaBuilding
} from "react-icons/fa";
import { useAuth } from "../context/AuthContext";
import { getHotels } from "../api/hotelApi";
import { getRooms } from "../api/roomApi";
import { getAllReservations } from "../api/reservationApi";
import { statusBadgeClass, formatDate } from "../utils/format";

function Dashboard() {
  const navigate = useNavigate();
  const { user, logout } = useAuth();

  const [hotels, setHotels] = useState([]);
  const [rooms, setRooms] = useState([]);
  const [reservations, setReservations] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([getHotels(), getRooms(), getAllReservations()])
      .then(([hotelsData, roomsData, reservationsData]) => {
        setHotels(hotelsData);
        setRooms(roomsData);
        setReservations(
          reservationsData.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
        );
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const availableCount = rooms.filter((r) => r.status === "AVAILABLE").length;
  const confirmedCount = reservations.filter((r) => r.status === "CONFIRMED").length;
  const recentBookings = reservations.slice(0, 6);

  return (
    <div className="dashboard">
      {/* ================= Sidebar ================= */}
      <aside className="sidebar">
        <div className="logo">
          <FaHotel />
          <h2>Grand Hotel</h2>
        </div>

        <ul>
          <li className="active">
            <FaHome />
            Dashboard
          </li>

          <li onClick={() => navigate("/admin/hotels")}>
            <FaBuilding />
            Hotels
          </li>

          <li onClick={() => navigate("/rooms")}>
            <FaBed />
            Rooms
          </li>

          <li onClick={() => navigate("/admin/reservations")}>
            <FaClipboardList />
            Reservations
          </li>

          <li onClick={() => navigate("/customers")}>
            <FaUsers />
            Customers
          </li>
        </ul>
      </aside>

      {/* ================= Main ================= */}
      <main className="main">
        <header className="topbar">
          <FaBars className="menu" />

          <div className="top-right">
            <FaBell className="icon" />

            <div className="profile" onClick={logout} title="Click to logout" style={{ cursor: "pointer" }}>
              <FaUserCircle className="profile-icon" />
              <div>
                <h4>{user?.name || "Admin"}</h4>
                <span>Administrator</span>
              </div>
            </div>
          </div>
        </header>

        <section className="welcome">
          <h1>Welcome Back 👋</h1>
          <p>Manage hotels, rooms, bookings and customers from one dashboard.</p>
        </section>

        <section className="hero-cards">
          <div className="hero-card rooms">
            <div>
              <h2>ROOM MANAGEMENT</h2>
              <p>Browse and manage all hotel rooms, block for maintenance.</p>
              <button onClick={() => navigate("/rooms")}>
                Manage Rooms <FaArrowRight />
              </button>
            </div>
            <FaDoorOpen className="hero-icon" />
          </div>

          <div className="hero-card booking">
            <div>
              <h2>RESERVATIONS</h2>
              <p>View the calendar and manage every reservation's status.</p>
              <button onClick={() => navigate("/admin/reservations")}>
                View Calendar <FaArrowRight />
              </button>
            </div>
            <FaHotel className="hero-icon" />
          </div>
        </section>

        <section className="stats">
          <div className="stat-card">
            <div className="circle blue">
              <FaHotel />
            </div>
            <div>
              <h4>Total Hotels</h4>
              <h2>{loading ? "-" : hotels.length}</h2>
            </div>
          </div>

          <div className="stat-card">
            <div className="circle green">
              <FaCheckCircle />
            </div>
            <div>
              <h4>Available Rooms</h4>
              <h2>{loading ? "-" : availableCount}</h2>
            </div>
          </div>

          <div className="stat-card">
            <div className="circle orange">
              <FaCalendarCheck />
            </div>
            <div>
              <h4>Confirmed Bookings</h4>
              <h2>{loading ? "-" : confirmedCount}</h2>
            </div>
          </div>

          <div className="stat-card">
            <div className="circle purple">
              <FaBed />
            </div>
            <div>
              <h4>Total Rooms</h4>
              <h2>{loading ? "-" : rooms.length}</h2>
            </div>
          </div>
        </section>

        <section className="content-grid">
          <div className="table-card" style={{ gridColumn: "1 / -1" }}>
            <div className="table-header">
              <h2>Recent Bookings</h2>
              <button className="view-all" onClick={() => navigate("/admin/reservations")}>
                View All
              </button>
            </div>

            <table>
              <thead>
                <tr>
                  <th>Guest</th>
                  <th>Hotel</th>
                  <th>Room</th>
                  <th>Check In</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {recentBookings.map((r) => (
                  <tr key={r.id}>
                    <td>{r.guestName}</td>
                    <td>{r.hotelName}</td>
                    <td>{r.roomNumber}</td>
                    <td>{formatDate(r.checkInDate)}</td>
                    <td>
                      <span className={statusBadgeClass(r.status)}>{r.status.replace(/_/g, " ")}</span>
                    </td>
                  </tr>
                ))}
                {recentBookings.length === 0 && (
                  <tr>
                    <td colSpan={5}>No bookings yet.</td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </section>
      </main>
    </div>
  );
}

export default Dashboard;
