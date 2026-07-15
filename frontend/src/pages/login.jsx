import { useState } from "react";
import { useNavigate, Link, useLocation } from "react-router-dom";
import "../css/login.css";
import "../css/shared.css";
import { useAuth } from "../context/AuthContext";
import { extractErrorMessage } from "../api/client";

function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setSubmitting(true);
    try {
      const user = await login({ email, password });
      const redirectTo = location.state?.from || (user.role === "ADMIN" ? "/admin" : "/search");
      navigate(redirectTo, { replace: true });
    } catch (err) {
      setError(extractErrorMessage(err, "Invalid email or password."));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="page">
      <div className="overlay"></div>

      <div className="login-container">
        <h1>Hotel Reservation </h1>
        <h3>system</h3>
        <p>Sign in to continue</p>

        {error && <div className="alert-error">{error}</div>}

        <form onSubmit={handleSubmit}>
          <input
            type="email"
            placeholder="Enter Email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />

          <input
            type="password"
            placeholder="Enter Password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />

          <button type="submit" disabled={submitting}>
            {submitting ? "Signing in..." : "Login"}
          </button>
        </form>

        <div className="footer">
          New User? <Link to="/register">Register Here</Link>
        </div>
      </div>
    </div>
  );
}

export default Login;
