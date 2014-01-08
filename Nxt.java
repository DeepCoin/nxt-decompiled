import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;
import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.servlet.AsyncContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class Nxt
  extends HttpServlet
{
  static final String VERSION = "0.5.3";
  static final long GENESIS_BLOCK_ID = 2680262203532249785L;
  static final long CREATOR_ID = 1739068987193023818L;
  static final int BLOCK_HEADER_LENGTH = 224;
  static final int MAX_NUMBER_OF_TRANSACTIONS = 255;
  static final int MAX_PAYLOAD_LENGTH = 32640;
  static final int ALIAS_SYSTEM_BLOCK = 22000;
  static final int TRANSPARENT_FORGING_BLOCK = 30000;
  static final byte[] CHECKSUM_TRANSPARENT_FORGING = { 27, -54, -59, -98, 49, -42, 48, -68, -112, 49, 41, 94, -41, 78, -84, 27, -87, -22, -28, 36, -34, -90, 112, -50, -9, 5, 89, -35, 80, -121, -128, 112 };
  static final long MAX_BALANCE = 1000000000L;
  static final long initialBaseTarget = 153722867L;
  static final long maxBaseTarget = 153722867000000000L;
  static final BigInteger two64 = new BigInteger("18446744073709551616");
  static long epochBeginning;
  static final String alphabet = "0123456789abcdefghijklmnopqrstuvwxyz";
  static String myPlatform;
  static String myScheme;
  static String myAddress;
  static String myHallmark;
  static int myPort;
  static boolean shareMyAddress;
  static Set<String> allowedUserHosts;
  static Set<String> allowedBotHosts;
  static int blacklistingPeriod;
  static final int LOGGING_MASK_EXCEPTIONS = 1;
  static final int LOGGING_MASK_NON200_RESPONSES = 2;
  static final int LOGGING_MASK_200_RESPONSES = 4;
  static int communicationLoggingMask;
  static final AtomicInteger transactionCounter = new AtomicInteger();
  static final ConcurrentMap<Long, Nxt.Transaction> transactions = new ConcurrentHashMap();
  static final ConcurrentMap<Long, Nxt.Transaction> unconfirmedTransactions = new ConcurrentHashMap();
  static final ConcurrentMap<Long, Nxt.Transaction> doubleSpendingTransactions = new ConcurrentHashMap();
  static Set<String> wellKnownPeers;
  static int maxNumberOfConnectedPublicPeers;
  static int connectTimeout;
  static int readTimeout;
  static boolean enableHallmarkProtection;
  static int pushThreshold;
  static int pullThreshold;
  static final AtomicInteger peerCounter = new AtomicInteger();
  static final ConcurrentMap<String, Nxt.Peer> peers = new ConcurrentHashMap();
  static final Object blocksAndTransactionsLock = new Object();
  static final AtomicInteger blockCounter = new AtomicInteger();
  static final ConcurrentMap<Long, Nxt.Block> blocks = new ConcurrentHashMap();
  static volatile long lastBlock;
  static volatile Nxt.Peer lastBlockchainFeeder;
  static final ConcurrentMap<Long, Nxt.Account> accounts = new ConcurrentHashMap();
  static final ConcurrentMap<String, Nxt.Alias> aliases = new ConcurrentHashMap();
  static final ConcurrentMap<Long, Nxt.Alias> aliasIdToAliasMappings = new ConcurrentHashMap();
  static final HashMap<Long, Nxt.Asset> assets = new HashMap();
  static final HashMap<String, Long> assetNameToIdMappings = new HashMap();
  static final HashMap<Long, Nxt.AskOrder> askOrders = new HashMap();
  static final HashMap<Long, Nxt.BidOrder> bidOrders = new HashMap();
  static final HashMap<Long, TreeSet<Nxt.AskOrder>> sortedAskOrders = new HashMap();
  static final HashMap<Long, TreeSet<Nxt.BidOrder>> sortedBidOrders = new HashMap();
  static final ConcurrentMap<String, Nxt.User> users = new ConcurrentHashMap();
  static final ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(7);
  static final ThreadLocal<SimpleDateFormat> logDateFormat = new Nxt.1();
  
  static int getEpochTime(long paramLong)
  {
    return (int)((paramLong - epochBeginning + 500L) / 1000L);
  }
  
  static void logMessage(String paramString)
  {
    System.out.println(((SimpleDateFormat)logDateFormat.get()).format(new Date()) + paramString);
  }
  
  static byte[] convert(String paramString)
  {
    byte[] arrayOfByte = new byte[paramString.length() / 2];
    for (int i = 0; i < arrayOfByte.length; i++) {
      arrayOfByte[i] = ((byte)Integer.parseInt(paramString.substring(i * 2, i * 2 + 2), 16));
    }
    return arrayOfByte;
  }
  
  static String convert(byte[] paramArrayOfByte)
  {
    StringBuilder localStringBuilder = new StringBuilder();
    for (int k : paramArrayOfByte)
    {
      int m;
      localStringBuilder.append("0123456789abcdefghijklmnopqrstuvwxyz".charAt((m = k & 0xFF) >> 4)).append("0123456789abcdefghijklmnopqrstuvwxyz".charAt(m & 0xF));
    }
    return localStringBuilder.toString();
  }
  
  static String convert(long paramLong)
  {
    BigInteger localBigInteger = BigInteger.valueOf(paramLong);
    if (paramLong < 0L) {
      localBigInteger = localBigInteger.add(two64);
    }
    return localBigInteger.toString();
  }
  
  static void matchOrders(long paramLong)
    throws Exception
  {
    TreeSet localTreeSet1 = (TreeSet)sortedAskOrders.get(Long.valueOf(paramLong));
    TreeSet localTreeSet2 = (TreeSet)sortedBidOrders.get(Long.valueOf(paramLong));
    synchronized (askOrders)
    {
      synchronized (bidOrders)
      {
        while ((!localTreeSet1.isEmpty()) && (!localTreeSet2.isEmpty()))
        {
          Nxt.AskOrder localAskOrder = (Nxt.AskOrder)localTreeSet1.first();
          Nxt.BidOrder localBidOrder = (Nxt.BidOrder)localTreeSet2.first();
          if (localAskOrder.price > localBidOrder.price) {
            break;
          }
          int i = localAskOrder.quantity < localBidOrder.quantity ? localAskOrder.quantity : localBidOrder.quantity;
          long l = (localAskOrder.height < localBidOrder.height) || ((localAskOrder.height == localBidOrder.height) && (localAskOrder.id < localBidOrder.id)) ? localAskOrder.price : localBidOrder.price;
          if (localAskOrder.quantity -= i == 0)
          {
            askOrders.remove(Long.valueOf(localAskOrder.id));
            localTreeSet1.remove(localAskOrder);
          }
          localAskOrder.account.addToBalanceAndUnconfirmedBalance(i * l);
          if (localBidOrder.quantity -= i == 0)
          {
            bidOrders.remove(Long.valueOf(localBidOrder.id));
            localTreeSet2.remove(localBidOrder);
          }
          synchronized (localBidOrder.account)
          {
            Integer localInteger = (Integer)localBidOrder.account.assetBalances.get(Long.valueOf(paramLong));
            if (localInteger == null)
            {
              localBidOrder.account.assetBalances.put(Long.valueOf(paramLong), Integer.valueOf(i));
              localBidOrder.account.unconfirmedAssetBalances.put(Long.valueOf(paramLong), Integer.valueOf(i));
            }
            else
            {
              localBidOrder.account.assetBalances.put(Long.valueOf(paramLong), Integer.valueOf(localInteger.intValue() + i));
              localBidOrder.account.unconfirmedAssetBalances.put(Long.valueOf(paramLong), Integer.valueOf(((Integer)localBidOrder.account.unconfirmedAssetBalances.get(Long.valueOf(paramLong))).intValue() + i));
            }
          }
        }
      }
    }
  }
  
  public void init(ServletConfig paramServletConfig)
    throws ServletException
  {
    logMessage("Nxt 0.5.3 started.");
    try
    {
      Calendar localCalendar = Calendar.getInstance();
      localCalendar.set(15, 0);
      localCalendar.set(1, 2013);
      localCalendar.set(2, 10);
      localCalendar.set(5, 24);
      localCalendar.set(11, 12);
      localCalendar.set(12, 0);
      localCalendar.set(13, 0);
      localCalendar.set(14, 0);
      epochBeginning = localCalendar.getTimeInMillis();
      String str1 = paramServletConfig.getInitParameter("blockchainStoragePath");
      logMessage("\"blockchainStoragePath\" = \"" + str1 + "\"");
      myPlatform = paramServletConfig.getInitParameter("myPlatform");
      logMessage("\"myPlatform\" = \"" + myPlatform + "\"");
      if (myPlatform == null) {
        myPlatform = "PC";
      } else {
        myPlatform = myPlatform.trim();
      }
      myScheme = paramServletConfig.getInitParameter("myScheme");
      logMessage("\"myScheme\" = \"" + myScheme + "\"");
      if (myScheme == null) {
        myScheme = "http";
      } else {
        myScheme = myScheme.trim();
      }
      String str2 = paramServletConfig.getInitParameter("myPort");
      logMessage("\"myPort\" = \"" + str2 + "\"");
      try
      {
        myPort = Integer.parseInt(str2);
      }
      catch (Exception localException2)
      {
        myPort = myScheme.equals("https") ? 7875 : 7874;
      }
      myAddress = paramServletConfig.getInitParameter("myAddress");
      logMessage("\"myAddress\" = \"" + myAddress + "\"");
      if (myAddress != null) {
        myAddress = myAddress.trim();
      }
      String str3 = paramServletConfig.getInitParameter("shareMyAddress");
      logMessage("\"shareMyAddress\" = \"" + str3 + "\"");
      try
      {
        shareMyAddress = Boolean.parseBoolean(str3);
      }
      catch (Exception localException3)
      {
        shareMyAddress = true;
      }
      myHallmark = paramServletConfig.getInitParameter("myHallmark");
      logMessage("\"myHallmark\" = \"" + myHallmark + "\"");
      if (myHallmark != null) {
        myHallmark = myHallmark.trim();
      }
      String str4 = paramServletConfig.getInitParameter("wellKnownPeers");
      logMessage("\"wellKnownPeers\" = \"" + str4 + "\"");
      if (str4 != null)
      {
        localObject1 = new HashSet();
        for (String str8 : str4.split(";"))
        {
          str8 = str8.trim();
          if (str8.length() > 0)
          {
            ((Set)localObject1).add(str8);
            Nxt.Peer.addPeer(str8, str8);
          }
        }
        wellKnownPeers = Collections.unmodifiableSet((Set)localObject1);
      }
      else
      {
        wellKnownPeers = Collections.emptySet();
        logMessage("No wellKnownPeers defined, it is unlikely to work");
      }
      Object localObject1 = paramServletConfig.getInitParameter("maxNumberOfConnectedPublicPeers");
      logMessage("\"maxNumberOfConnectedPublicPeers\" = \"" + (String)localObject1 + "\"");
      try
      {
        maxNumberOfConnectedPublicPeers = Integer.parseInt((String)localObject1);
      }
      catch (Exception localException4)
      {
        maxNumberOfConnectedPublicPeers = 10;
      }
      String str5 = paramServletConfig.getInitParameter("connectTimeout");
      logMessage("\"connectTimeout\" = \"" + str5 + "\"");
      try
      {
        connectTimeout = Integer.parseInt(str5);
      }
      catch (Exception localException5)
      {
        connectTimeout = 1000;
      }
      String str6 = paramServletConfig.getInitParameter("readTimeout");
      logMessage("\"readTimeout\" = \"" + str6 + "\"");
      try
      {
        readTimeout = Integer.parseInt(str6);
      }
      catch (Exception localException6)
      {
        readTimeout = 1000;
      }
      String str7 = paramServletConfig.getInitParameter("enableHallmarkProtection");
      logMessage("\"enableHallmarkProtection\" = \"" + str7 + "\"");
      try
      {
        enableHallmarkProtection = Boolean.parseBoolean(str7);
      }
      catch (Exception localException7)
      {
        enableHallmarkProtection = true;
      }
      String str9 = paramServletConfig.getInitParameter("pushThreshold");
      logMessage("\"pushThreshold\" = \"" + str9 + "\"");
      try
      {
        pushThreshold = Integer.parseInt(str9);
      }
      catch (Exception localException8)
      {
        pushThreshold = 0;
      }
      String str10 = paramServletConfig.getInitParameter("pullThreshold");
      logMessage("\"pullThreshold\" = \"" + str10 + "\"");
      try
      {
        pullThreshold = Integer.parseInt(str10);
      }
      catch (Exception localException9)
      {
        pullThreshold = 0;
      }
      String str11 = paramServletConfig.getInitParameter("allowedUserHosts");
      logMessage("\"allowedUserHosts\" = \"" + str11 + "\"");
      if ((str11 != null) && (!str11.trim().equals("*")))
      {
        localObject2 = new HashSet();
        for (String str13 : str11.split(";"))
        {
          str13 = str13.trim();
          if (str13.length() > 0) {
            ((Set)localObject2).add(str13);
          }
        }
        allowedUserHosts = Collections.unmodifiableSet((Set)localObject2);
      }
      Object localObject2 = paramServletConfig.getInitParameter("allowedBotHosts");
      logMessage("\"allowedBotHosts\" = \"" + (String)localObject2 + "\"");
      Object localObject5;
      if ((localObject2 != null) && (!((String)localObject2).trim().equals("*")))
      {
        ??? = new HashSet();
        for (localObject5 : ((String)localObject2).split(";"))
        {
          localObject5 = ((String)localObject5).trim();
          if (((String)localObject5).length() > 0) {
            ((Set)???).add(localObject5);
          }
        }
        allowedBotHosts = Collections.unmodifiableSet((Set)???);
      }
      ??? = paramServletConfig.getInitParameter("blacklistingPeriod");
      logMessage("\"blacklistingPeriod\" = \"" + (String)??? + "\"");
      try
      {
        blacklistingPeriod = Integer.parseInt((String)???);
      }
      catch (Exception localException10)
      {
        blacklistingPeriod = 300000;
      }
      String str12 = paramServletConfig.getInitParameter("communicationLoggingMask");
      logMessage("\"communicationLoggingMask\" = \"" + str12 + "\"");
      try
      {
        communicationLoggingMask = Integer.parseInt(str12);
      }
      catch (Exception localException11) {}
      Object localObject4;
      Object localObject6;
      try
      {
        logMessage("Loading transactions...");
        Nxt.Transaction.loadTransactions("transactions.nxt");
        logMessage("...Done");
      }
      catch (FileNotFoundException localFileNotFoundException1)
      {
        transactions.clear();
        localObject4 = new long[] { new BigInteger("163918645372308887").longValue(), new BigInteger("620741658595224146").longValue(), new BigInteger("723492359641172834").longValue(), new BigInteger("818877006463198736").longValue(), new BigInteger("1264744488939798088").longValue(), new BigInteger("1600633904360147460").longValue(), new BigInteger("1796652256451468602").longValue(), new BigInteger("1814189588814307776").longValue(), new BigInteger("1965151371996418680").longValue(), new BigInteger("2175830371415049383").longValue(), new BigInteger("2401730748874927467").longValue(), new BigInteger("2584657662098653454").longValue(), new BigInteger("2694765945307858403").longValue(), new BigInteger("3143507805486077020").longValue(), new BigInteger("3684449848581573439").longValue(), new BigInteger("4071545868996394636").longValue(), new BigInteger("4277298711855908797").longValue(), new BigInteger("4454381633636789149").longValue(), new BigInteger("4747512364439223888").longValue(), new BigInteger("4777958973882919649").longValue(), new BigInteger("4803826772380379922").longValue(), new BigInteger("5095742298090230979").longValue(), new BigInteger("5271441507933314159").longValue(), new BigInteger("5430757907205901788").longValue(), new BigInteger("5491587494620055787").longValue(), new BigInteger("5622658764175897611").longValue(), new BigInteger("5982846390354787993").longValue(), new BigInteger("6290807999797358345").longValue(), new BigInteger("6785084810899231190").longValue(), new BigInteger("6878906112724074600").longValue(), new BigInteger("7017504655955743955").longValue(), new BigInteger("7085298282228890923").longValue(), new BigInteger("7446331133773682477").longValue(), new BigInteger("7542917420413518667").longValue(), new BigInteger("7549995577397145669").longValue(), new BigInteger("7577840883495855927").longValue(), new BigInteger("7579216551136708118").longValue(), new BigInteger("8278234497743900807").longValue(), new BigInteger("8517842408878875334").longValue(), new BigInteger("8870453786186409991").longValue(), new BigInteger("9037328626462718729").longValue(), new BigInteger("9161949457233564608").longValue(), new BigInteger("9230759115816986914").longValue(), new BigInteger("9306550122583806885").longValue(), new BigInteger("9433259657262176905").longValue(), new BigInteger("9988839211066715803").longValue(), new BigInteger("10105875265190846103").longValue(), new BigInteger("10339765764359265796").longValue(), new BigInteger("10738613957974090819").longValue(), new BigInteger("10890046632913063215").longValue(), new BigInteger("11494237785755831723").longValue(), new BigInteger("11541844302056663007").longValue(), new BigInteger("11706312660844961581").longValue(), new BigInteger("12101431510634235443").longValue(), new BigInteger("12186190861869148835").longValue(), new BigInteger("12558748907112364526").longValue(), new BigInteger("13138516747685979557").longValue(), new BigInteger("13330279748251018740").longValue(), new BigInteger("14274119416917666908").longValue(), new BigInteger("14557384677985343260").longValue(), new BigInteger("14748294830376619968").longValue(), new BigInteger("14839596582718854826").longValue(), new BigInteger("15190676494543480574").longValue(), new BigInteger("15253761794338766759").longValue(), new BigInteger("15558257163011348529").longValue(), new BigInteger("15874940801139996458").longValue(), new BigInteger("16516270647696160902").longValue(), new BigInteger("17156841960446798306").longValue(), new BigInteger("17228894143802851995").longValue(), new BigInteger("17240396975291969151").longValue(), new BigInteger("17491178046969559641").longValue(), new BigInteger("18345202375028346230").longValue(), new BigInteger("18388669820699395594").longValue() };
        localObject5 = new int[] { 36742, 1970092, 349130, 24880020, 2867856, 9975150, 2690963, 7648, 5486333, 34913026, 997515, 30922966, 6650, 44888, 2468850, 49875751, 49875751, 9476393, 49875751, 14887912, 528683, 583546, 7315, 19925363, 29856290, 5320, 4987575, 5985, 24912938, 49875751, 2724712, 1482474, 200999, 1476156, 498758, 987540, 16625250, 5264386, 15487585, 2684479, 14962725, 34913026, 5033128, 2916900, 49875751, 4962637, 170486123, 8644631, 22166945, 6668388, 233751, 4987575, 11083556, 1845403, 49876, 3491, 3491, 9476, 49876, 6151, 682633, 49875751, 482964, 4988, 49875751, 4988, 9144, 503745, 49875751, 52370, 29437998, 585375, 9975150 };
        localObject6 = new byte[][] { { 41, 115, -41, 7, 37, 21, -3, -41, 120, 119, 63, -101, 108, 48, -117, 1, -43, 32, 85, 95, 65, 42, 92, -22, 123, -36, 6, -99, -61, -53, 93, 7, 23, 8, -30, 65, 57, -127, -2, 42, -92, -104, 11, 72, -66, 108, 17, 113, 99, -117, -75, 123, 110, 107, 119, -25, 67, 64, 32, 117, 111, 54, 82, -14 }, { 118, 43, 84, -91, -110, -102, 100, -40, -33, -47, -13, -7, -88, 2, -42, -66, -38, -22, 105, -42, -69, 78, 51, -55, -48, 49, -89, 116, -96, -104, -114, 14, 94, 58, -115, -8, 111, -44, 76, -104, 54, -15, 126, 31, 6, -80, 65, 6, 124, 37, -73, 92, 4, 122, 122, -108, 1, -54, 31, -38, -117, -1, -52, -56 }, { 79, 100, -101, 107, -6, -61, 40, 32, -98, 32, 80, -59, -76, -23, -62, 38, 4, 105, -106, -105, -121, -85, 13, -98, -77, 126, -125, 103, 12, -41, 1, 2, 45, -62, -69, 102, 116, -61, 101, -14, -68, -31, 9, 110, 18, 2, 33, -98, -37, -128, 17, -19, 124, 125, -63, 92, -70, 96, 96, 125, 91, 8, -65, -12 }, { 58, -99, 14, -97, -75, -10, 110, -102, 119, -3, -2, -12, -82, -33, -27, 118, -19, 55, -109, 6, 110, -10, 108, 30, 94, -113, -5, -98, 19, 12, -125, 14, -77, 33, -128, -21, 36, -120, -12, -81, 64, 95, 67, -3, 100, 122, -47, 127, -92, 114, 68, 72, 2, -40, 80, 117, -17, -56, 103, 37, -119, 3, 22, 23 }, { 76, 22, 121, -4, -77, -127, 18, -102, 7, 94, -73, -96, 108, -11, 81, -18, -37, -85, -75, 86, -119, 94, 126, 95, 47, -36, -16, -50, -9, 95, 60, 15, 14, 93, -108, -83, -67, 29, 2, -53, 10, -118, -51, -46, 92, -23, -56, 60, 46, -90, -84, 126, 60, 78, 12, 53, 61, 121, -6, 77, 112, 60, 40, 63 }, { 64, 121, -73, 68, 4, -103, 81, 55, -41, -81, -63, 10, 117, -74, 54, -13, -85, 79, 21, 116, -25, -12, 21, 120, -36, -80, 53, -78, 103, 25, 55, 6, -75, 96, 80, -125, -11, -103, -20, -41, 121, -61, -40, 63, 24, -81, -125, 90, -12, -40, -52, -1, -114, 14, -44, -112, -80, 83, -63, 88, -107, -10, -114, -86 }, { -81, 126, -41, -34, 66, -114, -114, 114, 39, 32, -125, -19, -95, -50, -111, -51, -33, 51, 99, -127, 58, 50, -110, 44, 80, -94, -96, 68, -69, 34, 86, 3, -82, -69, 28, 20, -111, 69, 18, -41, -23, 27, -118, 20, 72, 21, -112, 53, -87, -81, -47, -101, 123, -80, 99, -15, 33, -120, -8, 82, 80, -8, -10, -45 }, { 92, 77, 53, -87, 26, 13, -121, -39, -62, -42, 47, 4, 7, 108, -15, 112, 103, 38, -50, -74, 60, 56, -63, 43, -116, 49, -106, 69, 118, 65, 17, 12, 31, 127, -94, 55, -117, -29, -117, 31, -95, -110, -2, 63, -73, -106, -88, -41, -19, 69, 60, -17, -16, 61, 32, -23, 88, -106, -96, 37, -96, 114, -19, -99 }, { 68, -26, 57, -56, -30, 108, 61, 24, 106, -56, -92, 99, -59, 107, 25, -110, -57, 80, 79, -92, -107, 90, 54, -73, -40, -39, 78, 109, -57, -62, -17, 6, -25, -29, 37, 90, -24, -27, -61, -69, 44, 121, 107, -72, -57, 108, 32, -69, -21, -41, 126, 91, 118, 11, -91, 50, -11, 116, 126, -96, -39, 110, 105, -52 }, { 48, 108, 123, 50, -50, -58, 33, 14, 59, 102, 17, -18, -119, 4, 10, -29, 36, -56, -31, 43, -71, -48, -14, 87, 119, -119, 40, 104, -44, -76, -24, 2, 48, -96, -7, 16, -119, -3, 108, 78, 125, 88, 61, -53, -3, -16, 20, -83, 74, 124, -47, -17, -15, -21, -23, -119, -47, 105, -4, 115, -20, 77, 57, 88 }, { 33, 101, 79, -35, 32, -119, 20, 120, 34, -80, -41, 90, -22, 93, -20, -45, 9, 24, -46, 80, -55, -9, -24, -78, -124, 27, -120, -36, -51, 59, -38, 7, 113, 125, 68, 109, 24, -121, 111, 37, -71, 100, -111, 78, -43, -14, -76, -44, 64, 103, 16, -28, -44, -103, 74, 81, -118, -74, 47, -77, -65, 8, 42, -100 }, { -63, -96, -95, -111, -85, -98, -85, 42, 87, 29, -62, -57, 57, 48, 9, -39, -110, 63, -103, -114, -48, -11, -92, 105, -26, -79, -11, 78, -118, 14, -39, 1, -115, 74, 70, -41, -119, -68, -39, -60, 64, 31, 25, -111, -16, -20, 61, -22, 17, -13, 57, -110, 24, 61, -104, 21, -72, -69, 56, 116, -117, 93, -1, -123 }, { -18, -70, 12, 112, -111, 10, 22, 31, -120, 26, 53, 14, 10, 69, 51, 45, -50, -127, -22, 95, 54, 17, -8, 54, -115, 36, -79, 12, -79, 82, 4, 1, 92, 59, 23, -13, -85, -87, -110, -58, 84, -31, -48, -105, -101, -92, -9, 28, -109, 77, -47, 100, -48, -83, 106, -102, 70, -95, 94, -1, -99, -15, 21, 99 }, { 109, 123, 54, 40, -120, 32, -118, 49, -52, 0, -103, 103, 101, -9, 32, 78, 124, -56, 88, -19, 101, -32, 70, 67, -41, 85, -103, 1, 1, -105, -51, 10, 4, 51, -26, -19, 39, -43, 63, -41, -101, 80, 106, -66, 125, 47, -117, -120, -93, -120, 99, -113, -17, 61, 102, -2, 72, 9, -124, 123, -128, 78, 43, 96 }, { -22, -63, 20, 65, 5, -89, -123, -61, 14, 34, 83, -113, 34, 85, 26, -21, 1, 16, 88, 55, -92, -111, 14, -31, -37, -67, -8, 85, 39, -112, -33, 8, 28, 16, 107, -29, 1, 3, 100, -53, 2, 81, 52, -94, -14, 36, -123, -82, -6, -118, 104, 75, -99, -82, -100, 7, 30, -66, 0, -59, 108, -54, 31, 20 }, { 0, 13, -74, 28, -54, -12, 45, 36, -24, 55, 43, -110, -72, 117, -110, -56, -72, 85, 79, -89, -92, 65, -67, -34, -24, 38, 67, 42, 84, -94, 91, 13, 100, 89, 20, -95, -76, 2, 116, 34, 67, 52, -80, -101, -22, -32, 51, 32, -76, 44, -93, 11, 42, -69, -12, 7, -52, -55, 122, -10, 48, 21, 92, 110 }, { -115, 19, 115, 28, -56, 118, 111, 26, 18, 123, 111, -96, -115, 120, 105, 62, -123, -124, 101, 51, 3, 18, -89, 127, 48, -27, 39, -78, -118, 5, -2, 6, -105, 17, 123, 26, 25, -62, -37, 49, 117, 3, 10, 97, -7, 54, 121, -90, -51, -49, 11, 104, -66, 11, -6, 57, 5, -64, -8, 59, 82, -126, 26, -113 }, { 16, -53, 94, 99, -46, -29, 64, -89, -59, 116, -21, 53, 14, -77, -71, 95, 22, -121, -51, 125, -14, -96, 95, 95, 32, 96, 79, 41, -39, -128, 79, 0, 5, 6, -115, 104, 103, 77, -92, 93, -109, 58, 96, 97, -22, 116, -62, 11, 30, -122, 14, 28, 69, 124, 63, -119, 19, 80, -36, -116, -76, -58, 36, 87 }, { 109, -82, 33, -119, 17, 109, -109, -16, 98, 108, 111, 5, 98, 1, -15, -32, 22, 46, -65, 117, -78, 119, 35, -35, -3, 41, 23, -97, 55, 69, 58, 9, 20, -113, -121, -13, -41, -48, 22, -73, -1, -44, -73, 3, -10, -122, 19, -103, 10, -26, -128, 62, 34, 55, 54, -43, 35, -30, 115, 64, -80, -20, -25, 67 }, { -16, -74, -116, -128, 52, 96, -75, 17, -22, 72, -43, 22, -95, -16, 32, -72, 98, 46, -4, 83, 34, -58, -108, 18, 17, -58, -123, 53, -108, 116, 18, 2, 7, -94, -126, -45, 72, -69, -65, -89, 64, 31, -78, 78, -115, 106, 67, 55, -123, 104, -128, 36, -23, -90, -14, -87, 78, 19, 18, -128, 39, 73, 35, 120 }, { 20, -30, 15, 111, -82, 39, -108, 57, -80, 98, -19, -27, 100, -18, 47, 77, -41, 95, 80, -113, -128, -88, -76, 115, 65, -53, 83, 115, 7, 2, -104, 3, 120, 115, 14, -116, 33, -15, -120, 22, -56, -8, -69, 5, -75, 94, 124, 12, -126, -48, 51, -105, 22, -66, -93, 16, -63, -74, 32, 114, -54, -3, -47, -126 }, { 56, -101, 55, -1, 64, 4, -64, 95, 31, -15, 72, 46, 67, -9, 68, -43, -55, 28, -63, -17, -16, 65, 11, -91, -91, 32, 88, 41, 60, 67, 105, 8, 58, 102, -79, -5, -113, -113, -67, 82, 50, -26, 116, -78, -103, 107, 102, 23, -74, -47, 115, -50, -35, 63, -80, -32, 72, 117, 47, 68, 86, -20, -35, 8 }, { 21, 27, 20, -59, 117, -102, -42, 22, -10, 121, 41, -59, 115, 15, -43, 54, -79, -62, -16, 58, 116, 15, 88, 108, 114, 67, 3, -30, -99, 78, 103, 11, 49, 63, -4, -110, -27, 41, 70, -57, -69, -18, 70, 30, -21, 66, -104, -27, 3, 53, 50, 100, -33, 54, -3, -78, 92, 85, -78, 54, 19, 32, 95, 9 }, { -93, 65, -64, -79, 82, 85, -34, -90, 122, -29, -40, 3, -80, -40, 32, 26, 102, -73, 17, 53, -93, -29, 122, 86, 107, -100, 50, 56, -28, 124, 90, 14, 93, -88, 97, 101, -85, -50, 46, -109, -88, -127, -112, 63, -89, 24, -34, -9, -116, -59, -87, -86, -12, 111, -111, 87, -87, -13, -73, -124, -47, 7, 1, 9 }, { 60, -99, -77, -20, 112, -75, -34, 100, -4, -96, 81, 71, -116, -62, 38, -68, 105, 7, -126, 21, -125, -25, -56, -11, -59, 95, 117, 108, 32, -38, -65, 13, 46, 65, -46, -89, 0, 120, 5, 23, 40, 110, 114, 79, 111, -70, 8, 16, -49, -52, -82, -18, 108, -43, 81, 96, 72, -65, 70, 7, -37, 58, 46, -14 }, { -95, -32, 85, 78, 74, -53, 93, -102, -26, -110, 86, 1, -93, -50, -23, -108, -37, 97, 19, 103, 94, -65, -127, -21, 60, 98, -51, -118, 82, -31, 27, 7, -112, -45, 79, 95, -20, 90, -4, -40, 117, 100, -6, 19, -47, 53, 53, 48, 105, 91, -70, -34, -5, -87, -57, -103, -112, -108, -40, 87, -25, 13, -76, -116 }, { 44, -122, -70, 125, -60, -32, 38, 69, -77, -103, 49, -124, -4, 75, -41, -84, 68, 74, 118, 15, -13, 115, 117, -78, 42, 89, 0, -20, -12, -58, -97, 10, -48, 95, 81, 101, 23, -67, -23, 74, -79, 21, 97, 123, 103, 101, -50, -115, 116, 112, 51, 50, -124, 27, 76, 40, 74, 10, 65, -49, 102, 95, 5, 35 }, { -6, 57, 71, 5, -61, -100, -21, -9, 47, -60, 59, 108, -75, 105, 56, 41, -119, 31, 37, 27, -86, 120, -125, -108, 121, 104, -21, -70, -57, -104, 2, 11, 118, 104, 68, 6, 7, -90, -70, -61, -16, 77, -8, 88, 31, -26, 35, -44, 8, 50, 51, -88, -62, -103, 54, -41, -2, 117, 98, -34, 49, -123, 83, -58 }, { 54, 21, -36, 126, -50, -72, 82, -5, -122, -116, 72, -19, -18, -68, -71, -27, 97, -22, 53, -94, 47, -6, 15, -92, -55, 127, 13, 13, -69, 81, -82, 8, -50, 10, 84, 110, -87, -44, 61, -78, -65, 84, -32, 48, -8, -105, 35, 116, -68, -116, -6, 75, -77, 120, -95, 74, 73, 105, 39, -87, 98, -53, 47, 10 }, { -113, 116, 37, -1, 95, -89, -93, 113, 36, -70, -57, -99, 94, 52, -81, -118, 98, 58, -36, 73, 82, -67, -80, 46, 83, -127, -8, 73, 66, -27, 43, 7, 108, 32, 73, 1, -56, -108, 41, -98, -15, 49, 1, 107, 65, 44, -68, 126, -28, -53, 120, -114, 126, -79, -14, -105, -33, 53, 5, -119, 67, 52, 35, -29 }, { 98, 23, 23, 83, 78, -89, 13, 55, -83, 97, -30, -67, 99, 24, 47, -4, -117, -34, -79, -97, 95, 74, 4, 21, 66, -26, 15, 80, 60, -25, -118, 14, 36, -55, -41, -124, 90, -1, 84, 52, 31, 88, 83, 121, -47, -59, -10, 17, 51, -83, 23, 108, 19, 104, 32, 29, -66, 24, 21, 110, 104, -71, -23, -103 }, { 12, -23, 60, 35, 6, -52, -67, 96, 15, -128, -47, -15, 40, 3, 54, -81, 3, 94, 3, -98, -94, -13, -74, -101, 40, -92, 90, -64, -98, 68, -25, 2, -62, -43, 112, 32, -78, -123, 26, -80, 126, 120, -88, -92, 126, -128, 73, -43, 87, -119, 81, 111, 95, -56, -128, -14, 51, -40, -42, 102, 46, 106, 6, 6 }, { -38, -120, -11, -114, -7, -105, -98, 74, 114, 49, 64, -100, 4, 40, 110, -21, 25, 6, -92, -40, -61, 48, 94, -116, -71, -87, 75, -31, 13, -119, 1, 5, 33, -69, -16, -125, -79, -46, -36, 3, 40, 1, -88, -118, -107, 95, -23, -107, -49, 44, -39, 2, 108, -23, 39, 50, -51, -59, -4, -42, -10, 60, 10, -103 }, { 67, -53, 55, -32, -117, 3, 94, 52, -115, -127, -109, 116, -121, -27, -115, -23, 98, 90, -2, 48, -54, -76, 108, -56, 99, 30, -35, -18, -59, 25, -122, 3, 43, -13, -109, 34, -10, 123, 117, 113, -112, -85, -119, -62, -78, -114, -96, 101, 72, -98, 28, 89, -98, -121, 20, 115, 89, -20, 94, -55, 124, 27, -76, 94 }, { 15, -101, 98, -21, 8, 5, -114, -64, 74, 123, 99, 28, 125, -33, 22, 23, -2, -56, 13, 91, 27, -105, -113, 19, 60, -7, -67, 107, 70, 103, -107, 13, -38, -108, -77, -29, 2, 9, -12, 21, 12, 65, 108, -16, 69, 77, 96, -54, 55, -78, -7, 41, -48, 124, -12, 64, 113, -45, -21, -119, -113, 88, -116, 113 }, { -17, 77, 10, 84, -57, -12, 101, 21, -91, 92, 17, -32, -26, 77, 70, 46, 81, -55, 40, 44, 118, -35, -97, 47, 5, 125, 41, -127, -72, 66, -18, 2, 115, -13, -74, 126, 86, 80, 11, -122, -29, -68, 113, 54, -117, 107, -75, -107, -54, 72, -44, 98, -111, -33, -56, -40, 93, -47, 84, -43, -45, 86, 65, -84 }, { -126, 60, -56, 121, 31, -124, -109, 100, -118, -29, 106, 94, 5, 27, 13, -79, 91, -111, -38, -42, 18, 61, -100, 118, -18, -4, -60, 121, 46, -22, 6, 4, -37, -20, 124, -43, 51, -57, -49, -44, -24, -38, 81, 60, -14, -97, -109, -11, -5, -85, 75, -17, -124, -96, -53, 52, 64, 100, -118, -120, 6, 60, 76, -110 }, { -12, -40, 115, -41, 68, 85, 20, 91, -44, -5, 73, -105, -81, 32, 116, 32, -28, 69, 88, -54, 29, -53, -51, -83, 54, 93, -102, -102, -23, 7, 110, 15, 34, 122, 84, 52, -121, 37, -103, -91, 37, -77, -101, 64, -18, 63, -27, -75, -112, -11, 1, -69, -25, -123, -99, -31, 116, 11, 4, -42, -124, 98, -2, 53 }, { -128, -69, -16, -33, -8, -112, 39, -57, 113, -76, -29, -37, 4, 121, -63, 12, -54, -66, -121, 13, -4, -44, 50, 27, 103, 101, 44, -115, 12, -4, -8, 10, 53, 108, 90, -47, 46, -113, 5, -3, -111, 8, -66, -73, 57, 72, 90, -33, 47, 99, 50, -55, 53, -4, 96, 87, 57, 26, 53, -45, -83, 39, -17, 45 }, { -121, 125, 60, -9, -79, -128, -19, 113, 54, 77, -23, -89, 105, -5, 47, 114, -120, -88, 31, 25, -96, -75, -6, 76, 9, -83, 75, -109, -126, -47, -6, 2, -59, 64, 3, 74, 100, 110, -96, 66, -3, 10, -124, -6, 8, 50, 109, 14, -109, 79, 73, 77, 67, 63, -50, 10, 86, -63, -125, -86, 35, -26, 7, 83 }, { 36, 31, -77, 126, 106, 97, 63, 81, -37, -126, 69, -127, -22, -69, 104, -111, 93, -49, 77, -3, -38, -112, 47, -55, -23, -68, -8, 78, -127, -28, -59, 10, 22, -61, -127, -13, -72, -14, -87, 14, 61, 76, -89, 81, -97, -97, -105, 94, -93, -9, -3, -104, -83, 59, 104, 121, -30, 106, -2, 62, -51, -72, -63, 55 }, { 81, -88, -8, -96, -31, 118, -23, -38, -94, 80, 35, -20, -93, -102, 124, 93, 0, 15, 36, -127, -41, -19, 6, -124, 94, -49, 44, 26, -69, 43, -58, 9, -18, -3, -2, 60, -122, -30, -47, 124, 71, 47, -74, -68, 4, -101, -16, 77, -120, -15, 45, -12, 68, -77, -74, 63, -113, 44, -71, 56, 122, -59, 53, -44 }, { 122, 30, 27, -79, 32, 115, -104, -28, -53, 109, 120, 121, -115, -65, -87, 101, 23, 10, 122, 101, 29, 32, 56, 63, -23, -48, -51, 51, 16, -124, -41, 6, -71, 49, -20, 26, -57, 65, 49, 45, 7, 49, -126, 54, -122, -43, 1, -5, 111, 117, 104, 117, 126, 114, -77, 66, -127, -50, 69, 14, 70, 73, 60, 112 }, { 104, -117, 105, -118, 35, 16, -16, 105, 27, -87, -43, -59, -13, -23, 5, 8, -112, -28, 18, -1, 48, 94, -82, 55, 32, 16, 59, -117, 108, -89, 101, 9, -35, 58, 70, 62, 65, 91, 14, -43, -104, 97, 1, -72, 16, -24, 79, 79, -85, -51, -79, -55, -128, 23, 109, -95, 17, 92, -38, 109, 65, -50, 46, -114 }, { 44, -3, 102, -60, -85, 66, 121, -119, 9, 82, -47, -117, 67, -28, 108, 57, -47, -52, -24, -82, 65, -13, 85, 107, -21, 16, -24, -85, 102, -92, 73, 5, 7, 21, 41, 47, -118, 72, 43, 51, -5, -64, 100, -34, -25, 53, -45, -115, 30, -72, -114, 126, 66, 60, -24, -67, 44, 48, 22, 117, -10, -33, -89, -108 }, { -7, 71, -93, -66, 3, 30, -124, -116, -48, -76, -7, -62, 125, -122, -60, -104, -30, 71, 36, -110, 34, -126, 31, 10, 108, 102, -53, 56, 104, -56, -48, 12, 25, 21, 19, -90, 45, -122, -73, -112, 97, 96, 115, 71, 127, -7, -46, 84, -24, 102, -104, -96, 28, 8, 37, -84, -13, -65, -6, 61, -85, -117, -30, 70 }, { -112, 39, -39, -24, 127, -115, 68, -1, -111, -43, 101, 20, -12, 39, -70, 67, -50, 68, 105, 69, -91, -106, 91, 4, -52, 75, 64, -121, 46, -117, 31, 10, -125, 77, 51, -3, -93, 58, 79, 121, 126, -29, 56, -101, 1, -28, 49, 16, -80, 92, -62, 83, 33, 17, 106, 89, -9, 60, 79, 38, -74, -48, 119, 24 }, { 105, -118, 34, 52, 111, 30, 38, -73, 125, -116, 90, 69, 2, 126, -34, -25, -41, -67, -23, -105, -12, -75, 10, 69, -51, -95, -101, 92, -80, -73, -120, 2, 71, 46, 11, -85, -18, 125, 81, 117, 33, -89, -42, 118, 51, 60, 89, 110, 97, -118, -111, -36, 75, 112, -4, -8, -36, -49, -55, 35, 92, 70, -37, 36 }, { 71, 4, -113, 13, -48, 29, -56, 82, 115, -38, -20, -79, -8, 126, -111, 5, -12, -56, -107, 98, 111, 19, 127, -10, -42, 24, -38, -123, 59, 51, -64, 3, 47, -1, -83, -127, -58, 86, 33, -76, 5, 71, -80, -50, -62, 116, 75, 20, -126, 23, -31, -21, 24, -83, -19, 114, -17, 1, 110, -70, -119, 126, 82, -83 }, { -77, -69, -45, -78, -78, 69, 35, 85, 84, 25, -66, -25, 53, -38, -2, 125, -38, 103, 88, 31, -9, -43, 15, -93, 69, -22, -13, -20, 73, 3, -100, 7, 26, -18, 123, -14, -78, 113, 79, -57, -109, -118, 105, -104, 75, -88, -24, -109, 73, -126, 9, 55, 98, -120, 93, 114, 74, 0, -86, -68, 47, 29, 75, 67 }, { -104, 11, -85, 16, -124, -91, 66, -91, 18, -67, -122, -57, -114, 88, 79, 11, -60, -119, 89, 64, 57, 120, -11, 8, 52, -18, -67, -127, 26, -19, -69, 2, -82, -56, 11, -90, -104, 110, -10, -68, 87, 21, 28, 87, -5, -74, -21, -84, 120, 70, -17, 102, 72, -116, -69, 108, -86, -79, -74, 115, -78, -67, 6, 45 }, { -6, -101, -17, 38, -25, -7, -93, 112, 13, -33, 121, 71, -79, -122, -95, 22, 47, -51, 16, 84, 55, -39, -26, 37, -36, -18, 11, 119, 106, -57, 42, 8, -1, 23, 7, -63, -9, -50, 30, 35, -125, 83, 9, -60, -94, -15, -76, 120, 18, -103, -70, 95, 26, 48, -103, -95, 10, 113, 66, 54, -96, -4, 37, 111 }, { -124, -53, 43, -59, -73, 99, 71, -36, -31, 61, -25, -14, -71, 48, 17, 10, -26, -21, -22, 104, 64, -128, 27, -40, 111, -70, -90, 91, -81, -88, -10, 11, -62, 127, -124, -2, -67, -69, 65, 73, 40, 82, 112, -112, 100, -26, 30, 86, 30, 1, -105, 45, 103, -47, -124, 58, 105, 24, 20, 108, -101, 84, -34, 80 }, { 28, -1, 84, 111, 43, 109, 57, -23, 52, -95, 110, -50, 77, 15, 80, 85, 125, -117, -10, 8, 59, -58, 18, 97, -58, 45, 92, -3, 56, 24, -117, 9, -73, -9, 48, -99, 50, -24, -3, -41, -43, 48, -77, -8, -89, -42, 126, 73, 28, -65, -108, 54, 6, 34, 32, 2, -73, -123, -106, -52, -73, -106, -112, 109 }, { 73, -76, -7, 49, 67, -34, 124, 80, 111, -91, -22, -121, -74, 42, -4, -18, 84, -3, 38, 126, 31, 54, -120, 65, -122, -14, -38, -80, -124, 90, 37, 1, 51, 123, 69, 48, 109, -112, -63, 27, 67, -127, 29, 79, -26, 99, -24, -100, 51, 103, -105, 13, 85, 74, 12, -37, 43, 80, -113, 6, 70, -107, -5, -80 }, { 110, -54, 109, 21, -124, 98, 90, -26, 69, -44, 17, 117, 78, -91, -7, -18, -81, -43, 20, 80, 48, -109, 117, 125, -67, 19, -15, 69, -28, 47, 15, 4, 34, -54, 51, -128, 18, 61, -77, -122, 100, -58, -118, -36, 5, 32, 43, 15, 60, -55, 120, 123, -77, -76, -121, 77, 93, 16, -73, 54, 46, -83, -39, 125 }, { 115, -15, -42, 111, -124, 52, 29, -124, -10, -23, 41, -128, 65, -60, -121, 6, -42, 14, 98, -80, 80, -46, -38, 64, 16, 84, -50, 47, -97, 11, -88, 12, 68, -127, -92, 87, -22, 54, -49, 33, -4, -68, 21, -7, -45, 84, 107, 57, 8, -106, 0, -87, -104, 93, -43, -98, -92, -72, 110, -14, -66, 119, 14, -68 }, { -19, 7, 7, 66, -94, 18, 36, 8, -58, -31, 21, -113, -124, -5, -12, 105, 40, -62, 57, -56, 25, 117, 49, 17, -33, 49, 105, 113, -26, 78, 97, 2, -22, -84, 49, 67, -6, 33, 89, 28, 30, 12, -3, -23, -45, 7, -4, -39, -20, 25, -91, 55, 53, 21, -94, 17, -54, 109, 125, 124, 122, 117, -125, 60 }, { -28, -104, -46, -22, 71, -79, 100, 48, -90, -57, -30, -23, -24, 1, 2, -31, 85, -6, -113, -116, 105, -31, -109, 106, 1, 78, -3, 103, -6, 100, -44, 15, -100, 97, 59, -42, 22, 83, 113, -118, 112, -57, 80, -45, -86, 72, 77, -26, -106, 50, 28, -24, 41, 22, -73, 108, 18, -93, 30, 8, -11, -16, 124, 106 }, { 16, -119, -109, 115, 67, 36, 28, 74, 101, -58, -82, 91, 4, -97, 111, -77, -37, -125, 126, 3, 10, -99, -115, 91, -66, -83, -81, 10, 7, 92, 26, 6, -45, 66, -26, 118, -77, 13, -91, 20, -18, -33, -103, 43, 75, -100, -5, -64, 117, 30, 5, -100, -90, 13, 18, -52, 26, 24, -10, 24, -31, 53, 88, 112 }, { 7, -90, 46, 109, -42, 108, -84, 124, -28, -63, 34, -19, -76, 88, -121, 23, 54, -73, -15, -52, 84, -119, 64, 20, 92, -91, -58, -121, -117, -90, -102, 1, 49, 21, 3, -85, -3, 38, 117, 73, -38, -71, -37, 40, -2, -50, -47, -46, 75, -105, 125, 126, -13, 68, 50, -81, -43, -93, 85, -79, 52, 98, 118, 50 }, { -104, 65, -61, 12, 68, 106, 37, -64, 40, -114, 61, 73, 74, 61, -113, -79, 57, 47, -57, -21, -68, -62, 23, -18, 93, -7, -55, -88, -106, 104, -126, 5, 53, 97, 100, -67, -112, -88, 41, 24, 95, 15, 25, -67, 79, -69, 53, 21, -128, -101, 73, 17, 7, -98, 5, -2, 33, -113, 99, -72, 125, 7, 18, -105 }, { -17, 28, 79, 34, 110, 86, 43, 27, -114, -112, -126, -98, -121, 126, -21, 111, 58, -114, -123, 75, 117, -116, 7, 107, 90, 80, -75, -121, 116, -11, -76, 0, -117, -52, 76, -116, 115, -117, 61, -7, 55, -34, 38, 101, 86, -19, -36, -92, -94, 61, 88, -128, -121, -103, 84, 19, -83, -102, 122, -111, 62, 112, 20, 3 }, { -127, -90, 28, -77, -48, -56, -10, 84, -41, 59, -115, 68, -74, -104, -119, -49, -37, -90, -57, 66, 108, 110, -62, -107, 88, 90, 29, -65, 74, -38, 95, 8, 120, 88, 96, -65, -109, 68, -63, -4, -16, 90, 7, 39, -56, -110, -100, 86, -39, -53, -89, -35, 127, -42, -48, -36, 53, -66, 109, -51, 51, -23, -12, 73 }, { -12, 78, 81, 30, 124, 22, 56, -112, 58, -99, 30, -98, 103, 66, 89, 92, -52, -20, 26, 82, -92, -18, 96, 7, 38, 21, -9, -25, -17, 4, 43, 15, 111, 103, -48, -50, -83, 52, 59, 103, 102, 83, -105, 87, 20, -120, 35, -7, -39, -24, 29, -39, -35, -87, 88, 120, 126, 19, 108, 34, -59, -20, 86, 47 }, { 19, -70, 36, 55, -42, -49, 33, 100, 105, -5, 89, 43, 3, -85, 60, -96, 43, -46, 86, -33, 120, -123, -99, -100, -34, 48, 82, -37, 34, 78, 127, 12, -39, -76, -26, 117, 74, -60, -68, -2, -37, -56, -6, 94, -27, 81, 32, -96, -19, -32, -77, 22, -56, -49, -38, -60, 45, -69, 40, 106, -106, -34, 101, -75 }, { 57, -92, -44, 8, -79, -88, -82, 58, -116, 93, 103, -127, 87, -121, -28, 31, -108, -14, -23, 38, 57, -83, -33, -110, 24, 6, 68, 124, -89, -35, -127, 5, -118, -78, -127, -35, 112, -34, 30, 24, -70, -71, 126, 39, -124, 122, -35, -97, -18, 25, 119, 79, 119, -65, 59, -20, -84, 120, -47, 4, -106, -125, -38, -113 }, { 18, -93, 34, -80, -43, 127, 57, -118, 24, -119, 25, 71, 59, -29, -108, -99, -122, 58, 44, 0, 42, -111, 25, 94, -36, 41, -64, -53, -78, -119, 85, 7, -45, -70, 81, -84, 71, -61, -68, -79, 112, 117, 19, 18, 70, 95, 108, -58, 48, 116, -89, 43, 66, 55, 37, -37, -60, 104, 47, -19, -56, 97, 73, 26 }, { 78, 4, -111, -36, 120, 111, -64, 46, 99, 125, -5, 97, -126, -21, 60, -78, -33, 89, 25, -60, 0, -49, 59, -118, 18, 3, -60, 30, 105, -92, -101, 15, 63, 50, 25, 2, -116, 78, -5, -25, -59, 74, -116, 64, -55, -121, 1, 69, 51, -119, 43, -6, -81, 14, 5, 84, -67, -73, 67, 24, 82, -37, 109, -93 }, { -44, -30, -64, -68, -21, 74, 124, 122, 114, -89, -91, -51, 89, 32, 96, -1, -101, -112, -94, 98, -24, -31, -50, 100, -72, 56, 24, 30, 105, 115, 15, 3, -67, 107, -18, 111, -38, -93, -11, 24, 36, 73, -23, 108, 14, -41, -65, 32, 51, 22, 95, 41, 85, -121, -35, -107, 0, 105, -112, 59, 48, -22, -84, 46 }, { 4, 38, 54, -84, -78, 24, -48, 8, -117, 78, -95, 24, 25, -32, -61, 26, -97, -74, 46, -120, -125, 27, 73, 107, -17, -21, -6, -52, 47, -68, 66, 5, -62, -12, -102, -127, 48, -69, -91, -81, -33, -13, -9, -12, -44, -73, 40, -58, 120, -120, 108, 101, 18, -14, -17, -93, 113, 49, 76, -4, -113, -91, -93, -52 }, { 28, -48, 70, -35, 123, -31, 16, -52, 72, 84, -51, 78, 104, 59, -102, -112, 29, 28, 25, 66, 12, 75, 26, -85, 56, -12, -4, -92, 49, 86, -27, 12, 44, -63, 108, 82, -76, -97, -41, 95, -48, -95, -115, 1, 64, -49, -97, 90, 65, 46, -114, -127, -92, 79, 100, 49, 116, -58, -106, 9, 117, -7, -91, 96 }, { 58, 26, 18, 76, 127, -77, -58, -87, -116, -44, 60, -32, -4, -76, -124, 4, -60, 82, -5, -100, -95, 18, 2, -53, -50, -96, -126, 105, 93, 19, 74, 13, 87, 125, -72, -10, 42, 14, 91, 44, 78, 52, 60, -59, -27, -37, -57, 17, -85, 31, -46, 113, 100, -117, 15, 108, -42, 12, 47, 63, 1, 11, -122, -3 } };
        Nxt.Transaction localTransaction;
        for (int i2 = 0; i2 < localObject4.length; i2++)
        {
          localTransaction = new Nxt.Transaction((byte)0, (byte)0, 0, (short)0, new byte[] { 18, 89, -20, 33, -45, 26, 48, -119, -115, 124, -47, 96, -97, -128, -39, 102, -117, 71, 120, -29, -39, 126, -108, 16, 68, -77, -97, 12, 68, -46, -27, 27 }, localObject4[i2], localObject5[i2], 0, 0L, localObject6[i2]);
          transactions.put(Long.valueOf(localTransaction.getId()), localTransaction);
        }
        Iterator localIterator = transactions.values().iterator();
        while (localIterator.hasNext())
        {
          localTransaction = (Nxt.Transaction)localIterator.next();
          localTransaction.index = transactionCounter.incrementAndGet();
          localTransaction.block = 2680262203532249785L;
        }
        Nxt.Transaction.saveTransactions("transactions.nxt");
      }
      long l2;
      try
      {
        logMessage("Loading blocks...");
        Nxt.Block.loadBlocks("blocks.nxt");
        logMessage("...Done");
      }
      catch (FileNotFoundException localFileNotFoundException2)
      {
        blocks.clear();
        localObject4 = new Nxt.Block(-1, 0, 0L, transactions.size(), 1000000000, 0, transactions.size() * 128, null, new byte[] { 18, 89, -20, 33, -45, 26, 48, -119, -115, 124, -47, 96, -97, -128, -39, 102, -117, 71, 120, -29, -39, 126, -108, 16, 68, -77, -97, 12, 68, -46, -27, 27 }, new byte[64], new byte[] { 105, -44, 38, -60, -104, -73, 10, -58, -47, 103, -127, -128, 53, 101, 39, -63, -2, -32, 48, -83, 115, 47, -65, 118, 114, -62, 38, 109, 22, 106, 76, 8, -49, -113, -34, -76, 82, 79, -47, -76, -106, -69, -54, -85, 3, -6, 110, 103, 118, 15, 109, -92, 82, 37, 20, 2, 36, -112, 21, 72, 108, 72, 114, 17 });
        ((Nxt.Block)localObject4).index = blockCounter.incrementAndGet();
        blocks.put(Long.valueOf(2680262203532249785L), localObject4);
        int i1 = 0;
        localObject6 = transactions.keySet().iterator();
        while (((Iterator)localObject6).hasNext())
        {
          l2 = ((Long)((Iterator)localObject6).next()).longValue();
          ((Nxt.Block)localObject4).transactions[(i1++)] = l2;
        }
        Arrays.sort(((Nxt.Block)localObject4).transactions);
        localObject6 = MessageDigest.getInstance("SHA-256");
        for (i1 = 0; i1 < ((Nxt.Block)localObject4).numberOfTransactions; i1++) {
          ((MessageDigest)localObject6).update(((Nxt.Transaction)transactions.get(Long.valueOf(localObject4.transactions[i1]))).getBytes());
        }
        ((Nxt.Block)localObject4).payloadHash = ((MessageDigest)localObject6).digest();
        ((Nxt.Block)localObject4).baseTarget = 153722867L;
        lastBlock = 2680262203532249785L;
        ((Nxt.Block)localObject4).cumulativeDifficulty = BigInteger.ZERO;
        Nxt.Block.saveBlocks("blocks.nxt", false);
      }
      logMessage("Scanning blockchain...");
      HashMap localHashMap = new HashMap(blocks);
      blocks.clear();
      lastBlock = 2680262203532249785L;
      long l1 = 2680262203532249785L;
      do
      {
        localObject6 = (Nxt.Block)localHashMap.get(Long.valueOf(l1));
        l2 = ((Nxt.Block)localObject6).nextBlock;
        ((Nxt.Block)localObject6).analyze();
        l1 = l2;
      } while (l1 != 0L);
      logMessage("...Done");
      scheduledThreadPool.scheduleWithFixedDelay(new Nxt.2(this), 0L, 5L, TimeUnit.SECONDS);
      scheduledThreadPool.scheduleWithFixedDelay(new Nxt.3(this), 0L, 1L, TimeUnit.SECONDS);
      scheduledThreadPool.scheduleWithFixedDelay(new Nxt.4(this), 0L, 5L, TimeUnit.SECONDS);
      scheduledThreadPool.scheduleWithFixedDelay(new Nxt.5(this), 0L, 5L, TimeUnit.SECONDS);
      scheduledThreadPool.scheduleWithFixedDelay(new Nxt.6(this), 0L, 1L, TimeUnit.SECONDS);
      scheduledThreadPool.scheduleWithFixedDelay(new Nxt.7(this), 0L, 1L, TimeUnit.SECONDS);
      scheduledThreadPool.scheduleWithFixedDelay(new Nxt.8(this), 0L, 1L, TimeUnit.SECONDS);
    }
    catch (Exception localException1)
    {
      logMessage("10: " + localException1.toString());
    }
  }
  
  public void doGet(HttpServletRequest paramHttpServletRequest, HttpServletResponse paramHttpServletResponse)
    throws ServletException, IOException
  {
    paramHttpServletResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, private");
    paramHttpServletResponse.setHeader("Pragma", "no-cache");
    paramHttpServletResponse.setDateHeader("Expires", 0L);
    Object localObject1 = null;
    Object localObject2;
    Object localObject7;
    Object localObject6;
    Object localObject5;
    try
    {
      String str1 = paramHttpServletRequest.getParameter("user");
      Object localObject3;
      Object localObject4;
      Object localObject33;
      Object localObject29;
      Object localObject16;
      if (str1 == null)
      {
        localObject2 = new JSONObject();
        if ((allowedBotHosts != null) && (!allowedBotHosts.contains(paramHttpServletRequest.getRemoteHost())))
        {
          ((JSONObject)localObject2).put("errorCode", Integer.valueOf(7));
          ((JSONObject)localObject2).put("errorDescription", "Not allowed");
        }
        else
        {
          localObject3 = paramHttpServletRequest.getParameter("requestType");
          if (localObject3 == null)
          {
            ((JSONObject)localObject2).put("errorCode", Integer.valueOf(1));
            ((JSONObject)localObject2).put("errorDescription", "Incorrect request");
          }
          else
          {
            localObject4 = localObject3;
            int n = -1;
            switch (((String)localObject4).hashCode())
            {
            case 1708941985: 
              if (((String)localObject4).equals("assignAlias")) {
                n = 0;
              }
              break;
            case 62209885: 
              if (((String)localObject4).equals("broadcastTransaction")) {
                n = 1;
              }
              break;
            case -907924844: 
              if (((String)localObject4).equals("decodeHallmark")) {
                n = 2;
              }
              break;
            case 1183136939: 
              if (((String)localObject4).equals("decodeToken")) {
                n = 3;
              }
              break;
            case -1836634766: 
              if (((String)localObject4).equals("getAccountId")) {
                n = 4;
              }
              break;
            case -1594290433: 
              if (((String)localObject4).equals("getAccountPublicKey")) {
                n = 5;
              }
              break;
            case -1415951151: 
              if (((String)localObject4).equals("getAccountTransactionIds")) {
                n = 6;
              }
              break;
            case 1948728474: 
              if (((String)localObject4).equals("getAlias")) {
                n = 7;
              }
              break;
            case -502897730: 
              if (((String)localObject4).equals("getAliasIds")) {
                n = 8;
              }
              break;
            case -502886798: 
              if (((String)localObject4).equals("getAliasURI")) {
                n = 9;
              }
              break;
            case 697674406: 
              if (((String)localObject4).equals("getBalance")) {
                n = 10;
              }
              break;
            case 1949657815: 
              if (((String)localObject4).equals("getBlock")) {
                n = 11;
              }
              break;
            case -431881575: 
              if (((String)localObject4).equals("getConstants")) {
                n = 12;
              }
              break;
            case 635655024: 
              if (((String)localObject4).equals("getMyInfo")) {
                n = 13;
              }
              break;
            case -75245096: 
              if (((String)localObject4).equals("getPeer")) {
                n = 14;
              }
              break;
            case 1962369435: 
              if (((String)localObject4).equals("getPeers")) {
                n = 15;
              }
              break;
            case 1965583067: 
              if (((String)localObject4).equals("getState")) {
                n = 16;
              }
              break;
            case -75121853: 
              if (((String)localObject4).equals("getTime")) {
                n = 17;
              }
              break;
            case 1500977576: 
              if (((String)localObject4).equals("getTransaction")) {
                n = 18;
              }
              break;
            case -996573277: 
              if (((String)localObject4).equals("getTransactionBytes")) {
                n = 19;
              }
              break;
            case -1835768118: 
              if (((String)localObject4).equals("getUnconfirmedTransactionIds")) {
                n = 20;
              }
              break;
            case -944172977: 
              if (((String)localObject4).equals("listAccountAliases")) {
                n = 21;
              }
              break;
            case 246104597: 
              if (((String)localObject4).equals("markHost")) {
                n = 22;
              }
              break;
            case 9950744: 
              if (((String)localObject4).equals("sendMoney")) {
                n = 23;
              }
              break;
            }
            Object localObject8;
            Object localObject13;
            Object localObject14;
            Object localObject18;
            Object localObject21;
            String str6;
            int i13;
            Nxt.Account localAccount;
            Object localObject34;
            Object localObject35;
            int i5;
            byte[] arrayOfByte2;
            int i20;
            Object localObject9;
            Object localObject25;
            Object localObject19;
            Object localObject22;
            Object localObject28;
            Object localObject15;
            Object localObject10;
            Object localObject11;
            String str3;
            switch (n)
            {
            case 0: 
              localObject7 = paramHttpServletRequest.getParameter("secretPhrase");
              localObject8 = paramHttpServletRequest.getParameter("alias");
              localObject13 = paramHttpServletRequest.getParameter("uri");
              localObject14 = paramHttpServletRequest.getParameter("fee");
              localObject18 = paramHttpServletRequest.getParameter("deadline");
              localObject21 = paramHttpServletRequest.getParameter("referencedTransaction");
              if (localObject7 == null)
              {
                ((JSONObject)localObject2).put("errorCode", Integer.valueOf(3));
                ((JSONObject)localObject2).put("errorDescription", "\"secretPhrase\" not specified");
              }
              else if (localObject8 == null)
              {
                ((JSONObject)localObject2).put("errorCode", Integer.valueOf(3));
                ((JSONObject)localObject2).put("errorDescription", "\"alias\" not specified");
              }
              else if (localObject13 == null)
              {
                ((JSONObject)localObject2).put("errorCode", Integer.valueOf(3));
                ((JSONObject)localObject2).put("errorDescription", "\"uri\" not specified");
              }
              else if (localObject14 == null)
              {
                ((JSONObject)localObject2).put("errorCode", Integer.valueOf(3));
                ((JSONObject)localObject2).put("errorDescription", "\"fee\" not specified");
              }
              else if (localObject18 == null)
              {
                ((JSONObject)localObject2).put("errorCode", Integer.valueOf(3));
                ((JSONObject)localObject2).put("errorDescription", "\"deadline\" not specified");
              }
              else
              {
                localObject8 = ((String)localObject8).trim();
                if ((((String)localObject8).length() == 0) || (((String)localObject8).length() > 100))
                {
                  ((JSONObject)localObject2).put("errorCode", Integer.valueOf(4));
                  ((JSONObject)localObject2).put("errorDescription", "Incorrect \"alias\" (length must be in [1..100] range)");
                }
                else
                {
                  str6 = ((String)localObject8).toLowerCase();
                  for (i13 = 0; (i13 < str6.length()) && ("0123456789abcdefghijklmnopqrstuvwxyz".indexOf(str6.charAt(i13)) >= 0); i13++) {}
                  if (i13 != str6.length())
                  {
                    ((JSONObject)localObject2).put("errorCode", Integer.valueOf(4));
                    ((JSONObject)localObject2).put("errorDescription", "Incorrect \"alias\" (must contain only digits and latin letters)");
                  }
                  else
                  {
                    localObject13 = ((String)localObject13).trim();
                    if (((String)localObject13).length() > 1000)
                    {
                      ((JSONObject)localObject2).put("errorCode", Integer.valueOf(4));
                      ((JSONObject)localObject2).put("errorDescription", "Incorrect \"uri\" (length must be not longer than 1000 characters)");
                    }
                    else
                    {
                      try
                      {
                        int i15 = Integer.parseInt((String)localObject14);
                        if ((i15 <= 0) || (i15 >= 1000000000L)) {
                          throw new Exception();
                        }
                        try
                        {
                          short s2 = Short.parseShort((String)localObject18);
                          if (s2 < 1) {
                            throw new Exception();
                          }
                          long l11 = localObject21 == null ? 0L : new BigInteger((String)localObject21).longValue();
                          byte[] arrayOfByte4 = Nxt.Crypto.getPublicKey((String)localObject7);
                          long l13 = Nxt.Account.getId(arrayOfByte4);
                          localAccount = (Nxt.Account)accounts.get(Long.valueOf(l13));
                          if (localAccount == null)
                          {
                            ((JSONObject)localObject2).put("errorCode", Integer.valueOf(6));
                            ((JSONObject)localObject2).put("errorDescription", "Not enough funds");
                          }
                          else if (i15 * 100L > localAccount.getUnconfirmedBalance())
                          {
                            ((JSONObject)localObject2).put("errorCode", Integer.valueOf(6));
                            ((JSONObject)localObject2).put("errorDescription", "Not enough funds");
                          }
                          else
                          {
                            localObject34 = (Nxt.Alias)aliases.get(str6);
                            if ((localObject34 != null) && (((Nxt.Alias)localObject34).account != localAccount))
                            {
                              ((JSONObject)localObject2).put("errorCode", Integer.valueOf(8));
                              ((JSONObject)localObject2).put("errorDescription", "\"" + (String)localObject8 + "\" is already used");
                            }
                            else
                            {
                              int i22 = getEpochTime(System.currentTimeMillis());
                              localObject35 = new Nxt.Transaction((byte)1, (byte)1, i22, s2, arrayOfByte4, 1739068987193023818L, 0, i15, l11, new byte[64]);
                              ((Nxt.Transaction)localObject35).attachment = new Nxt.Transaction.MessagingAliasAssignmentAttachment((String)localObject8, (String)localObject13);
                              ((Nxt.Transaction)localObject35).sign((String)localObject7);
                              JSONObject localJSONObject8 = new JSONObject();
                              localJSONObject8.put("requestType", "processTransactions");
                              JSONArray localJSONArray3 = new JSONArray();
                              localJSONArray3.add(((Nxt.Transaction)localObject35).getJSONObject());
                              localJSONObject8.put("transactions", localJSONArray3);
                              Nxt.Peer.sendToAllPeers(localJSONObject8);
                              ((JSONObject)localObject2).put("transaction", convert(((Nxt.Transaction)localObject35).getId()));
                            }
                          }
                        }
                        catch (Exception localException20)
                        {
                          ((JSONObject)localObject2).put("errorCode", Integer.valueOf(4));
                          ((JSONObject)localObject2).put("errorDescription", "Incorrect \"deadline\"");
                        }
                      }
                      catch (Exception localException18)
                      {
                        ((JSONObject)localObject2).put("errorCode", Integer.valueOf(4));
                        ((JSONObject)localObject2).put("errorDescription", "Incorrect \"fee\"");
                      }
                    }
                  }
                }
              }
              break;
            case 1: 
              localObject7 = paramHttpServletRequest.getParameter("transactionBytes");
              if (localObject7 == null)
              {
                ((JSONObject)localObject2).put("errorCode", Integer.valueOf(3));
                ((JSONObject)localObject2).put("errorDescription", "\"transactionBytes\" not specified");
              }
              else
              {
                try
                {
                  localObject8 = ByteBuffer.wrap(convert((String)localObject7));
                  ((ByteBuffer)localObject8).order(ByteOrder.LITTLE_ENDIAN);
                  localObject13 = Nxt.Transaction.getTransaction((ByteBuffer)localObject8);
                  localObject14 = new JSONObject();
                  ((JSONObject)localObject14).put("requestType", "processTransactions");
                  localObject18 = new JSONArray();
                  ((JSONArray)localObject18).add(((Nxt.Transaction)localObject13).getJSONObject());
                  ((JSONObject)localObject14).put("transactions", localObject18);
                  Nxt.Peer.sendToAllPeers((JSONObject)localObject14);
                  ((JSONObject)localObject2).put("transaction", convert(((Nxt.Transaction)localObject13).getId()));
                }
                catch (Exception localException2)
                {
                  ((JSONObject)localObject2).put("errorCode", Integer.valueOf(4));
                  ((JSONObject)localObject2).put("errorDescription", "Incorrect \"transactionBytes\"");
                }
              }
              break;
            case 2: 
              localObject7 = paramHttpServletRequest.getParameter("hallmark");
              if (localObject7 == null)
              {
                ((JSONObject)localObject2).put("errorCode", Integer.valueOf(3));
                ((JSONObject)localObject2).put("errorDescription", "\"hallmark\" not specified");
              }
              else
              {
                try
                {
                  byte[] arrayOfByte1 = convert((String)localObject7);
                  localObject13 = ByteBuffer.wrap(arrayOfByte1);
                  ((ByteBuffer)localObject13).order(ByteOrder.LITTLE_ENDIAN);
                  localObject14 = new byte[32];
                  ((ByteBuffer)localObject13).get((byte[])localObject14);
                  i5 = ((ByteBuffer)localObject13).getShort();
                  localObject21 = new byte[i5];
                  ((ByteBuffer)localObject13).get((byte[])localObject21);
                  str6 = new String((byte[])localObject21, "UTF-8");
                  i13 = ((ByteBuffer)localObject13).getInt();
                  int i16 = ((ByteBuffer)localObject13).getInt();
                  ((ByteBuffer)localObject13).get();
                  arrayOfByte2 = new byte[64];
                  ((ByteBuffer)localObject13).get(arrayOfByte2);
                  ((JSONObject)localObject2).put("account", convert(Nxt.Account.getId((byte[])localObject14)));
                  ((JSONObject)localObject2).put("host", str6);
                  ((JSONObject)localObject2).put("weight", Integer.valueOf(i13));
                  int i18 = i16 / 10000;
                  i20 = i16 % 10000 / 100;
                  int i21 = i16 % 100;
                  ((JSONObject)localObject2).put("date", (i18 < 1000 ? "0" : i18 < 100 ? "00" : i18 < 10 ? "000" : "") + i18 + "-" + (i20 < 10 ? "0" : "") + i20 + "-" + (i21 < 10 ? "0" : "") + i21);
                  localObject33 = new byte[arrayOfByte1.length - 64];
                  System.arraycopy(arrayOfByte1, 0, localObject33, 0, localObject33.length);
                  ((JSONObject)localObject2).put("valid", Boolean.valueOf((str6.length() > 100) || (i13 <= 0) || (i13 > 1000000000L) ? false : Nxt.Crypto.verify(arrayOfByte2, (byte[])localObject33, (byte[])localObject14)));
                }
                catch (Exception localException3)
                {
                  ((JSONObject)localObject2).put("errorCode", Integer.valueOf(4));
                  ((JSONObject)localObject2).put("errorDescription", "Incorrect \"hallmark\"");
                }
              }
              break;
            case 3: 
              localObject7 = paramHttpServletRequest.getParameter("website");
              localObject9 = paramHttpServletRequest.getParameter("token");
              if (localObject7 == null)
              {
                ((JSONObject)localObject2).put("errorCode", Integer.valueOf(3));
                ((JSONObject)localObject2).put("errorDescription", "\"website\" not specified");
              }
              else if (localObject9 == null)
              {
                ((JSONObject)localObject2).put("errorCode", Integer.valueOf(3));
                ((JSONObject)localObject2).put("errorDescription", "\"token\" not specified");
              }
              else
              {
                localObject13 = ((String)localObject7).trim().getBytes("UTF-8");
                localObject14 = new byte[100];
                i5 = 0;
                int i8 = 0;
                try
                {
                  while (i5 < ((String)localObject9).length())
                  {
                    long l9 = Long.parseLong(((String)localObject9).substring(i5, i5 + 8), 32);
                    localObject14[i8] = ((byte)(int)l9);
                    localObject14[(i8 + 1)] = ((byte)(int)(l9 >> 8));
                    localObject14[(i8 + 2)] = ((byte)(int)(l9 >> 16));
                    localObject14[(i8 + 3)] = ((byte)(int)(l9 >> 24));
                    localObject14[(i8 + 4)] = ((byte)(int)(l9 >> 32));
                    i5 += 8;
                    i8 += 5;
                  }
                }
                catch (Exception localException16) {}
                if (i5 != 160)
                {
                  ((JSONObject)localObject2).put("errorCode", Integer.valueOf(4));
                  ((JSONObject)localObject2).put("errorDescription", "Incorrect \"token\"");
                }
                else
                {
                  localObject25 = new byte[32];
                  System.arraycopy(localObject14, 0, localObject25, 0, 32);
                  i13 = localObject14[32] & 0xFF | (localObject14[33] & 0xFF) << 8 | (localObject14[34] & 0xFF) << 16 | (localObject14[35] & 0xFF) << 24;
                  localObject29 = new byte[64];
                  System.arraycopy(localObject14, 36, localObject29, 0, 64);
                  arrayOfByte2 = new byte[localObject13.length + 36];
                  System.arraycopy(localObject13, 0, arrayOfByte2, 0, localObject13.length);
                  System.arraycopy(localObject14, 0, arrayOfByte2, localObject13.length, 36);
                  boolean bool = Nxt.Crypto.verify((byte[])localObject29, arrayOfByte2, (byte[])localObject25);
                  ((JSONObject)localObject2).put("account", convert(Nxt.Account.getId((byte[])localObject25)));
                  ((JSONObject)localObject2).put("timestamp", Integer.valueOf(i13));
                  ((JSONObject)localObject2).put("valid", Boolean.valueOf(bool));
                }
              }
              break;
            case 4: 
              localObject7 = paramHttpServletRequest.getParameter("secretPhrase");
              if (localObject7 == null)
              {
                ((JSONObject)localObject2).put("errorCode", Integer.valueOf(3));
                ((JSONObject)localObject2).put("errorDescription", "\"secretPhrase\" not specified");
              }
              else
              {
                localObject9 = MessageDigest.getInstance("SHA-256").digest(Nxt.Crypto.getPublicKey((String)localObject7));
                localObject13 = new BigInteger(1, new byte[] { localObject9[7], localObject9[6], localObject9[5], localObject9[4], localObject9[3], localObject9[2], localObject9[1], localObject9[0] });
                ((JSONObject)localObject2).put("accountId", ((BigInteger)localObject13).toString());
              }
              break;
            case 5: 
              localObject7 = paramHttpServletRequest.getParameter("account");
              if (localObject7 == null)
              {
                ((JSONObject)localObject2).put("errorCode", Integer.valueOf(3));
                ((JSONObject)localObject2).put("errorDescription", "\"account\" not specified");
              }
              else
              {
                try
                {
                  long l1 = new BigInteger((String)localObject7).longValue();
                  localObject14 = (Nxt.Account)accounts.get(Long.valueOf(l1));
                  if (localObject14 == null)
                  {
                    ((JSONObject)localObject2).put("errorCode", Integer.valueOf(5));
                    ((JSONObject)localObject2).put("errorDescription", "Unknown account");
                  }
                  else if (((Nxt.Account)localObject14).publicKey.get() != null)
                  {
                    ((JSONObject)localObject2).put("publicKey", convert((byte[])((Nxt.Account)localObject14).publicKey.get()));
                  }
                }
                catch (Exception localException4)
                {
                  ((JSONObject)localObject2).put("errorCode", Integer.valueOf(4));
                  ((JSONObject)localObject2).put("errorDescription", "Incorrect \"account\"");
                }
              }
              break;
            case 6: 
              localObject7 = paramHttpServletRequest.getParameter("account");
              String str2 = paramHttpServletRequest.getParameter("timestamp");
              if (localObject7 == null)
              {
                ((JSONObject)localObject2).put("errorCode", Integer.valueOf(3));
                ((JSONObject)localObject2).put("errorDescription", "\"account\" not specified");
              }
              else if (str2 == null)
              {
                ((JSONObject)localObject2).put("errorCode", Integer.valueOf(3));
                ((JSONObject)localObject2).put("errorDescription", "\"timestamp\" not specified");
              }
              else
              {
                try
                {
                  localObject13 = (Nxt.Account)accounts.get(Long.valueOf(new BigInteger((String)localObject7).longValue()));
                  if (localObject13 == null)
                  {
                    ((JSONObject)localObject2).put("errorCode", Integer.valueOf(5));
                    ((JSONObject)localObject2).put("errorDescription", "Unknown account");
                  }
                  else
                  {
                    try
                    {
                      int i3 = Integer.parseInt(str2);
                      if (i3 < 0) {
                        throw new Exception();
                      }
                      localObject19 = new JSONArray();
                      localObject22 = transactions.entrySet().iterator();
                      while (((Iterator)localObject22).hasNext())
                      {
                        localObject25 = (Map.Entry)((Iterator)localObject22).next();
                        localObject28 = (Nxt.Transaction)((Map.Entry)localObject25).getValue();
                        if ((((Nxt.Block)blocks.get(Long.valueOf(((Nxt.Transaction)localObject28).block))).timestamp >= i3) && ((Nxt.Account.getId(((Nxt.Transaction)localObject28).senderPublicKey) == ((Nxt.Account)localObject13).id) || (((Nxt.Transaction)localObject28).recipient == ((Nxt.Account)localObject13).id))) {
                          ((JSONArray)localObject19).add(convert(((Long)((Map.Entry)localObject25).getKey()).longValue()));
                        }
                      }
                      ((JSONObject)localObject2).put("transactionIds", localObject19);
                    }
                    catch (Exception localException13)
                    {
                      ((JSONObject)localObject2).put("errorCode", Integer.valueOf(4));
                      ((JSONObject)localObject2).put("errorDescription", "Incorrect \"timestamp\"");
                    }
                  }
                }
                catch (Exception localException12)
                {
                  ((JSONObject)localObject2).put("errorCode", Integer.valueOf(4));
                  ((JSONObject)localObject2).put("errorDescription", "Incorrect \"account\"");
                }
              }
              break;
            case 7: 
              localObject7 = paramHttpServletRequest.getParameter("alias");
              if (localObject7 == null)
              {
                ((JSONObject)localObject2).put("errorCode", Integer.valueOf(3));
                ((JSONObject)localObject2).put("errorDescription", "\"alias\" not specified");
              }
              else
              {
                try
                {
                  long l2 = new BigInteger((String)localObject7).longValue();
                  localObject15 = (Nxt.Alias)aliasIdToAliasMappings.get(Long.valueOf(l2));
                  if (localObject15 == null)
                  {
                    ((JSONObject)localObject2).put("errorCode", Integer.valueOf(5));
                    ((JSONObject)localObject2).put("errorDescription", "Unknown alias");
                  }
                  else
                  {
                    ((JSONObject)localObject2).put("account", convert(((Nxt.Alias)localObject15).account.id));
                    ((JSONObject)localObject2).put("alias", ((Nxt.Alias)localObject15).alias);
                    if (((Nxt.Alias)localObject15).uri.length() > 0) {
                      ((JSONObject)localObject2).put("uri", ((Nxt.Alias)localObject15).uri);
                    }
                    ((JSONObject)localObject2).put("timestamp", Integer.valueOf(((Nxt.Alias)localObject15).timestamp));
                  }
                }
                catch (Exception localException5)
                {
                  ((JSONObject)localObject2).put("errorCode", Integer.valueOf(4));
                  ((JSONObject)localObject2).put("errorDescription", "Incorrect \"alias\"");
                }
              }
              break;
            case 8: 
              localObject7 = paramHttpServletRequest.getParameter("timestamp");
              if (localObject7 == null)
              {
                ((JSONObject)localObject2).put("errorCode", Integer.valueOf(3));
                ((JSONObject)localObject2).put("errorDescription", "\"timestamp\" not specified");
              }
              else
              {
                try
                {
                  int i1 = Integer.parseInt((String)localObject7);
                  if (i1 < 0) {
                    throw new Exception();
                  }
                  JSONArray localJSONArray1 = new JSONArray();
                  localObject15 = aliasIdToAliasMappings.entrySet().iterator();
                  while (((Iterator)localObject15).hasNext())
                  {
                    localObject19 = (Map.Entry)((Iterator)localObject15).next();
                    if (((Nxt.Alias)((Map.Entry)localObject19).getValue()).timestamp >= i1) {
                      localJSONArray1.add(convert(((Long)((Map.Entry)localObject19).getKey()).longValue()));
                    }
                  }
                  ((JSONObject)localObject2).put("aliasIds", localJSONArray1);
                }
                catch (Exception localException6)
                {
                  ((JSONObject)localObject2).put("errorCode", Integer.valueOf(4));
                  ((JSONObject)localObject2).put("errorDescription", "Incorrect \"timestamp\"");
                }
              }
              break;
            case 9: 
              localObject7 = paramHttpServletRequest.getParameter("alias");
              if (localObject7 == null)
              {
                ((JSONObject)localObject2).put("errorCode", Integer.valueOf(3));
                ((JSONObject)localObject2).put("errorDescription", "\"alias\" not specified");
              }
              else
              {
                localObject10 = (Nxt.Alias)aliases.get(((String)localObject7).toLowerCase());
                if (localObject10 == null)
                {
                  ((JSONObject)localObject2).put("errorCode", Integer.valueOf(5));
                  ((JSONObject)localObject2).put("errorDescription", "Unknown alias");
                }
                else if (((Nxt.Alias)localObject10).uri.length() > 0)
                {
                  ((JSONObject)localObject2).put("uri", ((Nxt.Alias)localObject10).uri);
                }
              }
              break;
            case 10: 
              localObject7 = paramHttpServletRequest.getParameter("account");
              if (localObject7 == null)
              {
                ((JSONObject)localObject2).put("errorCode", Integer.valueOf(3));
                ((JSONObject)localObject2).put("errorDescription", "\"account\" not specified");
              }
              else
              {
                try
                {
                  localObject10 = (Nxt.Account)accounts.get(Long.valueOf(new BigInteger((String)localObject7).longValue()));
                  if (localObject10 == null)
                  {
                    ((JSONObject)localObject2).put("balance", Integer.valueOf(0));
                    ((JSONObject)localObject2).put("unconfirmedBalance", Integer.valueOf(0));
                    ((JSONObject)localObject2).put("effectiveBalance", Integer.valueOf(0));
                  }
                  else
                  {
                    synchronized (localObject10)
                    {
                      ((JSONObject)localObject2).put("balance", Long.valueOf(((Nxt.Account)localObject10).getBalance()));
                      ((JSONObject)localObject2).put("unconfirmedBalance", Long.valueOf(((Nxt.Account)localObject10).getUnconfirmedBalance()));
                      ((JSONObject)localObject2).put("effectiveBalance", Long.valueOf(((Nxt.Account)localObject10).getEffectiveBalance() * 100L));
                    }
                  }
                }
                catch (Exception localException7)
                {
                  ((JSONObject)localObject2).put("errorCode", Integer.valueOf(4));
                  ((JSONObject)localObject2).put("errorDescription", "Incorrect \"account\"");
                }
              }
              break;
            case 11: 
              localObject7 = paramHttpServletRequest.getParameter("block");
              if (localObject7 == null)
              {
                ((JSONObject)localObject2).put("errorCode", Integer.valueOf(3));
                ((JSONObject)localObject2).put("errorDescription", "\"block\" not specified");
              }
              else
              {
                try
                {
                  Nxt.Block localBlock = (Nxt.Block)blocks.get(Long.valueOf(new BigInteger((String)localObject7).longValue()));
                  if (localBlock == null)
                  {
                    ((JSONObject)localObject2).put("errorCode", Integer.valueOf(5));
                    ((JSONObject)localObject2).put("errorDescription", "Unknown block");
                  }
                  else
                  {
                    ((JSONObject)localObject2).put("height", Integer.valueOf(localBlock.height));
                    ((JSONObject)localObject2).put("generator", convert(Nxt.Account.getId(localBlock.generatorPublicKey)));
                    ((JSONObject)localObject2).put("timestamp", Integer.valueOf(localBlock.timestamp));
                    ((JSONObject)localObject2).put("numberOfTransactions", Integer.valueOf(localBlock.numberOfTransactions));
                    ((JSONObject)localObject2).put("totalAmount", Integer.valueOf(localBlock.totalAmount));
                    ((JSONObject)localObject2).put("totalFee", Integer.valueOf(localBlock.totalFee));
                    ((JSONObject)localObject2).put("payloadLength", Integer.valueOf(localBlock.payloadLength));
                    ((JSONObject)localObject2).put("version", Integer.valueOf(localBlock.version));
                    ((JSONObject)localObject2).put("baseTarget", convert(localBlock.baseTarget));
                    if (localBlock.previousBlock != 0L) {
                      ((JSONObject)localObject2).put("previousBlock", convert(localBlock.previousBlock));
                    }
                    if (localBlock.nextBlock != 0L) {
                      ((JSONObject)localObject2).put("nextBlock", convert(localBlock.nextBlock));
                    }
                    ((JSONObject)localObject2).put("payloadHash", convert(localBlock.payloadHash));
                    ((JSONObject)localObject2).put("generationSignature", convert(localBlock.generationSignature));
                    if (localBlock.version > 1) {
                      ((JSONObject)localObject2).put("previousBlockHash", convert(localBlock.previousBlockHash));
                    }
                    ((JSONObject)localObject2).put("blockSignature", convert(localBlock.blockSignature));
                    ??? = new JSONArray();
                    for (int i4 = 0; i4 < localBlock.numberOfTransactions; i4++) {
                      ((JSONArray)???).add(convert(localBlock.transactions[i4]));
                    }
                    ((JSONObject)localObject2).put("transactions", ???);
                  }
                }
                catch (Exception localException8)
                {
                  ((JSONObject)localObject2).put("errorCode", Integer.valueOf(4));
                  ((JSONObject)localObject2).put("errorDescription", "Incorrect \"block\"");
                }
              }
              break;
            case 12: 
              localObject7 = new JSONArray();
              localObject11 = new JSONObject();
              ((JSONObject)localObject11).put("value", Byte.valueOf((byte)0));
              ((JSONObject)localObject11).put("description", "Payment");
              ??? = new JSONArray();
              localObject16 = new JSONObject();
              ((JSONObject)localObject16).put("value", Byte.valueOf((byte)0));
              ((JSONObject)localObject16).put("description", "Ordinary payment");
              ((JSONArray)???).add(localObject16);
              ((JSONObject)localObject11).put("subtypes", ???);
              ((JSONArray)localObject7).add(localObject11);
              localObject11 = new JSONObject();
              ((JSONObject)localObject11).put("value", Byte.valueOf((byte)1));
              ((JSONObject)localObject11).put("description", "Messaging");
              ??? = new JSONArray();
              localObject16 = new JSONObject();
              ((JSONObject)localObject16).put("value", Byte.valueOf((byte)0));
              ((JSONObject)localObject16).put("description", "Arbitrary message");
              ((JSONArray)???).add(localObject16);
              localObject16 = new JSONObject();
              ((JSONObject)localObject16).put("value", Byte.valueOf((byte)1));
              ((JSONObject)localObject16).put("description", "Alias assignment");
              ((JSONArray)???).add(localObject16);
              ((JSONObject)localObject11).put("subtypes", ???);
              ((JSONArray)localObject7).add(localObject11);
              localObject11 = new JSONObject();
              ((JSONObject)localObject11).put("value", Byte.valueOf((byte)2));
              ((JSONObject)localObject11).put("description", "Colored coins");
              ??? = new JSONArray();
              localObject16 = new JSONObject();
              ((JSONObject)localObject16).put("value", Byte.valueOf((byte)0));
              ((JSONObject)localObject16).put("description", "Asset issuance");
              ((JSONArray)???).add(localObject16);
              localObject16 = new JSONObject();
              ((JSONObject)localObject16).put("value", Byte.valueOf((byte)1));
              ((JSONObject)localObject16).put("description", "Asset transfer");
              ((JSONArray)???).add(localObject16);
              localObject16 = new JSONObject();
              ((JSONObject)localObject16).put("value", Byte.valueOf((byte)2));
              ((JSONObject)localObject16).put("description", "Ask order placement");
              ((JSONArray)???).add(localObject16);
              localObject16 = new JSONObject();
              ((JSONObject)localObject16).put("value", Byte.valueOf((byte)3));
              ((JSONObject)localObject16).put("description", "Bid order placement");
              ((JSONArray)???).add(localObject16);
              localObject16 = new JSONObject();
              ((JSONObject)localObject16).put("value", Byte.valueOf((byte)4));
              ((JSONObject)localObject16).put("description", "Ask order cancellation");
              ((JSONArray)???).add(localObject16);
              localObject16 = new JSONObject();
              ((JSONObject)localObject16).put("value", Byte.valueOf((byte)5));
              ((JSONObject)localObject16).put("description", "Bid order cancellation");
              ((JSONArray)???).add(localObject16);
              ((JSONObject)localObject11).put("subtypes", ???);
              ((JSONArray)localObject7).add(localObject11);
              ((JSONObject)localObject2).put("transactionTypes", localObject7);
              localObject19 = new JSONArray();
              localObject22 = new JSONObject();
              ((JSONObject)localObject22).put("value", Integer.valueOf(0));
              ((JSONObject)localObject22).put("description", "Non-connected");
              ((JSONArray)localObject19).add(localObject22);
              localObject22 = new JSONObject();
              ((JSONObject)localObject22).put("value", Integer.valueOf(1));
              ((JSONObject)localObject22).put("description", "Connected");
              ((JSONArray)localObject19).add(localObject22);
              localObject22 = new JSONObject();
              ((JSONObject)localObject22).put("value", Integer.valueOf(2));
              ((JSONObject)localObject22).put("description", "Disconnected");
              ((JSONArray)localObject19).add(localObject22);
              ((JSONObject)localObject2).put("peerStates", localObject19);
              break;
            case 13: 
              ((JSONObject)localObject2).put("host", paramHttpServletRequest.getRemoteHost());
              ((JSONObject)localObject2).put("address", paramHttpServletRequest.getRemoteAddr());
              break;
            case 14: 
              localObject7 = paramHttpServletRequest.getParameter("peer");
              if (localObject7 == null)
              {
                ((JSONObject)localObject2).put("errorCode", Integer.valueOf(3));
                ((JSONObject)localObject2).put("errorDescription", "\"peer\" not specified");
              }
              else
              {
                localObject11 = (Nxt.Peer)peers.get(localObject7);
                if (localObject11 == null)
                {
                  ((JSONObject)localObject2).put("errorCode", Integer.valueOf(5));
                  ((JSONObject)localObject2).put("errorDescription", "Unknown peer");
                }
                else
                {
                  ((JSONObject)localObject2).put("state", Integer.valueOf(((Nxt.Peer)localObject11).state));
                  ((JSONObject)localObject2).put("announcedAddress", ((Nxt.Peer)localObject11).announcedAddress);
                  if (((Nxt.Peer)localObject11).hallmark != null) {
                    ((JSONObject)localObject2).put("hallmark", ((Nxt.Peer)localObject11).hallmark);
                  }
                  ((JSONObject)localObject2).put("weight", Integer.valueOf(((Nxt.Peer)localObject11).getWeight()));
                  ((JSONObject)localObject2).put("downloadedVolume", Long.valueOf(((Nxt.Peer)localObject11).downloadedVolume));
                  ((JSONObject)localObject2).put("uploadedVolume", Long.valueOf(((Nxt.Peer)localObject11).uploadedVolume));
                  ((JSONObject)localObject2).put("application", ((Nxt.Peer)localObject11).application);
                  ((JSONObject)localObject2).put("version", ((Nxt.Peer)localObject11).version);
                  ((JSONObject)localObject2).put("platform", ((Nxt.Peer)localObject11).platform);
                }
              }
              break;
            case 15: 
              localObject7 = new JSONArray();
              ((JSONArray)localObject7).addAll(peers.keySet());
              ((JSONObject)localObject2).put("peers", localObject7);
              break;
            case 16: 
              ((JSONObject)localObject2).put("version", "0.5.3");
              ((JSONObject)localObject2).put("time", Integer.valueOf(getEpochTime(System.currentTimeMillis())));
              ((JSONObject)localObject2).put("lastBlock", convert(lastBlock));
              ((JSONObject)localObject2).put("numberOfBlocks", Integer.valueOf(blocks.size()));
              ((JSONObject)localObject2).put("numberOfTransactions", Integer.valueOf(transactions.size()));
              ((JSONObject)localObject2).put("numberOfAccounts", Integer.valueOf(accounts.size()));
              ((JSONObject)localObject2).put("numberOfAssets", Integer.valueOf(assets.size()));
              ((JSONObject)localObject2).put("numberOfOrders", Integer.valueOf(askOrders.size() + bidOrders.size()));
              ((JSONObject)localObject2).put("numberOfAliases", Integer.valueOf(aliases.size()));
              ((JSONObject)localObject2).put("numberOfPeers", Integer.valueOf(peers.size()));
              ((JSONObject)localObject2).put("numberOfUsers", Integer.valueOf(users.size()));
              ((JSONObject)localObject2).put("lastBlockchainFeeder", lastBlockchainFeeder == null ? null : lastBlockchainFeeder.announcedAddress);
              ((JSONObject)localObject2).put("availableProcessors", Integer.valueOf(Runtime.getRuntime().availableProcessors()));
              ((JSONObject)localObject2).put("maxMemory", Long.valueOf(Runtime.getRuntime().maxMemory()));
              ((JSONObject)localObject2).put("totalMemory", Long.valueOf(Runtime.getRuntime().totalMemory()));
              ((JSONObject)localObject2).put("freeMemory", Long.valueOf(Runtime.getRuntime().freeMemory()));
              break;
            case 17: 
              ((JSONObject)localObject2).put("time", Integer.valueOf(getEpochTime(System.currentTimeMillis())));
              break;
            case 18: 
              localObject7 = paramHttpServletRequest.getParameter("transaction");
              if (localObject7 == null)
              {
                ((JSONObject)localObject2).put("errorCode", Integer.valueOf(3));
                ((JSONObject)localObject2).put("errorDescription", "\"transaction\" not specified");
              }
              else
              {
                try
                {
                  long l3 = new BigInteger((String)localObject7).longValue();
                  localObject16 = (Nxt.Transaction)transactions.get(Long.valueOf(l3));
                  if (localObject16 == null)
                  {
                    localObject16 = (Nxt.Transaction)unconfirmedTransactions.get(Long.valueOf(l3));
                    if (localObject16 == null)
                    {
                      ((JSONObject)localObject2).put("errorCode", Integer.valueOf(5));
                      ((JSONObject)localObject2).put("errorDescription", "Unknown transaction");
                    }
                    else
                    {
                      localObject2 = ((Nxt.Transaction)localObject16).getJSONObject();
                      ((JSONObject)localObject2).put("sender", convert(Nxt.Account.getId(((Nxt.Transaction)localObject16).senderPublicKey)));
                    }
                  }
                  else
                  {
                    localObject2 = ((Nxt.Transaction)localObject16).getJSONObject();
                    ((JSONObject)localObject2).put("sender", convert(Nxt.Account.getId(((Nxt.Transaction)localObject16).senderPublicKey)));
                    localObject19 = (Nxt.Block)blocks.get(Long.valueOf(((Nxt.Transaction)localObject16).block));
                    ((JSONObject)localObject2).put("block", convert(((Nxt.Block)localObject19).getId()));
                    ((JSONObject)localObject2).put("confirmations", Integer.valueOf(Nxt.Block.getLastBlock().height - ((Nxt.Block)localObject19).height + 1));
                  }
                }
                catch (Exception localException9)
                {
                  ((JSONObject)localObject2).put("errorCode", Integer.valueOf(4));
                  ((JSONObject)localObject2).put("errorDescription", "Incorrect \"transaction\"");
                }
              }
              break;
            case 19: 
              localObject7 = paramHttpServletRequest.getParameter("transaction");
              if (localObject7 == null)
              {
                ((JSONObject)localObject2).put("errorCode", Integer.valueOf(3));
                ((JSONObject)localObject2).put("errorDescription", "\"transaction\" not specified");
              }
              else
              {
                try
                {
                  long l4 = new BigInteger((String)localObject7).longValue();
                  localObject16 = (Nxt.Transaction)transactions.get(Long.valueOf(l4));
                  if (localObject16 == null)
                  {
                    localObject16 = (Nxt.Transaction)unconfirmedTransactions.get(Long.valueOf(l4));
                    if (localObject16 == null)
                    {
                      ((JSONObject)localObject2).put("errorCode", Integer.valueOf(5));
                      ((JSONObject)localObject2).put("errorDescription", "Unknown transaction");
                    }
                    else
                    {
                      ((JSONObject)localObject2).put("bytes", convert(((Nxt.Transaction)localObject16).getBytes()));
                    }
                  }
                  else
                  {
                    ((JSONObject)localObject2).put("bytes", convert(((Nxt.Transaction)localObject16).getBytes()));
                    localObject19 = (Nxt.Block)blocks.get(Long.valueOf(((Nxt.Transaction)localObject16).block));
                    ((JSONObject)localObject2).put("confirmations", Integer.valueOf(Nxt.Block.getLastBlock().height - ((Nxt.Block)localObject19).height + 1));
                  }
                }
                catch (Exception localException10)
                {
                  ((JSONObject)localObject2).put("errorCode", Integer.valueOf(4));
                  ((JSONObject)localObject2).put("errorDescription", "Incorrect \"transaction\"");
                }
              }
              break;
            case 20: 
              localObject7 = new JSONArray();
              Iterator localIterator = unconfirmedTransactions.keySet().iterator();
              while (localIterator.hasNext())
              {
                ??? = (Long)localIterator.next();
                ((JSONArray)localObject7).add(convert(((Long)???).longValue()));
              }
              ((JSONObject)localObject2).put("unconfirmedTransactionIds", localObject7);
              break;
            case 21: 
              localObject7 = paramHttpServletRequest.getParameter("account");
              if (localObject7 == null)
              {
                ((JSONObject)localObject2).put("errorCode", Integer.valueOf(3));
                ((JSONObject)localObject2).put("errorDescription", "\"account\" not specified");
              }
              else
              {
                try
                {
                  long l5 = new BigInteger((String)localObject7).longValue();
                  localObject16 = (Nxt.Account)accounts.get(Long.valueOf(l5));
                  if (localObject16 == null)
                  {
                    ((JSONObject)localObject2).put("errorCode", Integer.valueOf(5));
                    ((JSONObject)localObject2).put("errorDescription", "Unknown account");
                  }
                  else
                  {
                    localObject19 = new JSONArray();
                    localObject22 = aliases.values().iterator();
                    while (((Iterator)localObject22).hasNext())
                    {
                      localObject25 = (Nxt.Alias)((Iterator)localObject22).next();
                      if (((Nxt.Alias)localObject25).account.id == l5)
                      {
                        localObject28 = new JSONObject();
                        ((JSONObject)localObject28).put("alias", ((Nxt.Alias)localObject25).alias);
                        ((JSONObject)localObject28).put("uri", ((Nxt.Alias)localObject25).uri);
                        ((JSONArray)localObject19).add(localObject28);
                      }
                    }
                    ((JSONObject)localObject2).put("aliases", localObject19);
                  }
                }
                catch (Exception localException11)
                {
                  ((JSONObject)localObject2).put("errorCode", Integer.valueOf(4));
                  ((JSONObject)localObject2).put("errorDescription", "Incorrect \"account\"");
                }
              }
              break;
            case 22: 
              localObject7 = paramHttpServletRequest.getParameter("secretPhrase");
              str3 = paramHttpServletRequest.getParameter("host");
              ??? = paramHttpServletRequest.getParameter("weight");
              localObject16 = paramHttpServletRequest.getParameter("date");
              if (localObject7 == null)
              {
                ((JSONObject)localObject2).put("errorCode", Integer.valueOf(3));
                ((JSONObject)localObject2).put("errorDescription", "\"secretPhrase\" not specified");
              }
              else if (str3 == null)
              {
                ((JSONObject)localObject2).put("errorCode", Integer.valueOf(3));
                ((JSONObject)localObject2).put("errorDescription", "\"host\" not specified");
              }
              else if (??? == null)
              {
                ((JSONObject)localObject2).put("errorCode", Integer.valueOf(3));
                ((JSONObject)localObject2).put("errorDescription", "\"weight\" not specified");
              }
              else if (localObject16 == null)
              {
                ((JSONObject)localObject2).put("errorCode", Integer.valueOf(3));
                ((JSONObject)localObject2).put("errorDescription", "\"date\" not specified");
              }
              else if (str3.length() > 100)
              {
                ((JSONObject)localObject2).put("errorCode", Integer.valueOf(4));
                ((JSONObject)localObject2).put("errorDescription", "Incorrect \"host\" (the length exceeds 100 chars limit)");
              }
              else
              {
                try
                {
                  int i6 = Integer.parseInt((String)???);
                  if ((i6 <= 0) || (i6 > 1000000000L)) {
                    throw new Exception();
                  }
                  try
                  {
                    int i9 = Integer.parseInt(((String)localObject16).substring(0, 4)) * 10000 + Integer.parseInt(((String)localObject16).substring(5, 7)) * 100 + Integer.parseInt(((String)localObject16).substring(8, 10));
                    localObject25 = Nxt.Crypto.getPublicKey((String)localObject7);
                    localObject28 = str3.getBytes("UTF-8");
                    localObject29 = ByteBuffer.allocate(34 + localObject28.length + 4 + 4 + 1);
                    ((ByteBuffer)localObject29).order(ByteOrder.LITTLE_ENDIAN);
                    ((ByteBuffer)localObject29).put((byte[])localObject25);
                    ((ByteBuffer)localObject29).putShort((short)localObject28.length);
                    ((ByteBuffer)localObject29).put((byte[])localObject28);
                    ((ByteBuffer)localObject29).putInt(i6);
                    ((ByteBuffer)localObject29).putInt(i9);
                    arrayOfByte2 = ((ByteBuffer)localObject29).array();
                    byte[] arrayOfByte3;
                    do
                    {
                      arrayOfByte2[(arrayOfByte2.length - 1)] = ((byte)ThreadLocalRandom.current().nextInt());
                      arrayOfByte3 = Nxt.Crypto.sign(arrayOfByte2, (String)localObject7);
                    } while (!Nxt.Crypto.verify(arrayOfByte3, arrayOfByte2, (byte[])localObject25));
                    ((JSONObject)localObject2).put("hallmark", convert(arrayOfByte2) + convert(arrayOfByte3));
                  }
                  catch (Exception localException15)
                  {
                    ((JSONObject)localObject2).put("errorCode", Integer.valueOf(4));
                    ((JSONObject)localObject2).put("errorDescription", "Incorrect \"date\"");
                  }
                }
                catch (Exception localException14)
                {
                  ((JSONObject)localObject2).put("errorCode", Integer.valueOf(4));
                  ((JSONObject)localObject2).put("errorDescription", "Incorrect \"weight\"");
                }
              }
              break;
            case 23: 
              localObject7 = paramHttpServletRequest.getParameter("secretPhrase");
              str3 = paramHttpServletRequest.getParameter("recipient");
              ??? = paramHttpServletRequest.getParameter("amount");
              localObject16 = paramHttpServletRequest.getParameter("fee");
              String str4 = paramHttpServletRequest.getParameter("deadline");
              String str5 = paramHttpServletRequest.getParameter("referencedTransaction");
              if (localObject7 == null)
              {
                ((JSONObject)localObject2).put("errorCode", Integer.valueOf(3));
                ((JSONObject)localObject2).put("errorDescription", "\"secretPhrase\" not specified");
              }
              else if (str3 == null)
              {
                ((JSONObject)localObject2).put("errorCode", Integer.valueOf(3));
                ((JSONObject)localObject2).put("errorDescription", "\"recipient\" not specified");
              }
              else if (??? == null)
              {
                ((JSONObject)localObject2).put("errorCode", Integer.valueOf(3));
                ((JSONObject)localObject2).put("errorDescription", "\"amount\" not specified");
              }
              else if (localObject16 == null)
              {
                ((JSONObject)localObject2).put("errorCode", Integer.valueOf(3));
                ((JSONObject)localObject2).put("errorDescription", "\"fee\" not specified");
              }
              else if (str4 == null)
              {
                ((JSONObject)localObject2).put("errorCode", Integer.valueOf(3));
                ((JSONObject)localObject2).put("errorDescription", "\"deadline\" not specified");
              }
              else
              {
                try
                {
                  localObject25 = new BigInteger(str3.trim());
                  if ((((BigInteger)localObject25).signum() < 0) || (((BigInteger)localObject25).compareTo(two64) != -1)) {
                    throw new Exception();
                  }
                  long l10 = ((BigInteger)localObject25).longValue();
                  try
                  {
                    int i17 = Integer.parseInt((String)???);
                    if ((i17 <= 0) || (i17 >= 1000000000L)) {
                      throw new Exception();
                    }
                    try
                    {
                      int i19 = Integer.parseInt((String)localObject16);
                      if ((i19 <= 0) || (i19 >= 1000000000L)) {
                        throw new Exception();
                      }
                      try
                      {
                        i20 = Short.parseShort(str4);
                        if (i20 < 1) {
                          throw new Exception();
                        }
                        long l12 = str5 == null ? 0L : new BigInteger(str5).longValue();
                        byte[] arrayOfByte5 = Nxt.Crypto.getPublicKey((String)localObject7);
                        localAccount = (Nxt.Account)accounts.get(Long.valueOf(Nxt.Account.getId(arrayOfByte5)));
                        if (localAccount == null)
                        {
                          ((JSONObject)localObject2).put("errorCode", Integer.valueOf(6));
                          ((JSONObject)localObject2).put("errorDescription", "Not enough funds");
                        }
                        else if ((i17 + i19) * 100L > localAccount.getUnconfirmedBalance())
                        {
                          ((JSONObject)localObject2).put("errorCode", Integer.valueOf(6));
                          ((JSONObject)localObject2).put("errorDescription", "Not enough funds");
                        }
                        else
                        {
                          localObject34 = new Nxt.Transaction((byte)0, (byte)0, getEpochTime(System.currentTimeMillis()), i20, arrayOfByte5, l10, i17, i19, l12, new byte[64]);
                          ((Nxt.Transaction)localObject34).sign((String)localObject7);
                          JSONObject localJSONObject7 = new JSONObject();
                          localJSONObject7.put("requestType", "processTransactions");
                          localObject35 = new JSONArray();
                          ((JSONArray)localObject35).add(((Nxt.Transaction)localObject34).getJSONObject());
                          localJSONObject7.put("transactions", localObject35);
                          Nxt.Peer.sendToAllPeers(localJSONObject7);
                          ((JSONObject)localObject2).put("transaction", convert(((Nxt.Transaction)localObject34).getId()));
                          ((JSONObject)localObject2).put("bytes", convert(((Nxt.Transaction)localObject34).getBytes()));
                        }
                      }
                      catch (Exception localException23)
                      {
                        ((JSONObject)localObject2).put("errorCode", Integer.valueOf(4));
                        ((JSONObject)localObject2).put("errorDescription", "Incorrect \"deadline\"");
                      }
                    }
                    catch (Exception localException22)
                    {
                      ((JSONObject)localObject2).put("errorCode", Integer.valueOf(4));
                      ((JSONObject)localObject2).put("errorDescription", "Incorrect \"fee\"");
                    }
                  }
                  catch (Exception localException21)
                  {
                    ((JSONObject)localObject2).put("errorCode", Integer.valueOf(4));
                    ((JSONObject)localObject2).put("errorDescription", "Incorrect \"amount\"");
                  }
                }
                catch (Exception localException17)
                {
                  ((JSONObject)localObject2).put("errorCode", Integer.valueOf(4));
                  ((JSONObject)localObject2).put("errorDescription", "Incorrect \"recipient\"");
                }
              }
              break;
            default: 
              ((JSONObject)localObject2).put("errorCode", Integer.valueOf(1));
              ((JSONObject)localObject2).put("errorDescription", "Incorrect request");
            }
          }
        }
        paramHttpServletResponse.setContentType("text/plain; charset=UTF-8");
        localObject3 = paramHttpServletResponse.getOutputStream();
        localObject4 = null;
        try
        {
          ((ServletOutputStream)localObject3).write(((JSONObject)localObject2).toString().getBytes("UTF-8"));
        }
        catch (Throwable localThrowable2)
        {
          localObject4 = localThrowable2;
          throw localThrowable2;
        }
        finally
        {
          if (localObject3 != null) {
            if (localObject4 != null) {
              try
              {
                ((ServletOutputStream)localObject3).close();
              }
              catch (Throwable localThrowable11)
              {
                ((Throwable)localObject4).addSuppressed(localThrowable11);
              }
            } else {
              ((ServletOutputStream)localObject3).close();
            }
          }
        }
        return;
      }
      if ((allowedUserHosts != null) && (!allowedUserHosts.contains(paramHttpServletRequest.getRemoteHost())))
      {
        localObject2 = new JSONObject();
        ((JSONObject)localObject2).put("response", "denyAccess");
        localObject3 = new JSONArray();
        ((JSONArray)localObject3).add(localObject2);
        localObject4 = new JSONObject();
        ((JSONObject)localObject4).put("responses", localObject3);
        paramHttpServletResponse.setContentType("text/plain; charset=UTF-8");
        localObject6 = paramHttpServletResponse.getOutputStream();
        localObject7 = null;
        try
        {
          ((ServletOutputStream)localObject6).write(((JSONObject)localObject4).toString().getBytes("UTF-8"));
        }
        catch (Throwable localThrowable6)
        {
          localObject7 = localThrowable6;
          throw localThrowable6;
        }
        finally
        {
          if (localObject6 != null) {
            if (localObject7 != null) {
              try
              {
                ((ServletOutputStream)localObject6).close();
              }
              catch (Throwable localThrowable12)
              {
                ((Throwable)localObject7).addSuppressed(localThrowable12);
              }
            } else {
              ((ServletOutputStream)localObject6).close();
            }
          }
        }
        return;
      }
      localObject1 = (Nxt.User)users.get(str1);
      if (localObject1 == null)
      {
        localObject1 = new Nxt.User();
        localObject2 = (Nxt.User)users.putIfAbsent(str1, localObject1);
        if (localObject2 != null)
        {
          localObject1 = localObject2;
          ((Nxt.User)localObject1).isInactive = false;
        }
      }
      else
      {
        ((Nxt.User)localObject1).isInactive = false;
      }
      localObject2 = paramHttpServletRequest.getParameter("requestType");
      int i = -1;
      switch (((String)localObject2).hashCode())
      {
      case 94341973: 
        if (((String)localObject2).equals("generateAuthorizationToken")) {
          i = 0;
        }
        break;
      case 592625624: 
        if (((String)localObject2).equals("getInitialData")) {
          i = 1;
        }
        break;
      case -1413383884: 
        if (((String)localObject2).equals("getNewData")) {
          i = 2;
        }
        break;
      case -1695267198: 
        if (((String)localObject2).equals("lockAccount")) {
          i = 3;
        }
        break;
      case -1215991508: 
        if (((String)localObject2).equals("removeActivePeer")) {
          i = 4;
        }
        break;
      case 892719322: 
        if (((String)localObject2).equals("removeBlacklistedPeer")) {
          i = 5;
        }
        break;
      case -632255711: 
        if (((String)localObject2).equals("removeKnownPeer")) {
          i = 6;
        }
        break;
      case 9950744: 
        if (((String)localObject2).equals("sendMoney")) {
          i = 7;
        }
        break;
      case -349483447: 
        if (((String)localObject2).equals("unlockAccount")) {
          i = 8;
        }
        break;
      }
      Object localObject20;
      Object localObject12;
      Object localObject31;
      long l6;
      int i10;
      Object localObject30;
      switch (i)
      {
      case 0: 
        localObject4 = paramHttpServletRequest.getParameter("secretPhrase");
        if (!((Nxt.User)localObject1).secretPhrase.equals(localObject4))
        {
          localObject6 = new JSONObject();
          ((JSONObject)localObject6).put("response", "showMessage");
          ((JSONObject)localObject6).put("message", "Invalid secret phrase!");
          ((Nxt.User)localObject1).pendingResponses.offer(localObject6);
        }
        else
        {
          localObject6 = paramHttpServletRequest.getParameter("website").trim().getBytes("UTF-8");
          localObject7 = new byte[localObject6.length + 32 + 4];
          System.arraycopy(localObject6, 0, localObject7, 0, localObject6.length);
          System.arraycopy(Nxt.Crypto.getPublicKey(((Nxt.User)localObject1).secretPhrase), 0, localObject7, localObject6.length, 32);
          int i2 = getEpochTime(System.currentTimeMillis());
          localObject7[(localObject6.length + 32)] = ((byte)i2);
          localObject7[(localObject6.length + 32 + 1)] = ((byte)(i2 >> 8));
          localObject7[(localObject6.length + 32 + 2)] = ((byte)(i2 >> 16));
          localObject7[(localObject6.length + 32 + 3)] = ((byte)(i2 >> 24));
          ??? = new byte[100];
          System.arraycopy(localObject7, localObject6.length, ???, 0, 36);
          System.arraycopy(Nxt.Crypto.sign((byte[])localObject7, ((Nxt.User)localObject1).secretPhrase), 0, ???, 36, 64);
          localObject16 = "";
          for (int i7 = 0; i7 < 100; i7 += 5)
          {
            long l8 = ???[i7] & 0xFF | (???[(i7 + 1)] & 0xFF) << 8 | (???[(i7 + 2)] & 0xFF) << 16 | (???[(i7 + 3)] & 0xFF) << 24 | (???[(i7 + 4)] & 0xFF) << 32;
            if (l8 < 32L) {
              localObject16 = (String)localObject16 + "0000000";
            } else if (l8 < 1024L) {
              localObject16 = (String)localObject16 + "000000";
            } else if (l8 < 32768L) {
              localObject16 = (String)localObject16 + "00000";
            } else if (l8 < 1048576L) {
              localObject16 = (String)localObject16 + "0000";
            } else if (l8 < 33554432L) {
              localObject16 = (String)localObject16 + "000";
            } else if (l8 < 1073741824L) {
              localObject16 = (String)localObject16 + "00";
            } else if (l8 < 34359738368L) {
              localObject16 = (String)localObject16 + "0";
            }
            localObject16 = (String)localObject16 + Long.toString(l8, 32);
          }
          localObject20 = new JSONObject();
          ((JSONObject)localObject20).put("response", "showAuthorizationToken");
          ((JSONObject)localObject20).put("token", localObject16);
          ((Nxt.User)localObject1).pendingResponses.offer(localObject20);
        }
        break;
      case 1: 
        localObject4 = new JSONArray();
        localObject6 = new JSONArray();
        localObject7 = new JSONArray();
        localObject12 = new JSONArray();
        ??? = new JSONArray();
        localObject16 = unconfirmedTransactions.values().iterator();
        Object localObject23;
        while (((Iterator)localObject16).hasNext())
        {
          localObject20 = (Nxt.Transaction)((Iterator)localObject16).next();
          localObject23 = new JSONObject();
          ((JSONObject)localObject23).put("index", Integer.valueOf(((Nxt.Transaction)localObject20).index));
          ((JSONObject)localObject23).put("timestamp", Integer.valueOf(((Nxt.Transaction)localObject20).timestamp));
          ((JSONObject)localObject23).put("deadline", Short.valueOf(((Nxt.Transaction)localObject20).deadline));
          ((JSONObject)localObject23).put("recipient", convert(((Nxt.Transaction)localObject20).recipient));
          ((JSONObject)localObject23).put("amount", Integer.valueOf(((Nxt.Transaction)localObject20).amount));
          ((JSONObject)localObject23).put("fee", Integer.valueOf(((Nxt.Transaction)localObject20).fee));
          ((JSONObject)localObject23).put("sender", convert(Nxt.Account.getId(((Nxt.Transaction)localObject20).senderPublicKey)));
          ((JSONArray)localObject4).add(localObject23);
        }
        localObject16 = peers.entrySet().iterator();
        JSONObject localJSONObject4;
        while (((Iterator)localObject16).hasNext())
        {
          localObject20 = (Map.Entry)((Iterator)localObject16).next();
          localObject23 = (String)((Map.Entry)localObject20).getKey();
          localObject26 = (Nxt.Peer)((Map.Entry)localObject20).getValue();
          if (((Nxt.Peer)localObject26).blacklistingTime > 0L)
          {
            localJSONObject4 = new JSONObject();
            localJSONObject4.put("index", Integer.valueOf(((Nxt.Peer)localObject26).index));
            localJSONObject4.put("announcedAddress", ((Nxt.Peer)localObject26).announcedAddress.length() > 0 ? ((Nxt.Peer)localObject26).announcedAddress : ((Nxt.Peer)localObject26).announcedAddress.length() > 30 ? ((Nxt.Peer)localObject26).announcedAddress.substring(0, 30) + "..." : localObject23);
            localObject29 = wellKnownPeers.iterator();
            while (((Iterator)localObject29).hasNext())
            {
              localObject31 = (String)((Iterator)localObject29).next();
              if (((Nxt.Peer)localObject26).announcedAddress.equals(localObject31))
              {
                localJSONObject4.put("wellKnown", Boolean.valueOf(true));
                break;
              }
            }
            ((JSONArray)localObject12).add(localJSONObject4);
          }
          else if (((Nxt.Peer)localObject26).state == 0)
          {
            if (((Nxt.Peer)localObject26).announcedAddress.length() > 0)
            {
              localJSONObject4 = new JSONObject();
              localJSONObject4.put("index", Integer.valueOf(((Nxt.Peer)localObject26).index));
              localJSONObject4.put("announcedAddress", ((Nxt.Peer)localObject26).announcedAddress.length() > 30 ? ((Nxt.Peer)localObject26).announcedAddress.substring(0, 30) + "..." : ((Nxt.Peer)localObject26).announcedAddress);
              localObject29 = wellKnownPeers.iterator();
              while (((Iterator)localObject29).hasNext())
              {
                localObject31 = (String)((Iterator)localObject29).next();
                if (((Nxt.Peer)localObject26).announcedAddress.equals(localObject31))
                {
                  localJSONObject4.put("wellKnown", Boolean.valueOf(true));
                  break;
                }
              }
              ((JSONArray)localObject7).add(localJSONObject4);
            }
          }
          else
          {
            localJSONObject4 = new JSONObject();
            localJSONObject4.put("index", Integer.valueOf(((Nxt.Peer)localObject26).index));
            if (((Nxt.Peer)localObject26).state == 2) {
              localJSONObject4.put("disconnected", Boolean.valueOf(true));
            }
            localJSONObject4.put("address", ((String)localObject23).length() > 30 ? ((String)localObject23).substring(0, 30) + "..." : localObject23);
            localJSONObject4.put("announcedAddress", ((Nxt.Peer)localObject26).announcedAddress.length() > 30 ? ((Nxt.Peer)localObject26).announcedAddress.substring(0, 30) + "..." : ((Nxt.Peer)localObject26).announcedAddress);
            localJSONObject4.put("weight", Integer.valueOf(((Nxt.Peer)localObject26).getWeight()));
            localJSONObject4.put("downloaded", Long.valueOf(((Nxt.Peer)localObject26).downloadedVolume));
            localJSONObject4.put("uploaded", Long.valueOf(((Nxt.Peer)localObject26).uploadedVolume));
            localJSONObject4.put("software", ((Nxt.Peer)localObject26).getSoftware());
            localObject29 = wellKnownPeers.iterator();
            while (((Iterator)localObject29).hasNext())
            {
              localObject31 = (String)((Iterator)localObject29).next();
              if (((Nxt.Peer)localObject26).announcedAddress.equals(localObject31))
              {
                localJSONObject4.put("wellKnown", Boolean.valueOf(true));
                break;
              }
            }
            ((JSONArray)localObject6).add(localJSONObject4);
          }
        }
        l6 = lastBlock;
        i10 = 0;
        while (i10 < 60)
        {
          i10++;
          localObject26 = (Nxt.Block)blocks.get(Long.valueOf(l6));
          localJSONObject4 = new JSONObject();
          localJSONObject4.put("index", Integer.valueOf(((Nxt.Block)localObject26).index));
          localJSONObject4.put("timestamp", Integer.valueOf(((Nxt.Block)localObject26).timestamp));
          localJSONObject4.put("numberOfTransactions", Integer.valueOf(((Nxt.Block)localObject26).numberOfTransactions));
          localJSONObject4.put("totalAmount", Integer.valueOf(((Nxt.Block)localObject26).totalAmount));
          localJSONObject4.put("totalFee", Integer.valueOf(((Nxt.Block)localObject26).totalFee));
          localJSONObject4.put("payloadLength", Integer.valueOf(((Nxt.Block)localObject26).payloadLength));
          localJSONObject4.put("generator", convert(Nxt.Account.getId(((Nxt.Block)localObject26).generatorPublicKey)));
          localJSONObject4.put("height", Integer.valueOf(((Nxt.Block)localObject26).height));
          localJSONObject4.put("version", Integer.valueOf(((Nxt.Block)localObject26).version));
          localJSONObject4.put("block", convert(l6));
          localJSONObject4.put("baseTarget", BigInteger.valueOf(((Nxt.Block)localObject26).baseTarget).multiply(BigInteger.valueOf(100000L)).divide(BigInteger.valueOf(153722867L)));
          ((JSONArray)???).add(localJSONObject4);
          if (l6 == 2680262203532249785L) {
            break;
          }
          l6 = ((Nxt.Block)localObject26).previousBlock;
        }
        Object localObject26 = new JSONObject();
        ((JSONObject)localObject26).put("response", "processInitialData");
        ((JSONObject)localObject26).put("version", "0.5.3");
        if (((JSONArray)localObject4).size() > 0) {
          ((JSONObject)localObject26).put("unconfirmedTransactions", localObject4);
        }
        if (((JSONArray)localObject6).size() > 0) {
          ((JSONObject)localObject26).put("activePeers", localObject6);
        }
        if (((JSONArray)localObject7).size() > 0) {
          ((JSONObject)localObject26).put("knownPeers", localObject7);
        }
        if (((JSONArray)localObject12).size() > 0) {
          ((JSONObject)localObject26).put("blacklistedPeers", localObject12);
        }
        if (((JSONArray)???).size() > 0) {
          ((JSONObject)localObject26).put("recentBlocks", ???);
        }
        ((Nxt.User)localObject1).pendingResponses.offer(localObject26);
        break;
      case 2: 
        break;
      case 3: 
        ((Nxt.User)localObject1).deinitializeKeyPair();
        localObject4 = new JSONObject();
        ((JSONObject)localObject4).put("response", "lockAccount");
        ((Nxt.User)localObject1).pendingResponses.offer(localObject4);
        break;
      case 4: 
        if ((allowedUserHosts == null) && (!InetAddress.getByName(paramHttpServletRequest.getRemoteAddr()).isLoopbackAddress()))
        {
          localObject4 = new JSONObject();
          ((JSONObject)localObject4).put("response", "showMessage");
          ((JSONObject)localObject4).put("message", "This operation is allowed to local host users only!");
          ((Nxt.User)localObject1).pendingResponses.offer(localObject4);
        }
        else
        {
          int j = Integer.parseInt(paramHttpServletRequest.getParameter("peer"));
          localObject6 = peers.values().iterator();
          while (((Iterator)localObject6).hasNext())
          {
            localObject7 = (Nxt.Peer)((Iterator)localObject6).next();
            if (((Nxt.Peer)localObject7).index == j)
            {
              if ((((Nxt.Peer)localObject7).blacklistingTime != 0L) || (((Nxt.Peer)localObject7).state == 0)) {
                break;
              }
              ((Nxt.Peer)localObject7).deactivate();
              break;
            }
          }
        }
        break;
      case 5: 
        if ((allowedUserHosts == null) && (!InetAddress.getByName(paramHttpServletRequest.getRemoteAddr()).isLoopbackAddress()))
        {
          JSONObject localJSONObject2 = new JSONObject();
          localJSONObject2.put("response", "showMessage");
          localJSONObject2.put("message", "This operation is allowed to local host users only!");
          ((Nxt.User)localObject1).pendingResponses.offer(localJSONObject2);
        }
        else
        {
          int k = Integer.parseInt(paramHttpServletRequest.getParameter("peer"));
          localObject6 = peers.values().iterator();
          while (((Iterator)localObject6).hasNext())
          {
            localObject7 = (Nxt.Peer)((Iterator)localObject6).next();
            if (((Nxt.Peer)localObject7).index == k)
            {
              if (((Nxt.Peer)localObject7).blacklistingTime <= 0L) {
                break;
              }
              ((Nxt.Peer)localObject7).removeBlacklistedStatus();
              break;
            }
          }
        }
        break;
      case 6: 
        if ((allowedUserHosts == null) && (!InetAddress.getByName(paramHttpServletRequest.getRemoteAddr()).isLoopbackAddress()))
        {
          JSONObject localJSONObject3 = new JSONObject();
          localJSONObject3.put("response", "showMessage");
          localJSONObject3.put("message", "This operation is allowed to local host users only!");
          ((Nxt.User)localObject1).pendingResponses.offer(localJSONObject3);
        }
        else
        {
          int m = Integer.parseInt(paramHttpServletRequest.getParameter("peer"));
          localObject6 = peers.values().iterator();
          while (((Iterator)localObject6).hasNext())
          {
            localObject7 = (Nxt.Peer)((Iterator)localObject6).next();
            if (((Nxt.Peer)localObject7).index == m)
            {
              ((Nxt.Peer)localObject7).removePeer();
              break;
            }
          }
        }
        break;
      case 7: 
        if (((Nxt.User)localObject1).secretPhrase != null)
        {
          localObject5 = paramHttpServletRequest.getParameter("recipient");
          localObject6 = paramHttpServletRequest.getParameter("amount");
          localObject7 = paramHttpServletRequest.getParameter("fee");
          localObject12 = paramHttpServletRequest.getParameter("deadline");
          ??? = paramHttpServletRequest.getParameter("secretPhrase");
          i10 = 0;
          int i12 = 0;
          short s1 = 0;
          try
          {
            localObject29 = new BigInteger(((String)localObject5).trim());
            if ((((BigInteger)localObject29).signum() < 0) || (((BigInteger)localObject29).compareTo(two64) != -1)) {
              throw new Exception();
            }
            l6 = ((BigInteger)localObject29).longValue();
            i10 = Integer.parseInt(((String)localObject6).trim());
            i12 = Integer.parseInt(((String)localObject7).trim());
            s1 = (short)(int)(Double.parseDouble((String)localObject12) * 60.0D);
          }
          catch (Exception localException19)
          {
            localObject31 = new JSONObject();
            ((JSONObject)localObject31).put("response", "notifyOfIncorrectTransaction");
            ((JSONObject)localObject31).put("message", "One of the fields is filled incorrectly!");
            ((JSONObject)localObject31).put("recipient", localObject5);
            ((JSONObject)localObject31).put("amount", localObject6);
            ((JSONObject)localObject31).put("fee", localObject7);
            ((JSONObject)localObject31).put("deadline", localObject12);
            ((Nxt.User)localObject1).pendingResponses.offer(localObject31);
            break;
          }
          if (!((Nxt.User)localObject1).secretPhrase.equals(???))
          {
            localObject30 = new JSONObject();
            ((JSONObject)localObject30).put("response", "notifyOfIncorrectTransaction");
            ((JSONObject)localObject30).put("message", "Wrong secret phrase!");
            ((JSONObject)localObject30).put("recipient", localObject5);
            ((JSONObject)localObject30).put("amount", localObject6);
            ((JSONObject)localObject30).put("fee", localObject7);
            ((JSONObject)localObject30).put("deadline", localObject12);
            ((Nxt.User)localObject1).pendingResponses.offer(localObject30);
          }
          else if ((i10 <= 0) || (i10 > 1000000000L))
          {
            localObject30 = new JSONObject();
            ((JSONObject)localObject30).put("response", "notifyOfIncorrectTransaction");
            ((JSONObject)localObject30).put("message", "\"Amount\" must be greater than 0!");
            ((JSONObject)localObject30).put("recipient", localObject5);
            ((JSONObject)localObject30).put("amount", localObject6);
            ((JSONObject)localObject30).put("fee", localObject7);
            ((JSONObject)localObject30).put("deadline", localObject12);
            ((Nxt.User)localObject1).pendingResponses.offer(localObject30);
          }
          else if ((i12 <= 0) || (i12 > 1000000000L))
          {
            localObject30 = new JSONObject();
            ((JSONObject)localObject30).put("response", "notifyOfIncorrectTransaction");
            ((JSONObject)localObject30).put("message", "\"Fee\" must be greater than 0!");
            ((JSONObject)localObject30).put("recipient", localObject5);
            ((JSONObject)localObject30).put("amount", localObject6);
            ((JSONObject)localObject30).put("fee", localObject7);
            ((JSONObject)localObject30).put("deadline", localObject12);
            ((Nxt.User)localObject1).pendingResponses.offer(localObject30);
          }
          else if (s1 < 1)
          {
            localObject30 = new JSONObject();
            ((JSONObject)localObject30).put("response", "notifyOfIncorrectTransaction");
            ((JSONObject)localObject30).put("message", "\"Deadline\" must be greater or equal to 1 minute!");
            ((JSONObject)localObject30).put("recipient", localObject5);
            ((JSONObject)localObject30).put("amount", localObject6);
            ((JSONObject)localObject30).put("fee", localObject7);
            ((JSONObject)localObject30).put("deadline", localObject12);
            ((Nxt.User)localObject1).pendingResponses.offer(localObject30);
          }
          else
          {
            localObject30 = Nxt.Crypto.getPublicKey(((Nxt.User)localObject1).secretPhrase);
            localObject31 = (Nxt.Account)accounts.get(Long.valueOf(Nxt.Account.getId((byte[])localObject30)));
            Object localObject32;
            if ((localObject31 == null) || ((i10 + i12) * 100L > ((Nxt.Account)localObject31).getUnconfirmedBalance()))
            {
              localObject32 = new JSONObject();
              ((JSONObject)localObject32).put("response", "notifyOfIncorrectTransaction");
              ((JSONObject)localObject32).put("message", "Not enough funds!");
              ((JSONObject)localObject32).put("recipient", localObject5);
              ((JSONObject)localObject32).put("amount", localObject6);
              ((JSONObject)localObject32).put("fee", localObject7);
              ((JSONObject)localObject32).put("deadline", localObject12);
              ((Nxt.User)localObject1).pendingResponses.offer(localObject32);
            }
            else
            {
              localObject32 = new Nxt.Transaction((byte)0, (byte)0, getEpochTime(System.currentTimeMillis()), s1, (byte[])localObject30, l6, i10, i12, 0L, new byte[64]);
              ((Nxt.Transaction)localObject32).sign(((Nxt.User)localObject1).secretPhrase);
              JSONObject localJSONObject6 = new JSONObject();
              localJSONObject6.put("requestType", "processTransactions");
              JSONArray localJSONArray2 = new JSONArray();
              localJSONArray2.add(((Nxt.Transaction)localObject32).getJSONObject());
              localJSONObject6.put("transactions", localJSONArray2);
              Nxt.Peer.sendToAllPeers(localJSONObject6);
              localObject33 = new JSONObject();
              ((JSONObject)localObject33).put("response", "notifyOfAcceptedTransaction");
              ((Nxt.User)localObject1).pendingResponses.offer(localObject33);
            }
          }
        }
        break;
      case 8: 
        localObject5 = paramHttpServletRequest.getParameter("secretPhrase");
        localObject6 = users.values().iterator();
        while (((Iterator)localObject6).hasNext())
        {
          localObject7 = (Nxt.User)((Iterator)localObject6).next();
          if (((String)localObject5).equals(((Nxt.User)localObject7).secretPhrase))
          {
            ((Nxt.User)localObject7).deinitializeKeyPair();
            if (!((Nxt.User)localObject7).isInactive)
            {
              localObject12 = new JSONObject();
              ((JSONObject)localObject12).put("response", "lockAccount");
              ((Nxt.User)localObject7).pendingResponses.offer(localObject12);
            }
          }
        }
        localObject6 = ((Nxt.User)localObject1).initializeKeyPair((String)localObject5);
        localObject7 = new JSONObject();
        ((JSONObject)localObject7).put("response", "unlockAccount");
        ((JSONObject)localObject7).put("account", ((BigInteger)localObject6).toString());
        if (((String)localObject5).length() < 30) {
          ((JSONObject)localObject7).put("secretPhraseStrength", Integer.valueOf(1));
        } else {
          ((JSONObject)localObject7).put("secretPhraseStrength", Integer.valueOf(5));
        }
        localObject12 = (Nxt.Account)accounts.get(Long.valueOf(((BigInteger)localObject6).longValue()));
        if (localObject12 == null)
        {
          ((JSONObject)localObject7).put("balance", Integer.valueOf(0));
        }
        else
        {
          ((JSONObject)localObject7).put("balance", Long.valueOf(((Nxt.Account)localObject12).getUnconfirmedBalance()));
          Object localObject27;
          Object localObject24;
          if (((Nxt.Account)localObject12).getEffectiveBalance() > 0)
          {
            ??? = new JSONObject();
            ((JSONObject)???).put("response", "setBlockGenerationDeadline");
            localObject17 = Nxt.Block.getLastBlock();
            localObject20 = MessageDigest.getInstance("SHA-256");
            if (((Nxt.Block)localObject17).height < 30000)
            {
              localObject27 = Nxt.Crypto.sign(((Nxt.Block)localObject17).generationSignature, ((Nxt.User)localObject1).secretPhrase);
              localObject24 = ((MessageDigest)localObject20).digest((byte[])localObject27);
            }
            else
            {
              ((MessageDigest)localObject20).update(((Nxt.Block)localObject17).generationSignature);
              localObject24 = ((MessageDigest)localObject20).digest(Nxt.Crypto.getPublicKey(((Nxt.User)localObject1).secretPhrase));
            }
            localObject27 = new BigInteger(1, new byte[] { localObject24[7], localObject24[6], localObject24[5], localObject24[4], localObject24[3], localObject24[2], localObject24[1], localObject24[0] });
            ((JSONObject)???).put("deadline", Long.valueOf(((BigInteger)localObject27).divide(BigInteger.valueOf(Nxt.Block.getBaseTarget()).multiply(BigInteger.valueOf(((Nxt.Account)localObject12).getEffectiveBalance()))).longValue() - (getEpochTime(System.currentTimeMillis()) - ((Nxt.Block)localObject17).timestamp)));
            ((Nxt.User)localObject1).pendingResponses.offer(???);
          }
          ??? = new JSONArray();
          Object localObject17 = unconfirmedTransactions.values().iterator();
          while (((Iterator)localObject17).hasNext())
          {
            localObject20 = (Nxt.Transaction)((Iterator)localObject17).next();
            if (Nxt.Account.getId(((Nxt.Transaction)localObject20).senderPublicKey) == ((BigInteger)localObject6).longValue())
            {
              localObject24 = new JSONObject();
              ((JSONObject)localObject24).put("index", Integer.valueOf(((Nxt.Transaction)localObject20).index));
              ((JSONObject)localObject24).put("transactionTimestamp", Integer.valueOf(((Nxt.Transaction)localObject20).timestamp));
              ((JSONObject)localObject24).put("deadline", Short.valueOf(((Nxt.Transaction)localObject20).deadline));
              ((JSONObject)localObject24).put("account", convert(((Nxt.Transaction)localObject20).recipient));
              ((JSONObject)localObject24).put("sentAmount", Integer.valueOf(((Nxt.Transaction)localObject20).amount));
              if (((Nxt.Transaction)localObject20).recipient == ((BigInteger)localObject6).longValue()) {
                ((JSONObject)localObject24).put("receivedAmount", Integer.valueOf(((Nxt.Transaction)localObject20).amount));
              }
              ((JSONObject)localObject24).put("fee", Integer.valueOf(((Nxt.Transaction)localObject20).fee));
              ((JSONObject)localObject24).put("numberOfConfirmations", Integer.valueOf(0));
              ((JSONObject)localObject24).put("id", convert(((Nxt.Transaction)localObject20).getId()));
              ((JSONArray)???).add(localObject24);
            }
            else if (((Nxt.Transaction)localObject20).recipient == ((BigInteger)localObject6).longValue())
            {
              localObject24 = new JSONObject();
              ((JSONObject)localObject24).put("index", Integer.valueOf(((Nxt.Transaction)localObject20).index));
              ((JSONObject)localObject24).put("transactionTimestamp", Integer.valueOf(((Nxt.Transaction)localObject20).timestamp));
              ((JSONObject)localObject24).put("deadline", Short.valueOf(((Nxt.Transaction)localObject20).deadline));
              ((JSONObject)localObject24).put("account", convert(Nxt.Account.getId(((Nxt.Transaction)localObject20).senderPublicKey)));
              ((JSONObject)localObject24).put("receivedAmount", Integer.valueOf(((Nxt.Transaction)localObject20).amount));
              ((JSONObject)localObject24).put("fee", Integer.valueOf(((Nxt.Transaction)localObject20).fee));
              ((JSONObject)localObject24).put("numberOfConfirmations", Integer.valueOf(0));
              ((JSONObject)localObject24).put("id", convert(((Nxt.Transaction)localObject20).getId()));
              ((JSONArray)???).add(localObject24);
            }
          }
          long l7 = lastBlock;
          for (int i11 = 1; ((JSONArray)???).size() < 1000; i11++)
          {
            localObject27 = (Nxt.Block)blocks.get(Long.valueOf(l7));
            if ((Nxt.Account.getId(((Nxt.Block)localObject27).generatorPublicKey) == ((BigInteger)localObject6).longValue()) && (((Nxt.Block)localObject27).totalFee > 0))
            {
              JSONObject localJSONObject5 = new JSONObject();
              localJSONObject5.put("index", convert(l7));
              localJSONObject5.put("blockTimestamp", Integer.valueOf(((Nxt.Block)localObject27).timestamp));
              localJSONObject5.put("block", convert(l7));
              localJSONObject5.put("earnedAmount", Integer.valueOf(((Nxt.Block)localObject27).totalFee));
              localJSONObject5.put("numberOfConfirmations", Integer.valueOf(i11));
              localJSONObject5.put("id", "-");
              ((JSONArray)???).add(localJSONObject5);
            }
            for (int i14 = 0; i14 < ((Nxt.Block)localObject27).transactions.length; i14++)
            {
              localObject30 = (Nxt.Transaction)transactions.get(Long.valueOf(localObject27.transactions[i14]));
              if (Nxt.Account.getId(((Nxt.Transaction)localObject30).senderPublicKey) == ((BigInteger)localObject6).longValue())
              {
                localObject31 = new JSONObject();
                ((JSONObject)localObject31).put("index", Integer.valueOf(((Nxt.Transaction)localObject30).index));
                ((JSONObject)localObject31).put("blockTimestamp", Integer.valueOf(((Nxt.Block)localObject27).timestamp));
                ((JSONObject)localObject31).put("transactionTimestamp", Integer.valueOf(((Nxt.Transaction)localObject30).timestamp));
                ((JSONObject)localObject31).put("account", convert(((Nxt.Transaction)localObject30).recipient));
                ((JSONObject)localObject31).put("sentAmount", Integer.valueOf(((Nxt.Transaction)localObject30).amount));
                if (((Nxt.Transaction)localObject30).recipient == ((BigInteger)localObject6).longValue()) {
                  ((JSONObject)localObject31).put("receivedAmount", Integer.valueOf(((Nxt.Transaction)localObject30).amount));
                }
                ((JSONObject)localObject31).put("fee", Integer.valueOf(((Nxt.Transaction)localObject30).fee));
                ((JSONObject)localObject31).put("numberOfConfirmations", Integer.valueOf(i11));
                ((JSONObject)localObject31).put("id", convert(((Nxt.Transaction)localObject30).getId()));
                ((JSONArray)???).add(localObject31);
              }
              else if (((Nxt.Transaction)localObject30).recipient == ((BigInteger)localObject6).longValue())
              {
                localObject31 = new JSONObject();
                ((JSONObject)localObject31).put("index", Integer.valueOf(((Nxt.Transaction)localObject30).index));
                ((JSONObject)localObject31).put("blockTimestamp", Integer.valueOf(((Nxt.Block)localObject27).timestamp));
                ((JSONObject)localObject31).put("transactionTimestamp", Integer.valueOf(((Nxt.Transaction)localObject30).timestamp));
                ((JSONObject)localObject31).put("account", convert(Nxt.Account.getId(((Nxt.Transaction)localObject30).senderPublicKey)));
                ((JSONObject)localObject31).put("receivedAmount", Integer.valueOf(((Nxt.Transaction)localObject30).amount));
                ((JSONObject)localObject31).put("fee", Integer.valueOf(((Nxt.Transaction)localObject30).fee));
                ((JSONObject)localObject31).put("numberOfConfirmations", Integer.valueOf(i11));
                ((JSONObject)localObject31).put("id", convert(((Nxt.Transaction)localObject30).getId()));
                ((JSONArray)???).add(localObject31);
              }
            }
            if (l7 == 2680262203532249785L) {
              break;
            }
            l7 = ((Nxt.Block)localObject27).previousBlock;
          }
          if (((JSONArray)???).size() > 0)
          {
            localObject27 = new JSONObject();
            ((JSONObject)localObject27).put("response", "processNewData");
            ((JSONObject)localObject27).put("addedMyTransactions", ???);
            ((Nxt.User)localObject1).pendingResponses.offer(localObject27);
          }
        }
        ((Nxt.User)localObject1).pendingResponses.offer(localObject7);
        break;
      default: 
        localObject5 = new JSONObject();
        ((JSONObject)localObject5).put("response", "showMessage");
        ((JSONObject)localObject5).put("message", "Incorrect request!");
        ((Nxt.User)localObject1).pendingResponses.offer(localObject5);
      }
    }
    catch (Exception localException1)
    {
      if (localObject1 != null)
      {
        localObject2 = new JSONObject();
        ((JSONObject)localObject2).put("response", "showMessage");
        ((JSONObject)localObject2).put("message", localException1.toString());
        ((Nxt.User)localObject1).pendingResponses.offer(localObject2);
      }
    }
    if (localObject1 != null) {
      synchronized (localObject1)
      {
        localObject2 = new JSONArray();
        JSONObject localJSONObject1;
        while ((localJSONObject1 = (JSONObject)((Nxt.User)localObject1).pendingResponses.poll()) != null) {
          ((JSONArray)localObject2).add(localJSONObject1);
        }
        if (((JSONArray)localObject2).size() > 0)
        {
          localObject5 = new JSONObject();
          ((JSONObject)localObject5).put("responses", localObject2);
          if (((Nxt.User)localObject1).asyncContext != null)
          {
            ((Nxt.User)localObject1).asyncContext.getResponse().setContentType("text/plain; charset=UTF-8");
            localObject6 = ((Nxt.User)localObject1).asyncContext.getResponse().getOutputStream();
            localObject7 = null;
            try
            {
              ((ServletOutputStream)localObject6).write(((JSONObject)localObject5).toString().getBytes("UTF-8"));
            }
            catch (Throwable localThrowable8)
            {
              localObject7 = localThrowable8;
              throw localThrowable8;
            }
            finally
            {
              if (localObject6 != null) {
                if (localObject7 != null) {
                  try
                  {
                    ((ServletOutputStream)localObject6).close();
                  }
                  catch (Throwable localThrowable13)
                  {
                    ((Throwable)localObject7).addSuppressed(localThrowable13);
                  }
                } else {
                  ((ServletOutputStream)localObject6).close();
                }
              }
            }
            ((Nxt.User)localObject1).asyncContext.complete();
            ((Nxt.User)localObject1).asyncContext = paramHttpServletRequest.startAsync();
            ((Nxt.User)localObject1).asyncContext.addListener(new Nxt.UserAsyncListener((Nxt.User)localObject1));
            ((Nxt.User)localObject1).asyncContext.setTimeout(5000L);
          }
          else
          {
            paramHttpServletResponse.setContentType("text/plain; charset=UTF-8");
            localObject6 = paramHttpServletResponse.getOutputStream();
            localObject7 = null;
            try
            {
              ((ServletOutputStream)localObject6).write(((JSONObject)localObject5).toString().getBytes("UTF-8"));
            }
            catch (Throwable localThrowable10)
            {
              localObject7 = localThrowable10;
              throw localThrowable10;
            }
            finally
            {
              if (localObject6 != null) {
                if (localObject7 != null) {
                  try
                  {
                    ((ServletOutputStream)localObject6).close();
                  }
                  catch (Throwable localThrowable14)
                  {
                    ((Throwable)localObject7).addSuppressed(localThrowable14);
                  }
                } else {
                  ((ServletOutputStream)localObject6).close();
                }
              }
            }
          }
        }
        else
        {
          if (((Nxt.User)localObject1).asyncContext != null)
          {
            ((Nxt.User)localObject1).asyncContext.getResponse().setContentType("text/plain; charset=UTF-8");
            localObject5 = ((Nxt.User)localObject1).asyncContext.getResponse().getOutputStream();
            localObject6 = null;
            try
            {
              ((ServletOutputStream)localObject5).write(new JSONObject().toString().getBytes("UTF-8"));
            }
            catch (Throwable localThrowable4)
            {
              localObject6 = localThrowable4;
              throw localThrowable4;
            }
            finally
            {
              if (localObject5 != null) {
                if (localObject6 != null) {
                  try
                  {
                    ((ServletOutputStream)localObject5).close();
                  }
                  catch (Throwable localThrowable15)
                  {
                    ((Throwable)localObject6).addSuppressed(localThrowable15);
                  }
                } else {
                  ((ServletOutputStream)localObject5).close();
                }
              }
            }
            ((Nxt.User)localObject1).asyncContext.complete();
          }
          ((Nxt.User)localObject1).asyncContext = paramHttpServletRequest.startAsync();
          ((Nxt.User)localObject1).asyncContext.addListener(new Nxt.UserAsyncListener((Nxt.User)localObject1));
          ((Nxt.User)localObject1).asyncContext.setTimeout(5000L);
        }
      }
    }
  }
  
  public void doPost(HttpServletRequest paramHttpServletRequest, HttpServletResponse paramHttpServletResponse)
    throws ServletException, IOException
  {
    Nxt.Peer localPeer = null;
    JSONObject localJSONObject1 = new JSONObject();
    try
    {
      localObject1 = new Nxt.CountingInputStream(paramHttpServletRequest.getInputStream());
      BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader((InputStream)localObject1, "UTF-8"));
      Object localObject3 = null;
      JSONObject localJSONObject2;
      try
      {
        localJSONObject2 = (JSONObject)JSONValue.parse(localBufferedReader);
      }
      catch (Throwable localThrowable4)
      {
        localObject3 = localThrowable4;
        throw localThrowable4;
      }
      finally
      {
        if (localBufferedReader != null) {
          if (localObject3 != null) {
            try
            {
              localBufferedReader.close();
            }
            catch (Throwable localThrowable5)
            {
              ((Throwable)localObject3).addSuppressed(localThrowable5);
            }
          } else {
            localBufferedReader.close();
          }
        }
      }
      localPeer = Nxt.Peer.addPeer(paramHttpServletRequest.getRemoteHost(), "");
      if (localPeer != null)
      {
        if (localPeer.state == 2) {
          localPeer.setState(1);
        }
        localPeer.updateDownloadedVolume(((Nxt.CountingInputStream)localObject1).getCount());
      }
      if (((Long)localJSONObject2.get("protocol")).longValue() == 1L)
      {
        localObject1 = (String)localJSONObject2.get("requestType");
        int i = -1;
        switch (((String)localObject1).hashCode())
        {
        case 1608811908: 
          if (((String)localObject1).equals("getCumulativeDifficulty")) {
            i = 0;
          }
          break;
        case -75444956: 
          if (((String)localObject1).equals("getInfo")) {
            i = 1;
          }
          break;
        case -1195538491: 
          if (((String)localObject1).equals("getMilestoneBlockIds")) {
            i = 2;
          }
          break;
        case -80817804: 
          if (((String)localObject1).equals("getNextBlockIds")) {
            i = 3;
          }
          break;
        case -2055947697: 
          if (((String)localObject1).equals("getNextBlocks")) {
            i = 4;
          }
          break;
        case 1962369435: 
          if (((String)localObject1).equals("getPeers")) {
            i = 5;
          }
          break;
        case 382446885: 
          if (((String)localObject1).equals("getUnconfirmedTransactions")) {
            i = 6;
          }
          break;
        case 1966367582: 
          if (((String)localObject1).equals("processBlock")) {
            i = 7;
          }
          break;
        case 1172622692: 
          if (((String)localObject1).equals("processTransactions")) {
            i = 8;
          }
          break;
        }
        Object localObject4;
        int m;
        Object localObject7;
        JSONArray localJSONArray;
        Iterator localIterator;
        Object localObject8;
        Object localObject5;
        switch (i)
        {
        case 0: 
          localJSONObject1.put("cumulativeDifficulty", Nxt.Block.getLastBlock().cumulativeDifficulty.toString());
          break;
        case 1: 
          if (localPeer != null)
          {
            localObject3 = (String)localJSONObject2.get("announcedAddress");
            if (localObject3 != null)
            {
              localObject3 = ((String)localObject3).trim();
              if (((String)localObject3).length() > 0) {
                localPeer.announcedAddress = ((String)localObject3);
              }
            }
            localObject4 = (String)localJSONObject2.get("application");
            if (localObject4 == null)
            {
              localObject4 = "?";
            }
            else
            {
              localObject4 = ((String)localObject4).trim();
              if (((String)localObject4).length() > 20) {
                localObject4 = ((String)localObject4).substring(0, 20) + "...";
              }
            }
            localPeer.application = ((String)localObject4);
            String str1 = (String)localJSONObject2.get("version");
            if (str1 == null)
            {
              str1 = "?";
            }
            else
            {
              str1 = str1.trim();
              if (str1.length() > 10) {
                str1 = str1.substring(0, 10) + "...";
              }
            }
            localPeer.version = str1;
            String str2 = (String)localJSONObject2.get("platform");
            if (str2 == null)
            {
              str2 = "?";
            }
            else
            {
              str2 = str2.trim();
              if (str2.length() > 10) {
                str2 = str2.substring(0, 10) + "...";
              }
            }
            localPeer.platform = str2;
            try
            {
              localPeer.shareAddress = Boolean.parseBoolean((String)localJSONObject2.get("shareAddress"));
            }
            catch (Exception localException2) {}
            if (localPeer.analyzeHallmark(paramHttpServletRequest.getRemoteHost(), (String)localJSONObject2.get("hallmark"))) {
              localPeer.setState(1);
            }
          }
          if ((myHallmark != null) && (myHallmark.length() > 0)) {
            localJSONObject1.put("hallmark", myHallmark);
          }
          localJSONObject1.put("application", "NRS");
          localJSONObject1.put("version", "0.5.3");
          localJSONObject1.put("platform", myPlatform);
          localJSONObject1.put("shareAddress", Boolean.valueOf(shareMyAddress));
          break;
        case 2: 
          localObject3 = new JSONArray();
          localObject4 = Nxt.Block.getLastBlock();
          int k = ((Nxt.Block)localObject4).height * 4 / 1461 + 1;
          while (((Nxt.Block)localObject4).height > 0)
          {
            ((JSONArray)localObject3).add(convert(((Nxt.Block)localObject4).getId()));
            for (m = 0; (m < k) && (((Nxt.Block)localObject4).height > 0); m++) {
              localObject4 = (Nxt.Block)blocks.get(Long.valueOf(((Nxt.Block)localObject4).previousBlock));
            }
          }
          localJSONObject1.put("milestoneBlockIds", localObject3);
          break;
        case 3: 
          localObject3 = new JSONArray();
          localObject4 = (Nxt.Block)blocks.get(Long.valueOf(new BigInteger((String)localJSONObject2.get("blockId")).longValue()));
          while ((localObject4 != null) && (((JSONArray)localObject3).size() < 1440))
          {
            localObject4 = (Nxt.Block)blocks.get(Long.valueOf(((Nxt.Block)localObject4).nextBlock));
            if (localObject4 != null) {
              ((JSONArray)localObject3).add(convert(((Nxt.Block)localObject4).getId()));
            }
          }
          localJSONObject1.put("nextBlockIds", localObject3);
          break;
        case 4: 
          localObject3 = new ArrayList();
          int j = 0;
          localObject7 = (Nxt.Block)blocks.get(Long.valueOf(new BigInteger((String)localJSONObject2.get("blockId")).longValue()));
          while (localObject7 != null)
          {
            localObject7 = (Nxt.Block)blocks.get(Long.valueOf(((Nxt.Block)localObject7).nextBlock));
            if (localObject7 != null)
            {
              m = 224 + ((Nxt.Block)localObject7).payloadLength;
              if (j + m > 1048576) {
                break;
              }
              ((List)localObject3).add(localObject7);
              j += m;
            }
          }
          localJSONArray = new JSONArray();
          localIterator = ((List)localObject3).iterator();
          while (localIterator.hasNext())
          {
            localObject8 = (Nxt.Block)localIterator.next();
            localJSONArray.add(((Nxt.Block)localObject8).getJSONObject(transactions));
          }
          localJSONObject1.put("nextBlocks", localJSONArray);
          break;
        case 5: 
          localObject3 = new JSONArray();
          localObject5 = peers.values().iterator();
          while (((Iterator)localObject5).hasNext())
          {
            localObject7 = (Nxt.Peer)((Iterator)localObject5).next();
            if ((((Nxt.Peer)localObject7).blacklistingTime == 0L) && (((Nxt.Peer)localObject7).announcedAddress.length() > 0) && (((Nxt.Peer)localObject7).state == 1) && (((Nxt.Peer)localObject7).shareAddress)) {
              ((JSONArray)localObject3).add(((Nxt.Peer)localObject7).announcedAddress);
            }
          }
          localJSONObject1.put("peers", localObject3);
          break;
        case 6: 
          localObject3 = new JSONArray();
          localObject5 = unconfirmedTransactions.values().iterator();
          while (((Iterator)localObject5).hasNext())
          {
            localObject7 = (Nxt.Transaction)((Iterator)localObject5).next();
            ((JSONArray)localObject3).add(((Nxt.Transaction)localObject7).getJSONObject());
          }
          localJSONObject1.put("unconfirmedTransactions", localObject3);
          break;
        case 7: 
          localObject5 = Nxt.Block.getBlock(localJSONObject2);
          boolean bool;
          if (localObject5 == null)
          {
            bool = false;
            if (localPeer != null) {
              localPeer.blacklist();
            }
          }
          else
          {
            localObject7 = ByteBuffer.allocate(224 + ((Nxt.Block)localObject5).payloadLength);
            ((ByteBuffer)localObject7).order(ByteOrder.LITTLE_ENDIAN);
            ((ByteBuffer)localObject7).put(((Nxt.Block)localObject5).getBytes());
            localJSONArray = (JSONArray)localJSONObject2.get("transactions");
            localIterator = localJSONArray.iterator();
            while (localIterator.hasNext())
            {
              localObject8 = localIterator.next();
              ((ByteBuffer)localObject7).put(Nxt.Transaction.getTransaction((JSONObject)localObject8).getBytes());
            }
            bool = Nxt.Block.pushBlock((ByteBuffer)localObject7, true);
          }
          localJSONObject1.put("accepted", Boolean.valueOf(bool));
          break;
        case 8: 
          Nxt.Transaction.processTransactions(localJSONObject2, "transactions");
          break;
        default: 
          localJSONObject1.put("error", "Unsupported request type!");
        }
      }
      else
      {
        localJSONObject1.put("error", "Unsupported protocol!");
      }
    }
    catch (Exception localException1)
    {
      localJSONObject1.put("error", localException1.toString());
    }
    paramHttpServletResponse.setContentType("text/plain; charset=UTF-8");
    Nxt.CountingOutputStream localCountingOutputStream = new Nxt.CountingOutputStream(paramHttpServletResponse.getOutputStream());
    Object localObject1 = new BufferedWriter(new OutputStreamWriter(localCountingOutputStream, "UTF-8"));
    Object localObject2 = null;
    try
    {
      localJSONObject1.writeJSONString((Writer)localObject1);
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
          catch (Throwable localThrowable6)
          {
            localObject2.addSuppressed(localThrowable6);
          }
        } else {
          ((Writer)localObject1).close();
        }
      }
    }
    if (localPeer != null) {
      localPeer.updateUploadedVolume(localCountingOutputStream.getCount());
    }
  }
  
  public void destroy()
  {
    scheduledThreadPool.shutdown();
    try
    {
      scheduledThreadPool.awaitTermination(10L, TimeUnit.SECONDS);
    }
    catch (InterruptedException localInterruptedException)
    {
      Thread.currentThread().interrupt();
    }
    if (!scheduledThreadPool.isTerminated())
    {
      logMessage("some threads didn't terminate, forcing shutdown");
      scheduledThreadPool.shutdownNow();
    }
    try
    {
      Nxt.Block.saveBlocks("blocks.nxt", true);
    }
    catch (Exception localException1)
    {
      logMessage("error saving blocks.nxt");
    }
    try
    {
      Nxt.Transaction.saveTransactions("transactions.nxt");
    }
    catch (Exception localException2)
    {
      logMessage("error saving transactions.nxt");
    }
    logMessage("Nxt stopped.");
  }
