# MonteMedia 2.0
A Java library for processing still images, video, audio and meta-data.


This version updates the MonteMedia library to Java 8 and Java 9.

## MonteMedia for Java 8
The entire library is included in one Jar file.

* The jar file includes the audio/video processing classes, the image processing classes, the screen recorder and the demo classes, and the JMF codecs.
* The main class of the Jar file is the screen recorder application.
* The codecs can be used to play back videos, that were recorded with screen recorder with the JMF library. The codecs must be registered with the JMF Registry before they can be used. 

To launch the screen recorder enter:
java -jar MonteMedia.jar

On some operating systems, you can launch the screen recorder by double clicking the MonteMedia.jar file.


## MonteMedia for Java 9
The library is split up into a main module, a JMF module and demo modules.

* org.monte.media    
  This is the main module. It includes the audio/video processing classes, the image processing classes and the screen recorder.
* org.monte.media.jmf
  This module provides codecs to the Java Media Framework (JMF). The codecs can be used to play back videos, that were recorded with screen recorder with the JMF library. The codecs must be registered with the JMF Registry before they can be used. This module depends on the org.monte.media module. 
* org.monte.demo.*
 These are demo modules.

To launch the screen recorder enter:
java -p modules -m org.monte.media

Double clicking the org.monte.media.jar file does not work, because then the VM will not register the services provided by the module.
