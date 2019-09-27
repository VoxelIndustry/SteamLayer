package net.voxelindustry.steamlayer.multiblock;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class MultiblockSide
{
    private final BlockPos   pos;
    private final Direction facing;
}
