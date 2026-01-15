create table if not exists siirtotiedosto_operaatio (
    id uuid not null,
    window_start varchar,
    window_end varchar,
    run_start timestamp with time zone not null default now(),
    run_end timestamp with time zone,
    PRIMARY KEY (id)
);
COMMENT ON column siirtotiedosto_operaatio.run_start IS 'Siirtotiedosto-operaation suorituksen alkuaika';
COMMENT ON column siirtotiedosto_operaatio.run_end IS 'Siirtotiedosto-operaation suorituksen loppuaika';