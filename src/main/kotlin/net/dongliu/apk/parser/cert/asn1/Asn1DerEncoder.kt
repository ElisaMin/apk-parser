/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dongliu.apk.parser.cert.asn1

import net.dongliu.apk.parser.cert.asn1.ber.BerEncoding
import java.io.ByteArrayOutputStream
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.math.BigInteger
import java.nio.ByteBuffer
import java.util.*

/**
 * Encoder of ASN.1 structures into DER-encoded form.
 *
 *
 *
 * Structure is described to the encoder by providing a class annotated with [Asn1Class],
 * containing fields annotated with [Asn1Field].
 */
@Suppress("UNCHECKED_CAST")
object Asn1DerEncoder {
    /**
     * Returns the DER-encoded form of the provided ASN.1 structure.
     *
     * @param container container to be encoded. The container's class must meet the following
     * requirements:
     *
     *  * The class must be annotated with [Asn1Class].
     *  * Member fields of the class which are to be encoded must be annotated with
     * [Asn1Field] and be public.
     *
     * @throws Asn1EncodingException if the input could not be encoded
     */
    @Throws(Asn1EncodingException::class)
    fun encode(container: Any): ByteArray {
        val containerClass: Class<*> = container.javaClass
        val containerAnnotation = containerClass.getAnnotation(Asn1Class::class.java)
            ?: throw Asn1EncodingException(
                containerClass.name + " not annotated with " + Asn1Class::class.java.name
            )
        val containerType: Asn1Type = containerAnnotation.type
        return when (containerType) {
            Asn1Type.Choice -> toChoice(container)
            Asn1Type.Sequence -> toSequence(container)
            else -> throw Asn1EncodingException("Unsupported container type: $containerType")
        }
    }

    @Throws(Asn1EncodingException::class)
    private fun toChoice(container: Any): ByteArray {
        val containerClass: Class<*> = container.javaClass
        val fields = getAnnotatedFields(container)
        if (fields.isEmpty()) {
            throw Asn1EncodingException(
                "No fields annotated with " + Asn1Field::class.java.name
                        + " in CHOICE class " + containerClass.name
            )
        }
        var resultField: AnnotatedField? = null
        for (field in fields) {
            getMemberFieldValue(container, field.field)
            if (resultField != null) {
                throw Asn1EncodingException(
                    "Multiple non-null fields in CHOICE class " + containerClass.name
                            + ": " + resultField.field.name
                            + ", " + field.field.name
                )
            }
            resultField = field
        }
        if (resultField == null) {
            throw Asn1EncodingException(
                "No non-null fields in CHOICE class " + containerClass.name
            )
        }
        return resultField.toDer()
    }

    @Throws(Asn1EncodingException::class)
    private fun toSequence(container: Any): ByteArray {
        val containerClass: Class<*> = container.javaClass
        val fields = getAnnotatedFields(container)
        Collections.sort(
            fields, Comparator.comparingInt { f: AnnotatedField -> f.annotation.index }
        )
        if (fields.size > 1) {
            var lastField: AnnotatedField? = null
            for (field in fields) {
                if (lastField != null && lastField.annotation.index == field.annotation.index) {
                    throw Asn1EncodingException(
                        "Fields have the same index: " + containerClass.name
                                + "." + lastField.field.name
                                + " and ." + field.field.name
                    )
                }
                lastField = field
            }
        }
        val serializedFields: MutableList<ByteArray> = ArrayList(fields.size)
        for (field in fields) {
            val serializedField: ByteArray?
            serializedField = try {
                field.toDer()
            } catch (e: Asn1EncodingException) {
                throw Asn1EncodingException(
                    "Failed to encode " + containerClass.name
                            + "." + field.field.name,
                    e
                )
            }
            if (serializedField != null) {
                serializedFields.add(serializedField)
            }
        }
        return createTag(
            BerEncoding.TAG_CLASS_UNIVERSAL, true, BerEncoding.TAG_NUMBER_SEQUENCE,
            *serializedFields.toTypedArray()
        )
    }

    @Throws(Asn1EncodingException::class)
    private fun toSetOf(values: Collection<Any>, elementType: Asn1Type?): ByteArray {
        val serializedValues: MutableList<ByteArray> = ArrayList(values.size)
        for (value in values) {
            serializedValues.add(JavaToDerConverter.toDer(value, elementType, null))
        }
        if (serializedValues.size > 1) {
            Collections.sort(serializedValues, ByteArrayLexicographicComparator.INSTANCE)
        }
        return createTag(
            BerEncoding.TAG_CLASS_UNIVERSAL, true, BerEncoding.TAG_NUMBER_SET,
            *serializedValues.toTypedArray()
        )
    }

    @Throws(Asn1EncodingException::class)
    private fun getAnnotatedFields(container: Any): List<AnnotatedField> {
        val containerClass: Class<*> = container.javaClass
        val declaredFields = containerClass.declaredFields
        val result: MutableList<AnnotatedField> = ArrayList(declaredFields.size)
        for (field in declaredFields) {
            val annotation = field.getAnnotation(Asn1Field::class.java) ?: continue
            if (Modifier.isStatic(field.modifiers)) {
                throw Asn1EncodingException(
                    Asn1Field::class.java.name + " used on a static field: "
                            + containerClass.name + "." + field.name
                )
            }
            val annotatedField: AnnotatedField
            annotatedField = try {
                AnnotatedField(container, field, annotation)
            } catch (e: Asn1EncodingException) {
                throw Asn1EncodingException(
                    "Invalid ASN.1 annotation on "
                            + containerClass.name + "." + field.name,
                    e
                )
            }
            result.add(annotatedField)
        }
        return result
    }

    private fun toInteger(value: Int): ByteArray {
        return toInteger(value.toLong())
    }

    private fun toInteger(value: Long): ByteArray {
        return toInteger(BigInteger.valueOf(value))
    }

    private fun toInteger(value: BigInteger): ByteArray {
        return createTag(
            BerEncoding.TAG_CLASS_UNIVERSAL, false, BerEncoding.TAG_NUMBER_INTEGER,
            value.toByteArray()
        )
    }

    @Throws(Asn1EncodingException::class)
    private fun toOid(oid: String): ByteArray {
        val encodedValue = ByteArrayOutputStream()
        val nodes = oid.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (nodes.size < 2) {
            throw Asn1EncodingException(
                "OBJECT IDENTIFIER must contain at least two nodes: $oid"
            )
        }
        val firstNode: Int
        firstNode = try {
            nodes[0].toInt()
        } catch (e: NumberFormatException) {
            throw Asn1EncodingException("Node #1 not numeric: " + nodes[0])
        }
        if (firstNode > 6 || firstNode < 0) {
            throw Asn1EncodingException("Invalid value for node #1: $firstNode")
        }
        val secondNode: Int
        secondNode = try {
            nodes[1].toInt()
        } catch (e: NumberFormatException) {
            throw Asn1EncodingException("Node #2 not numeric: " + nodes[1])
        }
        if (secondNode >= 40 || secondNode < 0) {
            throw Asn1EncodingException("Invalid value for node #2: $secondNode")
        }
        val firstByte = firstNode * 40 + secondNode
        if (firstByte > 0xff) {
            throw Asn1EncodingException(
                "First two nodes out of range: $firstNode.$secondNode"
            )
        }
        encodedValue.write(firstByte)
        for (i in 2 until nodes.size) {
            val nodeString = nodes[i]
            val node: Int
            node = try {
                nodeString.toInt()
            } catch (e: NumberFormatException) {
                throw Asn1EncodingException("Node #" + (i + 1) + " not numeric: " + nodeString)
            }
            if (node < 0) {
                throw Asn1EncodingException("Invalid value for node #" + (i + 1) + ": " + node)
            }
            if (node <= 0x7f) {
                encodedValue.write(node)
                continue
            }
            if (node < 1 shl 14) {
                encodedValue.write(0x80 or (node shr 7))
                encodedValue.write(node and 0x7f)
                continue
            }
            if (node < 1 shl 21) {
                encodedValue.write(0x80 or (node shr 14))
                encodedValue.write(0x80 or (node shr 7 and 0x7f))
                encodedValue.write(node and 0x7f)
                continue
            }
            throw Asn1EncodingException("Node #" + (i + 1) + " too large: " + node)
        }
        return createTag(
            BerEncoding.TAG_CLASS_UNIVERSAL, false, BerEncoding.TAG_NUMBER_OBJECT_IDENTIFIER,
            encodedValue.toByteArray()
        )
    }

    @Throws(Asn1EncodingException::class)
    private fun getMemberFieldValue(obj: Any, field: Field): Any {
        return try {
            field[obj]
        } catch (e: ReflectiveOperationException) {
            throw Asn1EncodingException(
                "Failed to read " + obj.javaClass.name + "." + field.name, e
            )
        }
    }

    private fun createTag(
        tagClass: Int, constructed: Boolean, tagNumber: Int, vararg contents: ByteArray
    ): ByteArray {
        require(tagNumber < 0x1f) { "High tag numbers not supported: $tagNumber" }
        // tag class & number fit into the first byte
        val firstIdentifierByte = (tagClass shl 6 or (if (constructed) 1 shl 5 else 0) or tagNumber).toByte()
        var contentsLength = 0
        for (c in contents) {
            contentsLength += c.size
        }
        var contentsPosInResult: Int
        val result: ByteArray
        if (contentsLength < 0x80) {
            // Length fits into one byte
            contentsPosInResult = 2
            result = ByteArray(contentsPosInResult + contentsLength)
            result[0] = firstIdentifierByte
            result[1] = contentsLength.toByte()
        } else {
            // Length is represented as multiple bytes
            // The low 7 bits of the first byte represent the number of length bytes (following the
            // first byte) in which the length is in big-endian base-256 form
            if (contentsLength <= 0xff) {
                contentsPosInResult = 3
                result = ByteArray(contentsPosInResult + contentsLength)
                result[1] = 0x81.toByte()
                // 1 length byte
                result[2] = contentsLength.toByte()
            } else if (contentsLength <= 0xffff) {
                contentsPosInResult = 4
                result = ByteArray(contentsPosInResult + contentsLength)
                result[1] = 0x82.toByte()
                // 2 length bytes
                result[2] = (contentsLength shr 8).toByte()
                result[3] = (contentsLength and 0xff).toByte()
            } else if (contentsLength <= 0xffffff) {
                contentsPosInResult = 5
                result = ByteArray(contentsPosInResult + contentsLength)
                result[1] = 0x83.toByte()
                // 3 length bytes
                result[2] = (contentsLength shr 16).toByte()
                result[3] = (contentsLength shr 8 and 0xff).toByte()
                result[4] = (contentsLength and 0xff).toByte()
            } else {
                contentsPosInResult = 6
                result = ByteArray(contentsPosInResult + contentsLength)
                result[1] = 0x84.toByte()
                // 4 length bytes
                result[2] = (contentsLength shr 24).toByte()
                result[3] = (contentsLength shr 16 and 0xff).toByte()
                result[4] = (contentsLength shr 8 and 0xff).toByte()
                result[5] = (contentsLength and 0xff).toByte()
            }
            result[0] = firstIdentifierByte
        }
        for (c in contents) {
            System.arraycopy(c, 0, result, contentsPosInResult, c.size)
            contentsPosInResult += c.size
        }
        return result
    }

    /**
     * Compares two bytes arrays based on their lexicographic order. Corresponding elements of the
     * two arrays are compared in ascending order. Elements at out of range indices are assumed to
     * be smaller than the smallest possible value for an element.
     */
    private class ByteArrayLexicographicComparator : Comparator<ByteArray> {
        override fun compare(arr1: ByteArray, arr2: ByteArray): Int {
            val commonLength = Math.min(arr1.size, arr2.size)
            for (i in 0 until commonLength) {
                val diff = (arr1[i].toInt() and 0xff) - (arr2[i].toInt() and 0xff)
                if (diff != 0) {
                    return diff
                }
            }
            return arr1.size - arr2.size
        }

        companion object {
            val INSTANCE = ByteArrayLexicographicComparator()
        }
    }

    private class AnnotatedField(private val mObject: Any, val field: Field, val annotation: Asn1Field) {
        private val mDataType: Asn1Type
        private val mElementDataType: Asn1Type
        private val mDerTagClass: Int
        private val mDerTagNumber: Int
        private val mTagging: Asn1Tagging
        private val mOptional: Boolean

        init {
            mDataType = annotation.type
            mElementDataType = annotation.elementType
            var tagClass: Asn1TagClass = annotation.cls
            if (tagClass === Asn1TagClass.Automatic) {
                tagClass = if (annotation.tagNumber != -1) {
                    Asn1TagClass.ContextSpecific
                } else {
                    Asn1TagClass.Universal
                }
            }
            mDerTagClass = BerEncoding.getTagClass(tagClass)
            val tagNumber: Int
            if (annotation.tagNumber != -1) {
                tagNumber = annotation.tagNumber
            } else if (mDataType === Asn1Type.Choice || mDataType === Asn1Type.Any) {
                tagNumber = -1
            } else {
                tagNumber = BerEncoding.getTagNumber(mDataType)
            }
            mDerTagNumber = tagNumber
            mTagging = annotation.tagging
            if (mTagging === Asn1Tagging.Explicit || mTagging === Asn1Tagging.Implicit && annotation.tagNumber == -1) {
                throw Asn1EncodingException(
                    "Tag number must be specified when tagging mode is $mTagging"
                )
            }
            mOptional = annotation.optional
        }

        @Throws(Asn1EncodingException::class)
        fun toDer(): ByteArray {
            val fieldValue = getMemberFieldValue(mObject, field)
            val encoded = JavaToDerConverter.toDer(fieldValue, mDataType, mElementDataType)
            return when (mTagging) {
                Asn1Tagging.Normal -> encoded
                Asn1Tagging.Explicit -> createTag(mDerTagClass, true, mDerTagNumber, encoded)
                Asn1Tagging.Implicit -> {
                    val originalTagNumber = BerEncoding.getTagNumber(encoded[0])
                    if (originalTagNumber == 0x1f) {
                        throw Asn1EncodingException("High-tag-number form not supported")
                    }
                    if (mDerTagNumber >= 0x1f) {
                        throw Asn1EncodingException(
                            "Unsupported high tag number: $mDerTagNumber"
                        )
                    }
                    encoded[0] = BerEncoding.setTagNumber(encoded[0], mDerTagNumber)
                    encoded[0] = BerEncoding.setTagClass(encoded[0], mDerTagClass)
                    encoded
                }

            }
        }
    }

    private object JavaToDerConverter {
        @Throws(Asn1EncodingException::class)
        fun toDer(source: Any, targetType: Asn1Type?, targetElementType: Asn1Type?): ByteArray {
            val sourceType: Class<*> = source.javaClass
            if (Asn1OpaqueObject::class.java == sourceType) {
                val buf = (source as Asn1OpaqueObject).encoded
                val result = ByteArray(buf.remaining())
                buf[result]
                return result
            }
            if (targetType == null || targetType === Asn1Type.Any) {
                return encode(source)
            }
            when (targetType) {
                Asn1Type.OctetString -> {
                    var value: ByteArray? = null
                    if (source is ByteBuffer) {
                        val buf = source
                        value = ByteArray(buf.remaining())
                        buf.slice()[value]
                    } else if (source is ByteArray) {
                        value = source
                    }
                    if (value != null) {
                        return createTag(
                            BerEncoding.TAG_CLASS_UNIVERSAL,
                            false,
                            BerEncoding.TAG_NUMBER_OCTET_STRING,
                            value
                        )
                    }
                }

                Asn1Type.Integer -> when (source) {
                    is Int -> {
                        return toInteger(source)
                    }

                    is Long -> {
                        return toInteger(source)
                    }

                    is BigInteger -> {
                        return toInteger(source)
                    }
                }

                Asn1Type.ObjectIdentifier -> if (source is String) {
                    return toOid(source)
                }

                Asn1Type.Sequence -> {
                    val containerAnnotation = sourceType.getAnnotation(Asn1Class::class.java)
                    if (containerAnnotation != null && containerAnnotation.type === Asn1Type.Sequence) {
                        return toSequence(source)
                    }
                }

                Asn1Type.Choice -> {
                    val containerAnnotation = sourceType.getAnnotation(Asn1Class::class.java)
                    if (containerAnnotation != null && containerAnnotation.type === Asn1Type.Choice) {
                        return toChoice(source)
                    }
                }

                Asn1Type.SetOf -> return toSetOf(source as Collection<Any>, targetElementType)
                else -> {}
            }
            throw Asn1EncodingException(
                "Unsupported conversion: " + sourceType.name + " to ASN.1 " + targetType
            )
        }
    }
}