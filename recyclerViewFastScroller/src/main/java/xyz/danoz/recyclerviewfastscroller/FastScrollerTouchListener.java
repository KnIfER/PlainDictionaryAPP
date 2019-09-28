package xyz.danoz.recyclerviewfastscroller;

import xyz.danoz.recyclerviewfastscroller.sectionindicator.SectionIndicator;

import androidx.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Toast;

/**
 * Touch listener that will move a {@link AbsRecyclerViewFastScroller}'s handle to a specified offset along the scroll bar
 */
class FastScrollerTouchListener implements OnTouchListener {

    private final AbsRecyclerViewFastScroller mFastScroller;

    /**
     * @param fastScroller {@link xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller} for this listener to scroll
     */
    public FastScrollerTouchListener(AbsRecyclerViewFastScroller fastScroller) {
        mFastScroller = fastScroller;
    }

    boolean draggingHandle;
    @Override
    public boolean onTouch(View v, MotionEvent event) {
		SectionIndicator sectionIndicator = mFastScroller.getSectionIndicator();
        showOrHideIndicator(sectionIndicator, event);
		//Toast.makeText(v.getContext(), ""+event.getY()+"::"+mFastScroller.mHandle.getY()+"::"+(mFastScroller.mHandle.getY()+mFastScroller.mHandle.getHeight())+"::"+, Toast.LENGTH_SHORT).show();
		//Toast.makeText(v.getContext(), ""+event.getY()+"::"+mFastScroller.getHeight()+"::"+mFastScroller.getWidth(), Toast.LENGTH_SHORT).show();

    	if(mFastScroller.getScrollMode()==0) {

	        float scrollProgress = mFastScroller.getScrollProgress(event);
	        mFastScroller.scrollTo(scrollProgress, true);
	        mFastScroller.moveHandleToPosition(scrollProgress);
	        
	        return true;
    	}
    	
    	switch(event.getAction()) {
    		case MotionEvent.ACTION_DOWN:
    			if(event.getY()>=mFastScroller.mHandle.getY() && event.getY()<=mFastScroller.mHandle.getY()+mFastScroller.mHandle.getHeight())
    				draggingHandle=true;
    			else
    				draggingHandle=false;
			break;
    		case MotionEvent.ACTION_MOVE:
    			if(draggingHandle) {

    		        float scrollProgress = mFastScroller.getScrollProgress(event);
    		        mFastScroller.scrollTo(scrollProgress, true);
    		        mFastScroller.moveHandleToPosition(scrollProgress);
    			}
			break;
    	}
    	
        return true;
    }

    private void showOrHideIndicator(@Nullable SectionIndicator sectionIndicator, MotionEvent event) {
        if (sectionIndicator == null) {
            return;
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                sectionIndicator.animateAlpha(1f);
                return;
            case MotionEvent.ACTION_UP:
                sectionIndicator.animateAlpha(0f);
        }
    }

}
