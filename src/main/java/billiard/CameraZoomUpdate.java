package billiard;

public class CameraZoomUpdate implements Event {

    public float fov;
    public Camera camera;

    public CameraZoomUpdate(Camera camera) {
        this.fov = camera.fov;
        this.camera = camera;
    }

}
