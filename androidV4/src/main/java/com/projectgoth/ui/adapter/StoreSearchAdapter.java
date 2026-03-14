package com.projectgoth.ui.adapter;

import java.util.ArrayList;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.StoreItem;
import com.projectgoth.common.Constants;
import com.projectgoth.i18n.I18n;
import com.projectgoth.model.StickerStoreItem;
import com.projectgoth.model.StorePagerItem;
import com.projectgoth.model.StoreSearchCategory;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;
import com.projectgoth.ui.holder.BasicListCategoryFooterViewHolder;
import com.projectgoth.ui.holder.GiftListViewHolder;
import com.projectgoth.ui.holder.StickerListViewHolder;
import com.projectgoth.ui.holder.StoreSearchGroupViewHolder;
import com.projectgoth.util.AndroidLogger;

/**
 * Created by houdangui on 8/12/14.
 */
public class StoreSearchAdapter extends BaseExpandableListAdapter {

    private static final String LOG_TAG = AndroidLogger.makeLogTag(StoreSearchAdapter.class);

    private final LayoutInflater mInflater;

    private ArrayList<StoreSearchCategory> searchCategories = new ArrayList<StoreSearchCategory>();

    private BaseViewListener<StoreItem> storeItemListener;

    private BaseViewListener<StickerStoreItem> stickerItemListener;

    private FooterClickListener groupFooterListener;

    private String searchString = Constants.BLANKSTR;

    public enum ChildViewType {
        GIFT,
        STICKER,
        EMOTICON,
        AVATAR,
        GROUP_FOOTER,
        NO_RESULT;
    }

    public interface FooterClickListener {
        public void onGroupFooterClick(View v, StoreSearchCategory data);
    }

    private final BaseViewListener<StoreSearchCategory> groupFooterHolderClickListener = new BaseViewListener<StoreSearchCategory>() {

        @Override
        public void onItemClick(View v, StoreSearchCategory data) {
            if (groupFooterListener != null) {
                groupFooterListener.onGroupFooterClick(v, data);
            }
        }

        @Override
        public void onItemLongClick(View v, StoreSearchCategory data) {
            // DO NOTHING
        }
    };

    public StoreSearchAdapter() {
        super();
        this.mInflater = LayoutInflater.from(ApplicationEx.getContext());
    }

    @Override
    public int getGroupCount() {
        int count = 0;
        if (searchCategories != null) {
            count = searchCategories.size();
        }
        return count;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        StoreSearchCategory searchCategory = (StoreSearchCategory)getGroup(groupPosition);

        if (searchCategory.hasNoResult()) {
            //the empty view showing no result
            return 1;
        }

        int count = searchCategory.getItemsCount();

        // to show the footer of see more
        if (searchCategory.hasMoreItems()) {
            count++;
        }

        return count;
    }

    @Override
    public Object getGroup(int groupPosition) {
        StoreSearchCategory storeCategory = null;

        if (searchCategories != null) {
            storeCategory = searchCategories.get(groupPosition);
        }

        return storeCategory;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        StoreSearchCategory storeCategory = (StoreSearchCategory) getGroup(groupPosition);
        if (storeCategory != null) {
            Object[] storeItems = storeCategory.getStoreItems();
            if (storeItems != null && storeItems.length > childPosition) {
                Object storeItem = storeItems[childPosition];
                return storeItem;
            }
        }

        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        StoreSearchGroupViewHolder viewHolder;
        if (convertView == null || convertView.getTag() == null) {
            convertView = mInflater.inflate(R.layout.holder_store_search_group, null);
            viewHolder = new StoreSearchGroupViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (StoreSearchGroupViewHolder) convertView.getTag();
        }

        StoreSearchCategory groupItem = (StoreSearchCategory)getGroup(groupPosition);
        viewHolder.setData(groupItem);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
                                                     ViewGroup parent) {
        Object storeItem = getChild(groupPosition, childPosition);

        ChildViewType viewType = getChildTypeEnum(groupPosition, childPosition);
        switch (viewType) {
            case GROUP_FOOTER:
                convertView = getGroupFooterView(groupPosition, convertView, parent);
                break;
            case NO_RESULT:
                convertView = getNoResultView(groupPosition, convertView, parent);
                break;
            case GIFT:
                convertView = getGiftItemView(convertView, (StoreItem)storeItem);
                break;
            case STICKER:
                convertView = getStickerItemView(convertView, (StickerStoreItem)storeItem);
                break;
            case EMOTICON:
                break;
            case AVATAR:
                break;
        }

        return convertView;
    }

    @Override
    public int getChildTypeCount() {
        return ChildViewType.values().length;
    }

    public ChildViewType getChildTypeEnum(int groupPosition, int childPosition) {
        ChildViewType viewType = null;
        StoreSearchCategory category = (StoreSearchCategory)getGroup(groupPosition);
        if(category.hasNoResult()) {
            viewType = ChildViewType.NO_RESULT;
        }else if(category.hasMoreItems() && childPosition == getChildrenCount(groupPosition)-1) {
            viewType = ChildViewType.GROUP_FOOTER;
        } else {
            switch (category.getType()) {
                case GIFTS:
                    viewType = ChildViewType.GIFT;
                    break;
                case STICKERS:
                    viewType = ChildViewType.STICKER;
                    break;
                case EMOTICONS:
                    viewType = ChildViewType.EMOTICON;
                    break;
                case AVATAR:
                    viewType = ChildViewType.AVATAR;
                    break;
            }
        }
        return viewType;
    }

    @Override
    public int getChildType(int groupPosition, int childPosition) {
        return getChildTypeEnum(groupPosition, childPosition).ordinal();
    }

    private View getGiftItemView(View convertView, StoreItem storeItem) {
        GiftListViewHolder giftListViewHolder;
        if (convertView == null || convertView.getTag(R.id.holder) == null ) {
            convertView = mInflater.inflate(R.layout.holder_gift_list_item, null);
            giftListViewHolder = new GiftListViewHolder(convertView);
            convertView.setTag(R.id.holder, giftListViewHolder);
        } else {
            giftListViewHolder = (GiftListViewHolder) convertView.getTag(R.id.holder);
        }

        giftListViewHolder.setBaseViewListener(storeItemListener);
        giftListViewHolder.setData(storeItem);

        return convertView;
    }

    private View getStickerItemView(View convertView, StickerStoreItem storeItem) {

        StickerListViewHolder stickerListViewHolder;

        if (convertView == null || convertView.getTag(R.id.holder) == null) {
            convertView = mInflater.inflate(R.layout.holder_sticker_list_item, null);
            stickerListViewHolder = new StickerListViewHolder(convertView);
            convertView.setTag(R.id.holder, stickerListViewHolder);

        } else {
            stickerListViewHolder = (StickerListViewHolder) convertView.getTag(R.id.holder);
        }

        if (storeItem != null) {
            stickerListViewHolder.setBaseViewListener(stickerItemListener);
            stickerListViewHolder.setData(storeItem);
        }

        return convertView;

    }

    private View getGroupFooterView(int groupPosition, View convertView, ViewGroup parent) {
        BasicListCategoryFooterViewHolder<StoreSearchCategory> categoryView = null;

        if (convertView == null || convertView.getTag(R.id.holder_footer) == null) {
            convertView = mInflater.inflate(R.layout.holder_list_footer, parent, false);
            categoryView = new BasicListCategoryFooterViewHolder<StoreSearchCategory>(convertView);
            convertView.setTag(R.id.holder_footer, categoryView);
        } else {
            categoryView = (BasicListCategoryFooterViewHolder<StoreSearchCategory>) convertView.getTag(R.id.holder_footer);
        }

        StoreSearchCategory storeCategory = (StoreSearchCategory)getGroup(groupPosition);
        categoryView.setData(storeCategory);
        String footerText = getFooterText(storeCategory, searchString);
        categoryView.setLabel(footerText);
        categoryView.setBackgroundColor(ApplicationEx.getColor(R.color.white_background));
        categoryView.setBaseViewListener(groupFooterHolderClickListener);


        return convertView;
    }

    private String getFooterText(StoreSearchCategory storeCategory, String searchString) {
        String footerText = Constants.BLANKSTR;

        StorePagerItem.StorePagerType type = storeCategory.getType();
        switch (type) {
            case GIFTS:
                footerText = String.format(I18n.tr("See all gifts for \"%s\""), searchString);
                break;
            case STICKERS:
                footerText = String.format(I18n.tr("See all stickers for \"%s\""), searchString);
                break;
            case EMOTICONS:
                footerText = String.format(I18n.tr("See all emoticons for \"%s\""), searchString);
                break;
            case AVATAR:
                footerText = String.format(I18n.tr("See all avatars for \"%s\""), searchString);
                break;
        }

        return footerText;
    }

    private View getNoResultView(int groupPosition, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.store_search_no_result, parent, false);
        }

        //set the text
        TextView text = (TextView)convertView.findViewById(R.id.no_result_text);
        text.setText(String.format(I18n.tr("No result for \"%s\""), searchString));

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    public void setData(ArrayList<StoreSearchCategory> data) {
        this.searchCategories = data;
        notifyDataSetChanged();
    }

    public void setStoreItemListener(BaseViewListener<StoreItem> storeItemListener) {
        this.storeItemListener = storeItemListener;
    }

    public void setGroupFooterListener(FooterClickListener groupFooterListener) {
        this.groupFooterListener = groupFooterListener;
    }

    public void setSearchString(String currentSearchString) {
        searchString = currentSearchString;
    }

    public void setStickerItemListener(BaseViewListener<StickerStoreItem> stickerItemListener) {
        this.stickerItemListener = stickerItemListener;
    }
}
