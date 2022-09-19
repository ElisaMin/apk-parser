package net.dongliu.apk.parser

import net.dongliu.apk.parser.bean.ApkSignStatus
import net.dongliu.apk.parser.utils.Inputs.readAll
import java.io.ByteArrayInputStream
import java.io.Closeable
import java.io.IOException
import java.nio.ByteBuffer
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * Parse apk file from byte array.
 * This class is not thread-safe
 *
 * @author Liu Dong
 */
class ByteArrayApkFile(private var apkData: ByteArray?) : AbstractApkFile(), Closeable {
    @get:Throws(IOException::class)
    protected override val allCertificateData: List<CertificateFile>
        protected get() {
            val list: MutableList<CertificateFile> = ArrayList()
            ByteArrayInputStream(apkData).use { `in` ->
                ZipInputStream(`in`).use { zis ->
                    var entry: ZipEntry
                    while (zis.nextEntry.also { entry = it } != null) {
                        val name = entry.name
                        if (name.uppercase(Locale.getDefault()).endsWith(".RSA") || name.uppercase(Locale.getDefault())
                                .endsWith(".DSA")
                        ) {
                            list.add(CertificateFile(name, readAll(zis)))
                        }
                    }
                }
            }
            return list
        }

    @Throws(IOException::class)
    override fun getFileData(path: String): ByteArray? {
        ByteArrayInputStream(apkData).use { `in` ->
            ZipInputStream(`in`).use { zis ->
                var entry: ZipEntry
                while (zis.nextEntry.also { entry = it } != null) {
                    if (path == entry.name) {
                        return readAll(zis)
                    }
                }
            }
        }
        return null
    }

    override fun fileData(): ByteBuffer {
        return ByteBuffer.wrap(apkData).asReadOnlyBuffer()
    }

    @Deprecated("")
    override fun verifyApk(): ApkSignStatus {
        throw UnsupportedOperationException()
    }

    @Throws(IOException::class)
    override fun close() {
        super.close()
        apkData = null
    }
}