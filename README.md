# MonteMedia 2.0
A Java library for processing still images, video, audio and meta-data.


This version updates the MonteMedia library to Java 11.

## MonteMedia for Java 11
The library is split up into a main module, a JMF module and demo modules.

* org.monte.media    
  This is the main module. It includes the audio/video processing classes, the image processing classes and the screen recorder.
* org.monte.media.jmf
  This module provides codecs to the Java Media Framework (JMF). The codecs can be used to play back videos, that were recorded with screen recorder with the JMF library. The codecs must be registered with the JMF Registry before they can be used. This module depends on the org.monte.media module. 
* org.monte.media.javafx
  This module provides integration with JavaFX.
* org.monte.demo.*
 These are demo modules.

To launch the screen recorder enter:
java -p modules -m org.monte.media
