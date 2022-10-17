package com.samrj.devil.game;

/**
 * Convenience class to set all callbacks (except init and destroy) of GameWindow at once.
 *
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2022 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public interface GameWindowMode
{
    void beforeInput();
    void afterInput();
    void resize(int width, int height);
    void mouseMoved(float x, float y);
    void mouseButton(int button, int action, int mods);
    void mouseScroll(float dx, float dy);
    void key(int key, int action, int mods);
    void character(char character, int codepoint);
    void step(float dt);
    void render();
    void destroy(boolean crashed);
}
