/**
 * Copyright (c) 2013 Project Goth
 *
 * FiksuInterface.java
 * Created 22 Aug, 2014, 11:02:57 am
 */

package com.projectgoth.util;

import android.content.Context;
import com.fiksu.asotracking.FiksuTrackingManager;
import com.fiksu.asotracking.FiksuTrackingManager.PurchaseEvent;
import com.fiksu.asotracking.FiksuTrackingManager.RegistrationEvent;
import com.projectgoth.common.Config;


/**
 * @author michaeljoos
 *
 */
public class FiksuInterface {
    
    public enum OneTimeEvent {
        FirstLogin(RegistrationEvent.EVENT1),
        ;
        
        private final RegistrationEvent event;
        private OneTimeEvent(RegistrationEvent event) {
            this.event = event;
        }
        public RegistrationEvent value() {
            return event;
        }
    }
    
    public enum RecurringEvent {
        Referrals(PurchaseEvent.EVENT1),
        RegistrationScreen(PurchaseEvent.EVENT2),
        PreLogin_PeekButton(PurchaseEvent.EVENT3),
        Registration_JoinButton(PurchaseEvent.EVENT4),
        Registration_FacebookButton(PurchaseEvent.EVENT5),
        ;
        
        private final PurchaseEvent event;
        private RecurringEvent(PurchaseEvent event) {
            this.event = event;
        }
        public PurchaseEvent value() {
            return event;
        }
    }
    
    public static void sendEvent(OneTimeEvent event, Context context) {
        if (Config.getInstance().isFiksuEnabled() && FiksuTrackingManager.isAppTrackingEnabled()) {
            FiksuTrackingManager.uploadRegistration(context, event.value());
        }
    }

    public static void sendEvent(RecurringEvent event, Context context) {
        if (Config.getInstance().isFiksuEnabled() && FiksuTrackingManager.isAppTrackingEnabled()) {
            FiksuTrackingManager.uploadPurchase(context, event.value(), 0, "USD");
        }
    }

}
