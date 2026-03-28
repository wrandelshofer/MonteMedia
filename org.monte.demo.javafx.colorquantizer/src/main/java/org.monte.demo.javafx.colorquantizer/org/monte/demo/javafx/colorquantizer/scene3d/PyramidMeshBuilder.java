/*
 * @(#)PyramidMeshBuilder.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.javafx.colorquantizer.scene3d;

import javafx.scene.shape.TriangleMesh;

public class PyramidMeshBuilder {
    public TriangleMesh pyramid(float width, float height, float depth) {
        TriangleMesh mesh = new TriangleMesh();

        // Vertex coordinates
        float h = height;    // Height (Y)
        float w = width;    // Width (X)
        float d = depth;    // Depth (Z)

        mesh.getPoints().addAll(
                0, -h / 2, 0,        // 0
                w / 2, h / 2, d / 2,    // 1
                w / 2, h / 2, -d / 2,   // 2
                -w / 2, h / 2, -d / 2,   // 3
                -w / 2, h / 2, d / 2     // 4
        );
        // Add texture coordinates
        mesh.getTexCoords().addAll(
                0.504f, 0.524f,     // 0
                0.701f, 0,          // 1
                0.126f, 0,          // 2
                0, 0.364f,     // 3
                0, 0.608f,     // 4
                0.165f, 1,          // 5
                0.606f, 1,          // 6
                0.575f, 0.420f,     // 7
                0.575f, 0.643f,     // 8
                0.740f, 0.643f,     // 9
                0.740f, 0.420f      // 10
        );

        mesh.getFaces().addAll(
                0, 0, 3, 5, 2, 6, // Front face
                0, 0, 2, 2, 1, 3, // Right face
                0, 0, 1, 1, 4, 2, // Back face
                0, 0, 4, 4, 3, 5, // Left right face
                2, 9, 3, 8, 4, 7, // Bottom face
                2, 9, 4, 7, 1, 10 // Bottom face
        );
        return mesh;
    }
}
