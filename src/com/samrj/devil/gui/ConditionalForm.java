package com.samrj.devil.gui;

import java.util.function.IntSupplier;

/**
 * This class lets you swap between any number of forms to display depending on
 * the index provided by a callback. This is useful for changing forms without
 * having to add or remove them.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class ConditionalForm extends Form
{
    private Form[] forms;
    private Form form;
    private IntSupplier condition;
    
    public ConditionalForm setCondition(IntSupplier condition)
    {
        this.condition = condition;
        return this;
    }
    
    public ConditionalForm setForms(Form... forms)
    {
        this.forms = forms;
        return this;
    }
    
    @Override
    protected void updateSize()
    {
        //Best place to update index. Done once per frame before everything.
        if (forms != null) form = forms[condition.getAsInt()];
        else form = null;
        
        if (form != null)
        {
            form.updateSize();
            width = form.width;
            height = form.height;
        }
        else
        {
            width = 0;
            height = 0;
        }
    }
    
    @Override
    protected void layout(float x, float y)
    {
        x0 = x; y0 = y;
        if (form != null) form.layout(x, y);
    }
    
    @Override
    protected Form hover(float x, float y)
    {
        if (form != null) return form.hover(x, y);
        return null;
    }
    
    @Override
    protected ScrollBox findScrollBox(float x, float y)
    {
        if (form != null) return form.findScrollBox(x, y);
        return null;
    }
    
    @Override
    protected void render(DUIDrawer drawer)
    {
        if (form != null) form.render(drawer);
    }
}
