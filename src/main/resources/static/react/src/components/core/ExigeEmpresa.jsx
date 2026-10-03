import React, { useEffect, useState } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { useTranslation } from 'react-i18next';
import { apiFetch } from '../../services/ApiConfig';
import { ConfigurarEmpresa } from './ConfigurarEmpresa';

/**
 * Exige empresa antes de liberar o sistema.
 *
 * Empresa e o tenant: e o filtro de todos os dados. Um usuario sem empresa
 * cadastrada nao ve nada, e so a tela de cadastro da empresa fica disponivel.
 *
 * Por que nao confiar no empresaId do token: ele e emitido no login. Com a
 * empresa cadastrada depois do login, o token ainda diria que o usuario nao
 * tem empresa, e o inverso tambem acontece. Aqui a consulta vai ao banco, que
 * e a fonte da verdade.
 */
const LIBERADAS_SEM_EMPRESA = ['/configurar-empresa', '/perfil'];

export const ExigeEmpresa = ({ children }) => {
    const { t } = useTranslation();
    const location = useLocation();
    const { user } = useAuth();

    // Mesma excecao do filtro no backend: SUPERUSER nao passa por esta regra.
    // Sem isso, o superuser era jogado para a tela de cadastro mesmo com a API
    // liberada — e sem empresa cadastrada, que e o caso de quem instala.
    const isSuperuser = !!user?.isSuperuser;
    const [verificando, setVerificando] = useState(true);
    const [cadastrada, setCadastrada] = useState(null);

    const caminho = location.pathname;
    const liberada = LIBERADAS_SEM_EMPRESA.some((c) => caminho === c || caminho.startsWith(c + '/'));

    useEffect(() => {
        if (liberada || isSuperuser) {
            setVerificando(false);
            return;
        }
        let cancelado = false;
        apiFetch('/api/core/minha-empresa')
            .then((r) => (r.ok ? r.json() : null))
            .then((json) => {
                if (!cancelado) setCadastrada(json?.data?.cadastrada === true);
            })
            .catch(() => {
                // Falha ao consultar nao pode trancar a tela: sem rede, o
                // usuario ficaria preso sem caminho para sair. O backend ja
                // recusa o dado de empresa, entao o sistema abre vazio em vez
                // de fechar.
                if (!cancelado) setCadastrada(true);
            });
        return () => { cancelado = true; };
    }, [caminho, liberada, isSuperuser]);

    if (liberada || isSuperuser) return children;

    if (verificando) {
        return (
            <div className="p-5 flex justify-content-center">
                <span className="bc-muted">{t('company.checking')}</span>
            </div>
        );
    }

    if (cadastrada === false) {
        // Empresa cadastrada em outra aba / outro login: manda para o cadastro
        // em vez de repetir a tela de espera.
        return <Navigate to="/configurar-empresa" replace state={{ origem: caminho }} />;
    }

    return children;
};

export default ExigeEmpresa;
