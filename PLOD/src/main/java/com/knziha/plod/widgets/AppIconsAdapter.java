package com.knziha.plod.widgets;


import static com.knziha.plod.widgets.ViewUtils.GrayBG;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.GlobalOptions;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.knziha.paging.AppIconCover.AppIconCover;
import com.knziha.paging.AppIconCover.AppInfoBean;
import com.knziha.plod.plaindict.CMN;
import com.knziha.plod.plaindict.MainActivityUIBase;
import com.knziha.plod.plaindict.PDICMainActivity;
import com.knziha.plod.plaindict.R;
import com.knziha.plod.plaindict.Toastable_Activity;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("rawtypes")
public class AppIconsAdapter extends RecyclerView.Adapter<AppIconsAdapter.ViewHolder> {
	public final BottomSheetDialog shareDialog;
	private final View bottomSheet;
    private final FlowTextView indicator;
    private TextPaint textPainter;
    private ArrayList<AppInfoBean> list = new ArrayList<>();
    private View.OnClickListener itemClicker;
    private PackageManager pm;
	private int landScapeMode;
	/** true:sharing url */
	private boolean shareLink;
	private boolean withIntent;
	private int headBtnSz = 1;
	//private String text;
	
	public AppIconsAdapter(Toastable_Activity a) {
		textPainter = new TextPaint();
		textPainter.setColor(a.AppBlack);
		textPainter.setTextSize(GlobalOptions.density*(GlobalOptions.isLarge?19:12));
		textPainter.setAntiAlias(true);
		
        shareDialog = new BottomSheetDialog(a);
		shareDialog.getWindow().setDimAmount(0.2f);
		Window win = shareDialog.getWindow();
		if(win!=null) {
			win.setDimAmount(0.2f);
			View decor = win.getDecorView();
			decor.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom)
					-> v.postDelayed(() -> {
				if(landScapeMode!=a.mConfiguration.orientation) {
					show(a);
				}
			}, 0));
		}
		shareDialog.tag = this;
        itemClicker = v1 -> {
			try {
				ViewHolder vh = (ViewHolder) v1.getTag();
				int pos = vh.getLayoutPosition()-headBtnSz;
				if (pos < 0) { // 选择分享方式
					shareLink = !shareLink;
					((MainActivityUIBase) a).shareUrlOrText(shareLink);
				}
				else { // 分享…
					AppInfoBean appBean = list.get(pos);
					Intent shareIntent = new Intent(appBean.intent);
					shareIntent.setComponent(new ComponentName(appBean.pkgName, appBean.appLauncherClassName));
					shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					if (shareLink && shareIntent.getData()!=null && shareIntent.getDataString().contains("localhost")) {
						// 开启服务器
						MainActivityUIBase act = (MainActivityUIBase) a;
						if (act.thisActType==MainActivityUIBase.ActType.PlainDict) {
							((PDICMainActivity)act).startServer(true);
						}
					}
					try {
						a.startActivity(shareIntent);
						shareDialog.dismiss();
					} catch (Exception e) {
						a.showT(e);
					}
				}
			} catch (Exception e) {
				CMN.debug(e);
			}
		};
        bottomSheet = View.inflate(a, R.layout.share_bottom_dialog, null);
        indicator = bottomSheet.findViewById(R.id.indicator);
        RecyclerView recyclerView = bottomSheet.findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new GridLayoutManager(a, 4));
        recyclerView.setRecycledViewPool(ViewUtils.MaxRecyclerPool(35));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(this);

        shareDialog.setContentView(bottomSheet);
    }

    public void show(Toastable_Activity a) {
        shareDialog.show();
		ViewUtils.ensureWindowType(shareDialog, (MainActivityUIBase) a, null);
		ViewUtils.TrimWindowWidth(shareDialog.getWindow(), a.dm);
		landScapeMode=a.mConfiguration.orientation;
        boolean landScape = landScapeMode==Configuration.ORIENTATION_LANDSCAPE;
        BottomSheetBehavior beh = shareDialog.getBehavior();
        beh.setState(landScape?BottomSheetBehavior.STATE_EXPANDED:BottomSheetBehavior.STATE_COLLAPSED);
        beh.setSkipCollapsed(landScape);
        int target = GlobalOptions.isDark?Color.WHITE:Color.BLACK;
        textPainter.setColor(target);
        indicator.setTextColor(target);
        indicator.setText(shareTargetsInfo1[withIntent?2:shareLink?1:0]);
        bottomSheet.setBackground(GlobalOptions.isDark?GrayBG:null);
    }
	
	public static class PrefetchedApps{
		public final Intent intent;
		public final List<ResolveInfo> activities;
		public PrefetchedApps(Intent intent, List<ResolveInfo> activities) {
			this.intent = intent;
			this.activities = activities;
		}
	}
    /** 获取应用列表 */
    public void pullAvailableApps(Toastable_Activity a, Intent intent, String url, String text, List<PrefetchedApps> preFetched) {
		list.clear();
		withIntent = intent!=null;
		if(withIntent) {
			shareLink=false;
			headBtnSz = 0;
			shareTargetsInfo1[2] = text;
		} else {
			shareLink=text==null;
			if(shareLink) {
				intent=new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				//this.text = url;
			} else {
//				if(shareWhat==1) {
//					intent=new Intent(Intent.ACTION_WEB_SEARCH);
//					intent.putExtra(SearchManager.QUERY, text);
//				} else {
//					intent=new Intent(Intent.ACTION_SEND);
//					intent.putExtra(Intent.EXTRA_TEXT, text);
//					intent.setType("text/plain");
//				}
				//this.text = text;
			}
			headBtnSz = 1;
		}
        pm = a.getPackageManager();
        ResolveResolvedQuery(intent, preFetched);
        if(shareLink) {
			intent=new Intent(intent);
			intent.setAction(Intent.ACTION_SEND);
			intent.putExtra(Intent.EXTRA_TEXT, url);
			intent.putExtra(Intent.EXTRA_HTML_TEXT, "FUCK");
			intent.setType("text/plain");
			ResolveResolvedQuery(intent, preFetched);
		}
        notifyDataSetChanged();
        show(a);
    }
	
	private void ResolveResolvedQuery(Intent intent, List<PrefetchedApps> preFetched) {
		if (preFetched != null) {
			for (PrefetchedApps pref : preFetched) {
				for (ResolveInfo RinfoI : pref.activities) {
					if (RinfoI.activityInfo.exported) {
						AppInfoBean appBean = new AppInfoBean();
						appBean.intent = pref.intent;
						appBean.data = RinfoI;
						appBean.pm = pm;
						appBean.pkgName = RinfoI.activityInfo.packageName;
						appBean.appLauncherClassName = RinfoI.activityInfo.name;
						list.add(appBean);
					}
				}
			}
		} else {
			List<ResolveInfo> resolved = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL);
			for (ResolveInfo RinfoI : resolved) {
				if (RinfoI.activityInfo.exported) {
					AppInfoBean appBean = new AppInfoBean();
					appBean.intent = intent;
					appBean.data = RinfoI;
					appBean.pm = pm;
					appBean.pkgName = RinfoI.activityInfo.packageName;
					appBean.appLauncherClassName = RinfoI.activityInfo.name;
					list.add(appBean);
				}
			}
		}
	}
	
	@Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //CMN.Log("AppIconsAdapter::onCreateViewHolder");
        ViewHolder ret = new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(viewType==0?R.layout.share_recycler_item:R.layout.share_pick_item, parent, false));
        ret.itemView.setOnClickListener(itemClicker);
        ret.textImageView.textPainter=textPainter;
        ret.textImageView.bNeedShadow=true;
		if(viewType==1) {
			ret.textImageView.setTag(ret.itemView.findViewById(R.id.tv));
		}
        return ret;
    }
	
	@Override
	public int getItemViewType(int position) {
		return position<headBtnSz?1:0;
	}
	
	String[] shareTargets = new String[]{
			"文本"
			,"网页+"
	};
	
	String[] shareTargetsInfo1 = new String[]{
			"分享单词："
			,"分享网址："
			,null
	};
	
	@Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") int position) {
		DescriptiveImageView iv = holder.textImageView;
		if (position < headBtnSz) { // 选择分享内容
			((TextView)iv.getTag()).setText(shareTargets[shareLink?1:0]);
		}
		else {
			position-=headBtnSz;
			holder.position = position;
			AppInfoBean app = list.get(position);
	
			RequestOptions options = new RequestOptions()
					.format(DecodeFormat.PREFER_ARGB_8888)//DecodeFormat.PREFER_ARGB_8888
					.skipMemoryCache(false)
					.diskCacheStrategy(DiskCacheStrategy.NONE)
					//.onlyRetrieveFromCache(true)
					.fitCenter()
					.override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
					;
			
			Glide.with(iv.getContext())
					.load(new AppIconCover(app, true))
					.apply(options)
					.listener(new RequestListener<Drawable>() {
						@Override
						public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
							return false;
						}
						@Override
						public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
							DescriptiveImageView medium_thumbnail = (DescriptiveImageView) ((ImageViewTarget<?>) target).getView();
							//todo check glide
							medium_thumbnail.setText(((AppInfoBean)((AppIconCover)model).getBeanInMemory()).appName);
							return false;
						}
					})
					.into(iv);
			
			iv.setText(app.appName);
		}
    }

    @Override
    public int getItemCount() {
        return list.size()+headBtnSz;
    }
	
	public static class ViewHolder extends RecyclerView.ViewHolder {
        public int position;
        public DescriptiveImageView textImageView;
        public ViewHolder(View itemView) {
            super(itemView);
            textImageView = itemView.findViewById(R.id.app_icon_iv);
            itemView.setTag(this);
        }
    }
}