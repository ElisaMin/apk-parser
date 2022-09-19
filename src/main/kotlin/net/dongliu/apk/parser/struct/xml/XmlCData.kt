package net.dongliu.apk.parser.struct.xml

import net.dongliu.apk.parser.struct.ResourceValue
import net.dongliu.apk.parser.struct.resource.ResourceTable
import java.util.*

/**
 * @author dongliu
 */
class XmlCData {
    /**
     * The raw CDATA character data.
     */
    var data: String? = null

    /**
     * The typed value of the character data if this is a CDATA node.
     */
    var typedData: ResourceValue? = null

    /**
     * the final value as string
     */
    var value: String? = null

    /**
     * get value as string
     */
    fun toStringValue(resourceTable: ResourceTable?, locale: Locale?): String {
        return if (data != null) {
            CDATA_START + data + CDATA_END
        } else {
            CDATA_START + typedData!!.toStringValue(
                resourceTable,
                locale
            ) + CDATA_END
        }
    }

    override fun toString(): String {
        return "XmlCData{" +
                "data='" + data + '\'' +
                ", typedData=" + typedData +
                '}'
    }

    companion object {
        const val CDATA_START = "<![CDATA["
        const val CDATA_END = "]]>"
    }
}