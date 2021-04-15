package com.useriq.sdk.helpcenter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.useriq.Logger;
import com.useriq.sdk.R;
import com.useriq.sdk.UIManager;
import com.useriq.sdk.UIRouter;
import com.useriq.sdk.UserIQSDKInternal;
import com.useriq.sdk.fonticon.UnfoldFontIcon;
import com.useriq.sdk.helpcenter.views.QuestionAnswerView;
import com.useriq.sdk.models.Question;
import com.useriq.sdk.models.SyncData;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * @author sudhakar
 * @created 23-Oct-2018
 */
public class AnswerWithToolbarCtrl implements UIRouter.Controller {
    private static final Logger logger = Logger.init(AnswerAdapter.class.getSimpleName());

    private final Context ctx;
    private final LinearLayout myRoot;
    private final String qId;
    private QuestionAnswerView qaListView;

    public AnswerWithToolbarCtrl(String qId) {
        this.qId = qId;
        this.ctx = UserIQSDKInternal.getContext();
        this.myRoot = buildView();
    }

    @Override
    public View onEnter() {
        SyncData syncData = UserIQSDKInternal.getSyncData();
        hydrate(syncData.getQuestionById(qId));

        return myRoot;
    }


    @Override
    public void onExit() { }

    @Override
    public boolean onBackPressed() {
        UIRouter.getInstance().pop();
        return true;
    }

    public ViewGroup getMyRoot() {
        return myRoot;
    }

    /**
     * hydrate CtxHelp views with real data
     *
     * Hydrate sounds sexy! from https://stackoverflow.com/a/6991192
     */
    public boolean hydrate(Question qn) {
        if(qn == null) return false;
        qaListView.setAdapter(new AnswerAdapter(ctx, qn));
        qaListView.setHeaderText(qn.name);
        return true;
    }

    private LinearLayout buildView() {

        LinearLayout ansView = new LinearLayout(ctx);
        ansView.setLayoutParams(new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        ansView.setOrientation(LinearLayout.VERTICAL);
        ansView.setBackgroundColor(Color.WHITE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ansView.setTransitionName(ctx.getString(R.string.transition_morph_view));
            ansView.setTag(R.id.viewBgColor, Color.WHITE);
            ansView.setTag(R.id.viewRadius, 0);
        }

        //Toolbar
        View toolbar = LayoutInflater.from(ctx).inflate(R.layout.uiq_toolbar, ansView, false);
        toolbar.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        ansView.addView(toolbar);
        Typeface iconFont = UnfoldFontIcon.getTypeface(UserIQSDKInternal.getContext());
        final TextView backBtn = toolbar.findViewById(R.id.tv_back);

        if(backBtn != null) {
            backBtn.setTypeface(iconFont);
            backBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }

        qaListView = new QuestionAnswerView(ctx);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        params.weight = 1;
        qaListView.setLayoutParams(params);

        ansView.addView(qaListView);
        return ansView;
    }

}
