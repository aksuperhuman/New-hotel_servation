export function statusBadgeClass(status) {
  return `badge badge-${(status || "").toLowerCase().replace(/_/g, "-")}`;
}

export function formatMoney(amount) {
  if (amount === null || amount === undefined) return "-";
  return `$${Number(amount).toFixed(2)}`;
}

export function formatDate(dateStr) {
  if (!dateStr) return "-";
  return new Date(dateStr).toLocaleDateString(undefined, {
    year: "numeric",
    month: "short",
    day: "numeric",
  });
}

export function nightsBetween(checkIn, checkOut) {
  if (!checkIn || !checkOut) return 0;
  const start = new Date(checkIn);
  const end = new Date(checkOut);
  return Math.max(0, Math.round((end - start) / (1000 * 60 * 60 * 24)));
}
