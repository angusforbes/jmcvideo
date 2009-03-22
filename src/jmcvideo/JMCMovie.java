/* JMCMovie.java ~ Mar 21, 2009 */
package jmcvideo;

/**
 *
 * @author angus
 */
import java.lang.reflect.*;
import com.sun.media.jmc.MediaProvider;
import com.sun.media.jmc.control.AudioControl;
import com.sun.media.jmc.control.VideoRenderControl;
import com.sun.media.jmc.event.BufferDownloadedProgressChangedEvent;
import com.sun.media.jmc.event.VideoRendererEvent;
import com.sun.media.jmc.event.VideoRendererListener;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureData;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.net.URI;
import java.net.URL;
import processing.core.*;

/**
 * JMCMovie provides methods to load a movie into a PImage pixel buffer.
 */
public class JMCMovie extends JMC //implements PConstants, VideoRendererListener//,
{
  private boolean DEBUG = false; //true;

  /**
   * Creates an instance of JMCMovie by loading a movie with the specified URL.
   * @param parent
   * @param url
   */
  public JMCMovie(PApplet parent, URL url)
  {
    initializeVideo(parent, VideoUtils.toURI(url));
  }

  /**
   * Creates an instance of JMCMovie by loading a movie with the specified URI.
   * @param parent
   * @param uri
   */
  public JMCMovie(PApplet parent, URI uri)
  {
    initializeVideo(parent, uri);
  }

  /**
   * Creates an instance of JMCVideo by loading a movie
   * from a specified file.
   * @param parent
   * @param file
   */
  public JMCMovie(PApplet parent, File file)
  {
    initializeVideo(parent, VideoUtils.toURI(file));
  }

  /**
   * Creates an instance of JMCMovie by loading a movie from a file in the data directory
   * with the specified filename.
   * @param parent A PApplet instance.
   * @param filename The name of the file.
   */
  public JMCMovie(PApplet parent, String filename)
  {
    initializeVideo(parent, VideoUtils.toURI(new File(parent.dataPath(filename))));
  }
  

  /**
   * JMC callback method when receiving new buffering information.
   * Testing this for loading large and/or streaming files...
   * @param bufferEvent
   */
  public void mediaDownloadProgressChanged(BufferDownloadedProgressChangedEvent bufferEvent)
  {
    /*
    double progress = bufferEvent.getProgress();
    double timestamp = bufferEvent.getTimestamp();
    String description = bufferEvent.toString();

    if (DEBUG)
    {
      System.out.println("download PROGRESS = " + progress + " TIMESTAMP: " + timestamp + " description " +
        " = " + description + " source? " + bufferEvent.getSource());
    }
    */
  }

  /**
   * JMC callback method when receiving a new video frame. If the parent does not contain
   * If the parent contains a movieEvent() method, then we let that method determine how to set the
   * pixel array by calling the read() method, otherwise we just call the read() method ourselves.
   * @param rendererEvent The new frame to process.
   */
  public void videoFrameUpdated(VideoRendererEvent rendererEvent)
  {
    if (DEBUG)
    {
      System.out.println("frame: " + rendererEvent.getFrameNumber());
    }

    handleLoopingBehavior();
    paintBufferedImage();
    read();
  }

  /**
   * Updates the JMCMovie by writing the current video frame into the pixel array.
   */
  public void read()
  {
    //((ByteBuffer) textureData.getBuffer()).order(ByteOrder.nativeOrder()).asIntBuffer().get(pixels);
    // this works too...
    raster = bufferedImage.getRaster();
    raster.getDataElements(0, 0, width, height, pixels);

    updatePixels();
  }


  /**
   * Draws the image within the previously set bounds of the parent canvas.
   */
  public void image()
  {
    parent.image(this, x, y, w, h);
  }


  /**
   * Draws the image within the specified location of the parent canvas.
   */
  public void image(float xloc, float yloc)
  {
    setBounds(xloc, yloc, w, h);
    image();
  }

  /**
   * Draws the image within the specified bounds of the parent canvas.
   */
  public void image(float x, float y, float w, float h)
  {
    setBounds(x, y, w, h);
    image();
  }


  /**
   * Draws the image across the entire bounds of the parent canvas.
   */
  public void frameImage()
  {
    frameImage(0f);
  }

  /**
   * Draws the image across the entire bounds, minus a specified inset, of the parent canvas.
   * @param inset Amount of space between the frame and the video, in pixels.
   */
  public void frameImage(float inset)
  {
    frameVideo(parent.width, parent.height, inset);
    image();
  }

  /**
   * Draws the image across the center of the parent canvas, preserving the original aspect ratio of the video.
   */
  public void centerImage()
  {
    centerImage(0f);
  }

  /**
   * Draws the image across the center of the parent canvas, preserving the original aspect ratio of the video.
   * @param inset The minimum amount of space between the frame and the video, in pixels
   */
  public void centerImage(float inset)
  {
    centerVideo(parent.width, parent.height, inset); //really this should just be done if resizing
    image();
  }

}

