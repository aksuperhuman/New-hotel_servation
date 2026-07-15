import api from "./client";

export function getHotels(city) {
  return api.get("/hotels", { params: city ? { city } : {} }).then((res) => res.data);
}

export function getHotel(id) {
  return api.get(`/hotels/${id}`).then((res) => res.data);
}

export function createHotel(hotel) {
  return api.post("/hotels", hotel).then((res) => res.data);
}

export function updateHotel(id, hotel) {
  return api.put(`/hotels/${id}`, hotel).then((res) => res.data);
}

export function deleteHotel(id) {
  return api.delete(`/hotels/${id}`).then((res) => res.data);
}
