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

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

@Target(AnnotationTarget.FIELD)
@Retention(RetentionPolicy.RUNTIME)
annotation class Asn1Field(
    /**
     * Index used to order fields in a container. Required for fields of SEQUENCE containers.
     */
    val index: Int = 0, val cls: Asn1TagClass = Asn1TagClass.Automatic, val type: Asn1Type,
    /**
     * Tagging mode. Default: NORMAL.
     */
    val tagging: Asn1Tagging = Asn1Tagging.Normal,
    /**
     * Tag number. Required when IMPLICIT and EXPLICIT tagging mode is used.
     */
    val tagNumber: Int = -1,
    /**
     * `true` if this field is optional. Ignored for fields of CHOICE containers.
     */
    val optional: Boolean = false,
    /**
     * Type of elements. Used only for SET_OF or SEQUENCE_OF.
     */
    val elementType: Asn1Type = Asn1Type.Any
)