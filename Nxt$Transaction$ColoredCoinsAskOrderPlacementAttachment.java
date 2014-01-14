import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.json.simple.JSONObject;

class Nxt$Transaction$ColoredCoinsAskOrderPlacementAttachment
  implements Nxt.Transaction.Attachment, Serializable
{
  static final long serialVersionUID = 0L;
  long asset;
  int quantity;
  long price;
  
  Nxt$Transaction$ColoredCoinsAskOrderPlacementAttachment(long paramLong1, int paramInt, long paramLong2)
  {
    this.asset = paramLong1;
    this.quantity = paramInt;
    this.price = paramLong2;
  }
  
  public int getSize()
  {
    return 20;
  }
  
  public byte[] getBytes()
  {
    ByteBuffer localByteBuffer = ByteBuffer.allocate(getSize());
    localByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    localByteBuffer.putLong(this.asset);
    localByteBuffer.putInt(this.quantity);
    localByteBuffer.putLong(this.price);
    return localByteBuffer.array();
  }
  
  public JSONObject getJSONObject()
  {
    JSONObject localJSONObject = new JSONObject();
    localJSONObject.put("asset", Nxt.convert(this.asset));
    localJSONObject.put("quantity", Integer.valueOf(this.quantity));
    localJSONObject.put("price", Long.valueOf(this.price));
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
