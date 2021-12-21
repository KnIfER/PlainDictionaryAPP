/*
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.jcodings.spi;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

/**
 * Implementation of Charset, CharsetDecoder, and CharsetEncoder for ISO-8859-16.
 */
public class ISO_8859_16 extends Charset {
    public static final ISO_8859_16 INSTANCE = new ISO_8859_16();

    ISO_8859_16() {
        super("ISO-8859-16", new String[]{"iso-ir-226", "ISO_8859-16:2001", "ISO_8859-16", "latin10", "l10", "csISO885916", "ISO8859_16", "ISO_8859_16", "8859_16", "ISO8859-16"});
    }

    @Override
    public boolean contains(Charset cs) {
        return cs.name().equals("US-ASCII") || cs instanceof ISO_8859_16;
    }

    @Override
    public CharsetDecoder newDecoder() {
        return new Decoder(this);
    }

    @Override
    public CharsetEncoder newEncoder() {
        return new Encoder(this);
    }

    private static class Decoder extends CharsetDecoder {
        Decoder(Charset charset) {
            super(charset, 1.0f, 1.0f);
        }

        @Override
        protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
            for (;;) {
                if (!in.hasRemaining()) return CoderResult.UNDERFLOW;
                if (!out.hasRemaining()) return CoderResult.OVERFLOW;
                int b = in.get() & 0xFF;
                char c = TABLE[b];
                out.put(c);
            }
        }

        private static final char[] TABLE;

        static {
            TABLE = new char[256];
            for (int i = 0; i < 256; i++) {
                TABLE[i] = (char)i;
            }
            TABLE[0xA1] = '\u0104';
            TABLE[0xA2] = '\u0105';
            TABLE[0xA3] = '\u0141';
            TABLE[0xA4] = '\u20AC';
            TABLE[0xA5] = '\u201E';
            TABLE[0xA6] = '\u0160';
            TABLE[0xA8] = '\u0161';
            TABLE[0xAA] = '\u0218';
            TABLE[0xAC] = '\u0179';
            TABLE[0xAE] = '\u017A';
            TABLE[0xAF] = '\u017B';

            TABLE[0xB2] = '\u010C';
            TABLE[0xB3] = '\u0142';
            TABLE[0xB4] = '\u017D';
            TABLE[0xB5] = '\u201D';
            TABLE[0xB8] = '\u017E';
            TABLE[0xB9] = '\u010D';
            TABLE[0xBA] = '\u0219';
            TABLE[0xBC] = '\u0152';
            TABLE[0xBD] = '\u0153';
            TABLE[0xBE] = '\u0178';
            TABLE[0xBF] = '\u017C';

            TABLE[0xC3] = '\u0102';
            TABLE[0xC5] = '\u0106';

            TABLE[0xD1] = '\u0110';
            TABLE[0xD2] = '\u0143';
            TABLE[0xD5] = '\u0150';
            TABLE[0xD7] = '\u015A';
            TABLE[0xD8] = '\u0170';
            TABLE[0xDD] = '\u0118';
            TABLE[0xDE] = '\u021A';

            TABLE[0xE3] = '\u0103';
            TABLE[0xE5] = '\u0107';
        }
    }

    private static class Encoder extends CharsetEncoder {
        Encoder(Charset charset) {
            super(charset, 1.0f, 1.0f, new byte[]{(byte)'?'});
        }

        @Override
        protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
            for (;;) {
                if (!in.hasRemaining()) return CoderResult.UNDERFLOW;
                if (!out.hasRemaining()) return CoderResult.OVERFLOW;
                char c = in.get();
                byte b = 0;
                byte[] replace = null;
                switch (c) {
                    case '\u0104': b = (byte)0xA1; break;
                    case '\u0105': b = (byte)0xA2; break;
                    case '\u0141': b = (byte)0xA3; break;
                    case '\u20AC': b = (byte)0xA4; break;
                    case '\u201E': b = (byte)0xA5; break;
                    case '\u0160': b = (byte)0xA6; break;
                    case '\u0161': b = (byte)0xA8; break;
                    case '\u0218': b = (byte)0xAA; break;
                    case '\u0179': b = (byte)0xAC; break;
                    case '\u017A': b = (byte)0xAE; break;
                    case '\u017B': b = (byte)0xAF; break;

                    case '\u010C': b = (byte)0xB2; break;
                    case '\u0142': b = (byte)0xB3; break;
                    case '\u017D': b = (byte)0xB4; break;
                    case '\u201D': b = (byte)0xB5; break;
                    case '\u017E': b = (byte)0xB8; break;
                    case '\u010D': b = (byte)0xB9; break;
                    case '\u0219': b = (byte)0xBA; break;
                    case '\u0152': b = (byte)0xBC; break;
                    case '\u0153': b = (byte)0xBD; break;
                    case '\u0178': b = (byte)0xBE; break;
                    case '\u017C': b = (byte)0xBF; break;

                    case '\u0102': b = (byte)0xC3; break;
                    case '\u0106': b = (byte)0xC5; break;

                    case '\u0110': b = (byte)0xD1; break;
                    case '\u0143': b = (byte)0xD2; break;
                    case '\u0150': b = (byte)0xD5; break;
                    case '\u015A': b = (byte)0xD7; break;
                    case '\u0170': b = (byte)0xD8; break;
                    case '\u0118': b = (byte)0xDD; break;
                    case '\u021A': b = (byte)0xDE; break;

                    case '\u0103': b = (byte)0xE3; break;
                    case '\u0107': b = (byte)0xE5; break;

                    default:
                        if (c < 256) b = (byte)c;
                        else replace = replacement();
                }

                if (replace != null) {
                    if (out.remaining() < replace.length) {
                        ((Buffer) in).position(in.position() - 1);
                        return CoderResult.OVERFLOW;
                    } else {
                        out.put(replace);
                    }
                } else {
                    out.put(b);
                }
            }
        }
    }
}
