package net.dongliu.apk.parser.parser

import net.dongliu.apk.parser.utils.Inputs.readAllAndClose
import kotlin.test.*
import java.io.IOException
import java.security.cert.CertificateException
import java.util.*


class BCCertificateParserTest {
    @Test
    @Throws(IOException::class, CertificateException::class)
    fun parse() {
        var data = readAllAndClose(Objects.requireNonNull(javaClass.getResourceAsStream("/sign/63_CERT.RSA")))
        var parser: CertificateParser = BCCertificateParser(data)
        var certificateMetas = parser.parse()
        assertEquals("SHA1WITHRSA", certificateMetas[0].signAlgorithm)
        data = readAllAndClose(Objects.requireNonNull(javaClass.getResourceAsStream("/sign/gmail_CERT.RSA")))
        parser = BCCertificateParser(data)
        certificateMetas = parser.parse()
        assertEquals(1, certificateMetas.size)
        val certificateMeta = certificateMetas[0]
        assertEquals("MD5WITHRSA", certificateMeta.signAlgorithm)
        assertEquals("9decc0608f773ad1f4a017c02598d80c", certificateMeta.certBase64Md5)
    }
}