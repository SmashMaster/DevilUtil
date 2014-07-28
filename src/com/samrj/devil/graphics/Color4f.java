package com.samrj.devil.graphics;

import com.samrj.devil.buffer.Bufferable;
import com.samrj.devil.buffer.FloatBuffer;
import org.lwjgl.opengl.GL11;

public final class Color4f implements Bufferable<FloatBuffer>
{
    // <editor-fold defaultstate="collapsed" desc="Factory Methods">
    public static Color4f black()
    {
        return new Color4f(0f, 0f, 0f, 1f);
    }
    
    public static Color4f white()
    {
        return new Color4f(1f, 1f, 1f, 1f);
    }
    
    public static Color4f grey(float b)
    {
        return new Color4f(b, b, b, 1f);
    }
    // </editor-fold>
    
    public float r, g, b, a;
    
    // <editor-fold defaultstate="collapsed" desc="Constructors">
    public Color4f(float r, float g, float b, float a)
    {
        set(r, g, b, a);
    }
    
    public Color4f(float r, float g, float b)
    {
        set(r, g, b, 1f);
    }
    
    public Color4f(Color4f c)
    {
        set(c);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Mutator Methods">
    public Color4f set(float r, float g, float b, float a)
    {
        this.r = r; this.g = g; this.b = b; this.a = a;
        return this;
    }
    
    public Color4f set(Color4f c)
    {
        return set(c.r, c.g, c.b, c.a);
    }
    
    public Color4f add(float r, float g, float b, float a)
    {
        return set(this.r+r, this.g+g, this.b+b, this.a+a);
    }
    
    public Color4f add(Color4f c)
    {
        return add(c.r, c.g, c.b, c.a);
    }
    
    public Color4f sub(float r, float g, float b, float a)
    {
        return set(this.r-r, this.g-g, this.b-b, this.a-a);
    }
    
    public Color4f sub(Color4f c)
    {
        return sub(c.r, c.g, c.b, c.a);
    }
    
    public Color4f mult(float r, float g, float b, float a)
    {
        return set(this.r*r, this.g*g, this.b*b, this.a*a);
    }
    
    public Color4f mult(Color4f c)
    {
        return mult(c.r, c.g, c.b, c.a);
    }
    
    public Color4f mult(float s)
    {
        return mult(s, s, s, s);
    }
    
    public Color4f div(float r, float g, float b, float a)
    {
        return set(this.r/r, this.g/g, this.b/b, this.a/a);
    }
    
    public Color4f div(Color4f c)
    {
        return div(c.r, c.g, c.b, c.a);
    }
    
    public Color4f div(float s)
    {
        return div(s, s, s, s);
    }
    
    public Color4f avg(Color4f c)
    {
        return add(c).div(2f);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Accessor Methods">
    public void glColor()
    {
        GL11.glColor4f(r, g, b, a);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Ocerriden Object Methods">
    @Override
    public String toString()
    {
        return "("+r+", "+g+", "+b+")";
    }
    
    @Override
    public Color4f clone()
    {
        return new Color4f(this);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Ocerriden Bufferable Methods">
    @Override
    public void putIn(FloatBuffer buf)
    {
        buf.put(r, g, b, a);
    }
    
    @Override
    public int size()
    {
        return 4;
    }
    // </editor-fold>
}