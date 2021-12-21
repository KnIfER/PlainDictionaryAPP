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
package org.jcodings.transcode;

import org.jcodings.exception.ErrorMessages;
import org.jcodings.exception.InternalException;
import org.jcodings.util.ArrayReader;
import org.jcodings.util.BytesHash;
import org.jcodings.util.ObjHash;

public abstract class Transcoder implements TranscodingInstruction {

    protected Transcoder(byte[] source, byte[] destination, int treeStart, String arrayKey, int inputUnitLength, int maxInput, int maxOutput,
            AsciiCompatibility compatibility, int stateSize) {
        this.source = source;
        this.destination = destination;
        this.hashCode = BytesHash.hashCode(this.source, 0, this.source.length);

        this.treeStart = treeStart;

        byte[] bytes = byteArrayHash.get(arrayKey);
        if (bytes == null) byteArrayHash.put(arrayKey, bytes = ArrayReader.readByteArray("Transcoder_" + arrayKey + "_ByteArray"));
        this.byteArray = bytes;

        int[] ints = wordArrayHash.get(arrayKey);
        if (ints == null) wordArrayHash.put(arrayKey, ints = ArrayReader.readIntArray("Transcoder_" + arrayKey + "_WordArray"));
        this.intArray = ints;

        this.inputUnitLength = inputUnitLength;
        this.maxInput = maxInput;
        this.maxOutput = maxOutput;
        this.compatibility = compatibility;
        this.stateSize = stateSize;
    }

    protected Transcoder(String source, String destination, int treeStart, String arrayKey, int inputUnitLength, int maxInput, int maxOutput,
            AsciiCompatibility compatibility, int stateSize) {
        this(source.getBytes(), destination.getBytes(), treeStart, arrayKey, inputUnitLength, maxInput, maxOutput, compatibility, stateSize);
    }

    public byte[] getSource() {
        return source;
    }

    public byte[] getDestination() {
        return destination;
    }

    final byte[] source, destination;
    final int hashCode;

    final int treeStart;

    final byte[] byteArray;
    final int[] intArray;

    // static final int wordSize = 4;
    public final int inputUnitLength, maxInput, maxOutput;

    public final AsciiCompatibility compatibility;

    final int stateSize;

    public boolean hasStateInit() {
        return false;
    }

    public int stateInit(byte[] statep) {
        return 0;
    }

    public int stateFinish(byte[] stateFinish) {
        return 0;
    }

    public int infoToInfo(byte[] statep, int o) {
        throw new RuntimeException("unimplemented infoToInfo needed in " + this);
    }

    public int startToInfo(byte[] statep, byte[] s, int sStart, int l) {
        throw new RuntimeException("unimplemented startToInfo needed in " + this);
    }

    public int infoToOutput(byte[] statep, int nextInfo, byte[] p, int start, int size) {
        throw new RuntimeException("unimplemented intoToOutput needed in " + this);
    }

    public boolean hasFinish() {
        return false;
    }

    public int finish(byte[] statep, byte[] p, int start, int size) {
        return 0;
    }

    public int resetSize(byte[] statep) {
        return 0;
    }

    public int resetState(byte[] statep, byte[] p, int start, int size) {
        return 0;
    }

    public int startToOutput(byte[] statep, byte[] s, int sStart, int l, byte[] o, int oStart, int oSize) {
        throw new RuntimeException("unimplemented startToOutput needed in " + this);
    }

    public int startInfoToOutput(byte[] statep, byte[] s, int sStart, int l, int info, byte[] o, int oStart, int oSize) {
        throw new RuntimeException("unimplemented startInfoToOutput needed in " + this);
    }

    /* rb_transcoding_open_by_transcoder */
    public final Transcoding transcoding(int flags) {
        Transcoding tc = new Transcoding(this, flags);

        if (hasStateInit()) {
            stateInit(tc.state);
        }

        return tc;
    }

    public static Transcoder load(String name) {
        String encClassName = name;

        Class<?> encClass;
        try {
            encClass = Class.forName(encClassName);
        } catch (ClassNotFoundException cnfe) {
            throw new InternalException(ErrorMessages.ERR_TRANSCODER_CLASS_DEF_NOT_FOUND, encClassName);
        }

        try {
            return (Transcoder) encClass.getField("INSTANCE").get(encClass);
        } catch (Exception e) {
            throw new InternalException(ErrorMessages.ERR_TRANSCODER_LOAD_ERROR, encClassName);
        }
    }

    static final class GenericTranscoderEntry {
        final byte[] source, destination;
        final String arrayKey;
        final int treeStart, inputUnitLength, maxInput, maxOutput, stateSize;
        final AsciiCompatibility compatibility;

        GenericTranscoderEntry(String source, String destination, int treeStart, String arrayKey, int inputUnitLength, int maxInput, int maxOutput,
                AsciiCompatibility compatibility, int stateSize) {

            this.source = source.getBytes();
            this.destination = destination.getBytes();
            this.treeStart = treeStart;
            this.arrayKey = arrayKey;
            this.inputUnitLength = inputUnitLength;
            this.maxInput = maxInput;
            this.maxOutput = maxOutput;
            this.compatibility = compatibility;
            this.stateSize = stateSize;
        }

        Transcoder createTranscoder() {
            return new GenericTranscoder(source, destination, treeStart, arrayKey, inputUnitLength, maxInput, maxOutput, compatibility, stateSize);
        }
    }

    static final ObjHash<String, byte[]> byteArrayHash = new ObjHash<String, byte[]>();
    static final ObjHash<String, int[]> wordArrayHash = new ObjHash<String, int[]>();

    @Override
    public String toString() {
        return new String(source) + " => " + new String(destination);
    }

    public String toStringFull() {
        String s = "Transcoder (" + new String(source) + " => " + new String(destination) + ")\n";
        s += "  class: " + getClass().getSimpleName() + "\n";
        s += "  treeStart: " + treeStart + "\n";
        s += "  byteArray:" + byteArray.length + " (";
        for (int i = 0; i < 20; i++) {
            s += (byteArray[i] & 0xff) + ", ";
        }
        s += "...)\n";

        s += "  wordArray:" + intArray.length + " (";
        for (int i = 0; i < 20; i++) {
            s += (intArray[i] & 0xffffffffl) + ", ";
        }
        s += "...)\n";

        s += "  input unit length: " + inputUnitLength + "\n";
        s += "  max input: " + maxInput + "\n";
        s += "  max output: " + maxOutput + "\n";
        s += "  compatibility: " + compatibility + "\n";
        s += "  state size: " + stateSize + "\n";
        return s;
    }
}