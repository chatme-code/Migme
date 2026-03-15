/**
 * Copyright (c) 2013 Project Goth
 *
 * BottomBarMenu.java
 * Created 2 Jun, 2014, 5:45:15 pm
 */

package com.projectgoth.ui.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.projectgoth.R;
import com.projectgoth.common.Theme;
import com.projectgoth.common.ThemeValues;
import com.projectgoth.model.BarItem;
import com.projectgoth.model.BarItem.ImageLoader;

import java.util.ArrayList;

/**
 * @author warrenbalcos
 * 
 */
public class BottomBarMenu extends LinearLayout implements OnClickListener {

    private static final int   MAX_ITEMS = 3;

    private View[]             buttons;

    private ImageView[]        icons;

    private TextView[]         texts;

    private View[]             divider;

    private boolean            hideDividers;

    private ArrayList<BarItem> items;

    /**
     * Supports 1 to {@value #MAX_ITEMS} {@link BarItem}'s only
     * 
     * @param context
     */
    public BottomBarMenu(Context context) {
        this(context, null);
    }

    /**
     * @param context
     * @param attrs
     */
    public BottomBarMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {

        buttons = new View[MAX_ITEMS];
        icons = new ImageView[MAX_ITEMS];
        texts = new TextView[MAX_ITEMS];
        divider = new View[MAX_ITEMS - 1];
        items = new ArrayList<BarItem>(MAX_ITEMS);
        for (int i = 0; i < MAX_ITEMS; i++) {
            items.add(null);
        }

        LayoutInflater.from(getContext()).inflate(R.layout.bottom_menu_bar, this);
        setBackgroundColor(Theme.getColor(ThemeValues.BOTTOM_BAR_BG_COLOR));

        buttons[0] = findViewById(R.id.button_1);
        buttons[1] = findViewById(R.id.button_2);
        buttons[2] = findViewById(R.id.button_3);

        divider[0] = findViewById(R.id.divider_1);
        divider[1] = findViewById(R.id.divider_2);

        icons[0] = (ImageView) findViewById(R.id.icon_1);
        icons[1] = (ImageView) findViewById(R.id.icon_2);
        icons[2] = (ImageView) findViewById(R.id.icon_3);

        texts[0] = (TextView) findViewById(R.id.text_1);
        texts[1] = (TextView) findViewById(R.id.text_2);
        texts[2] = (TextView) findViewById(R.id.text_3);

        for (View button : buttons) {
            if (button != null) {
                button.setOnClickListener(this);
            }
        }
    }

    private void showButton(int index) {
        if (index < MAX_ITEMS) {
            if (buttons[index] != null) {
                buttons[index].setVisibility(View.VISIBLE);
                if (!TextUtils.isEmpty(texts[index].getText())) {
                    texts[index].setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void hideButton(int index) {
        if (index < MAX_ITEMS) {
            if (buttons[index] != null) {
                buttons[index].setVisibility(View.GONE);
            }
        }
    }

    private void showDivider(int index) {
        if (index < MAX_ITEMS - 1) {
            if (divider[index] != null) {
                divider[index].setVisibility(View.VISIBLE);
            }
        }
    }

    private void hideDivider(int index) {
        if (index < MAX_ITEMS - 1) {
            if (divider[index] != null) {
                divider[index].setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        int i = 0;
        BarItem item = null;
        for (View button : buttons) {
            if (button != null && v.getId() == button.getId()) {
                item = items.get(i);
                if (item != null) {
                    if (item.hasCustomAction()) {
                        item.executeCustomAction();
                    } else {
                        item.onPress();
                    }
                }
                break;
            }
            i++;
        }
    }

    public void setItem(int index, BarItem item) {
        items.set(index, item);
        recompute();
    }

    private void recompute() {

        hideDivider(0);
        hideDivider(1);
        hideButton(0);
        hideButton(1);
        hideButton(2);

        int i = 0;
        for (BarItem item : items) {
            if (item != null) {
                if (icons[i] != null) {
                    icons[i].setImageResource(item.getIconRes());
                    ImageLoader loader = item.getImageLoader();
                    if (loader != null) {
                        loader.load(icons[i]);
                    }
                }
                if (texts[i] != null) {
                    String title = item.getTitle();
                    if (!TextUtils.isEmpty(title)) {
                        texts[i].setText(title);
                    }
                }
                showButton(i);
            }
            i++;
        }

        if (!isHideDividers()) {
            int length = items.size();
            if (length == 2) {
                showDivider(0);
            } else if (length == 3) {
                showDivider(0);
                showDivider(1);
            }
        }
    }

    /**
     * @return the hideDividers
     */
    public boolean isHideDividers() {
        return hideDividers;
    }

    /**
     * @param hideDividers
     *            the hideDividers to set
     */
    public void hideDividers() {
        this.hideDividers = true;
        recompute();
    }

    public void showDividers() {
        this.hideDividers = false;
        recompute();
    }

    public void show() {
        setVisibility(View.VISIBLE);
    }

    public void hide() {
        setVisibility(View.GONE);
    }
}
