package com.samrj.devil.game;

import com.samrj.devil.gui.*;

import java.util.function.Function;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Developer/command console.
 *
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2022 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Console
{
    private static final float WIDTH = 512.0f, HEIGHT = 384.0f;
    private static final int MAX_HISTORY_LENGTH = 128;

    private final Window window;
    private final ScrollBox scrollBox;
    private final Paragraph paragraph;
    private final TextField textField;

    private Function<String, String> callback;
    private History firstCommand, lastCommand;
    private int historyLength;
    private History historyChoice;
    
    private boolean justOpened;
    
    public Console()
    {
        window = new Window()
                .setTitle("Developer Console")
                .setContent(new LayoutRows()
                        .add(scrollBox = new ScrollBox()
                                .setContent(paragraph = new Paragraph()
                                        .setWidth(WIDTH - ScrollBox.SCROLLBAR_WIDTH - 30.0f)
                                        .setAlignment(Align.SW.vector()))
                                .setPadding(10.0f)
                                .setAlignment(Align.SW.vector())
                                .setSize(WIDTH, HEIGHT))
                        .add(textField = new TextField().setSize(WIDTH)
                                .setConfirmCallback(tf -> confirm()))
                        .setSpacing(5.0f))
                .setPadding(5.0f)
                .setSizeFromContent();
        
        scrollBox.setScrollBottom();
    }

    public void setCallback(Function<String, String> callback)
    {
        this.callback = callback;
    }
    
    public void setVisible(boolean visible)
    {
        if (visible)
        {
            DUI.show(window.setPosCenterViewport());
            justOpened = true;
        }
        else if (window.isVisible()) DUI.hide(window);
    }
    
    public boolean isVisible()
    {
        return window.isVisible();
    }

    public void toggleVisible()
    {
        setVisible(!isVisible());
    }
    
    public void afterInput()
    {
        if (!isVisible()) return;
        if (justOpened)
        {
            DUI.focus(textField);
            justOpened = false;
        }
    }
    
    public void clear()
    {
        paragraph.clear();
    }
    
    public boolean hasFocus()
    {
        return DUI.getFocusedForm() == textField;
    }
    
    public void key(int key, int action, int mods)
    {
        if (action != GLFW_PRESS && action != GLFW_REPEAT) return;
        
        switch (key)
        {
            case GLFW_KEY_UP:
                if (historyChoice == null) historyChoice = lastCommand;
                else if (historyChoice.prev != null) historyChoice = historyChoice.prev;
                
                if (historyChoice != null) textField.setText(historyChoice.command);
                break;
            case GLFW_KEY_DOWN:
                if (historyChoice == null) break;
                else if (historyChoice.next != null) historyChoice = historyChoice.next;
                
                textField.setText(historyChoice.command);
                break;
        }
    }
    
    private void confirm()
    {
        boolean atBottom = scrollBox.isAtBottom();
        float oldHeight = paragraph.getSize().y;
        
        String command = textField.get();
        if (command.isEmpty()) return;
        
        paragraph.println("> " + command);
        String result = callback != null ? callback.apply(command) : null;
        if (result != null) paragraph.println(result);
        
        textField.clear();
        float newHeight = paragraph.getSize().y;
        if (atBottom) scrollBox.setScrollY(scrollBox.getScrollY() + newHeight - oldHeight);
        
        historyChoice = null;

        if (lastCommand == null || !command.equals(lastCommand.command))
        {
            History hCmd = new History(command);
            if (lastCommand != null) lastCommand.next = hCmd;
            hCmd.prev = lastCommand;
            lastCommand = hCmd;

            if (firstCommand == null) firstCommand = hCmd;

            if (historyLength == MAX_HISTORY_LENGTH)
            {
                History newFirst = firstCommand.next;
                firstCommand = newFirst;
                newFirst.prev = null;
            }
            else historyLength++;
        }
    }
    
    private static class History
    {
        private final String command;
        
        private History prev, next;
        
        private History(String command)
        {
            this.command = command;
        }
    }
}
