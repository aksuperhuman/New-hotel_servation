import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Navbar from "../components/Navbar";
import "../css/shared.css";
import { getHotels } from "../api/hotelApi";
import { searchAvailability } from "../api/reservationApi";
import { extractErrorMessage } from "../api/client";
import { formatMoney } from "../utils/format";

function todayIso() {
  return new Date().toISOString().slice(0, 10);
}

function tomorrowIso() {
  const d = new Date();
  d.setDate(d.getDate() + 1);
  return d.toISOString().slice(0, 10);
}

export default function HotelSearch() {
  const navigate = useNavigate();

  const [city, setCity] = useState("");
  const [hotels, setHotels] = useState([]);
  const [loadingHotels, setLoadingHotels] = useState(true);
  const [error, setError] = useState("");

  const [selectedHotel, setSelectedHotel] = useState(null);
  const [checkInDate, setCheckInDate] = useState(todayIso());
  const [checkOutDate, setCheckOutDate] = useState(tomorrowIso());
  const [availability, setAvailability] = useState(null);
  const [searching, setSearching] = useState(false);

  useEffect(() => {
    loadHotels();
  }, []);

  async function loadHotels(cityFilter) {
    setLoadingHotels(true);
    setError("");
    try {
      const data = await getHotels(cityFilter);
      setHotels(data);
    } catch (err) {
      setError(extractErrorMessage(err, "Could not load hotels."));
    } finally {
      setLoadingHotels(false);
    }
  }

  function handleCitySearch(e) {
    e.preventDefault();
    loadHotels(city || undefined);
  }

  async function handleSelectHotel(hotel) {
    setSelectedHotel(hotel);
    setAvailability(null);
    setError("");
  }

  async function handleCheckAvailability(e) {
    e.preventDefault();
    if (!selectedHotel) return;
    setError("");
    setSearching(true);
    try {
      const results = await searchAvailability({
        hotelId: selectedHotel.id,
        checkInDate,
        checkOutDate,
      });
      setAvailability(results);
    } catch (err) {
      setError(extractErrorMessage(err, "Could not search availability."));
    } finally {
      setSearching(false);
    }
  }

  function handleBook(roomTypeResult) {
    if (!roomTypeResult.availableRoomIds || roomTypeResult.availableRoomIds.length === 0) return;
    const roomId = roomTypeResult.availableRoomIds[0];
    navigate("/book", {
      state: {
        roomId,
        hotelId: selectedHotel.id,
        hotelName: selectedHotel.name,
        roomTypeName: roomTypeResult.roomTypeName,
        basePrice: roomTypeResult.basePrice,
        checkInDate,
        checkOutDate,
      },
    });
  }

  return (
    <div>
      <Navbar />
      <div className="page-wrap">
        <h1>Search Hotels</h1>
        <p className="page-subtitle">Find a hotel, pick your dates, and see what's available.</p>

        {error && <div className="alert-error">{error}</div>}

        <div className="card">
          <form className="form-row" onSubmit={handleCitySearch}>
            <div className="form-field">
              <label>City</label>
              <input
                type="text"
                placeholder="e.g. Chicago"
                value={city}
                onChange={(e) => setCity(e.target.value)}
              />
            </div>
            <div className="form-field" style={{ flex: "0 0 auto", alignSelf: "flex-end" }}>
              <button type="submit" className="btn-primary">Search</button>
            </div>
          </form>
        </div>

        {loadingHotels ? (
          <p>Loading hotels...</p>
        ) : (
          <div className="hotel-grid">
            {hotels.map((hotel) => (
              <div
                key={hotel.id}
                className="hotel-tile"
                onClick={() => handleSelectHotel(hotel)}
                style={selectedHotel?.id === hotel.id ? { outline: "2px solid #2657ff" } : {}}
              >
                <h3>{hotel.name}</h3>
                <div className="stars">{"★".repeat(hotel.starRating || 0)}</div>
                <p>{hotel.city}, {hotel.country}</p>
                <p style={{ color: "#667", fontSize: "0.9rem" }}>{hotel.address}</p>
              </div>
            ))}
            {hotels.length === 0 && <p>No hotels found.</p>}
          </div>
        )}

        {selectedHotel && (
          <div className="card" style={{ marginTop: 24 }}>
            <h2>{selectedHotel.name}</h2>
            <form className="form-row" onSubmit={handleCheckAvailability}>
              <div className="form-field">
                <label>Check In</label>
                <input
                  type="date"
                  value={checkInDate}
                  min={todayIso()}
                  onChange={(e) => setCheckInDate(e.target.value)}
                  required
                />
              </div>
              <div className="form-field">
                <label>Check Out</label>
                <input
                  type="date"
                  value={checkOutDate}
                  min={checkInDate}
                  onChange={(e) => setCheckOutDate(e.target.value)}
                  required
                />
              </div>
              <div className="form-field" style={{ flex: "0 0 auto", alignSelf: "flex-end" }}>
                <button type="submit" className="btn-primary" disabled={searching}>
                  {searching ? "Searching..." : "Check Availability"}
                </button>
              </div>
            </form>

            {availability && (
              <div>
                {availability.length === 0 && <p>No room types configured for this hotel yet.</p>}
                {availability.map((rt) => (
                  <div className="room-type-row" key={rt.roomTypeId}>
                    <div>
                      <strong>{rt.roomTypeName}</strong>
                      <p style={{ margin: "4px 0", color: "#667" }}>
                        {rt.availableCount > 0
                          ? `${rt.availableCount} room(s) available`
                          : "No rooms available for these dates"}
                      </p>
                    </div>
                    <div style={{ textAlign: "right" }}>
                      <div className="price">{formatMoney(rt.basePrice)} / night</div>
                      <button
                        className="btn-primary"
                        style={{ marginTop: 8 }}
                        disabled={rt.availableCount === 0}
                        onClick={() => handleBook(rt)}
                      >
                        Book
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
