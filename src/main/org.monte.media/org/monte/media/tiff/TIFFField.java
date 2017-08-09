/* @(#)TIFFField.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Licensed under the MIT License.
 */
package org.monte.media.tiff;

import java.awt.image.BufferedImage;

/**
 * A field in a {@link TIFFDirectory}.
 *
 * @author Werner Randelshofer
 * @version 1.0 2010-07-25 Created.
 */
public class TIFFField extends TIFFNode {

    /** The data of this field. */
    private Object data;
    
    /** The type of this field, if no IFDEntry is provided.*/
    private IFDDataType type;

    public TIFFField(TIFFTag tag, Object data, IFDDataType type) {
        super(tag);
        this.data = data;
        this.type = type;
        type.checkType(data);
    }

    public TIFFField(TIFFTag tag, Object data, IFDEntry entry) {
        super(tag);
        this.data = data;
        this.ifdEntry = entry;
        IFDDataType.valueOf(entry.getTypeNumber()).checkType(data);
    }
    
    private void checkType(Object data, IFDDataType type) {
        
    }
    

    /** Returns a description of the field. If known. */
    public String getDescription() {
        return getTag().getDescription(getData());
    }

    public IFDDataType getType() {
        if (ifdEntry != null) {
            return IFDDataType.valueOf(ifdEntry.getTypeNumber());
        } else {
            return type;
        }
    }

    public long getCount() {
        if (ifdEntry != null) {
            return ifdEntry.getCount();
        } else if (data instanceof Object[]) {
            return ((Object[]) data).length;
        } else {
            return 1;
        }
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        if (data==null) return super.toString();
        return "TIFFField "+tag+"="+ data.toString();
    }
}
