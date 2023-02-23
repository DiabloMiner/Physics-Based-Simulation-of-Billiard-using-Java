package billiard;

public enum EventTypes {

    Event(Event.class),
    CameraDirectionUpdate(billiard.CameraDirectionUpdate.class),
    CameraPositionUpdate(billiard.CameraPositionUpdate.class),
    CameraZoomUpdate(billiard.CameraZoomUpdate.class),
    KeyPressEvent(billiard.KeyPressEvent.class),
    WindowResizeEvent(billiard.WindowResizeEvent.class);

    public final Class<?> classValue;

    EventTypes(Class<?> classValue) {
        this.classValue = classValue;
    }

}
