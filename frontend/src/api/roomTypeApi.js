import api from "./client";

export function getRoomTypes(hotelId) {
  return api.get("/room-types", { params: hotelId ? { hotelId } : {} }).then((res) => res.data);
}

export function getRoomType(id) {
  return api.get(`/room-types/${id}`).then((res) => res.data);
}

export function createRoomType(roomType) {
  return api.post("/room-types", roomType).then((res) => res.data);
}

export function updateRoomType(id, roomType) {
  return api.put(`/room-types/${id}`, roomType).then((res) => res.data);
}

export function deleteRoomType(id) {
  return api.delete(`/room-types/${id}`).then((res) => res.data);
}
