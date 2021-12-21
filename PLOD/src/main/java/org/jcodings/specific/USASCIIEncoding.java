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
package org.jcodings.specific;

import org.jcodings.SingleByteEncoding;
import org.jcodings.ascii.AsciiTables;

public final class USASCIIEncoding extends SingleByteEncoding {

    protected USASCIIEncoding() {
        super("US-ASCII", AsciiTables.AsciiCtypeTable, AsciiTables.ToLowerCaseTable);
    }

    @Override
    public int length(byte[] bytes, int p, int end) {
        return (bytes[p] & 0x80) == 0 ? 1 : -1;
    }
    
    @Override
    public final byte[] toLowerCaseTable() {
        return LowerCaseTable;
    }

    @Override
    public String getCharsetName() {
        return "US-ASCII";
    }

    /** ascii_is_code_ctype / ONIGENC_IS_ASCII_CODE_CTYPE
     */
    @Override
    public boolean isCodeCType(int code, int ctype) {
        return code < 128 ? isCodeCTypeInternal(code, ctype) : false;
    }

    public static final USASCIIEncoding INSTANCE = new USASCIIEncoding();
}
