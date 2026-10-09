import React from 'react';
import { useSearchParams, useNavigate, useLocation } from 'react-router-dom';
import { Button } from 'primereact/button';
import { Card } from 'primereact/card';

/** Páginas de retorno do Checkout Stripe (sucesso / cancelado). */
export const StripeResultado = () => {
    const [params] = useSearchParams();
    const nav = useNavigate();
    const loc = useLocation();
    const ok = loc.pathname.includes('sucesso');
    const session = params.get('session_id');

    return (
        <div className="p-4 flex justify-content-center">
            <Card className="w-full md:w-30rem">
                <h2 className="mt-0">{ok ? 'Pagamento iniciado / concluído' : 'Checkout cancelado'}</h2>
                <p className="text-color-secondary">
                    {ok
                        ? 'Se o pagamento for confirmado, o webhook Stripe baixará o título automaticamente.'
                        : 'Nenhuma cobrança foi feita. Você pode tentar de novo a partir do título.'}
                </p>
                {session && <p className="text-sm">Session: {session}</p>}
                <div className="flex gap-2 mt-3">
                    <Button label="Títulos" icon="pi pi-wallet" onClick={() => nav('/financeiro/titulos')} />
                    <Button label="Pagamentos Stripe" icon="pi pi-credit-card" outlined onClick={() => nav('/financeiro/stripe')} />
                </div>
            </Card>
        </div>
    );
};
export default StripeResultado;
