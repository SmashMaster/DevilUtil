package com.samrj.devil.ui;

import com.samrj.devil.graphics.GLTexture2D;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.res.Resource;
import java.io.*;
import org.lwjgl.opengl.GL11;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Font
{
    private GLTexture2D tex;
    private float[] widths;
    private float cellHeight;
    private float height;
    
    public Font(GLTexture2D tex, Resource wPath, float height) throws IOException
    {
        if (height <= 0f) throw new IllegalArgumentException();
        if (tex == null) throw new NullPointerException();
        this.tex = tex;
        this.height = height;
        cellHeight = tex.height/16.0f;
        widths = new float[256];
        
        InputStream in = wPath.open();
        if (in == null) throw new FileNotFoundException(wPath.path);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        
        for (int i=0; i<256; i++)
        {
            if (reader.ready())
            {
                String line = reader.readLine();
                if (line.isEmpty()) continue;
                String[] split = line.split(",");
                if (split.length != 2) continue;
                try
                {
                    widths[i] = Integer.parseInt(split[1]);
                }
                catch (NumberFormatException e) {}
            }
            else break;
        }
        
        reader.close();
    }
    
    public Font(GLTexture2D tex, String wPath, float height) throws IOException
    {
        this(tex, Resource.find(wPath), height);
    }
    
    public GLTexture2D getTexture()
    {
        return tex;
    }
    
    public float getWidth(char c)
    {
        if (c < 0 || c >= 256) return 0;
        
        return widths[c];
    }
    
    public float getWidth(String text)
    {
        float out = 0f;
        for (int i=0; i<text.length(); i++) out += getWidth(text.charAt(i));
        return out;
    }
    
    public float getHeight()
    {
        return height;
    }
    
    public void draw(String text, Vec2 pos, Vec2 align)
    {
        pos = new Vec2(pos.x, pos.y - cellHeight);
        align = new Vec2(align.x - 1.0f, -align.y - 1.0f).mult(0.5f);
        align.x *= getWidth(text);
        align.y *= -height;
        pos.add(align);
        
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glTranslatef(Math.round(pos.x), Math.round(pos.y), 0.0f);

        float x0 = 0f;
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        tex.glBind();
        GL11.glBegin(GL11.GL_QUADS);
        for (int i=0; i<text.length(); i++)
        {
            int charbits = text.charAt(i)-32;
            int fontposx = charbits & 0xF;
            int fontposy = 15 - (charbits >> 4);
            float u0 = fontposx++/16f;
            float v0 = fontposy++/16f;
            float u1 = fontposx/16f;
            float v1 = fontposy/16f;

            float x1 = x0 + cellHeight;

            GL11.glTexCoord2f(u0, v0); GL11.glVertex2f(x0, 0f);
            GL11.glTexCoord2f(u0, v1); GL11.glVertex2f(x0, cellHeight);
            GL11.glTexCoord2f(u1, v1); GL11.glVertex2f(x1, cellHeight);
            GL11.glTexCoord2f(u1, v0); GL11.glVertex2f(x1, 0f);

            x0 += getWidth(text.charAt(i));
        }
        GL11.glEnd();
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        GL11.glPopMatrix();
    }
    
    public void draw(String text, Vec2 p, Alignment align)
    {
        draw(text, p, align.dir());
    }
    
    public void draw(String text, Vec2 p)
    {
        draw(text, p, Alignment.NE);
    }
    
    public void delete()
    {
        tex.glDelete();
        tex = null;
    }
}
