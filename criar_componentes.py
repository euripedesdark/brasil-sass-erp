#!/usr/bin/env python3
"""
Script para criar componentes React restantes do Brasil SaaS ERP
"""

import os
from pathlib import Path

BASE_DIR = Path("/home/euripedes/OneDrive/python/projetos-leno/GIT Repos/BRASIL-SAAS-ERP/src/main/resources/static/react/src")
COMPONENTS_DIR = BASE_DIR / "components"

# Template para componentes de consulta (read-only)
CONSULTA_TEMPLATE = '''import React, { useState, useEffect, useRef } from 'react';
import {{ Card }} from 'primereact/card';
import {{ DataTable }} from 'primereact/datatable';
import {{ Column }} from 'primereact/column';
import {{ InputText }} from 'primereact/inputtext';
import {{ Toast }} from 'primereact/toast';
import {{ useAuth }} from '../contexts/AuthContext';
import {{ {service} }} from '../services/{service}';
import './{component}.css';

export const {component} = () => {{
    const {{ user }} = useAuth();
    const toast = useRef(null);
    const [dataList, setDataList] = useState([]);
    const [loading, setLoading] = useState(false);
    const [searchTerm, setSearchTerm] = useState('');

    useEffect(() => {{
        fetchData();
    }}, []);

    const fetchData = async () => {{
        setLoading(true);
        try {{
            const data = await {service}.listar();
            setDataList(data || []);
        }} catch (err) {{
            console.error('Erro ao carregar {title}', err);
            toast.current?.show({{
                severity: 'error',
                summary: 'Erro',
                detail: 'Nao foi possivel carregar {title}',
                life: 3000
            }});
        }} finally {{
            setLoading(false);
        }}
    }};

    const header = (
        <div className="{component_lower}-header flex justify-content-between align-items-center">
            <span className="p-input-icon-left">
                <i className="pi pi-search" />
                <InputText
                    value={{searchTerm}}
                    onChange={{(e) => setSearchTerm(e.target.value)}}
                    placeholder="Buscar..."
                />
            </span>
        </div>
    );

    return (
        <div className="{component_lower}-enterprise-container">
            <Toast ref={{toast}} />
            
            <Card title="{title}" className="{component_lower}-main-card">
                <div className="{component_lower}-header-actions mb-4">
                    <p className="text-muted m-0">{description}</p>
                </div>

                <DataTable
                    value={{dataList}}
                    header={{header}}
                    loading={{loading}}
                    paginator
                    rows={{20}}
                    rowsPerPageOptions={{[10, 20, 50, 100]}}
                    totalRecords={{dataList.length}}
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
}};

export default {component};
'''

CONSULTA_CSS_TEMPLATE = '''/* {component}.css - Estilos para o componente {title} */

.{component_lower}-enterprise-container {{
    padding: 1rem;
    animation: fadeIn 0.3s ease-in;
}}

.{component_lower}-main-card {{
    margin: 0;
    border-radius: 8px;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
}}

.{component_lower}-header-actions {{
    margin-bottom: 1.5rem;
}}

.{component_lower}-enterprise-container .p-datatable {{
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}}

.{component_lower}-enterprise-container .p-datatable .p-datatable-thead > tr > th {{
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    font-weight: 600;
    padding: 0.75rem;
}}

.{component_lower}-enterprise-container .p-datatable .p-datatable-tbody > tr > td {{
    padding: 0.75rem;
    border-bottom: 1px solid #dee2e6;
}}

.{component_lower}-header {{
    padding: 1rem;
    background: #f8f9fa;
    border-radius: 8px;
}}

.{component_lower}-header .p-input-icon-left i {{
    color: #6c757d;
}}

.{component_lower}-header .p-inputtext {{
    width: 100%;
    border-radius: 4px;
    border: 1px solid #ced4da;
}}

@keyframes fadeIn {{
    from {{ opacity: 0; transform: translateY(-10px); }}
    to {{ opacity: 1; transform: translateY(0); }}
}}

@media (max-width: 768px) {{
    .{component_lower}-enterprise-container {{ padding: 0.5rem; }}
    .{component_lower}-main-card {{ padding: 0.5rem; }}
}}

.{component_lower}-enterprise-container .p-toast {{
    z-index: 9999;
}}
'''

def create_consulta_component(module, component, service, title, description):
    """Cria um componente de consulta (read-only)"""
    comp_lower = component.lower()
    module_dir = COMPONENTS_DIR / module
    module_dir.mkdir(exist_ok=True)
    
    # Criar JSX
    jsx_content = CONSULTA_TEMPLATE.format(
        component=component,
        component_lower=comp_lower,
        service=service,
        title=title,
        description=description
    )
    
    with open(module_dir / f"{component}.jsx", "w") as f:
        f.write(jsx_content)
    
    # Criar CSS
    css_content = CONSULTA_CSS_TEMPLATE.format(
        component=component,
        component_lower=comp_lower,
        title=title,
        description=description
    )
    
    with open(module_dir / f"{component}.css", "w") as f:
        f.write(css_content)
    
    print(f"✓ Criado: {module}/{component}.jsx + {component}.css")

def create_crud_component(module, component, service, title, description, fields):
    """Cria um componente com CRUD completo"""
    comp_lower = component.lower()
    module_dir = COMPONENTS_DIR / module
    module_dir.mkdir(exist_ok=True)
    
    # Gerar campos
    initial_state = ""
    form_fields = ""
    table_columns = ""
    summary_items = ""
    
    for field_name, field_type in fields.items():
        if field_type == "boolean":
            initial_state += f"        {field_name}: true,\n"
        elif field_type == "number":
            initial_state += f"        {field_name}: 0,\n"
        else:
            initial_state += f"        {field_name}: '',\n"
        
        # Form field
        if field_type == "number":
            form_fields += f'''                        <div className="col-12 field">
                            <label className="font-bold mb-2 block">{field_name}</label>
                            <InputNumber value={{nova{component}.{field_name}}} onChange={{(e) => setNova{component}({{...nova{component}, {field_name}: e.value}})}} mode="currency" currency="BRL" locale="pt-BR" />
                        </div>
'''
        else:
            form_fields += f'''                        <div className="col-12 field">
                            <label className="font-bold mb-2 block">{field_name}</label>
                            <InputText value={{nova{component}.{field_name}}} onChange={{(e) => setNova{component}({{...nova{component}, {field_name}: e.target.value}})}} placeholder="{field_name}" />
                        </div>
'''
        
        table_columns += f'                    <Column field="{field_name}" header="{field_name}" sortable style={{ width: "200px" }} />\n'
        summary_items += f'                        <span>{{nova{component}.{field_name} || "N/A"}}</span>\n'
    
    # Template CRUD
    crud_template = f'''import React, {{ useState, useEffect, useRef }} from 'react';
import {{ Card }} from 'primereact/card';
import {{ DataTable }} from 'primereact/datatable';
import {{ Column }} from 'primereact/column';
import {{ Button }} from 'primereact/button';
import {{ Dialog }} from 'primereact/dialog';
import {{ InputText }} from 'primereact/inputtext';
import {{ InputNumber }} from 'primereact/inputnumber';
import {{ Dropdown }} from 'primereact/dropdown';
import {{ Toast }} from 'primereact/toast';
import {{ Tag }} from 'primereact/tag';
import {{ Divider }} from 'primereact/divider';
import {{ Message }} from 'primereact/message';
import {{ useAuth }} from '../contexts/AuthContext';
import {{ {service} }} from '../services/{service}';
import './{component}.css';

export const {component} = () => {{
    const {{ user }} = useAuth();
    const toast = useRef(null);
    const [data, setData] = useState([]);
    const [loading, setLoading] = useState(false);
    const [dialogVisible, setDialogVisible] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState(false);

    const [nova{component}, setNova{component}] = useState({{
{initial_state}    }});

    useEffect(() => {{
        fetchData();
    }}, []);

    const fetchData = async () => {{
        setLoading(true);
        try {{
            const data = await {service}.listar();
            setData(data || []);
        }} catch (err) {{
            console.error('Erro ao carregar {title}', err);
            toast.current?.show({{ severity: 'error', summary: 'Erro', detail: 'Nao foi possivel carregar {title}', life: 3000 }});
        }} finally {{
            setLoading(false);
        }}
    }};

    const salvar = async () => {{
        setLoading(true);
        setError('');
        setSuccess(false);
        
        try {{
            if (nova{component}.id) {{
                await {service}.atualizar(nova{component}.id, nova{component});
                toast.current?.show({{ severity: 'success', summary: 'Sucesso', detail: '{title} atualizado com sucesso', life: 3000 }});
            }} else {{
                await {service}.criar(nova{component});
                toast.current?.show({{ severity: 'success', summary: 'Sucesso', detail: '{title} criado com sucesso', life: 3000 }});
            }}
            setSuccess(true);
            setTimeout(() => {{
                setDialogVisible(false);
                fetchData();
                resetForm();
            }}, 1500);
        }} catch (err) {{
            setError(err.message || 'Erro ao salvar');
            toast.current?.show({{ severity: 'error', summary: 'Erro', detail: err.message, life: 3000 }});
        }} finally {{
            setLoading(false);
        }}
    }};

    const excluir = async (id) => {{
        try {{
            await {service}.excluir(id);
            toast.current?.show({{ severity: 'success', summary: 'Sucesso', detail: '{title} excluido com sucesso', life: 3000 }});
            fetchData();
        }} catch (err) {{
            toast.current?.show({{ severity: 'error', summary: 'Erro', detail: err.message, life: 3000 }});
        }}
    }};

    const abrirDialog = (item = null) => {{
        if (item) {{
            setNova{component}(item);
        }} else {{
            resetForm();
        }}
        setDialogVisible(true);
    }};

    const resetForm = () => {{
        setNova{component}({{
{initial_state}        }});
        setError('');
        setSuccess(false);
    }};

    const acoesTemplate = (rowData) => {{
        return (
            <div className="{comp_lower}-acoes">
                <Button icon="pi pi-pencil" className="p-button-info p-button-sm p-button-text" onClick={{() => abrirDialog(rowData)}} tooltip="Editar" />
                <Button icon="pi pi-trash" className="p-button-danger p-button-sm p-button-text" onClick={{() => excluir(rowData.id)}} tooltip="Excluir" />
            </div>
        );
    }};

    const dialogFooter = (
        <div>
            <Button label="Cancelar" icon="pi pi-times" className="p-button-text" onClick={{() => {{ setDialogVisible(false); resetForm(); }}}} />
            <Button label="Salvar" icon="pi pi-save" className="p-button-success" onClick={{salvar}} loading={{loading}} />
        </div>
    );

    return (
        <div className="{comp_lower}-enterprise-container">
            <Toast ref={{toast}} />
            
            <Card title="{title}" className="{comp_lower}-main-card">
                <div className="{comp_lower}-header-actions mb-4 flex justify-content-between align-items-center">
                    <div className="{comp_lower}-info">
                        <p className="text-muted m-0">{description}</p>
                    </div>
                    <Button label="Novo" icon="pi pi-plus" onClick={{() => abrirDialog()}} className="p-button-success" />
                </div>

                <DataTable
                    value={{data}}
                    loading={{loading}}
                    paginator
                    rows={{10}}
                    rowsPerPageOptions={{[10, 20, 50, 100]}}
                    totalRecords={{data.length}}
                    responsiveLayout="scroll"
                    className="p-datatable-sm"
                    emptyMessage="Nenhum registro encontrado"
                >
{table_columns}                    <Column body={{acoesTemplate}} style={{ width: '120px' }} />
                </DataTable>
            </Card>

            <Dialog
                header={{nova{component}.id ? `Editar: ${{nova{component}.nome || nova{component}.id}}` : `Novo {title}`}}
                visible={{dialogVisible}}
                style={{ width: '500px' }}
                onHide={{() => {{ setDialogVisible(false); resetForm(); }}}}
                footer={{dialogFooter}}
                maximizable
            >
                <div className="p-fluid">
                    {{error && <Message severity="error" text={{error}} className="w-full mb-3" />}}
                    {{success && <Message severity="success" text="Salvo com sucesso!" className="w-full mb-3" />}}

                    <div className="grid">
{form_fields}                    </div>

                    <Divider />
                    <div className="{comp_lower}-resumo flex flex-wrap justify-content-between align-items-center p-3 bg-gray-100 border-round">
{summary_items}                    </div>
                </div>
            </Dialog>
        </div>
    );
}};

export default {component};
'''
    
    with open(module_dir / f"{component}.jsx", "w") as f:
        f.write(crud_template)
    
    # CSS simples
    css_content = f'''/* {component}.css */

.{comp_lower}-enterprise-container {{
    padding: 1rem;
    animation: fadeIn 0.3s ease-in;
}}

.{comp_lower}-main-card {{
    margin: 0;
    border-radius: 8px;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
}}

.{comp_lower}-header-actions {{
    margin-bottom: 1.5rem;
}}

.{comp_lower}-acoes {{
    display: flex;
    gap: 0.5rem;
    justify-content: center;
}}

.{comp_lower}-enterprise-container .p-datatable {{
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}}

.{comp_lower}-enterprise-container .p-datatable .p-datatable-thead > tr > th {{
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    font-weight: 600;
    padding: 0.75rem;
}}

.{comp_lower}-enterprise-container .p-button-success {{
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
}}

@keyframes fadeIn {{
    from {{ opacity: 0; transform: translateY(-10px); }}
    to {{ opacity: 1; transform: translateY(0); }}
}}

@media (max-width: 768px) {{
    .{comp_lower}-enterprise-container {{ padding: 0.5rem; }}
}}
'''
    
    with open(module_dir / f"{component}.css", "w") as f:
        f.write(css_content)
    
    print(f"✓ Criado: {module}/{component}.jsx + {component}.css (CRUD)")

# Criar componentes de Fiscal
print("Criando componentes de Fiscal...")
create_consulta_component("fiscal", "EntradaNota", "EntradaNotaService", "Entradas de Notas", "Registro de notas fiscais de entrada")
create_consulta_component("fiscal", "Issqn", "IssqnService", "ISSQN", "Tabela de aliquotas de ISS por municipio")
create_consulta_component("fiscal", "SefazConsulta", "SefazConsultaService", "Consulta SEFAZ", "Consultas a servicos da SEFAZ")

print("\nCriando componentes de RH...")
create_crud_component("rh", "FolhaPagamento", "FolhaPagamentoService", "Folha de Pagamento", "Gestao de folhas de pagamento", {
    "competencia": "string",
    "status": "string",
    "valorTotal": "number"
})
create_crud_component("rh", "FuncionarioFoto", "FuncionarioFotoService", "Foto do Funcionario", "Gestao de fotos de funcionarios", {
    "id": "number"
})

print("\nCriando componentes de Core...")
create_crud_component("core", "EmpresaLogo", "EmpresaLogoService", "Logo da Empresa", "Gestao de logo da empresa", {
    "id": "number"
})
create_crud_component("core", "Logos", "LogosService", "Logos", "Gestao de logos do sistema", {
    "tipo": "string"
})
create_consulta_component("core", "Recent", "RecentService", "Atualizacoes Recentes", "Ultimas atualizacoes do sistema")
create_consulta_component("core", "SuperAdmin", "SuperAdminService", "Super Admin", "Ferramentas de administrador")

print("\nCriando componentes de Cadastro restantes...")
create_crud_component("cadastro", "ServicoCadastro", "ServicoCadastroService", "Servicos", "Gestao de servicos cadastrais", {
    "nome": "string",
    "codigo": "string",
    "descricao": "string",
    "ativo": "boolean",
    "valorUnitario": "number"
})
create_crud_component("cadastro", "Cliente", "ClienteService", "Clientes", "Gestao de clientes", {
    "nome": "string",
    "tipo": "string",
    "cpfCnpj": "string",
    "email": "string",
    "telefone": "string",
    "limiteCredito": "number",
    "ativo": "boolean"
})
create_crud_component("cadastro", "Fornecedor", "FornecedorService", "Fornecedores", "Gestao de fornecedores", {
    "nome": "string",
    "tipo": "string",
    "cpfCnpj": "string",
    "email": "string",
    "telefone": "string",
    "ativo": "boolean"
})
create_crud_component("cadastro", "ClienteLogo", "ClienteLogoService", "Logo do Cliente", "Gestao de logos de clientes", {
    "clienteId": "number"
})
create_crud_component("cadastro", "FornecedorLogo", "FornecedorLogoService", "Logo do Fornecedor", "Gestao de logos de fornecedores", {
    "fornecedorId": "number"
})

print("\n✅ Todos os componentes foram criados!")
