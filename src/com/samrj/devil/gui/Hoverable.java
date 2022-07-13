package com.samrj.devil.gui;

public interface Hoverable
{
    default Cursor getHoverCursor()
    {
        return Cursor.DEFAULT;
    }
}
