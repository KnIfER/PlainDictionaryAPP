package com.knziha.plod.plaindict;

import com.knziha.plod.dictionarymodels.BookPresenter;

public interface MdictServerLet {
	String md_getName(int i);
	BookPresenter md_get(int i);
	BookPresenter md_getById(long id);
	BookPresenter md_getByName(String name);
	int md_getSize();
	
	int getMainBackground();
}
