package com.samrj.devil.gui;

import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec2;

import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.*;

/**
 * A multiple-line text form.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Paragraph extends FormColor
{
    private String rawText = "";
    private int textLength;
    private final TreeMap<Integer, Line> lines = new TreeMap<>();
    
    private final Vec2 alignment = Align.NW.vector();
    private float linePadding = 0.0f;
    private boolean editable;
    private int caret, select;
    private Consumer<Paragraph> onFocus, onLoseFocus, onChanged;

    private int dragStartIndex; //For drag selection
    private boolean dragged;
    
    public Paragraph()
    {
    }

    private void addLine(String lineText, float lineWidth)
    {
        Line line = new Line(textLength, lineText, lineWidth);
        lines.put(textLength, line);
        textLength += lineText.length() + 1;
        height = lines.size()*DUI.font().getHeight() + Util.max((lines.size() - 1)*linePadding, 0.0f);
    }

    private void addLines(String... lineArray)
    {
        for (String line : lineArray)
        {
            float lineWidth = DUI.font().getWidth(line);

            while (lineWidth > width)
            {
                //need to split line.
                int overlapIndex = DUI.font().getCaret(line, width);
                int splitIndex = Math.min(overlapIndex, line.length() - 1);
                while (splitIndex > 0 && !Character.isWhitespace(line.charAt(splitIndex))) splitIndex--;

                if (splitIndex == 0) break;

                String splitStr = line.substring(0, splitIndex);
                float splitWidth = DUI.font().getWidth(splitStr);
                addLine(splitStr, splitWidth);

                lineWidth -= splitWidth;
                line = line.substring(splitIndex + 1);
            }

            addLine(line, lineWidth);
        }
    }
    
    public Paragraph println(String text)
    {
        addLines(text.split("\n", -1));

        rawText += text + '\n';
        caret = rawText.length();
        select = caret;
        DUI.resetCaretBlinkTimer();
        return this;
    }

    private void updateLines()
    {
        lines.clear();
        textLength = 0;
        addLines(rawText.split("\n", -1));
    }

    public Paragraph clear()
    {
        rawText = "";
        lines.clear();
        caret = 0;
        select = 0;
        return this;
    }
    
    public Paragraph setWidth(float width)
    {
        if (width <= 0.0f) throw new IllegalArgumentException();
        this.width = width;
        return this;
    }
    
    public Paragraph setAlignment(Vec2 alignment)
    {
        this.alignment.set(alignment);
        return this;
    }

    public Paragraph setEditable(boolean editable)
    {
        this.editable = editable;
        return this;
    }
    
    public Paragraph setLinePadding(float linePadding)
    {
        if (linePadding < 0.0f) throw new IllegalArgumentException();
        this.linePadding = linePadding;
        return this;
    }
    
    public Paragraph setFocusCallback(Consumer<Paragraph> onFocus)
    {
        this.onFocus = onFocus;
        return this;
    }
    
    public Paragraph setLoseFocusCallback(Consumer<Paragraph> onLoseFocus)
    {
        this.onLoseFocus = onLoseFocus;
        return this;
    }

    public Paragraph setChangedCallback(Consumer<Paragraph> onChanged)
    {
        this.onChanged = onChanged;
        return this;
    }

    public Paragraph selectAll()
    {
        caret = rawText.length();
        select = 0;
        DUI.resetCaretBlinkTimer();
        return this;
    }
    
    public Paragraph goToEnd()
    {
        caret = rawText.length();
        select = caret;
        DUI.resetCaretBlinkTimer();
        return this;
    }
    
    @Override
    protected void layout(Window window, float x, float y)
    {
        super.layout(window, x, y);

        updateLines();

        float fontHeight = DUI.font().getHeight();
        float lineY = y0 + height - fontHeight;
        for (Line line : lines.values())
        {
            line.x0 = Align.insideBounds(line.width, x0, x0 + width, alignment.x);
            line.y0 = lineY;
            lineY -= fontHeight + linePadding;
        }
    }
    
    private int getIndex(float x, float y)
    {
        if (lines.isEmpty()) return 0;
        
        Line closestLine = null;
        for (Line line : lines.values()) if (y > line.y0)
        {
            closestLine = line;
            break;
        }
        if (closestLine == null) closestLine = lines.lastEntry().getValue();
        
        int lineIndex = DUI.font().getCaret(closestLine.text, x - closestLine.x0);
        return closestLine.index + lineIndex;
    }
    
    @Override
    protected Form hover(float x, float y)
    {
        if (dragged)
        {
            int oldCaret = caret;
            
            caret = getIndex(x, y);
            select = dragStartIndex;
            
            if (caret != oldCaret) DUI.resetCaretBlinkTimer();
        }
        else dragStartIndex = getIndex(x, y);
        
        if (x < this.x0 || x > this.x0 + width || y < this.y0 || y > this.y0 + height) return null;
        return this;
    }

    @Override
    public Cursor getHoverCursor()
    {
        return Cursor.IBEAM;
    }

    @Override
    protected void character(char character, int codepoint)
    {
        if (!editable) return;

        int s0 = Math.min(caret, select);
        int s1 = Math.max(caret, select);

        int oldCaret = caret;

        rawText = rawText.substring(0, s0) + character + rawText.substring(s1);
        caret = s0 + 1;
        select = caret;

        updateLines();
        if (onChanged != null) onChanged.accept(this);
        if (caret != oldCaret) DUI.resetCaretBlinkTimer();
    }

    @Override
    protected boolean activate(int button)
    {
        if (button != GLFW_MOUSE_BUTTON_LEFT) return false;
        dragged = true;
        
        if (DUI.getFocusedForm() != this)
        {
            DUI.resetCaretBlinkTimer();
            DUI.focus(this);
            if (onFocus != null) onFocus.accept(this);
        }
        
        return true;
    }

    @Override
    protected void deactivate()
    {
        dragged = false;
    }
    
    private void offsetCaretLine(boolean up)
    {
        Entry<Integer, Line> entry = lines.floorEntry(caret);
        if (entry == null) return;
        
        Entry<Integer, Line> offsetEntry = up ? lines.lowerEntry(entry.getKey()) : lines.higherEntry(entry.getKey()); 
        if (offsetEntry == null) return;
        
        Font font = DUI.font();
        
        Line currentLine = entry.getValue();
        float x = currentLine.x0 + font.getWidth(currentLine.substring(0, caret - currentLine.index));
        
        Line newLine = offsetEntry.getValue();
        caret = newLine.index + font.getCaret(newLine.text, x - newLine.x0);
    }

    private void delete(int s0, int s1)
    {
        rawText = rawText.substring(0, s0) + rawText.substring(s1);
        caret = s0;
        select = s0;
    }

    @Override
    protected void key(int key, int action, int mods)
    {
        if (action != GLFW_PRESS && action != GLFW_REPEAT) return;
        
        boolean control = (mods & GLFW_MOD_CONTROL) != 0;
        boolean shift = (mods & GLFW_MOD_SHIFT) != 0;
        
        int s0 = Math.min(caret, select);
        int s1 = Math.max(caret, select);
        
        int oldCaret = caret;
        
        switch (key)
        {
            case GLFW_KEY_BACKSPACE:
                if (!editable) break;
                if (s1 != s0) delete(s0, s1);
                else if (caret > 0)
                {
                    rawText = rawText.substring(0, caret - 1) + rawText.substring(caret);
                    caret--;
                    select = caret;
                }
                updateLines();
                break;
            case GLFW_KEY_DELETE:
                if (!editable) break;
                if (s1 != s0) delete(s0, s1);
                else if (caret < rawText.length())
                    rawText = rawText.substring(0, caret) + rawText.substring(caret + 1);
                updateLines();
                break;
            case GLFW_KEY_LEFT:
                if (caret > 0)
                {
                    caret--;
                    if (!shift) select = caret;
                }
                break;
            case GLFW_KEY_RIGHT:
                if (caret < rawText.length())
                {
                    caret++;
                    if (!shift) select = caret;
                }
                break;
            case GLFW_KEY_UP:
                offsetCaretLine(true);
                if (!shift) select = caret;
                break;
            case GLFW_KEY_DOWN:
                offsetCaretLine(false);
                if (!shift) select = caret;
                break;
            case GLFW_KEY_HOME:
                caret = 0;
                if (!shift) select = caret;
                break;
            case GLFW_KEY_END:
                caret = rawText.length();
                if (!shift) select = caret;
                break;
            case GLFW_KEY_A:
                if (control)
                {
                    select = 0;
                    caret = rawText.length();
                }
                break;
            case GLFW_KEY_C: if (control && s0 != s1) glfwSetClipboardString(0, rawText.substring(s0, s1)); break;
            case GLFW_KEY_X:
                if (control && s0 != s1)
                {
                    glfwSetClipboardString(0, rawText.substring(s0, s1));
                    if (editable)
                    {
                        delete(s0, s1);
                        updateLines();
                    }
                }
                break;
            case GLFW_KEY_V:
                if (editable && control)
                {
                    String str = glfwGetClipboardString(0);
                    rawText = rawText.substring(0, s0) + str + rawText.substring(s1);
                    caret = s0 + str.length();
                    select = caret;
                    updateLines();
                }
                break;
            case GLFW_KEY_ENTER: character('\n', 0); break;
            case GLFW_KEY_Z: break; //Undo
            case GLFW_KEY_Y: break; //Redo
            default: return;
        }
        
        if (caret != oldCaret) DUI.resetCaretBlinkTimer();
    }
    
    @Override
    protected void defocus()
    {
        if (onLoseFocus != null) onLoseFocus.accept(this);
    }

    @Override
    protected void render(DUIDrawer drawer)
    {
        if (lines.isEmpty()) return;
        
        Font font = DUI.font();
        float fontHeight = font.getHeight();
        boolean focused = DUI.getFocusedForm() == this;
        
        if (focused)
        {
            int s0 = Math.min(caret, select);
            int s1 = Math.max(caret, select);
            
            Entry<Integer, Line> e0 = lines.floorEntry(s0);
            Entry<Integer, Line> e1 = lines.floorEntry(s1);
            
            Line line0 = e0.getValue();
            Line line1 = e1.getValue();
            
            String text0 = line0.substring(0, s0 - line0.index);
            String text1 = line1.substring(0, s1 - line1.index);
            
            float alignX0 = line0.x0 + font.getWidth(text0);
            float alignX1 = line1.x0 + font.getWidth(text1);
            
            if (s0 != s1)
            { 
                drawer.color(selectionColor);
                if (line0 == line1)
                    drawer.rectFill(alignX0, alignX1, line0.y0, line0.y0 + fontHeight);
                else for (Line line : lines.subMap(e0.getKey(), true, e1.getKey(), true).values())
                {
                    if (line == line0) drawer.rectFill(alignX0, line.x0 + line.width, line.y0, line.y0 + fontHeight);
                    else if (line == line1) drawer.rectFill(line.x0, alignX1, line.y0, line.y0 + fontHeight);
                    else drawer.rectFill(line.x0, line.x0 + line.width, line.y0, line.y0 + fontHeight);
                }
            }
            
            for (Line line : lines.values())
            {
                drawer.color(lineColor);
                drawer.text(line.text, font, line.x0, line.y0);
            }
            
            if (DUI.getCaretBlink())
            {
                drawer.color(activeColor);
                if (caret > select) drawer.line(alignX1, alignX1, line1.y0, line1.y0 + fontHeight);
                else drawer.line(alignX0, alignX0, line0.y0, line0.y0 + fontHeight);
            }
        }
        else for (Line line : lines.values())
        {
            drawer.color(lineColor);
            drawer.text(line.text, font, line.x0, line.y0);
        }
    }
    
    private static class Line
    {
        private final int index;
        private final String text;
        private final float width;
        
        private float x0, y0;
        
        private Line(int index, String text, float width)
        {
            this.index = index;
            this.text = text;
            this.width = width;
        }
        
        private String substring(int beginIndex, int endIndex)
        {
            if (beginIndex >= endIndex) return "";
            if (endIndex > text.length()) endIndex = text.length();
            return text.substring(beginIndex, endIndex);
        }
    }
}
