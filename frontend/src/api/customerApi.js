import api from "./client";

export function getCustomers() {
  return api.get("/customers").then((res) => res.data);
}

export function deleteCustomer(id) {
  return api.delete(`/customers/${id}`).then((res) => res.data);
}
