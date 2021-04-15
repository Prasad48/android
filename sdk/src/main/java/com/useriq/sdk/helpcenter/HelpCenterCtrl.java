package com.useriq.sdk.helpcenter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.IntDef;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.useriq.sdk.R;
import com.useriq.sdk.UIManager;
import com.useriq.sdk.UIRouter;
import com.useriq.sdk.UserIQSDKInternal;
import com.useriq.sdk.capture.ViewRoot;
import com.useriq.sdk.fonticon.UnfoldFontIcon;
import com.useriq.sdk.helpcenter.views.InboxLayoutBase;
import com.useriq.sdk.helpcenter.views.InboxQuestionAnswerView;
import com.useriq.sdk.helpcenter.views.InboxStickyScrollView;
import com.useriq.sdk.helpcenter.views.QuestionAnswerView;
import com.useriq.sdk.helpcenter.views.StickyScrollView;
import com.useriq.sdk.models.QGroup;
import com.useriq.sdk.models.Question;
import com.useriq.sdk.models.SyncData;

import java.lang.annotation.Retention;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.useriq.sdk.util.UnitUtil.dpToPx;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * @author sudhakar
 * @created 23-Oct-2018
 */
public class HelpCenterCtrl implements UIRouter.Controller {
    private final Context ctx;
    private final FrameLayout myRoot;
    private LinearLayout scrollHolder;
    private LinearLayout hcWelcome;
    private InboxQuestionAnswerView inboxQAView;
    private TextView title;
    public @HC_STATE
    int hcState = HOME;
    private boolean isAlreadyClosedByDrag = false;
    private QuestionAnswerView qaListView;
    private TextView groupTitleTV;
    private SearchPhoneCtrl searchPhoneCtrl;
    private SearchTabletCtrl searchTabletCtrl;
    private boolean isTablet;

    public HelpCenterCtrl() {
        this.ctx = UserIQSDKInternal.getContext();
        this.isTablet = ctx.getResources().getBoolean(R.bool.isTablet);
        if (isTablet) {
            this.myRoot = buildTabletView();
        } else {
            this.myRoot = buildPhoneView();
        }
        Typeface iconFont = UnfoldFontIcon.getTypeface(ctx);

        TextView searchIcon = hcWelcome.findViewById(R.id.tv_search);
        if (searchIcon != null) {
            searchIcon.setTypeface(iconFont);
        }

        TextView backBtn = myRoot.findViewById(R.id.tv_back);

        if (backBtn != null) {
            backBtn.setTypeface(iconFont);
            backBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            myRoot.setTransitionName(ctx.getString(R.string.transition_morph_view));
            myRoot.setTag(R.id.viewBgColor, Color.WHITE);
            myRoot.setTag(R.id.viewRadius, 0);
        }

    }

    @Override
    public View onEnter() {
        if (UIManager.getInstance().getUiRootView().getRootType() != ViewRoot.ACTIVITY) {
            Toast.makeText(UserIQSDKInternal.getApp(), "Close the dialog to see Help Center", Toast.LENGTH_LONG).show();
            return null;
        }
        UserIQSDKInternal.getAnalyticsManager().onHelpCenter(true);
        SyncData syncData = UserIQSDKInternal.getSyncData();
        hydrate(syncData.qGroups);
        TextView welcomeText = hcWelcome.findViewById(R.id.welcomeText);
        welcomeText.setText(syncData.hcWelcomeText);

//        UIRouter.ViewInfo viewInfo = exitCtrl.getAnimatingView();
//        View targetView = viewInfo == null ? null : viewInfo.viewRef.get();

        if (isTablet) {
            searchTabletCtrl = new SearchTabletCtrl(ctx);
            searchTabletCtrl.hydrate(syncData.qGroups);
        } else {
            searchPhoneCtrl = new SearchPhoneCtrl(ctx, new SearchResultsAdapter.SearchResultClick() {
                @Override
                public void onQuestionSelection(Question question) {
                    updateState(QN_OPEN_FROM_SEARCH);
                    AnswerWithToolbarCtrl answerWithToolbarCtrl = new AnswerWithToolbarCtrl(question.id);
                    answerWithToolbarCtrl.hydrate(question);
                    View questionView = answerWithToolbarCtrl.getMyRoot();
                    final TextView backBtn = questionView.findViewById(R.id.tv_back);
                    if (backBtn != null) {
                        backBtn.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                onBackPressed();
                            }
                        });
                    }
                    myRoot.addView(questionView);
                    hideKeyboard();
                }
            });
            searchPhoneCtrl.hydrate(syncData.qGroups);
        }

        return myRoot;
    }

    @Override
    public void onExit() {
        UserIQSDKInternal.getAnalyticsManager().onHelpCenter(false);
    }

    @Override
    public boolean onBackPressed() {
        if (isTablet) {
            if (searchTabletCtrl.isSearching()) {
                hcState = SEARCH_TYPING;
            }
        } else {
            if (hcState == SEARCH_HOME) {
                if (searchPhoneCtrl.isSearching()) {
                    hcState = SEARCH_TYPING;
                }
            }
        }
        switch (hcState) {
            case QN_OPEN: {
                if (isTablet) {
                    // No need to handle
                } else {
                    closeInbox();
                }
                return true;
            }
            case QN_OPEN_FROM_SEARCH: {
                myRoot.removeViewAt(myRoot.getChildCount() - 1);
                updateState(SEARCH_TYPING);
                return true;
            }
            case SEARCH_TYPING: {
                if (isTablet) {
                    searchTabletCtrl.clearSearch();
                } else {
                    searchPhoneCtrl.clearSearch();
                }
                updateState(SEARCH_HOME);
                return true;
            }
            case SEARCH_HOME: {
                myRoot.removeViewAt(myRoot.getChildCount() - 1);
                updateState(HOME);
                hideKeyboard();
                return true;
            }
            case HOME:
                // default to applyNext;
        }

        UIRouter.getInstance().pop();

        return true;
    }

    private void showQuestion(Question qn, String grpName) {
        UserIQSDKInternal.getAnalyticsManager().onHelpDetail(qn.id, true);
        AnswerAdapter answerAdapter = new AnswerAdapter(ctx, qn);

        groupTitleTV.setText(grpName);
        qaListView.setAdapter(answerAdapter);
        qaListView.setHeaderText(qn.name);
    }

    private void showWithAnimation(Question qn) {
        UserIQSDKInternal.getAnalyticsManager().onHelpDetail(qn.id, true);
        updateState(QN_OPEN);
        isAlreadyClosedByDrag = false;
        AnswerAdapter answerAdapter = new AnswerAdapter(ctx, qn);
        LinearLayout qnItem = scrollHolder.findViewWithTag(qn.id);

        SyncData syncData = UserIQSDKInternal.getSyncData();
        final QGroup qGroup = syncData.getQGroupForQuestionId(qn.id);

        inboxQAView.setAdapter(answerAdapter);
        inboxQAView.setHeaderText(qn.name);
        inboxQAView.openWithAnim(qnItem);
        inboxQAView.setDragListener(new InboxLayoutBase.OnDragStateChangeListener() {
            @Override
            public void onInboxOpen() {
                System.out.println("Setting group name");
                title.setText(qGroup.name);
            }

            @Override
            public void onInboxClosing() {
                System.out.println("Setting help");
                title.setText("Help and Support");
            }

            @Override
            public void onInboxClose() {
                hcState = HOME;
                isAlreadyClosedByDrag = true;
//                UIRouter.getInstance().push(UIRouter.HELP_CENTER);
            }
        });
    }

    public void closeInbox() {
        if (isAlreadyClosedByDrag) return;
        inboxQAView.setVisibility(View.INVISIBLE);
        inboxQAView.closeWithAnim();
        title.setText("Help and Support");
    }

    /**
     * hydrate CtxHelp views with real data
     * <p>
     * Hydrate sounds sexy! from https://stackoverflow.com/a/6991192
     */
    private boolean hydrate(List<QGroup> qGroups) {
        if (qGroups == null) return false;
        scrollHolder.removeAllViews();

        scrollHolder.addView(hcWelcome);

        if (isTablet) {
            if (qGroups.size() > 0) {
                showQuestion(qGroups.get(0).questions.get(0), qGroups.get(0).name);
            }
        }
        for (final QGroup group : qGroups) {
            scrollHolder.addView(buildGroupHeading(group.name));
            for (final Question qn : group.questions) {
                LinearLayout qnItem = buildQuestionRow(scrollHolder, qn.name);
                scrollHolder.addView(qnItem);
                scrollHolder.addView(buildDivider());

                qnItem.setTag(qn.id);
                qnItem.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if (isTablet) {
                            showQuestion(qn, group.name);
                        } else {
                            showWithAnimation(qn);
                        }
                    }
                });
            }
        }
        if (qGroups.size() == 0) {
            Typeface iconFont = UnfoldFontIcon.getTypeface(ctx);

            TextView blankPlaceholder = new TextView(ctx);
            blankPlaceholder.setText(ctx.getString(R.string.uiq_ic_blank_image));
            blankPlaceholder.setTextColor(ctx.getResources().getColor(R.color.empty_state_grey));
            blankPlaceholder.setGravity(Gravity.CENTER);
            blankPlaceholder.setTextSize(150);
            blankPlaceholder.setTypeface(iconFont);
            scrollHolder.addView(blankPlaceholder);
        }

        // this is very important when scrollview's height is greater then
        // the height of scrollview's child view
        scrollHolder.addView(buildEmptyView());

        return true;
    }

    private View buildEmptyView() {
        View v = new View(ctx);
        v.setLayoutParams(new LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        v.setTag("empty_view");
        return v;
    }

    private FrameLayout buildPhoneView() {
        LayoutInflater li = LayoutInflater.from(ctx);

        LinearLayout qnListLayout = new LinearLayout(ctx);
        qnListLayout.setLayoutParams(new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        qnListLayout.setOrientation(LinearLayout.VERTICAL);
        qnListLayout.setBackgroundColor(Color.WHITE);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            qnListLayout.setTransitionName(ctx.getString(R.string.transition_morph_view));
//        }

        //Toolbar
        qnListLayout.addView(getToolbar(qnListLayout));

        // Questions container
        FrameLayout frameLayout = new FrameLayout(ctx);
        frameLayout.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));

        InboxStickyScrollView inboxStickyScrollView = (InboxStickyScrollView) getStickyScrollView();
        frameLayout.addView(inboxStickyScrollView);

        inboxQAView = new InboxQuestionAnswerView(ctx);
        inboxQAView.setLayoutParams(new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        inboxQAView.setVisibility(View.INVISIBLE);
        inboxQAView.setBackgroundScrollView(inboxStickyScrollView);

        frameLayout.addView(inboxQAView);

        setHcWelcome();

        qnListLayout.addView(frameLayout);

        FrameLayout root = new FrameLayout(ctx);
        root.setLayoutParams(new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        root.addView(qnListLayout);

        return root;
    }

    private FrameLayout buildTabletView() {
        LinearLayout container = new LinearLayout(ctx);
        container.setLayoutParams(new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));

        LinearLayout hc = new LinearLayout(ctx);
        LinearLayout.LayoutParams lpHc = new LinearLayout.LayoutParams(0, MATCH_PARENT);
        lpHc.weight = 2;
        hc.setLayoutParams(lpHc);
        hc.setOrientation(LinearLayout.VERTICAL);
        hc.setBackgroundColor(Color.WHITE);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            hc.setTransitionName(ctx.getString(R.string.transition_morph_view));
//        }

        //Toolbar
        hc.addView(getToolbar(hc));

        FrameLayout frameLayout = new FrameLayout(ctx);
        frameLayout.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));

        StickyScrollView stickyScrollView = getStickyScrollView();
        frameLayout.addView(stickyScrollView);

        setHcWelcome();

        hc.addView(frameLayout);

        LinearLayout groupTitleLL = (LinearLayout) LayoutInflater.from(ctx).inflate(R.layout.uiq_toolbar, hc, false);
        groupTitleLL.findViewById(R.id.tv_back).setVisibility(View.GONE);
        ((LinearLayout) groupTitleLL.findViewById(R.id.ll_container)).setGravity(Gravity.CENTER);
        groupTitleTV = groupTitleLL.findViewById(R.id.tv_toolbar_title);
        groupTitleTV.setGravity(Gravity.CENTER);

        LinearLayout ansView = new LinearLayout(ctx);
        LinearLayout.LayoutParams lpAns = new LinearLayout.LayoutParams(0, MATCH_PARENT);
        lpAns.weight = 3;
        ansView.setLayoutParams(lpAns);
        ansView.setOrientation(LinearLayout.VERTICAL);
        ansView.setBackgroundColor(Color.WHITE);
        ansView.setGravity(Gravity.CENTER);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ansView.setTransitionName(ctx.getString(R.string.transition_morph_view));
        }

        qaListView = new QuestionAnswerView(ctx);
        qaListView.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));

        ansView.addView(groupTitleLL);
        ansView.addView(qaListView);

        View divider = new View(ctx);
        divider.setLayoutParams(new LinearLayout.LayoutParams(1, MATCH_PARENT));
        divider.setBackgroundColor(Color.rgb(235, 235, 235));

        container.addView(hc);
        container.addView(divider);
        container.addView(ansView);

        FrameLayout root = new FrameLayout(ctx);
        root.setLayoutParams(new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        root.addView(container);

        return root;
    }

    private View getToolbar(ViewGroup root) {
        View toolbar = LayoutInflater.from(ctx).inflate(R.layout.uiq_toolbar, root, false);
        title = toolbar.findViewById(R.id.tv_toolbar_title);
        return toolbar;
    }

    private StickyScrollView getStickyScrollView() {
        StickyScrollView stickyScrollView;
        if (isTablet) {
            stickyScrollView = new StickyScrollView(ctx);
        } else {
            stickyScrollView = new InboxStickyScrollView(ctx);
        }
        stickyScrollView.setOverScrollMode(InboxStickyScrollView.OVER_SCROLL_ALWAYS);
        stickyScrollView.setLayoutParams(new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));

        scrollHolder = new LinearLayout(ctx);
        scrollHolder.setLayoutParams(new LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        scrollHolder.setOrientation(LinearLayout.VERTICAL);

        stickyScrollView.addView(scrollHolder);
        return stickyScrollView;
    }

    private void setHcWelcome() {
        hcWelcome = (LinearLayout) LayoutInflater.from(ctx).inflate(R.layout.hc_welcome, scrollHolder, false);

        EditText editText = hcWelcome.findViewById(R.id.et_search);
        editText.setFocusable(false);
        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View searchView;
                if (isTablet) {
                    searchView = searchTabletCtrl.getMyRoot();
                } else {
                    searchView = searchPhoneCtrl.getMyRoot();
                }
                myRoot.addView(searchView);
                updateState(SEARCH_HOME);
                EditText et = searchView.findViewById(R.id.et_search);
                et.requestFocus();
                InputMethodManager imm = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(hcWelcome.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private TextView buildGroupHeading(String text) {

        TextView group = new TextView(ctx);
        group.setTag("sticky");
        group.setPadding(dpToPx(20), 0, 0, 0);
        group.setTextSize(16);
        group.setTextColor(ctx.getResources().getColor(android.R.color.black));
        group.setBackgroundColor(Color.argb(0xFF, 0xF6, 0xF6, 0xF6));
        group.setGravity(Gravity.CENTER_VERTICAL);

        group.setText(text);
        group.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, dpToPx(40)));

        return group;
    }

    private LinearLayout buildQuestionRow(ViewGroup root, String text) {
        LayoutInflater li = LayoutInflater.from(ctx);
        LinearLayout layout = (LinearLayout) li.inflate(R.layout.hc_qn_group_item, root, false);

        TextView textView = layout.findViewById(R.id.qn_name);
        textView.setText(text);

        return layout;
    }


    private View buildDivider() {
        View divider = new View(ctx);
        divider.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, 1));
        divider.setBackgroundColor(ctx.getResources().getColor(R.color.divider_grey));
        return divider;
    }

    private void updateState(int state) {
        hcState = state;
    }

    public static final int HOME = 0;
    public static final int QN_OPEN = 1;
    public static final int SEARCH_HOME = 2;
    public static final int SEARCH_TYPING = 3;
    public static final int QN_OPEN_FROM_SEARCH = 4;

    @Retention(SOURCE)
    @IntDef({HOME, QN_OPEN, SEARCH_HOME, SEARCH_TYPING, QN_OPEN_FROM_SEARCH})
    @interface HC_STATE {
    }
}
