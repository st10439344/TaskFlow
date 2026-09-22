const mongoose = require('mongoose');

const subtaskSchema = new mongoose.Schema(
  {
    title: { type: String, required: true, trim: true, maxlength: 200 },
    isComplete: { type: Boolean, default: false }
  },
  { _id: true }
);

const taskSchema = new mongoose.Schema(
  {
    userId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true, index: true },
    listId: { type: mongoose.Schema.Types.ObjectId, ref: 'TaskList', required: true, index: true },
    clientId: { type: String, default: null },       // UUID created by Room (offline)
    clientUpdatedAt: { type: Date, default: null },  // used for last-write-wins in /sync
    title: { type: String, required: true, trim: true, maxlength: 200 },
    description: { type: String, default: '', maxlength: 2000 },
    dueDate: { type: Date, default: null },
    dueTime: { type: String, default: null },        // 'HH:mm'
    priority: { type: String, enum: ['low', 'med', 'high'], default: 'med' },
    repeatRule: { type: String, enum: ['none', 'daily', 'weekly', 'monthly'], default: 'none' },
    isComplete: { type: Boolean, default: false },
    completedAt: { type: Date, default: null },
    subtasks: { type: [subtaskSchema], default: [] }
  },
  { timestamps: true }
);

taskSchema.index({ userId: 1, clientId: 1 }, { unique: true, partialFilterExpression: { clientId: { $type: 'string' } } });

taskSchema.set('toJSON', {
  transform: (_doc, ret) => {
    ret.id = ret._id.toString();
    delete ret._id;
    delete ret.__v;
    if (ret.subtasks) {
      ret.subtasks = ret.subtasks.map((s) => ({ id: s._id.toString(), title: s.title, isComplete: s.isComplete }));
    }
    return ret;
  }
});

module.exports = mongoose.model('Task', taskSchema);
