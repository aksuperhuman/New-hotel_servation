import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import "../css/shared.css";
import { FaArrowLeft, FaPlus, FaEdit, FaTrash } from "react-icons/fa";
import { getHotels, createHotel, updateHotel, deleteHotel } from "../api/hotelApi";
import { getRoomTypes, createRoomType, updateRoomType, deleteRoomType } from "../api/roomTypeApi";
import { extractErrorMessage } from "../api/client";
import { formatMoney } from "../utils/format";

const emptyHotelForm = { id: null, name: "", address: "", city: "", country: "", starRating: 3, description: "" };
const emptyRoomTypeForm = { id: null, hotelId: "", name: "", basePrice: "", maxOccupancy: 2, description: "", amenities: "" };

export default function HotelManager() {
  const navigate = useNavigate();

  const [hotels, setHotels] = useState([]);
  const [roomTypes, setRoomTypes] = useState([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  const [hotelForm, setHotelForm] = useState(emptyHotelForm);
  const [showHotelForm, setShowHotelForm] = useState(false);

  const [roomTypeForm, setRoomTypeForm] = useState(emptyRoomTypeForm);
  const [showRoomTypeForm, setShowRoomTypeForm] = useState(false);

  useEffect(() => {
    loadAll();
  }, []);

  async function loadAll() {
    setLoading(true);
    setError("");
    try {
      const [hotelsData, roomTypesData] = await Promise.all([getHotels(), getRoomTypes()]);
      setHotels(hotelsData);
      setRoomTypes(roomTypesData);
    } catch (err) {
      setError(extractErrorMessage(err, "Could not load hotels."));
    } finally {
      setLoading(false);
    }
  }

  function hotelName(id) {
    return hotels.find((h) => h.id === id)?.name || "-";
  }

  // ---- Hotels ----

  function openAddHotel() {
    setHotelForm(emptyHotelForm);
    setShowHotelForm(true);
  }

  function openEditHotel(hotel) {
    setHotelForm({
      id: hotel.id,
      name: hotel.name,
      address: hotel.address,
      city: hotel.city,
      country: hotel.country,
      starRating: hotel.starRating || 3,
      description: hotel.description || "",
    });
    setShowHotelForm(true);
  }

  async function submitHotel(e) {
    e.preventDefault();
    setError("");
    const payload = { ...hotelForm, starRating: Number(hotelForm.starRating) };
    try {
      if (hotelForm.id) {
        await updateHotel(hotelForm.id, payload);
      } else {
        await createHotel(payload);
      }
      setShowHotelForm(false);
      loadAll();
    } catch (err) {
      setError(extractErrorMessage(err, "Could not save hotel."));
    }
  }

  async function removeHotel(id) {
    if (!window.confirm("Delete this hotel? This also removes its room types and rooms.")) return;
    try {
      await deleteHotel(id);
      loadAll();
    } catch (err) {
      setError(extractErrorMessage(err, "Could not delete hotel."));
    }
  }

  // ---- Room Types ----

  function openAddRoomType(hotelId) {
    setRoomTypeForm({ ...emptyRoomTypeForm, hotelId: hotelId || "" });
    setShowRoomTypeForm(true);
  }

  function openEditRoomType(rt) {
    setRoomTypeForm({
      id: rt.id,
      hotelId: rt.hotel?.id || "",
      name: rt.name,
      basePrice: rt.basePrice,
      maxOccupancy: rt.maxOccupancy,
      description: rt.description || "",
      amenities: rt.amenities || "",
    });
    setShowRoomTypeForm(true);
  }

  async function submitRoomType(e) {
    e.preventDefault();
    setError("");
    const payload = {
      hotel: { id: Number(roomTypeForm.hotelId) },
      name: roomTypeForm.name,
      basePrice: Number(roomTypeForm.basePrice),
      maxOccupancy: Number(roomTypeForm.maxOccupancy),
      description: roomTypeForm.description,
      amenities: roomTypeForm.amenities,
    };
    try {
      if (roomTypeForm.id) {
        await updateRoomType(roomTypeForm.id, payload);
      } else {
        await createRoomType(payload);
      }
      setShowRoomTypeForm(false);
      loadAll();
    } catch (err) {
      setError(extractErrorMessage(err, "Could not save room type."));
    }
  }

  async function removeRoomType(id) {
    if (!window.confirm("Delete this room type?")) return;
    try {
      await deleteRoomType(id);
      loadAll();
    } catch (err) {
      setError(extractErrorMessage(err, "Could not delete room type."));
    }
  }

  return (
    <div className="page-wrap" style={{ maxWidth: 1100 }}>
      <button className="btn-secondary" onClick={() => navigate("/admin")} style={{ marginBottom: 16 }}>
        <FaArrowLeft /> Back to Dashboard
      </button>

      <h1>Hotel Manager</h1>
      <p className="page-subtitle">Add, edit, and remove hotels and their room types.</p>

      {error && <div className="alert-error">{error}</div>}

      <div className="card">
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <h2 style={{ margin: 0 }}>Hotels</h2>
          <button className="btn-primary" onClick={openAddHotel}><FaPlus /> Add Hotel</button>
        </div>

        {showHotelForm && (
          <form onSubmit={submitHotel} style={{ marginTop: 16 }}>
            <div className="form-row">
              <div className="form-field">
                <label>Name</label>
                <input value={hotelForm.name} onChange={(e) => setHotelForm({ ...hotelForm, name: e.target.value })} required />
              </div>
              <div className="form-field">
                <label>Star Rating</label>
                <input type="number" min={1} max={5} value={hotelForm.starRating} onChange={(e) => setHotelForm({ ...hotelForm, starRating: e.target.value })} />
              </div>
            </div>
            <div className="form-row">
              <div className="form-field">
                <label>Address</label>
                <input value={hotelForm.address} onChange={(e) => setHotelForm({ ...hotelForm, address: e.target.value })} required />
              </div>
              <div className="form-field">
                <label>City</label>
                <input value={hotelForm.city} onChange={(e) => setHotelForm({ ...hotelForm, city: e.target.value })} required />
              </div>
              <div className="form-field">
                <label>Country</label>
                <input value={hotelForm.country} onChange={(e) => setHotelForm({ ...hotelForm, country: e.target.value })} required />
              </div>
            </div>
            <div className="form-row">
              <div className="form-field">
                <label>Description</label>
                <textarea value={hotelForm.description} onChange={(e) => setHotelForm({ ...hotelForm, description: e.target.value })} />
              </div>
            </div>
            <button type="submit" className="btn-primary">Save</button>{" "}
            <button type="button" className="btn-secondary" onClick={() => setShowHotelForm(false)}>Cancel</button>
          </form>
        )}

        {loading ? (
          <p>Loading...</p>
        ) : (
          <table className="room-table" style={{ marginTop: 16 }}>
            <thead>
              <tr>
                <th>Name</th>
                <th>City</th>
                <th>Rating</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {hotels.map((h) => (
                <tr key={h.id}>
                  <td>{h.name}</td>
                  <td>{h.city}, {h.country}</td>
                  <td>{"★".repeat(h.starRating || 0)}</td>
                  <td>
                    <button className="edit" onClick={() => openEditHotel(h)}><FaEdit /></button>
                    <button className="edit" onClick={() => openAddRoomType(h.id)}><FaPlus /></button>
                    <button className="delete" onClick={() => removeHotel(h.id)}><FaTrash /></button>
                  </td>
                </tr>
              ))}
              {hotels.length === 0 && <tr><td colSpan={4}>No hotels yet. Add one above.</td></tr>}
            </tbody>
          </table>
        )}
      </div>

      <div className="card">
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <h2 style={{ margin: 0 }}>Room Types</h2>
          <button className="btn-primary" onClick={() => openAddRoomType()}><FaPlus /> Add Room Type</button>
        </div>

        {showRoomTypeForm && (
          <form onSubmit={submitRoomType} style={{ marginTop: 16 }}>
            <div className="form-row">
              <div className="form-field">
                <label>Hotel</label>
                <select value={roomTypeForm.hotelId} onChange={(e) => setRoomTypeForm({ ...roomTypeForm, hotelId: e.target.value })} required>
                  <option value="">Select hotel</option>
                  {hotels.map((h) => <option key={h.id} value={h.id}>{h.name}</option>)}
                </select>
              </div>
              <div className="form-field">
                <label>Name</label>
                <input value={roomTypeForm.name} onChange={(e) => setRoomTypeForm({ ...roomTypeForm, name: e.target.value })} required />
              </div>
              <div className="form-field">
                <label>Base Price / Night</label>
                <input type="number" step="0.01" min="0" value={roomTypeForm.basePrice} onChange={(e) => setRoomTypeForm({ ...roomTypeForm, basePrice: e.target.value })} required />
              </div>
              <div className="form-field">
                <label>Max Occupancy</label>
                <input type="number" min="1" value={roomTypeForm.maxOccupancy} onChange={(e) => setRoomTypeForm({ ...roomTypeForm, maxOccupancy: e.target.value })} required />
              </div>
            </div>
            <div className="form-row">
              <div className="form-field">
                <label>Amenities</label>
                <input value={roomTypeForm.amenities} onChange={(e) => setRoomTypeForm({ ...roomTypeForm, amenities: e.target.value })} placeholder="WiFi, TV, Mini bar" />
              </div>
            </div>
            <button type="submit" className="btn-primary">Save</button>{" "}
            <button type="button" className="btn-secondary" onClick={() => setShowRoomTypeForm(false)}>Cancel</button>
          </form>
        )}

        {!loading && (
          <table className="room-table" style={{ marginTop: 16 }}>
            <thead>
              <tr>
                <th>Hotel</th>
                <th>Name</th>
                <th>Price / Night</th>
                <th>Max Occupancy</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {roomTypes.map((rt) => (
                <tr key={rt.id}>
                  <td>{rt.hotel?.name || hotelName(rt.hotel?.id)}</td>
                  <td>{rt.name}</td>
                  <td>{formatMoney(rt.basePrice)}</td>
                  <td>{rt.maxOccupancy}</td>
                  <td>
                    <button className="edit" onClick={() => openEditRoomType(rt)}><FaEdit /></button>
                    <button className="delete" onClick={() => removeRoomType(rt.id)}><FaTrash /></button>
                  </td>
                </tr>
              ))}
              {roomTypes.length === 0 && <tr><td colSpan={5}>No room types yet.</td></tr>}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
