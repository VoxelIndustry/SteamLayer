package net.voxelindustry.steamlayer.tile.modular;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public interface ISerializableModule
{
    void load(ValueInput input);

    void save(ValueOutput output);
}
