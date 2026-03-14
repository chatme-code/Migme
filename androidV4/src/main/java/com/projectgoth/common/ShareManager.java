package com.projectgoth.common;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;

import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.Post;
import com.projectgoth.b.data.Profile;
import com.projectgoth.b.data.mime.ChatroomMimeData;
import com.projectgoth.b.data.mime.DeezerMimeData;
import com.projectgoth.b.data.mime.GiftMimeData;
import com.projectgoth.b.data.mime.MimeType;
import com.projectgoth.b.data.mime.PostMimeData;
import com.projectgoth.b.data.mime.ProfileMimeData;
import com.projectgoth.datastore.Session;
import com.projectgoth.datastore.UserDatastore;
import com.projectgoth.events.GAEvent;
import com.projectgoth.fusion.packet.UrlHandler;
import com.projectgoth.i18n.I18n;
import com.projectgoth.imagefetcher.UIUtils;
import com.projectgoth.model.ChatConversation;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.fragment.ShareToFragment.ShareItemListener;
import com.projectgoth.util.PostUtils;

import java.util.List;

public class ShareManager {

    public static final String FACEBOOK_PACKAGE_NAME = "com.facebook.katana";
    public static final String TWITTER_PACKAGE_NAME = "com.twitter.android";

    // these two variables used to pass data to start new chat.
    public static String mCurrentShareMimeType = "";
    public static String mCurrentShareMimeData = "";
    public static String mCurrentShareUrl = "";

    private enum SourceType {
        facebook, twitter, email, miniblog, chat, others
    }

    private enum CampaignType {
        post, user, music, chatroom, artist_post, artist_user
    }

    public enum ShareType {
        SHARE_TO_FEED(0),
        SHARE_TO_CHAT(1), SHARE_TO_EMAIL(2), SHARE_TO_FACEBOOK(3), SHARE_TO_TWITTER(4), SHARE_TO_OTHER(5);

        public int value;

        private ShareType(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }

        public static ShareType fromValue(int value) {
            for (ShareType type : values()) {
                if (type.value == value)
                    return type;
            }
            return null;
        }
    }

    public static void shareWeb(final String url) {
        final FragmentActivity currentActivity = ApplicationEx.getInstance().getCurrentActivity();
        ActionHandler.getInstance().displayShareToFragment(currentActivity, new ShareItemListener() {
            @Override
            public void onShareItemSelected(int type) {
                ShareType shareType = ShareType.fromValue(type);
                switch (shareType) {
                    case SHARE_TO_CHAT:
                        shareToChat(currentActivity, url);
                        break;
                    case SHARE_TO_EMAIL:
                        shareToEmail(currentActivity, Constants.BLANKSTR, url);
                        break;
                    case SHARE_TO_FACEBOOK:
                        shareToFacebookOrTwitter(currentActivity, Constants.BLANKSTR, url, type);
                        break;
                    case SHARE_TO_TWITTER:
                        shareToFacebookOrTwitter(currentActivity, Constants.BLANKSTR, url, type);
                        break;
                    case SHARE_TO_OTHER:
                        share(currentActivity, Constants.BLANKSTR, url);
                        break;
                }
            }
        });
    }

    public static void sharePost(final FragmentActivity activity, final GAEvent event, final Post post) {
        String authorName = PostUtils.getPostAuthorUsername(post);
        if (authorName != null) {
            final String myName = Session.getInstance().getUsername();
            final String url = UrlHandler.getInstance().getPageletServerUrl()
                    + String.format(
                    "/share/post/%s/%s?source=android&referrer=%s&utm_medium=migshares&utm_campaign=%s&utm_source=",
                    authorName, post.getId(), myName,
                    (post.getAuthor().getLabels().isVerified() ? CampaignType.artist_post : CampaignType.post));

            ActionHandler.getInstance().displayShareToFragment(activity, new ShareItemListener() {

                @Override
                public void onShareItemSelected(int type) {
                    String fullShareUrl = url;
                    String title = I18n.tr("You gotta see this!");
                    String content = I18n.tr("Saw this post on mig, thought you'd be interested!\n\n");
                    ShareType shareType = ShareType.fromValue(type);

                    switch (shareType) {
//                        case SHARE_TO_FEED:
//                            fullShareUrl += SourceType.miniblog.toString();
//                            shareToPost(activity, fullShareUrl, post);
//                            break;
                        case SHARE_TO_CHAT:
                            fullShareUrl += SourceType.chat.toString();
                            shareToChat(activity, fullShareUrl, post);
                            break;
                        case SHARE_TO_EMAIL:
                            fullShareUrl += SourceType.email.toString();
                            content += fullShareUrl;
                            shareToEmail(activity, title, content);
                            break;
                        case SHARE_TO_FACEBOOK:
                            fullShareUrl += SourceType.facebook.toString();
                            shareToFacebookOrTwitter(activity, content, fullShareUrl, type);
                            break;
                        case SHARE_TO_TWITTER:
                            fullShareUrl += SourceType.twitter.toString();
                            shareToFacebookOrTwitter(activity, content, fullShareUrl, type);
                            break;
                        case SHARE_TO_OTHER:
                            fullShareUrl += SourceType.others.toString();
                            content += fullShareUrl;
                            share(activity, title, content);
                            break;
                        default:
                    }

                    event.send(fullShareUrl);
                }

            });
        }
    }

    public static void shareProfile(final FragmentActivity activity, final String username) {
        String myName = Session.getInstance().getUsername();
        final Profile profile = UserDatastore.getInstance().getProfileWithUsername(username, false);
        final String url = UrlHandler.getInstance().getPageletServerUrl()
                + String.format("/share/user/%s?source=android&referrer=%s&utm_medium=migshares", username, myName);

        ActionHandler.getInstance().displayShareToFragment(activity, new ShareItemListener() {

            @Override
            public void onShareItemSelected(int type) {
                String fullShareUrl = url;
                String title = I18n.tr("This guy totally rocks");
                String content = I18n.tr("Saw this person on mig, thought you'd be interested! \n\n");

                if (profile != null) {
                    content += username + " (" + profile.getNumOfFollowers() + " fans): ";

                    if (profile.getLabels().isVerified()) {
                        fullShareUrl += "&utm_campaign=" + CampaignType.artist_user;
                    } else {
                        fullShareUrl += "&utm_campaign=" + CampaignType.user;
                    }
                }

                fullShareUrl += "&utm_source=";
                ShareType shareType = ShareType.fromValue(type);

                switch (shareType) {
//                    case SHARE_TO_FEED:
//                        fullShareUrl += SourceType.miniblog.toString();
//                        shareToPost(activity, url, profile);
//                        break;
                    case SHARE_TO_CHAT:
                        fullShareUrl += SourceType.chat.toString();
                        shareToChat(activity, url, profile);
                        break;
                    case SHARE_TO_EMAIL:
                        fullShareUrl += SourceType.email.toString();
                        content += fullShareUrl;
                        shareToEmail(activity, title, content);
                        break;
                    case SHARE_TO_FACEBOOK:
                        fullShareUrl += SourceType.facebook.toString();
                        //content += fullShareUrl;
                        shareToFacebookOrTwitter(activity, content, fullShareUrl, type);
                        break;
                    case SHARE_TO_TWITTER:
                        fullShareUrl += SourceType.twitter.toString();
                        //content += fullShareUrl;
                        shareToFacebookOrTwitter(activity, content, fullShareUrl, type);
                        break;
                    case SHARE_TO_OTHER:
                        fullShareUrl += SourceType.others.toString();
                        content += fullShareUrl;
                        share(activity, title, content);
                        break;
                    default:
                }

                GAEvent.Profile_Share.send(fullShareUrl);
            }

        });
    }

    public static void shareChatroom(final FragmentActivity activity, ChatConversation conversation) {
        final String chatroomName = conversation.getDisplayName();
        String escapedChatroomName = Uri.encode(chatroomName);

        String myName = Session.getInstance().getUsername();
        final String url = UrlHandler.getInstance().getPageletServerUrl()
                + String.format("/share/chatroom/%s?source=android&referrer=%s&utm_medium=migshares&utm_campaign=%s",
                escapedChatroomName, myName, CampaignType.chatroom.toString());
        final String subject = String.format(I18n.tr("Your friend %s thought you might like this and shared it with you. "),
                myName);

        ActionHandler.getInstance().displayShareToFragment(activity, new ShareItemListener() {
            @Override
            public void onShareItemSelected(int type) {
                String fullShareUrl = url;
                fullShareUrl += "&utm_source=";
                boolean isShared = true;
                ShareType shareType = ShareType.fromValue(type);

                switch (shareType) {
//                    case SHARE_TO_FEED:
//                        fullShareUrl += SourceType.miniblog.toString();
//                        shareChatroomToPost(activity, fullShareUrl, chatroomName);
//                        break;
                    case SHARE_TO_CHAT:
                        fullShareUrl += SourceType.chat.toString();
                        shareChatroomToChat(activity, fullShareUrl, chatroomName);
                        break;
                    case SHARE_TO_EMAIL:
                        fullShareUrl += SourceType.email.toString();
                        shareToEmail(activity, subject, fullShareUrl);
                        break;
                    case SHARE_TO_FACEBOOK:
                        fullShareUrl += SourceType.facebook.toString();
                        shareToFacebookOrTwitter(activity, subject, fullShareUrl, type);
                        break;
                    case SHARE_TO_TWITTER:
                        fullShareUrl += SourceType.twitter.toString();
                        shareToFacebookOrTwitter(activity, subject, fullShareUrl, type);
                        break;
                    case SHARE_TO_OTHER:
                        fullShareUrl += SourceType.others.toString();
                        share(activity, subject, fullShareUrl);
                        break;
                    default:
                        isShared = false;
                }

                if (isShared) {
                    GAEvent.Chat_Share.send(url);
                }
            }
        });

    }

    public static void shareDeezerRadio(final FragmentActivity activity, final String radioId, final String url) {
        final String myName = Session.getInstance().getUsername();
        final String shareUrl = UrlHandler.getInstance().getPageletServerUrl()
                + String.format(
                "/share/music/deezer/radio/%s?source=android&referrer=%s&utm_medium=migshares&utm_campaign=%s&utm_source=",
                radioId, myName, CampaignType.music.toString());

        ActionHandler.getInstance().displayShareToFragmentFromDeezer(activity, new ShareItemListener() {

            @Override
            public void onShareItemSelected(int type) {
                String title = I18n.tr("I think you'll like this music!");
                String content = I18n.tr("Really enjoyed this music selection! ");
                String fullShareUrl = shareUrl;
                ShareType shareType = ShareType.fromValue(type);

                switch (shareType) {
                    case SHARE_TO_FEED:
                        GAEvent.Deezer_ShareToPost.send(radioId);
                        fullShareUrl += SourceType.miniblog.toString();
                        shareRadioToPost(activity, fullShareUrl, radioId);
                        break;
                    case SHARE_TO_CHAT:
                        GAEvent.Deezer_ShareToChat.send(radioId);
                        fullShareUrl += SourceType.chat.toString();
                        shareRadioToChat(activity, fullShareUrl, radioId);
                        break;
                    case SHARE_TO_EMAIL:
                        GAEvent.Deezer_Share.send(radioId);
                        fullShareUrl += SourceType.email.toString();
                        content += fullShareUrl;
                        shareToEmail(activity, title, content);
                        break;
                    case SHARE_TO_FACEBOOK:
                        GAEvent.Deezer_Share.send(radioId);
                        fullShareUrl += SourceType.facebook.toString();
                        //content += fullShareUrl;
                        shareToFacebookOrTwitter(activity, content, fullShareUrl, type);
                        break;
                    case SHARE_TO_TWITTER:
                        GAEvent.Deezer_Share.send(radioId);
                        fullShareUrl += SourceType.twitter.toString();
                        //content += fullShareUrl;
                        shareToFacebookOrTwitter(activity, content, fullShareUrl, type);
                        break;
                    case SHARE_TO_OTHER:
                        GAEvent.Deezer_Share.send(radioId);
                        fullShareUrl += SourceType.others.toString();
                        content += fullShareUrl;
                        share(activity, title, content);
                        break;
                    default:
                }
            }

        });
    }

    public static void share(Activity activity, String subject, String content) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        sendIntent.putExtra(Intent.EXTRA_TEXT, content);
        sendIntent.setType("text/plain");
        activity.startActivityForResult(Intent.createChooser(sendIntent, I18n.tr("Share on")), -1);
    }

    public static void shareToFacebookOrTwitter(Activity activity, String shareText, String shareUrl,  int type) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        String fullShareUrl = shareText + shareUrl;
        intent.putExtra(Intent.EXTRA_TEXT, fullShareUrl);

        // see if official app is found
        boolean officialAppFound = false;
        List<ResolveInfo> matches = activity.getPackageManager().queryIntentActivities(intent, 0);

        String packageName = type == ShareType.SHARE_TO_TWITTER.value() ? TWITTER_PACKAGE_NAME : FACEBOOK_PACKAGE_NAME;
        for (ResolveInfo info : matches) {
            if (info.activityInfo.packageName.toLowerCase().startsWith(packageName)) {
                intent.setPackage(info.activityInfo.packageName);
                officialAppFound = true;
                break;
            }
        }

        // As fallback, launch appropriate sharer
        if (!officialAppFound || !UIUtils.hasICS()) {
            String sharerUrl;
            if (type == ShareType.SHARE_TO_FACEBOOK.value()) {
                shareUrl += shareText;
                sharerUrl = "https://www.facebook.com/sharer/sharer.php?u=" + shareUrl;
            } else {
                sharerUrl = "https://twitter.com/intent/tweet?&url=" + shareUrl + "&text=" + shareText;
            }
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(sharerUrl));
        }

        activity.startActivity(intent);
    }

    public static void shareToEmail(Activity activity, String title, String content) {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "", null));
        intent.putExtra(Intent.EXTRA_SUBJECT, title);
        intent.putExtra(Intent.EXTRA_TEXT, content);
        activity.startActivity(Intent.createChooser(intent, I18n.tr("Share on")));
    }

    public static void shareToChat(Activity activity, String url, Post post) {
        if (post == null) {
            return;
        }
        // convert the Post object to String mime type & data
        String mimeType = mCurrentShareMimeType = MimeType.POST.getValue();
        String mimeData = mCurrentShareMimeData = PostMimeData.createFromId(post.getId()).toJson();
        mCurrentShareUrl = url;

        ActionHandler.getInstance().displayShareInChat(activity, url, mimeType, mimeData);
    }

    public static void shareToChat(Activity activity, String url) {
        ActionHandler.getInstance().displayShareInChat(activity, url, Constants.BLANKSTR, Constants.BLANKSTR);
    }

    public static void shareToChat(Activity activity, String url, Profile profile) {
        if (profile == null) {
            return;
        }
        // convert the Post object to String mime type & data
        String mimeType = mCurrentShareMimeType = MimeType.PROFILE.getValue();
        String mimeData = mCurrentShareMimeData = ProfileMimeData.createFromName(profile.getUsername()).toJson();
        mCurrentShareUrl = url;

        ActionHandler.getInstance().displayShareInChat(activity, url, mimeType, mimeData);
    }

    public static void shareChatroomToChat(Activity activity, String url, String chatroomName) {
        // convert to String mime type & data
        String mimeType = mCurrentShareMimeType = MimeType.CHATROOM.getValue();
        String mimeData = mCurrentShareMimeData = ChatroomMimeData.createFromName(chatroomName).toJson();
        mCurrentShareUrl = url;

        ActionHandler.getInstance().displayShareInChat(activity, url, mimeType, mimeData);
    }

    public static void shareRadioToChat(Activity activity, String url, String id) {
        if (id == null) {
            return;
        }
        String mimeType = mCurrentShareMimeType = MimeType.DEEZER.getValue();
        String mimeData = mCurrentShareMimeData = DeezerMimeData.createDeezerData(DeezerMimeData.formatRadioId(id),
                DeezerMimeData.DeezerDataType.RADIO, true).toJson();
        mCurrentShareUrl = url;

        ActionHandler.getInstance().displayShareInChat(activity, url, mimeType, mimeData);
    }

    private static void shareToPost(FragmentActivity activity, String url, Post post) {
        if (post == null) {
            return;
        }

        // convert the Post object to String mime type & data
        mCurrentShareMimeType = MimeType.POST.getValue();
        mCurrentShareMimeData = PostMimeData.createFromId(post.getId()).toJson();
        mCurrentShareUrl = url;

        ActionHandler.getInstance().displayShareboxWithMimeData(activity);
    }

    private static void shareToPost(FragmentActivity activity, String url, Profile profile) {
        if (profile == null) {
            return;
        }

        // convert the Profile object to String mime type & data
        mCurrentShareMimeType = MimeType.PROFILE.getValue();
        mCurrentShareMimeData = ProfileMimeData.createFromName(profile.getUsername()).toJson();
        mCurrentShareUrl = url;

        ActionHandler.getInstance().displayShareboxWithMimeData(activity);
    }

    public static void shareToPost(FragmentActivity activity, GiftMimeData giftMimeData) {
        if (giftMimeData == null) {
            return;
        }

        mCurrentShareMimeType = MimeType.GIFT.getValue();
        mCurrentShareMimeData = giftMimeData.toJson();

        ActionHandler.getInstance().displayShareboxWithMimeData(activity);
    }

    private static void shareChatroomToPost(FragmentActivity activity, String url, String chatroomName) {

        // convert to String mime type & data
        mCurrentShareMimeType = MimeType.CHATROOM.getValue();
        mCurrentShareMimeData = ChatroomMimeData.createFromName(chatroomName).toJson();
        mCurrentShareUrl = url;

        ActionHandler.getInstance().displayShareboxWithMimeData(activity);
    }

    public static void shareRadioToPost(FragmentActivity activity, String url, String id) {
        if (id == null) {
            return;
        }
        mCurrentShareMimeType = MimeType.DEEZER.getValue();
        mCurrentShareMimeData = DeezerMimeData.createDeezerData(DeezerMimeData.formatRadioId(id),
                DeezerMimeData.DeezerDataType.RADIO, true).toJson();
        mCurrentShareUrl = url;

        ActionHandler.getInstance().displayShareboxWithMimeData(activity);
    }

    public static void clearShareData() {
        mCurrentShareMimeType = null;
        mCurrentShareMimeData = null;
        mCurrentShareUrl = null;
    }
}
