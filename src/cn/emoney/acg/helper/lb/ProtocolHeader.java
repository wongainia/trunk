package cn.emoney.acg.helper.lb;

public class ProtocolHeader {
	public static final short HEAD_ID = (short) 0x9988;
	
	public short m_sHeadID ;
	public short m_sDataType;
	public int m_iDataID;
	public int m_iDataLength;
	public short m_sChecksum;
	public short m_sReserved;

	ProtocolHeader(short protocolNum)
	{
		this.m_sHeadID = HEAD_ID;
		this.m_sDataType = protocolNum;
		this.m_iDataID = 0;
		this.m_sReserved = 0;
		
		this.m_iDataLength = -1;
		this.m_sChecksum = 0;
	}
	
	ProtocolHeader(byte[] bs)
	{
		if (bs != null || bs.length >= 16) {
			byte[] temp2 = new byte[2];
			byte[] temp4 = new byte[4];
			
			System.arraycopy(bs, 0, temp2, 0, 2);
			this.m_sHeadID = DataConvert.byteArrayToShort(temp2);
			
			System.arraycopy(bs, 2, temp2, 0, 2);
			this.m_sDataType = DataConvert.byteArrayToShort(temp2);
			
			System.arraycopy(bs, 4, temp4, 0, 4);
			this.m_iDataID = DataConvert.byteArrayToInteger(temp4);
			
			System.arraycopy(bs, 8, temp4, 0, 4);
			this.m_iDataLength = DataConvert.byteArrayToInteger(temp4);
			
			System.arraycopy(bs, 12, temp2, 0, 2);
			this.m_sChecksum = DataConvert.byteArrayToShort(temp2);
			
			System.arraycopy(bs, 14, temp2, 0, 2);
			this.m_sReserved = DataConvert.byteArrayToShort(temp2);
		}
	}
	
	public byte[] toByte()
	{
		if (m_iDataLength == -1) {
			return null;
		}
		byte[] byte_HeadId = DataConvert.shortToByteArray(m_sHeadID);
		byte[] byte_DataType = DataConvert.shortToByteArray(m_sDataType);
		byte[] byte_DataId = DataConvert.integerToByteArray(m_iDataID);
		byte[] byte_DataLen = DataConvert.integerToByteArray(m_iDataLength);
		byte[] byte_CheckSum = DataConvert.shortToByteArray(m_sChecksum);
		byte[] byte_Reserved = DataConvert.shortToByteArray(m_sReserved);
		
		byte[] byteHeader = new byte[16];
		int offset = 0;
		System.arraycopy(byte_HeadId, 0, byteHeader, offset, byte_HeadId.length);
		offset = byte_HeadId.length;
		System.arraycopy(byte_DataType, 0, byteHeader, offset, byte_DataType.length);
		offset += byte_DataType.length;
		System.arraycopy(byte_DataId, 0, byteHeader, offset, byte_DataId.length);
		offset += byte_DataId.length;
		System.arraycopy(byte_DataLen, 0, byteHeader, offset, byte_DataLen.length);
		offset += byte_DataLen.length;
		System.arraycopy(byte_CheckSum, 0, byteHeader, offset, byte_CheckSum.length);
		offset += byte_CheckSum.length;
		System.arraycopy(byte_Reserved, 0, byteHeader, offset, byte_Reserved.length);
		offset += byte_Reserved.length;
		
		return byteHeader;

	}
	
}
