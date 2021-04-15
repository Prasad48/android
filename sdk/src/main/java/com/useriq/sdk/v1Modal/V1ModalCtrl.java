package com.useriq.sdk.v1Modal;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.useriq.Logger;
import com.useriq.sdk.R;
import com.useriq.sdk.UIManager;
import com.useriq.sdk.UIRouter;
import com.useriq.sdk.UserIQSDKInternal;
import com.useriq.sdk.capture.ViewRoot;
import com.useriq.sdk.helpcenter.AnswerWithToolbarCtrl;
import com.useriq.sdk.models.SyncData;
import com.useriq.sdk.models.V1Modal;
import com.useriq.sdk.walkthrough.WalkthroughCtrl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * @author sudhakar
 * @created 25-Oct-2018
 */
public class V1ModalCtrl implements UIRouter.Controller {
    private final static Logger logger = Logger.init(V1ModalCtrl.class.getSimpleName());

    private final Context ctx;
    private final FrameLayout myRoot;
    private final String modalId;
    private final int dialogRadius;
    private final int dialogBgColor;
    private String modalType;
    private String selectedValue;

    public V1ModalCtrl(String modalId) {
        this.modalId = modalId;
        this.ctx = UserIQSDKInternal.getContext();
        this.myRoot = buildView();
        this.dialogRadius = ctx.getResources().getDimensionPixelSize(R.dimen.dialog_corners);
        this.dialogBgColor = ctx.getResources().getColor(R.color.v1modalBg);
    }

    @Override
    public View onEnter() {
        if (UIManager.getInstance().getUiRootView().getRootType() != ViewRoot.ACTIVITY) {
            return null;
        }
        UserIQSDKInternal.getAnalyticsManager().onModalVisible(modalId, true);
        SyncData syncData = UserIQSDKInternal.getSyncData();
        hydrate(syncData.getV1ModalById(modalId));

        return myRoot;
    }

    @Override
    public void onExit() {
        UserIQSDKInternal.getAnalyticsManager().onModalVisible(modalId, false);
    }

    @Override
    public boolean onBackPressed() {
        UIRouter.getInstance().pop();
        return true;
    }

    private FrameLayout buildView() {
        LayoutInflater li = LayoutInflater.from(ctx);
        FrameLayout dummyRoot = new FrameLayout(ctx);
        return (FrameLayout) li.inflate(R.layout.uiq_v1modal, dummyRoot, false);
    }

    private boolean hydrate(V1Modal modal) {
        if (modal == null) return false;

        modalType = modal.type;

        final CustomLayout customLayout = new CustomLayout(ctx, modal.layout, imageProvider, clickHandler, modal);

        FrameLayout.LayoutParams frameParam = new FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        frameParam.gravity = Gravity.CENTER;
        myRoot.addView(customLayout, frameParam);

        customLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                customLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                if (myRoot.getChildCount() != 1) {
                    myRoot.removeViewAt(0);
                }
            }
        });

        return true;
    }

    private CustomLayout.ClickHandler clickHandler = new CustomLayout.ClickHandler() {

        @Override
        public void onStarRatingClick(String rating) {
            selectedValue = rating;
        }

        @Override
        public void trackButton(String buttonId) {
            UserIQSDKInternal.getAnalyticsManager().onModalBtnClick(modalId, buttonId);
        }

        @Override
        public void close() {
            onBackPressed();
        }

        @Override
        public void onNpsClick(String id) {
            selectedValue = id;
        }

        @Override
        public void openURL(String url) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                ctx.startActivity(i);
                onBackPressed();
            } catch (Exception e) {
                Toast.makeText(ctx, "No activity found to handle url", Toast.LENGTH_SHORT).show();
                onBackPressed();
                logger.e("No activity found to handle url: " + url, e);
            }
        }

        @Override
        public void onSubmit(String response) {
            if (selectedValue != null) {
                switch (modalType) {
                    case V1Modal.MODAL_TYPE_starDefault:
                    case V1Modal.MODAL_TYPE_starFooter:
                    case V1Modal.MODAL_TYPE_starHeader: {
                        UserIQSDKInternal.getAnalyticsManager().onRatingSubmit(modalId, selectedValue, response);
                        Toast.makeText(ctx, "Thank you for the feedback", Toast.LENGTH_SHORT).show();
                        selectedValue = null;
                        break;
                    }

                    case V1Modal.MODAL_TYPE_npsDefault:
                    case V1Modal.MODAL_TYPE_npsFooter:
                    case V1Modal.MODAL_TYPE_npsHeader: {
                        UserIQSDKInternal.getAnalyticsManager().onNpsSubmit(modalId, selectedValue, response);
                        Toast.makeText(ctx, "Thank you for the feedback", Toast.LENGTH_SHORT).show();
                        selectedValue = null;
                        break;
                    }
                }
                close();
            } else {
                Toast.makeText(ctx, "Please select a value.", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void showWalkthrough(String wtId) {
            onBackPressed();
            UIRouter.getInstance().push(new WalkthroughCtrl(wtId));
        }

        @Override
        public void showQuestion(String quesId) {
            onBackPressed();
            UIRouter.getInstance().push(new AnswerWithToolbarCtrl(quesId));
        }

        @Override
        public void openWebview(String url) {
            onBackPressed();
            UIRouter.getInstance().push(new WebViewCtrl(url));
        }

        @Override
        public void openModalview(final String value) {
            onBackPressed();
            UIRouter.getInstance().push(new V1ModalCtrl(value));
        }
    };

    private CustomLayout.ImageProvider imageProvider = new CustomLayout.ImageProvider() {

        @Override
        public Bitmap getImageById(String assetId) {
            File assetFile = UserIQSDKInternal.getInstance().getAsset(assetId);
            if (assetFile == null) {
                logger.e("getImageById: null asset '" + assetId + "'", null);
                return null;
            }
            Bitmap bitmap = null;
            try {
                FileInputStream is = new FileInputStream(assetFile);
                bitmap = BitmapFactory.decodeStream(is);
            } catch (FileNotFoundException e) {
                logger.e(assetId + " : Image not found", e);
            }
            return bitmap;
        }
    };

}
