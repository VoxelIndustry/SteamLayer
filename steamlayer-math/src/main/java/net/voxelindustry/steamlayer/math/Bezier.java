package net.voxelindustry.steamlayer.math;

import org.joml.Vector3f;
import org.joml.Vector3fc;

public class Bezier
{
    public static Vector3fc pointOfBezier4f(Vector3fc[] controlPoints, float delta)
    {
        return new Vector3f(
                valueOfBezier4f(
                        controlPoints[0].x(),
                        controlPoints[1].x(),
                        controlPoints[2].x(),
                        controlPoints[3].x(),
                        delta),
                valueOfBezier4f(
                        controlPoints[0].y(),
                        controlPoints[1].y(),
                        controlPoints[2].y(),
                        controlPoints[3].y(),
                        delta),
                valueOfBezier4f(
                        controlPoints[0].z(),
                        controlPoints[1].z(),
                        controlPoints[2].z(),
                        controlPoints[3].z(),
                        delta));
    }

    public static float valueOfBezier4f(float value1, float value2, float value3, float value4, float delta)
    {
        var multiplied = (1 - delta) * value2 + delta * value3;
        return ((1 - delta) * ((1 - delta) * ((1 - delta) * value1 + delta * value2) + delta * multiplied) + delta * ((1 - delta) * multiplied + delta * ((1 - delta) * value3 + delta * value4)));
    }
}
