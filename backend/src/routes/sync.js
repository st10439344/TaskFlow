const express = require('express');
const mongoose = require('mongoose');
const auth = require('../middleware/auth');
const Task = require('../models/Task');
const TaskList = require('../models/TaskList');
const { parseTaskFields } = require('../utils/validators');
const { applyCompletion, currentStats } = require('../utils/streak');

const router = express.Router();
router.use(auth);

const MAX_CHANGES = 200;

/**
 * POST /api/sync
 * body: { tasks: [ { clientId, listId, updatedAt, deleted?, ...taskFields } ] }
 * Applies queued offline changes. Idempotent (keyed by clientId) and last-write-wins on updatedAt.
 * Returns one result per change so the app can mark each Room row as synced.
 */
router.post('/', async (req, res, next) => {
  try {
    const changes = req.body && req.body.tasks;
    if (!Array.isArray(changes)) return res.status(400).json({ message: 'tasks must be an array' });
    if (changes.length > MAX_CHANGES) return res.status(400).json({ message: `Max ${MAX_CHANGES} changes per sync` });

    const ownedLists = new Set((await TaskList.find({ userId: req.user._id }, '_id')).map((l) => l._id.toString()));
    const results = [];
    let streakTouched = false;

    for (const c of changes) {
      if (!c || typeof c.clientId !== 'string' || !c.clientId) {
        results.push({ clientId: null, status: 'error', message: 'clientId is required' });
        continue;
      }
      const existing = await Task.findOne({ userId: req.user._id, clientId: c.clientId });

      if (c.deleted === true) {
        if (existing) await existing.deleteOne();
        results.push({ clientId: c.clientId, status: 'deleted' });
        continue;
      }

      const { errors, data } = parseTaskFields(c, { partial: !!existing });
      if (!existing && !ownedLists.has(String(c.listId))) errors.listId = 'A valid listId is required';
      if (existing && c.listId !== undefined && !ownedLists.has(String(c.listId))) errors.listId = 'Invalid listId';
      if (Object.keys(errors).length) {
        results.push({ clientId: c.clientId, status: 'error', errors });
        continue;
      }

      const updatedAt = c.updatedAt ? new Date(c.updatedAt) : new Date();
      if (isNaN(updatedAt.getTime())) {
        results.push({ clientId: c.clientId, status: 'error', message: 'Invalid updatedAt' });
        continue;
      }

      if (existing) {
        if (existing.clientUpdatedAt && existing.clientUpdatedAt > updatedAt) {
          results.push({ clientId: c.clientId, status: 'conflict', task: existing }); // server copy is newer
          continue;
        }
        const wasComplete = existing.isComplete;
        Object.assign(existing, data);
        if (c.listId !== undefined) existing.listId = c.listId;
        if (data.isComplete !== undefined && data.isComplete !== wasComplete) {
          existing.completedAt = data.isComplete ? updatedAt : null;
          if (data.isComplete) { applyCompletion(req.user, updatedAt); streakTouched = true; }
        }
        existing.clientUpdatedAt = updatedAt;
        await existing.save();
        results.push({ clientId: c.clientId, status: 'updated', serverId: existing.id, updatedAt: existing.updatedAt });
      } else {
        const task = new Task({ ...data, listId: c.listId, userId: req.user._id, clientId: c.clientId, clientUpdatedAt: updatedAt });
        if (task.isComplete) {
          task.completedAt = updatedAt;
          applyCompletion(req.user, updatedAt);
          streakTouched = true;
        }
        await task.save();
        results.push({ clientId: c.clientId, status: 'created', serverId: task.id, updatedAt: task.updatedAt });
      }
    }

    if (streakTouched) await req.user.save();
    res.json({ results, stats: currentStats(req.user) });
  } catch (err) {
    next(err);
  }
});

module.exports = router;
