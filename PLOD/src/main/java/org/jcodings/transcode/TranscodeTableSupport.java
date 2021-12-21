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

package org.jcodings.transcode;


public class TranscodeTableSupport implements TranscodingInstruction {
    public static final int WORDINDEX_SHIFT_BITS = 2;

    public static int WORDINDEX2INFO(int widx) {
        return widx << WORDINDEX_SHIFT_BITS;
    }

    public static int INFO2WORDINDEX(int info) {
        return info >>> WORDINDEX_SHIFT_BITS;
    }

    public static int makeSTR1(int bi) {
        return (bi << 6) | STR1;
    }

    public static byte makeSTR1LEN(int len) {
        return (byte)(len - 4);
    }

    public static int o1(int b1) {
        return (b1 << 8) | ONEbt;
    }

    public static int o2(int b1, int b2) {
        return (b1 << 8) | (b2 << 16) | TWObt;
    }

    /*
     * 0xffffffff mask is dead code here, but no need to use longs since we can mask against 0xffffffffL in the clients
     * to simulate unsigned ints on jvm
     */

    public static int o3(int b1, int b2, int b3) {
        return ((b1 << 8) | (b2 << 16) | (b3 << 24) | THREEbt) & 0xffffffff;
    }

    public static int o4(int b0, int b1, int b2, int b3) {
        return ((b1 << 8) | (b2 << 16) | (b3 << 24) | ((b0 & 0x07) << 5) | FOURbt) & 0xffffffff;
    }

    public static int g4(int b0, int b1, int b2, int b3) {
        return ((b0 << 8) | (b2 << 16) | ((b1 & 0xf) << 24) | ((b3 & 0x0f) << 28) | GB4bt) & 0xffffffff;
    }

    public static int funsio(int diff) {
        return (diff << 8) & FUNsio;
    }

    public static int getBT1(int a) {
        return a >>> 8;
    }

    public static int getBT2(int a) {
        return a >>> 16;
    }

    public static int getBT3(int a) {
        return a >>> 24;
    }

    public static int getBT0(int a) {
        return ((a >>> 5) & 0x07) | 0xf0; /* for UTF-8 only */
    }

    public static int o2FUNii(int b1, int b2) {
        return (b1 << 8) | (b2 << 16) | FUNii;
    }
}