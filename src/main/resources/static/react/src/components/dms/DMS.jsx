import React, { useState, useEffect, useRef, useCallback } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Card } from 'primereact/card';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Dialog } from 'primereact/dialog';
import { InputText } from 'primereact/inputtext';
import { InputTextarea } from 'primereact/inputtextarea';
import { TabView, TabPanel } from 'primereact/tabview';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';

const BASE = '/api/dms';

export const DMS = () => {
    const toast = useRef(null);
    const fileRef = useRef(null);
    const [docs, setDocs] = useState([]);
    const [sel, setSel] = useState(null);
    const [vers, setVers] = useState([]);
    const [aprovs, setAprovs] = useState([]);
    const [ret, setRet] = useState([]);
    const [loading, setLoading] = useState(true);
    const [dlg, setDlg] = useState(false);
    const [f, setF] = useState({});

    const js = async (r) => { const j = await r.json().catch(() => null); return Array.isArray(j) ? j : (j?.data ?? j ?? []); };
    const carregar = useCallback(async () => {
        setLoading(true);
        try { const [d, r] = await Promise.all([apiFetch(BASE + '/documentos').then(js), apiFetch(BASE + '/retencao').then(js)]); setDocs(d); setRet(r); }
        catch (e) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: 'Falha ao carregar DMS', life: 4000 }); }
        finally { setLoading(false); }
    }, []);
    useEffect(() => { carregar(); }, [carregar]);
    const excluir = async (d) => {
        if (!window.confirm('Excluir o documento ' + (d.codigo || d.id) + '? Esta ação remove também o acesso às versões.')) return;
        const r = await apiFetch(BASE + '/documentos/' + d.id, { method: 'DELETE' });
        if (!r.ok) {
            const j = await r.json().catch(() => null);
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: j?.message || j?.errors?.[0]?.message || 'Não foi possível excluir o documento', life: 4000 });
            return;
        }
        if (sel && sel.id === d.id) { setSel(null); setVers([]); setAprovs([]); }
        toast.current?.show({ severity: 'success', summary: 'Documento excluído', life: 2500 });
        carregar();
    };
    const ver = async (d) => { setSel(d); setVers(await apiFetch(BASE + '/documentos/' + d.id + '/versoes').then(js)); setAprovs(await apiFetch(BASE + '/documentos/' + d.id + '/aprovacoes').then(js)); };
    const salvar = async () => {
        if (!f.codigo?.trim() || !f.titulo?.trim()) { toast.current?.show({ severity: 'warn', summary: 'Atenção', detail: 'Código e título obrigatórios', life: 3000 }); return; }
        await apiFetch(BASE + '/documentos', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(f) });
        setDlg(false); setF({}); carregar();
    };
    const upload = async (ev) => {
        const arq = ev.target.files && ev.target.files[0];
        if (!arq || !sel) return;
        const fd = new FormData();
        fd.append('arquivo', arq);
        await apiFetch(BASE + '/documentos/' + sel.id + '/versoes', { method: 'POST', body: fd });
        if (fileRef.current) fileRef.current.value = '';
        ver(sel);
    };
    const solicitar = async () => { await apiFetch(BASE + '/documentos/' + sel.id + '/aprovacoes', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({}) }); ver(sel); };
    const decidir = async (id, aprovar) => { await apiFetch(BASE + '/aprovacoes/' + id + '/decidir', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ aprovar }) }); ver(sel); carregar(); };
    const baixar = async (vid, nome) => {
        const r = await apiFetch(BASE + '/versoes/' + vid + '/download');
        if (!r.ok) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: 'Falha no download', life: 3000 }); return; }
        const blob = await r.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = nome || ('documento-' + vid);
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(url);
    };

    return (
        <div className='p-4'>
            <Toast ref={toast} />
            <input ref={fileRef} type='file' style={{ display: 'none' }} onChange={upload} />
            <div className='flex justify-content-between align-items-center mb-3 flex-wrap gap-2'>
                <div><h2 className='m-0'>DMS</h2><span className='bc-muted'>Documentos, versionamento, aprovação e retenção</span></div>
                <Button label='Novo documento' icon='pi pi-plus' onClick={() => { setF({}); setDlg(true); }} />
            </div>
            <div className='grid'>
                <div className='bc-form-col-6 col-12 md:col-6'>
                    <DataTable value={docs} loading={loading} paginator rows={10} emptyMessage='Nenhum documento.' responsiveLayout='scroll' dataKey='id' selectionMode='single' selection={sel} onSelectionChange={(e) => e.value && ver(e.value)}>
                        <Column field='codigo' header='Código' style={{ width: '7rem' }} />
                        <Column field='titulo' header='Título' />
                        <Column field='status' header='Status' body={(r) => <Tag value={r.status} severity={r.status === 'APROVADO' ? 'success' : 'info'} />} style={{ width: '8rem' }} />
                        <Column header='' body={(r) => (<Button icon='pi pi-trash' rounded text severity='danger' tooltip='Excluir' onClick={(e) => { e.stopPropagation(); excluir(r); }} />)} style={{ width: '4rem' }} />
                    </DataTable>
                </div>
                <div className='bc-form-col-6 col-12 md:col-6'>
                    {!sel && (<Card><p className='bc-muted'>Selecione um documento.</p></Card>)}
                    {sel && (<TabView>
                        <TabPanel header='Versões'>
                            <div className='flex justify-end gap-2 mb-2'><Button label='Enviar arquivo' size='small' icon='pi pi-upload' outlined onClick={() => fileRef.current && fileRef.current.click()} /><Button label='Solicitar aprovação' size='small' icon='pi pi-check' severity='success' onClick={solicitar} /></div>
                            <DataTable value={vers} paginator rows={6} emptyMessage='Sem versões.' responsiveLayout='scroll' dataKey='id'>
                                <Column field='versao' header='v' style={{ width: '3rem' }} />
                                <Column field='arquivoNome' header='Arquivo' />
                                <Column header='' body={(r) => (<Button icon='pi pi-download' rounded text tooltip='Baixar' onClick={() => baixar(r.id, r.arquivoNome)} />)} style={{ width: '4rem' }} />
                            </DataTable>
                        </TabPanel>
                        <TabPanel header='Aprovações'>
                            <DataTable value={aprovs} paginator rows={6} emptyMessage='Sem aprovações.' responsiveLayout='scroll' dataKey='id'>
                                <Column field='versao' header='v' style={{ width: '3rem' }} />
                                <Column field='aprovador' header='Aprovador' />
                                <Column field='status' header='Status' body={(r) => <Tag value={r.status} severity={r.status === 'APROVADA' ? 'success' : r.status === 'REJEITADA' ? 'danger' : 'warning'} />} />
                                <Column header='' body={(r) => (r.status === 'PENDENTE' ? (<div className='flex gap-1'><Button icon='pi pi-check' rounded text severity='success' onClick={() => decidir(r.id, true)} /><Button icon='pi pi-times' rounded text severity='danger' onClick={() => decidir(r.id, false)} /></div>) : null)} style={{ width: '6rem' }} />
                            </DataTable>
                        </TabPanel>
                    </TabView>)}
                </div>
            </div>
            <Card title='Retenção vencida' className='mt-3' subTitle='Documentos com reter_até passado'>
                <DataTable value={ret} paginator rows={5} emptyMessage='Nada vencido.' responsiveLayout='scroll' dataKey='id'>
                    <Column field='codigo' header='Código' />
                    <Column field='titulo' header='Título' />
                </DataTable>
            </Card>
            <Dialog visible={dlg} onHide={() => setDlg(false)} header='Novo documento' modal style={{ width: 'min(96vw, 520px)' }}>
                <div className='grid p-fluid'>
                    <div className='bc-form-col-6'><label className='bc-label'>Código *</label><InputText value={f.codigo || ''} onChange={(e) => setF({ ...f, codigo: e.target.value })} /></div>
                    <div className='bc-form-col-6'><label className='bc-label'>Categoria</label><InputText value={f.categoria || ''} onChange={(e) => setF({ ...f, categoria: e.target.value })} /></div>
                    <div className='bc-form-col-12'><label className='bc-label'>Título *</label><InputText value={f.titulo || ''} onChange={(e) => setF({ ...f, titulo: e.target.value })} /></div>
                    <div className='bc-form-col-6'><label className='bc-label'>Reter até</label><InputText value={f.reterAte || ''} onChange={(e) => setF({ ...f, reterAte: e.target.value })} placeholder='AAAA-MM-DD' /></div>
                </div>
                <div className='flex justify-end gap-2 mt-3'><Button label='Cancelar' text severity='secondary' onClick={() => setDlg(false)} /><Button label='Salvar' icon='pi pi-check' onClick={salvar} /></div>
            </Dialog>
        </div>
    );
};
export default DMS;
