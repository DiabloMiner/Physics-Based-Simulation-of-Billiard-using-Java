package billiard;

public interface CameraZoomObserver extends EventObserver {

    void update(CameraDirectionUpdate event);

}
