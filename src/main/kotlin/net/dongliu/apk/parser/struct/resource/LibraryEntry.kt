package net.dongliu.apk.parser.struct.resource

/**
 * Library chunk entry
 *
 * @author Liu Dong
 */
class LibraryEntry(
    /**
     * uint32. The package-id this shared library was assigned at build time.
     */
    val packageId: Int,
    /**
     * The package name of the shared library. \0 terminated. max 128
     */
    val name: String
)