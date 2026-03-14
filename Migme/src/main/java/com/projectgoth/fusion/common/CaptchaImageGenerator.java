package com.projectgoth.fusion.common;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.security.SecureRandom;

public class CaptchaImageGenerator {
   private static final double TWO_PI = 6.283185307179586D;
   private static SecureRandom random = new SecureRandom();
   private Color backgroundColor;
   private int numOfBackgroudPolygons;
   private int maxBackgroundPolygonRadius;
   private int minBackgroundPolygonColor;
   private int maxBackgroundPolygonColor;
   private int maxBackgroudPolygonTiltingAngle;
   private int minPolygonSides;
   private int maxPolygonSides;
   private String fontName;
   private int fontStyle;
   private int minFontColor;
   private int maxFontColor;
   private int maxFontTiltingAngle;
   private double minFontSizeRatio;

   public CaptchaImageGenerator() {
      this.backgroundColor = Color.BLACK;
      this.numOfBackgroudPolygons = 8;
      this.maxBackgroundPolygonRadius = 8;
      this.minBackgroundPolygonColor = 30;
      this.maxBackgroundPolygonColor = 90;
      this.maxBackgroudPolygonTiltingAngle = 10;
      this.minPolygonSides = 3;
      this.maxPolygonSides = 6;
      this.fontName = "Arial";
      this.fontStyle = 1;
      this.minFontColor = 220;
      this.maxFontColor = 255;
      this.maxFontTiltingAngle = 18;
      this.minFontSizeRatio = 0.6D;
   }

   public Color getBackgroundColor() {
      return this.backgroundColor;
   }

   public void setBackgroundColor(Color backgroundColor) {
      this.backgroundColor = backgroundColor;
   }

   public int getNumOfBackgroudPolygons() {
      return this.numOfBackgroudPolygons;
   }

   public void setNumOfBackgroudPolygons(int numOfBackgroudPolygons) {
      this.numOfBackgroudPolygons = numOfBackgroudPolygons;
   }

   public int getMaxBackgroundPolygonRadius() {
      return this.maxBackgroundPolygonRadius;
   }

   public void setMaxBackgroundPolygonRadius(int maxBackgroundPolygonRadius) {
      this.maxBackgroundPolygonRadius = maxBackgroundPolygonRadius;
   }

   public int getMinBackgroundPolygonColor() {
      return this.minBackgroundPolygonColor;
   }

   public void setMinBackgroundPolygonColor(int minBackgroundPolygonColor) {
      this.minBackgroundPolygonColor = minBackgroundPolygonColor;
   }

   public int getMaxBackgroundPolygonColor() {
      return this.maxBackgroundPolygonColor;
   }

   public void setMaxBackgroundPolygonColor(int maxBackgroundPolygonColor) {
      this.maxBackgroundPolygonColor = maxBackgroundPolygonColor;
   }

   public int getMaxBackgroudPolygonTiltingAngle() {
      return this.maxBackgroudPolygonTiltingAngle;
   }

   public void setMaxBackgroudPolygonTiltingAngle(int maxBackgroudPolygonTiltingAngle) {
      this.maxBackgroudPolygonTiltingAngle = maxBackgroudPolygonTiltingAngle;
   }

   public int getMinPolygonSides() {
      return this.minPolygonSides;
   }

   public void setMinPolygonSides(int minPolygonSides) {
      this.minPolygonSides = minPolygonSides;
   }

   public int getMaxPolygonSides() {
      return this.maxPolygonSides;
   }

   public void setMaxPolygonSides(int maxPolygonSides) {
      this.maxPolygonSides = maxPolygonSides;
   }

   public String getFontName() {
      return this.fontName;
   }

   public void setFontName(String fontName) {
      this.fontName = fontName;
   }

   public int getFontStyle() {
      return this.fontStyle;
   }

   public void setFontStyle(int fontStyle) {
      this.fontStyle = fontStyle;
   }

   public int getMinFontColor() {
      return this.minFontColor;
   }

   public void setMinFontColor(int minFontColor) {
      this.minFontColor = minFontColor;
   }

   public int getMaxFontColor() {
      return this.maxFontColor;
   }

   public void setMaxFontColor(int maxFontColor) {
      this.maxFontColor = maxFontColor;
   }

   public int getMaxFontTiltingAngle() {
      return this.maxFontTiltingAngle;
   }

   public void setMaxFontTiltingAngle(int maxFontTiltingAngle) {
      this.maxFontTiltingAngle = maxFontTiltingAngle;
   }

   public double getMinFontSizeRatio() {
      return this.minFontSizeRatio;
   }

   public void setMinFontSizeRatio(double minFontSizeRatio) {
      this.minFontSizeRatio = minFontSizeRatio;
   }

   public BufferedImage newImage(int width, int height, String word) {
      BufferedImage image = new BufferedImage(width, height, 1);
      Graphics2D g = image.createGraphics();
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g.setColor(this.backgroundColor);
      g.fillRect(0, 0, width, height);

      int horizontalBlock;
      for(horizontalBlock = 0; horizontalBlock < this.numOfBackgroudPolygons; ++horizontalBlock) {
         double angle = Math.toRadians((double)(random.nextInt(this.maxBackgroudPolygonTiltingAngle * 2 + 1) - this.maxBackgroudPolygonTiltingAngle));
         g.rotate(angle);
         g.setColor(this.generateRandomColor(this.minBackgroundPolygonColor, this.maxBackgroundPolygonColor));
         g.fillPolygon(this.generateRandomPolygon(width, height, this.maxBackgroundPolygonRadius));
         g.rotate(-angle);
      }

      horizontalBlock = width / word.length();
      int maxFontSize = Math.min(horizontalBlock, height);
      int minFontSize = (int)((double)maxFontSize * this.minFontSizeRatio);

      for(int i = 0; i < word.length(); ++i) {
         double angle = Math.toRadians((double)(random.nextInt(this.maxFontTiltingAngle * 2 + 1) - this.maxFontTiltingAngle));
         int fontSize = random.nextInt(maxFontSize - minFontSize + 1) + minFontSize;
         int x = random.nextInt(horizontalBlock - fontSize + 1);
         int y = random.nextInt(height - fontSize + 1) + fontSize;
         g.setFont(new Font(this.fontName, this.fontStyle, fontSize));
         g.setColor(this.generateRandomColor(this.minFontColor, this.maxFontColor));
         g.rotate(angle);
         g.drawString(word.substring(i, i + 1), x, y);
         g.rotate(-angle);
         g.translate(horizontalBlock, 0);
      }

      g.dispose();
      return image;
   }

   private Color generateRandomColor(int minColor, int maxColor) {
      int range = maxColor - minColor + 1;
      return new Color(random.nextInt(range) + minColor, random.nextInt(range) + minColor, random.nextInt(range) + minColor);
   }

   private Polygon generateRandomPolygon(int boundX, int boundY, int maxRadius) {
      int sides = random.nextInt(this.maxPolygonSides - this.minPolygonSides + 1) + this.minPolygonSides;
      int centreX = random.nextInt(boundX);
      int centreY = random.nextInt(boundY);
      return this.generatePolygon(centreX, centreY, maxRadius, sides);
   }

   private Polygon generatePolygon(int centreX, int centreY, int radius, int sides) {
      Polygon polygon = new Polygon();
      double theta = 6.283185307179586D / (double)sides;

      for(int i = 0; i < sides; ++i) {
         polygon.addPoint((int)((double)centreX + (double)radius * Math.cos((double)i * theta)), (int)((double)centreY + (double)radius * Math.sin((double)i * theta)));
      }

      return polygon;
   }
}
