import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import "../css/shared.css";
import { FaArrowLeft, FaTrash } from "react-icons/fa";
import { getCustomers, deleteCustomer } from "../api/customerApi";
import { extractErrorMessage } from "../api/client";

export default function CustomersAdmin() {
  const navigate = useNavigate();
  const [customers, setCustomers] = useState([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    load();
  }, []);

  async function load() {
    setLoading(true);
    setError("");
    try {
      setCustomers(await getCustomers());
    } catch (err) {
      setError(extractErrorMessage(err, "Could not load customers."));
    } finally {
      setLoading(false);
    }
  }

  async function handleDelete(id) {
    if (!window.confirm("Delete this customer record?")) return;
    try {
      await deleteCustomer(id);
      load();
    } catch (err) {
      setError(extractErrorMessage(err, "Could not delete customer."));
    }
  }

  return (
    <div className="page-wrap">
      <button className="btn-secondary" onClick={() => navigate("/admin")} style={{ marginBottom: 16 }}>
        <FaArrowLeft /> Back to Dashboard
      </button>

      <h1>Customers</h1>
      {error && <div className="alert-error">{error}</div>}

      {loading ? (
        <p>Loading...</p>
      ) : (
        <table className="room-table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Email</th>
              <th>Phone</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            {customers.map((c) => (
              <tr key={c.id}>
                <td>{c.firstName} {c.lastName}</td>
                <td>{c.email}</td>
                <td>{c.phone || "-"}</td>
                <td>
                  <button className="delete" onClick={() => handleDelete(c.id)}><FaTrash /></button>
                </td>
              </tr>
            ))}
            {customers.length === 0 && <tr><td colSpan={4}>No customer records yet.</td></tr>}
          </tbody>
        </table>
      )}
    </div>
  );
}
