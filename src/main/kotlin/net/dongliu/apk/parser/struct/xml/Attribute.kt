package net.dongliu.apk.parser.struct.xml

import net.dongliu.apk.parser.struct.ResourceValue
import net.dongliu.apk.parser.struct.resource.ResourceTable
import net.dongliu.apk.parser.utils.ResourceLoader
import java.util.*

/**
 * xml node attribute
 *
 * @author dongliu
 */
class Attribute(
    @JvmField val namespace: String, @JvmField val name: String,
    /**
     * The original raw string value of Attribute
     */
    @JvmField val rawValue: String?,
    /**
     * Processed typed value of Attribute
     */
    @JvmField val typedValue: ResourceValue?
) {
    /**
     * the final value as string
     */
    @JvmField
    var value: String? = null

    fun toStringValue(resourceTable: ResourceTable, locale: Locale): String {
        val rawValue = rawValue
        return if (rawValue != null) {
            rawValue
        } else {
            val typedValue = typedValue
            typedValue?.toStringValue(resourceTable, locale) ?: // something happen;
            ""
        }
    }

    /**
     * These are attribute resource constants for the platform; as found in android.R.attr
     *
     * @author dongliu
     */
    object AttrIds {
        private val ids = ResourceLoader.loadSystemAttrIds()

        @JvmStatic
        fun getString(id: Long): String {
            return ids[id.toInt()] ?: "AttrId:0x${java.lang.Long.toHexString(id)}"
        }
    }

    override fun toString(): String {
        return "Attribute{" +
                "name='" + name + '\'' +
                ", namespace='" + namespace + '\'' +
                '}'
    }
}
