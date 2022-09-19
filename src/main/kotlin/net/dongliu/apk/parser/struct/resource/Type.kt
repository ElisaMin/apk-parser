package net.dongliu.apk.parser.struct.resource

import net.dongliu.apk.parser.struct.*
import net.dongliu.apk.parser.utils.Buffers
import net.dongliu.apk.parser.utils.ParseUtils
import java.nio.ByteBuffer
import java.util.*

/**
 * @author dongliu
 */
class Type(header: TypeHeader) {
    var name: String? = null
    val id: Short
    val locale: Locale
    var keyStringPool: StringPool? = null
    var buffer: ByteBuffer? = null
    private var offsets: LongArray
    private var stringPool: StringPool? = null

    /**
     * see Densities.java for values
     */
    val density: Int

    init {
        id = header.id
        val config = header.config
        locale = Locale(config.language, config.country)
        density = config.density
    }

    fun getResourceEntry(id: Int): ResourceEntry? {
        if (id >= offsets.size) {
            return null
        }
        if (offsets[id] == TypeHeader.Companion.NO_ENTRY) {
            return null
        }
        // read Resource Entries
        Buffers.position(buffer!!, offsets[id])
        return readResourceEntry()
    }

    private fun readResourceEntry(): ResourceEntry {
        val beginPos = buffer!!.position().toLong()
        // size is always 8(simple), or 16(complex)
        val resourceEntrySize = Buffers.readUShort(buffer!!)
        val resourceEntryFlags = Buffers.readUShort(buffer!!)
        val keyRef = buffer!!.int.toLong()
        val resourceEntryKey = keyStringPool!![keyRef.toInt()]
        return if (resourceEntryFlags and ResourceEntry.Companion.FLAG_COMPLEX != 0) {
            // Resource identifier of the parent mapping, or 0 if there is none.
            val parent = Buffers.readUInt(buffer!!)
            val count = Buffers.readUInt(buffer!!)
            //            resourceMapEntry.setParent(parent);
//            resourceMapEntry.setCount(count);
            Buffers.position(buffer!!, beginPos + resourceEntrySize)
            //An individual complex Resource entry comprises an entry immediately followed by one or more fields.
            val resourceTableMaps = arrayOfNulls<ResourceTableMap>(count.toInt())
            for (i in 0 until count) {
                resourceTableMaps[i.toInt()] = readResourceTableMap()
            }
            ResourceMapEntry(resourceEntrySize, resourceEntryFlags, resourceEntryKey, parent, count, resourceTableMaps)
        } else {
            Buffers.position(buffer!!, beginPos + resourceEntrySize)
            val resourceEntryValue = ParseUtils.readResValue(buffer!!, stringPool)
            ResourceEntry(resourceEntrySize, resourceEntryFlags, resourceEntryKey, resourceEntryValue)
        }
    }

    private fun readResourceTableMap(): ResourceTableMap {
        val resourceTableMap = ResourceTableMap()
        resourceTableMap.nameRef = Buffers.readUInt(buffer!!)
        resourceTableMap.setResValue(ParseUtils.readResValue(buffer!!, stringPool))
        if (resourceTableMap.nameRef and 0x02000000L != 0L) {
            //read arrays
        } else if (resourceTableMap.nameRef and 0x01000000L != 0L) {
            // read attrs
        } else {
        }
        return resourceTableMap
    }

    fun setOffsets(offsets: LongArray) {
        this.offsets = offsets
    }

    fun setStringPool(stringPool: StringPool?) {
        this.stringPool = stringPool
    }

    override fun toString(): String {
        return "Type{" +
                "name='" + name + '\'' +
                ", id=" + id +
                ", locale=" + locale +
                '}'
    }
}