/* JMC.java ~ Mar 21, 2009 */
package jmcvideo;

import com.sun.media.jmc.MediaProvider;
import com.sun.media.jmc.control.AudioControl;
import com.sun.media.jmc.control.PlayControl;
import com.sun.media.jmc.control.VideoRenderControl;
import com.sun.media.jmc.event.BufferDownloadListener;
import com.sun.media.jmc.event.BufferDownloadedProgressChangedEvent;
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
public abstract class JMC extends PImage
  implements PConstants, VideoRendererListener, BufferDownloadListener
{

  public int vw = 0;
  public int vh = 0;
  public double rate = 1f; //have to store rate because MediaProvider is not storing it properly
  public boolean isBouncing = false;
  public int numBounces = PlayControl.REPEAT_FOREVER;
  public int bounces = 1;
  public double bounceStart;
  public double bounceStop;
  public boolean forward = true;
  public boolean stopVideoAfterBouncing = true;
  public double progress = 0f;
  public int imageType = BufferedImage.TYPE_INT_RGB;
  public int pixelFormat = PImage.RGB;
  WritableRaster raster = null;
  AudioControl ac;
  VideoRenderControl vrc;
  MediaProvider mp = null;
  public BufferedImage bufferedImage = null;
  Graphics2D g2d = null;
  Dimension videoSize = null;
  public float x, y, w, h;

  public void initializeVideo(PApplet parent, URI uri, int pixelFormat)
  {
    //Processing stuff...
    init(1, 1, pixelFormat);
    this.pixelFormat = pixelFormat;
    this.parent = parent;
    parent.registerDispose(this);

    //JMC stuff...
    this.mp = new MediaProvider();
    setVideo(uri);

    ac = mp.getControl(AudioControl.class);
    vrc = mp.getControl(VideoRenderControl.class);

    vrc.addVideoRendererListener(this);
    mp.addBufferDownloadListener(this);

  }

  public void mediaDownloadProgressChanged(BufferDownloadedProgressChangedEvent bde)
  {
    //System.out.println("dbe progess = " + bde.getProgress());
    this.progress = bde.getProgress();
  }

  /**
   * Switch to a new video.
   * @param filename The name of the new video file in the data directory.
   */
  public void switchVideo(String filename)
  {
    setVideo(VideoUtils.toURI(new File(parent.dataPath(filename))));
    //mp.play();
  }

  /**
   * Switch to a new video.
   * @param file A video file.
   */
  public void switchVideo(File file)
  {
    setVideo(VideoUtils.toURI(file));
    //mp.play();
  }

  /**
   * Switch to a new video.
   * @param url The URL of a video file.
   */
  public void switchVideo(URL url)
  {
    setVideo(VideoUtils.toURI(url));
    //mp.play();
  }

  /**
   * Switch to a new video.
   * @param uri The URI of a video file.
   */
  public void switchVideo(URI uri)
  {
    setVideo(uri);
    //mp.play();
  }

  /**
   * Sets the imageType of the bufferedImage that the frame is painted on.
   * @param imageType
   */
  public void setImageType(int imageType)
  {
    if (this.imageType != imageType)
    {
      this.imageType = imageType;
      this.bufferedImage = null; //signals that the image needs to be recreated
    }
  }

  public int getImageType()
  {
    return this.imageType;
  }

  public void handleBouncingBehavior()
  {
    if (this.isBouncing == true && forward == true && getCurrentTime() >= bounceStop)
    {
      rate *= -1;
      setRate(rate);
      forward = false;
      bounces++;
    }
    else if (this.isBouncing == true && forward == false && getCurrentTime() <= bounceStart)
    {
      rate *= -1;
      setRate(rate);
      forward = true;
      bounces++;
    }

    if (numBounces != PlayControl.REPEAT_FOREVER && bounces > numBounces)
    {
      this.isBouncing = false;

      if (stopVideoAfterBouncing == true)
      {
        stop();
      }
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

  /**
   * Creates a BufferedImage the same size as the video and initializes the PImage pixel buffer.
   */
  public void setupBufferedImage()
  {
    videoSize = vrc.getFrameSize();
    bufferedImage = new BufferedImage((int) videoSize.getWidth(), (int) videoSize.getHeight(), imageType);

    vw = (int) videoSize.getWidth();
    vh = (int) videoSize.getHeight();

    super.init(vw, vh, pixelFormat);
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
    return VideoUtils.formatMediaTime(getCurrentTime());
  }

  /**
   * Gets a nicely formatted version of the duration of the video.
   * @return The duration of the video.
   */
  public String getDurationString()
  {
    return VideoUtils.formatMediaTime(getDuration());
  }

  /**
   * Gets a nicely formatted version of the download progress of the video.
   * @return The progress of the video.
   *
   * @return
   */
  public String getProgressString()
  {
    return VideoUtils.formatMediaTime(getProgress());
  }

  /**
   * Gets info about the loop settings.
   * @return The loop settings.
   */
  public String getLoopString()
  {
    if (isBouncing == true)
    {
      return "bounce " + bounces + " of " + numBounces + ", " +
        "bouncing between " + bounceStart + " and " + bounceStop;
    }
    else
    {
      return "loop " + currentLoop() + " of " + totalLoops() + ", " +
        "looping between " + getStartTime() + " and " + getStopTime();
    }
  }

  public double getStartTime()
  {
    return mp.getStartTime();
  }

  public void setStartTime(double start)
  {
    mp.setStartTime(start);
  }

  public double getStopTime()
  {
    return mp.getStopTime();
  }

  public void setStopTime(double stop)
  {
    mp.setStopTime(stop);
  }

  /**
   * Gets the duration of the video.
   * @return The duration of the video.
   */
  public double getDuration()
  {
    waitUntilReady();
    return this.mp.getDuration();
  }

  /**
   * Gets the download progress of the video.
   * @return The download progress of the video.
   */
  public double getProgress()
  {
    return progress;
  }

  /**
   * Increase or decrease the rate of video playback by a specified amount.
   * @param amt Amount by which to change the rate.
   */
  public void changeRate(double amt)
  {
    this.rate = rate + amt;
    waitUntilReady();
    mp.setRate(rate);
  }

  /**
   * Sets the rate of video playback
   * @param rate The rate to set the playback to.
   */
  public void setRate(double rate)
  {
    this.rate = rate; //need to store the rate here because MediaProvider's getRate() is broken!
    waitUntilReady();
    mp.setRate(rate);
  }

  /**
   * Gets the current rate of video playback.
   * @return The current rate.
   */
  public double getRate() //this doesn't seem to return the correct value! *always* 1.0
  {
    return this.rate;
    //return mp.getRate(); //MediaProvider's getRate seems to be broken (in javafx1.2)
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
    loop(1);
  }

  public void play(double start, double stop)
  {
    loop(1, start, stop);
  }

  /**
   * Begins video playback, looping forever
   */
  public void loop()
  {
    loop(PlayControl.REPEAT_FOREVER);
  }

  /**
   * Begins video playback, looping a specified number of times.
   * @param numRepeats
   */
  public void loop(int numRepeats)
  {
    waitUntilReady();
    mp.play();
    mp.setPlayCount(numRepeats);
  }

  /**
   * Begins video playback, looping forever from the specified start time to the specified end time.
   * @param start
   * @param stop
   */
  public void loop(double start, double stop)
  {
    loop(PlayControl.REPEAT_FOREVER, start, stop);
  }

  /**
   * Begins video playback, looping a specified number of times from the specified start time to the specified end time.
   * @param start
   * @param stop
   */
  public void loop(int numRepeats, double start, double stop)
  {
    waitUntilReady();
    setStartTime(start);
    setStopTime(stop);
    loop(numRepeats);
  }

  /**
   * Begins video playback, playing forwards and then backwards forever if backward playback is supported.
   */
  public void bounce()
  {
    bounce(PlayControl.REPEAT_FOREVER);
  }

  /**
   * Begins video playback, playing forwards and then backwards a specified number of times.
   * @param numBounces
   */
  public void bounce(int numBounces)
  {
    this.isBouncing = true;
    this.numBounces = numBounces;

    waitUntilReady();
    //mp.play();

    this.bounceStart = .5;
    setCurrentTime(bounceStart);
    this.bounceStop = (getDuration() - .5);

  }

  /**
   * Begins video playback, playing forwards and then backwards forever from the specified start time to the specified end time.
   *
   * @param bounceStart
   * @param bounceStop
   */
  public void bounce(double bounceStart, double bounceStop)
  {
    bounce(PlayControl.REPEAT_FOREVER, bounceStart, bounceStop);
  }

  /**
   * Begins video playback, playing forwards and then backwards 
   * a specified number of times from the specified start time to the specified end time. 
   * 
   * @param numBounces
   * @param bounceStart
   * @param bounceStop
   */
  public void bounce(int numBounces, double bounceStart, double bounceStop)
  {
    this.isBouncing = true;
    this.numBounces = numBounces;
    this.bounceStart = bounceStart;
    this.bounceStop = bounceStop;

    waitUntilReady();

    setCurrentTime(bounceStart);
  }

  /**
   * Sleeps until the video is available.
   */
  private void waitUntilReady()
  {
    while (!isReady())
    {
      VideoUtils.sleep(10);
    }
  }

  /**
   * Checks to make sure that the video has a legitimate duration and that we are
   * not trying to play past what has been downloaded.
   * @return Whether or not the video is available for playing and querying.
   */
  public boolean isReady()
  {
    //    System.out.println("currentTime = " + getCurrentTime());
    //    System.out.println("progress = " + progress);
    if (mp.getDuration() == PlayControl.TIME_ETERNITY ||
      mp.getDuration() == PlayControl.TIME_UNKNOWN)
    {
      return false;
    }

    if (progress < getCurrentTime())
    {
      return false;
    }

    return true;
  }

  /**
   * Determine if the video is currently playing. This seems to be inaccurate sometimes!
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
    setVolume(getVolume() + amt);
  }

  /**
   * Sets the volume to the specified volume.
   * @param vol
   */
  public void setVolume(float vol)
  {
    waitUntilReady();
    this.ac.setVolume(vol);
  }

  /**
   * Gets the current volume.
   * @return The current volume.
   */
  public float getVolume()
  {
    waitUntilReady();
    return this.ac.getVolume();
  }

  /**
   * Mutes or unmutes the video.
   */
  public void setMute(boolean mute)
  {
    if (isMuted() != mute)
    {
      this.ac.setMute(mute);
    }
  }

  /**
   * Determine if the video is muted or not.
   * @return A boolean indicating if the video is muted.
   */
  public boolean isMuted()
  {
    waitUntilReady();
    return this.ac.isMuted();
  }

  /**
   * Make the video mute if it is not, or unmutes it if it is.
   */
  public void toggleMute()
  {
    setMute(!isMuted());
  }

  public void changeBalance(float amt)
  {
    setBalance(getBalance() + amt);
  }

  public void setBalance(float balance) //balance is automatically clamped between -1 and +1
  {
    this.ac.setBalance(balance);
  }

  public float getBalance()
  {
    return this.ac.getBalance();
  }

  //this doesn't seem to do anything...
  public void setFader(float fader)
  {
    this.ac.setFader(fader);
  }

  //this returns the value set by setFader, but doesn't appear to do anything...
  public float getFader()
  {
    return this.ac.getFader();
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
    waitUntilReady();
    if (time < progress - .01)
    {
      this.mp.play();
      this.mp.setMediaTime(time);
    }
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
    setCurrentTime(getDuration() * perc);
  }

  /**
   * Determines whether or not the video is looping.
   * @return A boolean indicating whether or not the video is looping.
   */
  public boolean isLooping()
  {
    if (totalLoops() > 1)
    {
      return true;
    }
    return false;
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
   * Stops and rewinds the movie to the beginning.
   */
  public void stop() //Stop the movie, and rewind.
  {
    this.pause();
    this.setCurrentTime(0);
  }

  /**
   * Dispose of native resources.
   */
  public void dispose()
  {
    pause();

    VideoUtils.sleep(250L);

    if (vrc != null)
    {
      vrc.removeVideoRendererListener(this);
    }
    if (mp != null)
    {
      mp.removeBufferDownloadListener(this);
      this.progress = 0.0;
      mp.setSource(null); //this will call mp.close()
    }

  }

  /**
   * Sets the video for this MediaProvider.
   * @param uri The URI of the video.
   */
  public void setVideo(URI uri)
  {
    mp.setSource(uri);
  }
}
