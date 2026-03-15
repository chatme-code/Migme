/**
 * Copyright (c) 2013 Project Goth
 * <p/>
 * SinglePostViewListener.java
 * Created Oct 17, 2014, 2:32:21 PM
 */

package com.projectgoth.listener;

import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.projectgoth.R;
import com.projectgoth.b.data.Author;
import com.projectgoth.b.data.Post;
import com.projectgoth.common.ShareManager;
import com.projectgoth.common.Tools;
import com.projectgoth.datastore.Session;
import com.projectgoth.events.GAEvent;
import com.projectgoth.i18n.I18n;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.activity.AlertHandler;
import com.projectgoth.util.PostUtils;


/**
 * A type of {@link PostViewListener} used for the Single Post Page.
 * @author angelorohit
 */
public class SinglePostViewListener extends PostViewListener {

    /**
     * @param activity
     */
    public SinglePostViewListener(FragmentActivity activity) {
        super(activity, null);
    }

    @Override
    public void onItemClick(View v, Post data) {

        if (Tools.hideVirtualKeyboard(this.activity)) {
            //If software keyboard showing just hide it and do nothing
            return;
        }

        final int viewId = v.getId();
        final Object tagData = v.getTag();
        switch (viewId) {
            case R.id.post_container:
                break;
            case R.id.content_view: {
                Boolean isRootData = (Boolean) v.getTag(R.id.is_root_data_content);
                if (isRootData == null || !isRootData) {
                    break;
                }
            }
            case R.id.root_post_container:
            case R.id.root_author_details:
                if (data == null) {
                    break;
                }
                Post root = data.getRootPost();
                if (root != null) {
                    // open root post page
                    ActionHandler.getInstance().displaySinglePostPage(activity, root.getId(), isGroupPostType, false);
                }

                break;
            case R.id.author_details:
            case R.id.author_picture:
                if (data == null) {
                    break;
                }
                Author author = data.getAuthor();
                if (author != null) {
                    ActionHandler.getInstance().displayProfile(activity, author.getUsername());
                }
                break;
            case R.id.footprint_item_container:
                if (tagData != null) {
                    ActionHandler.getInstance().displayProfile(activity, (String) tagData);
                }
                break;
            case R.id.reply_author_pic:
            case R.id.reply_author_details:
                final String replyAuthorUsername = PostUtils.getPostAuthorUsername(data);
                if (replyAuthorUsername != null) {
                    ActionHandler.getInstance().displayProfile(activity, replyAuthorUsername);
                }
                break;
            case R.id.share:
                if (!PostUtils.isMyPost(data) && !PostUtils.canPostBeReshared(data)) {
                    Tools.showToast(activity, I18n.tr("Private post."));
                } else {
                    ShareManager.sharePost(activity, GAEvent.Miniblog_Share, data);
                }
                break;
        }
    }

    @Override
    public void onItemLongClick(View v, Post data) {
        if (PostUtils.isMyPost(data) || Session.getInstance().isGlobalAdmin()) {
            AlertHandler.getInstance().showDeletePostDialog(activity, null, data.getId());
        }
    }
}
