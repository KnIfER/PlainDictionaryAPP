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
package org.jcodings.constants;

import org.jcodings.util.CaseInsensitiveBytesHash;

public class PosixBracket{

    public static final byte[][] PBSNamesLower = {
        "alnum".getBytes(),
        "alpha".getBytes(),
        "blank".getBytes(),
        "cntrl".getBytes(),
        "digit".getBytes(),
        "graph".getBytes(),
        "lower".getBytes(),
        "print".getBytes(),
        "punct".getBytes(),
        "space".getBytes(),
        "upper".getBytes(),
        "xdigit".getBytes(),
        "ascii".getBytes(),
        "word".getBytes()
    };

    public static final int PBSValues[] = {
        CharacterType.ALNUM,
        CharacterType.ALPHA,
        CharacterType.BLANK,
        CharacterType.CNTRL,
        CharacterType.DIGIT,
        CharacterType.GRAPH,
        CharacterType.LOWER,
        CharacterType.PRINT,
        CharacterType.PUNCT,
        CharacterType.SPACE,
        CharacterType.UPPER,
        CharacterType.XDIGIT,
        CharacterType.ASCII,
        CharacterType.WORD,
    };

    public static final CaseInsensitiveBytesHash<Integer> PBSTableUpper = new CaseInsensitiveBytesHash<Integer>(PBSNamesLower.length + 5);

    static {
	    for (int i=0; i<PBSValues.length; i++) PBSTableUpper.put(PBSNamesLower[i], PBSValues[i]);
    }

}
