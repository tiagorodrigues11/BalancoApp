package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import enm.Meses;
import helpers.Registo;

public class GeneralTab extends JPanel {
	private static final long serialVersionUID = -3234212681795902081L;
	private static final int GENERAL_TEXTFIELD_SIZE = 8;
	
	private enum RegistoType { RECEITA, DESPESA };
	private Integer[] anos = new Integer[]{2015,2016};
	
	private JTextField receitasTextField;
	private JTextField despesasTextField;
	private JTextField disponivelTextField;
	private JTextArea resultTextArea;
	private JComboBox<Meses> comboBoxMeses;
	private JComboBox<Integer> comboBoxAno;
	private JComboBox<String> removeComboBox;
	
	private int month;
	private int year;
	
	private Meses currentMes;
	private Integer currentAno;
	
	private HashMap<Integer, HashMap<Meses, ArrayList<Registo>>> receitasAnuaisMap;
	private HashMap<Integer, HashMap<Meses, ArrayList<Registo>>> despesasAnuaisMap;
	
	public GeneralTab(int month, int year) {
		setLayout(new BorderLayout());
		this.month = month;
		this.year = year;
		
		currentMes = Meses.values()[month];
		currentAno = year;
		
		try {
			readMapsFromFile();
		} catch (ClassNotFoundException | IOException e) {
			receitasAnuaisMap = new HashMap<Integer, HashMap<Meses, ArrayList<Registo>>>();
			despesasAnuaisMap = new HashMap<Integer, HashMap<Meses, ArrayList<Registo>>>();
			
			for (Integer ano : anos) {
				receitasAnuaisMap.put(ano, new HashMap<Meses, ArrayList<Registo>>());
				despesasAnuaisMap.put(ano, new HashMap<Meses, ArrayList<Registo>>());
			}
		}
		
		add(createNorthPanel(),BorderLayout.NORTH);
		add(createWestPanel(),BorderLayout.WEST);
		add(createCentralPanel());
		add(createSouthPanel(), BorderLayout.SOUTH );
		
		updateRegistosMensais();
		updateTotalAmount();
		updateRemoveComboBox();
	}

	private void readMapsFromFile() throws FileNotFoundException, IOException, ClassNotFoundException {
		ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream("aplication_data.txt"));
		
		receitasAnuaisMap = (HashMap<Integer, HashMap<Meses, ArrayList<Registo>>>) objectInputStream.readObject();
		despesasAnuaisMap = (HashMap<Integer, HashMap<Meses, ArrayList<Registo>>>) objectInputStream.readObject();
		
		objectInputStream.close();
	}

	private Component createNorthPanel() {
		JPanel northPanel = new JPanel(new GridLayout(1, 2));
		
		JPanel mesesPanel = new JPanel(new FlowLayout());
		JLabel mesesLabel = new JLabel("Meses:");
		comboBoxMeses = new JComboBox<>(Meses.values());
		comboBoxMeses.setSelectedIndex(month);
		
		comboBoxMeses.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					currentMes = (Meses) comboBoxMeses.getSelectedItem();
					updateRegistosMensais();
					updateRemoveComboBox();
				}
			}
		});
		
		mesesPanel.add(mesesLabel);
		mesesPanel.add(comboBoxMeses);
		
		JPanel anosPanel = new JPanel(new FlowLayout());
		JLabel anosLabel = new JLabel("Anos:");
		comboBoxAno = new JComboBox<>(anos);
		comboBoxAno.setSelectedItem(year);
		
		comboBoxAno.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED){
					currentAno = (Integer) comboBoxAno.getSelectedItem();
					updateRegistosMensais();
					updateRemoveComboBox();
				}
			}
		});
		
		anosPanel.add(anosLabel);
		anosPanel.add(comboBoxAno);
		
		northPanel.add(mesesPanel);
		northPanel.add(anosPanel);
		
		return northPanel;
	}

	private Component createWestPanel() {
		JPanel westPanel = new JPanel(new GridLayout(2, 1));
		
		JPanel receitasWrapper = new JPanel();
		receitasWrapper.setBorder(BorderFactory.createTitledBorder("Receitas"));
		receitasWrapper.setLayout(new GridLayout(2, 1));
		
		JPanel receitasNomePanel = new JPanel(new GridLayout(2, 1));
		JLabel receitasNome = new JLabel("Descrição:");
		JTextField receitasNomeTextField = new JTextField(10);
		receitasNomeTextField.setHorizontalAlignment(JTextField.CENTER);
		receitasNomePanel.add(receitasNome);
		receitasNomePanel.add(receitasNomeTextField);
		
		JPanel receitasValorPanel = new JPanel(new GridLayout(3, 1));
		JLabel receitasValor = new JLabel("Valor (€):");
		JTextField receitasValorTextField = new JTextField(5);
		receitasValorTextField.setHorizontalAlignment(JTextField.CENTER);
		JButton receitasButton = new JButton("Confirmar Receita");
		receitasValorPanel.add(receitasValor);
		receitasValorPanel.add(receitasValorTextField);
		receitasValorPanel.add(receitasButton);
		
		receitasButton.addActionListener(new RegistoActionListener(receitasNomeTextField, receitasValorTextField, RegistoType.RECEITA));
		
		receitasWrapper.add(receitasNomePanel);
		receitasWrapper.add(receitasValorPanel);
		
		JPanel despesasWrapper = new JPanel();
		despesasWrapper.setBorder(BorderFactory.createTitledBorder("Despesas"));
		despesasWrapper.setLayout(new GridLayout(2, 1));
		
		JPanel despesasNomePanel = new JPanel(new GridLayout(2, 1));
		JLabel despesasNome = new JLabel("Descrição:");
		JTextField despesasNomeTextField = new JTextField(10);
		despesasNomeTextField.setHorizontalAlignment(JTextField.CENTER);
		despesasNomePanel.add(despesasNome);
		despesasNomePanel.add(despesasNomeTextField);
		
		
		JPanel despesasValorPanel = new JPanel(new GridLayout(3, 1));
		JLabel despesasValor = new JLabel("Valor (€):");
		JTextField despesasValorTextField = new JTextField(5);
		despesasValorTextField.setHorizontalAlignment(JTextField.CENTER);
		JButton despesasButton = new JButton("Confirmar Despesa");
		despesasValorPanel.add(despesasValor);
		despesasValorPanel.add(despesasValorTextField);
		despesasValorPanel.add(despesasButton);
		
		despesasButton.addActionListener(new RegistoActionListener(despesasNomeTextField, despesasValorTextField, RegistoType.DESPESA));
		
		despesasWrapper.add(despesasNomePanel);
		despesasWrapper.add(despesasValorPanel);
		
		westPanel.add(receitasWrapper);
		westPanel.add(despesasWrapper);

		return westPanel;
	}
	
	private Component createCentralPanel() {
		JPanel centralPanel = new JPanel(new BorderLayout());
		
		resultTextArea = new JTextArea();
		resultTextArea.setEditable(false);
		JScrollPane scroll = new JScrollPane(resultTextArea);
		centralPanel.add(scroll);
		
		JPanel removePanel = new JPanel(new GridLayout(1, 3));
		JLabel removeLabel = new JLabel("Remove: ");
		removeComboBox = new JComboBox<String>();
		
		updateRemoveComboBox();
		
		JButton removeButton = new JButton("Remove");
		removeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String toRemove = (String) removeComboBox.getSelectedItem();
				HashMap<Meses, ArrayList<Registo>> receitasMensaisMap = receitasAnuaisMap.get(currentAno);
				Iterator<Registo> itr = receitasMensaisMap.get(currentMes).iterator();
				boolean removed = remove(toRemove, itr);
				
				if(!removed){
					HashMap<Meses, ArrayList<Registo>> despesasMensaisMap = despesasAnuaisMap.get(currentAno);
					Iterator<Registo> itr2 = despesasMensaisMap.get(currentMes).iterator();
					remove(toRemove, itr2);
				}
				
				updateRegistosMensais();
				updateTotalAmount();
				updateRemoveComboBox();
			}

			private boolean remove(String toRemove, Iterator<Registo> itr) {
				while(itr.hasNext()) {
					Registo element = itr.next();
					if(element.getDescricao().equals(toRemove)){
						itr.remove();
						return true;
					}
				}
				return false;
			}
		});
		
		removePanel.add(removeLabel);
		removePanel.add(removeComboBox);
		removePanel.add(removeButton);
		
		centralPanel.add(removePanel, BorderLayout.SOUTH);
		
		return centralPanel;
	}
	
	public JPanel createSouthPanel(){
		JPanel southPanel = new JPanel(new GridLayout(1, 3));
		southPanel.setBorder(BorderFactory.createTitledBorder("Totais: "));
		
		JPanel receitasPanel = new JPanel(new FlowLayout());
		JLabel receitasLabel = new JLabel("Receitas:");
		receitasTextField = new JTextField(GENERAL_TEXTFIELD_SIZE);
		receitasTextField.setEditable(false);
		receitasTextField.setForeground(Color.GREEN.darker());
		receitasTextField.setHorizontalAlignment(JTextField.CENTER);
		receitasPanel.add(receitasLabel);
		receitasPanel.add(receitasTextField);
		
		
		JPanel despesasPanel = new JPanel(new FlowLayout());
		JLabel despesasLabel = new JLabel("Despesas:");
		despesasTextField = new JTextField(GENERAL_TEXTFIELD_SIZE);
		despesasTextField.setEditable(false);
		despesasTextField.setForeground(Color.RED.darker());
		despesasTextField.setHorizontalAlignment(JTextField.CENTER);
		despesasPanel.add(despesasLabel);
		despesasPanel.add(despesasTextField);
		
		JPanel disponivelPanel = new JPanel(new FlowLayout());
		JLabel disponivelLabel = new JLabel("Disponivel:");
		disponivelTextField = new JTextField(GENERAL_TEXTFIELD_SIZE);
		disponivelTextField.setEditable(false);
		disponivelTextField.setHorizontalAlignment(JTextField.CENTER);
		disponivelPanel.add(disponivelLabel);
		disponivelPanel.add(disponivelTextField);
		
		southPanel.add(receitasPanel);
		southPanel.add(despesasPanel);
		southPanel.add(disponivelPanel);
		
		return southPanel;
	}

	private void updateRegistosMensais(){
		String result = "Receitas: \n";
		
		HashMap<Meses, ArrayList<Registo>> receitasMensaisMap = receitasAnuaisMap.get(currentAno);
		if(receitasMensaisMap.containsKey(currentMes)){
			for (Registo r : receitasMensaisMap.get(currentMes)) 
				result += "     > " + r.getDescricao() + " ........ " + NumberFormat.getCurrencyInstance(new Locale("pt", "PT")).format(r.getValor()) + "\n";
		}
		
		result += "\nDespesas: \n";
		HashMap<Meses, ArrayList<Registo>> despesasMensaisMap = despesasAnuaisMap.get(currentAno);
		if(despesasMensaisMap.containsKey(currentMes)){
			for (Registo d : despesasMensaisMap.get(currentMes))
				result += "     > " + d.getDescricao() + " ........ " + NumberFormat.getCurrencyInstance(new Locale("pt", "PT")).format(d.getValor()) + "\n"; 
		}
		
		result += "\nTotal Mensal: " + NumberFormat.getCurrencyInstance(new Locale("pt", "PT")).format(getMonthlyAmount());
		
		resultTextArea.setText(result);
	}
	
	private void updateTotalAmount(){
		int receitasVal = 0;
		int despesasVal = 0;
		
		for (Integer ano : receitasAnuaisMap.keySet()) {
			
			HashMap<Meses, ArrayList<Registo>> receitasMensaisMap = receitasAnuaisMap.get(ano);
			for (Meses mes : receitasMensaisMap.keySet()) {
				if(receitasMensaisMap.containsKey(mes)){
					for (Registo r : receitasMensaisMap.get(mes)) 
						receitasVal += r.getValor();
				}
			}
			
			
			HashMap<Meses, ArrayList<Registo>> despesasMensaisMap = despesasAnuaisMap.get(ano);
			for (Meses mes : receitasMensaisMap.keySet()) {
				if(despesasMensaisMap.containsKey(mes)){
					for (Registo d : despesasMensaisMap.get(mes)) 
						despesasVal += d.getValor();
				}
			}
			
		}
		receitasTextField.setText(NumberFormat.getCurrencyInstance(new Locale("pt", "PT")).format(receitasVal));
		despesasTextField.setText(NumberFormat.getCurrencyInstance(new Locale("pt", "PT")).format(despesasVal));
		
		int total = receitasVal - despesasVal; 
		disponivelTextField.setText(NumberFormat.getCurrencyInstance(new Locale("pt", "PT")).format(total));
	}
	
	private void updateRemoveComboBox(){
		removeComboBox.removeAllItems();
		
		HashMap<Meses, ArrayList<Registo>> receitasMensaisMap = receitasAnuaisMap.get(currentAno);
		if(receitasMensaisMap.containsKey(currentMes)){
			for (Registo r : receitasMensaisMap.get(currentMes)) 
				removeComboBox.addItem(r.getDescricao());
		}
		
		HashMap<Meses, ArrayList<Registo>> despesasMensaisMap = despesasAnuaisMap.get(currentAno);
		if(despesasMensaisMap.containsKey(currentMes)){
			for (Registo d : despesasMensaisMap.get(currentMes))
				removeComboBox.addItem(d.getDescricao()); 
		}
	}
	
	private int getMonthlyAmount() {
		int receitasVal = 0;
		int despesasVal = 0;
		
		HashMap<Meses, ArrayList<Registo>> receitasMensaisMap = receitasAnuaisMap.get(currentAno);
		if(receitasMensaisMap.containsKey(currentMes)){
			for (Registo r : receitasMensaisMap.get(currentMes)) 
				receitasVal += r.getValor();
		}
		
		HashMap<Meses, ArrayList<Registo>> despesasMensaisMap = despesasAnuaisMap.get(currentAno);
		if(despesasMensaisMap.containsKey(currentMes)){
			for (Registo d : despesasMensaisMap.get(currentMes)) 
				despesasVal += d.getValor();
		}
			
		return receitasVal - despesasVal;
	}
	
	public HashMap<Integer, HashMap<Meses, ArrayList<Registo>>> getReceitasAnuaisMap() {
		return receitasAnuaisMap;
	}
	
	public HashMap<Integer, HashMap<Meses, ArrayList<Registo>>> getDespesasAnuaisMap() {
		return despesasAnuaisMap;
	}

	public class RegistoActionListener implements ActionListener {
		
		private RegistoType type;
		private JTextField nomeTextField;
		private JTextField valorTextField;

		public RegistoActionListener(JTextField nomeTextField, JTextField valorTextField, RegistoType type) {
			this.nomeTextField = nomeTextField;
			this.valorTextField = valorTextField;
			this.type = type;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			String desc = nomeTextField.getText();
			if (!desc.isEmpty() && desc.matches(".*\\w.*")){
				String valString = valorTextField.getText();
				if (!valString.isEmpty() && valString.matches(".*\\w.*")){
					try {
						Integer val = Integer.valueOf(valString);
						Registo registo = new Registo(desc, val);
						addRegisto(registo);
					} catch (Exception e2) {
						JOptionPane.showMessageDialog(GeneralTab.this, "Campo Valor tem de ser um número.");
					}
				}else
					JOptionPane.showMessageDialog(GeneralTab.this, "Campo Valor não preenchido.");
			}else
				JOptionPane.showMessageDialog(GeneralTab.this, "Campo Descrição não preenchido.");
			
			updateRegistosMensais();
			updateTotalAmount();
			updateRemoveComboBox();
			
			nomeTextField.setText("");
			valorTextField.setText("");
		}
		
		public void addRegisto(Registo registo){
			switch (type) {
			case RECEITA:
				HashMap<Meses, ArrayList<Registo>> receitasMensaisMap = receitasAnuaisMap.get(currentAno);
				add(registo, receitasMensaisMap);
				break;
			case DESPESA:
				HashMap<Meses, ArrayList<Registo>> despesasMensaisMap = despesasAnuaisMap.get(currentAno);
				add(registo, despesasMensaisMap);
				break;
			default:
				System.out.println("Tipo ( " + type + " ) desconhecido!");
				break;
			}
		}

		private void add(Registo registo, HashMap<Meses, ArrayList<Registo>> map) {
			ArrayList<Registo> list;
			
			if(map.containsKey(currentMes))
				list = map.get(currentMes);
			else
				list = new ArrayList<>();
			
			list.add(registo);
			map.put(currentMes, list);
		}
		
	}
	
}
