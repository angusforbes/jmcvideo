/* JMCMovieGL.java ~ Mar 21, 2009 */
package jmcvideo;

/**
 *
 * @author angus
 */
import com.sun.media.jmc.event.BufferDownloadedProgressChangedEvent;
import com.sun.media.jmc.event.VideoRendererEvent;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureCoords;
import com.sun.opengl.util.texture.TextureData;
import com.sun.opengl.util.texture.TextureIO;
import java.io.File;
import java.net.URI;
import java.net.URL;
import javax.media.opengl.GL;
import processing.core.PApplet;

/**
 * JMCMovieGL provides methods to load a movie onto an openGL texture.
 */
public class JMCMovieGL extends JMC
{

  private boolean DEBUG = false;
  public float alpha = 1f;
  public boolean isTextureWaiting = false;
  public TextureData textureData = null;
  public Texture texture = null;

  /**
   * Creates an instance of JMCMovieGL by loading a movie with the specified URL.
   * @param parent
   * @param url
   */
  public JMCMovieGL(PApplet parent, URL url)
  {
    initializeVideo(parent, VideoUtils.toURI(url));
  }

  /**
   * Creates an instance of JMCMovieGL by loading a movie with the specified URI.
   * @param parent
   * @param uri
   */
  public JMCMovieGL(PApplet parent, URI uri)
  {
    initializeVideo(parent, uri);
  }

  /**
   * Creates an instance of JMCVideoGL by loading a movie
   * from a specified file.
   * @param parent
   * @param file
   */
  public JMCMovieGL(PApplet parent, File file)
  {
    initializeVideo(parent, VideoUtils.toURI(file));
  }

  /**
   * Creates an instance of JMCMovieGL by loading a movie from a file in the data directory with the specified filename.
   * @param parent A PApplet instance.
   * @param filename The name of the file.
   */
  public JMCMovieGL(PApplet parent, String filename)
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

  public void videoFrameUpdated(VideoRendererEvent rendererEvent)
  {
    if (DEBUG)
    {
      System.out.println("frame: " + rendererEvent.getFrameNumber());
    }

    handleLoopingBehavior();

    paintBufferedImage();

    textureData = TextureIO.newTextureData(bufferedImage, false); //mipmapping=false
    isTextureWaiting = true;
  }

  /**
   * Creates a Texture object from TextureData.
   */
  public void initializeTexture()
  {
    this.texture = TextureIO.newTexture(this.textureData);
  }

  /**
   * Binds the video frame to this object's texture. Not needed if you are
   * calling the automatic "image" methods.
   * @param gl
   */
  public boolean texture(GL gl)
  {
    if (this.textureData != null)
    {
      if (this.texture == null)
      {
        initializeTexture();
      }
      else
      {
        if (isTextureWaiting == true)
        {
          this.texture.updateImage(this.textureData);
          this.isTextureWaiting = false;
        }
      }

      gl.glColor4f(1f, 1f, 1f, alpha);
      this.texture.bind();

      return true;
    }
    return false;
  }

  /**
   * Draws the image across the entire bounds of the parent canvas.
   * @param gl
   */
  public void frameImage(GL gl)
  {
    frameImage(gl, 0f);
  }

  /**
   * Draws the image across the entire bounds, minus a specified inset, of the parent canvas.
   * @param gl
   * @param inset Amount of space between the frame and the video, in pixels.
   */
  public void frameImage(GL gl, float inset)
  {
    frameVideo(parent.width, parent.height, inset);
    image(gl);
  }

  /**
   * Draws the image across the center of the parent canvas, preserving the original aspect ratio of the video.
   * @param gl
   */
  public void centerImage(GL gl)
  {
    centerImage(gl, 0f);
  }

  /**
   * Draws the image across the center of the parent canvas, preserving the original aspect ratio of the video.
   * @param gl
   * @param inset The minimum amount of space between the frame and the video, in pixels
   */
  public void centerImage(GL gl, float inset)
  {
    centerVideo(parent.width, parent.height, inset); //really this should just be done if resizing
    image(gl);
  }

  /**
   * Draws the video texture at the specified coordinates.
   * @param gl The openGL context
   * @param x
   * @param y
   * @param w
   * @param h
   */
  public void image(GL gl, float x, float y, float w, float h)
  {
    setBounds(x, y, w, h);

    image(gl);
  }

  /**
   * Draws the video texture with the assumption that the bounds have been set previously.
   * @param gl A valid openGL context.
   */
  public void image(GL gl)
  {
    if (texture(gl))
    {
      gl.glEnable(GL.GL_TEXTURE_2D);
      //gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE);
      gl.glBegin(gl.GL_QUADS);

      TextureCoords tc = texture.getImageTexCoords();

      gl.glTexCoord2f(tc.left(), tc.top());
      gl.glVertex2f(x, y);
      gl.glTexCoord2f(tc.right(), tc.top());
      gl.glVertex2f(x + w, y);
      gl.glTexCoord2f(tc.right(), tc.bottom());
      gl.glVertex2f(x + w, y + h);
      gl.glTexCoord2f(tc.left(), tc.bottom());
      gl.glVertex2f(x, y + h);

      gl.glEnd();
      gl.glDisable(GL.GL_TEXTURE_2D);
    }
  }
}
