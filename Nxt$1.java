import java.text.SimpleDateFormat;

final class Nxt$1
  extends ThreadLocal<SimpleDateFormat>
{
  protected SimpleDateFormat initialValue()
  {
    return new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss.SSS] ");
  }
