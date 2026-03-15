
package com.projectgoth.ui.adapter;

import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.Post;
import com.projectgoth.enums.PostListType;
import com.projectgoth.listener.PostViewListener;
import com.projectgoth.ui.holder.PostViewHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PostListAdapter.java
 * 
 * @author dangui
 */

public class PostListAdapter extends BaseAdapter {

    private FragmentActivity                                    mActivity;
    private List<Post>                                          mPostList      = new ArrayList<Post>();
    private LayoutInflater                                      mInflater;
    private PostListType                                        mType;
    protected ConcurrentHashMap<String, SpannableStringBuilder> spannableCache = new ConcurrentHashMap<String, SpannableStringBuilder>();
    private PostViewListener                                    postViewListener;
    protected String                                            deezerIdPrefix = "post-";

    // ---- handler messages and delay configurations for the dataset changed notification -----
    // -----------------------------------------------------------------------------------------
    private static final int                                    NOTIFY_DATASET_CHANGED_MESSAGE = 0;
    
    /**
     * Note: 
     *  {@link #NOTIFY_DATASET_BLOCK_DELAY} must be greater than 
     *  the actual {@link #DATASET_CHANGED_DELAY} in milliseconds
     */
    private static final long                                   DATASET_CHANGED_DELAY          = 500;
    // -----------------------------------------------------------------------------------------
    
    //@formatter:off
    private Handler adapterHandler = new Handler(new Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case NOTIFY_DATASET_CHANGED_MESSAGE:
                    notifyDataSetChanged();
                    return true;
            }
            return false;
        }
    });
    //@formatter:on

    public PostListAdapter(FragmentActivity activity, PostListType type) {
        super();
        mActivity = activity;
        mInflater = LayoutInflater.from(ApplicationEx.getContext());
        mType = type;
        postViewListener = new PostViewListener(activity, type);
    }

    @Override
    public int getCount() {
        if (mPostList != null) {
            return mPostList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int pos) {
        if (mPostList != null && pos < getCount()) {
            return mPostList.get(pos);
        }
        return null;
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        PostViewHolder postViewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.holder_post_list_item, null);
            postViewHolder = new PostViewHolder(mActivity, convertView, spannableCache);
            convertView.setTag(R.id.holder, postViewHolder);
        } else {
            // Saves on inflate call. Get the view holder instead
            postViewHolder = (PostViewHolder) convertView.getTag(R.id.holder);
        }
        Post post = (Post) getItem(pos);
        if (post.getMimeTypeData() != null) {
            if (post.getMimeTypeData().contains("deezer")) {
                post.getFirstMimeData().setDataId(deezerIdPrefix + post.getId());
            }
        }
        // set isWatchable and isShowDelete here, because it is decided by the
        // client side, not the Post instance
        if (mType == PostListType.GROUP_POSTS) {
            postViewHolder.setWatchable(false);
        } else {
            postViewHolder.setWatchable(true);
        }

        postViewHolder.setBaseViewListener(postViewListener);
        postViewHolder.setData(post);

        return convertView;
    }

    public void setPostList(List<Post> postList) {
        mPostList = postList;
        scheduleUIUpdate();
    }

    /**
     * Schedule the {@link #notifyDataSetChanged()} in a later time.
     *
     * check {@link #DATASET_CHANGED_DELAY} and {@link #NOTIFY_DATASET_BLOCK_DELAY} for the delay settings
     */
    private void scheduleUIUpdate() {
        if (!adapterHandler.hasMessages(NOTIFY_DATASET_CHANGED_MESSAGE)) {
            adapterHandler.sendEmptyMessageDelayed(NOTIFY_DATASET_CHANGED_MESSAGE, DATASET_CHANGED_DELAY);
        }
    }

    public int getPostNum(boolean excludingPromotedPost) {
        if (mPostList == null) {
            return 0;
        }

        List<Post> postList = mPostList;
        int promotedPostNum = 0;
        if (excludingPromotedPost) {
            for (Post post : postList) {
                if (post.isPromoted()) {
                    promotedPostNum++;
                }
            }
            return postList.size() - promotedPostNum;
        } else {
            return postList.size();
        }

    }
}
