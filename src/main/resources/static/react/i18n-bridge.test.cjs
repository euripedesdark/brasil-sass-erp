const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const { pathToFileURL } = require('node:url');

(async () => {
  const { createLegacyTranslator, normalizeUiLanguage, flattenMessages } =
    await import(pathToFileURL(path.join(__dirname, 'src/i18n/legacyText.mjs')).href);
  const locales = Object.fromEntries(['pt-BR','en-US','es-ES','fr-FR'].map(code => [
    code, JSON.parse(fs.readFileSync(path.join(__dirname, 'src/locales', code + '.json'), 'utf8'))
  ]));
  const t = createLegacyTranslator(locales);
  assert.equal(normalizeUiLanguage('fr-CA'), 'fr-FR');
  assert.equal(normalizeUiLanguage('es-MX'), 'es-ES');
  assert.equal(normalizeUiLanguage('en-US'), 'en-US');
  assert.equal(normalizeUiLanguage('unknown'), 'pt-BR');
  assert.equal(t('Cancelar','en-US'), 'Cancel');
  assert.equal(t('Cancelar','fr-FR'), 'Annuler');
  assert.equal(t('Cancelar','pt-BR'), 'Cancelar');
  assert.equal(t('  Cancelar  ','en-US'), '  Cancel  ');
  assert.equal(t('Registro interno desconhecido ZX-9021','fr-FR'), 'Registro interno desconhecido ZX-9021'); // unknown data must remain intact
  const testTranslator = createLegacyTranslator({
    'pt-BR': { test: 'Mostrando {{first}} a {{last}} de {{total}} pedidos' },
    'en-US': { test: 'Showing {{first}} to {{last}} of {{total}} orders' },
    'es-ES': { test: 'Mostrando {{first}} a {{last}} de {{total}} pedidos' },
    'fr-FR': { test: 'Commandes {{first}} à {{last}} sur {{total}}' }
  });
  assert.equal(testTranslator('Mostrando 2 a 9 de 20 pedidos','en-US'),'Showing 2 to 9 of 20 orders');
  assert.equal(testTranslator('Mostrando 2 a 9 de 20 pedidos','fr-FR'),'Commandes 2 à 9 sur 20');
  assert.equal(testTranslator('Outro texto do banco','en-US'),'Outro texto do banco');
  const flat = flattenMessages(locales['pt-BR']);
  assert.ok(Object.keys(flat).length >= 1800, 'catalog lost existing translations');
  const bridge = fs.readFileSync(path.join(__dirname, 'src/components/shared/I18nDomBridge.jsx'),'utf8');
  assert.match(bridge, /WeakMap/);
  assert.match(bridge, /document\.body/);
  assert.match(bridge, /languageChanged/);
  assert.match(bridge, /state\.source/);
  console.log('i18n bridge regression checks passed for all 4 languages');
})().catch(error => { console.error(error); process.exitCode = 1; });
