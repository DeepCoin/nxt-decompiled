import java.util.Comparator;

final class Nxt$Block$2
  implements Comparator<Nxt.Block>
{
  public int compare(Nxt.Block paramBlock1, Nxt.Block paramBlock2)
  {
    return paramBlock1.height > paramBlock2.height ? 1 : paramBlock1.height < paramBlock2.height ? -1 : 0;
  }
