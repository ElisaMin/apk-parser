package net.dongliu.apk.parser

import net.dongliu.apk.parser.bean.ApkMeta
import java.io.File
import java.io.IOException
import java.util.*

/**
 * Convenient utils method for parse apk file
 *
 * @author Liu Dong
 */
object ApkParsers {
    private var useBouncyCastle = false
    fun useBouncyCastle(): Boolean {
        return useBouncyCastle
    }

    /**
     * Use BouncyCastle instead of JSSE to parse X509 certificate.
     * If want to use BouncyCastle, you will also need to add bcprov and bcpkix lib to your project.
     */
    fun useBouncyCastle(useBouncyCastle: Boolean) {
        ApkParsers.useBouncyCastle = useBouncyCastle
    }

    /**
     * Get apk meta info for apk file
     */
    @Throws(IOException::class)
    fun getMetaInfo(apkFilePath: String): ApkMeta? {
        ApkFile(apkFilePath).use { apkFile -> return apkFile.apkMeta }
    }

    /**
     * Get apk meta info for apk file
     */
    @Throws(IOException::class)
    fun getMetaInfo(file: File): ApkMeta? {
        ApkFile(file).use { apkFile -> return apkFile.apkMeta }
    }

    /**
     * Get apk meta info for apk file
     */
    @Throws(IOException::class)
    fun getMetaInfo(apkData: ByteArray?): ApkMeta? {
        ByteArrayApkFile(apkData).use { apkFile -> return apkFile.apkMeta }
    }

    /**
     * Get apk meta info for apk file, with locale
     */
    @Throws(IOException::class)
    fun getMetaInfo(apkFilePath: String, locale: Locale?): ApkMeta? {
        ApkFile(apkFilePath).use { apkFile ->
            apkFile.preferredLocale = locale!!
            return apkFile.apkMeta
        }
    }

    /**
     * Get apk meta info for apk file
     */
    @Throws(IOException::class)
    fun getMetaInfo(file: File, locale: Locale?): ApkMeta? {
        ApkFile(file).use { apkFile ->
            apkFile.preferredLocale = locale!!
            return apkFile.apkMeta
        }
    }

    /**
     * Get apk meta info for apk file
     */
    @Throws(IOException::class)
    fun getMetaInfo(apkData: ByteArray?, locale: Locale?): ApkMeta? {
        ByteArrayApkFile(apkData).use { apkFile ->
            apkFile.preferredLocale = locale!!
            return apkFile.apkMeta
        }
    }

    /**
     * Get apk manifest xml file as text
     */
    @Throws(IOException::class)
    fun getManifestXml(apkFilePath: String): String? {
        ApkFile(apkFilePath).use { apkFile -> return apkFile.manifestXml }
    }

    /**
     * Get apk manifest xml file as text
     */
    @Throws(IOException::class)
    fun getManifestXml(file: File): String? {
        ApkFile(file).use { apkFile -> return apkFile.manifestXml }
    }

    /**
     * Get apk manifest xml file as text
     */
    @Throws(IOException::class)
    fun getManifestXml(apkData: ByteArray?): String? {
        ByteArrayApkFile(apkData).use { apkFile -> return apkFile.manifestXml }
    }

    /**
     * Get apk manifest xml file as text
     */
    @Throws(IOException::class)
    fun getManifestXml(apkFilePath: String, locale: Locale?): String? {
        ApkFile(apkFilePath).use { apkFile ->
            apkFile.preferredLocale = locale!!
            return apkFile.manifestXml
        }
    }

    /**
     * Get apk manifest xml file as text
     */
    @Throws(IOException::class)
    fun getManifestXml(file: File, locale: Locale?): String? {
        ApkFile(file).use { apkFile ->
            apkFile.preferredLocale = locale!!
            return apkFile.manifestXml
        }
    }

    /**
     * Get apk manifest xml file as text
     */
    @Throws(IOException::class)
    fun getManifestXml(apkData: ByteArray?, locale: Locale?): String? {
        ByteArrayApkFile(apkData).use { apkFile ->
            apkFile.preferredLocale = locale!!
            return apkFile.manifestXml
        }
    }
}