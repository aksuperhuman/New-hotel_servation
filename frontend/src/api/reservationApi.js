import api from "./client";

export function searchAvailability({ hotelId, roomTypeId, checkInDate, checkOutDate }) {
  return api
    .post("/reservations/search", { hotelId, roomTypeId, checkInDate, checkOutDate })
    .then((res) => res.data);
}

export function checkRoomAvailability({ roomId, checkInDate, checkOutDate }) {
  return api
    .get("/reservations/availability", { params: { roomId, checkInDate, checkOutDate } })
    .then((res) => res.data);
}

export function createReservation(reservationRequest) {
  return api.post("/reservations", reservationRequest).then((res) => res.data);
}

export function getReservation(id) {
  return api.get(`/reservations/${id}`).then((res) => res.data);
}

export function getAllReservations() {
  return api.get("/reservations").then((res) => res.data);
}

export function getReservationsByUser(userId) {
  return api.get("/reservations", { params: { userId } }).then((res) => res.data);
}

export function getReservationsByHotel(hotelId) {
  return api.get("/reservations", { params: { hotelId } }).then((res) => res.data);
}

export function cancelReservation(id) {
  return api.post(`/reservations/${id}/cancel`).then((res) => res.data);
}

export function updateReservationStatus(id, status) {
  return api.patch(`/reservations/${id}/status`, { status }).then((res) => res.data);
}
