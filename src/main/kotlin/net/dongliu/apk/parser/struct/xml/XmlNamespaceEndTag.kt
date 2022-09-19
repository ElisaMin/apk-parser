package net.dongliu.apk.parser.struct.xml

/**
 * @author dongliu
 */
class XmlNamespaceEndTag(val prefix: String?, val uri: String?) {
    override fun toString(): String {
        return prefix + "=" + uri
    }
}