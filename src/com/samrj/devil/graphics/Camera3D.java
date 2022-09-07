/*
 * Copyright (c) 2019 Sam Johnson
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

package com.samrj.devil.graphics;

import com.samrj.devil.math.*;

/**
 * 3D view matrix class.
 * 
 * @author Samuel Johnson (SmashMaster)
 */
public final class Camera3D
{
    public final Vec3 pos = new Vec3();
    public final Quat dir = Quat.identity();
    public final Mat4 viewMat = Mat4.identity();
    public final Vec3 right = new Vec3(1.0f, 0.0f, 0.0f);
    public final Vec3 up = new Vec3(0.0f, 1.0f, 0.0f);
    public final Vec3 forward = new Vec3(0.0f, 0.0f, -1.0f);

    public void pointAt(Vec3 p)
    {
        dir.setRotation(new Vec3(0.0f, 0.0f, -1.0f), Vec3.sub(p, pos));
    }
    
    /**
     * Updates the matrices and axis directions for this camera.
     */
    public void update()
    {
        viewMat.setRotation(Quat.invert(dir));
        viewMat.translate(Vec3.negate(pos));
        
        Mat3 rot = Mat3.rotation(dir);
        right.setAsColumn(rot, 0);
        up.setAsColumn(rot, 1);
        forward.setAsColumn(rot, 2).negate();
    }
}
