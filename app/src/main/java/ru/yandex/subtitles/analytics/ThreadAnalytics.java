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
package ru.yandex.subtitles.analytics;

public class ThreadAnalytics {

    private int mConversationsCount = 0;
    private int mOwnerStatementCount = 0;
    private int mVisavisStatementCount = 0;

    public ThreadAnalytics() {
    }

    public int getConversationsCount() {
        return mConversationsCount;
    }

    public void setConversationsCount(final int conversationsCount) {
        mConversationsCount = conversationsCount;
    }

    public int getVisavisStatementCount() {
        return mVisavisStatementCount;
    }

    public void setVisavisStatementCount(final int visavisStatementCount) {
        mVisavisStatementCount = visavisStatementCount;
    }

    public int getOwnerStatementCount() {
        return mOwnerStatementCount;
    }

    public void setOwnerStatementCount(final int ownerStatementCount) {
        mOwnerStatementCount = ownerStatementCount;
    }
}
