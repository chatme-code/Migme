package com.projectgoth.fusion.reputation.cache;

import com.danga.MemCached.MemCachedClient;
import com.projectgoth.fusion.common.MemCachedUtils;
import java.io.Serializable;

public class ScoreFormulaParameters implements Serializable {
   public static final String REPUTATION_NAMESPACE = "REP";
   public static final String SCORE_FORMULA_PARAMETERS_KEY = "SCFP";
   private int chatRoomMessagesSentReasonableMax = 30;
   private int privateMessagesSentReasonableMax = 75;
   private int totalTimeReasonableMax = 6000;
   private int photosUploadedReasonableMax = 1;
   private int kicksInitiatedReasonableMax = 5;
   private int authenticatedReferralsReasonableMax = 3;
   private int rechargedAmountReasonableMax = 1;
   private int virtualGiftsReceivedReasonableMax = 2;
   private int virtualGiftsSentReasonableMax = 2;
   private int phoneCallDurationReasonableMax = 120;
   private int chatRoomMessagesSentWeight = 1;
   private int privateMessagesSentWeight = 1;
   private int totalTimeWeight = 1;
   private int photosUploadedWeight = 1;
   private int kicksInitiatedWeight = 1;
   private int authenticatedReferralsWeight = 1;
   private int rechargedAmountWeight = 1;
   private int virtualGiftsReceivedWeight = 1;
   private int virtualGiftsSentWeight = 1;
   private int phoneCallDurationWeight = 1;
   private int dailyHardCap = 400;
   private int dailyHardCapPercentage = 20;
   private int reasonableMaximumMinus50 = 2;
   private int reasonableMaximumMinus45 = 4;
   private int reasonableMaximumMinus40 = 6;
   private int reasonableMaximumMinus35 = 8;
   private int reasonableMaximumMinus30 = 10;
   private int reasonableMaximumMinus25 = 6;
   private int reasonableMaximumMinus20 = 4;
   private int reasonableMaximumMinus15 = 2;
   private int reasonableMaximumMinus10 = 0;

   public static ScoreFormulaParameters getScoreFormulaParameters(MemCachedClient instance) {
      return (ScoreFormulaParameters)instance.get(MemCachedUtils.getCacheKeyInNamespace("REP", "SCFP"));
   }

   public static boolean setScoreFormulaParameters(MemCachedClient instance, ScoreFormulaParameters params) {
      return instance.set(MemCachedUtils.getCacheKeyInNamespace("REP", "SCFP"), params);
   }

   public int getChatRoomMessagesSentReasonableMax() {
      return this.chatRoomMessagesSentReasonableMax;
   }

   public void setChatRoomMessagesSentReasonableMax(int chatRoomMessagesSentReasonableMax) {
      this.chatRoomMessagesSentReasonableMax = chatRoomMessagesSentReasonableMax;
   }

   public int getPrivateMessagesSentReasonableMax() {
      return this.privateMessagesSentReasonableMax;
   }

   public void setPrivateMessagesSentReasonableMax(int privateMessagesSentReasonableMax) {
      this.privateMessagesSentReasonableMax = privateMessagesSentReasonableMax;
   }

   public int getTotalTimeReasonableMax() {
      return this.totalTimeReasonableMax;
   }

   public void setTotalTimeReasonableMax(int totalTimeReasonableMax) {
      this.totalTimeReasonableMax = totalTimeReasonableMax;
   }

   public int getPhotosUploadedReasonableMax() {
      return this.photosUploadedReasonableMax;
   }

   public void setPhotosUploadedReasonableMax(int photosUploadedReasonableMax) {
      this.photosUploadedReasonableMax = photosUploadedReasonableMax;
   }

   public int getKicksInitiatedReasonableMax() {
      return this.kicksInitiatedReasonableMax;
   }

   public void setKicksInitiatedReasonableMax(int kicksInitiatedReasonableMax) {
      this.kicksInitiatedReasonableMax = kicksInitiatedReasonableMax;
   }

   public int getAuthenticatedReferralsReasonableMax() {
      return this.authenticatedReferralsReasonableMax;
   }

   public void setAuthenticatedReferralsReasonableMax(int authenticatedReferralsReasonableMax) {
      this.authenticatedReferralsReasonableMax = authenticatedReferralsReasonableMax;
   }

   public int getRechargedAmountReasonableMax() {
      return this.rechargedAmountReasonableMax;
   }

   public void setRechargedAmountReasonableMax(int rechargedAmountReasonableMax) {
      this.rechargedAmountReasonableMax = rechargedAmountReasonableMax;
   }

   public int getChatRoomMessagesSentWeight() {
      return this.chatRoomMessagesSentWeight;
   }

   public void setChatRoomMessagesSentWeight(int chatRoomMessagesSentWeight) {
      this.chatRoomMessagesSentWeight = chatRoomMessagesSentWeight;
   }

   public int getPrivateMessagesSentWeight() {
      return this.privateMessagesSentWeight;
   }

   public void setPrivateMessagesSentWeight(int privateMessagesSentWeight) {
      this.privateMessagesSentWeight = privateMessagesSentWeight;
   }

   public int getTotalTimeWeight() {
      return this.totalTimeWeight;
   }

   public void setTotalTimeWeight(int totalTimeWeight) {
      this.totalTimeWeight = totalTimeWeight;
   }

   public int getPhotosUploadedWeight() {
      return this.photosUploadedWeight;
   }

   public void setPhotosUploadedWeight(int photosUploadedWeight) {
      this.photosUploadedWeight = photosUploadedWeight;
   }

   public int getKicksInitiatedWeight() {
      return this.kicksInitiatedWeight;
   }

   public void setKicksInitiatedWeight(int kicksInitiatedWeight) {
      this.kicksInitiatedWeight = kicksInitiatedWeight;
   }

   public int getAuthenticatedReferralsWeight() {
      return this.authenticatedReferralsWeight;
   }

   public void setAuthenticatedReferralsWeight(int authenticatedReferralsWeight) {
      this.authenticatedReferralsWeight = authenticatedReferralsWeight;
   }

   public int getRechargedAmountWeight() {
      return this.rechargedAmountWeight;
   }

   public void setRechargedAmountWeight(int rechargedAmountWeight) {
      this.rechargedAmountWeight = rechargedAmountWeight;
   }

   public int getDailyHardCap() {
      return this.dailyHardCap;
   }

   public void setDailyHardCap(int dailyHardCap) {
      this.dailyHardCap = dailyHardCap;
   }

   public int getDailyHardCapPercentage() {
      return this.dailyHardCapPercentage;
   }

   public void setDailyHardCapPercentage(int dailyHardCapPercentage) {
      this.dailyHardCapPercentage = dailyHardCapPercentage;
   }

   public int getReasonableMaximumMinus50() {
      return this.reasonableMaximumMinus50;
   }

   public void setReasonableMaximumMinus50(int reasonableMaximumMinus50) {
      this.reasonableMaximumMinus50 = reasonableMaximumMinus50;
   }

   public int getReasonableMaximumMinus45() {
      return this.reasonableMaximumMinus45;
   }

   public void setReasonableMaximumMinus45(int reasonableMaximumMinus45) {
      this.reasonableMaximumMinus45 = reasonableMaximumMinus45;
   }

   public int getReasonableMaximumMinus40() {
      return this.reasonableMaximumMinus40;
   }

   public void setReasonableMaximumMinus40(int reasonableMaximumMinus40) {
      this.reasonableMaximumMinus40 = reasonableMaximumMinus40;
   }

   public int getReasonableMaximumMinus35() {
      return this.reasonableMaximumMinus35;
   }

   public void setReasonableMaximumMinus35(int reasonableMaximumMinus35) {
      this.reasonableMaximumMinus35 = reasonableMaximumMinus35;
   }

   public int getReasonableMaximumMinus30() {
      return this.reasonableMaximumMinus30;
   }

   public void setReasonableMaximumMinus30(int reasonableMaximumMinus30) {
      this.reasonableMaximumMinus30 = reasonableMaximumMinus30;
   }

   public int getReasonableMaximumMinus25() {
      return this.reasonableMaximumMinus25;
   }

   public void setReasonableMaximumMinus25(int reasonableMaximumMinus25) {
      this.reasonableMaximumMinus25 = reasonableMaximumMinus25;
   }

   public int getReasonableMaximumMinus20() {
      return this.reasonableMaximumMinus20;
   }

   public void setReasonableMaximumMinus20(int reasonableMaximumMinus20) {
      this.reasonableMaximumMinus20 = reasonableMaximumMinus20;
   }

   public int getReasonableMaximumMinus15() {
      return this.reasonableMaximumMinus15;
   }

   public void setReasonableMaximumMinus15(int reasonableMaximumMinus15) {
      this.reasonableMaximumMinus15 = reasonableMaximumMinus15;
   }

   public int getReasonableMaximumMinus10() {
      return this.reasonableMaximumMinus10;
   }

   public void setReasonableMaximumMinus10(int reasonableMaximumMinus10) {
      this.reasonableMaximumMinus10 = reasonableMaximumMinus10;
   }

   public int getVirtualGiftsReceivedReasonableMax() {
      return this.virtualGiftsReceivedReasonableMax;
   }

   public void setVirtualGiftsReceivedReasonableMax(int virtualGiftsReceivedReasonableMax) {
      this.virtualGiftsReceivedReasonableMax = virtualGiftsReceivedReasonableMax;
   }

   public int getVirtualGiftsSentReasonableMax() {
      return this.virtualGiftsSentReasonableMax;
   }

   public void setVirtualGiftsSentReasonableMax(int virtualGiftsSentReasonableMax) {
      this.virtualGiftsSentReasonableMax = virtualGiftsSentReasonableMax;
   }

   public int getPhoneCallDurationReasonableMax() {
      return this.phoneCallDurationReasonableMax;
   }

   public void setPhoneCallDurationReasonableMax(int phoneCallDurationReasonableMax) {
      this.phoneCallDurationReasonableMax = phoneCallDurationReasonableMax;
   }

   public int getVirtualGiftsReceivedWeight() {
      return this.virtualGiftsReceivedWeight;
   }

   public void setVirtualGiftsReceivedWeight(int virtualGiftsReceivedWeight) {
      this.virtualGiftsReceivedWeight = virtualGiftsReceivedWeight;
   }

   public int getVirtualGiftsSentWeight() {
      return this.virtualGiftsSentWeight;
   }

   public void setVirtualGiftsSentWeight(int virtualGiftsSentWeight) {
      this.virtualGiftsSentWeight = virtualGiftsSentWeight;
   }

   public int getPhoneCallDurationWeight() {
      return this.phoneCallDurationWeight;
   }

   public void setPhoneCallDurationWeight(int phoneCallDurationWeight) {
      this.phoneCallDurationWeight = phoneCallDurationWeight;
   }
}
