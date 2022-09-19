package net.dongliu.apk.parser.parser;

import net.dongliu.apk.parser.utils.Inputs;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.cert.CertificateException;
import java.util.Objects;

public class ApkSignBlockParserTest {

    @Test
    public void parse() throws IOException, CertificateException {
        byte[] bytes = Inputs.readAllAndClose(Objects.requireNonNull(getClass().getResourceAsStream("/sign/gmail_sign_block")));
        ApkSignBlockParser parser = new ApkSignBlockParser(ByteBuffer.wrap(bytes));
        parser.parse();
    }
}