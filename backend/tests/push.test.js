const { MILESTONES, isMilestone, shouldNotifyStreak, buildData, sendToUser } = require('../src/services/push');

describe('push helpers', () => {
  test('milestones are recognised', () => {
    MILESTONES.forEach((m) => expect(isMilestone(m)).toBe(true));
    [0, 1, 2, 4, 8].forEach((n) => expect(isMilestone(n)).toBe(false));
  });

  test('streak alert only when the streak changed and hit a milestone', () => {
    expect(shouldNotifyStreak(2, 3)).toBe(true);
    expect(shouldNotifyStreak(6, 7)).toBe(true);
    expect(shouldNotifyStreak(3, 3)).toBe(false); // same day, no change
    expect(shouldNotifyStreak(3, 4)).toBe(false); // not a milestone
    expect(shouldNotifyStreak(9, 1)).toBe(false); // streak reset
  });

  test('FCM data payload contains only strings', () => {
    expect(buildData('streak', { count: 7 })).toEqual({ type: 'streak', count: '7' });
    expect(buildData('sync')).toEqual({ type: 'sync' });
  });
});

describe('sendToUser (no Firebase credentials needed)', () => {
  const user = (over = {}) => ({ fcmTokens: [], notificationsEnabled: true, ...over });

  test('does nothing when the user has no registered devices', async () => {
    expect(await sendToUser(user(), 'test')).toMatchObject({ sent: 0, reason: 'no-devices' });
  });

  test('skips alerts when notifications are switched off in Settings', async () => {
    const res = await sendToUser(user({ fcmTokens: ['a'.repeat(30)], notificationsEnabled: false }), 'streak', { count: 3 });
    expect(res.reason).toBe('notifications-disabled');
  });

  test('never sends to the device that made the change', async () => {
    const token = 'b'.repeat(30);
    const res = await sendToUser(user({ fcmTokens: [token] }), 'sync', {}, { excludeToken: token, respectPreference: false });
    expect(res.reason).toBe('no-devices');
  });

  test('reports "not-configured" when FIREBASE_SERVICE_ACCOUNT is missing', async () => {
    delete process.env.FIREBASE_SERVICE_ACCOUNT;
    const res = await sendToUser(user({ fcmTokens: ['c'.repeat(30)] }), 'test', {}, { respectPreference: false });
    expect(res.reason).toBe('not-configured');
  });
});
