import api from "./client";

export function login({ email, password }) {
  return api.post("/api/auth/login", { email, password }).then((res) => res.data);
}

export function register({ name, email, password, role }) {
  return api.post("/api/auth/register", { name, email, password, role }).then((res) => res.data);
}
