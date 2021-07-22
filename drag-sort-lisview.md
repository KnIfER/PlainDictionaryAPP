A subclass of the Android ListView component that enables drag
and drop re-ordering of list items.

Copyright 2012 Carl Bauer

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.


--------------

### FloatViewManager

所谓悬浮视图（FloatView），是拖动排序时显示的视图，是ImageView，显示的是被拖动的原视图的drawing cache/bitmap之拷贝。


### DragSortController

控制类，实现 View.OnTouchListener 以及 GestureDetector.OnGestureListener。

原项目需给整个列表施用此控制器作为 View.OnTouchListener，导致触摸事件冗余、焦点失常，比如给拖动柄视图添加点击事件后（或者后方存在可点击的视图），就无法拖动排序了。

改进后，可直接设置给拖动柄视图，如此一来控制器也无需通过findViewById遍历视图树（来定位拖动柄视图、判断触摸点是否落在拖动柄上），无需做额外判断，简洁高效。

	private DragSortController buildController(DragSortListView dslv) {
		DragSortController controller = new DragSortController(dslv);
		controller.setDragHandleId(R.id.drag_handle);
		//controller.setClickRemoveId(R.id.click_remove);
		controller.setRemoveEnabled(false);
		controller.setRemoveMode(DragSortController.FLING_REMOVE);
		controller.setSortEnabled(true);
		controller.setDragInitMode(DragSortController.ON_DOWN);
		controller.setBackgroundColor(0xEE008EFF);
		return controller;
	}

-----------


### 相比于 RecyclerView 的优势

- 基于ListView，代码经典，更加可控。
- 动画流畅，排序时没有 RecyclerView 的滞涩感。
- 排序时的自动滚动速度更合理，更可控。不需要自定义复杂的计算方式，默认速度不会越来越快，不变得非常快、过分快。


-----------

### 扩展给 DropDownListView

之所以放在此模块而不分开，是因为想给列表弹出窗运用此排序规则。详见《多聚浏览器》开源项目中的搜索引擎列表之实现。

2021.7.22