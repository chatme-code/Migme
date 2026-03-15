/**
 * Copyright (c) 2013 Project Goth
 *
 * AlertHandler.java
 * Created Jul 23, 2013, 4:03:56 PM
 */

package com.projectgoth.ui.activity;

import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.mig33.diggle.common.StringUtils;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.common.Config;
import com.projectgoth.common.ConnectionDetail;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Logger;
import com.projectgoth.common.TextUtils;
import com.projectgoth.common.Tools;
import com.projectgoth.common.Version;
import com.projectgoth.controller.ChatController;
import com.projectgoth.controller.FriendsController;
import com.projectgoth.controller.StoreController;
import com.projectgoth.controller.StoreController.StoreItemFilterType;
import com.projectgoth.datastore.ChatDatastore;
import com.projectgoth.datastore.SystemDatastore;
import com.projectgoth.datastore.UserDatastore;
import com.projectgoth.fusion.packet.UrlHandler;
import com.projectgoth.i18n.I18n;
import com.projectgoth.model.ChatConversation;
import com.projectgoth.nemesis.enums.ActivitySourceEnum;
import com.projectgoth.nemesis.model.ContactGroup;
import com.projectgoth.notification.AlertListener;
import com.projectgoth.notification.DialogAlert;

/**
 * @author admin
 * 
 */
public class AlertHandler {

    private static class AlertHandlerHolder {
        static final AlertHandler sINSTANCE = new AlertHandler();
    }

    public static AlertHandler getInstance() {
        return AlertHandlerHolder.sINSTANCE;
    }

    private AlertHandler() {

    }

    public interface MultiTextInputListener {

        public void onOk(HashMap<String, String> results);

        public void onCancel();
    }

    public interface TextInputListener {

        public void onOk(String data);

        public void onCancel();
    }

    public interface ThreeStateAlertListener extends AlertListener {

        public void onDiscard();
    }

    /**
     * Creates a generic input text dialog
     * 
     * @param context
     * @param listener
     * @param title
     * @return
     */
    public static AlertDialog.Builder showTextInputDialog(Context context, final TextInputListener listener,
            String title) {
        if (context == null) {
            context = ApplicationEx.getInstance().getCurrentActivity();
        }

        final EditText input = new EditText(context);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setView(input);
        builder.setCancelable(false);
        builder.setPositiveButton(I18n.tr("Ok"), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null) {
                    listener.onOk(input.getText().toString());
                }
            }
        });
        builder.setNegativeButton(I18n.tr("Cancel"), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null) {
                    listener.onCancel();
                }
            }
        });

        return builder;
    }

    /**
     * Creates a generic input text dialog
     * 
     * @param context
     * @param listener
     * @param title
     * @return
     */
    public static AlertDialog.Builder showMultiTextInputDialog(Context context, final MultiTextInputListener listener,
            String title, HashMap<String, String> inputNameAndValue) {
        if (context == null) {
            context = ApplicationEx.getInstance().getCurrentActivity();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);

        final HashMap<String, EditText> inputs = new HashMap<String, EditText>();
        LinearLayout layout = new LinearLayout(context);
        for (String name : inputNameAndValue.keySet()) {
            EditText input = new EditText(context);
            TextView nameView = new TextView(context);
            nameView.setText(name);
            input.setText(inputNameAndValue.get(name));
            layout.setOrientation(1); // 1 is for vertical orientation
            layout.addView(nameView);
            layout.addView(input);
            inputs.put(name, input);
        }
        builder.setView(layout);

        builder.setPositiveButton(I18n.tr("Ok"), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null) {
                    HashMap<String, String> result = new HashMap<String, String>();
                    for (String key : inputs.keySet()) {
                        EditText input = inputs.get(key);
                        result.put(key, input.getText().toString());
                    }
                    listener.onOk(result);
                }
            }
        });
        builder.setNegativeButton(I18n.tr("Cancel"), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null) {
                    listener.onCancel();
                }
            }
        });

        return builder;
    }

    /**
     * Convenience method for displaying confirmation prompt when blocking a
     * user. The default handling can be overridden by calling methods by
     * passing a non-null listener value
     * 
     * @param context
     *            Context to be used to display the dialog
     * @param listener
     *            If null, default listener or handling defined in this method
     *            will be used
     * @param displayName
     *            Display name of user to be blocked. Will appear in the prompt
     *            message
     * @param userName
     *            Username of the user to be blocked. Used by backend to block
     *            the user
     */
    public void showBlockFriendDialog(Context context, AlertListener listener, String displayName, final String userName) {
        if (context == null) {
            context = ApplicationEx.getInstance().getCurrentActivity();
        }

        if (listener == null) {
            listener = new AlertListener() {

                @Override
                public void onDismiss() {
                    // DO NOTHING
                }

                @Override
                public void onConfirm() {
                    FriendsController.getInstance().requestToBlockUser(userName);
                }
            };
        }

        String message = String.format(I18n.tr("Sure you want to block/mute %s?"), displayName);
        DialogAlert blockDialog = new DialogAlert(context, I18n.tr("Block/Mute"), message, listener);
        blockDialog.showAlert();
    }

    /**
     * Convenience method for displaying confirmation prompt when leaving a
     * group chat and handling it. The default handling can be overridden though
     * by calling methods by passing a non-null leaveListener value
     * 
     * @param context
     *            Context to be used to display the dialog
     * @param leaveListener
     *            If null, default listener or handling defined in this method
     *            will be used
     * @param conversationId
     *            ConversationId of the group chat that user is leaving
     */
    public void showLeaveGroupChatDialog(Context context, AlertListener leaveListener, String conversationId) {
        if (context == null) {
            context = ApplicationEx.getInstance().getCurrentActivity();
        }

        final ChatConversation conversation = ChatDatastore.getInstance().getChatConversationWithId(conversationId);
        if (conversation != null) {
            final String chatId = conversation.getChatId();
            String displayName = conversation.getDisplayName();

            if (leaveListener == null) {
                leaveListener = new AlertListener() {

                    @Override
                    public void onDismiss() {
                        // DO NOTHING
                    }

                    @Override
                    public void onConfirm() {
                        ChatController.getInstance().requestLeaveGroupChat(chatId, conversation.getImMessageType().getImType());
                    }
                };
            }

            String message = String.format(I18n.tr("You'll stop getting messages from this chat. Sure?"),
                            displayName);
            DialogAlert leaveDialog = new DialogAlert(context, I18n.tr("Leave chat"), message, leaveListener);
            leaveDialog.showAlert();
        }
    }

    public void showChangeLanguageDialog(Context context, AlertListener listener, String name, final String id) {

        if (context == null) {
            context = ApplicationEx.getInstance().getCurrentActivity();
        }
        if (listener == null) {
            listener = new AlertListener() {
                @Override
                public void onDismiss() {}

                @Override
                public void onConfirm() {
                    I18n.loadLanguage(id);
                    SystemDatastore.getInstance().saveData(I18n.USER_LANGUAGE, id);
                    FragmentHandler.getInstance().clearAllCustomPopActivities();
                    ApplicationEx.getInstance().restartApp();

                }
            };
        }

        String message = String.format(I18n.tr("Sure you want to change the language to %s? We’ll relaunch the app in %s."), name, name);
        DialogAlert changeDialog = new DialogAlert(context, I18n.tr("Change language"), message, listener);
        changeDialog.showAlert();

    }

    /**
     * @param listener
     * 
     */
    public void showRemoveFriendDialog(Context context, AlertListener listener, String displayName, final int contactId) {
        if (context == null) {
            context = ApplicationEx.getInstance().getCurrentActivity();
        }

        if (listener == null) {
            listener = new AlertListener() {

                @Override
                public void onDismiss() {
                    // DO NOTHING
                }

                @Override
                public void onConfirm() {
                    UserDatastore.getInstance().requestRemoveFriend(contactId);
                }
            };
        }

        String message = String.format(I18n.tr("You won't be a fan or friend of %s anymore. Sure?"),
                        displayName, displayName);
        DialogAlert blockDialog = new DialogAlert(context, I18n.tr("Unfriend"), message, listener);
        blockDialog.showAlert();
    }

    /**
     * @param listener
     * 
     */
    public void showUnfollowFriendDialog(Context context, AlertListener listener, final String username) {
        if (context == null) {
            context = ApplicationEx.getInstance().getCurrentActivity();
        }

        if (listener == null) {
            listener = new AlertListener() {

                @Override
                public void onDismiss() {
                    // DO NOTHING
                }

                @Override
                public void onConfirm() {
                    ActionHandler.getInstance().followOrUnfollowUser(username, ActivitySourceEnum.UNKNOWN);
                }
            };
        }

        String message = String.format(I18n.tr("You won't be a fan of %s anymore. Sure?"),
                username);
        DialogAlert blockDialog = new DialogAlert(context, I18n.tr("Unfan"), message, listener);
        blockDialog.showAlert();
    }

    /**
     * @param listener
     * 
     */
    public void showThreeStateAlertBox(Context context, String title, String message,
            final ThreeStateAlertListener listener) {
        if (context == null) {
            context = ApplicationEx.getInstance().getCurrentActivity();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);

        builder.setNeutralButton(I18n.tr("Discard"), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                listener.onDiscard();
            }
        });

        builder.setPositiveButton(I18n.tr("Save"), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                listener.onConfirm();
            }
        });

        builder.setNegativeButton(I18n.tr("Cancel"), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                listener.onDismiss();
            }
        });

        builder.create().show();
    }

    int selection = -1;

    /**
     * @param context
     * @param groupId
     * @param username
     */
    public void showMoveFriendDialog(Context context, final int contactId, int contactGroupId) {
        if (context == null) {
            context = ApplicationEx.getInstance().getCurrentActivity();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(I18n.tr("Move to contact group"));

        UserDatastore userDatastore = UserDatastore.getInstance();
        final List<ContactGroup> contactGroups = userDatastore.getContactGroups(Config.getInstance().isImEnabled(), true);
        String[] groupNameArray = new String[contactGroups.size()];

        int size = contactGroups.size();
        for (int i = 0; i < size; i++) {
            ContactGroup contactGroup = contactGroups.get(i);
            if (contactGroup.getGroupID() == contactGroupId) {
                selection = i;
            }
            groupNameArray[i] = contactGroup.getGroupName();
        }

        builder.setSingleChoiceItems(groupNameArray, selection, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                selection = which;
            }
        });

        builder.setPositiveButton(I18n.tr("Ok"), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                int newGroupId = contactGroups.get(selection).getGroupID();
                UserDatastore.getInstance().requestMoveFriend(contactId, newGroupId);
            }
        });

        builder.setNegativeButton(I18n.tr("Cancel"), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        builder.create().show();
    }

    /**
     * @param context
     * @param groupId
     * @param groupName
     */
    public void showRemoveContactGroupDialog(Context context, final int groupId, String groupName) {

        ContactGroup contactGroup = UserDatastore.getInstance().getContactGroupWithId(groupId);
        if (contactGroup != null && contactGroup.getFriendIds().size() > 0) {
            String message = String.format(
                    I18n.tr("Move your contacts to another group before deleting this."),
                    groupName);
            DialogAlert dialog = new DialogAlert(context, I18n.tr("Delete contact group"), message,
                    new AlertListener() {

                        @Override
                        public void onDismiss() {
                            // Do nothing
                        }

                        @Override
                        public void onConfirm() {
                            // Do nothing
                        }
                    });
            dialog.showAlert();

        } else {
            String message = String.format(I18n.tr("Sure you want to delete %s?"),
                    groupName);
            DialogAlert dialog = new DialogAlert(context, I18n.tr("Delete contact group"), message,
                    new AlertListener() {

                        @Override
                        public void onDismiss() {
                            // Do nothing
                        }

                        @Override
                        public void onConfirm() {
                            // TODO Auto-generated method stub
                            UserDatastore.getInstance().requestRemoveContactGroup(groupId);
                        }
                    });
            dialog.showAlert();
        }
    }

    public void showDeletePostDialog(Context context, AlertListener listener, final String postId) {
        if (context == null) {
            context = ApplicationEx.getInstance().getCurrentActivity();
        }

        if (listener == null) {
            listener = new AlertListener() {

                @Override
                public void onDismiss() {
                    // DO NOTHING
                }

                @Override
                public void onConfirm() {
                    Tools.showToast(ApplicationEx.getInstance().getCurrentActivity(), I18n.tr("Deleting"));
                    ActionHandler.getInstance().deletePost(postId);
                }
            };
        }

        String message = I18n.tr("Sure you want to delete this post?");
        DialogAlert blockDialog = new DialogAlert(context, I18n.tr("Delete post"), message, listener);
        blockDialog.showAlert();
    }

    public void showCustomConfirmationDialog(Context context, String title, String message, AlertListener listener) {
        if (context == null) {
            context = ApplicationEx.getInstance().getCurrentActivity();
        }

        DialogAlert customConfirmationDialog = new DialogAlert(context, title, message, listener);
        customConfirmationDialog.showAlert();
    }

    public boolean showOkDialog(Context context, String message) {
        boolean result = false;
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(message);
            builder.setPositiveButton(I18n.tr("Ok"), null);
            builder.show();
        } catch (Exception e) {
            Logger.error.log(getClass(), e);
        }
        return result;
    }

    private void updateUIfromConnection(View view, final ConnectionDetail connectionDetails, boolean allowProxy) {
        final EditText txtGateway = (EditText) view.findViewById(R.id.env_custom_host);
        final EditText txtPort = (EditText) view.findViewById(R.id.env_port);
        final EditText txtProxyHost = (EditText) view.findViewById(R.id.env_proxy_host);
        final EditText txtProxyPort = (EditText) view.findViewById(R.id.env_proxy_port);
        final CheckBox cbxUseProxy = (CheckBox) view.findViewById(R.id.env_use_proxy);

        txtGateway.setText(connectionDetails.getGateway());
        txtPort.setText(String.valueOf(connectionDetails.getPort()));
        
        if (allowProxy) {
            cbxUseProxy.setChecked(connectionDetails.isUseProxy());
            txtProxyHost.setText(connectionDetails.getProxyHost());
            int proxyPort = connectionDetails.getProxyPort();
            txtProxyPort.setText(proxyPort==0? "" : String.valueOf(proxyPort));
        }
        
        boolean enableSettings = true;
        switch (connectionDetails.getType()) {
            case PROD:
            case STAGING:
            case QALAB:
                enableSettings = false;
                break;
           default:
               break;
        }
        txtGateway.setEnabled(enableSettings);
        txtPort.setEnabled(enableSettings);
        cbxUseProxy.setEnabled(enableSettings);
        txtProxyHost.setEnabled(enableSettings);
        txtProxyPort.setEnabled(enableSettings);
    }
    
    private void updateConnectionFromUI(View view, final ConnectionDetail connectionDetails, boolean allowProxy) {
        final EditText txtGateway = (EditText) view.findViewById(R.id.env_custom_host);
        final EditText txtPort = (EditText) view.findViewById(R.id.env_port);
        final EditText txtProxyHost = (EditText) view.findViewById(R.id.env_proxy_host);
        final EditText txtProxyPort = (EditText) view.findViewById(R.id.env_proxy_port);
        final CheckBox cbxUseProxy = (CheckBox) view.findViewById(R.id.env_use_proxy);
        
        connectionDetails.setGateway(txtGateway.getText().toString());
        String port = txtPort.getText().toString();
        if (!StringUtils.isEmpty(port)) {
            connectionDetails.setPort(Integer.parseInt(port));
        }
        
        if (allowProxy) {
            connectionDetails.setUseProxy(cbxUseProxy.isChecked());
            connectionDetails.setProxyHost(txtProxyHost.getText().toString());
            String proxyPort = txtProxyPort.getText().toString();
            if (!StringUtils.isEmpty(proxyPort)) {
                connectionDetails.setProxyPort(Integer.parseInt(proxyPort));
            }
        }
    }
    
    private ConnectionDetail createConnectionDetails(ConnectionDetail connDetails, ConnectionDetail.Type type) {
        return (connDetails.getType() == type)? connDetails : new ConnectionDetail(type);
    }

    public void showConnectionSelectorMenu(Context context, ConnectionDetail connectionDetails,
            final boolean isConnectionSelectorEnabled) {

        LayoutInflater inflater = ApplicationEx.getInstance().getCurrentActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.connection_settings_menu, null);
        
        final TextView versionTxt = (TextView) view.findViewById(R.id.version);
        if (versionTxt != null) {
            versionTxt.setText(String.format(I18n.tr("version %s"), Version.getVersionText()));
        }

        final ConnectionDetail connProd = createConnectionDetails(connectionDetails, ConnectionDetail.Type.PROD);
        final ConnectionDetail connStaging = createConnectionDetails(connectionDetails, ConnectionDetail.Type.STAGING);
        final ConnectionDetail connQalab = createConnectionDetails(connectionDetails, ConnectionDetail.Type.QALAB);
        final ConnectionDetail connMiab = createConnectionDetails(connectionDetails, ConnectionDetail.Type.MIAB);
        final ConnectionDetail connCustom = isConnectionSelectorEnabled ?
                createConnectionDetails(connectionDetails, ConnectionDetail.Type.CUSTOM) :
                connectionDetails;

        final RadioGroup environmentSelector = (RadioGroup) view.findViewById(R.id.environment);
        if (isConnectionSelectorEnabled) {
            environmentSelector.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    ConnectionDetail selectedConnection = null;
                    switch (checkedId) {
                        case R.id.env_prod:     selectedConnection = connProd;      break;
                        case R.id.env_staging:  selectedConnection = connStaging;   break;
                        case R.id.env_qalab:    selectedConnection = connQalab;     break;
                        case R.id.env_miab:     selectedConnection = connMiab;      break;
                        default:                selectedConnection = connCustom;    break;
                    }
                    updateUIfromConnection(view, selectedConnection, isConnectionSelectorEnabled);
                }
            });
            
            CompoundButton.OnCheckedChangeListener cl = new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (!isChecked) {
                        ConnectionDetail selectedConnection = null;
                        switch (buttonView.getId()) {
                            case R.id.env_prod:     selectedConnection = connProd;      break;
                            case R.id.env_staging:  selectedConnection = connStaging;   break;
                            case R.id.env_qalab:    selectedConnection = connQalab;     break;
                            case R.id.env_miab:     selectedConnection = connMiab;      break;
                            default:                selectedConnection = connCustom;    break;
                        }
                        updateConnectionFromUI(view, selectedConnection, isConnectionSelectorEnabled);
                    }
                }
            };

            final RadioButton envProd = (RadioButton) view.findViewById(R.id.env_prod);
            final RadioButton envStaging = (RadioButton) view.findViewById(R.id.env_staging);
            final RadioButton envQalab = (RadioButton) view.findViewById(R.id.env_qalab);
            final RadioButton envMiab = (RadioButton) view.findViewById(R.id.env_miab);
            final RadioButton envCustom = (RadioButton) view.findViewById(R.id.env_custom);

            envProd.setText("Production");
            envStaging.setText("Staging");
            envQalab.setText("Qalab");
            envMiab.setText("MIAB");
            envCustom.setText("Custom");

            envProd.setOnCheckedChangeListener(cl);
            envStaging.setOnCheckedChangeListener(cl);
            envQalab.setOnCheckedChangeListener(cl);
            envMiab.setOnCheckedChangeListener(cl);
            envCustom.setOnCheckedChangeListener(cl);

            switch (connectionDetails.getType()) {
                case PROD:      envProd.setChecked(true);       break;
                case STAGING:   envStaging.setChecked(true);    break;
                case QALAB:     envQalab.setChecked(true);      break;
                case MIAB:      envMiab.setChecked(true);       break;
                default:        envCustom.setChecked(true);     break;
            }
        } else {
            final CheckBox cbxUseProxy = (CheckBox) view.findViewById(R.id.env_use_proxy);
            final EditText txtProxyHost = (EditText) view.findViewById(R.id.env_proxy_host);
            final EditText txtProxyPort = (EditText) view.findViewById(R.id.env_proxy_port);
            cbxUseProxy.setVisibility(View.GONE);
            txtProxyHost.setVisibility(View.GONE);
            txtProxyPort.setVisibility(View.GONE);
            environmentSelector.setVisibility(View.GONE);

            connectionDetails.setType(ConnectionDetail.Type.CUSTOM);
            updateUIfromConnection(view, connectionDetails, isConnectionSelectorEnabled);
        }

        final CheckBox cbxUseProxy = (CheckBox) view.findViewById(R.id.env_use_proxy);
        cbxUseProxy.setText(I18n.tr("Use proxy"));

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(view);
        alertDialogBuilder.setTitle(I18n.tr("Connection settings"));

        alertDialogBuilder.setPositiveButton(I18n.tr("Ok"), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                ConnectionDetail selectedConnection = connCustom;
                if (isConnectionSelectorEnabled) {
                    switch(environmentSelector.getCheckedRadioButtonId()) {
                        case R.id.env_prod:     selectedConnection = connProd;      break;
                        case R.id.env_staging:  selectedConnection = connStaging;   break;
                        case R.id.env_qalab:    selectedConnection = connQalab;     break;
                        case R.id.env_miab:     selectedConnection = connMiab;      break;
                    }
                }
                updateConnectionFromUI(view, selectedConnection, isConnectionSelectorEnabled);
                Config.getInstance().setConnectionDetail(selectedConnection);
                UrlHandler.getInstance().setMigboDataServiceUrl(selectedConnection.getMigboDataservice());
                UrlHandler.getInstance().setImageServerUrl(selectedConnection.getImageServer());
                UrlHandler.getInstance().setImagesUrl(selectedConnection.getImagesUrl());
                UrlHandler.getInstance().setSsoUrl(selectedConnection.getSsoUrl());
            }
        });

        alertDialogBuilder.show();
    }

    public void showSearchStoreDialog(final Context context, final String localCurrency, final boolean isInChat,
            final String conversationId, final String initialRecipient) {
        LayoutInflater inflater = ApplicationEx.getInstance().getCurrentActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_store_search, null);

        final EditText giftName = (EditText) view.findViewById(R.id.gift_name);
        TextView priceLabel = (TextView) view.findViewById(R.id.price_label);
        final EditText fromPrice = (EditText) view.findViewById(R.id.from_price);
        TextView dash = (TextView) view.findViewById(R.id.dash);
        final EditText toPrice = (EditText) view.findViewById(R.id.to_price);
        TextView currency = (TextView) view.findViewById(R.id.currency);

        giftName.setHint(I18n.tr("Type gift name"));
        priceLabel.setText(I18n.tr("Select price range"));
        fromPrice.setHint(I18n.tr("From"));
        dash.setText("-");
        toPrice.setHint(I18n.tr("To"));
        currency.setText(localCurrency);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(view);
        alertDialogBuilder.setTitle(I18n.tr("Search gifts"));

        alertDialogBuilder.setPositiveButton(I18n.tr("Ok"), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                String searchString = Constants.BLANKSTR;
                float minPrice = StoreController.DEFAULT_MIN_PRICE;
                float maxPrice = StoreController.DEFAULT_MAX_PRICE;

                if (!TextUtils.isEmpty(giftName.getText().toString())) {
                    searchString = giftName.getText().toString();
                }
                if (!TextUtils.isEmpty(fromPrice.getText().toString())) {
                    minPrice = Float.parseFloat(fromPrice.getText().toString());
                }
                if (!TextUtils.isEmpty(toPrice.getText().toString())) {
                    maxPrice = Float.parseFloat(toPrice.getText().toString());
                }

                ActionHandler.getInstance().displayGiftCategory(ApplicationEx.getInstance().getCurrentActivity(),
                        StoreItemFilterType.GENERAL, Constants.BLANKSTR, I18n.tr("Search results"), searchString, minPrice,
                        maxPrice, isInChat, conversationId, initialRecipient);
            }
        });

        alertDialogBuilder.setNegativeButton(I18n.tr("Cancel"), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        alertDialogBuilder.show();
    }

}
