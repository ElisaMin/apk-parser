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
package net.dongliu.apk.parser.cert.asn1.ber

import net.dongliu.apk.parser.cert.asn1.Asn1TagClass
import net.dongliu.apk.parser.cert.asn1.Asn1Type

/**
 * ASN.1 Basic Encoding Rules (BER) constants and helper methods. See `X.690`.
 */
object BerEncoding {
    /**
     * Constructed vs primitive flag in the first identifier byte.
     */
    const val ID_FLAG_CONSTRUCTED_ENCODING = 1 shl 5

    /**
     * Tag class: UNIVERSAL
     */
    const val TAG_CLASS_UNIVERSAL = 0

    /**
     * Tag class: APPLICATION
     */
    const val TAG_CLASS_APPLICATION = 1

    /**
     * Tag class: CONTEXT SPECIFIC
     */
    const val TAG_CLASS_CONTEXT_SPECIFIC = 2

    /**
     * Tag class: PRIVATE
     */
    const val TAG_CLASS_PRIVATE = 3

    /**
     * Tag number: INTEGER
     */
    const val TAG_NUMBER_INTEGER = 0x2

    /**
     * Tag number: OCTET STRING
     */
    const val TAG_NUMBER_OCTET_STRING = 0x4

    /**
     * Tag number: NULL
     */
    const val TAG_NUMBER_NULL = 0x05

    /**
     * Tag number: OBJECT IDENTIFIER
     */
    const val TAG_NUMBER_OBJECT_IDENTIFIER = 0x6

    /**
     * Tag number: SEQUENCE
     */
    const val TAG_NUMBER_SEQUENCE = 0x10

    /**
     * Tag number: SET
     */
    const val TAG_NUMBER_SET = 0x11
    fun getTagNumber(dataType: Asn1Type): Int {
        return when (dataType) {
            Asn1Type.Integer -> TAG_NUMBER_INTEGER
            Asn1Type.ObjectIdentifier -> TAG_NUMBER_OBJECT_IDENTIFIER
            Asn1Type.OctetString -> TAG_NUMBER_OCTET_STRING
            Asn1Type.SetOf -> TAG_NUMBER_SET
            Asn1Type.Sequence, Asn1Type.SequenceOf -> TAG_NUMBER_SEQUENCE
            else -> throw IllegalArgumentException("Unsupported data type: $dataType")
        }
    }

    fun getTagClass(tagClass: Asn1TagClass): Int {
        return when (tagClass) {
            Asn1TagClass.Application -> TAG_CLASS_APPLICATION
            Asn1TagClass.ContextSpecific -> TAG_CLASS_CONTEXT_SPECIFIC
            Asn1TagClass.Private -> TAG_CLASS_PRIVATE
            Asn1TagClass.Universal -> TAG_CLASS_UNIVERSAL
            else -> throw IllegalArgumentException("Unsupported tag class: $tagClass")
        }
    }

    fun tagClassToString(typeClass: Int): String {
        return when (typeClass) {
            TAG_CLASS_APPLICATION -> "APPLICATION"
            TAG_CLASS_CONTEXT_SPECIFIC -> ""
            TAG_CLASS_PRIVATE -> "PRIVATE"
            TAG_CLASS_UNIVERSAL -> "UNIVERSAL"
            else -> throw IllegalArgumentException("Unsupported type class: $typeClass")
        }
    }

    fun tagClassAndNumberToString(tagClass: Int, tagNumber: Int): String {
        val classString = tagClassToString(tagClass)
        val numberString = tagNumberToString(tagNumber)
        return if (classString.isEmpty()) numberString else "$classString $numberString"
    }

    fun tagNumberToString(tagNumber: Int): String {
        return when (tagNumber) {
            TAG_NUMBER_INTEGER -> "INTEGER"
            TAG_NUMBER_OCTET_STRING -> "OCTET STRING"
            TAG_NUMBER_NULL -> "NULL"
            TAG_NUMBER_OBJECT_IDENTIFIER -> "OBJECT IDENTIFIER"
            TAG_NUMBER_SEQUENCE -> "SEQUENCE"
            TAG_NUMBER_SET -> "SET"
            else -> "0x" + Integer.toHexString(tagNumber)
        }
    }

    /**
     * Returns `true` if the provided first identifier byte indicates that the data value uses
     * constructed encoding for its contents, or `false` if the data value uses primitive
     * encoding for its contents.
     */
    fun isConstructed(firstIdentifierByte: Byte): Boolean {
        return firstIdentifierByte.toInt() and ID_FLAG_CONSTRUCTED_ENCODING != 0
    }

    /**
     * Returns the tag class encoded in the provided first identifier byte. See `TAG_CLASS`
     * constants.
     */
    fun getTagClass(firstIdentifierByte: Byte): Int {
        return firstIdentifierByte.toInt() and 0xff shr 6
    }

    fun setTagClass(firstIdentifierByte: Byte, tagClass: Int): Byte {
        return (firstIdentifierByte.toInt() and 0x3f or (tagClass shl 6)).toByte()
    }

    /**
     * Returns the tag number encoded in the provided first identifier byte. See `TAG_NUMBER`
     * constants.
     */
    fun getTagNumber(firstIdentifierByte: Byte): Int {
        return firstIdentifierByte.toInt() and 0x1f
    }

    fun setTagNumber(firstIdentifierByte: Byte, tagNumber: Int): Byte {
        return (firstIdentifierByte.toInt() and 0x1f.inv() or tagNumber).toByte()
    }
}