/* (c) Michal Novák, it.novakmi@gmail.com, see LICENSE file */

import groovy.util.logging.Slf4j

import javax.xml.bind.DatatypeConverter
import java.security.KeyStore
import java.security.Signature
import java.security.cert.X509Certificate

@Slf4j
class EetUtil {

    static def getUnique() {
        log.trace  "==> getUnique"

        def ret = UUID.randomUUID()

        log.trace "<== getUnique {}", ret
        return ret
    }

    /**
     * https://gist.github.com/kdabir/6bfe265d2f3c2f9b438b
     * @return
     */
    static def getDateUtc() {
        log.trace "==> getDateUtc"

        def ret = new Date().format("yyyy-MM-dd'T'HH:mm:ss'Z'", TimeZone.getTimeZone("UTC"))

        log.trace "<== getDateUtc ret {}", ret
        return ret
    }

    /**
     * http://stackoverflow.com/questions/19743851/base64-java-encode-and-decode-a-string
     * (In java 1.8 swe can also use
     *    Base64.getEncoder().withoutPadding().encodeToString(someByteArray);
     * )
     * @param bytes
     * @return
     */
    static def toBase64(bytes) {
        log.trace "==> toBase64"

        def ret = new String(DatatypeConverter.printBase64Binary(bytes));

        log.trace "<== toBase64 {}", ret
        return ret
    }

    static def makeKeyMap(config) {
        log.trace "==> makeKeyMap"

        final KeyStore keystore = KeyStore.getInstance("pkcs12");
        keystore.load(new FileInputStream(config.cert_popl), config.cert_pass.toCharArray())
        def al
        def aliases = keystore.aliases()
        if (aliases.hasMoreElements()) {
            al = aliases.nextElement();
            log.trace "Client alias {}", al
        } else {
            def ex = new Exception("Certificate {} alias not found!", config.cert_popl)
            log.error ex
        }
        def keyMap =  [keystore: keystore, alias: al]

        log.trace "<== makeKeyMap {}", keyMap
        return keyMap
    }

    static def makeSecToken(keyMap) {
        log.trace  "==> makeSecToken"

        X509Certificate certificate = (X509Certificate) keyMap.keystore.getCertificate(keyMap.alias);
        String token = toBase64(certificate.getEncoded())

        log.trace  "<== makeSecToken {}", token
        return token
    }

    static def makeDigestValue(body) {
        log.trace "==> makeDigestValue {}", body

        final java.security.MessageDigest d = java.security.MessageDigest.getInstance("SHA-256")
        d.reset();
        d.update(body.getBytes("UTF-8"))
        final byte[] bytes = d.digest()
        log.trace("bytes {}", bytes)
        def ret = toBase64(bytes)

        log.trace "<== makeDigestValue {}", ret
        return ret
    }

    static def makeSignatureValue(config, keyMap, signedInfo) {
        log.trace  "==> makeSignatureValue {}", signedInfo

        final Signature signature = Signature.getInstance("SHA256withRSA")
        signature.initSign(keyMap.keystore.getKey(keyMap.alias, config.cert_pass.toCharArray()))
        signature.update(signedInfo.getBytes("UTF-8"));
        def ret = toBase64(signature.sign())

        log.trace "<== makeSignatureValue {}", ret
        return ret
    }

    static def makeBkp(pkpValText) {
        log.trace "==> makeBkp"

        final java.security.MessageDigest d = java.security.MessageDigest.getInstance("SHA-1")
        d.reset();
        d.update(pkpValText)
        final byte[] bytes = d.digest()
        log.trace("bytes {}", bytes)
        def hex = DatatypeConverter.printHexBinary(bytes)
        log.trace("hex {}", hex)
        def ret = "${hex[0..7]}-${hex[8..15]}-${hex[16..23]}-${hex[24..31]}-${hex[32..-1]}"

        log.trace "<== makeBkp ret {}", ret
        return ret
    }

    static def makePkp(trzba, config) {
        log.trace "==> makePkp"

        def ret
        def pkpPlain = "$trzba.dic_popl|$trzba.id_provoz|$trzba.id_pokl|$trzba.porad_cis|$trzba.dat_trzby|$trzba.celk_trzba"
        log.trace "pkpPlain {}", pkpPlain

        log.trace "config.cert_popl {}", config.cert_popl
        log.trace "config.cert_pass {}", config.cert_pass //comment :-)

        final def keyMap = makeKeyMap(config)
        final Signature signature = Signature.getInstance("SHA256withRSA")
        signature.initSign(keyMap.keystore.getKey(keyMap.alias, config.cert_pass.toCharArray()))
        signature.update(pkpPlain.getBytes("UTF-8"));
        ret = signature.sign()

        log.trace "<== makePkp {}", ret
        return ret
    }

}