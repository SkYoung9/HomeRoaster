
public interface SR700Listener {
	void connected();
	void dataReceived(int control, int fanSpeed, int timer, int heat, int temp, int pidOutput);
	void timerDone();
}
