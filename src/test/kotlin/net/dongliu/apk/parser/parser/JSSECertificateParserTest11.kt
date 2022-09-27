package net.dongliu.apk.parser.parser

import net.dongliu.apk.parser.utils.Inputs.readAllAndClose
import kotlin.test.*
import java.io.IOException
import java.security.cert.CertificateException
import java.util.*

class JSSECertificateParserTest {
    @Test
    @Ignore // issue 63
    @Throws(IOException::class, CertificateException::class)
    fun parseJDKFailed() {
        val data = readAllAndClose(Objects.requireNonNull(javaClass.getResourceAsStream("/sign/63_CERT.RSA")))
        val parser: CertificateParser = JSSECertificateParser(data)
        val certificateMetas = parser.parse()
        assertEquals("SHA1WITHRSA", certificateMetas[0].signAlgorithm)
    }

    @Test
    @Throws(IOException::class, CertificateException::class)
    fun parseJDK() {
        val data = readAllAndClose(Objects.requireNonNull(javaClass.getResourceAsStream("/sign/gmail_CERT.RSA")))
        val parser: CertificateParser = JSSECertificateParser(data)
        val certificateMetas = parser.parse()
        assertEquals(1, certificateMetas.size.toLong())
        val certificateMeta = certificateMetas[0]
        assertEquals("MD5WITHRSA", certificateMeta.signAlgorithm)
        assertEquals("9decc0608f773ad1f4a017c02598d80c", certificateMeta.certBase64Md5)
    }
}