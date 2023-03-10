package billiard;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class SpotLight extends Light {

    private static ShaderProgram shadowShader;
    public static float near = 0.0001f, far = 30.0f;

    public Vector3f position, direction;
    protected final Framebuffer shadowFramebuffer;
    protected final FramebufferTexture2D shadowTexture;

    public SpotLight(Vector3f position, Vector3f direction, Vector3f color, int shadowSize) {
        super(color);
        this.position = position;
        this.direction = direction;

        shadowTexture = new FramebufferTexture2D(shadowSize, shadowSize, Texture.InternalFormat.DEPTH, Texture.Format.DEPTH, Texture.Type.FLOAT, BufferUtil.createBuffer(new Vector4f(1.0f)), FramebufferAttachment.DEPTH_ATTACHMENT);
        shadowFramebuffer = new Framebuffer(shadowTexture);
        shadowTexture.bind();
    }

    @Override
    void updateShadowMatrices() {
        Matrix4f projection = new Matrix4f().identity().ortho(-15.0f, 15.0f, -15.0f, 15.0f, near, far);
        Matrix4f view = new Matrix4f().identity().lookAt(new Vector3f(position), new Vector3f(position).add(direction), new Vector3f(0.0f, 1.0f, 0.0f));
        lightSpaceMatrices = new Matrix4f[] {new Matrix4f(projection).mul(view)};
    }

    @Override
    public void setUniformData(ShaderProgram shaderProgram, int index) {
        shaderProgram.setUniformVec3FBindless("spotLight" + index + ".position", position);
        shaderProgram.setUniformVec3FBindless("spotLight" + index + ".direction", direction);
        shaderProgram.setUniformVec3FBindless("spotLight" + index + ".color", color);
        shaderProgram.setUniformMat4FBindless("spotLight" + index + "Matrix", lightSpaceMatrices[0]);

        if (!shadowTexture.storedTexture.isBound()) {
            shadowTexture.storedTexture.bind();
        }
        shaderProgram.setUniform1IBindless("spotLight" + index + ".shadowMap", shadowTexture.storedTexture.getIndex());
    }

    @Override
    public void initializeShadowRenderer(RenderComponent[] renderComponents) {
        shadowRenderer = new SingleFramebufferRenderer(shadowFramebuffer, new RenderingUnit[] {new ShadowRenderingUnit(getShadowShader(), renderComponents, this)});
    }

    public static ShaderProgram getShadowShader() {
        if (shadowShader == null) {
            try {
                shadowShader = new ShaderProgram("DirShadowVS", "DirShadowFS");
                return shadowShader;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            return shadowShader;
        }
    }

}
