import React from 'react';
import { Card } from 'primereact/card';
import { Button } from 'primereact/button';
import { Tag } from 'primereact/tag';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

const cards = [
  ['menu.titles','finance.titlesDesc','pi pi-wallet','/financeiro/titulos'],
  ['menu.titleApprovals','finance.approvalsDesc','pi pi-verified','/financeiro/aprovacoes-titulos'],
  ['menu.commissions','finance.commissionsDesc','pi pi-money-bill','/financeiro/comissoes'],
  ['finance.statements','finance.statementsDesc','pi pi-chart-line','/financeiro/extrato'],
  ['menu.reconciliation','finance.reconciliationDesc','pi pi-check-square','/financeiro/conciliacao'],
  ['menu.bankAccounts','finance.bankAccountsDesc','pi pi-building','/financeiro/contas-bancarias'],
  ['menu.cash','finance.cashDesc','pi pi-shopping-bag','/financeiro/caixa'],
  ['menu.chartOfAccounts','finance.chartOfAccountsDesc','pi pi-sitemap','/financeiro/plano-contas'],
  ['menu.accountingEntries','finance.accountingEntriesDesc','pi pi-book','/financeiro/contabil'],
  ['menu.costCenters','finance.costCentersDesc','pi pi-tags','/financeiro/centro-custos'],
  ['menu.paymentTerms','finance.paymentTermsDesc','pi pi-calendar','/financeiro/condicoes-pagamento'],
  ['menu.paymentTypes','finance.paymentTypesDesc','pi pi-credit-card','/financeiro/tipos-pagamento']
];

export default function FinanceiroHub() {
  const navigate = useNavigate();
  const { t } = useTranslation();
  return <div className="p-3">
    <div className="flex justify-content-between align-items-center mb-4 flex-wrap gap-3">
      <div><h2 className="m-0">{t('nav.finance')}</h2><p className="mt-2 mb-0 text-color-secondary">{t('finance.subtitle')}</p></div>
      <Tag value={t('finance.operations', { count: cards.length })} icon="pi pi-wallet" severity="info" />
    </div>
    <div className="grid">
      {cards.map(([titleKey,descKey,icon,path]) => <div className="col-12 md:col-6 xl:col-4" key={path}>
        <Card className="h-full">
          <div className="flex align-items-start gap-3">
            <i className={icon} style={{fontSize:'1.8rem'}} />
            <div className="flex-1"><h3 className="mt-0 mb-2">{t(titleKey)}</h3><p className="text-color-secondary mt-0">{t(descKey)}</p>
              <Button label={t('common.open')} icon="pi pi-arrow-right" outlined onClick={()=>navigate(path)} />
            </div>
          </div>
        </Card>
      </div>)}
    </div>
  </div>;
}
