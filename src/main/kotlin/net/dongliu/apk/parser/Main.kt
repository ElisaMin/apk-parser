package net.dongliu.apk.parser

import net.dongliu.apk.parser.bean.IconTypes
import net.dongliu.apk.parser.bean.toImage
import java.io.IOException
import java.security.cert.CertificateException
import java.util.*
import javax.swing.*

/**
 * Main method for parser apk
 *
 * @author Liu Dong &lt;dongliu@live.cn&gt;
 */
internal object Main {
    @Throws(IOException::class, CertificateException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val action = args[0]
        val apkPath = args[1]
        ApkFile(apkPath).use { apkFile ->
            apkFile.preferredLocale = Locale.getDefault()
            when (action) {
                "meta" -> println(apkFile.apkMeta)
                "manifest" -> println(apkFile.manifestXml)
                "signer" -> println(apkFile.apkSingers)
                "icon" -> displayIcon(apkFile)
                "iconXml"-> apkFile.transBinaryXml("res/3N.xml").let(::println)
                else -> {}
            }
        }
    }
}
internal fun displayIcon(apkFile: ApkFile) {

    apkFile.icons.forEach {
        print(it::class.simpleName)
        print(it.path)
        println(it.density)
        if (it is IconTypes.Adaptive) {
            println(it)
        }
    }
    JFrame().apply {
        JPanel().apply {
            apkFile.icons.asSequence().filterIsInstance<IconTypes.Raster>().map {
                it
                    .toImage()
                    ?.let(::ImageIcon)
                    ?.let(::JLabel)
            }.filterNotNull().forEach(::add)
        }.run(::add)
        isVisible = true
    }
}