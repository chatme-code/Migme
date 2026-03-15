package com.projectgoth.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.projectgoth.R;
import com.projectgoth.b.data.GameItem;
import com.projectgoth.datastore.GamesDatastore;
import com.projectgoth.localization.I18n;
import com.projectgoth.ui.activity.CustomActionBarConfig;

/**
 * Created by danielchen on 15/1/22.
 */
public class GameDetailInformationFragment extends BaseFragment {

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_game_detail_information;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String gameId = getArguments().getString(GameDetailFragment.KEY_GAME_ITEM);
        GameItem gameItem = GamesDatastore.getInstance().getGameItemWithId(gameId);

        TextView descriptionContent = (TextView) view.findViewById(R.id.description_content);
        TextView howToPlayContent = (TextView) view.findViewById(R.id.how_to_play_content);
        TextView aboutDeveloperContent = (TextView) view.findViewById(R.id.about_developer_content);
        
        descriptionContent.setText(gameItem.getDescriptionInfo());
        howToPlayContent.setText(gameItem.getHowToPlayInfo());
        aboutDeveloperContent.setText(gameItem.getAboutInfo());
        TextView explain = (TextView) view.findViewById(R.id.explain);
        if(gameItem.getType() == GameItem.GAME_MULTIPLY){
            explain.setText("To play now, download the migme game app.");
        }else{
            explain.setVisibility(View.GONE);
        }
        
    }

    @Override
    public CustomActionBarConfig getActionBarConfig() {
        CustomActionBarConfig config = super.getActionBarConfig();
        config.setNavigationButtonState(CustomActionBarConfig.NavigationButtonState.BACK);
        return config;
    }

    @Override
    protected String getTitle() {
        return I18n.tr("Games");
    }

    @Override
    protected int getTitleIcon() {
        return R.drawable.ad_play_white;
    }
}