package com.projectgoth.events;

import com.facebook.FacebookRequestError;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.common.Logger;

public enum GAEvent {

    Chat_SendTextMessage                        (Category.CHAT, "SendTextMessage"                           ),
    Chat_SendTextMessageSuccess                 (Category.CHAT, "SendTextMessageSuccess"                    ),
    Chat_SendTextMessageFail                    (Category.CHAT, "SendTextMessageFail"                       ),
    Chat_RetryTextMessage                       (Category.CHAT, "RetryTextMessage"                          ),
    Chat_RetryTextMessageSuccess                (Category.CHAT, "RetryTextMessageSuccess"                   ),
    Chat_RetryTextMessageFail                   (Category.CHAT, "RetryTextMessageFail"                      ),
    Chat_SendStickerUi                          (Category.CHAT, "SendStickerUi"                             ),
    Chat_SendStickerUiSuccess                   (Category.CHAT, "SendStickerUiSuccess"                      ),
    Chat_SendStickerUiFail                      (Category.CHAT, "SendStickerUiFail"                         ),
    Chat_RetryStickerUi                         (Category.CHAT, "RetryStickerUi"                            ),
    Chat_RetryStickerUiSuccess                  (Category.CHAT, "RetryStickerUiSuccess"                     ),
    Chat_RetryStickerUiFail                     (Category.CHAT, "RetryStickerUiFail"                        ),
    Chat_SendImage                              (Category.CHAT, "SendImage"                                 ),
    Chat_SendImageSuccess                       (Category.CHAT, "SendImageSuccess"                          ),
    Chat_SendImageFail                          (Category.CHAT, "SendImageFail"                             ),
    Chat_SendGift                               (Category.CHAT, "SendGift"                                  ),
    Chat_SendGiftSuccess                        (Category.CHAT, "SendGiftSuccess"                           ),
    Chat_SendGiftFail                           (Category.CHAT, "SendGiftFail"                              ),
    Chat_SendFreeGiftCommandSuccess             (Category.CHAT, "SendFreeGiftCommandSuccess"                ),
    Chat_SendFreeGiftCommandFail                (Category.CHAT, "SendFreeGiftCommandFail"                   ),
    Chat_SendGiftUiRecharge                     (Category.CHAT, "SendGiftUiRecharge"                        ),
    Chat_SwipeLeft                              (Category.CHAT, "SwipeLeft"                                 ),
    Chat_SwipeRight                             (Category.CHAT, "SwipeRight"                                ),
    Chat_SwipeCenter                            (Category.CHAT, "SwipeCenter"                               ),
    Chat_SwipeRightOpenChat                     (Category.CHAT, "SwipeRightOpenChat"                        ),
    Chat_SwipeLeftOpenMiniprofile               (Category.CHAT, "SwipeLeftOpenMiniprofile"                  ),
    Chat_SwipeLeftLongPress                     (Category.CHAT, "SwipeLeftLongPress"                        ),
    Chat_SwipeLeftLongPressFollow               (Category.CHAT, "SwipeLeftLongPressFollow"                  ),
    Chat_SwipeLeftLongPressPrivateChat          (Category.CHAT, "SwipeLeftLongPressPrivateChat"             ),
    Chat_SwipeLeftLongPressViewProfile          (Category.CHAT, "SwipeLeftLongPressViewProfile"             ),
    Chat_SwipeLeftLongPressSendGift             (Category.CHAT, "SwipeLeftLongPressSendGift"                ),
    Chat_SwipeLeftLongPressBlockPeople          (Category.CHAT, "SwipeLeftLongPressBlockPeople"             ),
    Chat_SwipeLeftLongPressKick                 (Category.CHAT, "SwipeLeftLongPressKick"                    ),
    Chat_SwipeLeftLongPressReport               (Category.CHAT, "SwipeLeftLongPressReport"                  ),
    Chat_SwipeLeftGiftPage                      (Category.CHAT, "SwipeLeftGiftPage"                         ),
    Chat_SwipeLeftBadgePage                     (Category.CHAT, "SwipeLeftBadgePage"                        ),
    Chat_SwipeLeftFanPage                       (Category.CHAT, "SwipeLeftFanPage"                          ),
    Chat_DropdownAddFavorite                    (Category.CHAT, "DropdownAddFavorite"                       ),
    Chat_DropdownAddPeopleToChat                (Category.CHAT, "DropdownAddPeopleToChat"                   ),
    Chat_AddPeopleToChat                        (Category.CHAT, "AddPeopleToChat"                           ),
    Chat_OpenMiniprofile                        (Category.CHAT, "OpenMiniprofile"                           ),
    Chat_DropdownTransferCredits                (Category.CHAT, "DropdownTransferCredits"                   ),
    Chat_MainButtonStartChat                    (Category.CHAT, "MainButtonStartChat"                       ),
    Chat_MainButtonFindFriends                  (Category.CHAT, "MainButtonFindFriends"                     ),
    Chat_MainButtonCreateChatroom               (Category.CHAT, "MainButtonCreateChatroom"                  ),
    Chat_MainButtonSearchChat                   (Category.CHAT, "MainButtonSearchChat"                      ),
    Chat_MainButtonSearchChatroom               (Category.CHAT, "MainButtonSearchChatroom"                  ),
    Chat_StartChat                              (Category.CHAT, "StartChat"                                 ),
    Chat_ClickSearchUsername                    (Category.CHAT, "ClickSearchUsername"                       ),
    Chat_ClickFacebook                          (Category.CHAT, "ClickFacebook"                             ),
    Chat_ClickEmail                             (Category.CHAT, "ClickEmail"                                ),
    Chat_ClickRecommendedUsers                  (Category.CHAT, "ClickRecommendedUsers"                     ),
    Chat_SearchAddUsers                         (Category.CHAT, "SearchAddUsers"                            ),
    Chat_FacebookInviteSent                     (Category.CHAT, "FacebookInviteSent"                        ),
    Chat_EmailInviteSent                        (Category.CHAT, "EmailInviteSent"                           ),
    Chat_RecommendedUserAdd                     (Category.CHAT, "RecommendedUserAdd"                        ),
    Chat_ClickChatFilterResult                  (Category.CHAT, "ClickChatFilterResult"                     ),
    Chat_LaunchChatroomSearch                   (Category.CHAT, "LaunchChatroomSearch"                      ),
    Chat_LaunchPeopleSearch                     (Category.CHAT, "LaunchPeopleSearch"                        ),
    Chat_ClickChatroomFilterResult              (Category.CHAT, "ClickChatroomFilterResult"                 ),
    Chat_GlobalChatroomSearch                   (Category.CHAT, "GlobalChatroomSearch"                      ),
    Chat_ClickGlobalSearchResult                (Category.CHAT, "ClickGlobalSearchResult"                   ),
    Chat_CreateChatroomSuccess                  (Category.CHAT, "CreateChatroomSuccess"                     ),
    Chat_Share                                  (Category.CHAT, "Share"                                     ),

    Profile_OwnProfileDropdownBuyCredits        (Category.PROFILE, "OwnProfileDropdownBuyCredits"           ),
    Profile_OwnProfileDropdownSettings          (Category.PROFILE, "OwnProfileDropdownSettings"             ),
    Profile_OtherProfileDropdownSendGift        (Category.PROFILE, "OtherProfileDropdownSendGift"           ),
    Profile_OtherProfileDropdownTransferCredits (Category.PROFILE, "OtherProfileDropdownTransferCredits"    ),
    Profile_OtherProfileDropdownMention         (Category.PROFILE, "OtherProfileDropdownMention"            ),
    Profile_OtherProfileDropdownReport          (Category.PROFILE, "OtherProfileDropdownReport"             ),
    Profile_OtherProfileDropdownBlock           (Category.PROFILE, "OtherProfileDropdownBlock"              ),
    Profile_OtherProfileDropdownUnfriend        (Category.PROFILE, "OtherProfileDropdownUnfriend"           ),
    Profile_OtherProfileButtonSendGift          (Category.PROFILE, "OtherProfileButtonSendGift"             ),
    Profile_OtherProfileButtonFollow            (Category.PROFILE, "OtherProfileButtonFollow"               ),
    Profile_OtherProfileButtonChat              (Category.PROFILE, "OtherProfileButtonChat"                 ),
    Profile_FanListFollow                       (Category.PROFILE, "FanListFollow"                          ),
    Profile_FanListSendGift                     (Category.PROFILE, "FanListSendGift"                        ),
    Profile_FanListChat                         (Category.PROFILE, "FanListChat"                            ),
    Profile_FanOfListFollow                     (Category.PROFILE, "FanOfListFollow"                        ),
    Profile_FanOfListSendGift                   (Category.PROFILE, "FanOfListSendGift"                      ),
    Profile_FanOfListChat                       (Category.PROFILE, "FanOfListChat"                          ),
    Profile_OtherProfileAbout                   (Category.PROFILE, "OtherProfileAbout"                      ),
    Profile_OwnProfileAbout                     (Category.PROFILE, "OwnProfileAbout"                        ),
    Profile_GiftList                            (Category.PROFILE, "GiftList"                               ),
    Profile_BadgeList                           (Category.PROFILE, "BadgeList"                              ),
    Profile_FanList                             (Category.PROFILE, "FanList"                                ),
    Profile_FanOfList                           (Category.PROFILE, "FanOfList"                              ),
    Profile_ChatroomList                        (Category.PROFILE, "ChatroomList"                           ),
    Profile_PhotoList                           (Category.PROFILE, "PhotoList"                              ),
    Profile_GameList                            (Category.PROFILE, "GameList"                               ),
    Profile_MentionList                         (Category.PROFILE, "MentionList"                            ),
    Profile_FavoriteList                        (Category.PROFILE, "FavoriteList"                           ),
    Profile_Share                               (Category.PROFILE, "Share"                                  ),

    Miniprofile_Follow                          (Category.MINIPROFILE, "Follow"                             ),
    Miniprofile_IsFanSendGift                   (Category.MINIPROFILE, "IsFanSendGift"                      ),
    Miniprofile_StartChat                       (Category.MINIPROFILE, "StartChat"                          ),
    Miniprofile_DropdownSendGift                (Category.MINIPROFILE, "DropdownSendGift"                   ),
    Miniprofile_DropdownTransferCredits         (Category.MINIPROFILE, "DropdownTransferCredits"            ),
    Miniprofile_DropdownMention                 (Category.MINIPROFILE, "DropdownMention"                    ),
    Miniprofile_DropdownReport                  (Category.MINIPROFILE, "DropdownReport"                     ),
    Miniprofile_DropdownBlock                   (Category.MINIPROFILE, "DropdownBlock"                      ),
    Miniprofile_DropdownUnfriend                (Category.MINIPROFILE, "DropdownUnfriend"                   ),
    Miniprofile_GiftList                        (Category.MINIPROFILE, "GiftList"                           ),
    Miniprofile_BadgeList                       (Category.MINIPROFILE, "BadgeList"                          ),
    Miniprofile_FanList                         (Category.MINIPROFILE, "FanList"                            ),
    Miniprofile_MainActionSendGift              (Category.MINIPROFILE, "MainActionSendGift"                 ),
    Miniprofile_MainActionStartChat             (Category.MINIPROFILE, "MainActionStartChat"                ),

    Miniblog_CreatePost                         (Category.MINIBLOG, "CreatePost"                            ),
    Miniblog_CreatePostSuccess                  (Category.MINIBLOG, "CreatePostSuccess"                     ),
    Miniblog_CreatePostShareFacebook            (Category.MINIBLOG, "CreatePostShareFacebook"               ),
    Miniblog_CreatePostShareTwitter             (Category.MINIBLOG, "CreatePostShareTwitter"                ),
    Miniblog_CreatePostAddLocation              (Category.MINIBLOG, "CreatePostAddLocation"                 ),
    Miniblog_CreatePostAddImage                 (Category.MINIBLOG, "CreatePostAddImage"                    ),
    Miniblog_CreatePostImageSuccess             (Category.MINIBLOG, "CreatePostImageSuccess"                ),
    Miniblog_CreatePostPrivacySettings          (Category.MINIBLOG, "CreatePostPrivacySettings"             ),
    Miniblog_CreatePostMention                  (Category.MINIBLOG, "CreatePostMention"                     ),
    Miniblog_CreatePostHashtag                  (Category.MINIBLOG, "CreatePostHashtag"                     ),
    Miniblog_CreatePostEmoticon                 (Category.MINIBLOG, "CreatePostEmoticon"                    ),
    Miniblog_MainButtonCreatePost               (Category.MINIBLOG, "MainButtonCreatePost"                  ),
    Miniblog_MainButtonSearch                   (Category.MINIBLOG, "MainButtonSearch"                      ),
    Miniblog_MainButtonPhotoPost                (Category.MINIBLOG, "MainButtonPhotoPost"                   ),
    Miniblog_FeedsReplyClick                    (Category.MINIBLOG, "ReplyClick"                            ),
    Miniblog_ReplySuccess                       (Category.MINIBLOG, "ReplySuccess"                          ),
    Miniblog_FeedsRepostClick                   (Category.MINIBLOG, "RepostClick"                           ),
    Miniblog_RepostSuccess                      (Category.MINIBLOG, "RepostSuccess"                         ),
    Miniblog_EmotionFootprint                   (Category.MINIBLOG, "EmotionFootprint"                      ),
    Miniblog_ClickPostResult                    (Category.MINIBLOG, "ClickPostResult"                       ),
    Miniblog_ClickPeopleResult                  (Category.MINIBLOG, "ClickPeopleResult"                     ),
    Miniblog_DiscoverBanner                     (Category.MINIBLOG, "DiscoverBanner"                        ),
    Miniblog_Share                              (Category.MINIBLOG, "Share"                                 ),

    SinglePostPage_ReplyClick                   (Category.SINGLE_POST_PAGE, "ReplyClick"                    ),
    SinglePostPage_RepostClick                  (Category.SINGLE_POST_PAGE, "RepostClick"                   ),
    SinglePostPage_EmotionFootprint             (Category.SINGLE_POST_PAGE, "EmotionFootprint"              ),
    SinglePostPage_Share                        (Category.SINGLE_POST_PAGE, "Share"                         ),

    Notification_ChatNewFriends                 (Category.NOTIFICATION, "ChatNewFriends"                    ),
    Notification_ClickChatMessages              (Category.NOTIFICATION, "ClickChatMessages"                 ),
    Notification_Refresh                        (Category.NOTIFICATION, "Refresh"                           ),
    Notification_ClickGroupHeader               (Category.NOTIFICATION, "ClickGroupHeader"                  ),
    Notification_GroupInviteAccept              (Category.NOTIFICATION, "GroupInviteAccept"                 ),
    Notification_GroupInviteReject              (Category.NOTIFICATION, "GroupInviteReject"                 ),
    Notification_ViewGift                       (Category.NOTIFICATION, "ViewGift"                          ),
    Notification_GiftBack                       (Category.NOTIFICATION, "GiftBack"                          ),
    Notification_GameInviteAccept               (Category.NOTIFICATION, "GameInviteAccept"                  ),
    Notification_GameInviteReject               (Category.NOTIFICATION, "GameInviteReject"                  ),
    Notification_ViewBadges                     (Category.NOTIFICATION, "ViewBadges"                        ),
    Notification_FollowRequestAccept            (Category.NOTIFICATION, "FollowRequestAccept"               ),
    Notification_FollowRequestReject            (Category.NOTIFICATION, "FollowRequestReject"               ),
    Notification_LevelUpMoreAbout               (Category.NOTIFICATION, "LevelUpMoreAbout"                  ),
    Notification_ViewReply                      (Category.NOTIFICATION, "ViewReply"                         ),
    Notification_ViewAccountCredits             (Category.NOTIFICATION, "ViewAccountCredits"                ),
    Notification_GiftNewFriend                  (Category.NOTIFICATION, "GiftNewFriends"                    ),
    Notification_ViewMention                    (Category.NOTIFICATION, "ViewMention"                       ),

    LeftPanel_ChangePresence                    (Category.LEFT_PANEL, "ChangePresence"                      ),
    LeftPanel_EditStatus                        (Category.LEFT_PANEL, "EditStatus"                          ),
    LeftPanel_GoProfile                         (Category.LEFT_PANEL, "GoProfile"                           ),
    LeftPanel_GiftList                          (Category.LEFT_PANEL, "GiftList"                            ),
    LeftPanel_BadgeList                         (Category.LEFT_PANEL, "BadgeList"                           ),
    LeftPanel_FanList                           (Category.LEFT_PANEL, "FanList"                             ),
    LeftPanel_Game                              (Category.LEFT_PANEL, "Game"                                ),
    LeftPanel_Music                             (Category.LEFT_PANEL, "Music"                               ),
    LeftPanel_Store                             (Category.LEFT_PANEL, "Store"                               ),
    LeftPanel_Settings                          (Category.LEFT_PANEL, "Settings"                            ),
    LeftPanel_Banner                            (Category.LEFT_PANEL, "Banner"                              ),
    LeftPanel_DiscoverBanner                    (Category.LEFT_PANEL, "DiscoverBanner"                      ),
    LeftPanel_InviteBanner                      (Category.LEFT_PANEL, "InviteBanner"                        ),

    Settings_Privacy                            (Category.SETTINGS, "Privacy"                               ),
    Settings_System                             (Category.SETTINGS, "System"                                ),
    Settings_AccountSettings                    (Category.SETTINGS, "AccountSettings"                       ),
    Settings_MyAccount                          (Category.SETTINGS, "MyAccount"                             ),
    Settings_ManageIM                           (Category.SETTINGS, "ManageIM"                              ),
    Settings_ThirdPartySites                    (Category.SETTINGS, "ThirdPartySites"                       ),
    Settings_Application                        (Category.SETTINGS, "Application"                           ),
    Settings_AboutMigMe                         (Category.SETTINGS, "AboutMigMe"                            ),
    Settings_Logout                             (Category.SETTINGS, "Logout"                                ),
    Settings_Version                            (Category.SETTINGS, "Version"                               ),
    Settings_Services                           (Category.SETTINGS, "Services"                              ),
    Settings_ClearImageCache                    (Category.SETTINGS, "ClearImageCache"                       ),
    Settings_Language                           (Category.SETTINGS, "Language"                              ),
    Settings_ChatNotification                   (Category.SETTINGS, "ChatNotification"                      ),
    Settings_Sound                              (Category.SETTINGS, "Sound"                                 ),
    Settings_Vibrate                            (Category.SETTINGS, "Vibrate"                               ),

    Store_ClickUnlockGiftPage                   (Category.STORE, "ClickUnlockGiftPage"                      ),
    Store_GiftCategoryList                      (Category.STORE, "GiftCategoryList"                         ),
    Store_GiftSortList                          (Category.STORE, "GiftSortList"                             ),
    Store_GiftSortPopularity                    (Category.STORE, "GiftSortPopularity"                       ),
    Store_GiftSortLowToHigh                     (Category.STORE, "GiftSortLowToHigh"                        ),
    Store_GiftSortHighToLow                     (Category.STORE, "GiftSortHighToLow"                        ),
    Store_GiftSortAToZ                          (Category.STORE, "GiftSortAToZ"                             ),
    Store_GiftSortZToA                          (Category.STORE, "GiftSortZToA"                             ),
    Store_SendGift                              (Category.STORE, "SendGift"                                 ),
    Store_SearchGift                            (Category.STORE, "SearchGift"                               ),
    Store_SendUnlockGift                        (Category.STORE, "SendUnlockGift"                           ),
    Store_ClickMyStickerPage                    (Category.STORE, "ClickMyStickerPage"                       ),
    Store_StickerCategoryList                   (Category.STORE, "StickerCategoryList"                      ),
    Store_StickerSortList                       (Category.STORE, "StickerSortList"                          ),
    Store_StickerSortPopularity                 (Category.STORE, "StickerSortPopularity"                    ),
    Store_StickerSortLowToHigh                  (Category.STORE, "StickerSortLowToHigh"                     ),
    Store_StickerSortHighToLow                  (Category.STORE, "StickerSortHighToLow"                     ),
    Store_StickerSortAToZ                       (Category.STORE, "StickerSortAToZ"                          ),
    Store_StickerSortZToA                       (Category.STORE, "StickerSortZToA"                          ),
    Store_PurchaseSticker                       (Category.STORE, "PurchaseSticker"                          ),
    Store_SearchSticker                         (Category.STORE, "SearchSticker"                            ),
    Store_TurnOnSticker                         (Category.STORE, "TurnOnSticker"                            ),
    Store_TurnOffSticker                        (Category.STORE, "TurnOffSticker"                           ),
    Store_CheckMyCredits                        (Category.STORE, "CheckMyCredits"                           ),
    Store_VisitEmoticonFragment                 (Category.STORE, "VisitEmoticonFragment"                    ),
    Store_VisitAvatarFragment                   (Category.STORE, "VisitAvatarFragment"                      ),
    
    Deezer_LandingFragment                      (Category.DEEZER, "PlayRadio"                               ),
    Deezer_ShareToChat                          (Category.DEEZER, "ShareToChat"                             ),
    Deezer_ShareToPost                          (Category.DEEZER, "ShareToPost"                             ),
    Deezer_Share                                (Category.DEEZER, "Share"                                   ),
    Deezer_PinToChat                            (Category.DEEZER, "PinToChat"                               ),
    
    Signin_Land                                 (Category.SIGNIN, "Land"                                    ),
    Signin_Click                                (Category.SIGNIN, "Click"                                   ),
    Signin_Failure                              (Category.SIGNIN, "Failure"                                 ),
    Signin_Failure_AccountBanned                (Category.SIGNIN, "AccountBanned"                           ),
    Signin_Success                              (Category.SIGNIN, "Success"                                 ),
    Signin_ForgetPassword                       (Category.SIGNIN, "ForgetPassword"                          ),
    Signin_SwitchSignUp                         (Category.SIGNIN, "SwitchSignUp"                            ),
    Signin_Facebook                             (Category.SIGNIN, "Facebook"                                ),
    Signin_PeekInside                           (Category.SIGNIN, "PeekInside"                              ),
    Signin_ShowPassword                         (Category.SIGNIN, "ShowPassword"                            ),
    Signin_Timing_Login                         (Category.SIGNUP, "LoginPeriod"                             ),

    SignUp_UsernameSuccess                      (Category.SIGNUP, "UsernameSuccess"                         ),
    SignUp_UsernameFailure                      (Category.SIGNUP, "UsernameFailure"                         ),
    SignUp_UsernameRepeatFailure                (Category.SIGNUP, "UsernameRepeatFailure"                   ),
    SignUp_Facebook                             (Category.SIGNUP, "Facebook"                                ),
    SignUp_PeekInside                           (Category.SIGNUP, "PeekInside"                              ),
    SignUp_Term                                 (Category.SIGNUP, "Term"                                    ),
    SignUp_Privacy                              (Category.SIGNUP, "Privacy"                                 ),
    SignUp_EmailSuccess                         (Category.SIGNUP, "EmailSuccess"                            ),
    SignUp_EmailFailure                         (Category.SIGNUP, "EmailFailure"                            ),
    SignUp_PasswordSuccess                      (Category.SIGNUP, "PasswordSuccess"                         ),
    SignUp_PasswordFailure                      (Category.SIGNUP, "PasswordFailure"                         ),
    SignUp_PasswordRepeatFailure                (Category.SIGNUP, "PasswordRepeatFailure"                   ),
    SignUp_PasswordHide                         (Category.SIGNUP, "PasswordHide"                            ),
    SignUp_PasswordForget                       (Category.SIGNUP, "PasswordForget"                          ),
    SignUp_CaptchaSuccess                       (Category.SIGNUP, "CaptchaSuccess"                          ),
    SignUp_CaptchaFailure                       (Category.SIGNUP, "CaptchaFailure"                          ),
    SignUp_CaptchaRepeatFailure                 (Category.SIGNUP, "CaptchaRepeatFailure"                    ),
    SignUp_CaptchaRefresh                       (Category.SIGNUP, "CaptchaRefresh"                          ),
    SignUp_ConfirmExploreMig                    (Category.SIGNUP, "ConfirmExploreMig"                       ),
    Signup_ComfirmCheckEmail                    (Category.SIGNUP, "ComfirmCheckEmail"                       ),
    Signup_ComfirmResendEmail                   (Category.SIGNUP, "ComfirmResendEmail"                      ),
    Signup_Facebook_UsernameSuccess             (Category.FACBOOK_SIGNUP, "FacebookUsernameSuccess"         ),
    Signup_Facebook_UsernameFailure             (Category.FACBOOK_SIGNUP, "FacebookUsernameFailure"         ),
    Signup_Facebook_PasswordSuccess             (Category.FACBOOK_SIGNUP, "FacebookPasswordSuccess"         ),
    Signup_Facebook_PasswordFailure             (Category.FACBOOK_SIGNUP, "FacebookPasswordFailure"         ),
    Signup_ErrorHandle_UsernameSuccess          (Category.SIGNUP, "ErrorHandleUsernameSuccess"              ),
    Signup_ErrorHandle_UsernameErrorType        (Category.SIGNUP, "ErrorHandleUsernameErrorType"            ),
    Signup_ErrorHandle_UsernameProceed          (Category.SIGNUP, "ErrorHandleUsernameProceed"              ),
    Signup_ErrorHandle_UsernameTerminate        (Category.SIGNUP, "ErrorHandleUsernameTerminate"            ),
    Signup_ErrorHandle_EmailSuccess             (Category.SIGNUP, "ErrorHandleEmailSuccess"                 ),
    Signup_ErrorHandle_EmailErrorType           (Category.SIGNUP, "ErrorHandleEmailErrorType"               ),
    Signup_ErrorHandle_EmailProceed             (Category.SIGNUP, "ErrorHandleEmailProceed"                 ),
    Signup_ErrorHandle_EmailTerminate           (Category.SIGNUP, "ErrorHandleEmailTerminate"               ),
    Signup_Verify_ExpiredResend                 (Category.SIGNUP, "VerifyExpiredResend"                     ),
    Signup_Verify_FailRetrySuccess              (Category.SIGNUP, "VerifyFailRetrySuccess"                  ),
    Signup_Verify_FailRetryFailure              (Category.SIGNUP, "VerifyFailRetryFailure"                  ),
    Signup_Verify_FailResend                    (Category.SIGNUP, "VerifyFailResend"                        ),
    Signup_Verify_Success                       (Category.SIGNUP, "VerifySuccess"                           ),
    Signup_API_TimeOut                          (Category.SIGNUP, "APITimeOut"                              ),
    Signup_Undistinguishable_Error              (Category.SIGNUP, "UndistinguishableError"                  ),
    Singup_EmailVerifyRedirection               (Category.SIGNUP, "EmailVerifyRedirection"                  ),
    Signup_Timing_Validate_Username             (Category.SIGNUP, "UsernameCheckTimePeriod"                 ),
    Signup_Timing_Validate_Email                (Category.SIGNUP, "EmailCheckTimePeriod"                    ),
    Signup_Timing_Resend_email                  (Category.SIGNUP, "ResendEmailTimePeriod"                   ),
    Signup_Timing_CreateUser                    (Category.SIGNUP, "CreateUserTimePeriod"                    ),
    Signup_Timing_Verifying                     (Category.SIGNUP, "VerifyingEmailPeriod"                    ),
    Signup_Timing_Captcha                       (Category.SIGNUP, "GenerateCaptchaPeriod"                   ),
    Signup_Create_User_Success                  (Category.SIGNUP, "CreateUserSuccess"                       ),
    Signup_Facebook_Success                     (Category.SIGNUP, "FacebookSignupSuccess"                   ),
    GameCenter_ClickGameDetail                  (Category.GAME_CENTER, "ClickGameDetail"                    ),
    GameCenter_ClickChatRoomGame                (Category.GAME_CENTER, "ClickChatRoomGame"                  ),
    GameDetail_ClickPlayGame                    (Category.GAME_DETAIL, "ClickPlayGame"                      )

    ;

    private enum Category {
        CHAT            ("Chat"),
        PROFILE         ("Profile"),
        MINIPROFILE     ("Miniprofile"),
        MINIBLOG        ("Miniblog"),
        SINGLE_POST_PAGE("SinglePostPage"),
        NOTIFICATION    ("Notification"),
        LEFT_PANEL      ("LeftPanel"),
        SETTINGS        ("Settings"),
        STORE           ("Store"),
        SIGNIN          ("Signin"),
        SIGNUP          ("SignUp"),
        DEEZER          ("Deezer"),
        GAME_CENTER     ("GameCenter"),
        FACBOOK_SIGNUP  ("FacebookSignup"),
        GAME_DETAIL     ("GameDetail")
        ;

        public String name;

        Category(String name) {
            this.name = name;
        }
    }

    private Category    category;
    private String      action;
    private long        beginTime = -1;

    GAEvent(Category category, String action) {
        this.category = category;
        this.action = action;
    }

    public void send() {
        Tracker tracker = ApplicationEx.getTracker();
        if (tracker == null) return;
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(category.name)
                .setAction(action)
                .build());
    }

    public void send(String label) {
        Tracker tracker = ApplicationEx.getTracker();
        if (tracker == null) return;
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(category.name)
                .setAction(action)
                .setLabel(label)
                .build());
    }

    public void send(long value) {
        Tracker tracker = ApplicationEx.getTracker();
        if (tracker == null) return;
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(category.name)
                .setAction(action)
                .setValue(value)
                .build());
    }

    public void send(String label, long value) {
        Tracker tracker = ApplicationEx.getTracker();
        if (tracker == null) return;
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(category.name)
                .setAction(action)
                .setLabel(label)
                .setValue(value)
                .build());
    }

    public void sendTiming(String label, long time) {
        Tracker tracker = ApplicationEx.getTracker();
        if (tracker == null) return;
        tracker.send(new HitBuilders.TimingBuilder()
                .setCategory(category.name)
                .setValue(time)
                .setVariable(action)
                .setLabel(label)
                .build());
    }

    public void sendWithErrorCode(String label, short errorCode) {
        Tracker tracker = ApplicationEx.getTracker();
        if (tracker == null) return;
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(category.name)
                .setAction(action+"_"+errorCode)
                .setLabel(label)
                .build());
    }

    public void begin() {
        beginTime = System.currentTimeMillis();
    }

    public void end() {
        Tracker tracker = ApplicationEx.getTracker();
        if (tracker == null) return;
        if (beginTime == -1) {
            Logger.error.flog("end() called without matching begin() for event: %s.%s", category.name, action);
            return;
        }
        long duration = System.currentTimeMillis() - beginTime;
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(category.name)
                .setAction(action)
                .setValue(duration)
                .build());

        beginTime = -1;
    }

}
