package com.nuix.investigatorreport;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("serial")
public class IncludedFileTableModel extends AbstractTableModel {

    private ArrayList<IncludedFile> files = new ArrayList<IncludedFile>();

    private String[] columnNames = {"Title", "File"};

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public int getRowCount() {
        return files.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        IncludedFile file = files.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return file.getTitle();
            default:
                return file.getFilePath();
        }
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return true;
    }

    public void addFile(IncludedFile info) {
        files.add(info);
        fireTableDataChanged();
    }

    public void removeFileAt(int index) {
        files.remove(index);
        fireTableDataChanged();
    }

    public void clear() {
        files.clear();
        fireTableDataChanged();
    }

    public ArrayList<IncludedFile> getFiles() {
        return files;
    }

    public void setFiles(ArrayList<IncludedFile> files) {
        this.files = files;
        fireTableDataChanged();
    }

    public int[] shiftFiles(int[] positions, int offset) {
        List<IncludedFile> list = new ArrayList<IncludedFile>();
        for (int index : positions) {
            list.add(files.get(index));
        }
        return shiftFiles(list, offset);
    }

    public int[] shiftFiles(List<IncludedFile> list, int offset) {
        if (offset > 0) {
            Collections.reverse(list);
        }
        for (IncludedFile info : list) {
            int previousIndex = files.indexOf(info);
            int newIndex = previousIndex + offset;
            if (newIndex < 0) {
                newIndex = files.size() - 1;
            } else if (newIndex > files.size() - 1) {
                newIndex = 0;
            }
            files.remove(info);
            files.add(newIndex, info);
        }
        fireTableDataChanged();
        int[] newIndices = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            newIndices[i] = files.indexOf(list.get(i));
        }
        return newIndices;
    }
}
