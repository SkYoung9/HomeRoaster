import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class ControllerStatusPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private JLabel lblManual;
	private JLabel lblTime;
	private JLabel lblOutput;
	
	/**
	 * Create the panel.
	 */
	public ControllerStatusPanel() {
		initialize();
	}
	
	private void initialize() {
		this.setBackground(Color.ORANGE);
		this.setLayout(new GridLayout(5, 2, 10, 5));
		
		JLabel lblMode = new JLabel("Mode");
		lblMode.setFont(new Font("Gill Sans MT", Font.BOLD, 14));
		this.add(lblMode);
		
		lblManual = new JLabel("Manual");
		lblManual.setFont(new Font("Gill Sans MT", Font.BOLD, 14));
		this.add(lblManual);
		
		JLabel lblTimeDesc = new JLabel("Time");
		lblTimeDesc.setFont(new Font("Gill Sans MT", Font.BOLD, 14));
		this.add(lblTimeDesc);
		
		lblTime = new JLabel("0");
		lblTime.setFont(new Font("Gill Sans MT", Font.BOLD, 14));
		this.add(lblTime);
		
		JLabel lblOutputDesc = new JLabel("Output");
		lblOutputDesc.setFont(new Font("Gill Sans MT", Font.BOLD, 14));
		this.add(lblOutputDesc);
		
		lblOutput = new JLabel("0");
		lblOutput.setFont(new Font("Gill Sans MT", Font.BOLD, 14));
		this.add(lblOutput);
	}
	
	public void setOuput(int output) {
		lblOutput.setText(Integer.toString(output));
	}

}
