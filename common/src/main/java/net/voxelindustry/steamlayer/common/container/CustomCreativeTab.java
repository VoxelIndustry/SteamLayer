package net.voxelindustry.steamlayer.common.container;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class CustomCreativeTab extends ItemGroup
{
    private Supplier<ItemStack> iconSupplier;

    public CustomCreativeTab(String label, Supplier<ItemStack> iconSupplier)
    {
        super(label);

        this.iconSupplier = iconSupplier;
    }

    @Override
    @Nonnull
    public ItemStack createIcon()
    {
        return iconSupplier.get();
    }
}
