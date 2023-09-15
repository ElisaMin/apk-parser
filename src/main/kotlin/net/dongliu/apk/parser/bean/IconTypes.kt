package net.dongliu.apk.parser.bean

import net.dongliu.apk.parser.struct.resource.Densities
import java.awt.image.BufferedImage
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.Serializable
import javax.imageio.ImageIO

typealias IconResource = IconTypes<out Any>


fun IconTypes.Raster.toImage(): BufferedImage? = kotlin.runCatching {
    BufferedInputStream(ByteArrayInputStream(data)).let {
        ImageIO.read(it)
    }
}.onFailure { exception -> exception.printStackTrace() }.getOrNull()

/**
 * The icon interface
 */
sealed interface IconTypes<T:Any> : Serializable {
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

    data class Empty internal constructor (override val path:String)  : IconTypes<Unit> {

        internal constructor() : this("")

        override val density: Int = -255
        override val data = Unit
    }


    /**
     * The plain icon, using color drawable resource.
     */
    data class Color internal constructor(override val data: String) : IconTypes<String>, Serializable {
        override val density: Int get() = Densities.NONE
        override val path: String get() = data
        val value by lazy {
            data.removePrefix("#").toLong(16) or 0x00000000FF000000
        }
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
    ):IconTypes<String>,Serializable {
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
        override val data: Data
    ) : IconTypes<Adaptive.Data>, Serializable {
        /**
         * The foreground icon
         */
        @Deprecated("Use data.foreground instead ", ReplaceWith("data.foreground"), DeprecationLevel.HIDDEN)
        val foreground get() = data.foreground
        /**
         * The background icon
         */
        @Deprecated("Use data.background instead", ReplaceWith("data.background"), DeprecationLevel.HIDDEN)
        val background get() = data.background

        override val density: Int = 0

        companion object {
            private const val serialVersionUID = 4185750290211529320L
        }
        @JvmInline
        value class Data internal constructor(val data:Pair<IconResource,IconResource>):Serializable {
            companion object {
                private const val serialVersionUID = 4185750290211529320L
            }
            internal constructor(foreground:IconResource, background:IconResource):this(Pair(foreground,background))
            val foreground: IconTypes<*> get() = data.first
            val background: IconTypes<*> get() = data.second

            override fun toString(): String {
                return "AdaptiveData(foreground=${data.first.data},background=${data.second.data})"
            }
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
    ) : IconTypes<ByteArray>, Serializable {

        companion object {
            private const val serialVersionUID = 8680309892249769701L
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Raster

            if (path != other.path) return false
            if (density != other.density) return false
            return data.contentEquals(other.data)
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