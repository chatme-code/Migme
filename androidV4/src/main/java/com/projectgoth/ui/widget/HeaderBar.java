/**
 * Copyright (c) 2013 Project Goth
 *
 * TopBarMenu.java
 * Created Oct 9, 2014, 8:01:12 AM
 */

package com.projectgoth.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
public class HeaderBar extends RelativeLayout implements OnClickListener {

    private static final int   MAX_ITEMS   = 3;

    private IconCounterView[]  tabs;
    private ImageView[]        arrows;

    private ArrayList<BarItem> items;

    private int                selectedTab = 0;

    /**
     * @param context
     */
    public HeaderBar(Context context) {
        super(context);
        init();
    }

    /**
     * @param context
     * @param attrs
     */
    public HeaderBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * @param context
     * @param attrs
     * @param defStyle
     */
    public HeaderBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {

        tabs = new IconCounterView[MAX_ITEMS];
        arrows = new ImageView[MAX_ITEMS];
        items = new ArrayList<BarItem>(MAX_ITEMS);
        for (int i = 0; i < MAX_ITEMS; i++) {
            items.add(null);
        }

        LayoutInflater.from(getContext()).inflate(R.layout.header_bar_layout, this);
        setBackgroundColor(Theme.getColor(ThemeValues.LIGHT_BACKGROUND_COLOR));

        tabs[0] = (IconCounterView) findViewById(R.id.tab1);
        tabs[1] = (IconCounterView) findViewById(R.id.tab2);
        tabs[2] = (IconCounterView) findViewById(R.id.tab3);

        arrows[0] = (ImageView) findViewById(R.id.arrow1);
        arrows[1] = (ImageView) findViewById(R.id.arrow2);
        arrows[2] = (ImageView) findViewById(R.id.arrow3);

        for (View tab : tabs) {
            if (tab != null) {
                tab.setOnClickListener(this);
            }
        }

        setSelectedIndex(selectedTab);

    }

    @Override
    public void onClick(View v) {
        int i = 0;
        BarItem item = null;
        for (View button : tabs) {
            if (button != null && v.getId() == button.getId()) {
                setSelectedIndex(i);
                item = items.get(i);
                if (item != null) {
                    item.onPress();
                }
                break;
            }
            i++;
        }

    }

    public void setSelectedIndex(int index) {
        if (index < MAX_ITEMS) {
            selectedTab = index;
            for (int i = 0; i < MAX_ITEMS; i++) {
                if (tabs[i] != null) {
                    tabs[i].setSelected(i == index);
                }
                if (arrows[i] != null) {
                    arrows[i].setVisibility((i == index) ? View.VISIBLE : View.INVISIBLE);
                }
            }
        }
    }

    private void hideTab(int index) {
        if (index < MAX_ITEMS) {
            if (tabs[index] != null) {
                tabs[index].setVisibility(View.GONE);
            }
            if (arrows[index] != null) {
                arrows[index].setVisibility(View.GONE);
            }
        }
    }

    private void showTab(int index) {
        if (index < MAX_ITEMS) {
            if (tabs[index] != null) {
                tabs[index].setVisibility(View.VISIBLE);
            }
            if (arrows[index] != null) {
                arrows[index].setVisibility((selectedTab == index) ? View.VISIBLE : View.INVISIBLE);
            }
        }
    }

    public void setItem(int index, BarItem item) {
        items.set(index, item);
        recompute();
    }

    private void recompute() {

        for (int i = 0; i < MAX_ITEMS; i++) {
            hideTab(i);
        }

        int i = 0;
        for (BarItem item : items) {
            if (item != null) {
                if (tabs[i] != null) {
                    tabs[i].setIconResource(item.getIconRes());
                    ImageLoader loader = item.getImageLoader();
                    if (loader != null) {
                        loader.load(tabs[i]);
                    }
                }
                showTab(i);
            }
            i++;
        }
    }

    public void setCount(int index, int value) {
        if (index < MAX_ITEMS) {
            if (tabs[index] != null) {
                tabs[index].setCounter(value);
            }
        }
    }

}
