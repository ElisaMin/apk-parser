package net.dongliu.apk.parser.struct.resource

import net.dongliu.apk.parser.struct.ChunkHeader
import net.dongliu.apk.parser.struct.ChunkType
import net.dongliu.apk.parser.utils.Unsigned

/**
 * @author dongliu
 */
class TypeHeader(headerSize: Int, chunkSize: Long) : ChunkHeader(ChunkType.TABLE_TYPE, headerSize, chunkSize) {
    /**
     * The type identifier this chunk is holding.  Type IDs start at 1 (corresponding to the value
     * of the type bits in a resource identifier).  0 is invalid.
     * uint8_t
     */
    var id: Byte = 0
        get() = Unsigned.toShort(field).toByte()
        set(id) {
            field = Unsigned.toUByte(id.toShort())
        }

    /**
     * Must be 0. uint8_t
     */
    var res0: Byte = 0
        get() = Unsigned.toUShort(field.toInt()).toByte()
        set(res0) {
            field = Unsigned.toUByte(res0.toShort())
        }

    /**
     * Must be 0. uint16_t
     */
    var res1: Short = 0
        get() = Unsigned.toInt(field).toShort()
        set(res1) {
            field = Unsigned.toUShort(res1.toInt())
        }

    /**
     * Number of uint32_t entry indices that follow. uint32
     */
    var entryCount = 0
        private set

    /**
     * Offset from header where ResTable_entry data starts.uint32_t
     */
    var entriesStart = 0
        private set

    /**
     * Configuration this collection of entries is designed for.
     */
    var config: ResTableConfig? = null
    fun setEntryCount(entryCount: Long) {
        this.entryCount = Unsigned.ensureUInt(entryCount)
    }

    fun setEntriesStart(entriesStart: Long) {
        this.entriesStart = Unsigned.ensureUInt(entriesStart)
    }

    companion object {
        const val NO_ENTRY = 0xFFFFFFFFL
    }
}