/* JMC.java ~ Mar 21, 2009 */

package jmcvideo;

import com.sun.media.jmc.MediaProvider;
import com.sun.media.jmc.control.AudioControl;
import com.sun.media.jmc.control.VideoRenderControl;
import com.sun.media.jmc.event.BufferDownloadListener;
import com.sun.media.jmc.event.VideoRendererListener;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.net.URI;
import java.net.URL;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;

/**
 * JMC is an abstract class which provides the base functionality for
 * Processing to interact with the JMC rendering callbacks. You should either
 * use JMCMovie to access the video framss as a PImage pixel buffer or JMCMovieGL to
 * render the frames to a openGL texture. Or you can extend from this class
 * to create your own renderer.
 * @author angus
 */
public abstract class JMC extends PImage implements PConstants, VideoRendererListener, BufferDownloadListener
{

  public int vw = 0;
  public int vh = 0;

  boolean play = true;
  boolean repeat = false;
  boolean bounce = false;

  WritableRaster raster = null;
  AudioControl ac;
  VideoRenderControl vrc;
  MediaProvider mp = null;
  public BufferedImage bufferedImage = null;
  Graphics2D g2d = null;
  Dimension videoSize = null;

  public float x,  y,  w,  h;

  public void initializeVideo(PApplet parent, URI uri)
  {
    init(1,1,RGB);
    this.parent = parent;
    parent.registerDispose(this);

    this.mp = new MediaProvider(uri);
    ac = mp.getControl(AudioControl.class);
    vrc = mp.getControl(VideoRenderControl.class);
    vrc.addVideoRendererListener(this);

    //mp.addBufferDownloadListener(this);
    //mp.setPlayCount(100);
  }

  /**
   * Switch to a new video.
   * @param filename The name of the new video file in the data directory.
   */
  public void switchVideo(String filename)
  {
    mp.setSource(VideoUtils.toURI(new File(parent.dataPath(filename))));
    mp.play();
  }

  /**
   * Switch to a new video.
   * @param file A video file.
   */
  public void switchVideo(File file)
  {
    mp.setSource(VideoUtils.toURI(file));
    mp.play();
  }

  /**
   * Switch to a new video.
   * @param url The URL of a video file.
   */
  public void switchVideo(URL url)
  {
    mp.setSource(VideoUtils.toURI(url));
    mp.play();
  }

  /**
   * Switch to a new video.
   * @param uri The URI of a video file.
   */
  public void switchVideo(URI uri)
  {
    mp.setSource(uri);
    mp.play();
  }



  /**
   * Handles what happens when the playback has reached the end (or the beginning
   * if playing backwards). This is pretty wonky. Hopefully it will be cleaned up
   * in the next version of JMC.
   */
  public void handleLoopingBehavior()
  {
    if (this.repeat == true && getRate() > 0 && getPlaybackPercentage() >= 1.0)
    {
      setPlaybackPercentage(Math.random() * .001);
    }
    else if (this.bounce == true && getRate() > 0 && getPlaybackPercentage() >= 1.0)
    {
      setRate(-1.0);
      setPlaybackPercentage(1.0 - (Math.random() * .001));
    }
    else if (this.bounce == true && getRate() < 0 && getPlaybackPercentage() <= 0.0)
    {
      setRate(1.0);
      setPlaybackPercentage(Math.random() * .001);
    }
  }

  /**
   * Paints the newest video frame onto a BufferedImage.
   */
  public void paintBufferedImage()
  {
    if (bufferedImage == null || videoSize == null || vw <= 0 || vh <= 0)
    {
      setupBufferedImage();
      return;
    }

    g2d = bufferedImage.createGraphics();
    vrc.paintVideoFrame(g2d, new Rectangle(0, 0, vw, vh));
    g2d.dispose();
  }


  //Creates a BufferedImage the same size as the Videoa and initializes the PImage pixel buffer.
  public void setupBufferedImage()
  {
    videoSize = vrc.getFrameSize();
    bufferedImage = new BufferedImage((int) videoSize.getWidth(), (int) videoSize.getHeight(), BufferedImage.TYPE_INT_RGB);

    //g2d = bufferedImage.createGraphics();

    vw = (int) videoSize.getWidth();
    vh = (int) videoSize.getHeight();

    super.init(vw, vh, RGB);
    loadPixels();
  }


  /**
   * Explicitly sets the bounds of this PImage.
   * @param x
   * @param y
   * @param w
   * @param h
   */
  public void setBounds(float x, float y, float w, float h)
  {
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
  }
  
  /**
   * Sets the bounds
   * @param frameWidth
   * @param frameHeight
   * @param inset
   */
  public void frameVideo(float frameWidth, float frameHeight, float inset)
  {
    this.x = inset;
    this.w = frameWidth - (2 * inset);
    this.y = inset;
    this.h = frameHeight - (2 * inset);
  }

  public void centerVideo()
  {
    centerVideo(parent.width, parent.height, 0f);
  }

  public void centerVideo(float inset)
  {
    centerVideo(parent.width, parent.height, inset);
  }

  public void centerVideo(float frameWidth, float frameHeight, float inset)
  {
    if (videoSize == null) //video not ready yet
    {
      return;
    }

    float aspect_frame = frameWidth / frameHeight;
    float aspect_video = (float) (videoSize.getWidth() / videoSize.getHeight());

    if (aspect_frame > aspect_video)
    {
      this.h = frameHeight - (inset * 2);
      this.w = (float) ((videoSize.getWidth() / videoSize.getHeight()) * this.h);
      this.x = 0 + ((frameWidth - this.w) * .5f);
      this.y = inset;
    }
    else
    {
      this.w = frameWidth - (inset * 2);
      this.h = (float) ((videoSize.getHeight() / videoSize.getWidth()) * this.w);
      this.x = inset;
      this.y = 0 + ((frameHeight - this.h) * .5f);
    }
  }

  /**
   * Gets a nicely formatted version of the current playback position of the video.
   * @return The playback position.
   */
  public String getTimeString()
  {
    return VideoUtils.formatMediaTime(mp.getMediaTime());
  }

  /**
   * Gets a nicely formatted version of the duration of the video.
   * @return The duration of the video.
   */
  public String getDurationString()
  {
    return VideoUtils.formatMediaTime(mp.getDuration());
  }

  /**
   * Gets the duration of the video.
   * @return The duration of the video.
   */
  public double getDuration()
  {
    return this.mp.getDuration();
  }

  /**
   * Increase or decrease the rate of video playback by a specified amount.
   * @param amt Amount by which to change the rate.
   */
  public void changeRate(double amt)
  {
    mp.setRate(mp.getRate() + amt);
  }

  /**
   * Sets the rate of video playback
   * @param rate The rate to set the playback to.
   */
  public void setRate(double rate)
  {
    mp.setRate(rate);
  }

  /**
   * Gets the current rate of video playback.
   * @return The current rate.
   */
  public double getRate()
  {
    return mp.getRate();
  }

  /**
   * Pauses the video playback.
   */
  public void pause()
  {
    mp.pause();
  }

  /**
   * Begins video playback.
   */
  public void play()
  {
    this.repeat = false;
    this.bounce = false;
    mp.play();
  }

  /**
   * Begins video playback, looping forever
   */
  public void loop()
  {
    this.repeat = true;
    this.bounce = false;
    mp.play();
  }

  /**
   * Begins video playback, playing forwards and then backwards forever if backward playback is supported.
   */
  public void bounce()
  {
    this.bounce = true;
    this.repeat = false;
    mp.play();
  }

  /**
   * Determine if the video is currently playing.
   * @return A boolean indicating whether or not the video is currently playing.
   */
  public boolean isPlaying()
  {
    return mp.isPlaying();
  }

  /**
   * Increase or decrease the volume by a specified amount.
   * @param amt Amount by which to change the volume.
   */
  public void changeVolume(float amt)
  {
    this.ac.setVolume(this.ac.getVolume() + amt);
  }

  /**
   * Sets the volume to the specified volume.
   * @param vol
   */
  public void setVolume(float vol)
  {
    this.ac.setVolume(vol);
  }

  /**
   * Gets the current volume.
   * @return The current volume.
   */
  public float getVolume()
  {
    return this.ac.getVolume();
  }

  /**
   * Mutes the video.
   */
  public void mute()
  {
    this.ac.setMute(true);
  }

  /**
   * Determine if the video is muted or not.
   * @return A boolean indicating if the video is muted.
   */
  public boolean isMuted()
  {
    return this.ac.isMuted();
  }

  /**
   * Unmutes the video.
   */
  public void unmute()
  {
    this.ac.setMute(false);
  }

  /**
   * Make the video mute if it is not, or unmutes it if it is.
   */
  public void toggleMute()
  {
    this.ac.setMute(!this.ac.isMuted());
  }

  /**
   * Gets the current video playback position.
   * @return The current time.
   */
  public double getCurrentTime()
  {
    return this.mp.getMediaTime();
  }

  /**
   * Sets the current video playback position.
   * @param time
   */
  public void setCurrentTime(double time)
  {
    this.mp.setMediaTime(time);
  }

  /**
   * Gets the position of the video playback as a percentage between the
   * start time and the end time of the video.
   * @return A number between 0.0 and 1.0, where 0.0 is the start of the video and 1.0 is the end of the video.
   */
  public double getPlaybackPercentage()
  {
    return getCurrentTime() / getDuration();
  }

  /**
   * Sets the position of the video playback using a percentage between the
   * start time and the end time of the video.
   * @param perc A number between 0.0 and 1.0, where 0.0 is the start of the video and 1.0 is the end of the video.
   */
  public void setPlaybackPercentage(double perc)
  {
    //System.out.println("setting playback to ( " + this.mp.getDuration() + ") * (" + perc + " ... " + (this.mp.getDuration() * perc));
    this.mp.setMediaTime(this.mp.getDuration() * perc);
  }

  /**
   * Same as setRate().
   * @param rate
   */
  public void speed(float rate) //Set a multiplier for how fast/slow the movie should be run.
  {
    setRate(rate);
  }

  /**
   * Same as getDuration()
   * @return the duration.
   */
  public float duration() //Get the full length of this movie (in seconds).
  {
    return (float) getDuration();
  }

  /**
   * Jumps to a specified time within the video. Same as setCurrentTime().
   * @param where
   */
  public void jump(float where) //Jump to a specific location (in seconds).
  {
    this.mp.setMediaTime(where); //check this, it is prob. NOT in seconds
  }

  /**
   * Determines whether or not the video is looping.
   * @return A boolean indicating whether or not the video is looping.
   */
  public boolean isLooping()
  {
    return mp.isRepeating();
  }

  /**
   * Gets the total number of loops that has been set.
   * @return The total number of loops set.
   */
  public int totalLoops()
  {
    return mp.getPlayCount();
  }

  /**
   * Gets the current loop iteration.
   * @return The current loop number.
   */
  public int currentLoop()
  {
    return mp.getCurrentPlayCount();
  }


  /**
   * Temporary debugging-- Why is JMC not keeping track of loops properly???
   * @return Some info.
   */
  public String getLoopString()
  {
    if (isLooping())
    {
      return "loop # " + currentLoop() + " of " + totalLoops();
    }
    else
    {
      return "not looping...";
    }
  }

  /**
   * Stops and rewinds the movie to the beginning.
   */
  public void stop() //Stop the movie, and rewind.
  {
    this.pause();
    this.setCurrentTime(0);
  }

  /**
   * Same as getCurrentTime().
   * @return The current time.
   */
  public float time() //Return the current time in seconds.
  {
    return (float) getCurrentTime(); //prob NOT in seconds...
  }

  /**
   * Dispose of native resources. (Need to figure this out...)
   */
  public void dispose()
  {
    stop();
  }
}
