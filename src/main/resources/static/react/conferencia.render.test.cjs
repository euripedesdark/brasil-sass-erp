const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const esbuild = require('esbuild');
const React = require('react');
const { renderToString } = require('react-dom/server');
require('i18next').use(require('react-i18next').initReactI18next).init({lng:'pt',initImmediate:false,resources:{pt:{translation:{}}}});
const output = path.join(__dirname, 'node_modules/.cache/conferencia-render.cjs');
try {
  esbuild.buildSync({
    entryPoints: [path.join(__dirname, 'src/components/compras/ConferenciaFaturasCompra.jsx')],
    outfile: output, bundle: true, platform: 'node', format: 'cjs',
    external: ['react', 'react-dom', 'react-i18next', 'primereact/*', 'axios'],
  });
  const Component = require(output).default;
  const html = renderToString(React.createElement(Component));
  assert.match(html, /Valor da fatura/);
  assert.match(html, /Recebimento/);
  assert.match(html, /disabled/);
  console.log('Conferência: renderização inicial e bloqueio de formulário vazio passaram.');
} finally {
  if (fs.existsSync(output)) fs.unlinkSync(output);
}
