import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class SR700Controller implements SerialListener {
	final static int PACKET_SIZE = 14;
	final static byte[] INIT = {(byte) 0xAA, 0x55, 0x61, 0x74, 0x63, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xAA, (byte) 0xFA};
	final static byte[] INIT_RESPONSE = {(byte) 0xAA, (byte) 0xAA, 0x61, 0x74};
	final static int CONTROL_SLEEP = 1;
	final static int CONTROL_IDLE = 2;
	final static int CONTROL_ROAST = 3;
	final static int CONTROL_COOL = 4;
	final static double K_ULTIMATE = 1.5, OUT_LOWER = 0.0, OUT_UPPER = 100.0;

	private SerialController controller;
	private PIDController _pid;
	private List<SR700Listener> listeners = new ArrayList<SR700Listener>();
	private List<SR700RoastListener> roastListeners = new ArrayList<SR700RoastListener>();
	private boolean _initalized;
	private SR700State _currentState = null; 
	private SR700State _newState = null;
	private int _pidOutput = 0;

	public SR700Controller()
	{
		controller = new SerialController(INIT, INIT_RESPONSE, PACKET_SIZE);
		controller.addListener(this);
		
		_currentState = new SR700State();
		_currentState.setControl(CONTROL_IDLE);
		_initalized = false;
		
		_pid = new PIDController(0.6 * K_ULTIMATE, 0.5*K_ULTIMATE, 0.6*K_ULTIMATE, OUT_LOWER, OUT_UPPER);
		_pid.setToAuto(false); // set to manual mode
	}
	
	public void addListener(SR700Listener obj) {
		listeners.add(obj);
	}

	public void addListener(SR700RoastListener obj) {
		roastListeners.add(obj);
	}
	
	/**
	 * Sends initialization packet to the controller.
	 */		
	public void init() {
		controller.initComPort();
	}
	
	public int getControlState() {
		return _currentState.getControl();
	}
	
/*
	public void sendManualData(int control, int fanSpeed, int heat, int timer) {
		_newState = new SR700State();
		_newState.setControl(control);
		_newState.setFanSpeed(fanSpeed);
		_newState.setHeat(heat);
		_newState.setTimer(timer);
	}
*/

	public void SendAutoData(int timer, int targetTemp, boolean startAfterTemp) {
		SR700State state = new SR700State(timer, targetTemp, startAfterTemp);
		_newState = state;
		_pid.setToAuto(true);
	}
	
	/**
	 * Check if the controller is initialized
	 * Update the current state with the new state.
	 * Update the current state with the PID.
	 * Send data packet from the current state to the controller.
	 */		
	private int sendPacket(byte[] data) {
		if (!_initalized)
			return 0;	
		
		// Updates the current state with the new state.
		if (_newState != null) {
			_currentState = _newState;
			_currentState.startTimer();
			_newState = null;
		}
		
		// Updates the current state with the PID.
		pidUpdate(_currentState);
		
		// Sends data packet from the current state to the controller.
		data = _currentState.toPacket(data);
		
	    System.out.print("Sending  ");
		for (byte b : data)
			System.out.print(String.format("%02x ", b));
		System.out.println();
		
		return controller.packetWrite(data);
	}
	
	private void timerDone() {
		for (SR700RoastListener obj : roastListeners) {
			obj.stepComplete();;
		}
	}

	@Override
	public void connected() {
		for (SR700Listener obj : listeners) {
			obj.connected();
		}
	}

	/**
	 * When data is received:
	 * Initialize the machine is initialized.
	 * 
	 */		
	@Override
	public void dataReceived(byte[] data) {
		
		System.out.print("Receiving ");
		for (byte b : data)
			System.out.print(String.format("%02x ", b));
		System.out.println();
			
		if (!_initalized && (data[4] == (byte) 0xAF || data[4] == (byte) 0x11))
		{
			_initalized = true;
		}
		//System.out.print("Sent " + sendPacket());
		_currentState.setTempFromData(data);
		sendPacket(data);
		
		if (data[4] != (byte) 0x00) {
			return;
		}
		//System.out.println("GOOD ");
		
		SR700State state = new SR700State(data);
		
		for (SR700Listener obj : listeners) {
			obj.dataReceived(state.getControl(), state.getFanSpeed(), state.getTimer(), state.getHeat(), state.getTemp(), _pidOutput);
		}
		System.out.println("Done Receiving");
	}
	
	private void pidUpdate(SR700State state) {
		if (state.getTemp() == 249) {
			System.out.println("PID Updating");
		}
		if (!_pid.getAuto())
			return;
		
		int fanSpeed = 0;
		_pidOutput = (int) (_pid.getOuput(state.getTargetTemp(), state.getTemp()) + 0.5);
		
		state.setControl(CONTROL_ROAST);
		if (_pidOutput > 50.0) {
			// Modulate fanspeed. 9 if ouput is 50, and decrease fanspeed for every 10 output
			// up to 10.
			fanSpeed = 14 - _pidOutput / 10;
			state.setFanSpeed(fanSpeed);
			state.setHeat(3);
		}
		else if (_pidOutput > 30.0) {
			state.setFanSpeed(9);
			state.setHeat(2);
		}
		else if (_pidOutput > 10.0) {
			state.setFanSpeed(9);
			state.setHeat(1);
		}
		else {
			state.setFanSpeed(9);
			state.setHeat(0);
			//state.setControl(CONTROL_COOL);
		}

		if (state.getTemp() == 249) {
			System.out.println("PID Updated");
		}
	}

	private class SR700State {
		private final int CONTROL_INDEX = 5, FAN_INDEX = 7, TIMER_INDEX = 8, HEAT_INDEX = 9;
		private final byte CONTROL_SLEEP_BYTE1 = 0x08, CONTROL_SLEEP_BYTE2 = 0x01;
		private final byte CONTROL_IDLE_BYTE1 = 0x02, CONTROL_IDLE_BYTE2 = 0x01;
		private final byte CONTROL_COOL_BYTE1 = 0x04, CONTROL_COOL_BYTE2 = 0x04;
		private final byte CONTROL_ROAST_BYTE1 = 0x04, CONTROL_ROAST_BYTE2 = 0x02;
		
		private int _control, _fanSpeed, _timer, _heat, _temp, _targetTemp;
		private byte[] _outputPacket = {(byte) 0xAA, (byte) 0xAA, 0x61, 0x74, (byte) 0x63, 0, 0, 0, 0, 0, 0, 0, (byte) 0xAA, (byte) 0xFA};
		private long _startTime = 0;
		private int _startTimer = 0;
		private boolean _startAfterTemp = false;
		
		public SR700State() {
			
		}
		
		public SR700State(int timer, int targetTemp, boolean startAfterTemp) {
			this.setTimer(timer);
			this.setTargetTemp(targetTemp);
			this.setStartAfterTemp(startAfterTemp);
		}

		public SR700State(byte[] data) {
			this.setFromBytes(data);
		}
		
		public void startTimer() {
			// Check if already started
			if (_startTime > 0) {
				return;
			}

			// Check timer should start when target temp is reached
			if (_startAfterTemp && _temp < _targetTemp) {
				return;
			}

			// Update start time and start timer
			_startTime = System.nanoTime();
			_startTimer = _timer;
			//System.out.println(System.nanoTime() + " to " + endTime + " " + timer + " " + ((endTime - System.nanoTime())/1000000000));
		}
		
		public void updateState() {
			long deltaTime = System.nanoTime() - _startTime;
			double deltaSeconds = deltaTime / 1000000000.0;
					
			_timer = _startTimer - (int) deltaSeconds;
			if (_timer <= 0) {
				timerDone();
				_timer = 0;
			}
			//System.out.println("Time left " + timer + " Time elapsed " + seconds);
		}
			
		public byte[] toPacket(byte[] data) {
			updateState();
			//byte[] data = {(byte) 0xAA, (byte) 0xAA, 0x61, 0x74, (byte) 0x63, 0, 0, 0, 0, 0, 0, 0, 0, 0};
			setControl(_control);
			setFanSpeed(_fanSpeed);
			setTimer(_timer);
			_outputPacket[10] = data[10];
			_outputPacket[11] = data[11];
			return _outputPacket;
		}
		
		public int getControl() {
			return _control;
		}
		
		public void setControl(int control) {
			_control = control;
			switch(control) {
			case CONTROL_SLEEP:
				_outputPacket[CONTROL_INDEX] = CONTROL_SLEEP_BYTE1;
				_outputPacket[CONTROL_INDEX+1] = CONTROL_SLEEP_BYTE2;
				break;
			case CONTROL_IDLE:
				_outputPacket[CONTROL_INDEX] = CONTROL_IDLE_BYTE1;
				_outputPacket[CONTROL_INDEX+1] = CONTROL_IDLE_BYTE2;
				break;
			case CONTROL_ROAST:
				_outputPacket[CONTROL_INDEX] = CONTROL_ROAST_BYTE1;
				_outputPacket[CONTROL_INDEX+1] = CONTROL_ROAST_BYTE2;
				break;
			case CONTROL_COOL:
				_outputPacket[CONTROL_INDEX] = CONTROL_COOL_BYTE1;
				_outputPacket[CONTROL_INDEX+1] = CONTROL_COOL_BYTE2;
				break;
			default:
				_outputPacket[CONTROL_INDEX] = CONTROL_SLEEP_BYTE1;
				_outputPacket[CONTROL_INDEX+1] = CONTROL_SLEEP_BYTE2;
				break;
			}			
		}
		
		public int getFanSpeed() {
			return _fanSpeed;
		}
		
		public void setFanSpeed(int fanSpeed) {
			final int MAX_SPEED = 9;
			final int MIN_SPEED = 1;
			
			_fanSpeed = Math.max(Math.min(fanSpeed, MAX_SPEED), MIN_SPEED);
			_outputPacket[FAN_INDEX] = (byte) _fanSpeed;
		}
		
		public int getTimer() {
			return _timer;
		}
		
		public void setTimer(int timerSeconds) {
			final int MAX_TIMER = 600;
			final int MIN_TIMER = 0;
			
			_timer = Math.max(Math.min(timerSeconds, MAX_TIMER), MIN_TIMER);
			if (_timer == 0) 
				_outputPacket[TIMER_INDEX] = 0;
			else
				_outputPacket[TIMER_INDEX] = (byte) ((_timer/6) + 1);
		}
		
		public int getHeat() {
			return _heat;
		}
		
		public int getTemp() {
			return _temp;
		}
		
		public void setTargetTemp(int target) {
			_targetTemp = target;
		}
		
		public int getTargetTemp() {
			return _targetTemp;
		}
		
		public void setStartAfterTemp(boolean startAfterTemp) {
			_startAfterTemp = startAfterTemp;
		}
		
		public void setHeat(int heat) {
			final int MAX_HEAT = 3;
			final int MIN_HEAT = 0;
			
			_heat = Math.max(Math.min(heat, MAX_HEAT), MIN_HEAT);
			_outputPacket[9] = (byte) heat;
		}		
			
		public void setFromBytes(byte[] data) {
			if (data[CONTROL_INDEX] == CONTROL_IDLE_BYTE1 & data[CONTROL_INDEX+1] == CONTROL_IDLE_BYTE2) 
				_control = CONTROL_IDLE;
			
			else if (data[CONTROL_INDEX] == CONTROL_ROAST_BYTE1 && data[CONTROL_INDEX+1] == CONTROL_ROAST_BYTE2)
				_control = CONTROL_ROAST;
				
			else if (data[CONTROL_INDEX] == CONTROL_COOL_BYTE1 & data[CONTROL_INDEX+1] == CONTROL_COOL_BYTE2)
				_control = CONTROL_COOL;
				
			else if (data[CONTROL_INDEX] == CONTROL_SLEEP_BYTE1 & data[CONTROL_INDEX+1] == CONTROL_SLEEP_BYTE2)
				_control = CONTROL_SLEEP;
			
			setControl(_control);
			
			_fanSpeed = data[FAN_INDEX];
			setFanSpeed(_fanSpeed);
			
			_timer = data[TIMER_INDEX]*6;
			setTimer(_timer);
			
			_heat = data[HEAT_INDEX];
			setHeat(_heat);
			
			_temp = dataToTemp(data);
		}
		
		public void setTempFromData(byte[] data) {
			_temp = dataToTemp(data);
		}
		
		private int dataToTemp(byte[] data) {
			System.out.println("Calc Temp Start");
			byte[] tempByte = {0, 0, data[10], data[11]};
			int temp;
			ByteBuffer wrapped = ByteBuffer.wrap(tempByte);
			if (data[10] == (byte) 0xFF) {
				return 0;
			}
			temp = wrapped.getInt();
			System.out.println("Calc Temp Done " + temp);
			return temp;
		}
	}
}