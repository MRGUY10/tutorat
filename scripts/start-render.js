const { spawn } = require('child_process');

const port = process.env.PORT || '10000';
const ngCommand = process.platform === 'win32' ? 'npx.cmd' : 'npx';

const child = spawn(ngCommand, ['ng', 'serve', '--host', '0.0.0.0', '--port', port], {
  stdio: 'inherit'
});

child.on('exit', (code) => {
  process.exit(code ?? 0);
});
