package net.dongliu.apk.parser

import net.dongliu.apk.parser.bean.*
import net.dongliu.apk.parser.exception.ParserException
import net.dongliu.apk.parser.parser.*
import net.dongliu.apk.parser.parser.CertificateMetas.from
import net.dongliu.apk.parser.struct.AndroidConstants
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

/**
 * Common Apk Parser methods.
 * This Class is not thread-safe.
 *
 * @author Liu Dong
 */
abstract class AbstractApkFile : Closeable {
    private var dexClasses: Array<DexClass?> = emptyArray()
    private var resourceTableParsed = false
    private var resourceTable: ResourceTable? = null
    private var locales: Set<Locale?>? = null
    private var manifestParsed = false
    val manifestXml: String
        get() = xmlTranslator.xml
    val apkMeta get() = apkTranslator.apkMeta
    val iconPaths: List<IconPath?>
        get() = apkTranslator.iconPaths
    val apkSigners by lazy {
        ArrayList<ApkSigner>().apply {
            for (file in allCertificateData) {
                val certificateMetas = CertificateParser
                    .getInstance(file.data)
                    .parse()
                add(ApkSigner(file.path, certificateMetas))
            }
        }
    }

    private var apkV2Signers: List<ApkV2Signer>? = null

    /**
     * default use empty locale
     */
    var preferredLocale: Locale = DEFAULT_LOCALE
        set(value) {
            if (preferredLocale != value) {
                field = value

                manifestParsed = false
            } else this
        }

//
//    /**
//     * get locales supported from resource file
//     *
//     * @return decoded AndroidManifest.xml
//     */
//    @Throws(IOException::class)
//    fun getLocales(): Set<Locale?>? {
//        parseResourceTable()
//        return locales
//    }
//
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

    @get:Throws(IOException::class)
    protected abstract val allCertificateData: List<CertificateFile>

    protected class CertificateFile(val path: String, val data: ByteArray)


    private var xmlTranslatorTemp = XmlTranslator()
    private var xmlTranslator =
        if (manifestParsed) xmlTranslatorTemp else
            XmlTranslator().apply {
                parseResourceTable()
                apkTranslator = ApkMetaTranslator(resourceTable!!, preferredLocale)
                val xmlStreamer: XmlStreamer = CompositeXmlStreamer(this, apkTranslator)
                val data = getFileData(AndroidConstants.MANIFEST_FILE)
                    ?: throw ParserException("Manifest file not found")
                transBinaryXml(data, xmlStreamer)
                manifestParsed = true
            }

    lateinit var apkTranslator:ApkMetaTranslator
        @Throws(IOException::class)
    private fun parseManifest() {
        if (manifestParsed) {
            return
        }
        parseResourceTable()
        xmlTranslator = XmlTranslator()
        apkTranslator = ApkMetaTranslator(resourceTable!!, preferredLocale)
        val xmlStreamer: XmlStreamer = CompositeXmlStreamer(xmlTranslator, apkTranslator)
        val data = getFileData(AndroidConstants.MANIFEST_FILE)
            ?: throw ParserException("Manifest file not found")
        this.transBinaryXml(data, xmlStreamer)
        manifestParsed = true
    }

    /**
     * read file in apk into bytes
     */
    @Throws(IOException::class)
    abstract fun getFileData(path: String): ByteArray?

    /**
     * return the whole apk file as ByteBuffer
     */
    @Throws(IOException::class)
    protected abstract fun fileData(): ByteBuffer

    /**
     * trans binary xml file to text xml file.
     *
     * @param path the xml file path in apk file
     * @return the text. null if file not exists
     */
    @Throws(IOException::class)
    fun transBinaryXml(path: String): String? {
        val data = getFileData(path) ?: return null
        parseResourceTable()
        val xmlTranslator = XmlTranslator()
        this.transBinaryXml(data, xmlTranslator)
        return xmlTranslator.xml
    }

    @Throws(IOException::class)
    private fun transBinaryXml(data: ByteArray, xmlStreamer: XmlStreamer) {
        parseResourceTable()
        val buffer = ByteBuffer.wrap(data)
        val binaryXmlParser = BinaryXmlParser(buffer, resourceTable!!)
        binaryXmlParser.setLocale(preferredLocale)
        binaryXmlParser.xmlStreamer = xmlStreamer
        binaryXmlParser.parse()
    }// adaptive icon?

    /**
     * This method return icons specified in android manifest file, application.
     * The icons could be file icon, color icon, or adaptive icon, etc.
     *
     * @return icon files.
     */
    @get:Throws(IOException::class)
    val allIcons: List<IconFace>
        get() {
            parseManifest()
            val iconPaths = iconPaths
            if (iconPaths!!.isEmpty()) {
                return emptyList()
            }
            val iconFaces: MutableList<IconFace> = ArrayList(iconPaths.size)
            for (iconPath in iconPaths) {
                val filePath = iconPath!!.path
                if (filePath != null && filePath.endsWith(".xml")) {
                    // adaptive icon?
                    val data = getFileData(filePath) ?: continue
                    parseResourceTable()
                    val iconParser = AdaptiveIconParser()
                    this.transBinaryXml(data, iconParser)
                    var backgroundIcon: Icon? = null
                    val background = iconParser.background
                    if (background != null) {
                        backgroundIcon = newFileIcon(background, iconPath.density)
                    }
                    var foregroundIcon: Icon? = null
                    val foreground = iconParser.foreground
                    if (foreground != null) {
                        foregroundIcon = newFileIcon(foreground, iconPath.density)
                    }
                    val icon = AdaptiveIcon(foregroundIcon, backgroundIcon)
                    iconFaces.add(icon)
                } else {
                    val icon = newFileIcon(filePath!!, iconPath.density)
                    iconFaces.add(icon)
                }
            }
            return iconFaces
        }

    @Throws(IOException::class)
    private fun newFileIcon(filePath: String, density: Int): Icon {
        return Icon(filePath, density, getFileData(filePath))
    }

    /**
     * get class infos form dex file. currently only class name
     */
    @Throws(IOException::class)
    fun getDexClasses(): Array<DexClass?> {
        if (dexClasses == null) {
            parseDexFiles()
        }
        return dexClasses
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

    @Throws(IOException::class)
    private fun parseDexFiles() {
        dexClasses = parseDexFile(AndroidConstants.DEX_FILE)
        for (i in 2..999) {
            val path = String.format(Locale.ROOT, AndroidConstants.DEX_ADDITIONAL, i)
            try {
                val classes = parseDexFile(path)
                dexClasses = mergeDexClasses(dexClasses, classes)
            } catch (e: ParserException) {
                break
            }
        }
    }

    /**
     * parse resource table.
     */
    @Throws(IOException::class)
    private fun parseResourceTable() {
        if (resourceTableParsed) {
            return
        }
        resourceTableParsed = true
        val data = getFileData(AndroidConstants.RESOURCE_FILE)
        if (data == null) {
            // if no resource entry has been found, we assume it is not needed by this APK
            resourceTable = ResourceTable(null)
            locales = emptySet<Locale>()
            return
        }
        val buffer = ByteBuffer.wrap(data)
        val resourceTableParser = ResourceTableParser(buffer)
        resourceTableParser.parse()
        resourceTable = resourceTableParser.resourceTable
        locales = resourceTableParser.locales
    }

    /**
     * Check apk sign. This method only use apk v1 scheme verifier
     *
     */
    @Deprecated("using google official ApkVerifier of apksig lib instead.")
    @Throws(IOException::class)
    abstract fun verifyApk(): ApkSignStatus
    @Throws(IOException::class)
    override fun close() {
        apkSigners.clear()
        resourceTable = null
    }



    /**
     * Create ApkSignBlockParser for this apk file.
     *
     * @return null if do not have sign block
     */
    @Throws(IOException::class)
    protected fun findApkSignBlock(): ByteBuffer? {
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
            if (v == EOCD.Companion.SIGNATURE) {
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
        if (magic != ApkSigningBlock.Companion.MAGIC) {
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
        private val DEFAULT_LOCALE = Locale.US
    }
}