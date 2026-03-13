package com.projectgoth.fusion.payment.mimopay;

import java.io.Reader;
import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

public class MIMOXMLUtils {
   private static final JAXBContext JAXB_CTX_MIMO_DATA;
   private static final ThreadLocal<Unmarshaller> TL_UNMARSHALLER = new ThreadLocal();
   private static final ThreadLocal<Marshaller> TL_MARSHALLER = new ThreadLocal();

   public static String serializeToXML(Object dataObject) throws JAXBException {
      Marshaller marshaller = (Marshaller)TL_MARSHALLER.get();
      if (marshaller == null) {
         marshaller = JAXB_CTX_MIMO_DATA.createMarshaller();
         TL_MARSHALLER.set(marshaller);
      }

      StringWriter sw = new StringWriter();

      try {
         marshaller.marshal(dataObject, sw);
         return sw.toString();
      } catch (JAXBException var4) {
         TL_MARSHALLER.set((Object)null);
         throw var4;
      }
   }

   public static <T> T deserializeFromXML(Reader reader, Class<T> declaredType) throws JAXBException {
      Unmarshaller unmarshaller = (Unmarshaller)TL_UNMARSHALLER.get();
      if (unmarshaller == null) {
         unmarshaller = JAXB_CTX_MIMO_DATA.createUnmarshaller();
         TL_UNMARSHALLER.set(unmarshaller);
      }

      try {
         JAXBElement<T> element = unmarshaller.unmarshal(new StreamSource(reader), declaredType);
         if (element != null) {
            return element.getValue();
         } else {
            throw new JAXBException("unmarshaller returns a null JAXBElement");
         }
      } catch (JAXBException var4) {
         TL_MARSHALLER.set((Object)null);
         throw var4;
      }
   }

   static {
      try {
         JAXB_CTX_MIMO_DATA = JAXBContext.newInstance(CreditReloadRequest.class, CreditReloadResponse.class, PendingStatusInquiryRequest.class, PendingStatusInquiryResponse.class);
      } catch (JAXBException var1) {
         throw new ExceptionInInitializerError(var1);
      }
   }
}
