import { http, HttpResponse } from 'msw';

const BASE = 'http://localhost:8080';

export const handlers = [
  http.post(`${BASE}/api/auth/login`, async ({ request }) => {
    const body = await request.json() as { email: string; password: string };
    if (body.email === 'test@example.com' && body.password === 'password123') {
      return HttpResponse.json({ token: 'mock-jwt-token', email: body.email, name: '테스터' });
    }
    return HttpResponse.json({ message: '잘못된 자격증명' }, { status: 401 });
  }),

  http.post(`${BASE}/api/auth/register`, async ({ request }) => {
    const body = await request.json() as { email: string; name: string; password: string };
    return HttpResponse.json({ token: 'mock-jwt-token', email: body.email, name: body.name });
  }),

  http.get(`${BASE}/api/auth/me`, () => {
    return HttpResponse.json({ email: 'test@example.com', name: '테스터' });
  }),

  http.get(`${BASE}/api/tasks`, ({ request }) => {
    const url = new URL(request.url);
    const status = url.searchParams.get('status');
    const tasks = [
      { id: 1, title: '첫 번째 태스크', status: 'TODO', priority: 'MEDIUM', description: '' },
      { id: 2, title: '두 번째 태스크', status: 'IN_PROGRESS', priority: 'HIGH', description: '' },
    ];
    if (status) return HttpResponse.json(tasks.filter(t => t.status === status));
    return HttpResponse.json(tasks);
  }),

  http.post(`${BASE}/api/tasks`, async ({ request }) => {
    const body = await request.json() as { title: string; status?: string; priority?: string };
    return HttpResponse.json({
      id: 99,
      title: body.title,
      status: body.status ?? 'TODO',
      priority: body.priority ?? 'MEDIUM',
      description: '',
    });
  }),

  http.put(`${BASE}/api/tasks/:id`, async ({ params, request }) => {
    const body = await request.json() as { title: string; status?: string; priority?: string };
    return HttpResponse.json({
      id: Number(params.id),
      title: body.title,
      status: body.status ?? 'TODO',
      priority: body.priority ?? 'MEDIUM',
      description: '',
    });
  }),

  http.delete(`${BASE}/api/tasks/:id`, () => {
    return new HttpResponse(null, { status: 204 });
  }),

  http.get(`${BASE}/api/stats`, () => {
    return HttpResponse.json({
      totalTasks: 10, todo: 4, inProgress: 3, done: 3,
      lowPriority: 2, mediumPriority: 5, highPriority: 3,
      overdue: 1, totalProjects: 2, totalComments: 8,
    });
  }),
];
