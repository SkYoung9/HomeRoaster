import java.awt.EventQueue;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import javax.swing.JToggleButton;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JPanel;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JTabbedPane;

public class HomeRoaster implements SR700Listener, ActionListener {
	private JFrame frame;
	private JLabel lblStatus = new JLabel("Not Connected");
	private JLabel lblTemp = new JLabel("0");
	private JLabel lblFanSpeed = new JLabel("0");
	private JLabel lblTimer = new JLabel("0");
		
	static SR700Controller sr700;
	static HomeRoaster window;
	private Recipe _recipe;
	ControllerStatusPanel jpController;


	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {		
		window = new HomeRoaster();
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					window.initialize();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		sr700 = new SR700Controller();
		sr700.addListener(window);
		Thread tController = new Thread() {
			public void run() {
				sr700.init();
			}
		};
		tController.start();
	}

	/**
	 * Create the application.
	 */
	public HomeRoaster() {
		//initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		Font fontLabel = new Font("Gill Sans MT", Font.BOLD, 14);
		Font fontTitle = new Font("Elephant", Font.PLAIN, 25);
		Font fontButton = new Font("Elephant", Font.PLAIN, 20);
		
		frame = new JFrame();
		_recipe = new Recipe();
		jpController = new ControllerStatusPanel();
		
		JPanel jpRoaster = new JPanel();
		JLabel lblRoasterDesc = new JLabel("Roaster Status");
		JLabel lblRecipeDesc = new JLabel("Recipe Information");
		JLabel lblStatusDesc = new JLabel("Status: ");
		JLabel lblFanSpeedDesc = new JLabel("Fan Speed");
		JLabel lblTempDesc = new JLabel("Temperature");
		JLabel lblTimerDesc = new JLabel("Timer");
		JButton btnCool = new JButton("Cool Cycle");

		lblRoasterDesc.setFont(fontTitle);
		lblRecipeDesc.setFont(fontTitle);
		btnCool.setFont(fontButton);
		lblStatusDesc.setFont(fontLabel);
		lblStatus.setFont(fontLabel);
		lblFanSpeedDesc.setFont(fontLabel);
		lblFanSpeed.setFont(fontLabel);
		lblTempDesc.setFont(fontLabel);
		lblTemp.setFont(fontLabel);
		lblTimerDesc.setFont(fontLabel);
		lblTimer.setFont(fontLabel);

		GridBagConstraints gbc_lblRecipeDesc = new GridBagConstraints();
		GridBagConstraints gbc_lblRoasterDesc = new GridBagConstraints();
		GridBagConstraints gbc_btnRoast = new GridBagConstraints();
		GridBagConstraints gbc_jpRoaster = new GridBagConstraints();
		GridBagConstraints gbc__recipe = new GridBagConstraints();
		GridBagConstraints gbc_lblControllerStatusDesc = new GridBagConstraints();
		GridBagConstraints gbc_jpController = new GridBagConstraints();
		GridBagConstraints gbc_btnCool = new GridBagConstraints();

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{258, 0, 0};
		gridBagLayout.rowHeights = new int[]{56, 347, 0, 0, 14, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 1.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		
		frame.setBounds(100, 100, 1024, 768);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(gridBagLayout);
		
		btnCool.setActionCommand("COOL");
		btnCool.addActionListener(window);
		
		gbc_lblRoasterDesc.insets = new Insets(0, 0, 5, 5);
		gbc_lblRoasterDesc.gridx = 0;
		gbc_lblRoasterDesc.gridy = 0;
		
		gbc_lblRecipeDesc.gridwidth = 3;
		gbc_lblRecipeDesc.insets = new Insets(0, 0, 5, 0);
		gbc_lblRecipeDesc.gridx = 1;
		gbc_lblRecipeDesc.gridy = 0;

		gbc_jpRoaster.anchor = GridBagConstraints.NORTH;
		gbc_jpRoaster.insets = new Insets(0, 0, 5, 5);
		gbc_jpRoaster.fill = GridBagConstraints.HORIZONTAL;
		gbc_jpRoaster.gridx = 0;
		gbc_jpRoaster.gridy = 1;

		gbc_jpController.anchor = GridBagConstraints.NORTH;
		gbc_jpController.insets = new Insets(0, 0, 5, 5);
		gbc_jpController.fill = GridBagConstraints.HORIZONTAL;
		gbc_jpController.gridx = 0;
		gbc_jpController.gridy = 3;

		gbc__recipe.gridheight = 3;
		gbc__recipe.anchor = GridBagConstraints.NORTH;
		gbc__recipe.gridwidth = 3;
		gbc__recipe.insets = new Insets(0, 0, 5, 0);
		gbc__recipe.fill = GridBagConstraints.BOTH;
		gbc__recipe.gridx = 1;
		gbc__recipe.gridy = 1;

		gbc_btnCool.gridx = 3;
		gbc_btnCool.gridy = 4;
		
		jpRoaster.setLayout(new GridLayout(5, 2, 10, 5));
		jpRoaster.setBackground(new Color(154, 205, 50));
		
		jpRoaster.add(lblStatusDesc);
		jpRoaster.add(lblStatus);
		jpRoaster.add(lblFanSpeedDesc);
		jpRoaster.add(lblFanSpeed);
		jpRoaster.add(lblTempDesc);
		jpRoaster.add(lblTemp);
		jpRoaster.add(lblTimerDesc);
		jpRoaster.add(lblTimer);
		
		JLabel lblControllerStatusDesc = new JLabel("Controller Status");
		lblControllerStatusDesc.setFont(new Font("Elephant", Font.PLAIN, 25));
		gbc_lblControllerStatusDesc.insets = new Insets(0, 0, 5, 5);
		gbc_lblControllerStatusDesc.gridx = 0;
		gbc_lblControllerStatusDesc.gridy = 2;
		
		JButton btnRoast = new JButton("Start Roast");
		btnRoast.setFont(new Font("Elephant", Font.PLAIN, 20));
		btnRoast.setActionCommand("ROAST");
		btnRoast.addActionListener(window);
		gbc_btnRoast.insets = new Insets(0, 0, 0, 5);
		gbc_btnRoast.gridx = 2;
		gbc_btnRoast.gridy = 4;

		frame.getContentPane().add(lblRoasterDesc, gbc_lblRoasterDesc);
		frame.getContentPane().add(lblRecipeDesc, gbc_lblRecipeDesc);
		frame.getContentPane().add(jpRoaster, gbc_jpRoaster);
		frame.getContentPane().add(_recipe, gbc__recipe);
		frame.getContentPane().add(lblControllerStatusDesc, gbc_lblControllerStatusDesc);
		frame.getContentPane().add(jpController, gbc_jpController);
		frame.getContentPane().add(btnCool, gbc_btnCool);
		frame.getContentPane().add(btnRoast, gbc_btnRoast);
	}
	
	@Override
	public void connected() {
		lblStatus.setText("Connected");
	}

	@Override
	public void dataReceived(int control, int fanSpeed, int timer, int heat, int temp, int pidOutput) {
		switch(control) {
		case SR700Controller.CONTROL_SLEEP:
			lblStatus.setText("SLEEP");
			lblFanSpeed.setText("-");
			lblTemp.setText("---");
			lblTimer.setText("--");
			break;
		case SR700Controller.CONTROL_IDLE:
			lblStatus.setText("IDLE");
			lblFanSpeed.setText(String.valueOf(fanSpeed));
			lblTemp.setText(String.valueOf(temp));
			lblTimer.setText(String.valueOf(timer));
			break;
		case SR700Controller.CONTROL_ROAST:
			lblStatus.setText("ROAST");
			lblFanSpeed.setText(String.valueOf(fanSpeed));
			lblTemp.setText(String.valueOf(temp));
			lblTimer.setText(String.valueOf(timer));
			break;
		case SR700Controller.CONTROL_COOL:
			lblStatus.setText("COOL");
			lblFanSpeed.setText(String.valueOf(fanSpeed));
			lblTemp.setText(String.valueOf(temp));
			lblTimer.setText(String.valueOf(timer));
			break;
		}
		
		jpController.setOuput(pidOutput);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		System.out.println("Action");
		switch(e.getActionCommand())
		{
		case "COOL":
			if (sr700.getControlState() == SR700Controller.CONTROL_ROAST) {
				//sr700.sendManualData(SR700Controller.CONTROL_COOL, 9, 0, 180);
			}
			break;
		case "ROAST":
			if (sr700.getControlState() == SR700Controller.CONTROL_IDLE || sr700.getControlState() == SR700Controller.CONTROL_SLEEP) {
				sr700.SendAutoData(_recipe.getTime(), _recipe.getTemp(), _recipe.getStartAfterTemp());
			}
			break;
		}
		
	}

	@Override
	public void timerDone() {
		if (_recipe.nextStep()) {
			sr700.SendAutoData(_recipe.getTime(), _recipe.getTemp(), _recipe.getStartAfterTemp());
		}
	}
}
