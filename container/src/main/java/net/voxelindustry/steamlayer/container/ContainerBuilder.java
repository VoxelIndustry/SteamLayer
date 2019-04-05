package net.voxelindustry.steamlayer.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;
import net.voxelindustry.steamlayer.container.slot.ListenerSlot;
import net.voxelindustry.steamlayer.container.sync.SyncedValue;
import org.apache.commons.lang3.Range;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

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

    final List<Consumer<InventoryCrafting>> craftEvents;

    List<SyncedValue>        syncs;
    Map<String, SyncedValue> namedSyncs;

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

    public ContainerSyncBuilder sync()
    {
        return new ContainerSyncBuilder(this);
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
        BuiltContainer built = new BuiltContainer(this.name, this.player, this.inventories, this.canInteract,
                this.playerInventoryRanges, this.tileInventoryRanges);

        if (this.syncs != null)
            built.setSyncables(this.syncs, this.namedSyncs);
        if (!this.craftEvents.isEmpty())
            built.addCraftEvents(this.craftEvents);

        this.slots.forEach(built::addSlot);

        this.slots.clear();
        return built;
    }
}
