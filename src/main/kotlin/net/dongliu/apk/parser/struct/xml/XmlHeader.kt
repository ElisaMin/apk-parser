package net.dongliu.apk.parser.struct.xml

import net.dongliu.apk.parser.struct.ChunkHeader

/**
 * Binary XML header. It is simply a struct ResChunk_header.
 * The header.type is always 0×0003 (XML).
 *
 * @author dongliu
 */
class XmlHeader(chunkType: Int, headerSize: Int, chunkSize: Long) : ChunkHeader(chunkType, headerSize, chunkSize)