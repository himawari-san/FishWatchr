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
import java.util.concurrent.Executors;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;



public class FileSharingPane extends JOptionPane {
	private static final long serialVersionUID = 1L;
	private static final int N_SCAN_PATH = 2;
	private DefaultListModel<String> recieverListModel = new DefaultListModel<String>();
	private JList<String> recieverList = new JList<String>(recieverListModel);
	private DefaultListModel<String> senderListModel = new DefaultListModel<String>();
	private JList<String> senderList = new JList<String>(senderListModel);
	private String username;
	private Path commentFilePath;
	private Path mediaFilePath;
	private PipeMemberFinder memberFinder;
	private JTextField pathField;
	private JTextArea sendLogTextarea;
	private JTextArea recieveLogTextarea;
	private DataPiper pipe;


	public FileSharingPane(String pipeServer, String username, Path commentFilePath, Path mediaFilePath) {
		super();
//		this.pipeServer = pipeServer;
		this.username = username;
		this.commentFilePath = commentFilePath;
		this.commentFilePath = Paths.get("/home/masaya/Downloads/GLS1901_merged/GLS1901.mp4.merged_bunseki.xml");
		this.mediaFilePath = mediaFilePath;
		this.mediaFilePath = Paths.get("/home/masaya/Downloads/GLS1901_merged/GLS1901.mp4");
//		distPath = commentFilePath.getParent();
		this.pipe = new DataPiper(pipeServer);
		System.err.println("cf:" + commentFilePath);
		System.err.println("mf:" + mediaFilePath);
		ginit();
	}
	
	
	private void ginit(){
		int nTab = 0;
		
		setOptions(new Object[0]); // remove the default OK button
		setPreferredSize(new Dimension(450, 300));
		
		JPanel idPanel = new JPanel();
		JLabel usernameLabel = new JLabel("Username");
		JLabel usernameBody = new JLabel(username);
		JLabel pathLabel = new JLabel("Path:");
		pathField = new JTextField("a");
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

		// Send tab
		JPanel sendPanel = new JPanel();
		sendPanel.setLayout(new BorderLayout());
		JPanel sendDisplayPanel = new JPanel();
		JPanel sendRecieverPanel = new JPanel();
		sendRecieverPanel.setLayout(new BorderLayout());
		JPanel sendLogPanel = new JPanel();
		sendLogPanel.setLayout(new BorderLayout());
		sendDisplayPanel.setLayout(new GridLayout(1, 2));
		sendDisplayPanel.add(sendRecieverPanel);
		sendDisplayPanel.add(sendLogPanel);
		
		JPanel sendButtonPanel = new JPanel();
		JButton sendButton = new SendButton();
		JButton sendCancelButton = new JButton("キャンセル");
		sendButtonPanel.add(sendButton);
		sendButtonPanel.add(sendCancelButton);
		sendButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
			}
		});
		
		JLabel sendRecieverLabel = new JLabel("受信者リスト");
		JScrollPane sendRecieverScrollPane = new JScrollPane();
		sendRecieverScrollPane.setViewportView(recieverList);
		sendRecieverPanel.add(sendRecieverLabel, BorderLayout.NORTH);
		sendRecieverPanel.add(sendRecieverScrollPane, BorderLayout.CENTER);

		JLabel sendLogLabel = new JLabel("進行状況");
		JScrollPane sendLogScrollPane = new JScrollPane();
		sendLogTextarea = new JTextArea("");
		sendLogTextarea.setLineWrap(true);
		sendLogScrollPane.setViewportView(sendLogTextarea);
		sendLogPanel.add(sendLogLabel, BorderLayout.NORTH);
		sendLogPanel.add(sendLogScrollPane, BorderLayout.CENTER);
		
		sendPanel.add(sendDisplayPanel, BorderLayout.CENTER);
		sendPanel.add(sendButtonPanel, BorderLayout.SOUTH);
		tabbedpane.add(sendPanel);
		tabbedpane.setTitleAt(nTab++, "送信");
		
		
		// Recieve tab
		JPanel recievePanel = new JPanel();
		recievePanel.setLayout(new BorderLayout());
		tabbedpane.add(recievePanel);
		JPanel recieveButtonPanel = new JPanel();
		RecieveButton recieveButton = new RecieveButton();

		JButton recieveCancelButton = new JButton("キャンセル");
		recieveButtonPanel.add(recieveButton);
		recieveButtonPanel.add(recieveCancelButton);
		JPanel recieveDisplayPanel = new JPanel();
		recieveDisplayPanel.setLayout(new GridLayout(1, 2));

		JPanel recieveSenderPanel = new JPanel();
		recieveSenderPanel.setLayout(new BorderLayout());
		JLabel recieveSenderLabel = new JLabel("送信者リスト");
		JScrollPane recieveSenderScrollPane = new JScrollPane();
		recieveSenderScrollPane.setViewportView(senderList);
		recieveSenderPanel.add(recieveSenderLabel, BorderLayout.NORTH);
		recieveSenderPanel.add(recieveSenderScrollPane, BorderLayout.CENTER);
		
		JPanel recieveLogPanel = new JPanel();
		recieveLogPanel.setLayout(new BorderLayout());
		JLabel recieveLogLabel = new JLabel("進行状況");
		JScrollPane recieveLogScrollPane = new JScrollPane();
		recieveLogTextarea = new JTextArea();
		recieveLogTextarea.setLineWrap(true);
		recieveLogScrollPane.setViewportView(recieveLogTextarea);
		recieveLogPanel.add(recieveLogLabel, BorderLayout.NORTH);
		recieveLogPanel.add(recieveLogScrollPane, BorderLayout.CENTER);
		recieveDisplayPanel.add(recieveSenderPanel);
		recieveDisplayPanel.add(recieveLogPanel);
		recievePanel.add(recieveDisplayPanel, BorderLayout.CENTER);
		recievePanel.add(recieveButtonPanel, BorderLayout.SOUTH);
		tabbedpane.setTitleAt(nTab++, "受信");
		
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

	
	private class RecieveButton extends JButton {
		private static final long serialVersionUID = 1L;
		private final static int STATUS_INIT = 0;
		private final static int STATUS_SEARHING_RECIEVERS = 1;
		private final static int STATUS_SENDING = 2;
		private final String[] labels = {"相手を探す", "受信", "受信をやめる"};
		
		private int status = STATUS_INIT;
		
		public RecieveButton() {
			super();
			
			setText(labels[STATUS_INIT]);
			
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					switch (status) {
					case STATUS_INIT:
						status++;
						setText(labels[status]);
						
						String basePath = pathField.getText();
						PipeMessage response = new PipeMessage(username);
						memberFinder = new PipeMemberFinder(pipe, N_SCAN_PATH, basePath, response,
								(message)->{
									String sender = message.get("username");
									senderListModel.addElement(sender);
									recieveLogTextarea.append("- " + sender + "を送信者リストに追加しました。\n");
								},
								(e)->{
									JOptionPane.showMessageDialog(FileSharingPane.this, e.getMessage());
									System.err.println(e.getMessage());
								});
						Executors.newSingleThreadExecutor().submit(memberFinder);
						recieveLogTextarea.append("- 送信者を探しています。\n");

						
					break;
					case STATUS_SEARHING_RECIEVERS:
						int nSenders = senderListModel.getSize();
						if(nSenders < 1) {
							JOptionPane.showMessageDialog(RecieveButton.this, "送信者がいません");
							return;
						}
						
						status++;
						setText(labels[status]);
						memberFinder.stop();
						
						for(int i = 0; i < nSenders; i++) {
							String username = senderListModel.getElementAt(i);
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
													displayString(recieveLogTextarea, str);
												});
									} catch (IOException | URISyntaxException | InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							});
						}
						setText(labels[status=STATUS_INIT]);
						senderListModel.clear();

						break;
					case STATUS_SENDING:
						break;
					default:
						break;
					}
				}
			});
		}
	}
	
	private class SendButton extends JButton {
		private static final long serialVersionUID = 1L;
		private final static int STATUS_INIT = 0;
		private final static int STATUS_SEARHING_RECIEVERS = 1;
		private final static int STATUS_SENDING = 2;
		private final String[] labels = {"相手を探す", "送信", "送信をやめる"};
		
		private int status = STATUS_INIT;
		
		public SendButton() {
			super();
			
			setText(labels[STATUS_INIT]);
			
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					switch (status) {
					case STATUS_INIT:
						status++;
						setText(labels[status]);
						Executors.newSingleThreadExecutor().submit(new Runnable() {
							@Override
							public void run() {
								sendLogTextarea.append("- 受信者を探しています。\n");
								String basePath = pathField.getText();
								String reciever = pipe.getUserInformation(basePath+PipeMemberFinder.SUFFIX_RESPONSER_PATH);
								recieverListModel.addElement(reciever);
								sendLogTextarea.append("- " + reciever + "を受信者リストに追加しました。\n");
							}
						});
						break;
					case STATUS_SEARHING_RECIEVERS:
						int nRecievers = recieverListModel.getSize();
						if(nRecievers < 1) {
							JOptionPane.showMessageDialog(SendButton.this, "受信者がいません");
							return;
						}

						memberFinder.stop();
						status++;
						setText(labels[status]);

						String basePath = pathField.getText();
//						String selectvalues[] = {};
//						JOptionPane op = new JOptionPane("messages", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, selectvalues, null);
//						final JDialog jd = op.createDialog(FileSharingPane.this, "title");
						System.err.println("ps:" + basePath + "," + commentFilePath);
						Path filePaths[] = new Path[] {commentFilePath, mediaFilePath};

						Executors.newSingleThreadExecutor().submit(new Runnable() {
							@Override
							public void run() {
								displayString(sendLogTextarea, "- 送信準備中です！\n");
								String newPath = pipe.sendUserInformation(username, basePath);
								if(newPath == null) {
									return;
								}
								
								displayString(sendLogTextarea, "- 送信準備完了です！\n");
								try {
									pipe.postFile(newPath, filePaths, 
											(str)->{
												displayString(sendLogTextarea, str + "\n");
											});
								} catch (URISyntaxException | IOException | InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								displayString(sendLogTextarea, "- 送信完了しました\n");
								setText(labels[status=STATUS_INIT]);
								recieverListModel.clear();
							}
						});
						break;
					case STATUS_SENDING:
						break;
					default:
						break;
					}
				}
			});
		}
	}
}
