package net.dongliu.apk.parser.struct.xml

/**
 * @author dongliu
 */
class XmlNodeEndTag {
    var namespace: String? = null
    var name: String? = null
    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("</")
        if (namespace != null) {
            sb.append(namespace).append(":")
        }
        sb.append(name).append('>')
        return sb.toString()
    }
}