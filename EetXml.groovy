/* (c) Michal Novák, it.novakmi@gmail.com, see LICENSE file */

//import com.sun.org.apache.xml.internal.security.c14n.Canonicalizer
import groovy.util.logging.Slf4j
import groovy.xml.StreamingMarkupBuilder

import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

@Slf4j
class EetXml {

    static String indentXml(def xml, def indent = 4) {
        log.trace "==> indentXml {} indent", xml, indent

        def factory = TransformerFactory.newInstance()
        factory.setAttribute("indent-number", indent);
        Transformer transformer = factory.newTransformer()
        transformer.setOutputProperty(OutputKeys.INDENT, 'yes')
        StreamResult result = new StreamResult(new StringWriter())
        transformer.transform(new StreamSource(new ByteArrayInputStream(xml.toString().bytes)), result)
        def res = result.writer.toString()

        log.trace "==> indentXml {}", res
        return res
    }

    // TODO not needed, if body is buuild with builder builder.expandEmptyElements = true
//    static String canonicalizeXml(xml) {
//        log.trace "==> canonicalizeXml {}", xml
//
//        com.sun.org.apache.xml.internal.security.Init.init()
//        def algo = Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS
//        Canonicalizer canon = Canonicalizer.getInstance(algo)
//        def canonXmlBytes = canon.canonicalize(xml.toString().getBytes("UTF-8"))
//        def canonXmlString = new String(canonXmlBytes)
//
//        log.trace "<== canonicalizeXml {}", canonXmlString
//        return canonXmlString
//    }

    static makeDigest(body) {
        log.debug "==> makeDigest {}", body

        def canonBody = body //EetXml.canonicalizeXml(body) //not needed  if  builder.expandEmptyElements = true
        def digestValue = EetUtil.makeDigestValue(canonBody)
        def ret = {
            "ds:DigestValue"(digestValue)
        }

        log.debug "<== makeDigest"
        return ret
    }

    static makeSignedInfo(id, body) {
        log.debug "==> makeSignedInfo {}", body
        def ret = {
            "ds:SignedInfo"("xmlns:ds": "http://www.w3.org/2000/09/xmldsig#", "xmlns:soap": "http://schemas.xmlsoap.org/soap/envelope/") {
                "ds:CanonicalizationMethod"(Algorithm: "http://www.w3.org/2001/10/xml-exc-c14n#") {
                    "ec:InclusiveNamespaces"("xmlns:ec": "http://www.w3.org/2001/10/xml-exc-c14n#", PrefixList: "soap")
                }
                "ds:SignatureMethod"(Algorithm: "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256")
                "ds:Reference"(URI: "#${id}") {
                    "ds:Transforms"() {
                        "ds:Transform"(Algorithm: "http://www.w3.org/2001/10/xml-exc-c14n#")
                    }
                    "ds:DigestMethod"(Algorithm: "http://www.w3.org/2001/04/xmlenc#sha256")
                    out << makeDigest(body)
                }
            }
        }
        log.debug "<== makeSignedInfo {}", ret
        return ret
    }

    static makeHeader(config, id, body, uniques) {
        log.debug "==> makeHeader {}", body

        final def keyMap = EetUtil.makeKeyMap(config)
        def binarySecToken = EetUtil.makeSecToken(keyMap)
        def tokenId = "${uniques.tokenId}"

        def builder = new StreamingMarkupBuilder()
        builder.expandEmptyElements = true
        builder.useDoubleQuotes = true
        def signedInfo = builder.bind {
            out << makeSignedInfo(id, body)
        }
        def sigInfo = signedInfo.toString()
        //def sigInfo = EetXml.canonicalizeXml(signedInfo.toString()) //Signature cannot be canonized!! (not valid TODO)!!
        def signatureValue = EetUtil.makeSignatureValue(config, keyMap, sigInfo)

        def retVal = {
            "SOAP-ENV:Header"("xmlns:SOAP-ENV": "http://schemas.xmlsoap.org/soap/envelope/") {
                "wsse:Security"("xmlns:wsse": "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
                        "xmlns:wsu": "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
                        "soap:mustUnderstand": "1") {
                    "wsse:BinarySecurityToken"(EncodingType: "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary",
                            ValueType: "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509v3",
                            "wsu:Id": tokenId, binarySecToken)
                    "ds:Signature"("xmlns:ds": "http://www.w3.org/2000/09/xmldsig#", Id: "${uniques.signatureId}") {
                        out << makeSignedInfo(id, body)
                        "ds:SignatureValue"(signatureValue)
                        "ds:KeyInfo"(Id: "${uniques.keyId}") {
                            "wsse:SecurityTokenReference"("xmlns:wsse": "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
                                    "xmlns:wsu": "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
                                    "wsu:Id": "STR-${uniques.referenceId}") {
                                "wsse:Reference"(URI: "#${tokenId}",
                                        ValueType: "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509v3")
                            }
                        }
                    }
                }
            }
        }

        log.debug "<== makeHeader"
        return retVal

    }

    static makeBody(trzba, config, id, date) {
        log.debug "==> makeBody id={}", id

        def uuid = UUID.randomUUID()
        def dataMap = [celk_trzba: trzba.celk_trzba, dat_trzby: trzba.dat_trzby, dic_popl: trzba.dic_popl,
                       id_pokl   : trzba.id_pokl, id_provoz: trzba.id_provoz, porad_cis: trzba.porad_cis, rezim: trzba.rezim]

        def pkpValText = EetUtil.makePkp(trzba, config)
        def pkpVal = EetUtil.toBase64(pkpValText)
        def bkpVal = EetUtil.makeBkp(pkpValText)

        def retVal = {
            "soap:Body"("xmlns:soap": "http://schemas.xmlsoap.org/soap/envelope/", "xmlns:wsu": "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
                    "wsu:Id": "${id}", "xml:id": "${id}") {
                Trzba(xmlns: "http://fs.mfcr.cz/eet/schema/v2") {
                    Hlavicka(dat_odesl: date, overeni: config.overeni, prvni_zaslani: config.prvni_zaslani, uuid_zpravy: uuid)
                    Data(dataMap)
                    KontrolniKody {
                        pkp(cipher: "RSA2048", digest: "SHA256", encoding: "base64", pkpVal)
                        bkp(digest: "SHA1", encoding: "base16", bkpVal)
                    }
                }
            }
        }

        log.debug "<== makeBody"
        return retVal
    }

    static makeMsg(trzba, config, uniques) {
        log.debug "==> makeMsg"
        def id = "${uniques.bodyId}"
        def builder = new StreamingMarkupBuilder()
        builder.useDoubleQuotes = true
        builder.expandEmptyElements = true

        def final bodyClosure = makeBody(trzba, config, id, EetUtil.getDateUtc())
        def body = builder.bind {
            out << bodyClosure
        }

        def retVal = builder.bind {
            "soap:Envelope"("xmlns:soap": "http://schemas.xmlsoap.org/soap/envelope/") {
                out << makeHeader(config, id, body.toString(), uniques)
                out << bodyClosure
            }
        }

        log.debug(indentXml(retVal, 4))
        log.debug "<== makeMsg {}", retVal
        return retVal
    }

    static processResponse(response) {
        log.debug "==> processResponse {}", response
        def ret
        def envelope = new XmlParser().parseText(response)
        def potvrzeni = envelope.'**'.find {node -> node.@fik} //find node with 'fik' attribute
        ret =  potvrzeni.@fik
        log.debug "<== processResponse ret {}", ret
        return ret
    }
}
