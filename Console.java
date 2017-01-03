import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class Console {

	public static void main(String[] args) {
		new Console();
	}

	public JFrame frame;
	public JTextPane console;
	public JTextField input;
	public JScrollPane scrollpane;

	public StyledDocument document;

	// boolean trace = false;

	ArrayList<String> recent_used = new ArrayList<String>();
	int recent_used_id = 0;
	int recent_used_maximum = 10;

	boolean dir_mode = false;
	String path;
	String def_path;

	public Console() {

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
		}

		frame = new JFrame();
		frame.setTitle("OS Shell");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		console = new JTextPane();
		console.setEditable(false);
		console.setFont(new Font("Courier New", Font.PLAIN, 12));
		console.setOpaque(false);

		document = console.getStyledDocument();

		input = new JTextField();
		input.setBackground(Color.GRAY);
		input.setEditable(true);
		input.setFont(new Font("Courier New", Font.PLAIN, 12));
		input.setForeground(Color.CYAN);
		input.setCaretColor(Color.WHITE);
		input.setOpaque(false);
		//input.setText("Type Here...");
		initial();
		askMsg();
		input.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String text = input.getText();
				if (text.length() > 1) {

					recent_used.add(text);
					recent_used_id = 0;

					doCommand(text);
					scrollBottom();
					input.selectAll();
				}
			}
		});

		input.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent arg0) {

			}

			@Override
			public void keyReleased(KeyEvent arg0) {

			}

			@Override
			public void keyPressed(KeyEvent arg0) {
				if (arg0.getKeyCode() == KeyEvent.VK_UP) {
					if (recent_used_id < (recent_used_maximum - 1) && recent_used_id < (recent_used.size() - 1)) {
						recent_used_id++;
					}

					input.setText(recent_used.get(recent_used.size() - 1 - recent_used_id));
				} else if (arg0.getKeyCode() == KeyEvent.VK_DOWN) {
					if (recent_used_id > 0) {
						recent_used_id--;
					}
					input.setText(recent_used.get(recent_used.size() - 1 - recent_used_id));
				}
			}
		});

		scrollpane = new JScrollPane(console);
		scrollpane.setBorder(null);
		scrollpane.setOpaque(false);
		scrollpane.getViewport().setOpaque(false);

		frame.getContentPane().add(input, BorderLayout.SOUTH);
		frame.getContentPane().add(scrollpane, BorderLayout.CENTER);
		frame.setSize(660, 350);
		frame.setLocationRelativeTo(null);

		frame.getContentPane().setBackground(new Color(0, 0, 0));

		frame.setResizable(false);
		frame.setVisible(true);

	}

	public void doCommand(String s) {

		final String[] commands = s.split(" ");

		if (commands[0].equals("don")) {
			println("\n" + "|---------------------------|\n" + "|    Directory Mode 'ON'    |\n"
					+ "|---------------------------|\n", Color.YELLOW);
			dir_mode = true;

			path = Paths.get(".").toAbsolutePath().normalize().toString();
			def_path = path;
			println(path, Color.CYAN);
			return;
		} else if (commands[0].equals("doff")) {
			println("\n" + "|---------------------------|\n" + "|    Directory Mode 'OFF'   |\n"
					+ "|---------------------------|\n", Color.YELLOW);
			println("Enter commands to execute", Color.CYAN);
			dir_mode = false;
			return;
		}

		if (dir_mode) {
			dirMode(s);
		} else {
			try {
				if (commands[0].equalsIgnoreCase("clear")) {
					clear();
					initial();
					askMsg();
				} else if (commands[0].equalsIgnoreCase("visit")) {
					try {
						String cmd = "C:\\WINDOWS\\system32\\cmd.exe /c start " + commands[1];
						Runtime.getRuntime().exec(cmd);
						println("Browsing :" + commands[1]);
						askMsg();
					} catch (Exception ex) {
						error(ex.getMessage());
					}
				} else if (commands[0].equalsIgnoreCase("getip")) {
					new Thread(new Runnable() {

						@Override
						public void run() {
							try {

								println("Hostname	: " + InetAddress.getLocalHost().getHostName(),
										new Color(255, 155, 255));
								println("Local IP	: " + InetAddress.getLocalHost().getHostAddress(),
										new Color(155, 155, 255));
								scrollBottom();
								askMsg();
							} catch (Exception ex) {
								println("Error -> " + ex.getMessage(), new Color(255, 155, 155));
							}

						}
					}).start();
				} else if (commands[0].equalsIgnoreCase("help")) {
					help();
					askMsg();
				} else if (commands[0].equalsIgnoreCase("reset")) {
					reset();
				} else if (commands[0].equalsIgnoreCase("drive")) {

					File[] roots = File.listRoots();
					for (File root : roots) {
						print("\nFile system root   : " + root.getAbsolutePath(), Color.GREEN);
						print("\nTotal space (byte) : " + root.getTotalSpace());
						print("\nFree space (byte)  : " + root.getFreeSpace());
						print("\nUsable space (byte): " + root.getUsableSpace() + "\n\n");
					}
					askMsg();
				} else {
					println(s + "\nInvalid Command, Use 'help' for more info", new Color(255, 255, 255));
					askMsg();
				}

			} catch (Exception ex) {
				println("Error -> " + ex.getMessage(), new Color(255, 155, 155));
			}
		}

	}

	private void printLines(String name, InputStream ins) throws Exception {
		String line = null;
		BufferedReader in = new BufferedReader(new InputStreamReader(ins));
		while ((line = in.readLine()) != null) {
			println(line, Color.ORANGE);
		}
		scrollBottom();
	}

	public void askMsg() {
		println("\nChoose 'don' mode or excute command :", Color.CYAN);
	}

	public void initial() {
		println("A1Terminal", new Color(155, 155, 255));
		println("One tool for all operating system.", new Color(155, 155, 255));
		println("Type 'help' for more information. \n", new Color(155, 155, 255));
	}

	public void dirMode(String s) {

		String[] commands = s.split(" ");
		if (commands[0].equals("ls")) {
			dir(path);
			println(path, Color.CYAN);
		} else if (commands[0].equals("cd")) {
			if (commands.length > 1) {
				if (Files.isDirectory(Paths.get(path + "//" + commands[1]))) {
					Path conPath = Paths.get(path + "//" + commands[1]);
					path = conPath.toString();
					println(path, Color.CYAN);
				} else {
					error("File doesn't exist");
				}
			} else {
				error("Please pass an additional arguement to open the folder");
			}
		} else if (commands[0].equals("cd..")) {
			File theFile = new File(path);
			String prevFol = theFile.getParent();
			path = prevFol;
			if (path == null) {
				error("Only system drive is allowed to navigate");
				path = def_path;
			} else if (path.equals("//")) {
				error("Only system drive is allowed to navigate");
				path = def_path;
			}
			println(path, Color.CYAN);

		} else if (commands[0].equals("cat")) {
			if (commands.length > 1) {
				try {
					BufferedReader br = new BufferedReader(new FileReader(path + commands[1]));
					try {
						String x;
						println("Printing the file: " + commands[1], Color.CYAN);
						while ((x = br.readLine()) != null) {
							print(x);
						}
						print("\n");
						println(path, Color.CYAN);
					} catch (IOException e) {
						error(e.getMessage());
					}
				} catch (FileNotFoundException ex) {
					error(ex.getMessage());
				}
			} else {
				error("Please pass an additional arguement to print the file");
			}
		} else if (commands[0].equalsIgnoreCase("run")) {
			if (commands.length > 1) {
				String ntxt = s.replace("run " + commands[1], "");
				File f = new File(path + "\\" + commands[1]);
				try {
					Process pro = Runtime.getRuntime().exec(f.getAbsolutePath() + ntxt);
					printLines(commands[1], pro.getInputStream());
					printLines(commands[1], pro.getErrorStream());
					pro.waitFor();
				} catch (IOException e) {
					error(e.getMessage());
				} catch (Exception e) {
					error(e.getMessage());
				}
			} else {
				error("Please pass an additional arguement to run");
			}
		} else if (commands[0].equalsIgnoreCase("help")) {
			help();
			println(path, Color.CYAN);
		} else if (commands[0].equalsIgnoreCase("mkdir")) {
			if (commands.length > 1) {
				File file = new File(path + "//" + commands[1]);
				if (!file.exists()) {
					if (file.mkdirs()) {
						println("Folder created successfully", Color.GREEN);
						println(path, Color.CYAN);
					} else {
						error("Failed to create directory!");
					}
				} else {
					error("File name already exists");
				}
			} else {
				error("Please pass an additional arguement to create a folder");
				println(path, Color.CYAN);
			}
		} else if (commands[0].equalsIgnoreCase("rmdir")) {
			if (commands.length > 1) {
				File file = new File(path + "//" + commands[1]);
				print("Cleaning out folder : " + file.getPath() + "\n", Color.GREEN);
				deleteFileFolder(file.getAbsoluteFile());
				println(path, Color.CYAN);

			} else {
				error("Please pass an additional arguement to create a folder");
			}
		} else if (commands[0].equalsIgnoreCase("clear")) {
			clear();
			initial();
			println(path, Color.CYAN);
		} else if (commands[0].equalsIgnoreCase("reset")) {
			reset();
		} else {
			println("\nExecuting : '" + s + "'", Color.GREEN);
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						if (commands.length == 1) {
							Process pro = Runtime.getRuntime().exec(commands[0]);
							printLines(commands[0], pro.getInputStream());
							printLines(commands[0], pro.getErrorStream());
							pro.waitFor();
							println("\n" + path, Color.CYAN);
							scrollBottom();
						} else {
							Process pro = Runtime.getRuntime().exec(s);
							printLines(commands[0], pro.getInputStream());
							printLines(commands[0], pro.getErrorStream());
							pro.waitFor();
							println("\n" + path, Color.CYAN);
							scrollBottom();
						}

					} catch (IOException e) {
						error(e.getMessage());
					} catch (Exception e) {
						error("Invalid Command, Use 'help' for more info");
					}
				}
			}).start();
		}

	}

	public void deleteFileFolder(File Path) {
		try {
			File file = new File(Path.getAbsolutePath());
			File[] c = file.listFiles();
			for (File f : c) {
				if (f.isDirectory()) {
					print("Deleting Folder : " + f.getName() + "\n", Color.ORANGE);
					deleteFileFolder(f);
					f.delete();
				} else {
					print("Deleting File : " + f.getName() + "\n", Color.MAGENTA);
					f.delete();
				}
				f.delete();
			}
			file.delete();
		} catch (Exception ex) {
			error("Invalid File Path");
		}
	}

	public void dir(String s) {
		try {
			File f = new File(s);
			File[] files = f.listFiles();
			for (File file : files) {
				if (file.isDirectory()) {
					println(file.getName(), Color.ORANGE);
				}
				if (file.isFile()) {
					println(file.getName(), Color.MAGENTA);
				}
			}
		} catch (Exception ex) {
			println("Error -> " + ex.getMessage(), new Color(255, 155, 155));
			println(path, Color.CYAN);
		}
	}

	public void reset() {
		clear();
		initial();
		askMsg();
		dir_mode = false;
	}

	public void scrollTop() {
		console.setCaretPosition(0);
	}

	public void scrollBottom() {
		console.setCaretPosition(console.getDocument().getLength());
	}

	public void print(String s) {
		print(s, new Color(255, 255, 255));
	}

	public void print(String s, Color c) {

		Style style = console.addStyle("Style", null);
		StyleConstants.setForeground(style, c);

		try {
			document.insertString(document.getLength(), s, style);
		} catch (Exception ex) {
		}

	}

	public void println(String s) {
		println(s, new Color(255, 255, 255));
	}

	public void println(String s, Color c) {
		print(s + "\n", c);
	}

	public void clear() {
		try {
			document.remove(0, document.getLength());
		} catch (Exception ex) {
		}
	}

	public void error(String s) {
		println("Error -> " + s + "\n", new Color(255, 155, 155));
		println(path, Color.CYAN);
		scrollBottom();
	}

	public void help() {
		println("\n***********************************************************\n"
				+ " There are 2 modes to perform in this command line program\n"
				+ "***********************************************************\n\n"
				+ "    don  = Directory Navigation Mode ON\n" + "    doff = Directory Navigarion Mode OFF\n\n"
				+ "-----------------------------------------------------------\n"
				+ "           List of commands for 'doff' mode\n"
				+ "-----------------------------------------------------------\n\n"
				+ "    reset            = Reset the mode into default\n"
				+ "    help             = List all the available options\n"
				+ "    clear            = Clear all content on the screen\n"
				+ "    getip            = View the 'LOCAL IP' and 'HOSTNAME'\n"
				+ "    visit            = Type URL after visit command to browse website\n"
				+ "                       eg: 'visit www.google.com'\n"
				+ "    drive            = List all drive information\n\n"
				+ "-----------------------------------------------------------\n"
				+ "           List of commands for 'don' mode\n"
				+ "-----------------------------------------------------------\n\n"
				+ "    cd [fname]       = Takes one directory up\n"
				+ "    cd..             = Takes one directory down\n"
				+ "    ls               = List all the files and folder on the directory\n"
				+ "    mkdir [fname]    = Makes a new folder\n" + "    rmdir [fname]    = Delete the folder\n"
				+ "    cat [fname]      = print the file on the console\n"
				+ "    run [fname]      = Runs the .exe file or any app file on the directory\n"
				+ "    java [Path/fname]= Compiles the java file and generates .class file\n"
				+ "    javac -cp [Path] = Run the .class java file on the G Shell\n"
				+ "                       eg: 'java -cp C:/Users/user/Desktop/[space][fname]'\n"
				+ "    reset            = Reset the mode into default\n"
				+ "    clear            = Clear all content on the screen\n"
				+ "    help             = List all the available options\n\n", new Color(155, 155, 255));
	}

}
