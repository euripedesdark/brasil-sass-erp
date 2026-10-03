/* src/main/resources/static/js/sysfluxo.js */

document.addEventListener("DOMContentLoaded", () => {
    const currentPath = window.location.pathname;
    document.querySelectorAll('.sys-nav-link').forEach(link => {
        if (link.getAttribute('href') === currentPath) {
            link.classList.add('active');
        }
    });

    // --- Aplica a imagem de fundo corporativa globalmente ---
    document.body.style.backgroundImage = "url('/images/fluxo-caixa-color.png')";
    document.body.style.backgroundRepeat = "no-repeat";
    document.body.style.backgroundPosition = "center center";
    document.body.style.backgroundAttachment = "fixed";
    document.body.style.backgroundSize = "cover";
});

function logout() {
    fetch('/api/auth/logout', { method: 'POST', credentials: 'include' })
    .then(() => {
        document.cookie = 'sysfluxo_token=; Max-Age=0; path=/';
        localStorage.removeItem('sysfluxo_token');
        window.location.href = '/login';
    })
    .catch(err => console.error('Erro ao encerrar sessão:', err));
}

async function crudListar(endpoint) {
    try {
        const response = await fetch(endpoint);
        if (!response.ok) throw new Error('Erro na busca');
        return await response.json();
    } catch (error) {
        console.error(`Erro ao carregar ${endpoint}:`, error);
        return [];
    }
}

async function crudSalvar(endpoint, payload, formElement, callbackSucesso) {
    try {
        const response = await fetch(endpoint, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (response.ok) {
            if(formElement) formElement.reset();
            if(callbackSucesso) callbackSucesso();
            alert('Registro salvo com sucesso!');
        } else {
            let erroBackend = '';
            try {
                const erroData = await response.json();
                const erroDataString = JSON.stringify(erroData);
                if (erroDataString.includes('foreign key') || erroDataString.includes('violates')) {
                    erroBackend = "O registro está vinculado a outras tabelas (financeiro, notas ou movimentações).";
                } else {
                    erroBackend = erroData.message || erroData.error || erroDataString;
                }
            } catch(e) {
                erroBackend = response.statusText;
            }
            alert(`Erro ao gravar registro.\nMotivo: ${erroBackend}`);
        }
    } catch (error) {
        alert('Erro de comunicação com o servidor.');
    }
}

async function crudExcluir(endpoint, id, callbackSucesso) {
    if (!id || id === 'null' || id === 'undefined') {
        alert('ERRO: Este registro não possui um ID válido gerado pelo banco.');
        return;
    }

    if (confirm('Atenção: Tem certeza que deseja excluir este registro permanentemente?')) {
        try {
            const response = await fetch(`${endpoint}/${id}`, { method: 'DELETE' });
            if (response.ok) {
                if(callbackSucesso) callbackSucesso();
            } else {
                let detalhe = "O registro possui dependências ativas no sistema.";
                try {
                    const errJson = await response.json();
                    detalhe = errJson.message || errJson.error || JSON.stringify(errJson);
                } catch(e) {}

                alert(`Não foi possível excluir o registro.\nMotivo Técnico / Vínculo: ${detalhe}\n\nDica: Se houver lançamentos financeiros atrelados, remova-os primeiro.`);
            }
        } catch (error) {
            console.error(`Erro ao excluir ${id} em ${endpoint}:`, error);
            alert('Erro de comunicação ao tentar excluir o registro.');
        }
    }
}
