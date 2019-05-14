package cn.emoney.acg;

import java.io.IOException;

public class EncodeUtf8ByteToString {
	public static String EncodeUtf8ByteToString(byte[] buffer)  throws IOException
    {  
		int count = 0;
		int index = 0;
		byte a = 0;
		int utfLength = buffer.length;
		char[] result = new char[utfLength];
		
        while (count < utfLength)  
        {  
            if ((result[index] = (char)buffer[count++]) < 0x80)  
            {  
                index++;  
            }  
            else if (((a = (byte)result[index]) & 0xE0) == 0xC0)  
            {  
                if (count >= utfLength)  
                {  
                    throw new IOException("Invalid UTF-8 encoding found, start of two byte char found at end.");  
                }  
                byte b = buffer[count++];  
                if ((b & 0xC0) != 0x80)  
                {  
                    throw new IOException(  
                        "Invalid UTF-8 encoding found, byte two does not start with 0x80.");  
                }  
                result[index++] = (char)(((a & 0x1F) << 6) | (b & 0x3F));  

            }  
            else if ((a & 0xF0) == 0xE0)  {  
                if (count + 1 >= utfLength)  
                {  
                    throw new IOException(  
                        "Invalid UTF-8 encoding found, start of three byte char found at end.");  
                }  
                byte b = buffer[count++];  
                byte c = buffer[count++];  
                if (((b & 0xC0) != 0x80) || ((c & 0xC0) != 0x80))  
                {  
                    throw new IOException(  
                        "Invalid UTF-8 encoding found, byte two does not start with 0x80.");  
                }  
                result[index++] = (char)(((a & 0x0F) << 12) |  
                                          ((b & 0x3F) << 6) | (c & 0x3F));  
            }  
            else  
            {  
                throw new IOException("Invalid UTF-8 encoding found, aborting.");  
            }  
        }  
        return new String(result, 0, index); 
    }  
	
	 public static String ReadString(byte[] bytearr)  throws Exception
     {  
         int utflen = bytearr.length;  
         if (utflen > -1)  
         {  
             StringBuilder str = new StringBuilder(utflen);  
             int c, char2, char3;  
             int count = 0;  
             while (count < utflen)  
             {  
                 c = bytearr[count] & 0xff;  
                 switch (c >> 4)  
                 {  
                     case 0:  
                     case 1:  
                     case 2:  
                     case 3:  
                     case 4:  
                     case 5:  
                     case 6:  
                     case 7:  
                         count++;  
                         str.append((char)c);  
                         break;  
                     case 12:  
                     case 13:  
                         /* 110x xxxx 10xx xxxx */  
                         count += 2;  
                         if (count > utflen)  
                         {  
                             throw new IOException("Invalid UTF-8 encoding found, aborting.");  
                         }  
                         char2 = bytearr[count - 1];  
                         if ((char2 & 0xC0) != 0x80)  
                         {  
                             throw new IOException("Invalid UTF-8 encoding found, aborting.");  
                         }  
                         str.append((char)(((c & 0x1F) << 6) | (char2 & 0x3F)));  
                         break;  
                     case 14:  
                         /* 1110 xxxx 10xx xxxx 10xx xxxx */  
                         count += 3;  
                         if (count > utflen)  
                         {  
                             throw new IOException("Invalid UTF-8 encoding found, aborting.");  
                         }  
                         char2 = bytearr[count - 2];  
                         char3 = bytearr[count - 1];  
                         if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))  
                         {  
                             throw new IOException("Invalid UTF-8 encoding found, aborting.");  
                         }  
                         str.append((char)(((c & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0)));  
                         break;  
                     default:  
                         /* 10xx xxxx, 1111 xxxx */  
                         throw new IOException("Invalid UTF-8 encoding found, aborting.");  
                 }  
             }  
             return str.toString();  
         }  
         else  
         {  
             return null;  
         }  
     }  
	 
	 public static byte[] EncodeStringToUtf8Byte(String text)  throws Exception
     {  
         if (text != null)  
         {  
             char[] charr = text.toCharArray();  
             int utfLength = CountUtf8Bytes(charr);  
             if (utfLength > Integer.MAX_VALUE)  
             {  
                 throw new IOException(  
                     String.format(  
                         "Cannot marshall an encoded string longer than: {0} bytes, supplied" +  
                         "string requires: {1} characters to encode", Integer.MAX_VALUE, utfLength));  
             }  
             byte[] bytearr = new byte[utfLength];  
             EncodeUTF8toBuffer(charr, bytearr);  
             return bytearr;  
         }  
         else  
         {  
            return new byte[0];  
         }  
     }  
	 
	 private static int CountUtf8Bytes(char[] chars) {
		 int utfLength = 0;
		 int c = 0;
		 for(int i = 0; i < chars.length; i++) {
			 c = chars[i];
			 if((c >= 0x00001) && (c <= 0x007f)) {
				 utfLength++;
			 }
			 else if(c > 0x07ff) {
				 utfLength+=3;
			 }else {
				 utfLength+=2;
			 }
		 }
		 return utfLength;
	 }
	 
	 private static void EncodeUTF8toBuffer(char[] chars, byte[] buffer)  
     {  
         int c = 0;  
         int count = 0;  
         for (int i = 0; i < chars.length; i++)  
         {  
             c = chars[i];  
             if ((c >= 0x0001) && (c <= 0x007F))  
             {  
                 buffer[count++] = (byte)c;  
             }  
             else if (c > 0x07FF)  
             {  
                 buffer[count++] = (byte)(0xE0 | ((c >> 12) & 0x0F));  
                 buffer[count++] = (byte)(0x80 | ((c >> 6) & 0x3F));  
                 buffer[count++] = (byte)(0x80 | ((c >> 0) & 0x3F));  
             }  
             else  
             {  
                 buffer[count++] = (byte)(0xC0 | ((c >> 6) & 0x1F));  
                 buffer[count++] = (byte)(0x80 | ((c >> 0) & 0x3F));  
             }  
         }  
     }  
}
