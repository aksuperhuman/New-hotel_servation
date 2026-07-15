import { Link, useNavigate } from "react-router-dom";
import { FaHotel, FaSignOutAlt } from "react-icons/fa";
import { useAuth } from "../context/AuthContext";
import "../css/Navbar.css";

export default function Navbar() {
  const { isAuthenticated, isAdmin, user, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate("/login");
  }

  return (
    <nav className="navbar">
      <Link to="/" className="navbar-brand">
        <FaHotel /> Grand Hotel
      </Link>
      <div className="navbar-links">
        <Link to="/search">Search Hotels</Link>
        {isAuthenticated && <Link to="/my-bookings">My Bookings</Link>}
        {isAdmin && <Link to="/admin">Admin</Link>}
        {isAuthenticated ? (
          <>
            <span className="navbar-user">Hi, {user.name}</span>
            <button className="navbar-logout" onClick={handleLogout}>
              <FaSignOutAlt /> Logout
            </button>
          </>
        ) : (
          <>
            <Link to="/login">Login</Link>
            <Link to="/register">Register</Link>
          </>
        )}
      </div>
    </nav>
  );
}
