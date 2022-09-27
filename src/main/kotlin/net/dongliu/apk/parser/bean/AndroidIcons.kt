package net.dongliu.apk.parser.bean

import java.awt.image.BufferedImage
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.Serializable
import javax.imageio.ImageIO


fun AndroidIcons.Raster.toImage(): BufferedImage =
    BufferedInputStream(ByteArrayInputStream(data)).let {
        ImageIO.read(it)
    }

/**
 * The icon interface
 */
sealed interface AndroidIcons<T:Any> : Serializable {
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


    /**
     * Vector data Icon , its drawable
     */
    data class Vector(
        override val path: String,
        override val data: String,
    ):AndroidIcons<String>,Serializable {
        override val density: Int = -2
        companion object {
            private const val serialVersionUID = 4185750290222529320L
        }
    }

    /**
     * Android adaptive icon, from android 8.0
     */
    data class Adaptive(
        override val path:String,
        override val data: Pair<Vector?, Vector?>
    ) : AndroidIcons<Pair<Vector?,Vector?>>, Serializable {

        /**
         * The foreground icon
         */
        val foreground: Vector? get() = data.first
        /**
         * The background icon
         */
        val background: Vector? get() = data.second

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
    open class Raster(
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
    ) : AndroidIcons<ByteArray>, Serializable {

        override fun toString(): String {
            return "Icon{path='$path', density=$density, size=$data}"
        }

        companion object {
            private const val serialVersionUID = 8680309892249769701L
        }
    }

//    /**
//     * The plain icon, using color drawable resource.
//     */
//    class ColorIcon : AndroidIcons<String>, Serializable {
//        override val density: Int
//            get() = throw UnsupportedOperationException()
//        override val data: String
//            get() {
//                throw UnsupportedOperationException()
//            }
//        override val path: String
//            get() {
//                throw UnsupportedOperationException()
//            }
//
//        companion object {
//            private const val serialVersionUID = -7913024425268466186L
//        }
//    }
}