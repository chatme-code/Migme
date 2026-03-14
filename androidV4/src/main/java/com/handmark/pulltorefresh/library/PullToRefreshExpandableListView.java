package com.handmark.pulltorefresh.library;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import com.handmark.pulltorefresh.library.internal.EmptyViewMethodAccessor;
import com.projectgoth.ui.widget.PinnedHeaderExpandableListView;

public class PullToRefreshExpandableListView extends PullToRefreshAdapterViewBase<PinnedHeaderExpandableListView> {

	class InternalExpandableListView extends PinnedHeaderExpandableListView implements EmptyViewMethodAccessor {

		public InternalExpandableListView(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		@Override
		public void setEmptyView(View emptyView) {
			PullToRefreshExpandableListView.this.setEmptyView(emptyView);
		}

		@Override
		public void setEmptyViewInternal(View emptyView) {
			super.setEmptyView(emptyView);
		}

		public ContextMenuInfo getContextMenuInfo() {
			return super.getContextMenuInfo();
		}
	}

	public PullToRefreshExpandableListView(Context context) {
		super(context);
        setDisableScrollingWhileRefreshing(false);
	}

	public PullToRefreshExpandableListView(Context context, int mode) {
		super(context, mode);
        setDisableScrollingWhileRefreshing(false);
	}

	public PullToRefreshExpandableListView(Context context, AttributeSet attrs) {
		super(context, attrs);
        setDisableScrollingWhileRefreshing(false);
	}

	@Override
	protected final PinnedHeaderExpandableListView createRefreshableView(Context context, AttributeSet attrs) {
        PinnedHeaderExpandableListView lv = new InternalExpandableListView(context, attrs);

		// Set it to this so it can be used in ListActivity/ListFragment
		lv.setId(android.R.id.list);
		return lv;
	}

	@Override
	public ContextMenuInfo getContextMenuInfo() {
		return ((InternalExpandableListView) getRefreshableView()).getContextMenuInfo();
	}
}
