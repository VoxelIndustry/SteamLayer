package net.voxelindustry.steamlayer.container;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.ScreenHandlerType;
import net.voxelindustry.steamlayer.container.slot.ListenerSlot;
import net.voxelindustry.steamlayer.container.sync.SyncedValue;
import org.apache.commons.lang3.Range;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    private final ScreenHandlerType<BuiltContainer> type;

    private final PlayerEntity            player;
    private       Predicate<PlayerEntity> canInteract = player -> true;

    final List<ListenerSlot>   slots;
    final List<Range<Integer>> playerInventoryRanges, tileInventoryRanges;

    private ContainerEvent       closeEvent;
    private List<ContainerEvent> tickEvents;

    List<SyncedValue>        syncs;
    Map<String, SyncedValue> namedSyncs;

    List<Inventory> inventories;
    BlockEntity     mainTile;

    /**
     * Creates a ContainerBuilder instance to produce a BuiltContainer
     * that can be used as a generic Container in Minecraft inventory system.
     * <p>
     * A new instance of ContainerBuilder must be created on each Container openings.
     * This restriction is subject to change.
     * <p>
     * This builder contains several sub builders to configure specific aspects of the Container logic.
     *
     * @param type   the ContainerType corresponding to the produced Container to be used as an identifier.
     * @param player the player instance to which the Container is to be attached.
     */
    public ContainerBuilder(ScreenHandlerType<BuiltContainer> type, PlayerEntity player)
    {
        this.type = type;
        this.player = player;

        slots = new ArrayList<>();
        playerInventoryRanges = new ArrayList<>();
        tileInventoryRanges = new ArrayList<>();

        tickEvents = new ArrayList<>();

        inventories = new ArrayList<>();
    }

    /**
     * Use this method to configure a custom interact predicate.
     * The vanilla behavior is to check the distance between the opened tile and the player.
     *
     * @param canInteract predicate consuming an {@link PlayerEntity} instance provided by the {@link BuiltContainer}
     * @return a reference to this {@code ContainerBuilder} to resume the "Builder" pattern
     */
    public ContainerBuilder interact(Predicate<PlayerEntity> canInteract)
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
    public ContainerPlayerInventoryBuilder player(PlayerEntity player)
    {
        return new ContainerPlayerInventoryBuilder(this, player, player.getInventory());
    }

    public ContainerSyncBuilder sync()
    {
        return new ContainerSyncBuilder(this);
    }

    public ContainerBuilder onClose(ContainerEvent event)
    {
        closeEvent = event;
        return this;
    }

    public ContainerBuilder onTick(ContainerEvent event)
    {
        tickEvents.add(event);
        return this;
    }

    void addPlayerInventoryRange(Range<Integer> range)
    {
        playerInventoryRanges.add(range);
    }

    void addTileInventoryRange(Range<Integer> range)
    {
        tileInventoryRanges.add(range);
    }

    /**
     * Create a {@link BuiltContainer} instance based on the current configuration.
     * <p>
     * While this method always return a separate instance it is currently unwise to
     * call it multiple times and to reuse a builder for multiple containers creation.
     *
     * @return an instance of {@link BuiltContainer} configured accordingly to this builder
     */
    public BuiltContainer create(int windowId)
    {
        BuiltContainer built = new BuiltContainer(
                type,
                windowId,
                player,
                inventories,
                canInteract,
                playerInventoryRanges,
                tileInventoryRanges);
        built.setMainTile(mainTile);

        built.setCloseEvent(closeEvent);
        built.setTickEvents(tickEvents);

        if (syncs != null)
            built.setSyncables(syncs, namedSyncs);

        slots.forEach(built::addSlot);

        slots.clear();
        return built;
    }
}
