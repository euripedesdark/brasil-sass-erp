/** Legacy UI translations shared by the DOM bridge and Node regression tests.
 * Source strings remain in pt-BR until individual screens adopt useTranslation.
 * Backend data and free-text user input must never be translated heuristically.
 */
export const SUPPORTED_UI_LANGUAGES = ['pt-BR', 'en-US', 'es-ES', 'fr-FR'];

export const normalizeUiLanguage = language => {
  const exact = SUPPORTED_UI_LANGUAGES.find(code => code === language);
  if (exact) return exact;
  const base = String(language || '').split('-')[0].toLowerCase();
  return SUPPORTED_UI_LANGUAGES.find(code => code.split('-')[0] === base) || 'pt-BR';
};

export function flattenMessages(messages, prefix = '', out = {}) {
  for (const [key, item] of Object.entries(messages || {})) {
    const name = prefix ? prefix + '.' + key : key;
    if (typeof item === 'string' && item.trim()) out[name] = item;
    else if (item && typeof item === 'object' && !Array.isArray(item)) {
      flattenMessages(item, name, out);
    }
  }
  return out;
}

const VARIABLE = /(\{\{[^}]+\}\}|\{[a-zA-Z_][^}]*\})/g;
const IS_VARIABLE = /^(?:\{\{[^}]+\}\}|\{[a-zA-Z_][^}]*\})$/;
const escapeRegex = value => value.replace(/[|\\{}()[\]^$+*?.]/g, '\\$&');
const variableName = token => token.replace(/^\{\{?|\}\}?$/g, '').trim();

const buildTemplate = (original, translated) => {
  const names = [];
  const parts = original.split(VARIABLE).filter(Boolean);
  let pattern = '^';
  for (const part of parts) {
    if (IS_VARIABLE.test(part)) {
      names.push(variableName(part));
      pattern += '(.+?)';
    } else {
      pattern += escapeRegex(part);
    }
  }
  pattern += '$';
  return {
    matcher: new RegExp(pattern),
    render: match => {
      const byName = Object.fromEntries(names.map((name, i) => [name, match[i + 1]]));
      return translated.replace(VARIABLE, token => byName[variableName(token)] ?? token);
    }
  };
};

/** Returns a safe, exact/template-match translator. Unknown strings stay unchanged. */
export function createLegacyTranslator(locales) {
  const flattened = Object.fromEntries(SUPPORTED_UI_LANGUAGES.map(code => [
    code, flattenMessages(locales[code] || {})
  ]));
  const original = flattened['pt-BR'];
  const compiled = Object.fromEntries(SUPPORTED_UI_LANGUAGES.map(code => {
    const exact = new Map();
    const templates = [];
    const target = flattened[code];
    for (const [key, source] of Object.entries(original)) {
      const translated = target[key];
      if (!source || !translated || source === translated) continue;
      const pt = source.trim();
      if (VARIABLE.test(pt)) {
        VARIABLE.lastIndex = 0;
        templates.push(buildTemplate(pt, translated.trim()));
      } else if (!exact.has(pt)) {
        exact.set(pt, translated.trim());
      }
      VARIABLE.lastIndex = 0;
    }
    return { exact, templates };
  }));
  return (text, requestedLanguage) => {
    if (!text || !text.trim()) return text;
    const language = normalizeUiLanguage(requestedLanguage);
    if (language === 'pt-BR') return text;
    const { exact, templates } = compiled[language];
    const core = text.trim();
    let translated = exact.get(core);
    if (!translated) {
      for (const template of templates) {
        const match = template.matcher.exec(core);
        if (match) {
          translated = template.render(match);
          break;
        }
      }
    }
    if (!translated || translated === core) return text;
    const leading = text.match(/^\s*/u)?.[0] || '';
    const trailing = text.match(/\s*$/u)?.[0] || '';
    return leading + translated + trailing;
  };
}
