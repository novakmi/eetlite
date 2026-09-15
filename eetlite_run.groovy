#!/usr/bin/env groovy

import com.github.novakmi.libeetlite.EetUtil
import com.github.novakmi.libeetlite.EetXml
import groovy.util.logging.Slf4j

@Grapes([
        @GrabConfig(systemClassLoader = true),

        @Grab(group = 'ch.qos.logback', module = 'logback-classic', version = '1.4.14'),
        @Grab(group = 'org.apache.santuario', module = 'xmlsec', version = '4.0.4')

        // Java 8: comment out the versions above and uncomment these:
//         @Grab(group = 'ch.qos.logback', module = 'logback-classic', version = '1.3.14'),
//         @Grab(group = 'org.apache.santuario', module = 'xmlsec', version = '3.0.3')
])

/* (c) 2016 - 2026 Michal Novák, it.novakmi@gmail.com, see LICENSE file */
@Slf4j
class EetRunner {
    def version = "2.0.0"
    def scriptName = getClass().protectionDomain.codeSource.location.path
    def printToFile = 1

    // ****** UPRAVIT PARAMETRY *****
    def trzba_var = [
            porad_cis : "0/6460/ZQ42",
            dat_trzby : EetUtil.nowToIso(),
            celk_trzba: "7897.00",
            // urceno_cerp_zuct: "0.00",
            // cerp_zuct: "0.00",
    ]

    def trzba_fix = [
            eic_popl    : "CZ00000019",
            id_jednotky: "123",
            id_pokl     : "Q-126-R",
            // eic_poverujiciho: "CZ1212121218",
            // povereni_vice_popl: "true",
    ]

    def hlavicka = [
            overeni      : "false",
            prvni_zaslani: "true",
    ]

    def config_fix = [
            cert_popl_path: "cert/CA_EET-Playground-CZ00000019.p12",
            cert_pass     : null, // alternativně zadejte heslo přímo jako String
            cert_pass_file: "cert/password_pokladni_cert_playground.txt",
            url           : "https://pg.trzbyeet.gov.cz/eet/services/EETServiceSOAP/v4",
            validate_xml  : true,
            xsd_path      : "EETXMLSchema.xsd",
    ]
    // ****** KONEC PARAMETRU *****

    def config = hlavicka + trzba_var + trzba_fix + config_fix

    def getReceipt(message, fileName, pok, duration) {
        def nl = System.getProperty("line.separator")
        def ret = "eetlite ${version} uctenka" + nl
        ret += "https://sites.google.com/view/eetlite" + nl
        ret += "(https://github.com/novakmi/eetlite)" + nl
        if (printToFile) {
            ret += "Soubor: ${new File(fileName).path}" + nl
        }
        ret += "====================================" + nl
        EetXml.dataFields.keySet().each { key ->
            if (config[key] != null) {
                ret += "${key}: ${config[key]}" + nl
            }
        }
        ret += "POK: ${pok}" + nl
        ret += "REZIM: EET 2.0 (online)" + nl
        ret += "CAS ZPRACOVANI: ${duration}ms"
        return ret
    }

    def fileWithSubStringExists(dirName, fileSubString) {
        def files = new File(dirName).listFiles { file ->
            file.name.contains(fileSubString as String)
        }
        return files != null && files.length > 0
    }

    def validateXmlIfEnabled(config, xml) {
        if (!config.validate_xml) {
            log.info "Validace XML proti XSD je vypnuta"
            return
        }
        def xsd = new File(config.xsd_path)
        if (!xsd.isFile()) {
            throw new FileNotFoundException("XSD soubor nebyl nalezen: ${xsd.absolutePath}")
        }
        xsd.withInputStream { stream ->
            def errors = EetUtil.validateXml(xml, stream)
            if (!errors.empty) {
                throw new IllegalStateException("XML neprošlo validací: ${errors.join('; ')}")
            }
        }
    }

    def processEet(config, message, fileName) {
        def timeIn = System.currentTimeMillis()
        def toSend = message.xml.toString()
        validateXmlIfEnabled(config, toSend)

        def responseText = EetUtil.sendSOAPRequest(config.url, toSend)
        log.debug "indented response: {}", EetXml.indentXml(responseText)
        def processed = EetXml.processResponse(responseText)
        if (processed.failed) {
            throw new IllegalStateException("EET odpověď obsahuje chybu: ${processed.errors}")
        }

        def duration = System.currentTimeMillis() - timeIn
        def receipt = getReceipt(message, fileName, processed.pok, duration)
        println receipt
        if (printToFile) {
            new File(fileName).write(receipt, "UTF-8")
        }
    }

    def isValid(config, message, fileSubString) {
        if (message.failed) {
            println "Zprávu se nepodařilo vytvořit: ${message.errors}"
            return false
        }
        if (printToFile && fileWithSubStringExists(".", fileSubString)) {
            println "Uctenka s cislem ${config.porad_cis} jiz existuje."
            return false
        }
        return true
    }

    def run() {
        log.info "eetlite ver {}", version
        println "eetlite script Groovy: ${GroovySystem.version} JVM: ${System.getProperty("java.specification.version")}"

        config.cert_pass = config.cert_pass ?: new File(config.cert_pass_file).text.trim()
        if (!config.cert_pass) {
            throw new IllegalArgumentException("Nebylo zadano heslo certifikatu.")
        }

        def orderInFileName = "PC${config.porad_cis.replaceAll(/[\\\/:]/, "_")}"
        def fileName = scriptName.replace(".groovy",
                "_online_${new Date().format("yyyy_MM_dd_HH_mm_ss")}_${orderInFileName}_eetlite.txt")

        config.cert_popl = new FileInputStream(config.cert_popl_path)
        try {
            def message = EetXml.makeMsg(config)
            if (isValid(config, message, orderInFileName)) {
                processEet(config, message, fileName)
            }
        } finally {
            config.cert_popl.close()
        }
    }
}

new EetRunner().run()
