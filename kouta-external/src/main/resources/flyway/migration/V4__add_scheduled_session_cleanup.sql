-- Siivoaa vanhentuneet sessiot kahden vuorokauden välein. Sessio vanhenee jo 60 minuutin
-- käyttämättömyyden jälkeen (ks. sessionDAO.getSessionQuery), joten kahden vuorokauden
-- raja jättää reilusti pelivaraa.
--
-- Session authorities-rivit poistuvat samalla, koska authorities.session on
-- "references sessions (id) on delete cascade" (ks. V1__create_sessions.sql).

-- Lokaalisti pg_cron on jo luotu superkäyttäjänä (postgresql/docker/3-configure-pg-cron.sh).

-- Tarvittavan extensionin luonti vaatii superuser- tai rds_superuser-oikeudet. App-käyttäjällä ei ole näitä oikeuksia,
-- joten testi- ja tuotantoympäristöihin on ajettava käsin superuser-oikeuksilla seuraavat komennot ennen asennusta:
-- CREATE EXTENSION IF NOT EXISTS pg_cron;
-- GRANT USAGE ON SCHEMA cron TO app;

SELECT cron.schedule('session-cleanup', '0 0 */2 * *', $$DELETE FROM sessions WHERE last_read < now() - interval '2 days'$$);
