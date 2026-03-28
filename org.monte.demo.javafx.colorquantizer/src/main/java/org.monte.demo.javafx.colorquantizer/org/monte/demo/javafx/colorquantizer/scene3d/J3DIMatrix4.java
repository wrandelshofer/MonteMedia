/*
 * @(#)J3DIMatrix4.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.javafx.colorquantizer.scene3d;

/// A mutable 4x4 matrix.
/// The matrix is column major.
public class J3DIMatrix4 {

    double m11, m12, m13, m14;
    double m21, m22, m23, m24;
    double m31, m32, m33, m34;
    double m41, m42, m43, m44;

    public J3DIMatrix4() {
        this.makeIdentity();
    }

    public J3DIMatrix4(double m11, double m12, double m13, double m14, double m21, double m22, double m23, double m24, double m31, double m32, double m33, double m34, double m41, double m42, double m43, double m44) {
        this.m11 = m11;
        this.m12 = m12;
        this.m13 = m13;
        this.m14 = m14;
        this.m21 = m21;
        this.m22 = m22;
        this.m23 = m23;
        this.m24 = m24;
        this.m31 = m31;
        this.m32 = m32;
        this.m33 = m33;
        this.m34 = m34;
        this.m41 = m41;
        this.m42 = m42;
        this.m43 = m43;
        this.m44 = m44;
    }

    public void load(double m11, double m12, double m13, double m14, double m21, double m22, double m23, double m24, double m31, double m32, double m33, double m34, double m41, double m42, double m43, double m44) {
        this.m11 = m11;
        this.m12 = m12;
        this.m13 = m13;
        this.m14 = m14;
        this.m21 = m21;
        this.m22 = m22;
        this.m23 = m23;
        this.m24 = m24;
        this.m31 = m31;
        this.m32 = m32;
        this.m33 = m33;
        this.m34 = m34;
        this.m41 = m41;
        this.m42 = m42;
        this.m43 = m43;
        this.m44 = m44;
    }

    public J3DIMatrix4(double[] m) {
        this.m11 = m[0];
        this.m12 = m[1];
        this.m13 = m[2];
        this.m14 = m[3];
        this.m21 = m[4];
        this.m22 = m[5];
        this.m23 = m[6];
        this.m24 = m[7];
        this.m31 = m[8];
        this.m32 = m[9];
        this.m33 = m[10];
        this.m34 = m[11];
        this.m41 = m[12];
        this.m42 = m[13];
        this.m43 = m[14];
        this.m44 = m[15];
    }

    public J3DIMatrix4(J3DIMatrix4 m) {
        this.m11 = m.m11;
        this.m12 = m.m12;
        this.m13 = m.m13;
        this.m14 = m.m14;
        this.m21 = m.m21;
        this.m22 = m.m22;
        this.m23 = m.m23;
        this.m24 = m.m24;
        this.m31 = m.m31;
        this.m32 = m.m32;
        this.m33 = m.m33;
        this.m34 = m.m34;
        this.m41 = m.m41;
        this.m42 = m.m42;
        this.m43 = m.m43;
        this.m44 = m.m44;
    }

    public double[] getAsColumnMajorArray() {
        return new double[]{
                m11, m12, m13, m14,
                m21, m22, m23, m24,
                m31, m32, m33, m34,
                m41, m42, m43, m44
        };
    }

    public double[] getAsRowMajorArray() {
        return new double[]{
                m11, m21, m31, m41,
                m12, m22, m32, m42,
                m13, m23, m33, m43,
                m14, m24, m34, m44
        };
    }


    public J3DIMatrix4 load(J3DIMatrix4 that) {
        this.m11 = that.m11;
        this.m12 = that.m12;
        this.m13 = that.m13;
        this.m14 = that.m14;
        this.m21 = that.m21;
        this.m22 = that.m22;
        this.m23 = that.m23;
        this.m24 = that.m24;
        this.m31 = that.m31;
        this.m32 = that.m32;
        this.m33 = that.m33;
        this.m34 = that.m34;
        this.m41 = that.m41;
        this.m42 = that.m42;
        this.m43 = that.m43;
        this.m44 = that.m44;
        return this;
    }

    public J3DIMatrix4 load(double[] matrix) {
        this.m11 = matrix[0];
        this.m12 = matrix[1];
        this.m13 = matrix[2];
        this.m14 = matrix[3];
        this.m21 = matrix[4];
        this.m22 = matrix[5];
        this.m23 = matrix[6];
        this.m24 = matrix[7];
        this.m31 = matrix[8];
        this.m32 = matrix[9];
        this.m33 = matrix[10];
        this.m34 = matrix[11];
        this.m41 = matrix[12];
        this.m42 = matrix[13];
        this.m43 = matrix[14];
        this.m44 = matrix[15];
        return this;
    }

    public J3DIMatrix4 makeIdentity() {
        this.m11 = 1;
        this.m12 = 0;
        this.m13 = 0;
        this.m14 = 0;
        this.m21 = 0;
        this.m22 = 1;
        this.m23 = 0;
        this.m24 = 0;
        this.m31 = 0;
        this.m32 = 0;
        this.m33 = 1;
        this.m34 = 0;
        this.m41 = 0;
        this.m42 = 0;
        this.m43 = 0;
        this.m44 = 1;
        return this;
    }

    public J3DIMatrix4 transpose() {
        var tmp = this.m12;
        this.m12 = this.m21;
        this.m21 = tmp;
        tmp = this.m13;
        this.m13 = this.m31;
        this.m31 = tmp;
        tmp = this.m14;
        this.m14 = this.m41;
        this.m41 = tmp;
        tmp = this.m23;
        this.m23 = this.m32;
        this.m32 = tmp;
        tmp = this.m24;
        this.m24 = this.m42;
        this.m42 = tmp;
        tmp = this.m34;
        this.m34 = this.m43;
        this.m43 = tmp;
        return this;
    }

    public J3DIMatrix4 invert() {
        var det = this._determinant4x4();
        if (Math.abs(det) < 1e-8)
            return null;
        this._makeAdjoint();
        this.m11 /= det;
        this.m12 /= det;
        this.m13 /= det;
        this.m14 /= det;
        this.m21 /= det;
        this.m22 /= det;
        this.m23 /= det;
        this.m24 /= det;
        this.m31 /= det;
        this.m32 /= det;
        this.m33 /= det;
        this.m34 /= det;
        this.m41 /= det;
        this.m42 /= det;
        this.m43 /= det;
        this.m44 /= det;
        return this;
    }

    public J3DIMatrix4 translate(double x, double y, double z) {
        var matrix = new J3DIMatrix4();
        matrix.m41 = x;
        matrix.m42 = y;
        matrix.m43 = z;
        this.multiply(matrix);
        return this;
    }

    public J3DIMatrix4 scale(double x, double y, double z) {
        var matrix = new J3DIMatrix4();
        matrix.m11 = x;
        matrix.m22 = y;
        matrix.m33 = z;
        this.multiply(matrix);
        return this;
    }

    public J3DIMatrix4 rotateX(double angle) {
        return this.rotate(angle, 1, 0, 0);
    }

    public J3DIMatrix4 rotateY(double angle) {
        return this.rotate(angle, 0, 1, 0);
    }

    public J3DIMatrix4 rotateZ(double angle) {
        return this.rotate(angle, 0, 0, 1);
    }

    public J3DIMatrix4 rotate(double angle, double x, double y, double z) {
        angle = angle / 180 * Math.PI;
        angle /= 2;
        var sinA = Math.sin(angle);
        var cosA = Math.cos(angle);
        var sinA2 = sinA * sinA;
        var len = Math.sqrt(x * x + y * y + z * z);
        if (len == 0) {
            x = 0;
            y = 0;
            z = 1;
        } else if (len != 1) {
            x /= len;
            y /= len;
            z /= len;
        }
        var mat = new J3DIMatrix4();
        if (x == 1 && y == 0 && z == 0) {
            mat.m11 = 1;
            mat.m12 = 0;
            mat.m13 = 0;
            mat.m21 = 0;
            mat.m22 = 1 - 2 * sinA2;
            mat.m23 = 2 * sinA * cosA;
            mat.m31 = 0;
            mat.m32 = -2 * sinA * cosA;
            mat.m33 = 1 - 2 * sinA2;
            mat.m14 = mat.m24 = mat.m34 = 0;
            mat.m41 = mat.m42 = mat.m43 = 0;
            mat.m44 = 1;
        } else if (x == 0 && y == 1 && z == 0) {
            mat.m11 = 1 - 2 * sinA2;
            mat.m12 = 0;
            mat.m13 = -2 * sinA * cosA;
            mat.m21 = 0;
            mat.m22 = 1;
            mat.m23 = 0;
            mat.m31 = 2 * sinA * cosA;
            mat.m32 = 0;
            mat.m33 = 1 - 2 * sinA2;
            mat.m14 = mat.m24 = mat.m34 = 0;
            mat.m41 = mat.m42 = mat.m43 = 0;
            mat.m44 = 1;
        } else if (x == 0 && y == 0 && z == 1) {
            mat.m11 = 1 - 2 * sinA2;
            mat.m12 = 2 * sinA * cosA;
            mat.m13 = 0;
            mat.m21 = -2 * sinA * cosA;
            mat.m22 = 1 - 2 * sinA2;
            mat.m23 = 0;
            mat.m31 = 0;
            mat.m32 = 0;
            mat.m33 = 1;
            mat.m14 = mat.m24 = mat.m34 = 0;
            mat.m41 = mat.m42 = mat.m43 = 0;
            mat.m44 = 1;
        } else {
            var x2 = x * x;
            var y2 = y * y;
            var z2 = z * z;
            mat.m11 = 1 - 2 * (y2 + z2) * sinA2;
            mat.m12 = 2 * (x * y * sinA2 + z * sinA * cosA);
            mat.m13 = 2 * (x * z * sinA2 - y * sinA * cosA);
            mat.m21 = 2 * (y * x * sinA2 - z * sinA * cosA);
            mat.m22 = 1 - 2 * (z2 + x2) * sinA2;
            mat.m23 = 2 * (y * z * sinA2 + x * sinA * cosA);
            mat.m31 = 2 * (z * x * sinA2 + y * sinA * cosA);
            mat.m32 = 2 * (z * y * sinA2 - x * sinA * cosA);
            mat.m33 = 1 - 2 * (x2 + y2) * sinA2;
            mat.m14 = mat.m24 = mat.m34 = 0;
            mat.m41 = mat.m42 = mat.m43 = 0;
            mat.m44 = 1;
        }
        this.multiply(mat);
        return this;
    }

    /// ```
    /// this = this * that
    /// ```
    public J3DIMatrix4 multiply(J3DIMatrix4 mat) {

        var a = mat;
        var b = this;
        var m11 = (a.m11 * b.m11 + a.m12 * b.m21
                + a.m13 * b.m31 + a.m14 * b.m41);
        var m12 = (a.m11 * b.m12 + a.m12 * b.m22
                + a.m13 * b.m32 + a.m14 * b.m42);
        var m13 = (a.m11 * b.m13 + a.m12 * b.m23
                + a.m13 * b.m33 + a.m14 * b.m43);
        var m14 = (a.m11 * b.m14 + a.m12 * b.m24
                + a.m13 * b.m34 + a.m14 * b.m44);
        var m21 = (a.m21 * b.m11 + a.m22 * b.m21
                + a.m23 * b.m31 + a.m24 * b.m41);
        var m22 = (a.m21 * b.m12 + a.m22 * b.m22
                + a.m23 * b.m32 + a.m24 * b.m42);
        var m23 = (a.m21 * b.m13 + a.m22 * b.m23
                + a.m23 * b.m33 + a.m24 * b.m43);
        var m24 = (a.m21 * b.m14 + a.m22 * b.m24
                + a.m23 * b.m34 + a.m24 * b.m44);
        var m31 = (a.m31 * b.m11 + a.m32 * b.m21
                + a.m33 * b.m31 + a.m34 * b.m41);
        var m32 = (a.m31 * b.m12 + a.m32 * b.m22
                + a.m33 * b.m32 + a.m34 * b.m42);
        var m33 = (a.m31 * b.m13 + a.m32 * b.m23
                + a.m33 * b.m33 + a.m34 * b.m43);
        var m34 = (a.m31 * b.m14 + a.m32 * b.m24
                + a.m33 * b.m34 + a.m34 * b.m44);
        var m41 = (a.m41 * b.m11 + a.m42 * b.m21
                + a.m43 * b.m31 + a.m44 * b.m41);
        var m42 = (a.m41 * b.m12 + a.m42 * b.m22
                + a.m43 * b.m32 + a.m44 * b.m42);
        var m43 = (a.m41 * b.m13 + a.m42 * b.m23
                + a.m43 * b.m33 + a.m44 * b.m43);
        var m44 = (a.m41 * b.m14 + a.m42 * b.m24
                + a.m43 * b.m34 + a.m44 * b.m44);
        this.m11 = m11;
        this.m12 = m12;
        this.m13 = m13;
        this.m14 = m14;
        this.m21 = m21;
        this.m22 = m22;
        this.m23 = m23;
        this.m24 = m24;
        this.m31 = m31;
        this.m32 = m32;
        this.m33 = m33;
        this.m34 = m34;
        this.m41 = m41;
        this.m42 = m42;
        this.m43 = m43;
        this.m44 = m44;
        return this;
    }


    public J3DIMatrix4 multiply(double mat) {
        this.m11 *= mat;
        this.m12 *= mat;
        this.m13 *= mat;
        this.m14 *= mat;
        this.m21 *= mat;
        this.m22 *= mat;
        this.m23 *= mat;
        this.m24 *= mat;
        this.m31 *= mat;
        this.m32 *= mat;
        this.m33 *= mat;
        this.m34 *= mat;
        this.m41 *= mat;
        this.m42 *= mat;
        this.m43 *= mat;
        this.m44 *= mat;
        return this;
    }

    public J3DIMatrix4 premultiply(J3DIMatrix4 mat) {
        var b = mat;
        var a = this;
        var m11 = (a.m11 * b.m11 + a.m12 * b.m21
                + a.m13 * b.m31 + a.m14 * b.m41);
        var m12 = (a.m11 * b.m12 + a.m12 * b.m22
                + a.m13 * b.m32 + a.m14 * b.m42);
        var m13 = (a.m11 * b.m13 + a.m12 * b.m23
                + a.m13 * b.m33 + a.m14 * b.m43);
        var m14 = (a.m11 * b.m14 + a.m12 * b.m24
                + a.m13 * b.m34 + a.m14 * b.m44);
        var m21 = (a.m21 * b.m11 + a.m22 * b.m21
                + a.m23 * b.m31 + a.m24 * b.m41);
        var m22 = (a.m21 * b.m12 + a.m22 * b.m22
                + a.m23 * b.m32 + a.m24 * b.m42);
        var m23 = (a.m21 * b.m13 + a.m22 * b.m23
                + a.m23 * b.m33 + a.m24 * b.m43);
        var m24 = (a.m21 * b.m14 + a.m22 * b.m24
                + a.m23 * b.m34 + a.m24 * b.m44);
        var m31 = (a.m31 * b.m11 + a.m32 * b.m21
                + a.m33 * b.m31 + a.m34 * b.m41);
        var m32 = (a.m31 * b.m12 + a.m32 * b.m22
                + a.m33 * b.m32 + a.m34 * b.m42);
        var m33 = (a.m31 * b.m13 + a.m32 * b.m23
                + a.m33 * b.m33 + a.m34 * b.m43);
        var m34 = (a.m31 * b.m14 + a.m32 * b.m24
                + a.m33 * b.m34 + a.m34 * b.m44);
        var m41 = (a.m41 * b.m11 + a.m42 * b.m21
                + a.m43 * b.m31 + a.m44 * b.m41);
        var m42 = (a.m41 * b.m12 + a.m42 * b.m22
                + a.m43 * b.m32 + a.m44 * b.m42);
        var m43 = (a.m41 * b.m13 + a.m42 * b.m23
                + a.m43 * b.m33 + a.m44 * b.m43);
        var m44 = (a.m41 * b.m14 + a.m42 * b.m24
                + a.m43 * b.m34 + a.m44 * b.m44);
        this.m11 = m11;
        this.m12 = m12;
        this.m13 = m13;
        this.m14 = m14;
        this.m21 = m21;
        this.m22 = m22;
        this.m23 = m23;
        this.m24 = m24;
        this.m31 = m31;
        this.m32 = m32;
        this.m33 = m33;
        this.m34 = m34;
        this.m41 = m41;
        this.m42 = m42;
        this.m43 = m43;
        this.m44 = m44;
        return this;
    }

    public J3DIMatrix4 premultiply(double mat) {
        this.m11 *= mat;
        this.m12 *= mat;
        this.m13 *= mat;
        this.m14 *= mat;
        this.m21 *= mat;
        this.m22 *= mat;
        this.m23 *= mat;
        this.m24 *= mat;
        this.m31 *= mat;
        this.m32 *= mat;
        this.m33 *= mat;
        this.m34 *= mat;
        this.m41 *= mat;
        this.m42 *= mat;
        this.m43 *= mat;
        this.m44 *= mat;

        return this;
    }

    public J3DIMatrix4 divide(double divisor) {
        this.m11 /= divisor;
        this.m12 /= divisor;
        this.m13 /= divisor;
        this.m14 /= divisor;
        this.m21 /= divisor;
        this.m22 /= divisor;
        this.m23 /= divisor;
        this.m24 /= divisor;
        this.m31 /= divisor;
        this.m32 /= divisor;
        this.m33 /= divisor;
        this.m34 /= divisor;
        this.m41 /= divisor;
        this.m42 /= divisor;
        this.m43 /= divisor;
        this.m44 /= divisor;
        return this;
    }

    public void ortho(double left, double right, double bottom, double top, double near, double far) {
        var tx = (left + right) / (left - right);
        var ty = (top + bottom) / (top - bottom);
        var tz = (far + near) / (far - near);
        var matrix = new J3DIMatrix4();
        matrix.m11 = 2 / (left - right);
        matrix.m12 = 0;
        matrix.m13 = 0;
        matrix.m14 = 0;
        matrix.m21 = 0;
        matrix.m22 = 2 / (top - bottom);
        matrix.m23 = 0;
        matrix.m24 = 0;
        matrix.m31 = 0;
        matrix.m32 = 0;
        matrix.m33 = -2 / (far - near);
        matrix.m34 = 0;
        matrix.m41 = tx;
        matrix.m42 = ty;
        matrix.m43 = tz;
        matrix.m44 = 1;
        this.multiply(matrix);
    }

    public void frustum(double left, double right, double bottom, double top, double near, double far) {
        var matrix = new J3DIMatrix4();
        var A = (right + left) / (right - left);
        var B = (top + bottom) / (top - bottom);
        var C = -(far + near) / (far - near);
        var D = -(2 * far * near) / (far - near);
        matrix.m11 = (2 * near) / (right - left);
        matrix.m12 = 0;
        matrix.m13 = 0;
        matrix.m14 = 0;
        matrix.m21 = 0;
        matrix.m22 = 2 * near / (top - bottom);
        matrix.m23 = 0;
        matrix.m24 = 0;
        matrix.m31 = A;
        matrix.m32 = B;
        matrix.m33 = C;
        matrix.m34 = -1;
        matrix.m41 = 0;
        matrix.m42 = 0;
        matrix.m43 = D;
        matrix.m44 = 0;
        this.multiply(matrix);
    }

    public void perspective(double fovy, double aspect, double zNear, double zFar) {
        var top = Math.tan(fovy * Math.PI / 360) * zNear;
        var bottom = -top;
        var left = aspect * bottom;
        var right = aspect * top;
        this.frustum(left, right, bottom, top, zNear, zFar);
    }

    public void world(J3DIVector3 pos, J3DIVector3 forward, J3DIVector3 up) {
        world(pos.x(), pos.y(), pos.z(), forward.x(), forward.y(), forward.z(), up.x(), up.y(), up.z());
    }

    public void world(double posx, double posy, double posz, double forwardx, double forwardy, double forwardz, double upx, double upy, double upz) {
        var matrix = new J3DIMatrix4();
        var forward = new J3DIVector3(forwardx, forwardy, forwardz);
        var up = new J3DIVector3(upx, upy, upz);
        forward.normalize();
        up.normalize();
        var right = new J3DIVector3();
        right.load(up);
        right.cross(forward);
        right.normalize();
        up.load(forward);
        up.cross(right);
        matrix.m11 = right.t(0);
        matrix.m21 = right.t(1);
        matrix.m31 = right.t(2);
        matrix.m12 = up.t(0);
        matrix.m22 = up.t(1);
        matrix.m32 = up.t(2);
        matrix.m13 = forward.t(0);
        matrix.m23 = forward.t(1);
        matrix.m33 = forward.t(2);
        matrix.translate(-posx, -posy, -posz);
        this.multiply(matrix);
    }

    public void lookat(J3DIVector3 eye, J3DIVector3 center, J3DIVector3 up) {
        lookat(eye.x(), eye.y(), eye.z(), center.x(), center.y(), center.z(), up.x(), up.y(), up.z());
    }

    public void lookat(double eyex, double eyey, double eyez, double centerx, double centery, double centerz, double upx, double upy, double upz) {
        var matrix = new J3DIMatrix4();
        var zx = centerx - eyex;
        var zy = centery - eyey;
        var zz = centerz - eyez;
        var mag = Math.sqrt(zx * zx + zy * zy + zz * zz);
        if (mag != 0) {
            zx /= mag;
            zy /= mag;
            zz /= mag;
        }
        var yx = upx;
        var yy = upy;
        var yz = upz;
        var xx = yy * zz - yz * zy;
        var xy = -yx * zz + yz * zx;
        var xz = yx * zy - yy * zx;
        yx = zy * xz - zz * xy;
        yy = -zx * xz + zz * xx;
        yz = zx * xy - zy * xx;
        mag = Math.sqrt(xx * xx + xy * xy + xz * xz);
        if (mag != 0) {
            xx /= mag;
            xy /= mag;
            xz /= mag;
        }
        mag = Math.sqrt(yx * yx + yy * yy + yz * yz);
        if (mag != 0) {
            yx /= mag;
            yy /= mag;
            yz /= mag;
        }
        matrix.m11 = xx;
        matrix.m21 = xy;
        matrix.m31 = xz;
        matrix.m41 = 0;
        matrix.m12 = yx;
        matrix.m22 = yy;
        matrix.m32 = yz;
        matrix.m42 = 0;
        matrix.m13 = zx;
        matrix.m23 = zy;
        matrix.m33 = zz;
        matrix.m43 = 0;
        matrix.m14 = 0;
        matrix.m24 = 0;
        matrix.m34 = 0;
        matrix.m44 = 1;
        matrix.translate(-eyex, -eyey, -eyez);
        this.multiply(matrix);
    }

    public boolean decompose(J3DIVector3 _translate, J3DIVector3 _rotate, J3DIVector3 _scale, J3DIVector3 _skew, double[] _perspective) {
        if (this.m44 == 0)
            return false;
        var translate = (_translate == null) ? new J3DIVector3() : _translate;
        var rotate = (_rotate == null) ? new J3DIVector3() : _rotate;
        var scale = (_scale == null) ? new J3DIVector3() : _scale;
        var skew = (_skew == null) ? new J3DIVector3() : _skew;
        var perspective = (_perspective == null) ? new double[4] : _perspective;
        var matrix = new J3DIMatrix4(this);
        matrix.divide(matrix.m44);
        var perspectiveMatrix = new J3DIMatrix4(matrix);
        perspectiveMatrix.m14 = 0;
        perspectiveMatrix.m24 = 0;
        perspectiveMatrix.m34 = 0;
        perspectiveMatrix.m44 = 1;
        if (perspectiveMatrix._determinant4x4() == 0)
            return false;
        //First, isolate perspective.
        if (matrix.m14 != 0 || matrix.m24 != 0 || matrix.m34 != 0) {
            var rightHandSide = new double[]{matrix.m14, matrix.m24, matrix.m34, matrix.m44};

            //Solve the equation by inverting perspectiveMatrix and multiplying
            //rightHandSide by the inverse
            var inversePerspectiveMatrix = new J3DIMatrix4(perspectiveMatrix);
            inversePerspectiveMatrix.invert();
            var transposedInversePerspectiveMatrix = new J3DIMatrix4(inversePerspectiveMatrix);
            transposedInversePerspectiveMatrix.transpose();
            transposedInversePerspectiveMatrix.multVecMatrix(perspective, rightHandSide);

            //Clear the perspective partition
            matrix.m14 = matrix.m24 = matrix.m34 = 0;
            matrix.m44 = 1;
        } else {
            // No perspective .
            perspective[0] = perspective[1] = perspective[2] = 0;
            perspective[3] = 1;
        }
        translate.x = matrix.m41;
        matrix.m41 = 0;
        translate.y = matrix.m42;
        matrix.m42 = 0;
        translate.z = matrix.m43;
        matrix.m43 = 0;
        var row0 = new J3DIVector3(matrix.m11, matrix.m12, matrix.m13);
        var row1 = new J3DIVector3(matrix.m21, matrix.m22, matrix.m23);
        var row2 = new J3DIVector3(matrix.m31, matrix.m32, matrix.m33);
        scale.x = row0.vectorLength();
        row0.divide(scale.x);
        skew.x = row0.dot(row1);
        row1.combine(row0, 1.0, -skew.x);
        scale.y = row1.vectorLength();
        row1.divide(scale.y);
        skew.x /= scale.y;
        skew.y = row1.dot(row2);
        row2.combine(row0, 1.0, -skew.y);
        skew.z = row1.dot(row2);
        row2.combine(row1, 1.0, -skew.z);
        scale.z = row2.vectorLength();
        row2.divide(scale.z);
        skew.y /= scale.z;
        skew.z /= scale.z;
        var pdum3 = new J3DIVector3(row1);
        pdum3.cross(row2);
        if (row0.dot(pdum3) < 0) {
            for (int i = 0; i < 3; i++) {
                scale.set(i, scale.get(i) * -1);
                row0.set(i, row0.get(i) * -1);
                row1.set(i, row1.get(i) * -1);
                row2.set(i, row2.get(i) * -1);
            }
        }
        rotate.y = Math.asin(-row0.z);
        if (Math.cos(rotate.y) != 0) {
            rotate.x = Math.atan2(row1.z, row2.z);
            rotate.z = Math.atan2(row0.y, row0.x);
        } else {
            rotate.x = Math.atan2(-row2.x, row1.y);
            rotate.z = 0;
        }
        var rad2deg = 180 / Math.PI;
        rotate.x *= rad2deg;
        rotate.y *= rad2deg;
        rotate.z *= rad2deg;
        return true;
    }

    private void multVecMatrix(double[] leftHandSide, double[] rightHandSide) {
        var w = rightHandSide[0];
        var x = rightHandSide[1];
        var y = rightHandSide[2];
        var z = rightHandSide[3];
        leftHandSide[0] = w * this.m41 + x * this.m11 + y * this.m21 + z * this.m31;
        leftHandSide[1] = w * this.m42 + x * this.m12 + y * this.m22 + z * this.m32;
        leftHandSide[2] = w * this.m43 + x * this.m13 + y * this.m23 + z * this.m33;
        leftHandSide[3] = w * this.m44 + x * this.m14 + y * this.m24 + z * this.m34;
    }

    double _determinant2x2(double a, double b, double c, double d) {
        return a * d - b * c;
    }

    double _determinant3x3(double a1, double a2, double a3, double b1, double b2, double b3, double c1, double c2, double c3) {
        return a1 * this._determinant2x2(b2, b3, c2, c3)
                - b1 * this._determinant2x2(a2, a3, c2, c3)
                + c1 * this._determinant2x2(a2, a3, b2, b3);
    }

    double _determinant4x4() {
        var a1 = this.m11;
        var b1 = this.m12;
        var c1 = this.m13;
        var d1 = this.m14;
        var a2 = this.m21;
        var b2 = this.m22;
        var c2 = this.m23;
        var d2 = this.m24;
        var a3 = this.m31;
        var b3 = this.m32;
        var c3 = this.m33;
        var d3 = this.m34;
        var a4 = this.m41;
        var b4 = this.m42;
        var c4 = this.m43;
        var d4 = this.m44;
        return a1 * this._determinant3x3(b2, b3, b4, c2, c3, c4, d2, d3, d4)
                - b1 * this._determinant3x3(a2, a3, a4, c2, c3, c4, d2, d3, d4)
                + c1 * this._determinant3x3(a2, a3, a4, b2, b3, b4, d2, d3, d4)
                - d1 * this._determinant3x3(a2, a3, a4, b2, b3, b4, c2, c3, c4);
    }

    void _makeAdjoint() {
        var a1 = this.m11;
        var b1 = this.m12;
        var c1 = this.m13;
        var d1 = this.m14;
        var a2 = this.m21;
        var b2 = this.m22;
        var c2 = this.m23;
        var d2 = this.m24;
        var a3 = this.m31;
        var b3 = this.m32;
        var c3 = this.m33;
        var d3 = this.m34;
        var a4 = this.m41;
        var b4 = this.m42;
        var c4 = this.m43;
        var d4 = this.m44;
        this.m11 = this._determinant3x3(b2, b3, b4, c2, c3, c4, d2, d3, d4);
        this.m21 = -this._determinant3x3(a2, a3, a4, c2, c3, c4, d2, d3, d4);
        this.m31 = this._determinant3x3(a2, a3, a4, b2, b3, b4, d2, d3, d4);
        this.m41 = -this._determinant3x3(a2, a3, a4, b2, b3, b4, c2, c3, c4);
        this.m12 = -this._determinant3x3(b1, b3, b4, c1, c3, c4, d1, d3, d4);
        this.m22 = this._determinant3x3(a1, a3, a4, c1, c3, c4, d1, d3, d4);
        this.m32 = -this._determinant3x3(a1, a3, a4, b1, b3, b4, d1, d3, d4);
        this.m42 = this._determinant3x3(a1, a3, a4, b1, b3, b4, c1, c3, c4);
        this.m13 = this._determinant3x3(b1, b2, b4, c1, c2, c4, d1, d2, d4);
        this.m23 = -this._determinant3x3(a1, a2, a4, c1, c2, c4, d1, d2, d4);
        this.m33 = this._determinant3x3(a1, a2, a4, b1, b2, b4, d1, d2, d4);
        this.m43 = -this._determinant3x3(a1, a2, a4, b1, b2, b4, c1, c2, c4);
        this.m14 = -this._determinant3x3(b1, b2, b3, c1, c2, c3, d1, d2, d3);
        this.m24 = this._determinant3x3(a1, a2, a3, c1, c2, c3, d1, d2, d3);
        this.m34 = -this._determinant3x3(a1, a2, a3, b1, b2, b3, d1, d2, d3);
        this.m44 = this._determinant3x3(a1, a2, a3, b1, b2, b3, c1, c2, c3);
    }

    public double trace() {
        return this.m11 + this.m22 + this.m33 + this.m44;
    }

    public J3DIVector3 loghat() {
        var r00 = this.m11;
        var r01 = this.m12;
        var r02 = this.m13;
        var r10 = this.m21;
        var r11 = this.m22;
        var r12 = this.m23;
        var r20 = this.m31;
        var r21 = this.m32;
        var r22 = this.m33;
        var cosa = (r00 + r11 + r22 - 1.0) * 0.5;
        var aa = new J3DIVector3(r21 - r12,
                r02 - r20,
                r10 - r01);
        var twosina = aa.norm();
        J3DIVector3 r;

        if (twosina < 1e-14) {
            if (Math.abs(r00 - r11) > 0.99
                    || Math.abs(r00 - r22) > 0.99
                    || Math.abs(r11 - r22) > 0.99) {
                if (Math.abs(r11 - r22) < 1e-14) {
                    r = new J3DIVector3(Math.PI * Math.signum(r00), 0, 0);
                } else if (Math.abs(r00 - r22) < 1e-14) {
                    r = new J3DIVector3(0, Math.PI * Math.signum(r11), 0);
                } else if (Math.abs(r00 - r11) < 1e-14) {
                    r = new J3DIVector3(0, 0, Math.PI * Math.signum(r22));
                } else {
                    r = new J3DIVector3(0, 0, 0);
                }
            } else {
                r = new J3DIVector3(0, 0, 0);
            }
        } else {
            var alpha = Math.atan2(twosina * 0.5, cosa);
            r = aa.multiply(alpha / twosina);
        }
        return r;
    }
}
