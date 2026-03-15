package com.projectgoth.fusion.invitation.restapi.data;

import com.projectgoth.fusion.invitation.InvitationMetadata;
import java.io.Serializable;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(
   name = "create"
)
public class SendingInvitationData implements Serializable {
   public int type;
   public int channel;
   public List<String> destinations;
   public InvitationMetadata invitationMetadata;
}
