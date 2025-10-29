[![Kouta-external](https://github.com/Opetushallitus/kouta-external/actions/workflows/build.yml/badge.svg)](https://github.com/Opetushallitus/kouta-external/actions/workflows/build.yml)

# kouta-external

## 1. Palvelun tehtävä

Tarjoaa rajapinnan ulkoisille palveluille uuden koulutustarjonnan indeksoituun dataan.   
Mahdollistaa myös kouta-backendin hakujen luonnin ja muokkauksen. 

## 2. Arkkitehtuuri

Kouta-external on Scalatralla toteutettu HTTP API, joka tarjoilee kouta-indeksoijan Elasticsearchiin indeksoimaa
kouta-backendin dataa.  

## 3. Kehitysympäristö

### 3.1. Esivaatimukset

Asenna haluamallasi tavalla koneellesi
1. [IntelliJ IDEA](https://www.jetbrains.com/idea/) + [scala plugin](https://plugins.jetbrains.com/plugin/1347-scala)
2. [Docker](https://www.docker.com/get-started) (postgresia ja elasticsearchia varten)
3. [Maven](https://maven.apache.org/) Jos haluat ajaa komentoriviltä Mavenia,
   mutta idean Mavenilla pärjää kyllä hyvin, joten tämä ei ole pakollinen

Lisäksi tarvitset Java SDK:n ja Scala SDK:n (Unix pohjaisissa käyttöjärjestelmissä auttaa esim. [SDKMAN!](https://sdkman.io/)).
Kirjoitushetkellä käytössä openJDK11 ja scala 2.12.10.   

PostgreSQL kontti-image buildataan (täytyy tehdä vain kerran) komennnolla:
``` shell
# projektin juuressa
cd postgresql/docker
docker build --tag koutaexternal-postgres .
```

Kopioi lokaalia kehitystä varten konfiguraatiotiedosto '/src/test/resources/dev-vars.template.yml' -> '/src/test/resources/dev-vars.yml'. Dev-vars.yml on ignoroitu Gitissä ettei salasanat valu repoon.

Lokaalin ajon asetuksia voi muuttaa muokkaamalla '/src/test/resources/dev-vars.yml'-tiedostoa. 

### 3.2. Testien ajaminen

Jos Maven on asennettuna, voi testit ajaa myös komentoriviltä `mvn test` komennolla tai rajaamalla
ajettavien testejä `mvn test -Dsuites="<testiluokan nimet pilkulla erotettuna>"`.
Esimerkiksi `mvn test -Dsuites="fi.oph.kouta.external.integration.HakukohdeSpec"`

Jos ajat testejä monta kertaa peräkkäin, ajamista voi nopeuttaa
pitämällä testidatalla täytetyn ElasticSearchin koko ajan käynnissä.
Tämä onnistuu seuraavilla komennoilla:

``` shell
$ docker-compose up -d kouta-elastic
$ docker-compose up elasticdump-loader
$ export TEST_USE_PRERUN_ELASTIC=true
```

### 3.3. Migraatiot

Migraatiot ajetaan automaattisesti testien alussa tai kun kouta-external käynnistetään.

### 3.4. Ajaminen lokaalisti

Ennen lokaalia ajoa täytyy olla elasticsearch pyörimässä. Katso ohjeet elasticsearch-kontin käynnistämiseen [kouta-indeksoijan README:sta](https://github.com/Opetushallitus/kouta-indeksoija/#elasticsearch-kontin-käynnistys)

Tämän jälkeen käynnistä Ideassa embeddedJettyLauncher.scala (right-click -> Run). Tämä käynnistää samalla
postgresql kontin. Sovellus käynnistyy porttiin 8097.

### 3.5. Ajaminen lokaalisti testiympäristön ElasticSearchia vasten

Lisää `dev-vars.yml`-tiedosto `kouta-external/kouta-external/src/test/resources`-hakemistoon ja korvaa `*YMPÄRISTÖ*` testiympäristön arvolla `testi`, `hahtuva` tai `untuva`:

    host_postgresql_koutaexternal: localhost
    host_postgresql_koutaexternal_port: 5435
    postgres_app_user: oph
    host_postgresql_koutaexternal_app_password: oph

    cas_url: https://virkailija.*YMPÄRISTÖ*opintopolku.fi/cas
    kouta_external_cas_service: http://localhost:8097/kouta-external/auth/login
    kouta_external_cas_username: DUMMY
    kouta_external_cas_password: DUMMY

    kouta_external_elasticsearch7_url: *ES_URL*
    kouta_external_elasticsearch_auth_enabled: true
    kouta_external_elasticsearch_username: *ES_TUNNUS*
    kouta_external_elasticsearch_password: *ES_SALASANA*

    host_virkailija: virkailija.*YMPÄRISTÖ*opintopolku.fi
    host_alb_virkailija: virkailija.*YMPÄRISTÖ*opintopolku.fi

    kouta_external_api_modify_enabled: true

Korvaa myös `*ES_URL*` ES:n ympäristökohtaisella **julkisella** osoitteella, joka löytyy täältä: https://wiki.eduuni.fi/pages/viewpage.action?pageId=266406750

Korvaa `*ES_TUNNUS*` ja `*ES_SALASANA*` oikeilla testiympäristön salasanoilla, jotka voit katsa SSM:stä AWS:n konsolin kautta tai [config.py-komentorivityökalulla](https://github.com/Opetushallitus/cloud-base/blob/master/docs/configuring-services.md#salaisuuden-hakeminen-ssmst%C3%A4-interaktiivisesti)

Kytke päälle [Opintopolun VPN](https://github.com/Opetushallitus/cloud-base/blob/master/docs/vpn.md).

**Huom!** Jos käytät SSM:stä löytyvää ElasticSearchin sisäverkon osoitetta etkä julkista, joudut tunneloimaan liikenteen bastionin läpi. Julkisella osoitteella riittää, että kytket päälle Opintopolun VPN:n

Käynnistä kouta-external lokaalisti ajamalla IntelliJ IDEA:ssa EmbeddedJettyLauncher ja mene osoitteeseen:

http://localhost:8097/kouta-external/auth/login

Kirjaudu sisään omilla testitunnuksillasi.

Lokaalin kouta-externalin pitäisi nyt ohjata kyselyt valitsemasi ympäristön ElasticSearchiin.

### 3.6. Swagger

Swagger löytyy osoitteesta [http://localhost:8097/kouta-external/swagger/](http://localhost:8097/kouta-external/swagger/)

### 3.7. Kehitystyökalut

Suositeltava kehitysympäristö on [IntelliJ IDEA](https://www.jetbrains.com/idea/) +
[scala plugin](https://plugins.jetbrains.com/plugin/1347-scala)

### 3.8. Testidata

Katso kouta-indeksoijan readme:stä kuinka saat lokaaliin elasticsearchiin indeksoitua dataa.
Tämän jälkeen käynnistä kouta-external tätä lokaalia elasticsearchia vasten.

Kouta-indeksoijan avulla saat päivitettyä tarvittaessa myös testien käyttämän mock-data-dumpin.

## 4. Ympäristöt

### 4.1. Testiympäristöt

Testiympäristöjen swaggerit löytyvät seuraavista osoitteista:

- [hahtuva](https://virkailija.hahtuvaopintopolku.fi/kouta-external/swagger)
- [QA eli pallero](https://virkailija.testiopintopolku.fi/kouta-external/swagger)

### 4.2. Asennus

Asennus hoituu samoilla työkaluilla kuin muidenkin OPH:n palvelujen.
[Cloud-basen dokumentaatiosta](https://github.com/Opetushallitus/cloud-base/tree/master/docs) ja ylläpidolta löytyy apuja.

### 4.3. Lokit

Lokit löytyvät AWS:n CloudWatchista. Log groupin nimemssä on etuliitteenä ympäristön nimi,
esim. untuva-app-kouta-external

## 5. Koodin tyyli

Projekti käyttää [Scalafmt](https://scalameta.org/scalafmt/) formatteria koodin 
formatoitiin. Voit
vaihtaa idean scalan code style asetuksista formatteriksi scalafmt ja laittaa vaikka päälle
automaattisen formatoinnin tallennuksen yhteydessä.
