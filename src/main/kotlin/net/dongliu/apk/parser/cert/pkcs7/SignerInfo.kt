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
package net.dongliu.apk.parser.cert.pkcs7

import net.dongliu.apk.parser.cert.asn1.*
import java.nio.ByteBuffer

/**
 * PKCS #7 `SignerInfo` as specified in RFC 5652.
 */
@Asn1Class(type = Asn1Type.Sequence)
class SignerInfo {
    @Asn1Field(index = 0, type = Asn1Type.Integer)
    var version = 0

    @Asn1Field(index = 1, type = Asn1Type.Choice)
    var sid: SignerIdentifier? = null

    @Asn1Field(index = 2, type = Asn1Type.Sequence)
    var digestAlgorithm: AlgorithmIdentifier? = null

    @Asn1Field(index = 3, type = Asn1Type.SetOf, tagging = Asn1Tagging.Implicit, tagNumber = 0, optional = true)
    var signedAttrs: Asn1OpaqueObject? = null

    @Asn1Field(index = 4, type = Asn1Type.Sequence)
    var signatureAlgorithm: AlgorithmIdentifier? = null

    @Asn1Field(index = 5, type = Asn1Type.OctetString)
    var signature: ByteBuffer? = null

    @Asn1Field(index = 6, type = Asn1Type.SetOf, tagging = Asn1Tagging.Implicit, tagNumber = 1, optional = true)
    var unsignedAttrs: List<Attribute>? = null
}