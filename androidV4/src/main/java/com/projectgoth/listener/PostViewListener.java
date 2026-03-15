/**
 * Copyright (c) 2013 Project Goth
 *
 * PostViewListener.java
 * Created Oct 16, 2014, 11:21:28 AM
 */

package com.projectgoth.listener;

import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;

import com.migme.commonlib.enums.ImageFileType;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.Post;
import com.projectgoth.b.data.Tag;
import com.projectgoth.common.ShareManager;
import com.projectgoth.common.Tools;
import com.projectgoth.controller.PostsController;
import com.projectgoth.datastore.PostsDatastore;
import com.projectgoth.datastore.Session;
import com.projectgoth.enums.PostListType;
import com.projectgoth.events.BroadcastHandler;
import com.projectgoth.events.GAEvent;
import com.projectgoth.i18n.I18n;
import com.projectgoth.ui.UrlHandler;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.fragment.ShareboxFragment;
import com.projectgoth.ui.fragment.ShareboxFragment.ShareboxActionType;
import com.projectgoth.ui.fragment.SinglePostFragment;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;
import com.projectgoth.ui.holder.PostViewHolder;
import com.projectgoth.util.AnimUtils;
import com.projectgoth.util.PostUtils;


/**
 * A listener that can be used for all {@link PostViewHolder} instances.
 * @author angelorohit
 */
public class PostViewListener implements BaseViewListener<Post> {

    protected final FragmentActivity  activity;
    protected final boolean           isGroupPostType;
    final PostListType                mListType;

    public PostViewListener(final FragmentActivity activity, PostListType postListType) {
        this.activity = activity;
        this.isGroupPostType = (postListType == PostListType.GROUP_POSTS);
        this.mListType = postListType;
    }

    /* (non-Javadoc)
     * @see com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener#onItemClick(android.view.View, java.lang.Object)
     */
    @Override
    public void onItemClick(View v, Post data) {

        // for [non-login] users
        if (Session.getInstance().isBlockUsers()) {
            int id = v.getId();
            if (id == R.id.author_picture) {
                ActionHandler.getInstance().displayProfile(activity, PostUtils.getPostAuthorUsername(data));
                return;
            } else {
                ActionHandler.getInstance().displayLoginDialogFragment(activity);
                return;
            }
        }

        if (Tools.hideVirtualKeyboard(this.activity)) {
            //If software keyboard showing just hide it and do nothing
            return;
        }

        final int viewId = v.getId();

        switch (viewId) {
            case R.id.replies:
                if (PostUtils.isPostLocked(data)) {
                    Tools.showToast(activity, I18n.tr("Post locked"));
                } else {
                    GAEvent.Miniblog_FeedsReplyClick.send();
                    ActionHandler.getInstance().displaySinglePostPage(activity, data.getId(),
                            isGroupPostType, SinglePostFragment.HeaderTab.REPLY_TAB, true);
                }
                break;
            case R.id.post_container:
            case R.id.root_post_container:
            case R.id.root_author_details:

            case R.id.content_view:
                if(mListType == PostListType.SEARCH_POSTS) {
                    GAEvent.Miniblog_ClickPostResult.send();
                }
                ActionHandler.getInstance().displaySinglePostPage(activity, data.getId(), isGroupPostType, false);
                break;
            case R.id.feedback:
                ActionHandler.getInstance().displaySinglePostPage(activity, data.getId(),
                        isGroupPostType, SinglePostFragment.HeaderTab.EMOTE_TAB, false);
                break;
            case R.id.author_details:
            case R.id.author_picture:
                ActionHandler.getInstance().displayProfile(activity, PostUtils.getPostAuthorUsername(data));
                break;
            case R.id.reshares:
                if (!PostUtils.canPostBeReshared(data)) {
                    Tools.showToast(activity, I18n.tr("Private post."));
                } else if (isGroupPostType) {
                    Tools.showToast(activity, I18n.tr("Oops. Group posts can't be reposted for now."));
                } else {
                    GAEvent.Miniblog_FeedsRepostClick.send();
                    ActionHandler.getInstance().displaySinglePostPage(activity, data.getId(),
                            isGroupPostType, SinglePostFragment.HeaderTab.RESHARE_TAB, true);
                    //ActionHandler.getInstance().displaySharebox(activity, ShareboxActionType.REPOST, data.getId(), null, null, true);
                }
                break;
            case R.id.watched_post_icon:
                if (isGroupPostType) {
                    Tools.showToast(activity, I18n.tr("Oops. Group posts can't be added to favorites for now."));
                } else {
                    ActionHandler.getInstance().watchOrUnwatchPost(data.getId(), !data.getIsWatching());
                }
                break;
            case R.id.footprint_item_container:
                String footprintUsername = (String) v.getTag();
                ActionHandler.getInstance().displayProfile(activity, footprintUsername);
                break;
            case R.id.remove_action:
                PostsDatastore.getInstance().removePostWithId(data.getId());
                PostsDatastore.getInstance().removePostDraftData(data.getId());
                BroadcastHandler.Post.sendDeleted(data.getId());
                break;
            case R.id.retry_action:
                if(!Session.getInstance().isNetworkConnected()) {
                    final TextView title = ((TextView) v.getTag());
                    AnimUtils.changeTextAnimation(title, I18n.tr("Try posting again"), I18n.tr("Network is not available"));
                    break;
                }
                PostsDatastore.getInstance().createNewPostFromDraft(data.getId());
                Tools.showToast(v.getContext(), I18n.tr("Sending"));
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

    /* (non-Javadoc)
     * @see com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener#onItemLongClick(android.view.View, java.lang.Object)
     */
    @Override
    public void onItemLongClick(View v, Post data) {
        // TODO Auto-generated method stub
        
    }

    protected void handlePhotoDisplayForPost(final Post post, final String urlToDisplay) {
        if (post != null && post.getPhoto() != null) {
            ActionHandler.getInstance().displayPhotoViewerFragmentForPost(activity, post);
        } else if (urlToDisplay != null) {
            if (ImageFileType.isGifUrl(urlToDisplay)) {
                ActionHandler.getInstance().displayPhotoViewerFragmentForUrlInPostBody(activity, urlToDisplay, post, true, true);  
            } else if (ImageFileType.isImageUrl(urlToDisplay)) {
                ActionHandler.getInstance().displayPhotoViewerFragmentForUrlInPostBody(activity, urlToDisplay, post, true, false);  
            } else {
                UrlHandler.displayUrl(activity, urlToDisplay);
            }
        }
    }
}
