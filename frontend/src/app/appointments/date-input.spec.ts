import { fromLocalInput, toLocalInput } from './date-input';

describe('date-input', () => {
  it('fait l’aller-retour entre ISO et datetime-local', () => {
    const iso = new Date(2026, 11, 1, 9, 30).toISOString();
    expect(toLocalInput(iso)).toBe('2026-12-01T09:30');
    expect(fromLocalInput('2026-12-01T09:30')).toBe(iso);
  });
});
