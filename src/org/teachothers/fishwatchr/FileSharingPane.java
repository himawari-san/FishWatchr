package org.teachothers.fishwatchr;

import java.awt.BorderLayout;
import java.awt.Component;
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
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;




public class FileSharingPane extends JOptionPane {
	private static final long serialVersionUID = 1L;
	private static final int N_SCAN_PATH = 2;
	private String username;
	private Path commentFilePath;
	private Path mediaFilePath;
	private JTextField pathField;
//	private JTextArea sendLogTextarea;
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

		tabbedpane.add(new SendPanel());
		tabbedpane.setTitleAt(nTab++, "送信");

		tabbedpane.add(new ReceivePanel());
		tabbedpane.setTitleAt(nTab++, "受信");
		
		tabbedpane.add(new CollectPanel());
		tabbedpane.setTitleAt(nTab++, "収集");

		tabbedpane.add(new DistributePanel());
		tabbedpane.setTitleAt(nTab++, "配布");

		
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


	
	
	private class SendPanel extends JPanel {
		
		private static final long serialVersionUID = 1L;

		public SendPanel() {

			JPanel memberListPanel = new JPanel();
			MessagePanel messagePanel = new MessagePanel("状況表示");
			JPanel buttonPanel = new JPanel();
			
			setLayout(new BorderLayout(10, 5));
			add(memberListPanel, BorderLayout.NORTH);
			add(messagePanel, BorderLayout.CENTER);
			add(buttonPanel, BorderLayout.SOUTH);
			
			memberListPanel.setLayout(new BorderLayout());
			JLabel listTitleLabel = new JLabel("送信の相手");
			MemberPanel memberPanel = new MemberPanel();
			memberListPanel.add(listTitleLabel, BorderLayout.NORTH);
			memberListPanel.add(memberPanel, BorderLayout.CENTER);
			
			JButton sendButton = new SendButton(memberPanel, messagePanel);
			JButton sendCancelButton = new JButton("キャンセル");
			buttonPanel.add(sendButton);
			buttonPanel.add(sendCancelButton);
			sendButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					
				}
			});
		}
	}
	
	
	private class ReceivePanel extends JPanel {
		
		private static final long serialVersionUID = 1L;

		public ReceivePanel() {

			JPanel memberListPanel = new JPanel();
			MessagePanel messagePanel = new MessagePanel("状況表示");
			JPanel buttonPanel = new JPanel();
			
			setLayout(new BorderLayout(10, 5));
			add(memberListPanel, BorderLayout.NORTH);
			add(messagePanel, BorderLayout.CENTER);
			add(buttonPanel, BorderLayout.SOUTH);
			
			memberListPanel.setLayout(new BorderLayout());
			JLabel listTitleLabel = new JLabel("受信の相手");
			MemberPanel memberPanel = new MemberPanel();
			memberListPanel.add(listTitleLabel, BorderLayout.NORTH);
			memberListPanel.add(memberPanel, BorderLayout.CENTER);
			
			JButton receiveButton = new ReceiveButton(memberPanel, messagePanel);
			JButton cancelButton = new JButton("キャンセル");
			buttonPanel.add(receiveButton);
			buttonPanel.add(cancelButton);
			receiveButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					
				}
			});
		}
	}
	

	private class CollectPanel extends JPanel {
		
		private static final long serialVersionUID = 1L;

		public CollectPanel() {
			JPanel displayPanel = new JPanel();
			JPanel buttonPanel = new JPanel();
			
			setLayout(new BorderLayout(10, 5));
			add(displayPanel, BorderLayout.CENTER);
			add(buttonPanel, BorderLayout.SOUTH);

			displayPanel.setLayout(new GridLayout(2, 1));
			MemberListPanel memberListPanel = new MemberListPanel("メンバー");
			MessagePanel messagePanel = new MessagePanel("状況表示");
			displayPanel.add(memberListPanel);
			displayPanel.add(messagePanel);
			
			CollectButton collectButton = new CollectButton(memberListPanel, messagePanel);
			JButton sendCancelButton = new JButton("キャンセル");
			buttonPanel.add(collectButton);
			buttonPanel.add(sendCancelButton);
			collectButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					
				}
			});
		}
	}

	
	private class DistributePanel extends JPanel {
		
		private static final long serialVersionUID = 1L;

		public DistributePanel() {
			JPanel displayPanel = new JPanel();
			JPanel buttonPanel = new JPanel();
			
			setLayout(new BorderLayout(10, 5));
			add(displayPanel, BorderLayout.CENTER);
			add(buttonPanel, BorderLayout.SOUTH);

			displayPanel.setLayout(new GridLayout(2, 1));
			MemberListPanel memberListPanel = new MemberListPanel("メンバー");
			MessagePanel messagePanel = new MessagePanel("状況表示");
			displayPanel.add(memberListPanel);
			displayPanel.add(messagePanel);
			
			DistributeButton distributeButton = new DistributeButton(memberListPanel, messagePanel);
			JButton cancelButton = new JButton("キャンセル");
			buttonPanel.add(distributeButton);
			buttonPanel.add(cancelButton);
			distributeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					
				}
			});
		}
	}

	
	
	private class MemberListPanel extends JPanel {

		private static final long serialVersionUID = 1L;
		private JProgressBar progressBar = new JProgressBar();
		private MemberListModel memberListModel = new MemberListModel();
		private JList<MemberPanel> memberList = new JList<MemberPanel>(memberListModel);

		public MemberListPanel(String title) {
			setLayout(new BorderLayout());
			JLabel listTitleLabel = new JLabel(title);
			JScrollPane listScrollPane = new JScrollPane();
			listScrollPane.setViewportView(memberList);
			memberList.setCellRenderer(new MemberCellRenderer());
			add(listTitleLabel, BorderLayout.NORTH);
			add(listScrollPane, BorderLayout.CENTER);
		}
		
		public void addMember(PipeMessage message) {
//			SwingUtilities.isEventDispatchThread();
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					MemberPanel m = new MemberPanel();
					m.setMember(message.getSenderName());
					m.initBar(0, (int)(message.getDataSize()));
					memberListModel.add(0, m);
				}
			});
		}
		
		public int getMemberSize() {
			return ((DefaultListModel<MemberPanel>)memberList.getModel()).getSize();
		}

		public MemberPanel getMemberPanelAt(int i) {
			return ((DefaultListModel<MemberPanel>)memberList.getModel()).getElementAt(i);
		}

		public void update(int i) {
			memberListModel.update(i);
		}

		public void setValue(int v) {
			progressBar.setValue(v);
		}
		
		
		class MemberCellRenderer extends MemberPanel implements ListCellRenderer<MemberPanel>{
			private static final long serialVersionUID = 1L;

			public MemberCellRenderer() {
			}

			public Component getListCellRendererComponent(JList list, MemberPanel value, int index, boolean isSelected, boolean cellHasFocus){
				return value;
			}
		}
		
		
		class MemberListModel extends DefaultListModel<MemberPanel> {
			private static final long serialVersionUID = 1L;
			
		    public void update(int index)
		    {
		        fireContentsChanged(this, index, index);
		    }
		}
	}
	
	
	private class MemberPanel extends JPanel {

		private static final long serialVersionUID = 1L;
		private static final String MEMBER_NOT_FOUND = "（未確定）";
		private JLabel nameLabel = new JLabel(MEMBER_NOT_FOUND);
		private JProgressBar progressBar = new JProgressBar();

		public MemberPanel() {
			setLayout(new GridLayout(1, 2));
			add(nameLabel);
			add(progressBar);
		}

		
		public void setMember(String str) {
			System.err.println("h:" + SwingUtilities.isEventDispatchThread());
			nameLabel.setText(str);
		}
		
		
		public String getMember() {
			return nameLabel.getText();
		}
		
		
		public void clearMember() {
			nameLabel.setText(MEMBER_NOT_FOUND);
		}

		public void initBar(int min, int max) {
			progressBar.setMinimum(min);
			progressBar.setMaximum(max);
		}

		public void setValue(int v) {
			progressBar.setValue(v);
		}
	}

	
	private class MessagePanel extends JPanel {

		private static final long serialVersionUID = 1L;
		JTextArea textArea = new JTextArea("");

		public MessagePanel(String title) {
			setLayout(new BorderLayout());
			JLabel titleLabel = new JLabel(title);
			JScrollPane scrollPane = new JScrollPane();
			textArea = new JTextArea("");
			textArea.setLineWrap(true);
			scrollPane.setViewportView(textArea);
			add(titleLabel, BorderLayout.NORTH);
			add(scrollPane, BorderLayout.CENTER);
		}

		
		public void append(String str) {
			textArea.append(str);
		}

	}
	
	
	
	
	private class ReceiveButton extends JButton {
		private static final long serialVersionUID = 1L;
		private final static int STATUS_SEARCH_SENDER = 0;
		private final static int STATUS_RECEIVE = 1;
		private final static int STATUS_FINISH = 2;
		private final String[] labels = {"相手を探す", "受信", "受信をやめる"};
		
		private int status = STATUS_SEARCH_SENDER;
		private String newPath = "";
		private long dataSize = 0;
		
		public ReceiveButton(MemberPanel memberPanel, MessagePanel messagePanel) {
			setText(labels[STATUS_SEARCH_SENDER]);
			
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					
					switch (status) {
					case STATUS_SEARCH_SENDER:
						status++;
						setText(labels[status]);
						setEnabled(false);

						Executors.newSingleThreadExecutor().submit(new Runnable() {
							@Override
							public void run() {
								messagePanel.append("- メンバーを探しています。\n");
								String basePath = pathField.getText();
								newPath = DataPiper.generatePath(username + basePath);
								PipeMessage myInfo = new PipeMessage(username, newPath);
								try {
									pipe.postMessage(basePath+PipeMessageReceiver.SUFFIX_FIRST_WAITER_PATH, myInfo);
								} catch (URISyntaxException | IOException | InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								try {
									int a;
									PipeMessage memberInfo = pipe.getMessage(newPath);
									String memberName = memberInfo.getSenderName();
									dataSize =  memberInfo.getDataSize();
									memberPanel.setMember(memberName);
									messagePanel.append("- " + memberName + "が見つかりました。\n");
									setEnabled(true);
								} catch (IOException | URISyntaxException | InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						});
						break;
					case STATUS_RECEIVE:
						status++;
						setText(labels[status]);
						
						String memberName = memberPanel.getMember();
						Path savePath =  Util.getUniquePath(commentFilePath.getParent(), memberName);
						try {
							Files.createDirectories(savePath);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}

						Executors.newSingleThreadExecutor().submit(new Runnable() {
							
							@Override
							public void run() {
								memberPanel.initBar(0, (int)dataSize);

								try {
									pipe.getTarFile(newPath, savePath,
											(readSize) -> {
												SwingUtilities.invokeLater(new Runnable() {
													@Override
													public void run() {
														memberPanel.setValue(readSize.intValue());
													}
												});
											});
								} catch (URISyntaxException | IOException | InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								setText(labels[status = STATUS_SEARCH_SENDER]);
							}
						});
						
//						memberPanel.clearMember();

						break;
					case STATUS_FINISH:
						break;
					default:
						break;
					}
				}
			});
		}
	}

	

	private class DistributeButton extends JButton {
		int a;
		private static final long serialVersionUID = 1L;
		private final static int STATUS_INIT = 0;
		private final static int STATUS_SEARHING = 1;
		private final static int STATUS_EXECUTING = 2;
		private final String[] labels = {"相手を探す", "配布", "配布をやめる"};
		
		private int status = STATUS_INIT;
		private String newPath = "";
		private long dataSize = 0;

//		PipeMemberFinder memberFinder = null;
		
		public DistributeButton(MemberListPanel memberListPanel, MessagePanel messagePanel) {
			setText(labels[STATUS_INIT]);

			Path filePaths[] = new Path[] {commentFilePath, mediaFilePath};
			String basePath = pathField.getText();
			String nextPath = DataPiper.generatePath(username + basePath);
			
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					switch (status) {
					case STATUS_INIT:
						status++;
						setText(labels[status]);
						
//						PipeMessage response = new PipeMessage(username, newPath);
//						response.put(DataPiper.MESSAGE_KEY_PATH, nextPath);
//						response.put(DataPiper.MESSAGE_KEY_TYPE, DataPiper.MESSAGE_VALUE_TYPE_DISTRIBUTE);
//						response.put(DataPiper.MESSAGE_KEY_DATASIZE, String.valueOf(Util.getTotalFilesize(filePaths)));

//						memberFinder = new PipeMemberFinder(pipe, N_SCAN_PATH, basePath, response,
//								(message)->{
//									String sender = message.get("username");
//									memberListPanel.addMember(message);
//									messagePanel.append("- " + sender + "をメンバーリストに追加しました。\n");
//								},
//								(e)->{
//									JOptionPane.showMessageDialog(FileSharingPane.this, e.getMessage());
//									System.err.println(e.getMessage());
//								});
//						Executors.newSingleThreadExecutor().submit(memberFinder);
						messagePanel.append("- メンバーを探しています。\n");

						
					break;
					case STATUS_SEARHING:
						int nSenders = memberListPanel.getMemberSize();
						if(nSenders < 1) {
							JOptionPane.showMessageDialog(DistributeButton.this, "メンバーが見つかっていません。");
							return;
						}
						
//						memberFinder.stop();
//						final String newPath = DataPiper.generatePath(username + basePath) + "&n=" + nSenders;
//						PipeMessage response2 = new PipeMessage(username);
//						response2.put(DataPiper.MESSAGE_KEY_PATH, newPath);
//						PipeMemberFinder memberFinder2 = new PipeMemberFinder(pipe, N_SCAN_PATH, nextPath, response2,
//								(message)->{
//									String sender = message.get("username");
//									messagePanel.append("- " + sender + "にパスを送信しました。\n");
//								},
//								(e)->{
//									JOptionPane.showMessageDialog(FileSharingPane.this, e.getMessage());
//									System.err.println(e.getMessage());
//								});
//						Executors.newSingleThreadExecutor().submit(memberFinder2);
						
						
						status++;
						setText(labels[status]);
						
//						memberPanel.initBar(0, (int)totalFilesize);

						Executors.newSingleThreadExecutor().submit(new Runnable() {
							@Override
							public void run() {
								messagePanel.append("- 送信準備中です！\n");
//								String newPath = pipe.sendUserInformation(username, basePath, totalFilesize);
//								if(newPath == null) {
//									return;
//								}
								
								messagePanel.append("- 送信準備完了です！\n");
//								memberFinder2.stop();

								try {
									pipe.postFile(newPath, filePaths, 
											(readLength)->{
												for(int i = 0; i < nSenders; i++) {
													MemberPanel mp = memberListPanel.getMemberPanelAt(i);
													mp.setValue(readLength.intValue());
												}
//												memberPanel.setValue(readLength.intValue());
											});
								} catch (URISyntaxException | IOException | InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								messagePanel.append("- 送信完了しました\n");
								setText(labels[status=STATUS_INIT]);
//								memberPanel.clearMember();
							}
						});
						break;
					case STATUS_EXECUTING:
						break;
					default:
						break;
					}
				}
			});
		}
	}
	
	
	private class CollectButton extends JButton {
		private static final long serialVersionUID = 1L;
		private final static int STATUS_INIT = 0;
		private final static int STATUS_SEARHING_RECEIVERS = 1;
		private final static int STATUS_SENDING = 2;
		private final String[] labels = {"相手を探す", "受信", "受信をやめる"};
		
		private int status = STATUS_INIT;
		private PipeMessageBroadcaster messageBroadcaster = null;
		
		public CollectButton(MemberListPanel memberListPanel, MessagePanel messagePanel) {
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
						PipeMessage myInfo = new PipeMessage(username);
						
						messageBroadcaster = new PipeMessageBroadcaster(pipe, 1, basePath, myInfo,
								(updatedMessage) -> {
									String newPath = updatedMessage.getPath();

									try {
										PipeMessage memberInfo = pipe.getMessage(newPath);
										String sender = memberInfo.getSenderName();
										memberListPanel.addMember(memberInfo);
										messagePanel.append("- " + sender + "をメンバーリストに追加しました。\n");
									} catch (URISyntaxException | IOException | InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}, (ex) -> {
								});
						Executors.newSingleThreadExecutor().submit(messageBroadcaster);
						messagePanel.append("- メンバーを探しています。\n");
						break;
					case STATUS_SEARHING_RECEIVERS:
						int nSenders = memberListPanel.getMemberSize();
						if(nSenders < 1) {
							JOptionPane.showMessageDialog(CollectButton.this, "メンバーが見つかっていません。");
							return;
						}
						
						status++;
						setText(labels[status]);
						
						Executors.newSingleThreadExecutor().submit(new Runnable() {
							@Override
							public void run() {
								for(int i = 0; i < nSenders; i++) {
									final int i2 = i;
									MemberPanel memberPanel = memberListPanel.getMemberPanelAt(i);
									String username = memberPanel.getMember();
									Path savePath =  Util.getUniquePath(commentFilePath.getParent(), username);
									try {
										Files.createDirectories(savePath);
									} catch (IOException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
									PipeMessage message = messageBroadcaster.getMap(username);
									
									try {
										pipe.getTarFile(message.getPath(), savePath, (readSize) -> {
											SwingUtilities.invokeLater(new Runnable() {
												@Override
												public void run() {
													memberPanel.setValue(readSize.intValue());
													memberListPanel.update(i2);
												}
											});
										});
									} catch (URISyntaxException | IOException | InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
								messagePanel.append("- 受信が完了しました。\n");
							}
						});
//						setText(labels[status=STATUS_INIT]);
//						memberListPanel.clearMember();

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
		private final static int STATUS_SEARCH_RECEIVER = 0;
		private final static int STATUS_SEND = 1;
		private final static int STATUS_FINISH = 2;
		private final String[] labels = {"相手を探す", "送信", "送信をやめる"};
		
		private int status = STATUS_SEARCH_RECEIVER;
		private String newPath = "";
		private long dataSize = 0;
		
		public SendButton(MemberPanel memberPanel, MessagePanel messagePanel) {
			setText(labels[STATUS_SEARCH_RECEIVER]);
			
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					Path filePaths[] = new Path[] {commentFilePath, mediaFilePath};
					
					switch (status) {
					case STATUS_SEARCH_RECEIVER:
						status++;
						setText(labels[status]);
						setEnabled(false);
						messagePanel.append("- メンバーを探しています。\n");
						Executors.newSingleThreadExecutor().submit(new Runnable() {
							
							@Override
							public void run() {
								String basePath = pathField.getText();
								PipeMessage memberInfo = new PipeMessage(username);
								try {
									memberInfo = pipe.getMessage(basePath+PipeMessageReceiver.SUFFIX_FIRST_WAITER_PATH);
									String memberName = memberInfo.getSenderName();
									newPath = memberInfo.getPath();
									memberPanel.setMember(memberName);
									messagePanel.append("- " + memberName + "が見つかりました。\n");
								} catch (URISyntaxException | IOException | InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								try {
									dataSize = Util.getTotalFilesize(filePaths);

									PipeMessage myInfo = new PipeMessage(username);
									myInfo.setDataSize(dataSize);
									pipe.postMessage(newPath, myInfo);
									setEnabled(true);
								} catch (IOException | URISyntaxException | InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						});
						break;
					case STATUS_SEND:
						status++;
						setText(labels[status]);

						memberPanel.initBar(0, (int)dataSize);

						Executors.newSingleThreadExecutor().submit(new Runnable() {
							@Override
							public void run() {
								try {
									pipe.postFile(newPath, filePaths, 
											(readLength)->{
												memberPanel.setValue(readLength.intValue());
											});
								} catch (URISyntaxException | IOException | InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								messagePanel.append("- 送信完了しました\n");
								setText(labels[status=STATUS_SEARCH_RECEIVER]);
								memberPanel.clearMember();
							}
						});
						break;
					case STATUS_FINISH:
						break;
					default:
						break;
					}
				}
			});
		}
	}
}
