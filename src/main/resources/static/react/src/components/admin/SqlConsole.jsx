import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { Card } from 'primereact/card';
import { Button } from 'primereact/button';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { InputTextarea } from 'primereact/inputtextarea';
import { InputText } from 'primereact/inputtext';
import { Tree } from 'primereact/tree';
import { TabView, TabPanel } from 'primereact/tabview';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';
import { Dropdown } from 'primereact/dropdown';
import { Message } from 'primereact/message';
import { ConfirmDialog, confirmDialog } from 'primereact/confirmdialog';
import { Paginator } from 'primereact/paginator';
import { useAuth } from '../../contexts/AuthContext';
import { apiFetch } from '../../services/ApiConfig';
import './SqlConsole.css';

const POR_PAGINA = 50;

/**
 * Gerenciador SQL do SUPERUSER — "modo Interbase".
 *
 * Substitui o console antigo, que era so uma caixa de texto e rodava o
 * comando por GET na query string. Aqui a navegacao e por arvore de tabelas
 * (browse), como no Interbase/IBConsole, e comando destructive sempre pede
 * confirmacao — o backend recusa com 409 se ela nao vier.
 */
export const SqlConsole = () => {
    const { pode, isSuperuser } = useAuth();
    const toast = useRef(null);

    const [catalogo, setCatalogo] = useState({ tabelas: [], views: [], estatisticas: {} });
    const [tabela, setTabela] = useState(null);
    const [colunas, setColunas] = useState([]);
    const [linhas, setLinhas] = useState([]);
    const [total, setTotal] = useState(0);
    const [pagina, setPagina] = useState(0);
    const [busca, setBusca] = useState('');
    const [colunaBusca, setColunaBusca] = useState('');

    const [comando, setComando] = useState('');
    const [resultado, setResultado] = useState(null);
    const [erro, setErro] = useState('');
    const [carregando, setCarregando] = useState(false);

    const autorizado = isSuperuser || pode('superadmin', 'sql');

    // ---------------- catalogo ----------------
    useEffect(() => {
        if (!autorizado) return;
        (async () => {
            try {
                const r = await apiFetch('/api/superadmin/sql/catalogo');
                if (!r.ok) throw new Error(await r.text());
                setCatalogo(await r.json());
            } catch (e) {
                setErro('Falha ao carregar o catalogo: ' + e.message);
            }
        })();
    }, [autorizado]);

    const arvore = useMemo(() => {
        const n = (c) => ({ key: c.table_name || c.view_name, label: c.table_name || c.view_name, data: c });
        return [
            {
                key: 'tabelas', label: `Tabelas (${catalogo.tabelas?.length || 0})`,
                children: (catalogo.tabelas || []).map(n),
            },
            {
                key: 'views', label: `Views (${catalogo.views?.length || 0})`,
                children: (catalogo.views || []).map(n),
            },
        ];
    }, [catalogo]);

    // ---------------- browse ----------------
    const abrirTabela = useCallback(async (nome) => {
        setTabela(nome);
        setPagina(0);
        setBusca('');
        setErro('');
        try {
            const meta = await apiFetch(`/api/superadmin/sql/tabelas/${nome}`);
            if (!meta.ok) throw new Error(await meta.text());
            const m = await meta.json();
            setColunas(m.colunas || []);
            setTotal(m.total || 0);
        } catch (e) {
            setErro('Falha ao abrir tabela: ' + e.message);
        }
    }, []);

    const carregar = useCallback(async () => {
        if (!tabela) return;
        setCarregando(true);
        try {
            const q = new URLSearchParams({ pagina: String(pagina), tamanho: String(POR_PAGINA) });
            if (busca.trim() && colunaBusca) {
                q.set('busca', busca.trim());
                q.set('coluna', colunaBusca);
            }
            const r = await apiFetch(`/api/superadmin/sql/tabelas/${tabela}/dados?${q}`);
            if (!r.ok) throw new Error(await r.text());
            const d = await r.json();
            setLinhas(d.linhas || []);
            setTotal(d.total || 0);
        } catch (e) {
            setErro('Falha ao carregar dados: ' + e.message);
            setLinhas([]);
        } finally {
            setCarregando(false);
        }
    }, [tabela, pagina, busca, colunaBusca]);

    useEffect(() => { carregar(); }, [carregar]);

    // ---------------- edicao de celula ----------------
    const chavePrimaria = useMemo(
        () => colunas.find((c) => c.chave_primaria)?.column_name || 'id',
        [colunas]
    );

    const onCelulaEditada = async (e) => {
        const { data, column, newValue } = e;
        if (newValue === null || newValue === undefined) return;
        try {
            const r = await apiFetch('/api/superadmin/sql/campo', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    tabela, coluna: column.field, valor: newValue,
                    id: data[chavePrimaria], chave: chavePrimaria,
                }),
            });
            if (!r.ok) throw new Error((await r.json())?.data || await r.text());
            toast.current?.show({ severity: 'success', summary: 'Campo atualizado', life: 2500 });
        } catch (err) {
            setErro('Erro ao salvar: ' + err.message);
        }
    };

    // ---------------- execucao de comando ----------------
    const rodar = async (sql, confirmar) => {
        setCarregando(true);
        setErro('');
        try {
            const r = await apiFetch('/api/superadmin/sql/executar', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ sql, confirmar }),
            });

            if (r.status === 409) {
                // o backend recusou: falta confirmacao do operador
                const corpo = await r.json();
                confirmDialog({
                    message: corpo.data,
                    header: 'Confirmar comando destrutivo',
                    icon: 'pi pi-exclamation-triangle',
                    acceptLabel: 'Executar assim mesmo',
                    rejectLabel: 'Cancelar',
                    accept: () => rodar(sql, true),
                });
                return;
            }

            if (!r.ok) {
                const corpo = await r.json().catch(() => null);
                throw new Error(corpo?.data || `HTTP ${r.status}`);
            }

            const j = await r.json();
            setResultado(j.data);
            toast.current?.show({
                severity: 'success',
                summary: `${j.data.linhas?.length ?? 0} linha(s) · ${j.data.tempoMs} ms`,
                life: 3000,
            });
        } catch (e) {
            setErro(e.message);
        } finally {
            setCarregando(false);
        }
    };

    /**
     * Roteiro de Commands, em um POST só.
     *
     * O /executar aceita um comando por vez. O /script é o mesmo caminho para
     * um bloco inteiro — o que a migração de um banco grande precisa, e era a
     * rota que existia sem uso. A confirmação de comando destrutivo funciona
     * igual: o backend responde 409 e o script inteiro é reenviado com
     * confirmar=true.
     */
    const rodarScript = async (script, confirmar) => {
        setCarregando(true);
        setErro('');
        try {
            const r = await apiFetch('/api/superadmin/sql/script', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ script, confirmar }),
            });

            if (r.status === 409) {
                const corpo = await r.json();
                confirmDialog({
                    message: corpo.data,
                    header: 'Confirmar script destrutivo',
                    icon: 'pi pi-exclamation-triangle',
                    acceptLabel: 'Executar assim mesmo',
                    rejectLabel: 'Cancelar',
                    accept: () => rodarScript(script, true),
                });
                return;
            }

            if (!r.ok) {
                const corpo = await r.json().catch(() => null);
                throw new Error(corpo?.data || `HTTP ${r.status}`);
            }

            const j = await r.json();
            const lista = j.data || [];
            // o resultado é uma lista por comando: o SQL console mostra o
            // primeiro como se fosse o único, entao junta as linhas de todos
            const linhas = lista.flatMap((c) => c.linhas || []);
            setResultado({ ...(lista[0] || {}), linhas, comandos: lista.length });
            toast.current?.show({
                severity: 'success',
                summary: `${lista.length} comando(s) · ${linhas.length} linha(s)`,
                life: 4000,
            });
        } catch (e) {
            setErro(e.message);
        } finally {
            setCarregando(false);
        }
    };

    // ---------------- exportacao ----------------
    const exportarCsv = () => {
        if (!linhas.length) return;
        const cols = Object.keys(linhas[0]);
        const esc = (v) => {
            const s = v === null || v === undefined ? '' : String(v);
            return /[";\n]/.test(s) ? `"${s.replace(/"/g, '""')}"` : s;
        };
        const csv = [cols.join(';'), ...linhas.map((l) => cols.map((c) => esc(l[c])).join(';'))].join('\n');
        const url = URL.createObjectURL(new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8' }));
        const a = document.createElement('a');
        a.href = url;
        a.download = `${tabela}_p${pagina + 1}.csv`;
        a.click();
        URL.revokeObjectURL(url);
    };

    if (!autorizado) {
        return (
            <Card>
                <Message severity="warn" text="Acesso restrito a SUPERUSER." />
            </Card>
        );
    }

    const colunasTabela = colunas.map((c) => (
        <Column
            key={c.column_name}
            field={c.column_name}
            header={c.column_name}
            sortable
            editable={!String(c.column_name).includes('uuid')}
            style={{ minWidth: '9rem' }}
            body={(row) => {
                const v = row[c.column_name];
                const txt = v === null || v === undefined
                    ? <span style={{ opacity: 0.4 }}>null</span>
                    : (typeof v === 'object' ? JSON.stringify(v) : String(v));
                if (c.chave_primaria) return <Tag value={txt} severity="info" />;
                return <span title={String(v ?? '')}>{txt}</span>;
            }}
        />
    ));

    return (
        <div className="sql-console">
            <Toast />
            <ConfirmDialog />

            <div className="sql-console__header">
                <div>
                    <h2>Gerenciador SQL</h2>
                    <p className="sql-console__sub">
                        Manutenção direta do banco — equivalente ao browse do Interbase.
                        {' '}<strong>Comandos que alteram dados pedem confirmação.</strong>
                    </p>
                </div>
                <div className="sql-console__stats">
                    <Tag severity="info" value={`${catalogo.estatisticas?.tabelas ?? 0} tabelas`} />
                    <Tag severity="success" value={`${catalogo.estatisticas?.tabelas_com_uso ?? 0} em uso`} />
                    <Tag severity="warning" value={catalogo.estatisticas?.tamanho_banco || '—'} />
                    <Tag severity="info" value={`${catalogo.estatisticas?.conexoes_ativas ?? 0} conexões`} />
                </div>
            </div>

            {erro && <Message severity="error" text={erro} className="mb-3" onLifeEnd={() => setErro('')} />}

            <TabView>
                {/* ---------------- browse ---------------- */}
                <TabPanel header="Browse" leftIcon="pi pi-table pi-fw">
                    <div className="sql-console__split">
                        <div className="sql-console__tree">
                            <Tree value={arvore} selectionMode="single"
                                  onSelectionChange={(e) => e.node?.data && abrirTabela(e.node.data.table_name || e.node.data.view_name)} />
                        </div>

                        <div className="sql-console__grid">
                            {!tabela ? (
                                <Message severity="info" text="Escolha uma tabela na lista para ver os dados." />
                            ) : (
                                <>
                                    <div className="sql-console__toolbar">
                                        <h3>{tabela}</h3>
                                        <div className="sql-console__filters">
                                            <Dropdown
                                                value={colunaBusca}
                                                options={colunas.map((c) => c.column_name)}
                                                onChange={(e) => setColunaBusca(e.value)}
                                                placeholder="Buscar em..."
                                                showClear
                                                className="w-14rem"
                                            />
                                            <InputText
                                                value={busca}
                                                onChange={(e) => setBusca(e.target.value)}
                                                placeholder="filtro..."
                                                className="w-56"
                                                onKeyDown={(e) => e.key === 'Enter' && setPagina(0)}
                                            />
                                            <Button icon="pi pi-search" onClick={() => setPagina(0)} loading={carregando} />
                                            <Button icon="pi pi-download" severity="secondary" outlined
                                                    onClick={exportarCsv} disabled={!linhas.length}
                                                    tooltip="Exportar a pagina atual em CSV" />
                                        </div>
                                    </div>

                                    <DataTable value={linhas} loading={carregando} editMode="cell"
                                               onCellEditComplete={onCelulaEditada}
                                               scrollable scrollHeight="flex" size="small"
 stripedRows className="sql-console__table">
                                        {colunasTabela}
                                    </DataTable>

                                    <Paginator
                                        first={pagina * POR_PAGINA}
                                        rows={POR_PAGINA}
                                        totalRecords={total}
                                        onPage={(e) => setPagina(e.first / POR_PAGINA)}
                                        template="RowsPerPageDropdown FirstPageLink PrevPageLink CurrentPageReport NextPageLink LastPageLink"
                                    />
                                </>
                            )}
                        </div>
                    </div>
                </TabPanel>

                {/* ---------------- comando ---------------- */}
                <TabPanel header="Comando" leftIcon="pi pi-terminal pi-fw">
                    <Message severity="info" className="mb-3"
                        text="Comando é um SQL por vez. Roteiro aceita várias linhas e envia tudo em um POST só — a rota existe para migração de banco." />
                    <div className="sql-console__cmd">
                        <InputTextarea value={comando} onChange={(e) => setComando(e.target.value)}
                                       rows={8} autoResize className="w-full"
                                       placeholder={'SELECT * FROM brasil_saas.bc_core_usuario LIMIT 20;'} />

                        <div className="sql-console__cmd-actions">
                            <Button label="Executar" icon="pi pi-play" loading={carregando}
                                    disabled={!comando.trim()} onClick={() => rodar(comando, false)} />
                            <Button label="Executar como roteiro" icon="pi pi-file" outlined loading={carregando}
                                    disabled={!comando.trim()}
                                    tooltip="Envia o bloco inteiro em um POST (/sql/script), útil para migração"
                                    onClick={() => rodarScript(comando, false)} />
                            <Button label="Limpar" icon="pi pi-times" severity="secondary" text
                                    onClick={() => { setComando(''); setResultado(null); }} />
                        </div>

                        {resultado && (
                            <div className="sql-console__result">
                                <div className="sql-console__result-meta">
                                    <Tag severity="info" value={`${resultado.afetadas} linha(s) afetada(s)`} />
                                    <Tag severity="success" value={`${resultado.tempoMs} ms`} />
                                    {resultado.linhas?.length > 0 && (
                                        <span className="text-muted">
                                            chaves: {Object.keys(resultado.linhas[0]).join(', ')}
                                        </span>
                                    )}
                                </div>

                                {resultado.linhas?.length > 0 && (
                                    <DataTable value={resultado.linhas} scrollable scrollHeight="28rem"
                                               size="small" stripedRows>
                                        {Object.keys(resultado.linhas[0]).map((c) => (
                                            <Column key={c} field={c} header={c} sortable
                                                    body={(r) => {
                                                        const v = r[c];
                                                        return v === null || v === undefined ? '—' : String(v);
                                                    }} />
                                        ))}
                                    </DataTable>
                                )}
                            </div>
                        )}
                    </div>
                </TabPanel>

                {/* ---------------- estrutura ---------------- */}
                <TabPanel header="Estrutura" leftIcon="pi pi-info-circle pi-fw">
                    {!tabela ? (
                        <Message severity="info" text="Escolha uma tabela no Browse para ver a estrutura." />
                    ) : (
                        <DataTable value={colunas} size="small" stripedRows>
                            <Column field="column_name" header="Coluna" />
                            <Column field="data_type" header="Tipo" />
                            <Column field="aceita_nulo" header="Aceita nulo"
                                    body={(c) => c.aceita_nulo
                                        ? <Tag severity="warning" value="sim" />
                                        : <Tag severity="success" value="não" />} />
                            <Column field="chave_primaria" header="PK"
                                    body={(c) => c.chave_primaria && <Tag severity="info" value="PK" />} />
                            <Column field="valor_padrao" header="Padrão" />
                        </DataTable>
                    )}
                </TabPanel>
            </TabView>
        </div>
    );
};

export default SqlConsole;
