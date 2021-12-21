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

import org.jcodings.CanBeTrailTableEncoding;
import org.jcodings.CodeRange;
import org.jcodings.IntHolder;
import org.jcodings.ascii.AsciiTables;
import org.jcodings.constants.CharacterType;
import org.jcodings.exception.ErrorCodes;
import org.jcodings.exception.ErrorMessages;
import org.jcodings.exception.InternalException;
import org.jcodings.util.BytesHash;

abstract class BaseSJISEncoding extends CanBeTrailTableEncoding {

    protected BaseSJISEncoding(String name, int[][]Trans) {
        super(name, 1, 2, SjisEncLen, Trans, AsciiTables.AsciiCtypeTable, SJIS_CAN_BE_TRAIL_TABLE);
    }

    @Override
    public String getCharsetName() {
        return "windows-31j";
    }

    @Override
    public int mbcToCode(byte[]bytes, int p, int end) {
        return mbnMbcToCode(bytes, p, end);
    }

    @Override
    public int codeToMbcLength(int code) {
        if (code < 256) {
            return SjisEncLen[code] == 1 ? 1 : ErrorCodes.ERR_INVALID_CODE_POINT_VALUE;
        } else if (code <= 0xffff) {
            int low = code & 0xff;
            if (!SJIS_ISMB_TRAIL(low)) {
                return ErrorCodes.ERR_INVALID_CODE_POINT_VALUE;
            }
            return 2;
        } else {
            return ErrorCodes.ERR_INVALID_CODE_POINT_VALUE;
        }
    }

    private static boolean SJIS_ISMB_TRAIL(int code) {
        return SJIS_CAN_BE_TRAIL_TABLE[code];
    }

    @Override
    public int codeToMbc(int code, byte[]bytes, int p) {
        int p_ = p;
        if ((code & 0xff00) != 0) bytes[p_++] = (byte)(((code >>  8) & 0xff));
        bytes[p_++] = (byte)(code & 0xff);
        return p_ - p;
    }

    private static int getLowerCase(int code) {
        if (isInRange(code, 0x8260, 0x8279)) {
            return code + 0x0021;
        } else if (isInRange(code, 0x839f, 0x83b6)) {
            return code + 0x0020;
        } else if (isInRange(code, 0x8440, 0x8460)) {
            int d = code >= 0x844f ? 1 : 0;
            return code + (0x0030 + d);
        }
        return code;
    }

    @Override
    public int mbcCaseFold(int flag, byte[]bytes, IntHolder pp, int end, byte[]lower) {
        if (isAscii(bytes[pp.value])) {
            return asciiMbcCaseFold(flag, bytes, pp, end, lower);
        } else {
            int lowerP = 0;
            int code = getLowerCase(mbcToCode(bytes, pp.value, end));
            int len = codeToMbc(code, lower, lowerP);
            pp.value += len;
            return len;
        }
    }

    private static final int CR_Hiragana[] = {
        1,
        0x829f, 0x82f1
    }; /* CR_Hiragana */

    private static final int CR_Katakana[] = {
        4,
        0x00a6, 0x00af,
        0x00b1, 0x00dd,
        0x8340, 0x837e,
        0x8380, 0x8396,
    }; /* CR_Katakana */

    private static final int PropertyList[][] = new int[][] {
        CR_Hiragana,
        CR_Katakana
    };

    private static final BytesHash<Integer> CTypeNameHash = new BytesHash<Integer>();

    static {
        CTypeNameHash.put("Hiragana".getBytes(), 1 + CharacterType.MAX_STD_CTYPE);
        CTypeNameHash.put("Katakana".getBytes(), 2 + CharacterType.MAX_STD_CTYPE);
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
                    return true;
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

    static final boolean SJIS_CAN_BE_TRAIL_TABLE[] = {
        false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
        false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
        false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
        false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true,
        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true,
        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true,
        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false,
        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true,
        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true,
        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true,
        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true,
        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true,
        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true,
        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true,
        true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false
    };

    static final int SjisEncLen[] = {
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
        2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
        2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1
    };
}
