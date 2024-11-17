package use_case.execute_buy;

import entity.Stock;
import entity.User;
import entity.UserFactory;
import entity.UserStock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import utility.StockMarket;
import utility.ViewManager;
import utility.exceptions.ValidationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ExecuteBuyInteractorTest {

    private ExecuteBuyDataAccessInterface dataAccess;
    private ExecuteBuyOutputBoundary outputPresenter;
    private UserFactory userFactory;
    private StockMarket stockMarketMock;
    private ViewManager viewManagerMock;

    @BeforeEach
    void setUp() {
        userFactory = new UserFactory();
        dataAccess = mock(ExecuteBuyDataAccessInterface.class);
        outputPresenter = mock(ExecuteBuyOutputBoundary.class);
        stockMarketMock = mock(StockMarket.class);
        viewManagerMock = mock(ViewManager.class);
    }

    @Test
    void successTest() throws ValidationException {
        User mockUser = createMockUserWithBalance(10000.0);
        Stock stock = new Stock("AAPL", "Apple Inc.", "Technology", 150.0);

        try (MockedStatic<StockMarket> stockMarketMockedStatic = Mockito.mockStatic(StockMarket.class)) {
            stockMarketMockedStatic.when(StockMarket::Instance).thenReturn(stockMarketMock);
            when(stockMarketMock.getStock("AAPL")).thenReturn(Optional.of(stock));

            ExecuteBuyInputData inputData = new ExecuteBuyInputData("dummy", "AAPL", 10);
            ExecuteBuyInteractor interactor = new ExecuteBuyInteractor(dataAccess, outputPresenter);

            interactor.execute(inputData);

            // Verify success view was prepared with updated data
            verify(outputPresenter).prepareSuccessView(any(ExecuteBuyOutputData.class));

            // Verify portfolio was updated
            Optional<UserStock> userStockOpt = mockUser.getPortfolio().getUserStock("AAPL");
            assertTrue(userStockOpt.isPresent(), "Portfolio should contain the ticker AAPL");
            assertEquals(10, userStockOpt.get().getQuantity(), "Stock quantity should match");

            // Verify balance was deducted
            assertEquals(8500.0, mockUser.getBalance(), "Balance should be reduced by total cost");
        }
    }

    @Test
    void insufficientBalanceTest() throws ValidationException {
        User mockUser = createMockUserWithBalance(500.0);
        Stock stock = new Stock("AAPL", "Apple Inc.", "Technology", 150.0);

        try (MockedStatic<StockMarket> stockMarketMockedStatic = Mockito.mockStatic(StockMarket.class)) {
            stockMarketMockedStatic.when(StockMarket::Instance).thenReturn(stockMarketMock);
            when(stockMarketMock.getStock("AAPL")).thenReturn(Optional.of(stock));

            ExecuteBuyInputData inputData = new ExecuteBuyInputData("dummy", "AAPL", 10);
            ExecuteBuyInteractor interactor = new ExecuteBuyInteractor(dataAccess, outputPresenter);

            interactor.execute(inputData);

            // Verify error view was prepared
            verify(outputPresenter).prepareInsufficientBalanceExceptionView();

            // Verify no changes were made
            assertFalse(mockUser.getPortfolio().getUserStock("AAPL").isPresent(),
                    "Stock should not be in portfolio due to insufficient funds");
            assertEquals(500.0, mockUser.getBalance(), "Balance should remain unchanged");
            assertTrue(mockUser.getTransactionHistory().getAllTransactions().isEmpty(),
                    "No transaction should be recorded");
        }
    }

    private User createMockUserWithBalance(double balance) throws ValidationException {
        User user = userFactory.create("testUser", "password");
        user.addBalance(balance);
        when(dataAccess.getUserWithCredential("dummy")).thenReturn(user);
        return user;
    }
}
