package com.useriq.sdk;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import com.useriq.Logger;
import com.useriq.sdk.capture.ViewRoot;
import com.useriq.sdk.ctxHelp.CtxHelpCtrl;
import com.useriq.sdk.helpcenter.HelpCenterCtrl;
import com.useriq.sdk.models.Screen;
import com.useriq.sdk.util.SharedPrefUtil;

import java.util.HashMap;
import java.util.Map;

public final class UserIQSDK {
    public static final String TAG = UserIQSDK.class.getSimpleName();
    private static final Logger logger = Logger.init(TAG);

    private static String host;
    private static UserIQSDK userIQSDK;
    private static boolean fabDisabled;
    private String apiKey;
    private Application application;
    Activity activity;

    private UserIQSDK(String apiKey, Application application) {
        this.apiKey = apiKey;
        this.application = application;
        logger.d("UserIQ SDK initialized with api key: " + apiKey);
    }

    private UserIQSDK(String apiKey, Activity activity) {
        this(apiKey, activity.getApplication());
        this.activity = activity;
    }

    public static void setHost(String host) {
        if (host.isEmpty()) logger.d("host can't be null or blank");
        UserIQSDK.host = host.startsWith("http") ? host
                : "https://" + host + ".useriq.com/sdk";
    }

    @Deprecated
    public static void init(Application application, String apiKey) {
        if (apiKey.isEmpty()) logger.d("api key can't be null or blank");
        userIQSDK = new UserIQSDK(apiKey, application);
        setAnonymousUser(application.getApplicationContext());
    }

    public static void init(Activity activity, String apiKey) {
        if (apiKey.isEmpty()) logger.d("api key can't be null or blank");
        userIQSDK = new UserIQSDK(apiKey, activity);
        setAnonymousUser(activity);
    }

    public static void setUser(Context context, User user) {
        if (userIQSDK == null) {
            Toast.makeText(context, "UserIQSDK: setUser called before init.", Toast.LENGTH_SHORT).show();
            logger.e("UserIQSDK hasn't been initialized yet. Call init first.", null);
            return;
        }
        setUser(user);
    }

    @Deprecated
    public static void setUser(User user) {
        if (user == null) {
            logger.e("user can't be null", null);
            return;
        }

        Map<String, Object> userMap;
        if (UserIQSDKInternal.getInstance() != null) {
            userMap = UserIQSDKInternal.getInstance().getSDKUser();
            if ((user.data.containsKey("userId") && userMap.containsKey("userId") && !userMap.get("userId").equals(user.data.get("userId")))
                    || (user.data.containsKey("accountId") && userMap.containsKey("accountId") && !userMap.get("accountId").equals(user.data.get("accountId")))) {
                userMap.clear();
            }
        } else {
            userMap = new HashMap<>();
        }

        userMap.putAll(user.data);

        if (!userMap.containsKey("userId")) {
            logger.e("UserId required. Initialization failed.", null);
            return;
        }
        if (!userMap.containsKey("name")) {
            logger.e("name required. Initialization failed.", null);
            return;
        }
        if (!userMap.containsKey("email")) {
            logger.e("emailId required. Initialization failed.", null);
            return;
        }
        if (!userMap.containsKey("accountId")) {
            logger.e("accountId required. Initialization failed.", null);
            return;
        }
        if (!userMap.containsKey("accountName")) {
            logger.e("accountName required. Initialization failed.", null);
            return;
        }
        if (UserIQSDKInternal.getInstance() == null) {
            logger.d("initializing the user with data: " + userMap);
            initUserIQInternal(userMap);
        } else {
            logger.d("updating the user with data: " + userMap);
            UserIQSDKInternal.getInstance().updateUserInSdkConfig(userMap);
        }
    }

    private static void setAnonymousUser(Context context) {
        User user = new UserBuilder()
                .setId("UIQ_MOBILE")
                .setName("UIQ ANONYMOUS")
                .setEmail("nobody@useriq.com")
                .setAccountId("UIQ_Account")
                .setAccountName("ANON ACCOUNT")
                .setSignupDate("")
                .build();
        setUser(context, user);
    }

    public static void logOut() {
        if (UserIQSDKInternal.getInstance() == null) {
            logger.d("Called LogOut before UserIQSDK initialized");
            return;
        }
        logger.d("LogOut called");
        UserIQSDKInternal.getInstance().getSDKUser().clear();
        setAnonymousUser(userIQSDK.activity);
    }

    public static boolean showCtxHelp(Context context) {
        if (UserIQSDKInternal.getInstance() == null) {
            Toast.makeText(context, "UserIQSDK: sdk not initialized, can't show Contextual Help", Toast.LENGTH_SHORT).show();
            logger.e("Can't show Contextual Help, UserIQSDK hasn't been initialized yet.", null);
            return false;
        }
        return showCtxHelp();
    }

    @Deprecated
    public static boolean showCtxHelp() {
        if (UserIQSDKInternal.getInstance() == null) {
            logger.e("Can't show Contextual Help, UserIQSDK hasn't been initialized yet.", null);
            return false;
        }
        Screen screen = UserIQSDKInternal.getCurrScreen();
        if (screen == null) return false;
        if (UserIQSDKInternal.getSyncData().getCtxHelpForScreen(screen.id) == null) {
            return false;
        }
        UserIQSDKInternal.getCurrActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                UIRouter.getInstance().push(new CtxHelpCtrl());
                logger.d("showing Contextual Help");
            }
        });
        return true;
    }

    public static boolean showHelpCentre(Context context) {
        if (UserIQSDKInternal.getInstance() == null) {
            Toast.makeText(context, "UserIQSDK: sdk not initialized, can't show HelpCenter", Toast.LENGTH_SHORT).show();
            logger.e("Can't show HelpCentre, UserIQSDK hasn't been initialized yet.", null);
            return false;
        }
        return showHelpCentre();
    }

    @Deprecated
    public static boolean showHelpCentre() {
        if (UserIQSDKInternal.getInstance() == null) {
            logger.e("Can't show HelpCentre, UserIQSDK hasn't been initialized yet.", null);
            return false;
        }
        if (UIManager.getInstance().getUiRootView().getRootType() != ViewRoot.ACTIVITY) {
            return false;
        }
        UserIQSDKInternal.getCurrActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                UIRouter.getInstance().push(new HelpCenterCtrl());
                logger.d("showing HelpCentre");
            }
        });
        return true;
    }

    public static void disableFAB() {
        logger.d("disableFAB called");
        fabDisabled = true;
    }

    private static void initUserIQInternal(Map<String, Object> userData) {
        if (userData == null) {
            logger.d("Initialization failed: user data is null");
            return;
        }
        UserIQSDKInternal.init(
                userIQSDK.application,
                userIQSDK.apiKey,
                userData,
                null,
                host,
                null,
                userIQSDK.activity,
                fabDisabled);
    }

    public static class User {
        private static final String SHARED_PREF_KEY = "UserData";
        Map<String, Object> data;

        private User(Map<String, Object> user) {
            this.data = user;
        }

        public static Map<String, Object> getSavedUserData(Application application) {
            return SharedPrefUtil.loadMap(application, SHARED_PREF_KEY);
        }

        public void save(Application application) {
            SharedPrefUtil.saveMap(application, SHARED_PREF_KEY, data);
        }

    }

    public static class UserBuilder {
        HashMap<String, String> params = new HashMap<>();
        private String userId, userName, emailId, accountId, accountName, signupDate;

        public UserBuilder setId(String userId) {
            this.userId = userId;
            return this;
        }

        public UserBuilder setName(String name) {
            this.userName = name;
            return this;
        }

        public UserBuilder setEmail(String email) {
            this.emailId = email;
            return this;
        }

        public UserBuilder setAccountId(String accId) {
            this.accountId = accId;
            return this;
        }

        public UserBuilder setAccountName(String name) {
            this.accountName = name;
            return this;
        }

        public UserBuilder setSignupDate(String signupDate) {
            this.signupDate = signupDate;
            return this;
        }

        public UserBuilder addParams(String key, String value) {
            params.put(key, value);
            return this;
        }

        public User build() {

            HashMap<String, Object> user = new HashMap<>();
            if (userId != null)
                user.put("userId", userId);
            if (userName != null)
                user.put("name", userName);
            if (emailId != null)
                user.put("email", emailId);
            if (accountId != null)
                user.put("accountId", accountId);
            if (accountName != null)
                user.put("accountName", accountName);
            if (signupDate != null) {
                user.put("signupDate", signupDate);
            }
            for (String key : params.keySet()) {
                user.put(key, params.get(key));
            }
            return new User(user);
        }
    }

}
