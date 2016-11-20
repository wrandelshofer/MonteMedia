package ru.sbtqa.monte.mathdemo;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.System.out;
import ru.sbtqa.monte.media.math.Rational;

/**
 * {@code RationalDemo}.
 *
 * @author Werner Randelshofer
 * @version $Id: RationalDemo.java 292 2012-12-04 08:20:59Z werner $
 */
public class RationalDemo {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        arithmetics();
    }

    public static void arithmetics() {
        Rational[] as = {new Rational(2,5),new Rational(2,5),new Rational(861198430599L, 1437500000L), new Rational(978969156151L, 1437500000L), new Rational(MAX_VALUE,1)};
        Rational[] bs = {new Rational(5,1),new Rational(3,7),new Rational(7360670347L, 2500000), new Rational(7360670347L, 2500000), new Rational(MAX_VALUE,1)};

        Rational a, b;
        out.println("Divisions");
        for (int i = 0; i < as.length; i++) {
            a = as[i];
            b = bs[i];
            out.println(a.doubleValue() + " / " + b.doubleValue() + "=" + a.divide(b).doubleValue() + " vs. " + (a.doubleValue() / b.doubleValue()));
        }
        out.println("Multiplications");
        for (int i = 0; i < as.length; i++) {
            a = as[i];
            b = bs[i];
            out.println(a.doubleValue() + " * " + b.doubleValue() + "=" + a.multiply(b).doubleValue() + " vs. " + (a.doubleValue() * b.doubleValue()));
        }
        out.println("Additions");
        for (int i = 0; i < as.length; i++) {
            a = as[i];
            b = bs[i];
            out.println(a.doubleValue() + " + " + b.doubleValue() + "=" + a.add(b).doubleValue() + " vs. " + (a.doubleValue() + b.doubleValue()));
        }
        out.println("Subtractions");
        for (int i = 0; i < as.length; i++) {
            a = as[i];
            b = bs[i];
            out.println(a.doubleValue() + " - " + b.doubleValue() + "=" + a.subtract(b).doubleValue() + " vs. " + (a.doubleValue() - b.doubleValue()));
        }

    }
}
