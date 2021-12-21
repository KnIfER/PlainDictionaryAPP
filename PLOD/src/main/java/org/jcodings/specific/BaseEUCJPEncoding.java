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

import org.jcodings.CodeRange;
import org.jcodings.EucEncoding;
import org.jcodings.IntHolder;
import org.jcodings.ascii.AsciiTables;
import org.jcodings.constants.CharacterType;
import org.jcodings.exception.ErrorCodes;
import org.jcodings.exception.ErrorMessages;
import org.jcodings.exception.InternalException;
import org.jcodings.util.CaseInsensitiveBytesHash;

abstract class BaseEUCJPEncoding extends EucEncoding {

    protected BaseEUCJPEncoding(int[][]Trans) {
        super("EUC-JP", 1, 3, EUCJPEncLen, Trans, AsciiTables.AsciiCtypeTable);
    }

    @Override
    public int mbcToCode(byte[]bytes, int p, int end) {
        return mbnMbcToCode(bytes, p, end);
    }

    @Override
    public int codeToMbcLength(int code) {
        if (isAscii(code)) return 1;
        if (code > 0x00ffffff) {
            return ErrorCodes.ERR_TOO_BIG_WIDE_CHAR_VALUE;
        }
        else if ((code & 0xff808080) == 0x00808080) return 3;
        else if ((code & 0xffff8080) == 0x00008080) return 2;
        return ErrorCodes.ERR_INVALID_CODE_POINT_VALUE;
    }

    @Override
    public int codeToMbc(int code, byte[]bytes, int p) {
        int p_ = p;
        if ((code & 0xff0000) != 0) bytes[p_++] = (byte)((code >> 16) & 0xff); // need mask here ??
        if ((code &   0xff00) != 0) bytes[p_++] = (byte)((code >>  8) & 0xff);
        bytes[p_++] = (byte)(code & 0xff);

        if (length(bytes, p, p_) != p_ - p) return ErrorCodes.ERR_INVALID_CODE_POINT_VALUE;
        return p_ - p;
    }

    private static int getLowerCase(int code) {
        if (isInRange(code, 0xa3c1, 0xa3da)) {
            return code + 0x0020;
        } else if (isInRange(code, 0xa6a1, 0xa6b8)) {
            return code + 0x0020;
        } else if (isInRange(code, 0xa7a1, 0xa7c1)) {
            return code + 0x0030;
        }
        return code;
    }

    @Override
    public int mbcCaseFold(int flag, byte[]bytes, IntHolder pp, int end, byte[]lower) {
        int p = pp.value;
        int lowerP = 0;

        if (isMbcAscii(bytes[p])) {
            lower[lowerP] = AsciiTables.ToLowerCaseTable[bytes[p] & 0xff];
            pp.value++;
            return 1;
        } else {
            //int len = length(bytes, p, end);
            int code = getLowerCase(mbcToCode(bytes, pp.value, end));
            int len = codeToMbc(code, lower, lowerP);
            if (len == ErrorCodes.ERR_INVALID_CODE_POINT_VALUE) len = 1;
            pp.value += len;
            return len; /* return byte length of converted char to lower */
        }
    }

    protected boolean isLead(int c) {
        return ((c - 0xa1) & 0xff) > 0xfe - 0xa1;
    }

    @Override
    public boolean isReverseMatchAllowed(byte[]bytes, int p, int end) {
        int c = bytes[p] & 0xff;
        return c <= 0x7e || c == 0x8e || c == 0x8f;
    }

    private static final int CR_Hiragana[] = {
        1,
        0xa4a1, 0xa4f3
    };

    private static final int CR_Katakana[] = {
        3,
        0x8ea6, 0x8eaf,   /* JIS X 0201 Katakana */
        0x8eb1, 0x8edd,   /* JIS X 0201 Katakana */
        0xa5a1, 0xa5f6,
    };

    private static final int CR_Han[] = {
        /* EUC-JP (JIS X 0208 based) */
        4,
        0xa1b8, 0xa1b8,
        0xb0a1, 0xcfd3,       /* Kanji level 1 */
        0xd0a1, 0xf4a6,       /* Kanji level 2 */
        0x8fb0a1, 0x8fedf3    /* JIS X 0212 Supplemental Kanji (row 16 .. 77) */
    };

    private static final int CR_Latin[] = {
        4,
        0x0041, 0x005a,
        0x0061, 0x007a,
        0xa3c1, 0xa3da,
        0xa3e1, 0xa3fa,
    };

    private static final int CR_Greek[] = {
        2,
        0xa6a1, 0xa6b8,
        0xa6c1, 0xa6d8,
    };

    private static final int CR_Cyrillic[] = {
        2,
        0xa7a1, 0xa7c1,
        0xa7d1, 0xa7f1,
    };

    private static final int PropertyList[][] = new int[][] {
        CR_Hiragana,
        CR_Katakana,
        CR_Han,
        CR_Latin,
        CR_Greek,
        CR_Cyrillic
    };

    private static final CaseInsensitiveBytesHash<Integer> CTypeNameHash = new CaseInsensitiveBytesHash<Integer>();

    static {
        String[] names = new String[] {"Hiragana", "Katakana", "Han", "Latin", "Greek", "Cyrillic"};
        for (int i = 0; i < names.length; i++) {
            CTypeNameHash.put(names[i].getBytes(), i + 1 + CharacterType.MAX_STD_CTYPE);
        }
    }

    @Override
    public int propertyNameToCType(byte[]bytes, int p, int end) {
        Integer ctype;
        if ((ctype = CTypeNameHash.get(bytes, p, end)) == null) {
            return super.propertyNameToCType(bytes, p, end);
        }
        return ctype;
    }

    @Override
    public boolean isCodeCType(int code, int ctype) {
        if (ctype <= CharacterType.MAX_STD_CTYPE) {
            if (code < 128) {
                // ctype table is configured with ASCII
                return isCodeCTypeInternal(code, ctype);
            } else {
                if (isWordGraphPrint(ctype)) {
                    return codeToMbcLength(code) > 1;
                }
            }
        } else {
            ctype -= (CharacterType.MAX_STD_CTYPE + 1);
            if (ctype >= PropertyList.length) throw new InternalException(ErrorMessages.ERR_TYPE_BUG);
            return CodeRange.isInCodeRange(PropertyList[ctype], code);
        }
        return false;
    }

    @Override
    public int[]ctypeCodeRange(int ctype, IntHolder sbOut) {
        if (ctype <= CharacterType.MAX_STD_CTYPE) {
            return null;
        } else {
            sbOut.value = 0x80;

            ctype -= (CharacterType.MAX_STD_CTYPE + 1);
            if (ctype >= PropertyList.length) throw new InternalException(ErrorMessages.ERR_TYPE_BUG);
            return PropertyList[ctype];
        }
    }

    static final int EUCJPEncLen[] = {
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 3,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
        2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
        2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
        2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
        2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
        2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1
    };
}
