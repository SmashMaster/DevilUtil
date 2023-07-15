package com.samrj.devil.gui;

import com.samrj.devil.math.Vec2;

import java.util.ArrayList;
import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

/**
 * A combo box allows for the selection of one of many options, by using a drop
 * down menu.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class ComboBox extends Form
{
    private static final float ICON_W = 0.40f;
    private static final float ICON_H = 0.20f;
    
    private final DropDown dropDown = new DropDown(this);
    private final ScrollBox scroll = new ScrollBox();
    private final LayoutRows rows = new LayoutRows();
    private final ArrayList<String> options = new ArrayList<>();
    private int selection;
    private final Vec2 alignment = Align.W.vector();
    private float dropDownHeight = 128.0f;
    private float padding = 3.0f;
    private Consumer<ComboBox> onSelect;
    
    public ComboBox()
    {
        scroll.setContent(rows).setPadding(rows.getSpacing()*0.5f);
        dropDown.setContent(scroll).setPadding(0.0f);
    }
    
    public int getSelection()
    {
        return selection;
    }
    
    public String getSelectionText()
    {
        return options.get(selection);
    }
    
    public ComboBox setSelection(int selection)
    {
        if (options.isEmpty()) return this;
        if (selection < 0 || selection >= options.size()) throw new IndexOutOfBoundsException();
        this.selection = selection;
        return this;
    }

    public int getSelectionCount()
    {
        return options.size();
    }
    
    public ComboBox clear()
    {
        options.clear();
        rows.clear();
        return this;
    }
    
    public ComboBox addOption(String option)
    {
        int index = options.size();
        options.add(option);
        rows.add(new Option(index, option), alignment);
        return this;
    }
    
    public ComboBox setOptions(String... options)
    {
        clear();
        for (String option : options) addOption(option);
        return this;
    }

    public ComboBox setOptions(Iterable<String> options)
    {
        clear();
        for (String option : options) addOption(option);
        return this;
    }

    public ComboBox setDropDownHeight(float dropDownHeight)
    {
        this.dropDownHeight = dropDownHeight;
        return this;
    }
    
    public ComboBox setAlignment(Vec2 alignment)
    {
        this.alignment.set(alignment);
        rows.setAllAlignments(alignment);
        return this;
    }
    
    public ComboBox setPadding(float padding)
    {
        if (padding < 0.0f) throw new IllegalArgumentException();
        this.padding = padding;
        return this;
    }
    
    public ComboBox setSelectCallback(Consumer<ComboBox> onSelect)
    {
        this.onSelect = onSelect;
        return this;
    }
    
    public ComboBox setSize(float width)
    {
        this.width = width;
        this.height = DUI.font().getHeight() + padding*2.0f;
        rows.updateSize();
        scroll.setSizeFromContent(dropDownHeight).setWidth(width - height + ScrollBox.SCROLLBAR_WIDTH);
        dropDown.setSizeFromContent();
        return this;
    }
    
    public ComboBox setSizeFromContent()
    {
        rows.updateSize();
        scroll.setSizeFromContent(dropDownHeight);
        dropDown.setSizeFromContent();
        this.height = DUI.font().getHeight() + padding*2.0f;
        this.width = dropDown.getSize().x + height - ScrollBox.SCROLLBAR_WIDTH;
        return this;
    }
    
    @Override
    protected void updateSize()
    {
        scroll.updateSize();
    }
    
    @Override
    protected Form hover(float x, float y)
    {
        if (!contains(x, y)) return null;
        return this;
    }
    
    @Override
    protected ScrollBox findScrollBox(float x, float y)
    {
        if (DUI.getDropDown() != dropDown) return null;
        if (!contains(x, y)) return null;
        return scroll;
    }
    
    @Override
    protected boolean activate(int button)
    {
        if (button != GLFW_MOUSE_BUTTON_LEFT) return false;
        DUI.dropDown(dropDown, x0, y0, getSize());
        return false;
    }

    @Override
    protected void render(DUIDrawer drawer)
    {
        float x1 = x0 + width, y1 = y0 + height;
        
        float outline = DUI.getHoveredForm() == this ? 1.0f : 0.75f;
        
        drawer.color(0.25f, 0.25f, 0.25f, 1.0f);
        drawer.rectFill(x0, x1, y0, y1);
        drawer.color(outline, outline, outline, 1.0f);
        drawer.rect(x0, x1, y0, y1);
        
        float iconW = height*ICON_W;
        float iconX1 = x1 - (height - iconW)*0.5f;
        float iconX0 = iconX1 - iconW;
        float iconXMid = iconX1 - iconW*0.5f;
        
        float iconH = height*ICON_H;
        float iconY0 = y0 + (height - iconH)*0.5f;
        float iconY1 = iconY0 + iconH;
        
        drawer.line(x1 - height, x1 - height, y0, y1);
        drawer.triFill(iconX0, iconY1, iconXMid, iconY0, iconX1, iconY1);
        
        if (!options.isEmpty())
        {
            String text = options.get(selection);
            Font font = DUI.font();
            Vec2 aligned = Align.insideBounds(font.getSize(text), x0 + padding, x1 - height - padding, y0 + padding, y1 - padding, alignment);
            drawer.text(text, font, aligned.x, aligned.y);
        }
    }
    
    private class Option extends Text
    {
        private final int index;
        
        public Option(int index, String text)
        {
            super(text);
            
            this.index = index;
        }
        
        @Override
        protected Form hover(float x, float y)
        {
            Vec2 ddp = dropDown.getPos();
            Vec2 dds = dropDown.getSize();
            float spacing = rows.getSpacing()*0.5f;
            if (x < ddp.x || x > ddp.x + dds.x || y < y0 - spacing || y > y0 + height + spacing) return null;
            return this;
        }
        
        @Override
        protected boolean activate(int button)
        {
            if (button != GLFW_MOUSE_BUTTON_LEFT) return false;
            if (selection != index)
            {
                selection = index;
                if (onSelect != null) onSelect.accept(ComboBox.this);
            }
            DUI.closeDropDown();
            return false;
        }
        
        @Override
        protected void render(DUIDrawer drawer)
        {
            float outline;
            
            if (DUI.getHoveredForm() == this)
            {
                Vec2 ddp = dropDown.getPos();
                Vec2 dds = dropDown.getSize();
                float spacing = rows.getSpacing()*0.5f;
                drawer.color(0.5f, 0.5f, 0.5f, 1.0f);
                drawer.rectFill(ddp.x, ddp.x + dds.x, y0 - spacing, y0 + height + spacing);
                
                outline = 1.0f;
            }
            else outline = 0.75f;
            
            drawer.color(outline, outline, outline, 1.0f);
            drawer.text(getText(), DUI.font(), x0, y0);
        }
    }
}
