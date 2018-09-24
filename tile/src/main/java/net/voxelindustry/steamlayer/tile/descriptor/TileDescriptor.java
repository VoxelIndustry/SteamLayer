package net.voxelindustry.steamlayer.tile.descriptor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class TileDescriptor
{
    private String name;

    private Map<Class<?>, ITileComponent> components;

    public TileDescriptor(String name)
    {
        this.name = name;

        this.components = new HashMap<>();
    }

    public boolean has(Class<?> componentType)
    {
        return this.components.containsKey(componentType);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> componentType)
    {
        return (T) this.components.get(componentType);
    }

    public TileDescriptor component(ITileComponent component)
    {
        component.setDescriptor(this);
        this.components.put(component.getClass(), component);
        return this;
    }
}
