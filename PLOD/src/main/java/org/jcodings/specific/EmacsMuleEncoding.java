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

import org.jcodings.IntHolder;
import org.jcodings.MultiByteEncoding;
import org.jcodings.ascii.AsciiTables;
import org.jcodings.exception.ErrorCodes;

public final class EmacsMuleEncoding extends MultiByteEncoding {

    protected EmacsMuleEncoding() {
        super("Emacs-Mule", 1, 4, EmacsMuleEncLen, EmacsMuleTrans, AsciiTables.AsciiCtypeTable);
    }

    @Override
    public int length(byte[]bytes, int p, int end) {
        return safeLengthForUptoFour(bytes, p, end);
    }

    @Override
    public int mbcToCode(byte[]bytes, int p, int end) {
        return mbnMbcToCode(bytes, p, end);
    }

    @Override
    public int codeToMbcLength(int code) {
        // TODO: this won't work with signed values, probably need to move to long or use bitwise 'and' and masks
        if (isAscii(code)) {
            return 1;
        } else if (code > 0xffffffff) {
            return 0;
        } else if ((code & 0xff000000) >= 0x80000000) {
            return 4;
        } else if ((code & 0xff0000) >= 0x800000) {
            return 3;
        } else if ((code & 0xff00) >= 0x8000) {
            return 2;
        }
        return ErrorCodes.ERR_INVALID_CODE_POINT_VALUE;
    }

    @Override
    public int codeToMbc(int code, byte[]bytes, int p) {
        // TODO: check if mb4CodeToMbc is enough (too much code duplication in 1.9 sources)
        int p_ = p;
        if ((code & 0xff000000) != 0)           bytes[p_++] = (byte)((code >>> 24) & 0xff);
        if ((code & 0xff0000) != 0)             bytes[p_++] = (byte)((code >>> 16) & 0xff);
        if ((code & 0xff00) != 0 )              bytes[p_++] = (byte)((code >>> 8) & 0xff);
        bytes[p_++] = (byte)(code & 0xff);

        if (length(bytes, p, p_) != (p_ - p)) return ErrorCodes.ERR_INVALID_CODE_POINT_VALUE;
        return p_ - p;
    }

    @Override
    public int mbcCaseFold(int flag, byte[]bytes, IntHolder pp, int end, byte[]lower) {
        return mbnMbcCaseFold(flag, bytes, pp, end, lower);
    }

    @Override
    public boolean isCodeCType(int code, int ctype) {
        if (code < 128) {
            return isCodeCTypeInternal(code, ctype); // configured with ascii
        } else {
            return codeToMbcLength(code) > 1;
        }
    }

    @Override
    public int[]ctypeCodeRange(int ctype, IntHolder sbOut) {
        return null;
    }

    private static boolean islead(int c) {
        return c < 0x9d;
    }

    @Override
    public int leftAdjustCharHead(byte[] bytes, int p, int s, int end) {
        if (s <= p) return s;
        int p_ = s;
        while (!islead(bytes[p_] & 0xff) && p_ > p) p_--;
        return p_;
    };

    @Override
    public boolean isReverseMatchAllowed(byte[]bytes, int p, int end) {
        return true;
    }

    static final int EmacsMuleEncLen[] = {
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
        3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1
    };

    private static final int EmacsMuleTrans[][] = new int[][]{
        { /* S0   0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f */
          /* 0 */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* 1 */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* 2 */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* 3 */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* 4 */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* 5 */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* 6 */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* 7 */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* 8 */ F, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
          /* 9 */ 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 4, 4, 5, 6, F, F,
          /* a */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* b */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* c */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* d */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* e */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* f */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F
        },
        { /* S1   0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f */
          /* 0 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 1 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 2 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 3 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 4 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 5 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 6 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 7 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 8 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 9 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* a */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* b */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* c */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* d */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* e */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* f */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A
        },
        { /* S2   0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f */
          /* 0 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 1 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 2 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 3 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 4 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 5 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 6 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 7 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 8 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 9 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* a */ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
          /* b */ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
          /* c */ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
          /* d */ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
          /* e */ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
          /* f */ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1
        },
        { /* S3   0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f */
          /* 0 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 1 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 2 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 3 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 4 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 5 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 6 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 7 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 8 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 9 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* a */ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
          /* b */ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
          /* c */ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
          /* d */ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
          /* e */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* f */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F
        },
        { /* S4   0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f */
          /* 0 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 1 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 2 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 3 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 4 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 5 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 6 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 7 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 8 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 9 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* a */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* b */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* c */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* d */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* e */ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
          /* f */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F
        },
        { /* S5   0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f */
          /* 0 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 1 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 2 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 3 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 4 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 5 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 6 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 7 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 8 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 9 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* a */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* b */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* c */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* d */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* e */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* f */ 2, 2, 2, 2, 2, F, F, F, F, F, F, F, F, F, F, F
        },
        { /* S6   0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f */
          /* 0 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 1 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 2 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 3 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 4 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 5 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 6 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 7 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 8 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 9 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* a */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* b */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* c */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* d */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* e */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* f */ F, F, F, F, F, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, F
        },
    };

    public static final EmacsMuleEncoding INSTANCE = new EmacsMuleEncoding();
}
