import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.json.simple.JSONObject;

class Nxt$8
  implements Runnable
{
  private final ConcurrentMap<Nxt.Account, Nxt.Block> lastBlocks = new ConcurrentHashMap();
  private final ConcurrentMap<Nxt.Account, BigInteger> hits = new ConcurrentHashMap();
  
  Nxt$8(Nxt paramNxt) {}
  
  public void run()
  {
    try
    {
      HashMap localHashMap = new HashMap();
      Iterator localIterator = Nxt.users.values().iterator();
      Object localObject1;
      Nxt.Account localAccount;
      while (localIterator.hasNext())
      {
        localObject1 = (Nxt.User)localIterator.next();
        if (((Nxt.User)localObject1).secretPhrase != null)
        {
          localAccount = (Nxt.Account)Nxt.accounts.get(Long.valueOf(Nxt.Account.getId(Nxt.Crypto.getPublicKey(((Nxt.User)localObject1).secretPhrase))));
          if ((localAccount != null) && (localAccount.getEffectiveBalance() > 0)) {
            localHashMap.put(localAccount, localObject1);
          }
        }
      }
      localIterator = localHashMap.entrySet().iterator();
      while (localIterator.hasNext())
      {
        localObject1 = (Map.Entry)localIterator.next();
        localAccount = (Nxt.Account)((Map.Entry)localObject1).getKey();
        Nxt.User localUser = (Nxt.User)((Map.Entry)localObject1).getValue();
        Nxt.Block localBlock = Nxt.Block.getLastBlock();
        Object localObject2;
        if (this.lastBlocks.get(localAccount) != localBlock)
        {
          MessageDigest localMessageDigest = MessageDigest.getInstance("SHA-256");
          if (localBlock.height < 30000)
          {
            localObject3 = Nxt.Crypto.sign(localBlock.generationSignature, localUser.secretPhrase);
            localObject2 = localMessageDigest.digest((byte[])localObject3);
          }
          else
          {
            localMessageDigest.update(localBlock.generationSignature);
            localObject2 = localMessageDigest.digest(Nxt.Crypto.getPublicKey(localUser.secretPhrase));
          }
          Object localObject3 = new BigInteger(1, new byte[] { localObject2[7], localObject2[6], localObject2[5], localObject2[4], localObject2[3], localObject2[2], localObject2[1], localObject2[0] });
          this.lastBlocks.put(localAccount, localBlock);
          this.hits.put(localAccount, localObject3);
          JSONObject localJSONObject = new JSONObject();
          localJSONObject.put("response", "setBlockGenerationDeadline");
          localJSONObject.put("deadline", Long.valueOf(((BigInteger)localObject3).divide(BigInteger.valueOf(Nxt.Block.getBaseTarget()).multiply(BigInteger.valueOf(localAccount.getEffectiveBalance()))).longValue() - (Nxt.getEpochTime(System.currentTimeMillis()) - localBlock.timestamp)));
          localUser.send(localJSONObject);
        }
        int i = Nxt.getEpochTime(System.currentTimeMillis()) - localBlock.timestamp;
        if (i > 0)
        {
          localObject2 = BigInteger.valueOf(Nxt.Block.getBaseTarget()).multiply(BigInteger.valueOf(localAccount.getEffectiveBalance())).multiply(BigInteger.valueOf(i));
          if (((BigInteger)this.hits.get(localAccount)).compareTo((BigInteger)localObject2) < 0) {
            localAccount.generateBlock(localUser.secretPhrase);
          }
        }
      }
    }
    catch (Exception localException) {}
  }
