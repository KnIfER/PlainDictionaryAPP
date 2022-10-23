package com.knziha.plod.PlainUI;

import android.annotation.SuppressLint;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.view.VU;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jaredrummler.colorpicker.ColorPickerDialog;
import com.jaredrummler.colorpicker.ColorPickerListener;
import com.knziha.ankislicer.customviews.ShelfLinearLayout2;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.widgets.TextMenuView;
import com.knziha.plod.widgets.ViewUtils;
import com.knziha.plod.widgets.WebViewmy;
//import static com.knziha.plod.PlainUI.JsonNames.*;
import com.knziha.text.ColoredTextSpan;

import org.apache.commons.lang3.ArrayUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;

public class AnnotationDialog implements View.OnClickListener, ColorPickerListener, SeekBar.OnSeekBarChangeListener, AdapterView.OnItemClickListener, PopupMenuHelper.PopupMenuListener, View.OnLongClickListener {
	MainActivityUIBase a;
	ColorPickerDialog noteDlg;
	Button btnEditNotes; // ante
	public ViewGroup editPanel;
	public View botPad;
	public ViewGroup editTools;
	public ShelfLinearLayout2 editToolbar;
	public EditText edit;
	public ViewGroup lnkPanel;
	public ViewGroup noteTypes;
	public View bubbleBtn;
	public TextView lnkTo;
	public ViewGroup btnPanel;
	View cv;
	TextView alphaText;
	SeekBar alphaSeek;
	Button[] btnTypes;
	AlertDialog tkShow;
	public WebViewmy mWebView;
	
	ColoredTextSpan[] spans;
	protected int MainAppBackground;
	private AnimationSet animationEnter;
	private AnimationSet animationExit;
	public long editingNoteId = 0;
	
	public static class UIData {
		public int toolIdx;
		public int[] colors = new int[8];
		public int[] alphaLocks = new int[8];
		public boolean[] showBubbles = new boolean[3];
		public int noteType; // 1=正文; 2=气泡; 3=脚注
		public int[] bubbleColors = new int[8];
		public int[] fontColors = new int[8];
		public int[] fontSizes = new int[8];
		public boolean[] fontSizesEnabled = new boolean[3];
		public boolean[] bubbleColorsEnabled = new boolean[3];
		public boolean[] fontColorEnabled = new boolean[3];
		public boolean noteOnBubble;
	}
	
	public UIData uiData;
	
	public AnnotationDialog(MainActivityUIBase a) {
		this.a = a;
	}
	
	public void show(WebViewmy wv, int type, boolean showAnteNotes) {
		tkShow = a.ucc == null || a.ucc.detached() ? null : a.ucc.getDialog();
		mWebView = wv;
		if(tkShow!=null) tkShow.hide();
		if (uiData==null) {
			uiData = new UIData();
			readUIData();
		}
		if (edit==null) {
			ViewGroup cv = (ViewGroup) a.getLayoutInflater().inflate(R.layout.create_note_view, a.root, false);
			lnkToAdapter = new ArrayAdapter<>(a, R.layout.popup_list_item, new String[]{a.mResource.getString(R.string.lnk_note)});
			binAdapter = new ArrayAdapter<>(a, R.layout.popup_list_item, new ArrayList<>(Collections.singletonList("展开笔记到气泡中")));
			editPanel = (ViewGroup) ViewUtils.findViewById(cv, R.id.editPanel);
			edit = (EditText) ViewUtils.findViewById(editPanel, R.id.edit, 1);
			editTools = (ViewGroup) ViewUtils.findViewById(editPanel, R.id.editTools);
			botPad = ViewUtils.findViewById(editPanel, R.id.botPad);
			editToolbar = (ShelfLinearLayout2) editTools.getChildAt(0);
			noteTypes = editToolbar.findViewById(R.id.noteTypes);
			bubbleBtn = editToolbar.findViewById(R.id.bubble);
			lnkPanel = editPanel.findViewById(R.id.lnkPanel);
			lnkTo = editPanel.findViewById(R.id.lnkTo);
			btnPanel = (ViewGroup) ViewUtils.findViewById(cv, R.id.btnPanel);
			
			View alphaPanel = ViewUtils.findViewById(cv, R.id.alpha);
			alphaText = alphaPanel.findViewById(R.id.alphaLock);
			alphaSeek = alphaPanel.findViewById(R.id.alphaSeek);
			
			ViewUtils.setOnClickListenersOneDepth(cv, this, 999, null);
			btnTypes = new Button[]{btnPanel.findViewById(R.id.btnH), btnPanel.findViewById(R.id.btnU)};
			ColorPickerDialog noteDlg = ColorPickerDialog.newInstance(a, uiData.colors[uiData.toolIdx]);
			noteDlg.setContentView(cv, true);
			this.noteDlg = noteDlg;
			
			btnEditNotes = VU.craftBtn(a, R.id.book_notes_drawer, R.string.edit_yy_note, null);
			View pad = VU.craftLinearPadding(a, null);
			VU.linearLayoutParams(pad).weight = 2;
			VU.addViewToParent(btnEditNotes, noteDlg.bottomBar, 2);
			VU.addViewToParent(pad, noteDlg.bottomBar, 3);
			btnEditNotes.setOnClickListener(this);
			
			spans = new ColoredTextSpan[]{new ColoredTextSpan(0xffffaaaa)
					, new ColoredTextSpan(Color.BLACK, 8.f, 2)};
			
			for (int k = 0; k < 2; k++) {
				Button btn = btnTypes[k];
				SpannableStringBuilder ssb = new SpannableStringBuilder();
				ssb.append(btn.getText());
				ssb.setSpan(spans[k], 0, ssb.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
				btn.setText(ssb);
			}
			
			noteDlg.forceAlphaLock(true);
			noteDlg.setColorPickerListener(this);
			alphaSeek.setOnSeekBarChangeListener(this);
			btnTypes[type<0?uiData.toolIdx:type].performClick();
			noteTypes.setTag(this);
			noteTypes.getChildAt(uiData.noteType).performClick();
			noteTypes.setTag(null);
		}
		ViewUtils.setVisible(alphaSeek, PDICMainAppOptions.alphaLockVisible());
		ViewUtils.setVisible(editPanel, !showAnteNotes && (PDICMainAppOptions.editNote()||true));
		if (!noteDlg.isAdded()) {
			noteDlg.show(a.getSupportFragmentManager(), "note-dlg");
		}
		refresh();
		if (false) { //swap
			ViewUtils.removeView(editTools);
			ViewUtils.addViewToParent(editTools, editPanel, 1);
		}
		if (showAnteNotes) {
			ViewUtils.setVisible(btnEditNotes, true);
			a.hdl.postDelayed(btnEditNotes::performClick, 100);
		}
		else {
			mWebView.evaluateJavascript("NidsInRange(1)", value -> ViewUtils.setVisible(btnEditNotes, "1".equals(value)));
		}
		setEditingNote(-1);
		//ViewUtils.setVisible(btnEditNotes, true);
	}
	
	// click
	@SuppressLint("NonConstantResourceId")
	@Override
	public void onClick(View v) {
		CMN.debug("v::", v);
		if (v == alphaText) {
			PDICMainAppOptions.alphaLockVisible(!ViewUtils.isVisible(alphaSeek));
			ViewUtils.setVisible(alphaSeek, PDICMainAppOptions.alphaLockVisible());
		}
		else if (ViewUtils.getNthParentNonNull(v,1).getId()==R.id.btnPanel) {
			for (int k = 0; k < 2; k++) {
				if (btnTypes[k] == v) {
					if (uiData.toolIdx!=k) {
						PDICMainAppOptions.currentTool(uiData.toolIdx=k);
					}
					btnTypes[k].setAlpha(1);
					int lock = uiData.alphaLocks[uiData.toolIdx];
					noteDlg.alphaLock(lock);
					String zhsh = (lock * 100 / 255) + "%";
					if(zhsh.length()<3) zhsh = "0"+zhsh;
					alphaText.setText(zhsh);
					noteDlg.setPreviewColor(uiData.colors[uiData.toolIdx]);
					alphaSeek.setProgress(lock);
					noteDlg.datasetChanged();
				} else {
					btnTypes[k].setAlpha(0.1f);
				}
			}
		}
		switch (v.getId()) {
			case R.id.editNoteBtn:
			case R.id.itemview:
				pressedV = new WeakReference<>(v);
				if (PDICMainAppOptions.tapEditAnteNote() || v.getId()==R.id.editNoteBtn) {
					try {
						AnnotAdapter.VueHolder vh = (AnnotAdapter.VueHolder) v.getTag();
						AnnotAdapter.AnnotationReader reader = rangeAdapter.dataAdapter.getReaderAt(vh.vh.position);
						pressedRowId = reader.row_id;
						editPressedNote();
					} catch (Exception e) {
						CMN.debug(e);
					}
				} else {
					v.performLongClick();
				}
				break;
			case R.id.create_note:
				if (ViewUtils.isVisible(editPanel)) { // 先把编辑器收起来
					onClick(a.anyView(R.id.editShow));
				} else {
					noteDlg.dismiss();
				}
			break;
			case R.id.editShow:
				boolean edit = !ViewUtils.isVisible(editPanel);
				PDICMainAppOptions.editNote(edit);
				ViewUtils.setVisible(editPanel, edit);
				if (edit) {
					if(animationEnter==null)
						animationEnter = (AnimationSet) AnimationUtils.loadAnimation(a, R.anim.dp_dialog_enter);
					animationEnter.getAnimations().get(0).setDuration(200);
					editPanel.startAnimation(animationEnter);
				} else {
					if(animationExit==null)
					animationExit = (AnimationSet) AnimationUtils.loadAnimation(a, R.anim.dp_dialog_exit);
					animationExit.getAnimations().get(0).setDuration(200);
					editPanel.startAnimation(animationExit);
				}
			break;
			case R.id.etDone:
				noteDlg.btnConfirm.performClick();
			break;
			case R.id.lnkBtn:
				ViewUtils.setVisible(lnkPanel, false);
			break;
			case R.id.etClear:
				if (TextUtils.isEmpty(getText())) {
					getText().clear();
					getText().append((String) v.getTag());
				} else {
					v.setTag(getText().toString());
					getText().clear();
				}
			break;
			case R.id.etPaste:
				getText().append(a.getFloatBtn().getPrimaryClip());
			break;
			case R.id.bubble:
				if (uiData.noteType!=1) {
					boolean show = editToolbar.toggleViewChecked(v);
					uiData.showBubbles[uiData.noteType] = show;
					if (uiData.noteType==0) {
						PDICMainAppOptions.showBubbleForEmbedNote(show);
					}
					else if (uiData.noteType==2) {
						PDICMainAppOptions.showBubbleForFootNote(show);
					}
					editToolbar.invalidate();
				} else {
					editToolbar.setViewChecked(v, true);
				}
				//v.jumpDrawablesToCurrentState();
			break;
			case R.id.ntyp3:
				if (noteTypes.getTag() == null) { // 显示弹窗菜单
					if (shareListAda != lnkToAdapter || !showedMenuPopup()) {
						showLiteMenuPopup(lnkToAdapter, v);
					}
				}
			case R.id.ntyp1:
			case R.id.ntyp2:
				if (noteTypes.getTag() == null && v.getId()==R.id.ntyp2) {
					if (shareListAda != binAdapter || !showedMenuPopup()) {
						binAdapter.clear();
						binAdapter.add(uiData.noteOnBubble ?"展开笔记到气泡中 √ ":"展开笔记到气泡中");
						showLiteMenuPopup(binAdapter, v);
					}
				}
				for (int i = 0; i < noteTypes.getChildCount(); i++) {
					View childAt = noteTypes.getChildAt(i);
					editToolbar.setViewChecked(childAt, childAt==v);
					if (childAt==v && uiData.noteType!=i) {
						PDICMainAppOptions.currentNoteType(uiData.noteType=i);
					}
				}
				editToolbar.setViewChecked(bubbleBtn, uiData.showBubbles[uiData.noteType]);
				editToolbar.invalidate();
			break;
			case R.id.bubbleColor:
			case R.id.fontColor:
				showSubColorPicker(v.getId());
			break;
			case R.id.book_notes_drawer: // btnEditNotes 修改已有笔记
				mWebView.evaluateJavascript("NidsInRange(0)", value -> {
					CMN.debug("NidsInRange::", value);
					if (value.length() > 2) {
						try {
							showAnteNotes(value, v);
							return;
						} catch (Exception e) {
							CMN.debug(e);
						}
					}
					ViewUtils.setVisible(v, false);
				});
			break;
		}
	}
	
	// longclick
	@Override
	public boolean onLongClick(View v) {
		PopupMenuHelper popupMenu = a.getPopupMenu();
		if (popupMenu.getListener() != this)
		{
			int[] texts = new int[]{
					R.layout.menu_danji_xiugai
					, R.string.copy
					, R.string.send_dot
					, R.string.delete
			};
			popupMenu.initLayout(texts, this);
		}
		
		TextMenuView tv = popupMenu.popRoot.findViewById(R.id.editNote);
		if (tv.leftDrawable == null)
			tv.leftDrawable = a.mResource.getDrawable(R.drawable.ic_yes_blue);
		tv.setActivated(PDICMainAppOptions.tapEditAnteNote());
		
		pressedV = new WeakReference<>(v);
		try {
			AnnotAdapter.VueHolder vh = (AnnotAdapter.VueHolder) v.getTag();
			AnnotAdapter.AnnotationReader reader = rangeAdapter.dataAdapter.getReaderAt(vh.vh.position);
			pressedRowId = reader.row_id;
			View rv = noteDlg.getView();
			View vp = (View) v.getParent();
			popupMenu.showAt(rv, 0, vp.getHeight()-v.getTop()-v.getHeight(), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL);
			ViewUtils.preventDefaultTouchEvent(v, 0, 0);
		} catch (Exception e) {
			CMN.debug(e);
		}
		return false;
	}
	
	AnnotAdapter rangeAdapter;
	BaseAdapter rangeListAdapter;
	WeakReference<PopupMenuHelper> popupMenuRef = ViewUtils.DummyRef;
	WeakReference<View> pressedV = ViewUtils.DummyRef;
	long pressedRowId;
	public PopupMenuHelper getPopupMenu() {
		PopupMenuHelper ret = popupMenuRef.get();
		if (ret==null) {
			ret  = new PopupMenuHelper(a, null, null);
			popupMenuRef = new WeakReference<>(ret);
		}
		return ret;
	}
	
	private void showAnteNotes(String value, View v) {
		SQLiteDatabase db = a.prepareHistoryCon().getDB();
		value = value.substring(1, value.length() - 1);
		PopupMenuHelper anteNotesPopup = getPopupMenu();
		if (anteNotesPopup.getListener() != this) {
			anteNotesPopup.initLayout(ArrayUtils.EMPTY_INT_ARRAY, this);
			ListView lv = new ListView(a);
			anteNotesPopup.lv.addView(lv);
			if (rangeAdapter==null) {
				rangeAdapter = new AnnotAdapter(a, R.id.text1, db, -1, null, null);
			}
			anteNotesPopup.popRoot.setTag(lv);
			lv.setAdapter(rangeListAdapter=new BaseAdapter() {
				@Override public int getCount() { return rangeAdapter.getItemCount(); }
				@Override public Object getItem(int position) { return null; }
				@Override public long getItemId(int position) { return 0;	}
				@Override
				public View getView(int position, View convertView, ViewGroup parent) {
					AnnotAdapter.VueHolder vh;
					if (convertView == null) {
						vh = rangeAdapter.onCreateViewHolder(lv, 0);
						convertView = vh.itemView;
						convertView.setId(R.id.itemview);
						convertView.setBackground(a.getListChoiceBackground());
						convertView.setOnClickListener(AnnotationDialog.this);
						convertView.setOnLongClickListener(AnnotationDialog.this);
					} else {
						vh = (AnnotAdapter.VueHolder) convertView.getTag();
					}
					vh.vh.position = position;
					rangeAdapter.onBindViewHolder(vh, position);
					return convertView;
				}
			});
		}
		
		ListView lv = (ListView) anteNotesPopup.popRoot.getTag();
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) lv.getLayoutParams();
		rangeAdapter.rebuildCursor(db, null, null, value/*"1,2,3"*/);
		
		View rv = noteDlg.getView();
		int rw = rv.getWidth(), rh = rv.getHeight();
		lp.width = rw*2/3;
		lp.height = rh/2;
		anteNotesPopup.showAt(v, 0, (int) (v.getHeight()/2 + GlobalOptions.density*5), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
	}
	
	WeakReference<ColorPickerDialog> subDlgRef = ViewUtils.DummyRef;
	int mSubPickerId;
	int mSubPickerNTyp;
	/** 修改气泡颜色、字体色、字体大小 */
	private void showSubColorPicker(int id)
	{
		mSubPickerId = id;
		mSubPickerNTyp = uiData.noteType;
		ColorPickerDialog subDlg = subDlgRef.get();
		if(subDlg==null) {
			View cv = a.getLayoutInflater().inflate(R.layout.create_note_sub_view, a.root, false);
			TextView name = cv.findViewById(R.id.skName)
			 , pickName = cv.findViewById(R.id.pickName)
			 , value = cv.findViewById(R.id.skVal);
			SeekBar seekBar = cv.findViewById(R.id.seekbar);
			
			subDlg = ColorPickerDialog.newInstance(a, 0);
			final ColorPickerDialog dialog = subDlg;
			subDlg.forceAlphaLock(true);
			subDlg.setColorPickerListener(new ColorPickerListener() {
				@Override public void onPreviewSelectedColor(ColorPickerDialog dialogInterface, int color) { }
				@Override public void onDialogDismissed(ColorPickerDialog dialogInterface, int color) { }
				@Override public boolean onColorSelected(ColorPickerDialog dialogInterface, int color, boolean doubleTap)
				{
					final int k = mSubPickerNTyp();
					CMN.debug("subDlg::onColorSelected:::", mSubPickerId==R.id.bubbleColor, k, Integer.toHexString(color));
					if (mSubPickerId==R.id.bubbleColor) { // 气泡颜色
						int alpha = Color.alpha(color);
						if(PDICMainAppOptions.forceAlphaLock() || alpha > dialog.alphaLock()) {
							alpha = dialog.alphaLock();
							color = (0xFF000000&(alpha<<24)) | (0x00FFFFFF&color);
						}
						uiData.bubbleColors[k] = color;
						a.opt.putLong("nbclr"+k, (uiData.bubbleColorsEnabled[k] ? 0x100000000L : 0L) | color&0xFFFFFFFFL);
					} else {
						uiData.fontColors[k] = color;
						long val = (uiData.fontColorEnabled[k] ? 0x100000000L : 0L) | color&0xFFFFFFFFL;
						a.opt.putLong("nfclr"+k, val);
						
						int percent = seekBar.getProgress() * 100 / 255;
						uiData.fontSizes[k] = percent;
						a.opt.putInt("nfntsz"+k, (uiData.fontSizesEnabled[k]?percent:-percent));
					}
					return true;
				}
			});
			subDlg.setContentView(cv, true);
			seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override public void onStartTrackingTouch(SeekBar seekBar) {  }
				@Override public void onStopTrackingTouch(SeekBar seekBar) { }
				@Override
				public void onProgressChanged(SeekBar seekBar, int lock, boolean fromUser) {
					final boolean b1 = mSubPickerId==R.id.bubbleColor;
					final int k = mSubPickerNTyp();
					int percent = lock * 100 / 255;
					if(b1){
						dialog.alphaLock(lock);
						dialog.datasetChanged();
					}
					if (fromUser) {
						if (b1) {
							uiData.bubbleColors[k] = (0xFF000000 & (lock << 24)) | (0x00FFFFFF & uiData.bubbleColors[k]);
						} else {
							uiData.fontSizes[k] = percent;
						}
					}
					String indi = percent + "%";
					if(indi.length()<3) indi = "0"+indi;
					value.setText(indi);
				}
			});
			pickName.setOnClickListener(v -> {
				final boolean b1 = mSubPickerId==R.id.bubbleColor;
				final int k = mSubPickerNTyp();
				if (!b1) {
					dialog.setEnabled(uiData.fontColorEnabled[k] = !uiData.fontColorEnabled[k]);
				} else {
					name.performClick();
				}
			});
			name.setOnClickListener(v -> {
				final boolean b1 = mSubPickerId==R.id.bubbleColor;
				final int k = mSubPickerNTyp();
				if (!b1) {
					boolean vis = uiData.fontSizesEnabled[k] = !uiData.fontSizesEnabled[k];
					ViewUtils.setVisible(seekBar, vis);
					ViewUtils.setVisible(value, vis);
				} else {
					boolean enabled = uiData.bubbleColorsEnabled[k] = !uiData.bubbleColorsEnabled[k];
					seekBar.setEnabled(enabled);
					try {
						dialog.setEnabled(enabled);
					} catch (Exception e) {
						CMN.debug(e);
					}
				}
			});
			subDlg.tag = new View[]{name, pickName, value, seekBar};
			subDlgRef = new WeakReference<>(subDlg);
		}
		final boolean pBc = mSubPickerId==R.id.bubbleColor;
		int color;
		final int k = mSubPickerNTyp();
		{
			color = (pBc?uiData.bubbleColors:uiData.fontColors)[k];
			if (color==0) {
				if (pBc) { // 气泡颜色
					color = 0xFFABCDEF;
				} else { // 字体颜色
					color = k==1?0xFFFFFFFF:0xFF000000;
				}
				(pBc?uiData.bubbleColors:uiData.fontColors)[k] = color;
			}
		}
		View[] views = (View[]) subDlg.tag;
		TextView name = (TextView) views[0]
				, pickName = (TextView) views[1]
				, value = (TextView) views[2];
		SeekBar seekBar = (SeekBar) views[3];
		
		final boolean vis = pBc || uiData.fontSizesEnabled[k];
		ViewUtils.setVisible(seekBar, vis);
		ViewUtils.setVisible(value, vis);
		name.setText(pBc?"不透明度":"字体大小");
		pickName.setText(pBc?"修改气泡颜色":"修改笔记文本的字体颜色");

		final boolean enabled = !pBc&&uiData.fontColorEnabled[k] || pBc&&uiData.bubbleColorsEnabled[k];
		boolean visible = true;
		if (pBc) { // 气泡alpha
			seekBar.setProgress(color>>24&0xFF);
			seekBar.setEnabled(enabled);
		} else { // 字体大小（百分比）
			seekBar.setProgress(uiData.fontSizes[k]*255/100);
			seekBar.setEnabled(true);
			visible = uiData.fontSizesEnabled[k];
		}
		VU.setVisible(seekBar, visible);
		VU.setVisible(value, visible);
		int alpha = pBc?color>>24&0xff:255;
		//if (subDlg.getPreviewingColor()!=color || subDlg.alphaLock()!=alpha) {
			subDlg.setPreviewColor(color);
			subDlg.setInitialColor(color);
			subDlg.alphaLock(alpha);
		//	subDlg.datasetChanged();
		//}
		CMN.debug("颜色::", Integer.toHexString(color), k, uiData.fontColorEnabled[k], enabled);
		if (!subDlg.isAdded()) {
			subDlg.show(a.getSupportFragmentManager(), "note-sub-dlg");
		}
		
		subDlg.setEnabled(enabled);
	}
	
	private int mSubPickerNTyp() {
		return PDICMainAppOptions.colorSameForNoteTypes()?0:mSubPickerNTyp;
	}
	
	PopupWindow menuPopup;
	ListView shareList;
	View menuAnchor;
	ArrayAdapter shareListAda, lnkToAdapter, notesAdapter, binAdapter;
	String[] arrNotes, arrNoteIds;
	long linkedNoteId, lastMenuTm;
	Runnable dismissMenuRn = () -> menuPopup.dismiss();
	
	boolean showedMenuPopup(){
		if (lastMenuTm==0) {
			return false;
		}
		if (CMN.now() - lastMenuTm > 150) {
			lastMenuTm = 0;
			return false;
		}
		return true;
	}
	
	void showLiteMenuPopup(ArrayAdapter adapter, View v){
		if (menuPopup == null) {
			View view = a.getLayoutInflater().inflate(R.layout.popup_more, null);
			menuPopup = new PopupWindow(view,(int)(160 * GlobalOptions.density), ViewGroup.LayoutParams.WRAP_CONTENT);
			shareList = view.findViewById(R.id.share_list);
			shareList.setOnItemClickListener(this);
			menuPopup.setOnDismissListener(() -> {
				lastMenuTm = CMN.now();
			});
		}
		shareList.setAdapter(shareListAda = adapter);
		if (v == null) {
			v = menuAnchor;
		} else {
			menuAnchor = v;
		}
		menuPopup.setFocusable(false);
		menuPopup.setOutsideTouchable(true);
		menuPopup.setBackgroundDrawable(null);
		menuPopup.showAsDropDown(v, v.getWidth(), 0);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (shareListAda == lnkToAdapter) {
			if (notesAdapter != null) {
				showLiteMenuPopup(notesAdapter, null);
			}
			mWebView.evaluateJavascript(a.getCommonAsset("getAnnots.js"), value -> {
				try {
					// CMN.debug("getAnnots", value);
					if (value != null) {
						JSONArray json = (JSONArray) JSONArray.parse(value);
						int size = json.size();
						arrNotes = new String[size / 2];
						arrNoteIds = new String[size / 2];
						for (int i = 0; i < size - 1; i += 2) {
							arrNotes[i/2] = json.getString(i);
							arrNoteIds[i/2] = json.getString(i + 1);
						}
						notesAdapter = new ArrayAdapter<>(a, R.layout.popup_list_item, arrNotes);
						showLiteMenuPopup(notesAdapter, null);
					}
				} catch (Exception e) {
					CMN.debug(e);
				}
			});
		}
		else if (shareListAda == binAdapter) {
			PDICMainAppOptions.noteInBubble(uiData.noteOnBubble =!uiData.noteOnBubble);
			view.postDelayed(dismissMenuRn, 300);
			TextView tv = view.findViewById(R.id.text1);
			if(tv!=null) tv.setText(uiData.noteOnBubble ?"展开笔记到气泡中 √ ":"展开笔记到气泡中");
		}
		else {
			String note = arrNotes[position];
			linkedNoteId = Long.parseLong(arrNoteIds[position]);
			ViewUtils.setVisible(lnkPanel, true);
			lnkTo.setText(note);
			view.postDelayed(dismissMenuRn, 300);
		}
	}
	
	public String getNoteText() {
		if (ViewUtils.isVisible(lnkPanel)) {
			return "_pd_lnk="+linkedNoteId;
		}
		return getText().toString().trim();
	}
	
	@Override
	public boolean onColorSelected(ColorPickerDialog dialogInterface, int color, boolean doubleTap) {
		if(doubleTap) return false;
		color = alphaLock(color);
		int k = uiData.toolIdx;
		a.opt.annotColor(k, color, true);
		uiData.colors[k] = color;
		a.Annot(mWebView, k, this);
		if(tkShow!=null) tkShow = null;
		onDialogDismissed(null, 0);
		return true;
	}
	
	public int alphaLock(int color) {
		int k = uiData.toolIdx;
		int alpha = Color.alpha(color);
		if(PDICMainAppOptions.forceAlphaLock() || alpha > uiData.alphaLocks[k]) {
			alpha = uiData.alphaLocks[k];
			color = (0xFF000000&(alpha<<24)) | (0x00FFFFFF&color);
		}
		return color;
	}
	
	@Override
	public void onPreviewSelectedColor(ColorPickerDialog dialogInterface, int color) {
		if(dialogInterface==null)
			color = noteDlg.getPreviewingColor();
		int k = uiData.toolIdx;
		View btn = btnTypes[k];
		ColoredTextSpan span = spans[k];
		color = alphaLock(color);
		span.mColor = color;
		btn.invalidate();
		if (false && dialogInterface!=null) {
			a.opt.annotColor(k, color, true);
		}
	}
	
	@Override
	public void onDialogDismissed(ColorPickerDialog dialogInterface, int color) {
		if(tkShow!=null) tkShow.show();
		if (menuPopup != null) menuPopup.dismiss();
	}
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int lock, boolean fromUser) {
		a.opt.alphaLock(uiData.toolIdx, lock, true);
		uiData.alphaLocks[uiData.toolIdx] = lock;
		String indi = (lock * 100 / 255) + "%";
		if(indi.length()<3) indi = "0"+indi;
		alphaText.setText(indi);
		noteDlg.alphaLock(lock);
		noteDlg.datasetChanged();
		onPreviewSelectedColor(null, -1);
	}
	@Override public void onStartTrackingTouch(SeekBar seekBar) { }
	@Override public void onStopTrackingTouch(SeekBar seekBar) { }
	
	public void refresh() {
		//if (MainAppBackground != a.MainAppBackground)
		{
			// 刷新颜色变化（黑暗模式或者设置更改）
			MainAppBackground = a.MainAppBackground;
			editTools.setBackgroundColor(GlobalOptions.isDark ? MainAppBackground : 0xFF03A9F4);
			//editTools.setBackgroundColor(ColorUtils.blendARGB(MainAppBackground, Color.WHITE, 0.1f));
			int wilte = GlobalOptions.isDark ? Color.GRAY : Color.WHITE;
			btnPanel.setBackgroundColor(wilte);
			edit.setBackgroundColor(wilte);
			//editToolbar.setBackgroundColor(MainAppBackground);
			
			int gray = 0x55888888;
			gray = 0x5501374F;
			editToolbar.setCheckedColor(gray);
		}
		boolean landScape = a.dm.heightPixels < a.dm.widthPixels;
		//((PercentRelativeLayout.LayoutParams)editPanel.getLayoutParams()).getPercentLayoutInfo().heightPercent=landScape?0.8f:0.58f;
		//botPad.getLayoutParams().height = (int) (GlobalOptions.density*(landScape?10:15));
	}
	
	private Editable getText() {
		Editable ret = edit.getText();
		if (ret==null) {
			edit.setText(" ");
			ret = edit.getText();
			ret.clear();
		}
		return ret;
	}
	
	@SuppressLint("ResourceType")
	@Override
	public boolean onMenuItemClick(PopupMenuHelper popupMenuHelper, View v, boolean isLongClick) {
		if (v.getId() == R.id.editNote) {
			if (!isLongClick) {
				TextMenuView tv = (TextMenuView) v;
				PDICMainAppOptions.tapEditAnteNote(!tv.isActivated());
				tv.setActivated(PDICMainAppOptions.tapEditAnteNote());
				popupMenuHelper.postDismiss(100);
			}
			return true;
		}
		if (v.getId() == R.id.editNoteBtn) {
			if (!isLongClick) {
				editPressedNote();
				popupMenuHelper.dismiss();
			}
			return true;
		}
		BookNotes bn = a.getBookNotes();
		bn.pressedV = pressedV;
		bn.pressedRowId = pressedRowId;
		boolean ret = bn.onMenuItemClick(popupMenuHelper, v, isLongClick);
		if (ret && v.getId() == R.string.delete) {
			View pv = pressedV.get();
			if (pv != null && rangeAdapter!=null) {
				AnnotAdapter.VueHolder vh = (AnnotAdapter.VueHolder) pv.getTag();
				((AnnotRangeAdapter)rangeAdapter.dataAdapter).deleteAt(vh.vh.position);
				rangeListAdapter.notifyDataSetChanged();
			}
			mWebView.evaluateJavascript("PatchNote("+pressedRowId+")", null); //todo
		}
		return true;
	}
	
	private void editPressedNote() {
		try {
			View v = pressedV.get();
			AnnotAdapter.VueHolder vh = (AnnotAdapter.VueHolder) v.getTag();
			AnnotAdapter.AnnotationReader reader = rangeAdapter.dataAdapter.getReaderAt(vh.vh.position);
			setEditingNote(reader.row_id);
			JSONObject json = reader.getAnnot();
			String note = JsonNames.readString(json, JsonNames.note);
			int color = JsonNames.readInt(json, JsonNames.clr, 0xffffaaaa);
			int type = JsonNames.readInt(json, JsonNames.typ, 0);
			int ntyp = JsonNames.readInt(json, JsonNames.ntyp, 0);
			CMN.debug("editPressedNote::", type, ntyp);
			
			uiData.toolIdx = type;
			uiData.alphaLocks[type] = color>>24&0xFF;
			uiData.colors[type] = color|0xFF000000;
			btnTypes[type].performClick();
			
			getText().clear();
			if (note != null) {
				noteTypes.getChildAt(ntyp).performClick();
				getText().append(note);
				int k = PDICMainAppOptions.colorSameForNoteTypes()?0:ntyp;
				try {
					uiData.noteOnBubble = json.containsKey("bon");
					uiData.showBubbles[ntyp] = ntyp==1||JsonNames.hasKey(json, JsonNames.bin);
					if (uiData.bubbleColorsEnabled[k] = JsonNames.hasKey(json, JsonNames.bclr)) {
						uiData.bubbleColors[k] = JsonNames.readInt(json, JsonNames.bclr, 0);
					}
					if (uiData.fontColorEnabled[k] = JsonNames.hasKey(json, JsonNames.fclr)) {
						uiData.fontColors[k] = JsonNames.readInt(json, JsonNames.fclr, 0);
					}
					if (uiData.fontSizesEnabled[k] = JsonNames.hasKey(json, JsonNames.fsz)) {
						uiData.fontSizes[k] = JsonNames.readInt(json, JsonNames.fsz, 0);
					}
				} catch (Exception e) {
					CMN.debug(e);
				}
				if (!ViewUtils.isVisible(editPanel)) {
					onClick(a.anyView(R.id.editShow));
				}
			}
			
			PopupMenuHelper helper = popupMenuRef.get();
			if (helper !=null) helper.dismiss();
		} catch (Exception e) {
			CMN.debug(e);
		}
	}
	
	private void setEditingNote(long nid) {
		if (editingNoteId!=nid) {
			editingNoteId = nid;
			noteDlg.btnConfirm.setText(nid==-1?"创建":"修改");
		}
	}
	
	private void readUIData() {
		uiData.toolIdx = PDICMainAppOptions.currentTool();
		uiData.noteType = PDICMainAppOptions.currentNoteType();
		uiData.noteOnBubble = PDICMainAppOptions.noteInBubble();
		uiData.showBubbles[0] = PDICMainAppOptions.showBubbleForEmbedNote();
		uiData.showBubbles[1] = true;
		uiData.showBubbles[2] = PDICMainAppOptions.showBubbleForFootNote();
		long value;
		for (int i = 0; i < 3; i++) {
			value = a.opt.getLong("nbclr"+i, 0);
			uiData.bubbleColors[i] = (int) (value);
			uiData.bubbleColorsEnabled[i] = (value&0x100000000L)>0;
			value = a.opt.getLong("nfclr"+i, 0);
			uiData.fontColors[i] = (int) (value);
			uiData.fontColorEnabled[i] = (value&0x100000000L)>0;
			value = a.opt.getInt("nfntsz"+i, 100);
			uiData.fontSizes[i] = (int) (value<0?-value:value);
			uiData.fontSizesEnabled[i] = value > 0;
//			uiData.bubbleColors[i] = 0;
//			uiData.fontColors[i] = 0;
//			uiData.fontSizes[i] = 100;
		}
		for (int i = 0; i < 2; i++) {
			uiData.colors[i] = a.opt.annotColor(i, 0, false);
			uiData.alphaLocks[i] = a.opt.alphaLock(i, 0, false);
		}
	}
}
