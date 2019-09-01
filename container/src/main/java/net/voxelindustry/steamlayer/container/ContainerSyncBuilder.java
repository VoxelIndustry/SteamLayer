package net.voxelindustry.steamlayer.container;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;
import net.voxelindustry.steamlayer.container.sync.SyncedArrayProperty;
import net.voxelindustry.steamlayer.container.sync.SyncedEnumListProperty;
import net.voxelindustry.steamlayer.container.sync.SyncedListProperty;
import net.voxelindustry.steamlayer.container.sync.SyncedProperty;
import net.voxelindustry.steamlayer.container.sync.SyncedValue;
import net.voxelindustry.steamlayer.container.sync.SyncedWrappers;
import org.apache.commons.lang3.Range;
import sun.misc.SharedSecrets;

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
        this.syncs = new ArrayList<>();
        this.namedSyncs = new HashMap<>();
    }

    private SyncedValue registerSynced(SyncedValue synced, String name)
    {
        this.syncs.add(synced);
        if (name != null)
            this.namedSyncs.put(name, synced);
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
        this.registerSynced(new SyncedProperty<>(supplier, setter,
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
        this.registerSynced(new SyncedProperty<>(supplier, setter,
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
        this.registerSynced(new SyncedProperty<>(supplier, setter,
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
        this.registerSynced(new SyncedProperty<>(supplier, setter,
                SyncedWrappers.instance().get(String.class)), name);
        return this;
    }

    /**
     * Sync a {@link FluidStack} value between the server and the client
     *
     * @param supplier a supplier giving the value from the server
     * @param setter   a consumer used to set the value of the client
     * @param name     unique name identifier for this Synced
     * @return a reference to this {@code ContainerSyncBuilder} to resume the "Builder" pattern
     */
    public ContainerSyncBuilder syncFluid(Supplier<FluidStack> supplier, Consumer<FluidStack> setter, String name)
    {
        this.registerSynced(new SyncedProperty<>(supplier, setter,
                SyncedWrappers.instance().get(FluidStack.class)), name);
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
        this.registerSynced(new SyncedProperty<>(supplier, setter,
                SyncedWrappers.instance().get(ItemStack.class)), name);
        return this;
    }

    /**
     * Sync an {@link IItemHandler} value between the server and the client
     *
     * @param supplier a supplier giving the value from the server
     * @param setter   a consumer used to set the value of the client
     * @param name     unique name identifier for this Synced
     * @return a reference to this {@code ContainerSyncBuilder} to resume the "Builder" pattern
     */
    public ContainerSyncBuilder syncInventory(Supplier<IItemHandler> supplier, Consumer<IItemHandler> setter,
                                              String name)
    {
        this.syncInventory(supplier, setter, 0, name);
        return this;
    }

    /**
     * Sync an {@link IItemHandler} value between the server and the client
     *
     * @param supplier a supplier giving the value from the server
     * @param setter   a consumer used to set the value of the client
     * @param syncRate the rate in ticks used to query and refresh the synced
     * @param name     unique name identifier for this Synced
     * @return a reference to this {@code ContainerSyncBuilder} to resume the "Builder" pattern
     */
    public ContainerSyncBuilder syncInventory(Supplier<IItemHandler> supplier, Consumer<IItemHandler> setter,
                                              int syncRate, String name)
    {
        this.registerSynced(new SyncedProperty<>(supplier, setter,
                SyncedWrappers.instance().get(IItemHandler.class), syncRate), name);
        return this;
    }

    /**
     * Sync a generic value between the server and the client
     *
     * @param synced a custom synced property
     * @param name   unique name identifier for this Synced
     * @return a reference to this {@code ContainerSyncBuilder} to resume the "Builder" pattern
     */
    public ContainerSyncBuilder syncAny(SyncedProperty<?> synced, String name)
    {
        this.registerSynced(synced, name);
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
        this.registerSynced(new SyncedArrayProperty<>(supplier, SyncedWrappers.instance().get(elementClass),
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

        this.registerSynced(new SyncedListProperty<>(supplier, SyncedWrappers.instance().get(elementClass), range),
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
        this.syncInteger(() -> supplier.get().ordinal(), ordinal -> consumer.accept(SharedSecrets.getJavaLangAccess()
                .getEnumConstantsShared(enumClass)[ordinal]), name);
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
        this.registerSynced(new SyncedEnumListProperty<>(supplier, enumClass, range), name);
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
        return this.syncBoolean(supplier, setter, null);
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
        return this.syncInteger(supplier, setter, null);
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
        this.syncFloat(supplier, setter, null);
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
        this.syncString(supplier, setter, null);
        return this;
    }

    /**
     * Sync a {@link FluidStack} value between the server and the client
     *
     * @param supplier a supplier giving the value from the server
     * @param setter   a consumer used to set the value of the client
     * @return a reference to this {@code ContainerSyncBuilder} to resume the "Builder" pattern
     */
    public ContainerSyncBuilder syncFluid(Supplier<FluidStack> supplier, Consumer<FluidStack> setter)
    {
        this.syncFluid(supplier, setter, null);
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
        this.syncItem(supplier, setter, null);
        return this;
    }

    /**
     * Sync an {@link IItemHandler} value between the server and the client
     *
     * @param supplier a supplier giving the value from the server
     * @param setter   a consumer used to set the value of the client
     * @return a reference to this {@code ContainerSyncBuilder} to resume the "Builder" pattern
     */
    public ContainerSyncBuilder syncInventory(Supplier<IItemHandler> supplier, Consumer<IItemHandler> setter)
    {
        this.syncInventory(supplier, setter, null);
        return this;
    }

    /**
     * Sync an {@link IItemHandler} value between the server and the client
     *
     * @param supplier a supplier giving the value from the server
     * @param setter   a consumer used to set the value of the client
     * @param syncRate the rate in ticks used to query and refresh the synced
     * @return a reference to this {@code ContainerSyncBuilder} to resume the "Builder" pattern
     */
    public ContainerSyncBuilder syncInventory(Supplier<IItemHandler> supplier, Consumer<IItemHandler> setter,
                                              int syncRate)
    {
        this.syncInventory(supplier, setter, syncRate, null);
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
        this.syncArray(supplier, elementClass, range, null);
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
        this.syncArray(supplier, elementClass, null, null);
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
        this.syncList(supplier, elementClass, range, null);
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
        this.syncList(supplier, elementClass, null, null);
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
        this.syncEnum(supplier, consumer, enumClass);
        return this;
    }

    /**
     * Sync a generic synced value between the server and the client
     *
     * @param synced a custom synced property
     * @return a reference to this {@code ContainerSyncBuilder} to resume the "Builder" pattern
     */
    public ContainerSyncBuilder syncAny(SyncedProperty<?> synced)
    {
        this.syncAny(synced, null);
        return this;
    }

    public BuiltContainer create()
    {
        this.parent.syncs = this.syncs;
        this.parent.namedSyncs = this.namedSyncs;
        return this.parent.create();
    }
}
