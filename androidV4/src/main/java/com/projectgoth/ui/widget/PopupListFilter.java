package com.projectgoth.ui.widget;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.common.Constants;
import com.projectgoth.i18n.I18n;

import java.util.List;

/**
 * Created by houdangui on 4/6/15.
 */
public class PopupListFilter extends LinearLayout implements View.OnKeyListener {

    private Context mContext;
    private ListView mList;
    private EditText mFilter;
    private ArrayAdapter<String> mAdapter;

    private PopupListFilterListener listener;

    public enum ListType {mentionList, hotTopicList}

    public interface PopupListFilterListener {
        void onPopupListItemSelected(final String selectedItemText, final ListType listType);
    }

    public PopupListFilter(Context context) {
        super(context);

        mContext = context;

        init();
    }

    public PopupListFilter(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;

        init();
    }

    private void init() {

        // <marge> tag in the layout merges its content, but not itself , so need to set its attributes here
        this.setBackgroundResource(R.drawable.rounded_corner);
        this.setOrientation(VERTICAL);
        int padding = ApplicationEx.getDimension(R.dimen.normal_padding);
        this.setPadding(padding, padding, padding, padding);

        LayoutInflater.from(mContext).inflate(R.layout.mention_filter, this, true);

        mList = (ListView) findViewById(R.id.mentions_list);
        mFilter = (EditText) findViewById(R.id.mentions_list_filter);


        mAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1);
        mList.setAdapter(mAdapter);

        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (listener != null) {
                    listener.onPopupListItemSelected(mAdapter.getItem(position), mListType);
                }

            }
        });

        mFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i2, int i3) {
                mAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mFilter.setOnKeyListener(this);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
              //do nothing to prevent it from the widgets behind it being clicked
            }
        });
    }

    public void setListener(PopupListFilterListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.FLAG_EDITOR_ACTION) {
            String listFilter = mFilter.getText().toString();
            if (listener != null) {
                listener.onPopupListItemSelected(listFilter, mListType);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (listener != null) {
                listener.onPopupListItemSelected(Constants.BLANKSTR, mListType);
            }
            return true;
        }
        return false;
    }

    public void requestInputFocus() {
        mFilter.requestFocus();
    }

    public void clearInputFocus() {
        mFilter.clearFocus();
    }

    private ListType mListType;
    public void refreshList(List<String> list, ListType listType) {
        mListType = listType;

        if (listType == ListType.mentionList) {
            mFilter.setHint(Constants.MENTIONS_TAG + I18n.tr("username"));
        } else {
            mFilter.setHint(Constants.HASH_TAG + I18n.tr("hot topic"));
        }

        if (list != null && list.size() > 0) {
            mAdapter.clear();

            for (String item : list) {
                //remove hash sign in the list, already appended in the edittext field
                if(mListType == ListType.hotTopicList) {
                    item = item.substring(1);
                }
                mAdapter.add(item);

            }

            mAdapter.notifyDataSetChanged();
        }
    }

}
