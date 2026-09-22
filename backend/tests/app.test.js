process.env.JWT_SECRET = 'test-secret';
const request = require('supertest');
const app = require('../src/app');

// These tests never touch the database: they cover routing, auth guards and input validation.
describe('API basics', () => {
  test('GET /health responds ok', async () => {
    const res = await request(app).get('/health');
    expect(res.status).toBe(200);
    expect(res.body.status).toBe('ok');
  });
  test('unknown route returns 404 JSON', async () => {
    const res = await request(app).get('/api/nope');
    expect(res.status).toBe(404);
  });
  test('protected routes reject requests without a token', async () => {
    for (const path of ['/api/tasks', '/api/lists', '/api/users/me']) {
      const res = await request(app).get(path);
      expect(res.status).toBe(401);
    }
  });
  test('protected routes reject a garbage token', async () => {
    const res = await request(app).get('/api/tasks').set('Authorization', 'Bearer not.a.token');
    expect(res.status).toBe(401);
  });
});

describe('POST /api/auth validation', () => {
  test('register rejects invalid input with field errors', async () => {
    const res = await request(app).post('/api/auth/register').send({ fullName: 'A', email: 'bad', password: '1' });
    expect(res.status).toBe(400);
    expect(res.body.errors).toHaveProperty('email');
    expect(res.body.errors).toHaveProperty('password');
  });
  test('login rejects an empty body', async () => {
    const res = await request(app).post('/api/auth/login').send({});
    expect(res.status).toBe(400);
  });
  test('google login requires an idToken', async () => {
    const res = await request(app).post('/api/auth/google').send({});
    expect(res.status).toBe(400);
  });
  test('malformed JSON returns 400 instead of crashing', async () => {
    const res = await request(app).post('/api/auth/login').set('Content-Type', 'application/json').send('{bad json');
    expect(res.status).toBe(400);
  });
});
