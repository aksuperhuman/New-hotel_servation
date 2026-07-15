import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import "../Register.css";
import "../css/shared.css";
import { useAuth } from "../context/AuthContext";
import { extractErrorMessage } from "../api/client";

function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();

  const [form, setForm] = useState({
    name: "",
    email: "",
    password: "",
    confirmPassword: "",
  });
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  function update(field) {
    return (e) => setForm((f) => ({ ...f, [field]: e.target.value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");

    if (form.password !== form.confirmPassword) {
      setError("Passwords do not match.");
      return;
    }

    setSubmitting(true);
    try {
      const user = await register({
        name: form.name,
        email: form.email,
        password: form.password,
        role: "CUSTOMER",
      });
      navigate(user.role === "ADMIN" ? "/admin" : "/search", { replace: true });
    } catch (err) {
      setError(extractErrorMessage(err, "Registration failed. Please try again."));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="page">
      <div className="overlay"></div>

      <div className="register-container">
        <div className="logo">🏨</div>

        <h1>Hotel Reservation System</h1>
        <p>Create Your Account</p>

        {error && <div className="alert-error">{error}</div>}

        <form onSubmit={handleSubmit}>
          <input
            type="text"
            placeholder="Enter Full Name"
            value={form.name}
            onChange={update("name")}
            required
          />
          <input
            type="email"
            placeholder="Enter Email"
            value={form.email}
            onChange={update("email")}
            required
          />
          <input
            type="password"
            placeholder="Enter Password"
            value={form.password}
            onChange={update("password")}
            minLength={6}
            required
          />
          <input
            type="password"
            placeholder="Confirm Password"
            value={form.confirmPassword}
            onChange={update("confirmPassword")}
            minLength={6}
            required
          />

          <button type="submit" disabled={submitting}>
            {submitting ? "Creating account..." : "Register"}
          </button>
        </form>

        <div className="footer">
          Already have an account? <Link to="/login">Login Here</Link>
        </div>
      </div>
    </div>
  );
}

export default Register;
