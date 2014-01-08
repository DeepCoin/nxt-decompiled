import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

class Nxt$Peer
  implements Comparable<Peer>
{
  static final int STATE_NONCONNECTED = 0;
  static final int STATE_CONNECTED = 1;
  static final int STATE_DISCONNECTED = 2;
  final int index;
  String platform;
  String announcedAddress;
  boolean shareAddress;
  String hallmark;
  long accountId;
  int weight;
  int date;
  long adjustedWeight;
  String application;
  String version;
  long blacklistingTime;
  int state;
  long downloadedVolume;
  long uploadedVolume;
  
  Nxt$Peer(String paramString, int paramInt)
  {
    this.announcedAddress = paramString;
    this.index = paramInt;
  }
  
  static Peer addPeer(String paramString1, String paramString2)
  {
    try
    {
      new URL("http://" + paramString1);
    }
    catch (Exception localException1)
    {
      return null;
    }
    try
    {
      new URL("http://" + paramString2);
    }
    catch (Exception localException2)
    {
      paramString2 = "";
    }
    if ((paramString1.equals("localhost")) || (paramString1.equals("127.0.0.1")) || (paramString1.equals("0:0:0:0:0:0:0:1"))) {
      return null;
    }
    if ((Nxt.myAddress != null) && (Nxt.myAddress.length() > 0) && (Nxt.myAddress.equals(paramString2))) {
      return null;
    }
    Peer localPeer = (Peer)Nxt.peers.get(paramString2.length() > 0 ? paramString2 : paramString1);
    if (localPeer == null)
    {
      localPeer = new Peer(paramString2, Nxt.peerCounter.incrementAndGet());
      Nxt.peers.put(paramString2.length() > 0 ? paramString2 : paramString1, localPeer);
    }
    return localPeer;
  }
  
  boolean analyzeHallmark(String paramString1, String paramString2)
  {
    if (paramString2 == null) {
      return true;
    }
    try
    {
      byte[] arrayOfByte1 = Nxt.convert(paramString2);
      ByteBuffer localByteBuffer = ByteBuffer.wrap(arrayOfByte1);
      localByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
      byte[] arrayOfByte2 = new byte[32];
      localByteBuffer.get(arrayOfByte2);
      int i = localByteBuffer.getShort();
      byte[] arrayOfByte3 = new byte[i];
      localByteBuffer.get(arrayOfByte3);
      String str = new String(arrayOfByte3, "UTF-8");
      if ((str.length() > 100) || (!str.equals(paramString1))) {
        return false;
      }
      int j = localByteBuffer.getInt();
      if ((j <= 0) || (j > 1000000000L)) {
        return false;
      }
      int k = localByteBuffer.getInt();
      localByteBuffer.get();
      byte[] arrayOfByte4 = new byte[64];
      localByteBuffer.get(arrayOfByte4);
      byte[] arrayOfByte5 = new byte[arrayOfByte1.length - 64];
      System.arraycopy(arrayOfByte1, 0, arrayOfByte5, 0, arrayOfByte5.length);
      if (Nxt.Crypto.verify(arrayOfByte4, arrayOfByte5, arrayOfByte2))
      {
        this.hallmark = paramString2;
        long l1 = Nxt.Account.getId(arrayOfByte2);
        LinkedList localLinkedList = new LinkedList();
        int m = 0;
        this.accountId = l1;
        this.weight = j;
        this.date = k;
        Iterator localIterator1 = Nxt.peers.values().iterator();
        while (localIterator1.hasNext())
        {
          Peer localPeer1 = (Peer)localIterator1.next();
          if (localPeer1.accountId == l1)
          {
            localLinkedList.add(localPeer1);
            if (localPeer1.date > m) {
              m = localPeer1.date;
            }
          }
        }
        long l2 = 0L;
        Iterator localIterator2 = localLinkedList.iterator();
        Peer localPeer2;
        while (localIterator2.hasNext())
        {
          localPeer2 = (Peer)localIterator2.next();
          if (localPeer2.date == m)
          {
            l2 += localPeer2.weight;
          }
          else
          {
            localPeer2.adjustedWeight = 0L;
            localPeer2.updateWeight();
          }
        }
        localIterator2 = localLinkedList.iterator();
        while (localIterator2.hasNext())
        {
          localPeer2 = (Peer)localIterator2.next();
          localPeer2.adjustedWeight = (1000000000L * localPeer2.weight / l2);
          localPeer2.updateWeight();
        }
        return true;
      }
    }
    catch (Exception localException) {}
    return false;
  }
  
  void blacklist()
  {
    this.blacklistingTime = System.currentTimeMillis();
    JSONObject localJSONObject1 = new JSONObject();
    localJSONObject1.put("response", "processNewData");
    JSONArray localJSONArray1 = new JSONArray();
    JSONObject localJSONObject2 = new JSONObject();
    localJSONObject2.put("index", Integer.valueOf(this.index));
    localJSONArray1.add(localJSONObject2);
    localJSONObject1.put("removedKnownPeers", localJSONArray1);
    JSONArray localJSONArray2 = new JSONArray();
    JSONObject localJSONObject3 = new JSONObject();
    localJSONObject3.put("index", Integer.valueOf(this.index));
    localJSONObject3.put("announcedAddress", this.announcedAddress.length() > 30 ? this.announcedAddress.substring(0, 30) + "..." : this.announcedAddress);
    Iterator localIterator = Nxt.wellKnownPeers.iterator();
    Object localObject;
    while (localIterator.hasNext())
    {
      localObject = (String)localIterator.next();
      if (this.announcedAddress.equals(localObject))
      {
        localJSONObject3.put("wellKnown", Boolean.valueOf(true));
        break;
      }
    }
    localJSONArray2.add(localJSONObject3);
    localJSONObject1.put("addedBlacklistedPeers", localJSONArray2);
    localIterator = Nxt.users.values().iterator();
    while (localIterator.hasNext())
    {
      localObject = (Nxt.User)localIterator.next();
      ((Nxt.User)localObject).send(localJSONObject1);
    }
  }
  
  public int compareTo(Peer paramPeer)
  {
    long l1 = getWeight();
    long l2 = paramPeer.getWeight();
    if (l1 > l2) {
      return -1;
    }
    if (l1 < l2) {
      return 1;
    }
    return this.index - paramPeer.index;
  }
  
  void connect()
  {
    JSONObject localJSONObject1 = new JSONObject();
    localJSONObject1.put("requestType", "getInfo");
    if ((Nxt.myAddress != null) && (Nxt.myAddress.length() > 0)) {
      localJSONObject1.put("announcedAddress", Nxt.myAddress);
    }
    if ((Nxt.myHallmark != null) && (Nxt.myHallmark.length() > 0)) {
      localJSONObject1.put("hallmark", Nxt.myHallmark);
    }
    localJSONObject1.put("application", "NRS");
    localJSONObject1.put("version", "0.5.2");
    localJSONObject1.put("platform", Nxt.myPlatform);
    localJSONObject1.put("scheme", Nxt.myScheme);
    localJSONObject1.put("port", Integer.valueOf(Nxt.myPort));
    localJSONObject1.put("shareAddress", Boolean.valueOf(Nxt.shareMyAddress));
    JSONObject localJSONObject2 = send(localJSONObject1);
    if (localJSONObject2 != null)
    {
      this.application = ((String)localJSONObject2.get("application"));
      this.version = ((String)localJSONObject2.get("version"));
      this.platform = ((String)localJSONObject2.get("platform"));
      try
      {
        this.shareAddress = Boolean.parseBoolean((String)localJSONObject2.get("shareAddress"));
      }
      catch (Exception localException)
      {
        this.shareAddress = true;
      }
      if (analyzeHallmark(this.announcedAddress, (String)localJSONObject2.get("hallmark"))) {
        setState(1);
      }
    }
  }
  
  void deactivate()
  {
    if (this.state == 1) {
      disconnect();
    }
    setState(0);
    JSONObject localJSONObject1 = new JSONObject();
    localJSONObject1.put("response", "processNewData");
    JSONArray localJSONArray = new JSONArray();
    JSONObject localJSONObject2 = new JSONObject();
    localJSONObject2.put("index", Integer.valueOf(this.index));
    localJSONArray.add(localJSONObject2);
    localJSONObject1.put("removedActivePeers", localJSONArray);
    Object localObject2;
    if (this.announcedAddress.length() > 0)
    {
      localObject1 = new JSONArray();
      localObject2 = new JSONObject();
      ((JSONObject)localObject2).put("index", Integer.valueOf(this.index));
      ((JSONObject)localObject2).put("announcedAddress", this.announcedAddress.length() > 30 ? this.announcedAddress.substring(0, 30) + "..." : this.announcedAddress);
      Iterator localIterator = Nxt.wellKnownPeers.iterator();
      while (localIterator.hasNext())
      {
        String str = (String)localIterator.next();
        if (this.announcedAddress.equals(str))
        {
          ((JSONObject)localObject2).put("wellKnown", Boolean.valueOf(true));
          break;
        }
      }
      ((JSONArray)localObject1).add(localObject2);
      localJSONObject1.put("addedKnownPeers", localObject1);
    }
    Object localObject1 = Nxt.users.values().iterator();
    while (((Iterator)localObject1).hasNext())
    {
      localObject2 = (Nxt.User)((Iterator)localObject1).next();
      ((Nxt.User)localObject2).send(localJSONObject1);
    }
  }
  
  void disconnect()
  {
    setState(2);
  }
  
  static Peer getAnyPeer(int paramInt, boolean paramBoolean)
  {
    ArrayList localArrayList = new ArrayList();
    Iterator localIterator1 = Nxt.peers.values().iterator();
    while (localIterator1.hasNext())
    {
      Peer localPeer1 = (Peer)localIterator1.next();
      if ((localPeer1.blacklistingTime <= 0L) && (localPeer1.state == paramInt) && (localPeer1.announcedAddress.length() > 0) && ((!paramBoolean) || (!Nxt.enableHallmarkProtection) || (localPeer1.getWeight() >= Nxt.pullThreshold))) {
        localArrayList.add(localPeer1);
      }
    }
    if (localArrayList.size() > 0)
    {
      long l1 = 0L;
      Iterator localIterator2 = localArrayList.iterator();
      while (localIterator2.hasNext())
      {
        Peer localPeer2 = (Peer)localIterator2.next();
        long l3 = localPeer2.getWeight();
        if (l3 == 0L) {
          l3 = 1L;
        }
        l1 += l3;
      }
      long l2 = ThreadLocalRandom.current().nextLong(l1);
      Iterator localIterator3 = localArrayList.iterator();
      while (localIterator3.hasNext())
      {
        Peer localPeer3 = (Peer)localIterator3.next();
        long l4 = localPeer3.getWeight();
        if (l4 == 0L) {
          l4 = 1L;
        }
        if (l2 -= l4 < 0L) {
          return localPeer3;
        }
      }
    }
    return null;
  }
  
  static int getNumberOfConnectedPublicPeers()
  {
    int i = 0;
    Iterator localIterator = Nxt.peers.values().iterator();
    while (localIterator.hasNext())
    {
      Peer localPeer = (Peer)localIterator.next();
      if ((localPeer.state == 1) && (localPeer.announcedAddress.length() > 0)) {
        i++;
      }
    }
    return i;
  }
  
  int getWeight()
  {
    if (this.accountId == 0L) {
      return 0;
    }
    Nxt.Account localAccount = (Nxt.Account)Nxt.accounts.get(Long.valueOf(this.accountId));
    if (localAccount == null) {
      return 0;
    }
    return (int)(this.adjustedWeight * (localAccount.getBalance() / 100L) / 1000000000L);
  }
  
  String getSoftware()
  {
    StringBuilder localStringBuilder = new StringBuilder();
    localStringBuilder.append(this.application == null ? "?" : this.application.substring(0, Math.min(this.application.length(), 10)));
    localStringBuilder.append(" (");
    localStringBuilder.append(this.version == null ? "?" : this.version.substring(0, Math.min(this.version.length(), 10)));
    localStringBuilder.append(")").append(" @ ");
    localStringBuilder.append(this.platform == null ? "?" : this.platform.substring(0, Math.min(this.platform.length(), 10)));
    return localStringBuilder.toString();
  }
  
  void removeBlacklistedStatus()
  {
    setState(0);
    this.blacklistingTime = 0L;
    JSONObject localJSONObject1 = new JSONObject();
    localJSONObject1.put("response", "processNewData");
    JSONArray localJSONArray1 = new JSONArray();
    JSONObject localJSONObject2 = new JSONObject();
    localJSONObject2.put("index", Integer.valueOf(this.index));
    localJSONArray1.add(localJSONObject2);
    localJSONObject1.put("removedBlacklistedPeers", localJSONArray1);
    JSONArray localJSONArray2 = new JSONArray();
    JSONObject localJSONObject3 = new JSONObject();
    localJSONObject3.put("index", Integer.valueOf(this.index));
    localJSONObject3.put("announcedAddress", this.announcedAddress.length() > 30 ? this.announcedAddress.substring(0, 30) + "..." : this.announcedAddress);
    Iterator localIterator = Nxt.wellKnownPeers.iterator();
    Object localObject;
    while (localIterator.hasNext())
    {
      localObject = (String)localIterator.next();
      if (this.announcedAddress.equals(localObject))
      {
        localJSONObject3.put("wellKnown", Boolean.valueOf(true));
        break;
      }
    }
    localJSONArray2.add(localJSONObject3);
    localJSONObject1.put("addedKnownPeers", localJSONArray2);
    localIterator = Nxt.users.values().iterator();
    while (localIterator.hasNext())
    {
      localObject = (Nxt.User)localIterator.next();
      ((Nxt.User)localObject).send(localJSONObject1);
    }
  }
  
  void removePeer()
  {
    Nxt.peers.values().remove(this);
    JSONObject localJSONObject1 = new JSONObject();
    localJSONObject1.put("response", "processNewData");
    JSONArray localJSONArray = new JSONArray();
    JSONObject localJSONObject2 = new JSONObject();
    localJSONObject2.put("index", Integer.valueOf(this.index));
    localJSONArray.add(localJSONObject2);
    localJSONObject1.put("removedKnownPeers", localJSONArray);
    Iterator localIterator = Nxt.users.values().iterator();
    while (localIterator.hasNext())
    {
      Nxt.User localUser = (Nxt.User)localIterator.next();
      localUser.send(localJSONObject1);
    }
  }
  
  static void sendToAllPeers(JSONObject paramJSONObject)
  {
    Iterator localIterator = Nxt.peers.values().iterator();
    while (localIterator.hasNext())
    {
      Peer localPeer = (Peer)localIterator.next();
      if ((!Nxt.enableHallmarkProtection) || (localPeer.getWeight() >= Nxt.pushThreshold)) {
        if ((localPeer.blacklistingTime == 0L) && (localPeer.state == 1) && (localPeer.announcedAddress.length() > 0)) {
          localPeer.send(paramJSONObject);
        }
      }
    }
  }
  
  JSONObject send(JSONObject paramJSONObject)
  {
    String str = null;
    int i = 0;
    HttpURLConnection localHttpURLConnection = null;
    JSONObject localJSONObject;
    try
    {
      if (Nxt.communicationLoggingMask != 0) {
        str = "\"" + this.announcedAddress + "\": " + paramJSONObject.toString();
      }
      paramJSONObject.put("protocol", Integer.valueOf(1));
      URL localURL = new URL("http://" + this.announcedAddress + (new URL("http://" + this.announcedAddress).getPort() < 0 ? ":7874" : "") + "/nxt");
      localHttpURLConnection = (HttpURLConnection)localURL.openConnection();
      localHttpURLConnection.setRequestMethod("POST");
      localHttpURLConnection.setDoOutput(true);
      localHttpURLConnection.setConnectTimeout(Nxt.connectTimeout);
      localHttpURLConnection.setReadTimeout(Nxt.readTimeout);
      Nxt.CountingOutputStream localCountingOutputStream = new Nxt.CountingOutputStream(localHttpURLConnection.getOutputStream());
      Object localObject1 = new BufferedWriter(new OutputStreamWriter(localCountingOutputStream, "UTF-8"));
      Object localObject2 = null;
      try
      {
        paramJSONObject.writeJSONString((Writer)localObject1);
      }
      catch (Throwable localThrowable2)
      {
        localObject2 = localThrowable2;
        throw localThrowable2;
      }
      finally
      {
        if (localObject1 != null) {
          if (localObject2 != null) {
            try
            {
              ((Writer)localObject1).close();
            }
            catch (Throwable localThrowable5)
            {
              ((Throwable)localObject2).addSuppressed(localThrowable5);
            }
          } else {
            ((Writer)localObject1).close();
          }
        }
      }
      updateUploadedVolume(localCountingOutputStream.getCount());
      if (localHttpURLConnection.getResponseCode() == 200)
      {
        if ((Nxt.communicationLoggingMask & 0x4) != 0)
        {
          localObject1 = new ByteArrayOutputStream();
          localObject2 = new byte[65536];
          Object localObject5 = localHttpURLConnection.getInputStream();
          Object localObject6 = null;
          try
          {
            int j;
            while ((j = ((InputStream)localObject5).read((byte[])localObject2)) > 0) {
              ((ByteArrayOutputStream)localObject1).write((byte[])localObject2, 0, j);
            }
          }
          catch (Throwable localThrowable7)
          {
            localObject6 = localThrowable7;
            throw localThrowable7;
          }
          finally
          {
            if (localObject5 != null) {
              if (localObject6 != null) {
                try
                {
                  ((InputStream)localObject5).close();
                }
                catch (Throwable localThrowable8)
                {
                  localObject6.addSuppressed(localThrowable8);
                }
              } else {
                ((InputStream)localObject5).close();
              }
            }
          }
          localObject5 = ((ByteArrayOutputStream)localObject1).toString("UTF-8");
          str = str + " >>> " + (String)localObject5;
          i = 1;
          updateDownloadedVolume(((String)localObject5).getBytes("UTF-8").length);
          localJSONObject = (JSONObject)JSONValue.parse((String)localObject5);
        }
        else
        {
          localObject1 = new Nxt.CountingInputStream(localHttpURLConnection.getInputStream());
          localObject2 = new BufferedReader(new InputStreamReader((InputStream)localObject1, "UTF-8"));
          Object localObject3 = null;
          try
          {
            localJSONObject = (JSONObject)JSONValue.parse((Reader)localObject2);
          }
          catch (Throwable localThrowable4)
          {
            localObject3 = localThrowable4;
            throw localThrowable4;
          }
          finally
          {
            if (localObject2 != null) {
              if (localObject3 != null) {
                try
                {
                  ((Reader)localObject2).close();
                }
                catch (Throwable localThrowable9)
                {
                  localObject3.addSuppressed(localThrowable9);
                }
              } else {
                ((Reader)localObject2).close();
              }
            }
          }
          updateDownloadedVolume(((Nxt.CountingInputStream)localObject1).getCount());
        }
      }
      else
      {
        if ((Nxt.communicationLoggingMask & 0x2) != 0)
        {
          str = str + " >>> Peer responded with HTTP " + localHttpURLConnection.getResponseCode() + " code!";
          i = 1;
        }
        disconnect();
        localJSONObject = null;
      }
    }
    catch (Exception localException)
    {
      if ((Nxt.communicationLoggingMask & 0x1) != 0)
      {
        str = str + " >>> " + localException.toString();
        i = 1;
      }
      if (this.state == 0) {
        blacklist();
      } else {
        disconnect();
      }
      localJSONObject = null;
    }
    if (i != 0) {
      Nxt.logMessage(str + "\n");
    }
    if (localHttpURLConnection != null) {
      localHttpURLConnection.disconnect();
    }
    return localJSONObject;
  }
  
  void setState(int paramInt)
  {
    JSONObject localJSONObject1;
    JSONArray localJSONArray;
    JSONObject localJSONObject2;
    Iterator localIterator;
    Object localObject;
    if ((this.state == 0) && (paramInt != 0))
    {
      localJSONObject1 = new JSONObject();
      localJSONObject1.put("response", "processNewData");
      if (this.announcedAddress.length() > 0)
      {
        localJSONArray = new JSONArray();
        localJSONObject2 = new JSONObject();
        localJSONObject2.put("index", Integer.valueOf(this.index));
        localJSONArray.add(localJSONObject2);
        localJSONObject1.put("removedKnownPeers", localJSONArray);
      }
      localJSONArray = new JSONArray();
      localJSONObject2 = new JSONObject();
      localJSONObject2.put("index", Integer.valueOf(this.index));
      if (paramInt == 2) {
        localJSONObject2.put("disconnected", Boolean.valueOf(true));
      }
      localIterator = Nxt.peers.entrySet().iterator();
      while (localIterator.hasNext())
      {
        localObject = (Map.Entry)localIterator.next();
        if (((Map.Entry)localObject).getValue() == this)
        {
          localJSONObject2.put("address", ((String)((Map.Entry)localObject).getKey()).length() > 30 ? ((String)((Map.Entry)localObject).getKey()).substring(0, 30) + "..." : (String)((Map.Entry)localObject).getKey());
          break;
        }
      }
      localJSONObject2.put("announcedAddress", this.announcedAddress.length() > 30 ? this.announcedAddress.substring(0, 30) + "..." : this.announcedAddress);
      localJSONObject2.put("weight", Integer.valueOf(getWeight()));
      localJSONObject2.put("downloaded", Long.valueOf(this.downloadedVolume));
      localJSONObject2.put("uploaded", Long.valueOf(this.uploadedVolume));
      localJSONObject2.put("software", getSoftware());
      localIterator = Nxt.wellKnownPeers.iterator();
      while (localIterator.hasNext())
      {
        localObject = (String)localIterator.next();
        if (this.announcedAddress.equals(localObject))
        {
          localJSONObject2.put("wellKnown", Boolean.valueOf(true));
          break;
        }
      }
      localJSONArray.add(localJSONObject2);
      localJSONObject1.put("addedActivePeers", localJSONArray);
      localIterator = Nxt.users.values().iterator();
      while (localIterator.hasNext())
      {
        localObject = (Nxt.User)localIterator.next();
        ((Nxt.User)localObject).send(localJSONObject1);
      }
    }
    else if ((this.state != 0) && (paramInt != 0))
    {
      localJSONObject1 = new JSONObject();
      localJSONObject1.put("response", "processNewData");
      localJSONArray = new JSONArray();
      localJSONObject2 = new JSONObject();
      localJSONObject2.put("index", Integer.valueOf(this.index));
      localJSONObject2.put(paramInt == 1 ? "connected" : "disconnected", Boolean.valueOf(true));
      localJSONArray.add(localJSONObject2);
      localJSONObject1.put("changedActivePeers", localJSONArray);
      localIterator = Nxt.users.values().iterator();
      while (localIterator.hasNext())
      {
        localObject = (Nxt.User)localIterator.next();
        ((Nxt.User)localObject).send(localJSONObject1);
      }
    }
    this.state = paramInt;
  }
  
  void updateDownloadedVolume(long paramLong)
  {
    this.downloadedVolume += paramLong;
    JSONObject localJSONObject1 = new JSONObject();
    localJSONObject1.put("response", "processNewData");
    JSONArray localJSONArray = new JSONArray();
    JSONObject localJSONObject2 = new JSONObject();
    localJSONObject2.put("index", Integer.valueOf(this.index));
    localJSONObject2.put("downloaded", Long.valueOf(this.downloadedVolume));
    localJSONArray.add(localJSONObject2);
    localJSONObject1.put("changedActivePeers", localJSONArray);
    Iterator localIterator = Nxt.users.values().iterator();
    while (localIterator.hasNext())
    {
      Nxt.User localUser = (Nxt.User)localIterator.next();
      localUser.send(localJSONObject1);
    }
  }
  
  void updateUploadedVolume(long paramLong)
  {
    this.uploadedVolume += paramLong;
    JSONObject localJSONObject1 = new JSONObject();
    localJSONObject1.put("response", "processNewData");
    JSONArray localJSONArray = new JSONArray();
    JSONObject localJSONObject2 = new JSONObject();
    localJSONObject2.put("index", Integer.valueOf(this.index));
    localJSONObject2.put("uploaded", Long.valueOf(this.uploadedVolume));
    localJSONArray.add(localJSONObject2);
    localJSONObject1.put("changedActivePeers", localJSONArray);
    Iterator localIterator = Nxt.users.values().iterator();
    while (localIterator.hasNext())
    {
      Nxt.User localUser = (Nxt.User)localIterator.next();
      localUser.send(localJSONObject1);
    }
  }
  
  void updateWeight()
  {
    JSONObject localJSONObject1 = new JSONObject();
    localJSONObject1.put("response", "processNewData");
    JSONArray localJSONArray = new JSONArray();
    JSONObject localJSONObject2 = new JSONObject();
    localJSONObject2.put("index", Integer.valueOf(this.index));
    localJSONObject2.put("weight", Integer.valueOf(getWeight()));
    localJSONArray.add(localJSONObject2);
    localJSONObject1.put("changedActivePeers", localJSONArray);
    Iterator localIterator = Nxt.users.values().iterator();
    while (localIterator.hasNext())
    {
      Nxt.User localUser = (Nxt.User)localIterator.next();
      localUser.send(localJSONObject1);
    }
  }
