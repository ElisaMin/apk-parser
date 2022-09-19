package net.dongliu.apk.parser.struct.resource

import net.dongliu.apk.parser.struct.ResourceValue
import java.util.*

/**
 * A Resource entry specifies the key (name) of the Resource.
 * It is immediately followed by the value of that Resource.
 *
 * @author dongliu
 */
open class ResourceEntry    //    /**
//     * If set, this resource has been declared public, so libraries
//     * are allowed to reference it.
//     */
//    public static final int FLAG_PUBLIC = 0x0002;
    (
    /**
     * Number of bytes in this structure. uint16_t
     */
    val size: Int,
    /**
     * uint16_t
     */
    val flags: Int,
    /**
     * Reference into ResTable_package::keyStrings identifying this entry.
     * public long keyRef;
     */
    val key: String?,
    /**
     * the resvalue following this resource entry.
     */
    val value: ResourceValue?
) {
    /**
     * get value as string
     */
    open fun toStringValue(resourceTable: ResourceTable?, locale: Locale?): String? {
        val value = value
        return if (value != null) {
            value.toStringValue(resourceTable, locale)
        } else {
            "null"
        }
    }

    override fun toString(): String {
        return "ResourceEntry{" +
                "size=" + size +
                ", flags=" + flags +
                ", key='" + key + '\'' +
                ", value=" + value +
                '}'
    }

    companion object {
        /**
         * If set, this is a complex entry, holding a set of name/value
         * mappings.  It is followed by an array of ResTable_map structures.
         */
        const val FLAG_COMPLEX = 0x0001
    }
}