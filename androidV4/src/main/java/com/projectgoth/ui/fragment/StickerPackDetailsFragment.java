/**
 * Copyright (c) 2013 Project Goth
 *
 * StickerPackDetailsFragment.java
 * Created Dec 16, 2014, 9:43:48 AM
 */

package com.projectgoth.ui.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.mig33.diggle.events.Events;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.StoreCategory;
import com.projectgoth.b.data.StoreItem;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Tools;
import com.projectgoth.controller.StoreController;
import com.projectgoth.datastore.EmoticonDatastore;
import com.projectgoth.events.GAEvent;
import com.projectgoth.i18n.I18n;
import com.projectgoth.imagefetcher.ImageHandler;
import com.projectgoth.model.BaseEmoticonPackData;
import com.projectgoth.model.StorePagerItem;
import com.projectgoth.nemesis.model.Sticker;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.adapter.StickerPackDetailsAdapter;
import com.projectgoth.ui.widget.ButtonEx;
import com.projectgoth.ui.widget.util.ButtonUtil;

/**
 * @author mapet
 * 
 */
public class StickerPackDetailsFragment extends BaseDialogFragment implements OnClickListener {

    private int                       packId;
    private int                       referenceId;
    private StoreItem                 storeItem;

    private BaseEmoticonPackData      packData;

    private ImageView                 packIcon;
    private TextView                  packName;
    private TextView                  packDescription;
    private ButtonEx                  downloadBtn;
    private GridView                  packGridList;
    private StickerPackDetailsAdapter packGridListAdapter;

    private boolean                   isPurchaseInProcess     = false;

    public static final String        PARAM_PACK_ID           = "PARAM_PACK_ID";
    public static final String        PARAM_PACK_REFERENCE_ID = "PARAM_PACK_REFERENCE_ID";

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        packName = (TextView) view.findViewById(R.id.pack_name);
        packDescription = (TextView) view.findViewById(R.id.pack_description);
        packIcon = (ImageView) view.findViewById(R.id.pack_icon);
        downloadBtn = (ButtonEx) view.findViewById(R.id.download_pack_button);
        packGridList = (GridView) view.findViewById(R.id.pack_grid);
        packGridListAdapter = new StickerPackDetailsAdapter();

        refreshData();
        bindOnClickListener(this, R.id.download_pack_button, R.id.close_button);
    }

    @Override
    protected void readBundleArguments(Bundle args) {
        super.readBundleArguments(args);
        packId = args.getInt(PARAM_PACK_ID);
        referenceId = args.getInt(PARAM_PACK_REFERENCE_ID);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_sticker_pack_details;
    }

    private void refreshData() {
        // get store item for pack name, description and price
        storeItem = StoreController.getInstance().getStoreItem(Integer.toString(packId));

        // get pack data to check if pack is owned
        packData = EmoticonDatastore.getInstance().getStickerPackWithId(referenceId);
        
        isPurchaseInProcess = StoreController.getInstance().isStickerPackPurchaseInProcess(Integer.toString(packId));

        if (storeItem != null) {
            packName.setText(storeItem.getName());
            packDescription.setText(storeItem.getDescription());

            StoreCategory[] storeCategories = StoreController.getInstance().getStoreCategories(
                    Integer.toString(StorePagerItem.StorePagerType.STICKERS.getValue()));

            if (storeCategories != null) {
                for (StoreCategory storeCategory : storeCategories) {
                    if (storeCategory.getId() == storeItem.getStoreCategoryID()) {
                        packDescription.setText(storeCategory.getName());
                    }
                }
            }

            final float roundedPrice = storeItem.getRoundedPrice();
            downloadBtn.setText(I18n.tr("DOWNLOAD") + Constants.SPACESTR + roundedPrice + Constants.SPACESTR
                    + storeItem.getLocalCurrency());
        }

        if (packData != null) {
            String icon = packData.getBaseEmoticonPack().getIconUrl();

            if (icon != null) {
                if (icon.startsWith(Constants.LINK_DRAWABLE)) {
                    int resId = Tools.getDrawableResId(ApplicationEx.getContext(), icon);
                    packIcon.setImageResource(resId);

                } else {
                    ImageHandler.getInstance().loadImageFromUrl(packIcon, icon, false, R.drawable.ad_loadstatic12_grey);
                }
            }

            if (isPurchaseInProcess) {
                downloadBtn.setType(ButtonUtil.BUTTON_TYPE_GRAY);
                downloadBtn.setText(I18n.tr("DOWNLOADING"));
                
            } else if (packData.isOwnPack()) {
                downloadBtn.setType(ButtonUtil.BUTTON_TYPE_GRAY);
                downloadBtn.setText(I18n.tr("DOWNLOADED"));
            }

            Set<String> hotkeys = packData.getBaseEmoticonPack().getHotkeys();

            List<Sticker> stickerList = new ArrayList<Sticker>();
            for (String hotkey : hotkeys) {
                stickerList.add(new Sticker(hotkey));
            }

            packGridListAdapter.setGridList(stickerList);
            packGridList.setAdapter(packGridListAdapter);
        }
    }

    @Override
    protected void registerReceivers() {
        registerEvent(Events.MigStore.Item.RECEIVED);
        registerEvent(Events.Emoticon.RECEIVED);
        registerEvent(Events.Emoticon.FETCH_ALL_COMPLETED);
        registerEvent(Events.Sticker.PACK_RECEIVED);
        registerEvent(Events.Sticker.FETCH_PACKS_COMPLETED);
        registerEvent(Events.Sticker.FETCH_PACK_ERROR);
        registerEvent(Events.MigStore.Item.PURCHASED);
        registerEvent(Events.MigStore.Item.PURCHASE_ERROR);
        registerEvent(Events.MigStore.Item.BEGIN_PURCHASE_STORE_ITEM);
    }

    @Override
    protected void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action.equals(Events.MigStore.Item.RECEIVED) || action.equals(Events.Sticker.PACK_RECEIVED)
                || action.equals(Events.Sticker.FETCH_PACKS_COMPLETED) || action.equals(Events.MigStore.Item.PURCHASED)
                || action.equals(Events.MigStore.Item.BEGIN_PURCHASE_STORE_ITEM)) {
            refreshData();
        } else if (action.equals(Events.MigStore.Item.PURCHASE_ERROR)) {
            Tools.showToastForIntent(context, intent);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.download_pack_button:
                GAEvent.Store_PurchaseSticker.send();
                if (!isPurchaseInProcess && !packData.isOwnPack() && storeItem != null) {
                    if (StoreController.getInstance().canPurchaseItem(storeItem.getPrice())) {
                        ActionHandler.getInstance().showStickerPurchaseConfirmDlg(getActivity(), storeItem);
                    } else {
                        ActionHandler.getInstance().displayAccountBalance(getActivity());
                    }
                }
                break;
            case R.id.close_button:
                closeFragment();
                break;
            default:
                break;
        }
    }

}
