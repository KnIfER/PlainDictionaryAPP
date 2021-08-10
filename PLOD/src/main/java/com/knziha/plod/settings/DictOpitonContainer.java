package com.knziha.plod.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class DictOpitonContainer extends DialogFragment
{
	DictOptions dictOptions = new DictOptions();
	
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		FrameLayout v = new FrameLayout(inflater.getContext());
		v.setId(android.R.id.content);
		getChildFragmentManager().beginTransaction()
				.add(android.R.id.content, dictOptions)
				.commit();
		return v;
	}
}