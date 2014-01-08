import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

class Nxt$Transaction
  implements Comparable<Transaction>, Serializable
{
  static final long serialVersionUID = 0L;
  static final byte TYPE_PAYMENT = 0;
  static final byte TYPE_MESSAGING = 1;
  static final byte TYPE_COLORED_COINS = 2;
  static final byte SUBTYPE_PAYMENT_ORDINARY_PAYMENT = 0;
  static final byte SUBTYPE_MESSAGING_ARBITRARY_MESSAGE = 0;
  static final byte SUBTYPE_MESSAGING_ALIAS_ASSIGNMENT = 1;
  static final byte SUBTYPE_COLORED_COINS_ASSET_ISSUANCE = 0;
  static final byte SUBTYPE_COLORED_COINS_ASSET_TRANSFER = 1;
  static final byte SUBTYPE_COLORED_COINS_ASK_ORDER_PLACEMENT = 2;
  static final byte SUBTYPE_COLORED_COINS_BID_ORDER_PLACEMENT = 3;
  static final byte SUBTYPE_COLORED_COINS_ASK_ORDER_CANCELLATION = 4;
  static final byte SUBTYPE_COLORED_COINS_BID_ORDER_CANCELLATION = 5;
  static final int ASSET_ISSUANCE_FEE = 1000;
  final byte type;
  final byte subtype;
  int timestamp;
  final short deadline;
  final byte[] senderPublicKey;
  final long recipient;
  final int amount;
  final int fee;
  final long referencedTransaction;
  byte[] signature;
  Nxt.Transaction.Attachment attachment;
  int index;
  volatile long block;
  int height;
  
  Nxt$Transaction(byte paramByte1, byte paramByte2, int paramInt1, short paramShort, byte[] paramArrayOfByte1, long paramLong1, int paramInt2, int paramInt3, long paramLong2, byte[] paramArrayOfByte2)
  {
    this.type = paramByte1;
    this.subtype = paramByte2;
    this.timestamp = paramInt1;
    this.deadline = paramShort;
    this.senderPublicKey = paramArrayOfByte1;
    this.recipient = paramLong1;
    this.amount = paramInt2;
    this.fee = paramInt3;
    this.referencedTransaction = paramLong2;
    this.signature = paramArrayOfByte2;
    this.height = 2147483647;
  }
  
  public int compareTo(Transaction paramTransaction)
  {
    if (this.height < paramTransaction.height) {
      return -1;
    }
    if (this.height > paramTransaction.height) {
      return 1;
    }
    if (this.fee * 1048576L / getBytes().length > paramTransaction.fee * 1048576L / paramTransaction.getBytes().length) {
      return -1;
    }
    if (this.fee * 1048576L / getBytes().length < paramTransaction.fee * 1048576L / paramTransaction.getBytes().length) {
      return 1;
    }
    if (this.timestamp < paramTransaction.timestamp) {
      return -1;
    }
    if (this.timestamp > paramTransaction.timestamp) {
      return 1;
    }
    if (this.index < paramTransaction.index) {
      return -1;
    }
    if (this.index > paramTransaction.index) {
      return 1;
    }
    return 0;
  }
  
  byte[] getBytes()
  {
    ByteBuffer localByteBuffer = ByteBuffer.allocate(128 + (this.attachment == null ? 0 : this.attachment.getBytes().length));
    localByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    localByteBuffer.put(this.type);
    localByteBuffer.put(this.subtype);
    localByteBuffer.putInt(this.timestamp);
    localByteBuffer.putShort(this.deadline);
    localByteBuffer.put(this.senderPublicKey);
    localByteBuffer.putLong(this.recipient);
    localByteBuffer.putInt(this.amount);
    localByteBuffer.putInt(this.fee);
    localByteBuffer.putLong(this.referencedTransaction);
    localByteBuffer.put(this.signature);
    if (this.attachment != null) {
      localByteBuffer.put(this.attachment.getBytes());
    }
    return localByteBuffer.array();
  }
  
  long getId()
    throws Exception
  {
    byte[] arrayOfByte = MessageDigest.getInstance("SHA-256").digest(getBytes());
    BigInteger localBigInteger = new BigInteger(1, new byte[] { arrayOfByte[7], arrayOfByte[6], arrayOfByte[5], arrayOfByte[4], arrayOfByte[3], arrayOfByte[2], arrayOfByte[1], arrayOfByte[0] });
    return localBigInteger.longValue();
  }
  
  JSONObject getJSONObject()
  {
    JSONObject localJSONObject = new JSONObject();
    localJSONObject.put("type", Byte.valueOf(this.type));
    localJSONObject.put("subtype", Byte.valueOf(this.subtype));
    localJSONObject.put("timestamp", Integer.valueOf(this.timestamp));
    localJSONObject.put("deadline", Short.valueOf(this.deadline));
    localJSONObject.put("senderPublicKey", Nxt.convert(this.senderPublicKey));
    localJSONObject.put("recipient", Nxt.convert(this.recipient));
    localJSONObject.put("amount", Integer.valueOf(this.amount));
    localJSONObject.put("fee", Integer.valueOf(this.fee));
    localJSONObject.put("referencedTransaction", Nxt.convert(this.referencedTransaction));
    localJSONObject.put("signature", Nxt.convert(this.signature));
    if (this.attachment != null) {
      localJSONObject.put("attachment", this.attachment.getJSONObject());
    }
    return localJSONObject;
  }
  
  static Transaction getTransaction(ByteBuffer paramByteBuffer)
  {
    byte b1 = paramByteBuffer.get();
    byte b2 = paramByteBuffer.get();
    int i = paramByteBuffer.getInt();
    short s = paramByteBuffer.getShort();
    byte[] arrayOfByte1 = new byte[32];
    paramByteBuffer.get(arrayOfByte1);
    long l1 = paramByteBuffer.getLong();
    int j = paramByteBuffer.getInt();
    int k = paramByteBuffer.getInt();
    long l2 = paramByteBuffer.getLong();
    byte[] arrayOfByte2 = new byte[64];
    paramByteBuffer.get(arrayOfByte2);
    Transaction localTransaction = new Transaction(b1, b2, i, s, arrayOfByte1, l1, j, k, l2, arrayOfByte2);
    int m;
    byte[] arrayOfByte3;
    int n;
    byte[] arrayOfByte4;
    switch (b1)
    {
    case 1: 
      switch (b2)
      {
      case 1: 
        m = paramByteBuffer.get();
        arrayOfByte3 = new byte[m];
        paramByteBuffer.get(arrayOfByte3);
        n = paramByteBuffer.getShort();
        arrayOfByte4 = new byte[n];
        paramByteBuffer.get(arrayOfByte4);
        try
        {
          localTransaction.attachment = new Nxt.Transaction.MessagingAliasAssignmentAttachment(new String(arrayOfByte3, "UTF-8"), new String(arrayOfByte4, "UTF-8"));
        }
        catch (Exception localException1) {}
      }
      break;
    case 2: 
      long l3;
      long l4;
      switch (b2)
      {
      case 0: 
        m = paramByteBuffer.get();
        arrayOfByte3 = new byte[m];
        paramByteBuffer.get(arrayOfByte3);
        n = paramByteBuffer.getShort();
        arrayOfByte4 = new byte[n];
        paramByteBuffer.get(arrayOfByte4);
        int i1 = paramByteBuffer.getInt();
        try
        {
          localTransaction.attachment = new Nxt.Transaction.ColoredCoinsAssetIssuanceAttachment(new String(arrayOfByte3, "UTF-8"), new String(arrayOfByte4, "UTF-8"), i1);
        }
        catch (Exception localException2) {}
        break;
      case 1: 
        l3 = paramByteBuffer.getLong();
        n = paramByteBuffer.getInt();
        localTransaction.attachment = new Nxt.Transaction.ColoredCoinsAssetTransferAttachment(l3, n);
        break;
      case 2: 
        l3 = paramByteBuffer.getLong();
        n = paramByteBuffer.getInt();
        l4 = paramByteBuffer.getLong();
        localTransaction.attachment = new Nxt.Transaction.ColoredCoinsAskOrderPlacementAttachment(l3, n, l4);
        break;
      case 3: 
        l3 = paramByteBuffer.getLong();
        n = paramByteBuffer.getInt();
        l4 = paramByteBuffer.getLong();
        localTransaction.attachment = new Nxt.Transaction.ColoredCoinsBidOrderPlacementAttachment(l3, n, l4);
        break;
      case 4: 
        l3 = paramByteBuffer.getLong();
        localTransaction.attachment = new Nxt.Transaction.ColoredCoinsAskOrderCancellationAttachment(l3);
        break;
      case 5: 
        l3 = paramByteBuffer.getLong();
        localTransaction.attachment = new Nxt.Transaction.ColoredCoinsBidOrderCancellationAttachment(l3);
      }
      break;
    }
    return localTransaction;
  }
  
  static Transaction getTransaction(JSONObject paramJSONObject)
  {
    byte b1 = ((Long)paramJSONObject.get("type")).byteValue();
    byte b2 = ((Long)paramJSONObject.get("subtype")).byteValue();
    int i = ((Long)paramJSONObject.get("timestamp")).intValue();
    short s = ((Long)paramJSONObject.get("deadline")).shortValue();
    byte[] arrayOfByte1 = Nxt.convert((String)paramJSONObject.get("senderPublicKey"));
    long l1 = new BigInteger((String)paramJSONObject.get("recipient")).longValue();
    int j = ((Long)paramJSONObject.get("amount")).intValue();
    int k = ((Long)paramJSONObject.get("fee")).intValue();
    long l2 = new BigInteger((String)paramJSONObject.get("referencedTransaction")).longValue();
    byte[] arrayOfByte2 = Nxt.convert((String)paramJSONObject.get("signature"));
    Transaction localTransaction = new Transaction(b1, b2, i, s, arrayOfByte1, l1, j, k, l2, arrayOfByte2);
    JSONObject localJSONObject = (JSONObject)paramJSONObject.get("attachment");
    String str1;
    String str2;
    switch (b1)
    {
    case 1: 
      switch (b2)
      {
      case 1: 
        str1 = (String)localJSONObject.get("alias");
        str2 = (String)localJSONObject.get("uri");
        localTransaction.attachment = new Nxt.Transaction.MessagingAliasAssignmentAttachment(str1.trim(), str2.trim());
      }
      break;
    case 2: 
      int m;
      long l3;
      long l4;
      switch (b2)
      {
      case 0: 
        str1 = (String)localJSONObject.get("name");
        str2 = (String)localJSONObject.get("description");
        m = ((Long)localJSONObject.get("quantity")).intValue();
        localTransaction.attachment = new Nxt.Transaction.ColoredCoinsAssetIssuanceAttachment(str1.trim(), str2.trim(), m);
        break;
      case 1: 
        l3 = new BigInteger((String)localJSONObject.get("asset")).longValue();
        m = ((Long)localJSONObject.get("quantity")).intValue();
        localTransaction.attachment = new Nxt.Transaction.ColoredCoinsAssetTransferAttachment(l3, m);
        break;
      case 2: 
        l3 = new BigInteger((String)localJSONObject.get("asset")).longValue();
        m = ((Long)localJSONObject.get("quantity")).intValue();
        l4 = ((Long)localJSONObject.get("price")).longValue();
        localTransaction.attachment = new Nxt.Transaction.ColoredCoinsAskOrderPlacementAttachment(l3, m, l4);
        break;
      case 3: 
        l3 = new BigInteger((String)localJSONObject.get("asset")).longValue();
        m = ((Long)localJSONObject.get("quantity")).intValue();
        l4 = ((Long)localJSONObject.get("price")).longValue();
        localTransaction.attachment = new Nxt.Transaction.ColoredCoinsBidOrderPlacementAttachment(l3, m, l4);
        break;
      case 4: 
        l3 = new BigInteger((String)localJSONObject.get("order")).longValue();
        localTransaction.attachment = new Nxt.Transaction.ColoredCoinsAskOrderCancellationAttachment(l3);
        break;
      case 5: 
        l3 = new BigInteger((String)localJSONObject.get("order")).longValue();
        localTransaction.attachment = new Nxt.Transaction.ColoredCoinsBidOrderCancellationAttachment(l3);
      }
      break;
    }
    return localTransaction;
  }
  
  static void loadTransactions(String paramString)
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
        Nxt.transactionCounter.set(localObjectInputStream.readInt());
        Nxt.transactions.clear();
        Nxt.transactions.putAll((HashMap)localObjectInputStream.readObject());
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
  
  static void processTransactions(JSONObject paramJSONObject, String paramString)
  {
    JSONArray localJSONArray1 = (JSONArray)paramJSONObject.get(paramString);
    JSONArray localJSONArray2 = new JSONArray();
    Object localObject1 = localJSONArray1.iterator();
    while (((Iterator)localObject1).hasNext())
    {
      Object localObject2 = ((Iterator)localObject1).next();
      Transaction localTransaction = getTransaction((JSONObject)localObject2);
      try
      {
        int i = Nxt.getEpochTime(System.currentTimeMillis());
        if ((localTransaction.timestamp > i + 15) || (localTransaction.deadline < 1) || (localTransaction.timestamp + localTransaction.deadline * 60 < i) || (localTransaction.fee <= 0) || (localTransaction.validateAttachment()))
        {
          long l1;
          int j;
          synchronized (Nxt.blocksAndTransactionsLock)
          {
            long l2 = localTransaction.getId();
            if ((Nxt.transactions.get(Long.valueOf(l2)) == null) && (Nxt.unconfirmedTransactions.get(Long.valueOf(l2)) == null) && (Nxt.doubleSpendingTransactions.get(Long.valueOf(l2)) == null) && (!localTransaction.verify())) {
              continue;
            }
            l1 = Nxt.Account.getId(localTransaction.senderPublicKey);
            localObject3 = (Nxt.Account)Nxt.accounts.get(Long.valueOf(l1));
            if (localObject3 == null)
            {
              j = 1;
            }
            else
            {
              int k = localTransaction.amount + localTransaction.fee;
              synchronized (localObject3)
              {
                if (((Nxt.Account)localObject3).getUnconfirmedBalance() < k * 100L)
                {
                  j = 1;
                }
                else
                {
                  j = 0;
                  ((Nxt.Account)localObject3).addToUnconfirmedBalance(-k * 100L);
                  if (localTransaction.type == 2)
                  {
                    Object localObject4;
                    if (localTransaction.subtype == 1)
                    {
                      localObject4 = (Nxt.Transaction.ColoredCoinsAssetTransferAttachment)localTransaction.attachment;
                      if ((((Nxt.Account)localObject3).unconfirmedAssetBalances.get(Long.valueOf(((Nxt.Transaction.ColoredCoinsAssetTransferAttachment)localObject4).asset)) == null) || (((Integer)((Nxt.Account)localObject3).unconfirmedAssetBalances.get(Long.valueOf(((Nxt.Transaction.ColoredCoinsAssetTransferAttachment)localObject4).asset))).intValue() < ((Nxt.Transaction.ColoredCoinsAssetTransferAttachment)localObject4).quantity))
                      {
                        j = 1;
                        ((Nxt.Account)localObject3).addToUnconfirmedBalance(k * 100L);
                      }
                      else
                      {
                        ((Nxt.Account)localObject3).unconfirmedAssetBalances.put(Long.valueOf(((Nxt.Transaction.ColoredCoinsAssetTransferAttachment)localObject4).asset), Integer.valueOf(((Integer)((Nxt.Account)localObject3).unconfirmedAssetBalances.get(Long.valueOf(((Nxt.Transaction.ColoredCoinsAssetTransferAttachment)localObject4).asset))).intValue() - ((Nxt.Transaction.ColoredCoinsAssetTransferAttachment)localObject4).quantity));
                      }
                    }
                    else if (localTransaction.subtype == 2)
                    {
                      localObject4 = (Nxt.Transaction.ColoredCoinsAskOrderPlacementAttachment)localTransaction.attachment;
                      if ((((Nxt.Account)localObject3).unconfirmedAssetBalances.get(Long.valueOf(((Nxt.Transaction.ColoredCoinsAskOrderPlacementAttachment)localObject4).asset)) == null) || (((Integer)((Nxt.Account)localObject3).unconfirmedAssetBalances.get(Long.valueOf(((Nxt.Transaction.ColoredCoinsAskOrderPlacementAttachment)localObject4).asset))).intValue() < ((Nxt.Transaction.ColoredCoinsAskOrderPlacementAttachment)localObject4).quantity))
                      {
                        j = 1;
                        ((Nxt.Account)localObject3).addToUnconfirmedBalance(k * 100L);
                      }
                      else
                      {
                        ((Nxt.Account)localObject3).unconfirmedAssetBalances.put(Long.valueOf(((Nxt.Transaction.ColoredCoinsAskOrderPlacementAttachment)localObject4).asset), Integer.valueOf(((Integer)((Nxt.Account)localObject3).unconfirmedAssetBalances.get(Long.valueOf(((Nxt.Transaction.ColoredCoinsAskOrderPlacementAttachment)localObject4).asset))).intValue() - ((Nxt.Transaction.ColoredCoinsAskOrderPlacementAttachment)localObject4).quantity));
                      }
                    }
                    else if (localTransaction.subtype == 3)
                    {
                      localObject4 = (Nxt.Transaction.ColoredCoinsBidOrderPlacementAttachment)localTransaction.attachment;
                      if (((Nxt.Account)localObject3).getUnconfirmedBalance() < ((Nxt.Transaction.ColoredCoinsBidOrderPlacementAttachment)localObject4).quantity * ((Nxt.Transaction.ColoredCoinsBidOrderPlacementAttachment)localObject4).price)
                      {
                        j = 1;
                        ((Nxt.Account)localObject3).addToUnconfirmedBalance(k * 100L);
                      }
                      else
                      {
                        ((Nxt.Account)localObject3).addToUnconfirmedBalance(-((Nxt.Transaction.ColoredCoinsBidOrderPlacementAttachment)localObject4).quantity * ((Nxt.Transaction.ColoredCoinsBidOrderPlacementAttachment)localObject4).price);
                      }
                    }
                  }
                }
              }
            }
            localTransaction.index = Nxt.transactionCounter.incrementAndGet();
            if (j != 0)
            {
              Nxt.doubleSpendingTransactions.put(Long.valueOf(localTransaction.getId()), localTransaction);
            }
            else
            {
              Nxt.unconfirmedTransactions.put(Long.valueOf(localTransaction.getId()), localTransaction);
              if (paramString.equals("transactions")) {
                localJSONArray2.add(localObject2);
              }
            }
          }
          ??? = new JSONObject();
          ((JSONObject)???).put("response", "processNewData");
          JSONArray localJSONArray3 = new JSONArray();
          JSONObject localJSONObject = new JSONObject();
          localJSONObject.put("index", Integer.valueOf(localTransaction.index));
          localJSONObject.put("timestamp", Integer.valueOf(localTransaction.timestamp));
          localJSONObject.put("deadline", Short.valueOf(localTransaction.deadline));
          localJSONObject.put("recipient", Nxt.convert(localTransaction.recipient));
          localJSONObject.put("amount", Integer.valueOf(localTransaction.amount));
          localJSONObject.put("fee", Integer.valueOf(localTransaction.fee));
          localJSONObject.put("sender", Nxt.convert(l1));
          localJSONObject.put("id", Nxt.convert(localTransaction.getId()));
          localJSONArray3.add(localJSONObject);
          if (j != 0) {
            ((JSONObject)???).put("addedDoubleSpendingTransactions", localJSONArray3);
          } else {
            ((JSONObject)???).put("addedUnconfirmedTransactions", localJSONArray3);
          }
          Object localObject3 = Nxt.users.values().iterator();
          while (((Iterator)localObject3).hasNext())
          {
            Nxt.User localUser = (Nxt.User)((Iterator)localObject3).next();
            localUser.send((JSONObject)???);
          }
        }
      }
      catch (Exception localException)
      {
        Nxt.logMessage("15: " + localException.toString());
      }
    }
    if (localJSONArray2.size() > 0)
    {
      localObject1 = new JSONObject();
      ((JSONObject)localObject1).put("requestType", "processTransactions");
      ((JSONObject)localObject1).put("transactions", localJSONArray2);
      Nxt.Peer.sendToAllPeers((JSONObject)localObject1);
    }
  }
  
  static void saveTransactions(String paramString)
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
        localObjectOutputStream.writeInt(Nxt.transactionCounter.get());
        localObjectOutputStream.writeObject(new HashMap(Nxt.transactions));
        localObjectOutputStream.close();
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
  
  void sign(String paramString)
  {
    this.signature = Nxt.Crypto.sign(getBytes(), paramString);
    try
    {
      while (!verify())
      {
        this.timestamp += 1;
        this.signature = new byte[64];
        this.signature = Nxt.Crypto.sign(getBytes(), paramString);
      }
    }
    catch (Exception localException)
    {
      Nxt.logMessage("16: " + localException.toString());
    }
  }
  
  boolean validateAttachment()
  {
    if (this.fee > 1000000000L) {
      return false;
    }
    switch (this.type)
    {
    case 0: 
      switch (this.subtype)
      {
      case 0: 
        return (this.amount > 0) && (this.amount <= 1000000000L);
      }
      return false;
    case 1: 
      switch (this.subtype)
      {
      case 1: 
        if (Nxt.Block.getLastBlock().height < 22000) {
          return false;
        }
        try
        {
          Nxt.Transaction.MessagingAliasAssignmentAttachment localMessagingAliasAssignmentAttachment = (Nxt.Transaction.MessagingAliasAssignmentAttachment)this.attachment;
          if ((this.recipient != 1739068987193023818L) || (this.amount != 0) || (localMessagingAliasAssignmentAttachment.alias.length() == 0) || (localMessagingAliasAssignmentAttachment.alias.length() > 100) || (localMessagingAliasAssignmentAttachment.uri.length() > 1000)) {
            return false;
          }
          String str = localMessagingAliasAssignmentAttachment.alias.toLowerCase();
          for (int i = 0; i < str.length(); i++) {
            if ("0123456789abcdefghijklmnopqrstuvwxyz".indexOf(str.charAt(i)) < 0) {
              return false;
            }
          }
          Nxt.Alias localAlias = (Nxt.Alias)Nxt.aliases.get(str);
          return (localAlias == null) || (localAlias.account.id == Nxt.Account.getId(this.senderPublicKey));
        }
        catch (Exception localException)
        {
          return false;
        }
      }
      return false;
    }
    return false;
  }
  
  boolean verify()
    throws Exception
  {
    Nxt.Account localAccount = (Nxt.Account)Nxt.accounts.get(Long.valueOf(Nxt.Account.getId(this.senderPublicKey)));
    if (localAccount == null) {
      return false;
    }
    byte[] arrayOfByte = getBytes();
    for (int i = 64; i < 128; i++) {
      arrayOfByte[i] = 0;
    }
    return (Nxt.Crypto.verify(this.signature, arrayOfByte, this.senderPublicKey)) && (localAccount.setOrVerify(this.senderPublicKey));
  }
  
  public static byte[] calculateTransactionsChecksum()
    throws Exception
  {
    synchronized (Nxt.blocksAndTransactionsLock)
    {
      TreeSet localTreeSet = new TreeSet(new Nxt.Transaction.1());
      localTreeSet.addAll(Nxt.transactions.values());
      MessageDigest localMessageDigest = MessageDigest.getInstance("SHA-256");
      Iterator localIterator = localTreeSet.iterator();
      while (localIterator.hasNext())
      {
        Transaction localTransaction = (Transaction)localIterator.next();
        localMessageDigest.update(localTransaction.getBytes());
      }
      return localMessageDigest.digest();
    }
  }
