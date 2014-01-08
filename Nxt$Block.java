import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
import java.util.concurrent.atomic.AtomicInteger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

class Nxt$Block
  implements Serializable
{
  static final long serialVersionUID = 0L;
  final int version;
  final int timestamp;
  final long previousBlock;
  final int numberOfTransactions;
  int totalAmount;
  int totalFee;
  int payloadLength;
  byte[] payloadHash;
  final byte[] generatorPublicKey;
  byte[] generationSignature;
  byte[] blockSignature;
  final byte[] previousBlockHash;
  int index;
  final long[] transactions;
  volatile long baseTarget;
  int height;
  volatile long nextBlock;
  volatile BigInteger cumulativeDifficulty;
  
  Nxt$Block(int paramInt1, int paramInt2, long paramLong, int paramInt3, int paramInt4, int paramInt5, int paramInt6, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, byte[] paramArrayOfByte4)
  {
    this(paramInt1, paramInt2, paramLong, paramInt3, paramInt4, paramInt5, paramInt6, paramArrayOfByte1, paramArrayOfByte2, paramArrayOfByte3, paramArrayOfByte4, null);
  }
  
  Nxt$Block(int paramInt1, int paramInt2, long paramLong, int paramInt3, int paramInt4, int paramInt5, int paramInt6, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, byte[] paramArrayOfByte4, byte[] paramArrayOfByte5)
  {
    if ((paramInt3 > 255) || (paramInt3 < 0)) {
      throw new IllegalArgumentException("attempted to create a block with " + paramInt3 + " transactions");
    }
    if ((paramInt6 > 32640) || (paramInt6 < 0)) {
      throw new IllegalArgumentException("attempted to create a block with payloadLength " + paramInt6);
    }
    this.version = paramInt1;
    this.timestamp = paramInt2;
    this.previousBlock = paramLong;
    this.numberOfTransactions = paramInt3;
    this.totalAmount = paramInt4;
    this.totalFee = paramInt5;
    this.payloadLength = paramInt6;
    this.payloadHash = paramArrayOfByte1;
    this.generatorPublicKey = paramArrayOfByte2;
    this.generationSignature = paramArrayOfByte3;
    this.blockSignature = paramArrayOfByte4;
    this.previousBlockHash = paramArrayOfByte5;
    this.transactions = new long[paramInt3];
  }
  
  void analyze()
    throws Exception
  {
    synchronized (Nxt.blocksAndTransactionsLock)
    {
      if (this.previousBlock == 0L)
      {
        Nxt.lastBlock = 2680262203532249785L;
        this.baseTarget = 153722867L;
        this.cumulativeDifficulty = BigInteger.ZERO;
        Nxt.blocks.put(Long.valueOf(Nxt.lastBlock), this);
        Nxt.Account.addAccount(1739068987193023818L);
      }
      else
      {
        getLastBlock().nextBlock = getId();
        this.height = (getLastBlock().height + 1);
        Nxt.lastBlock = getId();
        Nxt.blocks.put(Long.valueOf(Nxt.lastBlock), this);
        this.baseTarget = getBaseTarget();
        this.cumulativeDifficulty = ((Block)Nxt.blocks.get(Long.valueOf(this.previousBlock))).cumulativeDifficulty.add(Nxt.two64.divide(BigInteger.valueOf(this.baseTarget)));
        Nxt.Account localAccount1 = (Nxt.Account)Nxt.accounts.get(Long.valueOf(Nxt.Account.getId(this.generatorPublicKey)));
        localAccount1.addToBalanceAndUnconfirmedBalance(this.totalFee * 100L);
      }
      for (int i = 0; i < this.numberOfTransactions; i++)
      {
        Nxt.Transaction localTransaction = (Nxt.Transaction)Nxt.transactions.get(Long.valueOf(this.transactions[i]));
        long l1 = Nxt.Account.getId(localTransaction.senderPublicKey);
        Nxt.Account localAccount2 = (Nxt.Account)Nxt.accounts.get(Long.valueOf(l1));
        if (!localAccount2.setOrVerify(localTransaction.senderPublicKey)) {
          throw new RuntimeException("sender public key mismatch");
        }
        localAccount2.addToBalanceAndUnconfirmedBalance(-(localTransaction.amount + localTransaction.fee) * 100L);
        Nxt.Account localAccount3 = (Nxt.Account)Nxt.accounts.get(Long.valueOf(localTransaction.recipient));
        if (localAccount3 == null) {
          localAccount3 = Nxt.Account.addAccount(localTransaction.recipient);
        }
        Object localObject1;
        Object localObject2;
        switch (localTransaction.type)
        {
        case 0: 
          switch (localTransaction.subtype)
          {
          case 0: 
            localAccount3.addToBalanceAndUnconfirmedBalance(localTransaction.amount * 100L);
          }
          break;
        case 1: 
          switch (localTransaction.subtype)
          {
          case 1: 
            localObject1 = (Nxt.Transaction.MessagingAliasAssignmentAttachment)localTransaction.attachment;
            String str = ((Nxt.Transaction.MessagingAliasAssignmentAttachment)localObject1).alias.toLowerCase();
            localObject2 = (Nxt.Alias)Nxt.aliases.get(str);
            if (localObject2 == null)
            {
              localObject2 = new Nxt.Alias(localAccount2, ((Nxt.Transaction.MessagingAliasAssignmentAttachment)localObject1).alias, ((Nxt.Transaction.MessagingAliasAssignmentAttachment)localObject1).uri, this.timestamp);
              Nxt.aliases.put(str, localObject2);
              Nxt.aliasIdToAliasMappings.put(Long.valueOf(localTransaction.getId()), localObject2);
            }
            else
            {
              ((Nxt.Alias)localObject2).uri = ((Nxt.Transaction.MessagingAliasAssignmentAttachment)localObject1).uri;
              ((Nxt.Alias)localObject2).timestamp = this.timestamp;
            }
            break;
          }
          break;
        case 2: 
          switch (localTransaction.subtype)
          {
          case 0: 
            localObject1 = (Nxt.Transaction.ColoredCoinsAssetIssuanceAttachment)localTransaction.attachment;
            long l2 = localTransaction.getId();
            Nxt.Asset localAsset = new Nxt.Asset(l1, ((Nxt.Transaction.ColoredCoinsAssetIssuanceAttachment)localObject1).name, ((Nxt.Transaction.ColoredCoinsAssetIssuanceAttachment)localObject1).description, ((Nxt.Transaction.ColoredCoinsAssetIssuanceAttachment)localObject1).quantity);
            synchronized (Nxt.assets)
            {
              Nxt.assets.put(Long.valueOf(l2), localAsset);
              Nxt.assetNameToIdMappings.put(((Nxt.Transaction.ColoredCoinsAssetIssuanceAttachment)localObject1).name.toLowerCase(), Long.valueOf(l2));
            }
            synchronized (Nxt.askOrders)
            {
              Nxt.sortedAskOrders.put(Long.valueOf(l2), new TreeSet());
            }
            synchronized (Nxt.bidOrders)
            {
              Nxt.sortedBidOrders.put(Long.valueOf(l2), new TreeSet());
            }
            synchronized (localAccount2)
            {
              localAccount2.assetBalances.put(Long.valueOf(l2), Integer.valueOf(((Nxt.Transaction.ColoredCoinsAssetIssuanceAttachment)localObject1).quantity));
              localAccount2.unconfirmedAssetBalances.put(Long.valueOf(l2), Integer.valueOf(((Nxt.Transaction.ColoredCoinsAssetIssuanceAttachment)localObject1).quantity));
            }
            break;
          case 1: 
            localObject1 = (Nxt.Transaction.ColoredCoinsAssetTransferAttachment)localTransaction.attachment;
            synchronized (localAccount2)
            {
              localAccount2.assetBalances.put(Long.valueOf(((Nxt.Transaction.ColoredCoinsAssetTransferAttachment)localObject1).asset), Integer.valueOf(((Integer)localAccount2.assetBalances.get(Long.valueOf(((Nxt.Transaction.ColoredCoinsAssetTransferAttachment)localObject1).asset))).intValue() - ((Nxt.Transaction.ColoredCoinsAssetTransferAttachment)localObject1).quantity));
              localAccount2.unconfirmedAssetBalances.put(Long.valueOf(((Nxt.Transaction.ColoredCoinsAssetTransferAttachment)localObject1).asset), Integer.valueOf(((Integer)localAccount2.unconfirmedAssetBalances.get(Long.valueOf(((Nxt.Transaction.ColoredCoinsAssetTransferAttachment)localObject1).asset))).intValue() - ((Nxt.Transaction.ColoredCoinsAssetTransferAttachment)localObject1).quantity));
            }
            synchronized (localAccount3)
            {
              localObject2 = (Integer)localAccount3.assetBalances.get(Long.valueOf(((Nxt.Transaction.ColoredCoinsAssetTransferAttachment)localObject1).asset));
              if (localObject2 == null)
              {
                localAccount3.assetBalances.put(Long.valueOf(((Nxt.Transaction.ColoredCoinsAssetTransferAttachment)localObject1).asset), Integer.valueOf(((Nxt.Transaction.ColoredCoinsAssetTransferAttachment)localObject1).quantity));
                localAccount3.unconfirmedAssetBalances.put(Long.valueOf(((Nxt.Transaction.ColoredCoinsAssetTransferAttachment)localObject1).asset), Integer.valueOf(((Nxt.Transaction.ColoredCoinsAssetTransferAttachment)localObject1).quantity));
              }
              else
              {
                localAccount3.assetBalances.put(Long.valueOf(((Nxt.Transaction.ColoredCoinsAssetTransferAttachment)localObject1).asset), Integer.valueOf(((Integer)localObject2).intValue() + ((Nxt.Transaction.ColoredCoinsAssetTransferAttachment)localObject1).quantity));
                localAccount3.unconfirmedAssetBalances.put(Long.valueOf(((Nxt.Transaction.ColoredCoinsAssetTransferAttachment)localObject1).asset), Integer.valueOf(((Integer)localAccount3.unconfirmedAssetBalances.get(Long.valueOf(((Nxt.Transaction.ColoredCoinsAssetTransferAttachment)localObject1).asset))).intValue() + ((Nxt.Transaction.ColoredCoinsAssetTransferAttachment)localObject1).quantity));
              }
            }
            break;
          case 2: 
            localObject1 = (Nxt.Transaction.ColoredCoinsAskOrderPlacementAttachment)localTransaction.attachment;
            ??? = new Nxt.AskOrder(localTransaction.getId(), localAccount2, ((Nxt.Transaction.ColoredCoinsAskOrderPlacementAttachment)localObject1).asset, ((Nxt.Transaction.ColoredCoinsAskOrderPlacementAttachment)localObject1).quantity, ((Nxt.Transaction.ColoredCoinsAskOrderPlacementAttachment)localObject1).price);
            synchronized (localAccount2)
            {
              localAccount2.assetBalances.put(Long.valueOf(((Nxt.Transaction.ColoredCoinsAskOrderPlacementAttachment)localObject1).asset), Integer.valueOf(((Integer)localAccount2.assetBalances.get(Long.valueOf(((Nxt.Transaction.ColoredCoinsAskOrderPlacementAttachment)localObject1).asset))).intValue() - ((Nxt.Transaction.ColoredCoinsAskOrderPlacementAttachment)localObject1).quantity));
              localAccount2.unconfirmedAssetBalances.put(Long.valueOf(((Nxt.Transaction.ColoredCoinsAskOrderPlacementAttachment)localObject1).asset), Integer.valueOf(((Integer)localAccount2.unconfirmedAssetBalances.get(Long.valueOf(((Nxt.Transaction.ColoredCoinsAskOrderPlacementAttachment)localObject1).asset))).intValue() - ((Nxt.Transaction.ColoredCoinsAskOrderPlacementAttachment)localObject1).quantity));
            }
            synchronized (Nxt.askOrders)
            {
              Nxt.askOrders.put(Long.valueOf(((Nxt.AskOrder)???).id), ???);
              ((TreeSet)Nxt.sortedAskOrders.get(Long.valueOf(((Nxt.Transaction.ColoredCoinsAskOrderPlacementAttachment)localObject1).asset))).add(???);
            }
            Nxt.matchOrders(((Nxt.Transaction.ColoredCoinsAskOrderPlacementAttachment)localObject1).asset);
            break;
          case 3: 
            localObject1 = (Nxt.Transaction.ColoredCoinsBidOrderPlacementAttachment)localTransaction.attachment;
            ??? = new Nxt.BidOrder(localTransaction.getId(), localAccount2, ((Nxt.Transaction.ColoredCoinsBidOrderPlacementAttachment)localObject1).asset, ((Nxt.Transaction.ColoredCoinsBidOrderPlacementAttachment)localObject1).quantity, ((Nxt.Transaction.ColoredCoinsBidOrderPlacementAttachment)localObject1).price);
            localAccount2.addToBalanceAndUnconfirmedBalance(-((Nxt.Transaction.ColoredCoinsBidOrderPlacementAttachment)localObject1).quantity * ((Nxt.Transaction.ColoredCoinsBidOrderPlacementAttachment)localObject1).price);
            synchronized (Nxt.bidOrders)
            {
              Nxt.bidOrders.put(Long.valueOf(((Nxt.BidOrder)???).id), ???);
              ((TreeSet)Nxt.sortedBidOrders.get(Long.valueOf(((Nxt.Transaction.ColoredCoinsBidOrderPlacementAttachment)localObject1).asset))).add(???);
            }
            Nxt.matchOrders(((Nxt.Transaction.ColoredCoinsBidOrderPlacementAttachment)localObject1).asset);
            break;
          case 4: 
            localObject1 = (Nxt.Transaction.ColoredCoinsAskOrderCancellationAttachment)localTransaction.attachment;
            synchronized (Nxt.askOrders)
            {
              ??? = (Nxt.AskOrder)Nxt.askOrders.remove(Long.valueOf(((Nxt.Transaction.ColoredCoinsAskOrderCancellationAttachment)localObject1).order));
              ((TreeSet)Nxt.sortedAskOrders.get(Long.valueOf(((Nxt.AskOrder)???).asset))).remove(???);
            }
            synchronized (localAccount2)
            {
              localAccount2.assetBalances.put(Long.valueOf(((Nxt.AskOrder)???).asset), Integer.valueOf(((Integer)localAccount2.assetBalances.get(Long.valueOf(((Nxt.AskOrder)???).asset))).intValue() + ((Nxt.AskOrder)???).quantity));
              localAccount2.unconfirmedAssetBalances.put(Long.valueOf(((Nxt.AskOrder)???).asset), Integer.valueOf(((Integer)localAccount2.unconfirmedAssetBalances.get(Long.valueOf(((Nxt.AskOrder)???).asset))).intValue() + ((Nxt.AskOrder)???).quantity));
            }
            break;
          case 5: 
            localObject1 = (Nxt.Transaction.ColoredCoinsBidOrderCancellationAttachment)localTransaction.attachment;
            synchronized (Nxt.bidOrders)
            {
              ??? = (Nxt.BidOrder)Nxt.bidOrders.remove(Long.valueOf(((Nxt.Transaction.ColoredCoinsBidOrderCancellationAttachment)localObject1).order));
              ((TreeSet)Nxt.sortedBidOrders.get(Long.valueOf(((Nxt.BidOrder)???).asset))).remove(???);
            }
            localAccount2.addToBalanceAndUnconfirmedBalance(((Nxt.BidOrder)???).quantity * ((Nxt.BidOrder)???).price);
          }
          break;
        }
      }
    }
  }
  
  static long getBaseTarget()
    throws Exception
  {
    if (Nxt.lastBlock == 2680262203532249785L) {
      return ((Block)Nxt.blocks.get(Long.valueOf(2680262203532249785L))).baseTarget;
    }
    Block localBlock1 = getLastBlock();
    Block localBlock2 = (Block)Nxt.blocks.get(Long.valueOf(localBlock1.previousBlock));
    long l1 = localBlock2.baseTarget;
    long l2 = BigInteger.valueOf(l1).multiply(BigInteger.valueOf(localBlock1.timestamp - localBlock2.timestamp)).divide(BigInteger.valueOf(60L)).longValue();
    if ((l2 < 0L) || (l2 > 153722867000000000L)) {
      l2 = 153722867000000000L;
    }
    if (l2 < l1 / 2L) {
      l2 = l1 / 2L;
    }
    if (l2 == 0L) {
      l2 = 1L;
    }
    long l3 = l1 * 2L;
    if (l3 < 0L) {
      l3 = 153722867000000000L;
    }
    if (l2 > l3) {
      l2 = l3;
    }
    return l2;
  }
  
  static Block getBlock(JSONObject paramJSONObject)
  {
    int i = ((Long)paramJSONObject.get("version")).intValue();
    int j = ((Long)paramJSONObject.get("timestamp")).intValue();
    long l = new BigInteger((String)paramJSONObject.get("previousBlock")).longValue();
    int k = ((Long)paramJSONObject.get("numberOfTransactions")).intValue();
    int m = ((Long)paramJSONObject.get("totalAmount")).intValue();
    int n = ((Long)paramJSONObject.get("totalFee")).intValue();
    int i1 = ((Long)paramJSONObject.get("payloadLength")).intValue();
    byte[] arrayOfByte1 = Nxt.convert((String)paramJSONObject.get("payloadHash"));
    byte[] arrayOfByte2 = Nxt.convert((String)paramJSONObject.get("generatorPublicKey"));
    byte[] arrayOfByte3 = Nxt.convert((String)paramJSONObject.get("generationSignature"));
    byte[] arrayOfByte4 = Nxt.convert((String)paramJSONObject.get("blockSignature"));
    byte[] arrayOfByte5 = i == 1 ? null : Nxt.convert((String)paramJSONObject.get("previousBlockHash"));
    if ((k > 255) || (i1 > 32640)) {
      return null;
    }
    return new Block(i, j, l, k, m, n, i1, arrayOfByte1, arrayOfByte2, arrayOfByte3, arrayOfByte4, arrayOfByte5);
  }
  
  byte[] getBytes()
  {
    ByteBuffer localByteBuffer = ByteBuffer.allocate(224);
    localByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    localByteBuffer.putInt(this.version);
    localByteBuffer.putInt(this.timestamp);
    localByteBuffer.putLong(this.previousBlock);
    localByteBuffer.putInt(this.numberOfTransactions);
    localByteBuffer.putInt(this.totalAmount);
    localByteBuffer.putInt(this.totalFee);
    localByteBuffer.putInt(this.payloadLength);
    localByteBuffer.put(this.payloadHash);
    localByteBuffer.put(this.generatorPublicKey);
    localByteBuffer.put(this.generationSignature);
    if (this.version > 1) {
      localByteBuffer.put(this.previousBlockHash);
    }
    localByteBuffer.put(this.blockSignature);
    return localByteBuffer.array();
  }
  
  long getId()
    throws Exception
  {
    byte[] arrayOfByte = MessageDigest.getInstance("SHA-256").digest(getBytes());
    BigInteger localBigInteger = new BigInteger(1, new byte[] { arrayOfByte[7], arrayOfByte[6], arrayOfByte[5], arrayOfByte[4], arrayOfByte[3], arrayOfByte[2], arrayOfByte[1], arrayOfByte[0] });
    return localBigInteger.longValue();
  }
  
  JSONObject getJSONObject(Map<Long, Nxt.Transaction> paramMap)
  {
    JSONObject localJSONObject = new JSONObject();
    localJSONObject.put("version", Integer.valueOf(this.version));
    localJSONObject.put("timestamp", Integer.valueOf(this.timestamp));
    localJSONObject.put("previousBlock", Nxt.convert(this.previousBlock));
    localJSONObject.put("numberOfTransactions", Integer.valueOf(this.numberOfTransactions));
    localJSONObject.put("totalAmount", Integer.valueOf(this.totalAmount));
    localJSONObject.put("totalFee", Integer.valueOf(this.totalFee));
    localJSONObject.put("payloadLength", Integer.valueOf(this.payloadLength));
    localJSONObject.put("payloadHash", Nxt.convert(this.payloadHash));
    localJSONObject.put("generatorPublicKey", Nxt.convert(this.generatorPublicKey));
    localJSONObject.put("generationSignature", Nxt.convert(this.generationSignature));
    if (this.version > 1) {
      localJSONObject.put("previousBlockHash", Nxt.convert(this.previousBlockHash));
    }
    localJSONObject.put("blockSignature", Nxt.convert(this.blockSignature));
    JSONArray localJSONArray = new JSONArray();
    for (int i = 0; i < this.numberOfTransactions; i++) {
      localJSONArray.add(((Nxt.Transaction)paramMap.get(Long.valueOf(this.transactions[i]))).getJSONObject());
    }
    localJSONObject.put("transactions", localJSONArray);
    return localJSONObject;
  }
  
  static Block getLastBlock()
  {
    return (Block)Nxt.blocks.get(Long.valueOf(Nxt.lastBlock));
  }
  
  static void loadBlocks(String paramString)
    throws Exception
  {
    FileInputStream localFileInputStream = new FileInputStream(paramString);
    Object localObject1 = null;
    try
    {
      ObjectInputStream localObjectInputStream = new ObjectInputStream(localFileInputStream);
      Object localObject2 = null;
      try
      {
        Nxt.blockCounter.set(localObjectInputStream.readInt());
        Nxt.blocks.clear();
        Nxt.blocks.putAll((HashMap)localObjectInputStream.readObject());
        Nxt.lastBlock = localObjectInputStream.readLong();
      }
      catch (Throwable localThrowable4)
      {
        localObject2 = localThrowable4;
        throw localThrowable4;
      }
      finally {}
    }
    catch (Throwable localThrowable2)
    {
      localObject1 = localThrowable2;
      throw localThrowable2;
    }
    finally
    {
      if (localFileInputStream != null) {
        if (localObject1 != null) {
          try
          {
            localFileInputStream.close();
          }
          catch (Throwable localThrowable6)
          {
            localObject1.addSuppressed(localThrowable6);
          }
        } else {
          localFileInputStream.close();
        }
      }
    }
  }
  
  static boolean popLastBlock()
  {
    if (Nxt.lastBlock == 2680262203532249785L) {
      return false;
    }
    try
    {
      JSONObject localJSONObject1 = new JSONObject();
      localJSONObject1.put("response", "processNewData");
      JSONArray localJSONArray = new JSONArray();
      Block localBlock;
      Object localObject2;
      synchronized (Nxt.blocksAndTransactionsLock)
      {
        localBlock = getLastBlock();
        localObject1 = (Nxt.Account)Nxt.accounts.get(Long.valueOf(Nxt.Account.getId(localBlock.generatorPublicKey)));
        ((Nxt.Account)localObject1).addToBalanceAndUnconfirmedBalance(-localBlock.totalFee * 100L);
        for (int i = 0; i < localBlock.numberOfTransactions; i++)
        {
          localObject2 = (Nxt.Transaction)Nxt.transactions.remove(Long.valueOf(localBlock.transactions[i]));
          Nxt.unconfirmedTransactions.put(Long.valueOf(localBlock.transactions[i]), localObject2);
          Nxt.Account localAccount1 = (Nxt.Account)Nxt.accounts.get(Long.valueOf(Nxt.Account.getId(((Nxt.Transaction)localObject2).senderPublicKey)));
          localAccount1.addToBalance((((Nxt.Transaction)localObject2).amount + ((Nxt.Transaction)localObject2).fee) * 100L);
          Nxt.Account localAccount2 = (Nxt.Account)Nxt.accounts.get(Long.valueOf(((Nxt.Transaction)localObject2).recipient));
          localAccount2.addToBalanceAndUnconfirmedBalance(-((Nxt.Transaction)localObject2).amount * 100L);
          JSONObject localJSONObject2 = new JSONObject();
          localJSONObject2.put("index", Integer.valueOf(((Nxt.Transaction)localObject2).index));
          localJSONObject2.put("timestamp", Integer.valueOf(((Nxt.Transaction)localObject2).timestamp));
          localJSONObject2.put("deadline", Short.valueOf(((Nxt.Transaction)localObject2).deadline));
          localJSONObject2.put("recipient", Nxt.convert(((Nxt.Transaction)localObject2).recipient));
          localJSONObject2.put("amount", Integer.valueOf(((Nxt.Transaction)localObject2).amount));
          localJSONObject2.put("fee", Integer.valueOf(((Nxt.Transaction)localObject2).fee));
          localJSONObject2.put("sender", Nxt.convert(Nxt.Account.getId(((Nxt.Transaction)localObject2).senderPublicKey)));
          localJSONObject2.put("id", Nxt.convert(((Nxt.Transaction)localObject2).getId()));
          localJSONArray.add(localJSONObject2);
        }
        Nxt.lastBlock = localBlock.previousBlock;
      }
      ??? = new JSONArray();
      Object localObject1 = new JSONObject();
      ((JSONObject)localObject1).put("index", Integer.valueOf(localBlock.index));
      ((JSONObject)localObject1).put("timestamp", Integer.valueOf(localBlock.timestamp));
      ((JSONObject)localObject1).put("numberOfTransactions", Integer.valueOf(localBlock.numberOfTransactions));
      ((JSONObject)localObject1).put("totalAmount", Integer.valueOf(localBlock.totalAmount));
      ((JSONObject)localObject1).put("totalFee", Integer.valueOf(localBlock.totalFee));
      ((JSONObject)localObject1).put("payloadLength", Integer.valueOf(localBlock.payloadLength));
      ((JSONObject)localObject1).put("generator", Nxt.convert(Nxt.Account.getId(localBlock.generatorPublicKey)));
      ((JSONObject)localObject1).put("height", Integer.valueOf(localBlock.height));
      ((JSONObject)localObject1).put("version", Integer.valueOf(localBlock.version));
      ((JSONObject)localObject1).put("block", Nxt.convert(localBlock.getId()));
      ((JSONObject)localObject1).put("baseTarget", BigInteger.valueOf(localBlock.baseTarget).multiply(BigInteger.valueOf(100000L)).divide(BigInteger.valueOf(153722867L)));
      ((JSONArray)???).add(localObject1);
      localJSONObject1.put("addedOrphanedBlocks", ???);
      if (localJSONArray.size() > 0) {
        localJSONObject1.put("addedUnconfirmedTransactions", localJSONArray);
      }
      Iterator localIterator = Nxt.users.values().iterator();
      while (localIterator.hasNext())
      {
        localObject2 = (Nxt.User)localIterator.next();
        ((Nxt.User)localObject2).send(localJSONObject1);
      }
    }
    catch (Exception localException)
    {
      Nxt.logMessage("19: " + localException.toString());
      return false;
    }
    return true;
  }
  
  static boolean pushBlock(ByteBuffer paramByteBuffer, boolean paramBoolean)
    throws Exception
  {
    paramByteBuffer.flip();
    int i = paramByteBuffer.getInt();
    if (i != (getLastBlock().height < 30000 ? 1 : 2)) {
      return false;
    }
    if (getLastBlock().height == 30000)
    {
      byte[] arrayOfByte1 = Nxt.Transaction.calculateTransactionsChecksum();
      if (Nxt.CHECKSUM_TRANSPARENT_FORGING == null)
      {
        System.out.println(Arrays.toString(arrayOfByte1));
      }
      else if (!Arrays.equals(arrayOfByte1, Nxt.CHECKSUM_TRANSPARENT_FORGING))
      {
        Nxt.logMessage("Checksum failed at block 30000");
        return false;
      }
    }
    int j = paramByteBuffer.getInt();
    long l1 = paramByteBuffer.getLong();
    int k = paramByteBuffer.getInt();
    int m = paramByteBuffer.getInt();
    int n = paramByteBuffer.getInt();
    int i1 = paramByteBuffer.getInt();
    byte[] arrayOfByte2 = new byte[32];
    paramByteBuffer.get(arrayOfByte2);
    byte[] arrayOfByte3 = new byte[32];
    paramByteBuffer.get(arrayOfByte3);
    byte[] arrayOfByte4;
    byte[] arrayOfByte5;
    if (i == 1)
    {
      arrayOfByte4 = new byte[64];
      paramByteBuffer.get(arrayOfByte4);
      arrayOfByte5 = null;
    }
    else
    {
      arrayOfByte4 = new byte[32];
      paramByteBuffer.get(arrayOfByte4);
      arrayOfByte5 = new byte[32];
      paramByteBuffer.get(arrayOfByte5);
      if (!Arrays.equals(MessageDigest.getInstance("SHA-256").digest(getLastBlock().getBytes()), arrayOfByte5)) {
        return false;
      }
    }
    byte[] arrayOfByte6 = new byte[64];
    paramByteBuffer.get(arrayOfByte6);
    int i2 = Nxt.getEpochTime(System.currentTimeMillis());
    if ((j > i2 + 15) || (j <= getLastBlock().timestamp)) {
      return false;
    }
    if ((i1 > 32640) || (224 + i1 != paramByteBuffer.capacity()) || (k > 255)) {
      return false;
    }
    Block localBlock = new Block(i, j, l1, k, m, n, i1, arrayOfByte2, arrayOfByte3, arrayOfByte4, arrayOfByte6, arrayOfByte5);
    localBlock.index = Nxt.blockCounter.incrementAndGet();
    try
    {
      if ((localBlock.numberOfTransactions > 255) || (localBlock.previousBlock != Nxt.lastBlock) || (Nxt.blocks.get(Long.valueOf(localBlock.getId())) != null) || (!localBlock.verifyGenerationSignature()) || (!localBlock.verifyBlockSignature())) {
        return false;
      }
      HashMap localHashMap1 = new HashMap();
      HashSet localHashSet = new HashSet();
      for (int i3 = 0; i3 < localBlock.numberOfTransactions; i3++)
      {
        localObject1 = Nxt.Transaction.getTransaction(paramByteBuffer);
        ((Nxt.Transaction)localObject1).index = Nxt.transactionCounter.incrementAndGet();
        if (localHashMap1.put(Long.valueOf(localBlock.transactions[i3] = ((Nxt.Transaction)localObject1).getId()), localObject1) != null) {
          return false;
        }
        switch (((Nxt.Transaction)localObject1).type)
        {
        case 1: 
          switch (((Nxt.Transaction)localObject1).subtype)
          {
          case 1: 
            if (!localHashSet.add(((Nxt.Transaction.MessagingAliasAssignmentAttachment)((Nxt.Transaction)localObject1).attachment).alias.toLowerCase())) {
              return false;
            }
            break;
          }
          break;
        }
      }
      Arrays.sort(localBlock.transactions);
      HashMap localHashMap2 = new HashMap();
      Object localObject1 = new HashMap();
      int i4 = 0;
      int i5 = 0;
      Object localObject3;
      for (int i6 = 0; i6 < localBlock.numberOfTransactions; i6++)
      {
        localObject2 = (Nxt.Transaction)localHashMap1.get(Long.valueOf(localBlock.transactions[i6]));
        if ((((Nxt.Transaction)localObject2).timestamp > i2 + 15) || (((Nxt.Transaction)localObject2).deadline < 1) || ((((Nxt.Transaction)localObject2).timestamp + ((Nxt.Transaction)localObject2).deadline * 60 < j) && (getLastBlock().height > 303)) || (((Nxt.Transaction)localObject2).fee <= 0) || (((Nxt.Transaction)localObject2).fee > 1000000000L) || (((Nxt.Transaction)localObject2).amount < 0) || (((Nxt.Transaction)localObject2).amount > 1000000000L) || (!((Nxt.Transaction)localObject2).validateAttachment()) || (Nxt.transactions.get(Long.valueOf(localBlock.transactions[i6])) != null) || ((((Nxt.Transaction)localObject2).referencedTransaction != 0L) && (Nxt.transactions.get(Long.valueOf(((Nxt.Transaction)localObject2).referencedTransaction)) == null) && (localHashMap1.get(Long.valueOf(((Nxt.Transaction)localObject2).referencedTransaction)) == null)) || ((Nxt.unconfirmedTransactions.get(Long.valueOf(localBlock.transactions[i6])) == null) && (!((Nxt.Transaction)localObject2).verify()))) {
          break;
        }
        long l2 = Nxt.Account.getId(((Nxt.Transaction)localObject2).senderPublicKey);
        Long localLong = (Long)localHashMap2.get(Long.valueOf(l2));
        if (localLong == null) {
          localLong = Long.valueOf(0L);
        }
        localHashMap2.put(Long.valueOf(l2), Long.valueOf(localLong.longValue() + (((Nxt.Transaction)localObject2).amount + ((Nxt.Transaction)localObject2).fee) * 100L));
        if (((Nxt.Transaction)localObject2).type == 0)
        {
          if (((Nxt.Transaction)localObject2).subtype != 0) {
            break;
          }
          i4 += ((Nxt.Transaction)localObject2).amount;
        }
        else if (((Nxt.Transaction)localObject2).type == 1)
        {
          if (((Nxt.Transaction)localObject2).subtype != 1) {
            break;
          }
        }
        else
        {
          if (((Nxt.Transaction)localObject2).type != 2) {
            break;
          }
          if (((Nxt.Transaction)localObject2).subtype == 1)
          {
            localObject3 = (Nxt.Transaction.ColoredCoinsAssetTransferAttachment)((Nxt.Transaction)localObject2).attachment;
            localObject4 = (HashMap)((HashMap)localObject1).get(Long.valueOf(l2));
            if (localObject4 == null)
            {
              localObject4 = new HashMap();
              ((HashMap)localObject1).put(Long.valueOf(l2), localObject4);
            }
            localObject5 = (Long)((HashMap)localObject4).get(Long.valueOf(((Nxt.Transaction.ColoredCoinsAssetTransferAttachment)localObject3).asset));
            if (localObject5 == null) {
              localObject5 = new Long(0L);
            }
            ((HashMap)localObject4).put(Long.valueOf(((Nxt.Transaction.ColoredCoinsAssetTransferAttachment)localObject3).asset), Long.valueOf(((Long)localObject5).longValue() + ((Nxt.Transaction.ColoredCoinsAssetTransferAttachment)localObject3).quantity));
          }
          else if (((Nxt.Transaction)localObject2).subtype == 2)
          {
            localObject3 = (Nxt.Transaction.ColoredCoinsAskOrderPlacementAttachment)((Nxt.Transaction)localObject2).attachment;
            localObject4 = (HashMap)((HashMap)localObject1).get(Long.valueOf(l2));
            if (localObject4 == null)
            {
              localObject4 = new HashMap();
              ((HashMap)localObject1).put(Long.valueOf(l2), localObject4);
            }
            localObject5 = (Long)((HashMap)localObject4).get(Long.valueOf(((Nxt.Transaction.ColoredCoinsAskOrderPlacementAttachment)localObject3).asset));
            if (localObject5 == null) {
              localObject5 = new Long(0L);
            }
            ((HashMap)localObject4).put(Long.valueOf(((Nxt.Transaction.ColoredCoinsAskOrderPlacementAttachment)localObject3).asset), Long.valueOf(((Long)localObject5).longValue() + ((Nxt.Transaction.ColoredCoinsAskOrderPlacementAttachment)localObject3).quantity));
          }
          else if (((Nxt.Transaction)localObject2).subtype == 3)
          {
            localObject3 = (Nxt.Transaction.ColoredCoinsBidOrderPlacementAttachment)((Nxt.Transaction)localObject2).attachment;
            localHashMap2.put(Long.valueOf(l2), Long.valueOf(localLong.longValue() + ((Nxt.Transaction.ColoredCoinsBidOrderPlacementAttachment)localObject3).quantity * ((Nxt.Transaction.ColoredCoinsBidOrderPlacementAttachment)localObject3).price));
          }
          else
          {
            if ((((Nxt.Transaction)localObject2).subtype != 0) && (((Nxt.Transaction)localObject2).subtype != 4) && (((Nxt.Transaction)localObject2).subtype != 5)) {
              break;
            }
          }
        }
        i5 += ((Nxt.Transaction)localObject2).fee;
      }
      if ((i6 != localBlock.numberOfTransactions) || (i4 != localBlock.totalAmount) || (i5 != localBlock.totalFee)) {
        return false;
      }
      Object localObject2 = MessageDigest.getInstance("SHA-256");
      for (i6 = 0; i6 < localBlock.numberOfTransactions; i6++) {
        ((MessageDigest)localObject2).update(((Nxt.Transaction)localHashMap1.get(Long.valueOf(localBlock.transactions[i6]))).getBytes());
      }
      if (!Arrays.equals(((MessageDigest)localObject2).digest(), localBlock.payloadHash)) {
        return false;
      }
      Object localObject6;
      JSONArray localJSONArray1;
      JSONArray localJSONArray2;
      synchronized (Nxt.blocksAndTransactionsLock)
      {
        localObject3 = localHashMap2.entrySet().iterator();
        while (((Iterator)localObject3).hasNext())
        {
          localObject4 = (Map.Entry)((Iterator)localObject3).next();
          localObject5 = (Nxt.Account)Nxt.accounts.get(((Map.Entry)localObject4).getKey());
          if (((Nxt.Account)localObject5).getBalance() < ((Long)((Map.Entry)localObject4).getValue()).longValue()) {
            return false;
          }
        }
        localObject3 = ((HashMap)localObject1).entrySet().iterator();
        Object localObject7;
        while (((Iterator)localObject3).hasNext())
        {
          localObject4 = (Map.Entry)((Iterator)localObject3).next();
          localObject5 = (Nxt.Account)Nxt.accounts.get(((Map.Entry)localObject4).getKey());
          localObject6 = ((HashMap)((Map.Entry)localObject4).getValue()).entrySet().iterator();
          while (((Iterator)localObject6).hasNext())
          {
            localObject7 = (Map.Entry)((Iterator)localObject6).next();
            long l4 = ((Long)((Map.Entry)localObject7).getKey()).longValue();
            long l5 = ((Long)((Map.Entry)localObject7).getValue()).longValue();
            if (((Integer)((Nxt.Account)localObject5).assetBalances.get(Long.valueOf(l4))).intValue() < l5) {
              return false;
            }
          }
        }
        if (localBlock.previousBlock != Nxt.lastBlock) {
          return false;
        }
        localObject3 = localHashMap1.entrySet().iterator();
        while (((Iterator)localObject3).hasNext())
        {
          localObject4 = (Map.Entry)((Iterator)localObject3).next();
          localObject5 = (Nxt.Transaction)((Map.Entry)localObject4).getValue();
          ((Nxt.Transaction)localObject5).height = localBlock.height;
          Nxt.transactions.put(((Map.Entry)localObject4).getKey(), localObject5);
        }
        localBlock.analyze();
        localJSONArray1 = new JSONArray();
        localJSONArray2 = new JSONArray();
        localObject3 = localHashMap1.entrySet().iterator();
        while (((Iterator)localObject3).hasNext())
        {
          localObject4 = (Map.Entry)((Iterator)localObject3).next();
          localObject5 = (Nxt.Transaction)((Map.Entry)localObject4).getValue();
          localObject6 = new JSONObject();
          ((JSONObject)localObject6).put("index", Integer.valueOf(((Nxt.Transaction)localObject5).index));
          ((JSONObject)localObject6).put("blockTimestamp", Integer.valueOf(localBlock.timestamp));
          ((JSONObject)localObject6).put("transactionTimestamp", Integer.valueOf(((Nxt.Transaction)localObject5).timestamp));
          ((JSONObject)localObject6).put("sender", Nxt.convert(Nxt.Account.getId(((Nxt.Transaction)localObject5).senderPublicKey)));
          ((JSONObject)localObject6).put("recipient", Nxt.convert(((Nxt.Transaction)localObject5).recipient));
          ((JSONObject)localObject6).put("amount", Integer.valueOf(((Nxt.Transaction)localObject5).amount));
          ((JSONObject)localObject6).put("fee", Integer.valueOf(((Nxt.Transaction)localObject5).fee));
          ((JSONObject)localObject6).put("id", Nxt.convert(((Nxt.Transaction)localObject5).getId()));
          localJSONArray1.add(localObject6);
          localObject7 = (Nxt.Transaction)Nxt.unconfirmedTransactions.remove(((Map.Entry)localObject4).getKey());
          if (localObject7 != null)
          {
            JSONObject localJSONObject2 = new JSONObject();
            localJSONObject2.put("index", Integer.valueOf(((Nxt.Transaction)localObject7).index));
            localJSONArray2.add(localJSONObject2);
            Nxt.Account localAccount = (Nxt.Account)Nxt.accounts.get(Long.valueOf(Nxt.Account.getId(((Nxt.Transaction)localObject7).senderPublicKey)));
            localAccount.addToUnconfirmedBalance((((Nxt.Transaction)localObject7).amount + ((Nxt.Transaction)localObject7).fee) * 100L);
          }
        }
        long l3 = localBlock.getId();
        for (i6 = 0; i6 < localBlock.transactions.length; i6++) {
          ((Nxt.Transaction)Nxt.transactions.get(Long.valueOf(localBlock.transactions[i6]))).block = l3;
        }
        if (paramBoolean)
        {
          Nxt.Transaction.saveTransactions("transactions.nxt");
          saveBlocks("blocks.nxt", false);
        }
      }
      if (localBlock.timestamp >= i2 - 15)
      {
        ??? = localBlock.getJSONObject(Nxt.transactions);
        ((JSONObject)???).put("requestType", "processBlock");
        Nxt.Peer.sendToAllPeers((JSONObject)???);
      }
      ??? = new JSONArray();
      JSONObject localJSONObject1 = new JSONObject();
      localJSONObject1.put("index", Integer.valueOf(localBlock.index));
      localJSONObject1.put("timestamp", Integer.valueOf(localBlock.timestamp));
      localJSONObject1.put("numberOfTransactions", Integer.valueOf(localBlock.numberOfTransactions));
      localJSONObject1.put("totalAmount", Integer.valueOf(localBlock.totalAmount));
      localJSONObject1.put("totalFee", Integer.valueOf(localBlock.totalFee));
      localJSONObject1.put("payloadLength", Integer.valueOf(localBlock.payloadLength));
      localJSONObject1.put("generator", Nxt.convert(Nxt.Account.getId(localBlock.generatorPublicKey)));
      localJSONObject1.put("height", Integer.valueOf(getLastBlock().height));
      localJSONObject1.put("version", Integer.valueOf(localBlock.version));
      localJSONObject1.put("block", Nxt.convert(localBlock.getId()));
      localJSONObject1.put("baseTarget", BigInteger.valueOf(localBlock.baseTarget).multiply(BigInteger.valueOf(100000L)).divide(BigInteger.valueOf(153722867L)));
      ((JSONArray)???).add(localJSONObject1);
      Object localObject4 = new JSONObject();
      ((JSONObject)localObject4).put("response", "processNewData");
      ((JSONObject)localObject4).put("addedConfirmedTransactions", localJSONArray1);
      if (localJSONArray2.size() > 0) {
        ((JSONObject)localObject4).put("removedUnconfirmedTransactions", localJSONArray2);
      }
      ((JSONObject)localObject4).put("addedRecentBlocks", ???);
      Object localObject5 = Nxt.users.values().iterator();
      while (((Iterator)localObject5).hasNext())
      {
        localObject6 = (Nxt.User)((Iterator)localObject5).next();
        ((Nxt.User)localObject6).send((JSONObject)localObject4);
      }
      return true;
    }
    catch (Exception localException)
    {
      Nxt.logMessage("11: " + localException.toString());
    }
    return false;
  }
  
  static void saveBlocks(String paramString, boolean paramBoolean)
    throws Exception
  {
    FileOutputStream localFileOutputStream = new FileOutputStream(paramString);
    Object localObject1 = null;
    try
    {
      ObjectOutputStream localObjectOutputStream = new ObjectOutputStream(localFileOutputStream);
      Object localObject2 = null;
      try
      {
        localObjectOutputStream.writeInt(Nxt.blockCounter.get());
        localObjectOutputStream.writeObject(new HashMap(Nxt.blocks));
        localObjectOutputStream.writeLong(Nxt.lastBlock);
      }
      catch (Throwable localThrowable4)
      {
        localObject2 = localThrowable4;
        throw localThrowable4;
      }
      finally {}
    }
    catch (Throwable localThrowable2)
    {
      localObject1 = localThrowable2;
      throw localThrowable2;
    }
    finally
    {
      if (localFileOutputStream != null) {
        if (localObject1 != null) {
          try
          {
            localFileOutputStream.close();
          }
          catch (Throwable localThrowable6)
          {
            localObject1.addSuppressed(localThrowable6);
          }
        } else {
          localFileOutputStream.close();
        }
      }
    }
  }
  
  boolean verifyBlockSignature()
    throws Exception
  {
    Nxt.Account localAccount = (Nxt.Account)Nxt.accounts.get(Long.valueOf(Nxt.Account.getId(this.generatorPublicKey)));
    if (localAccount == null) {
      return false;
    }
    byte[] arrayOfByte1 = getBytes();
    byte[] arrayOfByte2 = new byte[arrayOfByte1.length - 64];
    System.arraycopy(arrayOfByte1, 0, arrayOfByte2, 0, arrayOfByte2.length);
    return (Nxt.Crypto.verify(this.blockSignature, arrayOfByte2, this.generatorPublicKey)) && (localAccount.setOrVerify(this.generatorPublicKey));
  }
  
  boolean verifyGenerationSignature()
  {
    try
    {
      Block localBlock = (Block)Nxt.blocks.get(Long.valueOf(this.previousBlock));
      if (localBlock == null) {
        return false;
      }
      if ((this.version == 1) && (!Nxt.Crypto.verify(this.generationSignature, localBlock.generationSignature, this.generatorPublicKey))) {
        return false;
      }
      Nxt.Account localAccount = (Nxt.Account)Nxt.accounts.get(Long.valueOf(Nxt.Account.getId(this.generatorPublicKey)));
      if ((localAccount == null) || (localAccount.getEffectiveBalance() <= 0)) {
        return false;
      }
      int i = this.timestamp - localBlock.timestamp;
      BigInteger localBigInteger1 = BigInteger.valueOf(getBaseTarget()).multiply(BigInteger.valueOf(localAccount.getEffectiveBalance())).multiply(BigInteger.valueOf(i));
      MessageDigest localMessageDigest = MessageDigest.getInstance("SHA-256");
      byte[] arrayOfByte;
      if (this.version == 1)
      {
        arrayOfByte = localMessageDigest.digest(this.generationSignature);
      }
      else
      {
        localMessageDigest.update(localBlock.generationSignature);
        arrayOfByte = localMessageDigest.digest(this.generatorPublicKey);
        if (!Arrays.equals(this.generationSignature, arrayOfByte)) {
          return false;
        }
      }
      BigInteger localBigInteger2 = new BigInteger(1, new byte[] { arrayOfByte[7], arrayOfByte[6], arrayOfByte[5], arrayOfByte[4], arrayOfByte[3], arrayOfByte[2], arrayOfByte[1], arrayOfByte[0] });
      return localBigInteger2.compareTo(localBigInteger1) < 0;
    }
    catch (Exception localException) {}
    return false;
  }
