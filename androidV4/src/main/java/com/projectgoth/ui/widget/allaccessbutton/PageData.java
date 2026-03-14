package com.projectgoth.ui.widget.allaccessbutton;

import java.util.ArrayList;
import java.util.List;

public class PageData {

	private int accessButtonIconRes = 0;
	
	private final List<ContextAction> actions = new ArrayList<ContextAction>();
	
	public PageData(final int imageResourceId) {
		this.accessButtonIconRes = imageResourceId;
	}
	
	public PageData addAction(final ContextAction action) {
		actions.add(action);
		return this;
	}
	
	public int getAccessButtonIcon() {
		return accessButtonIconRes;
	}
	
	public List<ContextAction> getActions() {
		return actions;
	}
}
