import { useTranslation } from 'react-i18next';
import React, { useState, useEffect } from 'react';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { InputText } from 'primereact/inputtext';
import { Button } from 'primereact/button';
import { Dialog } from 'primereact/dialog';
import { Toast } from 'primereact/toast';
import { Paginator } from 'primereact/paginator';
import { MunicipioService } from '../services/MunicipioService';
import './Municipios.css';

export default function Municipios() {
    const { t } = useTranslation();
    const [municipios, setMunicipios] = useState([]);
    const [loading, setLoading] = useState(false);
    const [totalRecords, setTotalRecords] = useState(0);
    const [first, setFirst] = useState(0);
    const [rows, setRows] = useState(20);
    const [dialogVisible, setDialogVisible] = useState(false);
    const [municipioEditando, setMunicipioEditando] = useState(null);
    const [formData, setFormData] = useState({ codigoIbge: '', nome: '', uf: '' });
    const [buscaCodigo, setBuscaCodigo] = useState('');
    const [buscaNome, setBuscaNome] = useState('');
    
    const toast = React.useRef(null);

    useEffect(() => {
        carregarMunicipios();
    }, [first, rows]);

    const carregarMunicipios = async (
        codigo = buscaCodigo,
        nome = buscaNome,
        page = Math.floor(first / rows)
    ) => {
        setLoading(true);
        try {
            const response = await MunicipioService.buscar(codigo, nome, page, rows);
            setMunicipios(response.content);
            setTotalRecords(response.totalElements);
        } catch (error) {
            toast.current.show({
                severity: 'error',
                summary: t('common.error'),
                detail: t('legacyUi.municipios.loadError')
            });
        } finally {
            setLoading(false);
        }
    };

    const onPageChange = (event) => {
        setFirst(event.first);
        setRows(event.rows);
    };

    const buscarPorCodigo = async () => {
        if (buscaCodigo.length < 7) {
            toast.current.show({
                severity: 'warn',
                summary: t('common.warning'),
                detail: t('legacyUi.municipios.minCode')
            });
            return;
        }
        setBuscaNome('');
        setFirst(0);
        await carregarMunicipios(buscaCodigo, '', 0);
    };

    const buscarPorNome = async () => {
        if (buscaNome.length < 3) {
            toast.current.show({
                severity: 'warn',
                summary: t('common.warning'),
                detail: t('legacyUi.municipios.minName')
            });
            return;
        }
        setBuscaCodigo('');
        setFirst(0);
        await carregarMunicipios('', buscaNome, 0);
    };

    const limparBusca = () => {
        setBuscaCodigo('');
        setBuscaNome('');
        setFirst(0);
        carregarMunicipios('', '', 0);
    };

    const abrirNovo = () => {
        setMunicipioEditando(null);
        setFormData({ codigoIbge: '', nome: '', uf: '' });
        setDialogVisible(true);
    };

    const abrirEdicao = (municipio) => {
        setMunicipioEditando(municipio);
        setFormData({
            codigoIbge: municipio.codigoIbge || '',
            nome: municipio.nome || '',
            uf: municipio.uf || ''
        });
        setDialogVisible(true);
    };

    const salvar = async () => {
        try {
            if (municipioEditando) {
                await MunicipioService.atualizar(municipioEditando.id, formData);
                toast.current.show({
                    severity: 'success',
                    summary: t('messages.success'),
                    detail: t('legacyUi.municipios.updated')
                });
            } else {
                await MunicipioService.salvar(formData);
                toast.current.show({
                    severity: 'success',
                    summary: t('messages.success'),
                    detail: t('legacyUi.municipios.created')
                });
            }
            setDialogVisible(false);
            carregarMunicipios();
        } catch (error) {
            toast.current.show({
                severity: 'error',
                summary: t('common.error'),
                detail: t('legacyUi.municipios.saveError')
            });
        }
    };

    const excluir = async (municipio) => {
        try {
            await MunicipioService.excluir(municipio.id);
            toast.current.show({
                severity: 'success',
                summary: t('messages.success'),
                detail: t('legacyUi.municipios.deleted')
            });
            carregarMunicipios();
        } catch (error) {
            toast.current.show({
                severity: 'error',
                summary: t('common.error'),
                detail: t('legacyUi.municipios.deleteError')
            });
        }
    };

    const acoesBodyTemplate = (municipio) => {
        return (
            <div className="acao-buttons">
                <Button 
                    icon="pi pi-pencil" 
                    className="p-button-rounded p-button-warning p-button-sm" 
                    onClick={() => abrirEdicao(municipio)}
                />
                <Button 
                    icon="pi pi-trash" 
                    className="p-button-rounded p-button-danger p-button-sm" 
                    onClick={() => excluir(municipio)}
                />
            </div>
        );
    };

    const dialogFooter = (
        <div>
            <Button 
                label={t('common.cancel')} 
                icon="pi pi-times" 
                className="p-button-text" 
                onClick={() => setDialogVisible(false)} 
            />
            <Button 
                label={t('common.save')} 
                icon="pi pi-check" 
                className="p-button-text" 
                onClick={salvar} 
            />
        </div>
    );

    return (
        <div className="municipios-container">
            <Toast ref={toast} />
            
            <div className="card-header">
                <h1>Municípios (IBGE)</h1>
                <Button 
                    label={t('legacyUi.municipios.new')} 
                    icon="pi pi-plus" 
                    onClick={abrirNovo}
                    className="p-button-success"
                />
            </div>

            <div className="busca-container">
                <div className="busca-field">
                    <label>Código IBGE:</label>
                    <InputText 
                        value={buscaCodigo} 
                        onChange={(e) => setBuscaCodigo(e.target.value)}
                        placeholder={t('legacyUi.municipios.searchCode')}
                        className="w-15rem"
                    />
                    <Button 
                        label={t('legacyUi.municipios.byCode')} 
                        icon="pi pi-search" 
                        onClick={buscarPorCodigo}
                        className="p-button-info ml-2"
                    />
                </div>
                
                <div className="busca-field">
                    <label>Nome do Município:</label>
                    <InputText 
                        value={buscaNome} 
                        onChange={(e) => setBuscaNome(e.target.value)}
                        placeholder={t('legacyUi.municipios.searchName')}
                        className="w-15rem"
                    />
                    <Button 
                        label={t('legacyUi.municipios.byName')} 
                        icon="pi pi-search" 
                        onClick={buscarPorNome}
                        className="p-button-info ml-2"
                    />
                </div>
                
                <Button 
                    label={t('legacyUi.municipios.clear')} 
                    icon="pi pi-refresh" 
                    onClick={limparBusca}
                    className="p-button-secondary ml-2"
                />
            </div>

            <DataTable 
                value={municipios}
                loading={loading}
                paginatorTemplate="CurrentPageReport FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink RowsPerPageDropdown"
                currentPageReportTemplate="Mostrando {first} a {last} de {totalRecords} municípios"
                rows={rows}
                first={first}
                totalRecords={totalRecords}
                onPageChange={onPageChange}
                rowsPerPageOptions={[10, 20, 50, 100]}
                emptyMessage={t('legacyUi.municipios.empty')}
            >
                <Column field="codigoIbge" header={t('legacyUi.municipios.code')} sortable style={{ width: '15%' }} />
                <Column field="nome" header={t('legacyUi.municipios.name')} sortable style={{ width: '60%' }} />
                <Column field="uf" header="UF" sortable style={{ width: '10%' }} />
                <Column body={acoesBodyTemplate} header={t('legacyUi.municipios.actions')} style={{ width: '15%' }} />
            </DataTable>

            <Dialog 
                visible={dialogVisible} 
                style={{ width: '500px' }} 
                header={municipioEditando ? t('legacyUi.municipios.edit') : t('legacyUi.municipios.new')}
                footer={dialogFooter}
                onHide={() => setDialogVisible(false)}
            >
                <div className="form-grid">
                    <div className="field">
                        <label htmlFor="codigoibge">Código IBGE</label>
                        <InputText 
                            id="codigoibge" 
                            value={formData.codigoIbge}
                            onChange={(e) => setFormData({...formData, codigoIbge: e.target.value})}
                            className="w-full"
                        />
                    </div>
                    <div className="field">
                        <label htmlFor="nome">Nome do Município</label>
                        <InputText 
                            id="nome" 
                            value={formData.nome}
                            onChange={(e) => setFormData({...formData, nome: e.target.value})}
                            className="w-full"
                        />
                    </div>
                    <div className="field">
                        <label htmlFor="uf">UF</label>
                        <InputText 
                            id="uf" 
                            value={formData.uf}
                            onChange={(e) => setFormData({...formData, uf: e.target.value.toUpperCase()})}
                            maxLength={2}
                            className="w-full"
                        />
                    </div>
                </div>
            </Dialog>
        </div>
    );
}
