package com.knziha.plod.ArrayList;

import com.knziha.plod.widgets.FlowTextView;

public class ArrayListHolder extends ArrayListGood<FlowTextView.LineObject> {
	static ArrayListGood.ArrayCreator<FlowTextView.LineObject> LineArrayCreator_Instance = initialCapacity -> new FlowTextView.LineObject[initialCapacity];
	
	public ArrayListHolder(int initialCapacity) {
		super(initialCapacity, LineArrayCreator_Instance);
	}
	
	@Override
	public void clear() {
		modCount++;
		size = 0;
	}
	
	public void add(int charOffsetStart, int charOffsetEnd, float xOffset, float yOffset) {
		int size1=size;
		FlowTextView.LineObject[] elementData = this.elementData;
		if(size1<elementData.length) {
			FlowTextView.LineObject lineObject = elementData[size1];
			if(lineObject!=null) {
				lineObject.set(charOffsetStart, charOffsetEnd, xOffset, yOffset);
				size=size1+1;
				return;
			}
		}
		super.add(new FlowTextView.LineObject(charOffsetStart, charOffsetEnd, xOffset, yOffset));
	}
}
