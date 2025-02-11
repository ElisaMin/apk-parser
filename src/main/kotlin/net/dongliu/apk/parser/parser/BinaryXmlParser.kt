package net.dongliu.apk.parser.parser

import net.dongliu.apk.parser.exception.ParserException
import net.dongliu.apk.parser.struct.*
import net.dongliu.apk.parser.struct.resource.ResourceTable
import net.dongliu.apk.parser.struct.xml.*
import net.dongliu.apk.parser.struct.xml.Attribute.AttrIds.getString
import net.dongliu.apk.parser.utils.Buffers
import net.dongliu.apk.parser.utils.Locales
import net.dongliu.apk.parser.utils.ParseUtils
import net.dongliu.apk.parser.utils.Strings.isNumeric
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

/**
 * Android Binary XML format
 * see http://justanapplication.wordpress.com/category/android/android-binary-xml/
 *
 * @author dongliu
 */
class BinaryXmlParser(buffer: ByteBuffer, resourceTable: ResourceTable) {
    /**
     * By default, the data buffer Chunks is buffer little-endian byte order both at runtime and when stored buffer
     * files.
     */
    private var stringPool: StringPool? = null

    /**
     * some attribute name stored by resource id
     */
    private var resourceMap: Array<String?> = emptyArray()
    private val buffer: ByteBuffer
    var xmlStreamer: XmlStreamer? = null
    private val resourceTable: ResourceTable

    /**
     * default locale.
     */
    var locale = Locales.any

    /**
     * Parse binary xml.
     */
    fun parse() {
        val firstChunkHeader = readChunkHeader() ?: return
        when (firstChunkHeader.chunkType.toInt()) {
            ChunkType.XML, ChunkType.NULL -> {}
            ChunkType.STRING_POOL -> {}
            else -> {}
        }
        // read string pool chunk
        val stringPoolChunkHeader: ChunkHeader = readChunkHeader() ?: return
        ParseUtils.checkChunkType(ChunkType.STRING_POOL, stringPoolChunkHeader.chunkType.toInt())
        stringPool = ParseUtils.readStringPool(buffer, stringPoolChunkHeader as StringPoolHeader)
        // read on chunk, check if it was an optional XMLResourceMap chunk
        var chunkHeader: ChunkHeader? = readChunkHeader() ?: return
        if (chunkHeader?.chunkType?.toInt() == ChunkType.XML_RESOURCE_MAP) {
            val resourceIds = readXmlResourceMap(chunkHeader as XmlResourceMapHeader)
            resourceMap = arrayOfNulls(resourceIds.size)
            for (i in resourceIds.indices) {
                resourceMap[i] = getString(resourceIds[i])
            }
            chunkHeader = readChunkHeader()
        }
        while (chunkHeader != null) {
            val beginPos = buffer.position().toLong()
            when (chunkHeader.chunkType.toInt()) {
                ChunkType.XML_END_NAMESPACE -> {
                    val xmlNamespaceEndTag = readXmlNamespaceEndTag()
                    xmlStreamer!!.onNamespaceEnd(xmlNamespaceEndTag)
                }

                ChunkType.XML_START_NAMESPACE -> {
                    val namespaceStartTag = readXmlNamespaceStartTag()
                    xmlStreamer!!.onNamespaceStart(namespaceStartTag)
                }

                ChunkType.XML_START_ELEMENT ->
                    readXmlNodeStartTag()
                ChunkType.XML_END_ELEMENT ->
                    readXmlNodeEndTag()
                ChunkType.XML_CDATA -> readXmlCData()
                else -> if (chunkHeader.chunkType.toInt() >= ChunkType.XML_FIRST_CHUNK &&
                    chunkHeader.chunkType.toInt() <= ChunkType.XML_LAST_CHUNK
                ) {
                    Buffers.skip(buffer, chunkHeader.bodySize)
                } else {
                    throw ParserException("Unexpected chunk type:" + chunkHeader.chunkType.toInt())
                }
            }
            Buffers.position(buffer, beginPos + chunkHeader.bodySize)
            chunkHeader = readChunkHeader()
        }
    }

    private fun readXmlCData(): XmlCData {
        val xmlCData = XmlCData()
        val dataRef = buffer.int
        if (dataRef > 0) {
            xmlCData.data = stringPool!![dataRef]
        }
        xmlCData.typedData = ParseUtils.readResValue(buffer, stringPool)
        if (xmlStreamer != null) {
            //TODO: to know more about cdata. some cdata appears buffer xml tags
//            String value = xmlCData.toStringValue(resourceTable, locale);
//            xmlCData.setValue(value);
//            xmlStreamer.onCData(xmlCData);
        }
        return xmlCData
    }

    private fun readXmlNodeEndTag(): XmlNodeEndTag {
        val xmlNodeEndTag = XmlNodeEndTag()
        val nsRef = buffer.int
        val nameRef = buffer.int
        if (nsRef > 0) {
            xmlNodeEndTag.namespace = stringPool!![nsRef]
        }
        xmlNodeEndTag.name = stringPool!![nameRef]
        if (xmlStreamer != null) {
            xmlStreamer!!.onEndTag(xmlNodeEndTag)
        }
        return xmlNodeEndTag
    }

    private fun readXmlNodeStartTag(): XmlNodeStartTag {
        val nsRef = buffer.int
        val nameRef = buffer.int
        val namespace = if (nsRef > 0) stringPool!![nsRef] else null
        val name = stringPool!![nameRef]
        // read attributes.
        // attributeStart and attributeSize are always 20 (0x14)
        val attributeStart = Buffers.readUShort(buffer)
        val attributeSize = Buffers.readUShort(buffer)
        val attributeCount = Buffers.readUShort(buffer)
        val idIndex = Buffers.readUShort(buffer)
        val classIndex = Buffers.readUShort(buffer)
        val styleIndex = Buffers.readUShort(buffer)
        // read attributes
        val attributes = Attributes(attributeCount)
        for (count in 0 until attributeCount) {
            val attribute = readAttribute()
            val attributeName = attribute.name
            if (xmlStreamer != null) {
                var value: String? = attribute.toStringValue(resourceTable, locale)
                if (intAttributes.contains(attributeName) && isNumeric(value)) {
                    try {
                        value = getFinalValueAsString(attributeName, value!!)
                    } catch (ignore: Exception) {
                    }
                }
                attribute.value = value
                attributes[count] = attribute
            }
        }
        val xmlNodeStartTag = XmlNodeStartTag(namespace, name, attributes)
        if (xmlStreamer != null) {
            xmlStreamer!!.onStartTag(xmlNodeStartTag)
        }
        return xmlNodeStartTag
    }

    init {
        this.buffer = buffer.duplicate()
        this.buffer.order(ByteOrder.LITTLE_ENDIAN)
        this.resourceTable = resourceTable
    }

    /**
     * trans int attr value to string
     */
    private fun getFinalValueAsString(attributeName: String, str: String): String? {
        val value = str.toInt()
        return when (attributeName) {
            "screenOrientation" -> AttributeValues.getScreenOrientation(value)
            "configChanges" -> AttributeValues.getConfigChanges(value)
            "windowSoftInputMode" -> AttributeValues.getWindowSoftInputMode(value)
            "launchMode" -> AttributeValues.getLaunchMode(value)
            "installLocation" -> AttributeValues.getInstallLocation(value)
            "protectionLevel" -> AttributeValues.getProtectionLevel(value)
            else -> str
        }
    }

    private fun readAttribute(): Attribute {
        val namespaceRef = buffer.int
        val nameRef = buffer.int
        var name = stringPool!![nameRef]
        if (name!!.isEmpty() && nameRef < resourceMap.size) {
            // some processed apk file make the string pool value empty, if it is a xmlmap attr.
            name = resourceMap[nameRef]
        }
        var namespace = if (namespaceRef > 0) stringPool!![namespaceRef] else null
        if (namespace.isNullOrEmpty() || "http://schemas.android.com/apk/res/android" == namespace) {
            //TODO parse namespaces better
            //workaround for a weird case that there is no namespace found: https://github.com/hsiafan/apk-parser/issues/122
            // Log.d("AppLog", "Got a weird namespace, so setting as empty (namespace isn't supposed to be a URL): " + attribute.getName());
            namespace = "android"
        }
        val rawValueRef = buffer.int
        val rawValue = if (rawValueRef > 0) stringPool!![rawValueRef] else null
        val resValue = ParseUtils.readResValue(buffer, stringPool)
        return Attribute(namespace, name!!, rawValue, resValue)
    }

    private fun readXmlNamespaceStartTag(): XmlNamespaceStartTag {
        val prefixRef = buffer.int
        val uriRef = buffer.int
        val prefix = if (prefixRef > 0) stringPool!![prefixRef] else null
        val uri = if (uriRef > 0) stringPool!![uriRef] else null
        return XmlNamespaceStartTag(prefix, uri)
    }

    private fun readXmlNamespaceEndTag(): XmlNamespaceEndTag {
        val prefixRef = buffer.int
        val prefix = if (prefixRef <= 0) null else stringPool!![prefixRef]
        val uriRef = buffer.int
        val uri = if (uriRef <= 0) null else stringPool!![uriRef]
        return XmlNamespaceEndTag(prefix, uri)
    }

    private fun readXmlResourceMap(chunkHeader: XmlResourceMapHeader): LongArray {
        val count = chunkHeader.bodySize / 4
        val resourceIds = LongArray(count)
        for (i in 0 until count) {
            resourceIds[i] = Buffers.readUInt(buffer)
        }
        return resourceIds
    }

    private fun readChunkHeader(): ChunkHeader? {
        // finished
        if (!buffer.hasRemaining()) {
            return null
        }
        val begin = buffer.position().toLong()
        val chunkType = Buffers.readUShort(buffer)
        val headerSize = Buffers.readUShort(buffer)
        val chunkSize = Buffers.readUInt(buffer)
        return when (chunkType) {
            ChunkType.XML -> XmlHeader(chunkType, headerSize, chunkSize)
            ChunkType.STRING_POOL -> {
                val stringPoolHeader = StringPoolHeader(headerSize, chunkSize)
                stringPoolHeader.setStringCount(Buffers.readUInt(buffer))
                stringPoolHeader.setStyleCount(Buffers.readUInt(buffer))
                stringPoolHeader.flags = Buffers.readUInt(buffer)
                stringPoolHeader.stringsStart = Buffers.readUInt(buffer)
                stringPoolHeader.stylesStart = Buffers.readUInt(buffer)
                Buffers.position(buffer, begin + headerSize)
                stringPoolHeader
            }

            ChunkType.XML_RESOURCE_MAP -> {
                Buffers.position(buffer, begin + headerSize)
                XmlResourceMapHeader(chunkType, headerSize, chunkSize)
            }

            ChunkType.XML_START_NAMESPACE, ChunkType.XML_END_NAMESPACE, ChunkType.XML_START_ELEMENT, ChunkType.XML_END_ELEMENT, ChunkType.XML_CDATA -> {
                val header = XmlNodeHeader(chunkType, headerSize, chunkSize)
                header.lineNum = Buffers.readUInt(buffer).toInt()
                header.commentRef = Buffers.readUInt(buffer).toInt()
                Buffers.position(buffer, begin + headerSize)
                header
            }

            ChunkType.NULL -> NullHeader(chunkType, headerSize, chunkSize)
            else -> throw ParserException("Unexpected chunk type:$chunkType")
        }
    }

    companion object {
        private val intAttributes: Set<String> = HashSet(
            Arrays.asList(
                "screenOrientation", "configChanges", "windowSoftInputMode",
                "launchMode", "installLocation", "protectionLevel"
            )
        )
    }
}