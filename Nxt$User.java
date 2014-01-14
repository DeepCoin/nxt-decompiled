import java.io.PrintWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import javax.servlet.AsyncContext;
import javax.servlet.ServletResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

class Nxt$User
{
  final ConcurrentLinkedQueue<JSONObject> pendingResponses = new ConcurrentLinkedQueue();
  AsyncContext asyncContext;
  volatile boolean isInactive;
  volatile String secretPhrase;
  
  void deinitializeKeyPair()
  {
    this.secretPhrase = null;
  }
  
  BigInteger initializeKeyPair(String paramString)
    throws Exception
  {
    this.secretPhrase = paramString;
    byte[] arrayOfByte = MessageDigest.getInstance("SHA-256").digest(Nxt.Crypto.getPublicKey(paramString));
    return new BigInteger(1, new byte[] { arrayOfByte[7], arrayOfByte[6], arrayOfByte[5], arrayOfByte[4], arrayOfByte[3], arrayOfByte[2], arrayOfByte[1], arrayOfByte[0] });
  }
  
  void send(JSONObject paramJSONObject)
  {
    synchronized (this)
    {
      if (this.asyncContext == null)
      {
        if (this.isInactive) {
          return;
        }
        if (this.pendingResponses.size() > 1000)
        {
          this.pendingResponses.clear();
          this.isInactive = true;
          if (this.secretPhrase == null) {
            Nxt.users.values().remove(this);
          }
          return;
        }
        this.pendingResponses.offer(paramJSONObject);
      }
      else
      {
        JSONArray localJSONArray = new JSONArray();
        JSONObject localJSONObject1;
        while ((localJSONObject1 = (JSONObject)this.pendingResponses.poll()) != null) {
          localJSONArray.add(localJSONObject1);
        }
        localJSONArray.add(paramJSONObject);
        JSONObject localJSONObject2 = new JSONObject();
        localJSONObject2.put("responses", localJSONArray);
        try
        {
          this.asyncContext.getResponse().setContentType("text/plain; charset=UTF-8");
          PrintWriter localPrintWriter = this.asyncContext.getResponse().getWriter();
          Object localObject1 = null;
          try
          {
            localJSONObject2.writeJSONString(localPrintWriter);
          }
          catch (Throwable localThrowable2)
          {
            localObject1 = localThrowable2;
            throw localThrowable2;
          }
          finally
          {
            if (localPrintWriter != null) {
              if (localObject1 != null) {
                try
                {
                  localPrintWriter.close();
                }
                catch (Throwable localThrowable3)
                {
                  localObject1.addSuppressed(localThrowable3);
                }
              } else {
                localPrintWriter.close();
              }
            }
          }
          this.asyncContext.complete();
          this.asyncContext = null;
        }
        catch (Exception localException)
        {
          Nxt.logMessage("17: " + localException.toString());
        }
      }
    }
  }
