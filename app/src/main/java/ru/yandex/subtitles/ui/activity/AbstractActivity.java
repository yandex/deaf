/**
 * Copyright 2015 YA LLC
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.yandex.subtitles.ui.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import ru.yandex.subtitles.R;
import ru.yandex.subtitles.analytics.Analytics;
import ru.yandex.subtitles.ui.fragment.OnBackKeyEventListener;
import ru.yandex.subtitles.utils.ApplicationUtils;
import ru.yandex.subtitles.utils.ViewUtils;

/**
 * Base activity that provides methods for showing fragments.
 * Notice that activity view should have FrameLayout with id=R.id.content
 * which will be used as fragment container.
 */
public abstract class AbstractActivity extends AppCompatActivity implements ActivityResultListener {

    private boolean mActivityResumed;
    private boolean mActivityDestroyed;
    private boolean mFragmentsStateSaved;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        ApplicationUtils.brandGlowColor(this);
        setupLollipopStatusBarFlags();
        mFragmentsStateSaved = false;
        mActivityDestroyed = false;
        super.onCreate(savedInstanceState);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setupLollipopStatusBarFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    public <V extends View> V findView(final int id) {
        return ViewUtils.findView(this, id);
    }

    public void showFragment(final Fragment fragment, final String tag) {
        showFragment(fragment, tag, true, false, true, false);
    }

    public void showFragmentNoAnimate(final Fragment fragment, final String tag) {
        showFragment(fragment, tag, true, false, false, false);
    }

    public void showFragmentNoAnimateAllowingStateLoss(final Fragment fragment, final String tag) {
        showFragment(fragment, tag, true, false, false, true);
    }

    @SuppressWarnings("all")
    public void showFragment(final Fragment fragment, final String tag,
                             final boolean addToBackStack, final boolean clearBackStack,
                             final boolean animate, final boolean allowingStateLoss) {
        if (mActivityDestroyed || (mFragmentsStateSaved && !allowingStateLoss)) {
            // This is a workaround.
            // For some reason onClick() event can occur when activity is stopped.
            return;
        }

        final FragmentManager fm = getSupportFragmentManager();
        if (clearBackStack) {
            fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        final FragmentTransaction ft = fm.beginTransaction();
        if (addToBackStack) {
            ft.addToBackStack(tag);
        }
        if (animate) {
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        }
        ft.replace(R.id.content, fragment, tag);
        if (allowingStateLoss) {
            ft.commitAllowingStateLoss();
        } else {
            ft.commit();
        }
    }

    @Nullable
    public Fragment switchToFragment(final String tag) {
        if (mFragmentsStateSaved) {
            return null;

        } else {
            final FragmentManager fm = getSupportFragmentManager();
            final Fragment fragment = fm.findFragmentByTag(tag);
            if (fragment != null) {
                fm.popBackStack(tag, 0); // 0 means that fragments will be popped to fragment specified by tag
            }
            return fragment;
        }
    }

    @Override
    public void onBackPressed() {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        final int backStackEntryCount = fragmentManager.getBackStackEntryCount();
        if (backStackEntryCount > 0) {
            final OnBackKeyEventListener l = getContentFragment(OnBackKeyEventListener.class);
            if (l == null || !l.onBackPressed()) {
                if (backStackEntryCount > 1) {
                    onBackPressedAction();
                } else {
                    finish();
                }
            }
        } else {
            onBackPressedAction();
        }
    }

    public void onBackPressedAction() {
        super.onBackPressed();
    }

    @Nullable
    public <F> F getContentFragment(final Class<F> cls) {
        return getFragment(cls, R.id.content);
    }

    @Nullable
    public <F> F getFragment(final Class<F> cls, final int id) {
        F f = null;
        try {
            f = cls.cast(getSupportFragmentManager().findFragmentById(id));
        } catch (final ClassCastException e) {
            // Ignore it
        }
        return f;
    }

    @Override
    protected void onStart() {
        mFragmentsStateSaved = false;
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Analytics.onResumeActivity(this);
        mActivityResumed = true;
    }

    public boolean isActivityResumed() {
        return mActivityResumed;
    }

    @Override
    public void onPause() {
        Analytics.onPauseActivity(this);
        mActivityResumed = false;
        super.onPause();
    }

    @Override
    protected void onStop() {
        mFragmentsStateSaved = true;
        super.onStop();
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        mActivityDestroyed = true;
        super.onDestroy();
    }

}