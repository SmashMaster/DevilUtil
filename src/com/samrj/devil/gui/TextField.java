package com.samrj.devil.gui;

import com.samrj.devil.math.Vec2;

import java.util.Objects;
import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.*;

/**
 * A single-line text editor.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class TextField extends Form
{
    private static int CHAR_LIMIT = 128;
    
    private String text = "";
    private String previewText;
    private final Vec2 alignment = Align.NW.vector();
    private float padding = 3.0f;
    private int caret, select;
    private Consumer<TextField> onFocus, onLoseFocus, onConfirm, onChanged;
    
    private float dragStartX; //For drag selection
    private boolean dragged;
    
    public TextField()
    {
    }
    
    public String get()
    {
        return text;
    }
    
    public TextField setText(String text)
    {
        this.text = Objects.requireNonNull(text);
        caret = text.length();
        select = caret;
        DUI.resetCaretBlinkTimer();
        return this;
    }
    
    public TextField clear()
    {
        return setText("");
    }
    
    public TextField setPreviewText(String previewText)
    {
        this.previewText = previewText;
        return this;
    }
    
    /**
     * Sets the size of this text field based on its font, padding, and the
     * given width.
     */
    public TextField setSize(float width)
    {
        this.width = width;
        this.height = DUI.font().getHeight() + padding*2.0f;
        return this;
    }
    
    public TextField setAlignment(Vec2 alignment)
    {
        this.alignment.set(alignment);
        return this;
    }
    
    public TextField setPadding(float padding)
    {
        if (padding < 0.0f) throw new IllegalArgumentException();
        this.padding = padding;
        return this;
    }
    
    public TextField setFocusCallback(Consumer<TextField> onFocus)
    {
        this.onFocus = onFocus;
        return this;
    }
    
    public TextField setLoseFocusCallback(Consumer<TextField> onLoseFocus)
    {
        this.onLoseFocus = onLoseFocus;
        return this;
    }
    
    public TextField setConfirmCallback(Consumer<TextField> onConfirm)
    {
        this.onConfirm = onConfirm;
        return this;
    }
    
    public TextField setChangedCallback(Consumer<TextField> onChanged)
    {
        this.onChanged = onChanged;
        return this;
    }
    
    public TextField selectAll()
    {
        caret = text.length();
        select = 0;
        DUI.resetCaretBlinkTimer();
        return this;
    }
    
    public TextField goToEnd()
    {
        caret = text.length();
        select = caret;
        DUI.resetCaretBlinkTimer();
        return this;
    }
    
    @Override
    protected Form hover(float x, float y)
    {
        if (dragged)
        {
            int oldCaret = caret;
            
            Font font = DUI.font();
            float x1 = x0 + width, y1 = y0 + height;
            Vec2 size = font.getSize(text);
            Vec2 aligned = Align.insideBounds(size, x0 + padding, x1 - padding, y0 + padding, y1 - padding, alignment);
            
            caret = font.getCaret(text, x - aligned.x);
            select = font.getCaret(text, (dragStartX + x0) - aligned.x);
            
            if (caret != oldCaret) DUI.resetCaretBlinkTimer();
        }
        else dragStartX = x - x0;
        
        if (x < this.x0 || x > this.x0 + width || y < this.y0 || y > this.y0 + height) return null;
        return this;
    }

    @Override
    public Cursor getHoverCursor()
    {
        return Cursor.IBEAM;
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
    
    @Override
    protected void character(char character, int codepoint)
    {
        int s0 = Math.min(caret, select);
        int s1 = Math.max(caret, select);
        
        String oldText = text;
        int oldCaret = caret;
        
        text = text.substring(0, s0) + character + text.substring(s1, text.length());
        caret = s0 + 1;
        select = caret;
        
        if (onChanged != null && !text.equals(oldText)) onChanged.accept(this);
        if (caret != oldCaret) DUI.resetCaretBlinkTimer();
    }
    
    private void delete(int s0, int s1)
    {
        text = text.substring(0, s0) + text.substring(s1, text.length());
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
        
        String oldText = text;
        int oldCaret = caret;
        
        switch (key)
        {
            case GLFW_KEY_BACKSPACE:
                if (s1 != s0) delete(s0, s1);
                else if (caret > 0)
                {
                    text = text.substring(0, caret - 1) + text.substring(caret, text.length());
                    caret--;
                    select = caret;
                }
                break;
            case GLFW_KEY_DELETE:
                if (s1 != s0) delete(s0, s1);
                else if (caret < text.length())
                    text = text.substring(0, caret) + text.substring(caret + 1, text.length());
                break;
            case GLFW_KEY_LEFT:
                if (caret > 0)
                {
                    caret--;
                    if (!shift) select = caret;
                }
                break;
            case GLFW_KEY_RIGHT:
                if (caret < text.length())
                {
                    caret++;
                    if (!shift) select = caret;
                }
                break;
            case GLFW_KEY_HOME:
                caret = 0;
                if (!shift) select = caret;
                break;
            case GLFW_KEY_END:
                caret = text.length();
                if (!shift) select = caret;
                break;
            case GLFW_KEY_A:
                if (control)
                {
                    select = 0;
                    caret = text.length();
                }
                break;
            case GLFW_KEY_C: if (control && s0 != s1) glfwSetClipboardString(0, text.substring(s0, s1)); break;
            case GLFW_KEY_X:
                if (control && s0 != s1)
                {
                    glfwSetClipboardString(0, text.substring(s0, s1));
                    delete(s0, s1); //
                }
                break;
            case GLFW_KEY_V:
                if (control)
                {
                    String str = glfwGetClipboardString(0);
                    text = text.substring(0, s0) + str + text.substring(s1, text.length());
                    caret = s0 + str.length();
                    select = caret;
                }
                break;
            case GLFW_KEY_ENTER:
            case GLFW_KEY_KP_ENTER:
                if (action == GLFW_PRESS && onConfirm != null) onConfirm.accept(this);
                break;
            case GLFW_KEY_Z: break; //Undo
            case GLFW_KEY_Y: break; //Redo
            default: return;
        }
        
        if (onChanged != null && !text.equals(oldText)) onChanged.accept(this);
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
        float x1 = x0 + width, y1 = y0 + height;
        
        float outline = (DUI.getFocusedForm() == this || DUI.getHoveredForm() == this) ? 1.0f : 0.75f;
        
        if (text.length() > CHAR_LIMIT) setText(text.substring(0, CHAR_LIMIT));
        
        drawer.color(0.1875f, 0.1875f, 0.1875f, 1.0f);
        drawer.rectFill(x0, x1, y0, y1);
        drawer.color(outline, outline, outline, 1.0f);
        drawer.rect(x0, x1, y0, y1);
        
        Font font = DUI.font();
        
        if (DUI.getFocusedForm() == this)
        {
            Vec2 size = font.getSize(text);
            Vec2 aligned = Align.insideBounds(size, x0 + padding, x1 - padding, y0 + padding, y1 - padding, alignment);
            
            int s0 = Math.min(caret, select);
            int s1 = Math.max(caret, select);
            String text0 = text.substring(0, s0);
            String text1 = text.substring(s0, s1);
            float alignX0 = aligned.x + font.getWidth(text0);
            float alignX1 = alignX0 + font.getWidth(text1);
            
            drawer.color(0.5f, 0.5f, 0.5f, 1.0f);
            drawer.rectFill(alignX0, alignX1, aligned.y, aligned.y + size.y);
            drawer.color(1.0f, 1.0f, 1.0f, 1.0f);
            drawer.text(text, font, aligned.x, aligned.y);
            
            if (DUI.getCaretBlink())
            {
                float caretX = caret >= select ? alignX1 : alignX0;
                drawer.line(caretX, caretX, aligned.y, aligned.y + size.y);
            }
        }
        else if (!text.isEmpty())
        {
            Vec2 aligned = Align.insideBounds(font.getSize(text), x0 + padding, x1 - padding, y0 + padding, y1 - padding, alignment);
            drawer.color(outline, outline, outline, 1.0f);
            drawer.text(text, font, aligned.x, aligned.y);
        }
        else if (previewText != null)
        {
            Vec2 aligned = Align.insideBounds(font.getSize(previewText), x0 + padding, x1 - padding, y0 + padding, y1 - padding, alignment);
            drawer.color(outline*0.5f, outline*0.5f, outline*0.5f, 1.0f);
            drawer.text(previewText, font, aligned.x, aligned.y);
        }
    }
}
