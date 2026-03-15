package com.projectgoth.ui.widget.allaccessbutton;

public class ContextAction {

	public int actionId;
	public int imageResourceId;
	public ContextActionListener listener;
	
	public ContextAction() {}
	
	public ContextAction(int actionId, int imageResourceId, ContextActionListener listener) {
		this.actionId = actionId;
		this.imageResourceId = imageResourceId;
		this.listener = listener;
	}
	
	public void execute() {
        if (listener != null) {
            listener.executeAction(actionId);
        }
	}

}
