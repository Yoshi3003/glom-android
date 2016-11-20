package com.abborg.glom.fragments;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.abborg.glom.ApplicationState;
import com.abborg.glom.R;
import com.abborg.glom.adapters.DiscoverRecyclerViewAdapter;
import com.abborg.glom.data.DataProvider;
import com.abborg.glom.di.ComponentInjector;
import com.abborg.glom.interfaces.DiscoverItemChangeListener;
import com.abborg.glom.interfaces.MainActivityCallbacks;
import com.abborg.glom.model.DiscoverItem;

import java.util.List;

import javax.inject.Inject;

public class DiscoverFragment extends Fragment implements
        SwipeRefreshLayout.OnRefreshListener,
        DiscoverItemChangeListener {

    @Inject
    ApplicationState appState;

    @Inject
    DataProvider dataProvider;

    private static final String TAG = "DiscoverFragment";

    public boolean isFragmentVisible;
    private SwipeRefreshLayout refreshView;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private DiscoverRecyclerViewAdapter adapter;
    private boolean firstView;

    private MainActivityCallbacks activityCallback;

    private Handler handler;

    public DiscoverFragment() {}

    /**********************************************************
     * View Initializations
     **********************************************************/

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            activityCallback = (MainActivityCallbacks) context;
            if (isFragmentVisible) {
                activityCallback.onSetFabVisible(false);
            }
            handler = activityCallback.getThreadHandler();
        }
        catch (ClassCastException ex) {
            Log.e(TAG, "Attached activity has to implement " + MainActivityCallbacks.class.getName());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ComponentInjector.INSTANCE.getApplicationComponent().inject(this);

        adapter = new DiscoverRecyclerViewAdapter(getContext(), handler);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_discover, container, false);
        refreshView = (SwipeRefreshLayout) root.findViewById(R.id.discover_refresh_layout);
        refreshView.setOnRefreshListener(this);
        refreshView.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark);
        recyclerView = (RecyclerView) root.findViewById(R.id.circle_discover_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        // set up adapter and its appearance animation
        recyclerView.setAdapter(adapter);

        return root;
    }

    @Override
    public void onRefresh() {
        dataProvider.requestMovies();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            isFragmentVisible = true;

            if (dataProvider != null) {
                if (!firstView && appState.getConnectionStatus() == ApplicationState.ConnectivityStatus.CONNECTED) {
                    if (refreshView != null) {
                        if (handler != null) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    refreshView.setRefreshing(true);
                                }
                            });
                        }
                    }
                    dataProvider.requestMovies();
                }
            }

            activityCallback.onSetFabVisible(false);
            setActivityToolbar();
            firstView = true;
        }
        else {
            isFragmentVisible = false;
        }
    }

    private void setActivityToolbar() {
        if (isFragmentVisible) {
            activityCallback.onToolbarTitleChanged(appState.getActiveCircle().getTitle());
            activityCallback.onToolbarSubtitleChanged(null);
        }
    }

    /**********************************************************
     * ITEM CHANGE EVENTS FROM NETWORK REQUESTS
     **********************************************************/

    @Override
    public void onItemsReceived(int type, List<DiscoverItem> items) {
        if (refreshView != null && refreshView.isRefreshing()) {
            refreshView.setRefreshing(false);
        }

        if (adapter != null) adapter.update(type, items);
    }
}
