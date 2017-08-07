/* @(#)module-info.java
 * Copyright © 2007 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */

module org.monte.media.amigamovie {
    requires java.desktop;
    
    requires transitive org.monte.media.movie;
    requires transitive org.monte.media.amiga;
    
    exports org.monte.media.amigamovie;
}
