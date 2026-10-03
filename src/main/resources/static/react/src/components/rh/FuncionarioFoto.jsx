import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { Button } from 'primereact/button';
import { Card } from 'primereact/card';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { InputText } from 'primereact/inputtext';
import { Message } from 'primereact/message';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';
import { apiFetch } from '../../services/ApiConfig';
import { ImagemRegistro } from '../shared/ImagemRegistro';

/**
 * Foto do colaborador.
 *
 * A tela existia inteira — com FileUpload, diálogo, validação e remount — mas
 * não tinha rota nem item de menu: era código morto, com as três rotas de foto
 * vivas e sem por onde serem chamado.
 *
 * O envio agora passa pelo componente compartilhado ImagemRegistro, o mesmo do
 * logo de cliente e da imagem de produto. Aqui ele entra no lugar do diálogo
 * próprio: as três telas precisam do mesmo comportamento (limite de tamanho,
 * tipo de imagem, revogar o objectURL ao trocar), e três implementações são três
 * lugares para dar problema.
 *
 * O id vai no caminho — /{id}/foto — como na imagem do produto. Estava na query
 * (?id=), o que quebrava o reuso do componente e deixava a URL gravada na
 * coluna fotoUrl apontando para uma rota inexistente.
 */
export const FuncionarioFoto = () => {
    const toast = useRef(null);

    const [funcionarios, setFuncionarios] = useState([]);
    const [carregando, setCarregando] = useState(true);
    const [filtro, setFiltro] = useState('');
    const [selecionado, setSelecionado] = useState(null);

    const carregar = useCallback(async () => {
        setCarregando(true);
        try {
            const r = await apiFetch('/api/rh/funcionarios');
            if (!r.ok) throw new Error('sem lista');
            const d = await r.json();
            const lista = d?.data ?? d;
            setFuncionarios(Array.isArray(lista) ? lista : (lista?.content ?? []));
        } catch {
            setFuncionarios([]);
        } finally {
            setCarregando(false);
        }
    }, []);

    useEffect(() => { carregar(); }, [carregar]);

    // a lista carrega antes da foto: o componente mostra a imagem já no
    // primeiro paint, sem esperar o upload responder
    useEffect(() => {
        if (!selecionado) return;
        apiFetch(`/api/rh/funcionarios/${selecionado.id}`)
            .then((r) => (r.ok ? r.json() : null))
            .then((d) => setSelecionado((s) => ({ ...s, ...((d?.data ?? d) || {}) })))
            .catch(() => { /* a foto mostrada não depende disto */ });
    }, [selecionado?.id]);

    const filtrados = useMemo(() => {
        const t = filtro.trim().toLowerCase();
        if (!t) return funcionarios;
        return funcionarios.filter((f) =>
            `${f.nome ?? ''} ${f.matricula ?? ''} ${f.cpf ?? ''}`.toLowerCase().includes(t));
    }, [funcionarios, filtro]);

    const escolher = (f) => {
        if (!f.fotoUrl) {
            toast.current?.show({
                severity: 'info', summary: 'Sem foto',
                detail: 'Envie a imagem para este colaborador', life: 3000
            });
        }
        setSelecionado(f);
    };

    const recarregar = async () => {
        await carregar();
        if (selecionado?.id) {
            const r = await apiFetch(`/api/rh/funcionarios/${selecionado.id}`).catch(() => null);
            if (r?.ok) {
                const d = await r.json();
                setSelecionado((s) => ({ ...s, ...((d?.data ?? d) || {}) }));
            }
        }
    };

    return (
        <div>
            <Toast ref={toast} />
            <div className="grid">
                <div className="col-12 md:col-7">
                    <Card title="Colaboradores">
                        <span className="p-input-icon-left mb-3" style={{ display: 'block' }}>
                            <i className="pi pi-search" style={{ left: '0.75rem', top: '0.75rem' }} />
                            <InputText
                                value={filtro}
                                onChange={(e) => setFiltro(e.target.value)}
                                placeholder="Buscar por nome, matrícula ou CPF"
                                style={{ width: '100%', paddingLeft: '2.25rem' }}
                            />
                        </span>

                        <DataTable
                            value={filtrados}
                            loading={carregando}
                            dataKey="id"
                            paginator
                            rows={10}
                            selectionMode="single"
                            selection={selecionado}
                            onSelectionChange={(e) => setSelecionado(e.value)}
                            onRowSelect={(e) => escolher(e.data)}
                            emptyMessage="Nenhum colaborador encontrado"
                            className="p-datatable-sm"
                            rowsPerPageOptions={[10, 20, 50]}
                        >
                            <Column field="matricula" header="Matrícula" sortable style={{ width: '110px' }} />
                            <Column field="nome" header="Nome" sortable />
                            <Column header="Foto" style={{ width: '120px' }} body={(f) => (
                                f.fotoUrl
                                    ? <Tag value="Com foto" severity="success" />
                                    : <Tag value="Sem foto" severity="warning" />
                            )} />
                        </DataTable>
                    </Card>
                </div>

                <div className="col-12 md:col-5">
                    <Card
                        title={selecionado ? `Foto — ${selecionado.nome ?? ''}` : 'Foto'}
                        subTitle="Os bytes ficam no MongoDB; o Postgres guarda só a referência"
                    >
                        {!selecionado ? (
                            <Message severity="info" text="Escolha um colaborador na lista para ver ou enviar a foto." />
                        ) : (
                            <ImagemRegistro
                                registroId={selecionado.id}
                                lerUrl={`/api/rh/funcionarios/${selecionado.id}/foto`}
                                enviarUrl={`/api/rh/funcionarios/${selecionado.id}/foto`}
                                removerUrl={`/api/rh/funcionarios/${selecionado.id}/foto`}
                                // o multipart deste caso chama o campo de "foto";
                                // logo usa "logo" e imagem de produto "arquivo"
                                campo="foto"
                                rotulo="Foto do colaborador"
                                aviso={(
                                    <Button
                                        label="Atualizar lista"
                                        icon="pi pi-refresh"
                                        text
                                        onClick={recarregar}
                                    />
                                )}
                            />
                        )}
                    </Card>
                </div>
            </div>
        </div>
    );
};

export default FuncionarioFoto;
