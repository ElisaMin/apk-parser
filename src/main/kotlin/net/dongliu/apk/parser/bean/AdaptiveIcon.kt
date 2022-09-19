package net.dongliu.apk.parser.bean

import java.io.Serializable

/**
 * Android adaptive icon, from android 8.0
 */
data class AdaptiveIcon(
    /**
     * The foreground icon
     */
    val foreground: Icon?,
    /**
     * The background icon
     */
    val background: Icon?
) : IconFace, Serializable {

    override val isFile: Boolean
        get() = foreground!!.isFile
    override val data: ByteArray?
        get() = foreground?.data
    override val path: String
        get() = foreground?.path!!

    companion object {
        private const val serialVersionUID = 4185750290211529320L
    }
}