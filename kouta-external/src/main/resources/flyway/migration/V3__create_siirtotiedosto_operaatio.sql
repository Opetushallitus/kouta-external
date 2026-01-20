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
COMMENT ON column siirtotiedosto_operaatio.window_start IS 'Aika, josta lähtien siirtotiedosto-operaatiossa käsiteltävät koulutukset on luotu tai päivitetty';
COMMENT ON column siirtotiedosto_operaatio.window_end IS 'Aika, johon mennessä siirtotiedosto-operaatiossa käsiteltävät koulutukset on luotu tai päivitetty';