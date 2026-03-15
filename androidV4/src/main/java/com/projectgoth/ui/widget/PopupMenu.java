/**
 * Copyright (c) 2013 Project Goth
 *
 * PopupMenu.java
 * Created Jul 8, 2013, 1:24:45 AM
 */

package com.projectgoth.ui.widget;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.common.Config;
import com.projectgoth.common.Tools;
import com.projectgoth.model.MenuOption;
import com.projectgoth.model.MenuOption.MenuAction;
import com.projectgoth.model.MenuOption.MenuOptionType;
import com.projectgoth.model.MenuOption.MenuViewType;

import java.util.List;

/**
 * @author cherryv
 * 
 */
public class PopupMenu extends ExtendedPopupWindow implements OnClickListener {

    private LayoutInflater      inflater;
    private LinearLayout        mMenuList;
    private List<MenuOption>    mMenuItems;
    private OnPopupMenuListener mPopupMenuListener;
    private ImageView           mPopupMenuMarker;
    private Context             mContext;

    private int                 popupWidth = (int) (Config.getInstance().getScreenWidth() * 0.6f);

    private static final int    MAX_COLS   = 3;

    public static interface OnPopupMenuListener {

        public void onMenuOptionClicked(MenuOption menuOption);
        
        public void onPopupMenuDismissed();
        
    }

    public PopupMenu(Context context) {
        this(context, null, null);
    }

    public PopupMenu(Context context, OnPopupMenuListener listener) {
        this(context, null, listener);
    }

    public PopupMenu(Context context, View anchor, OnPopupMenuListener listener) {
        super(context, anchor);
        this.mContext = context;
        this.mPopupMenuListener = listener;

        inflater = LayoutInflater.from(context);
        View root = inflater.inflate(R.layout.action_bar_popup_menu, null);

        mMenuList = (LinearLayout) root.findViewById(R.id.menu_contents);

        mWindow.setContentView(root);

        setCanceledOnKeyPress(new int[] { KeyEvent.KEYCODE_MENU });
        setCanceledOnTouchOutside();
    }

    @Override
    protected void onPopupCreated() {
        // DO NOTHING
    }

    @Override
    protected void onPopupDismissed() {
        mPopupMenuListener.onPopupMenuDismissed();
    }

    public void setMenuOptions(List<MenuOption> options) {
        this.mMenuItems = options;
    }

    public void setPopupMenuListener(OnPopupMenuListener listener) {
        this.mPopupMenuListener = listener;
    }
    
    public void setMarker(ImageView marker) {
        mPopupMenuMarker = marker;
    }
    
    public void setMarkerOn(final boolean state) {
        if (mPopupMenuMarker != null) {
            if (hasGravity(Gravity.TOP)) {
                mPopupMenuMarker.setImageResource((state) ? R.drawable.ad_solidarrow_green : R.drawable.ad_solidarrow_black);
            } else if (hasGravity(Gravity.BOTTOM)) {
                mPopupMenuMarker.setImageResource((state) ? R.drawable.ad_solidarrow_green_invert : R.drawable.ad_solidarrow_black_invert);
            }
        }
    }

    @Override
    protected void showPopup(final boolean shouldAnimate) {
        preparePopup();
        createMenuView();
        if (mAnchor != null) {
            determineAnimationStyle(shouldAnimate);

            mWindow.showAtLocation(mAnchor, mGravity, xOffset, yOffset);
        }
    }
    
    /**
     * Determines what animation style is to be set on the popup menu. 
     * @param shouldAnimate true if the popup menu is to be animated and false otherwise.
     */
    private void determineAnimationStyle(boolean shouldAnimate) {
        if (hasGravity(Gravity.BOTTOM)) {
            // Do an up animation if the popup menu is anchored to the bottom.
            if (shouldAnimate) {
                mWindow.setAnimationStyle(R.style.PopupMenuAnimationUp);
            } else {
                mWindow.setAnimationStyle(R.style.PopupMenuAnimationUpImmediate);
            }
        } else {
            // Do a down animation if the popup menu is *NOT* anchored to the bottom.
            if (shouldAnimate) {
                mWindow.setAnimationStyle(R.style.PopupMenuAnimationDown);
            } else {
                mWindow.setAnimationStyle(R.style.PopupMenuAnimationDownImmediate);
            }
        }
    }
    
    private boolean hasGravity(final int gravity) {
        return ((mGravity & gravity) == gravity);
    }

    private void preparePopup() {
        getAnchorRect();
    }

    private void createMenuView() {
        View view;

        if (this.mMenuItems != null) {
            int colCount = 0;
            LinearLayout rowContainer = null;
            mMenuList.removeAllViews();
            int rowHeight = ApplicationEx.getDimension(R.dimen.menu_list_item_height);

            for (MenuOption option : mMenuItems) {
                if (option.getViewType() == MenuViewType.GRID) {
                    rowHeight = ApplicationEx.getDimension(R.dimen.menu_grid_item_height);
                }

                if (option.getViewType() == MenuViewType.SINGLE) {
                    rowHeight = ApplicationEx.getDimension(R.dimen.menu_single_height);
                    popupWidth = (int) (Config.getInstance().getScreenWidth() * 0.85f);
                }

                if (colCount == 0 || rowContainer == null) {
                    rowContainer = new LinearLayout(mContext);
                    rowContainer.setOrientation(LinearLayout.VERTICAL);
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(popupWidth, rowHeight);
                    rowContainer.setLayoutParams(params);
                    rowContainer.setPadding(0, 0, 0, 0);
                }

                if (option.getViewType() == MenuViewType.GRID) {
                    view = getOptionAsGridView(option);
                } else if(option.getViewType() == MenuViewType.SINGLE) {
                    view = getOptionAsSingleView(option);
                } else {
                     view = getOptionAsListView(option);
                }

                if (view == null) {
                    continue;
                }
                
                // Set the row container color to green if it is the last one in menu items and it is checked.
                if (option.getMenuOptionType() == MenuOptionType.CHECKABLE) {
                    if (option.isChecked()) {
                        rowContainer.setBackgroundColor(ApplicationEx.getColor(R.color.default_green));
                    } else {
                        rowContainer.setBackgroundColor(0);
                    }
                }

                view.setFocusable(true);
                view.setClickable(true);
                rowContainer.addView(view);
                rowContainer.setGravity(Gravity.CENTER_VERTICAL);
                colCount++;

                if (colCount == MAX_COLS || option.getViewType() == MenuViewType.LIST) {
                    mMenuList.addView(rowContainer);
                    // if it is not the last one, add the separator
                    if (mMenuItems.indexOf(option) != mMenuItems.size() - 1) {
                        mMenuList.addView(getMenuOptionSeparator());
                    }

                    rowContainer = null;
                    colCount = 0;
                }
            }

            // add last row
            if (colCount > 0 && rowContainer != null) {
                mMenuList.addView(rowContainer);
                colCount = 0;
            }
        }
    }

    private GridMenuOptionView getOptionAsGridView(MenuOption option) {
        GridMenuOptionView optionView = new GridMenuOptionView(mContext);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                ApplicationEx.getDimension(R.dimen.menu_grid_item_height), 1);
        optionView.setLayoutParams(params);

        if (option.getIcon() != null) {
            optionView.setIcon(option.getIcon());
        }

        if (!TextUtils.isEmpty(option.getTitle())) {
            optionView.setLabel(option.getTitle());
        }

        optionView.setTag(option);
        optionView.setOnClickListener(this);

        return optionView;
    }

    private MenuOptionView getOptionAsSingleView(MenuOption option) {
        MenuOptionView optionView = new SingleMenuOptionView(mContext);

        if (option.getMenuOptionType() == MenuOptionType.ACTIONABLE) {
            optionView.showActionButton(option.getParam());
        }

        if (!TextUtils.isEmpty(option.getTitle())) {
            optionView.setLabel(option.getTitle());
        }

        if (!TextUtils.isEmpty(option.getSubTitle())) {
            optionView.setSubLabel(option.getSubTitle());
        }

        View actionBtn = optionView.findViewById(R.id.action_button);
        actionBtn.setTag(option);
        actionBtn.setOnClickListener(this);

        return optionView;
    }

    private ListMenuOptionView getOptionAsListView(MenuOption option) {
        ListMenuOptionView optionView = new ListMenuOptionView(mContext);

        if (option.getIcon() != null) {
            optionView.setIcon(option.getIcon());
        }

        if (option.getMenuOptionType() == MenuOptionType.CHECKABLE) {
            if (option.isChecked()) {
                optionView.showCheckedIcon();
            } else {
                optionView.hideCheckedIcon();
            }
        } else if (option.getMenuOptionType() == MenuOptionType.SELECTABLE) {
            if (option.isChecked()) {
                optionView.showSelectedIcon();
            } else {
                optionView.showUnselectedIcon();
            }
        }

        if (!TextUtils.isEmpty(option.getTitle())) {
            optionView.setLabel(option.getTitle());
        }

        optionView.setTag(option);
        optionView.setOnClickListener(this);

        return optionView;
    }

    @SuppressWarnings("deprecation")
    private View getMenuOptionSeparator() {
        ImageView separator = new ImageView(mContext);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, Tools.getPixels(2));
        separator.setLayoutParams(params);
        separator.setScaleType(ScaleType.FIT_XY);
        separator.setImageResource(R.drawable.main_menu_option_separator);
        return separator;
    }

    @Override
    public void onClick(View v) {
        MenuOption option = (MenuOption) v.getTag();
        MenuOption menuOpt = null;
        if (mMenuItems.contains(option)) {
            menuOpt = mMenuItems.get(mMenuItems.indexOf(option));
        }

        if (menuOpt != null) {
            switch (option.getMenuOptionType()) {
                case CHECKABLE:
                    menuOpt.toggle();
                    break;
                case SELECTABLE:
                    // if item is selected already, do nothing
                    // else, if another item is selected, remove selection
                    // and set this option to selected
                    if (!menuOpt.isChecked()) {
                        checkAndRemoveSelection(menuOpt);
                        menuOpt.setChecked(true);
                    }
                    break;
                case LABEL:
                case ACTIONABLE:
                    // DO NOTHING
                    break;
            }
            createMenuView();
            option = menuOpt;
        }
        handleAction(option);
        if (option.shouldDismissPopupOnClick()) {
            dismiss();
        }
    }
    
    private void handleAction(MenuOption option) {
        MenuAction action = option.getAction();
        if (action != null) {
            action.onAction(option, option.isChecked());
        } else if (mPopupMenuListener != null) {
            mPopupMenuListener.onMenuOptionClicked(option);
        }
    }
    
    private void checkAndRemoveSelection(MenuOption menuOpt) {
        if (mMenuItems != null) {
            for (MenuOption option : mMenuItems) {
                if (option.getMenuOptionType() == MenuOptionType.SELECTABLE && !option.equals(menuOpt)
                        && option.isChecked()) {
                    MenuOption tempOpt = mMenuItems.get(mMenuItems.indexOf(option));
                    tempOpt.setChecked(false);
                }
            }
        }
    }

}
