/* @(#)Movie.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.av;

import org.monte.media.math.Rational;
import java.beans.PropertyChangeListener;
import java.net.URI;
import org.monte.media.av.Format;

/**
 * A {@code movie} provides an editable container for media samples in a
 * time and tracks coordinate system.
 * <p>
 * <b>Note this interface is work in progress. It is not part of the API yet.</b>
 * <p>
 * A movie has a time dimension and a track dimension. The time dimension is
 * the duration of the movie. The track dimension is a list of tracks.
 * A track has a duration and a time offset.
 * <pre>
 * Movie duration: |------------------------------------------------|
 * Track 0:         LOREMIPSUM
 * Track 1:         ..............THEQUICKBROWNFOXJUMPSOVERTHELAZYDOG
 * Track 2:         .....FARFARAWAYBEHINDTHEMOUNTAINS
 * Track 3:         .......................THENIGHTWASHOTANDWET
 *    ... 
 * </pre>
 * <p>
 * A track provides random access to decoded samples, either by index or
 * by a time offset.
 * <p>
 * Internally, a track consists of a media and a sequence of clips. 
 * The duration of a track is the sum of the duration of its clips.
 * <p>
 * A clips selects a contiguous sequence of samples from a media. It specifies a 
 * playback rate, and filters which are applied to the samples before
 * they are presented. The playback rate can be negative. 
 * The duration of a clip is the duration of the samples multiplied by its 
 * absolute playback rate.
 * <p>
 * A media is made up of a sequence of samples. It provides random access to
 * samples, either by index or by a time offset. Media provides decoding
 * information about the samples. Including a decoding sequence for predicted
 * samples. 
 * <p>
 * A sample provides encoded data for a specified duration. A sample is a
 * sequence of bytes located in a file at a specific offset and length.
 * <pre>
 * Track 1:         THEQUICKBROWNFOXJUMPSOVERTHELAZYDOG
 * Edits:           (0)( 1 )(  2   )(   3   )(4)(  5  )
 * 
 * Media:           BLAQUICKBLAJUMPSOVERBLALAZYDOGBROWNFOXBLATHEBLABLA
 *                     ( 1 )   (   3   )   (  5  )(  2   )   (0)
 *                                                           (4)
 *
 * File A:          B.L..A..Q..U..IC.K
 * File B:          BLA..J.UM.PS.OV.
 * File C:          ...ER.BLA.LA.ZYD...DOG
 * File D:          BR..OWNF..OXB..LAT..HEB..LAB.LA..
 *</pre>
 *
 * @author Werner Randelshofer
 * @version $Id: Movie.java 364 2016-11-09 19:54:25Z werner $
 */
public interface Movie {

    public final static String INSERTION_POINT_PROPERTY = "insertionPoint";
    public final static String SELECTION_START_PROPERTY = "selectionStart";
    public final static String SELECTION_END_PROPERTY = "selectionEnd";
    public final static String URI_PROPERTY = "uri";

    /** Returns the total duration of the movie in seconds. */
    public Rational getDuration();

    /** Sets the position of the insertion point in seconds. */
    public void setInsertionPoint(Rational seconds);

    /** Returns the position of the insertion point in seconds.
     * If a movie has a duration of n, then there are n+1 insertion points.
     */
    public Rational getInsertionPoint();

    /** Returns the position of the in point in seconds. */
    public Rational getSelectionStart();

    /** Sets the position of the in point in seconds. */
    public void setSelectionStart(Rational in);

    /** Returns the position of the out point in seconds. */
    public Rational getSelectionEnd();

    /** Sets the position of the out point in seconds. */
    public void setSelectionEnd(Rational out);

    /** Returns the frame number for the specified time in seconds. */
    public long timeToSample(int track, Rational seconds);

    public void addPropertyChangeListener(PropertyChangeListener listener);

    public void removePropertyChangeListener(PropertyChangeListener listener);

    public Rational sampleToTime(int track, long sample);

    public int getTrackCount();

    public Format getFormat(int track);

    public Format getFileFormat();

    public MovieReader getReader();
    
    /** Returns the URI associated with this movie. This is typically a file,
     * but can be null if the movie only resides in memory. 
     * 
     * @return The Movie URI.
     */
    public URI getURI();
}
