const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const esbuild = require('esbuild');
const React = require('react');
const { renderToString } = require('react-dom/server');
const output = path.join(__dirname, 'node_modules/.cache/devolucoes-render.cjs');
try {
  esbuild.buildSync({
    entryPoints: [path.join(__dirname, 'src/components/vendas/Devolucoes.jsx')],
    outfile: output, bundle: true, platform: 'node', format: 'cjs',
    external: ['react', 'react-dom', 'primereact/*', 'axios'],
  });
  const Component = require(output).default;
  const html = renderToString(React.createElement(Component));
  assert.match(html, /Trocas e Devolu/);
  assert.match(html, /Solicitar devolu/);
  assert.match(html, /Selecione uma devolu/);
  console.log('Devoluções: renderização inicial passou.');
} finally {
  if (fs.existsSync(output)) fs.unlinkSync(output);
}
