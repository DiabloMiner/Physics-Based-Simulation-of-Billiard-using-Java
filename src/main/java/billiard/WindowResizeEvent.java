package billiard;

public class WindowResizeEvent implements Event {

    public Window window;

    public WindowResizeEvent(Window window) {
        this.window = window;
    }

}
