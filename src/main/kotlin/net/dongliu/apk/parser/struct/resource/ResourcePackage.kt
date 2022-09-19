package net.dongliu.apk.parser.struct.resource

import net.dongliu.apk.parser.struct.StringPool

/**
 * Resource packge.
 *
 * @author dongliu
 */
class ResourcePackage(header: PackageHeader) {
    // the packageName
    var name: String?
    var id: Short

    /**
     * contains the names of the types of the Resources defined in the ResourcePackage
     */
    var typeStringPool: StringPool? = null

    /**
     * contains the names (keys) of the Resources defined in the ResourcePackage.
     */
    var keyStringPool: StringPool? = null
    private var typeSpecMap: MutableMap<Short, TypeSpec> = HashMap()
    private var typesMap: MutableMap<Short, MutableList<Type>> = HashMap()

    init {
        name = header.name
        id = header.id.toShort()
    }

    fun addTypeSpec(typeSpec: TypeSpec) {
        typeSpecMap[typeSpec.id] = typeSpec
    }

    fun getTypeSpec(id: Short): TypeSpec? {
        return typeSpecMap[id]
    }

    fun addType(type: Type) {
        var types = typesMap[type.id]
        if (types == null) {
            types = ArrayList()
            typesMap[type.id] = types
        }
        types.add(type)
    }

    fun getTypes(id: Short): List<Type>? {
        return typesMap[id]
    }


    fun getTypeSpecMap(): Map<Short, TypeSpec> {
        return typeSpecMap
    }

    fun setTypeSpecMap(typeSpecMap: MutableMap<Short, TypeSpec>) {
        this.typeSpecMap = typeSpecMap
    }

    fun getTypesMap(): Map<Short, MutableList<Type>> {
        return typesMap
    }

    fun setTypesMap(typesMap: MutableMap<Short, MutableList<Type>>) {
        this.typesMap = typesMap
    }
}