package net.dongliu.apk.parser.struct.resource

import net.dongliu.apk.parser.struct.ChunkHeader
import net.dongliu.apk.parser.struct.ChunkType
import net.dongliu.apk.parser.utils.Unsigned

/**
 * resource file header
 *
 * @author dongliu
 */
class ResourceTableHeader(headerSize: Int, chunkSize: Int) :
    ChunkHeader(ChunkType.TABLE, headerSize, chunkSize.toLong()) {
    /**
     * The number of ResTable_package structures. uint32
     */
    var packageCount = 0
        get() = Unsigned.toLong(field).toInt()
        set(packageCount) {
            field = Unsigned.toUInt(packageCount.toLong())
        }
}