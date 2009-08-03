/* VideoUtils.java ~ Jan 1, 2009 */
package jmcvideo;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Methods for calculating bounds & formatting strings, etc.
 * @author angus
 */
public class VideoUtils
{
  public static URI toURI(URL url)
  {
    try
    {
      return url.toURI();
    }
    catch (URISyntaxException urise)
    {
      urise.printStackTrace();
    }

    return null;
  }

  public static URI toURI(File file)
  {
    return file.toURI();
  }

  //useful methods found on PushingPixels blog, from an article about jmc which was super-helpful
  private static String format(int val, int places)
  {
    String result = "" + val;
    while (result.length() < places)
    {
      result = "0" + result;
    }
    return result;
  }

  /**
   * Formats the media time into a more readable string for printing, debugging, etc.
   * @param mediaTime The time as returned by the media provider.
   * @return a more readable String representation of the mediaTime.
   */
  public static String formatMediaTime(double mediaTime)
  {
    int minutes = (int) (mediaTime / 60);
    int seconds = (int) mediaTime % 60;
    int milli = (int) (mediaTime * 1000) % 1000;

    return format(minutes, 2) + ":" + format(seconds, 2) + "." + format(milli, 3);
  }

  public static void sleep(long ms)
  {
    if (ms < 0L)
    {
      ms = 0L;
    }

    try
    {
      Thread.sleep(ms);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

}
