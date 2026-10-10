import React, { useEffect } from 'react';
import i18n from '../../i18n';
import ptBR from '../../locales/pt-BR.json';
import enUS from '../../locales/en-US.json';
import esES from '../../locales/es-ES.json';
import frFR from '../../locales/fr-FR.json';
import { createLegacyTranslator, normalizeUiLanguage } from '../../i18n/legacyText.mjs';

const translate = createLegacyTranslator({
  'pt-BR': ptBR, 'en-US': enUS, 'es-ES': esES, 'fr-FR': frFR
});

// WeakMaps keep the original Portuguese text even after language switching.
// They do not retain detached DOM nodes or interfere with React's virtual DOM.
const originalNodes = new WeakMap();
const originalAttributes = new WeakMap();
const ATTRIBUTES = ['placeholder', 'title', 'aria-label', 'aria-placeholder', 'alt', 'data-pr-tooltip'];
const EXCLUDED = new Set(['SCRIPT', 'STYLE', 'NOSCRIPT', 'TEXTAREA', 'CODE', 'PRE', 'KBD', 'SAMP']);

const shouldSkip = element => {
  if (!element) return true;
  if (EXCLUDED.has(element.tagName)) return true;
  return Boolean(element.closest('[translate="no"], [data-no-translate], [contenteditable="true"], code, pre, kbd, samp'));
};

const applyText = (node, language) => {
  if (!node.nodeValue?.trim() || shouldSkip(node.parentElement)) return;
  const current = node.nodeValue;
  let state = originalNodes.get(node);
  if (!state || current !== state.applied) {
    // If React re-rendered a genuinely new value, it becomes the source.
    state = { source: current, applied: current };
    originalNodes.set(node, state);
  }
  const next = translate(state.source, language);
  if (current !== next) node.nodeValue = next;
  state.applied = next;
};

const applyAttribute = (element, attribute, language) => {
  if (!element.hasAttribute(attribute) || shouldSkip(element)) return;
  const current = element.getAttribute(attribute);
  let stateMap = originalAttributes.get(element);
  if (!stateMap) {
    stateMap = new Map();
    originalAttributes.set(element, stateMap);
  }
  let state = stateMap.get(attribute);
  if (!state || current !== state.applied) {
    state = { source: current, applied: current };
    stateMap.set(attribute, state);
  }
  const next = translate(state.source, language);
  if (current !== next) element.setAttribute(attribute, next);
  state.applied = next;
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
  if (typeof document === 'undefined' || !document.body) return;
  const language = normalizeUiLanguage(i18n.resolvedLanguage || i18n.language);
  // PrimeReact dialogs, calendars and toasts may be portalled outside #root.
  const root = document.body;
  const walker = document.createTreeWalker(root, NodeFilter.SHOW_TEXT, {
    acceptNode(node) {
      return node.nodeValue?.trim() && !shouldSkip(node.parentElement)
        ? NodeFilter.FILTER_ACCEPT : NodeFilter.FILTER_REJECT;
    }
  });
  const nodes = [];
  let node;
  while ((node = walker.nextNode())) nodes.push(node);
  for (const textNode of nodes) applyText(textNode, language);
  const selector = ATTRIBUTES.map(name => '[' + name + ']').join(',');
  for (const element of root.querySelectorAll(selector)) {
    for (const attribute of ATTRIBUTES) applyAttribute(element, attribute, language);
  }
};
export function I18nDomBridge() {
  useEffect(() => {
    let frame = null;
    const schedule = () => {
      if (frame !== null) return;
      frame = window.requestAnimationFrame(() => {
        frame = null;
        applyDomTranslations();
      });
    };
    const observer = new MutationObserver(schedule);
    observer.observe(document.body, {
      childList: true,
      subtree: true,
      characterData: true,
      attributes: true,
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
