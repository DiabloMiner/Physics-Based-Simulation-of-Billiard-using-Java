package billiard;

import java.util.Collection;
import java.util.Map;

public class Quad extends Model {

    private final QuadMesh quadMesh;

    public Quad(Collection<Texture2D> textures) {
        super();
        quadMesh = new QuadMesh(textures);
        this.meshes.add(quadMesh);
    }

    public Quad(Collection<Texture2D> textures, boolean hasShadow) {
        super(hasShadow);
        quadMesh = new QuadMesh(textures);
        this.meshes.add(quadMesh);
    }

    @Override
    public void draw(ShaderProgram shaderProgram, Map.Entry<RenderInto, RenderParameters> flags) {
        for (Mesh mesh : meshes) {
            mesh.draw(shaderProgram, flags);
        }
    }

    @Override
    public void destroy() {
        destroyAllMeshes();
    }

}
