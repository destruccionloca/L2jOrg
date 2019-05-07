package org.l2j.gameserver.network.serverpackets.vip;

import org.l2j.gameserver.network.L2GameClient;
import org.l2j.gameserver.network.OutgoingPackets;
import org.l2j.gameserver.network.serverpackets.IClientOutgoingPacket;

import java.nio.ByteBuffer;

public class ReceiveVipLuckyGameInfo extends IClientOutgoingPacket {

    @Override
    protected void writeImpl(L2GameClient client, ByteBuffer packet) {
        OutgoingPackets.RECEIVE_VIP_LUCKY_GAME_INFO.writeId(packet);
        packet.put((byte) 1); //Enable 1
        packet.putInt((int) client.getActiveChar().getAdena());
        packet.putInt(client.getCoin());
    }

    @Override
    protected int size(L2GameClient client) {
        return 14;
    }
}
