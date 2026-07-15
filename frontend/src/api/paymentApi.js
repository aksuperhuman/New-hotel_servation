import api from "./client";

export function chargePayment(paymentRequest) {
  return api.post("/payments/charge", paymentRequest).then((res) => res.data);
}
