import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentMap;

class Nxt$3
  implements Runnable
{
  Nxt$3(Nxt paramNxt) {}
  
  public void run()
  {
    try
    {
      long l = System.currentTimeMillis();
      Iterator localIterator = Nxt.peers.values().iterator();
      while (localIterator.hasNext())
      {
        Nxt.Peer localPeer = (Nxt.Peer)localIterator.next();
        if ((localPeer.blacklistingTime > 0L) && (localPeer.blacklistingTime + Nxt.blacklistingPeriod <= l)) {
          localPeer.removeBlacklistedStatus();
        }
      }
    }
    catch (Exception localException) {}
  }
