import org.json.simple.JSONObject;

abstract interface Nxt$Transaction$Attachment
{
  public abstract byte[] getBytes();
  
  public abstract JSONObject getJSONObject();
