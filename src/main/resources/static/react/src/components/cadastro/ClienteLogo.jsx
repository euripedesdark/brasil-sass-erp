import { useTranslation } from 'react-i18next';
import React, { useState, useEffect, useRef } from 'react';
import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { Dialog } from 'primereact/dialog';
import { FileUpload } from 'primereact/fileupload';
import { Toast } from 'primereact/toast';
import { Tag } from 'primereact/tag';
import { ProgressBar } from 'primereact/progressbar';
import { useAuth } from '../../contexts/AuthContext';
import { ClienteLogoService } from '../../services/ClienteLogoService';
import { ClienteService } from '../../services/ClienteService';
import './ClienteLogo.css';

export const ClienteLogo = () => {
    const { t } = useTranslation();
    const { user } = useAuth();
    const toast = useRef(null);
    const [clientes, setClientes] = useState([]);
    const [loading, setLoading] = useState(false);
    const [dialogVisible, setDialogVisible] = useState(false);
    const [selectedCliente, setSelectedCliente] = useState(null);
    const [uploading, setUploading] = useState(false);
    const [uploadProgress, setUploadProgress] = useState(0);
    const [imagePreview, setImagePreview] = useState(null);

    useEffect(() => {
        fetchClientes();
    }, []);

    const fetchClientes = async () => {
        setLoading(true);
        try {
            const data = await ClienteService.listar(0, 100);
            setClientes(data.content || data || []);
        } catch (err) {
            console.error('Erro ao carregar clientes', err);
            toast.current?.show({
                severity: 'error',
                summary: 'Erro',
                detail: 'Nao foi possivel carregar os clientes',
                life: 3000
            });
        } finally {
            setLoading(false);
        }
    };

    const handleUpload = async (event) => {
        const file = event.files[0];
        if (!file) return;

        if (!selectedCliente) {
            toast.current?.show({
                severity: 'warn',
                summary: 'Aviso',
                detail: 'Selecione um cliente primeiro',
                life: 3000
            });
            return;
        }

        if (!file.type.startsWith('image/')) {
            toast.current?.show({
                severity: 'error',
                summary: 'Erro',
                detail: 'Por favor, selecione um arquivo de imagem',
                life: 3000
            });
            return;
        }

        setUploading(true);
        setUploadProgress(0);

        try {
            // Criar preview da imagem
            const reader = new FileReader();
            reader.onload = (e) => {
                setImagePreview(e.target.result);
            };
            reader.readAsDataURL(file);

            // Upload da imagem
            await ClienteLogoService.uploadLogo(selectedCliente.id, file);
            
            toast.current?.show({
                severity: 'success',
                summary: 'Sucesso',
                detail: 'Logo do cliente enviado com sucesso',
                life: 3000
            });
        } catch (err) {
            toast.current?.show({
                severity: 'error',
                summary: 'Erro',
                detail: err.message || 'Nao foi possivel enviar o logo',
                life: 3000
            });
        } finally {
            setUploading(false);
            setUploadProgress(100);
        }
    };

    const handleDelete = async (clienteId) => {
        if (!window.confirm('Tem certeza que deseja remover o logo deste cliente?')) {
            return;
        }

        try {
            await ClienteLogoService.deleteLogo(clienteId);
            toast.current?.show({
                severity: 'success',
                summary: 'Sucesso',
                detail: 'Logo do cliente removido com sucesso',
                life: 3000
            });
            // Recarregar a lista
            fetchClientes();
            setImagePreview(null);
        } catch (err) {
            toast.current?.show({
                severity: 'error',
                summary: 'Erro',
                detail: err.message || 'Nao foi possivel remover o logo',
                life: 3000
            });
        }
    };

    const abrirDialog = (cliente) => {
        setSelectedCliente(cliente);
        setImagePreview(null);
        setDialogVisible(true);
        
        // Tentar carregar a logo existente
        if (cliente && cliente.hasLogo) {
            loadLogo(cliente.id);
        }
    };

    const loadLogo = async (clienteId) => {
        try {
            const blob = await ClienteLogoService.getLogo(clienteId);
            if (blob) {
                const url = URL.createObjectURL(blob);
                setImagePreview(url);
            }
        } catch (err) {
            // Logo nao encontrado, ignorar
            console.log('Logo nao encontrado para o cliente:', clienteId);
        }
    };

    const hasLogoTemplate = (rowData) => {
        return (
            <Tag
                value={rowData.hasLogo ? 'SIM' : 'NAO'}
                severity={rowData.hasLogo ? 'success' : 'warning'}
            />
        );
    };

    const acoesTemplate = (rowData) => {
        return (
            <div className="cliente-logo-acoes">
                <Button
                    icon="pi pi-image"
                    className="p-button-info p-button-sm p-button-text"
                    onClick={() => abrirDialog(rowData)}
                    tooltip="Gerenciar Logo"
                />
            </div>
        );
    };

    const dialogFooter = (
        <div>
            <Button
                label="Fechar"
                icon="pi pi-times"
                className="p-button-text"
                onClick={() => {
                    setDialogVisible(false);
                    setSelectedCliente(null);
                    setImagePreview(null);
                }}
            />
        </div>
    );

    return (
        <div className="cliente-logo-enterprise-container">
            <Toast ref={toast} />
            
            <Card title="Logos de Clientes" className="cliente-logo-main-card">
                <div className="cliente-logo-header-actions mb-4 flex justify-content-between align-items-center">
                    <div className="cliente-logo-info">
                        <p className="text-muted m-0">Gerencie os logos dos clientes da empresa.</p>
                    </div>
                </div>

                <DataTable
                    value={clientes}
                    loading={loading}
                    paginator
                    rows={10}
                    rowsPerPageOptions={[10, 20, 50, 100]}
                    responsiveLayout="scroll"
                    className="p-datatable-sm"
                    emptyMessage="Nenhum cliente encontrado"
                >
                    <Column field="nome" header="Cliente" sortable style={{ width: '300px' }} />
                    <Column field="cpfCnpj" header="CPF/CNPJ" sortable style={{ width: '150px' }} />
                    <Column body={hasLogoTemplate} header="Possui Logo" sortable style={{ width: '120px' }} />
                    <Column body={acoesTemplate} style={{ width: '120px' }} />
                </DataTable>
            </Card>

            <Dialog
                header={selectedCliente ? `Logo: ${selectedCliente.nome}` : 'Logo do Cliente'}
                visible={dialogVisible}
                style={{ width: '600px' }}
                onHide={() => {
                    setDialogVisible(false);
                    setSelectedCliente(null);
                    setImagePreview(null);
                }}
                footer={dialogFooter}
                maximizable
            >
                <div className="p-fluid">
                    {uploading && (
                        <div className="upload-progress mb-3">
                            <ProgressBar value={uploadProgress} displayValueTemplate={(value) => `${value}%`} />
                            <p className="mt-2 text-center text-muted">Enviando imagem...</p>
                        </div>
                    )}

                    {imagePreview && !uploading && (
                        <div className="logo-preview mb-4">
                            <div className="logo-preview-title mb-2">
                                <span className="font-bold">Logo Atual:</span>
                            </div>
                            <img
                                src={imagePreview}
                                alt="Logo do cliente"
                                className="logo-preview-image"
                            />
                            <div className="logo-preview-actions mt-3">
                                <Button
                                    label="Remover Logo"
                                    icon="pi pi-trash"
                                    className="p-button-danger p-button-sm"
                                    onClick={() => handleDelete(selectedCliente.id)}
                                    disabled={uploading}
                                />
                            </div>
                        </div>
                    )}

                    {!imagePreview && !uploading && (
                        <div className="upload-area mb-4">
                            <p className="text-muted mb-3">
                                {selectedCliente?.hasLogo ? 'Nenhum logo carregado no momento.' : 'Este cliente nao tem logo cadastrado.'}
                            </p>
                        </div>
                    )}

                    <div className="upload-section">
                        <FileUpload
                            mode="basic"
                            name="logo"
                            accept="image/*"
                            maxFileSize={1000000}
                            customUpload
                            uploadHandler={handleUpload}
                            chooseLabel="Selecionar Imagem"
                            disabled={uploading}
                            auto
                        />
                        <p className="text-muted mt-2 text-sm">
                            Formatos aceitos: JPG, PNG, GIF. Tamanho max: 1MB
                        </p>
                        <p className="text-muted text-sm">
                            Recomendado: 300x100 pixels para melhor visualizacao
                        </p>
                    </div>
                </div>
            </Dialog>
        </div>
    );
};

export default ClienteLogo;
