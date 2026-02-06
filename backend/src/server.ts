import dotenv from 'dotenv';
import app from './app';

dotenv.config();

const port = Number(process.env.PORT ?? 3000);
const host = '0.0.0.0';

app
  .listen({ port, host })
  .then((address) => {
    app.log.info(`Server listening at ${address}`);
  })
  .catch((err) => {
    app.log.error(err);
    process.exit(1);
  });
