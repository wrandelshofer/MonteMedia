/*
 * @(#)Main.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.demo.movieconverter;

import org.monte.media.av.DefaultMovie;
import org.monte.media.av.Movie;
import org.monte.media.av.MovieReader;
import org.monte.media.av.Registry;
import org.monte.media.swing.Worker;
import org.monte.media.swing.datatransfer.DropFileTransferHandler;

import javax.swing.JFileChooser;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

/**
 * Main.
 * <p>
 * Symbols:
 * <dl>
 *     <dt>􀍉</dt><dd>video</dd>
 *     <dt>􀊃</dt><dd>play</dd>
 *     <dt>􀊄</dt><dd>play.fill</dd>
 *     <dt>􀊅</dt><dd>pause</dd>
 *     <dt>􀊆</dt><dd>pause.fill</dd>
 *     <dt>􀛶</dt><dd>stop</dd>
 *     <dt>􀛷</dt><dd>stop.fill</dd>
 *     <dt>􀢙</dt><dd>record.circle</dd>
 *     <dt>􀢚</dt><dd>record.circle.fill</dd>
 *     <dt>􀜪</dt><dd>stop.circle</dd>
 *     <dt>􀜫</dt><dd>stop.circle.fill</dd>
 *     <dt>􀊋</dt><dd>forward</dd>
 *     <dt>􀊌</dt><dd>forward.fill</dd>
 *     <dt>􀊏</dt><dd>forward.end</dd>
 *     <dt>􀊐</dt><dd>forward.end.fill</dd>
 *     <dt>􀊍</dt><dd>backward.end</dd>
 *     <dt>􀊎</dt><dd>backward.end.fill</dd>
 *     <dt>􀩪</dt><dd>forward.frame</dd>
 *     <dt>􀩫</dt><dd>forward.frame.fill</dd>
 *     <dt>􀩨</dt><dd>backward.frame</dd>
 *     <dt>􀩩</dt><dd>backward.frame.fill</dd>
 * </dl>
 *
 * @author Werner Randelshofer
 */
public class Main extends javax.swing.JFrame {
    private final static long serialVersionUID = 1L;

    private class Handler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            File f = new File(e.getActionCommand());
            if (isEnabled())
                setMovieFile(f);
        }

    }

    private Handler handler = new Handler();

    /**
     * Creates new form MovieConverterXMain
     */
    public Main() {
        initComponents();
        DropFileTransferHandler dfth = new DropFileTransferHandler(JFileChooser.FILES_ONLY);
        dfth.setActionListener(handler);
        setTransferHandler(dfth);
        movieConverterPanel.setTransferHandler(dfth);
    }

    public void setMovieFile(final File newFile) {
        setEnabled(false);
        setTitle(null);
        getRootPane().putClientProperty("Window.documentFile", null);
        new Worker<Movie>() {

            @Override
            protected Movie construct() throws Exception {

                MovieReader r = Registry.getInstance().getReader(newFile);
                if (r == null) throw new IOException("no reader");
                DefaultMovie m = new DefaultMovie();
                r.getMovieDuration();// this ensures that we realize the reader!
                m.setReader(r);
                return m;
            }

            @Override
            protected void done(Movie movie) {
                getRootPane().putClientProperty("Window.documentFile", newFile);
                setTitle(newFile.getName());
                movieConverterPanel.setMovie(movie);
            }

            @Override
            protected void finished() {
                setEnabled(true);
            }


        }.start();
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        movieConverterPanel = new org.monte.demo.movieconverter.MovieConverterPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().add(movieConverterPanel, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                new Main().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.monte.demo.movieconverter.MovieConverterPanel movieConverterPanel;
    // End of variables declaration//GEN-END:variables
}
