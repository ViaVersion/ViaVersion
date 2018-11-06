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

    public void storeBlock(UserConnection connection, Position position, int blockState){};

    public void removeBlock(UserConnection connection, Position position){};

    public void storeBlock(UserConnection connection, long x, long y, long z, int blockState){
        storeBlock(connection, new Position(x, y, z), blockState);
    }

    public boolean needBlockStore(){
        return false;
    }
}
