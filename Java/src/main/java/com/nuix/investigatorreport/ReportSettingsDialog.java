package com.nuix.investigatorreport;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.List;
import java.util.*;

@SuppressWarnings("serial")
public class ReportSettingsDialog extends JDialog {

    private static Logger logger = Logger.getLogger(ReportSettingsDialog.class);

    private static String[] availableItemSorts = new String[]{
            "Item Position",
            "Item Date Ascending",
            "Item Date Descending",
            "Top Level Item Date Ascending",
            "Top Level Item Date Descending",
            "Profile First Column Value",
            "Item Name",
            "Item Path",
            "Item Kind",
            "Mime Type",
            "Recipient Count Ascending",
            "Recipient Count Descending",
            "Audited Size Ascending",
            "Audited Size Descending",
            "MD5",
            "GUID",
            "None",
    };

    private static int defaultRecordsPerPage = 2000;

    public static int getDefaultRecordsPerPage() {
        return defaultRecordsPerPage;
    }

    public static void setDefaultRecordsPerPage(int defaultRecordsPerPage) {
        ReportSettingsDialog.defaultRecordsPerPage = defaultRecordsPerPage;
    }

    private static String defaultSummaryProfileName = "Default metadata profile";

    public static void setDefaultSummaryProfileName(String defaultProfileName) {
        defaultSummaryProfileName = defaultProfileName;
    }

    private static String defaultSummarySort = "Item Position";

    public static void setDefaultSummarySort(String defaultSort) {
        boolean isValid = false;
        for (int i = 0; i < availableItemSorts.length; i++) {
            if (availableItemSorts[i].contentEquals(defaultSort)) {
                isValid = true;
                break;
            }
        }

        if (isValid) {
            defaultSummarySort = defaultSort;
        } else {
            logger.warn(String.format("Provided default item sort '%s' is not valid, falling back to using 'Item Position'", defaultSort));
            logger.warn("Valid item sort values are: " + String.join(", ", availableItemSorts));
        }
    }

    private final JPanel contentPanel = new JPanel();
    private JTextField txtOutputdirectory;
    private boolean dialogResult = false;

    private List<String> availableTags = null;
    private List<String> availableProfiles = null;
    private JTable tableSummaries;
    private SummaryInfoTableModel tableSummariesModel = new SummaryInfoTableModel();
    private IncludedFileTableModel tableFilesModel = new IncludedFileTableModel();
    private JCheckBox chckbxExportNatives;
    private JCheckBox chckbxExportText;
    private JCheckBox chckbxExportPdfs;
    private JCheckBox chckbxExportThumbnails;
    private JCheckBox chckbxIncludeTags;
    private JCheckBox chckbxIncludeText;
    private JCheckBox chckbxIncludeCustomMetadata;
    private JCheckBox chckbxIncludeProperties;
    private JComboBox<String> comboBoxDetailsProfile;
    private JCheckBox chckbxIncludeProfile;
    private JCheckBox chckbxRecordSettings;
    private JTextField txtWorkerTemp;
    private JSpinner spinnerWorkerCount;
    private JSpinner spinnerWorkerMemory;
    private JCheckBox chckbxIncludeComments;
    private JTextField txtCvFilePath;
    private JTextField txtDefinitionsFilePath;
    private JCheckBox chckbxIncludeCvFile;
    private JButton btnPickCvFile;
    private JCheckBox chckbxIncludeDefinitionsFile;
    private JButton btnPickDefinitionsFile;
    private JButton btnMoveUp;
    private JButton btnMoveDown;
    private JScrollPane scrollPane_1;
    private JTable tableFiles;
    private JPanel panel_1;
    private JButton btnAddAdditionalFile;
    private JButton btnMoveUp_1;
    private JButton btnMoveDown_1;
    private Component horizontalStrut;
    private JButton btnRemoveSelected;
    private JButton btnRemoveAll;
    private JButton btnSetAllProfiles;
    private JPanel pdfOptionsPanel;
    private JCheckBox chckbxImageSpreadsheets;
    private JCheckBox chckbxRegenerateStored;
    private JCheckBox chckbxReportExcludedItems;
    private JSpinner spinnerRecordsPerPage;
    private JLabel lblRecordsPerPage;
    private JButton btnSetAllSorts;

    public ReportSettingsDialog() {
        this(new ArrayList<String>(), new ArrayList<String>(), "Report ");
    }

    public ReportSettingsDialog(List<String> tags, List<String> profiles, String defaultReportTitlePrefix) {
        availableTags = tags;
        availableProfiles = profiles;
        setTitle("Investigator Report");
        setSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setModal(true);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new BorderLayout(0, 0));
        {
            JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
            contentPanel.add(tabbedPane, BorderLayout.CENTER);
            {
                JPanel panelGeneralSettings = new JPanel();
                tabbedPane.addTab("General Settings", null, panelGeneralSettings, null);
                GridBagLayout gbl_panelGeneralSettings = new GridBagLayout();
                gbl_panelGeneralSettings.columnWidths = new int[]{0, 0, 0, 0};
                gbl_panelGeneralSettings.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
                gbl_panelGeneralSettings.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
                gbl_panelGeneralSettings.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
                panelGeneralSettings.setLayout(gbl_panelGeneralSettings);
                {
                    JButton btnOutputDirectory = new JButton("Output Directory");
                    btnOutputDirectory.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent arg0) {
                            JFileChooser fc = new JFileChooser();
                            fc.setDialogTitle("Select Output Directory");
                            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                            fc.setCurrentDirectory(new File(txtOutputdirectory.getText()));
                            if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                                txtOutputdirectory.setText(fc.getSelectedFile().getAbsolutePath());
                            }
                        }
                    });
                    GridBagConstraints gbc_btnOutputDirectory = new GridBagConstraints();
                    gbc_btnOutputDirectory.insets = new Insets(0, 0, 5, 5);
                    gbc_btnOutputDirectory.gridx = 0;
                    gbc_btnOutputDirectory.gridy = 0;
                    panelGeneralSettings.add(btnOutputDirectory, gbc_btnOutputDirectory);
                }
                {
                    txtOutputdirectory = new JTextField();
                    GridBagConstraints gbc_txtOutputdirectory = new GridBagConstraints();
                    gbc_txtOutputdirectory.gridwidth = 2;
                    gbc_txtOutputdirectory.insets = new Insets(0, 0, 5, 0);
                    gbc_txtOutputdirectory.fill = GridBagConstraints.HORIZONTAL;
                    gbc_txtOutputdirectory.gridx = 1;
                    gbc_txtOutputdirectory.gridy = 0;
                    panelGeneralSettings.add(txtOutputdirectory, gbc_txtOutputdirectory);
                    txtOutputdirectory.setColumns(10);
                }
                {
                    chckbxRecordSettings = new JCheckBox("Record Settings");
                    GridBagConstraints gbc_chckbxRecordSettings = new GridBagConstraints();
                    gbc_chckbxRecordSettings.gridwidth = 2;
                    gbc_chckbxRecordSettings.insets = new Insets(0, 0, 5, 0);
                    gbc_chckbxRecordSettings.anchor = GridBagConstraints.WEST;
                    gbc_chckbxRecordSettings.gridx = 1;
                    gbc_chckbxRecordSettings.gridy = 1;
                    panelGeneralSettings.add(chckbxRecordSettings, gbc_chckbxRecordSettings);
                    chckbxRecordSettings.setVisible(false); //HIDING THIS FOR NOW
                }
                {
                    chckbxIncludeCvFile = new JCheckBox("Include CV File");
                    chckbxIncludeCvFile.addItemListener(new ItemListener() {
                        public void itemStateChanged(ItemEvent arg0) {
                            btnPickCvFile.setEnabled(chckbxIncludeCvFile.isSelected());
                            txtCvFilePath.setEnabled(chckbxIncludeCvFile.isSelected());
                        }
                    });
                    GridBagConstraints gbc_chckbxIncludeCvFile = new GridBagConstraints();
                    gbc_chckbxIncludeCvFile.anchor = GridBagConstraints.WEST;
                    gbc_chckbxIncludeCvFile.insets = new Insets(0, 0, 5, 5);
                    gbc_chckbxIncludeCvFile.gridx = 0;
                    gbc_chckbxIncludeCvFile.gridy = 3;
                    panelGeneralSettings.add(chckbxIncludeCvFile, gbc_chckbxIncludeCvFile);
                }
                {
                    btnPickCvFile = new JButton("Choose");
                    btnPickCvFile.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent arg0) {
                            JFileChooser chooser = new JFileChooser();
                            chooser.setDialogTitle("Choose CV File");
                            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                                txtCvFilePath.setText(chooser.getSelectedFile().getAbsolutePath());
                            }
                        }
                    });
                    btnPickCvFile.setEnabled(false);
                    GridBagConstraints gbc_btnPickCvFile = new GridBagConstraints();
                    gbc_btnPickCvFile.insets = new Insets(0, 0, 5, 5);
                    gbc_btnPickCvFile.gridx = 1;
                    gbc_btnPickCvFile.gridy = 3;
                    panelGeneralSettings.add(btnPickCvFile, gbc_btnPickCvFile);
                }
                {
                    txtCvFilePath = new JTextField();
                    txtCvFilePath.setEnabled(false);
                    GridBagConstraints gbc_txtCvFilePath = new GridBagConstraints();
                    gbc_txtCvFilePath.insets = new Insets(0, 0, 5, 0);
                    gbc_txtCvFilePath.fill = GridBagConstraints.HORIZONTAL;
                    gbc_txtCvFilePath.gridx = 2;
                    gbc_txtCvFilePath.gridy = 3;
                    panelGeneralSettings.add(txtCvFilePath, gbc_txtCvFilePath);
                    txtCvFilePath.setColumns(10);
                }
                {
                    chckbxIncludeDefinitionsFile = new JCheckBox("Include Definitions File");
                    chckbxIncludeDefinitionsFile.addItemListener(new ItemListener() {
                        public void itemStateChanged(ItemEvent e) {
                            btnPickDefinitionsFile.setEnabled(chckbxIncludeDefinitionsFile.isSelected());
                            txtDefinitionsFilePath.setEnabled(chckbxIncludeDefinitionsFile.isSelected());
                        }
                    });
                    GridBagConstraints gbc_chckbxIncludeDefinitionsFile = new GridBagConstraints();
                    gbc_chckbxIncludeDefinitionsFile.anchor = GridBagConstraints.WEST;
                    gbc_chckbxIncludeDefinitionsFile.insets = new Insets(0, 0, 5, 5);
                    gbc_chckbxIncludeDefinitionsFile.gridx = 0;
                    gbc_chckbxIncludeDefinitionsFile.gridy = 4;
                    panelGeneralSettings.add(chckbxIncludeDefinitionsFile, gbc_chckbxIncludeDefinitionsFile);
                }
                {
                    btnPickDefinitionsFile = new JButton("Choose");
                    btnPickDefinitionsFile.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            JFileChooser chooser = new JFileChooser();
                            chooser.setDialogTitle("Choose Definitions File");
                            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                                txtDefinitionsFilePath.setText(chooser.getSelectedFile().getAbsolutePath());
                            }
                        }
                    });
                    btnPickDefinitionsFile.setEnabled(false);
                    GridBagConstraints gbc_btnPickDefinitionsFile = new GridBagConstraints();
                    gbc_btnPickDefinitionsFile.insets = new Insets(0, 0, 5, 5);
                    gbc_btnPickDefinitionsFile.gridx = 1;
                    gbc_btnPickDefinitionsFile.gridy = 4;
                    panelGeneralSettings.add(btnPickDefinitionsFile, gbc_btnPickDefinitionsFile);
                }
                {
                    txtDefinitionsFilePath = new JTextField();
                    txtDefinitionsFilePath.setEnabled(false);
                    GridBagConstraints gbc_txtDefinitionsFilePath = new GridBagConstraints();
                    gbc_txtDefinitionsFilePath.insets = new Insets(0, 0, 5, 0);
                    gbc_txtDefinitionsFilePath.fill = GridBagConstraints.HORIZONTAL;
                    gbc_txtDefinitionsFilePath.gridx = 2;
                    gbc_txtDefinitionsFilePath.gridy = 4;
                    panelGeneralSettings.add(txtDefinitionsFilePath, gbc_txtDefinitionsFilePath);
                    txtDefinitionsFilePath.setColumns(10);
                }
                {
                    panel_1 = new JPanel();
                    GridBagConstraints gbc_panel_1 = new GridBagConstraints();
                    gbc_panel_1.anchor = GridBagConstraints.WEST;
                    gbc_panel_1.gridwidth = 3;
                    gbc_panel_1.insets = new Insets(0, 0, 5, 5);
                    gbc_panel_1.fill = GridBagConstraints.VERTICAL;
                    gbc_panel_1.gridx = 0;
                    gbc_panel_1.gridy = 5;
                    panelGeneralSettings.add(panel_1, gbc_panel_1);
                    {
                        btnAddAdditionalFile = new JButton("Add Additional File");
                        btnAddAdditionalFile.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                JFileChooser chooser = new JFileChooser();
                                chooser.setDialogTitle("Choose Additional File");
                                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                                    File selectedFile = chooser.getSelectedFile();
                                    IncludedFile iFile = new IncludedFile();
                                    iFile.setFilePath(selectedFile);
                                    iFile.setTitle(FilenameUtils.getBaseName(selectedFile.getPath()));
                                    tableFilesModel.addFile(iFile);
                                }
                            }
                        });
                        panel_1.add(btnAddAdditionalFile);
                    }
                    {
                        btnMoveUp_1 = new JButton("Move Up");
                        btnMoveUp_1.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent arg0) {
                                int rows[] = tableFiles.getSelectedRows();
                                rows = tableFilesModel.shiftFiles(rows, -1);
                                tableFiles.setRowSelectionInterval(rows[0], rows[rows.length - 1]);
                            }
                        });
                        panel_1.add(btnMoveUp_1);
                    }
                    {
                        btnMoveDown_1 = new JButton("Move Down");
                        btnMoveDown_1.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                int rows[] = tableFiles.getSelectedRows();
                                rows = tableFilesModel.shiftFiles(rows, 1);
                                tableFiles.setRowSelectionInterval(rows[0], rows[rows.length - 1]);
                            }
                        });
                        panel_1.add(btnMoveDown_1);
                    }
                    {
                        horizontalStrut = Box.createHorizontalStrut(50);
                        panel_1.add(horizontalStrut);
                    }
                    {
                        btnRemoveSelected = new JButton("Remove Selected");
                        btnRemoveSelected.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                int selectedRows[] = tableFiles.getSelectedRows();
                                if (selectedRows.length > 0) {
                                    Arrays.sort(selectedRows);
                                    for (int i = selectedRows.length - 1; i >= 0; i--) {
                                        int row = selectedRows[i];
                                        tableFilesModel.removeFileAt(row);
                                    }
                                }
                            }
                        });
                        panel_1.add(btnRemoveSelected);
                    }
                    {
                        btnRemoveAll = new JButton("Remove All");
                        btnRemoveAll.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                tableFilesModel.clear();
                            }
                        });
                        panel_1.add(btnRemoveAll);
                    }
                }
                {
                    scrollPane_1 = new JScrollPane();
                    scrollPane_1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
                    GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
                    gbc_scrollPane_1.gridwidth = 3;
                    gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
                    gbc_scrollPane_1.gridx = 0;
                    gbc_scrollPane_1.gridy = 6;
                    panelGeneralSettings.add(scrollPane_1, gbc_scrollPane_1);
                    {
                        tableFiles = new JTable();
                        scrollPane_1.setViewportView(tableFiles);
                        tableFiles.setModel(tableFilesModel);
                    }
                }
            }
            {
                JPanel panelSummarySettings = new JPanel();
                tabbedPane.addTab("Summaries", null, panelSummarySettings, null);
                GridBagLayout gbl_panelSummarySettings = new GridBagLayout();
                gbl_panelSummarySettings.columnWidths = new int[]{0, 0};
                gbl_panelSummarySettings.rowHeights = new int[]{0, 0};
                gbl_panelSummarySettings.columnWeights = new double[]{1.0, Double.MIN_VALUE};
                gbl_panelSummarySettings.rowWeights = new double[]{1.0, Double.MIN_VALUE};
                panelSummarySettings.setLayout(gbl_panelSummarySettings);
                {
                    {
                        JPanel panel = new JPanel();
                        panel.setBorder(new TitledBorder(null, "Tag Based", TitledBorder.LEADING, TitledBorder.TOP, null, null));
                        GridBagConstraints gbc_panel = new GridBagConstraints();
                        gbc_panel.fill = GridBagConstraints.BOTH;
                        gbc_panel.gridx = 0;
                        gbc_panel.gridy = 0;
                        panelSummarySettings.add(panel, gbc_panel);
                        GridBagLayout gbl_panel = new GridBagLayout();
                        gbl_panel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                        gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0};
                        gbl_panel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
                        gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
                        panel.setLayout(gbl_panel);
                        {
                            chckbxReportExcludedItems = new JCheckBox("Report Excluded Items");
                            chckbxReportExcludedItems.setSelected(true);
                            GridBagConstraints gbc_chckbxReportExcludedItems = new GridBagConstraints();
                            gbc_chckbxReportExcludedItems.gridwidth = 2;
                            gbc_chckbxReportExcludedItems.insets = new Insets(0, 0, 5, 5);
                            gbc_chckbxReportExcludedItems.gridx = 0;
                            gbc_chckbxReportExcludedItems.gridy = 0;
                            panel.add(chckbxReportExcludedItems, gbc_chckbxReportExcludedItems);
                        }
                        {
                            spinnerRecordsPerPage = new JSpinner();
                            spinnerRecordsPerPage.setModel(new SpinnerNumberModel(defaultRecordsPerPage, new Integer(10), null, new Integer(100)));
                            GridBagConstraints gbc_spinnerRecordsPerPage = new GridBagConstraints();
                            gbc_spinnerRecordsPerPage.fill = GridBagConstraints.HORIZONTAL;
                            gbc_spinnerRecordsPerPage.insets = new Insets(0, 0, 5, 5);
                            gbc_spinnerRecordsPerPage.gridx = 0;
                            gbc_spinnerRecordsPerPage.gridy = 1;
                            panel.add(spinnerRecordsPerPage, gbc_spinnerRecordsPerPage);
                        }
                        {
                            lblRecordsPerPage = new JLabel("Records Per Summary Page");
                            GridBagConstraints gbc_lblRecordsPerPage = new GridBagConstraints();
                            gbc_lblRecordsPerPage.anchor = GridBagConstraints.WEST;
                            gbc_lblRecordsPerPage.gridwidth = 2;
                            gbc_lblRecordsPerPage.insets = new Insets(0, 0, 5, 5);
                            gbc_lblRecordsPerPage.gridx = 1;
                            gbc_lblRecordsPerPage.gridy = 1;
                            panel.add(lblRecordsPerPage, gbc_lblRecordsPerPage);
                        }
                        JButton btnAddRow = new JButton("Add Row");
                        GridBagConstraints gbc_btnAddRow = new GridBagConstraints();
                        gbc_btnAddRow.insets = new Insets(0, 0, 5, 5);
                        gbc_btnAddRow.gridx = 0;
                        gbc_btnAddRow.gridy = 2;
                        panel.add(btnAddRow, gbc_btnAddRow);
                        {
                            btnMoveUp = new JButton("Move Up");
                            btnMoveUp.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent arg0) {
                                    int rows[] = tableSummaries.getSelectedRows();
                                    rows = tableSummariesModel.shiftSummaries(rows, -1);
                                    tableSummaries.setRowSelectionInterval(rows[0], rows[rows.length - 1]);
                                }
                            });
                            GridBagConstraints gbc_btnMoveUp = new GridBagConstraints();
                            gbc_btnMoveUp.insets = new Insets(0, 0, 5, 5);
                            gbc_btnMoveUp.gridx = 1;
                            gbc_btnMoveUp.gridy = 2;
                            panel.add(btnMoveUp, gbc_btnMoveUp);
                        }
                        {
                            btnMoveDown = new JButton("Move Down");
                            btnMoveDown.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent e) {
                                    int rows[] = tableSummaries.getSelectedRows();
                                    rows = tableSummariesModel.shiftSummaries(rows, 1);
                                    tableSummaries.setRowSelectionInterval(rows[0], rows[rows.length - 1]);
                                }
                            });
                            GridBagConstraints gbc_btnMoveDown = new GridBagConstraints();
                            gbc_btnMoveDown.insets = new Insets(0, 0, 5, 5);
                            gbc_btnMoveDown.gridx = 2;
                            gbc_btnMoveDown.gridy = 2;
                            panel.add(btnMoveDown, gbc_btnMoveDown);
                        }
                        {
                            btnSetAllProfiles = new JButton("Set All Profiles");
                            btnSetAllProfiles.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent arg0) {
                                    String profileChoice = (String) JOptionPane.showInputDialog(
                                            ReportSettingsDialog.this,
                                            "Select profile for all summaries.",
                                            "Set all summary profiles",
                                            JOptionPane.PLAIN_MESSAGE,
                                            null,
                                            availableProfiles.toArray(),
                                            availableProfiles.get(0)
                                    );
                                    if (profileChoice != null) {
                                        for (SummaryInfo info : tableSummariesModel.getSummaries()) {
                                            info.setProfile(profileChoice);
                                        }
                                        tableSummariesModel.refresh();
                                    }
                                }
                            });
                            GridBagConstraints gbc_btnSetAllProfiles = new GridBagConstraints();
                            gbc_btnSetAllProfiles.insets = new Insets(0, 0, 5, 5);
                            gbc_btnSetAllProfiles.gridx = 4;
                            gbc_btnSetAllProfiles.gridy = 2;
                            panel.add(btnSetAllProfiles, gbc_btnSetAllProfiles);
                        }
                        {
                            btnSetAllSorts = new JButton("Set All Sorts");
                            btnSetAllSorts.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent arg0) {
                                    String sortChoice = (String) JOptionPane.showInputDialog(
                                            ReportSettingsDialog.this,
                                            "Select sort orger for all summaries.",
                                            "Set all summary sort order",
                                            JOptionPane.PLAIN_MESSAGE,
                                            null,
                                            availableItemSorts,
                                            availableItemSorts[0]
                                    );

                                    if (sortChoice != null) {
                                        for (SummaryInfo info : tableSummariesModel.getSummaries()) {
                                            info.setSort(sortChoice);
                                        }
                                        tableSummariesModel.refresh();
                                    }
                                }
                            });
                            GridBagConstraints gbc_btnSetAllSorts = new GridBagConstraints();
                            gbc_btnSetAllSorts.insets = new Insets(0, 0, 5, 5);
                            gbc_btnSetAllSorts.gridx = 5;
                            gbc_btnSetAllSorts.gridy = 2;
                            panel.add(btnSetAllSorts, gbc_btnSetAllSorts);
                        }
                        {
                            JButton btnRemoveRow = new JButton("Remove Row(s)");
                            GridBagConstraints gbc_btnRemoveRow = new GridBagConstraints();
                            gbc_btnRemoveRow.insets = new Insets(0, 0, 5, 5);
                            gbc_btnRemoveRow.gridx = 7;
                            gbc_btnRemoveRow.gridy = 2;
                            panel.add(btnRemoveRow, gbc_btnRemoveRow);
                            btnRemoveRow.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent arg0) {
                                    try {
                                        tableSummaries.getCellEditor().cancelCellEditing();
                                    } catch (Exception exc) {
                                    }
                                    int selectedRows[] = tableSummaries.getSelectedRows();
                                    if (selectedRows.length > 0) {
                                        Arrays.sort(selectedRows);
                                        for (int i = selectedRows.length - 1; i >= 0; i--) {
                                            int row = selectedRows[i];
                                            tableSummariesModel.removeSummaryAt(row);
                                        }
                                    }
                                }
                            });
                            {
                                JButton btnRemoveAllRows = new JButton("Remove All Rows");
                                GridBagConstraints gbc_btnRemoveAllRows = new GridBagConstraints();
                                gbc_btnRemoveAllRows.insets = new Insets(0, 0, 5, 0);
                                gbc_btnRemoveAllRows.gridx = 8;
                                gbc_btnRemoveAllRows.gridy = 2;
                                panel.add(btnRemoveAllRows, gbc_btnRemoveAllRows);

                                btnRemoveAllRows.addActionListener(new ActionListener() {
                                    public void actionPerformed(ActionEvent e) {
                                        if (JOptionPane.showConfirmDialog(ReportSettingsDialog.this,
                                                "Are you sure you want to remove all summaries?", "Remove all summaries?",
                                                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                                            try {
                                                tableSummaries.getCellEditor().cancelCellEditing();
                                            } catch (Exception exc) {
                                            }
                                            tableSummariesModel.clear();
                                        }
                                    }
                                });
                                JScrollPane scrollPane = new JScrollPane();
                                GridBagConstraints gbc_scrollPane = new GridBagConstraints();
                                gbc_scrollPane.fill = GridBagConstraints.BOTH;
                                gbc_scrollPane.gridwidth = 9;
                                gbc_scrollPane.gridx = 0;
                                gbc_scrollPane.gridy = 3;
                                panel.add(scrollPane, gbc_scrollPane);
                                scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
                                tableSummaries = new JTable();
                                tableSummaries.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
                                scrollPane.setViewportView(tableSummaries);
                                tableSummaries.setModel(tableSummariesModel);
                            }
                        }
                        btnAddRow.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent arg0) {
                                tableSummariesModel.addSummary(new SummaryInfo("Report " + (tableSummariesModel.getRowCount() + 1)));
                            }
                        });
                    }
                }
                {
                    {
                        {
                            JPanel panelItemDetails = new JPanel();
                            tabbedPane.addTab("Item Details", null, panelItemDetails, null);
                            GridBagLayout gbl_panelItemDetails = new GridBagLayout();
                            gbl_panelItemDetails.columnWidths = new int[]{0, 0};
                            gbl_panelItemDetails.rowHeights = new int[]{0, 0, 0, 0};
                            gbl_panelItemDetails.columnWeights = new double[]{1.0, Double.MIN_VALUE};
                            gbl_panelItemDetails.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
                            panelItemDetails.setLayout(gbl_panelItemDetails);
                            {
                                JPanel panel = new JPanel();
                                panel.setBorder(new TitledBorder(null, "Exported Products", TitledBorder.LEADING, TitledBorder.TOP, null, null));
                                GridBagConstraints gbc_panel = new GridBagConstraints();
                                gbc_panel.insets = new Insets(0, 0, 5, 0);
                                gbc_panel.fill = GridBagConstraints.BOTH;
                                gbc_panel.gridx = 0;
                                gbc_panel.gridy = 0;
                                panelItemDetails.add(panel, gbc_panel);
                                GridBagLayout gbl_panel = new GridBagLayout();
                                gbl_panel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
                                gbl_panel.rowHeights = new int[]{0, 0};
                                gbl_panel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
                                gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
                                panel.setLayout(gbl_panel);
                                {
                                    chckbxExportNatives = new JCheckBox("Export Natives");
                                    chckbxExportNatives.setToolTipText("Whether Native files will be exported for report items");
                                    GridBagConstraints gbc_chckbxExportNatives = new GridBagConstraints();
                                    gbc_chckbxExportNatives.anchor = GridBagConstraints.WEST;
                                    gbc_chckbxExportNatives.insets = new Insets(0, 0, 0, 5);
                                    gbc_chckbxExportNatives.gridx = 0;
                                    gbc_chckbxExportNatives.gridy = 0;
                                    panel.add(chckbxExportNatives, gbc_chckbxExportNatives);
                                }
                                {
                                    chckbxExportPdfs = new JCheckBox("Export PDFs");
                                    chckbxExportPdfs.addActionListener(new ActionListener() {
                                        public void actionPerformed(ActionEvent arg0) {
                                            pdfOptionsPanel.setEnabled(chckbxExportPdfs.isSelected());
                                        }
                                    });
                                    chckbxExportPdfs.setToolTipText("Whether PDF files will be exported for report items");
                                    GridBagConstraints gbc_chckbxExportPdfs = new GridBagConstraints();
                                    gbc_chckbxExportPdfs.anchor = GridBagConstraints.WEST;
                                    gbc_chckbxExportPdfs.insets = new Insets(0, 0, 0, 5);
                                    gbc_chckbxExportPdfs.gridx = 2;
                                    gbc_chckbxExportPdfs.gridy = 0;
                                    panel.add(chckbxExportPdfs, gbc_chckbxExportPdfs);
                                }
                                {
                                    chckbxExportText = new JCheckBox("Export Text");
                                    chckbxExportText.setToolTipText("Whether Text files will be exported for report items");
                                    GridBagConstraints gbc_chckbxExportText = new GridBagConstraints();
                                    gbc_chckbxExportText.insets = new Insets(0, 0, 0, 5);
                                    gbc_chckbxExportText.anchor = GridBagConstraints.WEST;
                                    gbc_chckbxExportText.gridx = 4;
                                    gbc_chckbxExportText.gridy = 0;
                                    panel.add(chckbxExportText, gbc_chckbxExportText);
                                }
                                {
                                    chckbxExportThumbnails = new JCheckBox("Export Thumbnails");
                                    chckbxExportThumbnails.setToolTipText("Whether Thumbnail files will be exported for report items");
                                    GridBagConstraints gbc_chckbxExportThumbnails = new GridBagConstraints();
                                    gbc_chckbxExportThumbnails.anchor = GridBagConstraints.WEST;
                                    gbc_chckbxExportThumbnails.gridx = 6;
                                    gbc_chckbxExportThumbnails.gridy = 0;
                                    panel.add(chckbxExportThumbnails, gbc_chckbxExportThumbnails);
                                }
                            }
                            {
                                pdfOptionsPanel = new JPanel() {
                                    @Override
                                    public void setEnabled(boolean value) {
                                        for (Component c : getComponents()) {
                                            c.setEnabled(value);
                                        }
                                        super.setEnabled(value);
                                    }
                                };
                                pdfOptionsPanel.setBorder(new TitledBorder(null, "PDF Options", TitledBorder.LEADING, TitledBorder.TOP, null, null));
                                GridBagConstraints gbc_pdfOptionsPanel = new GridBagConstraints();
                                gbc_pdfOptionsPanel.anchor = GridBagConstraints.WEST;
                                gbc_pdfOptionsPanel.insets = new Insets(0, 0, 5, 0);
                                gbc_pdfOptionsPanel.fill = GridBagConstraints.VERTICAL;
                                gbc_pdfOptionsPanel.gridx = 0;
                                gbc_pdfOptionsPanel.gridy = 1;
                                panelItemDetails.add(pdfOptionsPanel, gbc_pdfOptionsPanel);
                                {
                                    chckbxImageSpreadsheets = new JCheckBox("Image Spreadsheets");
                                    pdfOptionsPanel.add(chckbxImageSpreadsheets);
                                }
                                {
                                    chckbxRegenerateStored = new JCheckBox("Regenerate Stored");
                                    pdfOptionsPanel.add(chckbxRegenerateStored);
                                }
                            }
                            {
                                JPanel panel = new JPanel();
                                panel.setBorder(new TitledBorder(null, "Exported Information", TitledBorder.LEADING, TitledBorder.TOP, null, null));
                                GridBagConstraints gbc_panel = new GridBagConstraints();
                                gbc_panel.fill = GridBagConstraints.BOTH;
                                gbc_panel.gridx = 0;
                                gbc_panel.gridy = 2;
                                panelItemDetails.add(panel, gbc_panel);
                                GridBagLayout gbl_panel = new GridBagLayout();
                                gbl_panel.columnWidths = new int[]{0, 0, 0};
                                gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
                                gbl_panel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
                                gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
                                panel.setLayout(gbl_panel);
                                {
                                    chckbxIncludeComments = new JCheckBox("Include Comments");
                                    GridBagConstraints gbc_chckbxIncludeComments = new GridBagConstraints();
                                    gbc_chckbxIncludeComments.anchor = GridBagConstraints.WEST;
                                    gbc_chckbxIncludeComments.insets = new Insets(0, 0, 5, 5);
                                    gbc_chckbxIncludeComments.gridx = 0;
                                    gbc_chckbxIncludeComments.gridy = 0;
                                    panel.add(chckbxIncludeComments, gbc_chckbxIncludeComments);
                                }
                                {
                                    chckbxIncludeProperties = new JCheckBox("Include Properties");
                                    chckbxIncludeProperties.setToolTipText("Whether item details should include item property values");
                                    chckbxIncludeProperties.setSelected(true);
                                    GridBagConstraints gbc_chckbxIncludeProperties = new GridBagConstraints();
                                    gbc_chckbxIncludeProperties.anchor = GridBagConstraints.WEST;
                                    gbc_chckbxIncludeProperties.insets = new Insets(0, 0, 5, 5);
                                    gbc_chckbxIncludeProperties.gridx = 0;
                                    gbc_chckbxIncludeProperties.gridy = 1;
                                    panel.add(chckbxIncludeProperties, gbc_chckbxIncludeProperties);
                                }
                                {
                                    chckbxIncludeProfile = new JCheckBox("Include Profile");
                                    chckbxIncludeProfile.setToolTipText("Whether item details should contain the output of a specific metadata profile");
                                    chckbxIncludeProfile.addItemListener(new ItemListener() {
                                        public void itemStateChanged(ItemEvent arg0) {
                                            comboBoxDetailsProfile.setEnabled(chckbxIncludeProfile.isSelected());
                                        }
                                    });
                                    {
                                        chckbxIncludeTags = new JCheckBox("Include Tags");
                                        chckbxIncludeTags.setToolTipText("Whether the item details should include the tags present on an item");
                                        GridBagConstraints gbc_chckbxIncludeTags = new GridBagConstraints();
                                        gbc_chckbxIncludeTags.anchor = GridBagConstraints.WEST;
                                        gbc_chckbxIncludeTags.insets = new Insets(0, 0, 5, 5);
                                        gbc_chckbxIncludeTags.gridx = 0;
                                        gbc_chckbxIncludeTags.gridy = 2;
                                        panel.add(chckbxIncludeTags, gbc_chckbxIncludeTags);
                                    }
                                    {
                                        chckbxIncludeCustomMetadata = new JCheckBox("Include Custom Metadata");
                                        chckbxIncludeCustomMetadata.setToolTipText("Whether item details should contain the custom metadata present on an item");
                                        GridBagConstraints gbc_chckbxIncludeCustomMetadata = new GridBagConstraints();
                                        gbc_chckbxIncludeCustomMetadata.anchor = GridBagConstraints.WEST;
                                        gbc_chckbxIncludeCustomMetadata.insets = new Insets(0, 0, 5, 5);
                                        gbc_chckbxIncludeCustomMetadata.gridx = 0;
                                        gbc_chckbxIncludeCustomMetadata.gridy = 3;
                                        panel.add(chckbxIncludeCustomMetadata, gbc_chckbxIncludeCustomMetadata);
                                    }
                                    GridBagConstraints gbc_chckbxIncludeProfile = new GridBagConstraints();
                                    gbc_chckbxIncludeProfile.anchor = GridBagConstraints.WEST;
                                    gbc_chckbxIncludeProfile.insets = new Insets(0, 0, 5, 5);
                                    gbc_chckbxIncludeProfile.gridx = 0;
                                    gbc_chckbxIncludeProfile.gridy = 4;
                                    panel.add(chckbxIncludeProfile, gbc_chckbxIncludeProfile);
                                }
                                {
                                    comboBoxDetailsProfile = new JComboBox<String>();
                                    for (String profile : availableProfiles) {
                                        comboBoxDetailsProfile.addItem(profile);
                                    }
                                    comboBoxDetailsProfile.setEnabled(false);
                                    GridBagConstraints gbc_comboBoxDetailsProfile = new GridBagConstraints();
                                    gbc_comboBoxDetailsProfile.insets = new Insets(0, 0, 5, 0);
                                    gbc_comboBoxDetailsProfile.fill = GridBagConstraints.HORIZONTAL;
                                    gbc_comboBoxDetailsProfile.gridx = 1;
                                    gbc_comboBoxDetailsProfile.gridy = 4;
                                    panel.add(comboBoxDetailsProfile, gbc_comboBoxDetailsProfile);
                                }
                                {
                                    chckbxIncludeText = new JCheckBox("Include Text");
                                    chckbxIncludeText.setToolTipText("Whether item details should include the content text of items");
                                    chckbxIncludeText.setSelected(true);
                                    GridBagConstraints gbc_chckbxIncludeText = new GridBagConstraints();
                                    gbc_chckbxIncludeText.anchor = GridBagConstraints.WEST;
                                    gbc_chckbxIncludeText.insets = new Insets(0, 0, 0, 5);
                                    gbc_chckbxIncludeText.gridx = 0;
                                    gbc_chckbxIncludeText.gridy = 5;
                                    panel.add(chckbxIncludeText, gbc_chckbxIncludeText);
                                }
                            }
                        }
                        {
                            JPanel panelParallelProcessingSettings = new JPanel();
                            tabbedPane.addTab("Parallel Export Settings", null, panelParallelProcessingSettings, null);
                            GridBagLayout gbl_panelParallelProcessingSettings = new GridBagLayout();
                            gbl_panelParallelProcessingSettings.columnWidths = new int[]{0, 0, 0};
                            gbl_panelParallelProcessingSettings.rowHeights = new int[]{0, 0, 0, 0};
                            gbl_panelParallelProcessingSettings.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
                            gbl_panelParallelProcessingSettings.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
                            panelParallelProcessingSettings.setLayout(gbl_panelParallelProcessingSettings);
                            {
                                JLabel lblWorkerCount = new JLabel("Worker Count");
                                GridBagConstraints gbc_lblWorkerCount = new GridBagConstraints();
                                gbc_lblWorkerCount.anchor = GridBagConstraints.EAST;
                                gbc_lblWorkerCount.insets = new Insets(0, 0, 5, 5);
                                gbc_lblWorkerCount.gridx = 0;
                                gbc_lblWorkerCount.gridy = 0;
                                panelParallelProcessingSettings.add(lblWorkerCount, gbc_lblWorkerCount);
                            }
                            {
                                spinnerWorkerCount = new JSpinner();
                                spinnerWorkerCount.setModel(new SpinnerNumberModel(4, 1, 999, 1));
                                GridBagConstraints gbc_spinnerWorkerCount = new GridBagConstraints();
                                gbc_spinnerWorkerCount.anchor = GridBagConstraints.WEST;
                                gbc_spinnerWorkerCount.insets = new Insets(0, 0, 5, 0);
                                gbc_spinnerWorkerCount.gridx = 1;
                                gbc_spinnerWorkerCount.gridy = 0;
                                panelParallelProcessingSettings.add(spinnerWorkerCount, gbc_spinnerWorkerCount);
                            }
                            {
                                JLabel lblWorkerMemorymb = new JLabel("Worker Memory (MB)");
                                GridBagConstraints gbc_lblWorkerMemorymb = new GridBagConstraints();
                                gbc_lblWorkerMemorymb.anchor = GridBagConstraints.EAST;
                                gbc_lblWorkerMemorymb.insets = new Insets(0, 0, 5, 5);
                                gbc_lblWorkerMemorymb.gridx = 0;
                                gbc_lblWorkerMemorymb.gridy = 1;
                                panelParallelProcessingSettings.add(lblWorkerMemorymb, gbc_lblWorkerMemorymb);
                            }
                            {
                                spinnerWorkerMemory = new JSpinner();
                                spinnerWorkerMemory.setModel(new SpinnerNumberModel(768, 768, 1000000, 1));
                                GridBagConstraints gbc_spinnerWorkerMemory = new GridBagConstraints();
                                gbc_spinnerWorkerMemory.anchor = GridBagConstraints.WEST;
                                gbc_spinnerWorkerMemory.insets = new Insets(0, 0, 5, 0);
                                gbc_spinnerWorkerMemory.gridx = 1;
                                gbc_spinnerWorkerMemory.gridy = 1;
                                panelParallelProcessingSettings.add(spinnerWorkerMemory, gbc_spinnerWorkerMemory);
                            }
                            {
                                JButton btnWorkerTemp = new JButton("Worker Temp");
                                btnWorkerTemp.addActionListener(new ActionListener() {
                                    public void actionPerformed(ActionEvent e) {
                                        JFileChooser fc = new JFileChooser();
                                        fc.setDialogTitle("Select Worker Temp Directory");
                                        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                                        fc.setCurrentDirectory(new File(txtWorkerTemp.getText()));
                                        if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                                            txtWorkerTemp.setText(fc.getSelectedFile().getAbsolutePath());
                                        }
                                    }
                                });
                                GridBagConstraints gbc_btnWorkerTemp = new GridBagConstraints();
                                gbc_btnWorkerTemp.insets = new Insets(0, 0, 0, 5);
                                gbc_btnWorkerTemp.gridx = 0;
                                gbc_btnWorkerTemp.gridy = 2;
                                panelParallelProcessingSettings.add(btnWorkerTemp, gbc_btnWorkerTemp);
                            }
                            {
                                txtWorkerTemp = new JTextField();
                                txtWorkerTemp.setText("c:\\NuixWorkerTemp");
                                GridBagConstraints gbc_txtWorkerTemp = new GridBagConstraints();
                                gbc_txtWorkerTemp.fill = GridBagConstraints.HORIZONTAL;
                                gbc_txtWorkerTemp.gridx = 1;
                                gbc_txtWorkerTemp.gridy = 2;
                                panelParallelProcessingSettings.add(txtWorkerTemp, gbc_txtWorkerTemp);
                                txtWorkerTemp.setColumns(10);
                            }
                        }
                        TableColumn tagColumn = tableSummaries.getColumnModel().getColumn(1);
                        TableColumn profileColumn = tableSummaries.getColumnModel().getColumn(2);
                        TableColumn sortColumn = tableSummaries.getColumnModel().getColumn(3);

                        JComboBox<String> comboBoxTags = new JComboBox<String>();
                        for (String tag : availableTags) {
                            System.out.println(tag);
                            comboBoxTags.addItem(tag);
                        }
                        tagColumn.setCellEditor(new DefaultCellEditor(comboBoxTags));

                        JComboBox<String> comboBoxProfiles = new JComboBox<String>();
                        for (String profile : availableProfiles) {
                            comboBoxProfiles.addItem(profile);
                        }
                        profileColumn.setCellEditor(new DefaultCellEditor(comboBoxProfiles));

                        JComboBox<String> comboBoxItemSorts = new JComboBox<String>();
                        for (String sort : availableItemSorts) {
                            comboBoxItemSorts.addItem(sort);
                        }
                        sortColumn.setCellEditor(new DefaultCellEditor(comboBoxItemSorts));
                    }
                }
            }
        }
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                JButton okButton = new JButton("OK");
                okButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (tableSummariesModel.getRowCount() < 1) {
                            String title = "No Summaries Defined";
                            String message = "Please define at least one summary.";
                            JOptionPane.showMessageDialog(ReportSettingsDialog.this, message, title, JOptionPane.WARNING_MESSAGE);
                        } else if (!SummaryInfo.allAreValid(tableSummariesModel.getSummaries())) {
                            String title = "Validation";
                            String message = "Please ensure that all summary definitions have values for all columns!";
                            JOptionPane.showMessageDialog(ReportSettingsDialog.this, message, title, JOptionPane.WARNING_MESSAGE);
                        } else {
                            setDialogResult(true);
                            dispose();
                        }
                    }
                });
                okButton.setActionCommand("OK");
                buttonPane.add(okButton);
                getRootPane().setDefaultButton(okButton);
            }
            {
                JButton cancelButton = new JButton("Cancel");
                cancelButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        dispose();
                    }
                });
                cancelButton.setActionCommand("Cancel");
                buttonPane.add(cancelButton);
            }
        }
        {
            JMenuBar menuBar = new JMenuBar();
            setJMenuBar(menuBar);
        }

        pdfOptionsPanel.setEnabled(false);
        preFillSummaries(defaultReportTitlePrefix);
    }

    private void preFillSummaries(String defaultReportTitlePrefix) {
        // First we assume we can use whatever the configure default profile name is
        String defaultProfile = defaultSummaryProfileName;

        // Check if defaultP{rofileName is not null, not empty
        // and actually matches the name of an existing metadata profile
        boolean defaultProfileNameExists = false;
        if (defaultProfile != null && !defaultProfile.trim().isEmpty()) {
            for (String existingProfileName : availableProfiles) {
                if (existingProfileName.contentEquals(defaultSummaryProfileName)) {
                    defaultProfileNameExists = true;
                    break;
                }
            }
        }

        // If we found it to be invalid, then we just assume the first in the list of available
        if (!defaultProfileNameExists) {
            defaultProfile = availableProfiles.get(0);
        }

        for (String tag : availableTags) {
            SummaryInfo summaryInfo = new SummaryInfo(defaultReportTitlePrefix + tag);
            summaryInfo.setTag(tag);
            summaryInfo.setProfile(defaultProfile);
            summaryInfo.setSort(defaultSummarySort);
            tableSummariesModel.addSummary(summaryInfo);
        }
    }

    public void setOutputDirectory(String value) {
        txtOutputdirectory.setText(value);
    }

    public boolean getDialogResult() {
        return dialogResult;
    }

    protected void setDialogResult(boolean dialogResult) {
        this.dialogResult = dialogResult;
    }

    public Map<String, Object> getSettings() {
        Map<String, Object> settings = new HashMap<String, Object>();

        settings.put("output_directory", txtOutputdirectory.getText());
        //CV file?
        settings.put("include_cv_file", chckbxIncludeCvFile.isSelected());
        settings.put("cv_file_path", txtCvFilePath.getText());
        //Definitions file?
        settings.put("include_definitions_file", chckbxIncludeDefinitionsFile.isSelected());
        settings.put("definitions_file_path", txtDefinitionsFilePath.getText());
        settings.put("report_excluded_items", chckbxReportExcludedItems.isSelected());

        List<HashMap<String, String>> additionalFiles = new ArrayList<HashMap<String, String>>();
        for (IncludedFile file : tableFilesModel.getFiles()) {
            HashMap<String, String> v = new HashMap<String, String>();
            v.put("title", file.getTitle());
            v.put("path", file.getFilePath().getPath());
            additionalFiles.add(v);
        }
        settings.put("additional_files", additionalFiles);

        List<Map<String, Object>> summaryReports = new ArrayList<Map<String, Object>>();
        for (SummaryInfo info : tableSummariesModel.getSummaries()) {
            summaryReports.add(info.toMap());
        }
        settings.put("summary_reports", summaryReports);
        settings.put("records_per_summary_report", (Integer) spinnerRecordsPerPage.getValue());

        List<HashMap<String, Object>> products = new ArrayList<HashMap<String, Object>>();
        if (chckbxExportNatives.isSelected()) {
            HashMap<String, Object> entry = new HashMap<String, Object>();
            entry.put("type", "native");
            products.add(entry);
        }
        if (chckbxExportText.isSelected()) {
            HashMap<String, Object> entry = new HashMap<String, Object>();
            entry.put("type", "text");
            products.add(entry);
        }
        if (chckbxExportPdfs.isSelected()) {
            HashMap<String, Object> entry = new HashMap<String, Object>();
            entry.put("type", "pdf");
            entry.put("regenerateStored", chckbxRegenerateStored.isSelected());
            products.add(entry);
        }
        if (chckbxExportThumbnails.isSelected()) {
            HashMap<String, Object> entry = new HashMap<String, Object>();
            entry.put("type", "thumbnail");
            products.add(entry);
        }
        settings.put("products", products);

        Map<String, Object> imagingSettings = new HashMap<String, Object>();
        imagingSettings.put("imageExcelSpreadsheets", chckbxImageSpreadsheets.isSelected());
        settings.put("imaging_settings", imagingSettings);

        Map<String, Object> itemDetails = new HashMap<String, Object>();
        itemDetails.put("include_tags", chckbxIncludeTags.isSelected());
        itemDetails.put("include_text", chckbxIncludeText.isSelected());
        itemDetails.put("include_custom_metadata", chckbxIncludeCustomMetadata.isSelected());
        itemDetails.put("include_properties", chckbxIncludeProperties.isSelected());
        if (chckbxIncludeProfile.isSelected()) {
            itemDetails.put("include_profile", comboBoxDetailsProfile.getSelectedItem());
        } else {
            itemDetails.put("include_profile", null);
        }
        itemDetails.put("include_comments", chckbxIncludeComments.isSelected());
        settings.put("item_details", itemDetails);

        settings.put("record_settings", chckbxRecordSettings.isSelected());

        Map<String, Object> parallel_export_settings = new HashMap<String, Object>();
        parallel_export_settings.put("workerCount", spinnerWorkerCount.getValue());
        parallel_export_settings.put("workerMemory", spinnerWorkerMemory.getValue());
        parallel_export_settings.put("workerTemp", txtWorkerTemp.getText());
        settings.put("parallel_export_settings", parallel_export_settings);

        return settings;
    }

    public String getSettingsJson() {
        Gson gson = null;
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.serializeNulls();
        gsonBuilder.setPrettyPrinting();
        gson = gsonBuilder.create();
        String asJson = gson.toJson(getSettings());
        return asJson;
    }

    public void setDefaultSummaryReports(List<Map<String, Object>> reports) {
        for (Map<String, Object> reportInfo : reports) {
            SummaryInfo info = new SummaryInfo();
            info.setTag((String) reportInfo.get("tag"));
            info.setProfile((String) reportInfo.get("profile"));
            info.setTitle((String) reportInfo.get("title"));
            tableSummariesModel.addSummary(info);
        }
    }

    public void setItemDetailSettings(Map<String, Object> settings) {
        chckbxExportNatives.setSelected((Boolean) settings.get("export_natives"));
        chckbxExportPdfs.setSelected((Boolean) settings.get("export_pdfs"));
        chckbxExportText.setSelected((Boolean) settings.get("export_text"));
        chckbxExportThumbnails.setSelected((Boolean) settings.get("export_thumbnails"));
        chckbxImageSpreadsheets.setSelected((Boolean) settings.get("image_spreadsheets"));
        chckbxRegenerateStored.setSelected((Boolean) settings.get("regenerate_stored_pdfs"));
        chckbxIncludeComments.setSelected((Boolean) settings.get("include_comments"));
        chckbxIncludeProperties.setSelected((Boolean) settings.get("include_properties"));
        chckbxIncludeTags.setSelected((Boolean) settings.get("include_tags"));
        chckbxIncludeCustomMetadata.setSelected((Boolean) settings.get("include_custom_metadata"));
        chckbxIncludeProfile.setSelected((Boolean) settings.get("include_profile"));
        comboBoxDetailsProfile.setSelectedItem((String) settings.get("included_profile_name"));
        chckbxIncludeText.setSelected((Boolean) settings.get("include_text"));
    }

    public JSpinner getSpinnerWorkerCount() {
        return spinnerWorkerCount;
    }

    public JSpinner getSpinnerWorkerMemory() {
        return spinnerWorkerMemory;
    }

    public JTextField getTxtWorkerTemp() {
        return txtWorkerTemp;
    }

    public JCheckBox getChckbxIncludeComments() {
        return chckbxIncludeComments;
    }

    public JCheckBox getChckbxReportExcludedItems() {
        return chckbxReportExcludedItems;
    }
}
