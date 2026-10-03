import React from 'react';
import { Button } from 'primereact/button';
import i18n from '../../i18n';

/**
 * Barreira de erro por tela.
 *
 * Sem isto, um TypeError no render de qualquer tela derrubava a arvore React
 * inteira: sumiam o menu lateral, o cabecalho e o conteudo, e sobrava o fundo.
 * Era por isso que uma falha pequena em um cadastro parecia "a rota nao abre"
 * ou "o sistema quebrou" — o unico sintoma era uma pagina em branco.
 *
 * Aqui a falha fica contida na tela, com o motivo escrito na tela, e o resto
 * do aplicativo continua de pe. O menu continua navegavel, entao da para sair
 * da tela quebrada sem recarregar.
 */
export class ErrorBoundary extends React.Component {
    constructor(props) {
        super(props);
        this.state = { erro: null, info: null };
    }

    static getDerivedStateFromError(erro) {
        return { erro };
    }

    componentDidCatch(erro, info) {
        this.setState({ info });
        // O console e o unico lugar onde o stack completo fica registrado; a
        // tela mostra so a mensagem, para nao expor detalhe interno a quem
        // opera o sistema.
        console.error('Falha ao renderizar a tela', erro, info);
    }

    recarregar = () => {
        this.setState({ erro: null, info: null });
    };

    render() {
        const { erro, info } = this.state;
        if (!erro) return this.props.children;

        return (
            <div className="bc-404">
                <div className="bc-404-card">
                    <div className="bc-404-codigo" aria-hidden="true">!</div>
                    <h1 className="bc-404-titulo">{i18n.t('errors.screenFailed')}</h1>
                    <p className="bc-404-texto">
                        {i18n.t('errors.screenFailedMessage')}
                    </p>
                    <pre className="bc-404-detalhe">
                        {String(erro?.message || erro)}
                        {info?.componentStack
                            ? info.componentStack.split('\n').slice(0, 4).join('\n')
                            : ''}
                    </pre>
                    <div className="bc-404-acoes">
                        <Button
                            label={i18n.t('common.tryAgain')}
                            icon="pi pi-refresh"
                            onClick={this.recarregar}
                        />
                        <Button
                            label={i18n.t('notFound.home')}
                            icon="pi pi-home"
                            severity="secondary"
                            text
                            onClick={() => {
                                if (this.props.onSair) this.props.onSair();
                                this.recarregar();
                            }}
                        />
                    </div>
                </div>
            </div>
        );
    }
}

export default ErrorBoundary;
