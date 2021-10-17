/*
 * Copyright (c) 2021 Sam Johnson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.samrj.devil.math.zorder;

import com.samrj.devil.math.Vec2i;
import com.samrj.devil.math.Vec3i;

/**
 * Utility methods for encoding and decoding z-order curve indices, also known
 * as Morton codes. Useful for improving cache locality on big 2D and 3D arrays.
 * 
 * @author Samuel Johnson (SmashMaster)
 */
public final class ZOrderCurve
{
    private static int part1By1(int x)
    {
        x &= 0x0000ffff;
        x = (x ^ (x << 8)) & 0x00ff00ff;
        x = (x ^ (x << 4)) & 0x0f0f0f0f;
        x = (x ^ (x << 2)) & 0x33333333;
        x = (x ^ (x << 1)) & 0x55555555;
        return x;
    }

    private static int part1By2(int x)
    {
        x &= 0x000003ff;
        x = (x ^ (x << 16)) & 0xff0000ff;
        x = (x ^ (x << 8)) & 0x0300f00f;
        x = (x ^ (x << 4)) & 0x030c30c3;
        x = (x ^ (x << 2)) & 0x09249249;
        return x;
    }
    
    private static int compact1By1(int x)
    {
        x &= 0x55555555;
        x = (x ^ (x >>> 1)) & 0x33333333;
        x = (x ^ (x >>> 2)) & 0x0f0f0f0f;
        x = (x ^ (x >>> 4)) & 0x00ff00ff;
        x = (x ^ (x >>> 8)) & 0x0000ffff;
        return x;
    }

    private static int compact1By2(int x)
    {
        x &= 0x09249249;
        x = (x ^ (x >>> 2)) & 0x030c30c3;
        x = (x ^ (x >>> 4)) & 0x0300f00f;
        x = (x ^ (x >>> 8)) & 0xff0000ff;
        x = (x ^ (x >>> 16)) & 0x000003ff;
        return x;
    }
    
    /**
     * Encodes the given x and y coordinates to a z-order curve. Will correctly
     * produce results for coordinates up to 65535 (2^16 - 1).
     * 
     * @param x The X coordinate to encode.
     * @param y The Y coordinate to encode.
     * @return The index of the point along the Z-Order Curve corresponding to
     * the given coordinates.
     */
    public static int encode2(int x, int y)
    {
        return (part1By1(y) << 1) + part1By1(x);
    }

    /**
     * Encodes the given x and y coordinates to a z-order curve. Will correctly
     * produce results for coordinates up to 1023 (2^10 - 1).
     * 
     * @param x The X coordinate to encode.
     * @param y The Y coordinate to encode.
     * @return The index of the point along the Z-Order Curve corresponding to
     * the given coordinates.
     */
    public static int encode3(int x, int y, int z)
    {
        return (part1By2(z) << 2) + (part1By2(y) << 1) + part1By2(x);
    }
    
    /**
     * Decodes the given z-order curve index to 2D coordinates.
     * 
     * @param code The z-order curve index to decode.
     * @return 2D coordinates in the range [0, 65535]
     */
    public static Vec2i decode2(int code)
    {
        return new Vec2i(compact1By1(code), compact1By1(code >>> 1));
    }
    
    /**
     * Decodes the given z-order curve index to 3D coordinates.
     * 
     * @param code The z-order curve index to decode.
     * @return 2D coordinates in the range [0, 1023]
     */
    public static Vec3i decode3(int code)
    {
        return new Vec3i(compact1By2(code), compact1By2(code >>> 1), compact1By2(code >>> 2));
    }
}
