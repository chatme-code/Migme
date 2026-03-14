/**
 * Copyright (c) 2013 Project Goth
 *
 * LocationViewHolder.java
 * Created Jul 11, 2014, 5:30:57 PM
 */

package com.projectgoth.ui.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.projectgoth.R;
import com.projectgoth.i18n.I18n;
import com.projectgoth.model.LocationListItem;

/**
 * Represents a holder for LocationList Items.
 * @author angelorohit
 *
 */
public class LocationItemViewHolder extends BaseViewHolder<LocationListItem> {

    private final TextView txtAddress;
    private final TextView txtDistance;
    private final ImageView imgSelected;
    
    public LocationItemViewHolder(View view) {
        super(view);
        txtAddress = (TextView) view.findViewById(R.id.txt_address);
        txtDistance = (TextView) view.findViewById(R.id.txt_distance);
        imgSelected = (ImageView) view.findViewById(R.id.img_selected);
    }
    
    @Override
    public void setData(LocationListItem data) {
        super.setData(data);
        
        if (data != null) {
            if (txtAddress != null) {
                final String formattedLocation = data.getFormattedLocation();
                if (formattedLocation != null) {
                    txtAddress.setText(formattedLocation);
                }
            }
                        
            if (txtDistance != null) {
                final int distance = data.getDistance();
                if (distance >= 0) {
                    txtDistance.setText(String.format(I18n.tr("%d m away"), distance));
                }
            }
            
            if (imgSelected != null) {
                imgSelected.setVisibility((data.isChecked()) ? View.VISIBLE : View.INVISIBLE); 
            }
        }
    }

}
