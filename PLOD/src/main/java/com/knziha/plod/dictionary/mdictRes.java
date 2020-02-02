/*  Copyright 2018 KnIfER

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

package com.knziha.plod.dictionary;

import com.knziha.plod.dictionary.Utils.BSI;
import com.knziha.plod.dictionary.Utils.BU;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.zip.InflaterOutputStream;

import com.knziha.plod.dictionary.Utils.key_info_struct;
import com.knziha.plod.dictionary.Utils.record_info_struct;
import org.anarres.lzo.LzoDecompressor1x;
import org.anarres.lzo.lzo_uintp;

//import org.jvcompress.lzo.MiniLZO;
//import org.jvcompress.util.MInt;





/**
 * Mdict java : resource file (.mdd) class
 * @author KnIfER
 * @date 2017/12/8
 */

public class mdictRes extends mdBase{

	HashMap<Integer,String[]> _stylesheet = new HashMap<>();


    //构造
	public mdictRes(String fn) throws IOException{
		super(fn);
        //decode_record_block_header();
	}

	@Override
	protected void init(DataInputStream data_in) throws IOException {
		super.init(data_in);
		data_in.close();
	}
}


