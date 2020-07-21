package net.voxelindustry.steamlayer.math;

import net.minecraft.util.math.Quaternion;

public class Vec3fMutable extends Vec3f
{
    public Vec3fMutable(float x, float y, float z)
    {
        super(x, y, z);
    }

    public Vec3fMutable set(float x, float y, float z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    @Override
    public Vec3fMutable normalize()
    {
        float magnitude = magnitude();
        set(x / magnitude, y / magnitude, z / magnitude);
        return this;
    }

    @Override
    public Vec3fMutable subtract(Vec3f other)
    {
        set(getX() - other.getX(), getY() - other.getY(), getZ() - other.getZ());
        return this;
    }

    @Override
    public Vec3fMutable subtract(float x, float y, float z)
    {
        set(getX() - x, getY() - y, getZ() - z);
        return this;
    }

    @Override
    public Vec3fMutable add(Vec3f other)
    {
        set(getX() + other.getX(), getY() + other.getY(), getZ() + other.getZ());
        return this;
    }

    @Override
    public Vec3fMutable add(float x, float y, float z)
    {
        set(getX() + x, getY() + y, getZ() + z);
        return this;
    }
    
    @Override
    public Vec3fMutable cross(Vec3f other)
    {
        set(
                y * other.getZ() - z * other.getY(),
                z * other.getX() - x * other.getZ(),
                x * other.getY() - y * other.getX());
        return this;
    }

    @Override
    public Vec3fMutable lerp(Vec3f vector, float delta)
    {
        float negatedDelta = 1.0F - delta;
        set(x * negatedDelta + vector.x * delta, y * negatedDelta + vector.y * delta, z * negatedDelta + vector.z * delta);
        return this;
    }

    @Override
    public Vec3fMutable rotate(Quaternion quaternion)
    {
        Quaternion multipliedQuat = new Quaternion(quaternion);
        multipliedQuat.hamiltonProduct(new Quaternion(getX(), getY(), getZ(), 0.0F));
        Quaternion conjugatedQuat = new Quaternion(quaternion);
        conjugatedQuat.conjugate();
        multipliedQuat.hamiltonProduct(conjugatedQuat);

        set(multipliedQuat.getX(), multipliedQuat.getY(), multipliedQuat.getZ());
        return this;
    }

    @Override
    public Vec3fMutable scale(float scale)
    {
        set(x * scale, y * scale, z * scale);
        return this;
    }

    @Override
    public boolean isMutable()
    {
        return true;
    }
}
