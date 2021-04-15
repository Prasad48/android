package com.useriq.sdk.helpcenter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.useriq.Logger;
import com.useriq.sdk.R;
import com.useriq.sdk.UserIQSDKInternal;
import com.useriq.sdk.fonticon.UnfoldFontIcon;
import com.useriq.sdk.models.QGroup;
import com.useriq.sdk.models.Question;

import java.util.ArrayList;
import java.util.List;

/**
 * @author palkesh
 * @created 27-Oct-2018
 */
public class SearchPhoneCtrl implements SearchResultsAdapter.SearchResult {
    private static final Logger logger = Logger.init(SearchPhoneCtrl.class.getSimpleName());

    private final Context ctx;
    private final LinearLayout myRoot;
    private final List<Question> qList = new ArrayList<>();
    private SearchResultsAdapter adapter;
    private SearchResultsAdapter.SearchResultClick listener;
    private EditText searchET;
    private TextView noDataLabel;

    public SearchPhoneCtrl(Context ctx, SearchResultsAdapter.SearchResultClick listener) {
        this.ctx = ctx;
        this.listener = listener;
        this.myRoot = buildView();
    }

    public void attach(FrameLayout container) {
        container.removeAllViews();
        container.addView(myRoot);
    }

    public LinearLayout getMyRoot() {
        return myRoot;
    }

    /**
     * hydrate CtxHelp views with real data
     * <p>
     * Hydrate sounds sexy! from https://stackoverflow.com/a/6991192
     */
    public boolean hydrate(List<QGroup> qGroups) {
        for (QGroup item : qGroups) {
            qList.addAll(item.questions);
        }
        adapter.notifyDataSetChanged();
        return true;
    }

    private LinearLayout buildView() {
        LayoutInflater li = LayoutInflater.from(ctx);

        FrameLayout dummyRoot = new FrameLayout(ctx);
        LinearLayout rootView = (LinearLayout) li.inflate(R.layout.uiq_search, dummyRoot, false);

        ((TextView)rootView.findViewById(R.id.tv_back)).setTypeface(UnfoldFontIcon.getTypeface(ctx));
        ((TextView)rootView.findViewById(R.id.tv_search)).setTypeface(UnfoldFontIcon.getTypeface(ctx));
        ListView listView = rootView.findViewById(R.id.search_result_list);
        noDataLabel = rootView.findViewById(R.id.text_no_data);
        adapter = new SearchResultsAdapter(ctx, qList, listener, this);
        listView.setAdapter(adapter);

        searchET = rootView.findViewById(R.id.et_search);
        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                adapter.getFilter().filter(s);
            }
        });
        searchET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    UserIQSDKInternal.getAnalyticsManager().onQuestionSearch(searchET.getText().toString());
                }
            }
        });

        return rootView;
    }

    public boolean isSearching() {
        return adapter.isSearching();
    }

    public void clearSearch() {
        searchET.setText("");
    }

    public void showHideNoDataLabel(boolean show) {
        if (show) {
            noDataLabel.setVisibility(View.VISIBLE);
        } else {
            noDataLabel.setVisibility(View.GONE);
        }
    }
}
