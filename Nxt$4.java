import java.util.Iterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

class Nxt$4
  implements Runnable
{
  private final JSONObject getPeersRequest = new JSONObject();
  
  Nxt$4(Nxt paramNxt)
  {
    this.getPeersRequest.put("requestType", "getPeers");
  }
  
  public void run()
  {
    try
    {
      Nxt.Peer localPeer = Nxt.Peer.getAnyPeer(1, true);
      if (localPeer != null)
      {
        JSONObject localJSONObject = localPeer.send(this.getPeersRequest);
        if (localJSONObject != null)
        {
          JSONArray localJSONArray = (JSONArray)localJSONObject.get("peers");
          Iterator localIterator = localJSONArray.iterator();
          while (localIterator.hasNext())
          {
            Object localObject = localIterator.next();
            String str = ((String)localObject).trim();
            if (str.length() > 0) {
              Nxt.Peer.addPeer(str, str);
            }
          }
        }
      }
    }
    catch (Exception localException) {}
  }
