import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { fileURLToPath, URL } from 'node:url';

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    host: '0.0.0.0',
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
      // O catálogo administrativo usa o OpenAPI real do backend.
      '/v3': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
      '/swagger-ui.html': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
      '/swagger-ui': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
    },
  },
  build: {
    outDir: '../dist',
    emptyOutDir: true,
    assetsDir: 'assets',
    rollupOptions: {
      output: {
        // Chunking por DIRETORIO, e nao por lista de pacotes.
        //
        // A lista antiga ("vendor: [react, react-dom, react-router-dom]")
        // mandava o core do react-dom para o vendor mas deixava o
        // react-dom/client no chunk principal. O React 19 so preenche o
        // ReactSharedInternals (o `C.H` do bundle) quando o react-dom/client
        // e avaliado, e o principal chamava useContext antes disso:
        //
        //   TypeError: Cannot read properties of null (reading 'useContext')
        //
        // O sintoma era a SPA inteira em branco. Agrupando por diretorio o
        // react-dom/client volta para o vendor, junto do core.
        manualChunks(id) {
          if (!id.includes('node_modules')) return;
          if (/[\\/]node_modules[\\/](react|react-dom|react-router|react-router-dom|scheduler)[\\/]/.test(id)) return 'vendor';
          if (/[\\/]node_modules[\\/](primereact|primeicons)[\\/]/.test(id)) return 'prime';
          if (/[\\/]node_modules[\\/]axios[\\/]/.test(id)) return 'utils';
        }
      }
    }
  },
  resolve: {
    alias: [
      { find: /^primereact\/datatable$/, replacement: fileURLToPath(new URL('./src/components/shared/SafeDataTable.jsx', import.meta.url)) },
      { find: '@', replacement: '/src' },
    ],
  },
});
