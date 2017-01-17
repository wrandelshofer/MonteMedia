/* @(#)LinearEquations.java
 * Copyright © 2012 Werner Randelshofer, Switzerland. 
 * You may only use this software in accordance with the license terms.
 */
package ru.sbtqa.monte.media.math;

import static java.lang.System.out;

/**
 * {@code LinearEquations}.
 * 
 * Reference: http://en.wikipedia.org/wiki/Cramer's_rule
 * http://en.wikipedia.org/wiki/Determinant
 *
 * @author Werner Randelshofer
 * @version $Id: LinearEquations.java 364 2016-11-09 19:54:25Z werner $
 */
public class LinearEquations {

    private LinearEquations() {
    }

    ;
    
    /** Solves a linear system for x,y with cramer's rule.
     * 
     * 
     * a*x + b*y = e
     * c*x + d*y = f
     * 
     * 
     * @param a TODO
     * @param b TODO
     * @param c TODO
     * @param d TODO
     * @param e TODO
     * @param f TODO
     * @return  TODO
     */
    public static double[] solve(double a, double b, double c, double d, double e, double f) {
        out.println("[" + a + " " + b + ";" + c + " " + d + "]\\[" + e + ";" + f + "]");
        double x = (e * d - b * f) / (a * d - b * c);
        double y = (a * f - e * c) / (a * d - b * c);
        return new double[]{x, y};
    }

    /**
     * Solves a linear system for x,y,z with cramer's rule.
     *
     * 
     * a*x + b*y + c*z = j
     * d*x + e*y + f*z = k
     * g*x + h*y + i*z = l
     * 
     *
     * @param a TODO
     * @param b TODO
     * @param c TODO
     * @param d TODO
     * @param e TODO
     * @param f TODO
     * @param g TODO
     * @param l TODO
     * @param h TODO
     * @param k TODO
     * @param i TODO
     * @param j TODO
     * @return TODO
     */
    public static double[] solve(double a, double b, double c, double d, double e, double f, double g, double h, double i, double j, double k, double l) {
        double det_abcdefghi = det(a, b, c, d, e, f, g, h, i);
        double x = det(j, b, c, k, e, f, l, h, i) / det_abcdefghi;
        double y = det(a, j, c, d, k, f, g, l, i) / det_abcdefghi;
        double z = det(a, b, j, d, e, k, g, h, l) / det_abcdefghi;
        return new double[]{x, y, z};
    }

    /**
     * Computes the determinant of a 2x2 matrix using Sarrus' rule.
     * 
     * | a, b, c |     |e, f|   |d, f|   |d, e|
     * | d, e, f | = a*|h, i|-b*|g, i|+c*|g, h|=aei+bfg+cdh-ceg-bdi-afh
     * | g, h, i |
     * 
     *
     * @param a TODO
     * @param b TODO
     * @param c TODO
     * @param d TODO
     * @param e TODO
     * @param f TODO
     * @param g TODO
     * @param h TODO
     * @param i TODO
     * @return the determinant
     */
    public static double det(double a, double b, double c, double d, double e, double f, double g, double h, double i) {
        return a * e * i//
              + b * f * g //
              + c * d * h //
              - c * e * g //
              - b * d * i //
              - a * f * h;
    }

    /**
     * Computes the determinant of a 3x3 matrix using Sarrus' rule.
     * 
     * | a, b |
     * | c, d | = a*d - b*c
     * 
     *
     * @param a TODO
     * @param b TODO
     * @param c TODO
     * @param d TODO
     * @return the determinant
     */
    public static double det(double a, double b, double c, double d) {
        return a * d - b * c;
    }
}
