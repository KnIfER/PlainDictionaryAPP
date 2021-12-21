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

public interface ErrorMessages {
    final String ERR_TYPE_BUG = "undefined type (bug)";

    final String ERR_TOO_BIG_WIDE_CHAR_VALUE = "too big wide-char value";
    final String ERR_TOO_LONG_WIDE_CHAR_VALUE = "too long wide-char value";

    final String ERR_INVALID_CHAR_PROPERTY_NAME = "invalid character property name <%n>";
    final String ERR_INVALID_CODE_POINT_VALUE = "invalid code point value";

    final String ERR_ENCODING_CLASS_DEF_NOT_FOUND = "encoding class <%n> not found";
    final String ERR_ENCODING_LOAD_ERROR = "problem loading encoding <%n>";

    final String ERR_ILLEGAL_CHARACTER = "illegal character";

    final String ERR_ENCODING_ALREADY_REGISTERED = "encoding already registerd <%n>";
    final String ERR_ENCODING_ALIAS_ALREADY_REGISTERED = "encoding alias already registerd <%n>";
    final String ERR_ENCODING_REPLICA_ALREADY_REGISTERED = "encoding replica already registerd <%n>";
    final String ERR_NO_SUCH_ENCODNG = "no such encoding <%n>";
    final String ERR_COULD_NOT_REPLICATE = "could not replicate <%n> encoding";

    // transcoder messages
    final String ERR_TRANSCODER_ALREADY_REGISTERED = "transcoder from <%n> has been already registered";
    final String ERR_TRANSCODER_CLASS_DEF_NOT_FOUND = "transcoder class <%n> not found";
    final String ERR_TRANSCODER_LOAD_ERROR = "problem loading transcoder <%n>";
}
