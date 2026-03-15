package com.projectgoth.ui.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.SystemLanguage;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Tools;
import com.projectgoth.controller.ChatController;
import com.projectgoth.controller.SystemController;
import com.projectgoth.i18n.I18n;
import com.projectgoth.nemesis.listeners.GetSystemLanguageListener;
import com.projectgoth.nemesis.model.MigError;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.activity.CustomActionBarConfig;
import com.projectgoth.ui.widget.ButtonEx;
import com.projectgoth.ui.widget.SelectedItemBox;
import com.projectgoth.ui.widget.SelectedItemLabel;

/**
 * Created by houdangui on 12/11/14.
 */
public class CreateChatroomFragment extends BaseFragment implements SelectedItemBox.SelectedItemBoxListener,
        View.OnClickListener{

    private TextView hint;
    private TextView roomName;
    private EditText editRoomName;
    private TextView roomNameLimit;
    private TextView description;
    private EditText editDescription;
    private TextView descriptionOptional;
    private TextView keywords;
    private SelectedItemBox keywordsContainer;
    private TextView keywordsOptional;
    private TextView keywordsUsage;
    private TextView language;
    private TextView selectedLang;
    private LinearLayout languageBtn;
    private TextView allowKick;
    private ToggleButton kickToggleBtn;
    private ButtonEx createChatroom;

    private SystemLanguage[] systemLanguages;
    private SystemLanguage selectedLangItem;

    final static private String DEFAULT_LANGUAGE = "English";

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_create_chatroom;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        hint = (TextView) view.findViewById(R.id.hint);
        hint.setText(I18n.tr("Create a space to talk about what you love."));

        //room name
        roomName = (TextView) view.findViewById(R.id.room_name);
        roomName.setText(I18n.tr("Name"));

        editRoomName = (EditText) view.findViewById(R.id.edit_room_name);

        roomNameLimit = (TextView) view.findViewById(R.id.room_name_limit);
        roomNameLimit.setText(I18n.tr("max. 15 characters"));

        //description
        description = (TextView) view.findViewById(R.id.description);
        description.setText(I18n.tr("Description"));

        editDescription = (EditText) view.findViewById(R.id.edit_description);

        descriptionOptional = (TextView) view.findViewById(R.id.description_optional);
        descriptionOptional.setText(I18n.tr("optional"));

        //keywords
        keywords = (TextView) view.findViewById(R.id.keywords);
        keywords.setText(I18n.tr("Keywords"));

        keywordsContainer = (SelectedItemBox) view.findViewById(R.id.keywords_container);
        keywordsContainer.setListener(this);
        keywordsContainer.setEditTextHint(Constants.BLANKSTR);
        keywordsContainer.setOnClickListener(this);

        keywordsOptional = (TextView) view.findViewById(R.id.keywords_optional);
        keywordsOptional.setText(I18n.tr("optional"));

        keywordsUsage = (TextView) view.findViewById(R.id.keywords_usage);
        keywordsUsage.setText(I18n.tr("(separate with commas)"));

        //language
        language = (TextView) view.findViewById(R.id.language);
        language.setText(I18n.tr("Language"));

        selectedLang = (TextView) view.findViewById(R.id.selected_language);
        selectedLang.setText(DEFAULT_LANGUAGE);

        languageBtn = (LinearLayout) view.findViewById(R.id.sel_lang_container);
        languageBtn.setOnClickListener(this);

        //allow kick
        allowKick = (TextView) view.findViewById(R.id.allow_kick);
        allowKick.setText(I18n.tr("Let users kick others out"));

        kickToggleBtn = (ToggleButton) view.findViewById(R.id.toggle_button);

        //create chatroom
        createChatroom = (ButtonEx) view.findViewById(R.id.create_chatroom);
        createChatroom.setText(I18n.tr("CREATE CHAT ROOM"));
        createChatroom.setOnClickListener(this);

        //register no connection disable buttons
        addButtonToNoConnectionDisableButtonList(createChatroom);

    }

    @Override
    public void onResume() {
        super.onResume();

        //request language list
        SystemController.getInstance().requestGetSystemLanguageList(new GetSystemLanguageListener() {

            @Override
            public void onLanguagesReceived(SystemLanguage[] systemLanguages) {
                setSystemLanguages(systemLanguages);
            }

            @Override
            public void onError(MigError error) {
                super.onError(error);
            }

        });
    }

    private void setSystemLanguages(SystemLanguage[] systemLanguages) {
        this.systemLanguages = systemLanguages;
    }

    private CharSequence[] getLanguagesNames() {
        CharSequence[] languageNames;
        if (systemLanguages != null) {
            languageNames = new CharSequence[systemLanguages.length];
            for (int i=0; i< systemLanguages.length ; i++) {
                languageNames[i] = systemLanguages[i].getName();
            }
        } else {
            languageNames = new CharSequence[0];
        }

        return languageNames;
    }

    @Override
    public CustomActionBarConfig getActionBarConfig() {
        CustomActionBarConfig config = new CustomActionBarConfig();
        config.setNavigationButtonState(CustomActionBarConfig.NavigationButtonState.BACK);
        return config;
    }

    @Override
    protected String getTitle() {
        return I18n.tr("Create chat room");
    }

    @Override
    protected int getTitleIcon() {
        return R.drawable.ad_chatroomadd_white;
    }

    @Override
    public void performFilter(String filterString) {}

    @Override
    public void onTextInputChanged(CharSequence s, int start, int before, int count) {
        if (count > 0 && s.subSequence(start, start + count).toString().equals(",")) {
            String keyword = s.subSequence(0, start).toString();
            if (keyword.length() > 0) {
                //add the keyword
                SelectedItemLabel label = new SelectedItemLabel(ApplicationEx.getContext());
                label.setOnClickListener(this);
                label.setDisplayName(keyword);
                keywordsContainer.addItem(label);
                keywordsContainer.clearTextInput();
            } else {
                keywordsContainer.clearTextInput();
            }
        }
    }

    @Override
    public void onClick(View view) {
        if (view instanceof SelectedItemLabel) {
            SelectedItemLabel label  = (SelectedItemLabel)view;
            removeKeywordLabel(label);
        } else {
            int id = view.getId();
            switch (id) {
                case R.id.sel_lang_container:
                    displayLanguageList();
                    break;
                case R.id.create_chatroom:
                    handleCreateChatroom();
                    break;
                case R.id.keywords_container:
                    keywordsContainer.showInputCursor();
                    break;
            }
        }
    }

    private void removeKeywordLabel(SelectedItemLabel label) {
        keywordsContainer.removeView(label);
    }

    private void displayLanguageList() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(getLanguagesNames(), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                selectedLangItem = systemLanguages[which];
                selectedLang.setText(selectedLangItem.getName());
            }
        });
        builder.create().show();
    }

    public interface ChatroomCreatedListener {
        public void onChatroomCreated(String chatroomName, Integer groupId);
    }

    void handleCreateChatroom() {

        String strChatroomName = editRoomName.getText().toString();
        String strDescription = editDescription.getText().toString();
        String strKeywords = getKeyWordsString();
        String language = selectedLangItem != null ? selectedLangItem.getCode() : null;
        boolean allowKicking = kickToggleBtn.isChecked();

        if (!TextUtils.isEmpty(strChatroomName)) {
            Tools.showToast(getActivity(), I18n.tr("Creating"));
            ChatController.getInstance().createChatroom( strChatroomName, strDescription, strKeywords, language,
                    allowKicking, new ChatroomCreatedListener() {
                        @Override
                        public void onChatroomCreated(String chatroomName, Integer groupId) {
                            ActionHandler.getInstance().displayPublicChat(getActivity(), chatroomName,
                                    groupId != null ? groupId.intValue() : 0);
                        }
                    });
        }
    }

    private String getKeyWordsString() {
        StringBuilder keywords = new StringBuilder();

        int count = keywordsContainer.getChildCount();

        String textInput = keywordsContainer.getTextInput().trim();
        if(count == 1 && !TextUtils.isEmpty(textInput)) {
            //only one keyword entered, no label yet
            keywords.append(textInput);
        } else if (count > 1) {
            for (int i = 0; i < count - 1; i++) {
                if (i != 0) {
                    keywords.append(",");
                }
                keywords.append(((SelectedItemLabel) keywordsContainer.getChildAt(i)).getDisplayName().trim());
            }
        }

        return  keywords.toString();
    }
}
