/**
 * Copyright (c) 2013 Project Goth
 *
 * BaseDialogFragment.java
 * Created Aug 20, 2013, 12:07:42 PM
 */

package com.projectgoth.ui.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.projectgoth.R;

/**
 * This is based on the {@link DialogFragment} source code that comes with the
 * compatibility support package. It has been modified to extend from
 * {@link BaseFragment} instead of {@link DialogFragment} directly.
 * 
 * Another modification here is to set this DialogFragment as embeddable (not
 * displayed as dialog) by default. This is actually a fix as well for the bug
 * in pre-Honeycomb devices wherein an IllegalStateException error is thrown
 * when setting this DialogFragment as embedded instead of as dialog. Please
 * refer to
 * http://stackoverflow.com/questions/5637894/dialogfragments-with-devices
 * -api-level-11 for more info.
 * 
 * Also pre-set style and theme to no-frame and to use the DialogFragment style
 * by default
 * 
 * @author cherryv
 * 
 */
public abstract class BaseDialogFragment extends BaseFragment implements DialogInterface.OnCancelListener,
        DialogInterface.OnDismissListener {

    /**
     * Style for {@link #setStyle(int, int)}: a basic, normal dialog.
     */
    public static final int     STYLE_NORMAL                  = 0;

    /**
     * Style for {@link #setStyle(int, int)}: don't include a title area.
     */
    public static final int     STYLE_NO_TITLE                = 1;

    /**
     * Style for {@link #setStyle(int, int)}: don't draw any frame at all; the
     * view hierarchy returned by {@link #onCreateView} is entirely responsible
     * for drawing the dialog.
     */
    public static final int     STYLE_NO_FRAME                = 2;

    /**
     * Style for {@link #setStyle(int, int)}: like {@link #STYLE_NO_FRAME}, but
     * also disables all input to the dialog. The user can not touch it, and its
     * window will not receive input focus.
     */
    public static final int     STYLE_NO_INPUT                = 3;

    private static final String SAVED_DIALOG_STATE_TAG        = "android:savedDialogState";
    private static final String SAVED_STYLE                   = "android:style";
    public static final String  SAVED_THEME                   = "android:theme";
    public static final String  FROM_DEEZER                   = "android:deezer";
    public static final String  SAVED_GRAVITY                 = "android:gravity";
    private static final String SAVED_CANCELABLE              = "android:cancelable";
    private static final String SAVED_SHOWS_DIALOG            = "android:showsDialog";
    public static final String  SAVED_IS_TRANSPARENT          = "android:isTransparent";
    public static final String  SAVED_SHOULD_DISMISS_ON_TOUCH = "android:shouldDismissOnTouch";
    public static final String  SAVED_DIMISS_TOUCH_OUTSIDE    = "android:dimissTouchOutside";
    private static final String SAVED_BACK_STACK_ID           = "android:backStackId";
    public static final String  IS_POSITIVE_ALERT             = "android:positiveAlert";

    int                         mStyle                        = STYLE_NORMAL;
    int                         mGravity                      = Gravity.CENTER;
    int                         mTheme                        = 0;
    boolean                     mCancelable                   = true;
    boolean                     mShowsDialog                  = false;
    int                         mBackStackId                  = -1;

    OnClickListener             mOnClickListener              = null;
    Dialog                      mDialog;
    boolean                     mDestroyed;
    boolean                     mRemoved;
    boolean                     mDismissTouchOutside          = false;

    // Whether the background of the dialog is transparent or not.
    boolean                     mIsTransparent                = false;
    // Whether the dialog should automatically be dismissed on touch.
    boolean                     mShouldDismissOnTouch         = false;
    boolean                     mIsPositiveAlert              = false;
    int                         mPositiveAlertDismissMS       = 6000;

    public interface DialogDismissListener {
        public void onDismiss();
    }

    DialogDismissListener mDialogDismissListener;
    public void setOnDialogDismissListener(DialogDismissListener dialogDismissListener) {
        mDialogDismissListener = dialogDismissListener;
    }

    /**
     * Call to customize the basic appearance and behavior of the fragment's
     * dialog. This can be used for some common dialog behaviors, taking care of
     * selecting flags, theme, and other options for you. The same effect can be
     * achieve by manually setting Dialog and Window attributes yourself.
     * Calling this after the fragment's Dialog is created will have no effect.
     * 
     * @param style
     *            Selects a standard style: may be {@link #STYLE_NORMAL},
     *            {@link #STYLE_NO_TITLE}, {@link #STYLE_NO_FRAME}, or
     *            {@link #STYLE_NO_INPUT}.
     * @param theme
     *            Optional custom theme. If 0, an appropriate theme (based on
     *            the style) will be selected for you.
     */
    public void setStyle(int style, int theme) {
        mStyle = style;
        if (mStyle == STYLE_NO_FRAME || mStyle == STYLE_NO_INPUT) {
            mTheme = android.R.style.Theme_Panel;
        }
        if (theme != 0) {
            mTheme = theme;
        }

    }

    /**
     * Display the dialog, adding the fragment to the given FragmentManager.
     * This is a convenience for explicitly creating a transaction, adding the
     * fragment to it with the given tag, and committing it. This does
     * <em>not</em> add the transaction to the back stack. When the fragment is
     * dismissed, a new transaction will be executed to remove it from the
     * activity.
     * 
     * @param manager
     *            The FragmentManager this fragment will be added to.
     * @param tag
     *            The tag for this fragment, as per
     *            {@link FragmentTransaction#add(Fragment, String)
     *            FragmentTransaction.add}.
     */
    public void show(FragmentManager manager, String tag) {
        this.setShowsDialog(true);
        FragmentTransaction ft = manager.beginTransaction();
        ft.add(this, tag);
        ft.commit();
    }

    /**
     * Display the dialog, adding the fragment using an existing transaction and
     * then committing the transaction.
     * 
     * @param transaction
     *            An existing transaction in which to add the fragment.
     * @param tag
     *            The tag for this fragment, as per
     *            {@link FragmentTransaction#add(Fragment, String)
     *            FragmentTransaction.add}.
     * @return Returns the identifier of the committed transaction, as per
     *         {@link FragmentTransaction#commit() FragmentTransaction.commit()}
     *         .
     */
    public int show(FragmentTransaction transaction, String tag) {
        this.setShowsDialog(true);
        transaction.add(this, tag);
        mRemoved = false;
        mBackStackId = transaction.commit();
        return mBackStackId;
    }

    /**
     * Dismiss the fragment and its dialog. If the fragment was added to the
     * back stack, all back stack state up to and including this entry will be
     * popped. Otherwise, a new transaction will be committed to remove the
     * fragment.
     */
    public void dismiss() {
        dismissInternal(false);
    }

    void dismissInternal(boolean allowStateLoss) {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
        mRemoved = true;
        if (mBackStackId >= 0) {
            getFragmentManager().popBackStack(mBackStackId, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            mBackStackId = -1;
        } else {
            // null check for [#59105636]
            FragmentManager fragmentManager = getFragmentManager();
            if (fragmentManager != null) {
                FragmentTransaction ft = fragmentManager.beginTransaction();
                ft.remove(this);
                if (allowStateLoss) {
                    ft.commitAllowingStateLoss();
                } else {
                    ft.commit();
                }
            }
        }
    }

    public Dialog getDialog() {
        return mDialog;
    }

    public int getTheme() {
        return mTheme;
    }

    /**
     * Control whether the shown Dialog is cancelable. Use this instead of
     * directly calling {@link Dialog#setCancelable(boolean)
     * Dialog.setCancelable(boolean)}, because DialogFragment needs to change
     * its behavior based on this.
     * 
     * @param cancelable
     *            If true, the dialog is cancelable. The default is true.
     */
    public void setCancelable(boolean cancelable) {
        mCancelable = cancelable;
        if (mDialog != null) {
            mDialog.setCancelable(cancelable);
        }
    }

    /**
     * Return the current value of {@link #setCancelable(boolean)}.
     */
    public boolean isCancelable() {
        return mCancelable;
    }

    /**
     * Controls whether this fragment should be shown in a dialog. If not set,
     * no Dialog will be created in {@link #onActivityCreated(Bundle)}, and the
     * fragment's view hierarchy will thus not be added to it. This allows you
     * to instead use it as a normal fragment (embedded inside of its activity).
     * 
     * <p>
     * This is normally set for you based on whether the fragment is associated
     * with a container view ID passed to
     * {@link FragmentTransaction#add(int, Fragment)
     * FragmentTransaction.add(int, Fragment)}. If the fragment was added with a
     * container, setShowsDialog will be initialized to false; otherwise, it
     * will be true.
     * 
     * @param showsDialog
     *            If true, the fragment will be displayed in a Dialog. If false,
     *            no Dialog will be created and the fragment's view hierarchly
     *            left undisturbed.
     */
    public void setShowsDialog(boolean showsDialog) {
        mShowsDialog = showsDialog;
    }

    /**
     * Return the current value of {@link #setShowsDialog(boolean)}.
     */
    public boolean getShowsDialog() {
        return mShowsDialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle args = getArguments();

        if (savedInstanceState != null) {
            mStyle = savedInstanceState.getInt(SAVED_STYLE, STYLE_NORMAL);
            mGravity = savedInstanceState.getInt(SAVED_GRAVITY,Gravity.CENTER);
            mTheme = savedInstanceState.getInt(SAVED_THEME, 0);
            mCancelable = savedInstanceState.getBoolean(SAVED_CANCELABLE, true);
            mShowsDialog = savedInstanceState.getBoolean(SAVED_SHOWS_DIALOG, mShowsDialog);
            mIsTransparent = savedInstanceState.getBoolean(SAVED_IS_TRANSPARENT, mIsTransparent);
            mShouldDismissOnTouch = savedInstanceState.getBoolean(SAVED_SHOULD_DISMISS_ON_TOUCH, mShouldDismissOnTouch);
            mBackStackId = savedInstanceState.getInt(SAVED_BACK_STACK_ID, -1);
            mDismissTouchOutside = savedInstanceState.getBoolean(SAVED_DIMISS_TOUCH_OUTSIDE, mDismissTouchOutside);
        }

        if (args != null) {
            mIsTransparent = args.getBoolean(SAVED_IS_TRANSPARENT, mIsTransparent);
            mShouldDismissOnTouch = args.getBoolean(SAVED_SHOULD_DISMISS_ON_TOUCH, mShouldDismissOnTouch);
            mDismissTouchOutside = args.getBoolean(SAVED_DIMISS_TOUCH_OUTSIDE, mDismissTouchOutside);
            mGravity = args.getInt(SAVED_GRAVITY,Gravity.CENTER);
            mTheme = args.getInt(SAVED_THEME);
            mIsPositiveAlert = args.getBoolean(IS_POSITIVE_ALERT, false);

        }

        if (mTheme > 0) {
            setStyle(STYLE_NO_FRAME, mTheme);
        } else {
            if (mIsTransparent) {
                setStyle(STYLE_NO_FRAME, R.style.DialogFragment_Transparent);
            } else {
                setStyle(STYLE_NO_FRAME, R.style.DialogFragment);
            }
        }

    }

    /** @hide */
    @Override
    public LayoutInflater getLayoutInflater(Bundle savedInstanceState) {
        if (!mShowsDialog) {
            return super.getLayoutInflater(savedInstanceState);
        }

        mDialog = onCreateDialog(savedInstanceState);
        mDestroyed = false;
        switch (mStyle) {
            case STYLE_NO_INPUT:
                mDialog.getWindow().addFlags(
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                // fall through...
            case STYLE_NO_FRAME:
            case STYLE_NO_TITLE:
                mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        return (LayoutInflater) mDialog.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * Override to build your own custom Dialog container. This is typically
     * used to show an AlertDialog instead of a generic Dialog; when doing so,
     * {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)} does not need to
     * be implemented since the AlertDialog takes care of its own content.
     * 
     * <p>
     * This method will be called after {@link #onCreate(Bundle)} and before
     * {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}. The default
     * implementation simply instantiates and returns a {@link Dialog} class.
     * 
     * <p>
     * <em>Note: DialogFragment own the {@link Dialog#setOnCancelListener
     * Dialog.setOnCancelListener} and {@link Dialog#setOnDismissListener
     * Dialog.setOnDismissListener} callbacks.  You must not set them yourself.</em>
     * To find out about these events, override
     * {@link #onCancel(DialogInterface)} and
     * {@link #onDismiss(DialogInterface)}.
     * </p>
     * 
     * @param savedInstanceState
     *            The last saved instance state of the Fragment, or null if this
     *            is a freshly created Fragment.
     * 
     * @return Return a new Dialog instance to be displayed by the Fragment.
     */

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity(), getTheme());
        dialog.getWindow().setGravity(mGravity);
        if (mIsPositiveAlert) {
            dialog.getWindow().setWindowAnimations(R.style.notify_dialog_animation);
        }
        return dialog;
    }

    public void onCancel(DialogInterface dialog) {
    }

    public void onDismiss(DialogInterface dialog) {
        if (!mRemoved) {
            // Note: we need to use allowStateLoss, because the dialog
            // dispatches this asynchronously so we can receive the call
            // after the activity is paused. Worst case, when the user comes
            // back to the activity they see the dialog again.
            dismissInternal(true);
        }
        if (mDialogDismissListener != null) {
            mDialogDismissListener.onDismiss();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (!mShowsDialog) {
            return;
        }

        View view = getView();
        if (view != null) {
            if (view.getParent() != null) {
                throw new IllegalStateException("DialogFragment can not be attached to a container view");
            }
            mDialog.setContentView(view);
            view.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (mOnClickListener != null) {
                        mOnClickListener.onClick(v);
                    }

                    if (mShouldDismissOnTouch) {
                        dismiss();
                    }
                }
            });
        }

        mDialog.setOwnerActivity(getActivity());
        mDialog.setCancelable(mCancelable);
        mDialog.setOnCancelListener(this);
        mDialog.setOnDismissListener(this);

        if (mDismissTouchOutside) {
            mDialog.setCanceledOnTouchOutside(true);
        }

        if (mIsPositiveAlert) {
            mDialog.setCancelable(true);
            mDialog.setCanceledOnTouchOutside(true);
            mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            mDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mDialog != null) {
                        mDialog.dismiss();
                    }
                }
            }, mPositiveAlertDismissMS);
        }

        if (savedInstanceState != null) {
            Bundle dialogState = savedInstanceState.getBundle(SAVED_DIALOG_STATE_TAG);
            if (dialogState != null) {
                mDialog.onRestoreInstanceState(dialogState);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mDialog != null) {
            mRemoved = false;
            mDialog.show();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mDialog != null) {
            Bundle dialogState = mDialog.onSaveInstanceState();
            if (dialogState != null) {
                outState.putBundle(SAVED_DIALOG_STATE_TAG, dialogState);
            }
        }
        if (mStyle != STYLE_NORMAL) {
            outState.putInt(SAVED_STYLE, mStyle);
        }
        if (mGravity != Gravity.CENTER) {
            outState.putInt(SAVED_GRAVITY, mGravity);
        }
        if (mTheme != 0) {
            outState.putInt(SAVED_THEME, mTheme);
        }
        if (!mCancelable) {
            outState.putBoolean(SAVED_CANCELABLE, mCancelable);
        }
        if (mShowsDialog) {
            outState.putBoolean(SAVED_SHOWS_DIALOG, mShowsDialog);
        }
        if (mIsTransparent) {
            outState.putBoolean(SAVED_IS_TRANSPARENT, mIsTransparent);
        }
        if (mShouldDismissOnTouch) {
            outState.putBoolean(SAVED_SHOULD_DISMISS_ON_TOUCH, mShouldDismissOnTouch);
        }
        if (mBackStackId != -1) {
            outState.putInt(SAVED_BACK_STACK_ID, mBackStackId);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mDialog != null) {
            mDialog.hide();
        }
    }

    /**
     * Remove dialog.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mDestroyed = true;
        if (mDialog != null) {
            // Set removed here because this dismissal is just to hide
            // the dialog -- we don't want this to cause the fragment to
            // actually be removed.
            mRemoved = true;
            mDialog.dismiss();
            mDialog = null;
        }
    }

    @Override
    protected void closeFragment() {
        if (getShowsDialog()) {
            dismiss();
        } else {
            super.closeFragment();
        }
    }

    /**
     * If a listener is set, then any touches in the Dialog's view will report
     * back to the listener.
     * 
     * @param listener
     *            The {@link OnClickListener} to be set.
     */
    public void setOnClickListener(OnClickListener listener) {
        mOnClickListener = listener;
    }
}
