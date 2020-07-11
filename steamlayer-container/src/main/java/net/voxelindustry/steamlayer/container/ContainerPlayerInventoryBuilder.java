package net.voxelindustry.steamlayer.container;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.voxelindustry.steamlayer.container.slot.FilteredSlot;
import net.voxelindustry.steamlayer.container.slot.ListenerSlot;
import org.apache.commons.lang3.Range;

public class ContainerPlayerInventoryBuilder
{
    private final PlayerEntity     player;
    private final PlayerInventory  inventory;
    private final ContainerBuilder parent;
    private       Range<Integer>   main;
    private       Range<Integer>   hotbar;
    private       Range<Integer>   armor;

    ContainerPlayerInventoryBuilder(ContainerBuilder parent, PlayerEntity player, PlayerInventory inventory)
    {
        this.inventory = inventory;
        this.player = player;
        this.parent = parent;
    }

    /**
     * Utility method to add the entire main inventory of a inventory to the slot list.
     * Note that this does not include the hotbar nor the armor slots.
     *
     * @param xStart the horizontal position at which the inventory begins
     * @param yStart the vertical position at which the inventory begins
     * @return a reference to this {@code ContainerPlayerInventoryBuilder} to resume the "Builder" pattern
     */
    public ContainerPlayerInventoryBuilder inventory(int xStart, int yStart)
    {
        int startIndex = parent.slots.size();
        for (int i = 0; i < 3; ++i)
            for (int j = 0; j < 9; ++j)
                parent.slots.add(new ListenerSlot(inventory, j + i * 9 + 9, xStart + j * 18, yStart + i *
                        18));
        main = Range.between(startIndex, parent.slots.size() - 1);
        return this;
    }

    /**
     * Utility method to add the entire hotbar of a inventory to the slot list.
     *
     * @param xStart the horizontal position at which the inventory begins
     * @param yStart the vertical position at which the inventory begins
     * @return a reference to this {@code ContainerPlayerInventoryBuilder} to resume the "Builder" pattern
     */
    public ContainerPlayerInventoryBuilder hotbar(int xStart, int yStart)
    {
        int startIndex = parent.slots.size();
        for (int i = 0; i < 9; ++i)
            parent.slots.add(new ListenerSlot(inventory, i, xStart + i * 18, yStart));
        hotbar = Range.between(startIndex, parent.slots.size() - 1);
        return this;
    }

    /**
     * Utility method to add the entire main inventory of a inventory to the slot list.
     * Note that this does not include the hotbar nor the armor slots.
     * <p>
     * This method will use commonly used default values to position the slots.
     *
     * @return a reference to this {@code ContainerPlayerInventoryBuilder} to resume the "Builder" pattern
     */
    public ContainerPlayerInventoryBuilder inventory()
    {
        return inventory(8, 94);
    }

    /**
     * Utility method to add the entire hotbar of a inventory to the slot list.
     * <p>
     * This method will use commonly used default values to position the slots.
     *
     * @return a reference to this {@code ContainerPlayerInventoryBuilder} to resume the "Builder" pattern
     */
    public ContainerPlayerInventoryBuilder hotbar()
    {
        return hotbar(8, 152);
    }

    /**
     * Begin the construction of a {@link ContainerPlayerArmorInventoryBuilder} builder.
     * Only one should be used per {@code ContainerPlayerInventoryBuilder}
     *
     * @return a {@link ContainerPlayerArmorInventoryBuilder} marked as child of this builder
     */
    public ContainerPlayerArmorInventoryBuilder armor()
    {
        return new ContainerPlayerArmorInventoryBuilder(this);
    }

    /**
     * Close this builder and add the slot list to the current {@link BuiltContainer} construction.
     * <p>
     * A special case has been implemented with armor slots, they are considered as a inventory slot range. Allowing
     * shift-insert from the main inventory of the player.
     * <p>
     * Begin the construction of a {@link ContainerTileInventoryBuilder} builder.
     * <p>
     * Multiple tiles can be linked to a same container with recall of
     * this method after completing the previous nested builder.
     *
     * @param inventory an Inventory to use as main inventory
     * @return the inventory builder {@link ContainerTileInventoryBuilder} to resume the "Builder" pattern
     */
    public ContainerTileInventoryBuilder tile(Inventory inventory)
    {
        noTile();
        return new ContainerTileInventoryBuilder(parent, inventory);
    }

    /**
     * Close this builder and add the slot list to the current {@link BuiltContainer} construction.
     * <p>
     * A special case has been implemented with armor slots, they are considered as a inventory slot range. Allowing
     * shift-insert from the main inventory of the player.
     * <p>
     * Begin the construction of a {@link ContainerTileInventoryBuilder} builder.
     * <p>
     * Multiple tiles can be linked to a same container with recall of
     * this method after completing the previous nested builder.
     *
     * @param tile      the main BlockEntity passed to the BuiltContainer
     * @param inventory an Inventory to use as main inventory
     * @return the inventory builder {@link ContainerTileInventoryBuilder} to resume the "Builder" pattern
     */
    public ContainerTileInventoryBuilder tile(BlockEntity tile, Inventory inventory)
    {
        noTile();
        parent.mainTile = tile;
        return new ContainerTileInventoryBuilder(parent, inventory);
    }

    /**
     * Close this builder and add the slot list to the current {@link BuiltContainer} construction.
     * Allow to set a BlockEntity to use in the BuiltContainer
     * <p>
     * A special case has been implemented with armor slots, they are considered as a tile slot range. Allowing
     * shift-insert from the main inventory of the player.
     *
     * @return the parent builder {@link ContainerBuilder} to resume the "Builder" pattern
     */
    public ContainerBuilder emptyTile(BlockEntity tile)
    {
        parent.mainTile = tile;
        return noTile();
    }

    /**
     * Close this builder and add the slot list to the current {@link BuiltContainer} construction.
     * <p>
     * A special case has been implemented with armor slots, they are considered as a tile slot range. Allowing
     * shift-insert from the main inventory of the player.
     *
     * @return the parent builder {@link ContainerBuilder} to resume the "Builder" pattern
     */
    public ContainerBuilder noTile()
    {
        if (hotbar != null)
            parent.addPlayerInventoryRange(hotbar);
        if (main != null)
            parent.addPlayerInventoryRange(main);
        if (armor != null)
            parent.addTileInventoryRange(armor);
        return parent;
    }

    /**
     * Close this builder and add the slot list to the current {@link BuiltContainer} construction.
     *
     * @return the parent {@link ContainerBuilder} to resume the "Builder" pattern
     */
    public ContainerSyncBuilder sync()
    {
        noTile();
        return new ContainerSyncBuilder(parent);
    }

    public static class ContainerPlayerArmorInventoryBuilder
    {
        private final ContainerPlayerInventoryBuilder parent;
        private final int                             startIndex;

        public ContainerPlayerArmorInventoryBuilder(ContainerPlayerInventoryBuilder parent)
        {
            this.parent = parent;
            startIndex = parent.parent.slots.size();
        }

        private ContainerPlayerArmorInventoryBuilder armor(int index, int xStart, int yStart,
                                                           EquipmentSlot slotType)
        {
            parent.parent.slots.add(new FilteredSlot(parent.inventory, index, xStart, yStart)
                    .setFilter(stack -> MobEntity.getPreferredEquipmentSlot(stack) == slotType));
            return this;
        }

        public ContainerPlayerArmorInventoryBuilder helmet(int xStart, int yStart)
        {
            return armor(parent.inventory.size() - 2, xStart, yStart, EquipmentSlot.HEAD);
        }

        public ContainerPlayerArmorInventoryBuilder chestplate(int xStart, int yStart)
        {
            return armor(parent.inventory.size() - 3, xStart, yStart, EquipmentSlot.CHEST);
        }

        public ContainerPlayerArmorInventoryBuilder leggings(int xStart, int yStart)
        {
            return armor(parent.inventory.size() - 4, xStart, yStart, EquipmentSlot.LEGS);
        }

        public ContainerPlayerArmorInventoryBuilder boots(int xStart, int yStart)
        {
            return armor(parent.inventory.size() - 5, xStart, yStart, EquipmentSlot.FEET);
        }

        public ContainerPlayerArmorInventoryBuilder complete(int xStart, int yStart)
        {
            return helmet(xStart, yStart).chestplate(xStart, yStart + 18).leggings(xStart, yStart + 18 + 18)
                    .boots(xStart, yStart + 18 + 18 + 18);
        }

        public ContainerPlayerInventoryBuilder addArmor()
        {
            parent.armor = Range.between(startIndex, parent.parent.slots.size() - 1);
            return parent;
        }
    }
}