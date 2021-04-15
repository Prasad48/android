package com.useriq.sdk.helpcenter;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.useriq.Logger;
import com.useriq.sdk.R;
import com.useriq.sdk.UserIQSDKInternal;
import com.useriq.sdk.models.QDesc;
import com.useriq.sdk.models.Question;

import java.util.ArrayList;
import java.util.List;

/**
 * @author palkesh
 * @created 27-Oct-2018
 */
class SearchResultsAdapter extends BaseAdapter implements Filterable {
    private static final Logger logger = Logger.init(SearchResultsAdapter.class.getSimpleName());
    private final Context ctx;
    private List<Question> qList;
    private Filter searchResultFilter;
    private List<Question> searchResultQList = new ArrayList<>();
    private String searchQuery = "";
    private SearchResultClick clickListener;
    private SearchResult listener;

    SearchResultsAdapter(Context ctx, List<Question> qList, SearchResultClick clickListener, SearchResult listener) {
        this.ctx = ctx;
        this.qList = qList;
        this.clickListener = clickListener;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return searchResultQList.size();
    }

    @Override
    public Object getItem(int position) {
        return searchResultQList.get(position).name;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Question question = searchResultQList.get(position);
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(ctx).inflate(R.layout.uiq_search_item, null);
        }

        view.findViewById(R.id.container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserIQSDKInternal.getAnalyticsManager().onQuestionSearch(searchQuery);
                clickListener.onQuestionSelection(question);
            }
        });
        TextView tvQTitle = view.findViewById(R.id.tv_question);
        TextView tvDescription = view.findViewById(R.id.tv_description);

        String desc = "";
        for (QDesc item : question.descList) {
            if (item.type == QDesc.Type.rtf) {
                desc = item.value;
                if (desc.contains(searchQuery)) {
                    int index = desc.indexOf(searchQuery);
                    desc = desc.substring(index - 50 > 0 ? index - 50 : 0, index + 50 > desc.length() ? desc.length() : index + 50);
                    break;
                }
            }
        }
        if (desc.length() > 101) {
            desc = desc.substring(0, 100);
        }

        highlightString(tvDescription, Html.fromHtml(desc).toString());
        highlightString(tvQTitle, question.name);

        return view;
    }

    @Override
    public Filter getFilter() {
        if (searchResultFilter == null)
            searchResultFilter = new SearchResultFilter();
        return searchResultFilter;
    }

    private void highlightString(TextView textView, String text) {
        text = text.replaceAll("\n", "");
        SpannableString spannableString = new SpannableString(text);
        BackgroundColorSpan[] backgroundSpans = spannableString.getSpans(0, spannableString.length(), BackgroundColorSpan.class);

        for (BackgroundColorSpan span : backgroundSpans) {
            spannableString.removeSpan(span);
        }

        int indexOfKeyword = spannableString.toString().toLowerCase().indexOf(searchQuery);

        while (indexOfKeyword >= 0) {
            //Create a background color span on the keyword
            spannableString.setSpan(new BackgroundColorSpan(Color.YELLOW), indexOfKeyword, indexOfKeyword + searchQuery.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            //Get the next index of the keyword
            indexOfKeyword = spannableString.toString().toLowerCase().indexOf(searchQuery, indexOfKeyword + searchQuery.length());
        }

        textView.setText(spannableString);
    }

    public boolean isSearching() {
        return !searchQuery.isEmpty();
    }

    public class SearchResultFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            constraint = constraint.toString().toLowerCase();

            FilterResults newFilterResults = new FilterResults();

            List<Question> data = new ArrayList<>();

            if (!constraint.toString().isEmpty()) {
                for (int i = 0; i < qList.size(); i++) {
                    if (qList.get(i).name.toLowerCase().contains(constraint)) {
                        data.add(qList.get(i));
                    } else {
                        for (QDesc item : qList.get(i).descList) {
                            if (item.type == QDesc.Type.rtf && item.value.toLowerCase().contains(constraint)) {
                                data.add(qList.get(i));
                                break;
                            }
                        }
                    }
                }
            }

            newFilterResults.count = data.size();
            newFilterResults.values = data;

            return newFilterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            searchQuery = constraint.toString();
            searchResultQList = (List<Question>) results.values;
            listener.showHideNoDataLabel(!(searchResultQList.size() > 0));
            notifyDataSetChanged();
        }
    }

    interface SearchResultClick {
        void onQuestionSelection(Question question);
    }

    interface SearchResult {
        void showHideNoDataLabel(boolean show);
    }

}
