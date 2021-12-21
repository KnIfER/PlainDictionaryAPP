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
package org.jcodings;

public final class CaseFoldCodeItem {
    public static final CaseFoldCodeItem[] EMPTY_FOLD_CODES = new CaseFoldCodeItem[]{};

    public final int byteLen;
    public final int code[];

    private CaseFoldCodeItem(int byteLen, int[]code) {
        this.byteLen = byteLen;
        this.code = code;
    }

    public static CaseFoldCodeItem create(int byteLen, int code1) {
        return new CaseFoldCodeItem(byteLen, new int[] {code1});
    }

    public static CaseFoldCodeItem create(int byteLen, int code1, int code2) {
        return new CaseFoldCodeItem(byteLen, new int[] {code1, code2});
    }

    public static CaseFoldCodeItem create(int byteLen, int code1, int code2, int code3) {
        return new CaseFoldCodeItem(byteLen, new int[] {code1, code2, code3});
    }
}
