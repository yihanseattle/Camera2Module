package com.rokid.glass.camera;

import android.Manifest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowApplication;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Created by yihan on 12/18/18.
 *
 */
@RunWith(RobolectricTestRunner.class)
public class MainActivityTest {

    private ActivityController<MainActivity> mController;

    private MainActivity mActivity;

    private ShadowApplication application;

    String[] permissions = new String[]{Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Before
    public void setUp() {
        mController = Robolectric.buildActivity(MainActivity.class).create();
        mActivity = mController.get();
        application = new ShadowApplication();
        application.grantPermissions(permissions);

        mController.start().resume().visible();
    }

    @Test
    public void shouldNotBeNull() {
        // activity is not null
        assertNotNull(mActivity);
    }

    @Test
    public void hasAllPermissions() {
        // has all permissions
        assertNotNull(mActivity.getPermissionHelper());
        assertTrue(mActivity.getPermissionHelper().arePermissionsGranted());
    }

    @Test
    public void hasAllViews() {
        // has ImageView button
        assertNotNull(mActivity.getIVCameraButton());

        // has RecyclerView
        assertNotNull(mActivity.getRecyclerView());
        assertEquals(mActivity.getRecyclerView().getChildCount(), 4);
    }

    // @After => JUnit 4 annotation that specifies this method should be run after each test
    @After
    public void tearDown() {
        // Destroy activity after every test
        mController
            .pause()
            .stop()
            .destroy();
    }


}