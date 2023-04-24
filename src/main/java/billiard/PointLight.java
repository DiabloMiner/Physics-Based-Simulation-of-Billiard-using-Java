package billiard;

import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.*;

public class PointLight extends Light {

    private static ShaderProgram shadowShader;
    public static float near = 0.001f, far = 30.0f;
    public static Vector3f rotationCenter = new Vector3f(0.0f);

    public Vector3f position;
    private final Framebuffer shadowFramebuffer;
    private final float aspect;
    private final FramebufferCubeMap shadowTexture;
    private float angle, radius;

    public PointLight(Vector3f position, Vector3f color, int shadowSize) {
        super(color);
        this.position = position;
        this.aspect = (float) shadowSize / (float) shadowSize;
        this.radius = position.distance(rotationCenter.x, position.y, rotationCenter.z);
        this.angle = Math.acos(position.x / radius);

        shadowTexture = new FramebufferCubeMap(shadowSize, shadowSize, Texture.InternalFormat.DEPTH, Texture.Format.DEPTH, Texture.Type.FLOAT, FramebufferAttachment.DEPTH_ATTACHMENT);
        shadowFramebuffer = new Framebuffer(shadowTexture);
        shadowTexture.bind();
    }

    @Override
    void updateShadowMatrices() {
        Matrix4f projection = new Matrix4f().identity().perspective((float) Math.toRadians(90.0), aspect, near, far);
        Matrix4f[] viewMatrices = {
                new Matrix4f().identity().lookAt(position, new Vector3f(position).add(1.0f, 0.0f, 0.0f), new Vector3f(0.0f, -1.0f, 0.0f)),
                new Matrix4f().identity().lookAt(position, new Vector3f(position).add(-1.0f, 0.0f, 0.0f), new Vector3f(0.0f, -1.0f, 0.0f)),
                new Matrix4f().identity().lookAt(position, new Vector3f(position).add(0.0f, 1.0f, 0.0f), new Vector3f(0.0f, 0.0f, 1.0f)),
                new Matrix4f().identity().lookAt(position, new Vector3f(position).add(0.0f, -1.0f, 0.0f), new Vector3f(0.0f, .0f, -1.0f)),
                new Matrix4f().identity().lookAt(position, new Vector3f(position).add(0.0f, 0.0f, 1.0f), new Vector3f(0.0f, -1.0f, 0.0f)),
                new Matrix4f().identity().lookAt(position, new Vector3f(position).add(0.0f, 0.0f, -1.0f), new Vector3f(0.0f, -1.0f, 0.0f))
        };
        List<Matrix4f> lightSpaceMatrices = new ArrayList<>();
        Arrays.stream(viewMatrices).forEach(mat -> lightSpaceMatrices.add(new Matrix4f(projection).mul(mat)));
        this.lightSpaceMatrices = lightSpaceMatrices.toArray(new Matrix4f[0]);
    }

    @Override
    public void setUniformData(ShaderProgram shaderProgram, int index) {
        shaderProgram.setUniformVec3FBindless("pointLight" + index + ".position", position);
        shaderProgram.setUniformVec3FBindless("pointLight" + index + ".color", color);

        if (!shadowTexture.storedTexture.isBound()) {
            shadowTexture.storedTexture.bind();
        }
        shaderProgram.setUniform1IBindless("pointLight" + index + ".shadowMap", shadowTexture.storedTexture.getIndex());
        shaderProgram.setUniform1FBindless("pointLight" + index + ".far", far);
    }

    @Override
    public void initializeShadowRenderer(RenderComponent[] renderComponents) {
        shadowRenderer = new SingleFramebufferRenderer(shadowFramebuffer, new RenderingUnit[] {new ShadowRenderingUnit(getShadowShader(), renderComponents, this)});
    }


    public void rotate(double deltaAngle) {
        angle += deltaAngle;
        float clampedAngle = angle;
        while (true) {
            if (clampedAngle > 360.0f) {
                clampedAngle -= 360.0f;
            } else {
                break;
            }
        }

        position.setComponent(0, Math.cos(this.angle) * radius);
        position.setComponent(2, Math.sin(this.angle) * radius);
        /*if (clampedAngle <= 90.0f) {
            position.setComponent(0, Math.cos(this.angle) * radius);
            position.setComponent(2, Math.sin(this.angle) * radius);
        } else if (clampedAngle <= 180.0f) {
            float angle = 180.0f - this.angle;
            position.setComponent(0, Math.cos(angle) * radius);
            position.setComponent(2, Math.sin(angle) * radius);
        } else if (clampedAngle <= 270.0f) {
            float angle = 270.0f - this.angle;
            position.setComponent(0, Math.sin(angle) * radius);
            position.setComponent(2, Math.cos(angle) * radius);
        } else {
            float angle = 360.0f - this.angle;
            position.setComponent(0, Math.cos(angle) * radius);
            position.setComponent(2, Math.sin(angle) * radius);
        }*/
    }

    public static ShaderProgram getShadowShader() {
        if (shadowShader == null) {
            try {
                shadowShader = new ShaderProgram("OmniDirShadowVS", "OmniDirShadowGS", "OmniDirShadowFS");
                return shadowShader;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            return shadowShader;
        }
    }

}
