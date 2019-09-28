/*
 * Copyright (C) 2017 Jared Rummler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jaredrummler.colorpicker;
//for presets
import android.content.Context;
//1import androidx.annotation.RestrictTo;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.GridView;

//1@RestrictTo(RestrictTo.Scope.LIBRARY)
public class NestedGridView extends GridView {

  public NestedGridView(Context context) {
    super(context);
  }

  public NestedGridView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public NestedGridView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
    if(expanded)
    	super.onMeasure(widthMeasureSpec, expandSpec);
    else
    	super.onMeasure(widthMeasureSpec, heightMeasureSpec);
  }

  	boolean expanded=false;
	public void setExpanded(boolean b) {
		expanded=b;
		requestLayout();
	}

	public boolean isExpanded() {
		return expanded;
	}

}
