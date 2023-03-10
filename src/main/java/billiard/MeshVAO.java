package billiard;

import org.lwjgl.opengl.GL33;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class MeshVAO extends VAO {

    private final List<VertexBufferObject> attachedVertexBuffers;
    private ElementBufferObject attachedElementBuffer;

        public MeshVAO(List<float[]> floatData, List<Integer> sizeData, int[] intData, Buffer.Usage usage) {
        super();
        attachedVertexBuffers = new ArrayList<>();

        bind();
        for (int i = 0; i < floatData.size(); i++) {
            createAttachedBuffer(floatData.get(i), usage, i, sizeData.get(i));
        }
        VertexBufferObject.unbind();
        createAttachedBuffer(intData, usage);
        enableVertexAttribPointers();
        unbind();
    }

    private void createAttachedBuffer(float[] floatData, Buffer.Usage usage, int index, int size) {
        FloatBuffer floatBuffer = BufferUtil.createBuffer(floatData);
        VertexBufferObject bufferObject = new VertexBufferObject();
        bufferObject.fill(floatBuffer, usage);

        GL33.glVertexAttribPointer(index, size, GL33.GL_FLOAT, false, size * Float.BYTES, 0);
        this.attachedVertexBuffers.add(bufferObject);

        BufferUtil.destroyBuffer(floatBuffer);
    }

    private void createAttachedBuffer(int[] intData, Buffer.Usage usage) {
        IntBuffer intBuffer = BufferUtil.createBuffer(intData);
        attachedElementBuffer = new ElementBufferObject(intBuffer, usage);
    }

    public void enableVertexAttribPointers() {
        for (int i = 0; i < attachedVertexBuffers.size(); i++) {
            GL33.glEnableVertexAttribArray(i);
        }
    }

    public void disableVertexAttribPointers() {
        for (int i = 0; i < attachedVertexBuffers.size(); i++) {
            GL33.glDisableVertexAttribArray(i);
        }
    }

    public void draw() {
        bind();

        GL33.glDrawElements(GL33.GL_TRIANGLES, attachedElementBuffer.getIntBuffer());

        unbind();
    }

    public void destroy() {
        bind();
        disableVertexAttribPointers();
        unbind();

        destroyVAO();
        for (VertexBufferObject bufferObject : attachedVertexBuffers) {
            bufferObject.destroy();
        }
        attachedElementBuffer.destroy();
    }

}
