package net.dongliu.apk.parser.parser

import net.dongliu.apk.parser.struct.xml.*
import net.dongliu.apk.parser.utils.xml.XmlEscaper

/**
 * trans to xml text when parse binary xml file.
 *
 * @author dongliu
 */
class XmlTranslator : XmlStreamer {
    private val sb: StringBuilder = StringBuilder()
    private var shift = 0
    init {
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
    }
    private val namespaces: XmlNamespaces  = XmlNamespaces()
    private var isLastStartTag = false

    override fun onStartTag(xmlNodeStartTag: XmlNodeStartTag) {
        if (isLastStartTag) {
            sb.append(">\n")
        }
        appendShift(shift++)
        sb.append('<')
        if (xmlNodeStartTag.namespace != null) {
            val prefix = namespaces.getPrefixViaUri(xmlNodeStartTag.namespace)
            if (prefix != null) {
                sb.append(prefix).append(":")
            } else {
                sb.append(xmlNodeStartTag.namespace).append(":")
            }
        }
        sb.append(xmlNodeStartTag.name)
        val nps = namespaces.consumeNameSpaces()
        if (nps.isNotEmpty()) {
            for (np in nps) {
                sb.append(" xmlns:").append(np.prefix).append("=\"")
                    .append(np.uri)
                    .append("\"")
            }
        }
        isLastStartTag = true
        for (attribute in xmlNodeStartTag.attributes.attributes) {
            onAttribute(attribute)
        }
    }

    private fun onAttribute(attribute: Attribute?) {
        sb.append(" ")
        var namespace = namespaces.getPrefixViaUri(attribute!!.namespace)
        if (namespace == null) {
            namespace = attribute.namespace
        }
        if (namespace.isNotEmpty()) {
            sb.append(namespace).append(':')
        }
        val escapedFinalValue = XmlEscaper.escapeXml10(attribute.value)
        sb.append(attribute.name).append('=').append('"')
            .append(escapedFinalValue).append('"')
    }

    override fun onEndTag(xmlNodeEndTag: XmlNodeEndTag) {
        --shift
        if (isLastStartTag) {
            sb.append(" />\n")
        } else {
            appendShift(shift)
            sb.append("</")
            if (xmlNodeEndTag.namespace != null) {
                var namespace = namespaces.getPrefixViaUri(xmlNodeEndTag.namespace)
                if (namespace == null) {
                    namespace = xmlNodeEndTag.namespace
                }
                sb.append(namespace).append(":")
            }
            sb.append(xmlNodeEndTag.name)
            sb.append(">\n")
        }
        isLastStartTag = false
    }

    override fun onCData(xmlCData: XmlCData) {
        appendShift(shift)
        sb.append(xmlCData.value).append('\n')
        isLastStartTag = false
    }

    override fun onNamespaceStart(tag: XmlNamespaceStartTag) {
        namespaces.addNamespace(tag)
    }

    override fun onNamespaceEnd(tag: XmlNamespaceEndTag) {
        namespaces.removeNamespace(tag)
    }

    private fun appendShift(shift: Int) {
        for (i in 0 until shift) {
            sb.append("\t")
        }
    }

    val xml: String
        get() = sb.toString()
}