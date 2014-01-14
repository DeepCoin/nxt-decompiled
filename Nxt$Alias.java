class Nxt$Alias
{
  final Nxt.Account account;
  final long id;
  final String alias;
  volatile String uri;
  volatile int timestamp;
  
  Nxt$Alias(Nxt.Account paramAccount, long paramLong, String paramString1, String paramString2, int paramInt)
  {
    this.account = paramAccount;
    this.id = paramLong;
    this.alias = paramString1;
    this.uri = paramString2;
    this.timestamp = paramInt;
  }
