package net.dongliu.apk.parser.struct.resource

import net.dongliu.apk.parser.struct.ChunkHeader
import net.dongliu.apk.parser.struct.ChunkType
import net.dongliu.apk.parser.utils.Unsigned

/**
 * @author dongliu
 */
class TypeSpecHeader(headerSize: Int, chunkSize: Long) : ChunkHeader(ChunkType.TABLE_TYPE_SPEC, headerSize, chunkSize) {
    /**
     * The type identifier this chunk is holding.  Type IDs start at 1 (corresponding to the value
     * of the type bits in a resource identifier).  0 is invalid.
     * The id also specifies the name of the Resource type. It is the string at index id - 1 in the
     * typeStrings StringPool chunk in the containing Package chunk.
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
        get() = Unsigned.toShort(field).toByte()
        set(res0) {
            field = Unsigned.toUByte(res0.toShort())
        }

    /**
     * Must be 0.uint16_t
     */
    var res1: Short = 0
        get() = Unsigned.toInt(field).toShort()
        set(res1) {
            field = Unsigned.toUShort(res1.toInt())
        }

    /**
     * Number of uint32_t entry configuration masks that follow.
     */
    var entryCount = 0
        private set

    fun setEntryCount(entryCount: Long) {
        this.entryCount = Unsigned.ensureUInt(entryCount)
    }
}