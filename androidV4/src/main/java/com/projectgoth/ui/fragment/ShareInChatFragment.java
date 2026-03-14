
package com.projectgoth.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.mime.MimeData;
import com.projectgoth.b.data.mime.MimeTypeDataModel;
import com.projectgoth.common.Constants;
import com.projectgoth.controller.ChatController;
import com.projectgoth.i18n.I18n;
import com.projectgoth.model.ChatConversation;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.activity.CustomActionBarConfig;
import com.projectgoth.ui.activity.CustomActionBarConfig.NavigationButtonState;
import com.projectgoth.ui.activity.FragmentHandler;
import com.projectgoth.ui.fragment.StartChatFragment.StartChatActionType;
import com.projectgoth.ui.holder.BaseViewHolder;
import com.projectgoth.ui.widget.ScrollViewEx;
import com.projectgoth.ui.widget.SelectedItemBox;

/**
 * Created by houdangui on 2/3/15.
 */
public class ShareInChatFragment extends BaseFragment implements View.OnClickListener,
        BaseViewHolder.BaseViewListener<ChatConversation>, SelectedItemBox.SelectedItemBoxListener {

    private RelativeLayout         newChatBar;
    private TextView               newChat;
    private TextView               recentChat;
    private ImageView              btnSend;
    private EditText               msgBox;

    private SelectedItemBox        mSelectedUserContainer;
    private ScrollViewEx           mScrollContainer;

    private ChatListFragment       chatListFragment;

    private List<ChatConversation> mSelectedChatList = new ArrayList<ChatConversation>();
    private boolean                isPerformFilter   = false;

    private String                 url;
    private String                 mimeType;
    private String                 mimeData;

    public static final String     PARAM_URL         = "PARAM_URL";
    public static final String     PARAM_MIMETYPE    = "PARAM_MIMETYPE";
    public static final String     PARAM_MIMEDATA    = "PARAM_MIMEDATA";

    @Override
    protected void readBundleArguments(Bundle args) {
        super.readBundleArguments(args);
        url = args.getString(PARAM_URL);
        mimeType = args.getString(PARAM_MIMETYPE);
        mimeData = args.getString(PARAM_MIMEDATA);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSelectedUserContainer = (SelectedItemBox) view.findViewById(R.id.selected_container);
        mSelectedUserContainer.setOnClickListener(this);
        mSelectedUserContainer.setListener(this);

        mScrollContainer = (ScrollViewEx) view.findViewById(R.id.selected_scroll_view);
        mScrollContainer.setMaxHeight(ApplicationEx.getDimension(R.dimen.sel_usr_box_max_h));

        msgBox = (EditText) view.findViewById(R.id.share_msg_box);
        msgBox.setHint(I18n.tr("Type a message"));

        newChatBar = (RelativeLayout) view.findViewById(R.id.new_chat_bar);

        newChat = (TextView) view.findViewById(R.id.new_chat);
        newChat.setText(I18n.tr("New"));

        recentChat = (TextView) view.findViewById(R.id.recent_chat_title);

        chatListFragment = FragmentHandler.getInstance().getChatListFragment(false, true);
        chatListFragment.setExternalListener(this);

        addChildFragment(R.id.chat_list_container, chatListFragment);

        int recentCount = ChatController.getInstance().getSortedChatList().size();
        recentChat.setText(String.format(I18n.tr("Recent") + " (%d)", recentCount));

        btnSend = (ImageView) view.findViewById(R.id.button_send);
        btnSend.setOnClickListener(this);
        newChatBar.setOnClickListener(this);

        resetSendButtonState();

    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_share_in_chat;
    }

    @Override
    public void onItemClick(View v, ChatConversation data) {
        boolean isChecked = !data.isChecked();
        data.setChecked(isChecked);

        if (isChecked) {
            // add
            mSelectedChatList.add(data);
        } else {
            // remove
            mSelectedChatList.remove(data);
        }

        resetSendButtonState();

        // refresh chat list
        chatListFragment.notifyDataSetChanged(false);
    }

    private void resetSendButtonState() {
        btnSend.setEnabled(mSelectedChatList.size() > 0);
    }

    @Override
    public void onItemLongClick(View v, ChatConversation data) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        resetSelectedChatList();
    }

    private void resetSelectedChatList() {
        for (ChatConversation chat : mSelectedChatList) {
            chat.setChecked(false);
        }

        mSelectedChatList.clear();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.new_chat_bar:
                ActionHandler.getInstance().displayStartChat(getActivity(), StartChatActionType.SHARE_TO_NEW_CHAT,
                        null, null);
                getActivity().finish();
                break;
            case R.id.button_send:
                if (btnSend.isEnabled()) {
                    ArrayList<MimeData> dataList = new ArrayList<MimeData>(MimeTypeDataModel.parse(mimeType, mimeData));

                    if (dataList != null) {
                        for (ChatConversation chat : mSelectedChatList) {
                            ChatController.getInstance().sendShareMessage(chat.getId(), msgBox.getText().toString(),
                                    url, dataList);
                        }
                    }

                    if (mSelectedChatList.size() == 1) {
                        ActionHandler.getInstance().displayChatConversation(getActivity(),
                                mSelectedChatList.get(0).getId());
                    }

                    getActivity().finish();
                }

                break;
        }
    }

    @Override
    public CustomActionBarConfig getActionBarConfig() {
        CustomActionBarConfig config = new CustomActionBarConfig();
        config.setNavigationButtonState(NavigationButtonState.BACK);
        return config;
    }

    @Override
    protected String getTitle() {
        return I18n.tr("Share in chat");
    }

    @Override
    protected int getTitleIcon() {
        return R.drawable.ad_chat_white;
    }

    @Override
    public void performFilter(String filterString) {
        if (filterString.equals(Constants.BLANKSTR)) {
            isPerformFilter = false;
        } else {
            isPerformFilter = true;
        }
        chatListFragment.filter(filterString);
    }

    @Override
    public void onTextInputChanged(CharSequence s, int start, int before, int count) {
    }

}
