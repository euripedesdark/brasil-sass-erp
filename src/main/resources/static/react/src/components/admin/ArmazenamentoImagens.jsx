import React, { useCallback, useEffect, useRef, useState } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Card } from 'primereact/card';
import { ConfirmDialog, confirmDialog } from 'primereact/confirmdialog';
import { InputText } from 'primereact/inputtext';
import { Message } from 'primereact/message';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';

const BASE = '/api/superadmin/armazenamento';

/**
 * Armazenamento de imagens.
 *
 * Quatro rotas de SUPERUSER sem tela: importar as imagens da pasta local para
 * o Mongo, purgar o que ficou no disco, trocar um recurso visual direto de um
 * arquivo e ver o que está pendente de reenvio.
 *
 * "Purgar" apaga arquivos do disco, então a confirmação diz o que vai ser
 * apagado e de onde. O botão fica desabilitado enquanto a quantidade de
 * pendências é zero, e o texto muda — purgar sem pendência não é erro, é
 * trabalho à toa.
 */
export const ArmazenamentoImagens = () => {
    const toast = useRef(null);
    const pastaRef = useRef(null);

    const [staging, setStaging] = useState(null);
    const [carregando, setCarregando] = useState(true);
    const [ocupado, setOcupado] = useState(null);
    const [erro, setErro] = useState('');

    const [pasta, setPasta] = useState('');
    const [tipoEntidade, setTipoEntidade] = useState('arquivo-local');
    const [recurso, setRecurso] = useState({ tipo: 'system-background', arquivo: '', empresaId: '' });

    const toastDe = (severity, summary, detail) =>
        toast.current?.show({ severity, summary, detail, life: 6000 });

    const carregarStaging = useCallback(async () => {
        setCarregando(true);
        try {
            const r = await apiFetch(`${BASE}/staging-pendente`);
            if (!r.ok) throw new Error('sem leitura');
            setStaging((await r.json())?.data ?? null);
            setErro('');
        } catch (e) {
            setErro(e?.payload?.message || 'Não foi possível ler os temporários');
        } finally {
            setCarregando(false);
        }
    }, []);

    useEffect(() => { carregarStaging(); }, [carregarStaging]);

    // URL completa, e nao o trecho: um helper que recebia "/importar-imagens"
    // montava a chamada com a base de fora, e a rota sumia do rastreio de quem
    // chama o que. Ler a URL inteira tambem deixa a tela mais facil de ler.
    const pos = (caminho) => apiFetch(caminho, { method: 'POST' });

    const importar = async () => {
        setOcupado('importar');
        try {
            // pasta vazia = a configurada em brasil-saas.armazenamento.pasta-imagens
            const qs = new URLSearchParams();
            if (pasta.trim()) qs.set('pasta', pasta.trim());
            qs.set('tipoEntidade', tipoEntidade);
            const r = await pos(`${BASE}/importar-imagens?${qs}`);
            const j = await r.json().catch(() => null);
            if (!r.ok) throw new Error(j?.message || 'Falha ao importar');
            const d = j?.data ?? j;
            toastDe('success', 'Importação concluída',
                `${d?.importados ?? 0} arquivo(s) · ${d?.erros?.length ?? 0} erro(s)`);
            if (d?.erros?.length) {
                setErro(d.erros.slice(0, 3).join(' · '));
            }
            carregarStaging();
        } catch (e) {
            toastDe('error', 'Falha ao importar', e?.payload?.message || 'Não foi possível importar');
        } finally {
            setOcupado(null);
        }
    };

    const purgar = () => {
        const n = staging?.quantidade ?? 0;
        confirmDialog.require({
            header: 'Purgar temporários',
            message: `Apagar ${n} arquivo(s) de ${staging?.raiz || 'a pasta de staging'}? `
                + 'O que estiver lá só não foi ao Mongo ainda. Se o envio for refeito depois, '
                + 'o arquivo não estará mais.',
            icon: 'pi pi-exclamation-triangle',
            acceptLabel: 'Sim, apagar do disco',
            rejectLabel: 'Cancelar',
            accept: async () => {
                setOcupado('purgar');
                try {
                    const qs = new URLSearchParams();
                    if (pasta.trim()) qs.set('pasta', pasta.trim());
                    qs.set('tipoEntidade', tipoEntidade);
                    const r = await pos(`${BASE}/purgar-imagens?${qs}`);
                    const j = await r.json().catch(() => null);
                    if (!r.ok) throw new Error(j?.message || 'Falha ao purgar');
                    const d = j?.data ?? j;
                    toastDe('success', 'Purgado', `${d?.purgados ?? 0} arquivo(s) removido(s)`);
                    carregarStaging();
                } catch (e) {
                    toastDe('error', 'Falha ao purgar', e?.payload?.message || 'Não foi possível purgar');
                } finally {
                    setOcupado(null);
                }
            }
        });
    };

    const atualizarRecurso = async () => {
        if (!recurso.arquivo.trim()) { setErro('Informe o caminho do arquivo'); return; }
        setOcupado('recurso');
        try {
            const qs = new URLSearchParams({ arquivo: recurso.arquivo.trim() });
            if (recurso.empresaId) qs.set('empresaId', recurso.empresaId);
            const r = await pos(`${BASE}/recurso/${encodeURIComponent(recurso.tipo)}?${qs}`);
            const j = await r.json().catch(() => null);
            const d = j?.data ?? j;
            if (!r.ok || d?.erro) throw new Error(d?.erro || 'Falha ao trocar o recurso');
            toastDe('success', 'Recurso trocado',
                `${d.tipo} · ${d.arquivo} (${d.tamanho} bytes)`);
            setRecurso({ ...recurso, arquivo: '' });
        } catch (e) {
            toastDe('error', 'Falha ao trocar o recurso', e?.payload?.message || e.message);
        } finally {
            setOcupado(null);
        }
    };

    const pendentes = staging?.arquivos ?? [];

    return (
        <div>
            <Toast ref={toast} />
            <ConfirmDialog />

            <Card title="Temporários aguardando envio ao Mongo" className="mb-3">
                {erro && <Message severity="error" text={erro} className="mb-3" />}

                <div className="grid">
                    <div className="col-12 md:col-3">
                        <div className="bc-muted small">Pasta</div>
                        <div className="text-truncate" style={{ maxWidth: 260 }} title={staging?.raiz}>
                            {staging?.raiz || '—'}
                        </div>
                    </div>
                    <div className="col-12 md:col-3">
                        <div className="bc-muted small">Pendentes de reenvio</div>
                        <div className="text-xl font-medium">
                            {carregando ? '…' : (staging?.quantidade ?? 0)}
                        </div>
                    </div>
                    <div className="col-12 md:col-6 flex align-items-end justify-content-end gap-2">
                        <Button label="Atualizar" icon="pi pi-refresh" text onClick={carregarStaging}
                                loading={carregando} />
                        <Button label="Purgar do disco" icon="pi pi-trash" severity="danger" outlined
                                loading={ocupado === 'purgar'}
                                disabled={!staging?.quantidade || !!ocupado}
                                onClick={purgar} />
                    </div>
                </div>

                {pendentes.length > 0 && (
                    <div className="mt-3 flex flex-wrap gap-2">
                        {pendentes.slice(0, 30).map((f) => (
                            <Tag key={f} value={f} severity="warning" />
                        ))}
                        {pendentes.length > 30 && (
                            <span className="bc-muted small">+ {pendentes.length - 30}</span>
                        )}
                    </div>
                )}
            </Card>

            <Card title="Importar imagens da pasta local" className="mb-3">
                <span className="bc-muted">
                    Lê os arquivos do disco e grava no MongoDB. Não apaga nada — a pasta de
                    origem continua como está. Deixe a pasta vazia para usar a configurada no sistema.
                </span>
                <div className="grid p-fluid mt-2">
                    <div className="col-12 md:col-5">
                        <label className="bc-label" htmlFor="pasta">Pasta de origem</label>
                        <InputText id="pasta" value={pasta} placeholder="vazio = pasta configurada"
                                   onChange={(e) => setPasta(e.target.value)} />
                    </div>
                    <div className="col-12 md:col-4">
                        <label className="bc-label" htmlFor="tipo">Tipo de entidade</label>
                        <InputText id="tipo" value={tipoEntidade}
                                   onChange={(e) => setTipoEntidade(e.target.value)} />
                    </div>
                    <div className="col-12 md:col-3 flex align-items-end">
                        <Button label="Importar" icon="pi pi-upload" loading={ocupado === 'importar'}
                                disabled={!!ocupado} onClick={importar} />
                    </div>
                </div>
            </Card>

            <Card title="Trocar recurso visual direto de um arquivo">
                <span className="bc-muted">
                    Para a tela de login, o fundo e a logo que ficaram desatualizadas no Mongo,
                    sem passar por upload manual.
                </span>
                <div className="grid p-fluid mt-2">
                    <div className="col-12 md:col-3">
                        <label className="bc-label" htmlFor="rtipo">Tipo do recurso</label>
                        <InputText id="rtipo" value={recurso.tipo}
                                   onChange={(e) => setRecurso({ ...recurso, tipo: e.target.value })} />
                    </div>
                    <div className="col-12 md:col-5">
                        <label className="bc-label" htmlFor="rarq">Caminho do arquivo no disco</label>
                        <InputText id="rarq" value={recurso.arquivo}
                                   placeholder="/caminho/da/imagem.png"
                                   onChange={(e) => setRecurso({ ...recurso, arquivo: e.target.value })} />
                    </div>
                    <div className="col-12 md:col-2">
                        <label className="bc-label" htmlFor="remp">Empresa (opcional)</label>
                        <InputText id="remp" value={recurso.empresaId} useGrouping={false}
                                   onChange={(e) => setRecurso({ ...recurso, empresaId: e.target.value })} />
                    </div>
                    <div className="col-12 md:col-2 flex align-items-end">
                        <Button label="Trocar" icon="pi pi-check" loading={ocupado === 'recurso'}
                                disabled={!!ocupado} onClick={atualizarRecurso} />
                    </div>
                </div>
            </Card>
        </div>
    );
};

export default ArmazenamentoImagens;
