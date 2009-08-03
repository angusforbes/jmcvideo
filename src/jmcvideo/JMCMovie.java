/* JMCMovie.java ~ Mar 21, 2009 */
package jmcvideo;

/**
 *
 * @author angus
 */
import com.sun.media.jmc.event.VideoRendererEvent;
import java.io.File;
import java.net.URI;
import java.net.URL;
import processing.core.*;

/**
 * JMCMovie provides methods to load a movie into a PImage pixel buffer.
 */
public class JMCMovie extends JMC //implements PConstants, VideoRendererListener//,
{
  /**
   * Creates an instance of JMCMovie by loading a movie with the specified URL.
   * @param parent
   * @param url
   */
  public JMCMovie(PApplet parent, URL url, int pixelFormat)
  {
    initializeVideo(parent, VideoUtils.toURI(url), pixelFormat);
  }

  /**
   * Creates an instance of JMCMovie by loading a movie with the specified URI.
   * @param parent
   * @param uri
   */
  public JMCMovie(PApplet parent, URI uri, int pixelFormat)
  {
    initializeVideo(parent, uri, pixelFormat);
  }

  /**
   * Creates an instance of JMCVideo by loading a movie
   * from a specified file.
   * @param parent
   * @param file
   */
  public JMCMovie(PApplet parent, File file, int pixelFormat)
  {
    initializeVideo(parent, VideoUtils.toURI(file), pixelFormat);
  }

  /**
   * Creates an instance of JMCMovie by loading a movie from a file in the data directory
   * with the specified filename.
   * @param parent A PApplet instance.
   * @param filename The name of the file.
   */
  public JMCMovie(PApplet parent, String filename, int pixelFormat)
  {
    initializeVideo(parent, VideoUtils.toURI(new File(parent.dataPath(filename))), pixelFormat);
  }
  

  /**
   * JMC callback method when receiving a new video frame. If the parent does not contain
   * If the parent contains a movieEvent() method, then we let that method determine how to set the
   * pixel array by calling the read() method, otherwise we just call the read() method ourselves.
   * @param rendererEvent The new frame to process.
   */
  public void videoFrameUpdated(VideoRendererEvent rendererEvent)
  {
    if (!isPlaying() || !isReady())
    {
      return;
    }

//    System.out.println("playbackPercentage = " + getPlaybackPercentage());
//    System.out.println("now/duration = " + getCurrentTime() +"/"+ getDuration());

    if (isBouncing == true)
    {
      handleBouncingBehavior();
    }

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
   * Draws the image at the specified location within the previously set bounds of the parent canvas.
   */
  public void image(float xloc, float yloc)
  {
    setBounds(xloc, yloc, w, h);
    image();
  }

  /**
   * Draws the image at the specified location within the specified bounds of the parent canvas.
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

