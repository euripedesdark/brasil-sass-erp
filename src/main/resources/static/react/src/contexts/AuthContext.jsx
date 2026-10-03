import React, { createContext, useContext, useState, useEffect } from 'react';
import { authService } from '../services/AuthService';
import { setEmpresaId } from '../services/ApiConfig';

const AuthContext = createContext();

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
};

const PERFIS_DIRETORIA = ['ADMIN', 'DIRETORIA'];

/**
 * SUPERUSER e o perfil que opera o gerenciador SQL (modo Interbase).
 * Hoje ele tem as mesmas permissoes do ADMIN (ver migration V89); a
 * separacao esta em avaliacao. Manter os dois nomes aqui evita que a tela
 * precise mudar quando a separacao for feita.
 */
const PERFIS_SUPERUSER = ['SUPERUSER', 'SUPERADMIN'];

/**
 * Deriva isAdmin / isSuperuser / isDiretoria / podeEditar a partir dos perfis
 * e permissoes. O backend (/api/auth/me) devolve `perfis` e `permissoes`; o
 * login devolve `authorities`. Cobrimos os dois formatos.
 */
const enriquecer = (base) => {
    if (!base) return base;

    // Persiste a empresa para as camadas HTTP injetarem X-Empresa-Id. Sem isso
    // os endpoints que exigem empresa respondem 400 sem explicar o motivo.
    if (base.empresaId !== undefined && base.empresaId !== null) {
        try {
            setEmpresaId(base.empresaId);
        } catch (erro) {
            console.warn('Não foi possível persistir a empresa atual', erro);
        }
    }

    const perfis = toSet(base.perfis);
    const permissoes = toSet(base.permissoes);
    const authorities = toSet(base.authorities);
    const modulos = toSet(base.modulos);
    const modulosLeitura = toSet(base.modulosSomenteLeitura);

    const isAdmin = base.isAdmin ?? perfis.has('ADMIN');
    const isDiretoria = base.isDiretoria ?? PERFIS_DIRETORIA.some((p) => perfis.has(p));
    const isSuperuser = base.isSuperuser
        ?? PERFIS_SUPERUSER.some((p) => perfis.has(p));

    const pode = (recurso, acao) => {
        if (isAdmin || isSuperuser) return true;
        if (permissoes.size && permissoes.has(`${recurso}:${acao}`)) return true;
        return authorities.has(`${recurso}:${acao}`);
    };

    // ADMIN/SUPERUSER veem todos os modulos; para os demais, o que veio em
    // `modulos`. Sem modulo liberado, o item some do menu em vez de levar a
    // pessoa a um 403.
    const temModulo = (chave) => {
        if (isAdmin || isSuperuser) return true;
        if (!modulos.size) return true; // API nao devolveu: nao trava o menu
        return modulos.has(chave);
    };

    const somenteLeitura = (chave) => {
        if (isAdmin || isSuperuser) return false;
        return modulosLeitura.has(chave);
    };

    return {
        ...base,
        perfis: [...perfis],
        permissoes: [...permissoes],
        modulos: [...modulos],
        isAdmin,
        isSuperuser,
        isDiretoria,
        pode,
        temModulo,
        somenteLeitura
    };
};

const toSet = (valor) => {
    if (!valor) return new Set();
    if (valor instanceof Set) return valor;
    if (Array.isArray(valor)) return new Set(valor.map(String));
    return new Set();
};

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const checkAuthStatus = async () => {
            try {
                const userData = await authService.getCurrentUser();
                if (userData) {
                    setUser(enriquecer(userData));
                    // /api/auth/me traz perfis e permissoes; sem isso o menu
                    // de Administracao nunca aparece, pois le user.isAdmin.
                    try {
                        const perfil = await authService.loadProfile();
                        if (perfil) setUser(enriquecer({ ...userData, ...perfil }));
                    } catch (erro) {
                        console.warn('Não foi possível carregar o perfil do usuário', erro);
                    }
                }
            } catch (error) {
                console.error('Erro ao verificar autenticação:', error);
            } finally {
                setLoading(false);
            }
        };

        checkAuthStatus();
    }, []);

    const login = async (credentials, provider = 'ad') => {
        try {
            const userData = await authService.login(credentials, provider);
            setUser(enriquecer(userData));
            // O login so devolve authorities; /api/auth/me traz os perfis usados
            // para liberar o menu de Administracao.
            try {
                const perfil = await authService.loadProfile();
                if (perfil) setUser(enriquecer({ ...userData, ...perfil }));
            } catch (erro) {
                console.warn('Não foi possível carregar o perfil do usuário', erro);
            }
            return { success: true };
        } catch (error) {
            return { success: false, message: error.message };
        }
    };

    const logout = async () => {
        try {
            await authService.logout();
            setUser(null);
        } catch (error) {
            console.error('Erro ao fazer logout:', error);
        }
    };

    const value = {
        user,
        isAuthenticated: !!user,
        // exposto na raiz para o Layout e as telas filhas sem passar por `user`
        isAdmin: !!user?.isAdmin,
        isSuperuser: !!user?.isSuperuser,
        // filtro de menu por modulo liberado
        temModulo: user?.temModulo || (() => true),
        somenteLeitura: user?.somenteLeitura || (() => false),
        // `pode` era calculado mas nao exposto na raiz. SqlConsole.jsx faz
        // `pode('superadmin','sql')` direto no useAuth(), entao o valor era
        // undefined e, quando isSuperuser era falso, o `||` avaliava o lado
        // direito: "pode is not a function" no render, derrubando a tela.
        pode: user?.pode || (() => false),
        modulos: user?.modulos || [],
        login,
        logout,
        loading
    };

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
};
