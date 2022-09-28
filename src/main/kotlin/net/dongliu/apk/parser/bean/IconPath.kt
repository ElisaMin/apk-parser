package net.dongliu.apk.parser.bean

/**
 * Icon path, and density
 */
data class IconPath(
    /**
     * The icon path in apk file
     */
    val path: String = "",
    /**
     * Return the density this icon for. 0 means default icon.
     * see [net.dongliu.apk.parser.struct.resource.Densities] for more density values.
     */
    val density: Int = -255
)