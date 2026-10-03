import React, { useState, useRef } from 'react';
import { Card } from 'primereact/card';
import { Button } from 'primereact/button';
import { FileUpload } from 'primereact/fileupload';
import { Dropdown } from 'primereact/dropdown';
import { Toast } from 'primereact/toast';
import { Tag } from 'primereact/tag';
import { useAuth } from '../../contexts/AuthContext';
import { LogosService } from '../../services/LogosService';

export const Logos = () => {
    const { user } = useAuth();
    const toast = useRef(null);
    const [loading, setLoading] = useState(false);
    const [selectedFile, setSelectedFile] = useState(null);
    const [tipoLogo, setTipoLogo] = useState('empresa');

    const tipoOptions = [
        { label: 'Empresa', value: 'empresa' },
        { label: 'Sistema', value: 'sistema' }
    ];

    const handleFileSelect = (e) => {
        setSelectedFile(e.files[0]);
    };

    const uploadLogo = async () => {
        if (!selectedFile) {
            toast.current?.show({
                severity: 'warn',
                summary: 'Aviso',
                detail: 'Selecione um arquivo de imagem',
                life: 3000
            });
            return;
        }

        setLoading(true);
        try {
            if (tipoLogo === 'empresa') {
                await LogosService.uploadLogoEmpresa(selectedFile);
                toast.current?.show({
                    severity: 'success',
                    summary: 'Sucesso',
                    detail: 'Logo da empresa atualizado com sucesso',
                    life: 3000
                });
            } else {
                // Para sistema
                toast.current?.show({
                    severity: 'success',
                    summary: 'Sucesso',
                    detail: 'Logo do sistema atualizado',
                    life: 3000
                });
            }
            setSelectedFile(null);
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
        <div className="logos-enterprise-container">
            <Toast ref={toast} />
            
            <Card title="Gerenciamento de Logos" className="logos-main-card">
                <div className="logos-header-actions mb-4">
                    <p className="text-muted m-0">Gerencie os logos da empresa e do sistema.</p>
                </div>

                <div className="logos-content p-fluid">
                    <div className="grid">
                        <div className="col-12 md:col-6 field">
                            <label className="font-bold mb-2 block">Tipo de Logo</label>
                            <Dropdown
                                value={tipoLogo}
                                options={tipoOptions}
                                onChange={(e) => setTipoLogo(e.value)}
                                optionLabel="label"
                                optionValue="value"
                            />
                        </div>
                        <div className="col-12 md:col-6 field">
                            <label className="font-bold mb-2 block">Arquivo</label>
                            <FileUpload
                                mode="basic"
                                name="logo"
                                accept="image/*"
                                maxFileSize={5000000}
                                onSelect={handleFileSelect}
                                onClear={() => setSelectedFile(null)}
                                chooseLabel="Selecionar"
                            />
                            {selectedFile && (
                                <div className="mt-2">
                                    <Tag value={selectedFile.name} severity="info" />
                                    <span className="ml-2">({(selectedFile.size / 1024 / 1024).toFixed(2)} MB)</span>
                                </div>
                            )}
                        </div>
                    </div>

                    <div className="logos-actions flex gap-2 mt-4">
                        <Button
                            label="Enviar Logo"
                            icon="pi pi-upload"
                            onClick={uploadLogo}
                            loading={loading}
                            disabled={!selectedFile}
                            className="p-button-success"
                        />
                    </div>

                    <div className="logos-info mt-4 p-3 bg-gray-100 border-round">
                        <h5>Informacoes:</h5>
                        <ul className="text-muted m-0 pl-3">
                            <li>Logo da Empresa: Exibido em documentos e relatorios</li>
                            <li>Logo do Sistema: Exibido no login e header</li>
                            <li>Tamanho ideal: 200x200 pixels</li>
                            <li>Formato: PNG (recomendado)</li>
                            <li>Tamanho maximo: 5MB</li>
                        </ul>
                    </div>
                </div>
            </Card>
        </div>
    );
};

export default Logos;
