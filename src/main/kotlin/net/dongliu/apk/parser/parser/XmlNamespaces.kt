package net.dongliu.apk.parser.parser

import net.dongliu.apk.parser.struct.xml.XmlNamespaceEndTag
import net.dongliu.apk.parser.struct.xml.XmlNamespaceStartTag

/**
 * the xml file's namespaces.
 *
 * @author dongliu
 */
internal class XmlNamespaces {
    private val namespaces: MutableList<XmlNamespace> = ArrayList()
    private val newNamespaces: MutableList<XmlNamespace> = ArrayList()
    fun addNamespace(tag: XmlNamespaceStartTag) {
        val namespace = XmlNamespace(tag.prefix, tag.uri)
        namespaces.add(namespace)
        newNamespaces.add(namespace)
    }

    fun removeNamespace(tag: XmlNamespaceEndTag) {
        val namespace = XmlNamespace(tag.prefix, tag.uri)
        namespaces.remove(namespace)
        newNamespaces.remove(namespace)
    }

    fun getPrefixViaUri(uri: String?): String? {
        if (uri == null) {
            return null
        }
        for (namespace in namespaces) {
            if (uri == namespace.uri) {
                return namespace.prefix
            }
        }
        return null
    }

    fun consumeNameSpaces(): List<XmlNamespace> {
        return if (!newNamespaces.isEmpty()) {
            val xmlNamespaces: List<XmlNamespace> = ArrayList(newNamespaces)
            newNamespaces.clear()
            xmlNamespaces
        } else {
            emptyList()
        }
    }

    /**
     * one namespace
     */
    data class XmlNamespace(val prefix: String?, val uri: String?)
}