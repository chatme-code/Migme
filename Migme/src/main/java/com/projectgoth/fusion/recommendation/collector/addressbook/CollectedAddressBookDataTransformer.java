package com.projectgoth.fusion.recommendation.collector.addressbook;

import com.projectgoth.fusion.recommendation.collector.CollectedDataTypeEnum;
import com.projectgoth.fusion.recommendation.collector.CollectorTransformationException;
import com.projectgoth.fusion.recommendation.collector.ICollectorTransformation;
import com.projectgoth.fusion.slice.CollectedAddressBookDataIce;
import com.projectgoth.fusion.slice.CollectedDataIce;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;

public class CollectedAddressBookDataTransformer implements ICollectorTransformation<AddressBookRecordData> {
   private final String name;

   public CollectedAddressBookDataTransformer(String name) {
      this.name = name;
   }

   public String getName() {
      return this.name;
   }

   public Collection<AddressBookRecordData> toLoggables(CollectedDataIce dataIce) throws CollectorTransformationException {
      if (dataIce.dataType != CollectedDataTypeEnum.ADDRESSBOOKCONTACT.getCode()) {
         throw new CollectorTransformationException("Don't understand dataIce.type " + dataIce.dataType);
      } else {
         CollectedAddressBookDataIce collectedAddrBookDataIce = (CollectedAddressBookDataIce)dataIce;
         if (collectedAddrBookDataIce.contactValues == null) {
            throw new CollectorTransformationException("Null contact values");
         } else {
            ArrayList<AddressBookRecordData> addressBookRecordDataList = new ArrayList(collectedAddrBookDataIce.contactValues.length);
            String[] arr$ = collectedAddrBookDataIce.contactValues;
            int len$ = arr$.length;

            for(int i$ = 0; i$ < len$; ++i$) {
               String contactValue = arr$[i$];
               AddressBookRecordData addressBookRecordData = new AddressBookRecordData();
               addressBookRecordData.contactType = collectedAddrBookDataIce.contactType;
               addressBookRecordData.contactValue = contactValue;
               addressBookRecordData.submitterUserID = collectedAddrBookDataIce.submitterUserId;
               addressBookRecordData.createDate = new Date(collectedAddrBookDataIce.createTimestamp);
               addressBookRecordDataList.add(addressBookRecordData);
            }

            return addressBookRecordDataList;
         }
      }
   }
}
