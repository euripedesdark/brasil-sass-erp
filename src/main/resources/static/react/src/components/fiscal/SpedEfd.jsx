import React, { useState, useRef } from 'react';
import { Card } from 'primereact/card';
import { Button } from 'primereact/button';
import { InputText } from 'primereact/inputtext';
import { Dropdown } from 'primereact/dropdown';
import { Toast } from 'primereact/toast';
import { ProgressSpinner } from 'primereact/progressspinner';
import { SpedService } from '../../services/SpedService';
import { useTranslation } from 'react-i18next';

const initialForm = {
    competencia: '',
    cnpj: '',
    nome: '',
    uf: 'SP',
    ie: '',
    codMun: '',
    im: '',
    indPerfil: 'A',
    indAtiv: '1',
    cep: '',
    endereco: '',
    numero: '',
    complemento: '',
    bairro: '',
    telefone: '',
    email: ''
};

const ufOptions = [
    'AC', 'AL', 'AM', 'AP', 'BA', 'CE', 'DF', 'ES', 'GO', 'MA', 'MG', 'MS',
    'MT', 'PA', 'PB', 'PE', 'PI', 'PR', 'RJ', 'RN', 'RO', 'RR', 'RS', 'SC',
    'SE', 'SP', 'TO'
].map((value) => ({ label: value, value }));

const downloadText = (content, competencia) => {
    const blob = new Blob([content], { type: 'text/plain;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `SPED-EFD-${competencia || 'gerado'}.txt`;
    link.click();
    URL.revokeObjectURL(url);
};

export const SpedEfd = () => {
    const { t } = useTranslation();
    const toast = useRef(null);
    const [form, setForm] = useState(initialForm);
    const [resultado, setResultado] = useState(null);
    const [loading, setLoading] = useState(false);

    const update = (field, value) => setForm((current) => ({ ...current, [field]: value }));

    const executar = async (acao) => {
        if (acao === 'gerar' && (!form.competencia || !form.cnpj || !form.nome || !form.uf)) {
            toast.current?.show({ severity: 'warn', summary: t('common.required'), detail: t('sped.required'), life: 3500 });
            return;
        }

        setLoading(true);
        try {
            const resposta = acao === 'exemplo' ? await SpedService.exemplo() : acao === 'contrib' ? await SpedService.gerarContribPeriodo({ ...form, produtos: [], participantes: [] }) : acao === 'periodo' ? await SpedService.gerarPeriodo({ ...form, produtos: [], participantes: [] }) : await SpedService.gerar({ ...form, produtos: [], participantes: [] });
            setResultado(resposta);
            toast.current?.show({ severity: 'success', summary: t('sped.generated'), detail: t('sped.linesGenerated', { count: resposta.totalLinhas || 0 }), life: 3500 });
        } catch (error) {
            toast.current?.show({ severity: 'error', summary: t('sped.generationFailed'), detail: error.message, life: 5000 });
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="p-3">
            <Toast ref={toast} />
            <div className="flex justify-content-between align-items-start gap-3 flex-wrap mb-3">
                <div>
                    <h2 className="m-0">SPED EFD ICMS/IPI</h2>
                    <p className="text-color-secondary mt-2 mb-0">{t('sped.description')}</p>
                </div>
                <Button label={t('sped.officialExample')} icon="pi pi-file-check" outlined onClick={() => executar('exemplo')} disabled={loading} />
            </div>

            <Card title={t('sped.companyData')} className="mb-3">
                <div className="grid">
                    <div className="col-12 md:col-3">
                        <label htmlFor="sped-competencia" className="block mb-2">{t('sped.period')}</label>
                        <InputText id="sped-competencia" value={form.competencia} onChange={(event) => update('competencia', event.target.value)} placeholder="01/2026" className="w-full" />
                    </div>
                    <div className="col-12 md:col-3">
                        <label htmlFor="sped-cnpj" className="block mb-2">CNPJ</label>
                        <InputText id="sped-cnpj" value={form.cnpj} onChange={(event) => update('cnpj', event.target.value)} className="w-full" />
                    </div>
                    <div className="col-12 md:col-4">
                        <label htmlFor="sped-nome" className="block mb-2">{t('sped.legalName')}</label>
                        <InputText id="sped-nome" value={form.nome} onChange={(event) => update('nome', event.target.value)} className="w-full" />
                    </div>
                    <div className="col-12 md:col-2">
                        <label htmlFor="sped-uf" className="block mb-2">UF</label>
                        <Dropdown id="sped-uf" value={form.uf} options={ufOptions} onChange={(event) => update('uf', event.value)} className="w-full" />
                    </div>
                    <div className="col-12 md:col-3">
                        <label htmlFor="sped-ie" className="block mb-2">{t('sped.stateRegistration')}</label>
                        <InputText id="sped-ie" value={form.ie} onChange={(event) => update('ie', event.target.value)} className="w-full" />
                    </div>
                    <div className="col-12 md:col-3">
                        <label htmlFor="sped-cod-mun" className="block mb-2">{t('sped.cityCode')}</label>
                        <InputText id="sped-cod-mun" value={form.codMun} onChange={(event) => update('codMun', event.target.value)} className="w-full" />
                    </div>
                    <div className="col-12 md:col-3">
                        <label htmlFor="sped-perfil" className="block mb-2">{t('sped.profile')}</label>
                        <InputText id="sped-perfil" value={form.indPerfil} onChange={(event) => update('indPerfil', event.target.value)} className="w-full" />
                    </div>
                    <div className="col-12 md:col-3">
                        <label htmlFor="sped-atividade" className="block mb-2">{t('sped.activityIndicator')}</label>
                        <InputText id="sped-atividade" value={form.indAtiv} onChange={(event) => update('indAtiv', event.target.value)} className="w-full" />
                    </div>
                </div>
            </Card>

            <Card title={t('sped.reviewAndGenerate')} className="mb-3">
                <div className="flex align-items-center gap-2 flex-wrap">
                    <Button label='Gerar do periodo (NFe)' icon="pi pi-database" onClick={() => executar('periodo')} disabled={loading} />
                    <Button label='Contribuicoes do periodo (PIS/COFINS)' icon="pi pi-chart-bar" onClick={() => executar('contrib')} disabled={loading} />
                    <Button label={t('sped.generateFile')} icon="pi pi-cog" onClick={() => executar('gerar')} disabled={loading} />
                    {loading && <ProgressSpinner style={{ width: '24px', height: '24px' }} strokeWidth="4" />}
                    {resultado && <Button label={t('sped.downloadTxt')} icon="pi pi-download" severity="secondary" outlined onClick={() => downloadText(resultado.conteudo, resultado.competencia)} />}
                </div>
                {resultado && (
                    <div className="mt-3 surface-50 border-round p-3">
                        <strong>{t('sped.fileReady')}</strong>
                        <div className="text-color-secondary mt-2">{t('sped.fileSummary', { period: resultado.competencia || form.competencia, count: resultado.totalLinhas || 0 })}</div>
                    </div>
                )}
            </Card>
        </div>
    );
};

export default SpedEfd;
