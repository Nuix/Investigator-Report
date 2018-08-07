package com.nuix.investigatorreport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public class SummaryInfoTableModel extends AbstractTableModel {

	private ArrayList<SummaryInfo> summaries = new ArrayList<SummaryInfo>();

	private String[] columnNames = {"Title","Tag","Profile","Item Sort","Export No Products"};
	
	@Override
	public String getColumnName(int column){
		return columnNames[column];
	}
	
	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public int getRowCount() {
		return summaries.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		SummaryInfo info = summaries.get(rowIndex);
		switch(columnIndex){
			case 0:
				return info.getTitle();
			case 1:
				return info.getTag();
			case 2:
				return info.getProfile();
			case 3:
				return info.getSort();
			case 4:
				return info.getProductExportDisabled();
		}
		return null;
	}

	@Override
	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		SummaryInfo info = summaries.get(rowIndex);
		switch(columnIndex){
			case 0:
				info.setTitle((String) value);
				break;
			case 1:
				info.setTag((String) value);
				break;
			case 2:
				info.setProfile((String) value);
				break;
			case 3:
				info.setSort((String)value);
				break;
			case 4:
				info.setProductExportDisabled((boolean)value);
				break;
		}
	}
		
	@Override
	public boolean isCellEditable(int row, int column){
		return true;
	}
	
	@Override
	public Class<?> getColumnClass(int col) {
		if(col == 4){
			return Boolean.class;
		}
		else{
			return super.getColumnClass(col);
		}
	}
	
	//Internal refresh call
	void refresh(){
		fireTableDataChanged();
	}
	
	public void addSummary(SummaryInfo info){
		summaries.add(info);
		fireTableDataChanged();
	}
	
	public void removeSummaryAt(int index){
		summaries.remove(index);
		fireTableDataChanged();
	}
	
	public void clear(){
		summaries.clear();
		fireTableDataChanged();
	}
	
	public ArrayList<SummaryInfo> getSummaries() {
		return summaries;
	}

	public void setSummaries(ArrayList<SummaryInfo> summaries) {
		this.summaries = summaries;
		fireTableDataChanged();
	}
	
	public int[] shiftSummaries(int[] positions, int offset){
		List<SummaryInfo> list = new ArrayList<SummaryInfo>();
		for(int index : positions){
			list.add(summaries.get(index));
		}
		return shiftSummaries(list,offset);
	}
	
	public int[] shiftSummaries(List<SummaryInfo> list, int offset){
		if(offset > 0){
			Collections.reverse(list);
		}
		for(SummaryInfo info : list){
			int previousIndex = summaries.indexOf(info);
			int newIndex = previousIndex + offset;
			if(newIndex < 0){
				newIndex = summaries.size() - 1;
			} else if (newIndex > summaries.size() - 1){
				newIndex = 0;
			}
			summaries.remove(info);
			summaries.add(newIndex,info);
		}
		fireTableDataChanged();
		int[] newIndices = new int[list.size()];
		for(int i=0;i<list.size();i++){
			newIndices[i] = summaries.indexOf(list.get(i));
		}
		return newIndices;
	}
}
