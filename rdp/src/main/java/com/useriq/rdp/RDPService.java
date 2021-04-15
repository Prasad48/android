package com.useriq.rdp;

import android.graphics.Point;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.useriq.Logger;
import com.useriq.SimpleRPC;
import com.useriq.rdp.wrappers.InputManager;

import java.util.Map;

public class RDPService implements SimpleRPC.IService {
    private static final Logger logger = Logger.init(RDPService.class.getSimpleName());
    private final Device device;
    private final MotionEvent.PointerProperties[] pointerProperties = {new MotionEvent.PointerProperties()};
    private long lastMouseDown;
    private MotionEvent.PointerCoords[] pointerCoords = {new MotionEvent.PointerCoords()};
    private final KeyCharacterMap charMap = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD);

    RDPService(Device device) {
        this.device = device;
        initPointer();
    }

    private void initPointer() {
        MotionEvent.PointerProperties props = pointerProperties[0];
        props.id = 0;
        props.toolType = MotionEvent.TOOL_TYPE_FINGER;

        MotionEvent.PointerCoords coords = pointerCoords[0];
        coords.orientation = 0;
        coords.pressure = 1;
        coords.size = 1;
        coords.toolMajor = 1;
        coords.toolMinor = 1;
        coords.touchMajor = 1;
        coords.touchMinor = 1;
    }

    @SimpleRPC.Expose
    public boolean onCtrlCmd(long id) throws Exception {

        CtrlCmd ctrlCmd = CtrlCmd.getCmd((int) id);
        Ln.i("onCtrlCmd" + ctrlCmd);

        if (ctrlCmd == CtrlCmd.NONE) throw new Exception("Invalid Ctrl cmd.");

        switch (ctrlCmd) {
            case HOME:
                return device.injectKeycode(KeyEvent.KEYCODE_HOME);
            case BACK:
                return device.injectKeycode(KeyEvent.KEYCODE_BACK);
            case RECENTS:
                return device.injectKeycode(KeyEvent.KEYCODE_APP_SWITCH);
            case ROTATE_LEFT:
//                device.rotateLeft();
                return true;
            case ROTATE_RIGHT:
//                device.rotateRight();
                return true;
        }
        return false;
    }

    @SimpleRPC.Expose
    public boolean onPaste(String data) {
        device.injectText(data);
        return true;
    }

    @SimpleRPC.Expose
    public boolean onKey(Map<String, Object> data) {
        Ln.d("onKey(): " + data.toString());
        long now = SystemClock.uptimeMillis();
        int metaState = 0;

        String key = (String) data.get("key");
        int[] codes = KeyMap.keys.get(key);
        int keyCode = -1;

        if (codes == null) {
            logger.e("Invalid keycode: '" + key +"'", null);
            return false;
        }

        keyCode = codes[0];
        if (codes.length == 2) {
            if (codes[1] == KeyMap.KEY_SHIFT) metaState = 1;
        }

        KeyEvent event = new KeyEvent(now, now, 0, keyCode, 1, metaState, KeyCharacterMap.VIRTUAL_KEYBOARD, 0, 0,
                InputDevice.SOURCE_KEYBOARD);
        device.injectInputEvent(event, InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
        return true;
    }


    @SimpleRPC.Expose
    public boolean onText(Map<String, Object> data) {
        return false;
    }

    /**
     * onTouch injects mouse touch on screen
     * <br /><br />
     * <p>
     * <b> action:</b> <br />
     * 0 - {@link MotionEvent#ACTION_DOWN} <br />
     * 1 - {@link MotionEvent#ACTION_UP} <br />
     * 2 - {@link MotionEvent#ACTION_MOVE} <br />
     * 3 - {@link MotionEvent#ACTION_CANCEL} <br />
     * <p>
     * <br />
     * <b> buttons:</b><br />
     * 1 - {@link MotionEvent#BUTTON_PRIMARY} <br />
     * 2 - {@link MotionEvent#BUTTON_SECONDARY} <br />
     * 4 - {@link MotionEvent#BUTTON_TERTIARY} <br />
     * 8 - {@link MotionEvent#BUTTON_BACK} <br />
     * 16 - {@link MotionEvent#BUTTON_FORWARD} <br />
     * Buttons can be combined bitwise
     *
     * @param data data fot touch event
     */
    @SimpleRPC.Expose
    public boolean onTouch(Map<String, Object> data) {
        int action = (int) ((long) data.get("action")); // data.get("action") returns long so, typecast to long then to int
        int buttons = (int) ((long) data.get("buttons"));
        long now = SystemClock.uptimeMillis();
        if (action == MotionEvent.ACTION_DOWN) {
            lastMouseDown = now;
        }

        Map<String, Long> pos = (Map<String, Long>) data.get("position");

        Position position = new Position(
                pos.get("x").intValue(),
                pos.get("y").intValue(),
                pos.get("w").intValue(),
                pos.get("h").intValue()
        );

        Point point = device.getPhysicalPoint(position);
        Ln.i("Point: " + (point == null ? "" : point.toString()));

        if (point == null) {
            // ignore event
            return false;
        }

        MotionEvent.PointerCoords coords = pointerCoords[0];
        coords.x = point.x;
        coords.y = point.y;

        MotionEvent event = MotionEvent.obtain(lastMouseDown, now, action, 1, pointerProperties, pointerCoords, 0, buttons, 1f, 1f, 0, 0,
                InputDevice.SOURCE_TOUCHSCREEN, 0);

        return device.injectInputEvent(event, InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
    }

    @SimpleRPC.Expose
    public void onScroll(Map<String, Object> msg) {

    }

    private enum CtrlCmd {
        HOME,
        BACK,
        RECENTS,
        ROTATE_LEFT,
        ROTATE_RIGHT,
        NONE;

        public static CtrlCmd getCmd(int cmd) {
            switch (cmd) {
                case 1:
                    return HOME;
                case 2:
                    return BACK;
                case 3:
                    return RECENTS;
                case 4:
                    return ROTATE_LEFT;
                case 5:
                    return ROTATE_RIGHT;
            }
            return NONE;
        }
    }
}
