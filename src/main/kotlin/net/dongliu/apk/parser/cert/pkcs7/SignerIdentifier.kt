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

import net.dongliu.apk.parser.cert.asn1.Asn1Class
import net.dongliu.apk.parser.cert.asn1.Asn1Field
import net.dongliu.apk.parser.cert.asn1.Asn1Tagging
import net.dongliu.apk.parser.cert.asn1.Asn1Type
import java.nio.ByteBuffer

/**
 * PKCS #7 `SignerIdentifier` as specified in RFC 5652.
 */
@Asn1Class(type = Asn1Type.Choice)
class SignerIdentifier {
    @Asn1Field(type = Asn1Type.Sequence)
    var issuerAndSerialNumber: IssuerAndSerialNumber? = null

    @Asn1Field(type = Asn1Type.OctetString, tagging = Asn1Tagging.Implicit, tagNumber = 0)
    var subjectKeyIdentifier: ByteBuffer? = null

    constructor() {}
    constructor(issuerAndSerialNumber: IssuerAndSerialNumber?) {
        this.issuerAndSerialNumber = issuerAndSerialNumber
    }
}