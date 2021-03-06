package org.l2j.gameserver.network.serverpackets;

import org.l2j.gameserver.model.Location;
import org.l2j.gameserver.model.actor.instance.Player;
import org.l2j.gameserver.network.GameClient;
import org.l2j.gameserver.network.ServerPacketId;

public class ValidateLocationInVehicle extends ServerPacket {
    private final int _charObjId;
    private final int _boatObjId;
    private final int _heading;
    private final Location _pos;

    public ValidateLocationInVehicle(Player player) {
        _charObjId = player.getObjectId();
        _boatObjId = player.getBoat().getObjectId();
        _heading = player.getHeading();
        _pos = player.getInVehiclePosition();
    }

    @Override
    public void writeImpl(GameClient client) {
        writeId(ServerPacketId.VALIDATE_LOCATION_IN_VEHICLE);

        writeInt(_charObjId);
        writeInt(_boatObjId);
        writeInt(_pos.getX());
        writeInt(_pos.getY());
        writeInt(_pos.getZ());
        writeInt(_heading);
    }

}
