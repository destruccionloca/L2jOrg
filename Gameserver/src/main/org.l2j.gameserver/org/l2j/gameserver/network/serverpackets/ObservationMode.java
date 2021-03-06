package org.l2j.gameserver.network.serverpackets;

import org.l2j.gameserver.model.Location;
import org.l2j.gameserver.network.GameClient;
import org.l2j.gameserver.network.ServerPacketId;

public class ObservationMode extends ServerPacket {
    private final Location _loc;

    public ObservationMode(Location loc) {
        _loc = loc;
    }

    @Override
    public void writeImpl(GameClient client) {
        writeId(ServerPacketId.OBSERVER_START);

        writeInt(_loc.getX());
        writeInt(_loc.getY());
        writeInt(_loc.getZ());
        writeInt(0x00); // TODO: Find me
        writeInt(0xc0); // TODO: Find me
    }

}
