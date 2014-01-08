import java.util.Comparator;

final class Nxt$Transaction$1
  implements Comparator<Nxt.Transaction>
{
  public int compare(Nxt.Transaction paramTransaction1, Nxt.Transaction paramTransaction2)
  {
    try
    {
      long l1 = paramTransaction1.getId();
      long l2 = paramTransaction2.getId();
      return paramTransaction1.timestamp > paramTransaction2.timestamp ? 1 : paramTransaction1.timestamp < paramTransaction2.timestamp ? -1 : l1 > l2 ? 1 : l1 < l2 ? -1 : 0;
    }
    catch (Exception localException)
    {
      throw new RuntimeException(localException);
    }
  }
