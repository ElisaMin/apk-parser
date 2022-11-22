package net.dongliu.apk.parser.bean

import net.dongliu.apk.parser.struct.resource.Densities
import java.awt.image.BufferedImage
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.Serializable
import javax.imageio.ImageIO


fun ApkIcon.Raster.toImage(): BufferedImage? = kotlin.runCatching {
    BufferedInputStream(ByteArrayInputStream(data)).let {
        ImageIO.read(it)
    }
}.getOrNull()

/**
 * The icon interface
 */
sealed interface ApkIcon<T:Any> : Serializable {
    /**
     * Return the density this icon for. 0 means default icon.
     * see [net.dongliu.apk.parser.struct.resource.Densities] for more density values.
     */
    val density: Int

    /**
     * Return the icon file data ext.XML String, Color
     */
    val data: T

    /**
     * Return the icon file path in apk file. This method is valid only when [.isFile] return true.
     * Otherwise, [UnsupportedOperationException] should be thrown.
     */
    val path: String

    data class Empty internal constructor (override val path:String)  : ApkIcon<Unit> {

        internal constructor() : this("")

        override val density: Int = -255
        override val data = Unit
    }


    /**
     * The plain icon, using color drawable resource.
     */
    data class Color internal constructor(override val data: String) : ApkIcon<String>, Serializable {
        override val density: Int get() = Densities.NONE
        override val path: String get() = data

        companion object {
            private const val serialVersionUID = -7913024425268466186L
        }
    }

    /**
     * Vector data Icon , its drawable
     */
    data class Vector internal constructor(
        override val path: String,
        override val data: String,
    ):ApkIcon<String>,Serializable {
        override val density: Int = -2
        companion object {
            private const val serialVersionUID = 4185750290222529320L
        }
    }

    /**
     * Android adaptive icon, from android 8.0
     */
    data class Adaptive internal constructor(
        override val path:String,
        override val data: Pair<ApkIcon<*>, ApkIcon<*>>
    ) : ApkIcon<Pair<ApkIcon<*>, ApkIcon<*>>>, Serializable {
        /**
         * The foreground icon
         */
        val foreground: ApkIcon<*> get() = data.first
        /**
         * The background icon
         */
        val background: ApkIcon<*> get() = data.second

        override val density: Int = 0

        companion object {
            private const val serialVersionUID = 4185750290211529320L
        }
    }
    /**
     * The plain file apk icon.
     *
     * @author Liu Dong
     */
    data class Raster internal constructor(
        /**
         * The icon path in apk file
         */
        override val path: String,
        /**
         * Return the density this icon for. 0 means default icon.
         * see [net.dongliu.apk.parser.struct.resource.Densities] for more density values.
         */
        override val density: Int,
        /**
         * Icon data may be null, due to some apk missing the icon file.
         */
        override val data: ByteArray
    ) : ApkIcon<ByteArray>, Serializable {

        companion object {
            private const val serialVersionUID = 8680309892249769701L
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Raster) return false

            if (path != other.path) return false
            if (density != other.density) return false
            if (!data.contentEquals(other.data)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = path.hashCode()
            result = 31 * result + density
            result = 31 * result + data.contentHashCode()
            return result
        }

    }

    companion object {
        val empty = Empty()
    }

}