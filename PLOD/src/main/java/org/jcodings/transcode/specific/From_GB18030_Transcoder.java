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
package org.jcodings.transcode.specific;

import org.jcodings.transcode.AsciiCompatibility;
import org.jcodings.transcode.TranscodeFunctions;
import org.jcodings.transcode.Transcoder;

public class From_GB18030_Transcoder extends Transcoder {
    protected From_GB18030_Transcoder () {
        super("GB18030", "UTF-8", 57668, "Gb18030", 1, 4, 4, AsciiCompatibility.CONVERTER, 0);
    }

    public static final Transcoder INSTANCE = new From_GB18030_Transcoder();

    @Override
    public int startToOutput(byte[] statep, byte[] s, int sStart, int l, byte[] o, int oStart, int oSize) {
        return TranscodeFunctions.funSoFromGB18030(statep, s, sStart, l, o, oStart, oSize);
    }

    @Override
    public int startInfoToOutput(byte[] statep, byte[] s, int sStart, int l, int info, byte[] o, int oStart, int oSize) {
        return TranscodeFunctions.funSioFromGB18030(statep, s, sStart, l, info, o, oStart, oSize);
    }
}
