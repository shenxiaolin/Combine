package com.cr30a.logic;

import android.os.Environment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

//import cn.com.shptbm.DecodeWlt;

public class ParseSFZAPI {
	private static final byte[] command = "D&C00040101".getBytes();

	private static final String SUCCESS = "AAAAAA96690508000090";

	public static final int DATA_SIZE = 1295;
	
	private byte[] buffer = new byte[20];
	private byte[] buffer2 = new byte[20];
	private byte[] buffer3 = new byte[DATA_SIZE];
	
	private BluetoothChatService chatService;

	
	private String path = Environment.getExternalStorageDirectory()
			+ File.separator + "wltlib";
	
	public ParseSFZAPI(BluetoothChatService chatService){
		this.chatService = chatService;
	}


	private Result result;

	/**
	 * ��ȡ���֤��Ϣ���˷���Ϊ�����ģ�����������̴߳���
	 */
	public Result read2() {
		result = new Result();
		//AA AA AA 96 69 00 03 20 01 22
		

		byte[] bKey = new byte[10];
		bKey[0]= (byte)(0XAA&0xFF);
		bKey[1]= (byte)(0XAA&0xFF);
		bKey[2]= (byte)(0XAA&0xFF);
		bKey[3]= (byte)(0X96&0xFF);
		bKey[4]= 0X69;
		bKey[5]= 0X00;
		bKey[6]= 0X03;
		bKey[7]= 0X20;
		bKey[8]= 0X01;
		bKey[9]= 0X22;
		//String kbuffer = DataUtils.toHexString(bKey);
		
		
		chatService.write(bKey);
		BluetoothChatService.switchRFID = false;
		int length = chatService.read(buffer, 1000,300);
		if (length == 0) {
			result.confirmationCode = Result.TIME_OUT;
			return result;
		}
		//AA AA AA 96 69 00 03 20 02 21
		byte[] bKey2 = new byte[10];
		bKey2[0]= (byte)(0XAA&0xFF);
		bKey2[1]= (byte)(0XAA&0xFF);
		bKey2[2]= (byte)(0XAA&0xFF);
		bKey2[3]= (byte)(0X96&0xFF);
		bKey2[4]= 0X69;
		bKey2[5]= 0X00;
		bKey2[6]= 0X03;
		bKey2[7]= 0X20;
		bKey2[8]= 0X02;
		bKey2[9]= 0X21;

		chatService.write(bKey2);
		BluetoothChatService.switchRFID = false;
		int length2 = chatService.read(buffer2, 1000,300);
		if (length2 == 0) {
			result.confirmationCode = Result.TIME_OUT;
			return result;
		}


		return result;
	}

	/**
	 * ��ȡ���֤��Ϣ���˷���Ϊ�����ģ�����������̴߳���
	 */
	public Result read() {
		result = new Result();
		//AA AA AA 96 69 00 03 20 01 22
				
		
				byte[] bKey = new byte[10];
				bKey[0]= (byte)(0XAA&0xFF);
				bKey[1]= (byte)(0XAA&0xFF);
				bKey[2]= (byte)(0XAA&0xFF);
				bKey[3]= (byte)(0X96&0xFF);
				bKey[4]= 0X69;
				bKey[5]= 0X00;
				bKey[6]= 0X03;
				bKey[7]= 0X20;
				bKey[8]= 0X01;
				bKey[9]= 0X22;
				//String kbuffer = DataUtils.toHexString(bKey);
				
				
				chatService.write(bKey);
				BluetoothChatService.switchRFID = false;
				int length = chatService.read(buffer, 1000,300);
				if (length == 0) {
					result.confirmationCode = Result.TIME_OUT;
					return result;
				}
				//AA AA AA 96 69 00 03 20 02 21
				byte[] bKey2 = new byte[10];
				bKey2[0]= (byte)(0XAA&0xFF);
				bKey2[1]= (byte)(0XAA&0xFF);
				bKey2[2]= (byte)(0XAA&0xFF);
				bKey2[3]= (byte)(0X96&0xFF);
				bKey2[4]= 0X69;
				bKey2[5]= 0X00;
				bKey2[6]= 0X03;
				bKey2[7]= 0X20;
				bKey2[8]= 0X02;
				bKey2[9]= 0X21;
		
				chatService.write(bKey2);
				BluetoothChatService.switchRFID = false;
				int length2 = chatService.read(buffer2, 1000,300);
				if (length2 == 0) {
					result.confirmationCode = Result.TIME_OUT;
					return result;
				}

		//AA AA AA 96 69 00 03 30 01 32

		byte[] bKey3 = new byte[10];
		bKey3[0]= (byte)(0XAA&0xFF);
		bKey3[1]= (byte)(0XAA&0xFF);
		bKey3[2]= (byte)(0XAA&0xFF);
		bKey3[3]= (byte)(0X96&0xFF);
		bKey3[4]= 0X69;
		bKey3[5]= 0X00;
		bKey3[6]= 0X03;
		bKey3[7]= 0X30;
		bKey3[8]= 0X01;
		bKey3[9]= 0X32;
		chatService.write(bKey3);
		BluetoothChatService.switchRFID = false;
		int length3 = chatService.read(buffer3, 5000,300);
		if (length3 == 0) {
			result.confirmationCode = Result.TIME_OUT;
			return result;
		}

		People people = decode(buffer3);
		if (people == null) {
			result.confirmationCode = Result.FIND_FAIL;
		} else {
			result.confirmationCode = Result.SUCCESS;
			result.resultInfo = people;
		}
		return result;
	}

	private People decode(byte[] buffer) {
		if (buffer == null) {
			return null;
		}
		byte[] b = new byte[10];
		System.arraycopy(buffer, 0, b, 0, 10);
		String result = toHexString(b);
		People people = null;
		if (result.equalsIgnoreCase(SUCCESS)) {
			byte[] data = new byte[buffer.length - 10];
			System.arraycopy(buffer, 10, data, 0, buffer.length - 10);
			people = decodeInfo(data);
		}
		return people;

	}

	private People decodeInfo(byte[] buffer) {
		short textSize = getShort(buffer[0], buffer[1]);
		short imageSize = getShort(buffer[2], buffer[3]);
		byte[] text = new byte[textSize];
		System.arraycopy(buffer, 4, text, 0, textSize);
		byte[] image = new byte[imageSize];
		System.arraycopy(buffer, 4 + textSize, image, 0, imageSize);
		People people = null;
		try {
			String temp = null;
			people = new People();
			// ����
			temp = new String(text, 0, 30, "UTF-16LE").trim();
			people.setPeopleName(temp);

			// �Ա�
			temp = new String(text, 30, 2, "UTF-16LE");
			if (temp.equals("1"))
				temp = "\u7537";//��
			else
				temp = "\u5973";//"Ů";
			people.setPeopleSex(temp);

			// ����
			temp = new String(text, 32, 4, "UTF-16LE");
			try {
				int code = Integer.parseInt(temp.toString());
				temp = decodeNation(code);
			} catch (Exception e) {
				temp = "";
			}
			people.setPeopleNation(temp);

			// ����
			temp = new String(text, 36, 16, "UTF-16LE").trim();
			people.setPeopleBirthday(temp);

			// סַ
			temp = new String(text, 52, 70, "UTF-16LE").trim();
			people.setPeopleAddress(temp);

			// ���֤��
			temp = new String(text, 122, 36, "UTF-16LE").trim();
			people.setPeopleIDCode(temp);

			// ǩ������
			temp = new String(text, 158, 30, "UTF-16LE").trim();
			people.setDepartment(temp);

			// ��Ч��ʼ����
			temp = new String(text, 188, 16, "UTF-16LE").trim();
			people.setStartDate(temp);

			// ��Ч��ֹ����
			temp = new String(text, 204, 16, "UTF-16LE").trim();
			people.setEndDate(temp);
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			return null;
		}

		people.setPhoto(parsePhoto(image));
		return people;
	}

	private String decodeNation(int code) {
		String nation;
		switch (code) {
		case 1:
			nation = "\u6c49";//"��";
			break;
		case 2:
			nation = "\u8499\u53e4";//"�ɹ�";
			break;
		case 3:
			nation = "\u56de";//"��";
			break;
		case 4:
			nation = "\u85cf";//"��";
			break;
		case 5:
			nation = "\u7ef4\u543e\u5c14";//"ά���";
			break;
		case 6:
			nation = "\u82d7";//"��";
			break;
		case 7:
			nation = "\u5f5d";//"��";
			break;
		case 8:
			nation = "\u58ee";//"׳";
			break;
		case 9:
			nation = "\u5e03\u4f9d";//"����";
			break;
		case 10:
			nation = "\u671d\u9c9c";//"����";
			break;
		case 11:
			nation = "\u6ee1";//"��";
			break;
		case 12:
			nation = "\u4f97";//"��";
			break;
		case 13:
			nation = "\u7476";//"��";
			break;
		case 14:
			nation = "\u767d";//"��";
			break;
		case 15:
			nation = "\u571f\u5bb6";//"����";
			break;
		case 16:
			nation = "\u54c8\u5c3c";//"����";
			break;
		case 17:
			nation = "\u54c8\u8428\u514b";//"������";
			break;
		case 18:
			nation = "\u50a3";//"��";
			break;
		case 19:
			nation = "\u9ece";//"��";
			break;
		case 20:
			nation = "\u5088\u50f3";//"����";
			break;
		case 21:
			nation = "\u4f64";//"��";
			break;
		case 22:
			nation = "\u7572";//"�";
			break;
		case 23:
			nation = "\u9ad8\u5c71";//"��ɽ";
			break;
		case 24:
			nation = "\u62c9\u795c";//"����";
			break;
		case 25:
			nation = "\u6c34";//"ˮ";
			break;
		case 26:
			nation = "\u4e1c\u4e61";//"����";
			break;
		case 27:
			nation = "\u7eb3\u897f";//"����";
			break;
		case 28:
			nation = "\u666f\u9887";//"����";
			break;
		case 29:
			nation = "\u67ef\u5c14\u514b\u5b5c";//"�¶�����";
			break;
		case 30:
			nation = "\u571f";//"��";
			break;
		case 31:
			nation = "\u8fbe\u65a1\u5c14";//"���Ӷ�";
			break;
		case 32:
			nation = "\u4eeb\u4f6c";//"����";
			break;
		case 33:
			nation = "\u7f8c";//"Ǽ";
			break;
		case 34:
			nation = "\u5e03\u6717";//"����";
			break;
		case 35:
			nation = "\u6492\u62c9";//"����";
			break;
		case 36:
			nation = "\u6bdb\u5357";//"ë��";
			break;
		case 37:
			nation = "\u4ee1\u4f6c";//"����";
			break;
		case 38:
			nation = "\u9521\u4f2f";//"����";
			break;
		case 39:
			nation = "\u963f\u660c";//"����";
			break;
		case 40:
			nation = "\u666e\u7c73";//"����";
			break;
		case 41:
			nation = "\u5854\u5409\u514b";//"������";
			break;
		case 42:
			nation = "\u6012";//"ŭ";
			break;
		case 43:
			nation = "\u4e4c\u5b5c\u522b\u514b";//"���α��";
			break;
		case 44:
			nation = "\u4fc4\u7f57\u65af";//"����˹";
			break;
		case 45:
			nation = "\u9102\u6e29\u514b";//"���¿�";
			break;
		case 46:
			nation = "\u5fb7\u6602";//"�°�";
			break;
		case 47:
			nation = "\u4fdd\u5b89";//"����";
			break;
		case 48:
			nation = "\u88d5\u56fa";//"ԣ��";
			break;
		case 49:
			nation = "\u4eac";//"��";
			break;
		case 50:
			nation = "\u5854\u5854\u5c14";//"������";
			break;
		case 51:
			nation = "\u72ec\u9f99";//"����";
			break;
		case 52:
			nation = "\u9102\u4f26\u6625";//"���״�";
			break;
		case 53:
			nation = "\u8d6b\u54f2";//"����";
			break;
		case 54:
			nation = "\u95e8\u5df4";//"�Ű�";
			break;
		case 55:
			nation = "\u73de\u5df4";//"���";
			break;
		case 56:
			nation = "\u57fa\u8bfa";//"��ŵ";
			break;
		case 97:
			nation = "\u5176\u4ed6";//"����";
			break;
		case 98:
			nation = "\u5916\u56fd\u8840\u7edf\u4e2d\u56fd\u7c4d\u4eba\u58eb";//"���Ѫͳ�й�����ʿ";
			break;
		default:
			nation = "";
		}

		return nation;
	}

	/**
	 * ����ת��16�����ַ���
	 * 
	 * @param b
	 * @return
	 */
	public static String toHexString(byte[] b) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < b.length; i++) {
			buffer.append(toHexString1(b[i]));
		}
		return buffer.toString();
	}

	public static String toHexString1(byte b) {
		String s = Integer.toHexString(b & 0xFF);
		if (s.length() == 1) {
			return "0" + s;
		} else {
			return s;
		}
	}

	private short getShort(byte b1, byte b2) {
		short temp = 0;
		temp |= (b1 & 0xff);
		temp <<= 8;
		temp |= (b2 & 0xff);
		return temp;
	}


	private byte[] parsePhoto(byte[] wltdata) {
		String bmpPath = path + File.separator + "zp.bmp";
		String wltPath = path + File.separator + "zp.wlt";
		if (!isExistsParsePath(wltPath, wltdata)) {
			return null;
		}
		android.util.Log.d("wy","bmpPath= "+bmpPath);
		android.util.Log.d("wy","wltPath= "+wltPath);

		return null;
//		int result = DecodeWlt.Wlt2Bmp(wltPath, bmpPath);
//		if (result == 1) {
//			byte[] image = getBytes(bmpPath);
//			return image;
//		} else {
//			return null;
//		}
	
	}
	
	private boolean isExistsParsePath(String wltPath, byte[] wltdata) {
		File myFile = new File(path);
		boolean isMKDir = true;
		if (!myFile.exists()) {
			isMKDir = myFile.mkdir();
		}
		if (!isMKDir) {
			return false;
		}

		File wltFile = new File(wltPath);
		boolean isCreate = true;
		if (!wltFile.exists()) {
			try {
				isCreate = wltFile.createNewFile();
			} catch (IOException e) {
				isCreate = false;
				e.printStackTrace();
			}
		}
		if (!isCreate) {
			return false;
		}

		boolean isWriteData = false;
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(wltFile);
			fos.write(wltdata);
			isWriteData = true;
		} catch (FileNotFoundException e) {
			isWriteData = false;
			e.printStackTrace();
		} catch (IOException e) {
			isWriteData = false;
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return isWriteData;
	}

	/**
	 * ���ָ���ļ���byte����
	 */
	public static byte[] getBytes(String filePath) {
		byte[] buffer = null;
		try {
			File file = new File(filePath);
			FileInputStream fis = new FileInputStream(file);
			ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
			byte[] b = new byte[1000];
			int n;
			while ((n = fis.read(b)) != -1) {
				bos.write(b, 0, n);
			}
			fis.close();
			bos.close();
			buffer = bos.toByteArray();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return buffer;
	}

	public static class People {
		/**
		 * ����
		 */
		private String peopleName;

		/**
		 * �Ա�
		 */
		private String peopleSex;

		/**
		 * ����
		 */
		private String peopleNation;

		/**
		 * ��������
		 */
		private String peopleBirthday;

		/**
		 * סַ
		 */
		private String peopleAddress;

		/**
		 * ���֤��
		 */
		private String peopleIDCode;

		/**
		 * ǩ������
		 */
		private String department;

		/**
		 * ��Ч���ޣ���ʼ
		 */
		private String startDate;

		/**
		 * ��Ч���ޣ�����
		 */
		private String endDate;

		/**
		 * ���֤ͷ��
		 */
		private byte[] photo;

		public String getPeopleName() {
			return peopleName;
		}

		public void setPeopleName(String peopleName) {
			this.peopleName = peopleName;
		}

		public String getPeopleSex() {
			return peopleSex;
		}

		public void setPeopleSex(String peopleSex) {
			this.peopleSex = peopleSex;
		}

		public String getPeopleNation() {
			return peopleNation;
		}

		public void setPeopleNation(String peopleNation) {
			this.peopleNation = peopleNation;
		}

		public String getPeopleBirthday() {
			return peopleBirthday;
		}

		public void setPeopleBirthday(String peopleBirthday) {
			this.peopleBirthday = peopleBirthday;
		}

		public String getPeopleAddress() {
			return peopleAddress;
		}

		public void setPeopleAddress(String peopleAddress) {
			this.peopleAddress = peopleAddress;
		}

		public String getPeopleIDCode() {
			return peopleIDCode;
		}

		public void setPeopleIDCode(String peopleIDCode) {
			this.peopleIDCode = peopleIDCode;
		}

		public String getDepartment() {
			return department;
		}

		public void setDepartment(String department) {
			this.department = department;
		}

		public String getStartDate() {
			return startDate;
		}

		public void setStartDate(String startDate) {
			this.startDate = startDate;
		}

		public String getEndDate() {
			return endDate;
		}

		public void setEndDate(String endDate) {
			this.endDate = endDate;
		}

		public byte[] getPhoto() {
			return photo;
		}

		public void setPhoto(byte[] photo) {
			this.photo = photo;
		}

	}
	
	public static class Result {
		/**
		 * 1: �ɹ�
		 */
		public static final int SUCCESS = 1;
		/**
		 * 2��ʧ��
		 */
		public static final int FIND_FAIL = 2;
		/**
		 *  3: ��ʱ
		 */
		public static final int TIME_OUT = 3;
		/**
		 * 4�������쳣
		 */
		public static final int OTHER_EXCEPTION = 4;

		/**
		 * ȷ���� 1: �ɹ� 2��ʧ�� 3: ��ʱ 4�������쳣
		 */
		public int confirmationCode;

		/**
		 * �����:��ȷ����Ϊ1ʱ�����ж��Ƿ��н��
		 */
		public Object resultInfo;
	}
}
