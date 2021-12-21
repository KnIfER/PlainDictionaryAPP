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

public abstract class CanBeTrailTableEncoding extends MultiByteEncoding {

    protected final boolean[] CanBeTrailTable;

    protected CanBeTrailTableEncoding(String name, int minLength, int maxLength, int[]EncLen, int[][]Trans, short[]CTypeTable, boolean[]CanBeTrailTable) {
        super(name, minLength, maxLength, EncLen, Trans, CTypeTable);
        this.CanBeTrailTable = CanBeTrailTable;
    }

    @Override
    public int leftAdjustCharHead(byte[]bytes, int p, int s, int end) {
        if (s <= p) return s;

        int p_ = s;

        if (CanBeTrailTable[bytes[p_] & 0xff]) {
            while (p_ > p) {
                if (!(EncLen[bytes[--p_] & 0xff] > 1)) {
                    p_++;
                    break;
                }
            }
        }
        int len = length(bytes, p_, end);
        if (p_ + len > s) return p_;
        p_ += len;
        return p_ + ((s - p_) & ~1);
    }

    @Override
    public boolean isReverseMatchAllowed(byte[]bytes, int p, int end) {
        return !CanBeTrailTable[bytes[p] & 0xff];
    }
}