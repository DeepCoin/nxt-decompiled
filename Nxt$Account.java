import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import org.json.simple.JSONObject;

class Nxt$Account
{
  final long id;
  private long balance;
  final int height;
  final AtomicReference<byte[]> publicKey = new AtomicReference();
  final HashMap<Long, Integer> assetBalances;
  private long unconfirmedBalance;
  final HashMap<Long, Integer> unconfirmedAssetBalances;
  
  private Nxt$Account(long paramLong)
  {
    this.id = paramLong;
    this.height = Nxt.Block.getLastBlock().height;
    this.assetBalances = new HashMap();
    this.unconfirmedAssetBalances = new HashMap();
  }
  
  static Account addAccount(long paramLong)
  {
    Account localAccount = new Account(paramLong);
    Nxt.accounts.put(Long.valueOf(paramLong), localAccount);
    return localAccount;
  }
  
  boolean setOrVerify(byte[] paramArrayOfByte)
  {
    return (this.publicKey.compareAndSet(null, paramArrayOfByte)) || (Arrays.equals(paramArrayOfByte, (byte[])this.publicKey.get()));
  }
  
  void generateBlock(String paramString)
    throws Exception
  {
    TreeSet localTreeSet = new TreeSet();
    Object localObject1 = Nxt.unconfirmedTransactions.values().iterator();
    while (((Iterator)localObject1).hasNext())
    {
      localObject2 = (Nxt.Transaction)((Iterator)localObject1).next();
      if ((((Nxt.Transaction)localObject2).referencedTransaction == 0L) || (Nxt.transactions.get(Long.valueOf(((Nxt.Transaction)localObject2).referencedTransaction)) != null)) {
        localTreeSet.add(localObject2);
      }
    }
    localObject1 = new HashMap();
    Object localObject2 = new HashSet();
    HashMap localHashMap = new HashMap();
    int i = 0;
    Object localObject3;
    while (i <= 32640)
    {
      int j = ((Map)localObject1).size();
      localObject3 = localTreeSet.iterator();
      while (((Iterator)localObject3).hasNext())
      {
        localObject4 = (Nxt.Transaction)((Iterator)localObject3).next();
        int m = ((Nxt.Transaction)localObject4).getBytes().length;
        if ((((Map)localObject1).get(Long.valueOf(((Nxt.Transaction)localObject4).getId())) == null) && (i + m <= 32640))
        {
          long l1 = getId(((Nxt.Transaction)localObject4).senderPublicKey);
          Long localLong = (Long)localHashMap.get(Long.valueOf(l1));
          if (localLong == null) {
            localLong = Long.valueOf(0L);
          }
          long l2 = (((Nxt.Transaction)localObject4).amount + ((Nxt.Transaction)localObject4).fee) * 100L;
          if ((localLong.longValue() + l2 <= ((Account)Nxt.accounts.get(Long.valueOf(l1))).getBalance()) && (((Nxt.Transaction)localObject4).validateAttachment())) {
            switch (((Nxt.Transaction)localObject4).type)
            {
            case 1: 
              switch (((Nxt.Transaction)localObject4).subtype)
              {
              case 1: 
                if (!((Set)localObject2).add(((Nxt.Transaction.MessagingAliasAssignmentAttachment)((Nxt.Transaction)localObject4).attachment).alias.toLowerCase())) {}
                break;
              }
            default: 
              localHashMap.put(Long.valueOf(l1), Long.valueOf(localLong.longValue() + l2));
              ((Map)localObject1).put(Long.valueOf(((Nxt.Transaction)localObject4).getId()), localObject4);
              i += m;
            }
          }
        }
      }
      if (((Map)localObject1).size() == j) {
        break;
      }
    }
    Nxt.Block localBlock;
    if (Nxt.Block.getLastBlock().height < 30000)
    {
      localBlock = new Nxt.Block(1, Nxt.getEpochTime(System.currentTimeMillis()), Nxt.lastBlock, ((Map)localObject1).size(), 0, 0, 0, null, Nxt.Crypto.getPublicKey(paramString), null, new byte[64]);
    }
    else
    {
      localObject3 = MessageDigest.getInstance("SHA-256").digest(Nxt.Block.getLastBlock().getBytes());
      localBlock = new Nxt.Block(2, Nxt.getEpochTime(System.currentTimeMillis()), Nxt.lastBlock, ((Map)localObject1).size(), 0, 0, 0, null, Nxt.Crypto.getPublicKey(paramString), null, new byte[64], (byte[])localObject3);
    }
    int k = 0;
    Object localObject4 = ((Map)localObject1).entrySet().iterator();
    while (((Iterator)localObject4).hasNext())
    {
      localObject5 = (Map.Entry)((Iterator)localObject4).next();
      localObject6 = (Nxt.Transaction)((Map.Entry)localObject5).getValue();
      localBlock.totalAmount += ((Nxt.Transaction)localObject6).amount;
      localBlock.totalFee += ((Nxt.Transaction)localObject6).fee;
      localBlock.payloadLength += ((Nxt.Transaction)localObject6).getBytes().length;
      localBlock.transactions[(k++)] = ((Long)((Map.Entry)localObject5).getKey()).longValue();
    }
    Arrays.sort(localBlock.transactions);
    localObject4 = MessageDigest.getInstance("SHA-256");
    for (k = 0; k < localBlock.numberOfTransactions; k++) {
      ((MessageDigest)localObject4).update(((Nxt.Transaction)((Map)localObject1).get(Long.valueOf(localBlock.transactions[k]))).getBytes());
    }
    localBlock.payloadHash = ((MessageDigest)localObject4).digest();
    if (Nxt.Block.getLastBlock().height < 30000)
    {
      localBlock.generationSignature = Nxt.Crypto.sign(Nxt.Block.getLastBlock().generationSignature, paramString);
    }
    else
    {
      ((MessageDigest)localObject4).update(Nxt.Block.getLastBlock().generationSignature);
      localBlock.generationSignature = ((MessageDigest)localObject4).digest(Nxt.Crypto.getPublicKey(paramString));
    }
    Object localObject5 = localBlock.getBytes();
    Object localObject6 = new byte[localObject5.length - 64];
    System.arraycopy(localObject5, 0, localObject6, 0, localObject6.length);
    localBlock.blockSignature = Nxt.Crypto.sign((byte[])localObject6, paramString);
    if ((localBlock.verifyBlockSignature()) && (localBlock.verifyGenerationSignature()))
    {
      JSONObject localJSONObject = localBlock.getJSONObject((Map)localObject1);
      localJSONObject.put("requestType", "processBlock");
      Nxt.Peer.sendToAllPeers(localJSONObject);
    }
    else
    {
      Nxt.logMessage("Generated an incorrect block. Waiting for the next one...");
    }
  }
  
  int getEffectiveBalance()
  {
    if (this.height == 0) {
      return (int)(getBalance() / 100L);
    }
    if (Nxt.Block.getLastBlock().height - this.height < 1440) {
      return 0;
    }
    int i = 0;
    for (long l : Nxt.Block.getLastBlock().transactions)
    {
      Nxt.Transaction localTransaction = (Nxt.Transaction)Nxt.transactions.get(Long.valueOf(l));
      if (localTransaction.recipient == this.id) {
        i += localTransaction.amount;
      }
    }
    return (int)(getBalance() / 100L) - i;
  }
  
  static long getId(byte[] paramArrayOfByte)
    throws Exception
  {
    byte[] arrayOfByte = MessageDigest.getInstance("SHA-256").digest(paramArrayOfByte);
    BigInteger localBigInteger = new BigInteger(1, new byte[] { arrayOfByte[7], arrayOfByte[6], arrayOfByte[5], arrayOfByte[4], arrayOfByte[3], arrayOfByte[2], arrayOfByte[1], arrayOfByte[0] });
    return localBigInteger.longValue();
  }
  
  synchronized long getBalance()
  {
    return this.balance;
  }
  
  synchronized long getUnconfirmedBalance()
  {
    return this.unconfirmedBalance;
  }
  
  void addToBalance(long paramLong)
    throws Exception
  {
    synchronized (this)
    {
      this.balance += paramLong;
    }
    updatePeerWeights();
  }
  
  void addToUnconfirmedBalance(long paramLong)
    throws Exception
  {
    synchronized (this)
    {
      this.unconfirmedBalance += paramLong;
    }
    updateUserUnconfirmedBalance();
  }
  
  void addToBalanceAndUnconfirmedBalance(long paramLong)
    throws Exception
  {
    synchronized (this)
    {
      this.balance += paramLong;
      this.unconfirmedBalance += paramLong;
    }
    updatePeerWeights();
    updateUserUnconfirmedBalance();
  }
  
  private void updatePeerWeights()
  {
    Iterator localIterator = Nxt.peers.values().iterator();
    while (localIterator.hasNext())
    {
      Nxt.Peer localPeer = (Nxt.Peer)localIterator.next();
      if ((localPeer.accountId == this.id) && (localPeer.adjustedWeight > 0L)) {
        localPeer.updateWeight();
      }
    }
  }
  
  private void updateUserUnconfirmedBalance()
    throws Exception
  {
    JSONObject localJSONObject = new JSONObject();
    localJSONObject.put("response", "setBalance");
    localJSONObject.put("balance", Long.valueOf(getUnconfirmedBalance()));
    Iterator localIterator = Nxt.users.values().iterator();
    while (localIterator.hasNext())
    {
      Nxt.User localUser = (Nxt.User)localIterator.next();
      if ((localUser.secretPhrase != null) && (getId(Nxt.Crypto.getPublicKey(localUser.secretPhrase)) == this.id)) {
        localUser.send(localJSONObject);
      }
    }
  }
