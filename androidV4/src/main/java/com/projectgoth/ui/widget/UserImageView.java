package com.projectgoth.ui.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.Profile;
import com.projectgoth.blackhole.enums.ImType;
import com.projectgoth.blackhole.enums.PresenceType;
import com.projectgoth.common.Config;
import com.projectgoth.controller.ThirdPartyIMController;
import com.projectgoth.datastore.Session;
import com.projectgoth.datastore.UserDatastore;
import com.projectgoth.imagefetcher.ImageHandler;
import com.projectgoth.nemesis.model.Friend;

// UserImageView is a display image combined with a presence icon.
public class UserImageView extends ImageView {

    private Drawable presenceDrawable = null;

    private static final Drawable ONLINE;
    private static final Drawable AWAY;
    private static final Drawable BUSY;
    private static final Drawable OFFLINE;

    static {
        final Resources resources = ApplicationEx.getInstance().getResources();
        ONLINE = resources.getDrawable(R.drawable.ic_presence_online);
        AWAY = resources.getDrawable(R.drawable.ic_presence_away);
        BUSY = resources.getDrawable(R.drawable.ic_presence_busy);
        OFFLINE = resources.getDrawable(R.drawable.ic_presence_offline);
    }

    public UserImageView(Context context) {
        super(context);
    }

    public UserImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UserImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setUser(final String username) {
        ImageHandler.getInstance().loadDisplayPictureOfUser(this, username, Config.getInstance().getDisplayPicSizeNormal(), true);
        setPresenceImageForUsername(username);
    }

    public void setUser(final Friend friend) {
        if (friend.getGUID() != null) {
            setUserImageFromGuid(friend.getGUID());
        } else {
            //it could be null when the packet is pushed for the first time when mutual following happens
            setUserImage(friend.getUsername());
        }
        setPresenceImage(friend);
    }

    public void setUserImage(final String username) {
        ImageHandler.getInstance().loadDisplayPictureOfUser(this, username, Config.getInstance().getDisplayPicSizeNormal(), true);
    }

    public void setUserImage(Profile userProfile) {
        if (userProfile != null) {
            ImageHandler.getInstance().loadDisplayPictureOfUser(this, userProfile.getUsername(), userProfile.getDisplayPictureType(),
                    Config.getInstance().getDisplayPicSizeSmall(), true);
        }
    }

    public void setSelfImage(Profile userProfile) {
        if (userProfile != null) {
            if (Session.getInstance() != null) {
                ImageHandler.getInstance().loadDisplayPictureFromGuid(this, Session.getInstance().getDisplayableGuid(),
                        Config.getInstance().getDisplayPicSizeSmall(), true);
            } else {
                ImageHandler.getInstance().loadDisplayPictureOfUser(this, userProfile.getUsername(), userProfile.getDisplayPictureType(),
                        Config.getInstance().getDisplayPicSizeSmall(), true);
            }
        }
    }

    public void setUserImageFromGuid(final String guid) {
        ImageHandler.getInstance().loadDisplayPictureFromGuid(this, guid, Config.getInstance().getDisplayPicSizeNormal(), true);
    }

    public void setPresenceImageForUsername(final String username) {
        Friend friendDetails = UserDatastore.getInstance().findMig33User(username);
        if (friendDetails != null) {
            setPresenceImage(friendDetails.getPresence());
        } else {
            hidePresenceImage();
        }
    }

    public void setPresenceImage(final ImType imType, final PresenceType presence) {
        Bitmap bitmap = ThirdPartyIMController.getInstance().getIMContactPresenceBmp(imType, presence);
        presenceDrawable = new BitmapDrawable(getResources(), bitmap);
        invalidate();
    }

    public void setPresenceImage(final Friend friend) {
        if (friend.isFusionContact()) {
            setPresenceImage(friend.getPresence());
        } else if (friend.isIMContact()) {
            setPresenceImage(friend.getIMType(), friend.getPresence());
        } else {
            setPresenceImage(friend.getPresence());
        }
    }

    public void setPresenceImage(final PresenceType presence) {
        switch(presence) {
            case AVAILABLE:  presenceDrawable = ONLINE;  break;
            case AWAY:       presenceDrawable = AWAY;    break;
            case BUSY:       presenceDrawable = BUSY;    break;
            default:         presenceDrawable = OFFLINE; break;
        }
        invalidate();
    }

    public void hidePresenceImage() {
        presenceDrawable = null;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(presenceDrawable != null) {
            int paddingLeft = getPaddingLeft();
            int paddingRight = getPaddingRight();
            int left = paddingLeft;
            int bottom = getHeight() - getPaddingBottom();
            int size = (getWidth() - paddingLeft-paddingRight)/4;

            presenceDrawable.setBounds(left, bottom-size, left+size, bottom);
            presenceDrawable.draw(canvas);
        }
    }
}
