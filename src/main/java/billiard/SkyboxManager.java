package billiard;

import java.util.ArrayList;
import java.util.List;

public class SkyboxManager implements Manager {

    public List<Skybox> skyboxes;

    private boolean active;

    public SkyboxManager() {
        skyboxes = new ArrayList<>();
        active = true;
    }

    public Skybox addSkybox(Skybox skybox) {
        skyboxes.add(skybox);
        return skybox;
    }

    public void setSkyboxUniforms(List<ShaderProgram> shaderPrograms) {
        if (active) {
            if (RenderingEngine.blackTexture.isBound()) {
                RenderingEngine.blackTexture.unbind();
            }
            for (ShaderProgram shaderProgram : shaderPrograms) {
                for (int i = 0; i < skyboxes.size(); i++) {
                    skyboxes.get(i).setActive(active);
                    shaderProgram.bind();
                    shaderProgram.setUniform1IBindless("irradianceMap" + i, skyboxes.get(i).getConvolutedTextureIndex());
                    shaderProgram.setUniform1IBindless("prefilteredMap" + i, skyboxes.get(i).getPrefilteredTextureIndex());
                    shaderProgram.setUniform1IBindless("brdfLUT" + i, Skybox.getBrdfLookUpTextureIndex());
                    ShaderProgram.unbind();
                }
            }
        } else {
            if (!RenderingEngine.blackTexture.isBound()) {
                RenderingEngine.blackTexture.bind();
            }
            for (ShaderProgram shaderProgram : shaderPrograms) {
                for (int i = 0; i < skyboxes.size(); i++) {
                    skyboxes.get(i).setActive(active);
                    shaderProgram.bind();
                    shaderProgram.setUniform1IBindless("irradianceMap" + i, RenderingEngine.blackTexture.getIndex());
                    shaderProgram.setUniform1IBindless("prefilteredMap" + i, RenderingEngine.blackTexture.getIndex());
                    shaderProgram.setUniform1IBindless("brdfLUT" + i, RenderingEngine.blackTexture.getIndex());
                    ShaderProgram.unbind();
                }
            }
        }
    }

    public void destroyAllSkyboxes() {
        for (Skybox skybox : skyboxes) {
            skybox.destroy();
        }
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

}
