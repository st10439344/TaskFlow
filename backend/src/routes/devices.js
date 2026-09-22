const express = require('express');
const auth = require('../middleware/auth');

const router = express.Router();
router.use(auth);

// POST /api/devices/token   body: { fcmToken }
router.post('/token', async (req, res, next) => {
  try {
    const { fcmToken } = req.body || {};
    if (typeof fcmToken !== 'string' || fcmToken.length < 10) return res.status(400).json({ message: 'A valid fcmToken is required' });
    req.user.fcmToken = fcmToken;
    await req.user.save();
    res.json({ message: 'Device token saved' });
  } catch (err) {
    next(err);
  }
});

module.exports = router;
