import React, { useEffect } from 'react';
import i18n from '../../i18n';
import ptBR from '../../locales/pt-BR.json';
import enUS from '../../locales/en-US.json';
import esES from '../../locales/es-ES.json';
import frFR from '../../locales/fr-FR.json';

// Compatibilidade para telas legadas ainda sem t(): usa as mesmas quatro
// traducoes versionadas. Esta camada NAO chama APIs externas nem altera dados.
const localeObjects = { 'pt-BR': ptBR, 'en-US': enUS, 'es-ES': esES, 'fr-FR': frFR };
const flatten = (value, prefix = '', out = {}) => {
  for (const [key, item] of Object.entries(value || {})) {
    const path = prefix ? prefix + '.' + key : key;
    if (item && typeof item === 'object' && !Array.isArray(item)) flatten(item, path, out);
    else if (typeof item === 'string' && item.trim()) out[path] = item;
  }
  return out;
};
const dictionaries = Object.fromEntries(
  Object.entries(localeObjects).map(([lang, entries]) => [lang, flatten(entries)])
);
const languageCode = language => {
  if (dictionaries[language]) return language;
  return Object.keys(dictionaries).find(key => key.startsWith((language || '').split('-')[0] + '-')) || 'pt-BR';
};
const reverse = Object.fromEntries(Object.keys(dictionaries).map(lang => {
  const entries = new Map();
  for (const [key, original] of Object.entries(dictionaries['pt-BR'])) {
    const translation = dictionaries[lang][key];
    if (translation && original.trim() && translation !== original) entries.set(original.trim(), translation);
  }
  return [lang, entries];
}));
const escapeRegex = value => value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
const translate = (original, language) => {
  if (language === 'pt-BR' || !original?.trim()) return original;
  const dictionary = reverse[language];
  const trimmed = original.trim();
  const direct = dictionary.get(trimmed);
  const whitespace = original.match(/^(\s*)/)[0];
  const suffix = original.match(/(\s*)$/)[0];
  if (direct) return whitespace + direct + suffix;
  for (const [source, translated] of dictionary) {
    if (!source.includes('{{')) continue;
    const parts = source.split(/(\{\{[^}]+\}\})/g).filter(Boolean);
    let placeholders = 0;
    const regex = new RegExp('^' + parts.map(part => {
      if (/^\{\{[^}]+\}\}$/.test(part)) { placeholders++; return '(.+?)'; }
      return escapeRegex(part);
    }).join('') + '$');
    if (!placeholders) continue;
    const match = trimmed.match(regex);
    if (!match) continue;
    let index = 1;
    return whitespace + translated.replace(/\{\{[^}]+\}\}/g, () => match[index++] || '') + suffix;
  }
  return original;
};

// WeakMaps mantem o ORIGINAL, permitindo trocar en -> fr -> es -> pt sem reload.
// Textos substituidos pelo React sao reconhecidos como novo original.
const originalTexts = new WeakMap();
const originalAttributes = new WeakMap();
const ATTRIBUTES = ['placeholder', 'title', 'aria-label', 'aria-placeholder', 'alt'];
const SKIP = new Set(['SCRIPT', 'STYLE', 'NOSCRIPT', 'TEXTAREA', 'CODE', 'PRE', 'OPTION']);
const skipNode = node => {
  const el = node.parentElement;
  return !el || SKIP.has(el.tagName) || el.closest('[contenteditable="true"],[data-i18n-skip="true"]');
};
function translateTextNode(node, language) {
  if (skipNode(node) || !node.nodeValue?.trim()) return;
  const remembered = originalTexts.get(node);
  const original = remembered && node.nodeValue === remembered.last
    ? remembered.original : node.nodeValue;
  const next = translate(original, language);
  if (next !== node.nodeValue) node.nodeValue = next;
  originalTexts.set(node, { original, last: next });
}
function translateAttributes(el, language) {
  if (el.closest('[contenteditable="true"],[data-i18n-skip="true"]')) return;
  const remembered = originalAttributes.get(el) || {};
  for (const name of ATTRIBUTES) {
    const current = el.getAttribute(name);
    if (current === null) continue;
    const last = remembered[name];
    const original = last && current === last.last ? last.original : current;
    const next = translate(original, language);
    if (next !== current) el.setAttribute(name, next);
    remembered[name] = { original, last: next };
  }
  originalAttributes.set(el, remembered);
}
export const applyDomTranslations = () => {
  if (typeof document === 'undefined') return;
  const language = languageCode(i18n.resolvedLanguage || i18n.language);
  // Portais PrimeReact (Dialog, Dropdown, Toast) ficam diretamente no body.
  const walker = document.createTreeWalker(document.body, NodeFilter.SHOW_TEXT);
  const texts = [];
  while (walker.nextNode()) texts.push(walker.currentNode);
  for (const node of texts) translateTextNode(node, language);
  for (const el of document.body.querySelectorAll('[placeholder],[title],[aria-label],[aria-placeholder],[alt]')) {
    translateAttributes(el, language);
  }
};
export function I18nDomBridge() {
  useEffect(() => {
    let frame = null;
    const schedule = () => {
      if (frame !== null) return;
      frame = window.requestAnimationFrame(() => { frame = null; applyDomTranslations(); });
    };
    const observer = new MutationObserver(schedule);
    observer.observe(document.body, {
      childList: true, subtree: true, characterData: true, attributes: true,
      attributeFilter: ATTRIBUTES
    });
    i18n.on('languageChanged', schedule);
    schedule();
    return () => {
      observer.disconnect();
      i18n.off('languageChanged', schedule);
      if (frame !== null) window.cancelAnimationFrame(frame);
    };
  }, []);
  return null;
}
export default I18nDomBridge;
