package org.monte.quicktimedemo;

import java.io.File;
import java.net.URI;
import org.monte.media.quicktime.QuickTimeDeserializer;
import org.monte.media.quicktime.QuickTimeMeta;

/**
 * {@code QuickTimeReaderDemo}.
 *
 * @author Werner Randelshofer
 * @version 1.0 2013-03-21 Created.
 */
public class QuickTimeReaderDemo {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        URI uri=new File(System.getProperty("user.home"),"Movies/Untitled.mov").toURI();
        QuickTimeDeserializer d=new QuickTimeDeserializer();
        QuickTimeMeta m=d.read(uri);
        
        System.out.println(m);
    }

}
