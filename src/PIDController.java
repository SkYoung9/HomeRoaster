import java.util.ArrayList;

public class PIDController {
	private long _lastTime, _sampleTime;
	private double _input, _lastInput, _output, _upperBound, _lowerBound, _setPoint, _intergralTerm, _lastErr, _kp, _ki, _kd;
	private boolean _auto;
	public boolean useDerivativeOnInput = true;
	private final long SECOND_TIMEUNIT = 1000000000; //1,000,000,000 nanosec to sec 
	private ArrayList<Double> errorList;
	
	public PIDController(double kp, double ki, double kd, double lowerBound, double upperBound) {
		//errorList = new ArrayList<Double>();
		
		_lowerBound = lowerBound;
		_upperBound = upperBound;
		
		//default sample time to 1 sec
		_sampleTime = SECOND_TIMEUNIT / 10;
		
		setTuning(kp, ki, kd);
		_auto = true;
	}
	
	public double getOuput(double setPoint, double input) {
		System.out.println("PID Start " + setPoint + " " + input);
		if (!_auto)
			return _output;
		
		_setPoint = setPoint;
		_input = input;
		
		// Time since last compute		
		long now = System.nanoTime();
		long deltaTime = now - _lastTime;
		
		// Compute error
		double error = _setPoint - _input;
		System.out.println("PID Error " + error);
		
		// Only compute new output at sample time
		// meanwhile, store errors
		if (deltaTime < _sampleTime) {
			//errorList.add(error);
			return _output;
		}
		
		// Compute integral and derivative
		_intergralTerm += (_ki * error);
		_intergralTerm = Math.max(Math.min(_intergralTerm, _upperBound), _lowerBound);
		double deltaInput = (input - _lastInput);
		System.out.println("PID detal input " + deltaInput);
		double deltaError;
		if (useDerivativeOnInput) 
			deltaError = -deltaInput;
		else
			deltaError = (error - _lastErr) / deltaTime;
		System.out.println("PID detal error " + deltaError);
		
		// PID formula
		System.out.print("PID calculated ");
		System.out.print(" _kp " + _kp);
		System.out.print(" _kd " + _kd);
		System.out.print(" _kd " + _ki);
		System.out.print(" _intergralTerm " + _intergralTerm);
		System.out.print(" deltaError " + deltaError);
		_output = _kp * error + _intergralTerm - _kd * deltaError;
		System.out.println(" _output " + _output);		
		_output = Math.max(Math.min(_output, _upperBound), _lowerBound);
		
		// Store "last" variables and clear error list
		_lastErr = error;
		_lastTime = now;
		_lastInput = input;
		//errorList = new ArrayList<Double>();
		System.out.println("PID Done " + _output);
		
		return _output;
	}
	
	public void setTuning(double kp, double ki, double kd) {
		double sampleTimeInSec = (double) _sampleTime/SECOND_TIMEUNIT;
		_kp = kp;
		_ki = ki * sampleTimeInSec;
		_kd = kd / sampleTimeInSec;
	}
	
	public void setSampleTime(int sampleTime) {
		if (sampleTime > 0) {
			double ratio = (double) sampleTime / (double) _sampleTime;
			
			_ki *= ratio;
			_kd /= ratio;
			_sampleTime = sampleTime;
		}
	}
	
	public void setOutputBounds(double lowerBound, double upperBound) {
		if (lowerBound > upperBound)
			return;
		
		_lowerBound = lowerBound;
		_upperBound = upperBound;
		
		_intergralTerm = Math.max(Math.min(_intergralTerm, _upperBound), _lowerBound);
		_output = Math.max(Math.min(_output, _upperBound), _lowerBound);
	}
	
	public void setToAuto(boolean auto) {
		// Handle switching to auto
		if (auto && !_auto)
		{
			initialize();
		}
		
		_auto = auto;
	}
	
	public boolean getAuto() {
		return _auto;
	}
	
	private void initialize() {
		_lastInput = _input;
		_intergralTerm = _output;
		_intergralTerm = Math.max(Math.min(_intergralTerm, _upperBound), _lowerBound);
		_output = Math.max(Math.min(_output, _upperBound), _lowerBound);
	}
}
