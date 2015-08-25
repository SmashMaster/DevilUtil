package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;
import com.samrj.devil.util.SortedArray;

/**
 * This test sweeps an ellipsoid through geometry, returning a list of each
 * contact.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class EllipsoidCast
{
    public final Vec3 p0, p1;
    public final Vec3 radius;
    public final boolean terminated;
    public final SortedArray<Contact> contacts;
    
    EllipsoidCast(Vec3 p0, Vec3 p1, Vec3 radius, boolean terminated)
    {
        this.p0 = p0; this.p1 = p1;
        this.radius = radius;
        this.terminated = terminated;
        contacts = new SortedArray<>(10, Contact.comparator);
    }
    
    void test(Testable t)
    {
        Contact contact = t.test(this);
        if (contact == null) return;
        contacts.insert(contact);
    }
    
    public interface Testable
    {
        public Contact test(EllipsoidCast cast);
    }
}
