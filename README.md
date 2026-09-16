(c) Michal Novák, it.novakmi@gmail.com, see LICENSE file

# eetlite 

[![Run EET script](https://github.com/novakmi/eetlite/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/novakmi/eetlite/actions/workflows/ci.yml)
[![Pipeline status](https://gitlab.com/novakmi/eetlite/badges/main/pipeline.svg)](https://gitlab.com/novakmi/eetlite/-/pipelines)

Implementace "Elektronické evidence tržeb"  http://www.etrzby.cz  
Implementation of  "Registration of sales" see  http://www.etrzby.cz

Toto je ukázka implementace "Elektronické evidence tržeb" s použitím XML zpracování v Groovy
a knihovnou `libeetlite` 2.0.0. Implementace používá současné rozhraní EET 2.0/v4.

Díky své jednoduchosti může být použitelná například pro prodej v kanceláři, provozovně a jiný sporadický prodej 
(např. občasná úhrada přes platební bránu či kartou) za pomocí běžného PC.

EET 2.0 v této ukázce pracuje v online režimu a používá potvrzovací kód POK.

Používání může vyžadovat určité znalosti příkazové řádky. Doporučuje se vše nejdříve vyzkoušet oproti 
testovacímu prostředí EET (výchozí nastavení). 

Stránky projektu (popis instalace a použití):  

https://sites.google.com/view/eetlite

Zdrojové kódy:
 
https://github.com/novakmi/eetlite  
https://gitlab.com/novakmi/eetlite  

## Licence

Aplikace, její použití i zdrojové kódy jsou k dispozici zdarma pod MIT licencí.

## Odpovědnost

Autor nenese jakoukoliv odpovědnost za funkčnost a chování, ani neposkytuje jakoukoliv záruku!
Používání může vyžadovat určité znalosti používání příkazové řádky. Doporučuje se vše nejdříve vyzkoušet oproti 
testovacímu prostředí EET (výchozí nastavení).

Viz soubor `LICENSE`

## Changelog

* 2026-09-15 version `2.0.0`
  * migrace na EET 2.0/v4 a `libeetlite` 2.0.0
  * odstranění `groovy-wslite`, PKP, BKP a zjednodušeného režimu
  * volitelná validace XML proti XSD
  * heslo certifikátu lze zadat jako String nebo načíst ze souboru

* 2020-03-03 version `0.7.0`
  * libeetlite ver 0.6.0
  * otestovano s Oracle JDK 8, OpenJDK 11, OpenJDK 13, OpenJDK 14  
  * aktualizace README a LICENSE

* 2020-02-17 version `0.6.0`
  * libeetlite ver 0.5.1, logback 1.2.3
  * aktualizace certifkátů testovacího prostředí
  * aktualizace README a LICENSE
  
* 2017-03-09 version `0.5.0`
  *  libeetlite ver 0.5.0

* 2017-03-09 version `0.4.2`
  * pokud existuje v akt. adresáři soubor s nastaveným pořadovým číslem, tržba se nezpracuje

* 2017-03-06 version `0.4.1`
  * libeetlite ver 0.4.1

* 2017-03-06 version `0.4.0`
  * libeetlite ver 0.4.0
  * datum tržby je implicitně roven aktuálnímu času (lze změnit)   
  * certikát poplatníka je nyní předáván jako stream 
    (je třeba použít `new FileInputStream(path)` a stream uzavřít po zavolání `EetXml.makeMsg`)

* 2017-02-20 version `0.3.0`
  * ve zjednodušeném režimu se tiskne PKP 
   
* 2017-01-22 version `0.2.0`
  * tisk účtenky do souboru
  * aktualizace dokumentace
  * aktualizace verzí závislých knihoven (dependence)
  
* 2016-11-21 version `0.1.0`
  * použití knihovny `libeetlite`    
  
* 2016-10-13 version `0.0.3`
  * původní implementace pro testovací prostředí v3 (URL, namespace)

* 2016-08-31 version `0.0.2`
  * přidána podpora volitelných parametrů
  * přidán popis parametrů pro editaci v souboru `./eetlite_run.groovy`  

* 2016-08-16 version `0.0.1`
   * první verze
   * vytvoření a podepsání EET zprávy
   * odeslání EET zprávy na testovací prostředí
   * zpracování odpovědi původního rozhraní

## Další vývoj (TODO)

* kontrola správnosti parametrů 'tržby' a konfigurace (částečně implementováno)
* podpora pro hash (zakódování) hesla v konfiguračním souboru
* zpracování chybové odpovědi
* kontrola podpisu odpovědi

### Výhled

* jednoduchá okenní nástavba
* jednoduchá aplikace pokladna pro PC s možností PDF účtenky

## Instalace

K provozu je nutná Java (Java SE JDK http://www.oracle.com/technetwork/java/javase/downloads/index.html)
a instalace jazyka/prostředí Groovy - http://groovy-lang.org/download.html.
Pro MS Window spoužijte Windows installer (stačí zvolit pouze `Groovy binaries` a `Modify variables`). 
V prostředí Linux bývá instalace součástí systémů balíčků (např. `apt-get install groovy`). 
V jiných případech postupujte dle návodu http://groovy-lang.org/install.html.

Aplikace je k dispozici  ve formě zdrojových skriptů. Ty je třeba rozbalit do libovolného adresáře.

## Spuštění

Aplikaci lze používat z příkazové řádky nebo spuštěním scriptu (2x kliknout myší).

* Nejprve se přesvědčte, že máte správně neinstalovanou podporu Groovy (příkaz `groovy --version`):

`$ groovy --version`  
`Groovy Version: 5.1.2 JVM: 25.0.1 Vendor: Eclipse Adoptium OS: Linux`
           
* Upravte parametry tržby a konfigurace (cestu k certifikátu, heslo a další volby) v souboru `eetlite_run.groovy`.
  `cert_pass` lze vyplnit přímo jako String, nebo lze použít `cert_pass_file`.
  XML validaci lze zapnout/vypnout pomocí `validate_xml`; cesta ke schématu je v `xsd_path`.
  Pro úpravu lze použít jakýkoliv editor, doporučuje se použít editor s podporou 
  syntaxe `groovy` a Linux formátu  (popř. `JEdit`, `notepad++`)
* Spusťte aplikaci z adresáře, ve kterém jsou všechny její soubory, příkazem
  `groovy -cp /cesta/k/libeetlite/src/main/groovy eetlite_run.groovy`.
  Pokud jste zdrojový adresář knihovny nakopírovali do skriptu, použijte `groovy -cp . eetlite_run.groovy`.
  Při úplně prvním spuštění je třeba vyčkat, než Groovy stáhne potřebné 'dependence' (většinou do adresáře 
  `.groovy` v domovském adresáři).
* Po úspěšném zpracování se zobrazí jednoduchá účtenka dané tržby s hodnotou `POK` a vytiskne do souboru
  (je-li povoleno):
   
`$ groovy eetlite_run.groovy`

```
eetlite 2.0.0 uctenka
https://sites.google.com/view/eetlite
(https://github.com/novakmi/eetlite)
Soubor: /eetlite_run_online_2026_09_15_11_06_42_PC0_6460_ZQ42_eetlite.txt
====================================
celk_trzba: 7896.00
dat_trzby: 2026-09-15T18:45:15+02:00
eic_popl: CZ00000019
id_pokl: Q-126-R
id_jednotky: 123
porad_cis: 0/6460/ZQ42
POK: 58014b05-1bc5-46d0-8174-46bcf8ef8124-fa
REZIM: EET 2.0 (online)
CAS ZPRACOVANI: 988ms
```                      

## Použití
 
Od verze `0.1.0` je celá aplikace provozována jako jediný soubor (skript). Ze souboru `eetlite_run.groovy` lze vytvářet
kopie a mít tak například vlastní soubor pro každou platbu. Zároveň si lze takto připravit celou sadu nejčastěji 
používaných šablon. Pokud budete kopírovat soubor do jiného adresáře, překopírujete i soubor `logback.groovy` a 
popřípadě adresář `cert` (nebo změňte cestu jeho umístění ve skriptu).

**POZNÁMKA:** V základním tvaru tato aplikace komunikuje s testovacím prostředím EET, pro používání v produkčním 
              prostředí je potřeba přepnout `url:` a   `cert_popl:`
          
   
## Použité knihovny a nástroje

* Java  https://www.oracle.com/java/index.html
* Groovy http://groovy-lang.org/
* slf4j http://www.slf4j.org/
* http://logback.qos.ch/
* libeetlite 2.0.0 - https://github.com/novakmi/libeetlite

(_viz případné jednotlivé licence_)   
   
## Ladění

Změnou následujícího řádku v souboru `logback.groovy`:

`root(WARN, ["STDOUT"])   // change log level here to TRACE, DEBUG, INFO, WARN`
    
lze měnit úroveň logování na konzoli 

Skript nepoužívá `@Grab` pro `libeetlite`, protože artefakt nemusí být dostupný v repozitáři
Grape. K provozu je proto nutné zpřístupnit zdrojové soubory knihovny na classpath:

* naklonujte nebo stáhněte knihovnu `libeetlite`,
* spusťte skript s classpath na její zdrojové soubory:

`groovy -cp /cesta/k/libeetlite/src/main/groovy eetlite_run.groovy`

Alternativně nakopírujte adresář `libeetlite/src/main/groovy/com` do adresáře skriptu
a spusťte `groovy -cp . eetlite_run.groovy`.

Skript si přes `@Grab` stáhne pouze podpůrné knihovny `logback` a `xmlsec`. Při použití
zdrojů `libeetlite` není nutné knihovnu překládat.

## CI

GitHub Actions i GitLab CI spouštějí skript při každém pushi a jednou týdně podle
plánované pipeline. GitHub Actions používá matici runnerů
`ubuntu-latest`, `windows-latest` a `macos-latest`, takže je skript ověřen na
všech třech platformách. CI nejprve zpřístupní zdrojový adresář knihovny
`libeetlite` a potom spustí:

`groovy -cp libeetlite/src/main/groovy eetlite_run.groovy`

Certifikát se načítá z `cert/CA_EET-Playground-CZ00000019.p12`, heslo z
`cert/password_pokladni_cert_playground.txt` a validace XML z lokálního souboru
`EETXMLSchema.xsd`. CI tedy nepotřebuje další aplikační konfiguraci ani externí
uložení certifikátu.

GitHub Actions používá
https://github.com/marketplace/actions/setup-groovy[`wtfjoke/setup-groovy`]
s přesně určenou verzí Groovy `5.1.1` a Java `21`. Není proto potřeba explicitní
`docker run` ani instalace Groovy přes systémový balíček. GitLab CI používá
oficiální Docker image z
https://github.com/groovy/docker-groovy[`groovy:5.1.1-jdk21`], takže verze Groovy
je v CI pevně určena. Tag `groovy:5.1.2-jdk21` momentálně není dostupný;
aktualizaci verze je vhodné provést současně s ověřením dostupného tagu.

## Kontakt

Stránky projektu:

https://sites.google.com/view/eetlite

K hlášení chyb, podávání podnětů na zlepšení lze použít:  

https://github.com/novakmi/eetlite/issues  
https://gitlab.com/novakmi/eetlite/issues
  
e-mail: it.novakmi@gmail.com

## Podobné projekty a odkazy

https://github.com/l-ra/openeet    
https://github.com/todvora/eet-client
