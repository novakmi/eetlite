#!/usr/bin/env groovy
/* (c) Michal Novák, it.novakmi@gmail.com, see LICENSE file */

@Grapes([
        @GrabConfig(systemClassLoader = true), //logback config can be read, thanks to https://gist.github.com/grimrose/3759266
        @Grab(group = 'ch.qos.logback', module = 'logback-classic', version = '1.1.7'),
        @Grab('com.github.groovy-wslite:groovy-wslite:1.1.2'),
        @Grab("org.apache.santuario:xmlsec:1.5.6"),
])

import groovy.util.logging.Slf4j
import wslite.soap.SOAPClient
import wslite.soap.SOAPResponse

@Slf4j
class EetRunner { // class is used for Slf4j annotation
    def version = "0.0.1"

    def trzba_var = [
            porad_cis : "0/6460/ZQ42",
            dat_trzby : "2016-07-14T18:45:15+02:00",
            celk_trzba: "7896.00",
            rezim     : "0",
            // TODO optional parts
    ]

    def trzba_fix = [
            dic_popl : "CZ1212121218",
            id_provoz: "123",
            id_pokl  : "Q-126-R",
            rezim    : "0",
    ]

    def config_var = [
            overeni      : "0",
            prvni_zaslani: "1",
    ]

    def config_fix = [
            cert_popl: "cert/01000003.p12",
            cert_pass: "eet",
            url: "https://pg.eet.cz:443/eet/services/EETServiceSOAP/v2"
    ]

    def trzba = trzba_var + trzba_fix
    def config = config_var + config_fix

    def run() {
        log.info "==> run"
        log.info "eetlite ver {}", version

        // TODO validate trzba
        // TODO validate config

        def uniques = [
                bodyId: "BodyId+${EetUtil.getUnique()}",
                tokenId: "TokenId+${EetUtil.getUnique()}",
                signatureId: "SigId+${EetUtil.getUnique()}",
                keyId: "KeyId+${EetUtil.getUnique()}",
                referenceId: "RefId+${EetUtil.getUnique()}",
        ]
        def message = EetXml.makeMsg(trzba, config, uniques)

        def toSend = message.toString()
        log.debug "toSend: {}", toSend
        SOAPClient client = new SOAPClient(config.url)
        SOAPResponse response = client.send(toSend)

        log.trace "response {}", response
        def respText = response.text
        log.debug "indented response: {}",  EetXml.indentXml(respText)

        //TODO processing error messages
        def fik = EetXml.processResponse(respText)
        println "FIK: ${fik}"

        log.info "<== run fik {}", fik
    }
}

new EetRunner().run()