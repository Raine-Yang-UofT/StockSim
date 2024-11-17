package app;

import data_access.InMemoryStockDataAccessObject;
import data_access.InMemoryUserDataAccessObject;
import data_access.StockDataAccessInterface;
import entity.StockMarket;
import interface_adapter.execute_buy.ExecuteBuyController;
import interface_adapter.execute_buy.ExecuteBuyPresenter;
import interface_adapter.login.LoginController;
import interface_adapter.login.LoginPresenter;
import interface_adapter.view_history.ViewHistoryController;
import interface_adapter.view_history.ViewHistoryPresenter;
import use_case.execute_buy.ExecuteBuyDataAccessInterface;
import use_case.execute_buy.ExecuteBuyInputBoundary;
import use_case.execute_buy.ExecuteBuyInteractor;
import use_case.execute_buy.ExecuteBuyOutputBoundary;
import use_case.login.LoginDataAccessInterface;
import use_case.login.LoginInputBoundary;
import use_case.login.LoginInteractor;
import use_case.login.LoginOutputBoundary;
import use_case.view_history.ViewHistoryDataAccessInterface;
import use_case.view_history.ViewHistoryInputBoundary;
import use_case.view_history.ViewHistoryInteractor;
import use_case.view_history.ViewHistoryOutputBoundary;
import utility.ServiceManager;
import utility.ViewManager;
import view.components.DialogComponent;
import view.panels.*;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * A builder class for the application.
 * Manages application frame creation, service registration, and view management.
 */
public class AppBuilder {
    // Default dimensions and title for the application window
    private static final int DEFAULT_WIDTH = 1000;
    private static final int DEFAULT_HEIGHT = 800;
    private static final String DEFAULT_TITLE = "Application";

    // Components for the application
    private final JPanel cardPanel;
    private final CardLayout cardLayout;
    private final Map<String, JPanel> panels;
    private String initialPanel = "LogInPanel";

    // Custom dimensions and title for the application window
    private int width = DEFAULT_WIDTH;
    private int height = DEFAULT_HEIGHT;
    private String title = DEFAULT_TITLE;

    /**
     * Constructor for the AppBuilder class
     */
    public AppBuilder() {
        this.cardPanel = new JPanel();
        this.cardLayout = new CardLayout();
        this.panels = new HashMap<>();
        this.cardPanel.setLayout(cardLayout);
    }

    /**
     * Sets custom dimensions for the application window
     */
    public AppBuilder withDimensions(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    /**
     * Sets custom title for the application window
     */
    public AppBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * Sets the initial panel to be displayed
     */
    public AppBuilder withInitialPanel(String panelName) {
        this.initialPanel = panelName;
        return this;
    }

    /**
     * Adds authentication panels (Login and SignUp)
     */
    public AppBuilder addAuthenticationPanels() {
        addPanel("LogInPanel", new LogInPanel());
        addPanel("SignUpPanel", new SignUpPanel());
        return this;
    }

    /**
     * Adds the dashboard panel with user information
     */
    public AppBuilder addDashboardPanel() {
        addPanel("DashboardPanel", new DashboardPanel());
        return this;
    }

    /**
     * Adds the action panels
     */
    public AppBuilder addActionPanels() {
        addPanel("TradeSimulationPanel", new TradeSimulationPanel());
        addPanel("TransactionHistoryPanel", new TransactionHistoryPanel());
        return this;
    }

    /**
     * Adds the dialog component
     */
    public AppBuilder addDialogComponent() {
        ServiceManager.Instance().registerService(DialogComponent.class, new DialogComponent());
        return this;
    }

    /**
     * Helper method to add a panel to both the card layout and panels map
     */
    private void addPanel(String name, JPanel panel) {
        panels.put(name, panel);
        cardPanel.add(panel, name);
    }

    /**
     * Initializes all required services
     */
    private void initializeServices() {
        ServiceManager serviceManager = ServiceManager.Instance();

        // 1. Initialize DAOs first
        InMemoryStockDataAccessObject stockDAO = new InMemoryStockDataAccessObject();
        InMemoryUserDataAccessObject userDAO = new InMemoryUserDataAccessObject();

        // Register concrete DAOs and their interfaces
        serviceManager.registerService(StockDataAccessInterface.class, stockDAO);
        StockMarket.Instance().initialize(stockDAO);

        serviceManager.registerService(InMemoryUserDataAccessObject.class, userDAO);
        serviceManager.registerService(ExecuteBuyDataAccessInterface.class, userDAO);
        serviceManager.registerService(ViewHistoryDataAccessInterface.class, userDAO);
        serviceManager.registerService(LoginDataAccessInterface.class, userDAO);

        // 2. Initialize Presenters and register them as output boundaries
        ExecuteBuyOutputBoundary buyPresenter = new ExecuteBuyPresenter();
        ViewHistoryOutputBoundary viewHistoryPresenter = new ViewHistoryPresenter();
        LoginOutputBoundary loginPresenter = new LoginPresenter();

        serviceManager.registerService(ExecuteBuyOutputBoundary.class, buyPresenter);
        serviceManager.registerService(ViewHistoryOutputBoundary.class, viewHistoryPresenter);
        serviceManager.registerService(LoginOutputBoundary.class, loginPresenter);

        // 3. Initialize Interactors and register them as input boundaries
        ExecuteBuyInputBoundary buyInteractor = new ExecuteBuyInteractor(
                serviceManager.getService(ExecuteBuyDataAccessInterface.class),
                serviceManager.getService(ExecuteBuyOutputBoundary.class)
        );
        ViewHistoryInputBoundary viewHistoryInteractor = new ViewHistoryInteractor(
                serviceManager.getService(ViewHistoryDataAccessInterface.class),
                serviceManager.getService(ViewHistoryOutputBoundary.class)
        );
        LoginInputBoundary loginInteractor = new LoginInteractor(
                serviceManager.getService(LoginDataAccessInterface.class),
                serviceManager.getService(LoginOutputBoundary.class)
        );

        serviceManager.registerService(ExecuteBuyInputBoundary.class, buyInteractor);
        serviceManager.registerService(ViewHistoryInputBoundary.class, viewHistoryInteractor);
        serviceManager.registerService(LoginInputBoundary.class, loginInteractor);

        // 4. Initialize Controllers
        serviceManager.registerService(ExecuteBuyController.class, new ExecuteBuyController(
                serviceManager.getService(ExecuteBuyInputBoundary.class))
        );
        serviceManager.registerService(ViewHistoryController.class, new ViewHistoryController(
                serviceManager.getService(ViewHistoryInputBoundary.class))
        );
        serviceManager.registerService(LoginController.class, new LoginController(
                serviceManager.getService(LoginInputBoundary.class))
        );
    }

    /**
     * Builds and returns the configured application frame
     */
    public JFrame build() {
        // Initialize services
        initializeServices();

        // Configure view manager
        ViewManager.Instance().setCardLayout(cardLayout, cardPanel);

        // Create and configure the main frame
        JFrame application = new JFrame(title);
        application.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        application.setSize(width, height);
        application.add(cardPanel);

        // Show initial panel
        cardLayout.show(cardPanel, initialPanel);

        return application;
    }
}
