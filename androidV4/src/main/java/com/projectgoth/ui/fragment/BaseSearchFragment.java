/**
 * Copyright (c) 2013 Project Goth
 *
 * BaseSearchFragment.java
 * Created Oct 9, 2014, 10:25:19 AM
 */

package com.projectgoth.ui.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import com.projectgoth.R;
import com.projectgoth.common.TextUtils;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Tools;
import com.projectgoth.i18n.I18n;
import com.projectgoth.ui.activity.CustomActionBarConfig;
import com.projectgoth.ui.activity.CustomActionBarConfig.NavigationButtonState;

/**
 * - Provides functionality for displaying a search filter in the action bar.
 * 
 * @author angelorohit
 */
public abstract class BaseSearchFragment extends BaseFragment 
implements TextWatcher, OnEditorActionListener, OnKeyListener {

    /**
     * The types of action bar mode for the fragment.
     * 
     * In FILTERABLE mode, a title, icon and search icon is displayed in the action bar.
     * Clicking on the search icon, puts the fragment in FILTERING mode.
     * 
     * In FILTERING mode, the action bar is in a filterable state.
     * In FILTERING mode, further clicking on the search icon should launch the GlobalSearchFragment with the given type.
     * 
     * In NO_FILTER mode, the title and icon are displayed in the action bar but no search icon is visible. 
     * This gives the user no opportunity to perform a filter and subsequent global search.
     * @author angelorohit
     *
     */
    public enum Mode {
        FILTERABLE(0),
        FILTERING(1),
        NO_FILTER(2);
        
        private int value;

        private Mode(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }

        public static Mode fromValue(int value) {
            for (Mode type : values()) {
                if (type.value == value)
                    return type;
            }
            
            return NO_FILTER;
        }
    }

    /**
     * The current action bar mode for the fragment.
     * The default state is set to NO_FILTER.
     */
    protected Mode mode = Mode.NO_FILTER;
    
    protected String mFilterText = null;
    
    /**
     * Whether the search box should be auto-focused when going into FILTERING mode.
     */
    protected boolean shouldAutoFocusSearchBox = true; 
    
    /**
     * Determines whether the pressing back icon or back key will exit filter
     * mode instead of doing the default back action.
     */
    protected boolean canExitFilteringMode = true;
    
    /**
     * The search box UI element.
     */
    private EditText mSearchBox;
    
    /**
     * Bundle argument key for the initial {@link Mode} in which this fragment is to be displayed.
     * The default value set for this argument is {@link Mode#NO_FILTER}
     */
    public static final String PARAM_INITIAL_MODE = "PARAM_INITIAL_MODE";
    
    /**
     * Bundle argument key that determines whether the search box should be auto-focused when the fragment is displayed.
     * The default value set for this argument is true.
     */
    public static final String PARAM_SHOULD_AUTOFOCUS_SEARCH_BOX = "PARAM_SHOULD_AUTOFOCUS_SEARCH_BOX";
    
    /**
     * Bundle argument key that sets the initial filter text for the search box. 
     * This will only be set if the mode is {@link Mode#FILTERING}.
     */
    public static final String PARAM_INITIAL_FILTER_TEXT = "PARAM_INITIAL_FILTER_TEXT"; 
    

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        
        // Need to clear any references to elements in the view
        mSearchBox = null;
    }

    /**
     * Bundle argument key that makes it such that pressing back icon or back
     * key will exit filter mode instead of doing the default back action.
     */
    public static final String PARAM_CAN_EXIT_FILTERING_MODE = "PARAM_CAN_EXIT_FILTERING_MODE";
    
    /**
     * Checks whether this fragment can be filtered.
     * 
     * @return true if this fragment can be filtered and false otherwise.
     */
    protected boolean isFiltering() {
        return (mode == Mode.FILTERING);
    }
    
    protected void setMode(final Mode mode) {
        if (this.mode != mode) {
            this.mode = mode;
            onModeChanged(mode);
        } else if (mode == Mode.FILTERING) {
            // Force the search box to be focused and the soft keyboard to be visible
            focusSearchBox(true);
        }
    }

    protected Mode getMode() {
        return this.mode;
    }

    protected void onModeChanged(Mode newMode) {
        updateActionBar(getActivity());
    }

    /**
     * Sets whether the search box should be automatically focused when the
     * fragment switches to {@link Mode#FILTER}.
     * 
     * @param state
     *            true to auto-focus and false otherwise.
     * @return true if the search box was auto-focused false otherwise.
     */
    protected boolean setShouldAutoFocusSearchBox(final boolean state) {
        return setShouldAutoFocusSearchBox(state, false);
    }
    
    /**
     * Sets whether the search box should be automatically focused when the
     * fragment switches to {@link Mode#FILTER}.
     * 
     * @param state
     *            true to auto-focus and false to de-focus.
     * @param forceSet
     *            true if the state should be forcefully set. The search box is
     *            also immediately focused or de-focused (if the mode is
     *            filtering). A false value does not set the given state if it
     *            has already been set before.
     * @return true if the search box was auto-focused false otherwise.
     */
    private boolean setShouldAutoFocusSearchBox(final boolean state, final boolean forceSet) {
        if (this.shouldAutoFocusSearchBox != state || forceSet) {
            this.shouldAutoFocusSearchBox = state;
            focusSearchBox(state);
            
            return true;
        }
        
        return false;
    }
    
    private void setCanExitFilteringMode(final boolean state) {
        canExitFilteringMode = state;
    }
    
    /**
     * Sets the filter text in the search box.
     * 
     * @param filterText
     *            The filter text to be set.
     * @return true if the filter text was successfully changed and false
     *         otherwise.
     */
    protected boolean setFilterText(final String filterText) {
        return setFilterText(filterText, false);
    }
    
    /**
     * Sets the filter text in the search box.
     * 
     * @param filterText
     *            The filter text to be set.
     * @param forceSet
     *            true if the filter text should forcefully set. This will
     *            trigger a {@link #performFilter(String)} invoke as well (if in
     *            FILTERING mode). A false value does not set the filter text if
     *            it has already been set before.
     * @return true if the filter text was successfully changed and false
     *         otherwise. false is also returned if this fragment was never set
     *         to {@link Mode#FILTERING} mode ever.
     */
    private boolean setFilterText(final String filterText, final boolean forceSet) {
        if (filterText != null && mode == Mode.FILTERING && mSearchBox != null) {
            final String currentFilterText = mSearchBox.getText().toString();
            final String trimmedFilterText = filterText.trim();
            if (!currentFilterText.equals(trimmedFilterText) || forceSet) {
                mSearchBox.setText(trimmedFilterText);
                checkAndPerformFilter(mSearchBox.getText());
                
                return true;
            }
        }
        
        return false;
    }
    
    protected void clearFilterText() {
        setFilterText(Constants.BLANKSTR, false);
    }

    /**
     * Provides the current text that is entered in the filter box.
     * 
     * @return The current text that has been entered in the filter box. If the
     *         mode is not FILTERING, then an empty string is returned. An empty
     *         string is also returned if there is no filter text in the filter
     *         box.
     */
    protected String getFilterText() {
        if (mode == Mode.FILTERING && mSearchBox != null) {
            return mSearchBox.getText().toString().trim();
        }

        return Constants.BLANKSTR;
    }
    
    @Override
    protected void readBundleArguments(final Bundle bundleArgs) {
        super.readBundleArguments(bundleArgs);
        
        // Only set it if a value is sent. Don't set a default value, just leave it as it is.
        if (bundleArgs.containsKey(PARAM_INITIAL_MODE)) {
            mode = Mode.fromValue(bundleArgs.getInt(PARAM_INITIAL_MODE));
        }
        
        setShouldAutoFocusSearchBox(bundleArgs.getBoolean(PARAM_SHOULD_AUTOFOCUS_SEARCH_BOX, true));
        
        if (bundleArgs.containsKey(PARAM_INITIAL_FILTER_TEXT)) {
            // Store the value temporarily and set it later when the action bar is initialized
            mFilterText = bundleArgs.getString(PARAM_INITIAL_FILTER_TEXT);
        }
        
        setCanExitFilteringMode(bundleArgs.getBoolean(PARAM_CAN_EXIT_FILTERING_MODE, true));
    }
    
    @Override
    protected void onHideFragment() {
        Tools.hideVirtualKeyboard(getActivity());
        super.onHideFragment();
    }
    
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        checkAndPerformFilter(s);
    }
    
    private void checkAndPerformFilter(Editable s) {
        if (s != null) {
            checkAndPerformFilter(s.toString());
        }
    }
    
    /**
     * Use this function if filtering needs to be performed manually for derived
     * classes. Do not directly call {@link #performFilter(String)}. This
     * function will check all necessary pre-conditions for filtering to be
     * performed.
     * 
     * @param filterString
     *            The string to be used for filtering.
     * @return true if the filtering was performed and false otherwise.
     */
    protected boolean checkAndPerformFilter(final String filterString) {
        if (mode == Mode.FILTERING && filterString != null) {
            mFilterText = filterString.trim();
            performFilter(mFilterText);
            return true;
        }
        
        return false;
    }
    
    /**
     * To be overridden by derived classes for performing content filtering
     * based on filter text that was entererd in the search box. Do not call this function directly from derived 
     * classes. Use {@link #checkAndPerformFilter(String)} instead.
     * 
     * @param filterString
     *            The string to be used for filtering.
     */
    protected void performFilter(final String filterString) {
        // To be overridden
    }
    
    /**
     * Performs a global search with the text contained in a given
     * {@link TextView}.
     * 
     * @param txtView
     *            The {@link TextView} whose text contents are to be used for
     *            the global search.
     * @return true if the {@link #performGlobalSearch(String)} routine was
     *         called and false otherwise.
     */
    private boolean performGlobalSearchForTextView(final TextView txtView) {
        final String searchString = txtView.getText().toString();
        return checkAndPerformGlobalSearch(searchString);
    }
    
    /**
     * Use this function if global search needs to be performed manually for
     * derived classes. Do not directly call
     * {@link #performGlobalSearch(String)}. This function will check all
     * necessary pre-conditions for global search.
     * 
     * @param searchString
     *            The string to be used for performing a global search.
     * @return true if global search was performed and false otherwise.
     */
    protected boolean checkAndPerformGlobalSearch(final String searchString) {
        if (!TextUtils.isEmpty(searchString)) {
            performGlobalSearch(searchString.trim());
            return true;
        }
        
        return false;
    }
    
    /**
     * To be overridden by derived classes for performing a global search. Do
     * not call this function directly from derived classes. Use
     * {@link #checkAndPerformGlobalSearch(String)} instead.
     * 
     * @param searchString
     *            The search string to be used for performing a global search.
     */
    protected void performGlobalSearch(final String searchString) {
        // To be overridden.
    }

    @Override
    public CustomActionBarConfig getActionBarConfig() {
        CustomActionBarConfig config = new CustomActionBarConfig();
        switch (mode) {
            case FILTERABLE:
                config.setShowSearchButton(true);
                break;
            case FILTERING:
                config.setCustomViewLayoutSrc(R.layout.action_bar_search);
                config.setNavigationButtonState(NavigationButtonState.BACK);
                break;
            case NO_FILTER:
                break;
            default:
                break;
        }
        // In NO_FILTER mode, the deriving class is responsible for configuring the action bar.
        
        return config;
    }
    
    @Override
    public void initCustomViewInCustomActionBar(View customView) {
        if (mode == Mode.FILTERING) {
            mSearchBox = (EditText) customView.findViewById(R.id.search_box);
            mSearchBox.setHint(getSearchHint());
            mSearchBox.setOnEditorActionListener(this);
            mSearchBox.addTextChangedListener(this);
            mSearchBox.setFocusable(true);
            mSearchBox.setFocusableInTouchMode(true);
            mSearchBox.setOnKeyListener(this);

            setFilterText(mFilterText);
            focusSearchBox(shouldAutoFocusSearchBox);
        } else {
            super.initCustomViewInCustomActionBar(customView);
        }
    }
    
    @Override
    public void onSearchButtonPressed() {
        setMode(Mode.FILTERING);
    }    
    
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH && mode == Mode.FILTERING) {
            return performGlobalSearchForTextView(v);
        }
        
        return false;
    }
    
    /**
     * Focuses the search box only if the mode can be filtered. 
     * @param shouldFocus   true to focus the search box, false to de-focus the search box.
     */
    protected void focusSearchBox(final boolean shouldFocus) {
        if (mSearchBox != null && mode == Mode.FILTERING) {
            if (shouldFocus) {
                mSearchBox.requestFocus();
                Tools.showVirtualKeyboard(getActivity(), mSearchBox);
            } else {
                Tools.hideVirtualKeyboard(getActivity(), mSearchBox);
            }
        }
    }
    
    /**
     * Override this from derived classes if needed.
     * 
     * @return the hint text to be displayed when in FILTERING mode.
     */
    protected String getSearchHint() {
        return I18n.tr("Search");
    }
    
    
    @Override
    public void onBackIconPressed() {
        if (!handleBackButton()) {
            super.onBackIconPressed();
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return handleBackButton();
        }
        return false;
    }
    
    private boolean handleBackButton() {
        if (mode == Mode.FILTERING && canExitFilteringMode) {
            Tools.hideVirtualKeyboard(getActivity(), mSearchBox);
            clearFilterText();
            setMode(Mode.FILTERABLE);
            return true;
        }
        return false;
    }
}
