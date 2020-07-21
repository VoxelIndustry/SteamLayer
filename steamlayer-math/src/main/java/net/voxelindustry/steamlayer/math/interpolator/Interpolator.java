package net.voxelindustry.steamlayer.math.interpolator;

@FunctionalInterface
public interface Interpolator
{
    float apply(float delta);
}
