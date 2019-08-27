/*
    Copyright (C) 2014-2019 Masaya YAMAGUCHI

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.teachothers.fishwatchr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.KeyEventDispatcher;
import java.awt.KeyEventPostProcessor;
import java.awt.KeyboardFocusManager;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang3.StringUtils;
import org.teachothers.fishwatchr.AnnotationGlobalViewer.FilteredViewCheckBoxCallBack;
import org.xml.sax.SAXException;


public class MainFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private static final String VERSION = "Ver.0.9.14.1 [20190819]"; //$NON-NLS-1$
	private static final String COPYRIGHT = "Copyright(c) 2014-2019 Masaya YAMAGUCHI"; //$NON-NLS-1$
	private static final int TASK_INTERVAL = 250;
	private static final int THRESHOLD_CLICK_INTERVAL = 800; // ms
	private static final int TAB_STATUS_GLOBAL_VIEW = 0;
	private static final int TAB_STATUS_DETAIL_VIEW = 1;
	private static final String FILE_PREFIX = "fw"; //$NON-NLS-1$
	private static final String FILENAME_GRUE ="."; //$NON-NLS-1$
	
	private static final String COMMENTER_NAME_GLUE1 = "_"; //$NON-NLS-1$
	private static final String COMMENTER_NAME_GLUE2 = "_in_"; //$NON-NLS-1$
	private static String[] mergeModes = {Messages.getString("MainFrame.0"), Messages.getString("MainFrame.1")}; //$NON-NLS-1$ //$NON-NLS-2$

	private static final int DRAGGABLE_RANGE = 5;
	private static int COMMENT_PANEL_MIN_HEIGHT = 80;
	private static int DISPLAY_PANEL_MIN_HEIGHT = 100;
	
	private static int ANNOTATION_ADDITION_INTERVAL = 10; // msec

	public static final String USER_NOT_SPECIFIED = "noname"; //$NON-NLS-1$
	
	public static final int MAX_DISCUSSERS = 8;
	public static final int COMMENT_PANEL_HEIGHT = 270;
	public static final int BUTTON_PANEL_HEIGHT = 50;
	public static final int TIMELINE_PANEL_WIDTH = 512;
	public static final int TIMELINE_PANEL_HEIGHT = 360;
	
	public static final boolean FLAG_AutoFillAnnotatorName = false;
	public static final boolean FLAG_VALIDATE_ANNOTATIONS = false;
	
	private SoundPlayer soundPlayer;
	private SoundPanel soundPanel;

	private JTabbedPane timeLineTabbedPane;
	private JPanel displayPanel;
	private JPanel timeLinePanel;
	private AnnotationGlobalViewer annotationGlobalViewPanel;
	private JPanel moviePanel;
	private JPanel commentPanel;
	private JPanel buttonPanel;

	private JScrollPane scrollCommentTablePane;
	private CommentTable commentTable;
	private JScrollPane scrollCommentArea;
	private JTextArea commentArea;
	private JPanel operationPanel;
	private JPanel playerOperationPanel;
	private JPanel timeOperationPanel;

	private JButton soundPlayButton;
	private JButton soundForwardButton;
	private JButton soundBackwardButton;
	private JButton soundStopButton;
	private JButton soundRecordButton;
	private JCheckBox listViewSyncCheckBox;

	private TimeDisplay timeCurrent;
	private TimeDisplay timeEnd;
	private TimeSlider timeSlider;

	private CommentTableModel ctm;

	private JMenuBar jMenuBar;
	private JMenu jMenuFile;
	private JMenuItem jMenuItemFileOpen;
	private JMenuItem jMenuItemURLOpen;
	private JMenuItem jMenuItemFileSave;
	private JMenuItem jMenuItemFileMerge;
	private JMenuItem jMenuItemFileExport;
	private JMenuItem jMenuItemFileSaveConfig;
	private JMenuItem jMenuItemFileExit;
	private JMenu jMenuControl;
	private JMenuItem jMenuItemControlPlayPause;
	private JMenuItem jMenuItemControlStop;
	private JMenuItem jMenuItemControlRecord;
	private JMenuItem jMenuItemControlSkipBackward;
	private JMenuItem jMenuItemControlSkipForward;
	private JMenuItem jMenuItemControlJumpToPrevComment;
	private JMenuItem jMenuItemControlJumpToNextComment;
	private JMenuItem jMenuItemControlScroll;
	private JMenuItem jMenuItemControlPlayRateUp;
	private JMenuItem jMenuItemControlPlayRateDown;
	private JMenuItem jMenuItemControlPlayRateReset;
	private JMenu jMenuAnnotation;
	private JMenuItem jMenuItemAnnotationYourName;
	private JMenuItem jMenuItemAnnotationDiscussers;
	private JMenuItem jMenuItemAnnotationAnnotation;
	private JMenuItem jMenuItemAnnotationOrderDiscusser;
	private JMenuItem jMenuItemAnnotationOrderType;
	private JMenuItem jMenuItemAnnotationMulti;
	private JMenuItem jMenuItemAnnotationTimeCorrection;
	private JMenu jMenuAnalysis;
	private JMenu jMenuAnalysisTarget;
	private JMenu jMenuAnalysisLabel;
	private JMenu jMenuAnalysisEval;
	private JMenu jMenuAnalysisFreq;
	private JMenu jMenuOption;
	private JMenuItem jMenuItemOptionTextOverlay;
	private JMenuItem jMenuItemOptionVideoRatio;
	private JMenuItem jMenuItemOptionInputMediaDevices;
	private JMenuItem jMenuItemOptionMergeMode;
	private JMenuItem jMenuItemOptionInputVideoMediaDevices;
	private JMenuItem jMenuItemOptionInputAudioMediaDevices;
	private JMenuItem jMenuItemOptionSkipTime;
	private JMenuItem jMenuItemOptionJumpAdjustment;
	private JMenuItem jMenuItemOptionFocusRange;
	private JMenuItem jMenuItemOptionFontSize;
	private JMenuItem jMenuItemOptionRecorderMode;
	private JMenuItem jMenuItemOptionViewSyncMode;
	private JMenuItem jMenuItemOptionFilteredViewMode;
	private JMenuItem jMenuItemOptionAutoFillAnnotatorName;
	private JMenuItem jMenuItemOptionValidateAnnotations;
	private JMenuItem jMenuItemOptionWaveform;
	private JMenu jMenuHelp;
	private JMenuItem jMenuItemHelpVersion;
	private JMenuItem jMenuItemHelpURL;

	private CommentList commentList;
	private ArrayList<CommentType> commentTypes;
	private ArrayList<CommentButton> commentButtons;

	private String xf = ""; //$NON-NLS-1$
	private String mf = ""; //$NON-NLS-1$

	private DiscussersPanel discussersPanel;

	private ArrayList<User> discussers;
	private User commenter;

	private Timer timer;

	private String systemName;

	private int skipTime = 5000; // ジャンプ時のスキップ量（sec）
	private int adjustmentTimeAtJump = -2000; // ジャンプ時補正（再生，msec）
	// private int adjustmentJumpAtComment = 5; // ジャンプ時補正（コメント，行）
	private float playRate = 1.0f; // 再生速度
	private int iVideoAspectRatio = 0;
	private int iTextOverlayStyle = 0;
	private int iMergeMode = 0;
	
	// ボタンタイプの初期値（討論者優先）
	private int buttonType = CommentButton.BUTTON_TYPE_DISCUSSER;
	// 同時注釈
	private boolean isAnnotationMulti = false;

	// コメントテーブルの幅
	private int columnWidth[] = {120, 250, 250, 250, 250, 250, 1500, 250};

	// 録音+アノテーション or アノテーションのみ
	private boolean isRecorderMode = true;

	// テーブル表示同期
	private boolean isViewSyncMode = false;

	private boolean flagWaveform = false;

	private boolean isSoundPanelEnable = false;
	
	private String userHomeDir = ""; //$NON-NLS-1$
	
	// 強調表示の範囲（現在再生中のコメントから前後 x msec）
	private int focusRange = 10000; // msec
	
	private SysConfig config = new SysConfig();
	
	private String manualURLStr = "http://www2.ninjal.ac.jp/lrc/index.php?%B4%D1%BB%A1%BB%D9%B1%E7%A5%C4%A1%BC%A5%EB%20FishWatchr%2F%CD%F8%CD%D1%BC%D4%A5%DE%A5%CB%A5%E5%A5%A2%A5%EB%2F1_0"; //$NON-NLS-1$
	
	private ImageIcon iconPlay = new ImageIcon(getClass().getResource("resources/images/play.png")); //$NON-NLS-1$
	private ImageIcon iconForward = new ImageIcon(getClass().getResource("resources/images/forward.png")); //$NON-NLS-1$
	private ImageIcon iconBackward = new ImageIcon(getClass().getResource("resources/images/backward.png")); //$NON-NLS-1$
	private ImageIcon iconStop = new ImageIcon(getClass().getResource("resources/images/stop.png")); //$NON-NLS-1$
	private ImageIcon iconPause = new ImageIcon(getClass().getResource("resources/images/pause.png")); //$NON-NLS-1$
	private ImageIcon iconRecordSound = new ImageIcon(getClass().getResource("resources/images/recordSound.png")); //$NON-NLS-1$
	private ImageIcon iconRecordNoSound = new ImageIcon(getClass().getResource("resources/images/recordNoSound.png")); //$NON-NLS-1$
	private String iconSizes[] = {"16", "32", "64", "128", "256"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

	private List<CaptureDevice> videoDeviceList = null;
	private List<CaptureDevice> audioDeviceList = null;
	private int iSelectedVideoDevice = 0;
	private int iSelectedAudioDevice = 0;
	
	private int tableFontSize = FishWatchr.DEFAULT_FONT_SIZE;
	
	private int draggedY = 0; // used for changing the commentPanel size
	private boolean flagDragged = false;
	
	private boolean isReadOnlyMode = false;
	private boolean noOverwriteConfirmation = false;
	
	public MainFrame(String systemName) {
		this.systemName = systemName;

		setWindowTitle(""); //$NON-NLS-1$

		// コメントリスト
		commentList = new CommentList();

		// discussers 初期値
		discussers = new ArrayList<User>();
		// commentTypes 初期値
		commentTypes = new ArrayList<CommentType>();
		

		soundPlayer = new SoundPlayer(this);

		ctm = new CommentTableModel(commentList, discussers, commentTypes);
		
		userHomeDir = System.getProperty("user.home"); //$NON-NLS-1$
		if(userHomeDir == null) {
			userHomeDir = ""; //$NON-NLS-1$
			System.err.println("Warning(MainFrame): Can not get the user homedir."); //$NON-NLS-1$
		}
	}


	public void init() {
		// load config.xml
		config.load(commentTypes, discussers);


		// set column names
		String newColumnNames[] = config.getColumnNames(ctm.getColumnCount());
		if(newColumnNames != null){
			ctm.setColumnNames(newColumnNames);
		}
		
		// set readonly flags 
		boolean readonlyFlags[] = config.getColumnReadOnlyFlags(ctm.getColumnCount());
		if(readonlyFlags != null){
			ctm.setColumnReadOnlyFlags(readonlyFlags);
		}
		
		String constraints[] = config.getColumnConstraints(ctm.getColumnCount());
		if(constraints != null){
			ctm.setColumnConstraints(constraints);
		}
		
		// configure GUIs based on config
		String configValue = config.getFirstNodeAsString("/settings/button_type/@value"); //$NON-NLS-1$
		if(configValue != null){
			if(configValue.equals(CommentButton.BUTTON_TYPE_COMMENT_STR)){
				buttonType = CommentButton.BUTTON_TYPE_COMMENT;
			} else {
				buttonType = CommentButton.BUTTON_TYPE_DISCUSSER;				
			}
		}
		configValue = config.getFirstNodeAsString("/settings/isAnnotationMulti/@value"); //$NON-NLS-1$
		if(configValue != null){
			if(configValue.equals("true")){ //$NON-NLS-1$
				isAnnotationMulti = true;
			} else {
				isAnnotationMulti = false;
			}
		}
		configValue = config.getFirstNodeAsString("/settings/isViewSyncMode/@value"); //$NON-NLS-1$
		if(configValue != null){
			if(configValue.equals("true")){ //$NON-NLS-1$
				isViewSyncMode = true;
			} else {
				isViewSyncMode = false;
			}
		}
		configValue = config.getFirstNodeAsString("/settings/tableFontSize/@value"); //$NON-NLS-1$
		if(configValue != null){
			try {
				int newSize = Integer.parseInt(configValue);
				if(newSize > 0){
					tableFontSize = newSize;
				}
			} catch(NumberFormatException e){
				System.err.println("Warning(MainFrame): invalid font size setting > " + configValue); //$NON-NLS-1$
			}
		}
		configValue = config.getFirstNodeAsString("/settings/manual_url/@value"); //$NON-NLS-1$
		if(configValue != null){
			manualURLStr = configValue;
		}
		configValue = config.getFirstNodeAsString("/settings/default_discusser/@value"); //$NON-NLS-1$
		if(configValue != null){
			if(!configValue.matches("^\\s+$") && !configValue.matches(".*[<>&'\"\\s].*")){ //$NON-NLS-1$ //$NON-NLS-2$
				Comment.setDefaultDiscusserName(configValue);
			} else {
				System.err.println("Warning(MainFrame): Not used default_discusser, " + configValue); //$NON-NLS-1$
			}
		}

		// set commenter's name
		configValue = config.getFirstNodeAsString("/settings/commenter/@value"); //$NON-NLS-1$
		if(configValue == null
				|| configValue.isEmpty()
				|| configValue.matches("^\\s+$")){ //$NON-NLS-1$
			String username = System.getProperty("user.name"); //$NON-NLS-1$
			String hostname;
			try {
				hostname = InetAddress.getLocalHost().getHostName();
			} catch (UnknownHostException e) {
				hostname = ""; //$NON-NLS-1$
			}
			if(hostname.isEmpty()){
				if(username.isEmpty()){
					configValue = USER_NOT_SPECIFIED;
				} else {
					configValue = username;
				}
			} else {
				configValue = username + COMMENTER_NAME_GLUE2 + hostname;
			}
		}
		String modifiedName = configValue.replaceAll("[/<>&'\"\\s]", COMMENTER_NAME_GLUE1); //$NON-NLS-1$
		if(!configValue.equals(modifiedName)){
			System.err.println("Warning(mainFrame): commenter's name was modified from " + configValue + " to " + modifiedName); //$NON-NLS-1$ //$NON-NLS-2$
		}
		commenter = new User(modifiedName);

		ginit();
	}

	public synchronized void timerStart() {
		if (timer != null) {
			timer.cancel();
		}
		timer = new Timer();
		timer.schedule(new DrawGraphTask(), 0, TASK_INTERVAL);
	}

	public void ginit() {
		// icon
		ArrayList<Image> iconList = new ArrayList<Image>();
		for(String size: iconSizes){
			try{
				ImageIcon icon = new ImageIcon(getClass().getResource("resources/images/fw_icon" + size + ".png")); //$NON-NLS-1$ //$NON-NLS-2$
				iconList.add(icon.getImage());
			}catch(Exception e){
				
			}
		}
		setIconImages(iconList);
		
		jMenuBar = getJMenuBar();
		setJMenuBar(jMenuBar);
		// execute after getJMenuBar()
		jMenuItemOptionRecorderMode.setSelected(isRecorderMode);
		jMenuItemOptionViewSyncMode.setSelected(isViewSyncMode);
		jMenuItemOptionAutoFillAnnotatorName.setSelected(FLAG_AutoFillAnnotatorName);
		jMenuItemOptionValidateAnnotations.setSelected(FLAG_VALIDATE_ANNOTATIONS);
		jMenuItemOptionWaveform.setSelected(flagWaveform);
		jMenuItemAnnotationMulti.setSelected(isAnnotationMulti);
		jMenuItemOptionFilteredViewMode.setSelected(true);
		
		displayPanel = getDisplayPanel();
		commentPanel = getCommentPanel(); // execute after getJMenuBar();
		commentPanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseExited(MouseEvent e) {
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}

			@Override
			public void mouseReleased(final MouseEvent e) {
				if(!flagDragged){
					return;
				} else {
					flagDragged = false;
				}
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						int newY;
						if(commentPanel.getPreferredSize().height - draggedY <= COMMENT_PANEL_MIN_HEIGHT){
							// minimize commentPanel
							newY = COMMENT_PANEL_MIN_HEIGHT;
						} else if(MainFrame.this.getMousePosition().y > DISPLAY_PANEL_MIN_HEIGHT){
							if(MainFrame.this.getSize().height - MainFrame.this.getMousePosition().y > COMMENT_PANEL_MIN_HEIGHT){
								// user-specified size
								newY = commentPanel.getPreferredSize().height - draggedY;
							} else {
								// minimize commentPanel
								newY = COMMENT_PANEL_MIN_HEIGHT;
							}
						} else {
							// maximize commentPanel
							newY = MainFrame.this.getSize().height - DISPLAY_PANEL_MIN_HEIGHT;
						}
						
						setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						commentPanel.setPreferredSize(new Dimension(Integer.MAX_VALUE, newY));
						getContentPane().doLayout();
						commentPanel.doLayout();
						displayPanel.doLayout();
					}
				});
			}
		});
		commentPanel.addMouseMotionListener(new MouseAdapter() {
			boolean flag = false; 
			@Override
			public void mouseMoved(MouseEvent e) {
				if(e.getY() < DRAGGABLE_RANGE){
					flag = true;
					setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
				} else {
					flag = false;
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			}
			@Override
			public void mouseDragged(MouseEvent e) {
				if(flag){
					draggedY = e.getY();
					setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
					flagDragged = true;
				}
			}
		});

		commentPanel.setPreferredSize(new Dimension(Integer.MAX_VALUE,
				COMMENT_PANEL_HEIGHT));
		getContentPane().add(displayPanel, BorderLayout.CENTER);
		getContentPane().add(commentPanel, BorderLayout.SOUTH);
		soundRecordButton.setForeground(Color.red);
		commentTable.setAnnotator(commenter.getName());
		
		String strEnableAutoFillAnnotatorName = config.getFirstNodeAsString("/settings/enableAutoFillAnnotatorName/@value"); //$NON-NLS-1$
		if(strEnableAutoFillAnnotatorName == null
				|| strEnableAutoFillAnnotatorName.isEmpty()
				|| strEnableAutoFillAnnotatorName.equalsIgnoreCase("false")) { //$NON-NLS-1$
			jMenuItemOptionAutoFillAnnotatorName.setSelected(false);
		} else {
			jMenuItemOptionAutoFillAnnotatorName.setSelected(true);
		}
		commentTable.setAutoFillAnnotatorName(jMenuItemOptionAutoFillAnnotatorName.isSelected());

		String strEnableValidateAnnotations = config.getFirstNodeAsString("/settings/enableValidateAnnotations/@value"); //$NON-NLS-1$
		if(strEnableValidateAnnotations == null
				|| strEnableValidateAnnotations.isEmpty()
				|| strEnableValidateAnnotations.equalsIgnoreCase("false")) { //$NON-NLS-1$
			jMenuItemOptionValidateAnnotations.setSelected(false);
		} else {
			jMenuItemOptionValidateAnnotations.setSelected(true);
		}		
		
		changeStateStop();

		new DropTarget(this, new DropFileAdapter());

		// 閉じるボタンでいきなり閉じるのを抑制する
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				processesBeforeExit();
			}
		});

		// テーブル編集中のキー入力制御
		// CTRL-↑などが所得できないための対策
		KeyboardFocusManager.getCurrentKeyboardFocusManager()
				.addKeyEventDispatcher(new KeyEventDispatcher() {
					@Override
					public boolean dispatchKeyEvent(KeyEvent e) {
						if (e.getSource().getClass().getName()
								.endsWith(".CommentTable") //$NON-NLS-1$
								|| commentTable.isAncestorOf(e.getComponent())) {
							if (e.isControlDown()
									&& (e.getKeyCode() == KeyEvent.VK_UP
											|| e.getKeyCode() == KeyEvent.VK_DOWN
											|| e.getKeyCode() == KeyEvent.VK_LEFT
											|| e.getKeyCode() == KeyEvent.VK_RIGHT
											|| e.getKeyCode() == KeyEvent.VK_SLASH
											||
											// e.getKeyCode() ==
											// KeyEvent.VK_PLUS ||
											// e.getKeyCode() ==
											// KeyEvent.VK_MINUS ||
											e.getKeyCode() == KeyEvent.VK_S
											|| e.getKeyCode() == KeyEvent.VK_P || e
											.getKeyCode() == KeyEvent.VK_J)) {
								KeyboardFocusManager
										.getCurrentKeyboardFocusManager()
										.redispatchEvent(jMenuBar, e);
								return true;
							}
						}
						return false;
					}
				});
	}
	
	
	public void play(String filename, final long msec){
		if(!setTargetFile(filename)){
			return;
		}
		soundPlayer.myPlay();
		soundPlayer.setPlayPoint(msec);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				changeStatePlay();
				commentTable.setViewCenterByTime(msec);
			}
		});
	}
	

	private JPanel getCommentPanel() {
		if (commentPanel == null) {

			commentPanel = new JPanel();
			commentPanel.setLayout(new BorderLayout());
			operationPanel = getOperationPanel();
			buttonPanel = getButtonPanel();
			buttonPanel.setPreferredSize(new Dimension(Integer.MAX_VALUE, BUTTON_PANEL_HEIGHT));
			commentPanel.add(operationPanel, BorderLayout.NORTH);
			commentPanel.add(buttonPanel, BorderLayout.SOUTH);

			commentTable = new CommentTable(ctm);
			commentTable.setFont(new Font(Font.DIALOG, Font.PLAIN, tableFontSize));
			commentTable.getTableHeader().setFont(new Font(Font.DIALOG, Font.PLAIN, tableFontSize));
			commentTable.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					int row = commentTable.getSelectedRow();
					int column = commentTable.getSelectedColumn();
					ArrayList<Comment> filteredCommentList = ctm.getFilteredCommentList();
					if (column == Comment.F_COMMENT) {
					} else if (e.getClickCount() == 2) {
						Comment selectedComment = filteredCommentList.get(row);
						long commentTime = commentList.unifiedCommentTime(selectedComment)
								+ adjustmentTimeAtJump; // msec

						if (soundPlayer.getPlayerState() == SoundPlayer.PLAYER_STATE_STOP) {
							if(mf.isEmpty() || (!SoundPlayer.isPlayable(mf) && !mf.matches("^https?://.+"))){ //$NON-NLS-1$
								JOptionPane.showMessageDialog(MainFrame.this, Messages.getString("MainFrame.3") + mf); //$NON-NLS-1$
								return;
							}

							if(!soundPlayer.setFile(mf, jMenuItemOptionWaveform.isSelected())){
//								JOptionPane.showMessageDialog(MainFrame.this, "再生が開始できません。\n" + mf);
								return;
							}
							
							if(commentTime / 1000 > soundPlayer.getSoundLength() || commentTime < 0){
								JOptionPane.showMessageDialog(MainFrame.this, Messages.getString("MainFrame.4")); //$NON-NLS-1$
							}
							
							isSoundPanelEnable = soundPlayer.getSoundBufferEnable();

							timeSlider.setEnabled(true);

							changeStatePlay();
							soundPlayer.myPlay();
							soundPlayer.setPlayPoint(commentTime);
							timerStart();
						} else if (soundPlayer.getPlayerState() == SoundPlayer.PLAYER_STATE_PAUSE) {
							changeStatePlay();
							soundPlayer.myPlay();
							soundPlayer.setPlayPoint(commentTime);
						} else if (soundPlayer.getPlayerState() == SoundPlayer.PLAYER_STATE_PLAY) {
							if(commentTime / 1000 > soundPlayer.getSoundLength() || commentTime < 0){
								JOptionPane.showMessageDialog(MainFrame.this, Messages.getString("MainFrame.5")); //$NON-NLS-1$
								return;
							}
							soundPlayer.setPlayPoint(commentTime);
						}
						System.err.println("Jumping to " + commentTime + " msec");  //$NON-NLS-1$  //$NON-NLS-2$
					}
				}
			});
			JPopupMenu commentTablePopupMenu = commentTable.getPopupMenu();
			JMenuItem item = new JMenuItem(Messages.getString("MainFrame.78")); //$NON-NLS-1$
			item.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					Comment selectedComment = ctm.getCommentAt(commentTable.getSelectedRow());
					ctm.addComment("", //$NON-NLS-1$
							new CommentType("", Color.BLACK), //$NON-NLS-1$
							commenter,
							new User(""), //$NON-NLS-1$
							new Date(selectedComment.getDate() + ANNOTATION_ADDITION_INTERVAL),
							selectedComment.getCommentTime() + ANNOTATION_ADDITION_INTERVAL, ""); //$NON-NLS-1$
				}});
			commentTablePopupMenu.add(item, 1);

			scrollCommentTablePane = new JScrollPane(commentTable);
			commentPanel.add(scrollCommentTablePane, BorderLayout.CENTER);
			for (int i = 0; i < columnWidth.length; i++) {
				commentTable.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
				commentTable.getColumn(ctm.getColumnName(i)).setMinWidth(0);
				commentTable.getColumn(ctm.getColumnName(i)).setPreferredWidth(columnWidth[i]);
			}

			commentArea = new JTextArea();
			scrollCommentArea = new JScrollPane(commentArea);
			scrollCommentArea.setPreferredSize(new Dimension(0, 50));
		}
		return commentPanel;
	}

	private JPanel getOperationPanel() {
		if (operationPanel == null) {
			operationPanel = new JPanel();
			operationPanel.setLayout(new BoxLayout(operationPanel,
					BoxLayout.X_AXIS));
			playerOperationPanel = getPlayerOperationPanel();
			timeOperationPanel = getTimeOperationPanel();
			operationPanel.add(playerOperationPanel);
			operationPanel.add(timeOperationPanel);
		}

		return operationPanel;
	}

	private JPanel getPlayerOperationPanel() {
		if (playerOperationPanel == null) {
			playerOperationPanel = new JPanel();

			// height of images 10 pixels, saved with transparent color option by libreoffice
			soundPlayButton = new JButton(iconPlay);
			soundPlayButton.setPreferredSize(new Dimension(46,23));
			soundForwardButton = new JButton(iconForward);
			soundForwardButton.setPreferredSize(new Dimension(46,23));
			soundBackwardButton = new JButton(iconBackward);
			soundBackwardButton.setPreferredSize(new Dimension(46,23));
			soundStopButton = new JButton(iconStop);
			soundStopButton.setPreferredSize(new Dimension(46,23));
			soundRecordButton = new JButton(iconRecordSound);
			soundRecordButton.setPreferredSize(new Dimension(46,23));

			playerOperationPanel.add(soundBackwardButton);
			playerOperationPanel.add(soundPlayButton);
			playerOperationPanel.add(soundForwardButton);
			playerOperationPanel.add(soundStopButton);
			playerOperationPanel.add(soundRecordButton);

			listViewSyncCheckBox = new JCheckBox(Messages.getString("MainFrame.6")); //$NON-NLS-1$
			listViewSyncCheckBox.setToolTipText(Messages.getString("MainFrame.7")); //$NON-NLS-1$
			listViewSyncCheckBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					jMenuItemOptionViewSyncMode.doClick();
				}
			});
			playerOperationPanel.add(listViewSyncCheckBox);

			soundRecordButton.setMnemonic(KeyEvent.VK_R);
			soundPlayButton.setMnemonic(KeyEvent.VK_P);
			// 誤動作を避けるため，やめた
			// soundStopButton.setMnemonic(KeyEvent.VK_S);
			soundForwardButton.setMnemonic(KeyEvent.VK_RIGHT);
			soundBackwardButton.setMnemonic(KeyEvent.VK_LEFT);

			soundRecordButton
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							try {
								saveCommentList();
							} catch (IOException e) {
								e.printStackTrace();
							}
	
							CaptureDevice videoDevice = soundPlayer.getVideoDeviceList().get(iSelectedVideoDevice);
							CaptureDevice audioDevice = soundPlayer.getAudioDeviceList().get(iSelectedAudioDevice);
							
							SimpleDateFormat today = new SimpleDateFormat("yyyyMMdd"); //$NON-NLS-1$
							String basename = userHomeDir + File.separator 
									+ FILE_PREFIX + today.format(new Date()) + FILENAME_GRUE + commenter;

							if (jMenuItemOptionRecorderMode.isSelected()) {
								if(videoDevice.getType() == CaptureDevice.TYPE_NONE
										&& audioDevice.getType() == CaptureDevice.TYPE_NONE){
									JOptionPane.showMessageDialog(MainFrame.this, Messages.getString("MainFrame.8") + mf); //$NON-NLS-1$
									return;
								}
								
								mf = CommentList.getUniqueFilename(basename + CaptureDevice.getMediadataSuffix(videoDevice, audioDevice));
								xf = mf + CommentList.FILE_SUFFIX;
								isSoundPanelEnable = true;
							} else {
								mf = ""; //$NON-NLS-1$
								xf = CommentList.getUniqueFilename(basename + CommentList.FILE_SUFFIX);
							}
							System.err.println("mf: " + mf); //$NON-NLS-1$
							
							setWindowTitle(xf);
							// for testing
//							commentTable.resetPosition();
							commentList.clear();
							commentList.setMediaFilename(mf);
							commentList.setSetName(xf, commenter);
							ctm.refreshFilter();
							ctm.fireTableDataChanged();

							soundPlayer.init();
							soundPlayer.setDefaultRecordingParameters();
//							soundPlayer.setTargetFilename(targetFilename);

							timeSlider.setMinimum(0);
							timeSlider.setMaximum(SoundPlayer.LIMIT_RECODING_TIME);
							timeEnd.setTime(SoundPlayer.LIMIT_RECODING_TIME);
//							timerStart();
							changeStateRecord();
							soundPlayer.myRecord(mf,
									jMenuItemOptionRecorderMode.isSelected(),
									videoDevice, audioDevice);
							annotationGlobalViewPanel.updatePanel();
							timeLineTabbedPane.setSelectedComponent(timeLinePanel);
							timerStart();
							commentList.setStartTime(soundPlayer.getStartTime());
						}
					});

			soundStopButton
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							timer.cancel();
							soundPlayer.myStop();
						}
					});

			soundPlayButton
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							if(!mf.isEmpty()
									&& !SoundPlayer.isPlayable(mf)
									&& !mf.matches("^https?://.+")){ //$NON-NLS-1$
								JOptionPane.showMessageDialog(MainFrame.this, Messages.getString("MainFrame.9") + mf); //$NON-NLS-1$
								return;
							}

							if (soundPlayer.getPlayerState() == SoundPlayer.PLAYER_STATE_PLAY) {
								changeStatePause();
								soundPlayer.myPause();
								return;
							} else if (soundPlayer.getPlayerState() == SoundPlayer.PLAYER_STATE_PAUSE) {
								changeStatePlay();
								soundPlayer.myResume();
								return;
							} else {
								if (mf.isEmpty() && xf.isEmpty()) {
									if(!setTargetFile("")){ //$NON-NLS-1$
										return;
									}
								}

								// アノテーションだけのデータの場合
								if(mf.isEmpty()){
									JOptionPane.showMessageDialog(MainFrame.this, Messages.getString("MainFrame.10")); //$NON-NLS-1$
									return;
								}

								// 再開した時のため
								timeSlider.setEnabled(true);

								changeStatePlay();
								soundPlayer.myPlay();
								timerStart();
							}
						}
					});

			soundForwardButton
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							System.err.println("forward!"); //$NON-NLS-1$
							if(soundPlayer.getSoundLength()*1000 - soundPlayer.getElapsedTime() > skipTime){
								soundPlayer.forward(skipTime); // msec
							}
						}
					});

			soundBackwardButton
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							System.err.println("backward!"); //$NON-NLS-1$
							soundPlayer.backward(skipTime); // msec
						}
					});
		}

		return playerOperationPanel;
	}

	
	private boolean setTargetFile(String filename) {
		// filename:
		//	null	=> url 選択
		//	""		=> ファイル 選択
		//	otherwise	=> ファイル
		
		if(filename != null && !new File(filename).exists() && !filename.isEmpty()){
			JOptionPane.showMessageDialog(MainFrame.this, Messages.getString("MainFrame.11") + filename); //$NON-NLS-1$
			return false;
		}
		
		try {
			saveCommentList();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(MainFrame.this, Messages.getString("MainFrame.12") + e); //$NON-NLS-1$
			e.printStackTrace();
			return false;
		}

		playRate = 1.0f;
		iVideoAspectRatio = 0;
		isReadOnlyMode = false;
		noOverwriteConfirmation = false;
		commentTable.setPopupMenuEnable(true);


		commentTable.initState();
		ctm.clearFilter();
		setWindowTitle(xf);
		String selectedFilename;
		if(filename == null){
			JOptionPane pane = new JOptionPane(
					Messages.getString("MainFrame.13") + //$NON-NLS-1$
					Messages.getString("MainFrame.14"), JOptionPane.PLAIN_MESSAGE); //$NON-NLS-1$
			pane.setWantsInput(true);
			pane.setOptionType(JOptionPane.OK_CANCEL_OPTION);
			pane.setInputValue(""); //$NON-NLS-1$
			JDialog dialog = pane.createDialog(this, Messages.getString("MainFrame.15")); //$NON-NLS-1$
			dialog.setSize(500, 150);
			dialog.setVisible(true);
			selectedFilename = (String) pane.getInputValue();
			if(pane.getValue() == null
					|| (Integer)pane.getValue() == JOptionPane.NO_OPTION
					|| (Integer)pane.getValue() == JOptionPane.CLOSED_OPTION
					|| pane.getValue() == JOptionPane.UNINITIALIZED_VALUE
					|| selectedFilename.isEmpty()){
				return false;
			}
			mf = selectedFilename;
			xf = CommentList.getUniqueFilename(userHomeDir + File.separator
					+ FILE_PREFIX + new SimpleDateFormat("yyyyMMdd").format(new Date()) + FILENAME_GRUE + commenter + CommentList.FILE_SUFFIX); //$NON-NLS-1$
			commentList.clear();
			commentList.setStartTime(new Date());
			commentList.setMediaFilename(mf);
			ctm.refreshFilter();
			config.load(commentTypes, discussers);
			updateButtonPanel(buttonType);
			ctm.fireTableDataChanged();
		} else if(filename.isEmpty()){
			selectedFilename = chooseTargetFile();
		} else {
			selectedFilename = filename;
		}

		
		// 読み込んだファイルがメディアファイルの場合
		if(SoundPlayer.isPlayable(selectedFilename)){
			String oldMf = mf;
			String oldXf = xf;
			mf = selectedFilename;
			xf = mf + FILENAME_GRUE + commenter + CommentList.FILE_SUFFIX;
			if(new File(xf).exists()){
				String newXf = CommentList.getUniqueFilename(xf);
				int result = JOptionPane.showConfirmDialog(this, Messages.getString("MainFrame.16") + new File(xf).getName() + Messages.getString("MainFrame.17") + //$NON-NLS-1$ //$NON-NLS-2$
						Messages.getString("MainFrame.18") + new File(newXf).getName() + Messages.getString("MainFrame.19") + //$NON-NLS-1$ //$NON-NLS-2$
						Messages.getString("MainFrame.20") + //$NON-NLS-1$
						Messages.getString("MainFrame.21")); //$NON-NLS-1$
				
				switch(result){
					case JOptionPane.OK_OPTION:
						commentList.clear();
//						commentList.setSetName(xf, commenter);
						commentList.setStartTime(new Date());
						commentList.setMediaFilename(mf);
						ctm.refreshFilter();
						config.load(commentTypes, discussers);
						updateButtonPanel(buttonType);
						ctm.fireTableDataChanged();
						xf = newXf;
						break;
					case JOptionPane.NO_OPTION:
						selectedFilename = xf;
						commentList.clear();

						try {
							commentList.load(selectedFilename, commentTypes, discussers, false);
						} catch (XPathExpressionException | ParseException
								| ParserConfigurationException
								| SAXException | IOException e2) {
							JOptionPane.showMessageDialog(MainFrame.this, Messages.getString("MainFrame.22") + e2); //$NON-NLS-1$
							e2.printStackTrace();
							return false;
						}
						ctm.refreshFilter();
						updateButtonPanel(buttonType);
						checkLockFile(selectedFilename);
						ctm.fireTableDataChanged();
						
						break;
					case JOptionPane.CANCEL_OPTION:
						xf = oldXf;
						mf = oldMf;
						return false;
				}
			} else {
				commentList.clear();
				commentList.setStartTime(new Date());
				commentList.setMediaFilename(mf);
				ctm.refreshFilter();
				config.load(commentTypes, discussers);
//				setDefaultButton();
				updateButtonPanel(buttonType);
				ctm.fireTableDataChanged();
			}
		}
		// 読み込んだファイルが注釈ファイル(XML)の場合
		else if(selectedFilename.endsWith(CommentList.FILE_SUFFIX)){
			xf = selectedFilename;
			try {
				mf = commentList.load(selectedFilename, commentTypes, discussers, false);
				
				if(!new File(mf).exists()
						&& !mf.matches("^https?://.*") //$NON-NLS-1$
						&& !mf.isEmpty()){
					mf = ""; //$NON-NLS-1$
					xf = ""; //$NON-NLS-1$
					JOptionPane.showMessageDialog(MainFrame.this, Messages.getString("MainFrame.23") + mf); //$NON-NLS-1$
					ctm.refreshFilter();
					updateButtonPanel(buttonType);
					ctm.fireTableDataChanged();
					setWindowTitle(xf);
					return false;
				}
			} catch (XPathExpressionException | ParseException
					| ParserConfigurationException
					| SAXException | IOException e2) {
				JOptionPane.showMessageDialog(MainFrame.this, Messages.getString("MainFrame.24") + e2); //$NON-NLS-1$
				e2.printStackTrace();
				return false;
			}
			ctm.refreshFilter();
			updateButtonPanel(buttonType);
			checkLockFile(xf);
			ctm.fireTableDataChanged();

			commentList.setSetName(xf, commenter);

			if(mf.isEmpty()){
				setWindowTitle(xf);
				timeSlider.setMinimum(0);
				timeSlider.setMaximum(0);
				timeSlider.setEnabled(false);
				annotationGlobalViewPanel.applyFilter(jMenuItemOptionFilteredViewMode.isSelected());
				annotationGlobalViewPanel.init();
				return false;
			}
		} else if (selectedFilename.isEmpty()) {
			timeSlider.setMinimum(0);
			timeSlider.setEnabled(false);
			return false;
		} else if (filename != null) {
			// 関係ないファイルはここで止まるはず。null の場合は，url
			timeSlider.setMinimum(0);
			timeSlider.setEnabled(false);
			return false;
		} else {
			System.err.println("Warning(MainFrame): what?"); //$NON-NLS-1$
		}
		
		System.err.println("set mf: " + mf); //$NON-NLS-1$
		if(!soundPlayer.setFile(mf, jMenuItemOptionWaveform.isSelected())){
			JOptionPane.showMessageDialog(MainFrame.this, Messages.getString("MainFrame.25") + mf); //$NON-NLS-1$
			mf = ""; //$NON-NLS-1$
			xf = ""; //$NON-NLS-1$
			return false;
		}
		isSoundPanelEnable = soundPlayer.getSoundBufferEnable();

		
		commentList.setSetName(xf, commenter);

		setWindowTitle(xf);
		timerStart();
		timeSlider.setMinimum(0);
		timeSlider.setMaximum((int) soundPlayer.getSoundLength());
		timeSlider.setEnabled(true);
		timeEnd.setTime((int) soundPlayer.getSoundLength());
		annotationGlobalViewPanel.applyFilter(jMenuItemOptionFilteredViewMode.isSelected());
		annotationGlobalViewPanel.init();
		return true;
	}

	
	private void checkLockFile(String filename){
		File lockFile = commentList.lock(filename);
		if(lockFile == null){
			JOptionPane.showMessageDialog(this, Messages.getString("MainFrame.2") + //$NON-NLS-1$
					Messages.getString("MainFrame.47") + //$NON-NLS-1$
					Messages.getString("MainFrame.51") + //$NON-NLS-1$
					filename + CommentList.LOCKFILE_SUFFIX);
			isReadOnlyMode = true;
			boolean[] newReadOnlyFlags = {true, true, true, true, true, true, true, true};
			ctm.setColumnReadOnlyFlags(newReadOnlyFlags);
			commentTable.setPopupMenuEnable(false);
			// disable all CommentButtons
			for(CommentButton b : commentButtons){
				b.setEnabled(false);
			}
		}
	}
	
	
	private void saveCommentList() throws IOException {
		// 編集中のセルはキャンセル
		if(commentTable.isEditing()){
//			JOptionPane.showMessageDialog(this, "編集中のセルは値を確定させてください。");
			commentTable.getCellEditor().cancelCellEditing();
		}

		if (commentList.isModified() || (!xf.isEmpty() && !(new File(xf).exists()))) {
			if(xf.isEmpty()){
				xf = userHomeDir + File.separator + "fw_" + commenter.getName() + ".xml"; //$NON-NLS-1$ //$NON-NLS-2$
				System.err.println("Warning(MainFrame): No filename found. Save the data to the following file.\n" + xf);  //$NON-NLS-1$
			}
			
			String message = commentList.save(xf, commentTypes, discussers);
			JOptionPane.showMessageDialog(this, message);
			
			// invalid column
			if(jMenuItemOptionValidateAnnotations.isSelected()) {
				HashMap<Integer, Integer> invalidColumnSummary = ctm.validateAnnotations();
				if(invalidColumnSummary.size() > 0) {
					message = "<html>"  //$NON-NLS-1$
							+ "<p>" + Messages.getString("MainFrame.154") + "</p>" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							+ "<ul>"; //$NON-NLS-1$
					
					for(Entry<Integer, Integer> i : invalidColumnSummary.entrySet()) {
						message += "<li>"  //$NON-NLS-1$
								+ ctm.getColumnName(i.getKey())
								+ " (" + i.getValue() + ")" + "</li>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					}
					
					message += "</ul></html>"; //$NON-NLS-1$
					JOptionPane.showMessageDialog(this, message);
				}
			}
			
			return;
		}
	}

	private void processesBeforeExit() {

		switch (soundPlayer.getPlayerState()) {
		case SoundPlayer.PLAYER_STATE_RECORD:
				JOptionPane.showMessageDialog(this, Messages.getString("MainFrame.26")); //$NON-NLS-1$
				return;
		case SoundPlayer.PLAYER_STATE_PAUSE:
				soundPlayer.myResume();
		case SoundPlayer.PLAYER_STATE_PLAY:
			soundPlayer.myStop();
			break;
		default:
		}

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// stop cell editing before exit
				if(commentTable.getCellEditor() != null){
					commentTable.getCellEditor().stopCellEditing();
				}
			}
		});
		
		try {
			saveCommentList();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, Messages.getString("MainFrame.27") + xf + Messages.getString("MainFrame.28") + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
			e.printStackTrace();
//			System.exit(0);
		}

		System.exit(0);
	}


	public void updateMediaLengthUI(int prevState){
		if(prevState == SoundPlayer.PLAYER_STATE_RECORD){
			annotationGlobalViewPanel.initScaleFactor();
		}
		annotationGlobalViewPanel.updatePanel();
		timeSlider.setMaximum((int)soundPlayer.getSoundLength());
		timeEnd.setTime((int)soundPlayer.getSoundLength());
	}
	
	
	public void changeState(int state){
		switch(state){
		case SoundPlayer.PLAYER_STATE_PAUSE:
			changeStatePause(); break;
		case SoundPlayer.PLAYER_STATE_PLAY:
			changeStatePlay(); break;
		case SoundPlayer.PLAYER_STATE_RECORD:
			changeStateRecord(); break;
		case SoundPlayer.PLAYER_STATE_STOP:
			changeStateStop(); break;
		}
	}
	
	
	private void changeStateRecord() {
		soundStopButton.setEnabled(true);
		soundRecordButton.setEnabled(false);
		soundPlayButton.setEnabled(false);
		soundForwardButton.setEnabled(false);
		soundBackwardButton.setEnabled(false);
		jMenuItemFileOpen.setEnabled(false);
		jMenuItemURLOpen.setEnabled(false);
		jMenuItemFileSave.setEnabled(false);
		jMenuItemFileMerge.setEnabled(false);
		timeSlider.setEnabled(false);
//		setResizable(false);
	}

	private void changeStateStop() {
		// セルの編集内容の確定
		if (commentTable.getCellEditor() != null) {
			commentTable.getCellEditor().stopCellEditing();
		}
		
		setResizable(true);
		// saveAndClearCommentList();
		soundStopButton.setEnabled(false);
		soundRecordButton.setEnabled(true);
		soundPlayButton.setEnabled(true);
		soundForwardButton.setEnabled(false);
		soundBackwardButton.setEnabled(false);
		soundPlayButton.setIcon(iconPlay);
		jMenuItemFileOpen.setEnabled(true);
		jMenuItemURLOpen.setEnabled(true);
		jMenuItemFileSave.setEnabled(true);
		jMenuItemFileMerge.setEnabled(true);
		jMenuItemControlSkipBackward.setEnabled(false);
		jMenuItemControlSkipForward.setEnabled(false);
		jMenuItemControlPlayPause.setEnabled(true);
		jMenuItemControlRecord.setEnabled(true);
		jMenuItemControlStop.setEnabled(false);
		jMenuItemControlJumpToPrevComment.setEnabled(false);
		jMenuItemControlJumpToNextComment.setEnabled(false);
//		soundPlayer.setPlayPoint(0);
		timeCurrent.setTime(0);
		timeSlider.setValue(0);
		timeSlider.setEnabled(false);
		soundPanel.repaint();
		discussersPanel.repaintComponents();
		commentTable.initState();
		soundPlayer.setOverlayText(""); //$NON-NLS-1$
		
		if (timer != null) {
			timer.cancel();
		}
		timer = new Timer();
		timer.schedule(new DrawAnnotationGlobalViewerTask(), 0, TASK_INTERVAL);
	}
	

	private void changeStatePlay() {
		soundStopButton.setEnabled(true);
		soundRecordButton.setEnabled(false);
		soundPlayButton.setEnabled(true);
		soundForwardButton.setEnabled(true);
		soundBackwardButton.setEnabled(true);
		soundPlayButton.setIcon(iconPause);
		jMenuItemFileOpen.setEnabled(false);
		jMenuItemURLOpen.setEnabled(false);
		jMenuItemFileSave.setEnabled(true);
		jMenuItemFileMerge.setEnabled(false);
		jMenuItemControlSkipBackward.setEnabled(true);
		jMenuItemControlSkipForward.setEnabled(true);
		jMenuItemControlPlayPause.setEnabled(true);
		jMenuItemControlRecord.setEnabled(false);
		jMenuItemControlStop.setEnabled(true);
		jMenuItemControlJumpToPrevComment.setEnabled(true);
		jMenuItemControlJumpToNextComment.setEnabled(true);
		// timeSlider.setEnabled(true);
	}

	private void changeStatePause() {
		soundStopButton.setEnabled(true);
		soundRecordButton.setEnabled(false);
		soundPlayButton.setEnabled(true);
		soundForwardButton.setEnabled(true);
		soundBackwardButton.setEnabled(true);
		soundPlayButton.setIcon(iconPlay);
		jMenuItemFileOpen.setEnabled(false);
		jMenuItemURLOpen.setEnabled(false);
		jMenuItemFileSave.setEnabled(true);
		jMenuItemControlSkipBackward.setEnabled(true);
		jMenuItemControlSkipForward.setEnabled(true);
		jMenuItemControlPlayPause.setEnabled(true);
		jMenuItemControlRecord.setEnabled(false);
		jMenuItemControlStop.setEnabled(true);
		// timeSlider.setEnabled(true);
	}

	private JPanel getTimeOperationPanel() {
		if (timeOperationPanel == null) {
			timeOperationPanel = new JPanel();
			timeOperationPanel.setLayout(new BoxLayout(timeOperationPanel,
					BoxLayout.X_AXIS));
			timeCurrent = new TimeDisplay();
			timeCurrent.setPreferredSize(new Dimension(75, 20));
			timeCurrent.setTime(0);
			timeEnd = new TimeDisplay();
			timeEnd.setTime(0);
			// timeDisplay.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
			timeSlider = new TimeSlider();
			timeSlider.setPreferredSize(new Dimension(250, 20));
			timeSlider.setMinimum(0);
			timeSlider.setValue(0);
			timeSlider.setMajorTickSpacing(300);
			timeSlider.setMinorTickSpacing(60);
			timeSlider.setPaintTicks(true);
			timeSlider.addChangeListener(new ChangeListener() {
				boolean isDragged = false;
				int value = 0;
				int currentState = soundPlayer.getPlayerState();

				public void stateChanged(ChangeEvent e) {
					
					// ドラッグしている時
					if (timeSlider.getValueIsAdjusting()) {
						if(!isDragged){
							currentState = soundPlayer.getPlayerState();
						}
						soundPlayer.myPause();
						timer.cancel();
						value = timeSlider.getValue();
						isDragged = true;
						// ドラッグし終わった時
					} else if (isDragged) {
						if(currentState != SoundPlayer.PLAYER_STATE_PAUSE){
							soundPlayer.myResume();
						}
						soundPlayer.setPlayPoint(value * 1000);
						isDragged = false;
						// スライダーが一瞬元に戻るのを防ぐため，次の soundGraphBuffer が更新されるのを待つ
						try {
							Thread.sleep(100);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
						timerStart();
					}
				}
			});
			timeSlider.addMouseListener(new MouseAdapter() {
				ToolTipManager ttm = ToolTipManager.sharedInstance();
				int defaultDelay = ttm.getInitialDelay();

				@Override
				public void mouseEntered(MouseEvent e) {
					ttm.setInitialDelay(0);
				}
				
				@Override
				public void mouseExited(MouseEvent e) {
					ttm.setInitialDelay(defaultDelay);
				}
			});
			
			timeSlider.addMouseMotionListener(new MouseAdapter() {
				@Override
				public void mouseMoved(MouseEvent e) {
					timeSlider.setTipTime((int)soundPlayer.getSoundLength(), e.getX());
				}
			});
			
			timeOperationPanel.add(timeCurrent);
			timeOperationPanel.add(timeSlider);
			timeOperationPanel.add(timeEnd);
		}

		return timeOperationPanel;
	}

	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			buttonPanel = new JPanel();
			commentButtons = new ArrayList<CommentButton>();

			updateButtonPanel(buttonType);
		}
		return buttonPanel;
	}


	private JPanel getDisplayPanel() {
		if (displayPanel == null) {
			displayPanel = new JPanel();
			displayPanel.setLayout(new BorderLayout());
			displayPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));

			timeLinePanel = getTimeLinePanel();
			annotationGlobalViewPanel = new AnnotationGlobalViewer(ctm, soundPlayer, discussers, commentTypes);
			annotationGlobalViewPanel.setFocusRange(focusRange);
			annotationGlobalViewPanel.setCallBack(new FilteredViewCheckBoxCallBack() {
				@Override
				public void callback(boolean flag) {
					jMenuItemOptionFilteredViewMode.doClick();
				}
			});
			annotationGlobalViewPanel.applyFilter(jMenuItemOptionFilteredViewMode.isSelected());
			annotationGlobalViewPanel.setExternalMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if(e.getClickCount() < 2) {
						return;
					}

					if (soundPlayer.getPlayerState() == SoundPlayer.PLAYER_STATE_STOP) {
						if(mf.isEmpty() || (!SoundPlayer.isPlayable(mf) && !mf.matches("^https?://.+"))){ //$NON-NLS-1$
							JOptionPane.showMessageDialog(MainFrame.this, Messages.getString("MainFrame.29") + mf); //$NON-NLS-1$
							return;
						}

						if(!soundPlayer.setFile(mf, jMenuItemOptionWaveform.isSelected())){
							JOptionPane.showMessageDialog(MainFrame.this, Messages.getString("MainFrame.30") + mf); //$NON-NLS-1$
							return;
						}
					
						isSoundPanelEnable = soundPlayer.getSoundBufferEnable();
						timeSlider.setEnabled(true);

						changeStatePlay();
						soundPlayer.myPlay();
						annotationGlobalViewPanel.setPlayPoint(e.getX());
						timerStart();
					} else if (soundPlayer.getPlayerState() == SoundPlayer.PLAYER_STATE_PAUSE) {
						changeStatePlay();
						annotationGlobalViewPanel.setPlayPoint(e.getX());
						soundPlayer.myPlay();
					} else if (soundPlayer.getPlayerState() == SoundPlayer.PLAYER_STATE_PLAY) {
						annotationGlobalViewPanel.setPlayPoint(e.getX());
					}
				}
			});

			moviePanel = getMoviePanel();
			moviePanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
			
			timeLineTabbedPane = new JTabbedPane(JTabbedPane.TOP);
			timeLineTabbedPane.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
			
			
			timeLineTabbedPane.addTab(Messages.getString("MainFrame.31"), annotationGlobalViewPanel); //$NON-NLS-1$
			timeLineTabbedPane.addTab(Messages.getString("MainFrame.32"), timeLinePanel); //$NON-NLS-1$
			timeLineTabbedPane.setPreferredSize(new Dimension(TIMELINE_PANEL_WIDTH, TIMELINE_PANEL_HEIGHT));
			timeLineTabbedPane.setSelectedIndex(TAB_STATUS_GLOBAL_VIEW); // デフォルトは「全体」
			displayPanel.add(timeLineTabbedPane, BorderLayout.WEST);
			displayPanel.add(moviePanel, BorderLayout.CENTER);
		}
		return displayPanel;
	}

	
	Timer t;
	private JPanel getMoviePanel() {
		if (moviePanel == null) {
			moviePanel = new JPanel();
			moviePanel.setLayout(new BorderLayout());
			moviePanel.add(soundPlayer.getMediaplayerComponent());
			soundPlayer.getMediaplayerComponent().setOpaque(false); // これがないと背景がおかしくなる
			moviePanel.addComponentListener(new ComponentAdapter() {
				public void componentResized(final ComponentEvent ev) {
					if(soundPlayer.getPlayerState() == SoundPlayer.PLAYER_STATE_RECORD){
						return;
					}
					if(t != null) t.cancel();
					t = new Timer();
					t.schedule(new TimerTask() {
						public void run() {
							Dimension size = ev.getComponent().getSize();
							if(timer != null){
								timer.cancel();
							}
							soundPlayer.resizeMediaPlayer(size.width, size.height);
							annotationGlobalViewPanel.updatePanel();
							timerStart();
							System.err.println(ev.getComponent().getSize());
						}
					}, 200);
				}
			});
		}
		return moviePanel;
	}

	
	private JPanel getTimeLinePanel() {
		if (timeLinePanel == null) {
			timeLinePanel = new JPanel();
			timeLinePanel.setLayout(new BoxLayout(timeLinePanel,
					BoxLayout.Y_AXIS));
			// timeLinePanel.setPreferredSize(new
			// Dimension((int)(FishWatchAR.WINDOW_WIDTH*3),
			// FishWatchAR.VIEWER_HEIGHT * FishWatchAR.nUsers));
			timeLinePanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
			soundPanel = getSoundPanel();
//			soundPanel.setOpaque(false);
			timeLinePanel.add(soundPanel);
			discussersPanel = new DiscussersPanel(discussers, MAX_DISCUSSERS, ctm, soundPlayer);
			timeLinePanel.add(discussersPanel);

		}
		return timeLinePanel;
	}


	private SoundPanel getSoundPanel() {
		if (soundPanel == null) {
			soundPanel = new SoundPanel(soundPlayer);
			soundPanel
					.setPreferredSize(new Dimension(
							FishWatchr.WINDOW_WIDTH,
							FishWatchr.SOUND_VIEWER_HEIGHT));
			soundPanel
					.setMaximumSize(new Dimension(FishWatchr.WINDOW_WIDTH,
							FishWatchr.SOUND_VIEWER_HEIGHT));
			soundPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
		}
		return soundPanel;
	}

	public JMenuBar getJMenuBar() {
		if (jMenuBar == null) {
			jMenuBar = new JMenuBar();
			jMenuFile = getJMenuFile();
			jMenuControl = getJMenuControl();
			jMenuAnnotation = getJMenuAnnotation();
			jMenuAnalysis = getJMenuAnalysis();
			jMenuOption = getJMenuOption();
			jMenuHelp = getJMenuHelp();
			jMenuBar.add(jMenuFile);
			jMenuBar.add(jMenuControl);
			jMenuBar.add(jMenuAnnotation);
			jMenuBar.add(jMenuAnalysis);
			jMenuBar.add(jMenuOption);
			jMenuBar.add(jMenuHelp);
			KeyboardFocusManager.getCurrentKeyboardFocusManager()
					.addKeyEventPostProcessor(new KeyEventPostProcessor() {

						@Override
						public boolean postProcessKeyEvent(KeyEvent e) {
							// TODO Auto-generated method stub
							// KeyboardFocusManager.getCurrentKeyboardFocusManager().redispatchEvent(CommentTable.this,
							// e);
							return false;
						}
					});

			KeyboardFocusManager.getCurrentKeyboardFocusManager()
					.addKeyEventDispatcher(new KeyEventDispatcher() {

						@Override
						public boolean dispatchKeyEvent(KeyEvent e) {
							// KeyboardFocusManager.getCurrentKeyboardFocusManager().redispatchEvent(MainFrame.this,
							// e);
							// TODO Auto-generated method stub
							return false;
						}
					});
			setFocusable(true);

		}
		return jMenuBar;
	}

	private JMenu getJMenuFile() {
		if (jMenuFile == null) {
			jMenuFile = new JMenu();
			jMenuFile.setText(Messages.getString("MainFrame.33")); //$NON-NLS-1$
			jMenuFile.add(getJMenuItemFileOpen());
			jMenuFile.add(getJMenuItemURLOpen());
			jMenuFile.add(getJMenuItemFileSave());
			jMenuFile.add(getJMenuItemFileExport());
			jMenuFile.add(getJMenuItemFileMerge());
			jMenuFile.add(getJMenuItemFileSaveConfig());
			jMenuFile.add(getJMenuItemFileExit());
		}
		return jMenuFile;
	}

	private JMenuItem getJMenuItemFileOpen() {
		if (jMenuItemFileOpen == null) {
			jMenuItemFileOpen = new JMenuItem(Messages.getString("MainFrame.34")); //$NON-NLS-1$
			jMenuItemFileOpen.setAccelerator(KeyStroke.getKeyStroke('O',
					KeyEvent.CTRL_MASK, false));
			jMenuItemFileOpen
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							soundPlayer.myStop();
							if(!setTargetFile("")){ //$NON-NLS-1$
								return;
							}
							changeStatePlay();
							soundPlayer.myPlay();
							timerStart();
						}
					});
		}
		return jMenuItemFileOpen;
	}

	private JMenuItem getJMenuItemURLOpen() {
		if (jMenuItemURLOpen == null) {
			jMenuItemURLOpen = new JMenuItem(Messages.getString("MainFrame.35")); //$NON-NLS-1$
			jMenuItemURLOpen.setAccelerator(KeyStroke.getKeyStroke('U',
					KeyEvent.CTRL_MASK, false));
			jMenuItemURLOpen
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							soundPlayer.myStop();
							if(!setTargetFile(null)){
								return;
							}
							changeStatePlay();
							soundPlayer.myPlay();
							timerStart();
						}
					});
		}
		return jMenuItemURLOpen;
	}

	private String chooseFile(FileFilter filter, int fileType, boolean isSaveDialog) {
		JFileChooser jfc = new JFileChooser();
		jfc.setFileSelectionMode(fileType);

		if (filter != null) {
			
			jfc.setFileFilter(filter);
		}

		int result;
		if(isSaveDialog){
			result = jfc.showSaveDialog(MainFrame.this);
		} else {
			result = jfc.showOpenDialog(MainFrame.this);
		}
		
		if (result == JFileChooser.APPROVE_OPTION) {
			try {
				return jfc.getSelectedFile().getCanonicalPath();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return ""; //$NON-NLS-1$
	}

	
	private String chooseTargetFile() {
		JFileChooser jfc = new JFileChooser();
		jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		String allSuffix[] = Arrays.copyOf(SoundPlayer.getPlayableFileExtensions(), SoundPlayer.getPlayableFileExtensions().length+1);
		allSuffix[SoundPlayer.getPlayableFileExtensions().length] = "xml"; //$NON-NLS-1$
		
		FileFilter fishWatchrFilter = new FishWatchrFileFilter(Messages.getString("MainFrame.36"), FishWatchrFileFilter.TYPE_ALL); //$NON-NLS-1$
		FileFilter mediaFilter = new FishWatchrFileFilter(Messages.getString("MainFrame.37"), FishWatchrFileFilter.TYPE_MEDIA); //$NON-NLS-1$
		FileFilter xmlFilter = new FishWatchrFileFilter(Messages.getString("MainFrame.38"), FishWatchrFileFilter.TYPE_XML); //$NON-NLS-1$
		jfc.setAcceptAllFileFilterUsed(false);
		jfc.addChoosableFileFilter(fishWatchrFilter);
		jfc.addChoosableFileFilter(xmlFilter);
		jfc.addChoosableFileFilter(mediaFilter);

		int result = jfc.showOpenDialog(MainFrame.this);
		
		if (result == JFileChooser.APPROVE_OPTION) {
			try {
				return jfc.getSelectedFile().getCanonicalPath();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return ""; //$NON-NLS-1$
	}
	

	class FishWatchrFileFilter extends FileFilter {

		static final int TYPE_ALL = 0; 
		static final int TYPE_MEDIA = 1;
		static final int TYPE_XML = 2;
		String description;
		int type = TYPE_ALL; 
		

		public FishWatchrFileFilter(String description){
			this.description = description;
		}
		
		public FishWatchrFileFilter(String description, int type){
			this.description = description;
			if(type > TYPE_XML){
				this.type = TYPE_ALL;
			} else {
				this.type = type;
			}
		}
		
		public boolean accept(File f) {
			if (f.isDirectory()) {
				return true;
			}
			
			String filename = f.getName();
			
			if(filename.endsWith(SoundPlayer.SOUNDFILE_EXTENSION) &&
					(type == TYPE_ALL || type == TYPE_MEDIA)){
				// 生成元のメディアファイルがないか調べる
				String candidate = filename.replaceFirst(SoundPlayer.SOUNDFILE_EXTENSION + "$",  ""); //$NON-NLS-1$ //$NON-NLS-2$
				if(SoundPlayer.isPlayable(candidate)){
					for(String child: new File(f.getParent()).list()){
						// 生成元の wav ファイルがある場合は，リストに表示しない
						if(child.equals(candidate)){
							return false;
						}
					}
				}
				return true;
			} else if(SoundPlayer.isPlayable(filename) &&
					(type == TYPE_ALL || type == TYPE_MEDIA)){
				return true;
			} else if(filename.endsWith(CommentList.FILE_SUFFIX) &&
					(type == TYPE_ALL || type == TYPE_XML)){
				return true;
			} else {
				return false;
			}
		}

		public String getDescription() {
			return description;
		}
	}
	
	
	private JMenuItem getJMenuItemFileSave() {
		if (jMenuItemFileSave == null) {
			jMenuItemFileSave = new JMenuItem(Messages.getString("MainFrame.39")); //$NON-NLS-1$
			jMenuItemFileSave.setAccelerator(KeyStroke.getKeyStroke('S',
					KeyEvent.CTRL_MASK, false));
			jMenuItemFileSave
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							String message = ""; //$NON-NLS-1$
							try {
								if(xf.isEmpty()){
									// 「新規に注釈をつける場合は，録音/録画状態で行ってください。」は必要？
									JOptionPane.showMessageDialog(MainFrame.this, Messages.getString("MainFrame.40")); //$NON-NLS-1$
									return;
								} else if (soundPlayer.getPlayerState() == SoundPlayer.PLAYER_STATE_PLAY) {
									soundPlayer.myPause();
									message = commentList.save(xf, commentTypes, discussers);
									soundPlayer.myResume();
								} else {
									message = commentList.save(xf, commentTypes, discussers);
								}
							} catch (IOException e1) {
								e1.printStackTrace();
								JOptionPane.showMessageDialog(MainFrame.this, e1.getMessage());
							}
							
							if(!message.isEmpty()){
								if(!noOverwriteConfirmation){
									JCheckBox cb = new JCheckBox(Messages.getString("MainFrame.60")); //$NON-NLS-1$
									Object[] obj = {message, Box.createVerticalStrut(10), cb};
									JOptionPane.showMessageDialog(MainFrame.this, obj, Messages.getString("MainFrame.66"), JOptionPane.OK_OPTION); //$NON-NLS-1$
									
									if(cb.isSelected()){
										noOverwriteConfirmation = true;
									}
								}
							}
						}
					});
		}
		return jMenuItemFileSave;
	}

	
	private JMenuItem getJMenuItemFileExport() {
		if (jMenuItemFileExport == null) {
			jMenuItemFileExport = new JMenuItem(Messages.getString("MainFrame.41")); //$NON-NLS-1$
			jMenuItemFileExport.setAccelerator(KeyStroke.getKeyStroke('E',
					KeyEvent.CTRL_MASK, false));
			jMenuItemFileExport
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							try {
								String saveFilename = chooseFile(null, JFileChooser.FILES_ONLY, true);
								if(saveFilename.isEmpty()){
									JOptionPane.showMessageDialog(MainFrame.this, Messages.getString("MainFrame.42")); //$NON-NLS-1$
									return;
								} else if(!saveFilename.endsWith(".tsv")){ //$NON-NLS-1$
									saveFilename += ".tsv"; //$NON-NLS-1$
								}
								File saveFile = new File(saveFilename);
								if(saveFile.exists()){
									int response = JOptionPane.showConfirmDialog(MainFrame.this, saveFile.getName() + Messages.getString("MainFrame.43"), Messages.getString("MainFrame.44"), JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$ //$NON-NLS-2$
									if(response != JOptionPane.OK_OPTION){
										JOptionPane.showMessageDialog(MainFrame.this, Messages.getString("MainFrame.45")); //$NON-NLS-1$
										return;
									}
								}
								
								commentList.export(saveFilename);
								JOptionPane.showMessageDialog(MainFrame.this, Messages.getString("MainFrame.46") + saveFile.getName()); //$NON-NLS-1$
							} catch (IOException e1) {
								e1.printStackTrace();
								JOptionPane.showMessageDialog(MainFrame.this,
										Messages.getString("MainFrame.48") + e1.toString()); //$NON-NLS-1$
							}
						}
					});
		}
		return jMenuItemFileExport;
	}
	
	
	private JMenuItem getJMenuItemFileMerge() {
		if (jMenuItemFileMerge == null) {
			jMenuItemFileMerge = new JMenuItem(Messages.getString("MainFrame.49")); //$NON-NLS-1$
			jMenuItemFileMerge.setAccelerator(KeyStroke.getKeyStroke('M',
					KeyEvent.CTRL_MASK, false));
			jMenuItemFileMerge
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							soundPlayer.myStop();

							try {
								saveCommentList();
							} catch (IOException e1) {
								JOptionPane.showMessageDialog(MainFrame.this, Messages.getString("MainFrame.50") + e1); //$NON-NLS-1$
								e1.printStackTrace();
								return;
							}

							String targetDir = chooseFile(null, JFileChooser.DIRECTORIES_ONLY, false); // String[0] は空の配列
							if (targetDir.isEmpty()) {
								JOptionPane.showMessageDialog(MainFrame.this,
										Messages.getString("MainFrame.52")); //$NON-NLS-1$
								return;
							}

							if(!mergeAnnotationFiles(targetDir)){
								return;
							}
							changeStatePlay();
							soundPlayer.myPlay();
							timerStart();
						}
					});
		}
		return jMenuItemFileMerge;
	}

	
	private boolean mergeAnnotationFiles(String directoryName){
	    Date recordingDate = null;
	    if(iMergeMode > 0){
			String inputValue = JOptionPane.showInputDialog(
					MainFrame.this, Messages.getString("MainFrame.53") + //$NON-NLS-1$
							Messages.getString("MainFrame.55"), //$NON-NLS-1$
					JOptionPane.PLAIN_MESSAGE);
			if (inputValue != null) {
				try{
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"); //$NON-NLS-1$
					recordingDate = dateFormat.parse(inputValue);
				} catch (ParseException e) {
					JOptionPane.showMessageDialog(MainFrame.this,
							Messages.getString("MainFrame.56") + e); //$NON-NLS-1$
					return false;
				}
			} else {
				return false;
			}
	    }

	    try {
	    	// merge
			ArrayList<String> results = commentList.merge(directoryName, commentTypes, discussers);
			ArrayList<String> deletedResults = null;
			String deleteMessage = ""; //$NON-NLS-1$
			if(commentList.isDuplicated()){
				int optionValue = JOptionPane.showConfirmDialog(this, Messages.getString("MainFrame.57"), Messages.getString("MainFrame.58"), JOptionPane.YES_NO_OPTION); //$NON-NLS-1$ //$NON-NLS-2$
				if(optionValue == JOptionPane.YES_OPTION){
					deletedResults = commentList.mergeComments();
					deleteMessage = "<li>" + deletedResults.size() + Messages.getString("MainFrame.59") + "</li>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				} else {
					deleteMessage = "<li>" + Messages.getString("MainFrame.61") + "</li>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
			}
			
			mf = results.remove(0); // mediafilename
			xf = CommentList.getUniqueFilename(mf + CommentList.MERGED_FILE_SUFFIX);
			commentList.setModified(true);
			Collections.sort(results);

			boolean flagSyncCondition = true; // good
		    if(iMergeMode > 0){
		    	commentList.setStartTime(recordingDate);
		    	flagSyncCondition = commentList.syncByStartTime();
		    	commentList.sortByTime();
		    }

		    String message = 
		    		"<html>" //$NON-NLS-1$
		    		+ "<h1>" + Messages.getString("MainFrame.62") + "</h1>" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		    		+ "<ul>"  //$NON-NLS-1$
		    		+ "<li>" + mergeModes[iMergeMode] + Messages.getString("MainFrame.63") + "</li>" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					+ "<li>" + Messages.getString("MainFrame.64") + "<br />" + xf + "</li>"  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					+ "<li>" + results.size() + Messages.getString("MainFrame.65") + "</li>" //$NON-NLS-1$ //$NON-NLS-2$  //$NON-NLS-3$
					+ "<li>" + Messages.getString("MainFrame.67") + commentList.size() + Messages.getString("MainFrame.68") + "</li>" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		    		+ deleteMessage
		    		+ "</ul>"  //$NON-NLS-1$
		    		+ "<h1>" + Messages.getString("MainFrame.69") + "</h1>" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		    		+ "<ul><li>" + StringUtils.join(results, "</li><li>") + "</li></ul>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		    if(deletedResults != null){
		    	message +=
		    			"<h1>" + Messages.getString("MainFrame.70") + "</h1>" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					    + "<ul><li>" + StringUtils.join(deletedResults, "</li><li>") + "</li></ul>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		    			
		    }
		    JTextPane messagePane = new JTextPane();
			messagePane.setContentType("text/html"); //$NON-NLS-1$
		    messagePane.setText(message);
		    JScrollPane scrollPane = new JScrollPane(messagePane);
		    scrollPane.setPreferredSize(new Dimension(550, 400));
		    messagePane.setCaretPosition(0);
			JOptionPane.showMessageDialog(MainFrame.this, scrollPane);

			if(!flagSyncCondition){
				JOptionPane.showMessageDialog(MainFrame.this,
						Messages.getString("MainFrame.71") //$NON-NLS-1$
						+ Messages.getString("MainFrame.72") + ctm.getColumnName(Comment.F_COMMENT_TIME) + Messages.getString("MainFrame.73")); //$NON-NLS-1$ //$NON-NLS-2$
			}
			
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(MainFrame.this,
					Messages.getString("MainFrame.74") + e1); //$NON-NLS-1$
			return false;
		}

	    
		ctm.refreshFilter();
		updateButtonPanel(buttonType);
		ctm.fireTableDataChanged();

		if(!soundPlayer.setFile(mf, jMenuItemOptionWaveform.isSelected())){
			JOptionPane.showMessageDialog(MainFrame.this, Messages.getString("MainFrame.75") + mf); //$NON-NLS-1$
			mf = ""; //$NON-NLS-1$
			xf = ""; //$NON-NLS-1$
			return false;
		}
		isSoundPanelEnable = soundPlayer.getSoundBufferEnable();
		commentList.setSetName(xf, commenter);

		setWindowTitle(xf);
		timerStart();
		timeSlider.setMinimum(0);
		timeSlider.setMaximum((int) soundPlayer.getSoundLength());
		timeSlider.setEnabled(true);
		timeEnd.setTime((int) soundPlayer.getSoundLength());
		annotationGlobalViewPanel.updatePanel();
		annotationGlobalViewPanel.init();

		soundPlayer.myStop();
		changeStateStop();

		return true;
	}


	private JMenuItem getJMenuItemFileSaveConfig() {
		if (jMenuItemFileSaveConfig == null) {
			jMenuItemFileSaveConfig = new JMenuItem(Messages.getString("MainFrame.76")); //$NON-NLS-1$
			jMenuItemFileSaveConfig.setAccelerator(KeyStroke.getKeyStroke('C',
					KeyEvent.CTRL_MASK, false));
			jMenuItemFileSaveConfig
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							try {
								config.save();
								JOptionPane.showMessageDialog(MainFrame.this, Messages.getString("MainFrame.77") + SysConfig.CONFIG_FILENAME); //$NON-NLS-1$
							} catch (IOException e1) {
								JOptionPane.showMessageDialog(MainFrame.this, Messages.getString("MainFrame.79") + e1.getLocalizedMessage()); //$NON-NLS-1$
								e1.printStackTrace();
							} catch (TransformerException e1) {
								JOptionPane.showMessageDialog(MainFrame.this, Messages.getString("MainFrame.80") + e1.getLocalizedMessage()); //$NON-NLS-1$
								e1.printStackTrace();
							}
						}
					});
		}
		return jMenuItemFileSaveConfig;
	}
	
	
	private JMenuItem getJMenuItemFileExit() {
		if (jMenuItemFileExit == null) {
			jMenuItemFileExit = new JMenuItem(Messages.getString("MainFrame.81")); //$NON-NLS-1$
			jMenuItemFileExit.setAccelerator(KeyStroke.getKeyStroke('Q',
					KeyEvent.CTRL_MASK, false));
			jMenuItemFileExit
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							processesBeforeExit();
						}
					});
		}
		return jMenuItemFileExit;
	}

	private JMenu getJMenuControl() {
		if (jMenuControl == null) {
			jMenuControl = new JMenu();
			jMenuControl.setText(Messages.getString("MainFrame.82")); //$NON-NLS-1$
			jMenuControl.add(getJMenuItemControlPlayAndPause());
			jMenuControl.add(getJMenuItemControlSkipBackward());
			jMenuControl.add(getJMenuItemControlSkipForward());
			jMenuControl.add(getJMenuItemControlStop());
			jMenuControl.add(getJMenuItemControlRecord());
			jMenuControl.addSeparator();
			jMenuControl.add(getJMenuItemControlJumpToPrevComment());
			jMenuControl.add(getJMenuItemControlJumpToNextComment());
			jMenuControl.add(getJMenuItemControlScroll());
			jMenuControl.addSeparator();
			jMenuControl.add(getJMenuItemControlPlayRateUp());
			jMenuControl.add(getJMenuItemControlPlayRateDown());
			jMenuControl.add(getJMenuItemControlPlayRateReset());
		}
		return jMenuControl;
	}

	private JMenuItem getJMenuItemControlPlayRateUp() {
		if (jMenuItemControlPlayRateUp == null) {
			jMenuItemControlPlayRateUp = new JMenuItem(Messages.getString("MainFrame.83")); //$NON-NLS-1$
			jMenuItemControlPlayRateUp.setAccelerator(KeyStroke.getKeyStroke(
					KeyEvent.VK_PERIOD, InputEvent.CTRL_DOWN_MASK, false));
			jMenuItemControlPlayRateUp
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							if (playRate < 2.0) {
								playRate += 0.2f;
								soundPlayer.setPlayRate(playRate);
							}
						}
					});
		}
		return jMenuItemControlPlayRateUp;
	}

	private JMenuItem getJMenuItemControlPlayRateDown() {
		if (jMenuItemControlPlayRateDown == null) {
			jMenuItemControlPlayRateDown = new JMenuItem(Messages.getString("MainFrame.84")); //$NON-NLS-1$
			jMenuItemControlPlayRateDown.setAccelerator(KeyStroke.getKeyStroke(
					KeyEvent.VK_COMMA, InputEvent.CTRL_DOWN_MASK, false));
			jMenuItemControlPlayRateDown
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							if (playRate > 0) {
								playRate -= 0.1f;
								soundPlayer.setPlayRate(playRate);
							}
						}
					});
		}
		return jMenuItemControlPlayRateDown;
	}

	private JMenuItem getJMenuItemControlPlayRateReset() {
		if (jMenuItemControlPlayRateReset == null) {
			jMenuItemControlPlayRateReset = new JMenuItem(Messages.getString("MainFrame.85")); //$NON-NLS-1$
			jMenuItemControlPlayRateReset.setAccelerator(KeyStroke
					.getKeyStroke(KeyEvent.VK_SLASH, InputEvent.CTRL_DOWN_MASK,
							false));
			jMenuItemControlPlayRateReset
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							playRate = 1.0f;
							soundPlayer.setPlayRate(playRate);
						}
					});
		}
		return jMenuItemControlPlayRateReset;
	}

	private JMenuItem getJMenuItemControlPlayAndPause() {
		if (jMenuItemControlPlayPause == null) {
			jMenuItemControlPlayPause = new JMenuItem(Messages.getString("MainFrame.86")); //$NON-NLS-1$
			jMenuItemControlPlayPause.setAccelerator(KeyStroke.getKeyStroke(
					KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK, false));
			jMenuItemControlPlayPause
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							soundPlayButton.doClick();
						}
					});
		}
		return jMenuItemControlPlayPause;
	}

	private JMenuItem getJMenuItemControlStop() {
		if (jMenuItemControlStop == null) {
			jMenuItemControlStop = new JMenuItem(Messages.getString("MainFrame.87")); //$NON-NLS-1$
			jMenuItemControlStop.setAccelerator(KeyStroke.getKeyStroke(
					KeyEvent.VK_H, InputEvent.CTRL_DOWN_MASK|InputEvent.SHIFT_DOWN_MASK, false));
			jMenuItemControlStop
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							soundStopButton.doClick();
						}
					});
		}
		return jMenuItemControlStop;
	}

	private JMenuItem getJMenuItemControlRecord() {
		if (jMenuItemControlRecord == null) {
			jMenuItemControlRecord = new JMenuItem(Messages.getString("MainFrame.88")); //$NON-NLS-1$
			jMenuItemControlRecord
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							soundRecordButton.doClick();
						}
					});
		}
		return jMenuItemControlRecord;
	}

	private JMenuItem getJMenuItemControlSkipBackward() {
		if (jMenuItemControlSkipBackward == null) {
			jMenuItemControlSkipBackward = new JMenuItem(Messages.getString("MainFrame.89")); //$NON-NLS-1$
			jMenuItemControlSkipBackward.setAccelerator(KeyStroke.getKeyStroke(
					KeyEvent.VK_K, InputEvent.CTRL_DOWN_MASK, false));
			jMenuItemControlSkipBackward
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							soundBackwardButton.doClick();
						}
					});
		}
		return jMenuItemControlSkipBackward;
	}

	private JMenuItem getJMenuItemControlSkipForward() {
		if (jMenuItemControlSkipForward == null) {
			jMenuItemControlSkipForward = new JMenuItem(Messages.getString("MainFrame.90")); //$NON-NLS-1$
			jMenuItemControlSkipForward.setAccelerator(KeyStroke.getKeyStroke(
					KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK, false));
			jMenuItemControlSkipForward
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							soundForwardButton.doClick();
						}
					});
		}
		return jMenuItemControlSkipForward;
	}

	private long prevSkipTime = 0;
	private int prevSkipCommentIndex = -1;
	
	private JMenuItem getJMenuItemControlJumpToPrevComment() {
		if (jMenuItemControlJumpToPrevComment == null) {
			jMenuItemControlJumpToPrevComment = new JMenuItem(Messages.getString("MainFrame.91")); //$NON-NLS-1$
			jMenuItemControlJumpToPrevComment.setAccelerator(KeyStroke
					.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_DOWN_MASK|InputEvent.SHIFT_DOWN_MASK,
							false));
			jMenuItemControlJumpToPrevComment
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							int currentCommentIndex = commentTable.getCurrentCommentPosition();
							ArrayList<Comment> filteredCommentList = ctm.getFilteredCommentList();
							long now = new Date().getTime();
							if(now - prevSkipTime < THRESHOLD_CLICK_INTERVAL){
								if(prevSkipCommentIndex - 1 >= 0){
									currentCommentIndex = prevSkipCommentIndex - 1;
									prevSkipCommentIndex = currentCommentIndex;
								} else {
									prevSkipTime = now;	
									return;
								}
							} else {
								if(currentCommentIndex == CommentTable.UNDEFINED){
									currentCommentIndex = commentTable.getNearCommentPosition(soundPlayer.getElapsedTime(), false);
									if(currentCommentIndex == CommentTable.UNDEFINED){
										prevSkipTime = now;	
										return;
									}
									prevSkipCommentIndex = currentCommentIndex;
								} else {
									if(currentCommentIndex - 1 >= 0){
										currentCommentIndex--;
										prevSkipCommentIndex = currentCommentIndex;
									}
								}
							}
							prevSkipTime = now;	
							Comment nextComment;
							nextComment = filteredCommentList.get(currentCommentIndex);
							if(commentList.unifiedCommentTime(nextComment) + adjustmentTimeAtJump < 0){
								JOptionPane.showMessageDialog(MainFrame.this, Messages.getString("MainFrame.92")); //$NON-NLS-1$
								return;
							}
							soundPlayer.setPlayPoint(commentList.unifiedCommentTime(nextComment) + adjustmentTimeAtJump);
							if (soundPlayer.getPlayerState() == SoundPlayer.PLAYER_STATE_PAUSE) {
								// 300msec くらいにしないと数秒戻ってしまう
								soundPlayer.setPlayPoint(commentList.unifiedCommentTime(nextComment)+300);
							} else {
								soundPlayer.setPlayPoint(commentList.unifiedCommentTime(nextComment) + adjustmentTimeAtJump);
							}
						}
					});
		}
		return jMenuItemControlJumpToPrevComment;
	}

	private JMenuItem getJMenuItemControlJumpToNextComment() {
		if (jMenuItemControlJumpToNextComment == null) {
			jMenuItemControlJumpToNextComment = new JMenuItem(Messages.getString("MainFrame.93")); //$NON-NLS-1$
			jMenuItemControlJumpToNextComment.setAccelerator(KeyStroke
					.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK|InputEvent.SHIFT_DOWN_MASK,
							false));
			jMenuItemControlJumpToNextComment
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							int currentCommentIndex = commentTable.getCurrentCommentPosition();
							ArrayList<Comment> filteredCommentList = ctm.getFilteredCommentList();
							long now = new Date().getTime();
							if(now - prevSkipTime < THRESHOLD_CLICK_INTERVAL){
								if(prevSkipCommentIndex + 1 < filteredCommentList.size()){
									currentCommentIndex = prevSkipCommentIndex + 1;
									prevSkipCommentIndex = currentCommentIndex;
								} else {
									prevSkipTime = now;	
									return;
								}
							} else {
								if(currentCommentIndex == CommentTable.UNDEFINED){
									currentCommentIndex = commentTable.getNearCommentPosition(soundPlayer.getElapsedTime(), true);
									if(currentCommentIndex == CommentTable.UNDEFINED){
										prevSkipTime = now;	
										return;
									}
									prevSkipCommentIndex = currentCommentIndex;
								} else {
									if(currentCommentIndex + 1 < filteredCommentList.size()){
										currentCommentIndex++;
										prevSkipCommentIndex = currentCommentIndex;
									}
									
								}
							}
							prevSkipTime = now;	
							Comment nextComment;
							nextComment = filteredCommentList.get(currentCommentIndex);
							if((commentList.unifiedCommentTime(nextComment) + adjustmentTimeAtJump) / 1000 > soundPlayer.getSoundLength()){
								JOptionPane.showMessageDialog(MainFrame.this, Messages.getString("MainFrame.94")); //$NON-NLS-1$
								return;
							}
							if (soundPlayer.getPlayerState() == SoundPlayer.PLAYER_STATE_PAUSE) {
								// 300msec くらいにしないと数秒戻ってしまう
								soundPlayer.setPlayPoint(commentList.unifiedCommentTime(nextComment)+300);
							} else {
								soundPlayer.setPlayPoint(commentList.unifiedCommentTime(nextComment) + adjustmentTimeAtJump);
							}
						}
					});
		}
		return jMenuItemControlJumpToNextComment;
	}

	private JMenuItem getJMenuItemControlScroll() {
		if (jMenuItemControlScroll == null) {
			jMenuItemControlScroll = new JMenuItem(Messages.getString("MainFrame.95")); //$NON-NLS-1$
			jMenuItemControlScroll.setAccelerator(KeyStroke.getKeyStroke('J',
					KeyEvent.CTRL_MASK, false));
			jMenuItemControlScroll
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							System.err.println("jump!!"); //$NON-NLS-1$
							commentTable.setViewCenterByTime(soundPlayer.getElapsedTime());
						}
					});
		}
		return jMenuItemControlScroll;
	}	
	

	private JMenu getJMenuAnnotation() {
		if (jMenuAnnotation == null) {
			jMenuAnnotation = new JMenu();
			jMenuAnnotation.setText(Messages.getString("MainFrame.96")); //$NON-NLS-1$
			jMenuAnnotation.add(getJMenuItemAnnotationYourName());
			jMenuAnnotation.add(getJMenuItemAnnotationDiscussers());
			jMenuAnnotation.add(getJMenuItemAnnotationAnnotation());
			jMenuAnnotation.addSeparator();
			// jMenuAnnotation.add(getJMenuAnnotationOption());
			jMenuAnnotation.add(getJMenuItemAnnotationOrderDiscusser());
			jMenuAnnotation.add(getJMenuItemAnnotationOrderType());
			if (buttonType == CommentButton.BUTTON_TYPE_DISCUSSER) {
				jMenuItemAnnotationOrderDiscusser.setSelected(true);
			} else {
				jMenuItemAnnotationOrderType.setSelected(true);
			}
			ButtonGroup bg = new ButtonGroup();
			bg.add(jMenuItemAnnotationOrderDiscusser);
			bg.add(jMenuItemAnnotationOrderType);
			jMenuAnnotation.addSeparator();
			jMenuAnnotation.add(getJMenuItemAnnotationMulti());
		}
		return jMenuAnnotation;
	}

	private void updateButtonPanel(int newButtonType) {
		buttonType = newButtonType;
		buttonPanel.removeAll();
		commentButtons.clear();
		
		if(jMenuItemAnnotationMulti != null){
			isAnnotationMulti = jMenuItemAnnotationMulti.isSelected();
		}

		int i = 0;
		if (buttonType == CommentButton.BUTTON_TYPE_DISCUSSER) {
			for (User discusser : discussers) {
				if (!discusser.getName().isEmpty()) {
					final CommentButton newCommentButton = new CommentButton(ctm,
							soundPlayer, isAnnotationMulti,
							discusser, commentTypes, commenter);
					newCommentButton.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							Comment newComment = newCommentButton.addComment(e.getModifiers());
							int row = ctm.findFilteredComment(newComment);
							if(row != -1){
								if(commentTable.isEditing()){
									commentTable.getCellEditor().stopCellEditing();
								}
								commentTable.editCellAt(row, Comment.F_COMMENT);
								commentTable.getEditorComponent().requestFocusInWindow();
								commentTable.setViewCenteraByIndex(row);
							}
						}
					});
					newCommentButton.setActionKey(i++);
					newCommentButton.setPreferredSize(new Dimension(80, 40));
					commentButtons.add(newCommentButton);
					buttonPanel.add(newCommentButton);
				}
			}
		} else if (buttonType == CommentButton.BUTTON_TYPE_COMMENT) {
			for (CommentType commentType : commentTypes) {
				if (!commentType.getType().isEmpty()) {
					final CommentButton newCommentButton = new CommentButton(ctm,
							soundPlayer, isAnnotationMulti,
							commentType, discussers, commenter);
					newCommentButton.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							Comment newComment = newCommentButton.addComment(e.getModifiers());
							int row = ctm.findFilteredComment(newComment);
							if(row != -1){
								if(commentTable.isEditing()){
									commentTable.getCellEditor().stopCellEditing();
								}
								commentTable.editCellAt(row, Comment.F_COMMENT);
								commentTable.getEditorComponent().requestFocusInWindow();
								commentTable.setViewCenteraByIndex(row);
							}
						}
					});
					newCommentButton.setActionKey(i++);
					newCommentButton.setPreferredSize(new Dimension(80, 40));
					commentButtons.add(newCommentButton);
					buttonPanel.add(newCommentButton);
				}
			}

		}
		buttonPanel.updateUI();
		discussersPanel.updateCompoments();
	}

	private JMenuItem getJMenuItemAnnotationOrderDiscusser() {
		if (jMenuItemAnnotationOrderDiscusser == null) {
			jMenuItemAnnotationOrderDiscusser = new JRadioButtonMenuItem(ctm.getColumnName(Comment.F_COMMENT_TARGET) + Messages.getString("MainFrame.97")); //$NON-NLS-1$
			jMenuItemAnnotationOrderDiscusser
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							updateButtonPanel(CommentButton.BUTTON_TYPE_DISCUSSER);
							try {
								config.setValue("/settings/button_type", "value", CommentButton.BUTTON_TYPE_DISCUSSER_STR); //$NON-NLS-1$ //$NON-NLS-2$
							} catch (XPathExpressionException e1) {
								e1.printStackTrace();
							}
						}
					});
		}
		return jMenuItemAnnotationOrderDiscusser;
	}

	private JMenuItem getJMenuItemAnnotationOrderType() {
		if (jMenuItemAnnotationOrderType == null) {
			jMenuItemAnnotationOrderType = new JRadioButtonMenuItem(ctm.getColumnName(Comment.F_COMMENT_LABEL) + Messages.getString("MainFrame.98")); //$NON-NLS-1$
			jMenuItemAnnotationOrderType
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							updateButtonPanel(CommentButton.BUTTON_TYPE_COMMENT);
							try {
								config.setValue("/settings/button_type", "value", CommentButton.BUTTON_TYPE_COMMENT_STR); //$NON-NLS-1$ //$NON-NLS-2$
							} catch (XPathExpressionException e1) {
								e1.printStackTrace();
							}
						}
					});
		}
		return jMenuItemAnnotationOrderType;
	}

	private JMenuItem getJMenuItemAnnotationMulti() {
		if (jMenuItemAnnotationMulti == null) {
			jMenuItemAnnotationMulti = new JCheckBoxMenuItem(Messages.getString("MainFrame.99")); //$NON-NLS-1$
			jMenuItemAnnotationMulti
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							for (CommentButton commentButton : commentButtons) {
								commentButton
										.setMultiAnnotation(jMenuItemAnnotationMulti
												.isSelected());
							}
							try {
								if(jMenuItemAnnotationMulti.isSelected()){
									config.setValue("/settings/isAnnotationMulti", "value", "true"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
								} else {
									config.setValue("/settings/isAnnotationMulti", "value", "false"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
								}
							} catch (XPathExpressionException e1) {
								e1.printStackTrace();
							}
						}
					});
		}
		return jMenuItemAnnotationMulti;

	}

	private JMenuItem getJMenuItemAnnotationYourName() {
		if (jMenuItemAnnotationYourName == null) {
			jMenuItemAnnotationYourName = new JMenuItem(ctm.getColumnName(Comment.F_ANNOTATOR) + Messages.getString("MainFrame.100")); //$NON-NLS-1$
			jMenuItemAnnotationYourName
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							String currentCommenterName = commenter.getName() == null ? Messages.getString("MainFrame.101") //$NON-NLS-1$
									: commenter.getName();
							String inputValue = JOptionPane.showInputDialog(
									MainFrame.this, Messages.getString("MainFrame.102") //$NON-NLS-1$
											+ currentCommenterName, ctm.getColumnName(Comment.F_ANNOTATOR) + Messages.getString("MainFrame.103"), //$NON-NLS-1$
									JOptionPane.PLAIN_MESSAGE);
							if(inputValue == null){
								// do nothing if canceled
							} else if(inputValue.isEmpty()){
								JOptionPane.showMessageDialog(null, ctm.getColumnName(Comment.F_ANNOTATOR) + Messages.getString("MainFrame.104")); //$NON-NLS-1$
							} else if(inputValue.matches(".*[\\s<>/&'\"].*")){ //$NON-NLS-1$
								JOptionPane.showMessageDialog(null, inputValue + Messages.getString("MainFrame.105")); //$NON-NLS-1$
							} else {
								commenter.setName(inputValue);
								commentTable.setAnnotator(commenter.getName());
							}
						}
					});
		}
		return jMenuItemAnnotationYourName;
	}

	private JMenuItem getJMenuItemAnnotationDiscussers() {
		if (jMenuItemAnnotationDiscussers == null) {
			jMenuItemAnnotationDiscussers = new JMenuItem(ctm.getColumnName(Comment.F_COMMENT_TARGET));
			jMenuItemAnnotationDiscussers
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							DiscusserSettingPanel discusserSettingPanel = new DiscusserSettingPanel(
									discussers, MAX_DISCUSSERS);
							int selectedValue = JOptionPane.showConfirmDialog(
									null, discusserSettingPanel, ctm.getColumnName(Comment.F_COMMENT_TARGET) + Messages.getString("MainFrame.106"), //$NON-NLS-1$
									JOptionPane.OK_CANCEL_OPTION);
							if (selectedValue == -1 || selectedValue == JOptionPane.CANCEL_OPTION) {
								return;
							}
							String invalidItems = discusserSettingPanel.updateNewValue();
							if(!invalidItems.isEmpty()){
								JOptionPane.showMessageDialog(null, invalidItems + Messages.getString("MainFrame.107")); //$NON-NLS-1$
								return;
							}
							config.setDiscussers("/settings/discussers", "li", discussers); //$NON-NLS-1$ //$NON-NLS-2$
							discussersPanel.updateCompoments();
							annotationGlobalViewPanel.updatePanel();
							if (buttonType == CommentButton.BUTTON_TYPE_DISCUSSER) {
								updateButtonPanel(buttonType);
							}
						}
					});
		}
		return jMenuItemAnnotationDiscussers;
	}

	private JMenuItem getJMenuItemAnnotationAnnotation() {
		if (jMenuItemAnnotationAnnotation == null) {
			jMenuItemAnnotationAnnotation = new JMenuItem(ctm.getColumnName(Comment.F_COMMENT_LABEL));
			jMenuItemAnnotationAnnotation
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {

							AnnotationSettingPanel annotationSettingPanel = new AnnotationSettingPanel(
									commentTypes);
							int selectedValue = JOptionPane.showConfirmDialog(
									null, annotationSettingPanel, ctm.getColumnName(Comment.F_COMMENT_LABEL) + Messages.getString("MainFrame.108"), //$NON-NLS-1$
									JOptionPane.OK_CANCEL_OPTION);
							if (selectedValue == -1 || selectedValue == JOptionPane.CANCEL_OPTION) {
								return;
							}
							String invalidItems = annotationSettingPanel.updateNewValue();
							if(!invalidItems.isEmpty()){
								JOptionPane.showMessageDialog(null, invalidItems + Messages.getString("MainFrame.109")); //$NON-NLS-1$
								return;
							}
							config.setCommentTypes("/settings/comment_types", "li", commentTypes); //$NON-NLS-1$ //$NON-NLS-2$
							discussersPanel.updateCompoments();
							annotationGlobalViewPanel.updatePanel();
							if (buttonType == CommentButton.BUTTON_TYPE_COMMENT) {
								updateButtonPanel(buttonType);
							}
						}
					});
		}
		return jMenuItemAnnotationAnnotation;

	}


	private JMenu getJMenuAnalysis() {
		
		if (jMenuAnalysis == null) {
			jMenuAnalysis = new JMenu();
			jMenuAnalysis.setText(Messages.getString("MainFrame.110")); //$NON-NLS-1$
			jMenuAnalysisTarget = new JMenu(ctm.getColumnName(Comment.F_COMMENT_TARGET));
			jMenuAnalysisLabel = new JMenu(ctm.getColumnName(Comment.F_COMMENT_LABEL));
			jMenuAnalysisEval = new JMenu(StatFrame.LABEL_STYLE_EVAL);
			jMenuAnalysisFreq = new JMenu(StatFrame.LABEL_STYLE_UNIQ);
			jMenuAnalysis.add(jMenuAnalysisTarget);
			jMenuAnalysisTarget.add(getJMenuItemAnalysis(StatFrame.CHART_STYLE_TARGET, DataCounter.SUMMARY_MODE_ALL));
			jMenuAnalysisTarget.add(getJMenuItemAnalysis(StatFrame.CHART_STYLE_TARGET, DataCounter.SUMMARY_MODE_SELF));
			jMenuAnalysisTarget.add(getJMenuItemAnalysis(StatFrame.CHART_STYLE_TARGET, DataCounter.SUMMARY_MODE_ALL_COMPARE));
			jMenuAnalysisTarget.add(getJMenuItemAnalysis(StatFrame.CHART_STYLE_TARGET, DataCounter.SUMMARY_MODE_SELF_COMPARE));
			jMenuAnalysis.add(jMenuAnalysisLabel);
			jMenuAnalysisLabel.add(getJMenuItemAnalysis(StatFrame.CHART_STYLE_LABEL, DataCounter.SUMMARY_MODE_ALL));
			jMenuAnalysisLabel.add(getJMenuItemAnalysis(StatFrame.CHART_STYLE_LABEL, DataCounter.SUMMARY_MODE_SELF));
			jMenuAnalysisLabel.add(getJMenuItemAnalysis(StatFrame.CHART_STYLE_LABEL, DataCounter.SUMMARY_MODE_ALL_COMPARE));
			jMenuAnalysisLabel.add(getJMenuItemAnalysis(StatFrame.CHART_STYLE_LABEL, DataCounter.SUMMARY_MODE_SELF_COMPARE));
			jMenuAnalysis.add(jMenuAnalysisEval);
			jMenuAnalysisEval.add(getJMenuItemAnalysis(StatFrame.CHART_STYLE_EVAL, DataCounter.SUMMARY_MODE_ALL));
			jMenuAnalysisEval.add(getJMenuItemAnalysis(StatFrame.CHART_STYLE_EVAL, DataCounter.SUMMARY_MODE_SELF));
			jMenuAnalysisEval.add(getJMenuItemAnalysis(StatFrame.CHART_STYLE_EVAL, DataCounter.SUMMARY_MODE_ALL_COMPARE));
			jMenuAnalysisEval.add(getJMenuItemAnalysis(StatFrame.CHART_STYLE_EVAL, DataCounter.SUMMARY_MODE_SELF_COMPARE));
			jMenuAnalysis.add(jMenuAnalysisFreq);
			jMenuAnalysisFreq.add(getJMenuItemAnalysis(StatFrame.CHART_STYLE_UNIQ, DataCounter.SUMMARY_MODE_ALL));
			jMenuAnalysisFreq.add(getJMenuItemAnalysis(StatFrame.CHART_STYLE_UNIQ, DataCounter.SUMMARY_MODE_SELF));
			jMenuAnalysisFreq.add(getJMenuItemAnalysis(StatFrame.CHART_STYLE_UNIQ, DataCounter.SUMMARY_MODE_ALL_COMPARE));
//			jMenuAnalysisFreq.add(getJMenuItemAnalysis(StatFrame.CHART_STYLE_UNIQ, DataCounter.SUMMARY_MODE_SELF_COMPARE));
		}
		return jMenuAnalysis;
	}


	private JMenuItem getJMenuItemAnalysis(final int chartStyle, final String mode){
		final String title;
		final int iColumns[];
		
		switch(chartStyle){
		case StatFrame.CHART_STYLE_TARGET:
			title = ctm.getColumnName(Comment.F_COMMENT_TARGET);
			iColumns = new int[]{Comment.F_ANNOTATOR, Comment.F_COMMENT_TARGET};
			break;
		case StatFrame.CHART_STYLE_LABEL:
			title = ctm.getColumnName(Comment.F_COMMENT_LABEL);
			iColumns = new int[]{Comment.F_ANNOTATOR, Comment.F_COMMENT_LABEL};
			break;
		case StatFrame.CHART_STYLE_EVAL:
			title = StatFrame.LABEL_STYLE_EVAL;
			iColumns = new int[]{Comment.F_ANNOTATOR, Comment.F_COMMENT_TARGET, Comment.F_COMMENT_LABEL};
			break;
		case StatFrame.CHART_STYLE_UNIQ:
			title = StatFrame.LABEL_STYLE_UNIQ;
			iColumns = null;
			break;
		default:
			title = Messages.getString("MainFrame.111"); //$NON-NLS-1$
			iColumns = new int[]{};
		}
		
		JMenuItem menuItem = new JMenuItem(mode);
		
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] iSelectedColumns = iColumns;
				if(iColumns == null){ // CHART_STYLE_UNIQ
					iSelectedColumns = commentTable.getSelectedColumns();
					if(iSelectedColumns.length == 0){
						JOptionPane.showMessageDialog(null,  Messages.getString("MainFrame.112")); //$NON-NLS-1$
						return;
					}
				}
				if(ctm.getFilteredCommentList().size() == 0){
					JOptionPane.showMessageDialog(null,  Messages.getString("MainFrame.113")); //$NON-NLS-1$
					return;
				}
				
				String headers[] = new String[iSelectedColumns.length];
				for(int i = 0; i < iSelectedColumns.length; i++){
					headers[i] = ctm.getColumnName(iSelectedColumns[i]);
				}

				DataCounter dc = new DataCounter(ctm.getFilteredCommentList(), iSelectedColumns, commenter.getName());
				ArrayList<Object[]> results = dc.getSummary(mode);
				if(results.size() == 0){
					JOptionPane.showMessageDialog(null,  Messages.getString("MainFrame.114")); //$NON-NLS-1$
					return;
				}
				StatFrame sf = new StatFrame(results, headers);
				if(!sf.showChart(chartStyle)){
					JOptionPane.showMessageDialog(null,  Messages.getString("MainFrame.115")); //$NON-NLS-1$
					return;
				}
				sf.pack();
				sf.setTitle(Messages.getString("MainFrame.116") + title + Messages.getString("MainFrame.117") + mode); //$NON-NLS-1$ //$NON-NLS-2$
				sf.setVisible(true);
			}
		});
		return menuItem;
	}

	
	private JMenu getJMenuOption() {
		if (jMenuOption == null) {
			jMenuOption = new JMenu();
			jMenuOption.setText(Messages.getString("MainFrame.118")); //$NON-NLS-1$
			jMenuOption.add(getJMenuItemOptionTextOverlay());
			jMenuOption.add(getJMenuItemOptionVideoRatio());
//			jMenuOption.add(getJMenuItemOptionInputMediaDevices());
			jMenuOption.add(getJMenuItemOptionMergeMode());
			jMenuOption.add(getJMenuItemOptionSkipTime());
			jMenuOption.add(getJMenuItemOptionJumpAdjustment());
//			jMenuOption.add(getJMenuItemAnnotationTimeCorrection());
			jMenuOption.add(getJMenuItemOptionFocusRange());
			jMenuOption.add(getJMenuItemOptionChangeFontSize());
			jMenuOption.addSeparator();
			jMenuOption.add(getJMenuItemOptionRecorderMode());
			jMenuOption.add(getJMenuItemOptionViewSyncMode());
			jMenuOption.add(getJMenuItemOptionFilterdViewMode());
			jMenuOption.add(getJMenuItemOptionAutoFillAnnotatorName());
			jMenuOption.add(getJMenuItemOptionValidateAnnotations());
			jMenuOption.add(getJMenuItemOptionWaveform());
		}
		return jMenuOption;
	}

	
	private JMenuItem getJMenuItemOptionTextOverlay() {
		String[] textOverlayStyles = soundPlayer.getAvailableTextOverlayStyles();

		if (jMenuItemOptionTextOverlay == null) {
			jMenuItemOptionTextOverlay = new JMenu(Messages.getString("MainFrame.119")); //$NON-NLS-1$
			ButtonGroup itemGroup = new ButtonGroup();
			for (int i = 0; i < textOverlayStyles.length; i++) {
				final int ii = i;
				JMenuItem item = new JRadioButtonMenuItem(textOverlayStyles[i]);
				item.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						soundPlayer.setTextOverlayStyle(ii);
					}
				});
				jMenuItemOptionTextOverlay.add(item);
				itemGroup.add(item);
				if (i == iTextOverlayStyle) {
					item.setSelected(true);
				}
			}
		}
		return jMenuItemOptionTextOverlay;
	}
	
	
	private JMenuItem getJMenuItemOptionVideoRatio() {
		String[] videoAspectRatios = soundPlayer.getAvailableVideoAspectRatio();

		if (jMenuItemOptionVideoRatio == null) {
			jMenuItemOptionVideoRatio = new JMenu(Messages.getString("MainFrame.120")); //$NON-NLS-1$
			ButtonGroup itemGroup = new ButtonGroup();
			for (int i = 0; i < videoAspectRatios.length; i++) {
				String strVideoAspectRatio = videoAspectRatios[i];
				final float videoAspectRatio;
				if(strVideoAspectRatio.equals(SoundPlayer.DEFAULT_VIDEO_ASPECT_RATIO)) {
					videoAspectRatio = 0;
				} else {
					String[] strRatio = strVideoAspectRatio.split(":"); //$NON-NLS-1$
					videoAspectRatio = Float.parseFloat(strRatio[0]) / Float.parseFloat(strRatio[1]);
				}
				final int ii = i;
				JMenuItem item = new JRadioButtonMenuItem(strVideoAspectRatio);
				item.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(timer != null){
							timer.cancel();
						}
						if(videoAspectRatio == 0) { // default ratio of the video file
							soundPlayer.setVideoAspectRatio(soundPlayer.getDefaultVideoAspectRatio());
						} else {
							soundPlayer.setVideoAspectRatio(videoAspectRatio);
						}
						timerStart();
						int playerState = soundPlayer.getPlayerState();
						if(playerState == SoundPlayer.PLAYER_STATE_PAUSE){
							changeStatePause();
						} else if(playerState == SoundPlayer.PLAYER_STATE_PLAY){
							changeStatePlay();
						}
						iVideoAspectRatio = ii;
					}
				});
				strVideoAspectRatio.replaceAll("f", ""); //$NON-NLS-1$ //$NON-NLS-2$
				strVideoAspectRatio.replaceAll("/", ":"); //$NON-NLS-1$ //$NON-NLS-2$
				jMenuItemOptionVideoRatio.add(item);
				itemGroup.add(item);
				if (i == iVideoAspectRatio) {
					item.setSelected(true);
				}
			}
		}
		return jMenuItemOptionVideoRatio;
	}

	
	private JMenuItem getJMenuItemOptionMergeMode() {
		if (jMenuItemOptionMergeMode == null) {
			jMenuItemOptionMergeMode = new JMenu(Messages.getString("MainFrame.121")); //$NON-NLS-1$
			ButtonGroup itemGroup = new ButtonGroup();
			int i = 0;
			for (String mergeMode : mergeModes){
				JMenuItem item = new JRadioButtonMenuItem(mergeMode);
				final int ii = i;
				item.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						iMergeMode = ii;
					}
				});
				jMenuItemOptionMergeMode.add(item);
				itemGroup.add(item);
				if (i == iMergeMode) {
					item.setSelected(true);
				}
				i++;
			}
		}
		return jMenuItemOptionMergeMode;
	}

	
	private JMenuItem getJMenuItemOptionInputMediaDevices() {
		if (jMenuItemOptionInputMediaDevices == null) {
			jMenuItemOptionInputMediaDevices = new JMenu(Messages.getString("MainFrame.122")); //$NON-NLS-1$
			videoDeviceList = soundPlayer.getVideoDeviceList();
			jMenuItemOptionInputVideoMediaDevices = new JMenu(Messages.getString("MainFrame.123")); //$NON-NLS-1$
			ButtonGroup vButtonGroup = new ButtonGroup();
			if(videoDeviceList != null && videoDeviceList.size() > 0){
				int i = 0;
				if(iSelectedVideoDevice == -1){
					iSelectedVideoDevice = 0;
				}
				for(CaptureDevice videoDevice : videoDeviceList){
					JMenuItem item = new JRadioButtonMenuItem(videoDevice.getName());
					final int j = i++;
					item.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							iSelectedVideoDevice = j;
						}
					});
					if(iSelectedVideoDevice == j){
						item.setSelected(true);
					}
					jMenuItemOptionInputVideoMediaDevices.add(item);
					vButtonGroup.add(item);
				}
			}

			audioDeviceList = soundPlayer.getAudioDeviceList();
			jMenuItemOptionInputAudioMediaDevices = new JMenu(Messages.getString("MainFrame.124")); //$NON-NLS-1$
			ButtonGroup aButtonGroup = new ButtonGroup();
			if(audioDeviceList != null && audioDeviceList.size() > 0){
				int i = 0;
				for(CaptureDevice audioDevice : audioDeviceList){
					final int j = i++;
					JMenuItem item = new JRadioButtonMenuItem(audioDevice.getName());
					item.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							iSelectedAudioDevice = j;
						}
					});
					if(iSelectedAudioDevice == j){
						item.setSelected(true);
					}
					jMenuItemOptionInputAudioMediaDevices.add(item);
					aButtonGroup.add(item);
				}
			}

			jMenuItemOptionInputMediaDevices.add(jMenuItemOptionInputVideoMediaDevices);
			jMenuItemOptionInputMediaDevices.add(jMenuItemOptionInputAudioMediaDevices);
		}
		return jMenuItemOptionInputMediaDevices;
	}

		
	private JMenuItem getJMenuItemOptionSkipTime() {
		if (jMenuItemOptionSkipTime == null) {
			jMenuItemOptionSkipTime = new JMenuItem(Messages.getString("MainFrame.125")); //$NON-NLS-1$
			jMenuItemOptionSkipTime
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							String inputValue = JOptionPane.showInputDialog(
									MainFrame.this, Messages.getString("MainFrame.126") + skipTime //$NON-NLS-1$
											+ " (msec)", Messages.getString("MainFrame.127"), //$NON-NLS-1$ //$NON-NLS-2$
									JOptionPane.PLAIN_MESSAGE);
							if (inputValue != null) {
								skipTime = Integer.parseInt(inputValue);
							}
						}
					});
		}
		return jMenuItemOptionSkipTime;
	}

	private JMenuItem getJMenuItemOptionJumpAdjustment() {
		if (jMenuItemOptionJumpAdjustment == null) {
			jMenuItemOptionJumpAdjustment = new JMenuItem(Messages.getString("MainFrame.128")); //$NON-NLS-1$
			jMenuItemOptionJumpAdjustment
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							String inputValue = JOptionPane.showInputDialog(
									MainFrame.this, Messages.getString("MainFrame.129") //$NON-NLS-1$
											+ adjustmentTimeAtJump + " (msec)", //$NON-NLS-1$
									Messages.getString("MainFrame.130"), JOptionPane.PLAIN_MESSAGE); //$NON-NLS-1$
							if (inputValue != null) {
								adjustmentTimeAtJump = Integer
										.parseInt(inputValue);
							}
						}
					});
		}
		return jMenuItemOptionJumpAdjustment;
	}

	
	private JMenuItem getJMenuItemOptionFocusRange() {
		if (jMenuItemOptionFocusRange == null) {
			jMenuItemOptionFocusRange = new JMenuItem(Messages.getString("MainFrame.131")); //$NON-NLS-1$
			jMenuItemOptionFocusRange
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							String inputValue = JOptionPane.showInputDialog(
									MainFrame.this, Messages.getString("MainFrame.132") + (double)focusRange / 1000 //$NON-NLS-1$
											+ " (sec)", Messages.getString("MainFrame.133"), //$NON-NLS-1$ //$NON-NLS-2$
									JOptionPane.PLAIN_MESSAGE);
							if (inputValue != null) {
								focusRange = (int)(Double.parseDouble(inputValue) * 1000);
							}
							annotationGlobalViewPanel.setFocusRange(focusRange);
						}
					});
		}
		return jMenuItemOptionFocusRange;
	}

	
	private JMenuItem getJMenuItemOptionChangeFontSize() {
		if (jMenuItemOptionFontSize == null) {
			jMenuItemOptionFontSize = new JMenuItem(Messages.getString("MainFrame.134")); //$NON-NLS-1$
			jMenuItemOptionFontSize
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							Integer sizeList[] = {10, 11, 12, 13, 14, 15, 16, 17, 18};
							Object inputValue = JOptionPane.showInputDialog(
									MainFrame.this, Messages.getString("MainFrame.135"), Messages.getString("MainFrame.136"), //$NON-NLS-1$ //$NON-NLS-2$
									JOptionPane.PLAIN_MESSAGE, null, sizeList, tableFontSize);
							if (inputValue != null) {
								tableFontSize = (int)inputValue;
							}
							commentTable.setFont(new Font(Font.DIALOG, Font.PLAIN, tableFontSize));
							commentTable.getTableHeader().setFont(new Font(Font.DIALOG, Font.PLAIN, tableFontSize));
						}
					});
		}
		return jMenuItemOptionFontSize;
	}

	
	private JMenuItem getJMenuItemOptionRecorderMode() {
		if (jMenuItemOptionRecorderMode == null) {
			jMenuItemOptionRecorderMode = new JCheckBoxMenuItem(Messages.getString("MainFrame.137")); //$NON-NLS-1$
			jMenuItemOptionRecorderMode.setAccelerator(KeyStroke.getKeyStroke('R',
					KeyEvent.CTRL_MASK, false));
			jMenuItemOptionRecorderMode
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							if(jMenuItemOptionRecorderMode.isSelected()){
								soundRecordButton.setIcon(iconRecordSound);
							} else {
								soundRecordButton.setIcon(iconRecordNoSound);
							}
						}
					});
		}
		return jMenuItemOptionRecorderMode;
	}


	private JMenuItem getJMenuItemOptionViewSyncMode() {
		if (jMenuItemOptionViewSyncMode == null) {
			jMenuItemOptionViewSyncMode = new JCheckBoxMenuItem(Messages.getString("MainFrame.138")); //$NON-NLS-1$
			jMenuItemOptionViewSyncMode.setAccelerator(KeyStroke.getKeyStroke('Y',
					KeyEvent.CTRL_MASK, false));
			jMenuItemOptionViewSyncMode
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									try {
										if (jMenuItemOptionViewSyncMode
												.isSelected()) {
											config.setValue(
													"/settings/isViewSyncMode", //$NON-NLS-1$
													"value", "true"); //$NON-NLS-1$ //$NON-NLS-2$
											listViewSyncCheckBox
													.setSelected(true);
										} else {
											config.setValue(
													"/settings/isViewSyncMode", //$NON-NLS-1$
													"value", "false"); //$NON-NLS-1$ //$NON-NLS-2$
											listViewSyncCheckBox
													.setSelected(false);
										}
									} catch (XPathExpressionException e1) {
										e1.printStackTrace();
									}
								}
							});
						}
					});
		}
		return jMenuItemOptionViewSyncMode;
	}


	private JMenuItem getJMenuItemOptionFilterdViewMode() {
		if (jMenuItemOptionFilteredViewMode == null) {
			jMenuItemOptionFilteredViewMode = new JCheckBoxMenuItem(Messages.getString("MainFrame.139")); //$NON-NLS-1$
			jMenuItemOptionFilteredViewMode.setAccelerator(KeyStroke.getKeyStroke('V',
					KeyEvent.CTRL_MASK, false));
			jMenuItemOptionFilteredViewMode
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								annotationGlobalViewPanel.applyFilter(jMenuItemOptionFilteredViewMode.isSelected());
							}
						});
					}
				});
		}
		return jMenuItemOptionFilteredViewMode;
	}


	private JMenuItem getJMenuItemOptionAutoFillAnnotatorName() {
		if (jMenuItemOptionAutoFillAnnotatorName == null) {
			jMenuItemOptionAutoFillAnnotatorName = new JCheckBoxMenuItem(Messages.getString("MainFrame.155")); //$NON-NLS-1$
			jMenuItemOptionAutoFillAnnotatorName.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							boolean flag = jMenuItemOptionAutoFillAnnotatorName.isSelected();
							commentTable.setAutoFillAnnotatorName(flag);
							try {
								config.setValue("/settings/enableAutoFillAnnotatorName", "value",
										flag ? "true" : "false");
							} catch (XPathExpressionException e) {
								e.printStackTrace();
							}
						}
					});
				}
			});
		}
		return jMenuItemOptionAutoFillAnnotatorName;
	}

	
	private JMenuItem getJMenuItemOptionValidateAnnotations() {
		if (jMenuItemOptionValidateAnnotations == null) {
			jMenuItemOptionValidateAnnotations = new JCheckBoxMenuItem(Messages.getString("MainFrame.167")); //$NON-NLS-1$
			jMenuItemOptionValidateAnnotations.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							boolean flag = jMenuItemOptionValidateAnnotations.isSelected();
							try {
								config.setValue("/settings/enableValidateAnnotations", "value", //$NON-NLS-1$ //$NON-NLS-2$
										flag ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
							} catch (XPathExpressionException e) {
								e.printStackTrace();
							}
						}
					});
				}
			});
		}
		return jMenuItemOptionValidateAnnotations;
	}

	
	private JMenuItem getJMenuItemOptionWaveform() {
		if (jMenuItemOptionWaveform == null) {
			jMenuItemOptionWaveform = new JCheckBoxMenuItem(Messages.getString("MainFrame.140")); //$NON-NLS-1$
			jMenuItemOptionWaveform
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							JOptionPane.showMessageDialog(null,  Messages.getString("MainFrame.141")); //$NON-NLS-1$
						}
					});
		}
		return jMenuItemOptionWaveform;
	}

	
	
	// 仕様が確定してから使用する
	private JMenuItem getJMenuItemAnnotationTimeCorrection() {
		if (jMenuItemAnnotationTimeCorrection == null) {
			jMenuItemAnnotationTimeCorrection = new JMenuItem(Messages.getString("MainFrame.142")); //$NON-NLS-1$
			jMenuItemAnnotationTimeCorrection
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							if(commentList.size() == 0){
								JOptionPane.showMessageDialog(null, Messages.getString("MainFrame.143")); //$NON-NLS-1$
								return;
							}
							TimeCorrectionSettingPanel timeOffsetSettingPanel = new TimeCorrectionSettingPanel(commentList);
							int selectedValue = JOptionPane.showConfirmDialog(
									null, timeOffsetSettingPanel, Messages.getString("MainFrame.144"), //$NON-NLS-1$
									JOptionPane.OK_CANCEL_OPTION);
							 if(selectedValue == JOptionPane.CANCEL_OPTION){
								 return;
							 } else {
								 timeOffsetSettingPanel.applyData();
								 commentTable.revalidate();
								 commentTable.repaint();
								 discussersPanel.updateCompoments();
//								 discussersPanel.repaint();
//								 commentTable.ctm.fireTableDataChanged();
							 }
						}
					});
		}
		return jMenuItemAnnotationTimeCorrection;
	}

	private JMenu getJMenuHelp() {
		if (jMenuHelp == null) {
			jMenuHelp = new JMenu();
			jMenuHelp.setText(Messages.getString("MainFrame.145")); //$NON-NLS-1$
			jMenuHelp.add(getJMenuItemHelpURL());
			jMenuHelp.add(getJMenuItemHelpVersion());
		}
		return jMenuHelp;
	}

	private JMenuItem getJMenuItemHelpURL() {
		if (jMenuItemHelpURL == null) {
			jMenuItemHelpURL = new JMenuItem(Messages.getString("MainFrame.146")); //$NON-NLS-1$
			jMenuItemHelpURL
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							Desktop desktop = Desktop.getDesktop();
							try {
								desktop.browse(new URI(manualURLStr));
							} catch (IOException | URISyntaxException e1) {
								JOptionPane.showMessageDialog(MainFrame.this,
										Messages.getString("MainFrame.147") + e1.getLocalizedMessage(), //$NON-NLS-1$
										Messages.getString("MainFrame.148"), //$NON-NLS-1$
										JOptionPane.INFORMATION_MESSAGE);
								e1.printStackTrace();
							}
							
						}
					});
		}
		return jMenuItemHelpURL;
	}

	private JMenuItem getJMenuItemHelpVersion() {
		if (jMenuItemHelpVersion == null) {
			jMenuItemHelpVersion = new JMenuItem(Messages.getString("MainFrame.149")); //$NON-NLS-1$
			jMenuItemHelpVersion
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							JOptionPane.showMessageDialog(MainFrame.this,
									systemName + " " + VERSION + "\n" //$NON-NLS-1$ //$NON-NLS-2$
											+ System.getProperty("java.vm.name") //$NON-NLS-1$
											 + ", Ver." + System.getProperty("java.version")  + "\n" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
												+ COPYRIGHT,
											Messages.getString("MainFrame.150"), //$NON-NLS-1$
									JOptionPane.INFORMATION_MESSAGE);
						}
					});
		}
		return jMenuItemHelpVersion;
	}

	public void setWindowTitle(String filename) {
		if (filename == null || filename.isEmpty()) {
			filename = Messages.getString("MainFrame.151"); //$NON-NLS-1$
		}

		if(isReadOnlyMode){
			setTitle("[" + filename + "] - " + systemName + Messages.getString("MainFrame.54")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			
		} else {
			setTitle("[" + filename + "] - " + systemName); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	
	
	class DropFileAdapter extends DropTargetAdapter {

		@Override
		public void drop(DropTargetDropEvent dtde) {
			Transferable t = dtde.getTransferable();
			dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

			if(!dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)){
				System.err.println("Warning(MainFrame): not supported data"); //$NON-NLS-1$
				return;
			}
			try {
				@SuppressWarnings("unchecked")
				List<File> files = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);

				if(files.size() > 0){
					File target = files.get(0);
					System.err.println("dropped file: " + target.getCanonicalPath() + ", " + soundPlayer.getPlayerState()); //$NON-NLS-1$ //$NON-NLS-2$

					if(soundPlayer.getPlayerState() == SoundPlayer.PLAYER_STATE_STOP){
						if(target.isDirectory()){
							if(!mergeAnnotationFiles(target.getCanonicalPath())){
								return;
							}
						} else {
							if(!setTargetFile(target.getCanonicalPath())){
								return;
							}
						}
						changeStatePlay();
						soundPlayer.myPlay();
						timerStart();
					}
				}
			} catch (UnsupportedFlavorException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	
	class DrawGraphTask extends TimerTask {
		int time;

		public void run() {
			try {
				if (timeLineTabbedPane.getSelectedIndex() == TAB_STATUS_DETAIL_VIEW) {
					if (isSoundPanelEnable) {
						soundPanel.repaint();
					}
					discussersPanel.repaintComponents();
				} else {
					annotationGlobalViewPanel.repaint();
				}
				soundPlayer.updateVlcInfo();
				if (jMenuItemOptionRecorderMode.isSelected()) {
					time = soundPlayer.getElapsedTime();
				} else {
					time = soundPlayer.getCurrentRecordingTime();
				}
				soundPlayer.setOverlayText(commentTable.getCurrentComment());
				timeCurrent.setTime(time / 1000);
				timeSlider.setValue(time / 1000);
				commentTable.indicateCurrentComment(time, focusRange);
				if (jMenuItemOptionViewSyncMode.isSelected()) {
					commentTable.setViewCenterByTime(time);
				}
			} catch (Exception e) {
				System.err.println("Error(MainFrame): some exception is caused in run() of DrawGraphTask."); //$NON-NLS-1$
			}
		}
	}
	
	
	class DrawAnnotationGlobalViewerTask extends TimerTask {
		public void run() {
			try {
				if(timeLineTabbedPane.getSelectedIndex() == TAB_STATUS_DETAIL_VIEW){
					if(isSoundPanelEnable){
						soundPanel.repaint();
					}
					discussersPanel.repaintComponents();
				} else {
					annotationGlobalViewPanel.repaint();
				}
			} catch (Exception e) {
				System.err.println("Error(MainFrame): some exception is caused in run() of DrawAnnotationGlobalViewerTask."); //$NON-NLS-1$
			}
		}
	}
}
