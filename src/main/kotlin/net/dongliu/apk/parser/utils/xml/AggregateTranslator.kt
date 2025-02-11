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
 * Executes a sequence of translators one after the other. Execution ends whenever
 * the first translator consumes codepoints from the input.
 */
internal class AggregateTranslator( private val translators: Array<CharSequenceTranslator>) : CharSequenceTranslator() {

    /**
     * Specify the translators to be used at creation time.
     *
     * @param translators CharSequenceTranslator array to aggregate
     */
//    constructor(vararg translators: CharSequenceTranslator): this(arrayOf(translators))

    /**
     * The first translator to consume codepoints from the input is the 'winner'.
     * Execution stops with the number of consumed codepoints being returned.
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    override fun translate(input: CharSequence, index: Int, out: Writer): Int {
        for (translator in translators) {
            val consumed = translator.translate(input, index, out)
            if (consumed != 0) {
                return consumed
            }
        }
        return 0
    }
}