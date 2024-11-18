package view.panels;

import entity.Portfolio;
import entity.UserStock;
import utility.ViewManager;
import view.IComponent;
import view.components.TableComponent;
import view.view_events.UpdateAssetEvent;
import view.view_events.ViewEvent;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class PortfolioPanel extends JPanel implements IComponent {
    private static final String TITLE = "Portfolio";
    private static final String[] COLUMN_NAMES = {
            "Ticker", "Company", "Quantity", "Avg Cost", "Market Price", "Total Value", "Profit/Loss"
    };
    private static final double[] COLUMN_PROPORTIONS = {
            0.15,  // Ticker
            0.20,  // Company
            0.10,  // Quantity
            0.15,  // Avg Cost
            0.15,  // Market Price
            0.15,  // Total Value
            0.10   // Profit/Loss
    };
    private static final Font TITLE_FONT = new Font("Lucida Sans", Font.BOLD, 18);
    private static final Font CONTENT_FONT = new Font("Lucida Sans", Font.PLAIN, 14);
    private static final int HEADER_HEIGHT = 40;
    private static final int PADDING = 20;

    private final TableComponent portfolioTable;

    public PortfolioPanel() {
        ViewManager.Instance().registerComponent(this);

        // Initialize table
        DefaultTableModel tableModel = createTableModel();
        portfolioTable = new TableComponent(tableModel, COLUMN_PROPORTIONS);
        portfolioTable.setFont(CONTENT_FONT);

        // Set up panel layout
        setLayout(new BorderLayout(0, PADDING));
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // Add header
        JPanel headerPanel = createHeaderPanel();
        headerPanel.setPreferredSize(new Dimension(0, HEADER_HEIGHT));
        add(headerPanel, BorderLayout.NORTH);

        // Add table
        JScrollPane scrollPane = new JScrollPane(portfolioTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        // Add resize listener
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                portfolioTable.adjustColumnWidths();
            }
        });
    }

    private DefaultTableModel createTableModel() {
        return new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private JPanel createHeaderPanel() {
        // Create main header panel with fixed height
        JPanel headerPanel = new JPanel(new BorderLayout(PADDING, 0));
        headerPanel.setPreferredSize(new Dimension(0, HEADER_HEIGHT));

        // Title with vertical centering
        JLabel titleLabel = new JLabel(TITLE);
        titleLabel.setFont(TITLE_FONT);

        // Center the title vertically
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.add(Box.createVerticalGlue());
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalGlue());

        headerPanel.add(titlePanel, BorderLayout.WEST);

        return headerPanel;
    }

    private void updateTableData(Portfolio portfolio) {
        DefaultTableModel model = (DefaultTableModel) portfolioTable.getModel();
        model.setRowCount(0);

        if (portfolio != null) {
            for (UserStock userStock : portfolio.getAllStocks()) {
                double marketValue = userStock.getCurrentMarketValue();
                double totalCost = userStock.getPurchasedTotalCost();
                double profitLoss = marketValue - totalCost;

                model.addRow(new Object[]{
                        userStock.getStock().getTicker(),
                        userStock.getStock().getCompany(),
                        userStock.getQuantity(),
                        String.format("$%.2f", userStock.getCost()),
                        String.format("$%.2f", userStock.getStock().getPrice()),
                        String.format("$%.2f", marketValue),
                        String.format("$%.2f", profitLoss)
                });
            }
        }
    }

    @Override
    public void receiveViewEvent(ViewEvent event) {
        if (event instanceof UpdateAssetEvent assetEvent) {
            updateTableData(assetEvent.getPortfolio());
        }
    }
}
