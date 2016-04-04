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
package ru.yandex.subtitles.ui.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import java.util.List;

import ru.yandex.subtitles.R;
import ru.yandex.subtitles.utils.ApplicationUtils;
import ru.yandex.subtitles.utils.ViewUtils;

/**
 * Base fragment class that provides ability to work with in-fragment toolbar or activity's action bar.
 * It also holds link to the fragment listener {@see #mController} that automatically sets up in
 * {@link #onAttach(Context)} and clears on {@link #onDetach()}.
 * AbstractFragment handles {@link #onBackPressed()} and {@link #onActivityResult(int, int, Intent)}
 * events and forwards them to the nested fragments.
 */
public abstract class AbstractFragment<C> extends Fragment implements OnBackKeyEventListener,
        Toolbar.OnMenuItemClickListener {

    private static final String LOG_TAG = "AbstractFragment";

    private boolean mFragmentAttached = false;
    protected C mController;

    private Toolbar mToolbar;
    private boolean mHasBackNavigation = true;

    public void setHasBackNavigation(final boolean hasBackNavigation) {
        mHasBackNavigation = hasBackNavigation;
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupToolbar();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);

        mFragmentAttached = true;
        try {
            mController = (C) context;
        } catch (final ClassCastException cce) {
            // Ignore it!
        }
    }

    @Override
    public void onPause() {
        ViewUtils.hideSoftwareKeyboard(getView());
        super.onPause();
    }

    @Override
    public void onDetach() {
        mFragmentAttached = false;
        mController = null;
        super.onDetach();
    }

    public boolean isAttached() {
        return mFragmentAttached;
    }

    public <V extends View> V findView(final int id) {
        return ViewUtils.findView(getView(), id);
    }

    @Override
    public boolean onBackPressed() {
        boolean backPressedHandled = false;
        final FragmentManager fragmentManager = getChildFragmentManager();
        final List<Fragment> children = fragmentManager.getFragments();
        if (children != null) {
            for (final Fragment child : children) {
                if (child != null && OnBackKeyEventListener.class.isInstance(child) && child.getUserVisibleHint()) {
                    backPressedHandled |= ((OnBackKeyEventListener) child).onBackPressed();
                }
            }
        }
        return backPressedHandled;
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final List<Fragment> fragments = getChildFragmentManager().getFragments();
        if (fragments != null) {
            for (final Fragment child : fragments) {
                if (child != null) {
                    child.onActivityResult(requestCode, resultCode, data);
                }
            }
        }
    }

    @Nullable
    public Toolbar getToolbar() {
        return mToolbar;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setupToolbar() {
        mToolbar = findView(R.id.toolbar);
        if (mToolbar != null) {
            if (ApplicationUtils.hasLollipop()) {
                mToolbar.setElevation(0.f);
            }
            if (mHasBackNavigation) {
                mToolbar.setNavigationIcon(R.drawable.ic_ab_back_mtrl_black);
                mToolbar.setNavigationOnClickListener(mNavigationClickListener);
            }
            if (hasOptionsMenu()) {
                mToolbar.setOnMenuItemClickListener(this);
                createToolbarOptionsMenu(mToolbar);
            }
        }
    }

    private final View.OnClickListener mNavigationClickListener = new View.OnClickListener() {

        @Override
        public void onClick(final View v) {
            onNavigationClick();
        }

    };

    public void onNavigationClick() {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.onBackPressed();
        }
    }

    @Override
    public boolean onMenuItemClick(final MenuItem item) {
        return onOptionsItemSelected(item);
    }

    private void createToolbarOptionsMenu(@NonNull final Toolbar toolbar) {
        final Menu menu = toolbar.getMenu();
        menu.clear();
        onCreateOptionsMenu(menu, getMenuInflater());
        onPrepareOptionsMenu(menu);
    }

    @NonNull
    public MenuInflater getMenuInflater() {
        return getActivity().getMenuInflater();
    }

    public void invalidateOptionsMenu() {
        if (mToolbar != null) {
            createToolbarOptionsMenu(mToolbar);
        } else {
            final FragmentActivity activity = getActivity();
            if (activity != null) {
                activity.supportInvalidateOptionsMenu();
            }
        }
    }

    public void openContextMenu(final View view) {
        getActivity().openContextMenu(view);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setStatusBarColor(final int color) {
        if (mFragmentAttached && ApplicationUtils.hasLollipop()) {
            getActivity().getWindow().setStatusBarColor(color);
        }
    }

    public void setTitle(@StringRes final int titleRes) {
        setTitle(getText(titleRes));
    }

    public void setTitle(@Nullable final CharSequence title) {
        if (mToolbar != null) {
            mToolbar.setTitle(title);
        } else {
            final FragmentActivity activity = getActivity();
            if (activity != null) {
                activity.setTitle(title);
            }
        }
    }

}