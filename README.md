# MonteMedia 17.0

A Java library for processing still images, video, audio and meta-data.

This version updates the MonteMedia library to Java 17.

## MonteMedia for Java 17

The library is split up into a main module, additional modules and demo modules.

* org.monte.media    
  This is the main module. It includes the audio/video processing classes, the image processing classes.
* org.monte.media.jmf
  This module provides codecs to the Java Media Framework (JMF). The codecs can be used to play back videos,
  that were recorded with screen recorder with the JMF library. The codecs must be registered with the JMF Registry
  before they can be used. This module depends on the org.monte.media module.
* org.monte.media.javafx
  This module provides integration with JavaFX.
* org.monte.demo.*
 These are demo modules.

To launch the screen recorder enter:
java -p modules -m org.monte.media
