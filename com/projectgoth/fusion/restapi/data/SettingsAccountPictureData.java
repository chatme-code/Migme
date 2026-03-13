package com.projectgoth.fusion.restapi.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(
   name = "settings"
)
public class SettingsAccountPictureData {
   public SettingsEnums.DisplayPictureChoice displayPicture;
   public static final SettingsEnums.DisplayPictureChoice DEFAULT_DISPLAY_PICTURE;

   public SettingsAccountPictureData() {
   }

   public SettingsAccountPictureData(int displayPictureSetting) {
      this.displayPicture = SettingsEnums.DisplayPictureChoice.fromValue(displayPictureSetting);
      if (this.displayPicture == null) {
         this.displayPicture = DEFAULT_DISPLAY_PICTURE;
      }

   }

   public int retrieveDisplayPictureSetting() {
      return (this.displayPicture == null ? DEFAULT_DISPLAY_PICTURE : this.displayPicture).value();
   }

   static {
      DEFAULT_DISPLAY_PICTURE = SettingsEnums.DisplayPictureChoice.AVATAR;
   }
}
