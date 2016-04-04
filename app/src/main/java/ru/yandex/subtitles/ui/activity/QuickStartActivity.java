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
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import ru.yandex.subtitles.Preferences;
import ru.yandex.subtitles.R;
import ru.yandex.subtitles.analytics.Analytics;
import ru.yandex.subtitles.content.data.Phrase;
import ru.yandex.subtitles.ui.fragment.interactive.QualityFeedbackDialogFragment;
import ru.yandex.subtitles.ui.fragment.interactive.TutorialDialogFragment;
import ru.yandex.subtitles.ui.fragment.interactive.WhatsNewDialogFragment;
import ru.yandex.subtitles.ui.fragment.navigation.NavigationMenuFragment;
import ru.yandex.subtitles.ui.fragment.phrases.PhrasesFragment;
import ru.yandex.subtitles.utils.ApplicationUtils;
import ru.yandex.subtitles.utils.ViewUtils;

public class QuickStartActivity extends AbstractActivity
        implements NavigationMenuFragment.OnNavigationClickListener,
        PhrasesFragment.OnPhraseClickListener,
        DialogAppearanceHelper.OnShowInteractiveDialogListener {

    @NonNull
    public static Intent createStartIntent(final Context context) {
        final Intent intent = new Intent(context, QuickStartActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    public static void start(final Context context) {
        final Intent intent = new Intent(context, QuickStartActivity.class);
        context.startActivity(intent);
    }

    private DrawerLayout mDrawerLayout;
    private DrawerCallbacksAdapter mDrawerCallbacksAdapter;

    private DialogAppearanceHelper mDialogAppearanceHelper;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_start);

        final Toolbar toolbar = findView(R.id.toolbar);
        if (ApplicationUtils.hasLollipop()) {
            toolbar.setElevation(0.f);
        }
        toolbar.setNavigationIcon(R.drawable.ic_hamburger_black);
        setSupportActionBar(toolbar);

        mDrawerCallbacksAdapter = new DrawerCallbacksAdapter();

        mDrawerLayout = findView(R.id.drawer);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerLayout.setDrawerListener(mDrawerCallbacksAdapter);

        final View menuContainer = findView(R.id.menu);
        final ViewGroup.LayoutParams lp = menuContainer.getLayoutParams();
        lp.width = ViewUtils.getNavigationDrawerWidth(getResources());
        menuContainer.setLayoutParams(lp);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.menu, NavigationMenuFragment.newInstance())
                    .commit();
            showFragmentNoAnimate(PhrasesFragment.newInstance(), PhrasesFragment.TAG);
        }

        mDialogAppearanceHelper = new DialogAppearanceHelper(this, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        final Preferences.InstallationInfo installationInfo = Preferences.getInstance().getInstallationInfo();
        if (installationInfo.haveBeenInstalledOrUpdated()) {
            showWhatsNewDialogFragment(installationInfo);

        } else {
            mDialogAppearanceHelper.start();
        }
    }

    private void showWhatsNewDialogFragment(@NonNull final Preferences.InstallationInfo installationInfo) {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(WhatsNewDialogFragment.TAG) == null) {
            final WhatsNewDialogFragment dialog = WhatsNewDialogFragment.newInstance(installationInfo);
            dialog.show(getSupportFragmentManager(), WhatsNewDialogFragment.TAG);
        }
    }

    @Override
    public void onShowQualityFeedback() {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(QualityFeedbackDialogFragment.TAG) == null) {
            final QualityFeedbackDialogFragment dialog = QualityFeedbackDialogFragment.newInstance();
            dialog.show(getSupportFragmentManager(), QualityFeedbackDialogFragment.TAG);
        }
    }

    @Override
    public void onShowTutorial() {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(TutorialDialogFragment.TAG) == null) {
            final TutorialDialogFragment dialog = TutorialDialogFragment.newInstance();
            dialog.show(getSupportFragmentManager(), TutorialDialogFragment.TAG);
        }
    }


    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
            case android.R.id.home:
                toggleNavigationDrawer();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSettingsMenuItemClick() {
        mDrawerCallbacksAdapter.runOnDrawerClose(new Runnable() {
            @Override
            public void run() {
                Analytics.onNavigationMenuSettingsClick();
                SettingsActivity.start(QuickStartActivity.this);
            }
        });
    }

    @Override
    public void onStartConversationMenuItemClick() {
        mDrawerCallbacksAdapter.runOnDrawerClose(new Runnable() {
            @Override
            public void run() {
                Analytics.onNavigationMenuStartConversationClick();
                ConversationActivity.startConversation(QuickStartActivity.this);
            }
        });
    }

    @Override
    public void onConversationsMenuItemClick() {
        mDrawerCallbacksAdapter.runOnDrawerClose(new Runnable() {
            @Override
            public void run() {
                Analytics.onNavigationMenuConversationsClick();
                ConversationsActivity.start(QuickStartActivity.this);
            }
        });
    }

    @Override
    public void onYandexAppClick(@NonNull final String packageName) {
        ApplicationUtils.openGooglePlay(this, packageName);
    }

    @Override
    public void onPhraseClick(@NonNull final Phrase phrase) {
        ConversationActivity.startConversation(this, phrase.getText());
    }

    @Override
    public void onStartConversationClick() {
        ConversationActivity.startConversation(this);
    }

    private void toggleNavigationDrawer() {
        if (isDrawerOpen()) {
            closeDrawer();
        } else {
            openDrawer();
        }
    }

    private void openDrawer() {
        mDrawerLayout.openDrawer(GravityCompat.START);
    }

    @Override
    public void closeDrawer() {
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    @Override
    public boolean isDrawerOpen() {
        return mDrawerLayout.isDrawerOpen(GravityCompat.START);
    }

    @Override
    public void lockDrawer() {
        mDrawerLayout.requestDisallowInterceptTouchEvent(true);
    }

    @Override
    public void unlockDrawer() {
        mDrawerLayout.requestDisallowInterceptTouchEvent(false);
    }

    @Override
    public void onBackPressed() {
        if (isDrawerOpen()) {
            closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    private static class DrawerCallbacksAdapter implements DrawerLayout.DrawerListener {

        private Runnable mRunOnDrawerCloseRunnable;

        public DrawerCallbacksAdapter() {
        }

        public void runOnDrawerClose(@NonNull final Runnable runOnDrawerCloseRunnable) {
            mRunOnDrawerCloseRunnable = runOnDrawerCloseRunnable;
        }

        @Override
        public void onDrawerClosed(final View drawerView) {
            if (mRunOnDrawerCloseRunnable != null) {
                mRunOnDrawerCloseRunnable.run();
                mRunOnDrawerCloseRunnable = null;
            }
        }

        @Override
        public void onDrawerOpened(final View drawerView) {
        }

        @Override
        public void onDrawerSlide(final View drawerView, final float slideOffset) {
        }

        @Override
        public void onDrawerStateChanged(final int newState) {
        }

    }

}