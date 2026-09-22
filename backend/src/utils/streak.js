// Streaks are computed server-side (FR11). Days are calculated in South African time (UTC+2, no DST).
const SAST_OFFSET_MS = 2 * 60 * 60 * 1000;
const DAY_MS = 24 * 60 * 60 * 1000;

function dayKey(date) {
  return new Date(date.getTime() + SAST_OFFSET_MS).toISOString().slice(0, 10);
}

// Monday of the week containing `date`
function weekKey(date) {
  const d = new Date(date.getTime() + SAST_OFFSET_MS);
  const daysSinceMonday = (d.getUTCDay() + 6) % 7;
  d.setUTCDate(d.getUTCDate() - daysSinceMonday);
  return d.toISOString().slice(0, 10);
}

/** Call once each time a task goes from incomplete -> complete. Mutates and returns the user. */
function applyCompletion(user, now = new Date()) {
  const today = dayKey(now);
  const yesterday = dayKey(new Date(now.getTime() - DAY_MS));

  if (user.weekKey !== weekKey(now)) {
    user.weeklyCompletedCount = 0;
    user.weekKey = weekKey(now);
  }
  user.weeklyCompletedCount += 1;

  if (user.lastCompletedDay !== today) {
    user.streakCount = user.lastCompletedDay === yesterday ? user.streakCount + 1 : 1;
    user.lastCompletedDay = today;
  }
  return user;
}

/** Stats as they should be displayed right now (streak resets if a day was missed). */
function currentStats(user, now = new Date()) {
  const today = dayKey(now);
  const yesterday = dayKey(new Date(now.getTime() - DAY_MS));
  const alive = user.lastCompletedDay === today || user.lastCompletedDay === yesterday;
  const sameWeek = user.weekKey === weekKey(now);
  return {
    streakCount: alive ? user.streakCount : 0,
    weeklyCompletedCount: sameWeek ? user.weeklyCompletedCount : 0
  };
}

module.exports = { dayKey, weekKey, applyCompletion, currentStats };
