
import java.awt.Color;
import java.awt.Dimension;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.awt.GridLayout;
import javax.swing.*;

import javax.swing.JButton;

public class PCClient extends JPanel {

	private static final int ROW = 7;
	private static final int COLUMN = 6;
	private static final int GAP = 3;
	private static final Color BG = Color.BLACK;
	private static final Dimension BTN_PREF_SIZE = new Dimension(80, 80);
	private JButton[][] buttons = new JButton[ROW][COLUMN];

	public PCClient() {
		setBackground(BG);
		setLayout(new GridLayout(ROW, COLUMN, GAP, GAP));
		setBorder(BorderFactory.createEmptyBorder(GAP, GAP, GAP, GAP));
	}

	private static void createAndShowGui() {
		PCClient mainPanel = new PCClient();

		JFrame frame = new JFrame("JPanelGrid");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(mainPanel);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public static void main(String[] args) throws IOException {
		String ip = "192.168.70.175";

		if (args.length > 0)
			ip = args[0];

		Socket sock = new Socket(ip, 1234);
		System.out.println("Connected");
		java.io.InputStream in = sock.getInputStream();
		DataInputStream dIn = new DataInputStream(in);
		String str = dIn.readUTF();
		System.out.println(str);
		sock.close();

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				createAndShowGui();
			}
		});
	}
}
