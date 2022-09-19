package net.dongliu.apk.parser.struct.xml

/**
 * @author dongliu
 */
class XmlNodeStartTag(
    val namespace: String?, val name: String?, //    /**
    //     * Byte offset from the start of this structure where the attributes start. uint16
    //     */
    //    public int attributeStart;
    //    /**
    //     * Size of the ResXMLTree_attribute structures that follow. unit16
    //     */
    //    public int attributeSize;
    //    /**
    //     * Number of attributes associated with an ELEMENT. uint 16
    //     * These are available as an array of ResXMLTree_attribute structures immediately following this node.
    //     */
    //    public int attributeCount;
    //    /**
    //     * Index (1-based) of the "id" attribute. 0 if none. uint16
    //     */
    //    public short idIndex;
    //    /**
    //     * Index (1-based) of the "style" attribute. 0 if none. uint16
    //     */
    //    public short styleIndex;
    val attributes: Attributes
) {
    override fun toString(): String {
        val sb = StringBuilder()
        sb.append('<')
        if (namespace != null) {
            sb.append(namespace).append(":")
        }
        sb.append(name)
        sb.append('>')
        return sb.toString()
    }
}