package net.dongliu.apk.parser.parser

import net.dongliu.apk.parser.ApkParsers
import net.dongliu.apk.parser.bean.CertificateMeta
import java.security.cert.CertificateException

/**
 * Parser certificate info.
 * One apk may have multi certificates(certificate chain).
 *
 * @author dongliu
 */
abstract class CertificateParser(protected val data: ByteArray) {
    /**
     * get certificate info
     */
    @Throws(CertificateException::class)
    abstract fun parse(): List<CertificateMeta>

    companion object {
        fun getInstance(data: ByteArray): CertificateParser {
            return if (ApkParsers.useBouncyCastle()) {
                BCCertificateParser(data)
            } else JSSECertificateParser(data)
        }
    }
}