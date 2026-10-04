import React, { useEffect } from 'react';
import i18n from '../../i18n';
import ptBR from '../../locales/pt-BR.json';
import enUS from '../../locales/en-US.json';
import esES from '../../locales/es-ES.json';
import frFR from '../../locales/fr-FR.json';

const LOCALES = { 'pt-BR': ptBR, 'en-US': enUS, 'es-ES': esES, 'fr-FR': frFR };

const flatten = (value, prefix = '', output = {}) => {
  Object.entries(value || {}).forEach(([key, item]) => {
    const path = prefix ? prefix + '.' + key : key;
    if (item && typeof item === 'object' && !Array.isArray(item)) flatten(item, path, output);
    else if (typeof item === 'string' && item.trim()) output[path] = item;
  });
  return output;
};

const dictionaries = Object.fromEntries(Object.entries(LOCALES).map(([language, locale]) => [language, flatten(locale)]));
const normalizeLanguage = language => {
  if (dictionaries[language]) return language;
  const base = language?.split('-')[0];
  return Object.keys(dictionaries).find(code => code.split('-')[0] === base) || 'pt-BR';
};

const buildReverseDictionary = language => {
  const target = dictionaries[normalizeLanguage(language)];
  const source = dictionaries['pt-BR'];
  const reverse = new Map();
  Object.keys(source).forEach(key => {
    const original = source[key];
    const translated = target[key];
    if (original && translated && original !== translated) reverse.set(original.trim(), translated);
  });
  return reverse;
};

const escapeRegex = value => value.replace(/[.*+?^$()|[\\]{}]/g, '\\$&');
const translateTemplate = (text, reverse) => {
  const exact = reverse.get(text.trim());
  if (exact) return text.startsWith(' ') ? ' ' + exact : exact;
  for (const [source, target] of reverse.entries()) {
    if (!source.includes('{{')) continue;
    const parts = source.split(/(\{\{[^}]+\}\})/g).filter(Boolean);
    let pattern = '^';
    parts.forEach(part => {
      pattern += /^\{\{[^}]+\}\}$/.test(part) ? '(.+?)' : escapeRegex(part);
    });
    const match = text.trim().match(new RegExp(pattern + '$'));
    if (!match) continue;
    let index = 1;
    const translated = target.replace(/\{\{[^}]+\}\}/g, () => match[index++] ?? '');
    return text.startsWith(' ') ? ' ' + translated : translated;
  }
  return null;
};

export const applyDomTranslations = () => {
  if (typeof document === 'undefined') return;
  const root = document.getElementById('root');
  if (!root) return;
  const language = normalizeLanguage(i18n.language);
  if (language === 'pt-BR') return;
  const reverse = buildReverseDictionary(language);
  if (!reverse.size) return;
  const walker = document.createTreeWalker(root, NodeFilter.SHOW_TEXT, {
    acceptNode(node) {
      const parent = node.parentElement;
      if (!parent || !node.nodeValue?.trim()) return NodeFilter.FILTER_REJECT;
      if (['SCRIPT', 'STYLE', 'NOSCRIPT', 'TEXTAREA'].includes(parent.tagName)) return NodeFilter.FILTER_REJECT;
      return NodeFilter.FILTER_ACCEPT;
    }
  });
  const nodes = [];
  let node;
  while ((node = walker.nextNode())) nodes.push(node);
  nodes.forEach(textNode => {
    const translated = translateTemplate(textNode.nodeValue, reverse);
    if (translated && translated !== textNode.nodeValue) textNode.nodeValue = translated;
  });
  root.querySelectorAll('[placeholder],[title],[aria-label],[aria-placeholder],[alt]').forEach(element => {
    ['placeholder', 'title', 'aria-label', 'aria-placeholder', 'alt'].forEach(attribute => {
      const value = element.getAttribute(attribute);
      if (!value) return;
      const translated = translateTemplate(value, reverse);
      if (translated && translated !== value) element.setAttribute(attribute, translated);
    });
  });
};

export function I18nDomBridge() {
  useEffect(() => {
    let frame = null;
    const schedule = () => {
      if (frame !== null) return;
      frame = window.requestAnimationFrame(() => { frame = null; applyDomTranslations(); });
    };
    const root = document.getElementById('root') || document.body;
    const observer = new MutationObserver(schedule);
    observer.observe(root, { childList: true, subtree: true, characterData: true });
    const onLanguageChanged = () => window.requestAnimationFrame(applyDomTranslations);
    i18n.on('languageChanged', onLanguageChanged);
    schedule();
    return () => {
      observer.disconnect();
      i18n.off('languageChanged', onLanguageChanged);
      if (frame !== null) window.cancelAnimationFrame(frame);
    };
  }, []);
  return null;
};

export default I18nDomBridge;