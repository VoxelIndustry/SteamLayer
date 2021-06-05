package net.voxelindustry.steamlayer.container;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.voxelindustry.steamlayer.container.sync.SyncedArrayProperty;
import net.voxelindustry.steamlayer.container.sync.SyncedEnumListProperty;
import net.voxelindustry.steamlayer.container.sync.SyncedListProperty;
import net.voxelindustry.steamlayer.container.sync.SyncedProperty;
import net.voxelindustry.steamlayer.container.sync.SyncedValue;
import net.voxelindustry.steamlayer.container.sync.SyncedWrappers;
import org.apache.commons.lang3.Range;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ContainerSyncBuilder
{
    private final ContainerBuilder         parent;
    private final List<SyncedValue>        syncs;
    private final Map<String, SyncedValue> namedSyncs;

    ContainerSyncBuilder(ContainerBuilder parent)
    {
        this.parent = parent;
        syncs = new ArrayList<>();
        namedSyncs = new HashMap<>();
    }

    private SyncedValue registerSynced(SyncedValue synced, String name)
    {
        syncs.add(synced);
        if (name != null)
            namedSyncs.put(name, synced);
        return synced;
    }

    /**
     * Sync a Boolean value between the server and the client
     *
     * @param supplier a supplier giving the value from the server
     * @param setter   a consumer used to set the value of the client
     * @param name     unique name identifier for this Synced
     * @return a reference to this {@code ContainerSyncBuilder} to resume the "Builder" pattern
     */
    public ContainerSyncBuilder syncBoolean(Supplier<Boolean> supplier, Consumer<Boolean> setter, String name)
    {
        registerSynced(new SyncedProperty<>(supplier, setter,
                SyncedWrappers.instance().get(Boolean.class)), name);
        return this;
    }

    /**
     * Sync an Integer value between the server and the client
     *
     * @param supplier a supplier giving the value from the server
     * @param setter   a consumer used to set the value of the client
     * @param name     unique name identifier for this Synced
     * @return a reference to this {@code ContainerSyncBuilder} to resume the "Builder" pattern
     */
    public ContainerSyncBuilder syncInteger(Supplier<Integer> supplier, Consumer<Integer> setter, String name)
    {
        registerSynced(new SyncedProperty<>(supplier, setter,
                SyncedWrappers.instance().get(Integer.class)), name);
        return this;
    }

    /**
     * Sync a Float value between the server and the client
     *
     * @param supplier a supplier giving the value from the server
     * @param setter   a consumer used to set the value of the client
     * @param name     unique name identifier for this Synced
     * @return a reference to this {@code ContainerSyncBuilder} to resume the "Builder" pattern
     */
    public ContainerSyncBuilder syncFloat(Supplier<Float> supplier, Consumer<Float> setter, String name)
    {
        registerSynced(new SyncedProperty<>(supplier, setter,
                SyncedWrappers.instance().get(Float.class)), name);
        return this;
    }

    /**
     * Sync a String value between the server and the client
     *
     * @param supplier a supplier giving the value from the server
     * @param setter   a consumer used to set the value of the client
     * @param name     unique name identifier for this Synced
     * @return a reference to this {@code ContainerSyncBuilder} to resume the "Builder" pattern
     */
    public ContainerSyncBuilder syncString(Supplier<String> supplier, Consumer<String> setter, String name)
    {
        registerSynced(new SyncedProperty<>(supplier, setter,
                SyncedWrappers.instance().get(String.class)), name);
        return this;
    }

    /**
     * Sync an {@link ItemStack} value between the server and the client
     *
     * @param supplier a supplier giving the value from the server
     * @param setter   a consumer used to set the value of the client
     * @param name     unique name identifier for this Synced
     * @return a reference to this {@code ContainerSyncBuilder} to resume the "Builder" pattern
     */
    public ContainerSyncBuilder syncItem(Supplier<ItemStack> supplier, Consumer<ItemStack> setter, String name)
    {
        registerSynced(new SyncedProperty<>(supplier, setter,
                SyncedWrappers.instance().get(ItemStack.class)), name);
        return this;
    }

    /**
     * Sync an {@link Inventory} value between the server and the client
     *
     * @param supplier a supplier giving the value from the server
     * @param setter   a consumer used to set the value of the client
     * @param name     unique name identifier for this Synced
     * @return a reference to this {@code ContainerSyncBuilder} to resume the "Builder" pattern
     */
    public ContainerSyncBuilder syncInventory(Supplier<Inventory> supplier, Consumer<Inventory> setter,
                                              String name)
    {
        syncInventory(supplier, setter, 0, name);
        return this;
    }

    /**
     * Sync an {@link Inventory} value between the server and the client
     *
     * @param supplier a supplier giving the value from the server
     * @param setter   a consumer used to set the value of the client
     * @param syncRate the rate in ticks used to query and refresh the synced
     * @param name     unique name identifier for this Synced
     * @return a reference to this {@code ContainerSyncBuilder} to resume the "Builder" pattern
     */
    public ContainerSyncBuilder syncInventory(Supplier<Inventory> supplier, Consumer<Inventory> setter,
                                              int syncRate, String name)
    {
        registerSynced(new SyncedProperty<>(supplier, setter,
                SyncedWrappers.instance().get(Inventory.class), syncRate), name);
        return this;
    }

    /**
     * Sync a generic value between the server and the client
     *
     * @param synced a custom synced value
     * @param name   unique name identifier for this Synced
     * @return a reference to this {@code ContainerSyncBuilder} to resume the "Builder" pattern
     */
    public ContainerSyncBuilder syncAny(SyncedValue synced, String name)
    {
        registerSynced(synced, name);
        return this;
    }

    /**
     * Sync a generic array between the server and the client
     *
     * @param supplier     a supplier giving the value from the server
     * @param elementClass the Class instance of the array elements
     * @param range        to which the array will be synced
     * @param name         unique name identifier for this Synced
     * @param <T>          generic type of the array element
     * @return a reference to this {@code ContainerSyncBuilder} to resume the "Builder" pattern
     */
    public <T> ContainerSyncBuilder syncArray(Supplier<T[]> supplier, Class<T> elementClass, Range<Integer> range,
                                              String name)
    {
        registerSynced(new SyncedArrayProperty<>(supplier, SyncedWrappers.instance().get(elementClass),
                elementClass, range), name);
        return this;
    }

    /**
     * Sync a generic list between the server and the client
     *
     * @param supplier     a supplier giving the value from the server
     * @param elementClass the Class instance of the list elements
     * @param range        to which the list will be synced
     * @param name         unique name identifier for this Synced
     * @param <T>          generic type of the list element
     * @return a reference to this {@code ContainerSyncBuilder} to resume the "Builder" pattern
     */
    public <T> ContainerSyncBuilder syncList(Supplier<List<T>> supplier, Class<T> elementClass, Range<Integer> range,
                                             String name)
    {
        if (elementClass.isEnum())
            throw new RuntimeException("Enum must be synced with either syncEnum or syncEnumList!");

        registerSynced(new SyncedListProperty<>(supplier, SyncedWrappers.instance().get(elementClass), range),
                name);
        return this;
    }

    /**
     * Sync an enum between the server and the client
     *
     * @param supplier  a supplier giving the value from the server
     * @param consumer  a consumer used to set the value of the client
     * @param enumClass the Class instance of the enum
     * @param name      unique name identifier for this Synced
     * @param <E>       generic type of the enum
     * @return a reference to this {@code ContainerSyncBuilder} to resume the "Builder" pattern
     */
    public <E extends Enum<E>> ContainerSyncBuilder syncEnum(Supplier<E> supplier, Consumer<E> consumer,
                                                             Class<E> enumClass, String name)
    {
        syncInteger(() -> supplier.get().ordinal(), ordinal -> consumer.accept(enumClass.getEnumConstants()[ordinal]), name);
        return this;
    }

    /**
     * Sync a list of enum values between the server and the client
     *
     * @param supplier  a supplier giving the value from the server
     * @param enumClass the Class instance of the enum values
     * @param name      unique name identifier for this Synced
     * @param <E>       generic type of the enum
     * @return a reference to this {@code ContainerSyncBuilder} to resume the "Builder" pattern
     */
    public <E extends Enum<E>> ContainerSyncBuilder syncEnumList(Supplier<List<E>> supplier, Class<E> enumClass,
                                                                 Range<Integer> range, String name)
    {
        registerSynced(new SyncedEnumListProperty<>(supplier, enumClass, range), name);
        return this;
    }

    /**
     * Sync a Boolean value between the server and the client
     *
     * @param supplier a supplier giving the value from the server
     * @param setter   a consumer used to set the value of the client
     * @return a reference to this {@code ContainerSyncBuilder} to resume the "Builder" pattern
     */
    public ContainerSyncBuilder syncBoolean(Supplier<Boolean> supplier, Consumer<Boolean> setter)
    {
        return syncBoolean(supplier, setter, null);
    }

    /**
     * Sync an Integer value between the server and the client
     *
     * @param supplier a supplier giving the value from the server
     * @param setter   a consumer used to set the value of the client
     * @return a reference to this {@code ContainerSyncBuilder} to resume the "Builder" pattern
     */
    public ContainerSyncBuilder syncInteger(Supplier<Integer> supplier, Consumer<Integer> setter)
    {
        return syncInteger(supplier, setter, null);
    }

    /**
     * Sync a Float value between the server and the client
     *
     * @param supplier a supplier giving the value from the server
     * @param setter   a consumer used to set the value of the client
     * @return a reference to this {@code ContainerSyncBuilder} to resume the "Builder" pattern
     */
    public ContainerSyncBuilder syncFloat(Supplier<Float> supplier, Consumer<Float> setter)
    {
        syncFloat(supplier, setter, null);
        return this;
    }

    /**
     * Sync a String value between the server and the client
     *
     * @param supplier a supplier giving the value from the server
     * @param setter   a consumer used to set the value of the client
     * @return a reference to this {@code ContainerSyncBuilder} to resume the "Builder" pattern
     */
    public ContainerSyncBuilder syncString(Supplier<String> supplier, Consumer<String> setter)
    {
        syncString(supplier, setter, null);
        return this;
    }

    /**
     * Sync an {@link ItemStack} value between the server and the client
     *
     * @param supplier a supplier giving the value from the server
     * @param setter   a consumer used to set the value of the client
     * @return a reference to this {@code ContainerSyncBuilder} to resume the "Builder" pattern
     */
    public ContainerSyncBuilder syncItem(Supplier<ItemStack> supplier, Consumer<ItemStack> setter)
    {
        syncItem(supplier, setter, null);
        return this;
    }

    /**
     * Sync an {@link Inventory} value between the server and the client
     *
     * @param supplier a supplier giving the value from the server
     * @param setter   a consumer used to set the value of the client
     * @return a reference to this {@code ContainerSyncBuilder} to resume the "Builder" pattern
     */
    public ContainerSyncBuilder syncInventory(Supplier<Inventory> supplier, Consumer<Inventory> setter)
    {
        syncInventory(supplier, setter, null);
        return this;
    }

    /**
     * Sync an {@link Inventory} value between the server and the client
     *
     * @param supplier a supplier giving the value from the server
     * @param setter   a consumer used to set the value of the client
     * @param syncRate the rate in ticks used to query and refresh the synced
     * @return a reference to this {@code ContainerSyncBuilder} to resume the "Builder" pattern
     */
    public ContainerSyncBuilder syncInventory(Supplier<Inventory> supplier, Consumer<Inventory> setter,
                                              int syncRate)
    {
        syncInventory(supplier, setter, syncRate, null);
        return this;
    }

    /**
     * Sync a generic array between the server and the client
     *
     * @param supplier     a supplier giving the value from the server
     * @param elementClass the Class instance of the array elements
     * @param range        to which the array will be synced
     * @param <T>          generic type of the array element
     * @return a reference to this {@code ContainerSyncBuilder} to resume the "Builder" pattern
     */
    public <T> ContainerSyncBuilder syncArray(Supplier<T[]> supplier, Class<T> elementClass, Range<Integer> range)
    {
        syncArray(supplier, elementClass, range, null);
        return this;
    }

    /**
     * Sync a generic array between the server and the client
     *
     * @param supplier     a supplier giving the value from the server
     * @param elementClass the Class instance of the array elements
     * @param <T>          generic type of the array element
     * @return a reference to this {@code ContainerSyncBuilder} to resume the "Builder" pattern
     */
    public <T> ContainerSyncBuilder syncArray(Supplier<T[]> supplier, Class<T> elementClass)
    {
        syncArray(supplier, elementClass, null, null);
        return this;
    }

    /**
     * Sync a generic list between the server and the client
     *
     * @param supplier     a supplier giving the value from the server
     * @param elementClass the Class instance of the list elements
     * @param range        to which the array will be synced
     * @param <T>          generic type of the list element
     * @return a reference to this {@code ContainerSyncBuilder} to resume the "Builder" pattern
     */
    public <T> ContainerSyncBuilder syncList(Supplier<List<T>> supplier, Class<T> elementClass, Range<Integer> range)
    {
        syncList(supplier, elementClass, range, null);
        return this;
    }

    /**
     * Sync a generic list between the server and the client
     *
     * @param supplier     a supplier giving the value from the server
     * @param elementClass the Class instance of the list elements
     * @param <T>          generic type of the list element
     * @return a reference to this {@code ContainerSyncBuilder} to resume the "Builder" pattern
     */
    public <T> ContainerSyncBuilder syncList(Supplier<List<T>> supplier, Class<T> elementClass)
    {
        syncList(supplier, elementClass, null, null);
        return this;
    }

    /**
     * Sync an enum between the server and the client
     *
     * @param supplier  a supplier giving the value from the server
     * @param consumer  a consumer used to set the value of the client
     * @param enumClass the Class instance of the enum
     * @param <E>       generic type of the enum
     * @return a reference to this {@code ContainerSyncBuilder} to resume the "Builder" pattern
     */
    public <E extends Enum<E>> ContainerSyncBuilder syncEnum(Supplier<E> supplier, Consumer<E> consumer,
                                                             Class<E> enumClass)
    {
        syncEnum(supplier, consumer, enumClass);
        return this;
    }

    /**
     * Sync a generic synced value between the server and the client
     *
     * @param synced a custom synced value
     * @return a reference to this {@code ContainerSyncBuilder} to resume the "Builder" pattern
     */
    public ContainerSyncBuilder syncAny(SyncedValue synced)
    {
        syncAny(synced, null);
        return this;
    }

    public BuiltContainer create(int windowId)
    {
        parent.syncs = syncs;
        parent.namedSyncs = namedSyncs;
        return parent.create(windowId);
    }
}
