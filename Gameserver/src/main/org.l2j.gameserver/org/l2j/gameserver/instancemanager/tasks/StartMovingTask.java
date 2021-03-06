package org.l2j.gameserver.instancemanager.tasks;

import org.l2j.gameserver.instancemanager.WalkingManager;
import org.l2j.gameserver.model.actor.Npc;

/**
 * Task which starts npc movement.
 *
 * @author xban1x
 */
public final class StartMovingTask implements Runnable {
    final Npc _npc;
    final String _routeName;

    public StartMovingTask(Npc npc, String routeName) {
        _npc = npc;
        _routeName = routeName;
    }

    @Override
    public void run() {
        if (_npc != null) {
            WalkingManager.getInstance().startMoving(_npc, _routeName);
        }
    }
}
