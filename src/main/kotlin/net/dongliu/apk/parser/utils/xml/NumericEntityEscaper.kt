/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dongliu.apk.parser.utils.xml

import java.io.IOException
import java.io.Writer

/**
 * Translates codepoints to their XML numeric entity escaped value.
 */
internal class NumericEntityEscaper
/**
 *
 * Constructs a `NumericEntityEscaper` for the specified range. This is
 * the underlying method for the other constructors/builders. The `below`
 * and `above` boundaries are inclusive when `between` is
 * `true` and exclusive when it is `false`.
 *
 * @param below   int value representing the lowest codepoint boundary
 * @param above   int value representing the highest codepoint boundary
 * @param between whether to escape between the boundaries or outside them
 */ private constructor(private val below: Int, private val above: Int, private val between: Boolean) :
    CodePointTranslator() {
    /**
     *
     * Constructs a `NumericEntityEscaper` for all characters.
     */
    constructor() : this(0, Int.MAX_VALUE, true) {}

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    override fun translate(codepoint: Int, out: Writer): Boolean {
        if (between) {
            if (codepoint < below || codepoint > above) {
                return false
            }
        } else {
            if (codepoint >= below && codepoint <= above) {
                return false
            }
        }
        out.write("&#")
        out.write(Integer.toString(codepoint, 10))
        out.write(';'.code)
        return true
    }

    companion object {
        /**
         *
         * Constructs a `NumericEntityEscaper` below the specified value (exclusive).
         *
         * @param codepoint below which to escape
         * @return the newly created `NumericEntityEscaper` instance
         */
        fun below(codepoint: Int): NumericEntityEscaper {
            return outsideOf(codepoint, Int.MAX_VALUE)
        }

        /**
         *
         * Constructs a `NumericEntityEscaper` above the specified value (exclusive).
         *
         * @param codepoint above which to escape
         * @return the newly created `NumericEntityEscaper` instance
         */
        fun above(codepoint: Int): NumericEntityEscaper {
            return outsideOf(0, codepoint)
        }

        /**
         *
         * Constructs a `NumericEntityEscaper` between the specified values (inclusive).
         *
         * @param codepointLow  above which to escape
         * @param codepointHigh below which to escape
         * @return the newly created `NumericEntityEscaper` instance
         */
        fun between(codepointLow: Int, codepointHigh: Int): NumericEntityEscaper {
            return NumericEntityEscaper(codepointLow, codepointHigh, true)
        }

        /**
         *
         * Constructs a `NumericEntityEscaper` outside of the specified values (exclusive).
         *
         * @param codepointLow  below which to escape
         * @param codepointHigh above which to escape
         * @return the newly created `NumericEntityEscaper` instance
         */
        fun outsideOf(codepointLow: Int, codepointHigh: Int): NumericEntityEscaper {
            return NumericEntityEscaper(codepointLow, codepointHigh, false)
        }
    }
}