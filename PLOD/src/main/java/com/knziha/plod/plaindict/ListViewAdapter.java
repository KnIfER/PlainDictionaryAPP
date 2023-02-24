package com.knziha.plod.plaindict;

import static com.knziha.plod.dictionary.mdBase.markerReg;
import static com.knziha.plod.plaindict.MainActivityUIBase.ViewHolder;
import static com.knziha.plod.plaindict.PDICMainActivity.layoutScrollDisabled;

import android.content.Intent;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.core.graphics.ColorUtils;

import com.knziha.plod.dictionary.Utils.Flag;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.dictionarymodels.ScrollerRecord;
import com.knziha.plod.widgets.ViewUtils;
import com.knziha.plod.widgets.WebViewmy;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.List;

/** 词典词条列表，单本词典。 */
public class ListViewAdapter extends BasicAdapter {
	final MainActivityUIBase a;
	final PDICMainAppOptions opt;
	Flag mflag = new Flag();
	//AbsListView.LayoutParams lp;
	//构造
	public ListViewAdapter(MainActivityUIBase a, MenuBuilder allMenus, List<MenuItemImpl> singleContentMenu)
	{
		super(a.contentUIData, a.weblistHandler, allMenus, singleContentMenu);
		this.a = a;
		this.opt = a.opt;
		this.webviewHolder=contentUIData.webSingleholder;
		this.presenter=a.EmptyBook;
	}
	
	@Override
	public int getCount() {
//		if (ViewUtils.isVisible(a.listName(1)) ^ PDICMainAppOptions.listPreviewEnabled()) {
//			ViewUtils.setVisible(a.listName(1), PDICMainAppOptions.listPreviewEnabled());
//		}
		if(presenter!=null) {
			if(PDICMainAppOptions.getSimpleMode() 
					&& presenter.getIsNonSortedEntries() 
					&& a.etSearch.getText().length()==0) {
				return 0;
			}
			return (int) presenter.bookImpl.getNumberEntries();
		} else {
			return 0;
		}
	}
	
	@Override
	public String getEntry(int pos) {
		return presenter.getBookEntryAt(pos);
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		//return lstItemViews.get(position);
		MainActivityUIBase.ViewHolder vh;
		if (convertView == null) {
			vh = new ViewHolder(a, R.layout.listview_item01, parent);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}
		String currentKeyText = presenter.bookImpl.getEntryAt(position,mflag);
		if(presenter.bookImpl.hasVirtualIndex()){
			int tailIdx=currentKeyText.lastIndexOf(":");
			if(tailIdx>0)
				currentKeyText=currentKeyText.substring(0, tailIdx);
		}
		
		if( vh.title.getTextColors().getDefaultColor()!=a.AppBlack) {
			//decorateBackground(vh.itemView);
			vh.title.setTextColor(a.AppBlack);
		}
		
		int zhTrans = PDICMainAppOptions.listZhTranslate();
		if (zhTrans != 0) {
			currentKeyText = a.zhTranslate(currentKeyText, zhTrans);
		}
		
		vh.title.setText(currentKeyText);
//			if(position==0 && mdict_asset.class==currentDictionary.getClass()) {
//				vh.subtitle.setText(Html.fromHtml("<font color='#2B4391'> < "+"欢迎使用平典"+packageName()+" ></font >"));
//			}
//			else {
//				if(mflag.data!=null){
//					vh.subtitle.setText(Html.fromHtml(currentDictionary.appendCleanDictionaryName(null).append("<font color='#2B4391'> < ").append(mflag.data).append(" ></font >").toString()));
//				} else {
//
//				}
//			}
		//tofo
		vh.position = position;
		
		boolean showPreview = presenter.hasPreview() && PDICMainAppOptions.listPreviewEnabled();
		boolean selectable = PDICMainAppOptions.listPreviewSelectable();
		if (showPreview) {
			int maxLines = PDICMainAppOptions.listOverreadMode()?Integer.MAX_VALUE:3;
			int tmp = PDICMainAppOptions.listPreviewColor();
			int color = ColorUtils.blendARGB(a.AppWhite, a.AppBlack, tmp==0?0.08f:tmp==1?0.5f:0.8f);
			tmp = PDICMainAppOptions.listPreviewFont();
			int size = tmp==0?12:tmp==1?14:17;
			try {
				String record = presenter.bookImpl.getRecordAt(position, null, false);
				String text = Jsoup.parse(record).text();
				if (presenter.isMdict() && presenter.getMdict().hasStyleSheets() && text.contains("`")) {
					text = markerReg.matcher(text).replaceAll("").trim();
				}
				if (zhTrans != 0) {
					text = a.zhTranslate(text, zhTrans);
				}
				vh.preview.setText(text);
				vh.preview.setTextColor(color);
				vh.preview.setTextSize(size);
				vh.preview.setMaxLines(maxLines);
			} catch (Exception e) {
				showPreview = false;
				CMN.debug(e);
			}
		}
		if (selectable!=vh.selectable)
		{
			vh.title.setTextIsSelectable(selectable);
			vh.preview.setTextIsSelectable(selectable);
			View.OnTouchListener touch = selectable ? a.lineRightClicker : null;
			vh.title.setOnTouchListener(touch);
			vh.preview.setOnTouchListener(touch);
			vh.itemView.setOnClickListener(selectable?this:null);
			if (!selectable) vh.itemView.setClickable(false);
			vh.selectable = selectable;
		}
		
		if (!showPreview) {
			boolean showBookName = PDICMainAppOptions.listShowBookName();
			ViewUtils.setVisible(vh.preview, false);
			ViewUtils.setVisible(vh.subtitle, showBookName);
			if (showBookName) {
				vh.subtitle.setText(presenter.getInListName());
			}
		} else {
			ViewUtils.setVisible(vh.preview, true);
			ViewUtils.setVisible(vh.subtitle, false);
		}
		
		return vh.itemView;
	}
	
	@Override
	public void SaveVOA() {
		if(presenter != a.EmptyBook) {
			if (opt.getRemPos()) {
				a.DelegateSaveAndRestorePagePos().SaveVOA(contentUIData.PageSlider.getWebContext(), this);
			}
			if (browsed && PDICMainAppOptions.storePageTurn()==2
					&& presenter.store(presenter.lvClickPos)
					&& !PDICMainAppOptions.storeNothing()
					&& PDICMainAppOptions.storeClick()) {
				a.addHistory(presenter.currentDisplaying, a.schuiList, weblistHandler, null);
				browsed = false;
			}
		}
	}
	
	@Override
	public void ClearVOA() {
		super.ClearVOA();
		{
			//CMN.Log("江河湖海",currentDictionary.expectedPosX,currentDictionary.expectedPos,currentDictionary.webScale);
			if(opt.getRemPos())
				avoyager.put(presenter.lvClickPos, new ScrollerRecord(mWebView.expectedPosX, mWebView.expectedPos, mWebView.webScale));
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
		userCLick=true;
		lastClickedPosBefore=-1;
//		a.bNeedReAddCon=false;
		lastClickedPos = -1;
		a.mergedKeyHeaders.clear();
		super.onItemClick(parent, view, pos, id);
	}
	
	public String getRowText(int pos) {
		String text = presenter.getRowTextAt(pos);
		if (text == null) {
			return String.valueOf(a.etSearch.getText());
		}
		return text;
	}
	
	public void enterPeruseMode(int pos) {
		String lstKey = getRowText(pos);
		PeruseView pView = a.getPeruseView();
		pView.searchAll(lstKey, a, true);
		boolean storeEt = userCLick && a.storeLv1(lstKey);
		if(!PDICMainAppOptions.storeNothing()  && storeEt) { // 保存输入框历史记录
			a.addHistory(lstKey, a.schuiMainPeruse, weblistHandler, a.etTools);
			pView.lstKey = lstKey;
		} else {
			pView.lstKey = null;
			if (storeEt) {
				a.etTools.addHistory(lstKey);
			}
		}
		a.AttachPeruseView(true);
		//CMN.Log(PeruseView.data);
		a.imm.hideSoftInputFromWindow(a.main.getWindowToken(),0);
	}
	
	@Override
	public void onItemClick(int pos) {//lv1
//		if (a.thisActType == MainActivityUIBase.ActType.PlainDict) {
//			//a.testRandomWord();
//			a.startActivity(new Intent(a,FloatActivitySearch.class).putExtra("EXTRA_QUERY", "happy"));
//			return;
//		}
		if(pos<0 || !bOnePageNav && pos>=getCount()){
			a.showTopSnack(R.string.endendr);
			return;
		}
		a.shuntAAdjustment();
		if(a.DBrowser!=null && !a.isFloatingApp()) return;
		lastClickedPosBefore = lastClickedPos;
		super.onItemClick(pos);
		a.ActivedAdapter=this;
		shunt=presenter.getIsWebx() ||a.bRequestedCleanSearch;
		
		boolean bUseMergedUrl = false;//!presenter.getIsWebx();
		boolean bUseDictView = !bUseMergedUrl && (!PDICMainAppOptions.getUseSharedFrame() || presenter.getHasVidx());
		
		avoyager = presenter.avoyager;
		WebViewmy mWebView = null;
		if(bUseDictView) {
			presenter.initViewsHolder(a);  // xxx  current.mWebView.fromCombined=0;
			mWebView = presenter.mWebView;
		} else {
			mWebView = weblistHandler.getMergedFrame(presenter);
		}
		if(PDICMainAppOptions.revisitOnBackPressed()) mWebView.cleanPage = true;
		this.mWebView = mWebView;
		weblistHandler.setViewMode(null, 0, mWebView);
		weblistHandler.initMergedFrame(0, false, bUseMergedUrl);
		
		presenter.lvClickPos=pos;
		
		if(allMenus!=null) {
			allMenus.setItems(contentMenus);
		}
		//a.showT(allMenus+""+singleContentMenu);
		
		ViewUtils.addViewToParentUnique(mWebView.rl, contentUIData.webSingleholder);
		mWebView.weblistHandler = weblistHandler;
		/* ensureContentVis */
		a.viewContent(weblistHandler);
		
		float desiredScale=a.prepareSingleWebviewForAda(presenter, mWebView, pos, this, opt.getRemPos(), opt.getInheritePageScale());
		
		lastClickedPos = pos;
		
		if(!a.bWantsSelection && userCLick) {
			a.imm.hideSoftInputFromWindow(a.main.getWindowToken(),0);
			a.etSearch.clearFocus();
		}
		
		layoutScrollDisabled=true;
		if(bOnePageNav)
			desiredScale=111;
		
//		if (!PDICMainAppOptions.pageSchAutoJump() && PDICMainAppOptions.pageSchAutoJumpForLst()) {
//			weblistHandler. todo
//		}
		
		long[] POS = a.getMergedClickPositions(pos);
		if(bUseMergedUrl) {
			// deprecate
			StringBuilder mergedUrl = new StringBuilder("http://mdbr.com/merge.jsp?q=")
					.append("&exp=");
			mergedUrl.append("d");
			IU.NumberToText_SIXTWO_LE(presenter.getId(), mergedUrl);
			for (long val:POS) {
				mergedUrl.append("_");
				IU.NumberToText_SIXTWO_LE(val, mergedUrl);
			}
			mWebView.loadUrl(mergedUrl.toString());
			mWebView.word(presenter.currentDisplaying = StringUtils.trim(presenter.bookImpl.getEntryAt(mWebView.currentPos = POS[0])));
			weblistHandler.setStar(presenter.currentDisplaying);
		} else {
			/* 仿效 GoldenDict 返回尽可能多的结果 */
			presenter.renderContentAt(desiredScale,BookPresenter.RENDERFLAG_NEW,0,mWebView, POS);
		}
		a.contentview.setTag(R.id.image, a.PhotoPagerHolder!=null&&a.PhotoPagerHolder.getParent()!=null?false:null);
		
		contentUIData.PageSlider.setWebview(mWebView, null);
		
		String lstKey = this.currentKeyText = mWebView.word();
		
		boolean storeEt = userCLick && a.storeLv1(lstKey);
		if(!PDICMainAppOptions.storeNothing()  && (storeEt || PDICMainAppOptions.storeClick() && presenter.store(pos))) {
			a.addHistory(lstKey
					, storeEt? a.schuiMain:  // 保存输入框历史记录
					 (userCLick || PDICMainAppOptions.storePageTurn()==0) && !(shunt && pos==0)?a.schuiList  // 保存列表点击历史记录
							:-1, weblistHandler, a.etTools);
		} else if (storeEt) {
			a.etTools.addHistory(lstKey);
		}
		
		//showT("查时: "+(System.currentTimeMillis()-stst));
		a.bWantsSelection=!presenter.getIsWebx();
		if(userCLick) {
			userCLick=false;
		} else {
			browsed =true;
		}
//			if(PDICMainAppOptions.getInPageSearchAutoUpdateAfterClick()){
//				prepareInPageSearch(key, true);
//			} //333
		a.etSearch_ToToolbarMode(1);
	//	weblistHandler.announceContent();
	}
	
	@Override
	public int getId() {
		return 1;
	}
	
	@Override
	public String currentKeyText() {
		return mWebView.word();
	}
}