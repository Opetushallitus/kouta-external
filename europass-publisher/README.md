Europass-publisher a.k.a. kouta-external-europass
=================================================

Europass-publisher on eräajo-ohjelma, joka hakee kouta-elasticista
tietoja Suomen koulutustarjonnasta ja muuntaa ne Euroopan komission
QDR-portaalille kelpaavaan muotoon, joka on ELM-tietomallia (European
Learning Model) vastaava XML-skeema.

QDR-portaali on täällä:
 - varsinainen(https://europa.eu/europass/eportfolio/screen/course?lang=fi)
 - QA(https://webgate.acceptance.ec.europa.eu/europass/eportfolio/screen/course?lang=fi)

QDR-portaalin tiedontuottajapuoli:
 - varsinainen(https://europa.eu/europass/qdr/#/login)
 - QA(https://webgate.acceptance.ec.europa.eu/europass/qdr/)

XML-skeeman dokumentaatio:
 - Kaikki elementit (Courses on juurielementti):
   https://europa.eu/europass/elm-browser/documentation/xsd/ap/loq/documentation/loq_xsd.html
 - Vaaditut kentät:
   https://europass.europa.eu/en/stakeholders/qdr/publishdata#8705

Testien ajaminen
----------------

Päähakemistossa, siis git-repon juuressa:

``` shell
kouta-external$ docker-compose up -d kouta-elastic
kouta-external$ docker-compose up elasticdump-loader  # tarvitsee tehdä vain kerran
kouta-external$ (cd europass-publisher && TEST_USE_PRERUN_ELASTIC=true mvn test)
```

Lokaalisti ajaminen
-------------------

Päähakemistossa:

``` shell
kouta-external$ docker-compose up -d kouta-elastic europass-s3
kouta-external$ docker-compose up elasticdump-loader s3-configurator  # tarvitsee tehdä vain kerran
kouta-external$ (cd europass-publisher && mvn compile exec:java)
```

Ohjelma luo käynnistettäessä Elasticin tiedoista XML-tiedoston ja lataa
sen S3-ämpäriin.

Asennus
-------

Tämä tehdään cloud-basen CDK-työkaluilla, 

``` shell
cloud-base$ ./aws/cdk.sh pallero kouta-external-europass
```

Komento luo task-määrittelyn `ecs-support`-klusteriin, ja sille
EventBridgeen säännön jolla se käynnistyy kerran viikossa.

Käsin ajaminen ympäristössä
---------------------------

Scheduled taskin voi käynnistää käsin tekemällä käytännössä samat asiat
kuin mitä EventBridge tekisi käynnistettäessä.  Ohjelma luo
käynnistettäessä kouta-elasticin tiedoista XML-tiedoston ja lataa sen
S3-ämpäriin.

Esimerkiksi:

``` shell
cloud-base$ aws --profile oph-dev \
		ecs run-task \
		--cluster pallero-ecs-support \
		--task-definition koutaexternaleuropassScheduledTaskDefF51C5A26:11 \
		--launch-type FARGATE \
		--network-configuration '{"awsvpcConfiguration":{
			"subnets":["subnet-09c7b852","subnet-dde0d2ba","subnet-b52b2cfc"],
			"securityGroups":["sg-00b51659f4c6d7bda"]}}'
```

Kuten näkyy, oikeat arvot on aika vaikea arvata ja ne saa helpoiten
AWS:n konsolista menemällä oikean ECS-klusterin (kuten
pallero-ecs-support) Scheduled tasks -välilehteen ja valitsemalla sieltä
määrittelyn jonka nimessä on jossain kohtaa "koutaexternaleuro".

Kun task on käynnistetty, sen lokeihin pääsee melko helposti käsiksi
klusterin tasks-välilehden kautta.

Lopputulos näkyy (QA-ympäristössä) AWS-konsolissa paikassa osoitteessa
https://eu-west-1.console.aws.amazon.com/s3/object/europass-publish-pallero?region=eu-west-1&prefix=europass-export-1.xml
ja julkisesti osoitteessa
https://europass-publish-pallero.s3.eu-west-1.amazonaws.com/europass-export-1.xml

