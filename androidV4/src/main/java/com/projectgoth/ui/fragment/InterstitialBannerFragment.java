/**
 * Copyright (c) 2013 Project Goth
 *
 * InterstitialBannerFragment.java
 * Created Oct 7, 2013, 3:22:20 PM
 */

package com.projectgoth.ui.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import com.projectgoth.R;
import com.projectgoth.ui.activity.ActionHandler;

/**
 * @author angelorohit
 */
public class InterstitialBannerFragment extends BaseDialogFragment {

    private String      title         = null;
    private String      desc          = null;
    private String      actionUrl     = null;
    private TextView    titleTextView = null;
    private TextView    descTextView  = null;
    private ImageButton closeBtn      = null;    
    
    /**
     * Constructor
     * @param title     The title of the banner content.
     * @param desc      The description of the banner content.
     * @param actionUrl The url to navigate to when the content is clicked. Can be null.
     */
    public InterstitialBannerFragment(final String title, final String desc, final String actionUrl) {
        super();       
        this.title = title;
        this.desc = desc;
        this.actionUrl = actionUrl;
    }
    
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_interstitial_banner;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        titleTextView = (TextView) view.findViewById(R.id.interstitial_banner_title);
        descTextView = (TextView) view.findViewById(R.id.interstitial_banner_content_desc);
        closeBtn = (ImageButton) view.findViewById(R.id.btn_close_interstitial_banner);
        
        if (descTextView != null) {
            if (desc != null) {
                descTextView.setText(desc);
            }
            
            descTextView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!TextUtils.isEmpty(actionUrl)) {
                        // Open the browser fragment and dimiss this dialog.
                        ActionHandler.getInstance().displayBrowser(getActivity(), actionUrl);
                        dismiss();
                    }
                }
            });
        }
        
        if (titleTextView != null && !TextUtils.isEmpty(title)) {
            titleTextView.setText(title);
        }
        
        if (closeBtn != null) {
            closeBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        }
    }

}
