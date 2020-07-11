package net.voxelindustry.steamlayer.tile;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

public interface ITileInfoList
{
    void addText(String text);

    void addProgress(int current, int max);

    void addItem(ItemStack stack);

    void addEntity(Entity e);
}
