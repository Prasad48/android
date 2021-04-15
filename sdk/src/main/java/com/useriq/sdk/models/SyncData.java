package com.useriq.sdk.models;

import com.useriq.Logger.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.useriq.sdk.util.Utils.toInt;

/**
 * SyncData holds all data sent on onSync API from server
 * <p>
 * SyncData itself is intended to be used as a single instance with mutable
 * data structure on the top level objects. Sub objects (except primitives) are
 * immutable by default. ie top-level keys are update in "onSync" but values
 * themselves are immutable & safe to pass around.
 */
public class SyncData {
    public static final String SDK_ENABLED = "sdkEnabled";
    public static final String FAB_ENABLED = "fabEnabled";
    static final String EVENT_OPTS = "eventOpts";
    private static final String VERSION = "syncVersion";
    public static final String ACTIVATE = "activate";
    public static final String LOG_LEVEL = "logLevel";
    public static final String V1_MODAL = "v1Modal";
    public static final String SCREENS = "screens";
    public static final String ASSETS = "assets";
    static final String THEME = "theme";
    private static final String WALKTHROUGH = "walkthrough";
    private static final String QGROUPS = "qGroup";
    private static final String QUESTION = "question";
    public static final String CTX_HELP = "ctxHelp";
    private static final String HC_WELCOME_TEXT = "hcWelcomeText";

    public static final String NONE = "none";
    public static final String HELP_CENTER = "helpCenter";
    private static final String TRACK_PERFORMANCE = "trackPerformance";

    public long version = -1L;
    public boolean sdkEnabled = true;
    public boolean fabEnabled = true;
    public boolean trackPerformance = false;
    public EventOpts eventOpts = new EventOpts();
    public Theme theme = new Theme(new HashMap<String, Object>());
    public Level logLevel = Level.WARNING;
    public List<Screen> screens = new ArrayList<>();
    List<V1Modal> modals = new ArrayList<>();
    public List<Walkthrough> walkthroughs = new ArrayList<>();
    public List<QGroup> qGroups = new ArrayList<>();
    public List<CtxHelp> ctxHelps = new ArrayList<>();
    public List<String> assets = new ArrayList<>();
    public String hcWelcomeText = "We are here to help you with anything and everything related to App";
    private final AtomicBoolean isSoftActivate = new AtomicBoolean(false);

    private static SyncData instance;

    private SyncData() {
    }

    public static SyncData getInstance() {
        if (instance == null) {
            instance = new SyncData();
        }
        return instance;
    }

    public void update(Map<String, Object> data) {
        if (data.containsKey(VERSION)) {
            Object obj = data.get(VERSION);
            if (obj instanceof Number) {
                this.version = ((Number) obj).longValue();
            }
        }

        if (data.containsKey(SDK_ENABLED)) {
            Object obj = data.get(SDK_ENABLED);
            if (obj instanceof Boolean) {
                this.sdkEnabled = (boolean) obj;
            }
        }

        if (data.containsKey(FAB_ENABLED)) {
            Object obj = data.get(FAB_ENABLED);
            if (obj instanceof Boolean) {
                this.fabEnabled = (boolean) data.get(FAB_ENABLED);
            }
        }

        if (data.containsKey(EVENT_OPTS)) {
            //noinspection unchecked
            Map<String, Object> map = (Map<String, Object>) data.get(EVENT_OPTS);
            this.eventOpts = new EventOpts(map);
        }

        if (data.containsKey(THEME)) {
            //noinspection unchecked
            Map<String, Object> map = (Map<String, Object>) data.get(THEME);
            this.theme = new Theme(map);
        }

        if (data.containsKey(LOG_LEVEL)) {
            int level = toInt(data, LOG_LEVEL, Level.WARNING.ordinal());
            if (level <= Level.values().length) {
                this.logLevel = Level.values()[level-1];
            }
        }

        if (data.containsKey(ASSETS)) {
            //noinspection unchecked
            this.assets = (List<String>) data.get(ASSETS);
        }

        if (data.containsKey(SCREENS)) {
            //noinspection unchecked
            List<Map> screenList = (List<Map>) data.get(SCREENS);
            ArrayList<Screen> newScreens = new ArrayList<>(screenList.size());
            for (int i = 0; i < screenList.size(); i++) {
                newScreens.add(new Screen(screenList.get(i)));
            }
            this.screens = newScreens;
        }

        if (data.containsKey(V1_MODAL)) {
            //noinspection unchecked
            List<Map> modals = (List<Map>) data.get(V1_MODAL);
            ArrayList<V1Modal> newModals = new ArrayList<>(modals.size());
            for (int i = 0; i < modals.size(); i++) {
                newModals.add(new V1Modal(modals.get(i)));
            }
            this.modals = newModals;
        }

        if (data.containsKey(WALKTHROUGH)) {
            //noinspection unchecked
            List<Map> wtList = (List<Map>) data.get(WALKTHROUGH);
            ArrayList<Walkthrough> newWalkthroughs = new ArrayList<>(wtList.size());
            for (int i = 0; i < wtList.size(); i++) {
                newWalkthroughs.add(new Walkthrough(wtList.get(i)));
            }
            this.walkthroughs = newWalkthroughs;
        }

        if (data.containsKey(QGROUPS)) {
            //noinspection unchecked
            List<Map> qgList = (List<Map>) data.get(QGROUPS);
            ArrayList<QGroup> newQGroups = new ArrayList<>(qgList.size());
            for (int i = 0; i < qgList.size(); i++) {
                newQGroups.add(new QGroup(qgList.get(i)));
            }
            this.qGroups = newQGroups;
        }

        if (data.containsKey(CTX_HELP)) {
            //noinspection unchecked
            List<Map> ctxHelpList = (List<Map>) data.get(CTX_HELP);
             ArrayList<CtxHelp> newCtxHelps = new ArrayList<>(ctxHelpList.size());
            for (int i = 0; i < ctxHelpList.size(); i++) {
                newCtxHelps.add(new CtxHelp(ctxHelpList.get(i)));
            }
            this.ctxHelps = newCtxHelps;
        }

        if (data.containsKey(HC_WELCOME_TEXT)) {
            this.hcWelcomeText = (String) data.get(HC_WELCOME_TEXT);
        }

        if (data.containsKey(TRACK_PERFORMANCE)) {
            Object obj = data.get(TRACK_PERFORMANCE);
            if (obj instanceof Boolean) {
                this.trackPerformance = (boolean) obj;
            }
        }
    }

    public QGroup getQGroupForQuestionId(String qId) {
        if(qGroups == null) return null;

        for(QGroup qg: qGroups) {
            for (Question q: qg.questions) {
                if(qId.equals(q.id)) return qg;
            }
        }

        return null;
    }

    public Question getQuestionById(String qId) {
        if(qGroups == null) return null;

        for(QGroup qg: qGroups) {
            for (Question q: qg.questions) {
                if(qId.equals(q.id)) return q;
            }
        }

        return null;
    }

    public Walkthrough getWalkthroughById(String wtId) {
        if(walkthroughs == null) return null;

        for(Walkthrough wt: walkthroughs) {
            if(wtId.equals(wt.id)) return wt;
        }

        return null;
    }

    public List<Question> getAllQuestions() {
        List<Question> questions = new ArrayList<>();

        if(qGroups == null) return questions;

        for(QGroup qg: qGroups) {
            questions.addAll(qg.questions);
        }

        return questions;
    }

    public CtxHelp getCtxHelpForScreen(String screenId) {
        if(ctxHelps == null) return null;

        for(CtxHelp ctxHelp: this.ctxHelps) {
            if (ctxHelp.screenId != null && screenId != null) {
                if (ctxHelp.screenId.equals(screenId)) return ctxHelp;
            }
        }
        return null;
    }

    public V1Modal getV1ModalById(String id) {
        if(modals == null) return null;

        for(V1Modal modal: modals) {
            if(modal.id.equals(id)) return modal;
        }

        return null;
    }

    public CtxHelp getCtxHelpById(String id) {
        if(ctxHelps == null) return null;

        for(CtxHelp ctxHelp: this.ctxHelps) {
            if(ctxHelp.id.equals(id)) return ctxHelp;
        }
        return null;
    }

    public boolean getIsSoftActivate() {
        return isSoftActivate.get();
    }

    public void setIsSoftActivate(boolean isSoftActivate) {
        this.isSoftActivate.set(isSoftActivate);
    }

}