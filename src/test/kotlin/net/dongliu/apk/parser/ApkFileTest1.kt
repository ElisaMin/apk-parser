package net.dongliu.apk.parser

import net.dongliu.apk.parser.bean.ApkSigner
import java.io.File
import java.io.IOException
import java.security.cert.CertificateException
import java.util.*
import kotlin.test.*

class ApkFileTest {
    @Test
    @Throws(IOException::class)
    fun testParserMeta() {
        val path = Objects.requireNonNull(javaClass.classLoader.getResource("apks/Twitter_v7.93.2.apk")).path
        assert(File(path).exists())
        ApkFile(path).use { apkFile ->
            apkFile.close()
            apkFile.preferredLocale = Locale.ENGLISH
            assert("Twitter" == apkFile.apkMeta.label)
        }
    }

    @Test
    @Throws(IOException::class)
    fun testParserMeta_Type_0204() {
        val path = Objects.requireNonNull(javaClass.classLoader.getResource("apks/NetworkStack_210000000.apk")).path
        ApkFile(path).use { apkFile ->
            apkFile.preferredLocale = Locale.ENGLISH
            val (_, label) = apkFile.apkMeta
            assert("NetworkStack" == label)
        }
    }

    @Test
    @Throws(IOException::class, CertificateException::class)
    fun testGetSignature() {
        val path = javaClass.classLoader.getResource("apks/Twitter_v7.93.2.apk")!!.path
        ApkFile(path).use { apkFile ->
            val apkSingers: List<ApkSigner> = apkFile.apkSingers
            assertEquals(1, apkSingers.size)
            val apkSigner = apkSingers[0]
            assertEquals("META-INF/CERT.RSA", apkSigner.path)
            val certificateMetas = apkSigner.certificateMetas
            assertEquals(1, certificateMetas.size)
            val certificateMeta = certificateMetas[0]
            assertEquals("69ee076cc84f4d94802d61907b07525f", certificateMeta.certMd5)
        }
    }

    @Test
    @Throws(IOException::class)
    fun testAppIsNotDebuggable() {
        val path = Objects.requireNonNull(javaClass.classLoader.getResource("apks/app-release.apk")).path
        ApkFile(path).use { apkFile ->
            val isDebuggable = apkFile.apkMeta.isDebuggable
            assertFalse(isDebuggable)
        }
    }

    @Test
    @Throws(IOException::class)
    fun testAppIsDebuggable() {
        val path = Objects.requireNonNull(javaClass.classLoader.getResource("apks/app-debug.apk")).path
        ApkFile(path).use { apkFile ->
            val isDebuggable = apkFile.apkMeta.isDebuggable
            assertTrue(isDebuggable)
        }
    }
}