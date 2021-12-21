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

public enum EConvResult {

    InvalidByteSequence, UndefinedConversion, DestinationBufferFull, SourceBufferEmpty, Finished, AfterOutput, IncompleteInput;

    public boolean isInvalidByteSequence() {
        return this == InvalidByteSequence;
    }

    public boolean isUndefinedConversion() {
        return this == UndefinedConversion;
    }

    public boolean isDestinationBufferFull() {
        return this == DestinationBufferFull;
    }

    public boolean isSourceBufferEmpty() {
        return this == SourceBufferEmpty;
    }

    public boolean isFinished() {
        return this == Finished;
    }

    public boolean isAfterOutput() {
        return this == AfterOutput;
    }

    public boolean isIncompleteInput() {
        return this == IncompleteInput;
    }

    public String symbolicName() {
        return symbolicName;
    }

    private final String symbolicName;

    {
        String name = name();
        StringBuilder snakeName = new StringBuilder(name.length() + 3);
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isLowerCase(c)) {
                snakeName.append(c);
                continue;
            } else if (Character.isUpperCase(c)) {
                if (i > 0) snakeName.append('_');
                snakeName.append(Character.toLowerCase(c));
            }
        }
        this.symbolicName = snakeName.toString().intern();
    }
}