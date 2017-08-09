/* @(#)IFFChunk.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Under the MIT License.
 */
package org.monte.media.iff;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * IFF Chunks form the building blocks of an IFF file. This class is made for
 * reading purposes only. See MutableIFFChunk for writing purposes.
 *
 * @author Werner Randelshofer, Hausmatt 10, CH-6405 Goldau, Switzerland
 * @version 1.1 2006-07-20 Reworked for Java 1.5.
 * <br>1.0 1999-10-19
 */
public class IFFChunk {

    private int id;
    private int type;
    private long size;
    private long scan;
    private byte[] data;
    private HashMap<IFFChunk, IFFChunk> propertyChunks;
    private ArrayList<IFFChunk> collectionChunks;

    public IFFChunk(int type, int id) {
        this.id = id;
        this.type = type;
        this.size = -1;
        this.scan = -1;
    }

    public IFFChunk(int type, int id, long size, long scan) {
        this.id = id;
        this.type = type;
        this.size = size;
        this.scan = scan;
    }

    @SuppressWarnings("unchecked")
    public IFFChunk(int type, int id, long size, long scan, IFFChunk propGroup) {
        this.id = id;
        this.type = type;
        this.size = size;
        this.scan = scan;
        if (propGroup != null) {
            if (propGroup.propertyChunks != null) {
                propertyChunks = (HashMap<IFFChunk, IFFChunk>) propGroup.propertyChunks.clone();
            }
            if (propGroup.collectionChunks != null) {
                collectionChunks = (ArrayList<IFFChunk>) propGroup.collectionChunks.clone();
            }
        }
    }

    /**
     * @return ID of chunk.
     */
    public int getID() {
        return id;
    }

    /**
     * @return Type of chunk.
     */
    public int getType() {
        return type;
    }

    /**
     * @return Size of chunk.
     */
    public long getSize() {
        return size;
    }

    /**
     * @return Scan position of chunk within the file.
     */
    public long getScan() {
        return scan;
    }

    public void putPropertyChunk(IFFChunk chunk) {
        if (propertyChunks == null) {
            propertyChunks = new HashMap<IFFChunk, IFFChunk>();
        }
        propertyChunks.put(chunk, chunk);
    }

    public IFFChunk getPropertyChunk(int id) {
        if (propertyChunks == null) {
            return null;
        }
        IFFChunk chunk = new IFFChunk(type, id);
        return propertyChunks.get(chunk);
    }

    public Iterable<IFFChunk> propertyChunks() {
        if (propertyChunks == null) {
            propertyChunks = new HashMap<IFFChunk, IFFChunk>();
        }
        return propertyChunks.keySet();
    }

    public void addCollectionChunk(IFFChunk chunk) {
        if (collectionChunks == null) {
            collectionChunks = new ArrayList<IFFChunk>();
        }
        collectionChunks.add(chunk);
    }

    public IFFChunk[] getCollectionChunks(int id) {
        if (collectionChunks == null) {
            return new IFFChunk[0];
        }
        int i = 0;
        for (IFFChunk chunk :collectionChunks) {
            if (chunk.id == id) {
                i++;
            }
        }
        IFFChunk[] array = new IFFChunk[i];
        i = 0;
        for (IFFChunk chunk :collectionChunks) {
            if (chunk.id == id) {
                array[i++] = chunk;
            }
        }
        return array;
    }

    public Iterable<IFFChunk> collectionChunks() {
        if (collectionChunks == null) {
            collectionChunks = new ArrayList<IFFChunk>();
        }
        return collectionChunks;
    }

    /**
     * Sets the data. Note: The array will not be cloned.
     */
    public void setData(byte[] data) {
        this.data = data;
    }

    /**
     * Gets the data. Note: The array will not be cloned.
     */
    public byte[] getData() {
        return data;
    }

    @Override
    public boolean equals(Object another) {
        if (another instanceof IFFChunk) {
            IFFChunk that = (IFFChunk) another;
            return (that.id == this.id && that.type == this.type);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return super.toString() + "{" + IFFParser.idToString(getType()) + "," + IFFParser.idToString(getID()) + "}";
    }
}
