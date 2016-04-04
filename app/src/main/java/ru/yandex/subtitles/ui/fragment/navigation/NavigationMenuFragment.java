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
package ru.yandex.subtitles.ui.fragment.navigation;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import ru.yandex.subtitles.R;
import ru.yandex.subtitles.ui.adapter.NavigationMenuListAdapter;
import ru.yandex.subtitles.ui.fragment.AbstractFragment;
import ru.yandex.subtitles.utils.ViewUtils;

public class NavigationMenuFragment extends AbstractFragment<NavigationMenuFragment.OnNavigationClickListener>
        implements AdapterView.OnItemClickListener, View.OnClickListener {

    public interface DrawerController {

        void closeDrawer();

        boolean isDrawerOpen();

        void lockDrawer();

        void unlockDrawer();

    }


    public interface OnNavigationClickListener extends DrawerController {

        void onSettingsMenuItemClick();

        void onStartConversationMenuItemClick();

        void onConversationsMenuItemClick();

        void onYandexAppClick(@NonNull final String packageName);

    }

    private static final String LOG_TAG = "NavigationMenuFragment";

    public static NavigationMenuFragment newInstance() {
        return new NavigationMenuFragment();
    }

    private final static List<YandexAppItem> sYandexApps = new ArrayList<YandexAppItem>();

    static {
        sYandexApps.add(new YandexAppItem(R.drawable.ic_yandex_browser, R.string.yandex_browser, R.string.yandex_browser_package));
        sYandexApps.add(new YandexAppItem(R.drawable.ic_yandex_navigator, R.string.yandex_navigator, R.string.yandex_navigator_package));
        sYandexApps.add(new YandexAppItem(R.drawable.ic_yandex_disk, R.string.yandex_disk, R.string.yandex_disk_package));
        sYandexApps.add(new YandexAppItem(R.drawable.ic_yandex_translate, R.string.yandex_translate, R.string.yandex_translate_package));
        sYandexApps.add(new YandexAppItem(R.drawable.ic_yandex_city, R.string.yandex_city, R.string.yandex_city_package));
        sYandexApps.add(new YandexAppItem(R.drawable.ic_yandex_maps, R.string.yandex_maps, R.string.yandex_maps_package));
        sYandexApps.add(new YandexAppItem(R.drawable.ic_yandex_metro, R.string.yandex_metro, R.string.yandex_metro_package));
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_navigation_menu, container, false);
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Context context = getActivity();
        final LayoutInflater inflater = LayoutInflater.from(context);

        final ListView menuListView = findView(R.id.list);

        final View headerView = new View(context);
        final int dp4 = getResources().getDimensionPixelSize(R.dimen.dimen_4dp);
        final AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp4);
        headerView.setLayoutParams(lp);
        menuListView.addHeaderView(headerView, null, false);

        final View footerView = inflater.inflate(R.layout.layout_navigation_menu_footer, menuListView, false);
        menuListView.addFooterView(footerView, null, false);

        final ListAdapter adapter = createNavigationListAdapter();
        menuListView.setAdapter(adapter);
        menuListView.setOnItemClickListener(this);

        final ViewGroup yandexAppsContainer = findView(R.id.yandex_apps_container);
        populateYandexApps(inflater, yandexAppsContainer, sYandexApps);
    }

    @NonNull
    private ListAdapter createNavigationListAdapter() {
        final NavigationMenuListAdapter adapter = new NavigationMenuListAdapter(getActivity());

        adapter.add(new MenuItem(R.string.start_conversation, R.string.start_conversation, R.drawable.ic_start_conversation));
        adapter.add(new MenuItem(R.string.conversations, R.string.conversations, R.drawable.ic_conversations));
        adapter.add(new MenuItem(R.string.settings, R.string.settings, R.drawable.ic_settings));

        return adapter;
    }

    private void populateYandexApps(final LayoutInflater inflater, final ViewGroup container,
                                    final List<YandexAppItem> items) {
        for (final YandexAppItem item : items) {
            final View itemView = getYandexAppView(inflater, container, item);
            container.addView(itemView);
        }
    }

    private View getYandexAppView(final LayoutInflater inflater, final ViewGroup container,
                                  final YandexAppItem item) {
        final View itemView = inflater.inflate(R.layout.list_item_yandex_app, container, false);

        final View yandexAppView = ViewUtils.findView(itemView, R.id.yandex_app);
        yandexAppView.setTag(item.getPackageNameResId());
        yandexAppView.setOnClickListener(this);

        final ImageView imageView = ViewUtils.findView(itemView, R.id.yandex_app_icon);
        imageView.setImageResource(item.getIconResId());

        final TextView nameView = ViewUtils.findView(itemView, R.id.yandex_app_name);
        nameView.setText(item.getTitleResId());

        // We should disable handling touch events by drawer if there is scrollable content
        itemView.setOnTouchListener(new DisallowInterceptTouchEventListener(itemView, mController));

        return itemView;
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        mController.closeDrawer();

        switch (parent.getId()) {
            case R.id.list:
                onMenuItemClicked(parent, position);
                break;

            default:
                break;
        }
    }

    public void onMenuItemClicked(final AdapterView<?> parent, final int position) {
        final MenuItem item = (MenuItem) parent.getItemAtPosition(position);
        switch (item.getId()) {
            case R.string.settings:
                mController.onSettingsMenuItemClick();
                break;

            case R.string.start_conversation:
                mController.onStartConversationMenuItemClick();
                break;

            case R.string.conversations:
                mController.onConversationsMenuItemClick();
                break;
        }
    }

    @Override
    public void onClick(final View v) {
        mController.closeDrawer();

        switch (v.getId()) {
            case R.id.yandex_app:
                final Integer packageNameResId = (Integer) v.getTag();
                mController.onYandexAppClick(getString(packageNameResId));
                break;
        }
    }

    // Please refer for more details:
    // http://stackoverflow.com/questions/24310277/how-to-use-a-view-pager-inside-a-navigation-drawer
    private static class DisallowInterceptTouchEventListener implements View.OnTouchListener {

        private final WeakReference<View> mWeakView;
        private final WeakReference<DrawerController> mWeakController;

        public DisallowInterceptTouchEventListener(final View view, final DrawerController controller) {
            mWeakView = new WeakReference<View>(view);
            mWeakController = new WeakReference<DrawerController>(controller);
        }

        @Override
        public boolean onTouch(final View v, final MotionEvent event) {
            final View view = mWeakView.get();
            final DrawerController controller = mWeakController.get();

            if (view != null && controller != null) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        controller.lockDrawer();
                        break;

                    case MotionEvent.ACTION_UP:
                        controller.unlockDrawer();
                        break;
                }
                view.onTouchEvent(event);
            }
            return true;
        }

    }

    @Override
    public boolean onBackPressed() {
        if (mController.isDrawerOpen()) {
            mController.closeDrawer();
            return true;
        } else {
            return false;
        }
    }

}