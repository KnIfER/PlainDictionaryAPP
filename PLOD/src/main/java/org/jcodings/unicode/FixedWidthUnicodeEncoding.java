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
package org.jcodings.unicode;

import org.jcodings.IntHolder;
import org.jcodings.util.Macros;

public abstract class FixedWidthUnicodeEncoding extends UnicodeEncoding {
    protected final int shift;

    protected FixedWidthUnicodeEncoding(String name, int width) {
        super(name, width, width, null);
        shift = log2(width);
    }

    @Override
    public final int length(byte c) { 
        return minLength;       
    }

    @Override
    public int length(byte[] bytes, int p, int e) {
        if (e < p) {
            return Macros.CONSTRUCT_MBCLEN_INVALID();
        } else if (e-p < 4) {
            return Macros.CONSTRUCT_MBCLEN_NEEDMORE(4-e-p);
        } else {
            int c = mbcToCode(bytes, p, e);
            if (!Macros.UNICODE_VALID_CODEPOINT_P(c)) {
                return Macros.CONSTRUCT_MBCLEN_INVALID();
            }
            return Macros.CONSTRUCT_MBCLEN_CHARFOUND(4);
        }
    }

    @Override
    public final int strLength(byte[]bytes, int p, int end) {
        return (end - p) >>> shift;
    }

    @Override
    public final int strCodeAt(byte[]bytes, int p, int end, int index) {
        return mbcToCode(bytes, p + (index << shift), end);
    }

    @Override
    public final int codeToMbcLength(int code) {
        return minLength; 
    }

    /** onigenc_utf16_32_get_ctype_code_range
     */
    @Override
    public final int[]ctypeCodeRange(int ctype, IntHolder sbOut) {
        sbOut.value = 0x00;
        return super.ctypeCodeRange(ctype);
    }

    @Override
    public final int leftAdjustCharHead(byte[]bytes, int p, int s, int end) {
        if (s <= p) return s;
        return s - ((s - p) % maxLength);
    }    

    @Override
    public final boolean isReverseMatchAllowed(byte[]bytes, int p, int end) {
        return false;
    }

    private static int log2(int n){
        int log = 0;
        while ((n >>>= 1) != 0) log++;
        return log;
    }
}
