package net.dongliu.apk.parser.utils

import net.dongliu.apk.parser.utils.Buffers.readUByte
import kotlin.test.*
import java.nio.ByteBuffer

class BuffersTest {
    @Test
    @Throws(Exception::class)
    fun testGetUnsignedByte() {
        val byteBuffer = ByteBuffer.wrap(byteArrayOf(2, -10))
        assertEquals(2, readUByte(byteBuffer).toLong())
        assertEquals(246, readUByte(byteBuffer).toLong())
    }
}