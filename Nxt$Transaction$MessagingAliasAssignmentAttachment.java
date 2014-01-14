import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.json.simple.JSONObject;

class Nxt$Transaction$MessagingAliasAssignmentAttachment
  implements Nxt.Transaction.Attachment, Serializable
{
  static final long serialVersionUID = 0L;
  final String alias;
  final String uri;
  
  Nxt$Transaction$MessagingAliasAssignmentAttachment(String paramString1, String paramString2)
  {
    this.alias = paramString1;
    this.uri = paramString2;
  }
  
  public int getSize()
  {
    try
    {
      return 1 + this.alias.getBytes("UTF-8").length + 2 + this.uri.getBytes("UTF-8").length;
    }
    catch (Exception localException) {}
    return 0;
  }
  
  public byte[] getBytes()
  {
    try
    {
      byte[] arrayOfByte1 = this.alias.getBytes("UTF-8");
      byte[] arrayOfByte2 = this.uri.getBytes("UTF-8");
      ByteBuffer localByteBuffer = ByteBuffer.allocate(1 + arrayOfByte1.length + 2 + arrayOfByte2.length);
      localByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
      localByteBuffer.put((byte)arrayOfByte1.length);
      localByteBuffer.put(arrayOfByte1);
      localByteBuffer.putShort((short)arrayOfByte2.length);
      localByteBuffer.put(arrayOfByte2);
      return localByteBuffer.array();
    }
    catch (Exception localException) {}
    return null;
  }
  
  public JSONObject getJSONObject()
  {
    JSONObject localJSONObject = new JSONObject();
    localJSONObject.put("alias", this.alias);
    localJSONObject.put("uri", this.uri);
    return localJSONObject;
  }
  
  public long getRecipientDeltaBalance()
  {
    return 0L;
  }
  
  public long getSenderDeltaBalance()
  {
    return 0L;
  }
