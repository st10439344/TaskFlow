const mongoose = require('mongoose');

const userSchema = new mongoose.Schema(
  {
    fullName: { type: String, required: true, trim: true },
    email: { type: String, required: true, unique: true, lowercase: true, trim: true },
    passwordHash: { type: String, default: null }, // null for Google-only accounts
    authProvider: { type: String, enum: ['local', 'google'], default: 'local' },
    googleId: { type: String, default: null },
    preferredLanguage: { type: String, enum: ['en', 'zu', 'af'], default: 'en' },
    notificationsEnabled: { type: Boolean, default: true },
    theme: { type: String, enum: ['light', 'dark'], default: 'light' },
    fcmToken: { type: String, default: null },
    streakCount: { type: Number, default: 0 },
    weeklyCompletedCount: { type: Number, default: 0 },
    lastCompletedDay: { type: String, default: null }, // 'YYYY-MM-DD' (SAST)
    weekKey: { type: String, default: null }           // Monday of current week
  },
  { timestamps: true }
);

userSchema.set('toJSON', {
  transform: (_doc, ret) => {
    ret.id = ret._id.toString();
    delete ret._id;
    delete ret.__v;
    delete ret.passwordHash;
    delete ret.googleId;
    delete ret.lastCompletedDay;
    delete ret.weekKey;
    return ret;
  }
});

module.exports = mongoose.model('User', userSchema);
