/*
 * @(#)Preconditions.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

/*
 * Copyright (C) 2007 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.monte.media.h264.impl.jcodec.common;

/// Static convenience methods that help a method or constructor check whether it was invoked
/// correctly (whether its _preconditions_ have been met). These methods generally accept a
/// `boolean` expression which is expected to be `true` (or in the case of
/// `checkNotNull`, an object reference which is expected to be non-null). When `false` (or
/// `null`) is passed instead, the `Preconditions` method throws an unchecked exception,
/// which helps the calling method communicate to _its_ caller that _that_ caller has made
/// a mistake. Example: <pre>
/// `/*** Returns the positive square root of the given value.** @throws IllegalArgumentException if the value is negative*``/public static double sqrt(double value){Preconditions.checkArgument(value >= 0.0, "negative value: %s", value);// calculate the square root}void exampleBadCaller(){double d = sqrt(-1.0);}`</pre>
///
/// In this example, `checkArgument` throws an `IllegalArgumentException` to indicate
/// that `exampleBadCaller` made an error in _its_ call to `sqrt`.
/// ### Warning about performance
///
/// The goal of this class is to improve readability of code, but in some circumstances this may
/// come at a significant performance cost. Remember that parameter values for message construction
/// must all be computed eagerly, and autoboxing and varargs array creation may happen as well, even
/// when the precondition check then succeeds (as it should almost always do in production). In some
/// circumstances these wasted CPU cycles and allocations can add up to a real problem.
/// Performance-sensitive precondition checks can always be converted to the customary form:
/// <pre>
/// `if (value < 0.0){throw new IllegalArgumentException("negative value: " + value);}`</pre>
/// ### Other types of preconditions
///
/// Not every type of precondition failure is supported by these methods. Continue to throw
/// standard JDK exceptions such as [java.util.NoSuchElementException] or
/// [UnsupportedOperationException] in the situations they are intended for.
/// ### Non-preconditions
///
/// It is of course possible to use the methods of this class to check for invalid conditions
/// which are _not the caller's fault_. Doing so is **not recommended** because it is
/// misleading to future readers of the code and of stack traces. See
/// [Conditional failures
/// explained](https://github.com/google/guava/wiki/ConditionalFailuresExplained) in the Guava User Guide for more advice.
/// ### `java.util.Objects.requireNonNull()`
///
/// Projects which use `com.google.common` should generally avoid the use of
/// [#requireNonNull(Object)]. Instead, use whichever of
/// [#checkNotNull(Object)] or `Verify#verifyNotNull(Object)` is appropriate to the
/// situation. (The same goes for the message-accepting overloads.)
/// ### Only `%s` is supported
///
/// In `Preconditions` error message template strings, only the `"%s"` specifier is
/// supported, not the full range of [java.util.Formatter] specifiers.
/// ### More information
///
/// See the Guava User Guide on
/// [using
/// `Preconditions`](https://github.com/google/guava/wiki/PreconditionsExplained).
///
/// References:
///
/// JCodecProject. Copyright 2008-2019 JCodecProject.
/// : [BSD 2-Clause License.](https://github.com/jcodec/jcodec/blob/7e5283408a75c3cdbefba98a57d546e170f0b7d0/LICENSE)
/// : [github.com](https://github.com/jcodec/jcodec)
///
/// @author Kevin Bourrillion
/// @since 2.0
public final class Preconditions {
    private Preconditions() {
    }

    /// Ensures the truth of an expression involving one or more parameters to the calling method.
    ///
    /// @param expression a boolean expression
    /// @throws IllegalArgumentException if `expression` is false
    public static void checkArgument(boolean expression) {
        if (!expression) {
            throw new IllegalArgumentException();
        }
    }

    /// Ensures the truth of an expression involving one or more parameters to the calling method.
    ///
    /// @param expression   a boolean expression
    /// @param errorMessage the exception message to use if the check fails; will be converted to a
    ///                                                                                                                                                                                                         string using [#valueOf(Object)]
    /// @throws IllegalArgumentException if `expression` is false
    public static void checkArgument(boolean expression, Object errorMessage) {
        if (!expression) {
            throw new IllegalArgumentException(String.valueOf(errorMessage));
        }
    }

    /// Ensures the truth of an expression involving one or more parameters to the calling method.
    ///
    /// @param expression           a boolean expression
    /// @param errorMessageTemplate a template for the exception message should the check fail. The
    ///                                                                                                                                                                                                                                                             message is formed by replacing each `%s` placeholder in the template with an
    ///                                                                                                                                                                                                                                                             argument. These are matched by position - the first `%s` gets
    ///                                                                                                                                                                                                                                 `errorMessageArgs[0]`, etc. Unmatched arguments will be appended to the formatted message in
    ///                                                                                                                                                                                                                                                             square braces. Unmatched placeholders will be left as-is.
    /// @param errorMessageArgs     the arguments to be substituted into the message template. Arguments
    ///                                                                                                                                                                                                                                                             are converted to strings using [#valueOf(Object)].
    /// @throws IllegalArgumentException if `expression` is false
    /// @throws NullPointerException     if the check fails and either `errorMessageTemplate` or
    ///                                                                                                                                                                                                                                                                                                          `errorMessageArgs` is null (don't let this happen)
    public static void checkArgument(
            boolean expression,
            String errorMessageTemplate,
            Object... errorMessageArgs) {
        if (!expression) {
            throw new IllegalArgumentException(format(errorMessageTemplate, errorMessageArgs));
        }
    }

    /// Ensures the truth of an expression involving one or more parameters to the calling method.
    ///
    /// See [#checkArgument(boolean,String,Object...)] for details.
    public static void checkArgument(boolean b, String errorMessageTemplate, char p1) {
        if (!b) {
            throw new IllegalArgumentException(format(errorMessageTemplate, p1));
        }
    }

    /// Ensures the truth of an expression involving one or more parameters to the calling method.
    ///
    /// See [#checkArgument(boolean,String,Object...)] for details.
    public static void checkArgument(boolean b, String errorMessageTemplate, int p1) {
        if (!b) {
            throw new IllegalArgumentException(format(errorMessageTemplate, p1));
        }
    }

    /// Ensures the truth of an expression involving one or more parameters to the calling method.
    ///
    /// See [#checkArgument(boolean,String,Object...)] for details.
    public static void checkArgument(boolean b, String errorMessageTemplate, long p1) {
        if (!b) {
            throw new IllegalArgumentException(format(errorMessageTemplate, p1));
        }
    }

    /// Ensures the truth of an expression involving one or more parameters to the calling method.
    ///
    /// See [#checkArgument(boolean,String,Object...)] for details.
    public static void checkArgument(
            boolean b, String errorMessageTemplate, Object p1) {
        if (!b) {
            throw new IllegalArgumentException(format(errorMessageTemplate, p1));
        }
    }

    /// Ensures the truth of an expression involving one or more parameters to the calling method.
    ///
    /// See [#checkArgument(boolean,String,Object...)] for details.
    public static void checkArgument(
            boolean b, String errorMessageTemplate, char p1, char p2) {
        if (!b) {
            throw new IllegalArgumentException(format(errorMessageTemplate, p1, p2));
        }
    }

    /// Ensures the truth of an expression involving one or more parameters to the calling method.
    ///
    /// See [#checkArgument(boolean,String,Object...)] for details.
    public static void checkArgument(
            boolean b, String errorMessageTemplate, char p1, int p2) {
        if (!b) {
            throw new IllegalArgumentException(format(errorMessageTemplate, p1, p2));
        }
    }

    /// Ensures the truth of an expression involving one or more parameters to the calling method.
    ///
    /// See [#checkArgument(boolean,String,Object...)] for details.
    public static void checkArgument(
            boolean b, String errorMessageTemplate, char p1, long p2) {
        if (!b) {
            throw new IllegalArgumentException(format(errorMessageTemplate, p1, p2));
        }
    }

    /// Ensures the truth of an expression involving one or more parameters to the calling method.
    ///
    /// See [#checkArgument(boolean,String,Object...)] for details.
    public static void checkArgument(
            boolean b, String errorMessageTemplate, char p1, Object p2) {
        if (!b) {
            throw new IllegalArgumentException(format(errorMessageTemplate, p1, p2));
        }
    }

    /// Ensures the truth of an expression involving one or more parameters to the calling method.
    ///
    /// See [#checkArgument(boolean,String,Object...)] for details.
    public static void checkArgument(
            boolean b, String errorMessageTemplate, int p1, char p2) {
        if (!b) {
            throw new IllegalArgumentException(format(errorMessageTemplate, p1, p2));
        }
    }

    /// Ensures the truth of an expression involving one or more parameters to the calling method.
    ///
    /// See [#checkArgument(boolean,String,Object...)] for details.
    public static void checkArgument(
            boolean b, String errorMessageTemplate, int p1, int p2) {
        if (!b) {
            throw new IllegalArgumentException(format(errorMessageTemplate, p1, p2));
        }
    }

    /// Ensures the truth of an expression involving one or more parameters to the calling method.
    ///
    /// See [#checkArgument(boolean,String,Object...)] for details.
    public static void checkArgument(
            boolean b, String errorMessageTemplate, int p1, long p2) {
        if (!b) {
            throw new IllegalArgumentException(format(errorMessageTemplate, p1, p2));
        }
    }

    /// Ensures the truth of an expression involving one or more parameters to the calling method.
    ///
    /// See [#checkArgument(boolean,String,Object...)] for details.
    public static void checkArgument(
            boolean b, String errorMessageTemplate, int p1, Object p2) {
        if (!b) {
            throw new IllegalArgumentException(format(errorMessageTemplate, p1, p2));
        }
    }

    /// Ensures the truth of an expression involving one or more parameters to the calling method.
    ///
    /// See [#checkArgument(boolean,String,Object...)] for details.
    public static void checkArgument(
            boolean b, String errorMessageTemplate, long p1, char p2) {
        if (!b) {
            throw new IllegalArgumentException(format(errorMessageTemplate, p1, p2));
        }
    }

    /// Ensures the truth of an expression involving one or more parameters to the calling method.
    ///
    /// See [#checkArgument(boolean,String,Object...)] for details.
    public static void checkArgument(
            boolean b, String errorMessageTemplate, long p1, int p2) {
        if (!b) {
            throw new IllegalArgumentException(format(errorMessageTemplate, p1, p2));
        }
    }

    /// Ensures the truth of an expression involving one or more parameters to the calling method.
    ///
    /// See [#checkArgument(boolean,String,Object...)] for details.
    public static void checkArgument(
            boolean b, String errorMessageTemplate, long p1, long p2) {
        if (!b) {
            throw new IllegalArgumentException(format(errorMessageTemplate, p1, p2));
        }
    }

    /// Ensures the truth of an expression involving one or more parameters to the calling method.
    ///
    /// See [#checkArgument(boolean,String,Object...)] for details.
    public static void checkArgument(
            boolean b, String errorMessageTemplate, long p1, Object p2) {
        if (!b) {
            throw new IllegalArgumentException(format(errorMessageTemplate, p1, p2));
        }
    }

    /// Ensures the truth of an expression involving one or more parameters to the calling method.
    ///
    /// See [#checkArgument(boolean,String,Object...)] for details.
    public static void checkArgument(
            boolean b, String errorMessageTemplate, Object p1, char p2) {
        if (!b) {
            throw new IllegalArgumentException(format(errorMessageTemplate, p1, p2));
        }
    }

    /// Ensures the truth of an expression involving one or more parameters to the calling method.
    ///
    /// See [#checkArgument(boolean,String,Object...)] for details.
    public static void checkArgument(
            boolean b, String errorMessageTemplate, Object p1, int p2) {
        if (!b) {
            throw new IllegalArgumentException(format(errorMessageTemplate, p1, p2));
        }
    }

    /// Ensures the truth of an expression involving one or more parameters to the calling method.
    ///
    /// See [#checkArgument(boolean,String,Object...)] for details.
    public static void checkArgument(
            boolean b, String errorMessageTemplate, Object p1, long p2) {
        if (!b) {
            throw new IllegalArgumentException(format(errorMessageTemplate, p1, p2));
        }
    }

    /// Ensures the truth of an expression involving one or more parameters to the calling method.
    ///
    /// See [#checkArgument(boolean,String,Object...)] for details.
    public static void checkArgument(
            boolean b, String errorMessageTemplate, Object p1, Object p2) {
        if (!b) {
            throw new IllegalArgumentException(format(errorMessageTemplate, p1, p2));
        }
    }

    /// Ensures the truth of an expression involving one or more parameters to the calling method.
    ///
    /// See [#checkArgument(boolean,String,Object...)] for details.
    public static void checkArgument(
            boolean b,
            String errorMessageTemplate,
            Object p1,
            Object p2,
            Object p3) {
        if (!b) {
            throw new IllegalArgumentException(format(errorMessageTemplate, p1, p2, p3));
        }
    }

    /// Ensures the truth of an expression involving one or more parameters to the calling method.
    ///
    /// See [#checkArgument(boolean,String,Object...)] for details.
    public static void checkArgument(
            boolean b,
            String errorMessageTemplate,
            Object p1,
            Object p2,
            Object p3,
            Object p4) {
        if (!b) {
            throw new IllegalArgumentException(format(errorMessageTemplate, p1, p2, p3, p4));
        }
    }

    /// Ensures the truth of an expression involving the state of the calling instance, but not
    /// involving any parameters to the calling method.
    ///
    /// @param expression a boolean expression
    /// @throws IllegalStateException if `expression` is false
    public static void checkState(boolean expression) {
        if (!expression) {
            throw new IllegalStateException();
        }
    }

    /// Ensures the truth of an expression involving the state of the calling instance, but not
    /// involving any parameters to the calling method.
    ///
    /// @param expression   a boolean expression
    /// @param errorMessage the exception message to use if the check fails; will be converted to a
    ///                                                                                                                                                                                     string using [#valueOf(Object)]
    /// @throws IllegalStateException if `expression` is false
    public static void checkState(boolean expression, Object errorMessage) {
        if (!expression) {
            throw new IllegalStateException(String.valueOf(errorMessage));
        }
    }

    /// Ensures the truth of an expression involving the state of the calling instance, but not
    /// involving any parameters to the calling method.
    ///
    /// @param expression           a boolean expression
    /// @param errorMessageTemplate a template for the exception message should the check fail. The
    ///                                                                                                                                                                                                                                                             message is formed by replacing each `%s` placeholder in the template with an
    ///                                                                                                                                                                                                                                                             argument. These are matched by position - the first `%s` gets
    ///                                                                                                                                                                                                                                 `errorMessageArgs[0]`, etc. Unmatched arguments will be appended to the formatted message in
    ///                                                                                                                                                                                                                                                             square braces. Unmatched placeholders will be left as-is.
    /// @param errorMessageArgs     the arguments to be substituted into the message template. Arguments
    ///                                                                                                                                                                                                                                                             are converted to strings using [#valueOf(Object)].
    /// @throws IllegalStateException if `expression` is false
    /// @throws NullPointerException  if the check fails and either `errorMessageTemplate` or
    ///                                                                                                                                                                                                                                                                               `errorMessageArgs` is null (don't let this happen)
    public static void checkState(
            boolean expression,
            String errorMessageTemplate,
            Object... errorMessageArgs) {
        if (!expression) {
            throw new IllegalStateException(format(errorMessageTemplate, errorMessageArgs));
        }
    }

    /// Ensures the truth of an expression involving the state of the calling instance, but not
    /// involving any parameters to the calling method.
    ///
    /// See [#checkState(boolean,String,Object...)] for details.
    public static void checkState(boolean b, String errorMessageTemplate, char p1) {
        if (!b) {
            throw new IllegalStateException(format(errorMessageTemplate, p1));
        }
    }

    /// Ensures the truth of an expression involving the state of the calling instance, but not
    /// involving any parameters to the calling method.
    ///
    /// See [#checkState(boolean,String,Object...)] for details.
    public static void checkState(boolean b, String errorMessageTemplate, int p1) {
        if (!b) {
            throw new IllegalStateException(format(errorMessageTemplate, p1));
        }
    }

    /// Ensures the truth of an expression involving the state of the calling instance, but not
    /// involving any parameters to the calling method.
    ///
    /// See [#checkState(boolean,String,Object...)] for details.
    public static void checkState(boolean b, String errorMessageTemplate, long p1) {
        if (!b) {
            throw new IllegalStateException(format(errorMessageTemplate, p1));
        }
    }

    /// Ensures the truth of an expression involving the state of the calling instance, but not
    /// involving any parameters to the calling method.
    ///
    /// See [#checkState(boolean,String,Object...)] for details.
    public static void checkState(
            boolean b, String errorMessageTemplate, Object p1) {
        if (!b) {
            throw new IllegalStateException(format(errorMessageTemplate, p1));
        }
    }

    /// Ensures the truth of an expression involving the state of the calling instance, but not
    /// involving any parameters to the calling method.
    ///
    /// See [#checkState(boolean,String,Object...)] for details.
    public static void checkState(
            boolean b, String errorMessageTemplate, char p1, char p2) {
        if (!b) {
            throw new IllegalStateException(format(errorMessageTemplate, p1, p2));
        }
    }

    /// Ensures the truth of an expression involving the state of the calling instance, but not
    /// involving any parameters to the calling method.
    ///
    /// See [#checkState(boolean,String,Object...)] for details.
    public static void checkState(boolean b, String errorMessageTemplate, char p1, int p2) {
        if (!b) {
            throw new IllegalStateException(format(errorMessageTemplate, p1, p2));
        }
    }

    /// Ensures the truth of an expression involving the state of the calling instance, but not
    /// involving any parameters to the calling method.
    ///
    /// See [#checkState(boolean,String,Object...)] for details.
    public static void checkState(
            boolean b, String errorMessageTemplate, char p1, long p2) {
        if (!b) {
            throw new IllegalStateException(format(errorMessageTemplate, p1, p2));
        }
    }

    /// Ensures the truth of an expression involving the state of the calling instance, but not
    /// involving any parameters to the calling method.
    ///
    /// See [#checkState(boolean,String,Object...)] for details.
    public static void checkState(
            boolean b, String errorMessageTemplate, char p1, Object p2) {
        if (!b) {
            throw new IllegalStateException(format(errorMessageTemplate, p1, p2));
        }
    }

    /// Ensures the truth of an expression involving the state of the calling instance, but not
    /// involving any parameters to the calling method.
    ///
    /// See [#checkState(boolean,String,Object...)] for details.
    public static void checkState(boolean b, String errorMessageTemplate, int p1, char p2) {
        if (!b) {
            throw new IllegalStateException(format(errorMessageTemplate, p1, p2));
        }
    }

    /// Ensures the truth of an expression involving the state of the calling instance, but not
    /// involving any parameters to the calling method.
    ///
    /// See [#checkState(boolean,String,Object...)] for details.
    public static void checkState(boolean b, String errorMessageTemplate, int p1, int p2) {
        if (!b) {
            throw new IllegalStateException(format(errorMessageTemplate, p1, p2));
        }
    }

    /// Ensures the truth of an expression involving the state of the calling instance, but not
    /// involving any parameters to the calling method.
    ///
    /// See [#checkState(boolean,String,Object...)] for details.
    public static void checkState(boolean b, String errorMessageTemplate, int p1, long p2) {
        if (!b) {
            throw new IllegalStateException(format(errorMessageTemplate, p1, p2));
        }
    }

    /// Ensures the truth of an expression involving the state of the calling instance, but not
    /// involving any parameters to the calling method.
    ///
    /// See [#checkState(boolean,String,Object...)] for details.
    public static void checkState(
            boolean b, String errorMessageTemplate, int p1, Object p2) {
        if (!b) {
            throw new IllegalStateException(format(errorMessageTemplate, p1, p2));
        }
    }

    /// Ensures the truth of an expression involving the state of the calling instance, but not
    /// involving any parameters to the calling method.
    ///
    /// See [#checkState(boolean,String,Object...)] for details.
    public static void checkState(
            boolean b, String errorMessageTemplate, long p1, char p2) {
        if (!b) {
            throw new IllegalStateException(format(errorMessageTemplate, p1, p2));
        }
    }

    /// Ensures the truth of an expression involving the state of the calling instance, but not
    /// involving any parameters to the calling method.
    ///
    /// See [#checkState(boolean,String,Object...)] for details.
    public static void checkState(boolean b, String errorMessageTemplate, long p1, int p2) {
        if (!b) {
            throw new IllegalStateException(format(errorMessageTemplate, p1, p2));
        }
    }

    /// Ensures the truth of an expression involving the state of the calling instance, but not
    /// involving any parameters to the calling method.
    ///
    /// See [#checkState(boolean,String,Object...)] for details.
    public static void checkState(
            boolean b, String errorMessageTemplate, long p1, long p2) {
        if (!b) {
            throw new IllegalStateException(format(errorMessageTemplate, p1, p2));
        }
    }

    /// Ensures the truth of an expression involving the state of the calling instance, but not
    /// involving any parameters to the calling method.
    ///
    /// See [#checkState(boolean,String,Object...)] for details.
    public static void checkState(
            boolean b, String errorMessageTemplate, long p1, Object p2) {
        if (!b) {
            throw new IllegalStateException(format(errorMessageTemplate, p1, p2));
        }
    }

    /// Ensures the truth of an expression involving the state of the calling instance, but not
    /// involving any parameters to the calling method.
    ///
    /// See [#checkState(boolean,String,Object...)] for details.
    public static void checkState(
            boolean b, String errorMessageTemplate, Object p1, char p2) {
        if (!b) {
            throw new IllegalStateException(format(errorMessageTemplate, p1, p2));
        }
    }

    /// Ensures the truth of an expression involving the state of the calling instance, but not
    /// involving any parameters to the calling method.
    ///
    /// See [#checkState(boolean,String,Object...)] for details.
    public static void checkState(
            boolean b, String errorMessageTemplate, Object p1, int p2) {
        if (!b) {
            throw new IllegalStateException(format(errorMessageTemplate, p1, p2));
        }
    }

    /// Ensures the truth of an expression involving the state of the calling instance, but not
    /// involving any parameters to the calling method.
    ///
    /// See [#checkState(boolean,String,Object...)] for details.
    public static void checkState(
            boolean b, String errorMessageTemplate, Object p1, long p2) {
        if (!b) {
            throw new IllegalStateException(format(errorMessageTemplate, p1, p2));
        }
    }

    /// Ensures the truth of an expression involving the state of the calling instance, but not
    /// involving any parameters to the calling method.
    ///
    /// See [#checkState(boolean,String,Object...)] for details.
    public static void checkState(
            boolean b, String errorMessageTemplate, Object p1, Object p2) {
        if (!b) {
            throw new IllegalStateException(format(errorMessageTemplate, p1, p2));
        }
    }

    /// Ensures the truth of an expression involving the state of the calling instance, but not
    /// involving any parameters to the calling method.
    ///
    /// See [#checkState(boolean,String,Object...)] for details.
    public static void checkState(
            boolean b,
            String errorMessageTemplate,
            Object p1,
            Object p2,
            Object p3) {
        if (!b) {
            throw new IllegalStateException(format(errorMessageTemplate, p1, p2, p3));
        }
    }

    /// Ensures the truth of an expression involving the state of the calling instance, but not
    /// involving any parameters to the calling method.
    ///
    /// See [#checkState(boolean,String,Object...)] for details.
    public static void checkState(
            boolean b,
            String errorMessageTemplate,
            Object p1,
            Object p2,
            Object p3,
            Object p4) {
        if (!b) {
            throw new IllegalStateException(format(errorMessageTemplate, p1, p2, p3, p4));
        }
    }

    /// Ensures that an object reference passed as a parameter to the calling method is not null.
    ///
    /// @param reference an object reference
    /// @return the non-null reference that was validated
    /// @throws NullPointerException if `reference` is null
    public static <T> T checkNotNull(T reference) {
        if (reference == null) {
            throw new NullPointerException();
        }
        return reference;
    }

    /// Ensures that an object reference passed as a parameter to the calling method is not null.
    ///
    /// @param reference    an object reference
    /// @param errorMessage the exception message to use if the check fails; will be converted to a
    ///                                                                                                                                                                                     string using [#valueOf(Object)]
    /// @return the non-null reference that was validated
    /// @throws NullPointerException if `reference` is null
    public static <T> T checkNotNull(T reference, Object errorMessage) {
        if (reference == null) {
            throw new NullPointerException(String.valueOf(errorMessage));
        }
        return reference;
    }

    /// Ensures that an object reference passed as a parameter to the calling method is not null.
    ///
    /// @param reference            an object reference
    /// @param errorMessageTemplate a template for the exception message should the check fail. The
    ///                                                                                                                                                                                                                                                             message is formed by replacing each `%s` placeholder in the template with an
    ///                                                                                                                                                                                                                                                             argument. These are matched by position - the first `%s` gets
    ///                                                                                                                                                                                                                                 `errorMessageArgs[0]`, etc. Unmatched arguments will be appended to the formatted message in
    ///                                                                                                                                                                                                                                                             square braces. Unmatched placeholders will be left as-is.
    /// @param errorMessageArgs     the arguments to be substituted into the message template. Arguments
    ///                                                                                                                                                                                                                                                             are converted to strings using [#valueOf(Object)].
    /// @return the non-null reference that was validated
    /// @throws NullPointerException if `reference` is null
    public static <T> T checkNotNull(
            T reference, String errorMessageTemplate, Object... errorMessageArgs) {
        if (reference == null) {
            // If either of these parameters is null, the right thing happens anyway
            throw new NullPointerException(format(errorMessageTemplate, errorMessageArgs));
        }
        return reference;
    }

    /// Ensures that an object reference passed as a parameter to the calling method is not null.
    ///
    /// See [#checkNotNull(Object,String,Object...)] for details.
    public static <T> T checkNotNull(T obj, String errorMessageTemplate, char p1) {
        if (obj == null) {
            throw new NullPointerException(format(errorMessageTemplate, p1));
        }
        return obj;
    }

    /// Ensures that an object reference passed as a parameter to the calling method is not null.
    ///
    /// See [#checkNotNull(Object,String,Object...)] for details.
    public static <T> T checkNotNull(T obj, String errorMessageTemplate, int p1) {
        if (obj == null) {
            throw new NullPointerException(format(errorMessageTemplate, p1));
        }
        return obj;
    }

    /// Ensures that an object reference passed as a parameter to the calling method is not null.
    ///
    /// See [#checkNotNull(Object,String,Object...)] for details.
    public static <T> T checkNotNull(T obj, String errorMessageTemplate, long p1) {
        if (obj == null) {
            throw new NullPointerException(format(errorMessageTemplate, p1));
        }
        return obj;
    }

    /// Ensures that an object reference passed as a parameter to the calling method is not null.
    ///
    /// See [#checkNotNull(Object,String,Object...)] for details.
    public static <T> T checkNotNull(
            T obj, String errorMessageTemplate, Object p1) {
        if (obj == null) {
            throw new NullPointerException(format(errorMessageTemplate, p1));
        }
        return obj;
    }

    /// Ensures that an object reference passed as a parameter to the calling method is not null.
    ///
    /// See [#checkNotNull(Object,String,Object...)] for details.
    public static <T> T checkNotNull(T obj, String errorMessageTemplate, char p1, char p2) {
        if (obj == null) {
            throw new NullPointerException(format(errorMessageTemplate, p1, p2));
        }
        return obj;
    }

    /// Ensures that an object reference passed as a parameter to the calling method is not null.
    ///
    /// See [#checkNotNull(Object,String,Object...)] for details.
    public static <T> T checkNotNull(T obj, String errorMessageTemplate, char p1, int p2) {
        if (obj == null) {
            throw new NullPointerException(format(errorMessageTemplate, p1, p2));
        }
        return obj;
    }

    /// Ensures that an object reference passed as a parameter to the calling method is not null.
    ///
    /// See [#checkNotNull(Object,String,Object...)] for details.
    public static <T> T checkNotNull(T obj, String errorMessageTemplate, char p1, long p2) {
        if (obj == null) {
            throw new NullPointerException(format(errorMessageTemplate, p1, p2));
        }
        return obj;
    }

    /// Ensures that an object reference passed as a parameter to the calling method is not null.
    ///
    /// See [#checkNotNull(Object,String,Object...)] for details.
    public static <T> T checkNotNull(
            T obj, String errorMessageTemplate, char p1, Object p2) {
        if (obj == null) {
            throw new NullPointerException(format(errorMessageTemplate, p1, p2));
        }
        return obj;
    }

    /// Ensures that an object reference passed as a parameter to the calling method is not null.
    ///
    /// See [#checkNotNull(Object,String,Object...)] for details.
    public static <T> T checkNotNull(T obj, String errorMessageTemplate, int p1, char p2) {
        if (obj == null) {
            throw new NullPointerException(format(errorMessageTemplate, p1, p2));
        }
        return obj;
    }

    /// Ensures that an object reference passed as a parameter to the calling method is not null.
    ///
    /// See [#checkNotNull(Object,String,Object...)] for details.
    public static <T> T checkNotNull(T obj, String errorMessageTemplate, int p1, int p2) {
        if (obj == null) {
            throw new NullPointerException(format(errorMessageTemplate, p1, p2));
        }
        return obj;
    }

    /// Ensures that an object reference passed as a parameter to the calling method is not null.
    ///
    /// See [#checkNotNull(Object,String,Object...)] for details.
    public static <T> T checkNotNull(T obj, String errorMessageTemplate, int p1, long p2) {
        if (obj == null) {
            throw new NullPointerException(format(errorMessageTemplate, p1, p2));
        }
        return obj;
    }

    /// Ensures that an object reference passed as a parameter to the calling method is not null.
    ///
    /// See [#checkNotNull(Object,String,Object...)] for details.
    public static <T> T checkNotNull(
            T obj, String errorMessageTemplate, int p1, Object p2) {
        if (obj == null) {
            throw new NullPointerException(format(errorMessageTemplate, p1, p2));
        }
        return obj;
    }

    /// Ensures that an object reference passed as a parameter to the calling method is not null.
    ///
    /// See [#checkNotNull(Object,String,Object...)] for details.
    public static <T> T checkNotNull(T obj, String errorMessageTemplate, long p1, char p2) {
        if (obj == null) {
            throw new NullPointerException(format(errorMessageTemplate, p1, p2));
        }
        return obj;
    }

    /// Ensures that an object reference passed as a parameter to the calling method is not null.
    ///
    /// See [#checkNotNull(Object,String,Object...)] for details.
    public static <T> T checkNotNull(T obj, String errorMessageTemplate, long p1, int p2) {
        if (obj == null) {
            throw new NullPointerException(format(errorMessageTemplate, p1, p2));
        }
        return obj;
    }

    /// Ensures that an object reference passed as a parameter to the calling method is not null.
    ///
    /// See [#checkNotNull(Object,String,Object...)] for details.
    public static <T> T checkNotNull(T obj, String errorMessageTemplate, long p1, long p2) {
        if (obj == null) {
            throw new NullPointerException(format(errorMessageTemplate, p1, p2));
        }
        return obj;
    }

    /// Ensures that an object reference passed as a parameter to the calling method is not null.
    ///
    /// See [#checkNotNull(Object,String,Object...)] for details.
    public static <T> T checkNotNull(
            T obj, String errorMessageTemplate, long p1, Object p2) {
        if (obj == null) {
            throw new NullPointerException(format(errorMessageTemplate, p1, p2));
        }
        return obj;
    }

    /// Ensures that an object reference passed as a parameter to the calling method is not null.
    ///
    /// See [#checkNotNull(Object,String,Object...)] for details.
    public static <T> T checkNotNull(
            T obj, String errorMessageTemplate, Object p1, char p2) {
        if (obj == null) {
            throw new NullPointerException(format(errorMessageTemplate, p1, p2));
        }
        return obj;
    }

    /// Ensures that an object reference passed as a parameter to the calling method is not null.
    ///
    /// See [#checkNotNull(Object,String,Object...)] for details.
    public static <T> T checkNotNull(
            T obj, String errorMessageTemplate, Object p1, int p2) {
        if (obj == null) {
            throw new NullPointerException(format(errorMessageTemplate, p1, p2));
        }
        return obj;
    }

    /// Ensures that an object reference passed as a parameter to the calling method is not null.
    ///
    /// See [#checkNotNull(Object,String,Object...)] for details.
    public static <T> T checkNotNull(
            T obj, String errorMessageTemplate, Object p1, long p2) {
        if (obj == null) {
            throw new NullPointerException(format(errorMessageTemplate, p1, p2));
        }
        return obj;
    }

    /// Ensures that an object reference passed as a parameter to the calling method is not null.
    ///
    /// See [#checkNotNull(Object,String,Object...)] for details.
    public static <T> T checkNotNull(
            T obj, String errorMessageTemplate, Object p1, Object p2) {
        if (obj == null) {
            throw new NullPointerException(format(errorMessageTemplate, p1, p2));
        }
        return obj;
    }

    /// Ensures that an object reference passed as a parameter to the calling method is not null.
    ///
    /// See [#checkNotNull(Object,String,Object...)] for details.
    public static <T> T checkNotNull(
            T obj,
            String errorMessageTemplate,
            Object p1,
            Object p2,
            Object p3) {
        if (obj == null) {
            throw new NullPointerException(format(errorMessageTemplate, p1, p2, p3));
        }
        return obj;
    }

    /// Ensures that an object reference passed as a parameter to the calling method is not null.
    ///
    /// See [#checkNotNull(Object,String,Object...)] for details.
    public static <T> T checkNotNull(
            T obj,
            String errorMessageTemplate,
            Object p1,
            Object p2,
            Object p3,
            Object p4) {
        if (obj == null) {
            throw new NullPointerException(format(errorMessageTemplate, p1, p2, p3, p4));
        }
        return obj;
    }

    /*
     * All recent hotspots (as of 2009) *really* like to have the natural code
     *
     * if (guardExpression) {
     *    throw new BadException(messageExpression);
     * }
     *
     * refactored so that messageExpression is moved to a separate String-returning method.
     *
     * if (guardExpression) {
     *    throw new BadException(badMsg(...));
     * }
     *
     * The alternative natural refactorings into void or Exception-returning methods are much slower.
     * This is a big deal - we're talking factors of 2-8 in microbenchmarks, not just 10-20%. (This is
     * a hotspot optimizer bug, which should be fixed, but that's a separate, big project).
     *
     * The coding pattern above is heavily used in java.util, e.g. in ArrayList. There is a
     * RangeCheckMicroBenchmark in the JDK that was used to test this.
     *
     * But the methods in this class want to throw different exceptions, depending on the args, so it
     * appears that this pattern is not directly applicable. But we can use the ridiculous, devious
     * trick of throwing an exception in the middle of the construction of another exception. Hotspot
     * is fine with that.
     */

    /// Ensures that `index` specifies a valid _element_ in an array, list or string of size
    /// `size`. An element index may range from zero, inclusive, to `size`, exclusive.
    ///
    /// @param index a user-supplied index identifying an element of an array, list or string
    /// @param size  the size of that array, list or string
    /// @return the value of `index`
    /// @throws IndexOutOfBoundsException if `index` is negative or is not less than `size`
    /// @throws IllegalArgumentException  if `size` is negative
    public static int checkElementIndex(int index, int size) {
        return checkElementIndex(index, size, "index");
    }

    /// Ensures that `index` specifies a valid _element_ in an array, list or string of size
    /// `size`. An element index may range from zero, inclusive, to `size`, exclusive.
    ///
    /// @param index a user-supplied index identifying an element of an array, list or string
    /// @param size  the size of that array, list or string
    /// @param desc  the text to use to describe this index in an error message
    /// @return the value of `index`
    /// @throws IndexOutOfBoundsException if `index` is negative or is not less than `size`
    /// @throws IllegalArgumentException  if `size` is negative
    public static int checkElementIndex(int index, int size, String desc) {
        // Carefully optimized for execution by hotspot (explanatory comment above)
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(badElementIndex(index, size, desc));
        }
        return index;
    }

    private static String badElementIndex(int index, int size, String desc) {
        if (index < 0) {
            return format("%s (%s) must not be negative", desc, index);
        } else if (size < 0) {
            throw new IllegalArgumentException("negative size: " + size);
        } else { // index >= size
            return format("%s (%s) must be less than size (%s)", desc, index, size);
        }
    }

    /// Ensures that `index` specifies a valid _position_ in an array, list or string of
    /// size `size`. A position index may range from zero to `size`, inclusive.
    ///
    /// @param index a user-supplied index identifying a position in an array, list or string
    /// @param size  the size of that array, list or string
    /// @return the value of `index`
    /// @throws IndexOutOfBoundsException if `index` is negative or is greater than `size`
    /// @throws IllegalArgumentException  if `size` is negative
    public static int checkPositionIndex(int index, int size) {
        return checkPositionIndex(index, size, "index");
    }

    /// Ensures that `index` specifies a valid _position_ in an array, list or string of
    /// size `size`. A position index may range from zero to `size`, inclusive.
    ///
    /// @param index a user-supplied index identifying a position in an array, list or string
    /// @param size  the size of that array, list or string
    /// @param desc  the text to use to describe this index in an error message
    /// @return the value of `index`
    /// @throws IndexOutOfBoundsException if `index` is negative or is greater than `size`
    /// @throws IllegalArgumentException  if `size` is negative
    public static int checkPositionIndex(int index, int size, String desc) {
        // Carefully optimized for execution by hotspot (explanatory comment above)
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException(badPositionIndex(index, size, desc));
        }
        return index;
    }

    private static String badPositionIndex(int index, int size, String desc) {
        if (index < 0) {
            return format("%s (%s) must not be negative", desc, index);
        } else if (size < 0) {
            throw new IllegalArgumentException("negative size: " + size);
        } else { // index > size
            return format("%s (%s) must not be greater than size (%s)", desc, index, size);
        }
    }

    /// Ensures that `start` and `end` specify a valid _positions_ in an array, list
    /// or string of size `size`, and are in order. A position index may range from zero to
    /// `size`, inclusive.
    ///
    /// @param start a user-supplied index identifying a starting position in an array, list or string
    /// @param end   a user-supplied index identifying a ending position in an array, list or string
    /// @param size  the size of that array, list or string
    /// @throws IndexOutOfBoundsException if either index is negative or is greater than `size`,
    ///                                                                                                                                                                                                                                                                                                                   or if `end` is less than `start`
    /// @throws IllegalArgumentException  if `size` is negative
    public static void checkPositionIndexes(int start, int end, int size) {
        // Carefully optimized for execution by hotspot (explanatory comment above)
        if (start < 0 || end < start || end > size) {
            throw new IndexOutOfBoundsException(badPositionIndexes(start, end, size));
        }
    }

    private static String badPositionIndexes(int start, int end, int size) {
        if (start < 0 || start > size) {
            return badPositionIndex(start, size, "start index");
        }
        if (end < 0 || end > size) {
            return badPositionIndex(end, size, "end index");
        }
        // end < start
        return format("end index (%s) must not be less than start index (%s)", end, start);
    }

    /// Substitutes each `%s` in `template` with an argument. These are matched by
    /// position: the first `%s` gets `args[0]`, etc. If there are more arguments than
    /// placeholders, the unmatched arguments will be appended to the end of the formatted message in
    /// square braces.
    ///
    /// @param template a non-null string containing 0 or more `%s` placeholders.
    /// @param args     the arguments to be substituted into the message template. Arguments are converted
    ///                                                                                                                                                 to strings using [#valueOf(Object)]. Arguments can be null.
    // Note that this is somewhat-improperly used from Verify.java as well.
    static String format(String template, Object... args) {
        if (args == null) {
            throw new NullPointerException();
        }
        template = String.valueOf(template); // null -> "null"

        // start substituting the arguments into the '%s' placeholders
        StringBuilder builder = new StringBuilder(template.length() + 16 * args.length);
        int templateStart = 0;
        int i = 0;
        while (i < args.length) {
            int placeholderStart = template.indexOf("%s", templateStart);
            if (placeholderStart == -1) {
                break;
            }
            builder.append(template, templateStart, placeholderStart);
            builder.append(args[i++]);
            templateStart = placeholderStart + 2;
        }
        builder.append(template, templateStart, template.length());

        // if we run out of placeholders, append the extra args in square braces
        if (i < args.length) {
            builder.append(" [");
            builder.append(args[i++]);
            while (i < args.length) {
                builder.append(", ");
                builder.append(args[i++]);
            }
            builder.append(']');
        }

        return builder.toString();
    }
}
