package net.dongliu.apk.parser.struct.resource

import java.util.*

/**
 * @author dongliu.
 */
class ResourceMapEntry(
    size: Int, flags: Int, key: String?,
    /**
     * Resource identifier of the parent mapping, or 0 if there is none.
     * ResTable_ref specifies the parent Resource, if any, of this Resource.
     * struct ResTable_ref { uint32_t ident; };
     */
    val parent: Long,
    /**
     * Number of name/value pairs that follow for FLAG_COMPLEX. uint32_t
     */
    val count: Long, val resourceTableMaps: Array<ResourceTableMap?>
) : ResourceEntry(size, flags, key, null) {
    /**
     * get value as string
     */
    override fun toStringValue(resourceTable: ResourceTable?, locale: Locale?): String? {
        return if (resourceTableMaps.size > 0) {
            resourceTableMaps[0].toString()
        } else {
            null
        }
    }

    override fun toString(): String {
        return "ResourceMapEntry{" +
                "parent=" + parent +
                ", count=" + count +
                ", resourceTableMaps=" + Arrays.toString(resourceTableMaps) +
                '}'
    }
}