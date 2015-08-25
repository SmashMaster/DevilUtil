package com.samrj.devil.geo3d;

import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec3;
import java.util.Comparator;

/**
 * Contact class for results from ray and ellipsoid traces.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @param <TYPE> The type of object this contact handles.
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public abstract class Contact<TYPE>
{
    public static final Comparator<Contact> comparator = new ContactComparator();
    
    public enum Type
    {
        FACE, EDGE, POINT;
    }
    
    public final Type type;
    
    /**
     * The contact interpolant.
     */
    public final float t;
    
    /**
     * The contact distance from the start.
     */
    public final float d;
    
    /**
     * The position of the cast at the time of the contact.
     */
    public final Vec3 cp;
    
    /**
     * The surface position of the contact.
     */
    public final Vec3 p;
    
    /**
     * The normal of the contact.
     */
    public final Vec3 n;
    
    Contact(Type type, float t, float d, Vec3 cp, Vec3 p, Vec3 n)
    {
        this.type = type;
        this.t = t;
        this.d = d;
        this.cp = cp;
        this.p = p;
        this.n = n;
    }
    
    public abstract TYPE contact();
    
    private static class ContactComparator implements Comparator<Contact>
    {
        @Override
        public int compare(Contact c1, Contact c2)
        {
            if (c1 == c2) return 0;
            if (c1 == null) return 1;
            if (c2 == null) return -1;
            return Util.compare(c1.t, c2.t, 0.0f);
        }
    }
}
