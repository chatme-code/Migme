package com.projectgoth.tests;

import android.content.Context;
import android.test.AndroidTestCase;

public class MockitoTestCase extends AndroidTestCase {
    protected Context mTargetContext;
    @Override
    public void setUp() throws Exception {
        super.setUp();
        mTargetContext = getContext();
        //noinspection ConstantConditions
        System.setProperty("dexmaker.dexcache", mTargetContext.getCacheDir().getPath());
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }
}

