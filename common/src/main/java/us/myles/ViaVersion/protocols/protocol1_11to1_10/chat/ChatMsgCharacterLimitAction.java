package us.myles.ViaVersion.protocols.protocol1_11to1_10.chat;

public enum ChatMsgCharacterLimitAction {
    SPLIT,
    TRUNCATE,
    PASS_THROUGH,
    CANCEL;

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
        switch (action) {
            case SPLIT:
                if (msg.startsWith("/")) { // this will not work with commands
                    return handleChatMessage(msg, TRUNCATE);
                }
                int length = msg.length();
                int alength = length / CHARACTER_LIMIT;
                if (length % CHARACTER_LIMIT != 0) {
                    alength++;
                }
                String[] lines = new String[alength];
                int index = 0;
                for (int i = 0; i < length; i += CHARACTER_LIMIT) {
                    lines[index++] = msg.substring(i, Math.min(length, i + CHARACTER_LIMIT));
                }
                return lines;
            case TRUNCATE:
                if (msg.length() > CHARACTER_LIMIT) {
                    msg = msg.substring(0, CHARACTER_LIMIT);
                }
                return new String[] {msg};
            case PASS_THROUGH:
                return new String[] {msg};
            case CANCEL:
                return null;
            default:
                return handleChatMessage(msg, TRUNCATE);
        }
    }
}