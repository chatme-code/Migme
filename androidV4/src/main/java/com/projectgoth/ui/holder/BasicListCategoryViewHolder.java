/**
 * Copyright (c) 2013 Project Goth
 *
 * BasicListCategoryViewHolder.java.java
 * Created May 30, 2013, 5:58:39 PM
 */

package com.projectgoth.ui.holder;

import android.graphics.Bitmap;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;

/**
 * @author cherryv
 */
@Deprecated     // TODO: remove this class as it is currently no longer used.
public class BasicListCategoryViewHolder<T> extends BaseViewHolder<T> {

    private final RelativeLayout   categoryContainer;
    private final TextView         categoryName;
    private final ImageView        refreshIcon;
    private final ToggleButton     toggleButton;

    private GroupClickListener     mGroupClickListener     = null;
    private GroupLongClickListener mGroupLongClickListener = null;
    private int                    mGroupPosition          = -1;

    /**
     * Interface to be overridden by those fragments that need to handle clicks
     * on the list group header.
     */
    public interface GroupClickListener {

        public void onRefreshIconClicked(int groupPosition);
    }

    public interface GroupLongClickListener {

        public void onGroupLongClickListener(int groupPosition);

        // after setting a long click listener, the default behavior of clicking
        // the group view to collapse and expand it
        // doesn't work, so we also need to set the click listener to make it
        // work
        public void onGroupClickListener(int groupPosition);

        public void onGroupToggleButtonListener(int groupPosition);
    }

    /**
     * Construtor
     * 
     * @param baseView
     *            The root view that contains the UI elements for this holder.
     * @param shouldRegisterForClicks
     *            Whether this holder should register for clicks on UI elements.
     */
    public BasicListCategoryViewHolder(View view, boolean shouldRegisterForClicks) {
        super(view, shouldRegisterForClicks);
        categoryContainer = (RelativeLayout) view.findViewById(R.id.list_category);
        refreshIcon = (ImageView) view.findViewById(R.id.grouprefresh);
        categoryName = (TextView) view.findViewById(R.id.groupname);
        toggleButton = (ToggleButton) view.findViewById(R.id.togglebutton);
    }

    public BasicListCategoryViewHolder(View baseView) {
        this(baseView, true);
    }

    public void setGroupClickListener(final GroupClickListener groupClickListener, final int groupPosition) {
        mGroupClickListener = groupClickListener;
        mGroupPosition = groupPosition;

        if (mGroupClickListener != null && mGroupPosition >= 0) {
            refreshIcon.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    mGroupClickListener.onRefreshIconClicked(mGroupPosition);
                }
            });

        } else {
            refreshIcon.setOnClickListener(null);
        }
    }

    /**
     * @param groupPosition
     * @param onGroupClickListener
     * 
     */
    public void setGroupLongClickListener(int groupPosition, GroupLongClickListener groupLongClickListener) {
        mGroupPosition = groupPosition;
        mGroupLongClickListener = groupLongClickListener;

        if (mGroupLongClickListener != null) {
            categoryContainer.setOnLongClickListener(new OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    mGroupLongClickListener.onGroupLongClickListener(mGroupPosition);
                    return true;
                }
            });

            // after setting a long click listener, the default behavior of
            // clicking the group view to collapse and expand it
            // doesn't work, so we need to set it manually
            categoryContainer.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    mGroupLongClickListener.onGroupClickListener(mGroupPosition);
                }
            });

            toggleButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    mGroupLongClickListener.onGroupToggleButtonListener(mGroupPosition);
                }
            });
        }
    }

    public void setRefreshIconData(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            refreshIcon.setImageBitmap(bitmap);
        }
    }

    public void setRefreshIconData(int resourceId) {
        refreshIcon.setImageResource(resourceId);
    }

    public void setTitle(String title) {
        // Since cells are reused, if we change the title, then we immediately stop 
        // the animation. It can always be played later if required. What was
        // happening without this is that if a cell was reused for a different
        // title, and it was previous loading, and this one isn't, it would
        // complete one revolution of animation before stopping -- giving the
        // temporary appearance that the group is refreshing when it isn't.
        if(categoryName.getText().equals(title) == false) {
            categoryName.setText(title);
            refreshIcon.clearAnimation();
        }
    }

    public void shouldShowRefresh(boolean showRefresh) {
        if (showRefresh) {
            refreshIcon.setVisibility(View.VISIBLE);
        } else {
            refreshIcon.setVisibility(View.INVISIBLE);
        }
    }

    public void disableGroupItem() {
        categoryName.setTextColor(ApplicationEx.getColor(R.color.default_hint));
    }

    public void enableGroupItem() {
        categoryName.setTextColor(ApplicationEx.getColor(R.color.default_text));
    }

    public void displayToggleButton() {
        toggleButton.setVisibility(View.VISIBLE);
    }

    public void checkToggleButton(boolean state) {
        toggleButton.setChecked(state);
    }

    public void hideToggleButton() {
        toggleButton.setVisibility(View.GONE);
    }

    /**
     * When overriding this method, you will need to call the super
     * 
     * @param data
     * @param imageFetcher
     */
    @Override
    public void setData(T data) {
        super.setData(data);
    }

    /**
     * Animates the refresh icon if it is present in the group.
     * 
     * @param shouldAnimate
     *            Whether animation should be started or stopped.
     */
    public void animateRefreshIcon(final boolean shouldAnimate) {
        if (refreshIcon != null && refreshIcon.getVisibility() == View.VISIBLE) {
            Animation rotateAnimation = refreshIcon.getAnimation();
            if (rotateAnimation == null) {
                rotateAnimation = AnimationUtils.loadAnimation(ApplicationEx.getContext(),
                        R.anim.rotate_around_center_point);
            }

            if (shouldAnimate) {
                rotateAnimation.setRepeatCount(Animation.INFINITE);

                if (!rotateAnimation.hasStarted()) {
                    refreshIcon.startAnimation(rotateAnimation);
                }
            } else if (rotateAnimation.hasStarted()) {
                // Let the rotate animation play out for one more time.
                // This is so that it aligns back to its start position.
                rotateAnimation.setRepeatCount(1);
            }
        }
    }
}
