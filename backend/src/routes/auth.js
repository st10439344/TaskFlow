const express = require('express');
const bcrypt = require('bcryptjs');
const { OAuth2Client } = require('google-auth-library');
const User = require('../models/User');
const TaskList = require('../models/TaskList');
const { validateRegister, validateLogin } = require('../utils/validators');
const { signToken } = require('../utils/token');

const router = express.Router();
const BCRYPT_ROUNDS = 12;
const DEFAULT_LISTS = [
  { name: 'Personal', colorTag: '#2D9B6F' },
  { name: 'Work', colorTag: '#1F4E79' },
  { name: 'School', colorTag: '#E0A100' }
];

async function createDefaultLists(userId) {
  await TaskList.insertMany(DEFAULT_LISTS.map((l) => ({ ...l, userId })));
}

// POST /api/auth/register
router.post('/register', async (req, res, next) => {
  try {
    const errors = validateRegister(req.body);
    if (Object.keys(errors).length) return res.status(400).json({ message: 'Validation failed', errors });

    const email = req.body.email.trim().toLowerCase();
    if (await User.findOne({ email })) return res.status(409).json({ message: 'An account with this email already exists' });

    const passwordHash = await bcrypt.hash(req.body.password, BCRYPT_ROUNDS);
    const user = await User.create({
      fullName: req.body.fullName.trim(),
      email,
      passwordHash,
      authProvider: 'local'
    });
    await createDefaultLists(user._id);
    res.status(201).json({ user, token: signToken(user) });
  } catch (err) {
    next(err);
  }
});

// POST /api/auth/login  (generic error so attackers can't tell which part was wrong)
router.post('/login', async (req, res, next) => {
  try {
    const errors = validateLogin(req.body);
    if (Object.keys(errors).length) return res.status(400).json({ message: 'Validation failed', errors });

    const user = await User.findOne({ email: req.body.email.trim().toLowerCase() });
    const ok = user && user.passwordHash && (await bcrypt.compare(req.body.password, user.passwordHash));
    if (!ok) return res.status(401).json({ message: 'Invalid email or password' });

    res.json({ user, token: signToken(user) });
  } catch (err) {
    next(err);
  }
});

// POST /api/auth/google   body: { idToken }
router.post('/google', async (req, res, next) => {
  try {
    const { idToken } = req.body || {};
    if (typeof idToken !== 'string' || !idToken) return res.status(400).json({ message: 'idToken is required' });
    if (!process.env.GOOGLE_WEB_CLIENT_ID) return res.status(500).json({ message: 'Google sign-in is not configured' });

    const client = new OAuth2Client(process.env.GOOGLE_WEB_CLIENT_ID);
    let payload;
    try {
      const ticket = await client.verifyIdToken({ idToken, audience: process.env.GOOGLE_WEB_CLIENT_ID });
      payload = ticket.getPayload();
    } catch {
      return res.status(401).json({ message: 'Google sign-in failed' });
    }
    if (!payload || !payload.email || !payload.email_verified) {
      return res.status(401).json({ message: 'Google account email is not verified' });
    }

    const email = payload.email.toLowerCase();
    let user = await User.findOne({ email });
    let created = false;
    if (!user) {
      user = await User.create({
        fullName: payload.name || email.split('@')[0],
        email,
        authProvider: 'google',
        googleId: payload.sub
      });
      await createDefaultLists(user._id);
      created = true;
    } else if (!user.googleId) {
      user.googleId = payload.sub; // link Google to existing local account
      await user.save();
    }
    res.status(created ? 201 : 200).json({ user, token: signToken(user) });
  } catch (err) {
    next(err);
  }
});

module.exports = router;
