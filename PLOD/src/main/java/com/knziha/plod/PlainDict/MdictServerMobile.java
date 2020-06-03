/*  Copyright 2018 KnIfER Zenjio-Kang

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	    http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
	
	Mdict-Java Query Library
*/
package com.knziha.plod.PlainDict;

import android.graphics.Bitmap;

import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionary.Utils.SU;
import com.knziha.plod.dictionarymodels.mdict;
import com.knziha.rbtree.RBTree_additive;
import com.knziha.rbtree.additiveMyCpr1;

import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.imaging.BufferedImage;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.ImagingConstants;
import org.apache.commons.imaging.ManagedImageBufferedImageFactory;
import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;
import org.xiph.speex.ByteArrayRandomOutputStream;
import org.xiph.speex.manyclass.JSpeexDec;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static org.nanohttpd.protocols.http.response.Response.newFixedLengthResponse;

//import fi.iki.elonen.NanoHTTPD;
//import fi.iki.elonen.NanoHTTPD.Response.Status;


/**
 * Mdict Server
 * @author KnIfER
 * @date 2018/09/19
 */

public class MdictServerMobile extends MdictServer {
	private PDICMainActivity a;
	
	public MdictServerMobile(int port, PDICMainActivity _a, PDICMainAppOptions _opt) {
		super(port, _opt);
		a = _a;
	}
	
	protected InputStream OpenMdbResourceById(int id) throws IOException {
		InputStream ret = null;
		switch (id) {
			case ServerMainPage:
				ret = a.getResources().getAssets().open("mdict_browser.html");
			break;
			case ServerSubPage:
				ret = a.getResources().getAssets().open("MdbR/subpage.html");
			break;
		}
		return ret;
	}
	
}