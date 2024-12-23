package fund.data.assets.controller;

import com.fasterxml.jackson.core.type.TypeReference;

import fund.data.assets.TestUtils;
import fund.data.assets.config.SpringConfigForTests;
import fund.data.assets.dto.asset.exchange.FirstBuyFixedRateBondDTO;
import fund.data.assets.model.asset.exchange.FixedRateBondPackage;
import fund.data.assets.model.asset.relationship.FinancialAssetRelationship;
import fund.data.assets.model.financial_entities.Account;
import fund.data.assets.model.owner.AssetsOwner;
import fund.data.assets.model.owner.RussianAssetsOwner;
import fund.data.assets.repository.AccountCashRepository;
import fund.data.assets.repository.AccountRepository;
import fund.data.assets.repository.RussianAssetsOwnerRepository;
import fund.data.assets.repository.TurnoverCommissionValueRepository;
import fund.data.assets.repository.FixedRateBondRepository;
import fund.data.assets.repository.FinancialAssetRelationshipRepository;
import fund.data.assets.utils.enums.AssetCurrency;
import fund.data.assets.utils.enums.CommissionSystem;
import fund.data.assets.utils.enums.TaxSystem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static fund.data.assets.TestUtils.asJson;
import static fund.data.assets.TestUtils.fromJson;
import static fund.data.assets.TestUtils.TEST_FIRST_RUSSIAN_OWNER_CASH_AMOUNT;
import static fund.data.assets.TestUtils.TEST_SECOND_RUSSIAN_OWNER_CASH_AMOUNT;
import static fund.data.assets.TestUtils.TEST_DECIMAL_FORMAT;
import static fund.data.assets.TestUtils.TEST_FIXED_RATE_BOND_LAST_ASSET_SELL_DATE;
import static fund.data.assets.config.SecurityConfig.ADMIN_NAME;
import static fund.data.assets.config.SpringConfigForTests.TEST_PROFILE;
import static fund.data.assets.controller.FixedRateBondPackageController.FIXED_RATE_BOND_CONTROLLER_PATH;
import static fund.data.assets.controller.FixedRateBondPackageController.BUY_PATH;
import static fund.data.assets.controller.FixedRateBondPackageController.REDEEM_PATH;
import static fund.data.assets.controller.RussianAssetsOwnerController.ID_PATH;

import static org.assertj.core.api.Assertions.assertThat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @version 0.1-b
 * @author MarkDementev a.k.a JavaMarkDem
 */
@SpringBootTest(classes = SpringConfigForTests.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles(TEST_PROFILE)
@AutoConfigureMockMvc
public class FixedRateBondControllerIT {
    @Autowired
    private TestUtils testUtils;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private RussianAssetsOwnerRepository russianAssetsOwnerRepository;
    @Autowired
    private AccountCashRepository accountCashRepository;
    @Autowired
    private TurnoverCommissionValueRepository turnoverCommissionValueRepository;
    @Autowired
    private FixedRateBondRepository fixedRateBondRepository;
    @Autowired
    private FinancialAssetRelationshipRepository financialAssetRelationshipRepository;

    @BeforeEach
    public void loginBeforeTest() throws Exception {
        testUtils.login(testUtils.getAdminLoginDto());
    }

    @AfterEach
    public void clearRepositories() {
        testUtils.tearDown();
    }

    @Test
    public void getFixedRateBondIT() throws Exception {
        testUtils.createDefaultFixedRateBond();

        FixedRateBondPackage expectedFixedRateBond = fixedRateBondRepository.findAll().get(0);
        var response = testUtils.perform(
                get("/data" + FIXED_RATE_BOND_CONTROLLER_PATH + ID_PATH,
                        expectedFixedRateBond.getId()),
                        ADMIN_NAME
                ).andExpect(status().isOk())
                .andReturn()
                .getResponse();
        FixedRateBondPackage fixedRateBondPackageFromResponse = fromJson(response.getContentAsString(),
                new TypeReference<>() {});

        /*
        Идёт проверка только полей созданного бонда, т.к. бонд для тестов имеет дефолтный DTO, и поля связанных с ним
        сущностей тестируются в специализированных интеграционных тестах создания бонда.
         */
        assertNotNull(fixedRateBondPackageFromResponse.getId());
        assertEquals(expectedFixedRateBond.getAssetCurrency(), fixedRateBondPackageFromResponse.getAssetCurrency());
        assertEquals(FixedRateBondPackage.class.getSimpleName(), fixedRateBondPackageFromResponse.getAssetTypeName());
        assertEquals(expectedFixedRateBond.getAssetTitle(), fixedRateBondPackageFromResponse.getAssetTitle());
        assertEquals(expectedFixedRateBond.getAssetCount(), fixedRateBondPackageFromResponse.getAssetCount());
        assertEquals(TaxSystem.EQUAL_COUPON_DIVIDEND_TRADE, fixedRateBondPackageFromResponse.getAssetTaxSystem());
        assertNotNull(fixedRateBondPackageFromResponse.getCreatedAt());
        assertNotNull(fixedRateBondPackageFromResponse.getUpdatedAt());
        assertEquals(expectedFixedRateBond.getISIN(), fixedRateBondPackageFromResponse.getISIN());
        assertEquals(expectedFixedRateBond.getAssetIssuerTitle(),
                fixedRateBondPackageFromResponse.getAssetIssuerTitle());
        assertNotNull(fixedRateBondPackageFromResponse.getLastAssetBuyOrSellDate());
        assertEquals(CommissionSystem.TURNOVER, fixedRateBondPackageFromResponse.getAssetCommissionSystem());
        assertEquals(expectedFixedRateBond.getBondParValue(), fixedRateBondPackageFromResponse.getBondParValue());
        assertEquals(expectedFixedRateBond.getPurchaseBondParValuePercent(),
                fixedRateBondPackageFromResponse.getPurchaseBondParValuePercent());
        assertEquals(expectedFixedRateBond.getBondsAccruedInterest(),
                fixedRateBondPackageFromResponse.getBondsAccruedInterest());
        assertEquals(300.00F, fixedRateBondPackageFromResponse.getTotalCommissionForPurchase());
        assertEquals(30300.00F, fixedRateBondPackageFromResponse.getTotalAssetPurchasePriceWithCommission());
        assertEquals(expectedFixedRateBond.getBondCouponValue(),
                fixedRateBondPackageFromResponse.getBondCouponValue());
        assertEquals(expectedFixedRateBond.getExpectedBondCouponPaymentsCount(),
                fixedRateBondPackageFromResponse.getExpectedBondCouponPaymentsCount());
        assertEquals(expectedFixedRateBond.getBondMaturityDate(),
                fixedRateBondPackageFromResponse.getBondMaturityDate());
        assertEquals(10.00F, fixedRateBondPackageFromResponse.getSimpleYieldToMaturity());
        assertEquals(7.6238F, Float.parseFloat(TEST_DECIMAL_FORMAT.format(
                fixedRateBondPackageFromResponse.getMarkDementevYieldIndicator())));
    }

    @Test
    public void getAllFixedRateBondsIT() throws Exception {
        testUtils.createDefaultFixedRateBond();

        var response = testUtils.perform(
                    get("/data" + FIXED_RATE_BOND_CONTROLLER_PATH),
                        ADMIN_NAME
                ).andExpect(status().isOk())
                .andReturn()
                .getResponse();
        List<FixedRateBondPackage> allFixedRateBondsFromResponse = fromJson(response.getContentAsString(),
                new TypeReference<>() {});

        assertThat(allFixedRateBondsFromResponse).hasSize(1);
    }

    @Test
    public void firstBuyFixedRateBondCheckBondFieldsIT() throws Exception {
        final FirstBuyFixedRateBondDTO firstBuyFixedRateBondDTO = testUtils.getFirstBuyFixedRateBondDTO();
        var response = testUtils.perform(
                post("/data" + FIXED_RATE_BOND_CONTROLLER_PATH)
                        .content(asJson(firstBuyFixedRateBondDTO))
                        .contentType(APPLICATION_JSON),
                        ADMIN_NAME
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse();
        FixedRateBondPackage fixedRateBondPackageFromResponse = fromJson(response.getContentAsString(),
                new TypeReference<>() {});

        assertNotNull(fixedRateBondPackageFromResponse.getId());
        assertEquals(firstBuyFixedRateBondDTO.getAssetCurrency(), fixedRateBondPackageFromResponse.getAssetCurrency());
        assertEquals(FixedRateBondPackage.class.getSimpleName(), fixedRateBondPackageFromResponse.getAssetTypeName());
        assertEquals(firstBuyFixedRateBondDTO.getAssetTitle(), fixedRateBondPackageFromResponse.getAssetTitle());
        assertEquals(firstBuyFixedRateBondDTO.getAssetCount(), fixedRateBondPackageFromResponse.getAssetCount());
        assertEquals(TaxSystem.EQUAL_COUPON_DIVIDEND_TRADE, fixedRateBondPackageFromResponse.getAssetTaxSystem());
        assertNotNull(fixedRateBondPackageFromResponse.getCreatedAt());
        assertNotNull(fixedRateBondPackageFromResponse.getUpdatedAt());
        assertEquals(firstBuyFixedRateBondDTO.getISIN(), fixedRateBondPackageFromResponse.getISIN());
        assertEquals(firstBuyFixedRateBondDTO.getAssetIssuerTitle(),
                fixedRateBondPackageFromResponse.getAssetIssuerTitle());
        assertNotNull(fixedRateBondPackageFromResponse.getLastAssetBuyOrSellDate());
        assertEquals(CommissionSystem.TURNOVER, fixedRateBondPackageFromResponse.getAssetCommissionSystem());
        assertEquals(firstBuyFixedRateBondDTO.getBondParValue(), fixedRateBondPackageFromResponse.getBondParValue());
        assertEquals(firstBuyFixedRateBondDTO.getPurchaseBondParValuePercent(),
                fixedRateBondPackageFromResponse.getPurchaseBondParValuePercent());
        assertEquals(firstBuyFixedRateBondDTO.getBondsAccruedInterest(),
                fixedRateBondPackageFromResponse.getBondsAccruedInterest());
        assertEquals(300.00F, fixedRateBondPackageFromResponse.getTotalCommissionForPurchase());
        assertEquals(30300.00F, fixedRateBondPackageFromResponse.getTotalAssetPurchasePriceWithCommission());
        assertEquals(firstBuyFixedRateBondDTO.getBondCouponValue(),
                fixedRateBondPackageFromResponse.getBondCouponValue());
        assertEquals(firstBuyFixedRateBondDTO.getExpectedBondCouponPaymentsCount(),
                fixedRateBondPackageFromResponse.getExpectedBondCouponPaymentsCount());
        assertEquals(firstBuyFixedRateBondDTO.getBondMaturityDate(),
                fixedRateBondPackageFromResponse.getBondMaturityDate());
        assertEquals(10.00F, fixedRateBondPackageFromResponse.getSimpleYieldToMaturity());
        assertEquals(7.6238F, Float.parseFloat(TEST_DECIMAL_FORMAT.format(
                fixedRateBondPackageFromResponse.getMarkDementevYieldIndicator())));
    }

    @Test
    public void firstBuyFixedRateBondCheckRelationshipFieldsIT() throws Exception {
        final FirstBuyFixedRateBondDTO firstBuyFixedRateBondDTO = testUtils.getFirstBuyFixedRateBondDTO();
        Map<String, Float> assetOwnersWithAssetCountsMapFromDTO
                = firstBuyFixedRateBondDTO.getAssetOwnersWithAssetCounts();
        var response = testUtils.perform(
                post("/data" + FIXED_RATE_BOND_CONTROLLER_PATH)
                        .content(asJson(firstBuyFixedRateBondDTO))
                        .contentType(APPLICATION_JSON),
                        ADMIN_NAME
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse();
        FixedRateBondPackage fixedRateBondPackageFromResponse = fromJson(response.getContentAsString(),
                new TypeReference<>() {});

        assertNotNull(fixedRateBondPackageFromResponse.getAssetRelationship());
        assertEquals(fixedRateBondPackageFromResponse.getAssetRelationship().getClass(),
                FinancialAssetRelationship.class);

        FinancialAssetRelationship financialAssetRelationship
                = (FinancialAssetRelationship) fixedRateBondPackageFromResponse.getAssetRelationship();

        assertNotNull(fixedRateBondPackageFromResponse.getAssetRelationship().getId());
        assertEquals(fixedRateBondPackageFromResponse.getId(), financialAssetRelationship.getAssetId());
        assertNotNull(financialAssetRelationship.getAssetOwnersWithAssetCounts());

        for (Map.Entry<String, Float> element : financialAssetRelationship.getAssetOwnersWithAssetCounts().entrySet()) {
            String elementKey = element.getKey();
            Float elementValue = element.getValue();

            assertEquals(elementValue, assetOwnersWithAssetCountsMapFromDTO.get(elementKey));
        }
        assertNotNull(financialAssetRelationship.getCreatedAt());
        assertNotNull(financialAssetRelationship.getUpdatedAt());
        assertEquals(accountRepository.findAll().get(0).getId(), financialAssetRelationship.getAccount().getId());
    }

    @Test
    public void firstBuyFixedRateBondCheckAccountCashesIT() throws Exception {
        final FirstBuyFixedRateBondDTO firstBuyFixedRateBondDTO = testUtils.getFirstBuyFixedRateBondDTO();
        Map<String, Float> assetOwnersWithAssetCountsMapFromDTO
                = firstBuyFixedRateBondDTO.getAssetOwnersWithAssetCounts();
        Map<String, Float> correctAccountCashAmounts = new TreeMap<>();

        /*
        Наполнили мапу айдишниками оунеров и значениями количества денег на нужных аккаунтах.
         */
        for (Map.Entry<String, Float> element : assetOwnersWithAssetCountsMapFromDTO.entrySet()) {
            Account account = accountRepository.findById(firstBuyFixedRateBondDTO.getAccountID()).orElseThrow();
            String assetsOwnerID = element.getKey();
            AssetsOwner assetsOwner = russianAssetsOwnerRepository
                    .findById(Long.parseLong(assetsOwnerID)).orElseThrow();
            Float accountCashAmount = accountCashRepository
                    .findByAccountAndAssetCurrencyAndAssetsOwner(account, firstBuyFixedRateBondDTO.getAssetCurrency(),
                            assetsOwner).getAmount();

            correctAccountCashAmounts.put(assetsOwnerID, accountCashAmount);
        }

        /*
        Вручную уменьшили значения в этой мапе, чтобы потом сверить их с теми, что получатся при запросе.
         */
        for (Map.Entry<String, Float> element : assetOwnersWithAssetCountsMapFromDTO.entrySet()) {
            String assetsOwnerID = element.getKey();
            Float amountToChangeValue = element.getValue();
            Float newValueToPreviousCreatedMap = correctAccountCashAmounts.get(assetsOwnerID)
                    - amountToChangeValue * firstBuyFixedRateBondDTO.getBondParValue()
                    - amountToChangeValue * firstBuyFixedRateBondDTO.getBondParValue()
                        * turnoverCommissionValueRepository.findAll().get(0).getCommissionPercentValue();

            correctAccountCashAmounts.put(assetsOwnerID, newValueToPreviousCreatedMap);
        }

        testUtils.perform(
                post("/data" + FIXED_RATE_BOND_CONTROLLER_PATH)
                        .content(asJson(firstBuyFixedRateBondDTO))
                        .contentType(APPLICATION_JSON),
                        ADMIN_NAME
                )
                .andExpect(status().isCreated());

        /*
        Проходимся по мапе с корректными значениями, и сверяем со значениями в репозитории, чтобы понять, корректно ли
        изменились значения там после выполнения запроса на создание пакета облигаций.
         */
        for (Map.Entry<String, Float> element : correctAccountCashAmounts.entrySet()) {
            Account account = accountRepository.findById(firstBuyFixedRateBondDTO.getAccountID()).orElseThrow();
            AssetCurrency assetCurrency = firstBuyFixedRateBondDTO.getAssetCurrency();
            Long accountCashOwnerID = Long.valueOf(element.getKey());
            RussianAssetsOwner assetsOwner = russianAssetsOwnerRepository.findById(accountCashOwnerID).orElseThrow();

            assertEquals(element.getValue(), accountCashRepository.findByAccountAndAssetCurrencyAndAssetsOwner(account,
                            assetCurrency, assetsOwner).getAmount());
        }
    }

    @Test
    public void firstBuyNotValidFixedRateBondIT() throws Exception {
        final FirstBuyFixedRateBondDTO notValidFirstBuyFixedRateBondDTO
                = testUtils.getNotValidFirstBuyFixedRateBondDTO();

        assertThat(fixedRateBondRepository.findAll()).hasSize(0);
        assertThat(financialAssetRelationshipRepository.findAll()).hasSize(0);
        assertEquals(TEST_FIRST_RUSSIAN_OWNER_CASH_AMOUNT, accountCashRepository.findAll().get(0).getAmount());
        assertEquals(TEST_SECOND_RUSSIAN_OWNER_CASH_AMOUNT, accountCashRepository.findAll().get(1).getAmount());

        Exception exception = testUtils.perform(
                post("/data" + FIXED_RATE_BOND_CONTROLLER_PATH)
                        .content(asJson(notValidFirstBuyFixedRateBondDTO))
                        .contentType(APPLICATION_JSON),
                ADMIN_NAME
        ).andExpect(status().isBadRequest()).andReturn().getResolvedException();

        assert exception != null;
        assertEquals(MethodArgumentNotValidException.class, exception.getClass());
        assertThat(fixedRateBondRepository.findAll()).hasSize(0);
        assertThat(financialAssetRelationshipRepository.findAll()).hasSize(0);
        assertEquals(TEST_FIRST_RUSSIAN_OWNER_CASH_AMOUNT, accountCashRepository.findAll().get(0).getAmount());
        assertEquals(TEST_SECOND_RUSSIAN_OWNER_CASH_AMOUNT, accountCashRepository.findAll().get(1).getAmount());
    }

    @Test
    public void partialBuyFixedRateBondIT() throws Exception {
        testUtils.createDefaultFixedRateBond();
        testUtils.addMoreMoneyToOwnersWhileBuyingMoreBonds();

        assertThat(fixedRateBondRepository.findAll()).hasSize(1);
        assertThat(financialAssetRelationshipRepository.findAll()).hasSize(1);
        assertEquals(30, fixedRateBondRepository.findAll().get(0).getAssetCount());

        Long createdFixedRateBondPackageId = fixedRateBondRepository.findAll().get(0).getId();
        var response = testUtils.perform(
                        put("/data" + FIXED_RATE_BOND_CONTROLLER_PATH + ID_PATH + BUY_PATH,
                                createdFixedRateBondPackageId)
                                .content(asJson(testUtils.getBuyFixedRateBondDTO()))
                                .contentType(APPLICATION_JSON),
                        ADMIN_NAME
                )
                .andExpect(status().isOk())
                .andReturn().getResponse();
        FixedRateBondPackage fixedRateBondPackageFromResponse = fromJson(response.getContentAsString(),
                new TypeReference<>() {});

        assertThat(fixedRateBondRepository.findAll()).hasSize(1);
        assertThat(financialAssetRelationshipRepository.findAll()).hasSize(1);
        assertEquals(createdFixedRateBondPackageId, fixedRateBondPackageFromResponse.getId());
        assertEquals(33, fixedRateBondPackageFromResponse.getAssetCount());
        assertEquals(11, fixedRateBondPackageFromResponse.getAssetRelationship()
                .getAssetOwnersWithAssetCounts().get(String.valueOf(russianAssetsOwnerRepository.findAll().get(0)
                        .getId())));
        assertEquals(22, fixedRateBondPackageFromResponse.getAssetRelationship()
                .getAssetOwnersWithAssetCounts().get(String.valueOf(russianAssetsOwnerRepository.findAll().get(1)
                        .getId())));
        assertNotEquals(fixedRateBondPackageFromResponse.getAssetRelationship().getCreatedAt(),
                fixedRateBondPackageFromResponse.getAssetRelationship().getUpdatedAt());
        assertEquals(TEST_FIXED_RATE_BOND_LAST_ASSET_SELL_DATE,
                fixedRateBondPackageFromResponse.getLastAssetBuyOrSellDate());
        assertEquals(99.9091F, Float.parseFloat(TEST_DECIMAL_FORMAT.format(
                        fixedRateBondPackageFromResponse.getPurchaseBondParValuePercent())));
        assertEquals(10.00F, fixedRateBondPackageFromResponse.getBondsAccruedInterest());
        assertEquals(329.8F, Float.parseFloat(TEST_DECIMAL_FORMAT.format(
                fixedRateBondPackageFromResponse.getTotalCommissionForPurchase())));
        assertEquals(33309.8F, fixedRateBondPackageFromResponse.getTotalAssetPurchasePriceWithCommission());
        assertEquals(1, fixedRateBondPackageFromResponse.getExpectedBondCouponPaymentsCount());
        assertEquals(10.6531F, Float.parseFloat(TEST_DECIMAL_FORMAT.format(
                        fixedRateBondPackageFromResponse.getSimpleYieldToMaturity())));
        assertEquals(8.2212F, Float.parseFloat(TEST_DECIMAL_FORMAT.format(
                fixedRateBondPackageFromResponse.getMarkDementevYieldIndicator())));
        assertNotEquals(fixedRateBondPackageFromResponse.getCreatedAt(),
                fixedRateBondPackageFromResponse.getUpdatedAt());
        assertEquals(9196.733F, accountCashRepository.findByAccountAndAssetCurrencyAndAssetsOwner(
                accountRepository.findAll().get(0),
                AssetCurrency.RUSRUB,
                russianAssetsOwnerRepository.findAll().get(0)
        ).getAmount());
        assertEquals(18393.467F, accountCashRepository.findByAccountAndAssetCurrencyAndAssetsOwner(
                accountRepository.findAll().get(0),
                AssetCurrency.RUSRUB,
                russianAssetsOwnerRepository.findAll().get(1)
        ).getAmount());
    }

    @Test
    public void partialBuyFixedRateBondNotAllOwnersAndAnotherPriceIT() throws Exception {
        testUtils.createDefaultFixedRateBond();
        testUtils.addMoreMoneyToOwnersWhileBuyingMoreBonds();

        assertThat(fixedRateBondRepository.findAll()).hasSize(1);
        assertThat(financialAssetRelationshipRepository.findAll()).hasSize(1);
        assertEquals(30, fixedRateBondRepository.findAll().get(0).getAssetCount());

        Long createdFixedRateBondPackageId = fixedRateBondRepository.findAll().get(0).getId();
        var response = testUtils.perform(
                        put("/data" + FIXED_RATE_BOND_CONTROLLER_PATH + ID_PATH + BUY_PATH,
                                createdFixedRateBondPackageId)
                                .content(asJson(testUtils.getBuyFixedRateBondNotAllOwnersDTO()))
                                .contentType(APPLICATION_JSON),
                        ADMIN_NAME
                )
                .andExpect(status().isOk())
                .andReturn().getResponse();
        FixedRateBondPackage fixedRateBondPackageFromResponse = fromJson(response.getContentAsString(),
                new TypeReference<>() {});

        assertThat(fixedRateBondRepository.findAll()).hasSize(1);
        assertThat(financialAssetRelationshipRepository.findAll()).hasSize(1);
        assertEquals(createdFixedRateBondPackageId, fixedRateBondPackageFromResponse.getId());
        assertEquals(35, fixedRateBondPackageFromResponse.getAssetCount());
        assertEquals(10, fixedRateBondPackageFromResponse.getAssetRelationship()
                .getAssetOwnersWithAssetCounts().get(String.valueOf(russianAssetsOwnerRepository.findAll().get(0)
                        .getId())));
        assertEquals(25, fixedRateBondPackageFromResponse.getAssetRelationship()
                .getAssetOwnersWithAssetCounts().get(String.valueOf(russianAssetsOwnerRepository.findAll().get(1)
                        .getId())));
        assertNotEquals(fixedRateBondPackageFromResponse.getAssetRelationship().getCreatedAt(),
                fixedRateBondPackageFromResponse.getAssetRelationship().getUpdatedAt());
        assertEquals(TEST_FIXED_RATE_BOND_LAST_ASSET_SELL_DATE,
                fixedRateBondPackageFromResponse.getLastAssetBuyOrSellDate());
        assertEquals(99.2857F, Float.parseFloat(TEST_DECIMAL_FORMAT.format(
                fixedRateBondPackageFromResponse.getPurchaseBondParValuePercent())));
        assertEquals(50.00F, fixedRateBondPackageFromResponse.getBondsAccruedInterest());
        assertEquals(348.0F, Float.parseFloat(TEST_DECIMAL_FORMAT.format(
                fixedRateBondPackageFromResponse.getTotalCommissionForPurchase())));
        assertEquals(35148.0F, fixedRateBondPackageFromResponse.getTotalAssetPurchasePriceWithCommission());
        assertEquals(1, fixedRateBondPackageFromResponse.getExpectedBondCouponPaymentsCount());
        assertEquals(11.0971F, Float.parseFloat(TEST_DECIMAL_FORMAT.format(
                fixedRateBondPackageFromResponse.getSimpleYieldToMaturity())));
        assertEquals(9.3248F, Float.parseFloat(TEST_DECIMAL_FORMAT.format(
                fixedRateBondPackageFromResponse.getMarkDementevYieldIndicator())));
        assertNotEquals(fixedRateBondPackageFromResponse.getCreatedAt(),
                fixedRateBondPackageFromResponse.getUpdatedAt());
        assertEquals(10200.0F, accountCashRepository.findByAccountAndAssetCurrencyAndAssetsOwner(
                accountRepository.findAll().get(0),
                AssetCurrency.RUSRUB,
                russianAssetsOwnerRepository.findAll().get(0)
        ).getAmount());
        assertEquals(15552.0F, accountCashRepository.findByAccountAndAssetCurrencyAndAssetsOwner(
                accountRepository.findAll().get(0),
                AssetCurrency.RUSRUB,
                russianAssetsOwnerRepository.findAll().get(1)
        ).getAmount());
    }

    @Test
    public void partialBuyFixedRateBondWithNewOwnerIT() throws Exception {
        testUtils.createDefaultFixedRateBond();
        testUtils.addMoreMoneyToOwnersWhileBuyingMoreBonds();
        testUtils.addThirdRussianAssetsOwnerWithMoney();

        assertThat(fixedRateBondRepository.findAll()).hasSize(1);
        assertThat(financialAssetRelationshipRepository.findAll()).hasSize(1);
        assertEquals(30, fixedRateBondRepository.findAll().get(0).getAssetCount());

        Long createdFixedRateBondPackageId = fixedRateBondRepository.findAll().get(0).getId();
        var response = testUtils.perform(
                        put("/data" + FIXED_RATE_BOND_CONTROLLER_PATH + ID_PATH + BUY_PATH,
                                createdFixedRateBondPackageId)
                                .content(asJson(testUtils.getBuyWithNewOwnerFixedRateBondDTO()))
                                .contentType(APPLICATION_JSON),
                        ADMIN_NAME
                )
                .andExpect(status().isOk())
                .andReturn().getResponse();
        FixedRateBondPackage fixedRateBondPackageFromResponse = fromJson(response.getContentAsString(),
                new TypeReference<>() {});

        assertThat(fixedRateBondRepository.findAll()).hasSize(1);
        assertThat(financialAssetRelationshipRepository.findAll()).hasSize(1);
        assertEquals(createdFixedRateBondPackageId, fixedRateBondPackageFromResponse.getId());
        assertEquals(37, fixedRateBondPackageFromResponse.getAssetCount());
        assertEquals(11, fixedRateBondPackageFromResponse.getAssetRelationship()
                .getAssetOwnersWithAssetCounts().get(String.valueOf(russianAssetsOwnerRepository.findAll().get(0)
                        .getId())));
        assertEquals(22, fixedRateBondPackageFromResponse.getAssetRelationship()
                .getAssetOwnersWithAssetCounts().get(String.valueOf(russianAssetsOwnerRepository.findAll().get(1)
                        .getId())));
        assertEquals(4, fixedRateBondPackageFromResponse.getAssetRelationship()
                .getAssetOwnersWithAssetCounts().get(String.valueOf(russianAssetsOwnerRepository.findAll().get(2)
                        .getId())));
        assertNotEquals(fixedRateBondPackageFromResponse.getAssetRelationship().getCreatedAt(),
                fixedRateBondPackageFromResponse.getAssetRelationship().getUpdatedAt());
        assertEquals(TEST_FIXED_RATE_BOND_LAST_ASSET_SELL_DATE,
                fixedRateBondPackageFromResponse.getLastAssetBuyOrSellDate());
        assertEquals(99.8108F, Float.parseFloat(TEST_DECIMAL_FORMAT.format(
                fixedRateBondPackageFromResponse.getPurchaseBondParValuePercent())));
        assertEquals(10.00F, fixedRateBondPackageFromResponse.getBondsAccruedInterest());
        assertEquals(369.3999F, Float.parseFloat(TEST_DECIMAL_FORMAT.format(
                fixedRateBondPackageFromResponse.getTotalCommissionForPurchase())));
        assertEquals(37309.4F, fixedRateBondPackageFromResponse.getTotalAssetPurchasePriceWithCommission());
        assertEquals(1, fixedRateBondPackageFromResponse.getExpectedBondCouponPaymentsCount());
        assertEquals(11.3580F, Float.parseFloat(TEST_DECIMAL_FORMAT.format(
                fixedRateBondPackageFromResponse.getSimpleYieldToMaturity())));
        assertEquals(8.9326F, Float.parseFloat(TEST_DECIMAL_FORMAT.format(
                fixedRateBondPackageFromResponse.getMarkDementevYieldIndicator())));
        assertNotEquals(fixedRateBondPackageFromResponse.getCreatedAt(),
                fixedRateBondPackageFromResponse.getUpdatedAt());
        assertEquals(9198.657F, accountCashRepository.findByAccountAndAssetCurrencyAndAssetsOwner(
                accountRepository.findAll().get(0),
                AssetCurrency.RUSRUB,
                russianAssetsOwnerRepository.findAll().get(0)
        ).getAmount());
        assertEquals(18397.3143F, accountCashRepository.findByAccountAndAssetCurrencyAndAssetsOwner(
                accountRepository.findAll().get(0),
                AssetCurrency.RUSRUB,
                russianAssetsOwnerRepository.findAll().get(1)
        ).getAmount());
        assertEquals(6144.628F, accountCashRepository.findByAccountAndAssetCurrencyAndAssetsOwner(
                accountRepository.findAll().get(0),
                AssetCurrency.RUSRUB,
                russianAssetsOwnerRepository.findAll().get(2)
        ).getAmount());
    }

    @Test
    public void partialBuyFixedRateBondNotEnoughMoneyIT() throws Exception {
        testUtils.createDefaultFixedRateBond();

        assertThat(fixedRateBondRepository.findAll()).hasSize(1);
        assertThat(financialAssetRelationshipRepository.findAll()).hasSize(1);
        assertEquals(30, fixedRateBondRepository.findAll().get(0).getAssetCount());

        Long createdFixedRateBondPackageId = fixedRateBondRepository.findAll().get(0).getId();
        Exception exception = testUtils.perform(
                        put("/data" + FIXED_RATE_BOND_CONTROLLER_PATH + ID_PATH + BUY_PATH,
                                createdFixedRateBondPackageId)
                                .content(asJson(testUtils.getBuyFixedRateBondDTO()))
                                .contentType(APPLICATION_JSON),
                        ADMIN_NAME
                ).andExpect(status().isBadRequest())
                .andReturn().getResolvedException();

        assert exception != null;
        assertEquals(IllegalArgumentException.class, exception.getClass());
        assertThat(fixedRateBondRepository.findAll()).hasSize(1);
        assertThat(financialAssetRelationshipRepository.findAll()).hasSize(1);
        assertEquals(30, fixedRateBondRepository.findAll().get(0).getAssetCount());
    }

    @Test
    public void partialBuyFixedRateBondNotValidTaxResidencyIT() throws Exception {
        testUtils.createDefaultFixedRateBond();

        assertThat(fixedRateBondRepository.findAll()).hasSize(1);
        assertThat(financialAssetRelationshipRepository.findAll()).hasSize(1);
        assertEquals(30, fixedRateBondRepository.findAll().get(0).getAssetCount());

        Long createdFixedRateBondPackageId = fixedRateBondRepository.findAll().get(0).getId();
        Exception exception = testUtils.perform(
                put("/data" + FIXED_RATE_BOND_CONTROLLER_PATH + ID_PATH + BUY_PATH,
                        createdFixedRateBondPackageId)
                        .content(asJson(testUtils.getBuyFixedRateBondNotValidTaxResidencyDTO()))
                        .contentType(APPLICATION_JSON),
                ADMIN_NAME
        ).andExpect(status().isBadRequest()).andReturn().getResolvedException();

        assert exception != null;
        assertEquals(IllegalArgumentException.class, exception.getClass());
        assertThat(fixedRateBondRepository.findAll()).hasSize(1);
        assertThat(financialAssetRelationshipRepository.findAll()).hasSize(1);
        assertEquals(30, fixedRateBondRepository.findAll().get(0).getAssetCount());
    }

    @Test
    public void partialSellForAllOwnersFixedRateBondPackageIT() throws Exception {
        testUtils.createDefaultFixedRateBond();

        assertThat(fixedRateBondRepository.findAll()).hasSize(1);
        assertThat(financialAssetRelationshipRepository.findAll()).hasSize(1);
        assertEquals(30, fixedRateBondRepository.findAll().get(0).getAssetCount());

        Long createdFixedRateBondPackageId = fixedRateBondRepository.findAll().get(0).getId();
        var response = testUtils.perform(
                        put("/data" + FIXED_RATE_BOND_CONTROLLER_PATH + ID_PATH,
                                createdFixedRateBondPackageId)
                                .content(asJson(testUtils.getPartialSellFixedRateBondPackageFirstDTO()))
                                .contentType(APPLICATION_JSON),
                        ADMIN_NAME
                )
                .andExpect(status().isOk())
                .andReturn().getResponse();
        FixedRateBondPackage fixedRateBondPackageFromResponse = fromJson(response.getContentAsString(),
                new TypeReference<>() {});

        assertThat(fixedRateBondRepository.findAll()).hasSize(1);
        assertThat(financialAssetRelationshipRepository.findAll()).hasSize(1);
        assertEquals(createdFixedRateBondPackageId, fixedRateBondPackageFromResponse.getId());
        assertEquals(15, fixedRateBondPackageFromResponse.getAssetCount());
        assertEquals(5, fixedRateBondPackageFromResponse.getAssetRelationship().getAssetOwnersWithAssetCounts()
                .get(String.valueOf(russianAssetsOwnerRepository.findAll().get(0).getId())));
        assertEquals(10, fixedRateBondPackageFromResponse.getAssetRelationship().getAssetOwnersWithAssetCounts()
                .get(String.valueOf(russianAssetsOwnerRepository.findAll().get(1).getId())));
        assertNotEquals(fixedRateBondPackageFromResponse.getAssetRelationship().getCreatedAt(),
                fixedRateBondPackageFromResponse.getAssetRelationship().getUpdatedAt());
        assertEquals(TEST_FIXED_RATE_BOND_LAST_ASSET_SELL_DATE,
                fixedRateBondPackageFromResponse.getLastAssetBuyOrSellDate());
        assertEquals(0.00F, fixedRateBondPackageFromResponse.getBondsAccruedInterest());
        assertEquals(150.00F, fixedRateBondPackageFromResponse.getTotalCommissionForPurchase());
        assertEquals(15150.00F, fixedRateBondPackageFromResponse.getTotalAssetPurchasePriceWithCommission());
        assertEquals(1, fixedRateBondPackageFromResponse.getExpectedBondCouponPaymentsCount());
        assertEquals(10.00F, fixedRateBondPackageFromResponse.getSimpleYieldToMaturity());
        assertEquals(7.6238F, Float.parseFloat(TEST_DECIMAL_FORMAT.format(
                fixedRateBondPackageFromResponse.getMarkDementevYieldIndicator())));
        assertNotEquals(fixedRateBondPackageFromResponse.getCreatedAt(),
                fixedRateBondPackageFromResponse.getUpdatedAt());
        assertEquals(10180.8010F, accountCashRepository.findByAccountAndAssetCurrencyAndAssetsOwner(
                accountRepository.findAll().get(0),
                AssetCurrency.RUSRUB,
                russianAssetsOwnerRepository.findAll().get(0)
        ).getAmount());
        assertEquals(20361.6020F, accountCashRepository.findByAccountAndAssetCurrencyAndAssetsOwner(
                accountRepository.findAll().get(0),
                AssetCurrency.RUSRUB,
                russianAssetsOwnerRepository.findAll().get(1)
        ).getAmount());
    }

    @Test
    public void partialSellForNotAllOwnersWithoutTaxesFixedRateBondPackageIT() throws Exception {
        testUtils.createDefaultFixedRateBond();

        assertThat(fixedRateBondRepository.findAll()).hasSize(1);
        assertThat(financialAssetRelationshipRepository.findAll()).hasSize(1);
        assertEquals(30, fixedRateBondRepository.findAll().get(0).getAssetCount());

        Long createdFixedRateBondPackageId = fixedRateBondRepository.findAll().get(0).getId();
        var response = testUtils.perform(
                        put("/data" + FIXED_RATE_BOND_CONTROLLER_PATH + ID_PATH,
                                createdFixedRateBondPackageId)
                                .content(asJson(testUtils.getPartialSellFixedRateBondPackageSecondDTO()))
                                .contentType(APPLICATION_JSON),
                        ADMIN_NAME
                )
                .andExpect(status().isOk())
                .andReturn().getResponse();
        FixedRateBondPackage fixedRateBondPackageFromResponse = fromJson(response.getContentAsString(),
                new TypeReference<>() {});

        assertThat(fixedRateBondRepository.findAll()).hasSize(1);
        assertThat(financialAssetRelationshipRepository.findAll()).hasSize(1);
        assertEquals(createdFixedRateBondPackageId, fixedRateBondPackageFromResponse.getId());
        assertEquals(12, fixedRateBondPackageFromResponse.getAssetCount());
        assertEquals(4, fixedRateBondPackageFromResponse.getAssetRelationship().getAssetOwnersWithAssetCounts()
                .get(String.valueOf(russianAssetsOwnerRepository.findAll().get(0).getId())));
        assertEquals(8, fixedRateBondPackageFromResponse.getAssetRelationship().getAssetOwnersWithAssetCounts()
                .get(String.valueOf(russianAssetsOwnerRepository.findAll().get(1).getId())));
        assertEquals(TEST_FIXED_RATE_BOND_LAST_ASSET_SELL_DATE,
                fixedRateBondPackageFromResponse.getLastAssetBuyOrSellDate());
        assertEquals(1, fixedRateBondPackageFromResponse.getExpectedBondCouponPaymentsCount());
        assertEquals(0.00F, fixedRateBondPackageFromResponse.getBondsAccruedInterest());
        assertEquals(120.00F, fixedRateBondPackageFromResponse.getTotalCommissionForPurchase());
        assertEquals(12120.00F, fixedRateBondPackageFromResponse.getTotalAssetPurchasePriceWithCommission());
        assertEquals(10.00F, fixedRateBondPackageFromResponse.getSimpleYieldToMaturity());
        assertEquals(7.6238F, Float.parseFloat(TEST_DECIMAL_FORMAT.format(
                fixedRateBondPackageFromResponse.getMarkDementevYieldIndicator())));
        assertNotEquals(fixedRateBondPackageFromResponse.getCreatedAt(),
                fixedRateBondPackageFromResponse.getUpdatedAt());
        assertEquals(3317.00F, accountCashRepository.findByAccountAndAssetCurrencyAndAssetsOwner(
                accountRepository.findAll().get(0),
                AssetCurrency.RUSRUB,
                russianAssetsOwnerRepository.findAll().get(0)
        ).getAmount());
        assertEquals(6634.00F, accountCashRepository.findByAccountAndAssetCurrencyAndAssetsOwner(
                accountRepository.findAll().get(0),
                AssetCurrency.RUSRUB,
                russianAssetsOwnerRepository.findAll().get(1)
        ).getAmount());
    }

    @Test
    public void partialSellFixedRateBondPackageNotEnoughAssetsIT() throws Exception {
        testUtils.createDefaultFixedRateBond();

        assertThat(fixedRateBondRepository.findAll()).hasSize(1);
        assertThat(financialAssetRelationshipRepository.findAll()).hasSize(1);
        assertEquals(30, fixedRateBondRepository.findAll().get(0).getAssetCount());

        Long createdFixedRateBondPackageId = fixedRateBondRepository.findAll().get(0).getId();
        Exception exception = testUtils.perform(
                        put("/data" + FIXED_RATE_BOND_CONTROLLER_PATH + ID_PATH,
                                createdFixedRateBondPackageId)
                                .content(asJson(testUtils.getPartialSellFixedRateBondPackageNotEnoughAssetsDTO()))
                                .contentType(APPLICATION_JSON),
                        ADMIN_NAME)
                .andExpect(status().isBadRequest())
                .andReturn().getResolvedException();

        assert exception != null;
        assertEquals(IllegalArgumentException.class, exception.getClass());
        assertThat(fixedRateBondRepository.findAll()).hasSize(1);
        assertThat(financialAssetRelationshipRepository.findAll()).hasSize(1);
        assertEquals(30, fixedRateBondRepository.findAll().get(0).getAssetCount());
    }

    @Test
    public void partialSellFixedRateBondPackageNotValidTaxResidencyIT() throws Exception {
        testUtils.createDefaultFixedRateBond();

        assertThat(fixedRateBondRepository.findAll()).hasSize(1);
        assertThat(financialAssetRelationshipRepository.findAll()).hasSize(1);
        assertEquals(30, fixedRateBondRepository.findAll().get(0).getAssetCount());

        Long createdFixedRateBondPackageId = fixedRateBondRepository.findAll().get(0).getId();
        Exception exception = testUtils.perform(
                        put("/data" + FIXED_RATE_BOND_CONTROLLER_PATH + ID_PATH,
                                createdFixedRateBondPackageId)
                                .content(asJson(testUtils.getPartialSellFixedRateBondPackageNotValidTaxResidencyDTO()))
                                .contentType(APPLICATION_JSON),
                        ADMIN_NAME)
                .andExpect(status().isBadRequest())
                .andReturn().getResolvedException();

        assert exception != null;
        assertEquals(IllegalArgumentException.class, exception.getClass());
        assertThat(fixedRateBondRepository.findAll()).hasSize(1);
        assertThat(financialAssetRelationshipRepository.findAll()).hasSize(1);
        assertEquals(30, fixedRateBondRepository.findAll().get(0).getAssetCount());
    }

    @Test
    public void sellAllPackageIT() throws Exception {
        testUtils.createDefaultFixedRateBond();
        assertThat(fixedRateBondRepository.findAll()).hasSize(1);
        assertThat(financialAssetRelationshipRepository.findAll()).hasSize(1);

        Long createdFixedRateBondID = fixedRateBondRepository.findAll().get(0).getId();

        testUtils.perform(
                delete("/data" + FIXED_RATE_BOND_CONTROLLER_PATH + ID_PATH,
                        createdFixedRateBondID)
                        .content(asJson(testUtils.getFixedRateBondFullSellDTO()))
                        .contentType(APPLICATION_JSON),
                        ADMIN_NAME)
                .andExpect(status().isOk());
        assertThat(fixedRateBondRepository.findAll()).hasSize(0);
        assertThat(financialAssetRelationshipRepository.findAll()).hasSize(0);
        assertEquals(10837.301F, accountCashRepository.findAll().get(0).getAmount());
        assertEquals(21674.602F, accountCashRepository.findAll().get(1).getAmount());
    }

    @Test
    public void sellAllPackageWithoutTaxesIT() throws Exception {
        testUtils.createDefaultFixedRateBond();
        assertThat(fixedRateBondRepository.findAll()).hasSize(1);
        assertThat(financialAssetRelationshipRepository.findAll()).hasSize(1);

        Long createdFixedRateBondID = fixedRateBondRepository.findAll().get(0).getId();

        testUtils.perform(
                        delete("/data" + FIXED_RATE_BOND_CONTROLLER_PATH + ID_PATH,
                                createdFixedRateBondID)
                                .content(asJson(testUtils.getFixedRateBondFullSellDTODiffWithoutTaxes()))
                                .contentType(APPLICATION_JSON),
                        ADMIN_NAME)
                .andExpect(status().isOk());
        assertThat(fixedRateBondRepository.findAll()).hasSize(0);
        assertThat(financialAssetRelationshipRepository.findAll()).hasSize(0);
        assertEquals(3317.00F, accountCashRepository.findAll().get(0).getAmount());
        assertEquals(6634.00F, accountCashRepository.findAll().get(1).getAmount());
    }

    @Test
    public void sellAllPackageNotSupportedTaxResidencyIT() throws Exception {
        testUtils.createDefaultFixedRateBond();
        assertThat(fixedRateBondRepository.findAll()).hasSize(1);
        assertThat(financialAssetRelationshipRepository.findAll()).hasSize(1);

        Long createdFixedRateBondID = fixedRateBondRepository.findAll().get(0).getId();
        Exception exception = testUtils.perform(
                        delete("/data" + FIXED_RATE_BOND_CONTROLLER_PATH + ID_PATH,
                                createdFixedRateBondID)
                                .content(asJson(testUtils.getNotValidCountryFixedRateBondFullSellDTO()))
                                .contentType(APPLICATION_JSON),
                        ADMIN_NAME)
                .andExpect(status().isBadRequest())
                .andReturn().getResolvedException();

        assert exception != null;
        assertEquals(IllegalArgumentException.class, exception.getClass());
        assertThat(fixedRateBondRepository.findAll()).hasSize(1);
        assertThat(financialAssetRelationshipRepository.findAll()).hasSize(1);
    }

    @Test
    public void redeemBondsIT() throws Exception {
        testUtils.createCheapFixedRateBond();
        assertThat(fixedRateBondRepository.findAll()).hasSize(1);
        assertThat(financialAssetRelationshipRepository.findAll()).hasSize(1);

        Long createdFixedRateBondID = fixedRateBondRepository.findAll().get(0).getId();

        testUtils.perform(
                        delete("/data" + FIXED_RATE_BOND_CONTROLLER_PATH + ID_PATH + REDEEM_PATH,
                                createdFixedRateBondID)
                                .content(asJson(testUtils.getAssetsOwnersCountryDTO()))
                                .contentType(APPLICATION_JSON),
                        ADMIN_NAME)
                .andExpect(status().isOk());
        assertThat(fixedRateBondRepository.findAll()).hasSize(0);
        assertThat(financialAssetRelationshipRepository.findAll()).hasSize(0);
        assertEquals(10941.70F, accountCashRepository.findAll().get(0).getAmount());
        assertEquals(21883.40F, accountCashRepository.findAll().get(1).getAmount());
    }

    @Test
    public void redeemBondsWithoutTaxesIT() throws Exception {
        testUtils.createDefaultFixedRateBond();
        assertThat(fixedRateBondRepository.findAll()).hasSize(1);
        assertThat(financialAssetRelationshipRepository.findAll()).hasSize(1);

        Long createdFixedRateBondID = fixedRateBondRepository.findAll().get(0).getId();

        testUtils.perform(
                        delete("/data" + FIXED_RATE_BOND_CONTROLLER_PATH + ID_PATH + REDEEM_PATH,
                                createdFixedRateBondID)
                                .content(asJson(testUtils.getAssetsOwnersCountryDTO()))
                                .contentType(APPLICATION_JSON),
                        ADMIN_NAME)
                .andExpect(status().isOk());
        assertThat(fixedRateBondRepository.findAll()).hasSize(0);
        assertThat(financialAssetRelationshipRepository.findAll()).hasSize(0);
        assertEquals(10050.00F, accountCashRepository.findAll().get(0).getAmount());
        assertEquals(20100.00F, accountCashRepository.findAll().get(1).getAmount());
    }

    @Test
    public void redeemBondsNotSupportedTaxResidencyIT() throws Exception {
        testUtils.createDefaultFixedRateBond();
        assertThat(fixedRateBondRepository.findAll()).hasSize(1);
        assertThat(financialAssetRelationshipRepository.findAll()).hasSize(1);

        Long createdFixedRateBondID = fixedRateBondRepository.findAll().get(0).getId();
        Exception exception = testUtils.perform(
                        delete("/data" + FIXED_RATE_BOND_CONTROLLER_PATH + ID_PATH + REDEEM_PATH,
                                createdFixedRateBondID)
                                .content(asJson(testUtils.getNotValidAssetsOwnersCountryDTO()))
                                .contentType(APPLICATION_JSON),
                        ADMIN_NAME)
                .andExpect(status().isBadRequest())
                .andReturn().getResolvedException();

        assert exception != null;
        assertEquals(IllegalArgumentException.class, exception.getClass());
        assertThat(fixedRateBondRepository.findAll()).hasSize(1);
        assertThat(financialAssetRelationshipRepository.findAll()).hasSize(1);
    }

    @Test
    public void redeemBondsNotYetMaturedBondsIT() throws Exception {
        testUtils.createNotYetMaturedFixedRateBond();
        assertThat(fixedRateBondRepository.findAll()).hasSize(1);
        assertThat(financialAssetRelationshipRepository.findAll()).hasSize(1);

        Long createdFixedRateBondID = fixedRateBondRepository.findAll().get(0).getId();
        Exception exception = testUtils.perform(
                        delete("/data" + FIXED_RATE_BOND_CONTROLLER_PATH + ID_PATH + REDEEM_PATH,
                                createdFixedRateBondID)
                                .content(asJson(testUtils.getAssetsOwnersCountryDTO()))
                                .contentType(APPLICATION_JSON),
                        ADMIN_NAME)
                .andExpect(status().isBadRequest())
                .andReturn().getResolvedException();

        assert exception != null;
        assertEquals(IllegalArgumentException.class, exception.getClass());
        assertThat(fixedRateBondRepository.findAll()).hasSize(1);
        assertThat(financialAssetRelationshipRepository.findAll()).hasSize(1);
    }
}
