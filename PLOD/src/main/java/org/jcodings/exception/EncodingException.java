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
package org.jcodings.exception;

public class EncodingException extends JCodingsException {
    private final EncodingError error;

    public EncodingException(EncodingError error) {
        super(error.getMessage());
        this.error = error;
    }

    public EncodingException(EncodingError error, String str) {
        super(error.getMessage());
        this.error = error;
    }

    public EncodingException(EncodingError error, byte[]bytes, int p, int end) {
        super(error.getMessage(), bytes, p, end);
        this.error = error;
    }

    public EncodingError getError() {
        return error;
    }

    @Deprecated
    public EncodingException(String message) {
        super(message);
        error = null;
    }

    @Deprecated
    public EncodingException(String message, String str) {
        super(message, str);
        error = null;
    }

    @Deprecated
    public EncodingException(String message, byte[]bytes, int p, int end) {
        super(message, bytes, p, end);
        error = null;
    }
}
