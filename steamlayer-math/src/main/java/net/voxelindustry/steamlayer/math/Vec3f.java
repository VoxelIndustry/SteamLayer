package net.voxelindustry.steamlayer.math;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;

@EqualsAndHashCode
@ToString
@AllArgsConstructor
public class Vec3f
{
    public static final Vec3f ORIGIN = new Vec3f(0, 0, 0);
    public static final Vec3f ONE    = new Vec3f(1, 1, 1);

    public static final Vec3f UP    = new Vec3f(0, 1, 0);
    public static final Vec3f DOWN  = new Vec3f(0, -1, 0);
    public static final Vec3f NORTH = new Vec3f(0, 0, -1);
    public static final Vec3f SOUTH = new Vec3f(0, 0, 1);
    public static final Vec3f EAST  = new Vec3f(-1, 0, 0);
    public static final Vec3f WEST  = new Vec3f(1, 0, 0);

    @Getter
    protected float x;
    @Getter
    protected float y;
    @Getter
    protected float z;

    public float magnitude()
    {
        return (float) Math.sqrt(getX() * getX() + getY() * getY() + getZ() * getZ());
    }

    public Vec3f normalize()
    {
        float magnitude = magnitude();
        return new Vec3f(x / magnitude, y / magnitude, z / magnitude);
    }

    public Vec3f subtract(Vec3f other)
    {
        return new Vec3f(getX() - other.getX(), getY() - other.getY(), getZ() - other.getZ());
    }

    public Vec3f subtract(float x, float y, float z)
    {
        return new Vec3f(getX() - x, getY() - y, getZ() - z);
    }

    public Vec3f add(Vec3f other)
    {
        return new Vec3f(getX() + other.getX(), getY() + other.getY(), getZ() + other.getZ());
    }

    public Vec3f add(float x, float y, float z)
    {
        return new Vec3f(getX() + x, getY() + y, getZ() + z);
    }

    public float dot(Vec3f other)
    {
        return x * other.x + y * other.y + z * other.z;
    }

    public Vec3f cross(Vec3f other)
    {
        return new Vec3f(
                y * other.getZ() - z * other.getY(),
                z * other.getX() - x * other.getZ(),
                x * other.getY() - y * other.getX());
    }

    public Vec3f lerp(Vec3f vector, float delta)
    {
        float negatedDelta = 1.0F - delta;
        return new Vec3f(x * negatedDelta + vector.x * delta, y * negatedDelta + vector.y * delta, z * negatedDelta + vector.z * delta);
    }

    public Vec3f rotate(Quaternion quaternion)
    {
        Quaternion multipliedQuat = new Quaternion(quaternion);
        multipliedQuat.hamiltonProduct(new Quaternion(getX(), getY(), getZ(), 0.0F));
        Quaternion conjugatedQuat = new Quaternion(quaternion);
        conjugatedQuat.conjugate();
        multipliedQuat.hamiltonProduct(conjugatedQuat);

        return new Vec3f(multipliedQuat.getX(), multipliedQuat.getY(), multipliedQuat.getZ());
    }

    public Vec3f scale(float scale)
    {
        return new Vec3f(x * scale, y * scale, z * scale);
    }

    public net.minecraft.util.math.Vec3f toVector3f()
    {
        return new net.minecraft.util.math.Vec3f(x, y, z);
    }

    public Vec3d toVector3d()
    {
        return new Vec3d(x, y, z);
    }

    public boolean isMutable()
    {
        return false;
    }

    public Vec3fMutable toMutable()
    {
        return new Vec3fMutable(x, y, z);
    }

    public static Vec3fMutable mutable()
    {
        return new Vec3fMutable(0, 0, 0);
    }

    public static Vec3f immutable(float x, float y, float z)
    {
        return new Vec3f(x, y, z);
    }

    public static Vec3f fromVector3f(net.minecraft.util.math.Vec3f vector)
    {
        return immutable(vector.getX(), vector.getY(), vector.getZ());
    }

    public static Vec3f fromVector3d(Vec3d vector)
    {
        return immutable((float) vector.getX(), (float) vector.getY(), (float) vector.getZ());
    }

    public Quaternion getDegreesQuaternion(float angle)
    {
        return new Quaternion(new net.minecraft.util.math.Vec3f(x, y, z), angle, true);
    }
}
