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
package ru.yandex.subtitles.ui.adapter;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import ru.yandex.subtitles.analytics.ZoomedMessage;
import ru.yandex.subtitles.ui.fragment.conversations.ZoomedMessageFragment;
import ru.yandex.subtitles.ui.fragment.conversations.ZoomedMessagesFragment;

public class ZoomedMessagesAdapter extends AbstractFragmentViewPagerAdapter<ZoomedMessage> {

    private Fragment mTargetFragment;

    public ZoomedMessagesAdapter(@NonNull final Fragment targetFragment,
                                 @NonNull final FragmentManager fragmentManager) {
        super(targetFragment.getActivity(), fragmentManager);
        mTargetFragment = targetFragment;
    }

    @NonNull
    @Override
    public Fragment getItem(final int position) {
        final Fragment fragment = ZoomedMessageFragment.newInstance(getData(position));
        fragment.setTargetFragment(mTargetFragment, ZoomedMessagesFragment.EXIT_FULLSCREEN_REQUEST_CODE);
        return fragment;
    }

}
