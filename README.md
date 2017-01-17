(c) Michal Novák, it.novakmi@gmail.com, see LICENSE file

# eetlite 

Implementace "Elektronické evidence tržeb"  http://www.etrzby.cz  
Implementation of  "Registration of sales" see  http://www.etrzby.cz

Toto je ukázka implementace "Elektronické evidence tržeb" s použitím XML zpracování v Groovy 
a s knihovnou `groovy-wslite`.  Implementace byla otestována oproti testovacímu prostředí EET.

Díky své jednoduchosti může být použitelná například pro prodej v kanceláři, provozovně a jiný sporadický prodej 
(např. občasná úhrada přes platební bránu či kartou) za pomocí běžného PC.

S použitím *zjednodušeného režimu* lze vytvořit účtenku i bez přítomnosti Internetu (např. u zákazníka) a tu pak
odeslat dodatečně v ostrém režimu.

Používání může vyžadovat určité znalosti příkazové řádky. Doporučuje se vše nejdříve vyzkoušet oproti 
testovacímu prostředí EET (výchozí nastavení). 

Stránky projektu:  
 
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

* 2017-01-18 version `0.2.0`
  * tisk účtenky do souboru
  * aktualizace dokumentace
  * aktualizace verzí závislých knihoven (dependence)
  
* 2016-11-21 version `0.1.0`
  * použití knihovny `libeetlite`    
  
* 2016-10-13 version `0.0.3`
  * upraveno pro testovací prostředí v3 (URL, namespace)    

* 2016-08-31 version `0.0.2`
  * přidána podpora volitelných parametrů
  * přidán popis parametrů pro editaci v souboru `./eetlite_run.groovy`  

* 2016-08-16 version `0.0.1`  
   * první verze
   * vytvoření a podepsání EET zprávy
   * odeslání EET zprávy na testovací prostředí
   * zpracování odpovědi
   * vytisknutí hodnoty 'FIK' z odpovědi

## Další vývoj (TODO)

* kontrola správnosti parametrů 'tržby' a konfigurace (částečně implementováno)
* validace XML zprávy oproti XML schématu
* podpora pro hash (zakódování) hesla v konfiguračním souboru
* zpracování chybové odpovědi
* kontrola podpisu odpovědi

### Výhled

* jednoduchá okenní nástavba
* jednoduchá aplikace pokladna pro PC s možností PDF účtenky

## Instalace

K provozu je nutná instalace jazyka/prostředí Groovy - http://groovy-lang.org/install.html.
Aplikace je k dispozici  ve formě zdrojových skriptů. Ty je třeba rozbalit do libovolného adresáře.

## Spuštění

Aplikaci lze používat z příkazové řádky.

* Nejprve se přesvědčte, že máte správně neinstalovanou podporu Groovy (příkaz `groovy --version`):

`$ groovy --version`  
`Groovy Version: 2.4.0 JVM: 1.8.0_101 Vendor: Oracle Corporation OS: Linux`
           
* Upravte parametry tržby a případné konfigurace (cestu na soubor s klíčem, heslo a další volby) v souboru `eetlite_run.groovy`.
  Pro úpravu lze použít jakýkoliv editor, doporučuje se použít editor s podporou syntaxe `groovy` (např. `notepad++`)
* Spusťte aplikaci přikazem `groovy eetlite_run.groovy` (Windows, Linux, Mac) popřípadě 
  příkazem `./eetlite_run.groovy` (Linux). Aplikaci je třeba spustit z adresáře, ve kterém jsou všechny její soubory.
  Při úplně prvním spuštění je třeba vyčkat, než Groovy stáhne potřebné 'dependence' (většinou do adresáře 
  `.groovy` v domovském adresáři).
* Po úspěšném zpracování se zobrazí jednoduchá účtenka dané tržby s hodnotou `FIK` a `BKP` a vytiskne do souboru 
  (je-li povoleno):
   
`$ groovy eetlite_run.groovy`

```
eetlite 0.2.0 uctenka
(https://github.com/novakmi/eetlite)
Soubor: /eetlite_run_ostry_2017_01_17_10_31_00_eetlite.txt
====================================
celk_trzba: 7896.00
dat_trzby: 2017-01-17T18:45:15+01:00
dic_popl: CZ00000019
id_pokl: Q-126-R
id_provoz: 123
porad_cis: 0/6460/ZQ42
rezim: 0
FIK: 225882ca-6cb4-4a06-8722-8c03e5ae54b9-ff
BKP: E33ABBF3-FF18F4EB-AACB71CF-7FAB2EC7-1E221A6B
REZIM: bezny (s Internetem)
CAS ZPRACOVANI: 992ms
```                      

## Použití
 
Od verze `0.1.0` je celá aplikace provozována jako jediný soubor (skript). Ze souboru `eetlite_run.groovy` lze vytvářet
kopie a mít tak například vlastní soubor pro každou platbu. Zároveň si lze takto připravit celou sadu nejčastěji 
používaných šablon. Pokud budete kopírovat soubor do jiného adresáře, překopírujete i soubor `logback.groovy` a 
popřípadě adresář `cert` (nebo změnte cestu jeho umístění ve skriptu).

**POZNÁMKA:** V základním tvaru tato aplikace komunikuje s testovacím prostředím EET, pro používání v produkčním 
              prostředí je potřeba přepnout `url:` a   `cert_popl:`
          
   
## Použité knihovny a nástroje

* Java  https://www.oracle.com/java/index.html
* Groovy http://groovy-lang.org/
* groovy-wslite  https://github.com/jwagenleitner/groovy-wslite
* slf4j http://www.slf4j.org/
* http://logback.qos.ch/
* libeetlite - https://github.com/novakmi/libeetlite

(_viz případné jednotlivé licence_)   
   
## Ladění

Změnou následujícího řádku v souboru `logback.groovy`:

`root(WARN, ["STDOUT"])   // change log level here to TRACE, DEBUG, INFO, WARN`
    
lze měnit úroveň logování na konzoli 

V případě, že je třeba nutno provádět ladění (úpravu kódu) na úrovni knihovny `libeetlite`,
lze doporučit následující postup:

* odstranit `@Grab` řádek  `@Grab("com.github.novakmi:libeetlite:0.1.0"),`, který imporutje knihovnu
* provést `clone` knihovny `libeetlite` pomocí `git clone https://github.com/novakmi/libeetlite`
    * spustit s `classpath` ukazující na zdrojové soubory `libeetlite` příkazem `groovy -cp <adresář s libeetlite>/libeetlite/src/main/groovy/ eetlite_run.groovy`
    * (nebo) nakopírovat obsah adresáře  `<adresář s libeetlite>/libeetlite/src/main/groovy` do adresáře se souborem `eetlite_run.groovy`

* nyní lze provádět změny ve zdrojových souborech `libeetlite`, které se projeví v ihned dalším spuštění 
  (v `groovy` není třeba nic překládat)

## Kontakt

K hlášení chyb, podávání podnětů na zlepšení lze použít:  

https://github.com/novakmi/eetlite/issues  
https://gitlab.com/novakmi/eetlite/issues
  
e-mail: it.novakmi@gmail.com

## Podobné projekty a odkazy

http://www.etrzby.cz/cs/technicka-specifikace  

https://github.com/l-ra/openeet    
https://github.com/todvora/eet-client  
