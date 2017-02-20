#!/usr/bin/env groovy
import com.github.novakmi.libeetlite.test.EetXml
@Grapes([
        @GrabConfig(systemClassLoader = true), //logback config can be read, thanks to https://gist.github.com/grimrose/3759266
        @Grab(group = 'ch.qos.logback', module = 'logback-classic', version = '1.1.8'),
        @Grab('com.github.groovy-wslite:groovy-wslite:1.1.3'),
        @Grab("org.apache.santuario:xmlsec:1.5.6"),
        @Grab("com.github.novakmi:libeetlite:0.3.0"),
])

import groovy.util.logging.Slf4j
import wslite.soap.SOAPClient
import wslite.soap.SOAPResponse

/* (c) 2016 - 2017 Michal Novák, it.novakmi@gmail.com, see LICENSE file */

@Slf4j
class EetRunner { // class is used for Slf4j annotation
    def version = "0.3.0"
    def scriptName = getClass().protectionDomain.codeSource.location.path

    // ****** UPRAVIT PARAMETRY *****
    //viz http://www.etrzby.cz/cs/technicka-specifikace
    // ------ VE VETSINE PRIPADU STACI UPRAVIT POUZE TUTO CAST -----
    def printToFile = 1  //0 .. uctenka jen na obrazovku, 1 .. uctenka i do souboru <jmeno>_rezim_<datum>_eetlite.txt
    def trzba_var = [
            porad_cis : "0/6460/ZQ42",               // poradove cislo uctenky (1-20 znaku)
            dat_trzby : "2017-01-21T18:45:15+01:00", // datum a cas prijeti trzby dle ISO 8601, rrrr-mm-ddThh:mm:ss±hh:mm (±hh je ±01 pro zimni cas, ±02 pro letni cas)
            celk_trzba: "7896.00",                   // celkova castka trzby
            /* nepovinne polozky (odstranit //)*/
//            zakl_nepodl_dph : "0.00",                 // celkova castka plneni osvobozenych od DPH, ostatnich plneni
//            zakl_dan1 : "0.00",                       // celkovy zaklad dane se zakladni sazbou DPH
//            dan1: "0.00",                             // celkova DPH se zakladni sazbou
//            zakl_dan2 : "0.00",                       // celkovy zaklad dane s prvni snizenou sazbou DPH
//            dan2: "0.00",                             // celkova DPH s prvni snizenou sazbou
//            zakl_dan3 : "0.00",                       // celkovy zaklad dane s druhou snizenou sazbou DPH
//            dan3: "0.00",                             // celkova DPH s druhou snizenou sazbou
//            cest_sluz : "0.00",                       // celková castka v rezimu DPH pro cestovni sluzbu
//            pouzit_zboz1 : "0.00",                    // celková castka v rezimu DPH pro prodej pouziteho zbozi se zakladni sazbou
//            pouzit_zboz2 : "0.00",                    // celková castka v rezimu DPH pro prodej pouziteho zbozi s prvni snizenou sazbou
//            pouzit_zboz3 : "0.00",                    // celková castka v rezimu DPH pro prodej pouziteho zbozi s druhou snizenou sazbou
//            urceno_cerp_zuct: "0.00",                  // celková castka plateb urcena k naslednemu cerpani nebo zuctovani
//            cerp_zuct: "0.00",                        // celková castka plateb, ktere jsou naslednym cerpanim nebo zuctovanim platby
    ]
    // ------

    // Nasledujici casti se upravi jednou, v ostatnich pripadech jsou jiz vetsinou stejne
    def trzba_fix = [
            rezim    : "0",                      // 0 .. bezny rezim (s Internetem), 1 .. zjednoduseny rezim (bez Internetu)
            dic_popl : "CZ00000019",             // DIC poplatnika, ktery odesila datovou zpravu (mel by se schodovat s certifikatem nize)
            id_provoz: "123",                    // oznaceni provozovny (1. az 5. cif. cislo)
            id_pokl  : "Q-126-R",                // oznaceni pokladniho zarizeni (1-20 znaku)
            /* nepovinne polozky (odstranit //)*/
//           dic_poverujiciho : "CZ1212121218",  // DIC poverujiciho poplatnika (nepovinne)
    ]

    def hlavicka = [
            overeni      : "0",   //0 .. ostry mod, 1 ... overovaci mod
            prvni_zaslani: "1",   //1 .. prvni zaslani trzby, 0 .. opakovane zaslani trzby
    ]

    def config_fix = [
            cert_popl: "cert/EET_CA1_Playground-CZ00000019.p12",  // cesta na certificat poplatnika (relativni k adresari eetlite)
            cert_pass: "eet",                             // heslo cetrifikatu (zatim text, pozdeji bude zasifrovano)
            url      : "https://pg.eet.cz:443/eet/services/EETServiceSOAP/v3", // url EET (testovaci prostredi)
            //url: "https://prod.eet.cz:443/eet/services/EETServiceSOAP/v3", // url EET (produkcni prostredi)
    ]
    // ****** KONEC PRO UPRAVU PARAMETRU *****

    def config = hlavicka + trzba_var + trzba_fix + config_fix

    def getReceipt(message, fileName, fik, rezim, duration) {
        log.debug "==> getReceipt"
        def nl = System.getProperty("line.separator");
        def ret = "eetlite ${version} uctenka" + nl
        ret += "https://sites.google.com/view/eetlite" + nl
        ret += "(https://github.com/novakmi/eetlite)" + nl
        if (printToFile) {
            def file = new File(fileName).getPath()
            ret += "Soubor: ${file}" + nl
        }
        ret += "====================================" + nl
        for (i in EetXml.dataFields.keySet()) {
            if (config[i]) {
                ret += "${i}: ${config[i]}" + nl
            }
        }

        ret += "BKP: ${message.bkp}" + nl
        if (!rezim) {
            ret += "FIK: ${fik}" + nl
        } else {
            ret += "PKP: ${message.pkp}" + nl
        }
        ret += "REZIM: ${!rezim ? "bezny (s Internetem)" : "zjednoduseny (bez Internetu)"}" + nl
        ret += "CAS ZPRACOVANI: ${duration}ms"
        log.debug "<== getReceipt ret=${ret}"
        return ret
    }

    def run() {
        log.info "==> run"
        log.info "eetlite ver {}", version

        // TODO validate config (mandatory prams, regex patterns)

        def timeIn = new Date();
        def message = EetXml.makeMsg(config)
        //TODO validate message against schema

        def toSend = message.xml.toString()
        log.debug "bkp: {}", message.bkp
        log.debug "toSend: {}", toSend
        def rezim = config.rezim != "0"

        def fik = ""
        if (!rezim) { // ostry rezim - Internet
            SOAPClient client = new SOAPClient(config.url)
            SOAPResponse response = client.send(toSend)

            log.trace "response {}", response
            def respText = response.text
            log.debug "indented response: {}", EetXml.indentXml(respText)

            //TODO processing error messages
            //TODO verify signed response
            fik = EetXml.processResponse(respText)
        } else {
            log.debug("Message not send, 'zjednoduseny rezim' ${config.rezim}")
        }
        def duration = new Date(new Date().getTime() - timeIn.getTime()).getTime()
        def fileName = scriptName.replace(".groovy",
                "_${!rezim ? "ostry" : "zjednoduseny"}_${new Date().format("yyyy_MM_dd_hh_mm_ss")}_eetlite.txt")
        def receipt = getReceipt(message, fileName, fik, rezim, duration)
        println receipt
        if (printToFile) {
            new File(fileName).write(receipt)
        }

        log.info "<== run fik {}", fik
    }
}

new EetRunner().run()