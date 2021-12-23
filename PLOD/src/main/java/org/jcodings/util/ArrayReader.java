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
package org.jcodings.util;

//import com.knziha.plod.dictionary.Utils.BU;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.jcodings.exception.InternalException;

public class ArrayReader {

    public static DataInputStream openStream(String name) {
//        String entry = "/tables/" + name + ".bin";
//        InputStream is = ArrayReader.class.getResourceAsStream(entry);
//        if (is == null) throw new InternalException("entry: " + entry + " not found");
//        return new DataInputStream(new BufferedInputStream(is));
        String entry = name + ".bin";
        InputStream is = ArrayReader.class.getResourceAsStream("/tables.zip");
		ZipInputStream zipInputStream = new ZipInputStream(is);
		is = null;
		ZipEntry Entry;
		try {
			while ((Entry = zipInputStream.getNextEntry()) != null) {
				if (!Entry.isDirectory() && entry.equals(Entry.getName())) {
					is = new BufferedInputStream(zipInputStream);
					break;
				}
			}
		} catch (IOException ignored) { }
		if (is == null) {
			try {
				zipInputStream.close();
			} catch (IOException ignored) { }
			throw new InternalException("entry: " + entry + " not found");
		}
        return new DataInputStream(is);
    }

    public static byte[] readByteArray(String name) {
        DataInputStream dis = openStream(name);
        try {
            int size = dis.readInt();
            byte[] bytes = new byte[size];
            for (int i = 0; i < size; i++) {
                bytes[i] = dis.readByte();
            }
            checkAvailable(dis, name);
            dis.close();
            return bytes;
        } catch (IOException ioe) {
            decorate(ioe, name);
            return null;
        }
    }

    public static int[] readIntArray(String name) {
        DataInputStream dis = openStream(name);
        try {
            int size = dis.readInt();
            int[] ints = new int[size];
            for (int i = 0; i < size; i++) {
                ints[i] = dis.readInt();
            }
            checkAvailable(dis, name);
            dis.close();
            return ints;
        } catch (IOException ioe) {
            decorate(ioe, name);
            return null;
        }
    }

    public static int[][] readNestedIntArray(String name) {
        DataInputStream dis = openStream(name);
        try {
            int size = dis.readInt();
            int[][] ints = new int[size][];
            for (int i = 0; i < size; i++) {
                int iSize = dis.readInt();
                int[] iints = new int[iSize];
                ints[i] = iints;
                for (int k = 0; k < iSize; k++) {
                    iints[k] = dis.readInt();
                }
            }

            checkAvailable(dis, name);
            dis.close();
            return ints;
        } catch (IOException ioe) {
            decorate(ioe, name);
            return null;
        }
    }

    static void checkAvailable(DataInputStream dis, String name) throws IOException {
        if (dis.available() != 0) throw new InternalException("length mismatch for table: " + name + " (" + dis.available() + " left)");
    }

    static void decorate(IOException ioe, String name) {
        throw new InternalException("problem reading table: " + name + ": " + ioe);
    }
}
