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
import com.chatwing.whitelabel.adapters.ChatboxesAdapter;
import com.chatwing.whitelabel.contentproviders.ChatWingContentProvider;
import com.chatwing.whitelabel.events.SyncCommunicationBoxEvent;
import com.chatwing.whitelabel.events.UserSelectedChatBoxEvent;
import com.chatwing.whitelabel.managers.CurrentChatBoxManager;
import com.chatwing.whitelabel.pojos.ChatBox;
import com.chatwing.whitelabel.tables.ChatBoxTable;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

/**
 * Created by cuongthai on 22/10/2014.
 */
public class ChatboxesFragment extends CommunicationBoxesFragment {

    @Inject
    CurrentChatBoxManager mCurrentChatboxManager;

    @Override
    protected final CursorAdapter getCommunicationBoxesAdapter() {
        return new ChatboxesAdapter(getActivity(),
                mUserManager.getCurrentUser(),
                null,
                0);
    }

    @Override
    protected final void handleItemClick(Cursor cursor) {
        ChatBox chatBox = ChatBoxTable.getChatBox(cursor);
        mBus.post(new UserSelectedChatBoxEvent(chatBox.getId()));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDelegate.back(ChatboxesFragment.this);
            }
        });
    }

    @Override
    protected final Loader<Cursor> createLoader(int i, Bundle bundle) {
        return new CursorLoader(
                getActivity(),
                ChatWingContentProvider.getChatBoxesUri(),
                ChatBoxTable.getMinimumProjection(),
                null,
                null,
                null);
    }

    @Subscribe
    public void onSyncCommunicationBoxEvent(SyncCommunicationBoxEvent event){
        super.onSyncCommunicationBoxEvent(event);
    }


}
