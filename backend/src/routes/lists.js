const express = require('express');
const mongoose = require('mongoose');
const auth = require('../middleware/auth');
const TaskList = require('../models/TaskList');
const Task = require('../models/Task');

const router = express.Router();
router.use(auth);

const HEX_RE = /^#[0-9a-fA-F]{6}$/;

function parseList(body = {}) {
  const errors = {};
  const data = {};
  if (typeof body.name !== 'string' || body.name.trim().length < 1 || body.name.length > 60) {
    errors.name = 'List name is required (max 60 characters)';
  } else data.name = body.name.trim();
  if (body.colorTag !== undefined && body.colorTag !== null) {
    if (!HEX_RE.test(body.colorTag)) errors.colorTag = 'colorTag must be a hex colour like #2D9B6F';
    else data.colorTag = body.colorTag;
  }
  return { errors, data };
}

// GET /api/lists
router.get('/', async (req, res, next) => {
  try {
    const lists = await TaskList.find({ userId: req.user._id }).sort({ createdAt: 1 });
    res.json({ lists });
  } catch (err) {
    next(err);
  }
});

// POST /api/lists
router.post('/', async (req, res, next) => {
  try {
    const { errors, data } = parseList(req.body);
    if (Object.keys(errors).length) return res.status(400).json({ message: 'Validation failed', errors });
    const list = await TaskList.create({ ...data, userId: req.user._id });
    res.status(201).json({ list });
  } catch (err) {
    next(err);
  }
});

// PUT /api/lists/:id  (rename / recolour)
router.put('/:id', async (req, res, next) => {
  try {
    if (!mongoose.isValidObjectId(req.params.id)) return res.status(400).json({ message: 'Invalid list id' });
    const { errors, data } = parseList(req.body);
    if (Object.keys(errors).length) return res.status(400).json({ message: 'Validation failed', errors });
    const list = await TaskList.findOneAndUpdate({ _id: req.params.id, userId: req.user._id }, data, { new: true });
    if (!list) return res.status(404).json({ message: 'List not found' });
    res.json({ list });
  } catch (err) {
    next(err);
  }
});

// DELETE /api/lists/:id  (also deletes the tasks inside it)
router.delete('/:id', async (req, res, next) => {
  try {
    if (!mongoose.isValidObjectId(req.params.id)) return res.status(400).json({ message: 'Invalid list id' });
    const list = await TaskList.findOneAndDelete({ _id: req.params.id, userId: req.user._id });
    if (!list) return res.status(404).json({ message: 'List not found' });
    const { deletedCount } = await Task.deleteMany({ listId: list._id, userId: req.user._id });
    res.json({ message: 'List deleted', tasksDeleted: deletedCount });
  } catch (err) {
    next(err);
  }
});

module.exports = router;
