document.addEventListener('DOMContentLoaded', () => {
    const form      = document.getElementById('loginForm');
    const submitBtn = document.getElementById('submitBtn');
    const errorMsg  = document.getElementById('errorMsg');

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const username = document.getElementById('username').value;
        const password = document.getElementById('password').value;

        submitBtn.textContent = 'Autenticando...';
        submitBtn.disabled    = true;
        errorMsg.style.display = 'none';

        try {
            const response = await fetch('/api/auth/login', {
                method:  'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify({ username, password })
            });
            const data = await response.json();

            if (response.ok && data.success) {
                localStorage.setItem('sysfluxo_token', data.token);
                window.location.href = '/inicio';
            } else {
                throw new Error(data.message || 'Acesso negado');
            }
        } catch (error) {
            console.error('Erro:', error);
            errorMsg.textContent  = error.message;
            errorMsg.style.display = 'block';
            submitBtn.textContent  = 'Entrar';
            submitBtn.disabled     = false;
        }
    });
});
