package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections.providers;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.Position;
import us.myles.ViaVersion.api.platform.providers.Provider;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data.MappingData;

/**
 * Created by Marco Neuhaus on 26.10.2018 for the Project ViaVersionGerry.
 */
public class BlockConnectionProvider implements Provider {

    public int getBlockdata(UserConnection connection, Position position){
        int oldId = getWorldBlockData(connection, position);
        return MappingData.blockMappings.getNewBlock(oldId);
    }

    public int getWorldBlockData(UserConnection connection, Position position){
        return -1;
    }
}
