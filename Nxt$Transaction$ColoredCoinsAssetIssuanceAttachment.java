import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.json.simple.JSONObject;

class Nxt$Transaction$ColoredCoinsAssetIssuanceAttachment
  implements Nxt.Transaction.Attachment, Serializable
{
  static final long serialVersionUID = 0L;
  String name;
  String description;
  int quantity;
  
  Nxt$Transaction$ColoredCoinsAssetIssuanceAttachment(String paramString1, String paramString2, int paramInt)
  {
    this.name = paramString1;
    this.description = (paramString2 == null ? "" : paramString2);
    this.quantity = paramInt;
  }
  
  public int getSize()
  {
    try
    {
      return 1 + this.name.getBytes("UTF-8").length + 2 + this.description.getBytes("UTF-8").length + 4;
    }
    catch (Exception localException) {}
    return 0;
  }
  
  public byte[] getBytes()
  {
    try
    {
      byte[] arrayOfByte1 = this.name.getBytes("UTF-8");
      byte[] arrayOfByte2 = this.description.getBytes("UTF-8");
      ByteBuffer localByteBuffer = ByteBuffer.allocate(1 + arrayOfByte1.length + 2 + arrayOfByte2.length + 4);
      localByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
      localByteBuffer.put((byte)arrayOfByte1.length);
      localByteBuffer.put(arrayOfByte1);
      localByteBuffer.putShort((short)arrayOfByte2.length);
      localByteBuffer.put(arrayOfByte2);
      localByteBuffer.putInt(this.quantity);
      return localByteBuffer.array();
    }
    catch (Exception localException) {}
    return null;
  }
  
  public JSONObject getJSONObject()
  {
    JSONObject localJSONObject = new JSONObject();
    localJSONObject.put("name", this.name);
    localJSONObject.put("description", this.description);
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
