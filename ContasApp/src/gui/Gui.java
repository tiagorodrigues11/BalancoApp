package gui;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Calendar;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

public class Gui extends JFrame {

	private static final long serialVersionUID = -7886147914342084854L;

	private GeneralTab generalTab;
	private FoodTab foodTab;
	
	private JTabbedPane tabbedPane;
	
	public Gui() {
		super("Contas App");
		
		Calendar cal = Calendar.getInstance();
		int month = cal.get(Calendar.MONTH);
		int year = cal.get(Calendar.YEAR);
		
		tabbedPane = new JTabbedPane();
		generalTab = new GeneralTab(month, year);
		tabbedPane.addTab("Geral", generalTab);
		foodTab = new FoodTab(month, year);
		tabbedPane.addTab("Alimentação", foodTab);
		
		addShutdownHooks();
		
		add(tabbedPane);
		
		pack();
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}
	
	private void addShutdownHooks() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("aplication_data.txt"));
					
					oos.writeObject(generalTab.getReceitasAnuaisMap());
					oos.writeObject(generalTab.getDespesasAnuaisMap());
					
					oos.flush();
					oos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
		});
	}
	
}
