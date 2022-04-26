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
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
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
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;





public class FileSharingDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private static final int N_RETRY = 2;
	private static final int WIDTH = 450;
	private static final int HEIGHT = 600;
	private User user;
	private Path commentFilePath;
	private Path mediaFilePath;
	private JTextField pathField;
	private DataPiper pipe;
	private Consumer<Path> receiveSuccess;
	private Consumer<ArrayList<Path>> collectSuccess;


	public FileSharingDialog(String pipeServer, User user, Path commentFilePath, Path mediaFilePath,
			Consumer<Path> receiveSuccess, Consumer<ArrayList<Path>> collectSuccess) {
		super();
		this.user = user;
		this.commentFilePath = commentFilePath;
		this.mediaFilePath = mediaFilePath;
		this.pipe = new DataPiper(pipeServer);
		this.receiveSuccess = receiveSuccess;
		this.collectSuccess = collectSuccess;
		ginit();
	}

	
	private void ginit(){
		int nTab = 0;
		
		setModal(true);
		setSize(WIDTH, HEIGHT);
		setMinimumSize(new Dimension(WIDTH, HEIGHT));

		
		JPanel idPanel = new JPanel();
		JLabel usernameLabel = new JLabel("Username");
		JLabel usernameBody = new JLabel(user.getUserName());
		JLabel pathLabel = new JLabel("Path:");
		pathField = new JTextField("a");
		idPanel.setLayout(new GridLayout(2, 2, 1, 3));
		idPanel.add(usernameLabel);
		idPanel.add(usernameBody);
		idPanel.add(pathLabel);
		idPanel.add(pathField);
		
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout(10, 10));
		mainPanel.setBorder(new EmptyBorder( 5, 5, 5, 5));
		
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

		getContentPane().add(mainPanel);
		
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
			
			SendButton sendButton = new SendButton(memberPanel, messagePanel);
			buttonPanel.add(sendButton);
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
			
			ReceiveButton receiveButton = new ReceiveButton(memberPanel, messagePanel);
			buttonPanel.add(receiveButton);
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
			buttonPanel.add(collectButton);
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
			buttonPanel.add(distributeButton);
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
	
	
	
	
	private class ReceiveButton extends PipeActionButton {
		private static final long serialVersionUID = 1L;
		private final String[] labels = {"相手を探索", "キャセル", "受信を実行", "閉じる"};
		
		private String newPath = "";
		private long dataSize = 0;
		private Path saveRootPath = null;
		private Path savePath = null;

		
		public ReceiveButton(MemberPanel memberPanel, MessagePanel messagePanel) {
			setLabels(labels);
			setLabel(status);
			
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					
					switch (status) {
					case STATUS_SEARCH:

						if(commentFilePath.getParent() == null) {
							if(Util.getDefaultDirPath() == null) {
								JOptionPane.showMessageDialog(ReceiveButton.this, "保存用のフォルダを作成できません。\n動作が確認できている観察結果をFishWatchrに読み込んだ上で，再度実行してみてください。");
								return;
							}
							saveRootPath = Util.getDefaultDirPath();
						} else {
							saveRootPath = commentFilePath.getParent();
						}

						
						setLabel(status);
						
						future = Executors.newSingleThreadExecutor().submit(new Runnable() {
							@Override
							public void run() {
								messagePanel.append("- メンバーを探しています。\n");
								String basePath = pathField.getText();
								newPath = DataPiper.generatePath(user.getUserName() + basePath);
								PipeMessage myInfo = new PipeMessage(user.getUserName(), newPath);
								PipeMessage memberInfo = null;
								try {
									pipe.postMessage(basePath, myInfo, N_RETRY);
									memberInfo = pipe.getMessage(newPath);

									String memberName = memberInfo.getSenderName();
									memberPanel.setMember(memberName);
									messagePanel.append("- " + memberName + "が見つかりました。\n");

									// sender uses Distribute mode
									if(memberInfo.getType() == PipeMessage.TYPE_CONTINUED) {
										messagePanel.append("- " + memberName + "が送信すると「受信」ボタンが使えるようになります。お待ちください。\n");
										memberInfo = pipe.getMessage(newPath);
									}

									dataSize =  memberInfo.getDataSize();
									newPath = memberInfo.getPath();
									setLabel(status = STATUS_EXECUTE);
								} catch (URISyntaxException | IOException e) {
									JOptionPane.showMessageDialog(ReceiveButton.this, e.getMessage());
									initState();
									return;
								} catch (InterruptedException e) {
									// postMessage() closes the pipe internally
									initState();
									return;
								}

								setEnabled(true);
							}
						});
						
						setLabel(status = STATUS_CANCEL);
						break;
					case STATUS_CANCEL:
						future.cancel(true);
						break;
					case STATUS_EXECUTE:
						setLabel(status = STATUS_CANCEL);
						
						String memberName = memberPanel.getMember();
						savePath = Util.getUniquePath(saveRootPath, memberName);
						
						try {
							Files.createDirectories(savePath);
						} catch (IOException e) {
							JOptionPane.showMessageDialog(ReceiveButton.this,
									"保存用のフォルダが作成できませんでした。\n" + savePath.toAbsolutePath() + "\n" + e.getMessage());
							initState();
							return;
						}

						future = Executors.newSingleThreadExecutor().submit(new Runnable() {
							
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
								} catch (URISyntaxException | ExecutionException | IOException e) {
									JOptionPane.showMessageDialog(ReceiveButton.this, "データ送信が中断されるなどして，受信が完了しませんでした。\n" + e.getMessage());
									initState();
									return;
								} catch (InterruptedException e) {
									// postMessage() closes the pipe internally
									initState();
									return;
								}
								messagePanel.append("- 受信が完了しました\n");
								messagePanel.append("- 保存先：" + savePath + "\n");
								setLabel(status = STATUS_FINISH);
							}
						});
						
						break;
					case STATUS_FINISH:
						FileSharingDialog.this.setVisible(false);
						receiveSuccess.accept(savePath);
						break;
					default:
						break;
					}
				}
			});
		}
	}

	

	private class DistributeButton extends PipeActionButton {
		private static final long serialVersionUID = 1L;
		private final String[] labels = {"相手を探索", "キャンセル", "配布を実行"};
		private PipeMessageReceiver messageReceiver = null;
		
		public DistributeButton(MemberListPanel memberListPanel, MessagePanel messagePanel) {
			setLabels(labels);
			setLabel(status);

			addActionListener(new ActionListener() {
				Path filePaths[] = (mediaFilePath == null || mediaFilePath.toString().matches("^https?:/.+")) 
						? new Path[] {commentFilePath} : new Path[] {commentFilePath, mediaFilePath};

				@Override
				public void actionPerformed(ActionEvent arg0) {
					String basePath = pathField.getText();
					
					switch (status) {
					case STATUS_SEARCH:
						if(commentFilePath.getParent() == null) {
							JOptionPane.showMessageDialog(DistributeButton.this, "配布する観察結果をFishWatchrに読み込んでください。");
							return;
						}
						messagePanel.append("- 次のファイルが配布対象です。\n");
						messagePanel.append("-- " + commentFilePath.toString() + "\n");
						if(mediaFilePath.getParent() != null) {
							messagePanel.append("-- " + mediaFilePath.toString() + "\n");
						}
						
						setLabel(status);
						
						messageReceiver = new PipeMessageReceiver(pipe, basePath,
								(memberMessage) -> {
									String memberName = memberMessage.getSenderName();
									messageReceiver.setMap(memberName, memberMessage);
									memberListPanel.addMember(memberMessage);
									messagePanel.append("- " + memberName + "をメンバーリストに追加しました。\n");
									String tempPath = memberMessage.getPath();
									PipeMessage myInfo = new PipeMessage(user.getUserName(), tempPath);
									myInfo.setType(PipeMessage.TYPE_CONTINUED);
									try {
										pipe.postMessage(tempPath, myInfo);
									} catch (IOException | URISyntaxException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									setLabel(status = STATUS_EXECUTE);
								}, 
								(ex) -> {
									initState();
								});
						future = Executors.newSingleThreadExecutor().submit(messageReceiver);

						messagePanel.append("- メンバーを探しています。\n");
						setLabel(status = STATUS_CANCEL);
						break;
					case STATUS_CANCEL:
						future.cancel(true);
						break;
					case STATUS_EXECUTE:
						int nSenders = memberListPanel.getMemberSize();
						if(nSenders < 1) {
							JOptionPane.showMessageDialog(DistributeButton.this, "メンバーが見つかっていません。");
							return;
						}
						
						setLabel(status = STATUS_CANCEL);

						// Stop PipeMessageReceiver
						future.cancel(true);

						
						future = Executors.newSingleThreadExecutor().submit(new Runnable() {
							@Override
							public void run() {
								String newPath = DataPiper.generatePath(user.getUserName() + basePath) + "?n=" + nSenders;
								PipeMessage myInfo = new PipeMessage(user.getUserName(), newPath);
								long dataSize = Util.getTotalFilesize(filePaths);
								myInfo.setDataSize(dataSize);
								
								for(int i = 0; i < nSenders; i++) {
									MemberPanel memberPanel = memberListPanel.getMemberPanelAt(i);
									if(i == 0) {
										memberPanel.initBar(0, (int)dataSize);
									}
									String memberName = memberPanel.getMember();
									PipeMessage memberInfo = messageReceiver.getMap(memberName);

									try {
										pipe.postMessage(memberInfo.getPath(), myInfo);
									} catch (URISyntaxException | IOException e) {
										JOptionPane.showMessageDialog(DistributeButton.this, e.getMessage());
										initState();
										return;
									} catch (InterruptedException e) {
										// postMessage() closes the pipe internally
										initState();
										return;
									}

								}

								try {
									pipe.postFile(newPath, filePaths, 
											(readLength)->{
												for(int i = 0; i < nSenders; i++) {
													final int i2 = i;
													SwingUtilities.invokeLater(new Runnable() {
														@Override
														public void run() {
															MemberPanel memberPanel = memberListPanel.getMemberPanelAt(i2);
															memberPanel.setValue(readLength.intValue());
															memberListPanel.update(i2);
														}
													});
												}
											});
									setEnabled(true);
								} catch (URISyntaxException | ExecutionException | IOException e) {
									JOptionPane.showMessageDialog(DistributeButton.this, "データ送信が中断されるなどして，受信が完了しませんでした。\n" + e.getMessage());
									initState();
									return;
								} catch (InterruptedException e) {
									// postMessage() closes the pipe internally
									initState();
									return;
								}
								
								messagePanel.append("- 配布が完了しました。\n");
								setLabel(status=STATUS_SEARCH);
							}
						});

						break;
					default:
						break;
					}
				}
			});
		}
	}
	
	
	private class CollectButton extends PipeActionButton {
		private static final long serialVersionUID = 1L;
		private final String[] labels = {"相手を探索", "キャンセル", "収集を実行", "閉じる"};
		private PipeMessageBroadcaster messageBroadcaster = null;
		private Path saveRootPath = null;
		private ArrayList<Path> savePaths = new ArrayList<Path>();
	
		public CollectButton(MemberListPanel memberListPanel, MessagePanel messagePanel) {
			setLabels(labels);
			setLabel(status);
			
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					switch (status) {
					case STATUS_SEARCH:
						if(commentFilePath.getParent() == null) {
							JOptionPane.showMessageDialog(CollectButton.this, "保存用のフォルダを作成できません。\n動作が確認できている観察結果をFishWatrchrに読み込んだ上で，再度実行してみてください。");
								return;
						} else {
							saveRootPath = commentFilePath.getParent();
						}
						
						setLabel(status);
						
						String basePath = pathField.getText();
						PipeMessage myInfo = new PipeMessage(user.getUserName());
						
						messageBroadcaster = new PipeMessageBroadcaster(pipe, basePath, myInfo,
								(updatedMessage) -> {
									String newPath = updatedMessage.getPath();

									try {
										PipeMessage memberInfo = pipe.getMessage(newPath);
										String senderName = memberInfo.getSenderName();
										messageBroadcaster.setMap(senderName, memberInfo);
										memberListPanel.addMember(memberInfo);
										messagePanel.append("- " + senderName + "をメンバーリストに追加しました。\n");
										setLabel(status = STATUS_EXECUTE);
									} catch (URISyntaxException | IOException e) {
										JOptionPane.showMessageDialog(CollectButton.this, e.getMessage());
										initState();
										return;
									} catch (InterruptedException e) {
										// getMessage() closes the pipe internally
										initState();
										return;
									}
								}, (ex) -> {
								});
						future = Executors.newSingleThreadExecutor().submit(messageBroadcaster);
						messagePanel.append("- メンバーを探しています。\n");
						setLabel(status = STATUS_CANCEL);
						break;
					case STATUS_CANCEL:
						future.cancel(true);
						break;
					case STATUS_EXECUTE:
						int nSenders = memberListPanel.getMemberSize();
						if(nSenders < 1) {
							JOptionPane.showMessageDialog(CollectButton.this, "メンバーが見つかっていません。");
							return;
						}
						
						setLabel(status = STATUS_CANCEL);
						
						// Stop MessageBroadcaster
						future.cancel(true);
						
						future = Executors.newSingleThreadExecutor().submit(new Runnable() {
							@Override
							public void run() {
								BlockingQueue<Future<Void>> queue = new ArrayBlockingQueue<>(nSenders);
								
								for(int i = 0; i < nSenders; i++) {
									final int i2 = i;
									MemberPanel memberPanel = memberListPanel.getMemberPanelAt(i);
									String memberName = memberPanel.getMember();
									Path savePath = Util.getUniquePath(saveRootPath, memberName);
											
									try {
										Files.createDirectories(savePath);
									} catch (IOException e) {
										messagePanel.append("保存用のフォルダを作成できません。\n" + memberName + "のファイルの保存処理をキャンセルします。\n");
										continue;
									}
									
									PipeMessage message = messageBroadcaster.getMap(memberName);
									
									var f = Executors.newSingleThreadExecutor().submit(new Callable<Void>() {
										@Override
										public Void call() throws URISyntaxException, IOException, ExecutionException, InterruptedException {
											pipe.getTarFile(message.getPath(), savePath, (readSize) -> {
												SwingUtilities.invokeLater(new Runnable() {
													@Override
													public void run() {
														memberPanel.setValue(readSize.intValue());
														memberListPanel.update(i2);
													}
												});
											});
											messagePanel.append("- 保存完了：" + memberName + ", " + savePath.toString() + "\n");

											return null;
										}
									});
									
									queue.add(f);
									savePaths.add(savePath);
								}
								
								for(Future<Void> f : queue) {
									try {
										f.get();
									} catch (InterruptedException e) {
										f.cancel(true);
										initState();
										return;
									} catch (ExecutionException e) {
										JOptionPane.showMessageDialog(CollectButton.this, e.getMessage());
										initState();
										return;
									}
								}
								
								messagePanel.append("- 収集が完了しました。\n");
								setLabel(status = STATUS_FINISH);
						}
						});
//						memberListPanel.clearMember();

						break;
					case STATUS_FINISH:
						FileSharingDialog.this.setVisible(false);
						collectSuccess.accept(savePaths);
						break;
					default:
						break;
					}
				}
			});
		}
	}

	
	private class SendButton extends PipeActionButton {
		private static final long serialVersionUID = 1L;
		private final String[] labels = {"相手を探索", "キャンセル", "送信を実行"};
		
		private String newPath = "";
		private long dataSize = 0;
		
		public SendButton(MemberPanel memberPanel, MessagePanel messagePanel) {
			setLabels(labels);
			setLabel(status);
			
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					Path filePaths[] = (mediaFilePath == null || mediaFilePath.toString().matches("^https?:/.+")) 
							? new Path[] {commentFilePath} : new Path[] {commentFilePath, mediaFilePath};
							
					System.err.println("len:" + filePaths.length + "," + mediaFilePath.toString());
					switch (status) {
					case STATUS_SEARCH:
						if(commentFilePath.getParent() == null) {
							JOptionPane.showMessageDialog(SendButton.this, "送信する観察結果をFishWatchrに読み込んでください。");
							return;
						}
						messagePanel.append("- 次のファイルが送信対象です。\n");
						messagePanel.append("-- " + commentFilePath.toString() + "\n");
						if(mediaFilePath.getParent() != null) {
							messagePanel.append("-- " + mediaFilePath.toString() + "\n");
						}
						setLabel(status);
						
						messagePanel.append("- メンバーを探しています。\n");
						future = Executors.newSingleThreadExecutor().submit(new Runnable() {
							
							@Override
							public void run() {
								String basePath = pathField.getText();
								try {
									PipeMessage memberInfo = pipe.getMessage(basePath, N_RETRY);
									String memberName = memberInfo.getSenderName();
									newPath = memberInfo.getPath();
									memberPanel.setMember(memberName);
									messagePanel.append("- " + memberName + "が見つかりました。\n");
								} catch (URISyntaxException | IOException e) {
									JOptionPane.showMessageDialog(SendButton.this, e.getMessage());
									initState();
									return;
								} catch (InterruptedException e) {
									// getMessage() closes the pipe internally
									initState();
									return;
								}

								try {
									dataSize = Util.getTotalFilesize(filePaths);

									PipeMessage myInfo = new PipeMessage(user.getUserName(), newPath);
									myInfo.setDataSize(dataSize);
									pipe.postMessage(newPath, myInfo);
									setLabel(status = STATUS_EXECUTE);
								} catch (URISyntaxException | IOException e) {
									JOptionPane.showMessageDialog(SendButton.this, e.getMessage());
									initState();
									return;
								} catch (InterruptedException e) {
									// postMessage() closes the pipe internally
									initState();
									return;
								}
							}
						});

						setLabel(status = STATUS_CANCEL);
						break;
					case STATUS_CANCEL:
						future.cancel(true);
						break;
					case STATUS_EXECUTE:
						setLabel(status = STATUS_CANCEL);

						memberPanel.initBar(0, (int)dataSize);

						future = Executors.newSingleThreadExecutor().submit(new Runnable() {
							@Override
							public void run() {
								try {
									pipe.postFile(newPath, filePaths, 
											(readLength)->{
												SwingUtilities.invokeLater(new Runnable() {
													
													@Override
													public void run() {
														memberPanel.setValue(readLength.intValue());
													}
												});
											});
								} catch (URISyntaxException | IOException | ExecutionException e) {
									SwingUtilities.invokeLater(new Runnable() {
										@Override
										public void run() {
											JOptionPane.showMessageDialog(SendButton.this, e.getMessage());	
											initState();
										}
									});
									return;
								} catch (InterruptedException e) {
									// postMessage() closes the pipe internally
									initState();
									return;
								}
								messagePanel.append("- 送信が完了しました\n");
								setLabel(status=STATUS_SEARCH);
								memberPanel.clearMember();
							}
						});
						break;
					default:
						break;
					}
				}
			});
		}
	}
	
	
	private class PipeActionButton extends JButton {
		private static final long serialVersionUID = 1L;
		protected final static int STATUS_SEARCH = 0;
		protected final static int STATUS_CANCEL = 1;
		protected final static int STATUS_EXECUTE = 2;
		protected final static int STATUS_FINISH = 3;

		private String[] labels = {"phase_search", "phase_action", "phase_finish"};

		protected int status = STATUS_SEARCH;
		protected Future<?> future = null;
	

		public void setLabels(String[] labels) {
			this.labels = labels;
		}
		
		public void initState() {
			status = STATUS_SEARCH;
			setLabel(status);
			setEnabled(true);
		}
		

		public void setLabel(int status) {
			setText(labels[status]);
		}
	}
}
