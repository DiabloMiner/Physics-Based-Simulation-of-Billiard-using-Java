package billiard;

import org.lwjgl.glfw.GLFW;

import java.util.*;

public class MainIOEngine extends IOEngine implements WindowResizeObserver {

    public Map<Window, Map.Entry<Camera, RenderingEngine>> windowMap;

    public MainIOEngine(Window[] windows, Camera[] cameras, RenderingEngine[] renderingEngines) {
        windowMap = new HashMap<>();
        for (int i = 0; i < windows.length; i++) {
            windowMap.put(windows[i], new AbstractMap.SimpleEntry<>(cameras[i], renderingEngines[i]));
        }
        BilliardEngine.engineInstance.getEventManager().addEventObserver(EventTypes.WindowResizeEvent, this);
    }

    @Override
    public void processInputs(double deltaTime) {
        for (Window window : windowMap.keySet()) {
            if (window.isKeyPressed(GLFW.GLFW_KEY_W)) {
                windowMap.get(window).getKey().moveForwards();
            }
            if (window.isKeyPressed(GLFW.GLFW_KEY_S)) {
                windowMap.get(window).getKey().moveBackwards();
            }
            if (window.isKeyPressed(GLFW.GLFW_KEY_A)) {
                windowMap.get(window).getKey().moveLeft();
            }
            if (window.isKeyPressed(GLFW.GLFW_KEY_D)) {
                windowMap.get(window).getKey().moveRight();
            }

            if (window.isKeyPressed(GLFW.GLFW_KEY_ESCAPE)) {
                window.shouldClose();
                if (windowMap.keySet().stream().allMatch(Window::isClosed)) {
                    BilliardEngine.engineInstance.continueEngineLoop = false;
                }
            }
            if (window.isKeyPressed(GLFW.GLFW_KEY_F11)) {
                window.setFullscreen(!window.isFullscreen());
            }

            windowMap.get(window).getValue().getSkyboxManager().setActive(BilliardEngine.activeSkybox);
        }
    }

    @Override
    public void resize(Window window) {
        windowMap.get(window).getValue().resize();
    }

    @Override
    public void update(Event event) { }

    @Override
    public void update(WindowResizeEvent event) {
        resize(event.window);
    }

    @Override
    public void destroy() {
        for (Window window : windowMap.keySet()) {
            if (!window.isClosed()) {
                window.shouldClose();
            }
            window.destroy();
        }
    }

}
