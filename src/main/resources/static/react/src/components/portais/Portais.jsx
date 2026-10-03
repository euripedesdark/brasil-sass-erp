import React, { useState, useEffect, useRef, useCallback } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Dialog } from 'primereact/dialog';
import { Dropdown } from 'primereact/dropdown';
import { InputNumber } from 'primereact/inputnumber';
import { InputText } from 'primereact/inputtext';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';

const BASE = '/api/portais';

export const Portais = () => {
    const toast = useRef(null);
    const [rows, setRows] = useState([]);
    const [loading, setLoading] = useState(true);
    const [dlg, setDlg] = useState(false);
    const [f, setF] = useState({ tipo: 'CLIENTE', pessoaId: '', diasValidade: 30 });
    const [link, setLink] = useState('');
    const js = async (r) => { const j = await r.json().catch(() => null); return Array.isArray(j) ? j : (j?.data ?? j ?? []); };
    const carregar = useCallback(async () => {
        setLoading(true);
        try { setRows(await apiFetch(BASE + '/acessos').then(js)); }
        catch (e) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: 'Falha ao carregar', life: 4000 }); }
        finally { setLoading(false); }
    }, []);
    useEffect(() => { carregar(); }, [carregar]);
    const gerar = async () => {
        if (!f.pessoaId) { toast.current?.show({ severity: 'warn', summary: 'Atenção', detail: 'Informe a pessoa', life: 3000 }); return; }
        const r = await apiFetch(BASE + '/acessos', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ tipo: f.tipo, pessoaId: Number(f.pessoaId), diasValidade: Number(f.diasValidade || 30) }) });
        const j = await r.json().catch(() => null);
        if (j && j.token) setLink(window.location.origin + '/portal?token=' + j.token);
        setDlg(false); carregar();
    };
    const revogar = async (id) => { await apiFetch(BASE + '/acessos/' + id + '/revogar', { method: 'POST' }); carregar(); };
    return (
        <div className='p-4'>
            <Toast ref={toast} />
            <div className='flex justify-content-between align-items-center mb-3 flex-wrap gap-2'>
                <div><h2 className='m-0'>Portais</h2><span className='bc-muted'>Autoatendimento: cliente, fornecedor e funcionário</span></div>
                <Button label='Gerar acesso' icon='pi pi-link' onClick={() => { setLink(''); setDlg(true); }} />
            </div>
            {link && (<div className='mb-3 p-3 border-round surface-card'><strong>Link gerado:</strong><br /><code style={{ userSelect: 'all' }}>{link}</code></div>)}
            <DataTable value={rows} loading={loading} paginator rows={10} emptyMessage='Nenhum acesso.' responsiveLayout='scroll' dataKey='id'>
                <Column field='tipo' header='Tipo' style={{ width: '9rem' }} />
                <Column field='pessoaId' header='Pessoa' style={{ width: '7rem' }} />
                <Column field='expiraEm' header='Expira em' />
                <Column header='Ativo' body={(r) => <Tag value={r.ativo ? 'Sim' : 'Não'} severity={r.ativo ? 'success' : 'secondary'} />} style={{ width: '6rem' }} />
                <Column header='' body={(r) => (r.ativo ? (<Button icon='pi pi-ban' rounded text severity='danger' tooltip='Revogar' onClick={() => revogar(r.id)} />) : null)} style={{ width: '4rem' }} />
            </DataTable>
            <Dialog visible={dlg} onHide={() => setDlg(false)} header='Gerar acesso' modal style={{ width: 'min(96vw, 480px)' }}>
                <div className='grid p-fluid'>
                    <div className='bc-form-col-6'><label className='bc-label'>Tipo *</label><Dropdown value={f.tipo} options={['CLIENTE','FORNECEDOR','FUNCIONARIO'].map((t) => ({ label: t, value: t }))} onChange={(e) => setF({ ...f, tipo: e.value })} /></div>
                    <div className='bc-form-col-6'><label className='bc-label'>Pessoa ID *</label><InputText value={f.pessoaId} onChange={(e) => setF({ ...f, pessoaId: e.target.value })} keyfilter='int' /></div>
                    <div className='bc-form-col-6'><label className='bc-label'>Validade (dias)</label><InputNumber value={f.diasValidade} onValueChange={(e) => setF({ ...f, diasValidade: e.value })} min={1} max={365} /></div>
                </div>
                <div className='flex justify-end gap-2 mt-3'><Button label='Cancelar' text severity='secondary' onClick={() => setDlg(false)} /><Button label='Gerar link' icon='pi pi-check' onClick={gerar} /></div>
            </Dialog>
        </div>
    );
};
export default Portais;
