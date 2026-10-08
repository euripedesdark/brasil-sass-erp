const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const esbuild = require('esbuild');
const React = require('react');
const { renderToString } = require('react-dom/server');
const output = path.join(__dirname, 'node_modules/.cache/mrp-render.cjs');
try {
  esbuild.buildSync({
    entryPoints: [path.join(__dirname, 'src/components/producao/Mrp.jsx')],
    outfile: output, bundle: true, platform: 'node', format: 'cjs',
    external: ['react', 'react-dom', 'primereact/*', 'axios'],
  });
  const Component = require(output).default;
  const html = renderToString(React.createElement(Component));
  assert.match(html, /MRP/);
  assert.match(html, /Executar MRP/);
  assert.match(html, /Execute o MRP para ver/);
  assert.match(html, /disabled/);
  console.log('MRP: renderização inicial passou.');
} finally {
  if (fs.existsSync(output)) fs.unlinkSync(output);
}
