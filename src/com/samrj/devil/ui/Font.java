package com.samrj.devil.ui;

import com.samrj.devil.graphics.GLTexture2D;
import com.samrj.devil.math.Vector2f;
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
    private float height;
    
    public Font(GLTexture2D tex, Resource wPath, float height) throws IOException
    {
        if (height <= 0f) throw new IllegalArgumentException();
        if (tex == null) throw new NullPointerException();
        this.tex = tex;
        this.height = height;
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
    
    public void draw(String text, Vector2f pos, Vector2f align)
    {
        pos = pos.cadd(0f, -32f);
        align = align.cflipY().sub(1f, 1f).mult(.5f);
        align.mult(getWidth(text), -height);
        pos.add(align);
        
        GL11.glPushMatrix();
        pos.round().glTranslate();

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

            float x1 = x0 + 32f;

            GL11.glTexCoord2f(u0, v0); GL11.glVertex2f(x0, 0f);
            GL11.glTexCoord2f(u0, v1); GL11.glVertex2f(x0, 32f);
            GL11.glTexCoord2f(u1, v1); GL11.glVertex2f(x1, 32f);
            GL11.glTexCoord2f(u1, v0); GL11.glVertex2f(x1, 0f);

            x0 += getWidth(text.charAt(i));
        }
        GL11.glEnd();
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        GL11.glPopMatrix();
    }
    
    public void draw(String text, Vector2f p, Alignment align)
    {
        draw(text, p, align.dir());
    }
    
    public void draw(String text, Vector2f p)
    {
        draw(text, p, Alignment.NE);
    }
}
