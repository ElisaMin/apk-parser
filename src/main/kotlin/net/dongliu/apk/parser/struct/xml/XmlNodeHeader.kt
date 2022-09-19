package net.dongliu.apk.parser.struct.xml

import net.dongliu.apk.parser.struct.ChunkHeader

/**
 * @author dongliu
 */
class XmlNodeHeader(chunkType: Int, headerSize: Int, chunkSize: Long) : ChunkHeader(chunkType, headerSize, chunkSize) {
    /**
     * Line number in original source file at which this element appeared.
     */
    var lineNum = 0

    /**
     * Optional XML comment string pool ref, -1 if none
     */
    var commentRef = 0
}