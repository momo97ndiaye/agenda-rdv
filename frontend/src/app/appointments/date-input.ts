/** Convertit une date ISO en valeur pour <input type="datetime-local"> (heure locale). */
export function toLocalInput(iso: string): string {
  const d = new Date(iso);
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

/** Convertit la valeur d'un <input type="datetime-local"> en date ISO UTC pour l'API. */
export function fromLocalInput(value: string): string {
  return new Date(value).toISOString();
}
