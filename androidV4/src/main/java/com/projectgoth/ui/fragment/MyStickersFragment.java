package com.projectgoth.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.projectgoth.R;
import com.projectgoth.datastore.EmoticonDatastore;
import com.projectgoth.events.GAEvent;
import com.projectgoth.i18n.I18n;
import com.projectgoth.model.BaseEmoticonPackData;
import com.projectgoth.ui.activity.CustomActionBarConfig;
import com.projectgoth.ui.adapter.MyStickerPackAdapter;
import com.projectgoth.ui.holder.BaseViewHolder;

import java.util.List;

/**
 * Created by houdangui on 15/12/14.
 */
public class MyStickersFragment extends BaseFragment implements BaseViewHolder.BaseViewListener<BaseEmoticonPackData> {

    private TextView description;
    private ListView stickerList;
    private MyStickerPackAdapter stickerPackAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_my_stickers;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        description = (TextView) LayoutInflater.from(view.getContext()).inflate(R.layout.header_mysticker_description, null);
        stickerList = (ListView) view.findViewById(R.id.my_sticker_list);
        stickerList.addHeaderView(description);
        description.setText(I18n.tr("Make sticker selection easier in chat,\n by turning off the ones you don’t use."));

        stickerPackAdapter = new MyStickerPackAdapter();
        //set data
        List<BaseEmoticonPackData> myStickerPacks = EmoticonDatastore.getInstance().getOwnStickerPackDataList();
        stickerPackAdapter.setStickerPackList(myStickerPacks);
        //set listener
        stickerPackAdapter.setListener(this);
        stickerList.setAdapter(stickerPackAdapter);

    }

    @Override
    protected String getTitle() {
        return I18n.tr("My stickers");
    }

    @Override
    protected int getTitleIcon() {
        return R.drawable.ad_sticker_white;
    }

    @Override
    public CustomActionBarConfig getActionBarConfig() {
        CustomActionBarConfig config = super.getActionBarConfig();
        config.setNavigationButtonState(CustomActionBarConfig.NavigationButtonState.BACK);
        return config;
    }

    @Override
    public void onItemClick(View v, BaseEmoticonPackData data) {
        int id = v.getId();
        if(id == R.id.toggle_button) {

            boolean isChecked = ((ToggleButton) v).isChecked();

            if(isChecked) {
                GAEvent.Store_TurnOnSticker.send();
            } else {
                GAEvent.Store_TurnOffSticker.send();
            }

            data.setEnable(isChecked);

            //save it persistently
            EmoticonDatastore.getInstance().addStickerPack(data, true);
        }
    }

    @Override
    public void onItemLongClick(View v, BaseEmoticonPackData data) {

    }
}
