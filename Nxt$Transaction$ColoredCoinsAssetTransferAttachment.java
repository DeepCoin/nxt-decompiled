import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.json.simple.JSONObject;

class Nxt$Transaction$ColoredCoinsAssetTransferAttachment
  implements Nxt.Transaction.Attachment, Serializable
{
  static final long serialVersionUID = 0L;
  long asset;
  int quantity;
  
  Nxt$Transaction$ColoredCoinsAssetTransferAttachment(long paramLong, int paramInt)
  {
    this.asset = paramLong;
    this.quantity = paramInt;
  }
  
  public int getSize()
  {
    return 12;
  }
  
  public byte[] getBytes()
  {
    ByteBuffer localByteBuffer = ByteBuffer.allocate(getSize());
    localByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    localByteBuffer.putLong(this.asset);
    localByteBuffer.putInt(this.quantity);
    return localByteBuffer.array();
  }
  
  public JSONObject getJSONObject()
  {
    JSONObject localJSONObject = new JSONObject();
    localJSONObject.put("asset", Nxt.convert(this.asset));
    localJSONObject.put("quantity", Integer.valueOf(this.quantity));
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
