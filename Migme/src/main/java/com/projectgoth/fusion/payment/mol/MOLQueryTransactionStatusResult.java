package com.projectgoth.fusion.payment.mol;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class MOLQueryTransactionStatusResult {
   public String resCode;
   public String transactionId;
   public String vendorTransactionId;
   public String amount;
   public String currency;
   public String vendorSignature;
   public String status;

   MOLQueryTransactionStatusResult(Document doc) {
      Element elem = (Element)doc.getFirstChild();
      this.resCode = MOLXMLUtils.getChildNodeTagValue(elem, "ResCode");
      this.transactionId = MOLXMLUtils.getChildNodeTagValue(elem, "MRef_ID");
      this.vendorTransactionId = MOLXMLUtils.getChildNodeTagValue(elem, "MOLOrderID");
      this.amount = MOLXMLUtils.getChildNodeTagValue(elem, "Amount");
      this.currency = MOLXMLUtils.getChildNodeTagValue(elem, "Currency");
      this.status = MOLXMLUtils.getChildNodeTagValue(elem, "Status");
      this.vendorSignature = MOLXMLUtils.getChildNodeTagValue(elem, "Signature");
   }

   public String toString() {
      return "ResultCode=" + this.resCode + ";" + "TransactionID=" + this.transactionId + ";" + "VendorTransactionID=" + this.vendorTransactionId + ";" + "Amount=" + this.amount + ";" + "Currency=" + this.currency + ";" + "Status=" + this.status + ";" + "VendorSignature=" + this.vendorSignature;
   }
}
