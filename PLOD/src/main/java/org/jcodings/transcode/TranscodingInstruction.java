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

interface TranscodingInstruction {
    final int NOMAP =           0x01; /* direct map */
    final int ONEbt =           0x02; /* one byte payload */
    final int TWObt =           0x03; /* two bytes payload */
    final int THREEbt =         0x05; /* three bytes payload */
    final int FOURbt =          0x06; /* four bytes payload, UTF-8 only, macros start at getBT0 */
    final int INVALID =         0x07; /* invalid byte sequence */
    final int UNDEF =           0x09; /* legal but undefined */
    final int ZERObt =          0x0A; /* zero bytes of payload, i.e. remove */
    final int FUNii =           0x0B; /* function from info to info */
    final int FUNsi =           0x0D; /* function from start to info */
    final int FUNio =           0x0E; /* function from info to output */
    final int FUNso =           0x0F; /* function from start to output */
    final int STR1 =            0x11; /* string 4 <= len <= 259 bytes: 1byte length + content */
    final int GB4bt =           0x12; /* GB18030 four bytes payload */
    final int FUNsio =          0x13; /* function from start and info to output */

    final int LAST =                0x1C;
    final int NOMAP_RESUME_1 =      LAST + 1;

    final int ZeroXResume_1 =       LAST + 2;
    final int ZeroXResume_2 =       LAST + 3;

}