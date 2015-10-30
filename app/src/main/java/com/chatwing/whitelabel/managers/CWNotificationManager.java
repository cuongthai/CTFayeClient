package com.chatwing.whitelabel.managers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.chatwing.whitelabel.ChatWing;
import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.activities.CommunicationActivity;
import com.chatwing.whitelabel.pojos.Message;
import com.chatwing.whitelabel.pojos.User;
import com.chatwing.whitelabel.utils.LogUtils;
import com.chatwing.whitelabel.utils.StatisticTracker;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by cuongthai on 10/30/15.
 */
public class CWNotificationManager {
    private static final int MAX_MESSAGES_PER_GROUP = 5;

    private SoundPool mSoundEffectsPool;
    private Context mContext;
    private NotificationManager mNotificationManager;
    private UserManager mUserManager;
    private int newMessageSoundId;

    @Inject
    public CWNotificationManager(Context context,
                                 SoundPool soundEffectsPool,
                                 NotificationManager notificationManager,
                                 UserManager userManager) {
        mContext = context;
        mSoundEffectsPool = soundEffectsPool;
        mNotificationManager = notificationManager;
        mUserManager = userManager;
        newMessageSoundId = mSoundEffectsPool.load(mContext, R.raw.new_message, 1);
    }

    public void notifyMessage(Message message, boolean withSound) {
        LogUtils.v("Notify Single Message " + withSound);
        List<Message> messages = new ArrayList<>();
        messages.add(message);
        if (message.getConversationID() == null) { //Chatbox Message
            notifyForBox(messages, message.getChatboxName(), message.getChatBoxId(), withSound);
        } else { //Notification message
            notifyForBox(messages, message.getConversationID(), null, withSound);
        }
    }

    public void showBroadCastMessage(String name, String message) {
        StatisticTracker.trackReceiveNotification(StatisticTracker.NOTIFICATION_BROADCAST_TYPE);

        PendingIntent contentIntent = PendingIntent.getActivity(mContext,
                0,
                new Intent(mContext, ChatWing.instance(mContext).getMainActivityClass()),
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(mContext)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(mContext.getString(R.string.broadcast_tag) + " to " + name + " ")
                        .setTicker(message)
                        .setContentText(message);

        builder.setContentIntent(contentIntent);
        builder.setAutoCancel(true);
        mNotificationManager.notify(message.hashCode(), builder.build());
    }

    public void notifyForBox(List<Message> messages,
                             String conversationID,
                             User targetUser,
                             boolean withSound) {
        //Extra filter here to make sure user won't receive messages not sending to them.
        if (targetUser != null && !targetUser.equals(mUserManager.getCurrentUser()))
            return; //Not send to me, so nothing right now
        if (messages.size() == 0) return;
        StatisticTracker.trackReceiveNotification(StatisticTracker.NOTIFICATION_CONVERSATION_TYPE);

        Intent i = new Intent(mContext, ChatWing.instance(mContext).getMainActivityClass());
        i.setAction(CommunicationActivity.ACTION_OPEN_CONVERSATION);
        i.putExtra(CommunicationActivity.CONVERSATION_ID, conversationID);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        LogUtils.v("Check user name " + messages.get(0).getUserName());
        doNotify(i, messages.get(0).getUserName(), messages, conversationID.hashCode(), withSound);
    }

    public void notifyForBox(List<Message> messages,
                             String chatboxName,
                             int chatboxID,
                             boolean withSound) {
        if (messages.size() == 0) return;

        StatisticTracker.trackReceiveNotification(StatisticTracker.NOTIFICATION_CHATBOX_TYPE);
        Intent i = new Intent(mContext, ChatWing.instance(mContext).getMainActivityClass());
        i.setAction(CommunicationActivity.ACTION_OPEN_CHATBOX);
        i.putExtra(CommunicationActivity.CHATBOX_ID, chatboxID);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|
                Intent.FLAG_ACTIVITY_SINGLE_TOP);

        doNotify(i, chatboxName, messages, chatboxID, withSound);
    }

    private void doNotify(Intent i, String contentTitle,
                          List<Message> messages,
                          int notificationCode,
                          boolean withSound) {
        PendingIntent contentIntent = PendingIntent.getActivity(
                mContext,
                notificationCode,
                i,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(mContext)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(contentTitle)
                        .setTicker(messages.get(0).getContent())
                        .setContentText(messages.get(0).getContent());


        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();
        String[] lastMessages = new String[Math.min(messages.size(), MAX_MESSAGES_PER_GROUP)];
        inboxStyle.setBigContentTitle(contentTitle);
        for (int j = 0; j < lastMessages.length; j++) {
            inboxStyle.addLine(messages.get(j).getContent());
        }
        builder.setStyle(inboxStyle);
        builder.setNumber(messages.size());
        builder.setContentIntent(contentIntent);
        builder.setAutoCancel(true);

        mNotificationManager.notify(notificationCode, builder.build());

        if (withSound) {
            mSoundEffectsPool.play(newMessageSoundId, 1.0f, 1.0f, 0, 0, 1);
        }
    }
}
