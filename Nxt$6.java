import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentMap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

class Nxt$6
  implements Runnable
{
  Nxt$6(Nxt paramNxt) {}
  
  public void run()
  {
    try
    {
      int i = Nxt.getEpochTime(System.currentTimeMillis());
      JSONArray localJSONArray = new JSONArray();
      Iterator localIterator = Nxt.unconfirmedTransactions.values().iterator();
      Object localObject1;
      Object localObject2;
      Object localObject3;
      while (localIterator.hasNext())
      {
        localObject1 = (Nxt.Transaction)localIterator.next();
        if ((((Nxt.Transaction)localObject1).timestamp + ((Nxt.Transaction)localObject1).deadline * 60 < i) || (!((Nxt.Transaction)localObject1).validateAttachment()))
        {
          localIterator.remove();
          localObject2 = (Nxt.Account)Nxt.accounts.get(Long.valueOf(Nxt.Account.getId(((Nxt.Transaction)localObject1).senderPublicKey)));
          ((Nxt.Account)localObject2).addToUnconfirmedBalance((((Nxt.Transaction)localObject1).amount + ((Nxt.Transaction)localObject1).fee) * 100L);
          localObject3 = new JSONObject();
          ((JSONObject)localObject3).put("index", Integer.valueOf(((Nxt.Transaction)localObject1).index));
          localJSONArray.add(localObject3);
        }
      }
      if (localJSONArray.size() > 0)
      {
        localObject1 = new JSONObject();
        ((JSONObject)localObject1).put("response", "processNewData");
        ((JSONObject)localObject1).put("removedUnconfirmedTransactions", localJSONArray);
        localObject2 = Nxt.users.values().iterator();
        while (((Iterator)localObject2).hasNext())
        {
          localObject3 = (Nxt.User)((Iterator)localObject2).next();
          ((Nxt.User)localObject3).send((JSONObject)localObject1);
        }
      }
    }
    catch (Exception localException) {}
  }
