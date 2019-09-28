/*
 *  Copyright © 2016-2017, Turing Technologies, an unincorporated organisation of Wynne Plaga
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.knziha.plod.widgets;

import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

/*
 * Lots of complicated maths taken mostly from Google. Abandon all hope, ye who enter here.
 */
class ScrollingUtilities {

    private SumsungLikeScrollBar materialScrollBar;

    ScrollingUtilities(SumsungLikeScrollBar msb){
        materialScrollBar = msb;
    }

    ICustomScroller customScroller;

    private ScrollPositionState scrollPosState = new ScrollPositionState();

    private int constant;

    private LinearLayoutManager layoutManager;

    private class ScrollPositionState {
        // The index of the first visible row
        private int rowIndex;
        // The offset of the first visible row
        private int rowTopOffset;
        // The height of a given row (they are currently all the same height)
        private int rowHeight;
    }

    void scrollHandleAndIndicator(){
        int scrollBarY;
        getCurScrollState();
        if(customScroller != null){
        } else {
            constant = scrollPosState.rowHeight * scrollPosState.rowIndex;
        }
        scrollBarY = (int) getScrollPosition();
        ViewCompat.setY(materialScrollBar.handleThumb, scrollBarY);
        materialScrollBar.handleThumb.invalidate();
    }

    private float getScrollPosition(){
        getCurScrollState();
        int scrollY = materialScrollBar.getPaddingTop() + constant - scrollPosState.rowTopOffset;
        return ((float) scrollY / getAvailableScrollHeight()) * getAvailableScrollBarHeight();
    }

    private int getRowCount(){

        return 0;
    }

    /**
     * Returns the available scroll bar height:
     * AvailableScrollBarHeight = Total height of the visible view - thumb height
     */
    int getAvailableScrollBarHeight() {
        return materialScrollBar.getHeight() - materialScrollBar.handleThumb.getHeight();
    }

    void scrollToPositionAtProgress(float touchFraction) {}

    int getAvailableScrollHeight() {
        int visibleHeight = materialScrollBar.getHeight();
        int scrollHeight;
        if(customScroller != null){
            scrollHeight = materialScrollBar.getPaddingTop() + customScroller.getTotalDepth() + materialScrollBar.getPaddingBottom();
        } else {
            scrollHeight = materialScrollBar.getPaddingTop() + getRowCount() * scrollPosState.rowHeight + materialScrollBar.getPaddingBottom();
        }
        return scrollHeight - visibleHeight;
    }

    void getCurScrollState() {
        scrollPosState.rowIndex = -1;
        scrollPosState.rowTopOffset = -1;
        scrollPosState.rowHeight = -1;



    }

}
