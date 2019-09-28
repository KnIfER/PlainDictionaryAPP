package com.knziha.plod.widgets;
import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;


public class RecyclerViewmy extends RecyclerView{
	public RecyclerViewmy(Context context) {
		super(context);
	}
	public RecyclerViewmy(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public RecyclerViewmy(Context arg0, AttributeSet arg1, int arg2) {
		super(arg0, arg1, arg2);
	}
	public interface OnItemClickListener{
        void onItemClick(View view,int position);
    }





	
}