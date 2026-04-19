import { createServer } from "node:http";
import { readFile } from "node:fs/promises";
import { extname, join, normalize, sep } from "node:path";
import { fileURLToPath } from 'url';

const root = new URL(".", import.meta.url).pathname;
const port = Number(process.env.PORT || 5173);
const types = {
  ".html": "text/html; charset=utf-8",
  ".css": "text/css; charset=utf-8",
  ".js": "text/javascript; charset=utf-8",
  ".json": "application/json; charset=utf-8"
};

createServer(async (request, response) => {
  try {
    const url = new URL(request.url, `http://${request.headers.host}`);
    const requestedPath = url.pathname === "/" ? "/index.html" : url.pathname;
    
    let filePath = join(root, requestedPath);

    if (process.platform === 'win32' && filePath.startsWith('\\')) {
      filePath = filePath.substring(1);
    }
    
    console.log("ROOT:", root);
    console.log("FILEPATH:", filePath);
    const checkRoot = root.toLowerCase().replace(/\\/g, '/').replace(/^\//, '');
    const checkFile = filePath.toLowerCase().replace(/\\/g, '/').replace(/^\//, '');
    
    if (!checkFile.startsWith(checkRoot)) {
      response.writeHead(403);
      response.end("Forbidden");
      return;
    }

    console.log("FINAL SYSTEM PATH:", filePath);

    const body = await readFile(filePath);
    response.writeHead(200, { "Content-Type": types[extname(filePath)] || "application/octet-stream" });
    response.end(body);
  } catch {
    response.writeHead(404, { "Content-Type": "text/plain; charset=utf-8" });
    response.end("Not found");
  }
}).listen(port, () => {
  console.log(`AutoOps frontend running at http://localhost:${port}`);
});
