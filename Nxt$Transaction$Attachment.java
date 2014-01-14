import org.json.simple.JSONObject;

abstract interface Nxt$Transaction$Attachment
{
  public abstract int getSize();
  
  public abstract byte[] getBytes();
  
  public abstract JSONObject getJSONObject();
  
  public abstract long getRecipientDeltaBalance();
  
  public abstract long getSenderDeltaBalance();
