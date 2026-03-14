package com.projectgoth.ui.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mig33.diggle.events.BaseBroadcastHandler;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.enums.UserProfileDisplayPictureChoiceEnum;
import com.projectgoth.common.Config;
import com.projectgoth.i18n.I18n;
import com.projectgoth.imagefetcher.ImageHandler;

import java.util.ArrayList;

/**
 * Created by houdangui on 18/6/15.
 */
public class HorizontalRecipientListAdapter extends BaseAdapter {

    private ArrayList<String>       mUsernames      = new ArrayList<String>();
    private ArrayList<String>       mSelectedUsers  = new ArrayList<String>();
    private boolean                 mShowSelectAll;
    private LayoutInflater          mInflater;
    private RecipientItemListener   mListener;

    public interface RecipientItemListener {
        public void onSelectAllClicked();

        public void onRecipientClicked(String username);

        public void onAddMoreClicked();
    }

    public enum Type {
        SELECT_ALL,
        RECIPIENT,
        ADD_RECIPIENT;
    }

    public HorizontalRecipientListAdapter() {
        super();
        mInflater = LayoutInflater.from(ApplicationEx.getContext());
    }

    @Override
    public Object getItem(int position) {
        if (getItemViewType(position) == Type.RECIPIENT.ordinal()) {
            int offset = 0;
            if (getItemViewType(0) != Type.RECIPIENT.ordinal()) {
                offset = 1;
            }
            return mUsernames.get(position - offset);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        if (mShowSelectAll) {
            return mUsernames.size() + 2;
        } else {
            return mUsernames.size() + 1;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mShowSelectAll && position == 0) {
            return Type.SELECT_ALL.ordinal();
        } else if (position == getCount() - 1) {
            return Type.ADD_RECIPIENT.ordinal();
        } else {
            return Type.RECIPIENT.ordinal();
        }
    }


    @Override
    public int getViewTypeCount() {
        return Type.values().length;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        int typeValue = getItemViewType(position);
        if (typeValue == Type.SELECT_ALL.ordinal()) {
            convertView = setupSelectAllItemView();
        } else if (typeValue == Type.RECIPIENT.ordinal()) {
            convertView = setupRecipientItemView(position);
        } else if (typeValue == Type.ADD_RECIPIENT.ordinal()) {
            convertView = setupAddRecipientItemView();
        }

        return convertView;
    }

    private View setupSelectAllItemView() {
        View view = mInflater.inflate(R.layout.select_all_recipients, null);

        ImageView selAllIcon = (ImageView) view.findViewById(R.id.select_all_icon);
        TextView selAllText = (TextView) view.findViewById(R.id.select_all_text);
        selAllText.setTextColor(ApplicationEx.getInstance().getResources().getColor(R.color.gift_balance_black));

        selAllText.setText(I18n.tr("Everyone") + " (" + mUsernames.size() + ")");

        ImageView checked = (ImageView) view.findViewById(R.id.check);

        if (isAllSelected()) {
            checked.setVisibility(View.VISIBLE);
        } else {
            checked.setVisibility(View.GONE);
        }

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onSelectAllClicked();
            }
        };
        selAllIcon.setOnClickListener(clickListener);
        selAllText.setOnClickListener(clickListener);

        return view;
    }

    private boolean isAllSelected() {
        return mUsernames.size() == mSelectedUsers.size();
    }

    private View setupRecipientItemView(int position) {
        View view = mInflater.inflate(R.layout.holder_horizontal_gift_recipient, null);

        final ImageView recipientIcon = (ImageView) view.findViewById(R.id.recipient_icon);
        final String username = (String) getItem(position);
        ImageHandler.ImageLoadListener listener = new ImageHandler.ImageLoadListener() {
            @Override
            public void onImageLoaded(Bitmap bitmap) {
                BaseBroadcastHandler.User.sendFetchAvatarCompleted();
            }

            @Override
            public void onImageFailed(ImageView imageView) {
            }
        };
        ImageHandler.getInstance().loadDisplayPictureOfUser(recipientIcon, username, Config.getInstance().getDisplayPicSizeNormal(), true, listener);
        ImageView checked = (ImageView) view.findViewById(R.id.check);

        if (mSelectedUsers.contains(username)) {
            checked.setVisibility(View.VISIBLE);
        } else {
            checked.setVisibility(View.GONE);
        }

        TextView recipientName = (TextView) view.findViewById(R.id.recipient_name);
        recipientName.setTextColor(ApplicationEx.getInstance().getResources().getColor(R.color.gift_balance_black));
        recipientName.setText(username);

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onRecipientClicked(username);
            }
        };
        recipientIcon.setOnClickListener(clickListener);
        recipientName.setOnClickListener(clickListener);

        return view;
    }

    private View setupAddRecipientItemView() {
        View view = mInflater.inflate(R.layout.add_recipient_btn, null);
        /** hide add recipient temporary **/
//        ImageView addBtn = (ImageView) view.findViewById(R.id.add_btn);
//        addBtn.setImageResource(R.drawable.ad_gift_add_people);
//
//        addBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                listener.onAddMoreClicked();
//            }
//        });

        return view;
    }

    public void setUsernames(ArrayList<String> usernames) {
        this.mUsernames = usernames;
    }

    public void setSelectedUsers(ArrayList<String> selectedUsers) {
        this.mSelectedUsers = selectedUsers;
    }

    public void setShowSelectAll(boolean showSelectAll) {
        this.mShowSelectAll = showSelectAll;
    }

    public void setListener(RecipientItemListener listener) {
        this.mListener = listener;
    }
}