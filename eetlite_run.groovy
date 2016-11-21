#!/usr/bin/env groovy

/* (c) Michal Novák, it.novakmi@gmail.com, see LICENSE file */

@Grapes([
        @GrabConfig(systemClassLoader = true), //logback config can be read, thanks to https://gist.github.com/grimrose/3759266
        @Grab(group = 'ch.qos.logback', module = 'logback-classic', version = '1.1.7'),
        @Grab('com.github.groovy-wslite:groovy-wslite:1.1.2'),
        @Grab("org.apache.santuario:xmlsec:1.5.6"),
        @Grab("com.github.novakmi:libeetlite:0.2.0"),
])

import groovy.util.logging.Slf4j
import wslite.soap.SOAPClient
import wslite.soap.SOAPResponse
import com.github.novakmi.libeetlite.test.EetXml

@Slf4j
class EetRunner { // class is used for Slf4j annotation
    def version = "0.1.0"

    // ****** UPRAVIT PARAMETRY *****

    //viz http://www.etrzby.cz/cs/technicka-specifikace

    // ------ VE VETSINE PRIPADU STACI UPRAVIT POUZE TUTO CAST -----
    def trzba_var = [
            porad_cis : "0/6460/ZQ42",               // poradove cislo uctenky (1-20 znaku)
            dat_trzby : "2016-11-20T18:45:15+02:00", // datum a cas prijeti trzby dle ISO 8601, rrrr-mm-ddThh:mm:ss±hh:mm (±hh je ±01 pro zimni cas, ±02 pro letni cas)
            celk_trzba: "7896.00",                   // celkova castka trzby
            // nepovinne polozky (odstranit //)
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
            dic_popl : "CZ1212121218",           // DIC poplatnika, ktery odesiladatovou zpravu
            id_provoz: "123",                    // oznaceni provozovny (1. az 5. cif. cislo)
            id_pokl  : "Q-126-R",                // oznaceni pokladniho zarizeni (1-20 znaku)
            rezim    : "0",                      // 0 .. bezny rezim, 1 .. zjednoduseny rezim
            // nepovinne polozky (odstranit //)
//            dic_poverujiciho : "CZ1212121218",  // DIC poverujiciho poplatnika (nepovinne)
    ]

    def hlavicka = [
            overeni      : "0",   //0 .. ostry mod, 1 ... overovaci mod
            prvni_zaslani: "1",   //1 .. prvni zaslani trzby, 0 .. opakovane zaslani trzby
    ]

    def config_fix = [
            cert_popl: "cert/01000003.p12",               // cesta na certificat poplatnika (relativni k adresari eetlite)
            cert_pass: "eet",                             // heslo cetrifikatu (zatim text, pozdeji bude zasifrovano)
            url: "https://pg.eet.cz:443/eet/services/EETServiceSOAP/v3" // url EET (zatim testovaci prostredi)
    ]
    // ***********

    def config = hlavicka + trzba_var + trzba_fix + config_fix

    def run() {
        log.info "==> run"
        log.info "eetlite ver {}", version

        // TODO validate config (mandatory prams, regex patterns)

        def message = EetXml.makeMsg(config)
        //TODO validate message against schema

        def toSend = message.xml.toString()
        log.debug "bkp: {}", message.bkp
        log.debug "toSend: {}", toSend
        SOAPClient client = new SOAPClient(config.url)
        SOAPResponse response = client.send(toSend)

        log.trace "response {}", response
        def respText = response.text
        log.debug "indented response: {}",  EetXml.indentXml(respText)

        //TODO processing error messages
        //TODO verify signed response
        def fik = EetXml.processResponse(respText)
        println "eetlite ${version} uctenka"
        println "(https://github.com/novakmi/eetlite)"
        println "===================================="
        for (i in EetXml.dataFields.keySet()) {
            if (config[i]) {
                println "${i}: ${config[i]}"
            }
        }
        println "FIK: ${fik}"
        println "BKP: ${message.bkp}"

        log.info "<== run fik {}", fik
    }
}

new EetRunner().run()