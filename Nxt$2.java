import java.util.concurrent.ThreadLocalRandom;

class Nxt$2
  implements Runnable
{
  Nxt$2(Nxt paramNxt) {}
  
  public void run()
  {
    try
    {
      if (Nxt.Peer.getNumberOfConnectedPublicPeers() < Nxt.maxNumberOfConnectedPublicPeers)
      {
        Nxt.Peer localPeer = Nxt.Peer.getAnyPeer(ThreadLocalRandom.current().nextInt(2) == 0 ? 0 : 2, false);
        if (localPeer != null) {
          localPeer.connect();
        }
      }
    }
    catch (Exception localException) {}
  }
