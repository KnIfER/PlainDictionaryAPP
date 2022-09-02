package com.knziha.plod.plaindict;

import static com.knziha.plod.plaindict.MainActivityUIBase.ViewHolder;

import android.graphics.Color;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.core.graphics.ColorUtils;

import com.knziha.ankislicer.customviews.WahahaTextView;
import com.knziha.plod.dictionarymodels.BookPresenter;
import com.knziha.plod.dictionarymodels.resultRecorderCombined;
import com.knziha.plod.dictionarymodels.resultRecorderDiscrete;
import com.knziha.plod.dictionarymodels.resultRecorderScattered;
import com.knziha.plod.widgets.ViewUtils;

import org.jsoup.Jsoup;

import java.util.List;

// 联合搜索、全文搜索词条列表
public class ListViewAdapter2 extends BasicAdapter {
	final MainActivityUIBase a;
	final PDICMainAppOptions opt;
	final int id;
	public int itemId = R.layout.listview_item01;
	
	public ListViewAdapter2(MainActivityUIBase a, ViewGroup vg, MenuBuilder allMenus, List<MenuItemImpl> contentMenu, int resId, int id)
	{
		this(a, vg, allMenus, contentMenu, id);
		itemId = resId;
	}
	//构造
	public ListViewAdapter2(MainActivityUIBase a, ViewGroup vg, MenuBuilder allMenus, List<MenuItemImpl> contentMenu, int id)
	{
		super(a.contentUIData, a.weblistHandler, allMenus, contentMenu);
		this.webviewHolder=vg;
		this.a = a;
		this.opt = a.opt;
		this.id = id;
		this.results =  a.EmptySchResults;
		this.presenter=a.EmptyBook;
	}
	@Override
	public int getCount() {
		return results.size();
	}
	
	@Override
	public String getEntry(int pos) {
		return results.getResAt(a, pos).toString();
	}
	
	public int expectedPos;
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		//return lstItemViews.get(position);
		ViewHolder vh;
		CharSequence currentKeyText = results.getResAt(a, position);
		if(convertView!=null) {
			vh=(ViewHolder) convertView.getTag();
		} else {
			vh=new ViewHolder(a, itemId, parent);
			//vh.itemView.setOnClickListener(this);
			//vh.itemView.setOnLongClickListener(MainActivity.this);
			if(itemId==R.layout.listview_item1)
				vh.subtitle.setTag(vh.itemView.findViewById(R.id.counter));
		}
		if( vh.title.getTextColors().getDefaultColor()!=a.AppBlack) {
			//decorateBackground(vh.itemView);
			vh.title.setTextColor(a.AppBlack);
		}
		vh.position = position;
		vh.title.setText(currentKeyText);
//			if(combining_search_result.mflag.data!=null){
//				vh.subtitle.setText(Html.fromHtml(currentDictionary.appendCleanDictionaryName(null).append("<font color='#2B4391'> < ").append(combining_search_result.mflag.data).append(" ></font >").toString()));
//			} else {
//
//			}
		
		BookPresenter book = a.getBookById(results.bookId);
		CharSequence preview = book.hasPreview() ? results.getPreviewAt(book, a, position, vh) : null;
		boolean set0 = id != 5 || PDICMainAppOptions.listPreviewSet01Same();
		boolean selectable = set0 ? PDICMainAppOptions.listPreviewSelectable() : PDICMainAppOptions.listPreviewSelectable1();
		if (preview != null) {
			int maxLines = (set0 ? PDICMainAppOptions.listOverreadMode() : PDICMainAppOptions.listOverreadMode1()) ? Integer.MAX_VALUE : 3;
			int tmp = set0 ? PDICMainAppOptions.listPreviewColor() : PDICMainAppOptions.listPreviewColor1();
			int color = ColorUtils.blendARGB(a.AppWhite, a.AppBlack, tmp == 0 ? 0.08f : tmp == 1 ? 0.5f : 0.8f);
			tmp = set0 ? PDICMainAppOptions.listPreviewFont() : PDICMainAppOptions.listPreviewFont1();
			int size = tmp == 0 ? 12 : tmp == 1 ? 14 : 17;
			{
				if (preview!=ViewUtils.WAIT) {
					vh.preview.setText(preview);
				}
				vh.preview.setTextColor(color);
				vh.preview.setTextSize(size);
				vh.preview.setMaxLines(maxLines);
			}
			ViewUtils.setVisible(vh.preview, true);
			ViewUtils.setVisible(vh.subtitle, false);
		} else {
			boolean showBookName = PDICMainAppOptions.listShowBookName();
			ViewUtils.setVisible(vh.preview, false);
			ViewUtils.setVisible(vh.subtitle, showBookName);
			if (showBookName) {
				vh.subtitle.setText(book.getDictionaryName());
			}
		}
		if (selectable!=vh.selectable)
		{
			vh.title.setTextIsSelectable(selectable);
			vh.preview.setTextIsSelectable(selectable);
			vh.title.getLayoutParams().width = selectable?-2:-1;
			vh.itemView.setOnClickListener(selectable?this:null);
			vh.selectable = selectable;
		}
		
		if(id==2) {
			TextView v = ((TextView) vh.subtitle.getTag());
			if(results.count!=null) {
				v.setText(results.count);
				v.setVisibility(View.VISIBLE);
			} else {
				v.setVisibility(View.GONE);
			}
		}
		//vh.itemView.setTag(R.id.position,position);
		return vh.itemView;
	}
	
	@Override
	public void shutUp() {
		results.shutUp();
		notifyDataSetChanged();
	}
	
	@Override
	public void SaveVOA() {
		//111
//		if(this!=a.adaptermy2||a.opt.getRemPos2()) {
//			new a.SaveAndRestorePagePosDelegate().SaveVOA(contentUIData.PageSlider.WebContext, this);
//		}
//		//lastClickTime=System.currentTimeMillis();
		if (browsed && PDICMainAppOptions.storePageTurn()==2
				&& results.shouldSaveHistory()
				&& !PDICMainAppOptions.storeNothing() && PDICMainAppOptions.storeClick()) {
			a.addHistory(currentKeyText, a.schuiList, weblistHandler, null);
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
		if(checkAllWebs(results, view, pos)) return;
		contentUIData.mainProgressBar.setVisibility(View.GONE);
		userCLick=true;
		lastClickedPosBefore=-1;
//		a.bNeedReAddCon=false;
		super.onItemClick(parent, view, pos, id);
	}
	
	boolean checkAllWebs(resultRecorderDiscrete result, View view, int pos) {
		if(result instanceof resultRecorderCombined && pos==0 && view==null){
			if(a.mergeFrames()!=2 && ((resultRecorderCombined)result).checkAllWebs(a)){
				CMN.Log("驳回！！！");
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void onItemClick(int pos){//lv2 mlv1 mlv2
		a.shuntAAdjustment();
		weblistHandler.WHP.touchFlag.first=true;
		
		if(a.DBrowser!=null) return;
		lastClickedPosBefore = lastClickedPos;
		if(pos<0 || pos>=getCount()) {
			a.showTopSnack(R.string.endendr);
			return;
		}
		String lstKey = currentKeyText = results.getResAt(a, pos).toString();
		
		if(a.PeruseListModeMenu.isChecked()) {
			PeruseView pView = a.getPeruseView();
			boolean storeSch = results.shouldAddHistory(a);
			if(storeSch) { // 保存输入框历史记录
				a.addHistory(results.schKey, results.storeRealm, weblistHandler, a.etTools);
				pView.lstKey = lstKey;
			} else {
				pView.lstKey = null;
			}
			if(!pView.keepGroup() || pView.bookIds.size()==0) {
				results.getBooksAt(pView.bookIds, pos);
			}
			a.JumpToPeruseMode(lstKey, pView.bookIds, -2, true);
			a.imm.hideSoftInputFromWindow(a.main.getWindowToken(),0);
			return;
		}
		
		if(allMenus!=null) {
			allMenus.setItems(contentMenus);
		}
		//a.showT(allMenus+""+contentMenus);
		
		
		if(id==2) {
			weblistHandler.bShowInPopup = false;
			weblistHandler.bMergeFrames = a.mergeFrames();
			
//			boolean bUseMergedUrl = false;
//			weblistHandler.setViewMode(WEB_LIST_MULTI, bUseMergedUrl);
//			weblistHandler.bMergeFrames = bUseMergedUrl;
//			a.ensureContentVis(weblistHandler, contentUIData.webSingleholder);
//			weblistHandler.initMergedFrame(bUseMergedUrl, false, bUseMergedUrl);

//			if(bUseMergedUrl) {
//				ViewUtils.addViewToParentUnique(weblistHandler.getMergedFrame().rl, weblistHandler.WHP1);
//			}
			//a.showT("111 !");
			
			//////////a.locateNaviIcon();
//			if(opt.getRemPos2() && !a.bRequestedCleanSearch) {
//				new a.SaveAndRestorePagePosDelegate().SaveAndRestorePagesForAdapter(this, pos);
//			}
			//111
		}
		else {
			boolean bUseMergedUrl = false;
			weblistHandler.setViewMode(null, 0, null);
			weblistHandler.initMergedFrame(0, false, bUseMergedUrl);
			if(bUseMergedUrl) {
				ViewUtils.addViewToParentUnique(weblistHandler.getMergedFrame().rl, a.webSingleholder);
			}
			
			a.viewContent(weblistHandler);
		}

		a.ActivedAdapter=this;
		super.onItemClick(pos);

//		contentUIData.webcontentlister.setVisibility(View.VISIBLE);
//		contentUIData.webcontentlister.setAlpha(1);
		
		if(!a.bWantsSelection) {
			a.imm.hideSoftInputFromWindow(a.main.getWindowToken(),0);
			a.etSearch.clearFocus();
		}
		
		results.renderContentAt(lastClickedPos, a,this, weblistHandler);
		
		weblistHandler.setStar(lstKey);
		
		
		a.bWantsSelection=true;
//		if(PDICMainAppOptions.getInPageSearchAutoUpdateAfterClick()){
//			a.prepareInPageSearch(lstKey, true);
//		} //333
		contentUIData.webcontentlister.setTag(R.id.image, a.PhotoPagerHolder!=null&&a.PhotoPagerHolder.getParent()!=null?false:null);
		a.etSearch_ToToolbarMode(1);
		
		boolean storeSch = results.shouldAddHistory(a);
		if(storeSch) {
			a.addHistory(results.schKey, results.storeRealm, weblistHandler, a.etTools);
		}
		if((!storeSch || !results.schKey.equals(lstKey))
				&& !PDICMainAppOptions.storeNothing() && PDICMainAppOptions.storeClick()
				&& results.shouldSaveHistory()
				&& (userCLick||PDICMainAppOptions.storePageTurn()==0)) {
			a.addHistory(lstKey, results.storeRealm1, weblistHandler, a.etTools);
		}
		
		if(userCLick) {
			userCLick=false;
		} else {
			browsed =true;
		}
	}
	
	
	@Override
	public int getId() {
		return id;
	}
	
	@Override
	public String currentKeyText() {
		return //results instanceof resultRecorderScattered?
//				((resultRecorderScattered) results).getCurrentKeyText(a, lastClickedPos)
				currentKeyText;
	}
}
