import api from "./client";

export function getRooms(hotelId) {
  return api.get("/rooms", { params: hotelId ? { hotelId } : {} }).then((res) => res.data);
}

export function getRoom(id) {
  return api.get(`/rooms/${id}`).then((res) => res.data);
}

export function createRoom(room) {
  return api.post("/rooms", room).then((res) => res.data);
}

export function updateRoom(id, room) {
  return api.put(`/rooms/${id}`, room).then((res) => res.data);
}

export function setRoomStatus(id, status) {
  return api.patch(`/rooms/${id}/status`, { status }).then((res) => res.data);
}

export function deleteRoom(id) {
  return api.delete(`/rooms/${id}`).then((res) => res.data);
}
