package com.projectgoth.ui.widget;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Tools;
import com.projectgoth.i18n.I18n;

/**
 * Created by houdangui on 16/10/14.
 */
public class SelectedItemBox extends FlowLayout implements TextWatcher {

    private Context mContext;
    private EditText mEditText;

    private SelectedItemBoxListener mListener;

    public interface SelectedItemBoxListener {
        void performFilter(final String filterString);
        void onTextInputChanged(CharSequence s, int start, int before, int count);
    }

    public SelectedItemBox(Context context) {
        super(context);

        mContext = context;

        init();
    }

    public SelectedItemBox(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        mContext = context;

        init();
    }

    public SelectedItemBox(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);

        mContext = context;

        init();
    }

    private void init() {
        //add EditText
        EditText editText =  (EditText) LayoutInflater.from(mContext).inflate(R.layout.selected_user_box_input, null);
        mEditText = editText;
        editText.setHint(I18n.tr("Search"));
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        editText.setLayoutParams(params);
        addView(editText);

        editText.addTextChangedListener(this);
    }

    public void addItem(View label) {
        int childNum = getChildCount();
        addView(label, childNum - 1);

        updateSearchHint();
    }

    public void showInputCursor() {
        mEditText.requestFocus();
        Tools.showVirtualKeyboard(ApplicationEx.getInstance().getCurrentActivity(), mEditText);
    }

    private void updateSearchHint() {
        int childNum = getChildCount();
        if (childNum > 0) {
            mEditText.setHint(Constants.BLANKSTR);
        } else {
            mEditText.setHint(I18n.tr("Search"));
        }
    }

    public void setListener(SelectedItemBoxListener listener) {
        this.mListener = listener;
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (mListener != null) {
            mListener.performFilter(s.toString());
        }
    }

    @Override
    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (mListener != null) {
            mListener.onTextInputChanged(s, start, before, count);
        }
    }

    public void clearTextInput() {
        mEditText.setText(Constants.BLANKSTR);
    }

    public String getTextInput() {
        return mEditText.getText().toString();
    }

    public void setEditTextHint(String hint) {
        mEditText.setHint(hint);
    }
}
