const { applyCompletion, currentStats, dayKey, weekKey } = require('../src/utils/streak');

const fresh = () => ({ streakCount: 0, weeklyCompletedCount: 0, lastCompletedDay: null, weekKey: null });
const at = (iso) => new Date(iso);

describe('streak logic (SAST days)', () => {
  test('first completion starts a streak of 1', () => {
    const u = applyCompletion(fresh(), at('2026-08-24T08:00:00Z'));
    expect(u.streakCount).toBe(1);
    expect(u.weeklyCompletedCount).toBe(1);
  });
  test('second completion on the same day does not increase the streak', () => {
    const u = fresh();
    applyCompletion(u, at('2026-08-24T08:00:00Z'));
    applyCompletion(u, at('2026-08-24T12:00:00Z'));
    expect(u.streakCount).toBe(1);
    expect(u.weeklyCompletedCount).toBe(2);
  });
  test('completion on consecutive days extends the streak', () => {
    const u = fresh();
    applyCompletion(u, at('2026-08-24T08:00:00Z'));
    applyCompletion(u, at('2026-08-25T08:00:00Z'));
    expect(u.streakCount).toBe(2);
  });
  test('missing a day resets the streak to 1', () => {
    const u = fresh();
    applyCompletion(u, at('2026-08-24T08:00:00Z'));
    applyCompletion(u, at('2026-08-27T08:00:00Z'));
    expect(u.streakCount).toBe(1);
  });
  test('weekly count resets on a new week (Monday)', () => {
    const u = fresh();
    applyCompletion(u, at('2026-08-28T08:00:00Z')); // Friday
    applyCompletion(u, at('2026-08-31T08:00:00Z')); // next Monday
    expect(u.weeklyCompletedCount).toBe(1);
  });
  test('SAST boundary: 22:30 UTC is already the next day in South Africa', () => {
    expect(dayKey(at('2026-08-24T22:30:00Z'))).toBe('2026-08-25');
  });
  test('weekKey returns the Monday', () => expect(weekKey(at('2026-08-27T10:00:00Z'))).toBe('2026-08-24'));
  test('currentStats shows 0 if the streak has lapsed', () => {
    const u = fresh();
    applyCompletion(u, at('2026-08-24T08:00:00Z'));
    expect(currentStats(u, at('2026-08-31T08:00:00Z'))).toEqual({ streakCount: 0, weeklyCompletedCount: 0 });
  });
});
