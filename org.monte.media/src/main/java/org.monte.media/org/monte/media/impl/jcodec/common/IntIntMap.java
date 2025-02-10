package org.monte.media.impl.jcodec.common;

import java.util.Arrays;

import static java.lang.System.arraycopy;

/**
 * References:
 * <p>
 * This code has been derived from JCodecProject.
 * <dl>
 *     <dt>JCodecProject. Copyright 2008-2019 JCodecProject.
 *     <br><a href="https://github.com/jcodec/jcodec/blob/7e5283408a75c3cdbefba98a57d546e170f0b7d0/LICENSE">BSD 2-Clause License.</a></dt>
 *     <dd><a href="https://github.com/jcodec/jcodec">github.com</a></dd>
 * </dl>
 *
 * @author The JCodec project
 */
public class IntIntMap {

    private static final int GROW_BY = 128;
    private static final int MIN_VALUE = 0x80000000;
    private int[] storage;
    private int _size;

    public IntIntMap() {
        this.storage = createArray(GROW_BY);
        Arrays.fill(this.storage, MIN_VALUE);
    }

    public void put(int key, int val) {
        if (val == MIN_VALUE)
            throw new IllegalArgumentException("This implementation can not store " + MIN_VALUE);

        if (storage.length <= key) {
            int[] ns = createArray(key + GROW_BY);
            arraycopy(storage, 0, ns, 0, storage.length);
            Arrays.fill(ns, storage.length, ns.length, MIN_VALUE);
            storage = ns;
        }
        if (storage[key] == MIN_VALUE)
            _size++;
        storage[key] = val;
    }

    public int get(int key) {
        return key >= storage.length ? MIN_VALUE : storage[key];
    }

    public boolean contains(int key) {
        return key >= 0 && key < storage.length;
    }

    public int[] keys() {
        int[] result = new int[_size];
        for (int i = 0, r = 0; i < storage.length; i++) {
            if (storage[i] != MIN_VALUE)
                result[r++] = i;
        }
        return result;
    }

    public void clear() {
        for (int i = 0; i < storage.length; i++)
            storage[i] = MIN_VALUE;
        _size = 0;
    }

    public int size() {
        return _size;
    }

    public void remove(int key) {
        if (storage[key] != MIN_VALUE)
            _size--;
        storage[key] = MIN_VALUE;
    }

    public int[] values() {
        int[] result = createArray(_size);
        for (int i = 0, r = 0; i < storage.length; i++) {
            if (storage[i] != MIN_VALUE)
                result[r++] = storage[i];
        }
        return result;
    }

    private static int[] createArray(int size) {
        return new int[size];
    }
}
