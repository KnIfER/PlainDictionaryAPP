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

public interface EConvFlags {
    final int ERROR_HANDLER_MASK              = 0x000000ff;

    final int INVALID_MASK                    = 0x0000000f;
    final int INVALID_REPLACE                 = 0x00000002;

    final int UNDEF_MASK                      = 0x000000f0;
    final int UNDEF_REPLACE                   = 0x00000020;
    final int UNDEF_HEX_CHARREF               = 0x00000030;

    final int DECORATOR_MASK                  = 0x0000ff00;
    final int NEWLINE_DECORATOR_MASK          = 0x00003f00;
    final int NEWLINE_DECORATOR_READ_MASK      = 0x00000f00;
    final int NEWLINE_DECORATOR_WRITE_MASK     = 0x00003000;

    final int UNIVERSAL_NEWLINE_DECORATOR     = 0x00000100;
    final int CRLF_NEWLINE_DECORATOR          = 0x00001000;
    final int CR_NEWLINE_DECORATOR            = 0x00002000;
    final int XML_TEXT_DECORATOR              = 0x00004000;
    final int XML_ATTR_CONTENT_DECORATOR      = 0x00008000;

    final int STATEFUL_DECORATOR_MASK         = 0x00f00000;
    final int XML_ATTR_QUOTE_DECORATOR        = 0x00100000;


    final int PARTIAL_INPUT                    = 0x00010000;
    final int AFTER_OUTPUT                     = 0x00020000;

    final int  MAX_ECFLAGS_DECORATORS          = 32;
}