package org.monte.media.impl.jcodec.containers.mp4;

import org.monte.media.impl.jcodec.containers.mp4.boxes.Box;
import org.monte.media.impl.jcodec.containers.mp4.boxes.Header;

public interface IBoxFactory {

    Box newBox(Header header);
}