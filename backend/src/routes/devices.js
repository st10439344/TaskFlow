const express = require('express');
const auth = require('../middleware/auth');
const User = require('../models/User');
const { sendToUser, MAX_DEVICES } = require('../services/push');

const router = express.Router();
router.use(auth);

function validToken(token) {
  return typeof token === 'string' && token.length >= 20 && token.length <= 4096;
}

// POST /api/devices/token   body: { fcmToken }   - register this phone for push notifications
router.post('/token', async (req, res, next) => {
  try {
    const { fcmToken } = req.body || {};
    if (!validToken(fcmToken)) return res.status(400).json({ message: 'A valid fcmToken is required' });

    // A device belongs to one account at a time (matters when two people share a phone).
    await User.updateMany({ _id: { $ne: req.user._id }, fcmTokens: fcmToken }, { $pull: { fcmTokens: fcmToken } });

    const tokens = req.user.fcmTokens.filter((t) => t !== fcmToken);
    tokens.push(fcmToken);
    req.user.fcmTokens = tokens.slice(-MAX_DEVICES);
    await req.user.save();
    console.log(`[devices] token registered (${req.user.fcmTokens.length} device(s))`);
    res.json({ message: 'Device registered for notifications' });
  } catch (err) {
    next(err);
  }
});

// DELETE /api/devices/token   body: { fcmToken }   - called on log out
router.delete('/token', async (req, res, next) => {
  try {
    const { fcmToken } = req.body || {};
    if (!validToken(fcmToken)) return res.status(400).json({ message: 'A valid fcmToken is required' });
    req.user.fcmTokens = req.user.fcmTokens.filter((t) => t !== fcmToken);
    await req.user.save();
    res.json({ message: 'Device unregistered' });
  } catch (err) {
    next(err);
  }
});

// POST /api/devices/test   - sends a test notification to the caller's devices (used in the demo)
router.post('/test', async (req, res, next) => {
  try {
    const result = await sendToUser(req.user, 'test', {}, { respectPreference: false });
    if (result.reason === 'not-configured') return res.status(503).json({ message: 'Push notifications are not configured on the server' });
    if (result.reason === 'no-devices') return res.status(400).json({ message: 'No device is registered for notifications' });
    res.json({ message: 'Test notification sent', sent: result.sent, failed: result.failed });
  } catch (err) {
    next(err);
  }
});

module.exports = router;
