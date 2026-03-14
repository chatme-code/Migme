package com.projectgoth.ui.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.projectgoth.R;
import com.projectgoth.imagefetcher.ImageHandler;
import com.projectgoth.model.BaseEmoticonPackData;
import com.projectgoth.nemesis.model.BaseEmoticonPack;

/**
 * Created by houdangui on 15/12/14.
 */
public class MyStickerPackViewHolder extends BaseViewHolder<BaseEmoticonPackData> {

    private ImageView stickerPackIcon;
    private TextView stickerPackName;
    private ToggleButton toggleButton;

    public MyStickerPackViewHolder(View rootView) {
        super(rootView);

        stickerPackIcon = (ImageView) rootView.findViewById(R.id.sticker_pack_icon);
        stickerPackName = (TextView) rootView.findViewById(R.id.sticker_pack_name);
        toggleButton = (ToggleButton) rootView.findViewById(R.id.toggle_button);
        toggleButton.setOnClickListener(this);

    }

    @Override
    public void setData(BaseEmoticonPackData data) {
        super.setData(data);

        BaseEmoticonPack stickerPack = data.getBaseEmoticonPack();

        ImageHandler.getInstance().loadImage(stickerPack.getIconUrl(), stickerPackIcon);

        stickerPackName.setText(stickerPack.getName());

        toggleButton.setChecked(data.isEnable());

    }
}
