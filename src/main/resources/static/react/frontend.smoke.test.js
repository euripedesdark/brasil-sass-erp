const fs = require('fs');
const path = require('path');

const ROOT = __dirname;
const SRC = path.join(ROOT, 'src');
const extensions = ['.js', '.jsx', '.mjs', '.cjs', '.ts', '.tsx'];
let failures = 0;
let checks = 0;

const fail = (message) => {
  failures++;
  console.error('FAIL: ' + message);
};

const ok = (message) => {
  checks++;
  console.log('OK: ' + message);
};

function existsModule(fromFile, spec) {
  if (!spec.startsWith('.')) return true;
  const base = path.resolve(path.dirname(fromFile), spec);
  const candidates = [
    base,
    ...extensions.map((ext) => base + ext),
    ...extensions.map((ext) => path.join(base, 'index' + ext))
  ];
  return candidates.some((file) => fs.existsSync(file) && fs.statSync(file).isFile());
}

function walk(dir) {
  const result = [];
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    if (['node_modules', 'dist', '.git'].includes(entry.name)) continue;
    const file = path.join(dir, entry.name);
    if (entry.isDirectory()) result.push(...walk(file));
    else if (extensions.includes(path.extname(entry.name))) result.push(file);
  }
  return result;
}

const packageJson = JSON.parse(fs.readFileSync(path.join(ROOT, 'package.json'), 'utf8'));

if (packageJson.scripts?.build === 'vite build') ok('canonical frontend package uses Vite');
else fail('frontend package does not declare vite build');

if (packageJson.scripts?.test === 'node frontend.smoke.test.js') ok('frontend test script points to the smoke test');
else fail('frontend test script does not point to frontend.smoke.test.js');

if (fs.existsSync(path.join(ROOT, 'vite.config.js'))) ok('vite.config.js exists');
else fail('vite.config.js is missing');

const files = walk(SRC);
const importRegex = /(?:import\s+(?:[\s\S]*?)\s+from\s+|import\s*\(|export\s+(?:[\s\S]*?)\s+from\s+)['"]([^'"]+)['"]/g;

let importFailures = 0;
for (const file of files) {
  const source = fs.readFileSync(file, 'utf8');
  let match;
  while ((match = importRegex.exec(source))) {
    const specifier = match[1];
    if (specifier.startsWith('.') && !existsModule(file, specifier)) {
      importFailures++;
      fail(path.relative(ROOT, file) + ' imports missing module ' + specifier);
    }
  }
}
if (importFailures === 0) ok('all relative JS/JSX imports resolve to files');

const app = path.join(SRC, 'App.jsx');
if (!fs.existsSync(app)) {
  fail('src/App.jsx is missing');
} else {
  const source = fs.readFileSync(app, 'utf8');
  const routeNames = [
    ...source.matchAll(/<Route\b[^>]*\belement=\{\s*<([A-Za-z_$][\w$]*)/g)
  ].map((match) => match[1]);

  const importedNames = new Set();
  // A flag 'g' e' obrigatoria: String.prototype.matchAll lanca
  // "TypeError: called with a non-global RegExp argument" sem ela, e o
  // teste morre aqui antes de verificar o resto.
  for (const match of source.matchAll(
    /import\s+(?:\{\s*([^}]+)\s*\}|([A-Za-z_$][\w$]*))\s+from\s+['"][^'"]+['"]/g
  )) {
    if (match[1]) {
      for (const item of match[1].split(',')) {
        importedNames.add(item.trim().split(/\s+as\s+/)[0]);
      }
    } else if (match[2]) {
      importedNames.add(match[2]);
    }
  }

  for (const name of routeNames) {
    if (!importedNames.has(name)) fail('App.jsx route uses <' + name + '> without an import');
  }
  if (routeNames.length > 0 && failures === 0) ok('App.jsx route elements resolve to imported components');
}

if (failures > 0) {
  console.error('\nFrontend smoke failed: ' + failures + ' failure(s).');
  process.exit(1);
}
console.log('\nFrontend smoke passed: ' + checks + ' checks.');
