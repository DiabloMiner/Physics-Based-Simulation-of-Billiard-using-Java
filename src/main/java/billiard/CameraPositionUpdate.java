package billiard;

import org.joml.Vector3f;

public class CameraPositionUpdate implements Event {

    public Vector3f position;
    public Camera camera;

    public CameraPositionUpdate(Camera camera) {
        this.position = camera.position;
        this.camera = camera;
    }
}
