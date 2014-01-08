import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

class Nxt$7
  implements Runnable
{
  private final JSONObject getCumulativeDifficultyRequest = new JSONObject();
  private final JSONObject getMilestoneBlockIdsRequest = new JSONObject();
  
  Nxt$7(Nxt paramNxt)
  {
    this.getCumulativeDifficultyRequest.put("requestType", "getCumulativeDifficulty");
    this.getMilestoneBlockIdsRequest.put("requestType", "getMilestoneBlockIds");
  }
  
  public void run()
  {
    try
    {
      Nxt.Peer localPeer = Nxt.Peer.getAnyPeer(1, true);
      if (localPeer != null)
      {
        Nxt.lastBlockchainFeeder = localPeer;
        JSONObject localJSONObject1 = localPeer.send(this.getCumulativeDifficultyRequest);
        if (localJSONObject1 != null)
        {
          BigInteger localBigInteger1 = Nxt.Block.getLastBlock().cumulativeDifficulty;
          BigInteger localBigInteger2 = new BigInteger((String)localJSONObject1.get("cumulativeDifficulty"));
          if (localBigInteger2.compareTo(localBigInteger1) > 0)
          {
            localJSONObject1 = localPeer.send(this.getMilestoneBlockIdsRequest);
            if (localJSONObject1 != null)
            {
              long l1 = 2680262203532249785L;
              JSONArray localJSONArray1 = (JSONArray)localJSONObject1.get("milestoneBlockIds");
              Iterator localIterator = localJSONArray1.iterator();
              while (localIterator.hasNext())
              {
                Object localObject1 = localIterator.next();
                long l2 = new BigInteger((String)localObject1).longValue();
                Nxt.Block localBlock1 = (Nxt.Block)Nxt.blocks.get(Long.valueOf(l2));
                if (localBlock1 != null)
                {
                  l1 = l2;
                  break;
                }
              }
              int j;
              int i;
              do
              {
                JSONObject localJSONObject2 = new JSONObject();
                localJSONObject2.put("requestType", "getNextBlockIds");
                localJSONObject2.put("blockId", Nxt.convert(l1));
                localJSONObject1 = localPeer.send(localJSONObject2);
                if (localJSONObject1 == null) {
                  return;
                }
                JSONArray localJSONArray2 = (JSONArray)localJSONObject1.get("nextBlockIds");
                j = localJSONArray2.size();
                if (j == 0) {
                  return;
                }
                for (i = 0; i < j; i++)
                {
                  long l4 = new BigInteger((String)localJSONArray2.get(i)).longValue();
                  if (Nxt.blocks.get(Long.valueOf(l4)) == null) {
                    break;
                  }
                  l1 = l4;
                }
              } while (i == j);
              if (Nxt.Block.getLastBlock().height - ((Nxt.Block)Nxt.blocks.get(Long.valueOf(l1))).height < 720)
              {
                long l3 = l1;
                LinkedList localLinkedList = new LinkedList();
                HashMap localHashMap = new HashMap();
                Object localObject2;
                Object localObject3;
                Object localObject4;
                for (;;)
                {
                  JSONObject localJSONObject3 = new JSONObject();
                  localJSONObject3.put("requestType", "getNextBlocks");
                  localJSONObject3.put("blockId", Nxt.convert(l3));
                  localJSONObject1 = localPeer.send(localJSONObject3);
                  if (localJSONObject1 == null) {
                    break;
                  }
                  localObject2 = (JSONArray)localJSONObject1.get("nextBlocks");
                  j = ((JSONArray)localObject2).size();
                  if (j == 0) {
                    break;
                  }
                  for (i = 0; i < j; i++)
                  {
                    localObject3 = (JSONObject)((JSONArray)localObject2).get(i);
                    localObject4 = Nxt.Block.getBlock((JSONObject)localObject3);
                    if (localObject4 == null)
                    {
                      localPeer.blacklist();
                      return;
                    }
                    l3 = ((Nxt.Block)localObject4).getId();
                    synchronized (Nxt.blocksAndTransactionsLock)
                    {
                      int m = 0;
                      Object localObject5;
                      Object localObject6;
                      if (((Nxt.Block)localObject4).previousBlock == Nxt.lastBlock)
                      {
                        localObject5 = ByteBuffer.allocate(224 + ((Nxt.Block)localObject4).payloadLength);
                        ((ByteBuffer)localObject5).order(ByteOrder.LITTLE_ENDIAN);
                        ((ByteBuffer)localObject5).put(((Nxt.Block)localObject4).getBytes());
                        JSONArray localJSONArray3 = (JSONArray)((JSONObject)localObject3).get("transactions");
                        localObject6 = localJSONArray3.iterator();
                        while (((Iterator)localObject6).hasNext())
                        {
                          Object localObject7 = ((Iterator)localObject6).next();
                          ((ByteBuffer)localObject5).put(Nxt.Transaction.getTransaction((JSONObject)localObject7).getBytes());
                        }
                        if (Nxt.Block.pushBlock((ByteBuffer)localObject5, false))
                        {
                          m = 1;
                        }
                        else
                        {
                          localPeer.blacklist();
                          return;
                        }
                      }
                      if ((m == 0) && (Nxt.blocks.get(Long.valueOf(((Nxt.Block)localObject4).getId())) == null) && (((Nxt.Block)localObject4).numberOfTransactions <= 255))
                      {
                        localLinkedList.add(localObject4);
                        localObject5 = (JSONArray)((JSONObject)localObject3).get("transactions");
                        for (int n = 0; n < ((Nxt.Block)localObject4).numberOfTransactions; n++)
                        {
                          localObject6 = Nxt.Transaction.getTransaction((JSONObject)((JSONArray)localObject5).get(n));
                          ((Nxt.Block)localObject4).transactions[n] = ((Nxt.Transaction)localObject6).getId();
                          localHashMap.put(Long.valueOf(localObject4.transactions[n]), localObject6);
                        }
                      }
                    }
                  }
                }
                if ((!localLinkedList.isEmpty()) && (Nxt.Block.getLastBlock().height - ((Nxt.Block)Nxt.blocks.get(Long.valueOf(l1))).height < 720)) {
                  synchronized (Nxt.blocksAndTransactionsLock)
                  {
                    Nxt.Block.saveBlocks("blocks.nxt.bak", true);
                    Nxt.Transaction.saveTransactions("transactions.nxt.bak");
                    localBigInteger1 = Nxt.Block.getLastBlock().cumulativeDifficulty;
                    while ((Nxt.lastBlock != l1) && (Nxt.Block.popLastBlock())) {}
                    if (Nxt.lastBlock == l1)
                    {
                      localObject2 = localLinkedList.iterator();
                      while (((Iterator)localObject2).hasNext())
                      {
                        localObject3 = (Nxt.Block)((Iterator)localObject2).next();
                        if (((Nxt.Block)localObject3).previousBlock == Nxt.lastBlock)
                        {
                          localObject4 = ByteBuffer.allocate(224 + ((Nxt.Block)localObject3).payloadLength);
                          ((ByteBuffer)localObject4).order(ByteOrder.LITTLE_ENDIAN);
                          ((ByteBuffer)localObject4).put(((Nxt.Block)localObject3).getBytes());
                          for (int k = 0; k < ((Nxt.Block)localObject3).transactions.length; k++) {
                            ((ByteBuffer)localObject4).put(((Nxt.Transaction)localHashMap.get(Long.valueOf(localObject3.transactions[k]))).getBytes());
                          }
                          if (!Nxt.Block.pushBlock((ByteBuffer)localObject4, false)) {
                            break;
                          }
                        }
                      }
                    }
                    if (Nxt.Block.getLastBlock().cumulativeDifficulty.compareTo(localBigInteger1) < 0)
                    {
                      Nxt.Block.loadBlocks("blocks.nxt.bak");
                      Nxt.Transaction.loadTransactions("transactions.nxt.bak");
                      localPeer.blacklist();
                      Nxt.accounts.clear();
                      Nxt.aliases.clear();
                      Nxt.aliasIdToAliasMappings.clear();
                      Nxt.logMessage("Re-scanning blockchain...");
                      localObject2 = new HashMap(Nxt.blocks);
                      Nxt.blocks.clear();
                      Nxt.lastBlock = 2680262203532249785L;
                      long l5 = 2680262203532249785L;
                      do
                      {
                        Nxt.Block localBlock2 = (Nxt.Block)((Map)localObject2).get(Long.valueOf(l5));
                        long l6 = localBlock2.nextBlock;
                        localBlock2.analyze();
                        l5 = l6;
                      } while (l3 != 0L);
                      Nxt.logMessage("...Done");
                    }
                  }
                }
                synchronized (Nxt.blocksAndTransactionsLock)
                {
                  Nxt.Block.saveBlocks("blocks.nxt", false);
                  Nxt.Transaction.saveTransactions("transactions.nxt");
                }
              }
            }
          }
        }
      }
    }
    catch (Exception localException) {}
  }
