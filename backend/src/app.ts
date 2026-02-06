import Fastify from 'fastify';
import prisma from './lib/prisma';
import { authRoutes } from './routes/auth';
import { servicesRoutes } from './routes/services';

const app = Fastify({ logger: true });

app.get('/health', async () => {
  let db: 'connected' | 'error' = 'connected';
  try {
    await prisma.$queryRaw`SELECT 1`;
  } catch {
    db = 'error';
  }

  return { status: 'ok', db };
});

app.register(authRoutes, { prefix: '/auth' });
app.register(servicesRoutes, { prefix: '/services' });

export default app;
