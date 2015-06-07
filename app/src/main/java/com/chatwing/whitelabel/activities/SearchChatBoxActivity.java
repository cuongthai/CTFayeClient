package com.chatwing.whitelabel.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.chatwing.whitelabel.ChatWingApplication;
import com.chatwing.whitelabel.Constants;
import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.adapters.SearchChatBoxResultsAdapter;
import com.chatwing.whitelabel.contentproviders.SearchChatBoxSuggestionsProvider;
import com.chatwing.whitelabel.listeners.EndlessOnScrollListener;
import com.chatwing.whitelabel.modules.ExtendChatWingModule;
import com.chatwing.whitelabel.modules.SearchChatBoxActivityModule;
import com.chatwing.whitelabel.utils.StatisticTracker;
import com.chatwingsdk.pojos.LightWeightChatBox;
import com.chatwing.whitelabel.pojos.responses.CreateChatBoxResponse;
import com.chatwing.whitelabel.pojos.responses.SearchChatBoxResponse;
import com.chatwing.whitelabel.tasks.CreateChatBoxTask;
import com.chatwing.whitelabel.tasks.SearchChatBoxTask;
import com.chatwingsdk.events.internal.TaskFinishedEvent;
import com.chatwingsdk.managers.UserManager;
import com.chatwingsdk.views.ErrorMessageView;
import com.flurry.android.FlurryAgent;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Provider;

import dagger.ObjectGraph;

public class SearchChatBoxActivity extends ActionBarActivity implements View.OnClickListener {
    public static final String EXTRA_RESULT_CHAT_BOX = "chat_box";
    private static final int SEARCH_RESULTS_LIMIT = 40;
    private static final int SEARCH_ADAPTER_VISIBLE_THRESHOLD = 10;

    private ObjectGraph mObjectGraph;
    @Inject
    Provider<SearchChatBoxTask> mSearchChatBoxTaskProvider;
    @Inject
    Provider<CreateChatBoxTask> mCreateChatBoxTaskProvider;
    @Inject
    SearchManager mSearchManager;
    @Inject
    UserManager mUserManager;
    @Inject
    Bus mBus;
    @Inject
    SearchChatBoxResultsAdapter mAdapter;
    @Inject
    ErrorMessageView mErrorMessageView;
    private MenuItem mSearchMenuItem;
    private TextView mProgressTextView;
    private Button mCreateChatBoxButton;
    private String mCurrentQuery;
    private boolean mIsEndOfResults;
    private View mProgressFooterView;
    private AsyncTask<?, ?, ?> mCurrentTask;
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ChatWingApplication application = (ChatWingApplication) getApplication();
        mObjectGraph = application.getApplicationGraph().plus(new ExtendChatWingModule(this),
                new SearchChatBoxActivityModule(this));
        mObjectGraph.inject(this);

        setContentView(R.layout.activity_search_chat_box);
        mListView = (ListView) findViewById(R.id.listview);

        configViews();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // Get the intent, verify the action and get the query
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doSearch(query);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FlurryAgent.onStartSession(this, getString(R.string.flurry_api_key));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBus.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBus.unregister(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopCurrentTask();
        FlurryAgent.onEndSession(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mObjectGraph = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search_chat_box, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Get the SearchView and set the chat_box_searchable configuration
        mSearchMenuItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) mSearchMenuItem.getActionView();
        // Assumes current activity is the chat_box_searchable activity
        searchView.setSearchableInfo(mSearchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryRefinementEnabled(true);
        mSearchMenuItem.expandActionView();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    ////////////////////////////////////////////////////////
    // Search task management
    ///////////////////////////////////////////////////////
    private void startTask(AsyncTask<?, ?, ?> task) {
        stopCurrentTask();
        mCurrentTask = task;
    }

    private void stopCurrentTask() {
        if (mCurrentTask != null) {
            if (mCurrentTask.getStatus() != AsyncTask.Status.FINISHED) {
                mCurrentTask.cancel(true);
            }
            mCurrentTask = null;
        }
    }

    /////////////////////////////////////////////////////////
    // Otto subscribe methods
    /////////////////////////////////////////////////////////
    @Subscribe
    public void onTaskFinished(TaskFinishedEvent event) {
        AsyncTask<?, ?, ?> task = event.getTask();
        if (event.getTask() != mCurrentTask) {
            return;
        }

        mCurrentTask = null;
        if (task instanceof SearchChatBoxTask) {
            onFinishedSearching(event);
        } else if (task instanceof CreateChatBoxTask) {
            onFinishedCreating(event);
        }
    }

    private void onFinishedSearching(TaskFinishedEvent event) {
        if (event.getStatus() == TaskFinishedEvent.Status.FAILED) {
            mErrorMessageView.show(event.getException());
        } else {
            ArrayList<LightWeightChatBox> results = ((SearchChatBoxResponse) event.getResult()).getData();
            if (results.size() > 0) {
                mAdapter.addAllData(results);
            }
            mIsEndOfResults = results.size() < SEARCH_RESULTS_LIMIT;
        }

        boolean isLoadingMoreResults = ((SearchChatBoxTask) event.getTask()).getOffset() > 0;
        if (isLoadingMoreResults) {
            mProgressFooterView.setVisibility(View.GONE);
        } else {
            if (mAdapter.getCount() == 0) {
                showEmptyResultText();
            } else {
                showListView();
            }
        }
    }

    private void onFinishedCreating(TaskFinishedEvent event) {
        if (event.getStatus() == TaskFinishedEvent.Status.FAILED) {
            mErrorMessageView.show(event.getException());
            showEmptyResultText();
            return;
        }
        CreateChatBoxResponse response = (CreateChatBoxResponse) event.getResult();
        LightWeightChatBox chatBox = response.getData();
        finishWithResult(chatBox);
    }

    /////////////////////////////////////////////////////////
    // Loading indicator management
    /////////////////////////////////////////////////////////
    private void showLoadingIndicator(int progressText) {
        updateViews(View.GONE, View.VISIBLE, progressText, View.GONE);
    }

    private void showEmptyQueryText() {
        updateViews(View.GONE,
                View.VISIBLE,
                R.string.empty_search_chat_box_query,
                View.GONE);
    }

    private void showEmptyResultText() {
        String progressText = String.format(
                getString(R.string.empty_search_chat_box_results),
                mCurrentQuery);
        updateViews(View.GONE, View.VISIBLE, progressText, View.VISIBLE);
    }

    private void showListView() {
        updateViews(View.VISIBLE, View.GONE, 0, View.VISIBLE);
    }

    private void updateViews(int listViewVisibility,
                             int progresTextVisibility,
                             int progressStringId,
                             int createButtonVisibility) {
        updateViews(listViewVisibility,
                progresTextVisibility,
                progressStringId == 0 ? "" : getString(progressStringId),
                createButtonVisibility);
    }

    private void updateViews(int listViewVisibility,
                             int progresTextVisibility,
                             String progressText,
                             int createButtonVisibility) {
        mListView.setVisibility(listViewVisibility);
        mProgressTextView.setVisibility(progresTextVisibility);
        mProgressTextView.setText(progressText);
        mCreateChatBoxButton.setVisibility(createButtonVisibility);
    }

    /////////////////////////////////////////////////////////
    // Instance methods
    ////////////////////////////////////////////////////////
    public void setCurrentQuery(String currentQuery) {
        mCurrentQuery = currentQuery;
        mIsEndOfResults = false;
        mAdapter.clear();
    }

    private void finishWithResult(LightWeightChatBox chatBox) {
        Intent data = new Intent();
        data.putExtra(EXTRA_RESULT_CHAT_BOX, chatBox);
        setResult(RESULT_OK, data);
        finish();
    }

    private void configViews() {
        mListView.setOnScrollListener(
                new EndlessOnScrollListener(SEARCH_ADAPTER_VISIBLE_THRESHOLD,
                        EndlessOnScrollListener.Direction.DOWN) {
                    @Override
                    protected int getHeaderCount() {
                        return mListView.getHeaderViewsCount();
                    }

                    @Override
                    public boolean isLoading() {
                        return mCurrentTask != null;
                    }

                    @Override
                    public void loadMoreResults() {
                        startSearchChatBoxTask(mAdapter.getCount());
                    }
                }
        );
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                LightWeightChatBox item = mAdapter.getItem(position);
                finishWithResult(item);
            }
        });
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View footerView = layoutInflater.inflate(R.layout.view_loading, null);
        mProgressFooterView = footerView.findViewById(R.id.progress_bar);
        // Prior to KitKat, this must be called before setting the adapter with
        // setAdapter(ListAdapter)
        mListView.addFooterView(footerView);
        mListView.setAdapter(mAdapter);
        mProgressTextView = (TextView) findViewById(R.id.progress_text);
        mCreateChatBoxButton = (Button) findViewById(R.id.create);
        mCreateChatBoxButton.setOnClickListener(this);
        showEmptyQueryText();
    }

    private void doSearch(String query) {
        if (!TextUtils.isEmpty(query)) {
            setCurrentQuery(query);
            mSearchMenuItem.collapseActionView();
            startSearchChatBoxTask(0);
            // Save the query as recent
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(
                    this,
                    SearchChatBoxSuggestionsProvider.AUTHORITY,
                    SearchChatBoxSuggestionsProvider.MODE);
            suggestions.saveRecentQuery(query, null);
        }
    }

    private void startSearchChatBoxTask(int offset) {
        if (TextUtils.isEmpty(mCurrentQuery) || mIsEndOfResults) {
            return;
        }
        if (offset > 0) {
            mProgressFooterView.setVisibility(View.VISIBLE);
        } else {
            mProgressFooterView.setVisibility(View.GONE);
            showLoadingIndicator(R.string.progress_searching);
        }
        StatisticTracker.trackChatBoxSearch(mCurrentQuery);
        SearchChatBoxTask task = mSearchChatBoxTaskProvider.get();
        task.setParams(mCurrentQuery, offset, SEARCH_RESULTS_LIMIT);
        startTask(task.execute());
    }

    @Override
    public void onClick(View v) {
        if (TextUtils.isEmpty(mCurrentQuery)) {
            return;
        }
        if (!mUserManager.userCanCreateChatBox()) {
            mErrorMessageView.show(R.string.error_required_chat_wing_login_to_create_chat_boxes);
            return;
        }
        showLoadingIndicator(R.string.progress_creating_chat_box);
        CreateChatBoxTask task = mCreateChatBoxTaskProvider.get();
        task.setName(mCurrentQuery);
        startTask(task.execute());
    }
}
