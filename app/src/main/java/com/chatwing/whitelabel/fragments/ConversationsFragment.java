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

package com.chatwing.whitelabel.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.View;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.adapters.ConversationsAdapter;
import com.chatwing.whitelabel.contentproviders.ChatWingContentProvider;
import com.chatwing.whitelabel.events.SyncCommunicationBoxEvent;
import com.chatwing.whitelabel.events.UserSelectedConversationEvent;
import com.chatwing.whitelabel.managers.CurrentConversationManager;
import com.chatwing.whitelabel.tables.ConversationTable;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

/**
 * Author: Huy Nguyen
 * Date: 7/25/13
 * Time: 9:29 AM
 */
public class ConversationsFragment extends CommunicationBoxesFragment {
    @Inject
    CurrentConversationManager mCurrentConversationManager;


    public ConversationsFragment() {

    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDelegate.back(ConversationsFragment.this);
            }
        });

    }

    @Override
    protected CursorAdapter getCommunicationBoxesAdapter() {
        return new ConversationsAdapter(getActivity(),
                mUserManager.getCurrentUser(),
                null,
                0);
    }

    @Override
    protected void handleItemClick(Cursor cursor) {
        int columnConversationIndex = cursor.getColumnIndexOrThrow(ConversationTable.CONVERSATION_ID);
        String conversationId = cursor.getString(columnConversationIndex);
        mBus.post(new UserSelectedConversationEvent(conversationId));

    }

    @Override
    protected Loader<Cursor> createLoader(int i, Bundle bundle) {
        return new CursorLoader(
                getActivity(),
                ChatWingContentProvider.getConversationsUri(),
                ConversationTable.getMinimumProjection(),
                null,
                null,
                ConversationTable.TABLE + "." + ConversationTable.DATE_UPDATED + " DESC");
    }

    @Subscribe
    public void onSyncCommunicationBoxEvent(SyncCommunicationBoxEvent event){
        super.onSyncCommunicationBoxEvent(event);
    }


}
