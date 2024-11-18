package view.panels;

import entity.Stock;
import utility.ViewManager;
import view.IComponent;
import view.components.TableComponent;
import view.view_events.UpdateStockEvent;
import view.view_events.ViewEvent;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

public class MarketSearchPanel extends JPanel implements IComponent {
    private static final String[] COLUMN_NAMES = {
            "Ticker", "Company", "Industry", "Price"
    };
    private static final double[] COLUMN_PROPORTIONS = {
            0.20, 0.40, 0.30, 0.10
    };
    private static final Font TITLE_FONT = new Font("Lucida Sans", Font.BOLD, 18);
    private static final Font CONTENT_FONT = new Font("Lucida Sans", Font.PLAIN, 14);
    private static final int HEADER_HEIGHT = 40;
    private static final int PADDING = 20;

    private final JTextField searchField;
    private final JButton searchButton;
    private final TableComponent stockTable;
    private final TableRowSorter<DefaultTableModel> rowSorter;

    public MarketSearchPanel() {
        ViewManager.Instance().registerComponent(this);

        // Initialize components
        searchField = new JTextField(20);
        searchField.setFont(CONTENT_FONT);

        searchButton = new JButton("Search");
        searchButton.setFont(CONTENT_FONT);

        DefaultTableModel tableModel = createTableModel();
        stockTable = new TableComponent(tableModel, COLUMN_PROPORTIONS);
        stockTable.setFont(CONTENT_FONT);
        rowSorter = new TableRowSorter<>(tableModel);
        stockTable.setRowSorter(rowSorter);

        // Set up panel layout
        setLayout(new BorderLayout(0, PADDING));
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // Create and add header
        JPanel headerPanel = createHeaderPanel();
        headerPanel.setPreferredSize(new Dimension(0, HEADER_HEIGHT));
        add(headerPanel, BorderLayout.NORTH);

        // Add table
        JScrollPane scrollPane = new JScrollPane(stockTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        // Configure search action
        searchButton.addActionListener(e -> performSearch());

        // Add resize listener
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                stockTable.adjustColumnWidths();
            }
        });
    }

    private JPanel createHeaderPanel() {
        // Create main header panel with fixed height
        JPanel headerPanel = new JPanel(new BorderLayout(PADDING, 0));
        headerPanel.setPreferredSize(new Dimension(0, HEADER_HEIGHT));

        // Title with vertical centering
        JLabel titleLabel = new JLabel("Market Overview");
        titleLabel.setFont(TITLE_FONT);

        // Center the title vertically
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        titlePanel.add(Box.createVerticalGlue());
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalGlue());

        // Search panel with vertical centering
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));

        // Create search controls panel
        JPanel searchControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchField.setPreferredSize(new Dimension(searchField.getPreferredSize().width, 30));
        searchButton.setPreferredSize(new Dimension(searchButton.getPreferredSize().width, 30));
        searchControls.add(searchField);
        searchControls.add(searchButton);

        // Center the search controls vertically
        searchPanel.add(Box.createVerticalGlue());
        searchPanel.add(searchControls);
        searchPanel.add(Box.createVerticalGlue());

        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(searchPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private DefaultTableModel createTableModel() {
        return new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private void performSearch() {
        String searchText = searchField.getText().toLowerCase();
        if (searchText.isEmpty()) {
            rowSorter.setRowFilter(null);
        } else {
            rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText));
        }
    }

    private void updateTableData(List<Stock> stocks) {
        DefaultTableModel model = (DefaultTableModel) stockTable.getModel();
        model.setRowCount(0);

        for (Stock stock : stocks) {
            model.addRow(new Object[]{
                    stock.getTicker(),
                    stock.getCompany(),
                    stock.getIndustry(),
                    String.format("$%.2f", stock.getPrice())
            });
        }
    }

    @Override
    public void receiveViewEvent(ViewEvent event) {
        if (event instanceof UpdateStockEvent stockEvent) {
            updateTableData(stockEvent.getStocks());
        }
    }
}