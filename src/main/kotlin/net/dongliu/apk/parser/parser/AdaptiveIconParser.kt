package net.dongliu.apk.parser.parser

import net.dongliu.apk.parser.struct.xml.*

/**
 * Parse adaptive icon xml file.
 *
 * @author Liu Dong dongliu@live.cn
 */
class AdaptiveIconParser : XmlStreamer {
    var foreground: String? = null
        private set
    var background: String? = null
        private set

    override fun onStartTag(xmlNodeStartTag: XmlNodeStartTag) {
        if ("background" == xmlNodeStartTag.name) {
            background = getDrawable(xmlNodeStartTag)
        } else if ("foreground" == xmlNodeStartTag.name) {
            foreground = getDrawable(xmlNodeStartTag)
        }
    }

    private fun getDrawable(xmlNodeStartTag: XmlNodeStartTag): String? {
        val attributes = xmlNodeStartTag.attributes
        for (attribute in attributes.attributes) {
            if (attribute!!.name == "drawable") {
                return attribute.value
            }
        }
        return null
    }

    override fun onEndTag(xmlNodeEndTag: XmlNodeEndTag) {}
    override fun onCData(xmlCData: XmlCData) {}
    override fun onNamespaceStart(tag: XmlNamespaceStartTag) {}
    override fun onNamespaceEnd(tag: XmlNamespaceEndTag) {}
}