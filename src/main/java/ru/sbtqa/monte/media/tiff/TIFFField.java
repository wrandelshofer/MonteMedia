/* @(#)TIFFField.java
 * Copyright © 2010 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package ru.sbtqa.monte.media.tiff;

import static ru.sbtqa.monte.media.tiff.IFDDataType.valueOf;

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
        valueOf(entry.getTypeNumber()).checkType(data);
    }
    
    private void checkType(Object data, IFDDataType type) {
        
    }
    

    /** Returns a description of the field. If known. */
    public String getDescription() {
        return getTag().getDescription(getData());
    }

    public IFDDataType getType() {
        if (ifdEntry != null) {
            return valueOf(ifdEntry.getTypeNumber());
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
