/**
 * Copyright (c) 2013 Project Goth
 *
 * ProfileFragment.java
 * Created Aug 20, 2013, 3:53:22 PM
 */

package com.projectgoth.ui.fragment;

import com.projectgoth.R;
import com.projectgoth.events.GAEvent;

public class MiniProfileChatFragment extends MiniProfilePopupFragment {

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_profile_chat;
    }

    @Override protected void dismissPopupDelay() {
        // Do nothing.
    }

    // This is overridden by MiniProfileChatFragment
    protected void sendGiftListEvent() {
        GAEvent.Chat_SwipeLeftGiftPage.send();
    }

    // This is overridden by MiniProfileChatFragment
    protected void sendBadgeListEvent() {
        GAEvent.Chat_SwipeLeftBadgePage.send();
    }

    // This is overridden by MiniProfileChatFragment
    protected void sendFanListEvent() {
        GAEvent.Chat_SwipeLeftFanPage.send();
    }

}
