import React, { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Card } from 'primereact/card';
import { Button } from 'primereact/button';
import { InputText } from 'primereact/inputtext';
import { Password } from 'primereact/password';
import { Message } from 'primereact/message';
import { Toast } from 'primereact/toast';
import { Tag } from 'primereact/tag';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { apiFetch } from '../../services/ApiConfig';
import './CertificadoDigital.css';

/**
 * Certificado digital para emissao de nota (A1 / .pfx).
 *
 * O caminho e digitado a cada uso em vez de vir de variavel de ambiente: em
 * LTDA o certificado e obrigatorio para emitir, e quem instala o sistema em
 * uma maquina nova nao tem como adivinhar o caminho do .pfx.
 *
 * Guardar no banco e escolha da pessoa, nao efeito colateral — o arquivo e a
 * credencial que libera a emissao da empresa.
 */
export const CertificadoDigital = () => {
    const { t } = useTranslation();
    const nav = useNavigate();
    const toast = useRef(null);
    const [caminho, setCaminho] = useState('');
    const [senha, setSenha] = useState('');
    const [salvar, setSalvar] = useState(false);
    const [carregando, setCarregando] = useState(false);
    const [erro, setErro] = useState('');
    const [sucesso, setSucesso] = useState(null);
    const [guardados, setGuardados] = useState([]);

    const carregar = async () => {
        if (!caminho.trim()) {
            setErro('Informe o caminho do certificado (.pfx ou .p12)');
            return;
        }
        setCarregando(true);
        setErro('');
        setSucesso(null);
        try {
            const r = await apiFetch('/api/fiscal/certificados/carregar', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    caminho: caminho.trim(),
                    senha,
                    salvar
                })
            });
            const corpo = await r.json();
            if (!r.ok) {
                setErro(corpo?.data || corpo?.errors?.[0]?.message || `HTTP ${r.status}`);
                return;
            }
            setSucesso(corpo.data);
            setSenha('');
            toast.current?.show({
                severity: corpo.data.gravado ? 'success' : 'info',
                summary: corpo.data.gravado ? 'Certificado guardado' : 'Certificado carregado',
                detail: corpo.data.mensagem,
                life: 4000
            });
            carregarGuardados();
        } catch (e) {
            setErro(e.message);
        } finally {
            setCarregando(false);
        }
    };

    const carregarGuardados = async () => {
        try {
            const r = await apiFetch('/api/fiscal/certificados');
            if (r.ok) {
                const corpo = await r.json();
                setGuardados(corpo?.data || []);
            }
        } catch {
            // lista e acessoria: nao impede o uso
        }
    };

    useEffect(() => { carregarGuardados(); }, []);

    return (
        <div className="certificado-digital">
            <Toast />

            <Message
                className="mb-3 w-full"
                severity="info"
                text="Preferência: cadastre o certificado no cadastro da empresa (matriz/filial) em Configurar empresa — junto com a API key Stripe."
            />
            <div className="mb-3">
                <Button label="Ir para cadastro da empresa" icon="pi pi-building" outlined onClick={() => nav('/configurar-empresa')} />
            </div>
            <div className="certificado-digital__header">
                <div>
                    <h2>Certificado Digital</h2>
                    <p className="certificado-digital__sub">
                        Certificado A1 usado para assinar as mensagens XML das notas.
                        {' '}<strong>A senha fica na sua máquina</strong> — ela não é gravada no banco.
                    </p>
                </div>
                <Tag severity="info" value="módulo: fiscal" />
            </div>

            {erro && <Message severity="error" text={erro} className="mb-3" onLifeEnd={() => setErro('')} />}

            {sucesso && (
                <Message
                    severity={sucesso.gravado ? 'success' : 'info'}
                    text={`${sucesso.arquivo} — ${sucesso.bytes} bytes. ${sucesso.mensagem}`}
                    className="mb-3"
                    onLifeEnd={() => setSucesso(null)}
                />
            )}

            <div className="certificado-digital__grid">
                <Card title="Carregar certificado">
                    <div className="field">
                        <label htmlFor="cert-caminho">Caminho do arquivo (.pfx ou .p12)</label>
                        <InputText
                            id="cert-caminho"
                            value={caminho}
                            onChange={(e) => setCaminho(e.target.value)}
                            placeholder="/caminho/do/certificado.pfx"
                            className="w-full"
                        />
                        <small className="field__hint">
                            Caminho absoluto no computador onde a nota será emitida.
                        </small>
                    </div>

                    <div className="field">
                        <label htmlFor="cert-senha">Senha do certificado</label>
                        <Password
                            id="cert-senha"
                            value={senha}
                            onChange={(e) => setSenha(e.target.value)}
                            placeholder="senha do .pfx"
                            feedback={false}
                            toggleMask
                            className="w-full"
                        />
                    </div>

                    <div className="field field--check">
                        <label>
                            <input
                                type="checkbox"
                                checked={salvar}
                                onChange={(e) => setSalvar(e.target.checked)}
                            />
                            {' '}Guardar no banco de documentos
                        </label>
                        <small className="field__hint">
                            {salvar
                                ? 'O arquivo será gravado na coleção "documentos", no módulo fiscal.'
                                : 'Nada será gravado. Use esta opção se preferir não deixar a credencial no banco.'}
                        </small>
                    </div>

                    <Button
                        label="Carregar certificado"
                        icon="pi pi-key"
                        loading={carregando}
                        disabled={!caminho.trim()}
                        onClick={carregar}
                    />
                </Card>

                <Card title="Guardados no banco">
                    {guardados.length === 0 ? (
                        <Message
                            severity="info"
                            text="Nenhum certificado guardado ainda."
                        />
                    ) : (
                        <DataTable value={guardados} size="small" stripedRows>
                            <Column field="arquivo" header="Arquivo" />
                            <Column field="tamanho" header="Bytes"
                                    body={(c) => new Intl.NumberFormat('pt-BR').format(c.tamanho)} />
                            <Column field="criadoEm" header="Guardado em" />
                        </DataTable>
                    )}
                </Card>
            </div>
        </div>
    );
};

export default CertificadoDigital;
