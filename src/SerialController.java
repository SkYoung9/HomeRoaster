import java.util.ArrayList;
import java.util.List;

import com.fazecast.jSerialComm.*;

public class SerialController {
	private byte[] _initStream, _initResponse;
	private int _packetSize;
	private List<SerialListener> listeners = new ArrayList<SerialListener>();
	private boolean _connected;
	
	private SerialPort _comPort;
	PacketListener packetListener = new PacketListener();

	public SerialController(byte[] initStream, byte[] initResponse, int packetSize) {
		_initStream = initStream;
		_initResponse = initResponse;
		_packetSize = packetSize;
		_connected = false;
	}
	
	public void addListener(SerialListener obj) {
		listeners.add(obj);
	}
	
	private void comPortConnected() {
		_connected = true;
		for (SerialListener obj : listeners) {
			obj.connected();
		}
	}

	public void initComPort() {
		for (SerialPort comPort : SerialPort.getCommPorts())
		{
			/*
			System.out.println("-------------------------------");			
			System.out.println(comPort.getSystemPortName());			
			System.out.println(comPort.getDescriptivePortName());			
			System.out.println(comPort.getPortDescription());			
			System.out.println();
			*/
			if (comPort.openPort()) {
				//System.out.println("Port open");
				comPort.addDataListener(packetListener);
				comPort.writeBytes(_initStream, _packetSize);
			};
			//System.out.println("-------------------------------");
		}
	}
	
	public int packetWrite(byte[] data) {
		if (_connected) {
			return _comPort.writeBytes(data, data.length);
		}
		return 0;
	}
	
	public void packetReceived(byte[] data, SerialPort comPort) {
		//System.out.println("PacketReceived");
		if (!_connected) {
			if (compareFirstByte(data, _initResponse, _initResponse.length)) {
				_comPort = comPort;
				comPortConnected();
				//System.out.println("ComPort found");
			}
		} else {
			if (comPort != _comPort) {
				//System.out.println("ComPort closed " + comPort.getDescriptivePortName());
				comPort.closePort();
			}
		}
		
		for (SerialListener obj : listeners) {
			obj.dataReceived(data);
		}
	}
	

	private static boolean compareFirstByte(byte[] a, byte[] b, int nb) {
		for (int i = 0; i < nb; i++) {
			if (a[i] != b[i])
				return false;
		}
		return true;
	}
	

	private class PacketListener implements SerialPortPacketListener
	{
		@Override
		public int getListeningEvents() 
		{
			return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
		}

		@Override
		public int getPacketSize() 
		{ 
			return _packetSize; 
		}

		@Override
		public void serialEvent(SerialPortEvent event)
		{
			packetReceived(event.getReceivedData(), event.getSerialPort());
		}
	}
}
