package com.knziha.plod.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.webkit.WebView;

public class ArticleWebView extends WebView {
    public interface PlayFinish{
        void doit();
    }
    PlayFinish df;
    public void setDf(PlayFinish playFinish) {
        this.df = playFinish;
    }
    //onDraw表示显示完毕
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        df.doit();
    }

    
    public interface ArticleWebViewListener {
        void onScrollOverOneScreen();
        void onScrollLessThanOneScreen();
    }

    private ArticleWebViewListener mListener = null;
    private boolean overedPage = false;

    public ArticleWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ArticleWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ArticleWebView(Context context) {
        super(context);
    }

    public void setArticleWebViewListener(ArticleWebViewListener listener) {
        this.mListener = listener;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (this.getHeight() > 0 && t > this.getHeight()/3 && mListener != null) {
            if (!overedPage) {
                overedPage = true;
                mListener.onScrollOverOneScreen();
            }
        }
        if (this.getHeight() > 0 && t < this.getHeight()/3 && mListener != null) {
            if (overedPage) {
                overedPage = false;
                mListener.onScrollLessThanOneScreen();
            }
        }
    }
}
