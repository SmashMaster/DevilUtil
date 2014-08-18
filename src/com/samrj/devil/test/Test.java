package com.samrj.devil.test;

import com.samrj.devil.ui.Element;
import com.samrj.devil.ui.KeyEvent;
import com.samrj.devil.ui.MouseEvent;

/**
 * Interface for tests that can be run on DevilUtil.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public interface Test extends Element
{
    @Override public void in(MouseEvent in);
    @Override public void in(KeyEvent in);
    public void step(float dt);
    public void render();
}
