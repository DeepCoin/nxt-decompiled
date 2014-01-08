class Nxt$Alias
{
  final Nxt.Account account;
  final String alias;
  volatile String uri;
  volatile int timestamp;
  
  Nxt$Alias(Nxt.Account paramAccount, String paramString1, String paramString2, int paramInt)
  {
    this.account = paramAccount;
    this.alias = paramString1;
    this.uri = paramString2;
    this.timestamp = paramInt;
  }
