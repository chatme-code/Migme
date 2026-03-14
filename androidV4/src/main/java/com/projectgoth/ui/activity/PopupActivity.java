/**
 * Copyright (c) 2013 Project Goth
 *
 * PopupActivity.java
 * Created Jul 23, 2013, 5:42:47 PM
 */

package com.projectgoth.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import com.projectgoth.R;
import com.projectgoth.events.AppEvents;

/**
 * @author cherryv
 * 
 */
public class PopupActivity extends BaseFragmentActivity {

    private ImageView          mCloseButton;
    private boolean            showCloseButton;

    public static final String PARAM_SHOW_CLOSE_BUTTON = "PARAM_SHOW_CLOSE_BUTTON";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        setContentView(R.layout.activity_popup);
        
        Intent params = getIntent();
        String fragmentId = params.getStringExtra(AppEvents.Application.Extra.FRAGMENT_ID);
        showCloseButton = params.getBooleanExtra(PARAM_SHOW_CLOSE_BUTTON, true);
        FragmentHandler.getInstance().showFragmentWithId(getSupportFragmentManager(), fragmentId, false);

        mCloseButton = (ImageView) findViewById(R.id.close_button);
        mCloseButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (v == mCloseButton) {
                    finish();
                }
            }
        });

        if (!showCloseButton) {
            mCloseButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onShowFragment(Fragment fragment) {
    }

    @Override
    public void onHideFragment(Fragment fragment) {
    }

    @Override
    public void registerReceivers() {
    }

    @Override
    protected void unregisterReceivers() {
    }

}
