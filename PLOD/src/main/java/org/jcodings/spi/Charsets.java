package org.jcodings.spi;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * An SPI character set provider for ISO-8859-16.
 */
public class Charsets extends java.nio.charset.spi.CharsetProvider {
    private static final List<Charset> charsets = Collections.singletonList((Charset) ISO_8859_16.INSTANCE);
    public Iterator<Charset> charsets() {
        return charsets.iterator();
    }

    public Charset charsetForName(String charsetName) {
        if ("ISO-8859-16".equals(charsetName) || ISO_8859_16.INSTANCE.aliases().contains(charsetName)) {
            return ISO_8859_16.INSTANCE;
        }
        return null;
    }
}
