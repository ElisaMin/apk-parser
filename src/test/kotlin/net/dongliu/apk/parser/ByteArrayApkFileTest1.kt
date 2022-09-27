package net.dongliu.apk.parser

import net.dongliu.apk.parser.bean.ApkSigner
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.security.cert.CertificateException
import java.util.*
import kotlin.test.*

class ByteArrayApkFileTest {
    private fun getPath(path:String) = File(javaClass.classLoader.getResource(path)!!.file).toPath().toAbsolutePath().toUri()
    @Test
    fun testParserMeta() {
        val path = getPath("apks/Twitter_v7.93.2.apk")
        val bytes = Files.readAllBytes(Paths.get(path))
        ByteArrayApkFile(bytes).use { apkFile ->
            apkFile.preferredLocale = Locale.ENGLISH
            val (_, label) = apkFile.apkMeta
            assertEquals("Twitter", label)
        }
    }

    @Test
    @Throws(IOException::class, CertificateException::class)
    fun testGetSignature() {
        val path = getPath("apks/Twitter_v7.93.2.apk")
        val bytes = Files.readAllBytes(Paths.get(path))
        ByteArrayApkFile(bytes).use { apkFile ->
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
}