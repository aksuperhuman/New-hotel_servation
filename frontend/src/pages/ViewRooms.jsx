import { useEffect, useState } from "react";
import "../css/ViewRooms.css";
import "../css/shared.css";
import { FaPlus, FaSearch, FaEdit, FaTrash, FaTools, FaCheckCircle } from "react-icons/fa";
import { getRooms, createRoom, updateRoom, deleteRoom, setRoomStatus } from "../api/roomApi";
import { getHotels } from "../api/hotelApi";
import { getRoomTypes } from "../api/roomTypeApi";
import { extractErrorMessage } from "../api/client";

const emptyForm = { id: null, hotelId: "", roomTypeId: "", roomNumber: "", floor: "" };

function ViewRooms() {
  const [rooms, setRooms] = useState([]);
  const [hotels, setHotels] = useState([]);
  const [roomTypes, setRoomTypes] = useState([]);
  const [search, setSearch] = useState("");
  const [form, setForm] = useState(emptyForm);
  const [showForm, setShowForm] = useState(false);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadAll();
  }, []);

  async function loadAll() {
    setLoading(true);
    setError("");
    try {
      const [roomsData, hotelsData, roomTypesData] = await Promise.all([
        getRooms(),
        getHotels(),
        getRoomTypes(),
      ]);
      setRooms(roomsData);
      setHotels(hotelsData);
      setRoomTypes(roomTypesData);
    } catch (err) {
      setError(extractErrorMessage(err, "Could not load rooms."));
    } finally {
      setLoading(false);
    }
  }

  function hotelName(id) {
    return hotels.find((h) => h.id === id)?.name || "-";
  }

  function roomTypeName(id) {
    return roomTypes.find((rt) => rt.id === id)?.name || "-";
  }

  function openAddForm() {
    setForm(emptyForm);
    setShowForm(true);
  }

  function openEditForm(room) {
    setForm({
      id: room.id,
      hotelId: room.hotel?.id || "",
      roomTypeId: room.roomType?.id || "",
      roomNumber: room.roomNumber || "",
      floor: room.floor ?? "",
    });
    setShowForm(true);
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    const payload = {
      hotel: { id: Number(form.hotelId) },
      roomType: { id: Number(form.roomTypeId) },
      roomNumber: form.roomNumber,
      floor: form.floor === "" ? null : Number(form.floor),
    };
    try {
      if (form.id) {
        await updateRoom(form.id, payload);
      } else {
        await createRoom(payload);
      }
      setShowForm(false);
      loadAll();
    } catch (err) {
      setError(extractErrorMessage(err, "Could not save room."));
    }
  }

  async function handleDelete(id) {
    if (!window.confirm("Delete this room?")) return;
    try {
      await deleteRoom(id);
      loadAll();
    } catch (err) {
      setError(extractErrorMessage(err, "Could not delete room."));
    }
  }

  async function toggleMaintenance(room) {
    const nextStatus = room.status === "AVAILABLE" ? "MAINTENANCE" : "AVAILABLE";
    try {
      await setRoomStatus(room.id, nextStatus);
      loadAll();
    } catch (err) {
      setError(extractErrorMessage(err, "Could not update room status."));
    }
  }

  const filteredRooms = rooms.filter((room) =>
    room.roomNumber?.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="rooms-page">
      <div className="page-header">
        <h1>🏨 Room Management</h1>
        <button className="add-room" onClick={openAddForm}>
          <FaPlus /> Add Room
        </button>
      </div>

      {error && <div className="alert-error">{error}</div>}

      <div className="search-box">
        <FaSearch />
        <input
          type="text"
          placeholder="Search room number..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
      </div>

      {showForm && (
        <div className="card" style={{ marginBottom: 20 }}>
          <h3>{form.id ? "Edit Room" : "Add Room"}</h3>
          <form onSubmit={handleSubmit}>
            <div className="form-row">
              <div className="form-field">
                <label>Hotel</label>
                <select
                  value={form.hotelId}
                  onChange={(e) => setForm({ ...form, hotelId: e.target.value })}
                  required
                >
                  <option value="">Select hotel</option>
                  {hotels.map((h) => (
                    <option key={h.id} value={h.id}>{h.name}</option>
                  ))}
                </select>
              </div>
              <div className="form-field">
                <label>Room Type</label>
                <select
                    value={form.roomTypeId}
                    onChange={(e) => setForm({ ...form, roomTypeId: e.target.value })}
                    required
                >
                  <option value="">Select room type</option>

                  {roomTypes.map((rt) => (
                      <option key={rt.id} value={rt.id}>
                        {rt.name}
                      </option>
                  ))}
                </select>
              </div>
              <div className="form-field">
                <label>Room Number</label>
                <input
                  type="text"
                  value={form.roomNumber}
                  onChange={(e) => setForm({ ...form, roomNumber: e.target.value })}
                  required
                />
              </div>
              <div className="form-field">
                <label>Floor</label>
                <input
                  type="number"
                  value={form.floor}
                  onChange={(e) => setForm({ ...form, floor: e.target.value })}
                />
              </div>
            </div>
            <button type="submit" className="btn-primary">Save</button>{" "}
            <button type="button" className="btn-secondary" onClick={() => setShowForm(false)}>Cancel</button>
          </form>
        </div>
      )}

      {loading ? (
        <p>Loading rooms...</p>
      ) : (
        <table className="room-table">
          <thead>
            <tr>
              <th>Room</th>
              <th>Hotel</th>
              <th>Type</th>
              <th>Status</th>
              <th>Floor</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            {filteredRooms.map((room) => (
              <tr key={room.id}>
                <td>{room.roomNumber}</td>
                <td>{room.hotel?.name || hotelName(room.hotel?.id)}</td>
                <td>{room.roomType?.name || roomTypeName(room.roomType?.id)}</td>
                <td>
                  <span className={`status ${room.status?.toLowerCase()}`}>{room.status}</span>
                </td>
                <td>{room.floor ?? "-"}</td>
                <td>
                  <button className="edit" onClick={() => openEditForm(room)} title="Edit">
                    <FaEdit />
                  </button>
                  <button
                    className="edit"
                    onClick={() => toggleMaintenance(room)}
                    title={room.status === "AVAILABLE" ? "Block for maintenance" : "Mark available"}
                  >
                    {room.status === "AVAILABLE" ? <FaTools /> : <FaCheckCircle />}
                  </button>
                  <button className="delete" onClick={() => handleDelete(room.id)} title="Delete">
                    <FaTrash />
                  </button>
                </td>
              </tr>
            ))}
            {filteredRooms.length === 0 && (
              <tr>
                <td colSpan={6}>No rooms found.</td>
              </tr>
            )}
          </tbody>
        </table>
      )}
    </div>
  );
}

export default ViewRooms;
