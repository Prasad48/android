package com.useriq.sdk.helpcenter.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Adapter;
import android.widget.ListAdapter;

public class InboxQuestionAnswerView extends InboxLayoutBase<QuestionAnswerView> {
    private QuestionAnswerView dragableView;

    public InboxQuestionAnswerView(Context context) {
        this(context, null);
    }

    public InboxQuestionAnswerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InboxQuestionAnswerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setHeaderText(String text) {
        dragableView.setHeaderText(text);
    }

    @Override
    protected QuestionAnswerView createDragableView(Context context, AttributeSet attrs) {
        dragableView = new QuestionAnswerView(context);
        return dragableView;
    }

    public void setAdapter(ListAdapter adapter){
        dragableView.setAdapter(adapter);
    }

    protected boolean isReadyForDragStart(){
        final Adapter adapter = dragableView.getAdapter();
        if(null == adapter || adapter.isEmpty()){
            return true;
        }else{
            if( dragableView.getFirstVisiblePosition()<=1 ){
                final View firstVisibleChild = dragableView.getChildAt(0);
                if(firstVisibleChild != null){
                    return firstVisibleChild.getTop() >= dragableView.getTop();
                }
            }
        }
        return false;
    }

    protected boolean isReadyForDragEnd(){
        final Adapter adapter = dragableView.getAdapter();

        if (null == adapter || adapter.isEmpty()) {
            return true;
        }
        else {
            final int lastItemPosition = dragableView.getCount() - 1;
            final int lastVisiblePosition = dragableView.getLastVisiblePosition();
            if (lastVisiblePosition >= lastItemPosition - 1) {
                final int childIndex = lastVisiblePosition - dragableView.getFirstVisiblePosition();
                final View lastVisibleChild = dragableView.getChildAt(childIndex);
                if (lastVisibleChild != null) {
                    return lastVisibleChild.getBottom() <= dragableView.getBottom();
                }
            }
        }
        return false;
    }

}
