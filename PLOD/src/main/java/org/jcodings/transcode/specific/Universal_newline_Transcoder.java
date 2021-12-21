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
import org.jcodings.transcode.Transcoding;

public class Universal_newline_Transcoder extends Transcoder {
    protected Universal_newline_Transcoder() {
        super("", "universal_newline", universal_newline, "Newline", 1, 1, 2, AsciiCompatibility.CONVERTER, 2);
    }

    private static final int universal_newline = Transcoding.WORDINDEX2INFO(1);

    public static final Transcoder INSTANCE = new Universal_newline_Transcoder();

    @Override
    public int stateInit(byte[] statep) {
        return TranscodeFunctions.universalNewlineInit(statep);
    }

    @Override
    public int stateFinish(byte[] state) {
        return TranscodeFunctions.universalNewlineInit(state);
    }

    @Override
    public int startToOutput(byte[] statep, byte[] s, int sStart, int l, byte[] o, int oStart, int oSize) {
        return TranscodeFunctions.funSoUniversalNewline(statep, s, sStart, l, o, oStart, oSize);
    }

    @Override
    public boolean hasFinish() {
        return true;
    }

    @Override
    public int finish(byte[] statep, byte[] p, int start, int size) {
        return TranscodeFunctions.universalNewlineFinish(statep, p, start, size);
    }

    @Override
    public int resetSize(byte[] statep) {
        return TranscodeFunctions.iso2022jpEncoderResetSequenceSize(statep);
    }

    @Override
    public int resetState(byte[] statep, byte[] p, int start, int size) {
        return TranscodeFunctions.finishIso2022jpEncoder(statep, p, start, size);
    }
}
