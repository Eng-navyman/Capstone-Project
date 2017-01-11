/*
 * MIT License
 *
 * Copyright (c) 2016 Kartik Sharma
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.crazyhitty.chdev.ks.predator.ui.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.crazyhitty.chdev.ks.predator.R;
import com.crazyhitty.chdev.ks.predator.account.PredatorAccount;
import com.crazyhitty.chdev.ks.predator.core.posts.PostsContract;
import com.crazyhitty.chdev.ks.predator.core.posts.PostsPresenter;
import com.crazyhitty.chdev.ks.predator.data.Constants;
import com.crazyhitty.chdev.ks.predator.data.PredatorSharedPreferences;
import com.crazyhitty.chdev.ks.predator.events.NetworkEvent;
import com.crazyhitty.chdev.ks.predator.ui.adapters.PostsRecyclerAdapter;
import com.crazyhitty.chdev.ks.predator.ui.base.BaseSupportFragment;
import com.crazyhitty.chdev.ks.predator.ui.views.LoadingView;
import com.crazyhitty.chdev.ks.predator.utils.ListItemDecorator;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Author:      Kartik Sharma
 * Email Id:    cr42yh17m4n@gmail.com
 * Created:     1/2/2017 9:03 PM
 * Description: Unavailable
 */

public class PostsFragment extends BaseSupportFragment implements PostsContract.View, PostsRecyclerAdapter.OnPostsLoadMoreRetryListener {
    private static final String TAG = "PostsFragment";
    private static final String CATEGORY_TECH = "tech";

    @BindView(R.id.recycler_view_posts)
    RecyclerView recyclerViewPosts;
    @BindView(R.id.swipe_refresh_layout_posts)
    SwipeRefreshLayout swipeRefreshLayoutPosts;
    @BindView(R.id.loading_view)
    LoadingView loadingView;

    private PostsContract.Presenter mPostsPresenter;

    private PostsRecyclerAdapter mPostsRecyclerAdapter;

    private boolean mIsLoading = false;

    public static PostsFragment newInstance() {
        return new PostsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setPresenter(new PostsPresenter(this));
        mPostsPresenter.subscribe();
        EventBus.getDefault().register(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_posts, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Set loading type.
        loadingView.setLoadingType(LoadingView.TYPE.LATEST_POSTS);
        initSwipeRefreshLayout();
        setRecyclerViewProperties();

        // Always load offline posts first.
        getOfflinePosts();
    }

    private void initSwipeRefreshLayout() {
        // Disable swipe refresh layout initially.
        swipeRefreshLayoutPosts.setEnabled(false);
        swipeRefreshLayoutPosts.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (isNetworkAvailable(true)) {
                    getLatestPosts();
                } else {
                    swipeRefreshLayoutPosts.post(new Runnable() {
                        @Override
                        public void run() {
                            swipeRefreshLayoutPosts.setRefreshing(false);
                        }
                    });
                }
            }
        });
    }

    private void getOfflinePosts() {
        mPostsPresenter.getOfflinePosts(true);
    }

    /**
     * Get auth token and retrieve latest posts.
     */
    private void getLatestPosts() {
        PredatorAccount.getAuthToken(getActivity(),
                Constants.Authenticator.PREDATOR_ACCOUNT_TYPE,
                PredatorSharedPreferences.getAuthTokenType(getContext().getApplicationContext()))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {
                        // Done
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError: " + e.getMessage(), e);
                    }

                    @Override
                    public void onNext(String s) {
                        mPostsPresenter.getPosts(s, CATEGORY_TECH, true, true);
                    }
                });
    }

    private void loadMorePosts() {
        PredatorAccount.getAuthToken(getActivity(),
                Constants.Authenticator.PREDATOR_ACCOUNT_TYPE,
                PredatorSharedPreferences.getAuthTokenType(getContext().getApplicationContext()))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {
                        // Done
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError: " + e.getMessage(), e);
                    }

                    @Override
                    public void onNext(String s) {
                        Log.d(TAG, "onNext: load more posts");
                        mPostsPresenter.loadMorePosts(s, CATEGORY_TECH, true);
                    }
                });
    }

    private void setRecyclerViewProperties() {
        // Create a list type layout manager.
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerViewPosts.setLayoutManager(layoutManager);

        // Add appropriate decorations to the recycler view items.
        ListItemDecorator listItemDecorator = new ListItemDecorator(getContext().getApplicationContext(), 72);
        recyclerViewPosts.addItemDecoration(listItemDecorator);

        // Add scroll listener that will manage scroll down to load more functionality.
        recyclerViewPosts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // Check if the last item is on screen, if yes then start loading more posts.
                if (!mIsLoading &&
                        layoutManager.findLastVisibleItemPosition() == mPostsRecyclerAdapter.getItemCount() - 1 &&
                        isNetworkAvailable(false)) {
                    mIsLoading = true;
                    loadMorePosts();
                }
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNetworkConnectivityChanged(NetworkEvent networkEvent) {
        mPostsRecyclerAdapter.setNetworkStatus(networkEvent.isConnected(), getString(R.string.item_load_more_error_desc));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPostsPresenter.unSubscribe();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_posts, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setPresenter(PostsContract.Presenter presenter) {
        mPostsPresenter = presenter;
    }

    @Override
    public void showPosts(Cursor cursorPosts, HashMap<Integer, String> datehashMap) {
        if (mIsLoading) {
            mIsLoading = false;
            mPostsRecyclerAdapter.updateCursor(cursorPosts, datehashMap);
        } else {
            // Hide loading view
            loadingView.stopLoading();

            // Enable swipe refresh layout
            swipeRefreshLayoutPosts.setEnabled(true);

            setListTypeAdapter(cursorPosts, datehashMap);
        }

        // Dismiss swipe refresh layout animation if it is going on.
        if (swipeRefreshLayoutPosts.isRefreshing()) {
            swipeRefreshLayoutPosts.setRefreshing(false);
        }

        mPostsRecyclerAdapter.setNetworkStatus(isNetworkAvailable(false), getString(R.string.item_load_more_error_desc));
    }

    @Override
    public void unableToGetPosts(boolean onLoadMore, String errorMessage) {
        if (mPostsRecyclerAdapter != null && mPostsRecyclerAdapter.getItemCount() != 0) {
            // If the adapter contains items already.
            showLongToast(errorMessage);
            if (onLoadMore) {
                // If the error occurs while loading more posts.
                mPostsRecyclerAdapter.setNetworkStatus(false, errorMessage);
            }
        } else {

        }
    }

    private void setListTypeAdapter(Cursor cursor, HashMap<Integer, String> datehashMap) {
        // Create the adapter, and pass the cursor object to it alongside its type.
        mPostsRecyclerAdapter = new PostsRecyclerAdapter(cursor,
                PostsRecyclerAdapter.TYPE.LIST,
                datehashMap,
                this);

        // Set the adapter onto the recycler view.
        recyclerViewPosts.setAdapter(mPostsRecyclerAdapter);
    }

    @Override
    public void onLoadMore() {
        if (isNetworkAvailable(true)) {
            loadMorePosts();
        }
        mPostsRecyclerAdapter.setNetworkStatus(isNetworkAvailable(false), getString(R.string.item_load_more_error_desc));
    }
}
