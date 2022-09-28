@file:Suppress("NAME_SHADOWING")

package net.dongliu.apk.parser

import net.dongliu.apk.parser.bean.*
import net.dongliu.apk.parser.bean.ApkIcon.Raster
import net.dongliu.apk.parser.bean.ApkIcon.Adaptive
import net.dongliu.apk.parser.exception.ParserException
import net.dongliu.apk.parser.parser.*
import net.dongliu.apk.parser.parser.CertificateMetas.from
import net.dongliu.apk.parser.struct.AndroidConstants
import net.dongliu.apk.parser.struct.resource.Densities
import net.dongliu.apk.parser.struct.resource.ResourceTable
import net.dongliu.apk.parser.struct.signingv2.ApkSigningBlock
import net.dongliu.apk.parser.struct.zip.EOCD
import net.dongliu.apk.parser.utils.*
import java.io.Closeable
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.cert.CertificateException
import java.util.*
import kotlin.collections.ArrayList


internal typealias CertificateFile = Pair<String,ByteArray>

internal inline val CertificateFile.path get() = first

internal inline val CertificateFile.data get() = second


/**
 * Common Apk Parser methods.
 * This Class is not thread-safe.
 *
 * @author Liu Dong
 */
abstract class AbstractApkFile:Closeable {

    /**
     * All certificate data
     */

    protected abstract val allCertificateData: List<CertificateFile>

    /**
     * read file in apk into bytes
     */
    abstract fun getFileData(path: String): ByteArray?

    /**
     * find data
     */
    abstract fun fileData(): ByteBuffer

    /**
     * xml translator
     *
     * set null to mark getter not ready
     */
    private var xmlTranslator: XmlTranslator? = null
        get() = field ?: genXmlTranslator().also {
            field = it
        }
        set(value) {
            require(value == null) {
                IllegalArgumentException("null only")
            }
            apkTranslator = null
        }
    private inline val xmlOrrThrows get() = xmlTranslator?:throw IOException("xml is null")

    /**
     * Resource table
     *
     * set null to mark getter not ready
     */
    private var resourceTable: ResourceTable? = null
        get() {
            if (field != null) {
                return field
            }
            locales = emptySet()
            field = getFileData(AndroidConstants.RESOURCE_FILE)?.let {
                ResourceTableParser(ByteBuffer.wrap(it))
            }?.let {
                it.parse()
                locales = it.locales
                it.resourceTable
            }
            return field?:ResourceTable(null)
        }

    /**
     * apk translator
     *
     * set null to mark getter not ready
     */
    private var apkTranslator:ApkMetaTranslator? = null
        get() {
            if (field == null) xmlTranslator
            return field
        }

    /**
     * Icon paths
     */
    private inline val iconPaths: List<IconPath>
        get() = apkTranslator!!.iconPaths

    /**
     * returns decoded manifest as String from xml
     */
    val manifestXml: String get()  = xmlOrrThrows.xml

    var locales: Set<Locale> = emptySet()
        private set
    var preferredLocale: Locale = Locale.getDefault()
        set(value) {
            if (preferredLocale != value) {
                field = value
                xmlTranslator = null
            }
        }

    /**
     * returns parsed apk meta info
     */
    val apkMeta: ApkMeta get() {
        xmlTranslator
        return apkTranslator!!.apkMeta
    }


    /**
     * This method return icons specified in android manifest file, application.
     * The icons could be file icon, color icon, or adaptive icon, etc.
     *
     * @return icon files.
     */
    val icons: List<ApkIcon<*>> by lazy {
        xmlTranslator
        iconPaths.map { path ->
            packagingIcon(path.path, path.density)
        }
    }




    /**
     * return a parsed IconFace from [filePath]
     */
    private fun packagingIcon(
        filePath: String?,density: Int,data: ByteArray? = null,
    ):ApkIcon<*> {
        if (density == Densities.Dynamic && filePath?.first() == '#' )
            return ApkIcon.Color(filePath)
        if (filePath.isNullOrEmpty()) return ApkIcon.empty
        val data = data ?: getFileData(filePath)
            ?: return ApkIcon.Empty(filePath)
        resourceTable
        val xml:String? = filePath.takeIf { it.endsWith("xml") }
            ?.let(::transBinaryXml)
        val iconParser = xml?.let {
            IconParser().apply { transBinaryXml(data,this) }
        }
        return when {
            iconParser?.isAdaptive == true -> {
                Adaptive(
                    path = filePath,
                    data = packagingIcon(iconParser.foreground,-2,) to packagingIcon(iconParser.background,-2)
                )
            }
            iconParser!=null -> {
                transBinaryXml(filePath)?.let { data -> ApkIcon.Vector(filePath,data) }
                    ?: ApkIcon.empty
            }
            else -> {
                Raster(filePath,density,data)
            }
        }
    }
    /**
     * return a apk meta xml translator for update [xmlTranslator]
     */
    private fun genXmlTranslator():XmlTranslator {
        val translator = XmlTranslator()
        apkTranslator = ApkMetaTranslator(resourceTable!!, preferredLocale)
        transBinaryXml(
            getFileData(AndroidConstants.MANIFEST_FILE) ?: throw ParserException("Manifest file not found"),
            CompositeXmlStreamer(translator, apkTranslator!!)
        )
        return translator
    }
    override fun close() {
        kotlin.runCatching {
            resourceTable = null
            xmlTranslator = null
        }
    }

    val apkSingers by lazy {
        ArrayList<ApkSigner>().apply {
            for (file in allCertificateData) {
                val certificateMetas = CertificateParser
                    .getInstance(file.data)
                    .parse()
                println(certificateMetas.size)
                add(ApkSigner(file.path, certificateMetas))
            }
        }
    }

    /**
     * trans binary xml file to text xml file.
     *
     * @param path the xml file path in apk file
     * @return the text. null if file not exists
     */
    @Throws(IOException::class)
    fun transBinaryXml(path: String): String? {
        val data = getFileData(path) ?: return null
        resourceTable
        val xmlTranslator = XmlTranslator()
        this.transBinaryXml(data, xmlTranslator)
        return xmlTranslator.xml
    }
    private fun transBinaryXml(data: ByteArray, xmlStreamer: XmlStreamer) {
        resourceTable
        val buffer = ByteBuffer.wrap(data)
        BinaryXmlParser(buffer, resourceTable!!).apply {
            this.xmlStreamer = xmlStreamer
            this.locale = preferredLocale
        }.parse()
    }
    /**
     * Create ApkSignBlockParser for this apk file.
     *
     * @return null if do not have sign block
     */
    private fun findApkSignBlock(): ByteBuffer? {
        val buffer = fileData().order(ByteOrder.LITTLE_ENDIAN)
        val len = buffer.limit()
        // first find zip end of central directory entry
        if (len < 22) {
            // should not happen
            throw RuntimeException("Not zip file")
        }
        val maxEOCDSize = 1024 * 100
        var eocd: EOCD? = null
        for (i in len - 22 downTo 0.coerceAtLeast(len - maxEOCDSize) + 1) {
            val v = buffer.getInt(i)
            if (v == EOCD.SIGNATURE) {
                Buffers.position(buffer, i + 4)
                eocd = EOCD()
                eocd.setDiskNum(Buffers.readUShort(buffer))
                eocd.cdStartDisk = Buffers.readUShort(buffer).toShort()
                eocd.cdRecordNum = Buffers.readUShort(buffer).toShort()
                eocd.totalCDRecordNum = Buffers.readUShort(buffer).toShort()
                eocd.cdSize = Buffers.readUInt(buffer).toInt()
                eocd.cdStart = Buffers.readUInt(buffer).toInt()
                eocd.commentLen = Buffers.readUShort(buffer).toShort()
            }
        }
        if (eocd == null) {
            return null
        }
        val magicStrLen = 16
        val cdStart = eocd.cdStart
        // find apk sign block
        Buffers.position(buffer, cdStart - magicStrLen)
        val magic = Buffers.readAsciiString(buffer, magicStrLen)
        if (magic != ApkSigningBlock.MAGIC) {
            return null
        }
        Buffers.position(buffer, cdStart - 24)
        val blockSize = Unsigned.ensureUInt(buffer.long)
        Buffers.position(buffer, cdStart - blockSize - 8)
        val size2 = Unsigned.ensureULong(buffer.long)
        return if (blockSize.toLong() != size2) {
            null
        } else Buffers.sliceAndSkip(buffer, blockSize - magicStrLen)
        // now at the start of signing block
    }
    companion object {
    }
    private fun mergeDexClasses(first: Array<DexClass?>, second: Array<DexClass?>): Array<DexClass?> {
        val result = arrayOfNulls<DexClass>(first.size + second.size)
        System.arraycopy(first, 0, result, 0, first.size)
        System.arraycopy(second, 0, result, first.size, second.size)
        return result
    }

    @Throws(IOException::class)
    private fun parseDexFile(path: String): Array<DexClass?> {
        val data = getFileData(path)
        if (data == null) {
            val msg = String.format("Dex file %s not found", path)
            throw ParserException(msg)
        }
        val buffer = ByteBuffer.wrap(data)
        val dexParser = DexParser(buffer)
        return dexParser.parse()
    }
    var dexClasses: Array<DexClass?> = emptyArray()
        private set
        get() {
            field = parseDexFile(AndroidConstants.DEX_FILE)
            for (i in 2..999) {
                val path = String.format(Locale.ROOT, AndroidConstants.DEX_ADDITIONAL, i)
                try {
                    val classes = parseDexFile(path)
                    field = mergeDexClasses(field, classes)
                } catch (e: ParserException) {
                    break
                }
            }
            return field
        }
    private var apkV2Signers: List<ApkV2Signer>? = null
    /**
     * Get the apk's all signer in apk sign block, using apk singing v2 scheme.
     * If apk v2 signing block not exists, return empty list.
     */
    @get:Throws(IOException::class, CertificateException::class)
    val apkV2Singers: List<ApkV2Signer>?
        get() {
            if (apkV2Signers == null) {
                parseApkSigningBlock()
            }
            return apkV2Signers
        }

    @Throws(IOException::class, CertificateException::class)
    private fun parseApkSigningBlock() {
        val list: MutableList<ApkV2Signer> = ArrayList()
        val apkSignBlockBuf = findApkSignBlock()
        if (apkSignBlockBuf != null) {
            val parser = ApkSignBlockParser(apkSignBlockBuf)
            val apkSigningBlock = parser.parse()
            for (signerBlock in apkSigningBlock.signerBlocks) {
                val certificates = signerBlock.certificates
                val certificateMetas = from(certificates)
                val apkV2Signer = ApkV2Signer(certificateMetas)
                list.add(apkV2Signer)
            }
        }
        apkV2Signers = list
    }
}