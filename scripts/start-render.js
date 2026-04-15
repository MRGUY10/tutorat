const { spawn } = require('child_process');

const port = process.env.PORT || '10000';
const command = `npx ng serve --host 0.0.0.0 --port ${port}`;

const child = spawn(command, {
  stdio: 'inherit',
  shell: true
});

child.on('exit', (code) => {
  process.exit(code ?? 0);
});
