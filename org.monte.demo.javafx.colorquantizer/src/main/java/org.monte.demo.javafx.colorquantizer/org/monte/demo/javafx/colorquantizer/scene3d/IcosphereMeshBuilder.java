/*
 * @(#)IcosphereMeshBuilder.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.javafx.colorquantizer.scene3d;

import javafx.collections.ObservableFloatArray;
import javafx.geometry.Point3D;
import javafx.scene.shape.ObservableFaceArray;
import javafx.scene.shape.TriangleMesh;

import static java.lang.Math.sqrt;

/// References:
/// [Daniel Sieger](https://danielsieger.com/blog/2021/01/03/generating-platonic-solids.html)
public class IcosphereMeshBuilder {
    public TriangleMesh icosahedron(double radius) {
        TriangleMesh mesh = new TriangleMesh();

        float phi = (float) ((1.0f + sqrt(5.0f)) * 0.5f); // golden ratio
        float a = 1.0f;
        float b = 1.0f / phi;

        // add vertices
        ObservableFloatArray points = mesh.getPoints();
        points.addAll(0, b, -a);
        points.addAll(b, a, 0);
        points.addAll(-b, a, 0);
        points.addAll(0, b, a);
        points.addAll(0, -b, a);
        points.addAll(-a, 0, b);
        points.addAll(0, -b, -a);
        points.addAll(a, 0, -b);
        points.addAll(a, 0, b);
        points.addAll(-a, 0, -b);
        points.addAll(b, -a, 0);
        points.addAll(-b, -a, 0);
        project_to_unit_sphere(mesh, radius);

        // add texture coordinates
        ObservableFloatArray tex = mesh.getTexCoords();
        tex.addAll(0, 0, 0, 1, 1, 1);
        tex.addAll(1, 1, 1, 0, 0, 0);


        // add triangles
        ObservableFaceArray faces = mesh.getFaces();
        faces.addAll(2, 0, 1, 1, 0, 2);
        faces.addAll(1, 0, 2, 1, 3, 2);
        faces.addAll(5, 0, 4, 1, 3, 2);
        faces.addAll(4, 0, 8, 1, 3, 2);
        faces.addAll(7, 0, 6, 1, 0, 2);
        faces.addAll(6, 0, 9, 1, 0, 2);
        faces.addAll(11, 0, 10, 1, 4, 2);
        faces.addAll(10, 0, 11, 1, 6, 2);
        faces.addAll(9, 0, 5, 1, 2, 2);
        faces.addAll(5, 0, 9, 1, 11, 2);
        faces.addAll(8, 0, 7, 1, 1, 2);
        faces.addAll(7, 0, 8, 1, 10, 2);
        faces.addAll(2, 0, 5, 1, 3, 2);
        faces.addAll(8, 0, 1, 1, 3, 2);
        faces.addAll(9, 0, 2, 1, 0, 2);
        faces.addAll(1, 0, 7, 1, 0, 2);
        faces.addAll(11, 0, 9, 1, 6, 2);
        faces.addAll(7, 0, 10, 1, 6, 2);
        faces.addAll(5, 0, 11, 1, 4, 2);
        faces.addAll(10, 0, 8, 1, 4, 2);

        return mesh;
    }

    void project_to_unit_sphere(TriangleMesh mesh, double radius) {
        var points = mesh.getPoints();
        for (int i = 0, n = points.size(); i < n; i += 3) {
            var p = new Point3D(points.get(i), points.get(i + 1), points.get(i + 2));
            double m = radius / p.magnitude();
            points.set(i, (float) (p.getX() * m));
            points.set(i + 1, (float) (p.getY() * m));
            points.set(i + 2, (float) (p.getZ() * m));
        }
    }
}
