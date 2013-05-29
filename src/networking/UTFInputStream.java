package networking;

import java.io.IOException;
import java.io.InputStream;


public class UTFInputStream{

	private InputStream is;

	public UTFInputStream(InputStream stream) {
		is = stream;
	}

	public String readUTF8() throws IOException {

		while (is.available() < 4) {
			Thread.yield();
		}
		byte[] tmp = new byte[4];
		is.read(tmp);

		int len = 0;
		len |= (tmp[3] & 0xff);
		len <<= 8;
		len |= (tmp[2] & 0xff);
		len <<= 8;
		len |= (tmp[1] & 0xff);
		len <<= 8;
		len |= (tmp[0] & 0xff);

		byte[] bytes = new byte[len];
		while (is.available() < len) {
			Thread.yield();
		}
		is.read(bytes);
		String message = new String(bytes, "UTF-8");
		return message;
	}
	
	public void close() throws IOException {
		this.is.close();
	}
	
}


