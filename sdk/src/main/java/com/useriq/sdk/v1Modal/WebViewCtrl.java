package com.useriq.sdk.v1Modal;

import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.useriq.Logger;
import com.useriq.sdk.R;
import com.useriq.sdk.UIRouter;
import com.useriq.sdk.UserIQSDKInternal;
import com.useriq.sdk.fonticon.UnfoldFontIcon;

/**
 * @author sudhakar
 * @created 25-Oct-2018
 */
public class WebViewCtrl implements UIRouter.Controller {
    private final static Logger logger = Logger.init(WebViewCtrl.class.getSimpleName());

    private final LinearLayout myRoot;
    private String url;
    private WebView webView;
    private ProgressBar progress;

    WebViewCtrl(String url) {
        this.myRoot = buildView();
        this.url = url;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            myRoot.setTransitionName(UserIQSDKInternal.getContext().getString(R.string.transition_morph_view));
            myRoot.setTag(R.id.viewBgColor, Color.WHITE);
            myRoot.setTag(R.id.viewRadius, 0);
        }
    }

    @Override
    public View onEnter() {
        final TextView title = myRoot.findViewById(R.id.tv_title);
        final TextView cross = myRoot.findViewById(R.id.cross);
        cross.setTypeface(UnfoldFontIcon.getTypeface(UserIQSDKInternal.getContext()));
        cross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIRouter.getInstance().pop();
            }
        });
        webView.loadUrl(url);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                progress.setVisibility(View.GONE);
                title.setText(view.getTitle());
            }
        });
        return myRoot;
    }

    @Override
    public void onExit() {

    }

    @Override
    public boolean onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            UIRouter.getInstance().pop();
        }
        return true;
    }

    private LinearLayout buildView() {
        LayoutInflater li = LayoutInflater.from(UserIQSDKInternal.getContext());
        LinearLayout ll = (LinearLayout) li.inflate(R.layout.uiq_webview, null, false);
        FrameLayout frameLayout = ll.findViewById(R.id.fl_webview_container);
        progress = ll.findViewById(R.id.progress);
        webView = new WebView(UserIQSDKInternal.getApp());
        frameLayout.addView(webView);
        return ll;
    }

}
