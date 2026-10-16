-- Siivoaa vanhentuneet sessiot kahden vuorokauden välein. Sessio vanhenee jo 60 minuutin
-- käyttämättömyyden jälkeen (ks. sessionDAO.getSessionQuery), joten kahden vuorokauden
-- raja jättää reilusti pelivaraa.
--
-- Session authorities-rivit poistuvat samalla, koska authorities.session on
-- "references sessions (id) on delete cascade" (ks. V1__create_sessions.sql).

-- Lokaalisti extension on jo luotu superkäyttäjänä (postgresql/docker/3-configure-pg-cron.sh),
-- jolloin tämä on no-op. Extensionin luonti vaatii superuser- tai rds_superuser-oikeudet.
CREATE EXTENSION IF NOT EXISTS pg_cron;

SELECT cron.schedule('session-cleanup', '0 0 */2 * *', $$DELETE FROM sessions WHERE last_read < now() - interval '2 days'$$);
