# ovara-kouta-light

## Kehitysympäristö

Sovellus käyttää kouta-externalin tietokantaa, joten embeddedJettyLauncherin tulee olla käynnissä, kun haluaa ajaa
SiirtotiedostoAppia kehitysympäristössä.

Ajoa varten kopioi konfiguraatio `ovara-kouta-light/ovara-kouta-light.dev.properties.example`-tiedoston sisältö 
`ovara-kouta-light/ovara-kouta-light.properties`-tiedostoon.

`ovara-kouta-light`-moduulin `SiirtotiedostoApp`ia pystyy ajamaan omalta koneelta,
kunhan lisää IDEA:n Run-konfiguraation "Environment Variables"-kenttään `AWS_PROFILE=oph-dev`,
kirjautuu AWS:ään sisään `aws sso login`-komennolla ja lisää kyseisen moduulin `pom.xml`:ään seuraavat riippuvuudet
(sekä ajaa Maven > Sync Project):
````
       <dependency>
           <groupId>software.amazon.awssdk</groupId>
           <artifactId>sso</artifactId>
           <version>2.33.12</version>
       </dependency>
       <dependency>
           <groupId>software.amazon.awssdk</groupId>
           <artifactId>ssooidc</artifactId>
           <version>2.33.12</version>
       </dependency>
````

SiirtotiedostoAppin voi ajaa IDEAn Run-komennolla.
