package billiard;

public abstract class RenderableLight extends RenderComponent {

    public Light light;

    public RenderableLight(Light light) {
        super();
        this.light = light;
    }

}
