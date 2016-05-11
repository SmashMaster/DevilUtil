package com.samrj.devil.util;

import java.util.Objects;

/**
 * Generic pair class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Pair<A, B>
{
    public final A a;
    public final B b;
    
    public Pair(A a, B b)
    {
        this.a = a;
        this.b = b;
    }
    
    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 53*hash + Objects.hashCode(a);
        hash = 53*hash + Objects.hashCode(b);
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Pair<?, ?> other = (Pair<?, ?>)obj;
        return Objects.equals(a, other.a) && Objects.equals(this.b, other.b);
    }
}
