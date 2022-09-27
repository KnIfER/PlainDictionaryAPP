package com.knziha.plod.PlainUI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;
import androidx.percentlayout.widget.PercentRelativeLayout;

import com.alibaba.fastjson.JSONArray;
import com.jaredrummler.colorpicker.ColorPickerDialog;
import com.jaredrummler.colorpicker.ColorPickerListener;
import com.knziha.ankislicer.customviews.ShelfLinearLayout2;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.widgets.ViewUtils;
import com.knziha.plod.widgets.WebViewmy;
import com.knziha.text.ColoredTextSpan;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;

public class AnnotationDialog implements View.OnClickListener, ColorPickerListener, SeekBar.OnSeekBarChangeListener, AdapterView.OnItemClickListener {
	MainActivityUIBase a;
	ColorPickerDialog noteDlg;
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
	Button[] btns;
	AlertDialog tkShow;
	WebViewmy mWebView;
	
	ColoredTextSpan[] spans;
	protected int MainAppBackground;
	
	public static class UIData {
		public int toolIdx;
		public int[] colors = new int[8];
		public int[] alphaLocks = new int[8];
		public boolean[] showBubbles = new boolean[3];
		public int noteType; // 1=正文; 2=气泡; 3=脚注
		public int[] fontColors = new int[8];
		public int[] bubbleColors = new int[8];
		public int[] fontSizes = new int[8];
		public boolean[] fontSizesEnabled = new boolean[3];
		public boolean[] bubbleColorsEnabled = new boolean[3];
		public boolean noteInBubble;
	}
	
	public UIData uiData;
	
	public AnnotationDialog(MainActivityUIBase a) {
		this.a = a;
	}
	
	public void show(WebViewmy wv, int type) {
		tkShow = a.ucc == null || a.ucc.detached() ? null : a.ucc.getDialog();
		mWebView = wv;
		if(tkShow!=null) tkShow.hide();
		if (uiData==null) {
			uiData = new UIData();
			uiData.toolIdx = PDICMainAppOptions.currentTool();
			uiData.noteType = PDICMainAppOptions.currentNoteType();
			uiData.noteInBubble = PDICMainAppOptions.noteInBubble();
			uiData.showBubbles[0] = PDICMainAppOptions.showBubbleForEmbedNote();
			uiData.showBubbles[1] = true;
			uiData.showBubbles[2] = PDICMainAppOptions.showBubbleForFootNote();
			for (int i = 0; i < 3; i++) {
				int color = uiData.bubbleColors[i] = a.opt.getInt("nbclr"+i, 0);
				uiData.fontColors[i] = a.opt.getInt("nfntclr"+i, 0);
				uiData.fontSizes[i] = a.opt.getInt("nfntsz"+i, 100);
				
//				color = uiData.bubbleColors[i] = 0;
//				uiData.fontColors[i] = 0;
//				uiData.fontSizes[i] = 100;
				
				uiData.bubbleColorsEnabled[i] = color==0 || (color>>24&0xFF)>0;
			}
			for (int i = 0; i < 2; i++) {
				uiData.colors[i] = a.opt.annotColor(i, 0, false);
				uiData.alphaLocks[i] = a.opt.alphaLock(i, 0, false);
			}
		}
		if (edit==null) {
			ViewGroup cv = (ViewGroup) a.getLayoutInflater().inflate(R.layout.create_note_view, a.root, false);
			lnkToAdapter = new ArrayAdapter<>(a, R.layout.popup_list_item, new String[]{a.mResource.getString(R.string.lnk_note)});
			binAdapter = new ArrayAdapter<>(a, R.layout.popup_list_item, new ArrayList<>(Collections.singletonList("展开笔记到气泡中")));
			editPanel = cv.findViewById(R.id.editPanel);
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
			ViewUtils.setVisible(alphaSeek, PDICMainAppOptions.alphaLock());
			ViewUtils.setVisible(editPanel, PDICMainAppOptions.editNote());
			
			ViewUtils.setOnClickListenersOneDepth(cv, this, 999, null);
			btns = new Button[]{btnPanel.findViewById(R.id.btnH), btnPanel.findViewById(R.id.btnU)};
			ColorPickerDialog noteDlg = ColorPickerDialog.newBuilder()
							.setDialogId(123124)
							.setInitialColor(uiData.colors[uiData.toolIdx])
							.create();
			noteDlg.setContentView(cv, true);
			noteDlg.show(a.getSupportFragmentManager(), "note-dlg");
			this.noteDlg = noteDlg;
			
			spans = new ColoredTextSpan[]{new ColoredTextSpan(0xffffaaaa)
					, new ColoredTextSpan(Color.BLACK, 8.f, 2)};
			
			for (int k = 0; k < 2; k++) {
				Button btn = btns[k];
				SpannableStringBuilder ssb = new SpannableStringBuilder();
				ssb.append(btn.getText());
				ssb.setSpan(spans[k], 0, ssb.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
				btn.setText(ssb);
			}
			
			noteDlg.forceAlphaLock(true);
			noteDlg.setColorPickerListener(this);
			alphaSeek.setOnSeekBarChangeListener(this);
			btns[type<0?uiData.toolIdx:type].performClick();
			noteTypes.setTag(this);
			noteTypes.getChildAt(uiData.noteType).performClick();
			noteTypes.setTag(null);
		}
		refresh();
		//ViewUtils.removeView(editTools);
		//ViewUtils.addViewToParent(editTools, editPanel, 1);
	}
	
	// click
	@SuppressLint("NonConstantResourceId")
	@Override
	public void onClick(View v) {
		CMN.debug("v::", v);
		if (v == alphaText) {
			PDICMainAppOptions.alphaLock(!ViewUtils.isVisible(alphaSeek));
			ViewUtils.setVisible(alphaSeek, PDICMainAppOptions.alphaLock());
		}
		else if (ViewUtils.getNthParentNonNull(v,1).getId()==R.id.btnPanel) {
			for (int k = 0; k < 2; k++) {
				if (btns[k] == v) {
					if (uiData.toolIdx!=k) {
						PDICMainAppOptions.currentTool(uiData.toolIdx=k);
					}
					btns[k].setAlpha(1);
					int lock = uiData.alphaLocks[uiData.toolIdx];
					noteDlg.alphaLock(lock);
					String zhsh = (lock * 100 / 255) + "%";
					if(zhsh.length()<3) zhsh = "0"+zhsh;
					alphaText.setText(zhsh);
					noteDlg.setPreviewColor(uiData.colors[uiData.toolIdx]);
					alphaSeek.setProgress(lock);
					noteDlg.datasetChanged();
				} else {
					btns[k].setAlpha(0.1f);
				}
			}
		}
		switch (v.getId()) {
			case R.id.editShow:
				v = ViewUtils.getNthParentNonNull(edit, 1);
				PDICMainAppOptions.editNote(!ViewUtils.isVisible(v));
				ViewUtils.setVisible(v, PDICMainAppOptions.editNote());
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
				v.jumpDrawablesToCurrentState();
			break;
			case R.id.ntyp3:
				if (noteTypes.getTag() == null) {
					if (shareListAda != lnkToAdapter || !showedMenuPopup()) {
						showEditMenuPopup(lnkToAdapter, v);
					}
				}
			case R.id.ntyp1:
			case R.id.ntyp2:
				if (noteTypes.getTag() == null && v.getId()==R.id.ntyp2) {
					if (shareListAda != binAdapter || !showedMenuPopup()) {
						binAdapter.clear();
						binAdapter.add(uiData.noteInBubble?"√ 展开笔记到气泡中":"展开笔记到气泡中");
						showEditMenuPopup(binAdapter, v);
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
		}
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
			
			subDlg = ColorPickerDialog.newBuilder()
					.setDialogId(123125)
					.setInitialColor(0)
					.create();
			final ColorPickerDialog dialog = subDlg;
			subDlg.forceAlphaLock(true);
			subDlg.setColorPickerListener(new ColorPickerListener() {
				@Override public void onPreviewSelectedColor(ColorPickerDialog dialogInterface, int color) { }
				@Override public void onDialogDismissed(ColorPickerDialog dialogInterface, int color) { }
				@Override public void onColorSelected(ColorPickerDialog dialogInterface, int color) {
					final boolean b1 = mSubPickerId==R.id.bubbleColor;
					final int k = mSubPickerNTyp;
					CMN.debug("onColorSelected:::", b1, k, Integer.toHexString(color));
					if (b1) { // 气泡颜色
						int alpha = Color.alpha(color);
						if(PDICMainAppOptions.forceAlphaLock() || alpha > dialog.alphaLock()) {
							alpha = dialog.alphaLock();
							color = (0xFF000000&(alpha<<24)) | (0x00FFFFFF&color);
						}
						uiData.bubbleColors[k] = color;
						a.opt.putInt("nbclr"+k, color);
					} else { // 字体大小
						uiData.fontColors[k] = color;
						a.opt.putInt("nfntclr"+k, color);
						if (uiData.fontSizesEnabled[k]) {
							int percent = seekBar.getProgress() * 100 / 255;
							uiData.fontSizes[k] = percent;
							if (true) {
								a.opt.putInt("nfntsz"+k, percent);
							}
						}
						a.opt.putInt("nfntclr"+k, color);
					}
				}
			});
			subDlg.setContentView(cv, true);
			seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override public void onStartTrackingTouch(SeekBar seekBar) {  }
				@Override public void onStopTrackingTouch(SeekBar seekBar) { }
				@Override
				public void onProgressChanged(SeekBar seekBar, int lock, boolean fromUser) {
					final boolean b1 = mSubPickerId==R.id.bubbleColor;
					final int k = mSubPickerNTyp;
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
			name.setOnClickListener(v -> {
				final boolean b1 = mSubPickerId==R.id.bubbleColor;
				final int k = mSubPickerNTyp;
				if (!b1) {
					boolean vis = uiData.fontSizesEnabled[k] = !uiData.fontSizesEnabled[k];
					ViewUtils.setVisible(seekBar, vis);
					ViewUtils.setVisible(value, vis);
				} else {
					boolean enabled = uiData.bubbleColorsEnabled[k] = !uiData.bubbleColorsEnabled[k];
					seekBar.setEnabled(enabled);
					try {
						dialog.getDialog().findViewById(R.id.views_holder).setAlpha(enabled?1:0.1f);
					} catch (Exception e) {
						CMN.debug(e);
					}
				}
			});
			value.setOnClickListener(v -> {
				final boolean b1 = mSubPickerId==R.id.bubbleColor;
				final int k = mSubPickerNTyp;
				if (!b1) {
					boolean vis = uiData.fontSizesEnabled[k] = !uiData.fontSizesEnabled[k];
					ViewUtils.setVisible(seekBar, vis);
					ViewUtils.setVisible(value, vis);
				} else {
					boolean enabled = uiData.bubbleColorsEnabled[k] = !uiData.bubbleColorsEnabled[k];
					seekBar.setEnabled(enabled);
					try {
						dialog.getDialog().findViewById(R.id.views_holder).setAlpha(enabled?1:0.1f);
					} catch (Exception e) {
						CMN.debug(e);
					}
				}
			});
			subDlg.tag = new View[]{name, pickName, value, seekBar};
			subDlgRef = new WeakReference<>(subDlg);
		}
		final boolean b1 = mSubPickerId==R.id.bubbleColor;
		final int k = mSubPickerNTyp;
		int color = (b1?uiData.bubbleColors:uiData.fontColors)[k];
		if (color==0) {
			if (b1) { // 气泡颜色
				color = 0xFFABCDEF;
			} else { // 字体颜色
				color = k==1?0xFFFFFFFF:0xFF000000;
			}
			(b1?uiData.bubbleColors:uiData.fontColors)[k] = color;
		}
		View[] views = (View[]) subDlg.tag;
		TextView name = (TextView) views[0]
				, pickName = (TextView) views[1]
				, value = (TextView) views[2];
		SeekBar seekBar = (SeekBar) views[3];
		
		final boolean vis = b1 || uiData.fontSizesEnabled[k];
		ViewUtils.setVisible(seekBar, vis);
		ViewUtils.setVisible(value, vis);
		name.setText(b1?"不透明度":"字体大小");
		pickName.setText(b1?"修改气泡颜色":"修改气泡中的字体颜色");

		final boolean enabled = !b1 || uiData.bubbleColorsEnabled[k];
		seekBar.setEnabled(enabled);
		
		if (b1) { // 气泡alpha
			seekBar.setProgress(color>>24&0xFF);
		} else { // 字体大小（百分比）
			seekBar.setProgress(uiData.fontSizes[k]*255/100);
		}
		int alpha = b1?color>>24&0xff:255;
		//if (subDlg.getPreviewingColor()!=color || subDlg.alphaLock()!=alpha) {
			subDlg.setPreviewColor(color);
			subDlg.setInitialColor(color);
			subDlg.alphaLock(alpha);
		//	subDlg.datasetChanged();
		//}
		CMN.debug("颜色::", Integer.toHexString(color));
		if (!subDlg.isAdded()) {
			subDlg.show(a.getSupportFragmentManager(), "note-sub-dlg");
		}
		
		ColorPickerDialog dialog = subDlg;
		seekBar.post(() -> {
			if (dialog.getDialog() != null) {
				try {
					dialog.getDialog().findViewById(R.id.views_holder).setAlpha(enabled?1:0.1f);
				} catch (Exception e) {
					CMN.debug(e);
				}
			}
		});
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
	
	void showEditMenuPopup(ArrayAdapter adapter, View v){
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
				showEditMenuPopup(notesAdapter, null);
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
						showEditMenuPopup(notesAdapter, null);
					}
				} catch (Exception e) {
					CMN.debug(e);
				}
			});
		}
		else if (shareListAda == binAdapter) {
			PDICMainAppOptions.noteInBubble(uiData.noteInBubble=!uiData.noteInBubble);
			view.postDelayed(dismissMenuRn, 300);
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
	public void onColorSelected(ColorPickerDialog dialogInterface, int color) {
		color = alphaLock(color);
		int k = uiData.toolIdx;
		a.opt.annotColor(k, color, true);
		uiData.colors[k] = color;
		a.Annot(mWebView, k, this);
		onDialogDismissed(null, 0);
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
		View btn = btns[k];
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
		((PercentRelativeLayout.LayoutParams)editPanel.getLayoutParams()).getPercentLayoutInfo().heightPercent=landScape?0.8f:0.8f;
		botPad.getLayoutParams().height = (int) (GlobalOptions.density*(landScape?10:15));
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
}
