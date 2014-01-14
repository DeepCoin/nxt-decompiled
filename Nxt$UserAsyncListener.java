import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletResponse;
import org.json.simple.JSONObject;

class Nxt$UserAsyncListener
  implements AsyncListener
{
  final Nxt.User user;
  
  Nxt$UserAsyncListener(Nxt.User paramUser)
  {
    this.user = paramUser;
  }
  
  public void onComplete(AsyncEvent paramAsyncEvent)
    throws IOException
  {}
  
  public void onError(AsyncEvent paramAsyncEvent)
    throws IOException
  {
    synchronized (this.user)
    {
      this.user.asyncContext.getResponse().setContentType("text/plain; charset=UTF-8");
      PrintWriter localPrintWriter = this.user.asyncContext.getResponse().getWriter();
      Object localObject1 = null;
      try
      {
        new JSONObject().writeJSONString(localPrintWriter);
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
      this.user.asyncContext.complete();
      this.user.asyncContext = null;
    }
  }
  
  public void onStartAsync(AsyncEvent paramAsyncEvent)
    throws IOException
  {}
  
  public void onTimeout(AsyncEvent paramAsyncEvent)
    throws IOException
  {
    synchronized (this.user)
    {
      this.user.asyncContext.getResponse().setContentType("text/plain; charset=UTF-8");
      PrintWriter localPrintWriter = this.user.asyncContext.getResponse().getWriter();
      Object localObject1 = null;
      try
      {
        new JSONObject().writeJSONString(localPrintWriter);
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
      this.user.asyncContext.complete();
      this.user.asyncContext = null;
    }
  }
