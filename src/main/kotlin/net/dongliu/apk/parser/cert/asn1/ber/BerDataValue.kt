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

import java.nio.ByteBuffer

/**
 * ASN.1 Basic Encoding Rules (BER) data value -- see `X.690`.
 */
class BerDataValue internal constructor(
    private val encodedSource: ByteBuffer,
    private val encodedContentsSource: ByteBuffer,
    /**
     * Returns the tag class of this data value. See [BerEncoding] `TAG_CLASS`
     * constants.
     */
    val tagClass: Int,
    /**
     * Returns `true` if the content octets of this data value are the complete BER encoding
     * of one or more data values, `false` if the content octets of this data value directly
     * represent the value.
     */
    val isConstructed: Boolean,
    /**
     * Returns the tag number of this data value. See [BerEncoding] `TAG_NUMBER`
     * constants.
     */
    val tagNumber: Int
) {
    /**
     * Returns the encoded form of this data value.
     */
    val encoded get() = encodedSource.slice()

    /**
     * Returns the encoded contents of this data value.
     */
    val encodedContents get() = encodedContentsSource.slice()

    /**
     * Returns a new reader of the contents of this data value.
     */
    fun contentsReader(): BerDataValueReader {
        return ByteBufferBerDataValueReader(encodedContents)
    }

    /**
     * Returns a new reader which returns just this data value. This may be useful for re-reading
     * this value in different contexts.
     */
    fun dataValueReader(): BerDataValueReader {
        return ParsedValueReader(this)
    }

    private class ParsedValueReader(private val mValue: BerDataValue) : BerDataValueReader {
        private var mValueOutput = false
        override fun readDataValue(): BerDataValue? {
            if (mValueOutput) {
                return null
            }
            mValueOutput = true
            return mValue
        }
    }
}