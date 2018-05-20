package us.myles.ViaVersion.protocols.protocol1_11to1_10.chat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ChatMsgCharacterLimitAction {
    SPLIT(false),
    TRUNCATE(false),
    PASS_THROUGH(false),
    CANCEL(true);

    private final boolean notify;
    public static final int CHARACTER_LIMIT = 100;
    private static final ChatMsgCharacterLimitAction[] v = values();

    public static ChatMsgCharacterLimitAction getChatMsgCharacterLimitAction(String actionType) {
        actionType = actionType.trim();
        // store the array to a variable so it doesn't allocate a new array
        // every time this method is called as this method might be called quite frequently.
        for (ChatMsgCharacterLimitAction action : v) {
            String name = action.name();
            if (actionType.equalsIgnoreCase(name)) {
                return action;
            }
        }
        return TRUNCATE;
    }

    public static String[] handleChatMessage(String msg, ChatMsgCharacterLimitAction action) {
        // TODO: MAKE
        switch (action) {
            case SPLIT:
                return null;
            case TRUNCATE:
                return null;
            case PASS_THROUGH:
                return null;
            case CANCEL:
                return null;
            default:
                return handleChatMessage(msg, TRUNCATE);
        }
    }
}