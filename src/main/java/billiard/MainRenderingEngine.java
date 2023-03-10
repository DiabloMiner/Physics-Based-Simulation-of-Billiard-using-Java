package billiard;

import org.joml.*;
import org.joml.Math;
import org.lwjgl.opengl.GL33;

import java.util.*;

public class MainRenderingEngine extends RenderingEngine {

    private boolean resize;

    private final Camera camera;
    private final Window window;
    private final VecMatUniformBufferBlock matricesUniforms;
    private final SingleFramebufferRenderer mainRenderer;
    private final BlurRenderer blurRenderer;
    private final Framebuffer intermediateFb;
    private final QuadRenderingUnit quadRenderingEngineUnit;

    public MainRenderingEngine(List<Entity> entities, Window window, Camera camera) throws Exception {
        super(entities);
        this.camera = camera;
        this.window = window;
        this.resize = false;

        ShaderProgram shaderProgram = shaderProgramManager.addShaderProgram(new ShaderProgram("VS", "FS"), true, true);
        ShaderProgram lsShaderProgram = shaderProgramManager.addShaderProgram(new ShaderProgram("VS", "FS_LS"), false, false);
        ShaderProgram simpleShaderProgram = shaderProgramManager.addShaderProgram(new ShaderProgram("SVS", "SFS"), false, false);

        lightManager.addDirectionalLight(new DirectionalLight(new Vector3f(-0.7f, 1.0f, 2.9f), new Vector3f(0.0f, 0.0f, 1.0f), 1024));
        lightManager.addPointLight(new PointLight(new Vector3f(0.0f, 5.0f, 0.0f), new Vector3f(50.0f, 38.0f, 0.0f), 1024));
        lightManager.addSpotLight(new CameraUpdatedSpotLight(new Vector3f(camera.position), new Vector3f(camera.direction), new Vector3f(0.8f, 0.0f, 0.0f), 1024, camera));

        RenderingUnit standardRenderingUnit = new StandardRenderingUnit(shaderProgram, Entity.getRenderComponents(renderComponentManager.allEntities));
        RenderingUnit lightRenderingUnit = new LightRenderingUnit(lsShaderProgram, lightManager.allRenderableLights);
        RenderingUnit skyboxRenderingUnit = new SkyboxRenderingUnit(skyboxManager.addSkybox(new Skybox("./src/main/resources/wooden_lounge_4k.hdr", 4000, true)));
        mainRenderer = new SingleFramebufferRenderer(framebufferManager.addFramebuffer(new Framebuffer(new FramebufferTexture2D[] {new FramebufferMSAATexture2D(window.width, window.height, Texture.InternalFormat.RGBA16F, 4, FramebufferAttachment.COLOR_ATTACHMENT0), new FramebufferMSAATexture2D(window.width, window.height, Texture.InternalFormat.RGBA16F, 4, FramebufferAttachment.COLOR_ATTACHMENT1)},
                new FramebufferRenderbuffer[] {new FramebufferMSAARenderbuffer(window.width, window.height, Renderbuffer.InternalFormat.DEPTH24_STENCIL8, 4, FramebufferAttachment.DEPTH_AND_STENCIL_ATTACHMENT)})),
                new RenderingUnit[] {standardRenderingUnit, lightRenderingUnit, skyboxRenderingUnit});
        intermediateFb = framebufferManager.addFramebuffer(new Framebuffer(new FramebufferTexture2D[] {new FramebufferTexture2D(window.width, window.height, Texture.InternalFormat.RGBA16F, Texture.Format.RGBA, Texture.Type.FLOAT, FramebufferAttachment.COLOR_ATTACHMENT0), new FramebufferTexture2D(window.width, window.height, Texture.InternalFormat.RGBA16F, Texture.Format.RGBA, Texture.Type.FLOAT, FramebufferAttachment.COLOR_ATTACHMENT1)}));
        blurRenderer = new BlurRenderer(window.width, window.height, Texture.InternalFormat.RGBA16F, Texture.Format.RGBA, Texture.Type.FLOAT, 10, intermediateFb.getAttached2DTexture(FramebufferAttachment.COLOR_ATTACHMENT1).storedTexture);
        quadRenderingEngineUnit = new QuadRenderingUnit(simpleShaderProgram, new ArrayList<>(Arrays.asList(intermediateFb.getAttached2DTexture(FramebufferAttachment.COLOR_ATTACHMENT0).storedTexture, blurRenderer.getFinalFramebuffer().getAttached2DTexture(FramebufferAttachment.COLOR_ATTACHMENT0).storedTexture)));
        framebufferManager.addFramebuffers(blurRenderer.getFramebuffers());

        matricesUniforms = new VecMatUniformBufferBlock(1, 2, Buffer.Usage.DYNAMIC_DRAW, "Matrices");
        shaderProgram.setUniformBlockBindings(new UniformBufferBlock[]{matricesUniforms});
        lsShaderProgram.setUniformBlockBindings(new UniformBufferBlock[]{matricesUniforms});

        lightManager.createShadowRenderers(renderComponentManager.allRenderComponentsThrowingShadows.toArray(new RenderComponent[0]));

        GL33.glEnable(GL33.GL_DEPTH_TEST);
        GL33.glEnable(GL33.GL_STENCIL_TEST);
        GL33.glDepthFunc(GL33.GL_LESS);
        GL33.glCullFace(GL33.GL_BACK);
    }

    public void update() {
        Matrix4f projection = new Matrix4f().identity();
        projection.perspective(Math.toRadians(camera.fov), (float) window.width / (float) window.height, camera.near, camera.far);

        matricesUniforms.setElements(new ArrayList<>(Arrays.asList(new Vector4FElement(camera.position), new Matrix4FElement(camera.getViewMatrix()), new Matrix4FElement(projection))));
        matricesUniforms.setUniformBlockData();
        lightManager.setLightUniforms(shaderProgramManager.allShaderProgramsUsingShadows);
        skyboxManager.setSkyboxUniforms(shaderProgramManager.allShaderProgramsUsingSkyboxes);

        mainRenderer.updateAllRenderingEngineUnits();
    }

    @Override
    public void render() {
        lightManager.renderShadowMaps();

        mainRenderer.render(RenderInto.COLOR_DEPTH);

        Framebuffer.blitFrameBuffers(mainRenderer.getFramebuffer(), intermediateFb, new FramebufferAttachment[]{FramebufferAttachment.COLOR_ATTACHMENT0, FramebufferAttachment.COLOR_ATTACHMENT1});

        // Blur disappears at first fullscreen
        // Texture used at beginning is false/from old frame
        blurRenderer.render(RenderInto.COLOR_ONLY);

        // The normal framebuffer is too large at the third window size
        Framebuffer.getStandardFramebuffer().bind();
        GL33.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT);
        GL33.glEnable(GL33.GL_FRAMEBUFFER_SRGB);
        if (resize) { GL33.glViewport(0, 0, window.width, window.height); resize = false;}

        // Most likely the source of the fullscreen problem
        quadRenderingEngineUnit.render(new AbstractMap.SimpleEntry<>(RenderInto.COLOR_ONLY, RenderParameters.COLOR_ENABLED));

        GL33.glDisable(GL33.GL_FRAMEBUFFER_SRGB);
        window.swapBuffers();
    }

    @Override
    public void resize() {
        framebufferManager.resize(window.width, window.height);
        resize = true;
    }

    public void destroy() {
        destroyAllManagers();
        matricesUniforms.destroy();
        mainRenderer.destroy();
        blurRenderer.destroy();
        intermediateFb.destroy();
        quadRenderingEngineUnit.destroy();
    }

    public Camera getCamera() {
        return camera;
    }

    public LightManager getLightManager() {
        return lightManager;
    }

}
