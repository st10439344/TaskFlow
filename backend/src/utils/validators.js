const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;

function isValidEmail(email) {
  return typeof email === 'string' && EMAIL_RE.test(email.trim());
}

// min 8 chars, at least one letter and one number
function isValidPassword(pw) {
  return typeof pw === 'string' && pw.length >= 8 && pw.length <= 72 && /[A-Za-z]/.test(pw) && /\d/.test(pw);
}

function validateRegister({ fullName, email, password } = {}) {
  const errors = {};
  if (typeof fullName !== 'string' || fullName.trim().length < 2) errors.fullName = 'Full name must be at least 2 characters';
  if (!isValidEmail(email)) errors.email = 'Enter a valid email address';
  if (!isValidPassword(password)) errors.password = 'Password must be 8+ characters with at least one letter and one number';
  return errors;
}

function validateLogin({ email, password } = {}) {
  const errors = {};
  if (!isValidEmail(email)) errors.email = 'Enter a valid email address';
  if (typeof password !== 'string' || password.length === 0) errors.password = 'Password is required';
  return errors;
}

const PRIORITIES = ['low', 'med', 'high'];
const REPEATS = ['none', 'daily', 'weekly', 'monthly'];
const TIME_RE = /^([01]\d|2[0-3]):[0-5]\d$/;

/**
 * Validates and whitelists task fields. Returns { errors, data }.
 * When partial=true, missing fields are simply skipped (used for PUT).
 */
function parseTaskFields(body = {}, { partial = false } = {}) {
  const errors = {};
  const data = {};

  if (body.title !== undefined || !partial) {
    if (typeof body.title !== 'string' || body.title.trim().length < 1 || body.title.length > 200) {
      errors.title = 'Title is required (max 200 characters)';
    } else data.title = body.title.trim();
  }
  if (body.description !== undefined) {
    if (typeof body.description !== 'string' || body.description.length > 2000) errors.description = 'Description too long';
    else data.description = body.description;
  }
  if (body.priority !== undefined) {
    if (!PRIORITIES.includes(body.priority)) errors.priority = 'Priority must be low, med or high';
    else data.priority = body.priority;
  }
  if (body.repeatRule !== undefined) {
    if (!REPEATS.includes(body.repeatRule)) errors.repeatRule = 'Invalid repeat rule';
    else data.repeatRule = body.repeatRule;
  }
  if (body.dueDate !== undefined) {
    if (body.dueDate === null) data.dueDate = null;
    else {
      const d = new Date(body.dueDate);
      if (isNaN(d.getTime())) errors.dueDate = 'Invalid due date';
      else data.dueDate = d;
    }
  }
  if (body.dueTime !== undefined) {
    if (body.dueTime === null) data.dueTime = null;
    else if (typeof body.dueTime !== 'string' || !TIME_RE.test(body.dueTime)) errors.dueTime = 'Due time must be HH:mm';
    else data.dueTime = body.dueTime;
  }
  if (body.isComplete !== undefined) {
    if (typeof body.isComplete !== 'boolean') errors.isComplete = 'isComplete must be true or false';
    else data.isComplete = body.isComplete;
  }
  if (body.subtasks !== undefined) {
    if (!Array.isArray(body.subtasks) || body.subtasks.length > 50) errors.subtasks = 'Subtasks must be an array (max 50)';
    else {
      const ok = body.subtasks.every((s) => s && typeof s.title === 'string' && s.title.trim().length > 0);
      if (!ok) errors.subtasks = 'Every subtask needs a title';
      else data.subtasks = body.subtasks.map((s) => ({ title: s.title.trim(), isComplete: !!s.isComplete }));
    }
  }
  return { errors, data };
}

module.exports = { isValidEmail, isValidPassword, validateRegister, validateLogin, parseTaskFields };
