package billiard;

public class KeyPressEvent implements Event {

    float factor;
    int pressedKey;

    public KeyPressEvent(float factor, int pressedKey) {
        this.factor = factor;
        this.pressedKey = pressedKey;
    }

}
