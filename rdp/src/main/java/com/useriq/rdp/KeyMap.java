package com.useriq.rdp;

import java.util.HashMap;
import java.util.Map;

public class KeyMap {
    private KeyMap() { }

    public static final int KEY_SHIFT = 60;
    public static final int KEY_CTRL = 114;
    public static final int KEY_ALT = 60;
    public static final int KEY_META = 60;
    public static Map<String, int[]> keys = new HashMap<>();

    static {
        keys.put("UNKNOWN", new int[]{0});
        /** Soft Left key. Usually situated below the display on phones and used as a multi-function
         * feature key for selecting a software defined function shown on the bottom left
         * of the display. */
        keys.put("SOFT_LEFT", new int[]{1});
        /** Soft Right key. Usually situated below the display on phones and used as a multi-function
         * feature key for selecting a software defined function shown on the bottom right
         * of the display. */
        keys.put("SOFT_RIGHT", new int[]{2});
        /** Home key. This key is handled by the framework and is never delivered to applications. */
        keys.put("HOME", new int[]{3});
        keys.put("BACK", new int[]{4});
        keys.put("CALL", new int[]{5});
        keys.put("ENDCALL", new int[]{6});
        keys.put("0", new int[]{7});
        keys.put("1", new int[]{8});
        keys.put("2", new int[]{9});
        keys.put("3", new int[]{10});
        keys.put("4", new int[]{11});
        keys.put("5", new int[]{12});
        keys.put("6", new int[]{13});
        keys.put("7", new int[]{14});
        keys.put("8", new int[]{15});
        keys.put("9", new int[]{16});
        keys.put(")", new int[]{7, KEY_SHIFT});
        keys.put("!", new int[]{8, KEY_SHIFT});
//        keys.put("@", new int[]{9});
        keys.put("#", new int[]{10, KEY_SHIFT});
        keys.put("$", new int[]{11, KEY_SHIFT});
        keys.put("%", new int[]{12, KEY_SHIFT});
        keys.put("^", new int[]{13, KEY_SHIFT});
        keys.put("&", new int[]{14, KEY_SHIFT});
        keys.put("*", new int[]{15, KEY_SHIFT});
        keys.put("(", new int[]{16, KEY_SHIFT});
        keys.put("STAR", new int[]{17});
        keys.put("POUND", new int[]{18});
        /** Directional Pad Up key. May also be synthesized from trackball motions. */
        keys.put("ArrowUp", new int[]{19});
        /** Directional Pad Down key. May also be synthesized from trackball motions. */
        keys.put("ArrowDown", new int[]{20});
        /** Directional Pad Left key. May also be synthesized from trackball motions. */
        keys.put("ArrowLeft", new int[]{21});
        /** Directional Pad Right key. May also be synthesized from trackball motions. */
        keys.put("ArrowRight", new int[]{22});
        /** Directional Pad Center key. May also be synthesized from trackball motions. */
        keys.put("DPAD_CENTER", new int[]{23});
        /** Volume Up key. Adjusts the speaker volume up. */
        keys.put("VOLUME_UP", new int[]{24});
        /** Volume Down key. Adjusts the speaker volume down. */
        keys.put("VOLUME_DOWN", new int[]{25});
        keys.put("POWER", new int[]{26});
        // Camera key. Used to launch a camera application or take pictures.
        keys.put("CAMERA", new int[]{27});
        keys.put("CLEAR", new int[]{28});
        keys.put("a", new int[]{29});
        keys.put("b", new int[]{30});
        keys.put("c", new int[]{31});
        keys.put("d", new int[]{32});
        keys.put("e", new int[]{33});
        keys.put("f", new int[]{34});
        keys.put("g", new int[]{35});
        keys.put("h", new int[]{36});
        keys.put("i", new int[]{37});
        keys.put("j", new int[]{38});
        keys.put("k", new int[]{39});
        keys.put("l", new int[]{40});
        keys.put("m", new int[]{41});
        keys.put("n", new int[]{42});
        keys.put("o", new int[]{43});
        keys.put("p", new int[]{44});
        keys.put("q", new int[]{45});
        keys.put("r", new int[]{46});
        keys.put("s", new int[]{47});
        keys.put("t", new int[]{48});
        keys.put("u", new int[]{49});
        keys.put("v", new int[]{50});
        keys.put("w", new int[]{51});
        keys.put("x", new int[]{52});
        keys.put("y", new int[]{53});
        keys.put("z", new int[]{54});
        keys.put("A", new int[]{29, KEY_SHIFT});
        keys.put("B", new int[]{30, KEY_SHIFT});
        keys.put("C", new int[]{31, KEY_SHIFT});
        keys.put("D", new int[]{32, KEY_SHIFT});
        keys.put("E", new int[]{33, KEY_SHIFT});
        keys.put("F", new int[]{34, KEY_SHIFT});
        keys.put("G", new int[]{35, KEY_SHIFT});
        keys.put("H", new int[]{36, KEY_SHIFT});
        keys.put("I", new int[]{37, KEY_SHIFT});
        keys.put("J", new int[]{38, KEY_SHIFT});
        keys.put("K", new int[]{39, KEY_SHIFT});
        keys.put("L", new int[]{40, KEY_SHIFT});
        keys.put("M", new int[]{41, KEY_SHIFT});
        keys.put("N", new int[]{42, KEY_SHIFT});
        keys.put("O", new int[]{43, KEY_SHIFT});
        keys.put("P", new int[]{44, KEY_SHIFT});
        keys.put("Q", new int[]{45, KEY_SHIFT});
        keys.put("R", new int[]{46, KEY_SHIFT});
        keys.put("S", new int[]{47, KEY_SHIFT});
        keys.put("T", new int[]{48, KEY_SHIFT});
        keys.put("U", new int[]{49, KEY_SHIFT});
        keys.put("V", new int[]{50, KEY_SHIFT});
        keys.put("W", new int[]{51, KEY_SHIFT});
        keys.put("X", new int[]{52, KEY_SHIFT});
        keys.put("Y", new int[]{53, KEY_SHIFT});
        keys.put("Z", new int[]{54, KEY_SHIFT});
        keys.put(",", new int[]{55});
        keys.put(".", new int[]{56});
        keys.put("ALT_LEFT", new int[]{57});
        keys.put("ALT_RIGHT", new int[]{58});
        keys.put("SHIFT_LEFT", new int[]{59});
        keys.put("SHIFT_RIGHT", new int[]{60});
        keys.put("Tab", new int[]{61});
        keys.put(" ", new int[]{62});
        /** Symbol modifier key. Used to enter alternate symbols. */
        keys.put("SYM", new int[]{63});
        /** Explorer special function key. Used to launch a browser application. */
        keys.put("EXPLORER", new int[]{64});
        /** Envelope special function key. Used to launch a mail application. */
        keys.put("ENVELOPE", new int[]{65});
        keys.put("Enter", new int[]{66});
        /** Backspace key. Deletes characters before the insertion point, unlike {@link AKEYCODE_FORWARD_DEL}. */
        keys.put("Backspace", new int[]{67});
        keys.put("`", new int[]{68});
        keys.put("-", new int[]{69});
        keys.put("=", new int[]{70});
        keys.put("[", new int[]{71});
        keys.put("]", new int[]{72});
        keys.put("\\", new int[]{73});
        keys.put(";", new int[]{74});
        keys.put("'", new int[]{75});
        keys.put("/", new int[]{76});
        keys.put("@", new int[]{77});
        keys.put("~", new int[]{68, KEY_SHIFT});
        keys.put("_", new int[]{69, KEY_SHIFT});
        keys.put("{", new int[]{71, KEY_SHIFT});
        keys.put("}", new int[]{72, KEY_SHIFT});
        keys.put("|", new int[]{73, KEY_SHIFT});
        keys.put(":", new int[]{74, KEY_SHIFT});
        keys.put("\"", new int[]{75, KEY_SHIFT});
        keys.put("?", new int[]{76, KEY_SHIFT});
        keys.put("<", new int[]{55, KEY_SHIFT});
        keys.put(">", new int[]{56, KEY_SHIFT});
        /** Number modifier key. Used to enter numeric symbols.
         * This key is not {@link AKEYCODE_NUM_LOCK}; it is more like {@link AKEYCODE_ALT_LEFT}. */
        keys.put("NUM", new int[]{78});
        /** Headset Hook key. Used to hang up calls and stop media. */
        keys.put("HEADSETHOOK", new int[]{79});
        /** Camera Focus key. Used to focus the camera. */
        keys.put("FOCUS", new int[]{80});
        keys.put("+", new int[]{81});
        keys.put("MENU", new int[]{82});
        keys.put("NOTIFICATION", new int[]{83});
        keys.put("SEARCH", new int[]{84});
        keys.put("MEDIA_PLAY_PAUSE", new int[]{85});
        keys.put("MEDIA_STOP", new int[]{86});
        keys.put("MEDIA_NEXT", new int[]{87});
        keys.put("MEDIA_PREVIOUS", new int[]{88});
        keys.put("MEDIA_REWIND", new int[]{89});
        keys.put("MEDIA_FAST_FORWARD", new int[]{90});
        /** Mute key. Mutes the microphone, unlike {@link AKEYCODE_VOLUME_MUTE}. */
        keys.put("MUTE", new int[]{91});
        keys.put("PAGE_UP", new int[]{92});
        keys.put("PAGE_DOWN", new int[]{93});
        /** Picture Symbols modifier key. Used to switch symbol sets (Emoji, Kao-moji). */
        keys.put("PICTSYMBOLS", new int[]{94});
        /** Switch Charset modifier key. Used to switch character sets (Kanji, Katakana). */
        keys.put("SWITCH_CHARSET", new int[]{95});
        /** A Button key. On a game controller, the A button should be either the button labeled A
         * or the first button on the bottom row of controller buttons. */
        keys.put("BUTTON_A", new int[]{96});
        /** B Button key. On a game controller, the B button should be either the button labeled B
         * or the second button on the bottom row of controller buttons. */
        keys.put("BUTTON_B", new int[]{97});
        /** C Button key. On a game controller, the C button should be either the button labeled C
         * or the third button on the bottom row of controller buttons. */
        keys.put("BUTTON_C", new int[]{98});
        /** X Button key. On a game controller, the X button should be either the button labeled X
         * or the first button on the upper row of controller buttons. */
        keys.put("BUTTON_X", new int[]{99});
        /** Y Button key. On a game controller, the Y button should be either the button labeled Y
         * or the second button on the upper row of controller buttons. */
        keys.put("BUTTON_Y", new int[]{100});
        /** Z Button key. On a game controller, the Z button should be either the button labeled Z
         * or the third button on the upper row of controller buttons. */
        keys.put("BUTTON_Z", new int[]{101});
        /** L1 Button key. On a game controller, the L1 button should be either the button labeled L1 (or L)
         * or the top left trigger button. */
        keys.put("BUTTON_L1", new int[]{102});
        /** R1 Button key. On a game controller, the R1 button should be either the button labeled R1 (or R)
         * or the top right trigger button. */
        keys.put("BUTTON_R1", new int[]{103});
        /** L2 Button key. On a game controller, the L2 button should be either the button labeled L2
         * or the bottom left trigger button. */
        keys.put("BUTTON_L2", new int[]{104});
        /** R2 Button key. On a game controller, the R2 button should be either the button labeled R2
         * or the bottom right trigger button. */
        keys.put("BUTTON_R2", new int[]{105});
        /** Left Thumb Button key. On a game controller, the left thumb button indicates that the left (or only)
         * joystick is pressed. */
        keys.put("BUTTON_THUMBL", new int[]{106});
        /** Right Thumb Button key. On a game controller, the right thumb button indicates that the right
         * joystick is pressed. */
        keys.put("BUTTON_THUMBR", new int[]{107});
        /** Start Button key. On a game controller, the button labeled Start. */
        keys.put("BUTTON_START", new int[]{108});
        /** Select Button key. On a game controller, the button labeled Select. */
        keys.put("BUTTON_SELECT", new int[]{109});
        /** Mode Button key. On a game controller, the button labeled Mode. */
        keys.put("BUTTON_MODE", new int[]{110});
        keys.put("Escape", new int[]{111});
        /** Forward Delete key. Deletes characters ahead of the insertion point, unlike {@link AKEYCODE_DEL}. */
        keys.put("FORWARD_DEL", new int[]{112});
        keys.put("CTRL_LEFT", new int[]{113});
        keys.put("CTRL_RIGHT", new int[]{114});
        keys.put("CapsLock", new int[]{115});
        keys.put("SCROLL_LOCK", new int[]{116});
        keys.put("META_LEFT", new int[]{117});
        keys.put("META_RIGHT", new int[]{118});
        keys.put("FUNCTION", new int[]{119});
        keys.put("SYSRQ", new int[]{120});
        keys.put("BREAK", new int[]{121});
        /** Home Movement key. Used for scrolling or moving the cursor around to the start of a line
         * or to the top of a list. */
        keys.put("MOVE_HOME", new int[]{122});
        /** End Movement key. Used for scrolling or moving the cursor around to the end of a line
         * or to the bottom of a list. */
        keys.put("MOVE_END", new int[]{123});
        /** Insert key. Toggles insert / overwrite edit mode. */
        keys.put("INSERT", new int[]{124});
        /** Forward key. Navigates forward in the history stack.  Complement of {@link AKEYCODE_BACK}. */
        keys.put("FORWARD", new int[]{125});
        keys.put("MEDIA_PLAY", new int[]{126});
        keys.put("MEDIA_PAUSE", new int[]{127});
        /** Close media key. May be used to close a CD tray, for example. */
        keys.put("MEDIA_CLOSE", new int[]{128});
        /** Eject media key. May be used to eject a CD tray, for example. */
        keys.put("MEDIA_EJECT", new int[]{129});
        keys.put("MEDIA_RECORD", new int[]{130});
        keys.put("F1", new int[]{131});
        keys.put("F2", new int[]{132});
        keys.put("F3", new int[]{133});
        keys.put("F4", new int[]{134});
        keys.put("F5", new int[]{135});
        keys.put("F6", new int[]{136});
        keys.put("F7", new int[]{137});
        keys.put("F8", new int[]{138});
        keys.put("F9", new int[]{139});
        keys.put("F10", new int[]{140});
        keys.put("F11", new int[]{141});
        keys.put("F12", new int[]{142});
        /** Num Lock key. This is the Num Lock key; it is different from {@link AKEYCODE_NUM}.
         * This key alters the behavior of other keys on the numeric keypad. */
        keys.put("NUM_LOCK", new int[]{143});
        keys.put("NUMPAD_0", new int[]{144});
        keys.put("NUMPAD_1", new int[]{145});
        keys.put("NUMPAD_2", new int[]{146});
        keys.put("NUMPAD_3", new int[]{147});
        keys.put("NUMPAD_4", new int[]{148});
        keys.put("NUMPAD_5", new int[]{149});
        keys.put("NUMPAD_6", new int[]{150});
        keys.put("NUMPAD_7", new int[]{151});
        keys.put("NUMPAD_8", new int[]{152});
        keys.put("NUMPAD_9", new int[]{153});
        keys.put("NUMPAD_DIVIDE", new int[]{154});
        keys.put("NUMPAD_MULTIPLY", new int[]{155});
        keys.put("NUMPAD_SUBTRACT", new int[]{156});
        keys.put("NUMPAD_ADD", new int[]{157});
        keys.put("NUMPAD_DOT", new int[]{158});
        keys.put("NUMPAD_COMMA", new int[]{159});
        keys.put("NUMPAD_ENTER", new int[]{160});
        keys.put("NUMPAD_EQUALS", new int[]{161});
        keys.put("NUMPAD_LEFT_PAREN", new int[]{162});
        keys.put("NUMPAD_RIGHT_PAREN", new int[]{163});
        /** Volume Mute key. Mutes the speaker, unlike {@link AKEYCODE_MUTE}.
         * This key should normally be implemented as a toggle such that the first press
         * mutes the speaker and the second press restores the original volume. */
        keys.put("VOLUME_MUTE", new int[]{164});
        /** Info key. Common on TV remotes to show additional information related to what is
         * currently being viewed. */
        keys.put("INFO", new int[]{165});
        /** Channel up key. On TV remotes, increments the television channel. */
        keys.put("CHANNEL_UP", new int[]{166});
        /** Channel down key. On TV remotes, decrements the television channel. */
        keys.put("CHANNEL_DOWN", new int[]{167});
        keys.put("ZOOM_IN", new int[]{168});
        keys.put("ZOOM_OUT", new int[]{169});
        /** TV key. On TV remotes, switches to viewing live TV. */
        keys.put("TV", new int[]{170});
        /** Window key. On TV remotes, toggles picture-in-picture mode or other windowing functions. */
        keys.put("WINDOW", new int[]{171});
        /** Guide key. On TV remotes, shows a programming guide. */
        keys.put("GUIDE", new int[]{172});
        /** DVR key. On some TV remotes, switches to a DVR mode for recorded shows. */
        keys.put("DVR", new int[]{173});
        /** Bookmark key. On some TV remotes, bookmarks content or web pages. */
        keys.put("BOOKMARK", new int[]{174});
        /** Toggle captions key. Switches the mode for closed-captioning text, for example during television shows. */
        keys.put("CAPTIONS", new int[]{175});
        /** Settings key. Starts the system settings activity. */
        keys.put("SETTINGS", new int[]{176});
        /** TV power key. On TV remotes, toggles the power on a television screen. */
        keys.put("TV_POWER", new int[]{177});
        /** TV input key. On TV remotes, switches the input on a television screen. */
        keys.put("TV_INPUT", new int[]{178});
        /** Set-top-box power key. On TV remotes, toggles the power on an external Set-top-box. */
        keys.put("STB_POWER", new int[]{179});
        /** Set-top-box input key. On TV remotes, switches the input mode on an external Set-top-box. */
        keys.put("STB_INPUT", new int[]{180});
        /** A/V Receiver power key. On TV remotes, toggles the power on an external A/V Receiver. */
        keys.put("AVR_POWER", new int[]{181});
        /** A/V Receiver input key. On TV remotes, switches the input mode on an external A/V Receiver. */
        keys.put("AVR_INPUT", new int[]{182});
        /** Red "programmable" key. On TV remotes, acts as a contextual/programmable key. */
        keys.put("PROG_RED", new int[]{183});
        /** Green "programmable" key. On TV remotes, actsas a contextual/programmable key. */
        keys.put("PROG_GREEN", new int[]{184});
        /** Yellow "programmable" key. On TV remotes, acts as a contextual/programmable key. */
        keys.put("PROG_YELLOW", new int[]{185});
        /** Blue "programmable" key. On TV remotes, acts as a contextual/programmable key. */
        keys.put("PROG_BLUE", new int[]{186});
        /** App switch key. Should bring up the application switcher dialog. */
        keys.put("APP_SWITCH", new int[]{187});
        keys.put("BUTTON_1", new int[]{188});
        keys.put("BUTTON_2", new int[]{189});
        keys.put("BUTTON_3", new int[]{190});
        keys.put("BUTTON_4", new int[]{191});
        keys.put("BUTTON_5", new int[]{192});
        keys.put("BUTTON_6", new int[]{193});
        keys.put("BUTTON_7", new int[]{194});
        keys.put("BUTTON_8", new int[]{195});
        keys.put("BUTTON_9", new int[]{196});
        keys.put("BUTTON_10", new int[]{197});
        keys.put("BUTTON_11", new int[]{198});
        keys.put("BUTTON_12", new int[]{199});
        keys.put("BUTTON_13", new int[]{200});
        keys.put("BUTTON_14", new int[]{201});
        keys.put("BUTTON_15", new int[]{202});
        keys.put("BUTTON_16", new int[]{203});
        /** Language Switch key. Toggles the current input language such as switching between English and Japanese on
         * a QWERTY keyboard.  On some devices, the same function may be performed by
         * pressing Shift+Spacebar. */
        keys.put("LANGUAGE_SWITCH", new int[]{204});
        /** Manner Mode key. Toggles silent or vibrate mode on and off to make the device behave more politely
         * in certain settings such as on a crowded train.  On some devices, the key may only
         * operate when long-pressed. */
        keys.put("MANNER_MODE", new int[]{205});
        /** 3D Mode key. Toggles the display between 2D and 3D mode. */
        keys.put("3D_MODE", new int[]{206});
        /** Contacts special function key. Used to launch an address book application. */
        keys.put("CONTACTS", new int[]{207});
        /** Calendar special function key. Used to launch a calendar application. */
        keys.put("CALENDAR", new int[]{208});
        /** Music special function key. Used to launch a music player application. */
        keys.put("MUSIC", new int[]{209});
        /** Calculator special function key. Used to launch a calculator application. */
        keys.put("CALCULATOR", new int[]{210});
        keys.put("ZENKAKU_HANKAKU", new int[]{211});
        keys.put("EISU", new int[]{212});
        keys.put("MUHENKAN", new int[]{213});
        keys.put("HENKAN", new int[]{214});
        keys.put("KATAKANA_HIRAGANA", new int[]{215});
        keys.put("YEN", new int[]{216});
        keys.put("RO", new int[]{217});
        keys.put("KANA", new int[]{218});
        /** Assist key. Launches the global assist activity.  Not delivered to applications. */
        keys.put("ASSIST", new int[]{219});
        /** Brightness Down key. Adjusts the screen brightness down. */
        keys.put("BRIGHTNESS_DOWN", new int[]{220});
        /** Brightness Up key. Adjusts the screen brightness up. */
        keys.put("BRIGHTNESS_UP", new int[]{221});
        /** Audio Track key. Switches the audio tracks. */
        keys.put("MEDIA_AUDIO_TRACK", new int[]{222});
        /** Sleep key. Puts the device to sleep.  Behaves somewhat like {@link AKEYCODE_POWER} but it
         * has no effect if the device is already asleep. */
        keys.put("SLEEP", new int[]{223});
        /** Wakeup key. Wakes up the device.  Behaves somewhat like {@link AKEYCODE_POWER} but it
         * has no effect if the device is already awake. */
        keys.put("WAKEUP", new int[]{224});
        /** Pairing key. Initiates peripheral pairing mode. Useful for pairing remote control
         * devices or game controllers, especially if no other input mode is
         * available. */
        keys.put("PAIRING", new int[]{225});
        /** Media Top Menu key. Goes to the top of media menu. */
        keys.put("MEDIA_TOP_MENU", new int[]{226});
        keys.put("11", new int[]{227});
        keys.put("12", new int[]{228});
        /** Last Channel key. Goes to the last viewed channel. */
        keys.put("LAST_CHANNEL", new int[]{229});
        /** TV data service key. Displays data services like weather, sports. */
        keys.put("TV_DATA_SERVICE", new int[]{230});
        /** Voice Assist key. Launches the global voice assist activity. Not delivered to applications. */
        keys.put("VOICE_ASSIST", new int[]{231});
        /** Radio key. Toggles TV service / Radio service. */
        keys.put("TV_RADIO_SERVICE", new int[]{232});
        /** Teletext key. Displays Teletext service. */
        keys.put("TV_TELETEXT", new int[]{233});
        /** Number entry key. Initiates to enter multi-digit channel nubmber when each digit key is assigned
         * for selecting separate channel. Corresponds to Number Entry Mode (0x1D) of CEC
         * User Control Code. */
        keys.put("TV_NUMBER_ENTRY", new int[]{234});
        /** Analog Terrestrial key. Switches to analog terrestrial broadcast service. */
        keys.put("TV_TERRESTRIAL_ANALOG", new int[]{235});
        /** Digital Terrestrial key. Switches to digital terrestrial broadcast service. */
        keys.put("TV_TERRESTRIAL_DIGITAL", new int[]{236});
        /** Satellite key. Switches to digital satellite broadcast service. */
        keys.put("TV_SATELLITE", new int[]{237});
        /** BS key. Switches to BS digital satellite broadcasting service available in Japan. */
        keys.put("TV_SATELLITE_BS", new int[]{238});
        /** CS key. Switches to CS digital satellite broadcasting service available in Japan. */
        keys.put("TV_SATELLITE_CS", new int[]{239});
        /** BS/CS key. Toggles between BS and CS digital satellite services. */
        keys.put("TV_SATELLITE_SERVICE", new int[]{240});
        /** Toggle Network key. Toggles selecting broacast services. */
        keys.put("TV_NETWORK", new int[]{241});
        /** Antenna/Cable key. Toggles broadcast input source between antenna and cable. */
        keys.put("TV_ANTENNA_CABLE", new int[]{242});
        /** HDMI #1 key. Switches to HDMI input #1. */
        keys.put("TV_INPUT_HDMI_1", new int[]{243});
        /** HDMI #2 key. Switches to HDMI input #2. */
        keys.put("TV_INPUT_HDMI_2", new int[]{244});
        /** HDMI #3 key. Switches to HDMI input #3. */
        keys.put("TV_INPUT_HDMI_3", new int[]{245});
        /** HDMI #4 key. Switches to HDMI input #4. */
        keys.put("TV_INPUT_HDMI_4", new int[]{246});
        /** Composite #1 key. Switches to composite video input #1. */
        keys.put("TV_INPUT_COMPOSITE_1", new int[]{247});
        /** Composite #2 key. Switches to composite video input #2. */
        keys.put("TV_INPUT_COMPOSITE_2", new int[]{248});
        /** Component #1 key. Switches to component video input #1. */
        keys.put("TV_INPUT_COMPONENT_1", new int[]{249});
        /** Component #2 key. Switches to component video input #2. */
        keys.put("TV_INPUT_COMPONENT_2", new int[]{250});
        /** VGA #1 key. Switches to VGA (analog RGB) input #1. */
        keys.put("TV_INPUT_VGA_1", new int[]{251});
        /** Audio description key. Toggles audio description off / on. */
        keys.put("TV_AUDIO_DESCRIPTION", new int[]{252});
        /** Audio description mixing volume up key. Louden audio description volume as compared with normal audio volume. */
        keys.put("TV_AUDIO_DESCRIPTION_MIX_UP", new int[]{253});
        /** Audio description mixing volume down key. Lessen audio description volume as compared with normal audio volume. */
        keys.put("TV_AUDIO_DESCRIPTION_MIX_DOWN", new int[]{254});
        /** Zoom mode key. Changes Zoom mode (Normal, Full, Zoom, Wide-zoom, etc.) */
        keys.put("TV_ZOOM_MODE", new int[]{255});
        /** Contents menu key. Goes to the title list. Corresponds to Contents Menu (0x0B) of CEC User Control
         * Code */
        keys.put("TV_CONTENTS_MENU", new int[]{256});
        /** Media context menu key. Goes to the context menu of media contents. Corresponds to Media Context-sensitive
         * Menu (0x11) of CEC User Control Code. */
        keys.put("TV_MEDIA_CONTEXT_MENU", new int[]{257});
        /** Timer programming key. Goes to the timer recording menu. Corresponds to Timer Programming (0x54) of
         * CEC User Control Code. */
        keys.put("TV_TIMER_PROGRAMMING", new int[]{258});
        keys.put("HELP", new int[]{259});
        keys.put("NAVIGATE_PREVIOUS", new int[]{260});
        keys.put("NAVIGATE_NEXT", new int[]{261});
        keys.put("NAVIGATE_IN", new int[]{262});
        keys.put("NAVIGATE_OUT", new int[]{263});
        /** Primary stem key for Wear Main power/reset button on watch. */
        keys.put("STEM_PRIMARY", new int[]{264});
        keys.put("STEM_1", new int[]{265});
        keys.put("STEM_2", new int[]{266});
        keys.put("STEM_3", new int[]{267});
        keys.put("DPAD_UP_LEFT", new int[]{268});
        keys.put("DPAD_DOWN_LEFT", new int[]{269});
        keys.put("DPAD_UP_RIGHT", new int[]{270});
        keys.put("DPAD_DOWN_RIGHT", new int[]{271});
        keys.put("MEDIA_SKIP_FORWARD", new int[]{272});
        keys.put("MEDIA_SKIP_BACKWARD", new int[]{273});
        /** Step forward media key. Steps media forward one from at a time. */
        keys.put("MEDIA_STEP_FORWARD", new int[]{274});
        /** Step backward media key. Steps media backward one from at a time. */
        keys.put("MEDIA_STEP_BACKWARD", new int[]{275});
        keys.put("SOFT_SLEEP", new int[]{276});
        keys.put("CUT", new int[]{277});
        keys.put("COPY", new int[]{278});
        keys.put("PASTE", new int[]{279});
        keys.put("SYSTEM_NAVIGATION_UP", new int[]{280});
        keys.put("SYSTEM_NAVIGATION_DOWN", new int[]{281});
        keys.put("SYSTEM_NAVIGATION_LEFT", new int[]{282});
        keys.put("SYSTEM_NAVIGATION_RIGHT", new int[]{283});
        keys.put("ALL_APPS", new int[]{28});
    }
}
