package billiard;

import java.util.*;

public abstract class RenderingUnit {

    protected ShaderProgram shaderProgram;
    protected final List<RenderComponent> renderComponents;

    public RenderingUnit(ShaderProgram shaderProgram) {
        this.shaderProgram = shaderProgram;
        renderComponents = new ArrayList<>();
    }

    public RenderingUnit(ShaderProgram shaderProgram, RenderComponent[] renderComponents) {
        this.shaderProgram = shaderProgram;
        this.renderComponents = new ArrayList<>(Arrays.asList(renderComponents));
    }

    public abstract void update();

    public abstract void update(ShaderProgram shaderProgram);

    public abstract void render(Map.Entry<RenderInto, RenderParameters> flags);

    public abstract void render(ShaderProgram shaderProgram, Map.Entry<RenderInto, RenderParameters> flags);

    public abstract void destroy();

    public void renderRenderables(Map.Entry<RenderInto, RenderParameters> flags) {
        for (RenderComponent renderComponent : renderComponents) {
            renderComponent.draw(shaderProgram, flags);
        }
    }

    public void renderRenderables(ShaderProgram shaderProgram, Map.Entry<RenderInto, RenderParameters> flags) {
        ShaderProgram temporaryShaderProgram = this.shaderProgram;
        this.shaderProgram = shaderProgram;
        renderRenderables(flags);
        this.shaderProgram = temporaryShaderProgram;
    }

    public Set<RenderComponent> containsRenderables(Set<RenderComponent> renderComponents) {
        Set<RenderComponent> result = new HashSet<>();
        for (RenderComponent renderComponent : renderComponents) {
            if (this.renderComponents.contains(renderComponent)) {
                result.add(renderComponent);
            }
        }
        return result;
    }

    public void addNewRenderable(RenderComponent renderComponent) {
        renderComponents.add(renderComponent);
    }

    public void addNewRenderables(List<RenderComponent> renderComponents) {
        this.renderComponents.addAll(renderComponents);
    }

    public List<RenderComponent> getRenderables() {
        return renderComponents;
    }

    public void setNewShaderProgram(ShaderProgram shaderProgram) {
        this.shaderProgram = shaderProgram;
    }

    public ShaderProgram getShaderProgram() {
        return shaderProgram;
    }

    public void destroyRenderables() {
        for (RenderComponent renderComponent : renderComponents) {
            renderComponent.destroy();
        }
    }

    public void destroyShaderProgram() {
        shaderProgram.destroy();
    }

}
