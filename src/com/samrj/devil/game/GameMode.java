package com.samrj.devil.game;

/**
 * Used to set the behavior of Game.
 *
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2022 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public interface GameMode
{
    void setHasFocus(boolean hasFocus);
    void resize(int width, int height);
    void beforeInput();
    void mouseAxis(float dx, float dy);
    void input(String target, boolean active);
    void step(float stepTime);
    void render(float frameTime);
    void renderHUD();
    void destroy();
}
