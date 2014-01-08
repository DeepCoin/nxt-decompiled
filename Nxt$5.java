import org.json.simple.JSONObject;

class Nxt$5
  implements Runnable
{
  private final JSONObject getUnconfirmedTransactionsRequest = new JSONObject();
  
  Nxt$5(Nxt paramNxt)
  {
    this.getUnconfirmedTransactionsRequest.put("requestType", "getUnconfirmedTransactions");
  }
  
  public void run()
  {
    try
    {
      Nxt.Peer localPeer = Nxt.Peer.getAnyPeer(1, true);
      if (localPeer != null)
      {
        JSONObject localJSONObject = localPeer.send(this.getUnconfirmedTransactionsRequest);
        if (localJSONObject != null) {
          Nxt.Transaction.processTransactions(localJSONObject, "unconfirmedTransactions");
        }
      }
    }
    catch (Exception localException) {}
  }
