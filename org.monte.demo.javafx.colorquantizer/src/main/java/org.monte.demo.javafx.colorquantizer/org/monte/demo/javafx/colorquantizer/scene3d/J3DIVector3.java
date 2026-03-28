/*
 * @(#)J3DIVector3.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.javafx.colorquantizer.scene3d;

public final class J3DIVector3 {
    public double x, y, z;

    public J3DIVector3() {
        x = y = z = 0;
    }

    public J3DIVector3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public J3DIVector3(double[] v) {
        x = v[0];
        this.y = v[1];
        this.z = v[2];
    }

    public J3DIVector3(J3DIVector3 v) {
        x = v.x;
        this.y = v.y;
        this.z = v.z;
    }

    public double t(int index) {
        return switch (index) {
            case 0 -> x;
            case 1 -> y;
            case 2 -> z;
            default -> 0;
        };
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double z() {
        return z;
    }

    public void load() {
        x = y = z = 0;
    }

    public void load(double x, double y, double z) {
        x = x;
        y = y;
        z = z;
    }

    public void load(J3DIVector3 v) {
        x = v.x;
        y = v.y;
        z = v.z;
    }

    public void load(double[] v) {
        x = v[0];
        this.y = v[1];
        this.z = v[2];
    }


    public double vectorLength() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public double norm() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public void cross(J3DIVector3 v) {
        double t0 = x, t1 = y, t2 = z;
        x = t1 * v.z - t2 * v.y;
        y = -t0 * v.z + t2 * v.x;
        z = t0 * v.y - t1 * v.x;
    }

    public double dot(J3DIVector3 v) {
        return x * v.x + y * v.y + z * v.z;
    }

    public void combine(J3DIVector3 v, double ascl, double bscl) {
        x = (ascl * x) + (bscl * v.x);
        y = (ascl * y) + (bscl * v.y);
        z = (ascl * z) + (bscl * v.z);
    }

    public J3DIVector3 multiply(double v) {
        x *= v;
        y *= v;
        z *= v;
        return this;
    }

    public J3DIVector3 multipyl(J3DIVector3 v) {
        x *= v.x;
        y *= v.y;
        z *= v.z;
        return this;
    }

    public J3DIVector3 divide(double v) {
        x /= v;
        y /= v;
        z /= v;
        return this;
    }

    public J3DIVector3 divide(J3DIVector3 v) {
        x /= v.x;
        y /= v.y;
        z /= v.z;
        return this;
    }

    public J3DIVector3 subtract(double v) {
        x -= v;
        y -= v;
        z -= v;
        return this;
    }

    public J3DIVector3 subtract(J3DIVector3 v) {
        x -= v.x;
        y -= v.y;
        z -= v.z;
        return this;
    }

    public J3DIVector3 add(double v) {
        x += v;
        y += v;
        z += v;
        return this;
    }

    public J3DIVector3 add(J3DIVector3 v) {
        x += v.x;
        y += v.y;
        z += v.z;
        return this;
    }

    public J3DIVector3 neg() {
        x = -x;
        y = -y;
        z = -z;
        return this;
    }

    public J3DIVector3 normalize() {
        var l = this.vectorLength();
        x /= l;
        y /= l;
        z /= l;
        return this;
    }

    public J3DIVector3 reflect(double n) {
        var l = new J3DIVector3(this);
        this.multiply(n);
        this.multiply(2);
        this.multiply(n);
        this.subtract(l);
        return this;
    }

    public J3DIVector3 multVecMatrix(J3DIMatrix4 matrix) {
        var x = this.x;
        var y = this.y;
        var z = this.z;
        x = matrix.m41 + x * matrix.m11 + y * matrix.m21 + z * matrix.m31;
        y = matrix.m42 + x * matrix.m12 + y * matrix.m22 + z * matrix.m32;
        z = matrix.m43 + x * matrix.m13 + y * matrix.m23 + z * matrix.m33;
        var w = matrix.m44 + x * matrix.m14 + y * matrix.m24 + z * matrix.m34;
        if (w != 1 && w != 0) {
            this.x /= w;
            this.y /= w;
            this.z /= w;
        }
        return this;
    }

    public J3DIVector3 multNormalMatrix(J3DIMatrix4 matrix) {
        var x = this.x;
        var y = this.y;
        var z = this.z;
        var S = new J3DIMatrix4(matrix);
        S.invert();
        S.transpose();
        x = S.m41 + x * S.m11 + y * S.m21 + z * S.m31;
        y = S.m42 + x * S.m12 + y * S.m22 + z * S.m32;
        z = S.m43 + x * S.m13 + y * S.m23 + z * S.m33;
        var w = S.m44 + x * S.m14 + y * S.m24 + z * S.m34;
        if (w != 1 && w != 0) {
            this.x /= w;
            this.y /= w;
            this.z /= w;
        }
        return this;
    }

    public J3DIMatrix4 hat() {
        return new J3DIMatrix4(
                0, -z, y, 0,
                z, 0, -x, 0,
                -y, x, 0, 0,
                0, 0, 0, 1
        );
    }

    public double get(int index) {
        return switch (index) {
            case 0 -> x;
            case 1 -> y;
            case 2 -> z;
            default -> 0;
        };
    }

    public void set(int index, double value) {
        switch (index) {
            case 0 -> x = value;
            case 1 -> y = value;
            case 2 -> z = value;
        }
        ;
    }

    public J3DIMatrix4 exphat() {
        var r = this;
        var theta = r.norm();
        var R = new J3DIMatrix4();
        if (Math.abs(theta) < 1e-14) {
        } else {
            var a = r.hat().multiply(Math.sin(theta) / theta).getAsColumnMajorArray();
            var b = r.hat().multiply(r.hat()).multiply((1 - Math.cos(theta)) / (theta * theta)).getAsColumnMajorArray();
            R.load(new double[]{
                    1 + a[0] + b[0], a[1] + b[1], a[2] + b[2], 0,
                    a[4] + b[4], 1 + a[5] + b[5], a[6] + b[6], 0,
                    a[8] + b[8], a[9] + b[9], 1 + a[10] + b[10], 0,
                    0, 0, 0, 1
            });
        }
        return R;
    }

    public String toString() {
        return "[" + (x) + "," + (y) + "," + (z) + "]";
    }
}