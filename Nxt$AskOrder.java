class Nxt$AskOrder
  implements Comparable<AskOrder>
{
  final long id;
  final long height;
  final Nxt.Account account;
  final long asset;
  volatile int quantity;
  final long price;
  
  Nxt$AskOrder(long paramLong1, Nxt.Account paramAccount, long paramLong2, int paramInt, long paramLong3)
  {
    this.id = paramLong1;
    this.height = Nxt.Block.getLastBlock().height;
    this.account = paramAccount;
    this.asset = paramLong2;
    this.quantity = paramInt;
    this.price = paramLong3;
  }
  
  public int compareTo(AskOrder paramAskOrder)
  {
    if (this.price < paramAskOrder.price) {
      return -1;
    }
    if (this.price > paramAskOrder.price) {
      return 1;
    }
    if (this.height < paramAskOrder.height) {
      return -1;
    }
    if (this.height > paramAskOrder.height) {
      return 1;
    }
    if (this.id < paramAskOrder.id) {
      return -1;
    }
    if (this.id > paramAskOrder.id) {
      return 1;
    }
    return 0;
  }
