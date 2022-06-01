package com.samrj.devil.game;

import com.samrj.devil.game.config.Config;
import com.samrj.devil.game.sync.MovingLongAvg;
import com.samrj.devil.gui.Align;
import com.samrj.devil.gui.DUI;
import com.samrj.devil.math.Vec2i;

/**
 * Displays an average frame rate in the top-right of the screen.
 *
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2022 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class FPSCounter
{
    private static final int FPS_WINDOW = 32;
    private static final float HUD_MARGIN = 16.0f;

    private final Config config;
    private int resX, resY;

    private final MovingLongAvg frameAvg = new MovingLongAvg(FPS_WINDOW);

    public FPSCounter(Vec2i resolution, Config config)
    {
        resX = resolution.x; resY = resolution.y;
        this.config = config;
    }
    
    public void resize(int width, int height)
    {
        resX = width;
        resY = height;
    }

    public void render()
    {
        if (config.showFPS.get())
        {
            frameAvg.push(GameWindow.getLastFrameNano());
            double fps = 1_000_000_000.0/frameAvg.mean();
            DUI.drawer().begin()
                    .color(1.0f, 1.0f, 1.0f, 1.0f)
                    .text(String.format("%.1f", fps), DUI.font(), resX - HUD_MARGIN, resY - HUD_MARGIN, Align.SW.vector());
        }
    }
    
    public void destroy()
    {
    }
}
