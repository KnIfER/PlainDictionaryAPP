package com.knziha.plod.PlainDict;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bm.library.PhotoView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.knziha.plod.PlainDict.R;
import com.knziha.plod.dictionary.mdictRes;
import com.knziha.plod.dictionarymodels.mdict;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashSet;


public class PhotoBrowserActivity extends Activity implements View.OnClickListener {

	public static mdictRes mdd;
	
	private ImageView crossIv;
    private ViewPager mPager;
    private ImageView centerIv;
    private TextView photoOrderTv;
    private TextView saveTv;
    private String curImageUrl = "";
    private String[] imageUrls = new String[]{};
    //private byte[] imageData = new byte[]{};
    private int curPosition = -1;
    private int[] initialedPositions = null;
    private ObjectAnimator objectAnimator;

    private HashSet<Integer> processedRec = new HashSet<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_photo_browser);
        imageUrls = getIntent().getStringArrayExtra("imageUrls");
        //imageData = getIntent().getByteArrayExtra("imageData");
        curImageUrl = getIntent().getStringExtra("curImageUrl");
        initialedPositions = new int[imageUrls.length];
        initInitialedPositions();

        photoOrderTv = (TextView) findViewById(R.id.photoOrderTv);
        saveTv = (TextView) findViewById(R.id.saveTv);
        saveTv.setOnClickListener(this);
        centerIv = (ImageView) findViewById(R.id.centerIv);
        crossIv = (ImageView) findViewById(R.id.crossIv);
        crossIv.setOnClickListener(this);

        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setPageMargin((int) (getResources().getDisplayMetrics().density * 15));
        mPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return imageUrls.length;
            }


            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, final int position) {
                if (imageUrls[position] != null && !"".equals(imageUrls[position])) {
                    final PhotoView view = new PhotoView(PhotoBrowserActivity.this);
                    view.enable();
                    view.setScaleType(ImageView.ScaleType.FIT_CENTER);//imageUrls[position]
                	

                    String SepWindows = "\\";

            		String FileTag = "file://";
            		
                    if(!processedRec.contains(position)) {
                    	int start = imageUrls[position].indexOf(FileTag);
	                	if(start==-1){
	                		if(imageUrls[position].startsWith("./"))
	                			imageUrls[position] = imageUrls[position].substring(1).replace("/", SepWindows);
	                		else
	                			imageUrls[position] = imageUrls[position].replace("/", SepWindows);
	                	}else{
	                		imageUrls[position] = imageUrls[position].substring(start+FileTag.length()).replace("/", SepWindows);
	                	}
	
	                    
	            		if(!imageUrls[position].startsWith(SepWindows)){
	            			imageUrls[position]=SepWindows+imageUrls[position];
	            		}
	            		try {
							imageUrls[position] =URLDecoder.decode(imageUrls[position],"UTF-8");
						} catch (UnsupportedEncodingException e1) {e1.printStackTrace();}
	            		processedRec.add(position);
                    }
                    
            		Log.e("mdd","fetching res:"+imageUrls[position]);
                    try {
						Glide.with(PhotoBrowserActivity.this).load(mdd.getRecordAt(mdd.lookUp(imageUrls[position]))).override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).fitCenter().listener(new RequestListener<Drawable>() {
							@Override
							public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
								if (position == curPosition) {
									hideLoadingAnimation();
								}
								showErrorLoading();
								return false;
							}

							@Override
							public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
								occupyOnePosition(position);
								if (position == curPosition) {
									hideLoadingAnimation();
								}
								return false;
							}
						}).into(view);
					} catch (Exception e) {
						e.printStackTrace();
					}

                    container.addView(view);
                    return view;
                }
                return null;
            }


            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                releaseOnePosition(position);
                container.removeView((View) object);
            }

        });

        curPosition = returnClickedPosition() == -1 ? 0 : returnClickedPosition();
        mPager.setCurrentItem(curPosition);
        mPager.setTag(curPosition);
        if (initialedPositions[curPosition] != curPosition) {//濡傛灉褰撳墠椤甸潰鏈姞杞藉畬姣曪紝鍒欐樉绀哄姞杞藉姩鐢伙紝鍙嶄箣鐩稿弽锛�
            showLoadingAnimation();
        }
        photoOrderTv.setText((curPosition + 1) + "/" + imageUrls.length);//璁剧疆椤甸潰鐨勭紪鍙�

        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (initialedPositions[position] != position) {//濡傛灉褰撳墠椤甸潰鏈姞杞藉畬姣曪紝鍒欐樉绀哄姞杞藉姩鐢伙紝鍙嶄箣鐩稿弽锛�
                    showLoadingAnimation();
                } else {
                    hideLoadingAnimation();
                }
                curPosition = position;
                photoOrderTv.setText((position + 1) + "/" + imageUrls.length);//璁剧疆椤甸潰鐨勭紪鍙�
                mPager.setTag(position);//涓哄綋鍓峷iew璁剧疆tag
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private int returnClickedPosition() {
        if (imageUrls == null || curImageUrl == null) {
            return -1;
        }
        int i=0,off;
        for (String strI:imageUrls) {
        	if(!strI.startsWith(mdict.FileTag))
        		off=mdict.FileTag.length();
        	else
        		off=0;
            if(curImageUrl.startsWith(imageUrls[i],off)) {
                return i;
            }
        	i++;
        }
        return -1;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.crossIv:
                finish();
                break;
            case R.id.saveTv:
                savePhotoToLocal();
                break;
            default:
                break;
        }
    }

    private void showLoadingAnimation() {
        centerIv.setVisibility(View.VISIBLE);
        centerIv.setImageResource(R.drawable.loading);
        if (objectAnimator == null) {
            objectAnimator = ObjectAnimator.ofFloat(centerIv, "rotation", 0f, 360f);
            objectAnimator.setDuration(2000);
            objectAnimator.setRepeatCount(ValueAnimator.INFINITE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                objectAnimator.setAutoCancel(true);
            }
        }
        objectAnimator.start();
    }

    private void hideLoadingAnimation() {
        releaseResource();
        centerIv.setVisibility(View.GONE);
    }

    private void showErrorLoading() {
        centerIv.setVisibility(View.VISIBLE);
        releaseResource();
        centerIv.setImageResource(R.drawable.load_error);
    }

    private void releaseResource() {
        if (objectAnimator != null) {
            objectAnimator.cancel();
        }
        if (centerIv.getAnimation() != null) {
            centerIv.getAnimation().cancel();
        }
    }

    private void occupyOnePosition(int position) {
        initialedPositions[position] = position;
    }

    private void releaseOnePosition(int position) {
        initialedPositions[position] = -1;
    }

    private void initInitialedPositions() {
        for (int i = 0; i < initialedPositions.length; i++) {
            initialedPositions[i] = -1;
        }
    }

    private void savePhotoToLocal() {
    }

    @Override
    protected void onDestroy() {
        releaseResource();
        if (mPager != null) {
            mPager.removeAllViews();
            mPager = null;
        }
        super.onDestroy();
    }
}
