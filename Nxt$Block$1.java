import java.io.IOException;
import java.io.Writer;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

class Nxt$Block$1
  implements JSONStreamAware
{
  private char[] jsonChars = this.this$0.getJSONObject(Nxt.transactions).toJSONString().toCharArray();
  
  Nxt$Block$1(Nxt.Block paramBlock) {}
  
  public void writeJSONString(Writer paramWriter)
    throws IOException
  {
    paramWriter.write(this.jsonChars);
  }
