import React, { useState, useRef } from 'react';
import { useTranslation } from 'react-i18next';
import { Card } from 'primereact/card';
import { Button } from 'primereact/button';
import { InputText } from 'primereact/inputtext';
import { Dropdown } from 'primereact/dropdown';
import { Toast } from 'primereact/toast';
import { useAuth } from '../../contexts/AuthContext';
import { SefazConsultaService } from '../../services/SefazConsultaService';

export const SefazConsulta = () => {
    const { t } = useTranslation();
    const { user } = useAuth();
    const toast = useRef(null);
    const [loading, setLoading] = useState(false);
    const [uf, setUf] = useState('SP');
    const [chave, setChave] = useState('');
    const [statusResult, setStatusResult] = useState(null);
    const [consultaResult, setConsultaResult] = useState(null);

    const ufOptions = [
        { label: 'AC', value: 'AC' }, { label: 'AL', value: 'AL' }, { label: 'AP', value: 'AP' },
        { label: 'AM', value: 'AM' }, { label: 'BA', value: 'BA' }, { label: 'CE', value: 'CE' },
        { label: 'DF', value: 'DF' }, { label: 'ES', value: 'ES' }, { label: 'GO', value: 'GO' },
        { label: 'MA', value: 'MA' }, { label: 'MT', value: 'MT' }, { label: 'MS', value: 'MS' },
        { label: 'MG', value: 'MG' }, { label: 'PA', value: 'PA' }, { label: 'PB', value: 'PB' },
        { label: 'PR', value: 'PR' }, { label: 'PE', value: 'PE' }, { label: 'PI', value: 'PI' },
        { label: 'RJ', value: 'RJ' }, { label: 'RN', value: 'RN' }, { label: 'RS', value: 'RS' },
        { label: 'RO', value: 'RO' }, { label: 'RR', value: 'RR' }, { label: 'SC', value: 'SC' },
        { label: 'SP', value: 'SP' }, { label: 'SE', value: 'SE' }, { label: 'TO', value: 'TO' }
    ];

    const consultarStatus = async () => {
        setLoading(true);
        try {
            const result = await SefazConsultaService.status(uf);
            setStatusResult(result);
            toast.current?.show({
                severity: 'success',
                summary: t('legacyUi.sefaz.success'),
                detail: t('legacyUi.sefaz.statusSuccess'),
                life: 3000
            });
        } catch (err) {
            toast.current?.show({
                severity: 'error',
                summary: 'Erro',
                detail: err.message,
                life: 3000
            });
        } finally {
            setLoading(false);
        }
    };

    const consultarNota = async () => {
        if (!chave) {
            toast.current?.show({
                severity: 'warn',
                summary: 'Aviso',
                detail: t('legacyUi.sefaz.keyRequired'),
                life: 3000
            });
            return;
        }
        setLoading(true);
        try {
            const result = await SefazConsultaService.consultar(uf, chave);
            setConsultaResult(result);
            toast.current?.show({
                severity: 'success',
                summary: t('legacyUi.sefaz.success'),
                detail: t('legacyUi.sefaz.invoiceSuccess'),
                life: 3000
            });
        } catch (err) {
            toast.current?.show({
                severity: 'error',
                summary: 'Erro',
                detail: err.message,
                life: 3000
            });
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="sefazconsulta-enterprise-container">
            <Toast ref={toast} />
            
            <Card title={t('legacyUi.sefaz.title')} className="sefazconsulta-main-card">
                <div className="sefazconsulta-header-actions mb-4">
                    <p className="text-muted m-0">Consultas a servicos da SEFAZ para validacao de notas fiscais.</p>
                </div>

                <div className="grid p-fluid">
                    <div className="col-12 md:col-6 field">
                        <label className="font-bold mb-2 block">UF</label>
                        <Dropdown
                            value={uf}
                            options={ufOptions}
                            onChange={(e) => setUf(e.value)}
                            optionLabel="label"
                            optionValue="value"
                        />
                    </div>
                    <div className="col-12 md:col-6 field">
                        <label className="font-bold mb-2 block">Chave de Acesso</label>
                        <InputText
                            value={chave}
                            onChange={(e) => setChave(e.target.value)}
                            placeholder={t('legacyUi.sefaz.accessKey')}
                        />
                    </div>
                </div>

                <div className="sefazconsulta-actions flex gap-2 mt-4">
                    <Button
                        label={t('legacyUi.sefaz.status')}
                        icon="pi pi-check"
                        onClick={consultarStatus}
                        loading={loading}
                        className="p-button-success"
                    />
                    <Button
                        label={t('legacyUi.sefaz.invoice')}
                        icon="pi pi-file"
                        onClick={consultarNota}
                        loading={loading}
                        className="p-button-info"
                    />
                </div>

                {statusResult && (
                    <Card title="Resultado - Status" className="mt-4">
                        <pre>{JSON.stringify(statusResult, null, 2)}</pre>
                    </Card>
                )}

                {consultaResult && (
                    <Card title="Resultado - Consulta NF-e" className="mt-4">
                        <pre>{JSON.stringify(consultaResult, null, 2)}</pre>
                    </Card>
                )}
            </Card>
        </div>
    );
};

export default SefazConsulta;
