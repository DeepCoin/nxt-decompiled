class Nxt$BidOrder
  implements Comparable<BidOrder>
{
  final long id;
  final long height;
  final Nxt.Account account;
  final long asset;
  volatile int quantity;
  final long price;
  
  Nxt$BidOrder(long paramLong1, Nxt.Account paramAccount, long paramLong2, int paramInt, long paramLong3)
  {
    this.id = paramLong1;
    this.height = Nxt.Block.getLastBlock().height;
    this.account = paramAccount;
    this.asset = paramLong2;
    this.quantity = paramInt;
    this.price = paramLong3;
  }
  
  public int compareTo(BidOrder paramBidOrder)
  {
    if (this.price > paramBidOrder.price) {
      return -1;
    }
    if (this.price < paramBidOrder.price) {
      return 1;
    }
    if (this.height < paramBidOrder.height) {
      return -1;
    }
    if (this.height > paramBidOrder.height) {
      return 1;
    }
    if (this.id < paramBidOrder.id) {
      return -1;
    }
    if (this.id > paramBidOrder.id) {
      return 1;
    }
    return 0;
  }
