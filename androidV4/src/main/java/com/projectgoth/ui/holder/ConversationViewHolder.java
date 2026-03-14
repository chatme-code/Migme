
package com.projectgoth.ui.holder;

import java.util.concurrent.ConcurrentHashMap;

import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.Profile;
import com.projectgoth.blackhole.enums.ContentType;
import com.projectgoth.blackhole.enums.EmoteType;
import com.projectgoth.blackhole.enums.ImType;
import com.projectgoth.blackhole.enums.PresenceType;
import com.projectgoth.common.Config;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Theme;
import com.projectgoth.common.ThemeValues;
import com.projectgoth.common.Tools;
import com.projectgoth.controller.ChatroomColorController;
import com.projectgoth.controller.ThirdPartyIMController;
import com.projectgoth.datastore.UserDatastore;
import com.projectgoth.i18n.I18n;
import com.projectgoth.imagefetcher.ImageHandler;
import com.projectgoth.model.ChatConversation;
import com.projectgoth.model.Message;
import com.projectgoth.nemesis.model.Friend;
import com.projectgoth.ui.widget.ClickableSpanEx.ClickableSpanExListener;
import com.projectgoth.ui.widget.TextViewEx;

public class ConversationViewHolder extends BaseViewHolder<ChatConversation> {

    private final RelativeLayout                              container;
    private final ImageView                                   mIcon;
    private final TextView                                    mChatroomIcon;
    private final TextView                                    mMainLabel;
    private final TextViewEx                                  mText;
    private final TextView                                    timestamp;
    private final TextView                                    counter;
    private final ImageView                                   mSubIconBottomLeft;
    private final ImageView                                   mPinIcon;
    private final ImageView                                   check;
    private final ImageView                                   mMuteIcon;

    private boolean isForSelectChat;

    private ConcurrentHashMap<String, SpannableStringBuilder> spannableCache;

    public ConversationViewHolder(View view, ConcurrentHashMap<String, SpannableStringBuilder> spannableCache,
                                  boolean isForSelectChat) {
        super(view);
        this.spannableCache = spannableCache;
        this.isForSelectChat = isForSelectChat;

        container = (RelativeLayout) view.findViewById(R.id.container);
        container.setBackgroundColor(Theme.getColor(ThemeValues.LIGHT_BACKGROUND_COLOR));

        mIcon = (ImageView) view.findViewById(R.id.icon);
        mChatroomIcon = (TextView) view.findViewById(R.id.chatroom_mark);
        mMainLabel = (TextView) view.findViewById(R.id.label);
        mText = (TextViewEx) view.findViewById(R.id.sublabel);

        timestamp = (TextView) view.findViewById(R.id.timestamp);
        counter = (TextView) view.findViewById(R.id.msgcounter);
        counter.setVisibility(View.GONE);

        mPinIcon = (ImageView) view.findViewById(R.id.pin);
        mMuteIcon = (ImageView) view.findViewById(R.id.mute);

        mSubIconBottomLeft = (ImageView) view.findViewById(R.id.subicon_bottom_left);

        check = (ImageView) view.findViewById(R.id.check);

        // for opening the user's profile of private fusion chat conversation
        // and post conversation
        mIcon.setOnClickListener(this);
        // for TextViewEx it doesn't work unless I set the clickListener
        // explicitly
        mText.setOnClickListener(this);
        mText.setOnLongClickListener(this);

    }

    @Override
    public void setData(ChatConversation data) {
        super.setData(data);

        // reset all the widgets
        setMainTitle(Constants.BLANKSTR);

        // No need to reset the text content of mText, every item should set it
        // but we must reset the hotkeys and isHtmlDecode of the mText
        setHotkeysFromServer(null);
        setHtmlDecodeText(false);
        // setText(null, spannableCache, null);
        setTimeStamp(Constants.BLANKSTR);
        setCounter(0);
        setUserPresenceIcon(null);

        setChatConversation(data);

        if (data.isPinned()) {
            mPinIcon.setVisibility(View.VISIBLE);
        } else {
            mPinIcon.setVisibility(View.GONE);
        }

        if (data.isMuted()) {
            mMuteIcon.setVisibility(View.VISIBLE);
        } else {
            mMuteIcon.setVisibility(View.GONE);
        }

        if(isForSelectChat) {
            if (data.isChecked()) {
                container.setBackgroundColor(ApplicationEx.getColor(R.color.friend_selected_bg));
                check.setVisibility(View.VISIBLE);
            } else {
                container.setBackgroundColor(ApplicationEx.getColor(R.color.friend_unselected_bg));
                check.setVisibility(View.GONE);
            }

            mPinIcon.setVisibility(View.GONE);
            mMuteIcon.setVisibility(View.GONE);
            counter.setVisibility(View.GONE);
            timestamp.setVisibility(View.GONE);
        }
    }

    private void setChatConversation(ChatConversation chatConversation) {
        // set conversation display name
        setMainTitle(chatConversation.getDisplayName());
        Bitmap defaultIcon = Tools.getBitmap(R.drawable.icon_default_avatar);

        if (chatConversation.isPrivateChat()) {
            // 1. private chat
            mIcon.setVisibility(View.VISIBLE);
            mChatroomIcon.setVisibility(View.INVISIBLE);
            mIcon.setImageBitmap(defaultIcon);

            Friend friend = UserDatastore.getInstance().findUser(chatConversation.getChatId());

            if (null != friend) {
                if (friend.isFusionContact()) {
                    // 1.1 private fusion chat. set display picture from guid
                    ImageHandler.getInstance().loadDisplayPictureFromGuid(mIcon, friend.getGUID(), Config.getInstance()
                            .getDisplayPicSizeNormal(), true);
                    // set presence
                    setUserPresenceIcon(friend.getPresence());
                } else if (friend.isIMContact()) {
                    ImType imType = chatConversation.getImMessageType().getImType();
                    if (imType != null) {
                        setIMUserPresenceIcon(friend.getPresence(), imType);
                    }
                }
            } else if (chatConversation.isIMChat()) {
                // 1.2 private IM chat
                // cannot find the IM contact, set the presence as offline
                ImType imType = chatConversation.getImMessageType().getImType();
                if (imType != null) {
                    setIMUserPresenceIcon(PresenceType.OFFLINE, imType);
                }
            } else {
                // 1.3 private chat with stranger
                Profile profile = UserDatastore.getInstance().getProfileWithUsername(chatConversation.getChatId(),
                        false);
                if (profile != null) {
                    ImageHandler.getInstance().loadDisplayPictureOfUser(mIcon, chatConversation.getChatId(),
                            profile.getDisplayPictureType(), Config.getInstance().getDisplayPicSizeNormal(), true);
                }
                setUserPresenceIcon(null);
            }

        } else if (chatConversation.isChatroom()) {
            mIcon.setVisibility(View.INVISIBLE);
            mChatroomIcon.setVisibility(View.VISIBLE);

            // first character of the chatroom name
            mChatroomIcon.setText(String.valueOf(Character.toUpperCase(chatConversation.getDisplayName().charAt(0))));

            // set color chatroom mark color
            int color = ChatroomColorController.getInstance().getChatroomColor(chatConversation.getChatId());

            GradientDrawable bg = (GradientDrawable) mChatroomIcon.getBackground();
            bg.setColor(color);

            // append the participants counter
            if (chatConversation.isJoined()) {
                appendChatPaticipantsCounter(chatConversation.getParticipantsCount(true));
            }

        } else if (chatConversation.isGroupChat()) {
            mIcon.setVisibility(View.VISIBLE);
            mChatroomIcon.setVisibility(View.INVISIBLE);

            ImageHandler.loadGroupChatIcon(mIcon, chatConversation, Config.getInstance().getDisplayPicSizeNormal());
            appendChatPaticipantsCounter(chatConversation.getParticipantsCount(true));
        }

        if (chatConversation.isChatroom() && (!chatConversation.isJoined())) {
            setText(I18n.tr("Tap to join"), spannableCache, null);
            if (chatConversation.hasUnreadMessage()) {
                chatConversation.resetUnreadMessageCounter();
                ApplicationEx.getInstance().getNotificationHandler().removeNotification(chatConversation.getId());
            }
        } else {
            // set latest message
            Message mostRecentMessage = chatConversation.getMostRecentMessage();

            if (isPhotoMessageYouSent(mostRecentMessage)) {
                setText(I18n.tr("Photo sent"), spannableCache, null);
            } else if (mostRecentMessage != null) {
                // handle the hotkeys of the message
                if (mostRecentMessage.isIncoming()) {
                    if (mostRecentMessage.getHotkeys() == null) {
                        // no hotkeys in the message, we need to indicate it
                        // still
                        setHotkeysFromServer(TextViewEx.NO_HOTKEYS);
                    } else {
                        // if it's sticker, do not set the hotkey here
                        if (mostRecentMessage.getContentType() == ContentType.EMOTE
                                && mostRecentMessage.getEmoteContentType() == EmoteType.STICKERS) {
                            setHotkeysFromServer(TextViewEx.NO_HOTKEYS);
                        } else {
                            setHotkeysFromServer(mostRecentMessage.getHotkeys());
                        }
                    }
                } else {
                    setHotkeysFromServer(null);
                }

                String latestMessage = mostRecentMessage.getMessage();
                if (!chatConversation.isPrivateChat() && !mostRecentMessage.isInfoMessage()) {
                    latestMessage = mostRecentMessage.getSender() + ": " + latestMessage;
                }
                setText(latestMessage, spannableCache, null);
            } else {
                setHtmlDecodeText(true);
                String hintText = Constants.BLANKSTR;
                String messageSnippet = chatConversation.getMessageSnippet();
                if (messageSnippet != null) {
                    hintText = messageSnippet;
                }
                setText(hintText, spannableCache, null);
            }
        }

        setTimeStamp(Tools.getDisplayDate(chatConversation.getChatTimestamp()));
        int unreadMsgCounter = chatConversation.getUnreadMessageCounter();
        setCounter(unreadMsgCounter);
        if (unreadMsgCounter > 0) {
            container.setBackgroundColor(ApplicationEx.getContext().getResources()
                    .getColor(R.color.chat_item_highlighted));
        } else {
            container.setBackgroundColor(ApplicationEx.getContext().getResources()
                    .getColor(R.color.chat_item_unhighlighted));
        }

    }

    public void setHtmlDecodeText(boolean decode) {
        mText.setDecodeText(decode);
    }

    public void setMainTitle(CharSequence s) {
        mMainLabel.setText(s);
    }

    /**
     * @param hotkeys
     *            - For the case that to use the local hotkeys rather than
     *            hotkeys from server, set it null; For the case to use hotkeys
     *            from server but no any hotkey for this TextViewEx, it should
     *            be blank string
     */
    private void setHotkeysFromServer(String[] hotkeys) {
        mText.setHotkeysFromServer(hotkeys);
    }

    private void setCounter(int counter) {
        if (this.counter != null) {
            this.counter.setTextColor(Theme.getColor(ThemeValues.LIGHT_TEXT_COLOR));

            if (counter > 0) {
                this.counter.setVisibility(View.VISIBLE);
                this.counter.setText(counter + Constants.BLANKSTR);
            } else {
                this.counter.setVisibility(View.GONE);
            }
        }
    }

    private void setTimeStamp(String str) {
        timestamp.setText(str);
    }

    private void setText(String text, ConcurrentHashMap<String, SpannableStringBuilder> spannableCache,
            ClickableSpanExListener listener) {
        mText.setClickableSpanExListener(listener);
        mText.setText(text, spannableCache);
    }

    private void appendChatPaticipantsCounter(int count) {
        if (count > 0) {
            // chat participants count has a smaller text size
            String countStr = " (" + count + ")";
            String label = mMainLabel.getText().toString() + countStr;
            SpannableStringBuilder builder = new SpannableStringBuilder(label);
            AbsoluteSizeSpan span = new AbsoluteSizeSpan((int) ApplicationEx.getContext().getResources()
                    .getDimension(R.dimen.text_size_medium));
            builder.setSpan(span, label.length() - countStr.length(), label.length(),
                    SpannableStringBuilder.SPAN_INCLUSIVE_INCLUSIVE);
            mMainLabel.setText(builder);
        }
    }

    private void setUserPresenceIcon(PresenceType presence) {
        if (presence != null) {
            mSubIconBottomLeft.setImageResource(Tools.getFusionPresenceResource(presence));
            mSubIconBottomLeft.setVisibility(View.VISIBLE);
        } else {
            mSubIconBottomLeft.setVisibility(View.GONE);
        }
    }

    public void setIMUserPresenceIcon(PresenceType presence, ImType imType) {
        mSubIconBottomLeft.setImageBitmap(ThirdPartyIMController.getInstance()
                .getIMContactPresenceBmp(imType, presence));
        mSubIconBottomLeft.setVisibility(View.VISIBLE);
    }

    private boolean isPhotoMessageYouSent(Message mostRecentMessage) {
        if (mostRecentMessage != null && mostRecentMessage.getContentType() == ContentType.IMAGE
                && mostRecentMessage.isOutgoing()) {
            return true;
        }
        return false;
    }

    public void setForSelectChat(boolean isForShareInChat) {
        this.isForSelectChat = isForShareInChat;
    }

}
