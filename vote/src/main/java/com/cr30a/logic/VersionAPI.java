package com.cr30a.logic;

import com.cr30a.utils.DataUtils;

import android.content.IntentSender.SendIntentException;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class VersionAPI {
	public static final int DEFALULT_VERSION = 0x14;
	public static final int CONNECTION_EXCEPTION = -1;

	private BluetoothChatService chatService;

	private byte[] buffer = new byte[50];
  
	public VersionAPI(BluetoothChatService chatSerivce) {
		this.chatService = chatSerivce;
	}

	public int getVersion() {

		byte[] getVersionCommand = { (byte) 0xef, (byte) 0x01, (byte) 0xff,
				(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0x01,
				(byte) 0x00, (byte) 0x03, (byte) 0x37, (byte) 0x00, (byte) 0x3b };
		sendCommand(getVersionCommand);
		int versionLength = chatService.read(buffer, 4000, 100);
		Log.i("whw",
				"length2=" + versionLength + "    hex="
						+ DataUtils.toHexString(buffer));
		if (versionLength == 13) {
			return buffer[10];
		} else {
			return DEFALULT_VERSION;
		}

	}

	private void sendCommand(byte[] commandBytes) {
		chatService.write(commandBytes);
		BluetoothChatService.switchRFID = false;
	}

}
