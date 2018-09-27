
public interface SerialListener {
	void connected();
	void dataReceived(byte[] data);
}
