package com.samrj.devil.gui;

import com.samrj.devil.game.GameWindow;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.math.Vec2i;
import java.util.HashSet;
import java.util.Set;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

/**
 *
 * @author angle
 */
public class MapForm extends Form {
    private final Set<Form> forms = new HashSet<>();
    private boolean panning = false;
    private final Vec2 prevMouse = new Vec2();
    private final Vec2 totalPan = new Vec2();
    private int panButton = GLFW_MOUSE_BUTTON_LEFT;
    private float panBorderSize = 0.1f;
    private float panBorderSpeed = 1;
    private float panBorderLeeway = 0.1f; //Allow panning even when outside the map by this much.
    private long lastClickMillis = 0;
    private int doubleClickTimer = 250;

    public int getPanButton() {
        return panButton;
    }

    public void setPanButton(int panButton) {
        this.panButton = panButton;
    }

    public float getPanBorderSize() {
        return panBorderSize;
    }

    public void setPanBorderSize(float panBorderSize) {
        this.panBorderSize = panBorderSize;
    }

    public float getPanBorderSpeed() {
        return panBorderSpeed;
    }

    public void setPanBorderSpeed(float panBorderSpeed) {
        this.panBorderSpeed = panBorderSpeed;
    }

    public float getPanBorderLeeway() {
        return panBorderLeeway;
    }

    public void setPanBorderLeeway(float panBorderLeeway) {
        this.panBorderLeeway = panBorderLeeway;
    }

    public int getDoubleClickTimer() {
        return doubleClickTimer;
    }

    public void setDoubleClickTimer(int doubleClickTimer) {
        this.doubleClickTimer = doubleClickTimer;
    }
    
    public MapForm add(Form form) {
        forms.add(form);
        return this;
    }
    
    public MapForm add(Form form, Vec2 pos) {
        form.layout(getWindow(), pos.x, pos.y);
        return add(form);
    }

    @Override
    protected void render(DUIDrawer drawer) {
        for (Form form : forms)
            form.render(drawer);
    }

    @Override
    protected boolean activate(int button) {
        if (button != panButton) return false;
        if(!panning) {
            panning = true;
            prevMouse.set(GameWindow.getMouse().getPos());
        }
        return true;
    }

    @Override
    protected void deactivate() {
        panning = false;
        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis - lastClickMillis < doubleClickTimer)
            resetPan();
        
        lastClickMillis = currentTimeMillis;
    }
    
    public void pan(Vec2 pan) {
        for (Form form : forms) {
            form.layout(getWindow(), form.x0 + pan.x, form.y0 + pan.y);
        }
        totalPan.add(pan);
    }
    
    public void resetPan() {
        pan(totalPan.negate());
        totalPan.set();
    }

    @Override
    protected Form hover(float x, float y) {
        
        if (panning) {
            pan(new Vec2(x, y).sub(prevMouse));
            prevMouse.set(x, y);
        } else {
            if (x < this.x0 || x > this.x0 + width || y < this.y0 || y > this.y0 + height) return null;
            else for (Form form : forms) {
                Form result = form.hover(x, y);
                if (result != null) return result;
            }
        }
        return this;
    }
    
    public void borderPanStep(float dt) {
        if (panning)
            return;
        Vec2 mousePos = GameWindow.getMouse().getPos();
        float relativeX = width - mousePos.x;
        float relativeY = height - mousePos.y;
        Vec2 pan = new Vec2();
        if (relativeX > -width * panBorderSize * panBorderLeeway && relativeX < width * panBorderSize)
            pan.x = (mousePos.x - width * (1 - panBorderSize));
        else if (mousePos.x > -width * panBorderSize * panBorderLeeway && mousePos.x < width * panBorderSize)
            pan.x = (mousePos.x - width * panBorderSize);
        if (relativeY > -height * panBorderSize * panBorderLeeway && relativeY < height * panBorderSize)
            pan.y = (mousePos.y - height * (1 - panBorderSize));
        else if (mousePos.y > -height * panBorderSize * panBorderLeeway && mousePos.y < height * panBorderSize)
            pan.y = (mousePos.y - height * panBorderSize);

        pan.div(new Vec2(width * panBorderSize, height * panBorderSize));
        pan.mult(-panBorderSpeed * 300 * dt);
        pan(pan);
    }
    
    public MapForm setSize(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }
    
    public MapForm setSize(Vec2i dimensions) {
        return setSize(dimensions.x, dimensions.y);
    }
}