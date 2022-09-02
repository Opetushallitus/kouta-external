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

Lisäksi tarvitset Java SDK:n ja Scala SDK:n (Unix pohjaisissa käyttöjärjestelmissä auttaa esim. [SDKMAN!](https://sdkman.io/)). Katso [.travis.yml](.travis.yml) mitä versioita sovellus käyttää.
Kirjoitushetkellä käytössä openJDK11 ja scala 2.12.10.   

PostgreSQL kontti-image buildataan (täytyy tehdä vain kerran) komennnolla:
``` shell
# projektin juuressa
cd postgresql/docker
docker build --tag koutaexternal-postgres .
```

Asetuksia voi muuttaa muokkaamalla `src/test/resources/dev-vars.yml`-tiedostoa, tai
ainakin luulen näin, koska kouta-backendissa on vastaava rakenne. Kunhan joku selvittää
konfig-tiedoston toiminnan, toivottavasti päivittää myös tämän osion.

`src/test/resources/dev-vars.yml` ei ole versionhallinnassa. Saat luotua sen itsellesi seuraavalla komennolla:
`cp src/test/resources/dev-vars.yml.template src/test/resources/dev-vars.yml`

### 3.2. Testien ajaminen

Testejä varten täytyy Docker daemon olla käynnissä.

Testit voi ajaa ideassa Maven ikkunasta valitsemalla test lifecycle phasen kouta-externalin kohdalta
tai avaamalla Edit Configurations valikon ja luomalla uuden Maven run configurationin jolle laitetaan
working directoryksi projektin juurikansio ja Command line komennoksi test. Tämän jälkeen konfiguraatio ajoon.

Yksittäisen testisuiten tai testin voi ajaa ottamalla right-click halutun testiclassin tai funktion päältä, run -> scalaTest.

Jos Maven on asennettuna, voi testit ajaa myös komentoriviltä `mvn test` komennolla tai rajaamalla
ajettavien testejä `mvn test -Dsuites="<testiluokan nimet pilkulla erotettuna>"`.
Esimerkiksi `mvn test -Dsuites="fi.oph.kouta.external.integration.HakukohdeSpec"`

Testit käynnistävät Elasticsearchin ja postgresql:n docker-konteissa satunnaisiin vapaisiin portteihin.

### 3.3. Migraatiot

Migraatiot ajetaan automaattisesti testien alussa tai kun kouta-external käynnistetään.

### 3.4. Ajaminen lokaalisti

Ennen lokaalia ajoa täytyy olla elasticsearch pyörimässä. Kontin saa pystyyn ajamalla
```shell
docker run --rm --name koutaexternal-elastic --env "discovery.type=single-node" -p 127.0.0.1:9200:9200 -p 127.0.0.1:9300:9300 docker.elastic.co/elasticsearch/elasticsearch:6.8.13
```

tämän jälkeen käynnistä Ideassa embeddedJettyLauncher.scala (right-click -> Run). Tämä käynnistää samalla
postgresql kontin. Sovellus käynnistyy porttiin 8097.

### 3.5. Käyttöliittymä

Swagger löytyy osoitteesta [http://localhost:8097/kouta-external/swagger/](http://localhost:8097/kouta-external/swagger/)

### 3.6. Kehitystyökalut

Suositeltava kehitysympäristö on [IntelliJ IDEA](https://www.jetbrains.com/idea/) +
[scala plugin](https://plugins.jetbrains.com/plugin/1347-scala)

### 3.7. Testidata

Katso kouta-indeksoijan readme:stä kuinka saat lokaaliin elasticsearchiin indeksoitua dataa.
Tämän jälkeen käynnistä kouta-external tätä lokaalia elasticsearchia vasten.

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