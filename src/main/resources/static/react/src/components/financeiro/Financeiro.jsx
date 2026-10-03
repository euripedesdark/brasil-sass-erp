import React from 'react';
import { useTranslation } from 'react-i18next';
import { Card } from 'primereact/card';
import { Button } from 'primereact/button';
import { useNavigate } from 'react-router-dom';

export const Financeiro = () => {
    const { t } = useTranslation();
    const navigate = useNavigate();

    return (
        <div className="financeiro-enterprise-container">
            <div className="financeiro-grid">
                <Card className="fin-card" onClick={() => navigate('/financeiro/lancamentos')}>
                    <div className="fin-card-icon pi pi-wallet"></div>
                    <h3>Lançamentos</h3>
                    <p>Contas a Pagar e Receber</p>
                    <Button label="Acessar" icon="pi pi-arrow-right" className="p-button-text" />
                </Card>

                <Card className="fin-card" onClick={() => navigate('/financeiro/comissoes')}>
                    <div className="fin-card-icon pi pi-money-bill"></div>
                    <h3>Comissões</h3>
                    <p>Pagamentos de Vendedores e Equipe</p>
                    <Button label="Acessar" icon="pi pi-arrow-right" className="p-button-text" />
                </Card>

                <Card className="fin-card" onClick={() => navigate('/financeiro/extrato')}>
                    <div className="fin-card-icon pi pi-chart-line"></div>
                    <h3>Extratos</h3>
                    <p>Movimentação de Contas Caixa</p>
                    <Button label="Acessar" icon="pi pi-arrow-right" className="p-button-text" />
                </Card>

                <Card className="fin-card" onClick={() => navigate('/financeiro/contabil')}>
                    <div className="fin-card-icon pi pi-book"></div>
                    <h3>Contabilidade</h3>
                    <p>Plano de Contas e Lançamentos</p>
                    <Button label="Acessar" icon="pi pi-arrow-right" className="p-button-text" />
                </Card>
            </div>

            <style>{`
.financeiro-grid{display:grid;grid-template-columns:repeat(12,1fr);gap:1vh 1vw;width:100%}
.fin-card{grid-column:span 3;cursor:pointer;transition:transform .15s,box-shadow .15s;text-align:left;border-radius:.8vw;border:1px solid rgba(148,190,235,.22);background:linear-gradient(145deg,rgba(15,34,60,.62),rgba(8,22,42,.54));color:#eaf3ff}
.fin-card:hover{transform:translateY(-2px);box-shadow:0 12px 28px rgba(0,20,55,.3)}
.fin-card-icon{font-size:2.2rem;color:#7db7ff;margin-bottom:1vh}
.fin-card h3{margin:0;font-size:1.2rem;color:#eaf3ff}
.fin-card p{color:#4d94f0;font-size:.9rem;margin-bottom:1rem}
@media(max-width:1100px){.fin-card{grid-column:span 6}}
@media(max-width:650px){.fin-card{grid-column:span 12;border-radius:2vw}}
`}</style>
        </div>
    );
};
