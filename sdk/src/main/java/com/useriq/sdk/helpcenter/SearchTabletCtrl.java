package com.useriq.sdk.helpcenter;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.useriq.Logger;
import com.useriq.sdk.R;
import com.useriq.sdk.UserIQSDKInternal;
import com.useriq.sdk.fonticon.UnfoldFontIcon;
import com.useriq.sdk.helpcenter.views.QuestionAnswerView;
import com.useriq.sdk.models.QGroup;
import com.useriq.sdk.models.Question;

import java.util.ArrayList;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * @author palkesh
 * @created 27-Oct-2018
 */
public class SearchTabletCtrl implements SearchResultsAdapter.SearchResult {
    private static final Logger logger = Logger.init(SearchTabletCtrl.class.getSimpleName());

    private final Context ctx;
    private final LinearLayout myRoot;
    private final List<Question> qList = new ArrayList<>();
    private SearchResultsAdapter adapter;
    private QuestionAnswerView qaListView;
    private EditText searchET;
    private TextView noDataLabel;

    public SearchTabletCtrl(Context ctx) {
        this.ctx = ctx;
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

    public void showQuestion(Question qn) {
        AnswerAdapter answerAdapter = new AnswerAdapter(ctx, qn);
        qaListView.setAdapter(answerAdapter);
        qaListView.setHeaderText(qn.name);
        InputMethodManager imm = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(myRoot.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }


    private LinearLayout buildView() {
        LayoutInflater li = LayoutInflater.from(ctx);

        LinearLayout container = new LinearLayout(ctx);
        container.setBackgroundColor(Color.WHITE);
        LinearLayout searchSection = (LinearLayout) li.inflate(R.layout.uiq_search, container, false);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, MATCH_PARENT);
        lp.weight = 2;
        searchSection.setLayoutParams(lp);
        ((TextView)searchSection.findViewById(R.id.tv_back)).setTypeface(UnfoldFontIcon.getTypeface(ctx));
        ((TextView)searchSection.findViewById(R.id.tv_search)).setTypeface(UnfoldFontIcon.getTypeface(ctx));
        ListView listView = searchSection.findViewById(R.id.search_result_list);
        noDataLabel = searchSection.findViewById(R.id.text_no_data);
        adapter = new SearchResultsAdapter(ctx, qList, new SearchResultsAdapter.SearchResultClick() {
            @Override
            public void onQuestionSelection(Question question) {
                showQuestion(question);
            }
        }, this);
        listView.setAdapter(adapter);

        searchET = searchSection.findViewById(R.id.et_search);
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

        LinearLayout ansView = new LinearLayout(ctx);
        LinearLayout.LayoutParams lpAns = new LinearLayout.LayoutParams(0, MATCH_PARENT);
        lpAns.weight = 3;
        ansView.setLayoutParams(lpAns);
        ansView.setOrientation(LinearLayout.VERTICAL);
        ansView.setBackgroundColor(Color.WHITE);

        qaListView = new QuestionAnswerView(ctx);
        qaListView.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));

        ansView.addView(qaListView);

        View divider = new View(ctx);
        divider.setLayoutParams(new LinearLayout.LayoutParams(1, MATCH_PARENT));
        divider.setBackgroundColor(Color.rgb(235,235,235));

        container.addView(searchSection);
        container.addView(divider);
        container.addView(ansView);

        ansView.setTag(R.id.viewMatch, R.string.transition_morph_view);

        return container;
    }

    public void clearSearch() {
        searchET.setText("");
    }

    public boolean isSearching() {
        return adapter.isSearching();
    }

    public void showHideNoDataLabel(boolean show) {
        if (show) {
            noDataLabel.setVisibility(View.VISIBLE);
        } else {
            noDataLabel.setVisibility(View.GONE);
        }
    }
}
