#!/bin/bash

# Script para criar componentes React restantes
# Autor: Mistral Vibe
# Data: 21/09/2026

REACT_DIR="/home/euripedes/OneDrive/python/projetos-leno/GIT Repos/BRASIL-SAAS-ERP/src/main/resources/static/react/src/components"
SERVICES_DIR="$REACT_DIR/services"

# Funcao para criar componente de consulta (read-only)
criar_componentes_consulta() {
    local module=$1
    local component=$2
    local service=$3
    local title=$4
    local description=$5
    local endpoint=$6
    
    local component_file="$REACT_DIR/$module/${component}.jsx"
    local css_file="$REACT_DIR/$module/${component}.css"
    
    # Criar diretorio se nao existir
    mkdir -p "$REACT_DIR/$module"
    
    # Criar componente JSX
    cat > "$component_file" <<EOL
import React, { useState, useEffect, useRef } from 'react';
import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { InputText } from 'primereact/inputtext';
import { Toast } from 'primereact/toast';
import { useAuth } from '../contexts/AuthContext';
import { ${service} } from '../services/${service}';
import './${component}.css';

export const ${component} = () => {
    const { user } = useAuth();
    const toast = useRef(null);
    const [dataList, setDataList] = useState([]);
    const [loading, setLoading] = useState(false);
    const [searchTerm, setSearchTerm] = useState('');

    useEffect(() => {
        fetchData();
    }, []);

    const fetchData = async () => {
        setLoading(true);
        try {
            const data = await ${service}.listar();
            setDataList(data || []);
        } catch (err) {
            console.error('Erro ao carregar $title', err);
            toast.current?.show({
                severity: 'error',
                summary: 'Erro',
                detail: 'Nao foi possivel carregar $title',
                life: 3000
            });
        } finally {
            setLoading(false);
        }
    };

    const header = (
        <div className="${component.toLowerCase()}-header flex justify-content-between align-items-center">
            <span className="p-input-icon-left">
                <i className="pi pi-search" />
                <InputText
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    placeholder="Buscar..."
                />
            </span>
        </div>
    );

    return (
        <div className="${component.toLowerCase()}-enterprise-container">
            <Toast ref={toast} />
            
            <Card title="$title" className="${component.toLowerCase()}-main-card">
                <div className="${component.toLowerCase()}-header-actions mb-4">
                    <p className="text-muted m-0">$description</p>
                </div>

                <DataTable
                    value={dataList}
                    header={header}
                    loading={loading}
                    paginator
                    rows={20}
                    rowsPerPageOptions={[10, 20, 50, 100]}
                    totalRecords={dataList.length}
                    responsiveLayout="scroll"
                    className="p-datatable-sm"
                    emptyMessage="Nenhum registro encontrado"
                >
                    <Column field="codigo" header="Codigo" sortable style={{ width: '100px' }} />
                    <Column field="descricao" header="Descricao" sortable style={{ width: '500px' }} />
                </DataTable>
            </Card>
        </div>
    );
};

export default ${component};
EOL

    # Criar CSS
    cat > "$css_file" <<EOL
/* ${component}.css - Estilos para o componente $title */

.${component.toLowerCase()}-enterprise-container {
    padding: 1rem;
    animation: fadeIn 0.3s ease-in;
}

.${component.toLowerCase()}-main-card {
    margin: 0;
    border-radius: 8px;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
}

.${component.toLowerCase()}-header-actions {
    margin-bottom: 1.5rem;
}

/* DataTable */
.${component.toLowerCase()}-enterprise-container .p-datatable {
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.${component.toLowerCase()}-enterprise-container .p-datatable .p-datatable-header {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    padding: 1rem;
}

.${component.toLowerCase()}-enterprise-container .p-datatable .p-datatable-thead > tr > th {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    font-weight: 600;
    padding: 0.75rem;
}

.${component.toLowerCase()}-enterprise-container .p-datatable .p-datatable-tbody > tr > td {
    padding: 0.75rem;
    border-bottom: 1px solid #dee2e6;
}

.${component.toLowerCase()}-enterprise-container .p-datatable .p-datatable-tbody > tr:hover {
    background: rgba(102, 126, 234, 0.05);
}

/* Header de busca */
.${component.toLowerCase()}-header {
    padding: 1rem;
    background: #f8f9fa;
    border-radius: 8px;
}

.${component.toLowerCase()}-header .p-input-icon-left i {
    color: #6c757d;
}

.${component.toLowerCase()}-header .p-inputtext {
    width: 100%;
    border-radius: 4px;
    border: 1px solid #ced4da;
}

/* Animacao */
@keyframes fadeIn {
    from {
        opacity: 0;
        transform: translateY(-10px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

/* Responsive */
@media (max-width: 768px) {
    .${component.toLowerCase()}-enterprise-container {
        padding: 0.5rem;
    }
    
    .${component.toLowerCase()}-main-card {
        padding: 0.5rem;
    }
}

/* Toast */
.${component.toLowerCase()}-enterprise-container .p-toast {
    z-index: 9999;
}

.${component.toLowerCase()}-enterprise-container .p-toast .p-toast-message {
    border-radius: 8px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}
EOL

    echo "Criado: $component_file e $css_file"
}

# Criar componentes de Cadastro
criar_componentes_crud() {
    local module=$1
    local component=$2
    local service=$3
    local title=$4
    local description=$5
    local fields=$6  # formato: field1:type,field2:type
    
    local component_file="$REACT_DIR/$module/${component}.jsx"
    local css_file="$REACT_DIR/$module/${component}.css"
    
    mkdir -p "$REACT_DIR/$module"
    
    # Extrair campos do parametro fields
    IFS=',' read -ra FIELDS <<< "$fields"
    
    # Gerar estado inicial
    initial_state=""
    form_fields=""
    table_columns=""
    summary_fields=""
    
    for field in "${FIELDS[@]}"; do
        IFS=':' read -r fname ftype <<< "$field"
        initial_state+="        ${fname}: ${ftype === 'number' ? '0' : ''}${ftype === 'boolean' ? 'true' : \"\"},\n"
        form_fields+="                        <div className=\"col-12 field\">\n"
        form_fields+="                            <label className=\"font-bold mb-2 block\">${fname}</label>\n"
        if [ "$ftype" = "number" ]; then
            form_fields+="                            <InputNumber value={nova${component}.${fname}} onChange={(e) => setNova${component}({...nova${component}, ${fname}: e.value})} mode=\"currency\" currency=\"BRL\" locale=\"pt-BR\" />\n"
        else
            form_fields+="                            <InputText value={nova${component}.${fname}} onChange={(e) => setNova${component}({...nova${component}, ${fname}: e.target.value})} placeholder=\"${fname}\" />\n"
        fi
        form_fields+="                        </div>\n"
        
        table_columns+="                    <Column field=\"${fname}\" header=\"${fname}\" sortable style={{ width: '200px' }} />\n"
        summary_fields+="                            <span>{nova${component}.${fname} || 'N/A'}</span>\n"
    done
    
    # Criar componente JSX
    cat > "$component_file" <<EOL
import React, { useState, useEffect, useRef } from 'react';
import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { Dialog } from 'primereact/dialog';
import { InputText } from 'primereact/inputtext';
import { Dropdown } from 'primereact/dropdown';
import { Toast } from 'primereact/toast';
import { Tag } from 'primereact/tag';
import { Divider } from 'primereact/divider';
import { Message } from 'primereact/message';
import { useAuth } from '../contexts/AuthContext';
import { ${service} } from '../services/${service}';
import './${component}.css';

export const ${component} = () => {
    const { user } = useAuth();
    const toast = useRef(null);
    const [data, setData] = useState([]);
    const [loading, setLoading] = useState(false);
    const [dialogVisible, setDialogVisible] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState(false);

    const [nova${component}, setNova${component}] = useState({
${initial_state}    });

    useEffect(() => {
        fetchData();
    }, []);

    const fetchData = async () => {
        setLoading(true);
        try {
            const data = await ${service}.listar();
            setData(data || []);
        } catch (err) {
            console.error('Erro ao carregar $title', err);
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: 'Nao foi possivel carregar $title', life: 3000 });
        } finally {
            setLoading(false);
        }
    };

    const salvar = async () => {
        setLoading(true);
        setError('');
        setSuccess(false);
        
        try {
            if (nova${component}.id) {
                await ${service}.atualizar(nova${component}.id, nova${component});
                toast.current?.show({ severity: 'success', summary: 'Sucesso', detail: '$title atualizado com sucesso', life: 3000 });
            } else {
                await ${service}.criar(nova${component});
                toast.current?.show({ severity: 'success', summary: 'Sucesso', detail: '$title criado com sucesso', life: 3000 });
            }
            setSuccess(true);
            setTimeout(() => {
                setDialogVisible(false);
                fetchData();
                resetForm();
            }, 1500);
        } catch (err) {
            setError(err.message || 'Erro ao salvar');
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: err.message, life: 3000 });
        } finally {
            setLoading(false);
        }
    };

    const excluir = async (id) => {
        try {
            await ${service}.excluir(id);
            toast.current?.show({ severity: 'success', summary: 'Sucesso', detail: '$title excluido com sucesso', life: 3000 });
            fetchData();
        } catch (err) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: err.message, life: 3000 });
        }
    };

    const abrirDialog = (item = null) => {
        if (item) {
            setNova${component}(item);
        } else {
            resetForm();
        }
        setDialogVisible(true);
    };

    const resetForm = () => {
        setNova${component}({
${initial_state}        });
        setError('');
        setSuccess(false);
    };

    const acoesTemplate = (rowData) => {
        return (
            <div className="${component.toLowerCase()}-acoes">
                <Button icon="pi pi-pencil" className="p-button-info p-button-sm p-button-text" onClick={() => abrirDialog(rowData)} tooltip="Editar" />
                <Button icon="pi pi-trash" className="p-button-danger p-button-sm p-button-text" onClick={() => excluir(rowData.id)} tooltip="Excluir" />
            </div>
        );
    };

    const dialogFooter = (
        <div>
            <Button label="Cancelar" icon="pi pi-times" className="p-button-text" onClick={() => { setDialogVisible(false); resetForm(); }} />
            <Button label="Salvar" icon="pi pi-save" className="p-button-success" onClick={salvar} loading={loading} />
        </div>
    );

    return (
        <div className="${component.toLowerCase()}-enterprise-container">
            <Toast ref={toast} />
            
            <Card title="$title" className="${component.toLowerCase()}-main-card">
                <div className="${component.toLowerCase()}-header-actions mb-4 flex justify-content-between align-items-center">
                    <div className="${component.toLowerCase()}-info">
                        <p className="text-muted m-0">$description</p>
                    </div>
                    <Button label="Novo" icon="pi pi-plus" onClick={() => abrirDialog()} className="p-button-success" />
                </div>

                <DataTable
                    value={data}
                    loading={loading}
                    paginator
                    rows={10}
                    rowsPerPageOptions={[10, 20, 50, 100]}
                    totalRecords={data.length}
                    responsiveLayout="scroll"
                    className="p-datatable-sm"
                    emptyMessage="Nenhum registro encontrado"
                >
${table_columns}                    <Column body={acoesTemplate} style={{ width: '120px' }} />
                </DataTable>
            </Card>

            <Dialog
                header={nova${component}.id ? `Editar: ${nova${component}.nome || nova${component}.id}` : 'Novo $title'}
                visible={dialogVisible}
                style={{ width: '500px' }}
                onHide={() => { setDialogVisible(false); resetForm(); }}
                footer={dialogFooter}
                maximizable
            >
                <div className="p-fluid">
                    {error && <Message severity="error" text={error} className="w-full mb-3" />}
                    {success && <Message severity="success" text="Salvo com sucesso!" className="w-full mb-3" />}

                    <div className="grid">
${form_fields}                    </div>

                    <Divider />
                    <div className="${component.toLowerCase()}-resumo flex flex-wrap justify-content-between align-items-center p-3 bg-gray-100 border-round">
${summary_fields}                    </div>
                </div>
            </Dialog>
        </div>
    );
};

export default ${component};
EOL

    # Criar CSS basico
    cat > "$css_file" <<EOL
/* ${component}.css */

.${component.toLowerCase()}-enterprise-container {
    padding: 1rem;
    animation: fadeIn 0.3s ease-in;
}

.${component.toLowerCase()}-main-card {
    margin: 0;
    border-radius: 8px;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
}

.${component.toLowerCase()}-header-actions {
    margin-bottom: 1.5rem;
}

.${component.toLowerCase()}-acoes {
    display: flex;
    gap: 0.5rem;
    justify-content: center;
}

.${component.toLowerCase()}-enterprise-container .p-datatable {
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.${component.toLowerCase()}-enterprise-container .p-datatable .p-datatable-thead > tr > th {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    font-weight: 600;
    padding: 0.75rem;
}

.${component.toLowerCase()}-enterprise-container .p-datatable .p-datatable-tbody > tr > td {
    padding: 0.75rem;
    border-bottom: 1px solid #dee2e6;
}

.${component.toLowerCase()}-enterprise-container .p-button-success {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
}

@keyframes fadeIn {
    from { opacity: 0; transform: translateY(-10px); }
    to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 768px) {
    .${component.toLowerCase()}-enterprise-container { padding: 0.5rem; }
}
EOL

    echo "Criado: $component_file e $css_file (CRUD)"
}

echo "Iniciando criacao de componentes..."
echo ""

# Criar componentes de Fiscal (consulta)
echo "Criando componentes de Fiscal..."
criar_componentes_consulta fiscal Imposto ImpostoService "Impostos" "Tabela de impostos configurados" 
criar_componentes_consulta fiscal Issqn IssqnService "ISSQN" "Tabela de aliquotas de ISS por municipio" 
criar_componentes_consulta fiscal EntradaNota EntradaNotaService "Entradas de Notas" "Registro de notas fiscais de entrada" 
criar_componentes_consulta fiscal SefazConsulta SefazConsultaService "Consulta SEFAZ" "Consultas a servicos da SEFAZ" 

echo ""
echo "Criando componentes de RH..."
criar_componentes_crud rh FolhaPagamento FolhaPagamentoService "Folha de Pagamento" "Gestao de folhas de pagamento" "competencia:string,status:string,valorTotal:number"
criar_componentes_crud rh FuncionarioFoto FuncionarioFotoService "Foto do Funcionario" "Gestao de fotos de funcionarios" "id:number,foto:file"

echo ""
echo "Criando componentes de Core..."
criar_componentes_crud core EmpresaLogo EmpresaLogoService "Logo da Empresa" "Gestao de logo da empresa" "id:number,logo:file"
criar_componentes_crud core Logos LogosService "Logos" "Gestao de logos do sistema" "tipo:string,logo:file"
criar_componentes_consulta core Recent RecentService "Atualizacoes Recentes" "Ultimas atualizacoes do sistema" 
criar_componentes_consulta core SuperAdmin SuperAdminService "Super Admin" "Ferramentas de administrador" 

echo ""
echo "Criando componentes de Cadastro restantes..."
criar_componentes_crud cadastro ServicoCadastro ServicoCadastroService "Servicos" "Gestao de servicos cadastrais" "nome:string,codigo:string,descricao:string,ativo:boolean,valorUnitario:number"
criar_componentes_crud cadastro Cliente ClienteService "Clientes" "Gestao de clientes" "nome:string,tipo:string,cpfCnpj:string,email:string,telefone:string,limiteCredito:number,ativo:boolean"
criar_componentes_crud cadastro Fornecedor FornecedorService "Fornecedores" "Gestao de fornecedores" "nome:string,tipo:string,cpfCnpj:string,email:string,telefone:string,ativo:boolean"
criar_componentes_crud cadastro ClienteLogo ClienteLogoService "Logo do Cliente" "Gestao de logos de clientes" "clienteId:number,logo:file"
criar_componentes_crud cadastro FornecedorLogo FornecedorLogoService "Logo do Fornecedor" "Gestao de logos de fornecedores" "fornecedorId:number,logo:file"

echo ""
echo "Todos os componentes foram criados!"
