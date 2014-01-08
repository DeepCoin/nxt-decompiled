import java.security.MessageDigest;
import java.util.Arrays;

class Nxt$Crypto
{
  static byte[] getPublicKey(String paramString)
  {
    try
    {
      byte[] arrayOfByte = new byte[32];
      Nxt.Curve25519.keygen(arrayOfByte, null, MessageDigest.getInstance("SHA-256").digest(paramString.getBytes("UTF-8")));
      return arrayOfByte;
    }
    catch (Exception localException) {}
    return null;
  }
  
  static byte[] sign(byte[] paramArrayOfByte, String paramString)
  {
    try
    {
      byte[] arrayOfByte1 = new byte[32];
      byte[] arrayOfByte2 = new byte[32];
      MessageDigest localMessageDigest = MessageDigest.getInstance("SHA-256");
      Nxt.Curve25519.keygen(arrayOfByte1, arrayOfByte2, localMessageDigest.digest(paramString.getBytes("UTF-8")));
      byte[] arrayOfByte3 = localMessageDigest.digest(paramArrayOfByte);
      localMessageDigest.update(arrayOfByte3);
      byte[] arrayOfByte4 = localMessageDigest.digest(arrayOfByte2);
      byte[] arrayOfByte5 = new byte[32];
      Nxt.Curve25519.keygen(arrayOfByte5, null, arrayOfByte4);
      localMessageDigest.update(arrayOfByte3);
      byte[] arrayOfByte6 = localMessageDigest.digest(arrayOfByte5);
      byte[] arrayOfByte7 = new byte[32];
      Nxt.Curve25519.sign(arrayOfByte7, arrayOfByte6, arrayOfByte4, arrayOfByte2);
      byte[] arrayOfByte8 = new byte[64];
      System.arraycopy(arrayOfByte7, 0, arrayOfByte8, 0, 32);
      System.arraycopy(arrayOfByte6, 0, arrayOfByte8, 32, 32);
      return arrayOfByte8;
    }
    catch (Exception localException) {}
    return null;
  }
  
  static boolean verify(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3)
  {
    try
    {
      byte[] arrayOfByte1 = new byte[32];
      byte[] arrayOfByte2 = new byte[32];
      System.arraycopy(paramArrayOfByte1, 0, arrayOfByte2, 0, 32);
      byte[] arrayOfByte3 = new byte[32];
      System.arraycopy(paramArrayOfByte1, 32, arrayOfByte3, 0, 32);
      Nxt.Curve25519.verify(arrayOfByte1, arrayOfByte2, arrayOfByte3, paramArrayOfByte3);
      MessageDigest localMessageDigest = MessageDigest.getInstance("SHA-256");
      byte[] arrayOfByte4 = localMessageDigest.digest(paramArrayOfByte2);
      localMessageDigest.update(arrayOfByte4);
      byte[] arrayOfByte5 = localMessageDigest.digest(arrayOfByte1);
      return Arrays.equals(arrayOfByte3, arrayOfByte5);
    }
    catch (Exception localException) {}
    return false;
  }
