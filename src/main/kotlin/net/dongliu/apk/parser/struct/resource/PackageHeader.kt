package net.dongliu.apk.parser.struct.resource

import net.dongliu.apk.parser.struct.ChunkHeader
import net.dongliu.apk.parser.struct.ChunkType
import net.dongliu.apk.parser.utils.Unsigned

/**
 * @author dongliu
 */
class PackageHeader(headerSize: Int, chunkSize: Long) : ChunkHeader(ChunkType.TABLE_PACKAGE, headerSize, chunkSize) {
    /**
     * ResourcePackage IDs start at 1 (corresponding to the value of the package bits in a resource identifier).
     * 0 means this is not a base package.
     * uint32_t
     * 0 framework-res.apk
     * 2-9 other framework files
     * 127 application package
     * Anroid 5.0+: Shared libraries will be assigned a package ID of 0x00 at build-time.
     * At runtime, all loaded shared libraries will be assigned a new package ID.
     */
    var id = 0
        get() = Unsigned.toLong(field).toInt()
        set(id) {
            field = Unsigned.toUInt(id.toLong())
        }

    /**
     * Actual name of this package, -terminated.
     * char16_t name[128]
     */
    var name: String? = null

    /**
     * Offset to a ResStringPool_header defining the resource type symbol table.
     * If zero, this package is inheriting from another base package (overriding specific values in it).
     * uinit 32
     */
    var typeStrings = 0
        private set

    /**
     * Last index into typeStrings that is for public use by others.
     * uint32_t
     */
    var lastPublicType = 0
        private set

    /**
     * Offset to a ResStringPool_header defining the resource
     * key symbol table.  If zero, this package is inheriting from
     * another base package (overriding specific values in it).
     * uint32_t
     */
    var keyStrings = 0
        private set

    /**
     * Last index into keyStrings that is for public use by others.
     * uint32_t
     */
    var lastPublicKey = 0
        private set

    fun setTypeStrings(typeStrings: Long) {
        this.typeStrings = Unsigned.ensureUInt(typeStrings)
    }

    fun setLastPublicType(lastPublicType: Long) {
        this.lastPublicType = Unsigned.ensureUInt(lastPublicType)
    }

    fun setKeyStrings(keyStrings: Long) {
        this.keyStrings = Unsigned.ensureUInt(keyStrings)
    }

    fun setLastPublicKey(lastPublicKey: Long) {
        this.lastPublicKey = Unsigned.ensureUInt(lastPublicKey)
    }
}