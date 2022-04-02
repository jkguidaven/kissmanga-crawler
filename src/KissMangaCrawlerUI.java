import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;



@SuppressWarnings("serial")
public class KissMangaCrawlerUI extends JFrame implements ActionListener{
	final static int MAX_TRACE_LINE_POOL = 6;
	private KissMangaConnector kissmanga = null;
	private JList tracelist = null;
	private JList listManga = null;
	private JButton btnSyncMangalist = null;
	
	private DefaultListModel TraceLogmodel = new DefaultListModel();
	private DefaultListModel MangaListmodel = new DefaultListModel();
	private ArrayList<Manga> mangalist = null;
	private int printCtr = 0;
	
	public KissMangaCrawlerUI(){
		initGUI();
		

		 kissmanga = new KissMangaConnector(this);
		 kissmanga.connect();
		 
		start();
	}
	
	public void start(){
		this.setSize(621, 621);
		this.setVisible(true);
	}
	
	public void setToSynchingMangaListSate(boolean synching){

		btnSyncMangalist.setEnabled(!synching);
	}
	
	public void clearMangaList() {
		MangaListmodel.clear();
	}
	
	public void populateMangaList(){
		for(Manga manga : mangalist){
			MangaListmodel.addElement(manga.getName());
		}
	}
	
	public void Trace(String log){
		
		if(printCtr <= MAX_TRACE_LINE_POOL)
			printCtr++;
		else
			TraceLogmodel.remove(0);

		

		TraceLogmodel.addElement(log);
		
	}
	
	public void initGUI() {
		setTitle("KissManga Crawler");
		SpringLayout springLayout = new SpringLayout();
		getContentPane().setLayout(springLayout);
		
		btnSyncMangalist = new JButton("Sync Mangalist");
		springLayout.putConstraint(SpringLayout.NORTH, btnSyncMangalist, 10, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, btnSyncMangalist, 5, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, btnSyncMangalist, 250, SpringLayout.WEST, getContentPane());
		getContentPane().add(btnSyncMangalist);
		btnSyncMangalist.addActionListener(this);
		
		listManga = new JList(MangaListmodel);
		JScrollPane listMangaScroller = new JScrollPane(listManga);
		springLayout.putConstraint(SpringLayout.NORTH, listMangaScroller, 6, SpringLayout.SOUTH, btnSyncMangalist);
		springLayout.putConstraint(SpringLayout.WEST, listMangaScroller, 0, SpringLayout.WEST, btnSyncMangalist);
		springLayout.putConstraint(SpringLayout.SOUTH, listMangaScroller, -5, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, listMangaScroller, 0, SpringLayout.EAST, btnSyncMangalist);
		getContentPane().add(listMangaScroller);
		
		JPanel panel = new JPanel();
		springLayout.putConstraint(SpringLayout.NORTH, panel, -150, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, panel, 5, SpringLayout.EAST, listMangaScroller);
		springLayout.putConstraint(SpringLayout.SOUTH, panel, 0, SpringLayout.SOUTH, listMangaScroller);
		springLayout.putConstraint(SpringLayout.EAST, panel, -5, SpringLayout.EAST, getContentPane());
		panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		getContentPane().add(panel);
		
		JPanel panel_1 = new JPanel();
		springLayout.putConstraint(SpringLayout.NORTH, panel_1, 10, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, panel_1, -5, SpringLayout.EAST, getContentPane());
		panel_1.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		springLayout.putConstraint(SpringLayout.WEST, panel_1, 6, SpringLayout.EAST, btnSyncMangalist);
		springLayout.putConstraint(SpringLayout.SOUTH, panel_1, -5, SpringLayout.NORTH, panel);
		SpringLayout sl_panel = new SpringLayout();
		panel.setLayout(sl_panel);
		
		tracelist = new JList(TraceLogmodel);
		sl_panel.putConstraint(SpringLayout.NORTH, tracelist, 5, SpringLayout.NORTH, panel);
		sl_panel.putConstraint(SpringLayout.WEST, tracelist, 5, SpringLayout.WEST, panel);
		sl_panel.putConstraint(SpringLayout.SOUTH, tracelist, -5, SpringLayout.SOUTH, panel);
		sl_panel.putConstraint(SpringLayout.EAST, tracelist, -5, SpringLayout.EAST, panel);
		tracelist.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel.add(tracelist);
		getContentPane().add(panel_1);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if(e.getSource().equals(btnSyncMangalist)){
			new Thread(new Runnable(){
				
				public void run(){
					kissmanga.syncMangaList();
					mangalist = kissmanga.getMangaList();
					populateMangaList();
				}
			}).start();
		}
	}
}
