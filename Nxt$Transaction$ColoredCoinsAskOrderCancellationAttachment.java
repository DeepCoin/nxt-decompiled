import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.json.simple.JSONObject;

class Nxt$Transaction$ColoredCoinsAskOrderCancellationAttachment
  implements Nxt.Transaction.Attachment, Serializable
{
  static final long serialVersionUID = 0L;
  long order;
  
  Nxt$Transaction$ColoredCoinsAskOrderCancellationAttachment(long paramLong)
  {
    this.order = paramLong;
  }
  
  public int getSize()
  {
    return 8;
  }
  
  public byte[] getBytes()
  {
    ByteBuffer localByteBuffer = ByteBuffer.allocate(getSize());
    localByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    localByteBuffer.putLong(this.order);
    return localByteBuffer.array();
  }
  
  public JSONObject getJSONObject()
  {
    JSONObject localJSONObject = new JSONObject();
    localJSONObject.put("order", Nxt.convert(this.order));
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
