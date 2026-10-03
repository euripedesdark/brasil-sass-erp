-- char(7) vira bpchar no Postgres e o Hibernate valida String como varchar.
ALTER TABLE brasil_saas.bc_ctb_lancamento ALTER COLUMN periodo TYPE varchar(7);
ALTER TABLE brasil_saas.bc_ctb_fechamento ALTER COLUMN periodo TYPE varchar(7);
