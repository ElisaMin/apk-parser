package net.dongliu.apk.parser

import net.dongliu.apk.parser.bean.ApkSignStatus
import net.dongliu.apk.parser.utils.Inputs.readAllAndClose
import java.io.Closeable
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.util.*
import java.util.jar.JarFile
import java.util.zip.ZipFile

/**
 * ApkFile, for parsing apk file info.
 * This class is not thread-safe.
 *
 * @author dongliu
 */
class ApkFile(private val apkFile: File) : AbstractApkFile(), Closeable {

    private val zf by lazy {
        ZipFile(apkFile)
//            .also { println(it.name) }
    }

    constructor(filePath: String) : this(File(filePath))

    @get:Throws(IOException::class)
    override val allCertificateData: List<CertificateFile>
        get() {
            val enu = zf.entries()
            val list: MutableList<CertificateFile> = ArrayList()
            while (enu.hasMoreElements()) {
                val ne = enu.nextElement()
                if (ne.isDirectory) {
                    continue
                }
                val name = ne.name.uppercase(Locale.getDefault())
                if (name.endsWith(".RSA") || name.endsWith(".DSA")) {
                    list.add(CertificateFile(name, readAllAndClose(zf.getInputStream(ne))))
                }
            }
            return list
        }

    @Throws(IOException::class)
    override fun getFileData(path: String): ByteArray? {
        val entry = zf.getEntry(path) ?: return null
        val inputStream = zf.getInputStream(entry)
        return readAllAndClose(inputStream)
    }

    @Throws(IOException::class)
    override fun fileData(): ByteBuffer {
        val channel = FileInputStream(apkFile).channel
        return channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size())
    }

    /**
     * {@inheritDoc}
     *
     */
    @Deprecated("using google official ApkVerifier of apksig lib instead.")
    @Throws(IOException::class)
    fun verifyApk(): ApkSignStatus {
        zf.getEntry("META-INF/MANIFEST.MF")
            ?: // apk is not signed;
            return ApkSignStatus.NotSigned
        JarFile(apkFile).use { jarFile ->
            val entries = jarFile.entries()
            val buffer = ByteArray(8192)
            while (entries.hasMoreElements()) {
                val e = entries.nextElement()
                if (e.isDirectory) {
                    continue
                }
                try {
                    jarFile.getInputStream(e).use { `in` ->
                        // Read in each jar entry. A security exception will be thrown if a signature/digest check fails.
                        while (`in`.read(buffer, 0, buffer.size) != -1) {
                            // Don't care
                        }
                    }
                } catch (se: SecurityException) {
                    return ApkSignStatus.Incorrect
                }
            }
        }
        return ApkSignStatus.Signed
    }


    override fun close() {
        try {
            Closeable { super@ApkFile.close() }.use { }
        } catch (e:Exception) {
            println(e)
        }
    }
}