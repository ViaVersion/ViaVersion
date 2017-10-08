package us.myles.ViaVersion.protocols.protocol1_12to1_11_1.storage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString
@Getter
public class ItemTransaction {
    private short windowId;
    private short slotId;
    private short actionId;
}