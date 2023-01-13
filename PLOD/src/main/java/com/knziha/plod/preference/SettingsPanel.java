package com.knziha.plod.preference;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.GlobalOptions;

import com.knziha.plod.PlainUI.PlainDialog;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.PDICMainAppOptions;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.dictionary.Utils.IU;
import com.knziha.plod.widgets.ViewUtils;
import com.knziha.plod.widgets.XYLinearLayout;

import java.util.Objects;

public class SettingsPanel extends AnimatorListenerAdapter implements View.OnClickListener {
	@NonNull public ViewGroup settingsLayout;
	public LinearLayout linearLayout;
	public Object tag;
	protected boolean bIsShowing;
	public final static int PANEL_SHOW_TYPE_POPUP = 1;
	public final static int PANEL_SHOW_TYPE_DIALOG = 2;
	protected int rootHash;
	public RadioSwitchButton actView;
	
	/**0=view;1=popup;2=dialog*/
	public int getLastShowType() {
		return lastShowType;
	}
	
	protected int lastShowType;
	protected int bFadeout;
	/**0=view;1=popup;2=dialog*/
	protected int showType;
	public int bottomPadding;
	public /*final*/ PDICMainAppOptions opt;
	protected String[][] UITexts;
	protected int[][] UITags;
	protected int[][] UIDrawable;
	protected boolean isHorizontal;
	protected boolean isHorizontalItems;
	protected boolean shouldWrapInScrollView = true;
	protected boolean bShouldRemoveAfterDismiss = true;
	protected boolean bSuppressNxtAnimation = false;
	protected boolean bAnimate = true;
	protected boolean bAutoRefresh = false;
	protected boolean hasDelegatePicker;
	public PopupWindow pop;
	public Dialog dialog;
	protected DialogInterface.OnDismissListener dialogDismissListener;
	protected int mPaddingLeft=10;
	protected int mPaddingRight=10;
	protected int mPaddingTop=0;
	protected int mPaddingBottom=0;
	protected int mInnerBottomPadding=0;
	protected int mItemPaddingLeft=5;
	protected int mItemPaddingTop=8;
	protected int mItemPaddingBottom=8;
	// 0=menu grid; 1=quick settings;
	protected int mBackgroundColorType;
	protected int mBackgroundColor = 0xefffffff;
	protected int mViewAttachIdx = -1;
	protected SettingsPanel parent;
	private SettingsPanel parentToDismiss;
	
	public void setPresetBgColorType(int type) {
		mBackgroundColorType = type;
		if (type==0) {
			mBackgroundColor = GlobalOptions.isDark?Color.TRANSPARENT:0x20FFEEEE; // 0x3E8F8F8F
		} else if(type==1) {
			mBackgroundColor = GlobalOptions.isDark?0xef333333:0xefffffff; // 0x3E8F8F8F
		}
	}
	
	public void setEmbedded(ActionListener actionListener){
		mPaddingLeft = 15;
		mPaddingTop = 2;
		mPaddingBottom = 15;
		shouldWrapInScrollView = false;
		bShouldRemoveAfterDismiss = false;
		mItemPaddingLeft = 8;
		mItemPaddingTop = 9;
		mItemPaddingBottom = 9;
		mActionListener = actionListener;
		hasDelegatePicker = true;
		if (actionListener instanceof SettingsPanel) {
			parent = (SettingsPanel) actionListener;
		}
	}
	
	public void setActionListener(ActionListener mActionListener) {
		this.mActionListener = mActionListener;
	}
	
	final static int MAX_FLAG_COUNT=7; // 当前支持7个标志位存储容器，其中第六第七为动态容器。
	
	final static int MAX_FLAG_POS_MASK=(1<<8)-1; // 长整型，位数 64，存储需要 7 位。
	
	protected final static int FLAG_IDX_SHIFT=16;
	
	final static int BIT_IS_REVERSE=1<<9;
	final static int BIT_IS_DYNAMIC=1<<10;
	protected final static int BIT_HAS_ICON=1<<11;
	public final static int BIT_STORE_VIEW=1<<12;
	
	// Flag-Idx  BITs   Flag-Pos
	// 索引   选项   偏移
	
	public final static int makeInt(int flagIdx, int flagPos, boolean reverse) {
		return makeInt(flagIdx, flagPos, reverse, false);
	}
	
	public static int makeDynInt(int flagIdx, int flagPos, boolean reverse) {
		return makeInt(flagIdx, flagPos, reverse, true);
	}
	
	protected static int makeDynIcoInt(int flagIdx, int flagPos, boolean reverse) {
		return makeInt(flagIdx, flagPos, reverse, true)|BIT_HAS_ICON;
	}
	
	protected static int makeInt(int flagIdx, int flagPos, boolean reverse, boolean dynamic) {
		int ret=flagPos&MAX_FLAG_POS_MASK;
		if (reverse) {
			ret|=BIT_IS_REVERSE;
		}
		if (dynamic) {
			ret|=BIT_IS_DYNAMIC;
		}
		ret|=flagIdx<<FLAG_IDX_SHIFT;
		return ret;
	}
	
	public void setInnerBottomPadding(int padding) {
		if (lastShowType==0) {
			View v = settingsLayout.getChildAt(0);
			v.setPadding(0, 0, 0, mInnerBottomPadding = padding);
		}
	}
	
	public void setHorizontalItems(boolean h) {
		isHorizontalItems = h;
	}
	
	public interface ActionListener{
		boolean onAction(View v, SettingsPanel settingsPanel, int flagIdxSection, int flagPos, boolean dynamic, boolean val, int storageInt);
		void onPickingDelegate(SettingsPanel settingsPanel, int flagIdxSection, int flagPos, int lastX, int lastY);
	}
	protected ActionListener mActionListener;
	
	public interface FlagAdapter{
		/** 取得标志位 */
		long Flag(int flagIndex);
		/** 保存标志位 */
		void Flag(int flagIndex, long val);
		/** 计算动态容器索引 */
		int getDynamicFlagIndex(int flagIdx);
		/** 选择动态容器 */
		void pickDelegateForSection(int flagIdx, int pickIndex);
	}
	protected final FlagAdapter mFlagAdapter;
	
	/** 装饰动态索引 */
	protected Drawable getIconForDynamicFlagBySection(int section){
		if (parent!=null) {
			return parent.getIconForDynamicFlagBySection(section);
		}
		return null;
	};
	
	/** 取得动态容器 */
	public long getDynamicFlag(int section) {
		return mFlagAdapter.Flag(mFlagAdapter.getDynamicFlagIndex(section));
	}
	
	/** 保存动态容器 */
	public void putDynamicFlag(int scrollSettings, long flag) {
		mFlagAdapter.Flag(mFlagAdapter.getDynamicFlagIndex(scrollSettings), flag);
	}
	
	protected boolean getBooleanInFlag(int storageInt) {
		//if (storageInt==0||storageInt==Integer.MAX_VALUE) return false;
		int flagIdx=storageInt>>FLAG_IDX_SHIFT;
		if(flagIdx==0) {
			return false;
		}
		int flagPos=storageInt&MAX_FLAG_POS_MASK;
		boolean reverse = (storageInt&BIT_IS_REVERSE)!=0;
		boolean dynamic = (storageInt&BIT_IS_DYNAMIC)!=0;
		if (dynamic) {
			flagIdx = mFlagAdapter.getDynamicFlagIndex(flagIdx);
		}
		return EvalBooleanForFlag(flagIdx, flagPos, reverse);
	}
	
	protected void setBooleanInFlag(View v, int storageInt, boolean val) {
		int flagIdx=storageInt>>FLAG_IDX_SHIFT;
		int flagPos=storageInt&MAX_FLAG_POS_MASK;
		CMN.Log("setBooleanInFlag", flagIdx, val, flagPos);
		boolean reverse = (storageInt&BIT_IS_REVERSE)!=0;
		boolean dynamic = (storageInt&BIT_IS_DYNAMIC)!=0;
		if(flagIdx!=0) {
			PutBooleanForFlag(dynamic?mFlagAdapter.getDynamicFlagIndex(flagIdx):flagIdx, flagPos, val, reverse);
		}
		onAction(v, flagIdx, flagPos, dynamic, val, storageInt);
	}
	
	public boolean EvalBooleanForFlag(int flagIndex, int flagPos, boolean reverse) {
		long flag = mFlagAdapter.Flag(flagIndex);
		return reverse ^ (((flag>>flagPos)&0x1)!=0);
	}
	
	public void PutBooleanForFlag(int flagIndex, int flagPos, boolean value, boolean reverse) {
		long flag = mFlagAdapter.Flag(flagIndex);
		long mask = 1l<<flagPos;
		if(value ^ reverse) {
			flag |= mask;
		} else {
			flag &= ~mask;
		}
		mFlagAdapter.Flag(flagIndex, flag);
	}
	
	public boolean onAction(View v, int flagIdx, int flagPos, boolean dynamic, boolean val, int storageInt) {
		if (mActionListener!=null) {
			return mActionListener.onAction(v, this, flagIdx, flagPos, dynamic, val, storageInt);
		}
		return true;
	}
	
	public SettingsPanel(@NonNull FlagAdapter mFlagAdapter, @NonNull PDICMainAppOptions opt, String[][] UITexts, int[][] UITags, int[][] drawable) {
		this.bottomPadding = 0;
		this.opt = opt;
		this.mFlagAdapter = mFlagAdapter;
		this.UITexts = UITexts;
		this.UITags = UITags;
		this.UIDrawable = drawable;
	}
	
	public SettingsPanel(@NonNull Context context, @NonNull ViewGroup root, int bottomPadding, @NonNull PDICMainAppOptions opt, @NonNull FlagAdapter mFlagAdapter) {
		this.bottomPadding = bottomPadding;
		this.opt = opt;
		this.mFlagAdapter = mFlagAdapter;
		init(context, root);
		ViewGroup sl = this.settingsLayout;
		if(sl!=null) {
			if (bottomPadding>0) {
				sl.setAlpha(0);
				if(isHorizontal) {
					sl.setTranslationX(bottomPadding);
				} else {
					sl.setTranslationY(bottomPadding);
				}
				sl.setBackgroundColor(mBackgroundColor);
			} else {
				sl.setBackgroundColor(mBackgroundColor);
			}
			if(root!=null && showType==0) {
				ViewUtils.addViewToParent(sl, root);
			}
		}
	}
	
	public void init(Context context, ViewGroup root) {
		//settingsLayout = getLayoutInflater().inflate(R.layout.test_settings, UIData.webcoord, false);
		if (settingsLayout!=null||context==null||UITexts==null) {
			return;
		}
		LinearLayout lv = linearLayout = hasDelegatePicker?new XYLinearLayout(context):new LinearLayout(context);
		lv.setOrientation(LinearLayout.VERTICAL);
		float density = GlobalOptions.density;
		lv.setPadding((int) (mPaddingLeft*density), (int) (mPaddingTop*density), (int) (mPaddingRight*density), (int) (mPaddingBottom*density));
		//ArrayList<View> views;
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
		if (isHorizontalItems) {
			lp.width=0;
			lp.weight=1;
			lv.setOrientation(LinearLayout.HORIZONTAL);
		}
		int storageInt;
		for (int i = 0; i < UITexts.length; i++) {
			String[] group = UITexts[i];
			int[] tags_group = UITags[i];
			if(group[0]!=null) { // 需显示标题
				TextView groupTitle = new TextView(context, null, 0);
				if(GlobalOptions.isDark) groupTitle.setTextColor(Color.WHITE);
				//groupTitle.setTextAppearance(android.R.attr.textAppearanceLarge);
				String text = group[0];
				if (text.startsWith("<")) {
					groupTitle.setText(Html.fromHtml(text));
				} else {
					groupTitle.setText(text);
				}
				groupTitle.setPadding((int) (2*density), (int) (8*density), 0, (int) (8*density));
				lv.addView(groupTitle, lp);
			}
			for (int j = 1; j < group.length; j++) { // 建立子项
				RadioSwitchButton button = new RadioSwitchButton(context);
				if(GlobalOptions.isDark) button.setTextColor(Color.WHITE);
				button.setText(group[j]);
				button.setButtonDrawable(R.drawable.radio_selector);
				button.setPadding((int) (mItemPaddingLeft*density), (int) (mItemPaddingTop*density), 0, (int) (mItemPaddingBottom*density));
				button.setOnClickListener(this);
				storageInt = tags_group[j];
				if (storageInt != Integer.MAX_VALUE) {
					if(storageInt!=0) {
						button.setChecked(getBooleanInFlag(storageInt));
					}
					//button.setTag(storageInt);
					button.setId(storageInt);
				}
				if ((storageInt&BIT_HAS_ICON)!=0) {
					Drawable drawable = getIconForDynamicFlagBySection(storageInt>>FLAG_IDX_SHIFT);
					//CMN.Log("drawable::??", drawable);
					if (drawable!=null) {
						button.setCompoundDrawables(null, null, drawable, null);
					}
				}
				lv.addView(button, lp);
				//button.getLayoutParams().width=-2;
			}
		}
		//View v = new View(context);
		//lv.addView(v);
		//v.getLayoutParams().width = -1;
		//v.getLayoutParams().height = (int) (bottomPaddding);
		if (isHorizontalItems) {
			lp = new LinearLayout.LayoutParams(-1, -2);
		}
		if (shouldWrapInScrollView) {
			ScrollView sv = new ScrollView(context);
			sv.addView(lv, lp);
			sv.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
			settingsLayout = sv;
		} else {
			settingsLayout = lv;
		}
	}
	
	public void refresh() {
		if(linearLayout!=null)
		for (int i = 0, len=linearLayout.getChildCount(); i < len; i++) {
			View v = linearLayout.getChildAt(i);
			if (v instanceof RadioSwitchButton && v.getTag()!=this) {
				RadioSwitchButton button = (RadioSwitchButton)v;
				int storageInt = v.getId();
				if (storageInt!=Integer.MAX_VALUE) {
					button.setChecked(getBooleanInFlag(storageInt));
					if ((storageInt&BIT_HAS_ICON)!=0) {
						Drawable drawable = getIconForDynamicFlagBySection(storageInt>>FLAG_IDX_SHIFT);
						if (drawable!=null) {
							button.setCompoundDrawables(null, null, drawable, null);
						}
					}
				}
			}
		}
	}
	
	public boolean toggle(ViewGroup root, SettingsPanel parentToDismiss, int forceShowType) {
		//CMN.Log("toggle!!!",!bIsShowing);
		//try {
		//	throw new RuntimeException();
		//} catch (RuntimeException e) {
		//	CMN.Log(e);
		//}
		if (settingsLayout==null) {
			if (root != null) {
				init(root.getContext(), root);
			}
			if(settingsLayout==null) {
				return bIsShowing=!bIsShowing;
			}
		}
		float targetAlpha = 1;
		float targetTrans = 0;
		if (bIsShowing=!bIsShowing) {
			rootChanged(root);
			forceShowType = forceShowType>=0?forceShowType:showType;
			if(lastShowType!=forceShowType) {
				ViewUtils.removeView(settingsLayout);
				lastShowType=forceShowType;
			}
			// CMN.debug("forceShowType::", forceShowType);
			if (forceShowType==1) {
				showPop(root);
			} else if(forceShowType==2) {
				showDialog();
			} else {
				ViewUtils.addViewToParent(settingsLayout, root, mViewAttachIdx);
			}
			settingsLayout.setVisibility(View.VISIBLE);
			if(bAutoRefresh) {
				refresh();
			}
		} else {
			if(bFadeout==0 || bFadeout==-2 && lastShowType==0) {
				targetAlpha = 0;
			}
			targetTrans = bottomPadding;
		}
		if (bSuppressNxtAnimation || !bAnimate) {
			if (isHorizontal) {
				settingsLayout.setTranslationX(targetTrans);
			} else {
				settingsLayout.setTranslationY(targetTrans);
			}
			settingsLayout.setAlpha(targetAlpha);
			onAnimationEnd(null);
			bSuppressNxtAnimation = false;
		} else {
			ViewPropertyAnimator animator = settingsLayout.animate().alpha(targetAlpha);
			if (isHorizontal) {
				animator.translationX(targetTrans);
			} else {
				animator.translationY(targetTrans);
			}
			animator.setDuration(220)
					.setListener(this)
			//.start()
			;
		}
		if (!bIsShowing) {
			onDismiss();
		} else {
			onShow();
			if (parentToDismiss!=null) {
				this.parentToDismiss = parentToDismiss;
			}
		}
		return bIsShowing;
	}
	
	protected void showPop(ViewGroup root) {
		throw new RuntimeException("Stub!");
	}
	
	protected void showDialog() {
		throw new RuntimeException("Stub!");
	}
	
	protected void onDismiss() { }
	
	protected void onShow() { }
	
	@Override
	public void onAnimationEnd(Animator animation) {
		ValueAnimator va = (ValueAnimator) animation;
		if (va==null || va.getAnimatedFraction()==1) {
			if (!bIsShowing) {
				CMN.debug("dismiss!!!", lastShowType);
				if (lastShowType==1) { pop.dismiss(); }
				if (lastShowType==2) {
					dialog.setOnDismissListener(null);
					dialog.dismiss();
					dialog.setOnDismissListener(dialogDismissListener);
				}
				else {
					settingsLayout.setVisibility(View.GONE);
					if (bShouldRemoveAfterDismiss) {
						ViewUtils.removeView(settingsLayout);
					}
				}
				if (settingsLayout.getAlpha()==0 && (isHorizontal?settingsLayout.getTranslationX():settingsLayout.getTranslationY())==0) {
					if (isHorizontal) {
						settingsLayout.setTranslationX(bottomPadding);
					} else {
						settingsLayout.setTranslationY(bottomPadding);
					}
				}
			} else {
				if (parentToDismiss!=null) {
					if ((mBackgroundColor&0xff000000)!=0xff000000) {
						parentToDismiss.fadeOut();
					} else {
						parentToDismiss.hide();
					}
					parentToDismiss = null;
				}
			}
		}
		
	}
	
	@Override
	public void onClick(View v) {
		if (v instanceof RadioSwitchButton) {
			RadioSwitchButton button = (RadioSwitchButton)v;
			//((Toastable_Activity)v.getContext()).showT("v::"+button.isChecked());
			int storageInt = v.getId();
			CMN.Log("storageInt::", v.getTag(), storageInt, (storageInt>>FLAG_IDX_SHIFT), button.isChecked());
			if(storageInt!=0) {
				Drawable d;
				if (linearLayout instanceof XYLinearLayout && (d=button.getCompoundDrawables()[2])!=null && (storageInt&BIT_IS_DYNAMIC)!=0) {
					XYLinearLayout xy = ((XYLinearLayout) linearLayout);
					if (xy.lastX>xy.getWidth()-v.getPaddingRight()-d.getIntrinsicWidth()- GlobalOptions.density*8) {
						button.setChecked(!button.isChecked());
						if (mActionListener!=null) {
							mActionListener.onPickingDelegate(this, storageInt>>FLAG_IDX_SHIFT, storageInt&MAX_FLAG_POS_MASK, (int)xy.lastX, (int)xy.lastY+settingsLayout.getTop());
						}
						//button.jumpDrawablesToCurrentState();
						return;
					}
				}
				if((storageInt&BIT_STORE_VIEW)!=0) {
					actView = button;
				}
				setBooleanInFlag(v, storageInt, button.isChecked());
				if((storageInt>>FLAG_IDX_SHIFT)==0) {
					button.setChecked(false); // 容器索引为零，代表非选项
				}
				//((Toastable_Activity)v.getContext()).showT(
				//		"v::"+button.isChecked()+"::"+getBooleanInFlag(storageInt)+opt.getHideKeyboardOnScrollSearchHints());
				
			}/* else if (mActionListener!=null) {
				mActionListener.onAction(this, storageInt>>FLAG_IDX_SHIFT, storageInt&MAX_FLAG_POS_MASK, false, false);
			}*/
		}
	}
	
	public final boolean isVisible() {
		return bIsShowing;
	}
	
	public boolean rootChanged(View root) {
		int hash = Objects.hashCode(root);
		if(hash != rootHash) {
			hash = rootHash;
			return true;
		}
		return false;
	}
	
	public void dismiss() {
		if(bIsShowing) {
			toggle(null, null, 0);
		}
	}
	
	public void dismissImmediate() {
		if(bIsShowing) {
			bSuppressNxtAnimation = true;
			toggle(null, null, 0);
		}
	}
	
	private void fadeOut() {
		if(bIsShowing) {
			bIsShowing=false;
			settingsLayout
				.animate()
				.alpha((bFadeout==0 || bFadeout==-2 && lastShowType==0)?0:1)
				.setDuration(300)
				.setListener(this);
			onDismiss();
		}
	}
	
	public void hide() {
		if(bIsShowing) {
			bIsShowing=false;
			settingsLayout.setAlpha(0);
			if (isHorizontal) {
				settingsLayout.setTranslationX(bottomPadding);
			} else {
				settingsLayout.setTranslationY(bottomPadding);
			}
			onAnimationEnd(null);
			onDismiss();
		}
	}
	
	public void setShowInPop() {
		showType = PANEL_SHOW_TYPE_POPUP;
	}
	
	public void setShowInDialog() {
		showType = PANEL_SHOW_TYPE_DIALOG;
	}
}
	