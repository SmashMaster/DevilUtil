package com.samrj.devil.gui;

import com.samrj.devil.math.Vec2;

public interface LayoutAligned<SELF_TYPE extends LayoutAligned>
{
    SELF_TYPE add(Form form, Vec2 alignment);

    default SELF_TYPE add(Form form, Align alignment)
    {
        return add(form, alignment.vector());
    }

    SELF_TYPE add(Form form);
    SELF_TYPE setAllAlignments(Vec2 alignment);
}
