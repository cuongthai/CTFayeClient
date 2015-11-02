package com.chatwing.whitelabel.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.chatwing.whitelabel.services.ChatWingChatService;

/**
 * Created by cuongthai on 10/30/15.
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, ChatWingChatService.class));
    }
}
