const express = require('express');
const mongoose = require('mongoose');
const auth = require('../middleware/auth');
const Task = require('../models/Task');
const TaskList = require('../models/TaskList');
const { parseTaskFields } = require('../utils/validators');
const { applyCompletion, currentStats } = require('../utils/streak');

const router = express.Router();
router.use(auth);

// GET /api/tasks?listId=&isComplete=
router.get('/', async (req, res, next) => {
  try {
    const filter = { userId: req.user._id };
    if (req.query.listId) {
      if (!mongoose.isValidObjectId(req.query.listId)) return res.status(400).json({ message: 'Invalid listId' });
      filter.listId = req.query.listId;
    }
    if (req.query.isComplete === 'true' || req.query.isComplete === 'false') {
      filter.isComplete = req.query.isComplete === 'true';
    }
    const tasks = await Task.find(filter).sort({ isComplete: 1, dueDate: 1, createdAt: -1 });
    res.json({ tasks, stats: currentStats(req.user) });
  } catch (err) {
    next(err);
  }
});

// GET /api/tasks/:id
router.get('/:id', async (req, res, next) => {
  try {
    if (!mongoose.isValidObjectId(req.params.id)) return res.status(400).json({ message: 'Invalid task id' });
    const task = await Task.findOne({ _id: req.params.id, userId: req.user._id });
    if (!task) return res.status(404).json({ message: 'Task not found' });
    res.json({ task });
  } catch (err) {
    next(err);
  }
});

// POST /api/tasks   (also creates subtasks: body.subtasks = [{title,isComplete}])
router.post('/', async (req, res, next) => {
  try {
    const { errors, data } = parseTaskFields(req.body);
    if (!mongoose.isValidObjectId(req.body.listId)) errors.listId = 'A valid listId is required';
    if (Object.keys(errors).length) return res.status(400).json({ message: 'Validation failed', errors });

    const list = await TaskList.findOne({ _id: req.body.listId, userId: req.user._id });
    if (!list) return res.status(404).json({ message: 'List not found' });

    const task = new Task({ ...data, listId: list._id, userId: req.user._id });
    if (typeof req.body.clientId === 'string') task.clientId = req.body.clientId;
    if (task.isComplete) {
      task.completedAt = new Date();
      applyCompletion(req.user);
      await req.user.save();
    }
    await task.save();
    res.status(201).json({ task, stats: currentStats(req.user) });
  } catch (err) {
    if (err.code === 11000) return res.status(409).json({ message: 'Task already exists (duplicate clientId)' });
    next(err);
  }
});

// PUT /api/tasks/:id   (edit, complete/uncomplete, change list)
router.put('/:id', async (req, res, next) => {
  try {
    if (!mongoose.isValidObjectId(req.params.id)) return res.status(400).json({ message: 'Invalid task id' });
    const { errors, data } = parseTaskFields(req.body, { partial: true });
    if (req.body.listId !== undefined && !mongoose.isValidObjectId(req.body.listId)) errors.listId = 'Invalid listId';
    if (Object.keys(errors).length) return res.status(400).json({ message: 'Validation failed', errors });

    const task = await Task.findOne({ _id: req.params.id, userId: req.user._id });
    if (!task) return res.status(404).json({ message: 'Task not found' });

    if (req.body.listId !== undefined) {
      const list = await TaskList.findOne({ _id: req.body.listId, userId: req.user._id });
      if (!list) return res.status(404).json({ message: 'List not found' });
      task.listId = list._id;
    }

    const wasComplete = task.isComplete;
    Object.assign(task, data);
    if (data.isComplete !== undefined && data.isComplete !== wasComplete) {
      if (data.isComplete) {
        task.completedAt = new Date();
        applyCompletion(req.user); // streak + weekly count (FR11)
        await req.user.save();
      } else {
        task.completedAt = null;
      }
    }
    await task.save();
    res.json({ task, stats: currentStats(req.user) });
  } catch (err) {
    next(err);
  }
});

// DELETE /api/tasks/:id
router.delete('/:id', async (req, res, next) => {
  try {
    if (!mongoose.isValidObjectId(req.params.id)) return res.status(400).json({ message: 'Invalid task id' });
    const task = await Task.findOneAndDelete({ _id: req.params.id, userId: req.user._id });
    if (!task) return res.status(404).json({ message: 'Task not found' });
    res.json({ message: 'Task deleted' });
  } catch (err) {
    next(err);
  }
});

module.exports = router;
