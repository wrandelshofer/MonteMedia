/* @(#)MovieConverterPanel.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.demo.movieconverter;

import org.monte.media.av.Buffer;
import org.monte.media.av.Movie;
import org.monte.media.swing.Worker;
import org.monte.media.math.Rational;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.TransferHandler;


/**
 * MovieConverterPanel.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class MovieConverterPanel extends javax.swing.JPanel {
    private final static long serialVersionUID = 1L;

    private ExecutorService executor;
private Buffer imageBuffer=new Buffer();
    private class Handler implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName() == Movie.INSERTION_POINT_PROPERTY) {
                updateImage();
            }
        }
    }
    private Handler handler = new Handler();
    private long imageTime = -1;

    /** Creates new form MovieConverterPanel */
    public MovieConverterPanel() {
        initComponents();

    }

    @Override
    public void setTransferHandler(TransferHandler newHandler) {
        super.setTransferHandler(newHandler);
        movieControlPanel.setTransferHandler(newHandler);
        jPanel1.setTransferHandler(newHandler);
        toolBar.setTransferHandler(newHandler);
        toolBar.putClientProperty("Quaqua.ToolBar.style", "bottom");
    }

    private void updateImage() {
        final Movie movie = getMovie();
        if (movie == null) {
            return;
        }

        execute(new Worker<BufferedImage>() {

            @Override
            protected BufferedImage construct() throws Exception {
                Rational time=movie.getInsertionPoint(); 
                
                
                return null;
            }

            @Override
            protected void done(BufferedImage value) {
                imagePanel.setImage(value);
            }
            
        });
    }

    public void execute(Runnable worker) {
        if (executor == null) {
            executor = Executors.newSingleThreadExecutor();
        }
        executor.execute(worker);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        toolBar = new javax.swing.JToolBar();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        movieControlPanel = new org.monte.media.swing.movie.JMovieControlPanel();
        imagePanel = new org.monte.media.swing.ImagePanel();

        setLayout(new java.awt.BorderLayout());

        toolBar.setFloatable(false);
        toolBar.setRollover(true);

        jLabel1.setText(" ");
        toolBar.add(jLabel1);

        add(toolBar, java.awt.BorderLayout.PAGE_END);

        jPanel1.setLayout(new java.awt.BorderLayout());
        jPanel1.add(movieControlPanel, java.awt.BorderLayout.SOUTH);
        jPanel1.add(imagePanel, java.awt.BorderLayout.CENTER);

        add(jPanel1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.monte.media.swing.ImagePanel imagePanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private org.monte.media.swing.movie.JMovieControlPanel movieControlPanel;
    private javax.swing.JToolBar toolBar;
    // End of variables declaration//GEN-END:variables

    public void setMovie(Movie movie) {
        Movie oldValue = movieControlPanel.getMovie();
        if (oldValue != null) {
            oldValue.removePropertyChangeListener(handler);
        }

        movieControlPanel.setMovie(movie);
        if (movie != null) {
            movie.addPropertyChangeListener(handler);
        }
        imageTime = -1;
      
        updateImage();
    }

    private Movie getMovie() {
        return movieControlPanel.getMovie();
    }
}
