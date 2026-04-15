const http = require('http');
const fs = require('fs');
const path = require('path');

const host = '0.0.0.0';
const port = Number(process.env.PORT || 10000);
const distRoot = path.join(__dirname, '..', 'dist');
const distDir = fs.existsSync(path.join(distRoot, 'browser', 'index.html'))
  ? path.join(distRoot, 'browser')
  : distRoot;

const mimeTypes = {
  '.html': 'text/html; charset=utf-8',
  '.js': 'application/javascript; charset=utf-8',
  '.mjs': 'application/javascript; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.gif': 'image/gif',
  '.svg': 'image/svg+xml',
  '.ico': 'image/x-icon',
  '.woff': 'font/woff',
  '.woff2': 'font/woff2',
  '.ttf': 'font/ttf',
  '.txt': 'text/plain; charset=utf-8'
};

function safeResolve(requestPath) {
  const sanitized = requestPath.split('?')[0].split('#')[0];
  const targetPath = path.normalize(path.join(distDir, sanitized));
  if (!targetPath.startsWith(distDir)) {
    return null;
  }
  return targetPath;
}

function sendFile(res, filePath) {
  fs.readFile(filePath, (err, data) => {
    if (err) {
      res.writeHead(500, { 'Content-Type': 'text/plain; charset=utf-8' });
      res.end('Internal Server Error');
      return;
    }

    const ext = path.extname(filePath).toLowerCase();
    const contentType = mimeTypes[ext] || 'application/octet-stream';
    res.writeHead(200, { 'Content-Type': contentType });
    res.end(data);
  });
}

if (!fs.existsSync(path.join(distDir, 'index.html'))) {
  console.error('Build output not found: dist/index.html or dist/browser/index.html is missing.');
  console.error('Run the build step before starting the server.');
  process.exit(1);
}

const server = http.createServer((req, res) => {
  const reqPath = req.url === '/' ? '/index.html' : req.url;
  const resolvedPath = safeResolve(reqPath);

  if (!resolvedPath) {
    res.writeHead(403, { 'Content-Type': 'text/plain; charset=utf-8' });
    res.end('Forbidden');
    return;
  }

  fs.stat(resolvedPath, (err, stats) => {
    if (!err && stats.isFile()) {
      sendFile(res, resolvedPath);
      return;
    }

    sendFile(res, path.join(distDir, 'index.html'));
  });
});

server.listen(port, host, () => {
  console.log(`Static server running on http://${host}:${port}`);
});
