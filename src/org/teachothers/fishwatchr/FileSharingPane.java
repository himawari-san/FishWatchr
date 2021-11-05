package org.teachothers.fishwatchr;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.text.BadLocationException;



public class FileSharingPane extends JOptionPane {
	private static final long serialVersionUID = 1L;
	private static final int N_SCAN_PATH = 2;
	private String pipeServer;
	private DefaultListModel<String> model = new DefaultListModel<String>();
	private JList<String> recieverList = new JList<String>(model);
	private String username;
	private Path commentFilePath;
	private Path mediaFilePath;
	private PipeMemberFinder memberFinder;
	private Path distPath;


	public FileSharingPane(String pipeServer, String username, Path commentFilePath, Path mediaFilePath) {
		super();
		this.pipeServer = pipeServer;
		this.username = username;
		this.commentFilePath = commentFilePath;
		this.commentFilePath = Paths.get("/home/masaya/Downloads/GLS1901_merged/GLS1901.mp4.merged_bunseki.xml");
		this.mediaFilePath = mediaFilePath;
		this.mediaFilePath = Paths.get("/home/masaya/Downloads/GLS1901_merged/GLS1901.mp4");
		distPath = commentFilePath.getParent();
		System.err.println("cf:" + commentFilePath);
		System.err.println("mf:" + mediaFilePath);
		ginit();
	}
	
	
	private void ginit(){
		int nTab = 0;
		
		setPreferredSize(new Dimension(450, 300));
		
		JPanel idPanel = new JPanel();
		JLabel usernameLabel = new JLabel("Username");
		JLabel usernameBody = new JLabel(username);
		JLabel pathLabel = new JLabel("Path:");
		JTextField pathField = new JTextField("a");
		idPanel.setLayout(new GridLayout(2, 2, 1, 3));
		idPanel.add(usernameLabel);
		idPanel.add(usernameBody);
		idPanel.add(pathLabel);
		idPanel.add(pathField);
		
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout(10, 10));
		
		JTabbedPane tabbedpane = new JTabbedPane();
		mainPanel.add(idPanel, BorderLayout.NORTH);
		mainPanel.add(tabbedpane, BorderLayout.CENTER);
//		mainPanel.add(recieversScrollPane);
//		mainPanel.add(scanButton);

		// submit tab
		JPanel submitPanel = new JPanel();
		submitPanel.setLayout(new BorderLayout());
		JPanel submitButtonPanel = new JPanel();
		JButton submitButton = new JButton("Submit");
		JButton confirmRecieverButton = new JButton("Confirm Reciever");
		JLabel submitProgressLabel = new JLabel("test desu");
		submitButtonPanel.add(confirmRecieverButton);
		submitButtonPanel.add(submitButton);

		JPanel submitDisplayPanel = new JPanel();
		JTextArea submitTextarea = new JTextArea("");
		JScrollPane submitScrollPane = new JScrollPane(submitTextarea);
//				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
//			      JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS
//				);	

		submitPanel.add(submitProgressLabel, BorderLayout.NORTH);
		submitPanel.add(submitScrollPane, BorderLayout.CENTER);
		submitPanel.add(submitButtonPanel, BorderLayout.SOUTH);
		tabbedpane.add(submitPanel);
		tabbedpane.setTitleAt(nTab++, "Submit");
		
		// recieve tab
		JPanel recievePanel = new JPanel();
		tabbedpane.add(recievePanel);
		JButton recieveButton = new JButton("recieve");
		recievePanel.add(recieveButton);
		tabbedpane.setTitleAt(nTab++, "recieve");
		
		// collect tab
		JPanel collectPanel = new JPanel();
		collectPanel.setLayout(new BorderLayout());
		tabbedpane.add(collectPanel);
		JPanel collectButtonPanel = new JPanel();
		JButton collectButton1 = new JButton("Scan");
		JButton collectButton2 = new JButton("Collect");
		collectButtonPanel.add(collectButton1);
		collectButtonPanel.add(collectButton2);
		JPanel collectDisplayPanel = new JPanel();
		collectDisplayPanel.setLayout(new GridLayout(1, 2));
		JScrollPane membersScrollPane = new JScrollPane();
		JScrollPane messageScrollPane = new JScrollPane();
		JTextArea collectMessageArea = new JTextArea();
		membersScrollPane.setViewportView(recieverList);
		messageScrollPane.setViewportView(collectMessageArea);
		collectDisplayPanel.add(membersScrollPane);
		collectDisplayPanel.add(messageScrollPane);
		collectPanel.add(collectDisplayPanel, BorderLayout.CENTER);
		collectPanel.add(collectButtonPanel, BorderLayout.SOUTH);
		tabbedpane.setTitleAt(nTab++, "Collect");
		
		
		
		// distribute tab
		JPanel distributePanel = new JPanel();
		tabbedpane.add(distributePanel);
		JButton distributeButton = new JButton("Distribute");
//		recievePanel.add(recieveButton);
		tabbedpane.setTitleAt(nTab++, "Distribute");

		
		
		setMessage(mainPanel);
		
		tabbedpane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				System.err.println("tab number:" + tabbedpane.getSelectedIndex());
//				if(memberFinder != null) {
//					memberFinder.pool.shutdownNow();
//				}
			}
		});
		
		
		confirmRecieverButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Executors.newSingleThreadExecutor().submit(new Runnable() {
					@Override
					public void run() {
						DataPiper pipe = new DataPiper(pipeServer);
						String basePath = pathField.getText();
						String reciever = pipe.getUserInformation(basePath+PipeMemberFinder.SUFFIX_RESPONSER_PATH);
						submitTextarea.append("-" + reciever);
						submitTextarea.append("\n");
					}
				});
			}
		});
		
		
		submitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				DataPiper pipe = new DataPiper(pipeServer);
				String basePath = pathField.getText();
				String selectvalues[] = {};
				JOptionPane op = new JOptionPane("messages", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, selectvalues, null);
				final JDialog jd = op.createDialog(FileSharingPane.this, "title");
				System.err.println("ps:" + basePath + "," + commentFilePath);
				Path filePaths[] = new Path[] {commentFilePath, mediaFilePath};

				Executors.newSingleThreadExecutor().submit(new Runnable() {
					@Override
					public void run() {
						displayString(submitTextarea, "- 送信準備中です！\n");
						String newPath = pipe.sendUserInformation(username, basePath);
						if(newPath == null) {
							return;
						}
						
						displayString(submitTextarea, "- 送信準備完了です！\n");
						try {
							pipe.postFile(newPath, filePaths, 
									(str)->{
										displayString(submitTextarea, str + "\n");
									});
						} catch (URISyntaxException | IOException | InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						displayString(submitTextarea, "- 送信完了しました\n");
					}
				});
			}
		});
		
		
		collectButton1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				DataPiper pipe = new DataPiper(pipeServer);
				String basePath = pathField.getText();
				PipeMessage response = new PipeMessage(username);
				memberFinder = new PipeMemberFinder(pipe, N_SCAN_PATH, basePath, response,
						(message)->{
							addReciever(message);
						},
						(e)->{
							JOptionPane.showMessageDialog(FileSharingPane.this, e.getMessage());
							System.err.println(e.getMessage());
						});
				Executors.newSingleThreadExecutor().submit(memberFinder);
				
			}
		});
		
		
		collectButton2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				DataPiper pipe = new DataPiper(pipeServer);
				ListModel<String> recieverListMode = recieverList.getModel();
				
				for(int i = 0; i < recieverListMode.getSize(); i++) {
					String username = recieverListMode.getElementAt(i);
					Path savePath =  Util.getUniquePath(commentFilePath.getParent(), username);
					try {
						Files.createDirectories(savePath);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					PipeMessage message = memberFinder.getMap(username);
					
					Executors.newSingleThreadExecutor().submit(new Runnable() {
						@Override
						public void run() {
							try {
								pipe.getTarFile(message.get(DataPiper.MESSAGE_KEY_PATH), savePath,
										(str)->{
											displayString(collectMessageArea, str);
										});
							} catch (IOException | URISyntaxException | InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					});
				}
			}
		});
		
		
		recieveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				DataPiper pipe = new DataPiper(pipeServer);
				String pathBase = pathField.getText();
				String selectvalues[] = {};
				JOptionPane op = new JOptionPane("messages", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, selectvalues, null);
				final JDialog jd = op.createDialog(FileSharingPane.this, "title");
				if(pathBase.isEmpty()) {
					pathBase = "a";
				}
				PipeReciever pr = new PipeReciever(pipe, pathBase, username,
						(str)->{
							System.err.println("open!");
							Executors.newSingleThreadExecutor().submit(new Runnable() {
								@Override
								public void run() {
									jd.setVisible(true);
								}
							});
						});
				Executors.newSingleThreadExecutor().submit(pr);
			}
		});
		
	}

	
	public void addReciever(PipeMessage message) {
		System.err.println("add:" + message.get("username"));
		DefaultListModel<String> model = (DefaultListModel<String>) recieverList.getModel();
		model.addElement(message.get("username"));
	}
	
	
	public void displayString(JTextArea display, String str) {
		if(str.startsWith("-")) {
			display.append(str);
		} else {
			int pLine = display.getLineCount();
			pLine = pLine >= 2 ? pLine - 2 : pLine;
			try {
				int lineStart = display.getLineStartOffset(pLine);
				if(!display.getText(lineStart, 1).equals("-")) {
					int lineEnd = display.getLineEndOffset(pLine);
					display.replaceRange("", lineStart, lineEnd);
				}
				display.append(str);
				
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}

	
	class PipeReciever implements Callable<Void> {

		private DataPiper pipe;
		private String pathBase;
		private String username;
		private Consumer<String> c;
	
		
		public PipeReciever(DataPiper pipe, String pathBase, String username, Consumer<String> c) {
			this.pipe = pipe;
			this.pathBase = pathBase;
			this.username = username;
			this.c = c;
		}

		@Override
		public Void call() {
			c.accept("test1");
			String newPath = pipe.sendUserInformation(username, pathBase);
			if(newPath == null) {
				return null;
			}
			c.accept(null);
			c.accept("test2");
			
//			try {
//				System.err.println("recieving!:" + newPath);
//				recieve(newPath);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			System.err.println("recieved:" + newPath);
			
			
			return null;
		}
		
		
//		public void recieve(String path) throws IOException {
//			pipe.getFile(path);
//		}
		
	}
}
