package net.dongliu.apk.parser.struct.resource

import net.dongliu.apk.parser.struct.ChunkHeader
import net.dongliu.apk.parser.struct.ChunkType
import net.dongliu.apk.parser.utils.Unsigned

/**
 * Table library chunk header
 *
 * @author Liu Dong
 */
class LibraryHeader(headerSize: Int, chunkSize: Long) : ChunkHeader(ChunkType.TABLE_LIBRARY, headerSize, chunkSize) {
    /**
     * uint32 value, The number of shared libraries linked in this resource table.
     */
    var count = 0
        private set

    fun setCount(count: Long) {
        this.count = Unsigned.ensureUInt(count)
    }
}