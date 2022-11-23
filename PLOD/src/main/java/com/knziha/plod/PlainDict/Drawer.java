package com.knziha.plod.plaindict;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebResourceResponse;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;

import com.knziha.filepicker.model.DialogConfigs;
import com.knziha.filepicker.model.DialogProperties;
import com.knziha.filepicker.model.DialogSelectionListener;
import com.knziha.filepicker.view.FilePickerDialog;
import com.knziha.plod.db.LexicalDBHelper;
import com.knziha.plod.dictionary.Utils.BU;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionary.Utils.SU;
import com.knziha.plod.dictionary.mdict;
import com.knziha.plod.dictionarymanager.files.ReusableBufferedReader;
import com.knziha.plod.dictionarymanager.files.ReusableBufferedWriter;
import com.knziha.plod.dictionarymanager.files.mFile;
import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.dictionarymodels.DictionaryAdapter;
import com.knziha.plod.dictionarymodels.PlainWeb;
import com.knziha.plod.settings.NightMode;
import com.knziha.plod.settings.ServerPreference;
import com.knziha.plod.widgets.CheckedTextViewmy;
import com.knziha.plod.widgets.FlowTextView;
import com.knziha.plod.widgets.SwitchCompatBeautiful;
import com.knziha.plod.widgets.ViewUtils;
import com.knziha.plod.widgets.WebViewmy;
import com.knziha.plod.widgets.XYTouchRecorder;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.noties.markwon.Markwon;
import io.noties.markwon.core.spans.LinkSpan;

import static androidx.appcompat.app.GlobalOptions.realWidth;
import static com.knziha.plod.PlainUI.HttpRequestUtil.DO_NOT_VERIFY;
import static com.knziha.plod.PlainUI.WordPopupTask.TASK_UPD_SCH;
import static com.knziha.plod.plaindict.PDICMainAppOptions.PLAIN_TARGET_FLOAT_SEARCH;

import javax.net.ssl.HttpsURLConnection;


/** @author KnIfER */
public class Drawer extends Fragment implements
		OnClickListener, OnDismissListener, OnCheckedChangeListener, OnLongClickListener {
	private PDICMainActivity a;
	private boolean bIsFirstLayout=true;
	AlertDialog d;

	String[] hints;
	private ListView mDrawerList;
	View mDrawerListLayout;
	MyAdapter myAdapter;

	public EditText etAdditional;

	SwitchCompat sw1,sw2,sw3,sw4,sw5;

	View HeaderView;
	View HeaderView2;
	View pasteBin;

	ViewGroup FooterView;
	private File filepickernow;
	public  ArrayList<String> mClipboard;
	FileOutputStream PasteFileTarget;
	ClipboardManager.OnPrimaryClipChangedListener ClipListener;
	private ListView ClipboardList;
	private CharSequence mPreviousCBContent;
	private ViewGroup swRow;
	private boolean toPDF;
	private int basicArrLen;
	private ViewGroup menu_item_setting;
	private ViewGroup menu_item_exit;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		//if(true) return new View(inflater.getContext());
		if(mDrawerListLayout ==null){
			mDrawerListLayout = inflater.inflate(R.layout.activity_main_navi_drawer, container,false);
			FooterView = mDrawerListLayout.findViewById(R.id.footer);
			menu_item_setting = FooterView.findViewById(R.id.menu_item_setting);
			menu_item_exit = FooterView.findViewById(R.id.menu_item_exit);
			menu_item_setting.setOnClickListener(this);
			menu_item_exit.setOnClickListener(this);
			//menu_item_exit.setOnLongClickListener(this); // 测试 fileManager
			mDrawerList = mDrawerListLayout.findViewById(R.id.left_drawer);
			
			int[] basicArr;
			if (GlobalOptions.isLarge) {
				basicArr = new int[]{
						R.string.fuzzyret1
						, R.string.fullret
						, 0
						, R.string.bookmarkH
						, R.string.lastmarks
						, 0
						, R.string.settings
						, 0
						, R.string.addd
						, R.string.pick_main
						, R.string.manager
						, R.string.switch_favor
						, 0
						, R.string.about
						, 0
						, R.string.exit
						, 0
						, R.string.clip_board
				};
				basicArrLen = basicArr.length-6;
				FooterView.setVisibility(View.GONE);
			}
			else {
				basicArr = new int[]{
						R.string.fuzzyret1
						, R.string.fullret
						, 0
						, R.string.bookmarkH
						, R.string.lastmarks
						, 0
						, R.string.settings
						, 0
						, R.string.addd
						, R.string.pick_main
						, R.string.manager
						, R.string.switch_favor
				};
				basicArrLen = basicArr.length;
			}
			myAdapter = new MyAdapter(basicArr);
			
			mDrawerList.setAdapter(myAdapter);
			
			HeaderView = inflater.inflate(R.layout.activity_main_navi_drawer_header, null);
			sw1 = HeaderView.findViewById(R.id.sw1);
			sw2 = HeaderView.findViewById(R.id.sw2);
			sw3 = HeaderView.findViewById(R.id.sw3);
			sw4 = HeaderView.findViewById(R.id.sw4);
			sw5 = HeaderView.findViewById(R.id.sw5);
			
			mDrawerList.addHeaderView(HeaderView);

			mDrawerListLayout.addOnLayoutChangeListener(new OnLayoutChangeListener() {
				int oldWidth;
				@Override
				public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop,
										   int oldRight, int oldBottom) {
					right=right-left;
					//CMN.debug("onLayoutChange::", right);
					//todo opt
					if(GlobalOptions.isLarge && a!=null) {
						right = Math.min(right, Math.max(realWidth, (int)a.mResource.getDimension(R.dimen.idealdpdp)));
						v.getLayoutParams().width = right;
					}
					if(swRow!=null && (right!=oldWidth || bIsFirstLayout)) {
						right = Math.max(swRow.getMinimumWidth(), right);
						//if(bIsFirstLayout) SwitchCompatBeautiful.bForbidRquestLayout = true;
						int width = (right - sw1.getWidth() * 5) / 6;
						View vI;
						boolean req = false;
						for (int i = 0; i < swRow.getChildCount(); i++) {
							vI=swRow.getChildAt(i);
							MarginLayoutParams lp = (MarginLayoutParams) vI.getLayoutParams();
							if (lp.leftMargin != width) {
								lp.leftMargin = width;
								//vI.requestLayout();
								req = true;
							}
						}
						if (req) swRow.requestLayout();
						oldWidth = right;
						if(bIsFirstLayout) {
							SwitchCompatBeautiful.bForbidRquestLayout = false;
							bIsFirstLayout = false;
						}
					}
				}
			});
		}
		return mDrawerListLayout;
	}

	public void setCheckedForce(SwitchCompat sw5, boolean b) {
		if(sw5.isChecked()==b){
			onCheckedChanged(sw5, b);
		}else{
			sw5.toggle();
		}
	}
	
	public void adjustBottomPadding() {
		if (!GlobalOptions.isLarge) {
			int pad = 0;
			if (PDICMainAppOptions.getEnableSuperImmersiveScrollMode()) {
				if (a.UIData.appbar.getTop()==0) {
					pad = a.toolbar.getHeight()+a.bottombar.getHeight();
				}
				//mDrawerList.setNestedScrollingEnabled(pad==0);
			}
			if (mDrawerListLayout.getPaddingBottom()!=pad) {
				mDrawerListLayout.setPadding(0, 0, 0, pad);
			}
		}
	}
	
	class MyAdapter extends BaseAdapter {
		int[] items;
		public MyAdapter(int[] items) {
			this.items = items;
		}
		boolean show_hints = true;
		public void notifyDataSetChangedX() {
			show_hints = true;//a.opt.isDrawer_Showhints();
			super.notifyDataSetChanged();
		}
		@Override
		public boolean areAllItemsEnabled() {
			return false;
		}
		@Override
		public int getCount() {
			if (GlobalOptions.isLarge) {
				if (pasteBin==null || pasteBin.getVisibility()==View.INVISIBLE)
					return items.length-2;
			}
			return items.length;
		}
		@Override
		public boolean isEnabled(int position) {
			return items[position]!=0;
		}
		
		@Override
		public int getViewTypeCount() {
			return 3;
		}
		
		@Override
		public int getItemViewType(int position) {
			int item = items[position];
			if (item==0) {
				return 1;
			}
			if (item==R.string.settings
				|| position>=basicArrLen
			) {
				return 2;
			}
			return 0;
		}
		
		@Override
		public String getItem(int position) {
			return a.mResource.getString(items[position]);
		}
		
		@Override
		public long getItemId(int position) {
			return items[position];
		}
		
		@NonNull
		@Override
		public View getView(int position, View convertView, @NonNull ViewGroup parent) {
			int id=items[position];
			if(id==0){
				/* divider border */
				return convertView!=null?convertView:LayoutInflater.from(getContext()).inflate(R.layout.listview_sep, parent, false);
			}
			MainActivityUIBase.ViewHolder vh;
			int viewType = getItemViewType(position);
			if(convertView!=null){
				vh=(MainActivityUIBase.ViewHolder)convertView.getTag();
			} else {
				vh=new MainActivityUIBase.ViewHolder(a,viewType==2?R.layout.drawer_settings:R.layout.drawer_item0, parent);
				vh.itemView.setBackgroundResource(R.drawable.listviewselector1);
				vh.itemView.setOnClickListener(Drawer.this);
				if (vh.subtitle!=null) {
					//vh.subtitle.trim=false; todo 890
					vh.subtitle.setTextColor(ContextCompat.getColor(a, R.color.colorHeaderBlue));
				}
			}
			vh.position=position;
			vh.itemView.setOnLongClickListener((id==R.string.addd||id==R.string.pick_main)?Drawer.this:null);
			if( vh.title.getTextColors().getDefaultColor()!=a.AppBlack) {
				PDICMainActivity.decorateBackground(vh.itemView);
				vh.title.setTextColor(a.AppBlack);
			}
			if (viewType==2) {
				// 设置图标
				((ImageView)vh.itemView.findViewById(R.id.icon)).setImageResource(id==R.string.settings?R.drawable.drawer_menu_icon_setting
						:id==R.string.about?R.drawable.info
						:id==R.string.exit?R.drawable.drawer_menu_icon_exit
						:/*id==R.string.clip_board?*/R.drawable.ic_content_paste_black_24dp
						);
			}
			vh.title.setText(id);
			vh.itemView.setId(id);
			if (vh.subtitle!=null) {
				String hint = null;
				if(show_hints) {
					if(hints==null) hints = getResources().getStringArray(R.array.drawer_hints);
					if(position<hints.length) hint=hints[position];
				}
				vh.subtitle.setText(hint);
			}
//			vh.subtitle.getLayoutParams().height=hint==null? (int) (GlobalOptions.density * 8) :-2;
//			vh.subtitle.requestLayout();
			//vh.subtitle.setVisibility(hint==null?View.GONE:View.VISIBLE);

			return vh.itemView;
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		//CMN.Log("Drawer onActivityCreated");
		super.onActivityCreated(savedInstanceState);
		a = ((PDICMainActivity)getActivity());
		a.drawerFragment = this;
		
		if (GlobalOptions.isSmall) {
			try {
				// 我真的不是反射狂魔
				ViewUtils.execSimple("$.findViewById[int]($1).mChildren[1].setVisibility[int](8)", null, mDrawerListLayout, R.id.menu_item_exit);
				ViewUtils.execSimple("$.findViewById[int]($1).mChildren[1].setVisibility[int](8)", null, mDrawerListLayout, R.id.menu_item_setting);
				ViewUtils.execSimple("$.mMinDrawerMargin=$1", null, a.UIData.drawerLayout, 0); // todo modify androidx.drawerLayout
			} catch (Exception e) { CMN.Log(e); }
		}
		
		if(HeaderView==null) return;
		
		myAdapter.show_hints = true;
		
		if(PDICMainAppOptions.getShowPasteBin())
			SetupPasteBin();

		swRow = (ViewGroup) sw1.getParent();
		sw1.setOnCheckedChangeListener(this);
		sw1.setChecked(PDICMainAppOptions.isFullScreen());
		sw1.setOnClickListener(v -> {
			// TODO Auto-generated method stub

		});

		boolean val = PDICMainAppOptions.getEnableSuperImmersiveScrollMode();
		sw2.setChecked(val);
		sw2.setOnCheckedChangeListener(this);

		sw3.setOnCheckedChangeListener(this);
		//sw3.setChecked(!a.opt.isViewPagerEnabled());
		sw3.setChecked(a.opt.getServerStarted());

		sw4.setOnLongClickListener(this);
		val = GlobalOptions.isDark;
		sw4.setChecked(val);
		sw4.setOnCheckedChangeListener(this);
		if (val) {
			a.changeToDarkMode();
			refreshBtns(val);
		}

		sw5.setChecked(a.opt.getUseVolumeBtn());
		sw5.setOnCheckedChangeListener(this);

		if(GlobalOptions.isDark) {
			mDrawerListLayout.setBackgroundColor(Color.BLACK);
			HeaderView.setBackgroundColor(a.AppWhite);
			FooterView.setBackgroundColor(a.AppWhite);
		}
		//test groups
		//MainActivityUIBase.ViewHolder vh = new MainActivityUIBase.ViewHolder(a, R.layout.listview_item0, null);
		//View v = new View(a);v.setTag(vh);onClick(v);
	}

	void SetupPasteBin() {
		ClipboardManager clipboardManager = (ClipboardManager) a.getSystemService(Context.CLIPBOARD_SERVICE);
		if(clipboardManager!=null){
			if(pasteBin==null){
				pasteBin = mDrawerListLayout.findViewById(R.id.pastebin);
				pasteBin.setOnClickListener(this);
				if (GlobalOptions.isLarge) {
					pasteBin.getLayoutParams().height=0;
				}
				mClipboard=new ArrayList<>(12);
				//mClipboard.add("Happy");
			}
			if(PDICMainAppOptions.getShowPasteBin()){
				pasteBin.setVisibility(View.VISIBLE);
				if(ClipListener==null){
					ClipListener = () -> {
						if (a.opt.getPasteBinEnabled())
							try {
								ClipData pclip = clipboardManager.getPrimaryClip();
								if (pclip==null) {
									return;
								}
								ClipData.Item firstItem = pclip.getItemAt(0);
								CharSequence content = firstItem.getText();
								//CMN.Log("剪贴板监听器:", content);
								//a.showT(  GlobalOptions.chromium+"剪贴板监听器:" + content + System.identityHashCode(pclip));
								
								long timeDelta = System.currentTimeMillis() - a.lastClickTime;
								if ((GlobalOptions.chromium || timeDelta < 256) && content.equals(mPreviousCBContent)){
									return;
								}
								String text = content.toString();
								if(false) {
									try {
										if(PasteFileTarget==null) {
											PasteFileTarget = new FileOutputStream(new File("/sdcard/myFolder/PlainDictPasted.txt"), true);
											PasteFileTarget.write("\n\n".getBytes());
										}
										Pattern p = Pattern.compile("(ht|f)tp(s?)://[0-9a-zA-Z]([-.\\w]*[0-9a-zA-Z])*(:(0-9)*)*(/?)([a-zA-Z0-9\\-.?,'/\\\\+&amp;%$#_]*)?");
										Matcher m = p.matcher(text);
										if(m.find()) {
											text = m.group(0);
										}
										text = text.trim();
										text = text.replace("\r", "");
										text = text.replace("\n", "");
										PasteFileTarget.write(text.getBytes());
										PasteFileTarget.write("\n".getBytes());
										PasteFileTarget.flush();
										a.showT("已记录！");
									} catch (Exception ignored) { }
									return;
								}
								int i = 0;
								for (; i < mClipboard.size(); i++) {
									if (mClipboard.get(i).equals(text))
										break;
								}
								if (i == mClipboard.size()) {
									mClipboard.add(0, text);
								} else {
									mClipboard.add(0, mClipboard.remove(i));
								}
								boolean focused = a.hasWindowFocus();
								boolean toFloat = a.opt.getPasteTarget() == PLAIN_TARGET_FLOAT_SEARCH;
								if (!toFloat && !focused && a.opt.getPasteBinBringTaskToFront()) {
									ActivityManager manager = (ActivityManager) a.getSystemService(Context.ACTIVITY_SERVICE);
									if (manager != null)
										manager.moveTaskToFront(a.getTaskId(), ActivityManager.MOVE_TASK_WITH_HOME);
								}
								if (toFloat || (focused || a.opt.getPasteBinBringTaskToFront()) && a.opt.getPasteBinUpdateDirect())
									a.JumpToWord(text, focused ? 1 : 2);
								else
									a.textToSetOnFocus = text;
								mPreviousCBContent = content;
								a.lastClickTime = System.currentTimeMillis();
							}
							catch (Exception e) {
								CMN.Log("ClipListener:" + e);
							}
					};
				}
				//CMN.Log("clipboardManager.addPrimaryClipChangedListener");
				clipboardManager.removePrimaryClipChangedListener(ClipListener);
				clipboardManager.addPrimaryClipChangedListener(ClipListener);
			}
			else {
				pasteBin.setVisibility(View.GONE);
				if(ClipListener!=null)
					clipboardManager.removePrimaryClipChangedListener(ClipListener);
			}
			if (GlobalOptions.isLarge) {
				myAdapter.notifyDataSetChanged();
			}
		}
	}
	
	long[] bnPos = new long[3];
	public BasicAdapter adaptermy;
	
	public WeakReference<AlertDialog> aboutDlg = ViewUtils.DummyRef;
	
	@Override
	public void onClick(View v) {
		if(!a.systemIntialized) return;
		int id = v.getId();
		switch(id) {
			case R.id.server:
				a.launchSettings(ServerPreference.id, 0);
			return;
			case R.string.about:
			case R.id.menu_item_setting:{
				AlertDialog d = aboutDlg.get();
				if (d == null || d.isDark != GlobalOptions.isDark || mdict.error_input!=null) {
					d = new AlertDialog.Builder(a)
							.setPositiveButton("取消", (dialog, which) -> a.wordPopup.stopTask())
							.setNegativeButton("检查更新", null)
							.setMessage("关于")
							.setTitle("应用信息")
							.show();
					d.isDark = GlobalOptions.isDark;
					Button btn = d.findViewById(android.R.id.button2);
					btn.setOnClickListener(v1 -> {
						AlertDialog dd = (AlertDialog) ViewUtils.getWeakRefObj(aboutDlg);
						dd.setCanceledOnTouchOutside(false);
						dd.setCancelable(false);
						a.wordPopup.startTask(TASK_UPD_SCH);
						btn.setText(btn.getText()+"……");
					});
					//a.mDrawerLayout.closeDrawer(GravityCompat.START);
					String infoStr = getString(R.string.infoStr, BuildConfig.VERSION_NAME+(BuildConfig.isDebug?"工程调试版":""));
					if (mdict.error_input!=null) {
						infoStr += "\n出错信息：" + mdict.error_input;
					}
					TextView tv = d.mAlert.mMessageView;
					XYTouchRecorder xyt = PDICMainAppOptions.setAsLinkedTextView(tv, false, false);
					xyt.clickInterceptor = (view, span) -> {
						if (span instanceof LinkSpan) {
							String url = ((LinkSpan) span).getURL();
							if ("kaiyuan".equals(url)) {
								a.launchSettings(6, 1297);
							} else if ("rizhi".equals(url)) {
								a.showUpdateInfos(null);
							} else if ("taolun".equals(url)) {
								try {
									WebViewListHandler weblist = a.getRandomPageHandler(true, false, null);
									WebViewmy randomPage = weblist.getMergedFrame();
									BookPresenter socialbook = a.new_book(a.defDicts1[1], a);
									weblist.getMergedFrame(socialbook);
									socialbook.renderContentAt(-1, BookPresenter.RENDERFLAG_NEW, 0, randomPage, 0);
									weblist.setViewMode(null, 0, randomPage);
									weblist.viewContent();
								} catch (Exception e) {
									CMN.debug(e);
								}
							}
						}
						return true;
					};
					Markwon markwon = Markwon.create(a);
					markwon.setMarkdown(tv, infoStr);
					d.tag = btn;
					aboutDlg = new WeakReference<>(d);
				} else {
					d.setCanceledOnTouchOutside(true);
					d.setCancelable(true);
					Button btn = (Button) d.tag;
					btn.setText("检查更新");
					ViewUtils.setVisible(btn, true);
				}
				d.show();
				d.mAlert.mMessageView.requestFocus();
				//android.view.WindowManager.LayoutParams lp = d.getWindow().getAttributes();  //获取对话框当前的参数值
				//lp.height = -2;
				//d.getWindow().setAttributes(lp);
			} break;
			//退出
			case R.string.exit:
			case R.id.menu_item_exit:
				a.showExitDialog(false);
				break;
			case R.string.clip_board:
			case R.id.pastebin:{//剪贴板对话框
				if(ClipboardList ==null){
					ClipboardList = new ListView(a.getBaseContext());
					View pastebin_header = getLayoutInflater().inflate(R.layout.pastebin_header,null);
					decorateHeader(pastebin_header);
					ArrayAdapter<String> adapter = new ArrayAdapter<>(a.getBaseContext(), R.layout.simple_column_litem, android.R.id.text1, mClipboard);
					ClipboardList.addHeaderView(pastebin_header);
					ClipboardList.setAdapter(adapter);
					ClipboardList.setPadding(0,0,0, (int) (5*a.dm.density));
					ClipboardList.setOnItemClickListener((p, view, position, id1) -> {//操作剪贴板
						int hc = ClipboardList.getHeaderViewsCount();
						if(position>= hc && position<hc+mClipboard.size()){
							a.etSearch.setText(mClipboard.get(position-hc));
							a.d.dismiss();
						}
					});
				}
				if(ClipboardList.getParent()!=null)
					((ViewGroup) ClipboardList.getParent()).removeView(ClipboardList);
				androidx.appcompat.app.AlertDialog.Builder dialog_builder = new  androidx.appcompat.app.AlertDialog.Builder(a);
				dialog_builder.setView(ClipboardList);
				androidx.appcompat.app.AlertDialog dTmp = dialog_builder.create();
				Window win = dTmp.getWindow();
				if (win != null) {
					a.fix_full_screen(win.getDecorView());
					win.setDimAmount(0);
				}
				dTmp.show();
				dTmp.setOnDismissListener(dialog -> a.checkFlags());
				a.d=dTmp;
				ClipboardList.addOnLayoutChangeListener(MainActivityUIBase.mListsizeConfiner.setMaxHeight((int) (a.root.getHeight()-a.root.getPaddingTop()-2.8*getResources().getDimension(R.dimen._50_))));
			} break;
			//模糊搜索 全文搜索
			case R.string.fuzzyret1:
			case R.string.fullret:  {
				a.switchToSearchModeDelta((id==R.string.fuzzyret1)?100:-100);
				a.UIData.drawerLayout.closeDrawer(GravityCompat.START);
				a.etSearch.requestFocus();
				((InputMethodManager)a.getSystemService( Context.INPUT_METHOD_SERVICE )).toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
			} break;
			//书签历史
			case R.string.bookmarkH:  {
				String BKHistroryVagranter = a.opt.getString("bkHVgrts", "");// must 0<..<20
				//CMN.Log(BKHistroryVagranter);
				String[] items = BKHistroryVagranter.split(";");
				String[] entryNames = new String[items.length];
				AlertDialog.Builder builder = new AlertDialog.Builder(a);
				builder.setTitle(R.string.bookmarkH);
				builder.setSingleChoiceItems(new String[] {}, 0,
						(dialog, position1) -> d.getListView().postDelayed(new Runnable() {
							@Override
							public void run() {
								retrieveBnPos(bnPos, items[position1]);
								BookPresenter markedBook = a.getBookById(bnPos[0]);
								if(markedBook!=null) {
									//a.swicthToBook(markedBook);
									if(a.dictPicker !=null) a.dictPicker.isDirty=true;
									a.bWantsSelection=false;
									
									if(adaptermy==null)
										adaptermy = new ListViewAdapter(a, null, null);
									adaptermy.bOnePageNav=markedBook.getType()== DictionaryAdapter.PLAIN_BOOK_TYPE.PLAIN_TYPE_PDF;
									adaptermy.setPresenter(markedBook);
									int pos = (int) bnPos[2];
									String entryName = entryNames[position1];
									if(markedBook.getIsWebx()) {
										PlainWeb webx = markedBook.getWebx();
										pos = 0;
										// entryName is the save url
										if(entryName==null) entryName=webx.getHost();
										if(entryName!=null && !entryName.contains(":")) {
											entryName=webx.getHost()+entryName;
										}
										markedBook.SetSearchKey(entryName);
									} else {
										// 名称验证
										if (entryName!=null && !TextUtils.equals(mdict.processText(entryName), mdict.processText(markedBook.bookImpl.getEntryAt(pos)))) {
											// 重新查询
											int tmp_pos = markedBook.bookImpl.lookUp(entryName);
											if (tmp_pos>=0) {
												pos = tmp_pos;
											}
										}
									}
									adaptermy.onItemClick(pos);
									adaptermy.bOnePageNav=false;
									a.lv.setSelection(pos);
									d.hide();
								}}
						}, 0));
				d=builder.create();
				d.show();
				
				d.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
				d.setOnDismissListener(Drawer.this::onDismiss);
				d.getListView().setAdapter(new ArrayAdapter<String>(a,
						R.layout.singlechoice, android.R.id.text1, items) {
					@NonNull
					@Override
					public View getView(int position, View convertView,
										@NonNull ViewGroup parent) {
						View ret =  super.getView(position, convertView, parent);
						CheckedTextViewmy tv;
						if(ret.getTag()==null)
							ret.setTag(tv = ret.findViewById(android.R.id.text1));
						else
							tv = (CheckedTextViewmy)ret.getTag();
						
						retrieveBnPos(bnPos, items[position]);
						
						if(GlobalOptions.isDark)
							tv.setTextColor(Color.WHITE);
						
						BookPresenter markedBook = a.getBookById(bnPos[0]);
						
						String entryName = entryNames[position];
						if(entryName==null) {
							//数据库获取书签名
							try (Cursor cursor = a.prepareHistoryCon().getDB().rawQuery("select lex from "
									+ LexicalDBHelper.TABLE_BOOK_NOTE_v2 +" where id=?", new String[]{bnPos[1]+""})){
								if(cursor.moveToNext()) {
									entryName = entryNames[position] = cursor.getString(0);
								}
							} catch (Exception e) {
								CMN.Log(e);
							}
						}
						
						if(markedBook!=null) {
							if(entryName==null) {
								entryName = entryNames[position] = markedBook.getBookEntryAt((int) bnPos[2]);
							}
							tv.setSubText(markedBook.getDictionaryName());
						} else {//获取词典失败
							tv.setSubText("failed to fetch: "+id);
						}
						tv.setText(entryName);
						return ret;
					}
				});
			} break;
			//书签
			case R.string.lastmarks:  {
				String BKHistroryVagranter = a.opt.getString("bkHVgrts", "");// must 0<..<20
				retrieveBnPos(bnPos, BKHistroryVagranter);
				BookPresenter markedBook = a.getBookById(bnPos[0]);
				if(markedBook!=null) {
					if(adaptermy==null)
						adaptermy = new ListViewAdapter(a, null, null);
					//a.bOnePageNav=mdTmp instanceof bookPresenter_pdf; nimp
					int pos=(int) bnPos[2];
					String entryName = null;
					//数据库获取书签名 1
					try (Cursor cursor = a.prepareHistoryCon().getDB().rawQuery("select lex from "
							+ LexicalDBHelper.TABLE_BOOK_NOTE_v2 +" where id=?", new String[]{bnPos[1]+""})){
						if(cursor.moveToNext()) {
							entryName = cursor.getString(0);
						}
					} catch (Exception e) {
						entryName = markedBook.getBookEntryAt((int) bnPos[2]);
					}
					if(markedBook.getIsWebx()) {
						PlainWeb webx = markedBook.getWebx();
						pos = 0;
						// entryName is the save url
						if(entryName==null) entryName=webx.getHost();
						if(entryName!=null && entryName.indexOf(":")<0) {
							entryName=webx.getHost()+entryName;
						}
						markedBook.SetSearchKey(entryName);
					} else {
						// 名称验证 1
						if (entryName!=null && !TextUtils.equals(mdict.processText(entryName), mdict.processText(markedBook.bookImpl.getEntryAt(pos)))) {
							// 重新查询 1
							int tmp_pos = markedBook.bookImpl.lookUp(entryName);
							if (tmp_pos>=0) {
								pos = tmp_pos;
							}
						}
					}
					adaptermy.setPresenter(markedBook);
					adaptermy.onItemClick(pos);
				}
			} break;
			//追加词典 添加词典 打开
			case R.string.addd:  {
				if(false) { // MLSN
					a.startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
							.setType("*/*"), Constants.OpenBookRequset);
				}
				else {
					boolean AutoFixLostRecords = true;
					DialogProperties properties = new DialogProperties();
					properties.selection_mode = DialogConfigs.SINGLE_MULTI_MODE;
					properties.selection_type = DialogConfigs.FILE_SELECT;
					properties.root = new File("/");
					properties.error_dir = new File(Environment.getExternalStorageDirectory().getPath());
					properties.offset = filepickernow!=null?filepickernow:a.opt.lastMdlibPath;
					properties.opt_dir=new File(a.opt.pathToDatabases()+"favorite_dirs/");
					properties.opt_dir.mkdirs();
					properties.extensions = new HashSet<>();
					properties.extensions.add(".mdx");
					properties.extensions.add(".web"); // 在线词典，JSON格式。
					properties.extensions.add(".dsl");
					properties.extensions.add(".dz");
					properties.extensions.add(".pdf");
					if(toPDF) {
						properties.extensions.add(".mdd");
						properties.extensions.add(".txt");
					}
					properties.title_id = R.string.addd;
					properties.isDark = a.AppWhite==Color.BLACK;
					FilePickerDialog dialog = new FilePickerDialog(a, properties);
					HashSet<String> mdictInternal = new HashSet<>();
					dialog.setDialogSelectionListener(new DialogSelectionListener() {
						@Override
						public void
						onSelectedFilePaths(String[] files, File now) {
							//CMN.debug(files);
							if(now!=null) {
								for(PlaceHolder phI:a.lazyLoadManager().placeHolders) {
									mdictInternal.add(phI.getPath(a.opt).getPath());
								}
								filepickernow = now;
								if(files.length>1) {
									File ConfigFile = a.opt.fileToConfig();
									File rec = a.opt.fileToDecords(ConfigFile);
									a.ReadInMdlibs(rec);
									boolean hasSame=false;
									for (int i = 0; i < files.length; i++) {
										String fnI = files[i];
										if (!mdictInternal.contains(fnI)) {
											File fI = new File(fnI);
											fnI = mFile.tryDeScion(fI, a.opt.lastMdlibPath);
											if (a.mdlibsCon.contains(fnI)) {
												hasSame=true;
												break;
											}
										}
									}
									CMN.debug("hasSame::", hasSame, a.mdlibsCon);
									if(hasSame) {
										new AlertDialog.Builder(a)
												.setTitle("是否跳过打开过的词典？")
												.setPositiveButton("是", (dialog12, which) -> {
													ArrayList<String> arr = new ArrayList<>(Arrays.asList(files));
													for (int i = files.length-1; i>=0; i--) {
														String fnI = files[i];
														File fI = new File(fnI);
														fnI = mFile.tryDeScion(fI, a.opt.lastMdlibPath);
														if (a.mdlibsCon.contains(fnI)) {
															arr.remove(i);
														}
													}
													onSelectedFilePaths(arr.toArray(files), null);
												})
												.setNegativeButton("否", (dialog12, which) -> {
													onSelectedFilePaths(files, null);
												})
												.show();
										return;
									}
								}
							}
							if(files.length>0) {
								int newAdapterIdx=-1;
								int bscAdapterIdx=-1;
								final File def = a.opt.getCacheCurrentGroup()?new File(a.getExternalFilesDir(null),"default.txt")
										:a.getStartupFile(a.opt.fileToConfig());      //!!!原配
								File ConfigFile = a.opt.fileToConfig();
								File rec = a.opt.fileToDecords(ConfigFile);
								a.ReadInMdlibs(rec);
								HashMap<String, String> add_book_checker = a.lostFiles;
								
								HashSet<String> renameRec = new HashSet<>();
								HashMap<String,String> renameList = new HashMap<>();
								
								try {
									BufferedWriter output = new BufferedWriter(new FileWriter(rec,true));
									BufferedWriter output2 = null;
									int countAdd=0;
									int countRename=0;
									String removedAPath;
									boolean bNextPlaceHolder = false;
									for (int i = 0; i < files.length; i++) {
										String fnI = files[i];
										if(fnI==null) break;
										File fI = new File(fnI);
										CMN.debug("AddFiles", fnI, mdictInternal.contains(fI.getPath()));
										if(fI.isDirectory()) continue;
										//checker.put("sound_us.mdd", "/storage/emulated/0/PLOD/mdicts/发音库/sound_us.mdd");
										/* 检查文件名称是否乃记录之中已失效项，有则需重命名。*/
										if(!bNextPlaceHolder && AutoFixLostRecords && (removedAPath=add_book_checker.get(fI.getName()))!=null) {
											renameList.put(removedAPath, fnI);
											renameRec.add(fnI);
										}
										/* 追加不存于当前分组的全部词典至全部记录与缓冲组。 */
										else if(!mdictInternal.contains(fI.getPath())) {
											try {
												BookPresenter newBook;
												if (bNextPlaceHolder) {
													a.AddIndexingBookIdx(-1, a.loadManager.md_size);
													newBook = null;
													bNextPlaceHolder=false;
												} else {
													newBook=MainActivityUIBase.new_book(fnI, a);
												}
												PlaceHolder phI = new PlaceHolder(fnI);
												a.loadManager.addBook(newBook, phI);
												if(newAdapterIdx==-1) newAdapterIdx = a.loadManager.md_size-1;
												String raw=fnI;
												fnI = mFile.tryDeScion(fI, a.opt.lastMdlibPath);
												if(output2==null){
													boolean def_exists = def.exists();
													//tofo check
													output2 = new BufferedWriter(new FileWriter(def,true));
													if(!def_exists) { // 莫忘添加默认
														for (int j = 0; j < a.loadManager.md_size-1; j++) {
															BookPresenter book = a.loadManager.md_get(j);
															if (book!=a.EmptyBook) {
																output2.write(book.getPath());
																output2.write("\n");
															}
														}
													}
												}
												output2.write(fnI);
												output2.write("\n");
												output2.flush();
												/* 需追加至全部记录而无须重命名者 */
												if(a.mdlibsCon.add(fnI) && !renameRec.contains(raw)) {
													output.write(fnI);
													output.write("\n");
												}
												countAdd++;
											} catch (Exception e) {
												if(e instanceof IllegalStateException && "Needs Index Building!".equals(e.getMessage())) {
													bNextPlaceHolder=true;
													i--;
												} else {
													CMN.Log(e);
													a.showT("词典 "+new File(fnI).getName()+" 加载失败 @"+fnI+" Load Error！ "+e.getLocalizedMessage());
												}
											}
										}
										else if(newAdapterIdx==-1 && bscAdapterIdx==-1) {
											for (int j = 0; j < a.loadManager.md_size; j++) {
												PlaceHolder phI = a.loadManager.getPlaceHolderAt(j);
												if(fI.equals(phI.getPath(a.opt))) {
													bscAdapterIdx = j;
													break;
												}
											}
										}
									}
									CMN.Log(add_book_checker);
									CMN.Log(renameRec.toString());
									if(a.dictPicker !=null) {
										a.dictPicker.dataChanged();
										a.dictPicker.isDirty=true;
									}
									output.close();
									if(output2!=null) {
										output2.close();
									}
									renameRec.clear();
									
									for (PlaceHolder phI:a.lazyLoadManager().placeHolders){
										String newPath = renameList.get(phI.getPath(a.opt));
										if(newPath!=null){
											PlaceHolder phTmp = new PlaceHolder(newPath);
											phI.pathname = phTmp.pathname;
										}
									}
									
									if(AutoFixLostRecords && renameList.size()>0){
										ArrayList<File> moduleFullScannerArr;
										File[] moduleFullScanner = ConfigFile.listFiles(pathname -> !SU.isNotGroupSuffix(pathname.getPath()));
										moduleFullScannerArr = new ArrayList<>(Arrays.asList(moduleFullScanner));
										moduleFullScannerArr.add(rec);
										moduleFullScannerArr.add(def);
										StringBuilder sb = a.MainStringBuilder;
										AgentApplication app = ((AgentApplication) a.getApplication());
										char[] cb = app.get4kCharBuff();
										for(File fI:moduleFullScannerArr) {
											sb.setLength(0);
											String line;
											boolean bNeedRewrite=false;
											try {
												ReusableBufferedReader br = new ReusableBufferedReader(new FileReader(fI), cb, 4096);
												while((line = br.readLine()) != null) {
													String prefix = null;
													if(line.startsWith("[:")){
														int idx = line.indexOf("]",2);
														if(idx>=2) {
															idx+=1;
															prefix = line.substring(0, idx);
															line = line.substring(idx);
														}
													}
													/* from old path to neo path */
													String finder = renameList.get(line.startsWith("/")?line:a.opt.lastMdlibPath+"/"+line);
													CMN.Log(fI.getName(), "{重命名??}", finder, line);
													if(finder!=null){
														line=mFile.tryDeScion(new File(finder), a.opt.lastMdlibPath);
														if(prefix!=null){
															line=prefix+line;
														}
														bNeedRewrite = true;
														countRename++;
														CMN.Log(fI.getName(), "{当重命名之}", finder, line);
													}
													sb.append(line).append("\n");
												}
												br.close();
												cb=br.cb;
												if(bNeedRewrite) {
													ReusableBufferedWriter bw = new ReusableBufferedWriter(new FileWriter(fI), cb, 4096);
													bw.write(sb.toString());
													bw.flush(); bw.close();
													cb=br.cb;
												}
											} catch (IOException e) {
												e.printStackTrace();
											}
										}
										app.set4kCharBuff(cb);
										renameList.clear();
										add_book_checker.clear();
									}
									if (countRename > 0) {
										a.showT("新加入" + countAdd + "本词典, 重定位" + countRename + "次！");
										a.loadManager.md_size = 0;
										a.populateDictionaryList();
									} else {
										a.showT("新加入" + countAdd + "本词典！");
									}
									if (countAdd > 0) { // 修复新打开的词典不参与联合搜索
										a.adaptermy2.currentKeyText = null; //todo
									}
									if (newAdapterIdx==-1) {
										newAdapterIdx = bscAdapterIdx;
									}
									if(newAdapterIdx!=-1) {
										a.UIData.drawerLayout.closeDrawer(GravityCompat.START);
										a.switch_Dict(newAdapterIdx, true, true, null);
									}
								} catch (Exception e) {
									CMN.debug(e);
								}
							}
						}
						
						@Override
						public void onEnterSlideShow(Window win, int delay) {
						
						}
						
						@Override
						public void onExitSlideShow() {
						
						}
						
						@Override
						public Activity getDialogActivity() {
							return null;
						}
						
						@Override
						public void onDismiss() {
						
						}
					});
					dialog.show();
					dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
					
					a.d = dialog;
				}
				//.dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
			} break;
			//主目录
			case R.string.pick_main:  {
				DialogProperties properties1 = new DialogProperties();
				properties1.selection_mode = DialogConfigs.SINGLE_MODE;
				properties1.selection_type = DialogConfigs.DIR_SELECT;
				properties1.root = new File("/");
				properties1.error_dir = Environment.getExternalStorageDirectory();
				properties1.offset = a.opt.lastMdlibPath;
				//CMN.show(a.opt.lastMdlibPath+":"+Environment.getExternalStorageDirectory().getAbsolutePath());
				properties1.opt_dir=new File(a.opt.pathToDatabases().append("favorite_dirs/").toString());
				properties1.opt_dir.mkdirs();
				properties1.title_id=R.string.pick_main;
				//properties1.extensions = new String[] {"mdx"};
				FilePickerDialog dialog1 = new FilePickerDialog(a, properties1);
				dialog1.setDialogSelectionListener(new DialogSelectionListener() {
					@Override
					public void
					onSelectedFilePaths(String[] files, File n) {
						if(files.length>0) {
							a.pendingModPath(new File(files[0]).getAbsolutePath());
						}
					}
					
					@Override
					public void onEnterSlideShow(Window win, int delay) {
					
					}
					
					@Override
					public void onExitSlideShow() {
					
					}
					
					@Override
					public Activity getDialogActivity() {
						return null;
					}
					
					@Override
					public void onDismiss() {
					
					}
				});
				dialog1.show();
			} break;
			//词典管理中心
			case R.string.manager:  {
				a.showDictionaryManager();
			} break;
			//切换单词本
			case R.string.switch_favor:  {
				a.showPickFavorFolder();
			} break;
			case R.string.settings:  {
				((AgentApplication)a.getApplication()).opt=a.opt;
				a.launchSettings(0, 1297);
			} break;
		}
	}
	
	private void retrieveBnPos(long[] bnPos, String text) {
		CharSequenceKey wrap = new CharSequenceKey(text, 0, 0);
		int idx;
		idx=text.indexOf(",");
		bnPos[0] = IU.TextToNumber_SIXTWO_LE(wrap.reset(0, idx));
		idx=text.indexOf(",", idx+1);
		bnPos[1] = IU.TextToNumber_SIXTWO_LE(wrap.reset(wrap.getEnd()+1, idx));
		idx=text.indexOf(";", idx+1);
		bnPos[2] = IU.TextToNumber_SIXTWO_LE(wrap.reset(wrap.getEnd()+1, idx));
	}
	
	@Override
	public void onDismiss(DialogInterface dialog) {
		d = null;
	}
	
	public static String getIP(Context context) throws SocketException {
		for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
			NetworkInterface intf = en.nextElement();
			for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();)
			{
				InetAddress inetAddress = enumIpAddr.nextElement();
				if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address))
				{
					return inetAddress.getHostAddress();
				}
			}
		}
		return null;
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch(buttonView.getId()) {
			case R.id.sw1:{
				if(isChecked) {
					a.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
					 {
						View decorView = a.getWindow().getDecorView();
						int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
								| View.SYSTEM_UI_FLAG_FULLSCREEN;
						if(PDICMainAppOptions.isFullscreenHideNavigationbar()) uiOptions|=View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
								| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
								| View.SYSTEM_UI_FLAG_LOW_PROFILE
								| View.SYSTEM_UI_FLAG_IMMERSIVE;
						decorView.setSystemUiVisibility(uiOptions);
					}
				}else {
					a.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
					View decorView = a.getWindow().getDecorView();
					int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
							View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
					decorView.setSystemUiVisibility(uiOptions);
				}
				a.opt.setFullScreen(isChecked);
			} break;
			case R.id.sw2:{
				PDICMainAppOptions.setEnableSuperImmersiveScrollMode(isChecked);
				a.setNestedScrollingEnabled(isChecked);
				adjustBottomPadding();
			} break;
			case R.id.sw3:{
				a.opt.setServerStarted(isChecked);
				//a.viewPager.setNoScroll(isChecked);
				a.startServer(isChecked);
				if(isChecked) {
					if(HeaderView2==null) {
						HeaderView2 = a.getLayoutInflater().inflate(R.layout.activity_main_navi_server_header, null);
						HeaderView2.setOnClickListener(this);
						if(GlobalOptions.isDark) {
							HeaderView2.getBackground().setColorFilter(GlobalOptions.NEGATIVE);
							((TextView)HeaderView2.findViewById(R.id.text)).setTextColor(a.AppBlack);
						}
					}
					if(HeaderView2.getParent()==null) {
						mDrawerList.addHeaderView(HeaderView2);
					}
					FlowTextView ipView = (FlowTextView) HeaderView2.findViewById(R.id.subtext);
					ipView.trim=false;
					try {
						ipView.setText("http://"+getIP(getContext())+":"+a.getMdictServer().getListeningPort());
					} catch (Exception e) {
						ipView.setText(""+e);
					}
				} else if(HeaderView2!=null && HeaderView2.getParent()!=null) {
					mDrawerList.removeHeaderView(HeaderView2);
				}
			} break;
			/* 切换黑暗模式 */
			case R.id.sw4:{
				setInDarkMode(isChecked, true);
			} break;
			case R.id.sw5:{
				a.opt.setUseVolumeBtn(isChecked);
			} break;
		}
	}
	
	public void setInDarkMode(boolean dark, boolean set) {
		if(set) {
			a.opt.setInDarkMode(dark);
			a.changeToDarkMode();
		} else {
			sw4.setOnCheckedChangeListener(null);
			sw4.setChecked(dark);
			sw4.setOnCheckedChangeListener(this);
		}
		if(HeaderView2!=null) {
			HeaderView2.getBackground().setColorFilter(dark?GlobalOptions.NEGATIVE:null);
			((TextView)HeaderView2.findViewById(R.id.text)).setTextColor(a.AppBlack);
		}
		refreshBtns(dark);
	}
	
	private void refreshBtns(boolean dark) {
		if (!GlobalOptions.isLarge) {
			ColorMatrixColorFilter cf = dark ? GlobalOptions.NEGATIVE : null;
			((ImageView)menu_item_setting.getChildAt(0)).setColorFilter(cf);
			((ImageView)menu_item_exit.getChildAt(0)).setColorFilter(cf);
			((TextView)menu_item_setting.getChildAt(1)).setTextColor(a.AppBlack);
			((TextView)menu_item_exit.getChildAt(1)).setTextColor(a.AppBlack);
		}
	}
	
	@Override
	public boolean onLongClick(View v) {
		int id = v.getId();
		if(id==R.string.addd) {
			toPDF=true;
			((TextView)v.findViewById(R.id.subtext)).setText("Oh PDF !");
			return false;
		} else if(id==R.string.pick_main) {
			try { // MLSN
				a.startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
						.addCategory(Intent.CATEGORY_OPENABLE)
						.setType("*/*"), Constants.OpenBooksRequset);
			} catch (Exception e) {
				a.showT(e.getMessage());
			}
			return true;
		} else if(id==R.id.sw4) {
			a.launchSettings(NightMode.id, NightMode.requestCode);
			return true;
		}
		Intent i = new Intent(getActivity(), CuteFileManager.class);
		startActivity(i);
		return false;
	}
	
	public void decorateHeader(View footchechers) {
		PDICMainActivity a = ((PDICMainActivity) getActivity()); if(a==null) return;
		View.OnClickListener clicker = v -> {
			int id = v.getId();
			CheckBox ck = (CheckBox) v;
			switch (id) {
				case R.id.CKPBEnable:
					a.opt.setPasteBinEnabled(ck.isChecked());
					break;
				case R.id.CKPBUpdate:{
					a.opt.setPasteBinUpdateDirect(ck.isChecked());
				} break;
				case R.id.CKPBBringBack:{
					a.opt.setPasteBinBringTaskToFront(ck.isChecked());
				} break;
			}
		};
		CheckBox ck = footchechers.findViewById(R.id.CKPBEnable);//粘贴至翻阅模式
		ck.setChecked(a.opt.getPasteBinEnabled());
		ck.setOnClickListener(clicker);

		if(PDICMainAppOptions.getPasteToPeruseModeWhenFocued()) ck.setTextColor(getResources().getColor(R.color.colorAccent));
		ck.setOnLongClickListener(v -> {
			CheckBox ck1 = (CheckBox) v;
			if(PDICMainAppOptions.setPasteToPeruseModeWhenFocued(!PDICMainAppOptions.getPasteToPeruseModeWhenFocued()))
				ck1.setTextColor(getResources().getColor(R.color.colorAccent));
			else
				ck1.setTextColor(Color.BLACK);
			return true;
		});

		ck = footchechers.findViewById(R.id.CKPBUpdate);
		ck.setChecked(a.opt.getPasteBinUpdateDirect());
		ck.setOnClickListener(clicker);
		ck = footchechers.findViewById(R.id.CKPBBringBack);
		ck.setChecked(a.opt.getPasteBinBringTaskToFront());
		ck.setOnClickListener(clicker);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		ClipboardManager clipboardManager = (ClipboardManager) a.getSystemService(Context.CLIPBOARD_SERVICE);
		if(clipboardManager!=null && ClipListener!=null)
			clipboardManager.removePrimaryClipChangedListener(ClipListener);
		if(PasteFileTarget!=null) {
			try {
				PasteFileTarget.close();
			} catch (Exception ignored) { }
		}
	}
}
