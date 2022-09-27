package net.dongliu.apk.parser.parser

import net.dongliu.apk.parser.utils.Inputs.readAllAndClose
import java.io.IOException
import java.nio.ByteBuffer
import java.security.cert.CertificateException
import java.util.*
import kotlin.test.Test

class ApkSignBlockParserTest {
    @Test
    @Throws(IOException::class, CertificateException::class)
    fun parse() {
        val bytes = readAllAndClose(javaClass.getResourceAsStream("/sign/gmail_sign_block")!!)
        val parser = ApkSignBlockParser(ByteBuffer.wrap(bytes))
        parser.parse()
    }
}