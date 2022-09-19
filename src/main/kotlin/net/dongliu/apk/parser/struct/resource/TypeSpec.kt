package net.dongliu.apk.parser.struct.resource

/**
 * @author dongliu
 */
class TypeSpec(header: TypeSpecHeader, entryFlags: LongArray, name: String?) {
    val entryFlags: LongArray
    val name: String?
    val id: Short

    init {
        id = header.id.toShort()
        this.entryFlags = entryFlags
        this.name = name
    }

    fun exists(id: Int): Boolean {
        return id < entryFlags.size
    }

    override fun toString(): String {
        return "TypeSpec{" +
                "name='" + name + '\'' +
                ", id=" + id +
                '}'
    }
}