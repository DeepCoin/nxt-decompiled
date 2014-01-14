import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.json.simple.JSONObject;

class Nxt$Transaction$MessagingArbitraryMessageAttachment
  implements Nxt.Transaction.Attachment, Serializable
{
  static final long serialVersionUID = 0L;
  final byte[] message;
  
  Nxt$Transaction$MessagingArbitraryMessageAttachment(byte[] paramArrayOfByte)
  {
    this.message = paramArrayOfByte;
  }
  
  public int getSize()
  {
    return 4 + this.message.length;
  }
  
  public byte[] getBytes()
  {
    try
    {
      ByteBuffer localByteBuffer = ByteBuffer.allocate(getSize());
      localByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
      localByteBuffer.putInt(this.message.length);
      localByteBuffer.put(this.message);
      return localByteBuffer.array();
    }
    catch (Exception localException) {}
    return null;
  }
  
  public JSONObject getJSONObject()
  {
    JSONObject localJSONObject = new JSONObject();
    localJSONObject.put("message", Nxt.convert(this.message));
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
