package billiard;

public abstract class FramebufferObject {

    public FramebufferAttachment framebufferAttachment;

    public FramebufferObject(FramebufferAttachment framebufferAttachment) {
        this.framebufferAttachment = framebufferAttachment;
    }

}
