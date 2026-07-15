import axios from "axios";

// Backend runs on :8081 per application.properties (server.port=8081)
export const API_BASE_URL = "http://localhost:8081";

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

// Attach the JWT (if present) to every outgoing request.
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Centralize 401 handling: drop the stale session and bounce to login.
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem("token");
      localStorage.removeItem("user");
      if (window.location.pathname !== "/login") {
        window.location.href = "/login";
      }
    }
    return Promise.reject(error);
  }
);

/**
 * Pulls a human-readable message out of a backend ErrorResponse
 * (see GlobalExceptionHandler), falling back sensibly if the
 * response doesn't look like one (network error, etc).
 */
export function extractErrorMessage(error, fallback = "Something went wrong. Please try again.") {
  if (error?.response?.data?.message) {
    return error.response.data.message;
  }
  if (error?.response?.data?.fieldErrors) {
    const firstField = Object.values(error.response.data.fieldErrors)[0];
    if (firstField) return firstField;
  }
  if (error?.message) {
    return error.message;
  }
  return fallback;
}

export default api;
