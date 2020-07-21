package net.voxelindustry.steamlayer.math;

public class Bezier
{
    public static Vec3f pointOfBezier4f(Vec3f[] controlPoints, float delta)
    {
        return new Vec3f(
                valueOfBezier4f(
                        controlPoints[0].getX(),
                        controlPoints[1].getX(),
                        controlPoints[2].getX(),
                        controlPoints[3].getX(),
                        delta),
                valueOfBezier4f(
                        controlPoints[0].getY(),
                        controlPoints[1].getY(),
                        controlPoints[2].getY(),
                        controlPoints[3].getY(),
                        delta),
                valueOfBezier4f(
                        controlPoints[0].getZ(),
                        controlPoints[1].getZ(),
                        controlPoints[2].getZ(),
                        controlPoints[3].getZ(),
                        delta));
    }

    public static float valueOfBezier4f(float value1, float value2, float value3, float value4, float delta)
    {
        return ((1 - delta) * ((1 - delta) * ((1 - delta) * value1 + delta * value2) + delta * ((1 - delta) * value2 + delta * value3)) + delta * ((1 - delta) * ((1 - delta) * value2 + delta * value3) + delta * ((1 - delta) * value3 + delta * value4)));
    }
}
