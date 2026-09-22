const express = require('express');
const bcrypt = require('bcryptjs');
const auth = require('../middleware/auth');
const { isValidPassword } = require('../utils/validators');
const { currentStats } = require('../utils/streak');

const router = express.Router();
router.use(auth);

const LANGS = ['en', 'zu', 'af'];
const THEMES = ['light', 'dark'];

function profile(user) {
  return { ...user.toJSON(), ...currentStats(user) };
}

// GET /api/users/me
router.get('/me', (req, res) => res.json({ user: profile(req.user) }));

// PUT /api/users/me   (settings: name, language, theme, notifications)
router.put('/me', async (req, res, next) => {
  try {
    const { fullName, preferredLanguage, theme, notificationsEnabled } = req.body || {};
    const errors = {};
    const u = req.user;

    if (fullName !== undefined) {
      if (typeof fullName !== 'string' || fullName.trim().length < 2) errors.fullName = 'Full name must be at least 2 characters';
      else u.fullName = fullName.trim();
    }
    if (preferredLanguage !== undefined) {
      if (!LANGS.includes(preferredLanguage)) errors.preferredLanguage = 'Language must be en, zu or af';
      else u.preferredLanguage = preferredLanguage;
    }
    if (theme !== undefined) {
      if (!THEMES.includes(theme)) errors.theme = 'Theme must be light or dark';
      else u.theme = theme;
    }
    if (notificationsEnabled !== undefined) {
      if (typeof notificationsEnabled !== 'boolean') errors.notificationsEnabled = 'Must be true or false';
      else u.notificationsEnabled = notificationsEnabled;
    }
    if (Object.keys(errors).length) return res.status(400).json({ message: 'Validation failed', errors });

    await u.save();
    res.json({ user: profile(u) });
  } catch (err) {
    next(err);
  }
});

// PUT /api/users/me/password   body: { currentPassword, newPassword }
router.put('/me/password', async (req, res, next) => {
  try {
    const { currentPassword, newPassword } = req.body || {};
    const u = req.user;
    if (!u.passwordHash) return res.status(400).json({ message: 'This account signs in with Google and has no password' });
    if (typeof currentPassword !== 'string' || !(await bcrypt.compare(currentPassword, u.passwordHash))) {
      return res.status(401).json({ message: 'Current password is incorrect' });
    }
    if (!isValidPassword(newPassword)) {
      return res.status(400).json({ message: 'Password must be 8+ characters with at least one letter and one number' });
    }
    u.passwordHash = await bcrypt.hash(newPassword, 12);
    await u.save();
    res.json({ message: 'Password updated' });
  } catch (err) {
    next(err);
  }
});

module.exports = router;
