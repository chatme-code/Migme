/**
 * Copyright (c) 2013 Project Goth
 *
 * AttachmentPagerController.java
 * Created 27 Mar, 2014, 10:05:02 am
 */

package com.projectgoth.controller;

import com.projectgoth.enums.AttachmentType;

/**
 * @author dan
 * 
 */
public class AttachmentPagerCalculator {

    private static final int EMOTICON_COLUMNS = 6;
    private static final int STICKER_COLUMNS  = 4;

    public static final int EMOTICON_ROWS    = 3;
    public static final int STICKER_ROWS     = 2;

    int                packId;
    int                numOfRow;
    int                numOfColumn;

    public AttachmentPagerCalculator(int packId) {
        this.packId = packId;
        resetNumOfRowAndColumnByPackId();
    }

    private void resetNumOfRowAndColumnByPackId() {
        // row number and column number
        if (packId == AttachmentType.EMOTICON.value || packId == AttachmentType.RECENT_EMOTICON.value) {
            numOfColumn = EMOTICON_COLUMNS;
            numOfRow = EMOTICON_ROWS;
        } else {
            numOfColumn = STICKER_COLUMNS;
            numOfRow = STICKER_ROWS;
        }
    }
    
    public int[] getDataRangeOfPage(int pageIndex, int dataSize) {
        int[] dataRange = new int[2];
        // set list data
        int numOfPage = numOfRow * numOfColumn;
        int start = pageIndex * numOfPage;
        int end = start + numOfPage;
        // if it is last page
        if (start < dataSize && end > dataSize) {
            end = dataSize;
        }
        if (end <= dataSize) {
            dataRange[0] = start;
            dataRange[1] = end;
        } else {
            dataRange = null;
        }

        return dataRange;
    }

    public int getNumOfPage(int dataSize) {
        int numOfPage = numOfRow * numOfColumn;
        int pagerNum = dataSize / numOfPage;
        if (pagerNum * numOfPage < dataSize) {
            pagerNum++;
        }

        return pagerNum;

    }

    /**
     * @return the packId
     */
    public int getPackId() {
        return packId;
    }

    /**
     * @param packId
     *            the packId to set
     */
    public void setPackId(int packId) {
        this.packId = packId;
        resetNumOfRowAndColumnByPackId();
    }

    /**
     * @return the numOfRow
     */
    public int getNumOfRow() {
        return numOfRow;
    }

    /**
     * @param numOfRow
     *            the numOfRow to set
     */
    public void setNumOfRow(int numOfRow) {
        this.numOfRow = numOfRow;
    }

    /**
     * @return the numOfColumn
     */
    public int getNumOfColumn() {
        return numOfColumn;
    }

    /**
     * @param numOfColumn
     *            the numOfColumn to set
     */
    public void setNumOfColumn(int numOfColumn) {
        this.numOfColumn = numOfColumn;
    }

}
