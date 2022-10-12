package com.knziha.plod.PlainUI;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertController;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;

import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.widgets.MultiplexLongClicker;
import com.knziha.plod.widgets.TwoColumnAdapter;
import com.knziha.plod.widgets.WebViewmy;

import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ShareHelper {
	final MainActivityUIBase a;
	public int page;
	public final int pageSz = 11;
	private static int ver = 0;
	private final SparseIntArray mVers = new SparseIntArray();
	private final SparseArray<String[]> pages = new SparseArray<>();
	public final String[][] arraySelUtils = new String[3][];
	public int lastClickedPos;
	
	public ShareHelper(MainActivityUIBase a) {
		this.a = a;
	}
	
	public void readTargetNames(int page) {
		String[] arr = pages.get(page);
		if (arr==null) {
			pages.set(page, arr = new String[pageSz]);
		}
		if (page <= 1) {
			arr[7] = readTargetName(page, 7);
			arr[8] = readTargetName(page, 8);
			arr[9] = readTargetName(page, 9);
			arr[10] = readTargetName(page, 10);
		} else {
			for (int i = 0; i < pageSz; i++) {
				arr[i] = readTargetName(page, i);
			}
		}
		mVers.put(page, ver);
	}
	
	/** 小爷聊发爪哇狂，费点内存又何妨！ */
	private String getTargetName(String savid) throws JSONException {
		String text = a.opt.getString(savid, null);
		if(text==null) return CMN.AssetTag;
		JSONObject data = new JSONObject(text);
		try {
			text = data.getString("n");
		} catch (JSONException ignored) {
			text = null;
		}
		return text==null&&(data.has("b")||data.length()==0)?CMN.AssetTag:text;
	}
	
	public String getDefaultShareName(int pos) {
		pos -= 7;
		if(page == 1) pos += 4;
		switch (pos) {
			case 0: return "百度搜索";
			case 1: return "分享…";
			case 2: return "必应翻译";
			case 3: return "必应搜索";
			case 4: return "colordict";
			case 5: return "搜索…";
			case 6: return "文本处理";
			case 7: return "百科搜索";
		}
		return null;
	}
	
	private String readTargetName(int pageNum, int lstPos) {
		final String savid = savidForPos(lstPos);
		try {
			String ret = getTargetName(savid);
			if(ret==CMN.AssetTag) return getDefaultShareName(lstPos);
			if(ret!=null) return ret;
		} catch (Exception e) {
			CMN.debug(e);
		}
		if (pageNum <= 1) {
			int pos = lstPos - 7 + 1;
			if(pageNum==1) pos+= 4;
			return a.mResource.getString(R.string.share) + "#" + pos;
		} else {
			// ...
			return a.mResource.getString(R.string.share) + "#" + lstPos + ((page-2)*pageSz);
		}
	}
	
	public void initDefaultSharePattern(JSONObject json, final int pos) {
		CMN.debug("initDefaultSharePattern::", pos);
		int id = pos - 7;
		if (page == 1) {
			id += 4;
		}
		try {
			if(!json.has("n"))
				json.put("n", getDefaultShareName(pos));
			switch (id){
				//case R.string.send_dot:
				case 1:
				case 5:
//					if(!json.has("k1")) json.put("k1", Intent.EXTRA_TEXT);
//					if(!json.has("v1")) json.put("v1", "%s");
//					if(!json.has("k2")) json.put("k2", "_chooser");
//					if(!json.has("v2")) json.put("v2", "c/q");
//					if(!json.has("t")) json.put("t", "text/plain");
//					if(!json.has("a")) json.put("a", Intent.ACTION_SEND);
					if(!json.has("k1")) json.put("k1", "_share");
					if(!json.has("v1")) json.put("v1", id==1?"send":"search");
					break;
				case 0:
				case 2:
				case 3:
				case 7:
					if(!json.has("k1")) {
						json.put("k1", "_data");
					}
					if(!json.has("v1")) {
						if(id==7) {
							json.put("v1", "https://www.baidu.com/s?wd=%s 百科");
							//json.put("v1", "https://www.sogou.com/sogou?query=%s");
							//json.put("v1", "https://baike.sogou.com/m/fullLemma?key=%s");
						}
						json.put("v1", id==0?"https://www.baidu.com/s?wd=%s"
								:id==2?"https://cn.bing.com/translator?ref=TThis&text=%s&from=auto&to=zh-Hans"
								:"https://cn.bing.com/search?q=%s"
								);
					}
					if(!json.has("k2")) json.put("k2", "_chooser");
					if(!json.has("v2")) json.put("v2", "c/q");
					break;
				case 4:
					if(!json.has("a")) json.put("a", a.mResource.getString(R.string.colordict));
					if(!json.has("k1")) json.put("k1", MainActivityUIBase.EXTRA_QUERY);
					if(!json.has("v1")) json.put("v1", "%s");
					if(!json.has("k2")) json.put("k2", "_chooser");
					if(!json.has("v2")) json.put("v2", "/");
					break;
				case 6:
					if(!json.has("a")) json.put("a", Intent.ACTION_PROCESS_TEXT);
					if(!json.has("k1")) json.put("k1", Intent.EXTRA_PROCESS_TEXT);
					if(!json.has("v1")) json.put("v1", "%s");
					if(!json.has("t")) json.put("t", "text/plain");
					if(!json.has("k2")) json.put("k2", "_chooser");
					if(!json.has("v2")) json.put("v2", "/");
					break;
			}
			if(!json.has("k3")) json.put("k3", "_flags");
			if(!json.has("v3")) json.put("v3", "n");
		} catch (Exception e) {
			CMN.debug(e);
		}
		//CMN.debug("vs::initDefaultSharePattern::", pos, id, json);
	}
	
	public JSONObject packoutNeoJson(ArrayList<String> data) throws JSONException {
		JSONObject neo = new JSONObject();
		neo.put("n", data.get(0));
		neo.put("p", data.get(1));
		neo.put("m", data.get(2));
		neo.put("a", data.get(3));
		neo.put("t", data.get(4));
		for (int i = 5; i+1 < data.size(); i+=2) {
			if(data.get(i)!=null){
				String val = Integer.toString((i-5)/2+1);
				neo.put("k"+val, data.get(i));
				neo.put("v"+val, data.get(i+1));
			}
		}
		return neo;
	}
	
	public boolean baseOnDefaultSharePattern(JSONObject neo, JSONObject original) {
		OUT:
		try {
			/* 只能多不能少 */
			boolean b0;
			if((b0 = original.has("n")) && !neo.has("n"))
				break OUT;
			if(b0) b0 = neo.getString("n").equals(original.getString("n"));
			boolean b1;
			if((b1 = original.has("p")) && !neo.has("p"))
				break OUT;
			if(b1) b1 = neo.getString("p").equals(original.getString("p"));
			boolean b2;
			if((b2 = original.has("m")) && !neo.has("m"))
				break OUT;
			if(b2) b2 = neo.getString("m").equals(original.getString("m"));
			boolean b3;
			if((b3 = original.has("a")) && !neo.has("a"))
				break OUT;
			if(b3) b3 = neo.getString("a").equals(original.getString("a"));
			boolean b4;
			if((b4 = original.has("t")) && !neo.has("t"))
				break OUT;
			if(b4) b4 = neo.getString("t").equals(original.getString("t"));
			int cc=(original.length()-4);
			ArrayList<String> duplicatedKeys = new  ArrayList<>(cc);
			cc=0;
			while (++cc>0){
				boolean b5;
				String key="k"+cc;
				b5 = original.has(key);
				if(!b5) break;
				if(!neo.has(key))
					break OUT;
				b5 = neo.getString(key).equals(original.getString(key));
				if(b5) duplicatedKeys.add(key);
				
				key="v"+cc;
				b5 = original.has(key);
				if(!b5) continue;
				if(!neo.has(key))
					break OUT;
				b5 = neo.getString(key).equals(original.getString(key));
				if(b5) duplicatedKeys.add(key);
			}
			if(b0) neo.remove("n");
			if(b1) neo.remove("p");
			if(b2) neo.remove("m");
			if(b3) neo.remove("a");
			if(b4) neo.remove("t");
			for(String dkI:duplicatedKeys){
				neo.remove(dkI);
			}
			if(neo.length()>0){
				neo.put("b", 1);
			}
			CMN.debug("好哎！！！",neo.toString());
		} catch (Exception e) {
			CMN.debug(e);
			return true;
		}
		return false;
	}
	
	public void serializeSharePattern(JSONObject json, ArrayList<String> data) {
		for (int i = 0; i < 5; i++) data.add(null);
		try {
			if(json.has("n")) data.set(0, json.getString("n"));
			if(json.has("p")) data.set(1, json.getString("p"));
			if(json.has("m")) data.set(2, json.getString("m"));
			if(json.has("a")) data.set(3, json.getString("a"));
			if(json.has("t")) data.set(4, json.getString("t"));
			int cc=0;
			while (++cc>0){
				String key="k"+cc;
				if(!json.has(key)) break;
				data.add(json.getString(key));
				String vI = "v"+cc;
				data.add(json.has(vI)?json.getString(vI):"%s");
			}
		} catch (JSONException e) {
			CMN.debug(e);
		}
	}
	
	public final static int[] defPageStrIds = new int[]{
			R.string.favor_sel
			, R.string.select_all
			, R.string.hi_color
			, R.string.highlight
			, R.string.annote
			, R.string.underline
			, R.string.deunderline
			, R.string.send_dot
			, R.string.send_dot
			, R.string.send_dot
			, R.string.send_dot
			, R.string.favor_sel
			, R.string.send_inpage
			, R.string.tts
			, R.string.pop_sch
			, R.string.peruse_sch
			, R.string.send_etsch
			, R.string.fapp_name
			, R.string.send_dot
			, R.string.search_dot
			, R.string.send_dot
			, R.string.send_dot
	};
	
	public String[] getPageItems(MainActivityUIBase.VerseKit vk) {
		String[] ret = null;
		int page = this.page;
		if (page <= 1) {
			if (arraySelUtils[page] == null) {
				final Resources res = a.mResource;
				final String[] arr = arraySelUtils[page] = new String[pageSz];
				final int base = pageSz*page;
				for (int i = 0; i < pageSz; i++) {
					if (i < 7) {
						arr[i] = res.getString(defPageStrIds[base + i]);
					} else {
						arr[i] = readTargetName(page, i);
					}
				}
			}
			if (vk == null) {
				return null;
			}
			if (vk.bFromWebView) {
				ret = arraySelUtils[page];
			} else {
				if (arraySelUtils[1] == null) {
					this.page = 1;
					getPageItems(null);
					this.page = page;
				}
				ret = arraySelUtils[2];
				if(ret == null) {
					ret = arraySelUtils[2] = new String[pageSz];
					System.arraycopy(arraySelUtils[1], 0, ret, 0, pageSz - 4);
					System.arraycopy(arraySelUtils[page], 7, ret, 7, 4);
				}
			}
		} else {
			// 加载更多多维分享……
		}
		pages.set(page, ret);
		if (mVers.get(page) != ver) {
			readTargetNames(page);
		}
		return ret;
	}
	
	private String savidForPos(int pos){
		if (page <= 1) {
			pos -= 7;
			return "dsp#" + (page * pageSz + pos);
		} else {
			return "dsp_"+((page-2)*pageSz+pos);
		}
	}
	
	public boolean execVersatileShare(boolean isLongClicked, final int pos) {
		JSONObject json = a.opt.getDimensionalSharePatternByIndex(savidForPos(pos));
		boolean putDefault = json == null;
		if (putDefault) {
			json = new JSONObject();
		} else {
			putDefault = json.has("b")||json.length()==0;
		}
		if (putDefault) initDefaultSharePattern(json, pos);
		/* 将 json 散列为数组。 */
		ArrayList<String> data = new ArrayList<>(16);
		serializeSharePattern(json, data);
		if (!isLongClicked) {
			HandleShareIntent(data);
		}
		/* 对话框定义多维分享 */
		/* Customizable parts of MDCCSP ( from Share#0-Share#5 )*/
		else {
			Context context = a;
			AlertController.RecycleListView customList = new AlertController.RecycleListView(context);
			customList.mMaxHeight = (int) (a.root.getHeight() - a.root.getPaddingTop() - 3.8 * a.mResource.getDimension(R.dimen._50_));
			
			CustomShareAdapter csa = new CustomShareAdapter(data);
			customList.setAdapter(csa);
			customList.setDivider(null);
			
			AlertDialog.Builder builder2 = new AlertDialog.Builder(context, GlobalOptions.isDark ? R.style.DialogStyle3Line : R.style.DialogStyle4Line);
			builder2.setTitle("制定分享目标");
			builder2.setNeutralButton("添加字段", null);
			builder2.setNegativeButton("测试", null);
			builder2.setPositiveButton("保存", null);
			
			FrameLayout dv = new FrameLayout(context);
			/* 键盘能够弹出 */
			dv.addView(customList);
			dv.addView(new EditText(context), new LinearLayout.LayoutParams(0, 0));
			csa.nameWidth = (int) ((TextView) csa.getView(0, null, customList).findViewById(R.id.text2)).getPaint().measureText(a.mResource.getString(R.string.extra_key_value, 1001));
			builder2.setView(dv);
			
			AlertDialog dTmp = builder2.show();
			
			View.OnClickListener mClicker = new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					boolean isLongClicked = v.getTag(R.id.long_clicked) != null;
					switch (v.getId()) {
						case android.R.id.button1://+
							if (isLongClicked) {
								android.app.AlertDialog.Builder builder21 = new android.app.AlertDialog.Builder(a.getLayoutInflater().getContext());
								android.app.AlertDialog d1 = builder21.setTitle("确认删除并恢复默认值？")
										.setPositiveButton(R.string.confirm, (dialog, which) -> {
											a.opt.putDimensionalSharePatternByIndex(savidForPos(pos), null);
											JSONObject json = new JSONObject();
											data.clear();
											initDefaultSharePattern(json, pos);
											serializeSharePattern(json, data);
											csa.notifyDataSetChanged();
											itemChanged(pos);
											ver++;
										})
										.create();
								d1.show();
							}
							else try {
								JSONObject neo = packoutNeoJson(data);
								JSONObject original = new JSONObject();
								initDefaultSharePattern(original, pos);
								if (baseOnDefaultSharePattern(neo, original)) {
									neo = packoutNeoJson(data);
								}
								a.opt.putDimensionalSharePatternByIndex(savidForPos(pos), neo);
								a.showT("保存成功！");
								itemChanged(pos);
								ver++;
								dTmp.dismiss();
							}
							catch (Exception e) {
								CMN.debug(e);
								a.showT("保存失败！" + e);
							}
							break;
						case android.R.id.button2://-
							if (isLongClicked) break;
							HandleShareIntent(data);
							break;
						case android.R.id.button3://|
							if (isLongClicked) break;
							data.add(null);
							data.add(null);
							csa.notifyDataSetChanged();
							break;
					}
				}
			};
			
			Button btnTmp = dTmp.getButton(DialogInterface.BUTTON_POSITIVE);
			btnTmp.setOnClickListener(mClicker);
			btnTmp.setOnLongClickListener(new MultiplexLongClicker());
			dTmp.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(mClicker);
			dTmp.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(mClicker);
		}
		return false;
	}
	
	private void itemChanged(int pos) {
		try {
			pages.get(page)[pos] = readTargetName(page, pos);
			TwoColumnAdapter listAdapter = a.getVtk().twoColumnAda;
			if (listAdapter != null)
				listAdapter.notifyItemChanged(pos);
		} catch (Exception e) {
			CMN.debug(e);
		}
	}
	
	public void HandleShareIntent(ArrayList<String> data) {
		MainActivityUIBase.VerseKit vk = a.getVtk();
		if(vk.bFromTextView){
			a.handleIntentShare(vk.CurrentSelected, data);
			if(a.thisActType== MainActivityUIBase.ActType.MultiShare) {
				a.checkMultiVSTGO();
			}
		} else {
			if(data.contains(Intent.EXTRA_HTML_TEXT)){
				if(data.contains(Intent.EXTRA_TEXT)){
					vk.mWebView.evaluateJavascript(WebViewmy.CollectWord, v -> {
						if (v.length() > 2) {
							v = StringEscapeUtils.unescapeJava(v.substring(1, v.length() - 1));
							data.add(Intent.EXTRA_TEXT);
							data.add(v);
							FetchTextOrHtmlThen_handleIntentShare(data, true);
						}
					});
				} else {
					FetchTextOrHtmlThen_handleIntentShare(data, true);
				}
			} else {
				FetchTextOrHtmlThen_handleIntentShare(data, false);
			}
		}
	}
	
	public void FetchTextOrHtmlThen_handleIntentShare(ArrayList<String> data, boolean parseHtml) {
		MainActivityUIBase.VerseKit vk = a.getVtk();
		String FetchWord = parseHtml ? WebViewmy.CollectHtml : WebViewmy.CollectWord;
		vk.mWebView.evaluateJavascript(FetchWord, v -> {
			if (v.length() > 2) {
				v = StringEscapeUtils.unescapeJava(v.substring(1, v.length() - 1));
				//CMN.Log("Fetched Page Part : ", v);
				a.handleIntentShare(v, data);
			}
		});
	}
	
	private static class CustomShareAdapter extends BaseAdapter {
		ArrayList<String> data;
		private View.OnClickListener ocl=new OnClickListenermy();
		public int nameWidth;
		
		public CustomShareAdapter(ArrayList<String> _data){
			data = _data;
		}
		
		@Override
		public int getCount() {
			return data.size();
		}
		
		@Override
		public Object getItem(int position) {
			return data.get(position);
		}
		
		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TargetViewHolder tvh = convertView == null ? new TargetViewHolder(parent, ocl, data) : (TargetViewHolder) convertView.getTag();
			/** 至少六个可自定义的字段:<br/>
			 * {p:程序包名 m:活动名称 a:举措名称 t:MIME类型 k1:字段1键名 v1:字段1键值…} */
			String dataVal = data.get(position);
			if (dataVal == null)
				switch (position) {
					case 0:
					case 1:
						break;
					case 2:
						//value = Intent.ACTION_VIEW;
						break;
					case 3:
						//value = "text/plain";
						break;
					case 4:
						//value = Intent.EXTRA_TEXT;
						break;
					case 5:
						//value = "%s";
						break;
					default:
						//if (position % 2 != 0) {
						//	value = "%s";
						//}
						break;
				}
			
			TextView text = tvh.title;
			text.clearFocus();
			text.removeTextChangedListener(tvh.tw);
			text.setText(dataVal);
			text.addTextChangedListener(tvh.tw);
			tvh.position = position;
			
			int value = 0;
			text = tvh.name;
			ViewGroup.LayoutParams lp = text.getLayoutParams();
			if(lp.width!=nameWidth) {
				lp.width=nameWidth;
			}
			switch (position) {
				case 0:
					value = R.string.share_name;
					break;
				case 1:
					value = R.string.package_name;
					break;
				case 2:
					value = R.string.activity_name;
					break;
				case 3:
					value = R.string.action_name;
					break;
				case 4:
					value = R.string.mime_type;
					break;
				default:
					text.setText(parent.getContext().getString(position%2!=0?R.string.extra_key_name:R.string.extra_key_value, (position-5)/2+1));
					break;
			}
			
			if(value!=0) {
				text.setText(value);
			}
			
			return tvh.itemView;
		}
	}
	
	private static class OnClickListenermy implements View.OnClickListener{
		@Override
		public void onClick(View v) {
			TargetViewHolder tvh = (TargetViewHolder) ((View)v.getParent()).getTag();
			tvh.title.setText(null);
			
		}
	}
	
	private static class TargetViewHolder{
		View itemView;
		TextView name;
		EditText title;
		View deletText;
		int position;
		final ArrayList<String> data;
		TextWatcher tw;
		TargetViewHolder(ViewGroup parent, View.OnClickListener onc, ArrayList<String> data){
			this.data = data;
			itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.share_targets, parent, false);
			itemView.setTag(this);
			name = itemView.findViewById(R.id.text2);
			title = itemView.findViewById(R.id.text1);
			deletText = itemView.findViewById(R.id.ivDeleteText);
			title.addTextChangedListener(tw = new TextWatcher(){
				@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
				@Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
				@Override
				public void afterTextChanged(Editable s) {
					String text = s.toString().trim();
					data.set(position, text.length()>0?text:null);
				}
			});
			
			deletText.setOnClickListener(onc);
		}
	}
	
	public String getShareTitle() {
		try {
			return pages.get(lastClickedPos/pageSz)[lastClickedPos%pageSz];
		} catch (Exception e) {
			CMN.debug(e);
			return null;
		}
	}
}
