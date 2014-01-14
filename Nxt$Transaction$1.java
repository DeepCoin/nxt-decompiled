import java.util.Comparator;

final class Nxt$Transaction$1
  implements Comparator<Nxt.Transaction>
{
  public int compare(Nxt.Transaction paramTransaction1, Nxt.Transaction paramTransaction2)
  {
    return paramTransaction1.timestamp > paramTransaction2.timestamp ? 1 : paramTransaction1.timestamp < paramTransaction2.timestamp ? -1 : 0;
  }
