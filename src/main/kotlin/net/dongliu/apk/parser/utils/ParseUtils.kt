package net.dongliu.apk.parser.utils

import net.dongliu.apk.parser.exception.ParserException
import net.dongliu.apk.parser.parser.StringPoolEntry
import net.dongliu.apk.parser.struct.*
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

/**
 * @author dongliu
 */
object ParseUtils {
    val charsetUTF8 = StandardCharsets.UTF_8

    /**
     * read string from input buffer. if get EOF before read enough data, throw IOException.
     */
    fun readString(buffer: ByteBuffer, utf8: Boolean): String {
        return if (utf8) {
            //  The lengths are encoded in the same way as for the 16-bit format
            // but using 8-bit rather than 16-bit integers.
            val strLen = readLen(buffer)
            val bytesLen = readLen(buffer)
            val bytes = Buffers.readBytes(buffer, bytesLen)
            val str = String(bytes, charsetUTF8)
            // zero
            val trailling = Buffers.readUByte(buffer).toInt()
            str
        } else {
            // The length is encoded as either one or two 16-bit integers as per the commentRef...
            val strLen = readLen16(buffer)
            val str = Buffers.readString(buffer, strLen)
            // zero
            val trailling = Buffers.readUShort(buffer)
            str
        }
    }

    /**
     * read utf-16 encoding str, use zero char to end str.
     */
    fun readStringUTF16(buffer: ByteBuffer, strLen: Int): String {
        val str = Buffers.readString(buffer, strLen)
        for (i in 0 until str.length) {
            val c = str[i]
            if (c.code == 0) {
                return str.substring(0, i)
            }
        }
        return str
    }

    /**
     * read encoding len.
     * see StringPool.cpp ENCODE_LENGTH
     */
    private fun readLen(buffer: ByteBuffer): Int {
        var len = 0
        val i = Buffers.readUByte(buffer).toInt()
        if (i and 0x80 != 0) {
            //read one more byte.
            len = len or (i and 0x7f shl 8)
            len += Buffers.readUByte(buffer).toInt()
        } else {
            len = i
        }
        return len
    }

    /**
     * read encoding len.
     * see Stringpool.cpp ENCODE_LENGTH
     */
    private fun readLen16(buffer: ByteBuffer): Int {
        var len = 0
        val i = Buffers.readUShort(buffer)
        if (i and 0x8000 != 0) {
            len = len or (i and 0x7fff shl 16)
            len += Buffers.readUShort(buffer)
        } else {
            len = i
        }
        return len
    }

    /**
     * read String pool, for apk binary xml file and resource table.
     */
    fun readStringPool(buffer: ByteBuffer, stringPoolHeader: StringPoolHeader): StringPool {
        val beginPos = buffer.position().toLong()
        val offsets = IntArray(stringPoolHeader.stringCount)
        // read strings offset
        if (stringPoolHeader.stringCount > 0) {
            for (idx in 0 until stringPoolHeader.stringCount) {
                offsets[idx] = Unsigned.toUInt(Buffers.readUInt(buffer))
            }
        }
        // read flag
        // the string index is sorted by the string values if true
        val sorted = stringPoolHeader.flags and StringPoolHeader.SORTED_FLAG.toLong() != 0L
        // string use utf-8 format if true, otherwise utf-16
        val utf8 = stringPoolHeader.flags and StringPoolHeader.UTF8_FLAG.toLong() != 0L
        // read strings. the head and metas have 28 bytes
        val stringPos = beginPos + stringPoolHeader.stringsStart - stringPoolHeader.headerSize.toInt()
        Buffers.position(buffer, stringPos)
        val entries = arrayOfNulls<StringPoolEntry>(offsets.size)
        for (i in offsets.indices) {
            entries[i] = StringPoolEntry(i, stringPos + Unsigned.toLong(offsets[i]))
        }
        var lastStr: String? = null
        var lastOffset: Long = -1
        val stringPool = StringPool(stringPoolHeader.stringCount)
        for (entry in entries) {
            if (entry!!.offset == lastOffset) {
                stringPool[entry.idx] = lastStr
                continue
            }
            Buffers.position(buffer, entry.offset)
            lastOffset = entry.offset
            val str = readString(buffer, utf8)
            lastStr = str
            stringPool[entry.idx] = str
        }
        // read styles
        if (stringPoolHeader.styleCount > 0) {
            // now we just skip it
        }
        Buffers.position(buffer, beginPos + stringPoolHeader.bodySize)
        return stringPool
    }

    /**
     * read res value, convert from different types to string.
     */
    fun readResValue(buffer: ByteBuffer, stringPool: StringPool?): ResourceValue? {
//        ResValue resValue = new ResValue();
        val size = Buffers.readUShort(buffer)
        val res0 = Buffers.readUByte(buffer)
        val dataType = Buffers.readUByte(buffer)
        return when (dataType) {
            ResValue.ResType.INT_DEC -> ResourceValue.Companion.decimal(buffer.int)
            ResValue.ResType.INT_HEX -> ResourceValue.Companion.hexadecimal(buffer.int)
            ResValue.ResType.STRING -> {
                val strRef = buffer.int
                if (strRef >= 0) {
                    ResourceValue.Companion.string(strRef, stringPool)
                } else {
                    null
                }
            }

            ResValue.ResType.REFERENCE -> ResourceValue.Companion.reference(buffer.int)
            ResValue.ResType.INT_BOOLEAN -> ResourceValue.Companion.bool(buffer.int)
            ResValue.ResType.NULL -> ResourceValue.Companion.nullValue()
            ResValue.ResType.INT_COLOR_RGB8, ResValue.ResType.INT_COLOR_RGB4 -> ResourceValue.Companion.rgb(
                buffer.int,
                6
            )

            ResValue.ResType.INT_COLOR_ARGB8, ResValue.ResType.INT_COLOR_ARGB4 -> ResourceValue.Companion.rgb(
                buffer.int,
                8
            )

            ResValue.ResType.DIMENSION -> ResourceValue.Companion.dimension(buffer.int)
            ResValue.ResType.FRACTION -> ResourceValue.Companion.fraction(buffer.int)
            else -> ResourceValue.Companion.raw(buffer.int, dataType)
        }
    }

    fun checkChunkType(expected: Int, real: Int) {
        if (expected != real) {
            throw ParserException(
                "Expect chunk type:" + Integer.toHexString(expected)
                        + ", but got:" + Integer.toHexString(real)
            )
        }
    }
}