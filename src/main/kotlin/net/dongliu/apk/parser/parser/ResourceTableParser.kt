package net.dongliu.apk.parser.parser

import net.dongliu.apk.parser.exception.ParserException
import net.dongliu.apk.parser.struct.*
import net.dongliu.apk.parser.struct.resource.*
import net.dongliu.apk.parser.utils.Buffers
import net.dongliu.apk.parser.utils.Pair
import net.dongliu.apk.parser.utils.ParseUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

/**
 * Parse android resource table file.
 *
 * @author dongliu
 * @see [ResourceTypes.h](https://github.com/aosp-mirror/platform_frameworks_base/blob/master/libs/androidfw/include/androidfw/ResourceTypes.h)
 *
 * @see [ResourceTypes.cpp](https://github.com/aosp-mirror/platform_frameworks_base/blob/master/libs/androidfw/ResourceTypes.cpp)
 */
class ResourceTableParser(buffer: ByteBuffer) {
    /**
     * By default the data buffer Chunks is buffer little-endian byte order both at runtime and when stored buffer files.
     */
    private val byteOrder = ByteOrder.LITTLE_ENDIAN
    private var stringPool: StringPool? = null
    private val buffer: ByteBuffer

    /**
     * the resource table file size
     */
    var resourceTable: ResourceTable? = null
    val locales: MutableSet<Locale>

    init {
        this.buffer = buffer.duplicate()
        this.buffer.order(byteOrder)
        locales = HashSet()
    }

    /**
     * parse resource table file.
     */
    fun parse() {
        // read resource file header.
        val resourceTableHeader = readChunkHeader() as ResourceTableHeader
        // read string pool chunk
        val stringPool = ParseUtils.readStringPool(buffer, readChunkHeader() as StringPoolHeader)
        this.stringPool = stringPool
        resourceTable = ResourceTable(stringPool)
        val packageCount = resourceTableHeader.packageCount
        if (packageCount != 0L) {
            var packageHeader = readChunkHeader() as PackageHeader
            for (i in 0 until packageCount) {
                val pair = readPackage(packageHeader)
                resourceTable!!.addPackage(pair.left)
                packageHeader = pair.right
            }
        }
    }

    /**
     * read one package
     */
    private fun readPackage(packageHeader: PackageHeader): Pair<ResourcePackage, PackageHeader> {
        val pair = Pair<ResourcePackage, PackageHeader>()
        //read packageHeader
        val resourcePackage = ResourcePackage(packageHeader)
        pair.left = resourcePackage
        val beginPos = buffer.position().toLong()
        // read type string pool
        if (packageHeader.typeStrings > 0) {
            Buffers.position(buffer, beginPos + packageHeader.typeStrings - packageHeader.headerSize.toInt())
            resourcePackage.typeStringPool = ParseUtils.readStringPool(
                buffer,
                readChunkHeader() as StringPoolHeader
            )
        }
        //read key string pool
        if (packageHeader.keyStrings > 0) {
            Buffers.position(buffer, beginPos + packageHeader.keyStrings - packageHeader.headerSize.toInt())
            resourcePackage.keyStringPool = ParseUtils.readStringPool(
                buffer,
                readChunkHeader() as StringPoolHeader
            )
        }
        outer@ while (buffer.hasRemaining()) {
            val chunkHeader = readChunkHeader()
            val chunkBegin = buffer.position().toLong()
            when (chunkHeader.chunkType.toInt()) {
                ChunkType.TABLE_TYPE_SPEC -> {
                    val typeSpecHeader = chunkHeader as TypeSpecHeader
                    val entryFlags = LongArray(typeSpecHeader.entryCount)
                    var i = 0
                    while (i < typeSpecHeader.entryCount) {
                        entryFlags[i] = Buffers.readUInt(buffer)
                        i++
                    }
                    //id start from 1
                    val typeSpecName = resourcePackage.typeStringPool[typeSpecHeader.id - 1]
                    val typeSpec = TypeSpec(typeSpecHeader, entryFlags, typeSpecName)
                    resourcePackage.addTypeSpec(typeSpec)
                    Buffers.position(buffer, chunkBegin + typeSpecHeader.bodySize)
                }

                ChunkType.TABLE_TYPE -> {
                    val typeHeader = chunkHeader as TypeHeader
                    // read offsets table
                    val offsets = LongArray(typeHeader.entryCount)
                    var i = 0
                    while (i < typeHeader.entryCount) {
                        offsets[i] = Buffers.readUInt(buffer)
                        i++
                    }
                    val type = Type(typeHeader)
                    type.name = resourcePackage.typeStringPool[typeHeader.id - 1]
                    val entryPos = chunkBegin + typeHeader.entriesStart - typeHeader.headerSize.toInt()
                    Buffers.position(buffer, entryPos)
                    val b = buffer.slice()
                    b.order(byteOrder)
                    type.buffer = b
                    type.keyStringPool = resourcePackage.keyStringPool
                    type.setOffsets(offsets)
                    type.setStringPool(stringPool)
                    resourcePackage.addType(type)
                    locales.add(type.locale)
                    Buffers.position(buffer, chunkBegin + typeHeader.bodySize)
                }

                ChunkType.TABLE_PACKAGE -> {
                    // another package. we should read next package here
                    pair.right = (chunkHeader as PackageHeader)
                    break@outer
                }

                ChunkType.TABLE_LIBRARY -> {
                    // read entries
                    val libraryHeader = chunkHeader as LibraryHeader
                    var i: Long = 0
                    while (i < libraryHeader.count) {
                        val packageId = buffer.int
                        val name = Buffers.readZeroTerminatedString(buffer, 128)
                        val entry = LibraryEntry(packageId, name)
                        i++
                    }
                    Buffers.position(buffer, chunkBegin + chunkHeader.bodySize)
                }

                ChunkType.NULL -> //                    Buffers.position(buffer, chunkBegin + chunkHeader.getBodySize());
                    Buffers.position(buffer, buffer.position() + buffer.remaining())

                else -> throw ParserException("unexpected chunk type: 0x" + chunkHeader.chunkType.toInt())
            }
        }
        return pair
    }

    private fun readChunkHeader(): ChunkHeader {
        val begin = buffer.position().toLong()
        val chunkType = Buffers.readUShort(buffer)
        val headerSize = Buffers.readUShort(buffer)
        val chunkSize = Buffers.readUInt(buffer).toInt()
        return when (chunkType) {
            ChunkType.TABLE -> {
                val resourceTableHeader = ResourceTableHeader(headerSize, chunkSize)
                resourceTableHeader.packageCount = Buffers.readUInt(buffer)
                Buffers.position(buffer, begin + headerSize)
                resourceTableHeader
            }

            ChunkType.STRING_POOL -> {
                val stringPoolHeader = StringPoolHeader(headerSize, chunkSize.toLong())
                stringPoolHeader.setStringCount(Buffers.readUInt(buffer))
                stringPoolHeader.setStyleCount(Buffers.readUInt(buffer))
                stringPoolHeader.flags = Buffers.readUInt(buffer)
                stringPoolHeader.stringsStart = Buffers.readUInt(buffer)
                stringPoolHeader.stylesStart = Buffers.readUInt(buffer)
                Buffers.position(buffer, begin + headerSize)
                stringPoolHeader
            }

            ChunkType.TABLE_PACKAGE -> {
                val packageHeader = PackageHeader(headerSize, chunkSize.toLong())
                packageHeader.id = Buffers.readUInt(buffer)
                packageHeader.name = ParseUtils.readStringUTF16(buffer, 128)
                packageHeader.setTypeStrings(Buffers.readUInt(buffer))
                packageHeader.setLastPublicType(Buffers.readUInt(buffer))
                packageHeader.setKeyStrings(Buffers.readUInt(buffer))
                packageHeader.setLastPublicKey(Buffers.readUInt(buffer))
                Buffers.position(buffer, begin + headerSize)
                packageHeader
            }

            ChunkType.TABLE_TYPE_SPEC -> {
                val typeSpecHeader = TypeSpecHeader(headerSize, chunkSize.toLong())
                typeSpecHeader.id = Buffers.readUByte(buffer)
                typeSpecHeader.res0 = Buffers.readUByte(buffer)
                typeSpecHeader.res1 = Buffers.readUShort(buffer)
                typeSpecHeader.setEntryCount(Buffers.readUInt(buffer))
                Buffers.position(buffer, begin + headerSize)
                typeSpecHeader
            }

            ChunkType.TABLE_TYPE -> {
                val typeHeader = TypeHeader(headerSize, chunkSize.toLong())
                typeHeader.id = Buffers.readUByte(buffer)
                typeHeader.res0 = Buffers.readUByte(buffer)
                typeHeader.res1 = Buffers.readUShort(buffer)
                typeHeader.setEntryCount(Buffers.readUInt(buffer))
                typeHeader.setEntriesStart(Buffers.readUInt(buffer))
                typeHeader.config = readResTableConfig()
                Buffers.position(buffer, begin + headerSize)
                typeHeader
            }

            ChunkType.TABLE_LIBRARY -> {
                //DynamicRefTable
                val libraryHeader = LibraryHeader(headerSize, chunkSize.toLong())
                libraryHeader.setCount(Buffers.readUInt(buffer))
                Buffers.position(buffer, begin + headerSize)
                libraryHeader
            }

            ChunkType.TABLE_OVERLAYABLE, ChunkType.NULL -> {
                Buffers.position(buffer, begin + headerSize)
                NullHeader(headerSize, chunkSize)
            }

            ChunkType.TABLE_STAGED_ALIAS -> throw ParserException(
                "Unexpected chunk Type: 0x" + Integer.toHexString(
                    chunkType
                )
            )

            else -> throw ParserException(
                "Unexpected chunk Type: 0x" + Integer.toHexString(
                    chunkType
                )
            )
        }
    }

    private fun readResTableConfig(): ResTableConfig {
        val beginPos = buffer.position().toLong()
        val config = ResTableConfig()
        val size = Buffers.readUInt(buffer)
        // imsi
        config.mcc = buffer.short
        config.mnc = buffer.short
        //read locale
        config.language = String(Buffers.readBytes(buffer, 2)).replace("\u0000", "")
        config.country = String(Buffers.readBytes(buffer, 2)).replace("\u0000", "")
        //screen type
        config.orientation = Buffers.readUByte(buffer)
        config.touchscreen = Buffers.readUByte(buffer)
        config.density = Buffers.readUShort(buffer)
        // now just skip the others...
        val endPos = buffer.position().toLong()
        Buffers.skip(buffer, (size - (endPos - beginPos)).toInt())
        return config
    }
}