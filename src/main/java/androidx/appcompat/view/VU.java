package androidx.appcompat.view;

import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.appcompat.R;
import androidx.appcompat.app.CMN;
import androidx.core.view.NestedScrollingChildHelper;

public class VU {
	public final static NestedScrollingChildHelper sNestScrollHelper=new NestedScrollingChildHelper(null);
	public static boolean suppressNxtDialogReorder;
	
	public static View getViewItemByPath(View obj, int...path) {
		int cc=0;
		while(cc<path.length) {
			//CMN.Log(cc, obj);
			if(obj instanceof ViewGroup) {
				obj = ((ViewGroup)obj).getChildAt(path[cc]);
			} else {
				obj = null;
				break;
			}
			cc++;
		}
		return (View)obj;
	}
	
	public static View getNextView(View child) {
		View ret;
		if (child instanceof ViewGroup) {
			ret = ((ViewGroup) child).getChildAt(0);
			if (ret!=null) return ret;
		}
		while (child!=null && child.getParent() instanceof ViewGroup) {
			ViewGroup p = ((ViewGroup) child.getParent());
			ret = p.getChildAt(p.indexOfChild(child) + 1);
			if (ret != null) {
				return ret;
			}
			child = p;
		}
		return null;
	}
	
	public static void setOnClickListenersOneDepth(ViewGroup vg, View.OnClickListener clicker, int depth, Object[] viewFetcher) {
		int cc = vg.getChildCount();
		View ca;
		boolean longClickable = clicker instanceof View.OnLongClickListener;
		boolean touhable = clicker instanceof View.OnTouchListener;
		if(vg.isClickable()) {
			click(vg, clicker, longClickable, touhable);
		}
		for (int i = 0; i < cc; i++) {
			ca = vg.getChildAt(i);
			//CMN.Log("setOnClickListenersOneDepth", ca, (i+1)+"/"+(cc));
			if(ca instanceof ViewGroup) {
				if(--depth>0) {
					if(ca.isClickable()) {
						click(ca, clicker, longClickable, touhable);
					} else {
						setOnClickListenersOneDepth((ViewGroup) ca, clicker, depth, viewFetcher);
					}
				}
			} else {
				int id = ca.getId();
				if(ca.getId()!=View.NO_ID || ca.isClickable()){
					if(!(ca instanceof EditText) && ca.isEnabled()) {
						click(ca, clicker, longClickable, touhable);
					}
					if(viewFetcher!=null) {
						for (int j = 0; j < viewFetcher.length; j++) {
							if(viewFetcher[j] instanceof Integer && (int)viewFetcher[j]==id) {
								viewFetcher[j]=ca;
								break;
							}
						}
					}
				}
			}
		}
	}
	
	public static void setOnClickListenersOneDepth(ViewGroup vg, View.OnClickListener clicker, SparseArray<View> viewFetcher, int depth) {
		int cc = vg.getChildCount();
		View ca;
		boolean longClickable = clicker instanceof View.OnLongClickListener;
		boolean touhable = clicker instanceof View.OnTouchListener;
		if(vg.isClickable()) {
			click(vg, clicker, longClickable, touhable);
		}
		for (int i = 0; i < cc; i++) {
			ca = vg.getChildAt(i);
			//CMN.Log("setOnClickListenersOneDepth", ca, (i+1)+"/"+(cc));
			if(ca instanceof ViewGroup) {
				if(--depth>0) {
					if(ca.isClickable()) {
						click(ca, clicker, longClickable, touhable);
					} else {
						setOnClickListenersOneDepth((ViewGroup) ca, clicker, viewFetcher, depth);
					}
				}
			} else {
				int id = ca.getId();
				if(ca.getId()!=View.NO_ID){
					if(!(ca instanceof EditText) && ca.isEnabled()) {
						click(ca, clicker, longClickable, touhable);
					}
					if(viewFetcher!=null) {
						viewFetcher.put(ca.getId(), ca);
					}
				}
			}
		}
	}
	
	private static void click(View ca, View.OnClickListener clicker, boolean longClickable, boolean touhable) {
		ca.setOnClickListener(clicker);
		if(longClickable&&ca.isLongClickable()) {
			ca.setOnLongClickListener((View.OnLongClickListener) clicker);
		}
		if(touhable) {
			ca.setOnTouchListener((View.OnTouchListener) clicker);
		}
	}
	
	public static boolean removeView(View viewToRemove) {
		return removeIfParentBeOrNotBe(viewToRemove, null, false);
	}
	
	public static boolean removeIfParentBeOrNotBe(View view, ViewGroup parent, boolean tobe) {
		if(view!=null) {
			ViewParent svp = view.getParent();
			if((parent!=svp) ^ tobe) {
				if(svp!=null) {
					((ViewGroup)svp).removeView(view);
					//CMN.Log("removing from...", svp, view.getParent(), view);
					return view.getParent()==null;
				}
				return true;
			}
		}
		return false;
	}
	
	public static boolean addViewToParent(View view2Add, ViewGroup parent, int index) {
		if(removeIfParentBeOrNotBe(view2Add, parent, false)) {
			int cc=parent.getChildCount();
			if(index<0) {
				index = cc+index;
				if(index<0) {
					index = 0;
				}
			} else if(index>cc) {
				index = cc;
			}
			parent.addView(view2Add, index);
			return true;
		}
		return false;
	}
	
	public static boolean addViewToParent(View view2Add, ViewGroup parent, View index) {
		return addViewToParent(view2Add, parent, parent.indexOfChild(index)+1);
	}
	
	public static boolean addViewToParent(View view2Add, ViewGroup parent) {
		if(parent!=null && removeIfParentBeOrNotBe(view2Add, parent, false)) {
			parent.addView(view2Add);
			return true;
		}
		return false;
	}
	
	public static void postInvalidateLayout(View view) {
		view.post(view::requestLayout);
	}
	
	
	public static View findViewById(ViewGroup vg, int id) {
		for (int i = 0,len=vg.getChildCount(); i < len; i++) {
			View c = vg.getChildAt(i);
			if(c.getId()==id) return c;
		}
		return vg.findViewById(id);
	}
	
	public static View findViewById(ViewGroup vg, int id, int st) {
		for (int i = st,len=vg.getChildCount(); i < len; i++) {
			View c = vg.getChildAt(i);
			if(c.getId()==id) return c;
		}
		return vg.findViewById(id);
	}
	
	public static Button craftBtn(Context context, int id, int strId, ViewGroup parent) {
		Button btn = new Button(context, null, android.R.attr.buttonBarButtonStyle);
		btn.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		btn.setId(id);
		btn.setText(strId);
		if (parent != null) {
			parent.addView(btn);
		}
		return btn;
	}
	
	public static View craftLinearPadding(Context context, LinearLayout parent) {
		View pad = new View(context);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, 0);
		pad.setLayoutParams(lp);
		lp.weight=1;
		if (parent != null) {
			parent.addView(pad);
		}
		return pad;
	}
	
	public static LinearLayout.LayoutParams linearLayoutParams(View pad) {
		return (LinearLayout.LayoutParams) pad.getLayoutParams();
	}
	
	public static boolean isVisible(View v) {
		return v.getVisibility()==View.VISIBLE;
	}
	
	public static boolean isVisibleV2(View v) {
		return v!=null && v.getVisibility()==View.VISIBLE && v.getParent()!=null;
	}
	
	public static void setVisible(View v, boolean visible) {
		v.setVisibility(visible?View.VISIBLE:View.GONE);
	}
	
	public static void setVisibility(View v, boolean visible) {
		v.setVisibility(visible?View.VISIBLE:View.INVISIBLE);
	}
	
	public static void setVisibleV3(View v, boolean visible) {
		int vis = visible?View.VISIBLE:View.INVISIBLE;
		if(v.getVisibility()!=vis) v.setVisibility(vis);
	}
	
	public static void setVisibleV2(View v, boolean visible) {
		if(v!=null)v.setVisibility(visible?View.VISIBLE:View.GONE);
	}
	
	public static final FrameLayout.LayoutParams newFrameLayoutParams(FrameLayout.LayoutParams source) {
		final FrameLayout.LayoutParams ret = new FrameLayout.LayoutParams(source);
		ret.leftMargin = source.leftMargin;
		ret.topMargin = source.topMargin;
		ret.rightMargin = source.rightMargin;
		ret.bottomMargin = source.bottomMargin;
		ret.gravity = source.gravity;
		return ret;
	}
	
	public static Drawable getListChoiceBackground(Context context) {
		TypedArray ta = context.obtainStyledAttributes(new int[] {R.attr.listChoiceBackgroundIndicator});
		Drawable draw = ta.getDrawable(0);
		ta.recycle();
		return draw;
	}
	
	public static boolean isInstalled(Context context, String packageName) {
		if (!TextUtils.isEmpty(packageName)) {
			try {
				context.getPackageManager().getApplicationInfo(packageName, 0);
				return true;
			} catch (Exception e) {
				//CMN.Log(e);
			}
		}
		return false;
	}
	
	
	public static ObjectAnimator tada(View view, float shakeFactor) {
		PropertyValuesHolder pvhScaleX = PropertyValuesHolder.ofKeyframe(View.SCALE_X,
				Keyframe.ofFloat(0f, 1f),
				Keyframe.ofFloat(.1f, .9f),
				Keyframe.ofFloat(.2f, .9f),
				Keyframe.ofFloat(.3f, 1.1f),
				Keyframe.ofFloat(.4f, 1.1f),
				Keyframe.ofFloat(.5f, 1.1f),
				Keyframe.ofFloat(.6f, 1.1f),
				Keyframe.ofFloat(1f, 1f)
		);
		PropertyValuesHolder pvhScaleY = PropertyValuesHolder.ofKeyframe(View.SCALE_Y,
				Keyframe.ofFloat(0f, 1f),
				Keyframe.ofFloat(.1f, .9f),
				Keyframe.ofFloat(.2f, .9f),
				Keyframe.ofFloat(.3f, 1.1f),
				Keyframe.ofFloat(.4f, 1.1f),
				Keyframe.ofFloat(.5f, 1.1f),
				Keyframe.ofFloat(.6f, 1.1f),
				Keyframe.ofFloat(1f, 1f)
		);
		PropertyValuesHolder pvhRotate = PropertyValuesHolder.ofKeyframe(View.ROTATION,
				Keyframe.ofFloat(0f, 0f),
				Keyframe.ofFloat(.1f, -3f * shakeFactor),
				Keyframe.ofFloat(.2f, -3f * shakeFactor),
				Keyframe.ofFloat(.3f, 3f * shakeFactor),
				Keyframe.ofFloat(.4f, -3f * shakeFactor),
				Keyframe.ofFloat(.5f, 3f * shakeFactor),
				Keyframe.ofFloat(.6f, -3f * shakeFactor),
				Keyframe.ofFloat(1f, 0)
		);
		return view==null?null:ObjectAnimator.ofPropertyValuesHolder(view, pvhScaleX, pvhScaleY, pvhRotate).
				setDuration(900);
	}
}
