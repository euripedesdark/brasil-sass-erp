import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import './i18n';
import { AuthProvider } from './contexts/AuthContext';
import 'primereact/resources/themes/lara-light-indigo/theme.css';
import 'primereact/resources/primereact.min.css';
import 'primeicons/primeicons.css';
import './components/shared/base.css';

import { apiFetch } from './services/ApiConfig';

// Reset global — evita layout quebrado quando CSS de página ainda não carregou
if (typeof document !== 'undefined') {
  const style = document.createElement('style');
  style.setAttribute('data-bc-reset', 'true');
  style.textContent = `
    html, body, #root {
      margin: 0;
      padding: 0;
      width: 100%;
      height: 100%;
      overflow: hidden;
      box-sizing: border-box;
    }
    *, *::before, *::after { box-sizing: inherit; }
  `;
  document.head.appendChild(style);
}

// Compatibilidade com componentes legados que ainda usam apiFetch('/api/...').
const nativeFetch = window.fetch.bind(window);
window.fetch = (input, init = {}) => {
  const url = typeof input === 'string' ? input : input?.url;
  const isApiRequest = typeof url === 'string' &&
    (url.startsWith('/api/') || url === '/api' ||
      url.startsWith(window.location.origin + '/api/') ||
      url === window.location.origin + '/api');

  if (!isApiRequest) return nativeFetch(input, init);

  const token = localStorage.getItem('brasil-saas_token');
  if (!token) return nativeFetch(input, init);

  const headers = new Headers(input instanceof Request ? input.headers : init.headers);
  if (!headers.has('Authorization')) headers.set('Authorization', 'Bearer ' + token);

  // Mesma empresa que ApiConfig injeta: endpoints que exigem X-Empresa-Id
  // respondem 400 sem ele, e o erro nao diz qual header faltou.
  const empresaId = localStorage.getItem('brasil-saas_empresa_id');
  if (empresaId && !headers.has('X-Empresa-Id')) headers.set('X-Empresa-Id', empresaId);

  return nativeFetch(input, { ...init, headers });
};

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <AuthProvider>
      <App />
    </AuthProvider>
  </React.StrictMode>
);
