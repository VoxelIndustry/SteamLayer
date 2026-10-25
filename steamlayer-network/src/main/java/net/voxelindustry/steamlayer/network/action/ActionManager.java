package net.voxelindustry.steamlayer.network.action;

import java.util.HashMap;
import net.minecraft.nbt.CompoundTag;

public class ActionManager
{
    private static ActionManager INSTANCE;

    public static ActionManager getInstance()
    {
        if (INSTANCE == null)
            INSTANCE = new ActionManager();
        return INSTANCE;
    }

    private HashMap<Integer, IActionCallback> callbackMap;

    private ActionManager()
    {
        callbackMap = new HashMap<>();
    }

    void addCallback(Integer actionID, IActionCallback callback)
    {
        callbackMap.put(actionID, callback);
    }

    public void triggerCallback(int actionID, CompoundTag payload)
    {
        if (!callbackMap.containsKey(actionID))
            return;
        callbackMap.get(actionID).call(payload);
        callbackMap.remove(actionID);
    }
}
