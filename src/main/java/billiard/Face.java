package billiard;

import org.joml.*;

import java.lang.Math;
import java.util.*;

public class Face {

    public static final float epsilon = Math.ulp(1.0f);

    private final List<Vector3f> definingVertices = new ArrayList<>();
    private final List<Vector3f> originalVertices = new ArrayList<>();
    private final List<Vector3f> conflictList = new ArrayList<>();
    private final List<Face> neighbouringFaces = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();

    private final Vector3f supportVector;
    private final Vector3f normalizedNormal;
    private float offset;

    private final Vector3f centroid;
    private float area;

    private final boolean normalFromOrigin;


    /**
     * This face's normal is constructed from the origin automatically. For reference see: {@link #Face(Vector3f, Vector3f, Vector3f, boolean) Face}
     * @param edge Provides first and second vertex with top and tail
     * @param vertex3 Third vertex
     */
    public Face(Edge edge, Vector3f vertex3) {
        this(edge.getTop(), edge.getTail(), vertex3, true);
    }

    /**
     * @param vertex1 First vertex
     * @param vertex2 Second vertex
     * @param vertex3 Third vertex
     * @param normalFromOrigin Determines if normal is constructed according to the <a href="https://en.wikipedia.org/wiki/Hesse_normal_form">Hesse normal form</a> which means the normal will point away from the origin of the coordinate system
     */
    public Face(Vector3f vertex1, Vector3f vertex2, Vector3f vertex3, boolean normalFromOrigin) {
        this.normalFromOrigin = normalFromOrigin;

        this.definingVertices.add(new Vector3f(vertex1));
        this.definingVertices.add(new Vector3f(vertex2));
        this.definingVertices.add(new Vector3f(vertex3));
        this.originalVertices.add(new Vector3f(vertex1));
        this.originalVertices.add(new Vector3f(vertex2));
        this.originalVertices.add(new Vector3f(vertex3));

        edges.add(new Edge(vertex1, vertex2, this));
        edges.add(new Edge(vertex1, vertex3, this));
        edges.add(new Edge(vertex2, vertex3, this));

        supportVector = vertex2;
        Vector3f normal = new Vector3f(new Vector3f(vertex2).sub(vertex1)).cross(new Vector3f(vertex3).sub(vertex1));
        if (normalFromOrigin) {
            if (normal.dot(supportVector) >= 0) {
                normalizedNormal = normal.normalize();
            } else {
                normalizedNormal = normal.normalize().mul(-1.0f);
            }
        } else {
            normalizedNormal = normal.normalize();
        }
        offset = normalizedNormal.dot(supportVector);

        centroid = determineCentroid();
        area = determineArea();
    }

    public float signedDistance(Vector3f point) {
        return (point.dot(normalizedNormal) - offset);
    }

    public float signedDistancePlane(Vector3f normalVector, float distance) {
        return new Vector3f(centroid).dot(normalVector) - distance;
    }

    public Vector3f getFurthestPointFromConflictList() {
        Vector3f result = new Vector3f(supportVector);
        for (Vector3f vertex : conflictList) {
            if (signedDistance(vertex) > signedDistance(result)) {
                result = vertex;
            }
        }
        return result;
    }

    public boolean isConflictListEmpty() {
        return conflictList.isEmpty();
    }

    public boolean hasEdge(Edge edgeToBeTested) {
        for (Edge edge : edges) {
            if (edge.isOverlapping(edgeToBeTested).equals(Edge.OverlappingType.Identical)) {
                return true;
            }
        }
        return false;
    }

    public void addNewConflictVertex(Vector3f vertex) {
        conflictList.add(vertex);
    }

    public void addNewConflictVertices(Collection<Vector3f> vertices) {
        conflictList.addAll(vertices);
    }

    public void addNewNeighbouringFace(Face face) {
        if (!neighbouringFaces.contains(face) && !face.equals(this)) {
            boolean isPoint0ADefiningPoint = definingVertices.contains(face.definingVertices.get(0));
            boolean isPoint1ADefiningPoint = definingVertices.contains(face.definingVertices.get(1));
            boolean isPoint2ADefiningPoint = definingVertices.contains(face.definingVertices.get(2));
            if ((isPoint0ADefiningPoint && isPoint1ADefiningPoint) || (isPoint0ADefiningPoint && isPoint2ADefiningPoint) || (isPoint1ADefiningPoint && isPoint2ADefiningPoint)) {
                neighbouringFaces.add(face);
            }
        }
    }

    private Vector3f determineCentroid() {
        Vector3f result = new Vector3f(0.0f);
        result.add(definingVertices.get(0)).add(definingVertices.get(1)).add(definingVertices.get(2));
        result.div(3.0f);
        return result;
    }

    private float determineArea() {
        float a = new Vector3f(definingVertices.get(1)).sub(definingVertices.get(2)).length();
        float heightOnA = new Vector3f(definingVertices.get(0)).sub(definingVertices.get(1)).
                cross(new Vector3f(definingVertices.get(0)).sub(definingVertices.get(2))).length() /
                new Vector3f(definingVertices.get(2)).sub(definingVertices.get(1)).length();
        return (a * heightOnA) / 2.0f;
    }

    private void updateVertices(Vector3f v0, Vector3f v1, Vector3f v2) {
        Vector3f temp0 = new Vector3f(v0);
        Vector3f temp1 = new Vector3f(v1);
        Vector3f temp2 = new Vector3f(v2);
        this.definingVertices.get(0).set(temp0);
        this.definingVertices.get(1).set(temp1);
        this.definingVertices.get(2).set(temp2);
    }

    private void update() {
        this.supportVector.set(this.definingVertices.get(1));
        Vector3f normal = new Vector3f(new Vector3f(definingVertices.get(1)).sub(definingVertices.get(0))).cross(new Vector3f(definingVertices.get(2)).sub(definingVertices.get(0)));
        if (normalFromOrigin) {
            if (normal.dot(supportVector) >= 0) {
                normalizedNormal.set(normal.normalize());
            } else {
                normalizedNormal.set(normal.normalize().mul(-1.0f));
            }
        } else {
            normalizedNormal.set(normal.normalize());
        }
        offset = normalizedNormal.dot(supportVector);
        centroid.set(determineCentroid());
        area = determineArea();
    }

    public void update(Matrix4f worldMatrix) {
        this.definingVertices.get(0).set(Transforms.mulVectorWithMatrix4(this.originalVertices.get(0), worldMatrix));
        this.definingVertices.get(1).set(Transforms.mulVectorWithMatrix4(this.originalVertices.get(1), worldMatrix));
        this.definingVertices.get(2).set(Transforms.mulVectorWithMatrix4(this.originalVertices.get(2), worldMatrix));

        update();

        for (Edge edge : edges) {
            edge.update(worldMatrix);
        }
    }

    private double calculateDeterminant(Vector3f a, Vector3f b, Vector3f c, Vector3f d) {
        return new Vector3d(d).sub(a).dot(new Vector3d(b).sub(a).cross(new Vector3d(c).sub(a)));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Face face = (Face) o;
        return Float.compare(face.offset, offset) == 0 && definingVertices.equals(face.definingVertices) && supportVector.equals(face.supportVector) && normalizedNormal.equals(face.normalizedNormal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(definingVertices, supportVector, normalizedNormal, offset);
    }

    public Vector3f getNormalizedNormal() {
        return normalizedNormal;
    }

    public List<Vector3f> getConflictList() {
        return conflictList;
    }

    public List<Vector3f> getDefiningVertices() {
        return definingVertices;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public Vector3f getCentroid() {
        return centroid;
    }

    public float getArea() {
        return area;
    }
}
