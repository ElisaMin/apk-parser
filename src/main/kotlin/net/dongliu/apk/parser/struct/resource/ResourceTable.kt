package net.dongliu.apk.parser.struct.resource

import net.dongliu.apk.parser.struct.ResourceValue.ReferenceResourceValue
import net.dongliu.apk.parser.struct.StringPool
import net.dongliu.apk.parser.utils.ResourceLoader

/**
 * The apk resource table
 *
 * @author dongliu
 */
class ResourceTable(val stringPool: StringPool?) {
    private val packageMap: MutableMap<Short, ResourcePackage> = HashMap()
    fun addPackage(resourcePackage: ResourcePackage) {
        packageMap[resourcePackage.id] = resourcePackage
    }

    fun getPackage(id: Short): ResourcePackage? {
        return packageMap[id]
    }

    /**
     * Get resources match the given resource id.
     */
    fun getResourcesById(resourceId: Long): List<Resource> {
        // An Android Resource id is a 32-bit integer. It comprises
        // an 8-bit Package id [bits 24-31]
        // an 8-bit Type id [bits 16-23]
        // a 16-bit Entry index [bits 0-15]
        val packageId = (resourceId shr 24 and 0xffL).toShort()
        val typeId = (resourceId shr 16 and 0xffL).toShort()
        val entryIndex = (resourceId and 0xffffL).toInt()
        val resourcePackage = getPackage(packageId)
            ?: return emptyList()
        val typeSpec = resourcePackage.getTypeSpec(typeId)
        val types = resourcePackage.getTypes(typeId)
        if (typeSpec == null || types == null) {
            return emptyList()
        }
        if (!typeSpec.exists(entryIndex)) {
            return emptyList()
        }
        // read from type resource
        val result: MutableList<Resource> = ArrayList()
        for (type in types) {
            val resourceEntry = type!!.getResourceEntry(entryIndex) ?: continue
            val currentResourceValue = resourceEntry.value ?: continue
            // cyclic reference detect
            if (currentResourceValue is ReferenceResourceValue) {
                if (resourceId == currentResourceValue
                        .referenceResourceId
                ) {
                    continue
                }
            }
            result.add(Resource(typeSpec, type, resourceEntry))
        }
        return result
    }

    /**
     * contains all info for one resource
     */
    class Resource(val typeSpec: TypeSpec?, val type: Type, val resourceEntry: ResourceEntry)
    companion object {
        val sysStyle = ResourceLoader.loadSystemStyles()
    }
}