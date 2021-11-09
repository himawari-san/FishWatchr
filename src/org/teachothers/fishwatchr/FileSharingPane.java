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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
	private PipeMemberFinder memberFinder;
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

//		tabbedpane.add(getRecievePanel());
//		tabbedpane.setTitleAt(nTab++, "受信");
		
		tabbedpane.add(new CollectPanel());
		tabbedpane.setTitleAt(nTab++, "収集");

//		tabbedpane.add(getDistributePanel());
//		tabbedpane.setTitleAt(nTab++, "配布");

		
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
					m.setMember(message.get("username"));
					m.initBar(0, Integer.valueOf(message.get(DataPiper.MESSAGE_KEY_DATASIZE)));
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
			System.err.println("hey");
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
	
	
	private class CollectButton extends JButton {
		private static final long serialVersionUID = 1L;
		private final static int STATUS_INIT = 0;
		private final static int STATUS_SEARHING_RECIEVERS = 1;
		private final static int STATUS_SENDING = 2;
		private final String[] labels = {"相手を探す", "受信", "受信をやめる"};
		
		private int status = STATUS_INIT;
		
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
						PipeMessage response = new PipeMessage(username);
						memberFinder = new PipeMemberFinder(pipe, N_SCAN_PATH, basePath, response,
								(message)->{
									String sender = message.get("username");
									memberListPanel.addMember(message);
//									senderListModel.addElement(sender);
									messagePanel.append("- " + sender + "をメンバーリストに追加しました。\n");
								},
								(e)->{
									JOptionPane.showMessageDialog(FileSharingPane.this, e.getMessage());
									System.err.println(e.getMessage());
								});
						Executors.newSingleThreadExecutor().submit(memberFinder);
						messagePanel.append("- メンバーを探しています。\n");

						
					break;
					case STATUS_SEARHING_RECIEVERS:
						int nSenders = memberListPanel.getMemberSize();
//						int nSenders = senderListModel.getSize();
						if(nSenders < 1) {
							JOptionPane.showMessageDialog(CollectButton.this, "メンバーが見つかっていません。");
							return;
						}
						
						status++;
						setText(labels[status]);
						memberFinder.stop();
						
						ExecutorService pool = Executors.newFixedThreadPool(3);
						BlockingQueue<Future<Integer>> queue = new ArrayBlockingQueue<>(nSenders);
						
						for(int i = 0; i < nSenders; i++) {
							final int a = i;
							MemberPanel memberPanel = memberListPanel.getMemberPanelAt(i);
							String username = memberPanel.getMember();
							Path savePath =  Util.getUniquePath(commentFilePath.getParent(), username);
							try {
								Files.createDirectories(savePath);
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							PipeMessage message = memberFinder.getMap(username);
							
							final int iMember = i;
							Future<Integer> f = pool.submit(new Callable<Integer>() {

								@Override
								public Integer call() throws Exception {
									try {
										pipe.getTarFile(message.get(DataPiper.MESSAGE_KEY_PATH), savePath,
												(readSize)->{
													SwingUtilities.invokeLater(new Runnable() {
														@Override
														public void run() {
															memberPanel.setValue(readSize.intValue());
															memberListPanel.update(a);
														}
													});
												});
									} catch (IOException | URISyntaxException | InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									return iMember;
								}
								
							});
							queue.add(f);
						}

						Executors.newSingleThreadExecutor().submit(new Runnable() {
							
							@Override
							public void run() {
								for(Future<Integer> f : queue) {
									try {
										f.get();
									} catch (InterruptedException | ExecutionException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
								}
								messagePanel.append("- 受信が完了しました。\n");
								pool.shutdown();
								System.err.println("heyhey");
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
		private final static int STATUS_INIT = 0;
		private final static int STATUS_SEARHING_RECIEVERS = 1;
		private final static int STATUS_SENDING = 2;
		private final String[] labels = {"相手を探す", "送信", "送信をやめる"};
		
		private int status = STATUS_INIT;
		
		public SendButton(MemberPanel memberPanel, MessagePanel messagePanel) {
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
								messagePanel.append("- メンバーを探しています。\n");
								String basePath = pathField.getText();
								String reciever = pipe.getUserInformation(basePath+PipeMemberFinder.SUFFIX_RESPONSER_PATH);
								memberPanel.setMember(reciever);
								messagePanel.append("- " + reciever + "が見つかりました。\n");
							}
						});
						break;
					case STATUS_SEARHING_RECIEVERS:
						if(memberFinder != null) {
							memberFinder.stop();
						}
						status++;
						setText(labels[status]);

						String basePath = pathField.getText();
						System.err.println("ps:" + basePath + "," + commentFilePath);
						Path filePaths[] = new Path[] {commentFilePath, mediaFilePath};
						long totalFilesize = Util.getTotalFilesize(filePaths);
						memberPanel.initBar(0, (int)totalFilesize);


						Executors.newSingleThreadExecutor().submit(new Runnable() {
							@Override
							public void run() {
								messagePanel.append("- 送信準備中です！\n");
								String newPath = pipe.sendUserInformation(username, basePath, totalFilesize);
								if(newPath == null) {
									return;
								}
								
								messagePanel.append("- 送信準備完了です！\n");
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
								setText(labels[status=STATUS_INIT]);
								memberPanel.clearMember();
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
