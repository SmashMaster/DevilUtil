package com.samrj.devil.ui;

import com.samrj.devil.math.Vec2;
import java.util.ArrayList;
import org.lwjgl.opengl.GL11;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Paragraph
{
    private float width;
    private String[] lines;
    private Font font;
    
    public Paragraph(String text, Font font, float width)
    {
        this.font = font;
        this.width = width;
        setText(text);
    }
    
    public void setText(String text)
    {
        ArrayList<String> lines = new ArrayList();
        String line = "";
        String word = "";
        float lineX = 0f;
        float wordX = 0f;
        
        for (int i=0; i<text.length(); i++)
        {
            char c = text.charAt(i);
            float cWidth = font.getWidth(c);
            switch (c)
            {
                case '\n':
                    line += word;
                    lineX += wordX;
                    lines.add(line);
                    line = "";
                    lineX = 0f;
                    word = "";
                    wordX =  0f;
                    continue;
                case ' ':
                    if (lineX + wordX > width)
                    {
                        lines.add(line);
                        line = "";
                        lineX = 0f;
                    }
                    else
                    {
                        word += c;
                        wordX += cWidth;
                        line += word;
                        lineX += wordX;
                        word = "";
                        wordX = 0;
                        break;
                    }
                default:
                    word += c;
                    wordX += cWidth;
                    break;
            }
        }
        
        if (lineX + wordX > width)
        {
            lines.add(line);
            lines.add(word);
        }
        else lines.add(line + word);
        
        this.lines = new String[lines.size()];
        
        for (int i=0; i<lines.size(); i++) this.lines[i] = lines.get(i).trim();
    }
    
    public float getHeight()
    {
        return font.getHeight()*lines.length;
    }
    
    public void draw(Vec2 pos, Alignment a)
    {
        if (a == null) throw new IllegalArgumentException();
        
        GL11.glPushMatrix();
        GL11.glTranslatef(pos.x, pos.y, 0f);
        
        float y = 0f;
        for (int i=0; i<lines.length; i++)
        {
            font.draw(lines[i], new Vec2(0f, y), a);
            y -= font.getHeight();
        }
        
        GL11.glPopMatrix();
    }
    
    public void draw(Vec2 pos)
    {
        draw(pos, Alignment.W);
    }
}
