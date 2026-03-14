/**
 * Copyright (c) 2013 Project Goth
 *
 * ReplyListAdapter.java
 * Created Jul 25, 2013, 4:20:42 PM
 */

package com.projectgoth.ui.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import android.support.v4.app.FragmentActivity;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.Post;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;
import com.projectgoth.ui.holder.ReplyViewHolder;
import com.projectgoth.ui.widget.ClickableSpanEx.ClickableSpanExListener;

/**
 * @author Dangui
 * 
 */
public class ReplyListAdapter extends BaseAdapter {

    FragmentActivity                                          mActivity;

    private List<Post>                                        replyOrReshareData     = new ArrayList<Post>();
    private LayoutInflater                                    mInflater;
    private BaseViewListener<Post>                            replyItemListener;
    private boolean                                           replyOrReshareToOwnPost = false;

    private ClickableSpanExListener                           spanListener;
    private ConcurrentHashMap<String, SpannableStringBuilder> spannableCache;

    public ReplyListAdapter(FragmentActivity activity,  ConcurrentHashMap<String, SpannableStringBuilder> spannableCache) {
        mActivity = activity;
        mInflater = LayoutInflater.from(ApplicationEx.getContext());
        this.spannableCache = spannableCache;
    }

    @Override
    public int getCount() {
        if (replyOrReshareData != null) {
            return replyOrReshareData.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int pos) {
        if (replyOrReshareData != null && pos < replyOrReshareData.size()) {
            return replyOrReshareData.get(pos);
        }

        return null;
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        ReplyViewHolder replyViewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.post_reply_or_reshare, null);
            replyViewHolder = new ReplyViewHolder(mActivity, convertView, spannableCache, spanListener);
            replyViewHolder.setReplyOrReshareToOwnPost(replyOrReshareToOwnPost);
            replyViewHolder.setShowSeperator(false);
            convertView.setTag(R.id.holder, replyViewHolder);
        } else {
            // Saves on inflate call. Get the view holder instead
            replyViewHolder = (ReplyViewHolder) convertView.getTag(R.id.holder);
        }

        Post reply = (Post) getItem(pos);
        replyViewHolder.setData(reply);
        replyViewHolder.setBaseViewListener(replyItemListener);

        return convertView;
    }

    /**
     * @param repliesOrReshares
     */
    public void setRepliesOrReshares(List<Post> repliesOrReshares) {
        this.replyOrReshareData = repliesOrReshares;
    }

    public void setReplyItemListener(BaseViewListener<Post> replyItemListener) {
        this.replyItemListener = replyItemListener;
    }

    /**
     * @return the replyOrReshareToOwnPost
     */
    public boolean isReplyOrReshareToOwnPost() {
        return replyOrReshareToOwnPost;
    }

    /**
     * @param replyOrReshareToOwnPost
     *            the replyOrReshareToOwnPost to set
     */
    public void setReplyOrReshareToOwnPost(boolean replyOrReshareToOwnPost) {
        this.replyOrReshareToOwnPost = replyOrReshareToOwnPost;
    }

    public void setSpanListener(ClickableSpanExListener spanListener) {
        this.spanListener = spanListener;
    }

    /**
     * @param postId
     */
    public void removeReplyOrReshare(String postId) {
        for (int i = 0; i < replyOrReshareData.size(); i++) {
            Post post = replyOrReshareData.get(i);
            if (post.getId().equals(postId)) {
                replyOrReshareData.remove(i);
                break;
            }
        }

        notifyDataSetChanged();
    }
    
    public boolean isEmpty() {
        return getCount() <= 0;
    }
}
