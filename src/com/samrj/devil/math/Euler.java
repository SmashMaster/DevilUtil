package com.samrj.devil.math;

import java.util.Objects;

/**
 * Euler angles class.
 *
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2022 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Euler
{
    public enum RotOrder
    {
        XYZ(0, 1, 2, false),
        XZY(0, 2, 1, true),
        YXZ(1, 0, 2, true),
        YZX(1, 2, 0, false),
        ZXY(2, 0, 1, false),
        ZYX(2, 1, 0, true);

        private final int axis0, axis1, axis2;
        private boolean parity;

        RotOrder(int axis0, int axis1, int axis2, boolean parity)
        {
            this.axis0 = axis0;
            this.axis1 = axis1;
            this.axis2 = axis2;
            this.parity = parity;
        }
    }

    public static float getAxis(Euler rot, int axis)
    {
        return switch(axis)
        {
            case 0 -> rot.angle0;
            case 1 -> rot.angle1;
            case 2 -> rot.angle2;
            default -> throw new ArrayIndexOutOfBoundsException();
        };
    }

    public static void setAxis(Euler rot, int axis, float angle)
    {
        switch(axis)
        {
            case 0 -> rot.angle0 = angle;
            case 1 -> rot.angle1 = angle;
            case 2 -> rot.angle2 = angle;
            default -> throw new ArrayIndexOutOfBoundsException();
        }
    }

    public static void toQuat(Euler rot, Quat result)
    {
        int i = rot.order.axis0, j = rot.order.axis1, k = rot.order.axis2;

        double ti = getAxis(rot, i)*0.5f;
        double tj = getAxis(rot, j)*(rot.order.parity ? -0.5f : 0.5f);
        double th = getAxis(rot, k)*0.5f;

        double ci = Math.cos(ti);
        double cj = Math.cos(tj);
        double ch = Math.cos(th);
        double si = Math.sin(ti);
        double sj = Math.sin(tj);
        double sh = Math.sin(th);

        double cc = ci*ch;
        double cs = ci*sh;
        double sc = si*ch;
        double ss = si*sh;

        result.w = (float)(cj*cc + sj*ss);
        result.x = (float)(cj*sc - sj*cs);
        result.y = (float)(cj*ss + sj*cc);
        result.z = (float)(cj*cs - sj*sc);

        if (rot.order.parity) {
            result.setComponent(j + 1, -result.getComponent(j + 1));
        }
    }

    public static Quat toQuat(Euler rot)
    {
        Quat result = new Quat();
        toQuat(rot, result);
        return result;
    }

    public static void toMat3(Euler rot, Mat3 result)
    {
        int i = rot.order.axis0, j = rot.order.axis1, k = rot.order.axis2;

        float ti, tj, th;
        if (rot.order.parity) {
            ti = -rot.getAxis(i);
            tj = -rot.getAxis(j);
            th = -rot.getAxis(k);
        }
        else {
            ti = rot.getAxis(i);
            tj = rot.getAxis(j);
            th = rot.getAxis(k);
        }

        double ci = Math.cos(ti);
        double cj = Math.cos(tj);
        double ch = Math.cos(th);
        double si = Math.sin(ti);
        double sj = Math.sin(tj);
        double sh = Math.sin(th);

        double cc = ci*ch;
        double cs = ci*sh;
        double sc = si*ch;
        double ss = si*sh;

        result.setEntry(i, i, (float)(cj * ch));
        result.setEntry(i, j, (float)(sj * sc - cs));
        result.setEntry(i, k, (float)(sj * cc + ss));
        result.setEntry(j, i, (float)(cj * sh));
        result.setEntry(j, j, (float)(sj * ss + cc));
        result.setEntry(j, k, (float)(sj * cs - sc));
        result.setEntry(k, i, (float)(-sj));
        result.setEntry(k, j, (float)(cj * si));
        result.setEntry(k, k, (float)(cj * ci));
    }

    public static Mat3 toMat3(Euler rot)
    {
        Mat3 result = new Mat3();
        toMat3(rot, result);
        return result;
    }

    public float angle0, angle1, angle2;

    private RotOrder order;

    public Euler(float angle0, float angle1, float angle2, RotOrder order)
    {
        this.angle0 = angle0;
        this.angle1 = angle1;
        this.angle2 = angle2;
        this.order = Objects.requireNonNull(order);
    }

    public float getAxis(int axis)
    {
        return getAxis(this, axis);
    }

    public Euler setOrder(RotOrder order)
    {
        this.order = Objects.requireNonNull(order);
        return this;
    }

    public Euler setAxis(int axis, float angle)
    {
        setAxis(this, axis, angle);
        return this;
    }

    @Override
    public String toString()
    {
        return "Euler{" + "angle0=" + angle0 + ", angle1=" + angle1 + ", angle2=" + angle2 + ", order=" + order + '}';
    }
}
