package net.voxelindustry.steamlayer.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;
import net.voxelindustry.steamlayer.container.slot.ListenerSlot;
import net.voxelindustry.steamlayer.container.sync.DefaultSyncables;
import net.voxelindustry.steamlayer.container.sync.SyncableProperty;
import org.apache.commons.lang3.Range;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Use this builder to construct a {@link BuiltContainer} instance.
 * <p>
 * While a TileInventory is not required it is mandatory to supply a PlayerInventory.
 * By default the produced Container will always return true on interact query,
 * however the vanilla behavior is to check the Tile distance. A custom interact predicate
 * should be supplied on construction.
 * <p>
 * The builder defer most of its logic to {@link ContainerPlayerInventoryBuilder} and
 * {@link ContainerTileInventoryBuilder}
 * They should be consulted for further documentation.
 */
public class ContainerBuilder
{
    private final String name;

    private final EntityPlayer            player;
    private       Predicate<EntityPlayer> canInteract = player -> true;

    final List<ListenerSlot>   slots;
    final List<Range<Integer>> playerInventoryRanges, tileInventoryRanges;

    List<SyncableProperty<?>> syncables;

    final List<Consumer<InventoryCrafting>> craftEvents;

    List<ItemStackHandler> inventories;

    /**
     * Creates a ContainerBuilder instance to produce a BuiltContainer
     * that can be used as a generic Container in Minecraft inventory system.
     * <p>
     * A new instance of ContainerBuilder must be created on each Container openings.
     * This restriction is subject to change.
     * <p>
     * This builder contains several sub builders to configure specific aspects of the Container logic.
     *
     * @param name   an unique name to be used as an identifier of the produced Container.
     * @param player the player instance to which the Container is to be attached.
     */
    public ContainerBuilder(String name, EntityPlayer player)
    {
        this.name = name;

        this.player = player;

        this.slots = new ArrayList<>();
        this.playerInventoryRanges = new ArrayList<>();
        this.tileInventoryRanges = new ArrayList<>();

        this.syncables = new ArrayList<>();

        this.craftEvents = new ArrayList<>();

        this.inventories = new ArrayList<>();
    }

    /**
     * Use this method to configure a custom interact predicate.
     * The vanilla behavior is to check the distance between the opened tile and the player.
     *
     * @param canInteract predicate consuming an {@link EntityPlayer} instance provided by the {@link BuiltContainer}
     * @return a reference to this {@code ContainerBuilder} to resume the "Builder" pattern
     */
    public ContainerBuilder interact(Predicate<EntityPlayer> canInteract)
    {
        this.canInteract = canInteract;
        return this;
    }

    /**
     * Begin the construction of a {@link ContainerPlayerInventoryBuilder} builder.
     *
     * @param player the inventory of the player to base this container on.
     * @return a {@link ContainerPlayerInventoryBuilder} marked as child of this builder
     */
    public ContainerPlayerInventoryBuilder player(EntityPlayer player)
    {
        return new ContainerPlayerInventoryBuilder(this, player, new PlayerInvWrapper(player.inventory));
    }

    /**
     * Begin the construction of a {@link ContainerTileInventoryBuilder} builder.
     * <p>
     * Multiple tiles can be linked to a same container with recall of
     * this method after completing the previous nested builder.
     *
     * @param tile an IInventory representing a tile inventory
     * @return a {@link ContainerTileInventoryBuilder} marked as child of this builder
     */
    public ContainerTileInventoryBuilder tile(ItemStackHandler tile)
    {
        return new ContainerTileInventoryBuilder(this, tile);
    }

    /**
     * Sync a Boolean value between the server and the client
     *
     * @param supplier a supplier giving the value from the server
     * @param setter   a consumer used to set the value of the client
     * @return a reference to this {@code ContainerTileInventoryBuilder} to resume the "Builder" pattern
     */
    public ContainerBuilder syncBooleanValue(Supplier<Boolean> supplier, Consumer<Boolean> setter)
    {
        this.syncables.add(new DefaultSyncables.SyncableBoolean(supplier, setter));
        return this;
    }

    /**
     * Sync an Integer value between the server and the client
     *
     * @param supplier a supplier giving the value from the server
     * @param setter   a consumer used to set the value of the client
     * @return a reference to this {@code ContainerTileInventoryBuilder} to resume the "Builder" pattern
     */
    public ContainerBuilder syncIntegerValue(Supplier<Integer> supplier, Consumer<Integer> setter)
    {
        this.syncables.add(new DefaultSyncables.SyncableInteger(supplier, setter));
        return this;
    }

    /**
     * Sync a Float value between the server and the client
     *
     * @param supplier a supplier giving the value from the server
     * @param setter   a consumer used to set the value of the client
     * @return a reference to this {@code ContainerTileInventoryBuilder} to resume the "Builder" pattern
     */
    public ContainerBuilder syncFloatValue(Supplier<Float> supplier, Consumer<Float> setter)
    {
        this.syncables.add(new DefaultSyncables.SyncableFloat(supplier, setter));
        return this;
    }

    /**
     * Sync a String value between the server and the client
     *
     * @param supplier a supplier giving the value from the server
     * @param setter   a consumer used to set the value of the client
     * @return a reference to this {@code ContainerTileInventoryBuilder} to resume the "Builder" pattern
     */
    public ContainerBuilder syncStringValue(Supplier<String> supplier, Consumer<String> setter)
    {
        this.syncables.add(new DefaultSyncables.SyncableString(supplier, setter));
        return this;
    }

    /**
     * Sync a {@link FluidStack} value between the server and the client
     *
     * @param supplier a supplier giving the value from the server
     * @param setter   a consumer used to set the value of the client
     * @return a reference to this {@code ContainerTileInventoryBuilder} to resume the "Builder" pattern
     */
    public ContainerBuilder syncFluidValue(Supplier<FluidStack> supplier,
                                           Consumer<FluidStack> setter)
    {
        this.syncables.add(new DefaultSyncables.SyncableFluid(supplier, setter));
        return this;
    }

    /**
     * Sync an {@link ItemStack} value between the server and the client
     *
     * @param supplier a supplier giving the value from the server
     * @param setter   a consumer used to set the value of the client
     * @return a reference to this {@code ContainerTileInventoryBuilder} to resume the "Builder" pattern
     */
    public ContainerBuilder syncItemValue(Supplier<ItemStack> supplier, Consumer<ItemStack> setter)
    {
        this.syncables.add(new DefaultSyncables.SyncableItem(supplier, setter));
        return this;
    }

    /**
     * Sync an {@link IItemHandler} value between the server and the client
     *
     * @param supplier a supplier giving the value from the server
     * @param setter   a consumer used to set the value of the client
     * @return a reference to this {@code ContainerTileInventoryBuilder} to resume the "Builder" pattern
     */
    public ContainerBuilder syncInventory(Supplier<IItemHandler> supplier, Consumer<IItemHandler> setter)
    {
        this.syncables.add(new DefaultSyncables.SyncableInventory(supplier, setter));
        return this;
    }

    /**
     * Sync an {@link IItemHandler} value between the server and the client
     *
     * @param supplier a supplier giving the value from the server
     * @param setter   a consumer used to set the value of the client
     * @param syncRate the rate in ticks used to query and refresh the syncable
     * @return a reference to this {@code ContainerTileInventoryBuilder} to resume the "Builder" pattern
     */
    public ContainerBuilder syncInventory(Supplier<IItemHandler> supplier, Consumer<IItemHandler> setter, int syncRate)
    {
        this.syncables.add(new DefaultSyncables.SyncableInventory(supplier, setter, syncRate));
        return this;
    }

    /**
     * Sync a generic syncable value between the server and the client
     *
     * @param syncable a custom syncable property
     * @return a reference to this {@code ContainerTileInventoryBuilder} to resume the "Builder" pattern
     */
    public ContainerBuilder sync(SyncableProperty<?> syncable)
    {
        this.syncables.add(syncable);
        return this;
    }

    void addPlayerInventoryRange(Range<Integer> range)
    {
        this.playerInventoryRanges.add(range);
    }

    void addTileInventoryRange(Range<Integer> range)
    {
        this.tileInventoryRanges.add(range);
    }

    /**
     * Create a {@link BuiltContainer} instance based on the current configuration.
     * <p>
     * While this method always return a separate instance it is currently unwise to
     * call it multiple times and to reuse a builder for multiple containers creation.
     *
     * @return an instance of {@link BuiltContainer} configured accordingly to this builder
     */
    public BuiltContainer create()
    {
        final BuiltContainer built = new BuiltContainer(this.name, this.player, this.inventories, this.canInteract,
                this.playerInventoryRanges, this.tileInventoryRanges);
        if (!this.syncables.isEmpty())
            built.setSyncables(this.syncables);
        if (!this.craftEvents.isEmpty())
            built.addCraftEvents(this.craftEvents);

        this.slots.forEach(built::addSlot);

        this.slots.clear();
        return built;
    }
}
