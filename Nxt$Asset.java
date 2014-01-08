class Nxt$Asset
{
  long accountId;
  String name;
  String description;
  int quantity;
  
  Nxt$Asset(long paramLong, String paramString1, String paramString2, int paramInt)
  {
    this.accountId = paramLong;
    this.name = paramString1;
    this.description = paramString2;
    this.quantity = paramInt;
  }
