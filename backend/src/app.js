const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const rateLimit = require('express-rate-limit');
const { notFound, errorHandler } = require('./middleware/errorHandler');

const app = express();
app.set('trust proxy', 1); // Render sits behind a proxy
app.use(helmet());
app.use(cors());
app.use(express.json({ limit: '1mb' }));

// Health check (also handy for waking the free Render instance before a demo)
app.get('/', (_req, res) => res.json({ name: 'TaskFlow API', status: 'ok' }));
app.get('/health', (_req, res) => res.json({ status: 'ok', time: new Date().toISOString() }));

const authLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 100,
  standardHeaders: true,
  legacyHeaders: false,
  message: { message: 'Too many attempts, please try again later' }
});

app.use('/api/auth', authLimiter, require('./routes/auth'));
app.use('/api/users', require('./routes/users'));
app.use('/api/lists', require('./routes/lists'));
app.use('/api/tasks', require('./routes/tasks'));
app.use('/api/sync', require('./routes/sync'));
app.use('/api/devices', require('./routes/devices'));

app.use(notFound);
app.use(errorHandler);

module.exports = app;
