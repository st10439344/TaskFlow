const { isValidEmail, isValidPassword, validateRegister, validateLogin, parseTaskFields } = require('../src/utils/validators');

describe('email / password rules', () => {
  test('accepts a normal email', () => expect(isValidEmail('thabo@example.com')).toBe(true));
  test.each(['', 'abc', 'a@b', 'a b@c.com', null, 42])('rejects %p', (v) => expect(isValidEmail(v)).toBe(false));
  test('password needs 8+ chars, a letter and a number', () => {
    expect(isValidPassword('Passw0rd1')).toBe(true);
    expect(isValidPassword('short1')).toBe(false);
    expect(isValidPassword('onlyletters')).toBe(false);
    expect(isValidPassword('12345678')).toBe(false);
  });
});

describe('validateRegister / validateLogin', () => {
  test('valid registration has no errors', () => {
    expect(validateRegister({ fullName: 'Thabo Nkosi', email: 't@x.com', password: 'Passw0rd1' })).toEqual({});
  });
  test('reports every bad field', () => {
    const e = validateRegister({ fullName: 'T', email: 'nope', password: 'x' });
    expect(Object.keys(e).sort()).toEqual(['email', 'fullName', 'password']);
  });
  test('login needs email and password', () => {
    expect(Object.keys(validateLogin({}))).toEqual(['email', 'password']);
  });
});

describe('parseTaskFields', () => {
  test('requires a title on create', () => expect(parseTaskFields({}).errors.title).toBeDefined());
  test('partial update does not require a title', () => expect(parseTaskFields({ isComplete: true }, { partial: true }).errors).toEqual({}));
  test('rejects bad priority, time and subtasks', () => {
    const { errors } = parseTaskFields({ title: 'x', priority: 'urgent', dueTime: '25:99', subtasks: [{ title: '' }] });
    expect(Object.keys(errors).sort()).toEqual(['dueTime', 'priority', 'subtasks']);
  });
  test('whitelists fields and trims', () => {
    const { data } = parseTaskFields({ title: '  Hi  ', priority: 'high', dueTime: '16:00', hacker: 'x', subtasks: [{ title: ' a ', isComplete: true }] });
    expect(data).toEqual({ title: 'Hi', priority: 'high', dueTime: '16:00', subtasks: [{ title: 'a', isComplete: true }] });
  });
});
