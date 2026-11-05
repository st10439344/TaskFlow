
/**
 * Push notification service using Firebase Cloud Messaging.
 *
 * TaskFlow sends data-only messages, allowing Android to display
 * notifications in English, isiZulu, or Afrikaans.
 *
 * Message types:
 *   streak   - streak milestone notification
 *   all_done - all tasks completed notification
 *   test     - test notification from Settings
 *   sync     - silent notification for other devices
 *
 * Firebase credentials are loaded from FIREBASE_SERVICE_ACCOUNT
 * in the backend .env file.
 */

const User = require('../models/User');
const Task = require('../models/Task');

const MILESTONES = [3, 7, 14, 30, 60, 100];
const MAX_DEVICES = 5;
const ALL_DONE_COOLDOWN_MS = 6 * 60 * 60 * 1000;

const DEAD_TOKEN_CODES = [
  'messaging/registration-token-not-registered',
  'messaging/invalid-registration-token'
];

let messaging = null;
let initTried = false;

/**
 * Load Firebase service-account credentials from environment.
 * Supports JSON or Base64-encoded JSON.
 */
function loadCredentials() {
  const raw = process.env.FIREBASE_SERVICE_ACCOUNT;

  if (!raw) {
    return null;
  }

  try {
    const text = raw.trim().startsWith('{')
      ? raw
      : Buffer.from(raw, 'base64').toString('utf8');

    return JSON.parse(text);
  } catch (err) {
    console.error(
      '[push] FIREBASE_SERVICE_ACCOUNT is not valid JSON or Base64 JSON'
    );
    return null;
  }
}

/**
 * Initialise Firebase Admin SDK.
 *
 * FIX: Use the modular Firebase Admin imports instead of
 * admin.credential.cert(), which caused the startup error.
 */
function getMessaging() {
  if (initTried) {
    return messaging;
  }

  initTried = true;

  const credentials = loadCredentials();

  if (!credentials) {
    console.warn(
      '[push] FIREBASE_SERVICE_ACCOUNT not set - push notifications are disabled'
    );
    return null;
  }

  try {
    const {
      initializeApp,
      cert,
      getApps
    } = require('firebase-admin/app');

    const {
      getMessaging: firebaseGetMessaging
    } = require('firebase-admin/messaging');

    if (getApps().length === 0) {
      initializeApp({
        credential: cert(credentials)
      });
    }

    messaging = firebaseGetMessaging();

    console.log('[push] Firebase Admin initialised');
  } catch (err) {
    console.error(
      '[push] Firebase Admin failed to start:',
      err.message
    );
  }

  return messaging;
}

// ------------------------------------------------------
// Helpers
// ------------------------------------------------------

function isMilestone(n) {
  return MILESTONES.includes(n);
}

/**
 * Notify only when the streak has changed
 * and reaches a milestone.
 */
function shouldNotifyStreak(before, after) {
  return before !== after && isMilestone(after);
}

/**
 * Firebase data payload values must all be strings.
 */
function buildData(type, extra = {}) {
  const data = {
    type: String(type)
  };

  for (const [key, value] of Object.entries(extra)) {
    data[key] = String(value);
  }

  return data;
}

// ------------------------------------------------------
// Send push notifications
// ------------------------------------------------------

/**
 * Send a Firebase message to every registered device
 * belonging to the user.
 */
async function sendToUser(
  user,
  type,
  extra = {},
  {
    excludeToken = null,
    respectPreference = true
  } = {}
) {
  const tokens = (user.fcmTokens || []).filter(
    (token) => token && token !== excludeToken
  );

  if (!tokens.length) {
    return {
      sent: 0,
      failed: 0,
      reason: 'no-devices'
    };
  }

  if (
    respectPreference &&
    user.notificationsEnabled === false
  ) {
    return {
      sent: 0,
      failed: 0,
      reason: 'notifications-disabled'
    };
  }

  const fcm = getMessaging();

  if (!fcm) {
    return {
      sent: 0,
      failed: 0,
      reason: 'not-configured'
    };
  }

  try {
    const response = await fcm.sendEachForMulticast({
      tokens,
      data: buildData(type, extra),
      android: {
        priority: 'high'
      }
    });

    response.responses.forEach(r => { if (!r.success) console.error('[push] FCM ERROR:', r.error?.code, r.error?.message); });
    const dead = [];

    response.responses.forEach((result, index) => {
      if (
        !result.success &&
        result.error &&
        DEAD_TOKEN_CODES.includes(result.error.code)
      ) {
        dead.push(tokens[index]);
      }
    });

    if (dead.length) {
      await User.updateOne(
        { _id: user._id },
        {
          $pull: {
            fcmTokens: {
              $in: dead
            }
          }
        }
      );
    }

    console.log(
      `[push] ${type}: ${response.successCount} sent, ${response.failureCount} failed`
    );

    return {
      sent: response.successCount,
      failed: response.failureCount
    };
  } catch (err) {
    console.error(
      '[push] send failed:',
      err.message
    );

    return {
      sent: 0,
      failed: tokens.length,
      reason: 'error'
    };
  }
}

/**
 * Send a silent sync message to other devices.
 */
function notifySync(user, excludeToken) {
  return sendToUser(
    user,
    'sync',
    {},
    {
      excludeToken,
      respectPreference: false
    }
  ).catch(() => {});
}

/**
 * Check for streak milestones and all-tasks-complete
 * notifications after completing a task.
 */
async function afterCompletion(user, streakBefore) {
  try {
    if (
      shouldNotifyStreak(
        streakBefore,
        user.streakCount
      )
    ) {
      await sendToUser(
        user,
        'streak',
        {
          count: user.streakCount
        }
      );
    }

    const open = await Task.countDocuments({
      userId: user._id,
      isComplete: false
    });

    const last = user.lastAllDonePush
      ? user.lastAllDonePush.getTime()
      : 0;

    if (
      open === 0 &&
      Date.now() - last > ALL_DONE_COOLDOWN_MS
    ) {
      user.lastAllDonePush = new Date();

      await user.save();

      await sendToUser(
        user,
        'all_done'
      );
    }
  } catch (err) {
    console.error(
      '[push] afterCompletion failed:',
      err.message
    );
  }
}

// ------------------------------------------------------
// Exports
// ------------------------------------------------------

module.exports = {
  MILESTONES,
  MAX_DEVICES,
  isMilestone,
  shouldNotifyStreak,
  buildData,
  sendToUser,
  notifySync,
  afterCompletion
};
