package org.monte.media.impl.jcodec.containers.mp4.boxes;

import org.monte.media.impl.jcodec.common.StringUtils;
import org.monte.media.impl.jcodec.common.UsedViaReflection;
import org.monte.media.impl.jcodec.common.io.NIOUtils;
import org.monte.media.impl.jcodec.containers.mp4.IBoxFactory;
import org.monte.media.impl.jcodec.platform.Platform;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.ByteBuffer;

import static org.monte.media.impl.jcodec.common.Preconditions.checkState;

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
 * <p>
 * An MP4 file struncture (box).
 *
 * @author The JCodec project
 */
public abstract class Box {
    public Header header;
    public static final int MAX_BOX_SIZE = 128 * 1024 * 1024;

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public static @interface AtomField {
        int idx();
    }

    @UsedViaReflection
    public Box(Header header) {
        this.header = header;
    }

    public Header getHeader() {
        return header;
    }

    public abstract void parse(ByteBuffer buf);

    public void write(ByteBuffer buf) {
        ByteBuffer dup = buf.duplicate();
        NIOUtils.skip(buf, 8);
        doWrite(buf);

        header.setBodySize(buf.position() - dup.position() - 8);
        checkState(header.headerSize() == (long) 8);
        header.write(dup);
    }

    protected abstract void doWrite(ByteBuffer out);

    public abstract int estimateSize();

    public String getFourcc() {
        return header.getFourcc();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        dump(sb);
        return sb.toString();

    }

    protected void dump(StringBuilder sb) {
        sb.append("{\"tag\":\"" + header.getFourcc() + "\"}");
    }

    public static Box terminatorAtom() {
        return createLeafBox(new Header(Platform.stringFromBytes(new byte[4])), ByteBuffer.allocate(0));
    }

    public static String[] path(String path) {
        return StringUtils.splitC(path, '.');
    }

    public static LeafBox createLeafBox(Header atom, ByteBuffer data) {
        LeafBox leaf = new LeafBox(atom);
        leaf.data = data;
        return leaf;
    }

    public static Box parseBox(ByteBuffer input, Header childAtom, IBoxFactory factory) {
        Box box = factory.newBox(childAtom);

        if (childAtom.getBodySize() < Box.MAX_BOX_SIZE) {
            box.parse(input);
            return box;
        } else {
            return new LeafBox(Header.createHeader("free", 8));
        }
    }

    public static <T extends Box> T asBox(Class<T> class1, Box box) {
        try {
            T res = Platform.newInstance(class1, new Object[]{box.getHeader()});
            ByteBuffer buffer = ByteBuffer.allocate((int) box.getHeader().getBodySize());
            box.doWrite(buffer);
            ((java.nio.Buffer) buffer).flip();
            res.parse(buffer);
            return res;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static class LeafBox extends Box {
        ByteBuffer data;

        public LeafBox(Header atom) {
            super(atom);
        }

        public LeafBox(Header atom, ByteBuffer data) {
            super(atom);
            this.data = data;
        }

        public void parse(ByteBuffer input) {
            data = NIOUtils.read(input, (int) header.getBodySize());
        }

        public ByteBuffer getData() {
            return data.duplicate();
        }

        @Override
        protected void doWrite(ByteBuffer out) {
            NIOUtils.write(out, data);
        }

        @Override
        public int estimateSize() {
            return data.remaining() + Header.estimateHeaderSize(data.remaining());
        }
    }

}