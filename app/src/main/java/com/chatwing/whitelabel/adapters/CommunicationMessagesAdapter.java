package com.chatwing.whitelabel.adapters;

import android.content.Context;
import android.graphics.Point;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.events.EditChatMessageEvent;
import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.managers.CurrentChatBoxManager;
import com.chatwing.whitelabel.managers.UserManager;
import com.chatwing.whitelabel.managers.VolleyManager;
import com.chatwing.whitelabel.modules.ForActivity;
import com.chatwing.whitelabel.parsers.BBCodeParser;
import com.chatwing.whitelabel.pojos.CommunicationBoxJson;
import com.chatwing.whitelabel.pojos.Message;
import com.chatwing.whitelabel.utils.LogUtils;
import com.chatwing.whitelabel.utils.ScreenUtils;
import com.chatwing.whitelabel.utils.Utils;
import com.chatwing.whitelabel.views.ImageTagTextView;
import com.joooonho.SelectableRoundedImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

/**
 * Created by steve on 02/07/2015.
 */
public class CommunicationMessagesAdapter extends RecyclerView.Adapter<CommunicationMessagesAdapter.ViewHolder> {
    private final Context mContext;
    private final ImageLoader mImageLoader;
    private final DisplayImageOptions displayImageOptions;
    private final BBCodeParser mBBCodeParser;
    private final VolleyManager mVolleyManager;
    private LayoutInflater mLayoutInflater;
    private ApiManager mApiManager;
    private Bus mBus;
    private UserManager mUserManager;
    private boolean mCanEditChatMessages;
    private final CurrentChatBoxManager mCurrentChatBoxManager;
    private List<Message> mDataSet;
    private Map<String, String> mEmoticons;
    private int mImageMaxWidth;
    private int mImageMaxHeight;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        // create a new view
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_chat_message, viewGroup, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v, mBus);
        return vh;
    }

    @Inject
    CommunicationMessagesAdapter(@ForActivity Context context,
                                 @ForActivity LayoutInflater layoutInflater,
                                 ApiManager apiManager,
                                 VolleyManager volleyManager,
                                 Bus bus,
                                 BBCodeParser bbCodeParser,
                                 UserManager userManager,
                                 CurrentChatBoxManager currentChatboxManager) {
        mContext = context;
        mLayoutInflater = layoutInflater;
        mApiManager = apiManager;
        mBus = bus;
        mUserManager = userManager;
        mVolleyManager = volleyManager;
        mCurrentChatBoxManager = currentChatboxManager;
        mBBCodeParser = bbCodeParser;
        mDataSet = new ArrayList<Message>();
        mImageLoader = ImageLoader.getInstance();

        displayImageOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.default_avatar)
                .showImageForEmptyUri(R.drawable.default_avatar)
                .showImageOnFail(R.drawable.default_avatar)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .displayer(new SimpleBitmapDisplayer())
                .build();
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        Message message = mDataSet.get(i);
        viewHolder.username.setText(message.getUserName());

        viewHolder.createTime.setText(Utils.formatTimeOnly(message.getCreatedDate()));
        LogUtils.v("message.isStartedDateMessage() " + message.isStartedDateMessage());
        LogUtils.v("message content " + message.getContent() + ":" + message.getCreatedDate());
        if (message.isStartedDateMessage()) {
            viewHolder.createdDate.setVisibility(View.VISIBLE);
            viewHolder.createdDate.setText(Utils.formatDateMonth(message.getStartDate()));
        } else {
            viewHolder.createdDate.setVisibility(View.GONE);
        }

        if (message.getStatus() == Message.Status.SENDING) {
            viewHolder.progressBar.setVisibility(View.VISIBLE);
        } else {
            viewHolder.progressBar.setVisibility(View.INVISIBLE);
        }

        //Load avatar
        String avatarUrl = mApiManager.getAvatarUrl(message);
        mImageLoader.displayImage(avatarUrl, viewHolder.avatarImageView, displayImageOptions);

        viewHolder.content.setMovementMethod(LinkMovementMethod.getInstance());
        viewHolder.content.setEmoticons(mEmoticons);
        viewHolder.content.setImageMaxWidth(mImageMaxWidth);
        viewHolder.content.setImageMaxHeight(mImageMaxHeight);
        viewHolder.content.setVolleyManager(mVolleyManager);
        viewHolder.content.setBBCodeParser(mBBCodeParser);
        viewHolder.content.setBBCodeText(message.getContent());

    }


    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    /*
    Messages have to be sorted desc
     */
    public void addAllDataToTail(List<Message> messages) {
        int oldSize = mDataSet.size();
        mDataSet.addAll(messages);
        fillStartDate();
        notifyItemRangeInserted(oldSize - 1, messages.size());
    }

    public void setData(List<Message> messages) {
        mDataSet.clear();
        mDataSet.addAll(messages);
        fillStartDate();
        notifyDataSetChanged();
    }

    public void addToHead(Message message) {
        LogUtils.v("Check send message addToHead ");
        mDataSet.add(0, message);
        fillStartDate();
        notifyItemInserted(0);
    }

    public void clear() {
        mDataSet.clear();
        notifyDataSetChanged();
    }

    public Message getOldestMessageItem() {
        return mDataSet.size() == 0 ? null : mDataSet.get(mDataSet.size() - 1);
    }

    public Message getItemByRandomKey(String randomKey) {
        int count = mDataSet.size();
        for (int i = 0; i < count; ++i) {
            Message m = mDataSet.get(i);
            String k = m.getRandomKey();
            if (!TextUtils.isEmpty(k) && randomKey.equals(k)) {
                return m;
            }
            LogUtils.v("NOT FOUND!!!!!!!!!!! Check next");
        }
        return null;
    }

    public void updateCommunicationBoxDetail(CommunicationBoxJson json,
                                             Map<String, String> emoticons,
                                             boolean canEditChatMessages) {
        mEmoticons = emoticons;
        mCanEditChatMessages = canEditChatMessages;

        // Calculate size for external images
        Point screenSize = ScreenUtils.getScreenSize(mContext);
        mImageMaxWidth = getExternalImageDimension(
                json == null
                        ? 0
                        : json.getExternalImageMaxWidth(),
                screenSize.x
        );
        mImageMaxHeight = getExternalImageDimension(
                json == null
                        ? 0
                        : json.getExternalImageMaxHeight(),
                screenSize.y
        );

        notifyDataSetChanged();
    }


    public void replace(Message existingMessage, Message newMessage) {
        LogUtils.v("Check send message replacing " + existingMessage);
        int oldIndex = mDataSet.indexOf(existingMessage);
        mDataSet.remove(oldIndex);
        notifyItemRemoved(oldIndex);
        mDataSet.add(oldIndex, newMessage);
        fillStartDate();
        notifyItemInserted(oldIndex);
    }

    public Message getItem(int position) {
        return mDataSet.get(position);
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder implements OnLongClickListener {
        private final Bus bus;
        SelectableRoundedImageView avatarImageView;
        TextView username;
        ImageTagTextView content;
        TextView createdDate;
        TextView createTime;
        ProgressBar progressBar;

        public ViewHolder(View v, Bus bus) {
            super(v);
            this.bus = bus;
            v.setOnLongClickListener(this);
            avatarImageView
                    = (SelectableRoundedImageView) v.findViewById(R.id.image_view_avatar);
            username
                    = (TextView) v.findViewById(R.id.username);
            content
                    = (ImageTagTextView) v.findViewById(R.id.content);
            createdDate
                    = (TextView) v.findViewById(R.id.created_date);
            createTime
                    = (TextView) v.findViewById(R.id.created_time);

            progressBar
                    = (ProgressBar) v.findViewById(R.id.progress_bar);
        }

        @Override
        public boolean onLongClick(View v) {
            LogUtils.v("On long Click");
            bus.post(new EditChatMessageEvent(getAdapterPosition()));
            return true;
        }

        public static interface MessageItemLongClickListener {
            public void onLongClickItem();
        }
    }


    /**
     * Calculates an appropriate dimension (either width or height) for an
     * external image.
     * <p/>
     * The value should not only be big enough so that users can see images
     * easily but also small enough so that the device can hold multiple images
     * at the same time without running out of memory.
     * <p/>
     * The current implementation prefers the suggested dimension, unless it is
     * too small or too big. In those cases, a value of 1/6 or 1/3 of the screen
     * dimension is returned, respectively.
     *
     * @param suggestedDimension is the preferred dimension.
     * @param screenDimension    is the screen dimension of current device. It should
     *                           be either screen width or height.
     * @return an appropriate dimension for external images.
     */
    private static int getExternalImageDimension(int suggestedDimension,
                                                 int screenDimension) {
        int screenUpperLimit = screenDimension / 3;
        int screenLowerLimit = screenDimension / 6;
        return Math.max(screenLowerLimit, Math.min(suggestedDimension, screenUpperLimit));
    }

    private void fillStartDate() {
        Message lastDiffDateMessage = null;
        int size = mDataSet.size();
        for (int i = 0; i < size; i++) {
            Message message = mDataSet.get(i);
            if (i == size - 1) {
                message.setIsStartedDateMessage(true);
            } else {
                if ((lastDiffDateMessage != null && lastDiffDateMessage.getStartDate() != message.getStartDate())) {
                    lastDiffDateMessage.setIsStartedDateMessage(true); //Enough to define started date message
                } else if (lastDiffDateMessage != null) {
                    lastDiffDateMessage.setIsStartedDateMessage(false);
                }
            }
            lastDiffDateMessage = message;
        }
    }


}
