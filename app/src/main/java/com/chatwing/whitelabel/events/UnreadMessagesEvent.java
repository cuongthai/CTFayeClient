/*
 * Copyright (C) 2014 ChatWing
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chatwing.whitelabel.events;

/**
 * This event will be fired when unread message counter is ready.
 * For example: Anywhere in your activity
 * <code>
 *   @Override
 *   protected void onResume() {
 *      super.onResume();
 *      ChatWing.instance(this).getBus().register(this);
 *      ChatWing.instance(this).loadUnreadCount();
 *   }
 *
 *   @Override
 *   protected void onPause() {
 *      super.onPause();
 *      ChatWing.instance(this).getBus().unregister(this);
 *   }
 *   @Subscribe
 *   public void onUnreadMessageLoaded(UnreadMessagesEvent event) {}
 * </code>
 *
 * <p>
 *     Please refer to demo app to see complete example of this
 * </p>
 *
 *@author cuongthai
 */
public class UnreadMessagesEvent {
    private final int mUnreadCount;

    public UnreadMessagesEvent(int count) {
        mUnreadCount = count;
    }

    public int getUnreadCount() {
        return mUnreadCount;
    }
}
