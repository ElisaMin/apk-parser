package net.dongliu.apk.parser

import java.io.IOException
import java.security.cert.CertificateException
import java.util.*

/**
 * Main method for parser apk
 *
 * @author Liu Dong &lt;dongliu@live.cn&gt;
 */
object Main {
    @Throws(IOException::class, CertificateException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val action = args[0]
        val apkPath = args[1]
        ApkFile(apkPath).use { apkFile ->
            apkFile.preferredLocale = Locale.getDefault()
            when (action) {
                "meta" -> println(apkFile.apkMeta)
                "manifest" -> println(apkFile.manifestXml)
                "signer" -> println(apkFile.apkSigners)
                else -> {}
            }
        }
    }
}