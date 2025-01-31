package fund.data.assets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import fund.data.assets.dto.asset.exchange.AssetsOwnersCountryDTO;
import fund.data.assets.dto.asset.exchange.BuyFixedRateBondDTO;
import fund.data.assets.dto.asset.exchange.FirstBuyFixedRateBondDTO;
import fund.data.assets.dto.asset.exchange.SellFixedRateBondDTO;
import fund.data.assets.dto.asset.exchange.PartialSellFixedRateBondDTO;
import fund.data.assets.dto.common.LoginDto;
import fund.data.assets.dto.financial_entities.AccountDTO;
import fund.data.assets.dto.financial_entities.AccountCashDTO;
import fund.data.assets.dto.financial_entities.TurnoverCommissionValueDTO;
import fund.data.assets.dto.common.PercentFloatValueDTO;
import fund.data.assets.dto.owner.NewRussianAssetsOwnerDTO;
import fund.data.assets.dto.owner.ContactDataRussianAssetsOwnerDTO;
import fund.data.assets.dto.owner.PersonalDataRussianAssetsOwnerDTO;
import fund.data.assets.jwt.JWTHelper;
import fund.data.assets.model.asset.exchange.FixedRateBondPackage;
import fund.data.assets.repository.AccountRepository;
import fund.data.assets.repository.AccountCashRepository;
import fund.data.assets.repository.RussianAssetsOwnerRepository;
import fund.data.assets.repository.TurnoverCommissionValueRepository;
import fund.data.assets.repository.FinancialAssetRelationshipRepository;
import fund.data.assets.repository.FixedRateBondRepository;
import fund.data.assets.utils.enums.AssetsOwnersCountry;

import org.openapitools.jackson.nullable.JsonNullable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.text.DecimalFormat;

import java.time.LocalDate;

import java.util.LinkedHashMap;
import java.util.Map;

import static fund.data.assets.config.SecurityConfig.ADMIN_NAME;
import static fund.data.assets.config.SecurityConfig.ADMIN_PASSWORD;
import static fund.data.assets.config.SecurityConfig.LOGIN_PATH;
import static fund.data.assets.controller.AccountController.ACCOUNT_CONTROLLER_PATH;
import static fund.data.assets.controller.AccountCashController.ACCOUNT_CASH_CONTROLLER_PATH;
import static fund.data.assets.controller.FixedRateBondPackageController.FIXED_RATE_BOND_CONTROLLER_PATH;
import static fund.data.assets.controller.RussianAssetsOwnerController.RUSSIAN_OWNERS_CONTROLLER_PATH;
import static fund.data.assets.controller.TurnoverCommissionValueController.TURNOVER_COMMISSION_VALUE_CONTROLLER_PATH;
import static fund.data.assets.utils.enums.AssetCurrency.NOT_IMPLEMENTED;
import static fund.data.assets.utils.enums.AssetCurrency.RUSRUB;
import static fund.data.assets.utils.enums.RussianSexEnum.MAN;
import static fund.data.assets.utils.enums.RussianSexEnum.WOMAN;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * @version 0.1-b
 * @author MarkDementev a.k.a JavaMarkDem
 */
@Component
public class TestUtils {
    public static final String TEST_COMMISSION_PERCENT_VALUE = "1";
    public static final String TEST_ASSET_TYPE_NAME = "assetTypeName";
    public static final String TEST_STRING_FORMAT_PERCENT_VALUE = "20,1234";
    public static final Float TEST_FORMATTED_PERCENT_VALUE_FLOAT = 0.201234F;
    public static final Float TEST_COMMISSION_PERCENT_VALUE_FLOAT = 0.01F;
    public static final Float TEST_NORMAL_PACKAGE_SELL_VALUE = 33000.00F;
    public static final Float TEST_SMALL_PACKAGE_SELL_VALUE = 9900.00F;
    public static final Float TEST_FIRST_RUSSIAN_OWNER_CASH_AMOUNT = 10150.00F;
    public static final Float TEST_SECOND_RUSSIAN_OWNER_CASH_AMOUNT = 20300.00F;
    public static final DecimalFormat TEST_DECIMAL_FORMAT = new DecimalFormat( "#.####" );
    public static final LocalDate TEST_ACCOUNT_OPENING_DATE = LocalDate.of(2000, 1, 1);
    public static final LocalDate TEST_FIXED_RATE_BOND_LAST_ASSET_BUY_DATE = LocalDate.of(
            2021, 1, 1);
    public static final LocalDate TEST_FIXED_RATE_BOND_LAST_ASSET_SELL_DATE = LocalDate.of(
            2021, 6, 1);
    public static final LocalDate TEST_FIXED_RATE_BOND_MATURITY_DATE = LocalDate.of(
            2022, 1, 1);
    public static final LocalDate TEST_FIXED_RATE_BOND_LAST_ASSET_BUY_DATE_NOT_MATURED = LocalDate.of(
            2024, 1, 1);
    public static final LocalDate TEST_FIXED_RATE_BOND_MATURITY_DATE_NOT_MATURED = LocalDate.of(
            2125, 1, 1);
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();
    @Autowired
    private JWTHelper jwtHelper;
    private final AccountDTO accountDTO = new AccountDTO(
            "defaultBank",
            "1q2w3e4r5t",
            TEST_ACCOUNT_OPENING_DATE
    );
    private final AccountDTO secondAccountDTO = new AccountDTO(
            "UPDATEDDefaultBank",
            "UPDATED1q2w3e4r5t",
            TEST_ACCOUNT_OPENING_DATE
    );
    private final AccountDTO notValidAccountDTO = new AccountDTO(
            " ",
            " ",
            null
    );
    private final AccountDTO anotherBankButSameAccountNumberAccountDTO = new AccountDTO(
            "anotherBank",
            "1q2w3e4r5t",
            TEST_ACCOUNT_OPENING_DATE
    );
    private final TurnoverCommissionValueDTO turnoverCommissionValueDTO = new TurnoverCommissionValueDTO(
            null,
            TEST_ASSET_TYPE_NAME,
            TEST_COMMISSION_PERCENT_VALUE
    );
    private final TurnoverCommissionValueDTO notValidTurnoverCommissionValueDTO = new TurnoverCommissionValueDTO(
            null,
            " ",
            null
    );
    private final PercentFloatValueDTO percentFloatValueDTO = new PercentFloatValueDTO(
            TEST_STRING_FORMAT_PERCENT_VALUE
    );
    private final NewRussianAssetsOwnerDTO russianAssetsOwnerDTO = new NewRussianAssetsOwnerDTO(
        "name", "surname", "25.05.1995", "Email_sur@mail.ru", "patronymic", MAN,
            "9888888888", "2424", "111111", "placeOfBirth",
            "placeOfPassportGiven", "24.08.2021", "377-777"
    );
    private final NewRussianAssetsOwnerDTO secondRussianAssetsOwnerDTO = new NewRussianAssetsOwnerDTO(
            "ANOTHER_name", "ANOTHER_surname", "26.05.1995", "Email_mur@mail.ru",
            "ANOTHER_patronymic", WOMAN, "9888888887", "2425",
            "111112", "ANOTHER_placeOfBirth", "ANOTHER_placeOfPassportGiven",
            "25.08.2021", "377-778"
    );
    private final NewRussianAssetsOwnerDTO thirdRussianAssetsOwnerDTO = new NewRussianAssetsOwnerDTO(
            "THIRD_name", "THIRD_surname", "27.05.1995", "Email_tur@mail.ru",
            "THIRD_patronymic", MAN, "9888888886", "2426",
            "111113", "THIRD_placeOfBirth", "THIRD_placeOfPassportGiven",
            "26.08.2021", "377-779"
    );
    private final NewRussianAssetsOwnerDTO notValidRussianAssetsOwnerDTO = new NewRussianAssetsOwnerDTO(
            "", "", "25,05,1995", "Email_surmail.ru", "",
            null, "988888888", "242", "11111", "",
            "", "24,08,2021", "377-77"
    );
    private final ContactDataRussianAssetsOwnerDTO validRussianAssetsOwnerContactDataDTO =
            new ContactDataRussianAssetsOwnerDTO(
                    JsonNullable.of("NewEmail_sur@mail.ru"),
                    JsonNullable.of("9888888889"));
    private final PersonalDataRussianAssetsOwnerDTO validRussianAssetsOwnerPersonalDataDTO =
            new PersonalDataRussianAssetsOwnerDTO(
                    JsonNullable.of("NewName"),
                    JsonNullable.of("NewSurname"),
                    JsonNullable.of("NewPatronymic"),
                    JsonNullable.of("4444"),
                    JsonNullable.of("999999"),
                    JsonNullable.of("NewPlaceOfPassportGiven"),
                    JsonNullable.of("31.01.1999"),
                    JsonNullable.of("999-999"));
    private final ContactDataRussianAssetsOwnerDTO notValidRussianAssetsOwnerContactDataDTO =
            new ContactDataRussianAssetsOwnerDTO(
                    JsonNullable.of("NewEmail_surmail.ru"),
                    JsonNullable.of("988888888"));
    private final PersonalDataRussianAssetsOwnerDTO notValidRussianAssetsOwnerPersonalDataDTO =
            new PersonalDataRussianAssetsOwnerDTO(
                    JsonNullable.of(""),
                    JsonNullable.of(""),
                    JsonNullable.of(""),
                    JsonNullable.of("444"),
                    JsonNullable.of("99999"),
                    JsonNullable.of(""),
                    JsonNullable.of("31,01,1999"),
                    JsonNullable.of("999-99"));
    private final LoginDto adminLoginDto = new LoginDto(
            ADMIN_NAME,
            ADMIN_PASSWORD
    );
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private TurnoverCommissionValueRepository turnoverCommissionValueRepository;
    @Autowired
    private RussianAssetsOwnerRepository russianAssetsOwnerRepository;
    @Autowired
    private AccountCashRepository accountCashRepository;
    @Autowired
    private FixedRateBondRepository fixedRateBondRepository;
    @Autowired
    private FinancialAssetRelationshipRepository financialAssetRelationshipRepository;

    public static String asJson(final Object object) throws JsonProcessingException {
        return MAPPER.writeValueAsString(object);
    }

    public static <T> T fromJson(final String json, final TypeReference<T> to) throws JsonProcessingException {
        return MAPPER.readValue(json, to);
    }

    public void tearDown() {
        fixedRateBondRepository.deleteAll();
        financialAssetRelationshipRepository.deleteAll();
        accountCashRepository.deleteAll();
        russianAssetsOwnerRepository.deleteAll();
        turnoverCommissionValueRepository.deleteAll();
        accountRepository.deleteAll();
    }

    public AccountDTO getAccountDTO() {
        return accountDTO;
    }

    public AccountDTO getSecondAccountDTO() {
        return secondAccountDTO;
    }

    public AccountDTO getNotValidAccountDTO() {
        return notValidAccountDTO;
    }

    public AccountDTO getAnotherBankButSameAccountNumberAccountDTO() {
        return anotherBankButSameAccountNumberAccountDTO;
    }

    public TurnoverCommissionValueDTO getTurnoverCommissionValueDTO() {
        return turnoverCommissionValueDTO;
    }

    public TurnoverCommissionValueDTO getNotValidTurnoverCommissionValueDTO() {
        return notValidTurnoverCommissionValueDTO;
    }

    public PercentFloatValueDTO getPercentFloatValueDTO() {
        return percentFloatValueDTO;
    }

    public NewRussianAssetsOwnerDTO getNewRussianAssetsOwnerDTO() {
        return russianAssetsOwnerDTO;
    }

    public NewRussianAssetsOwnerDTO getNotValidNewRussianAssetsOwnerDTO() {
        return notValidRussianAssetsOwnerDTO;
    }

    public ContactDataRussianAssetsOwnerDTO getValidRussianAssetsOwnerContactDataDTO() {
        return validRussianAssetsOwnerContactDataDTO;
    }

    public PersonalDataRussianAssetsOwnerDTO getValidRussianAssetsOwnerPersonalDataDTO() {
        return validRussianAssetsOwnerPersonalDataDTO;
    }

    public ContactDataRussianAssetsOwnerDTO getNotValidRussianAssetsOwnerContactDataDTO() {
        return notValidRussianAssetsOwnerContactDataDTO;
    }

    public PersonalDataRussianAssetsOwnerDTO getNotValidRussianAssetsOwnerPersonalDataDTO() {
        return notValidRussianAssetsOwnerPersonalDataDTO;
    }

    public FirstBuyFixedRateBondDTO getFirstBuyFixedRateBondDTO() throws Exception {
        Map<String, Float> assetOwnersWithAssetCounts
                = prepareEntitiesBeforeFirstBuyFixedRateBondThenGetAssetOwnersWithAssetCounts();

        return new FirstBuyFixedRateBondDTO(
                AssetsOwnersCountry.RUS,
                30,
                assetOwnersWithAssetCounts,
                TEST_FIXED_RATE_BOND_LAST_ASSET_BUY_DATE,
                100.00F,
                0.00F,
                1,
                RUSRUB,
                "assetTitle",
                accountRepository.findAll().get(0).getId(),
                "RU000A103NZ8",
                "assetIssuerTitle",
                1000,
                100.00F,
                TEST_FIXED_RATE_BOND_MATURITY_DATE
        );
    }

    public FirstBuyFixedRateBondDTO getNotValidFirstBuyFixedRateBondDTO() throws Exception {
        Map<String, Float> assetOwnersWithAssetCounts
                = prepareEntitiesBeforeFirstBuyFixedRateBondThenGetAssetOwnersWithAssetCounts();

        return new FirstBuyFixedRateBondDTO(
                null,
                -30,
                assetOwnersWithAssetCounts,
                TEST_FIXED_RATE_BOND_LAST_ASSET_BUY_DATE,
                100.00F,
                0.00F,
                0,
                NOT_IMPLEMENTED,
                "",
                accountRepository.findAll().get(0).getId(),
                "",
                "",
                1000,
                100.00F,
                TEST_FIXED_RATE_BOND_MATURITY_DATE
        );
    }

    public BuyFixedRateBondDTO getBuyFixedRateBondDTO() {
        Map<String, Float> assetOwnersWithAssetCounts = new LinkedHashMap<>();

        assetOwnersWithAssetCounts.put(String.valueOf(russianAssetsOwnerRepository.findAll().get(0).getId()), 1.00F);
        assetOwnersWithAssetCounts.put(String.valueOf(russianAssetsOwnerRepository.findAll().get(1).getId()), 2.00F);

        return new BuyFixedRateBondDTO(
                AssetsOwnersCountry.RUS,
                3,
                assetOwnersWithAssetCounts,
                TEST_FIXED_RATE_BOND_LAST_ASSET_SELL_DATE,
                99.00F,
                10.00F,
                1
        );
    }

    public BuyFixedRateBondDTO getBuyFixedRateBondNotAllOwnersDTO() {
        Map<String, Float> assetOwnersWithAssetCounts = new LinkedHashMap<>();

        assetOwnersWithAssetCounts.put(String.valueOf(russianAssetsOwnerRepository.findAll().get(1).getId()), 5.00F);

        return new BuyFixedRateBondDTO(
                AssetsOwnersCountry.RUS,
                5,
                assetOwnersWithAssetCounts,
                TEST_FIXED_RATE_BOND_LAST_ASSET_SELL_DATE,
                95.00F,
                50.00F,
                1
        );
    }

    public BuyFixedRateBondDTO getBuyFixedRateBondNotValidTaxResidencyDTO() {
        Map<String, Float> assetOwnersWithAssetCounts = new LinkedHashMap<>();

        assetOwnersWithAssetCounts.put(String.valueOf(russianAssetsOwnerRepository.findAll().get(0).getId()), 1.00F);
        assetOwnersWithAssetCounts.put(String.valueOf(russianAssetsOwnerRepository.findAll().get(1).getId()), 2.00F);

        return new BuyFixedRateBondDTO(
                AssetsOwnersCountry.USA,
                3,
                assetOwnersWithAssetCounts,
                TEST_FIXED_RATE_BOND_LAST_ASSET_SELL_DATE,
                99.00F,
                10.00F,
                1
        );
    }

    public BuyFixedRateBondDTO getBuyWithNewOwnerFixedRateBondDTO() {
        Map<String, Float> assetOwnersWithAssetCounts = new LinkedHashMap<>();

        assetOwnersWithAssetCounts.put(String.valueOf(russianAssetsOwnerRepository.findAll().get(0).getId()), 1.00F);
        assetOwnersWithAssetCounts.put(String.valueOf(russianAssetsOwnerRepository.findAll().get(1).getId()), 2.00F);
        assetOwnersWithAssetCounts.put(String.valueOf(russianAssetsOwnerRepository.findAll().get(2).getId()), 4.00F);

        return new BuyFixedRateBondDTO(
                AssetsOwnersCountry.RUS,
                7,
                assetOwnersWithAssetCounts,
                TEST_FIXED_RATE_BOND_LAST_ASSET_SELL_DATE,
                99.00F,
                10.00F,
                1
        );
    }

    public PartialSellFixedRateBondDTO getPartialSellFixedRateBondPackageFirstDTO() {
        Map<String, Integer> assetOwnersWithAssetCountsToSell = new LinkedHashMap<>();

        assetOwnersWithAssetCountsToSell.put(String.valueOf(russianAssetsOwnerRepository.findAll().get(0).getId()),
                5);
        assetOwnersWithAssetCountsToSell.put(String.valueOf(russianAssetsOwnerRepository.findAll().get(1).getId()),
                10);

        return new PartialSellFixedRateBondDTO(
                AssetsOwnersCountry.RUS,
                TEST_NORMAL_PACKAGE_SELL_VALUE,
                assetOwnersWithAssetCountsToSell,
                TEST_FIXED_RATE_BOND_LAST_ASSET_SELL_DATE,
                1
        );
    }

    public PartialSellFixedRateBondDTO getPartialSellFixedRateBondPackageSecondDTO() {
        Map<String, Integer> assetOwnersWithAssetCountsToSell = new LinkedHashMap<>();

        assetOwnersWithAssetCountsToSell.put(String.valueOf(russianAssetsOwnerRepository.findAll().get(0).getId()),
                6);
        assetOwnersWithAssetCountsToSell.put(String.valueOf(russianAssetsOwnerRepository.findAll().get(1).getId()),
                12);

        return new PartialSellFixedRateBondDTO(
                AssetsOwnersCountry.RUS,
                TEST_SMALL_PACKAGE_SELL_VALUE,
                assetOwnersWithAssetCountsToSell,
                TEST_FIXED_RATE_BOND_LAST_ASSET_SELL_DATE,
                1
        );
    }

    public PartialSellFixedRateBondDTO getPartialSellFixedRateBondPackageNotEnoughAssetsDTO() {
        Map<String, Integer> assetOwnersWithAssetCountsToSell = new LinkedHashMap<>();

        assetOwnersWithAssetCountsToSell.put(String.valueOf(russianAssetsOwnerRepository.findAll().get(0).getId()),
                11);
        assetOwnersWithAssetCountsToSell.put(String.valueOf(russianAssetsOwnerRepository.findAll().get(1).getId()),
                21);

        return new PartialSellFixedRateBondDTO(
                AssetsOwnersCountry.RUS,
                TEST_SMALL_PACKAGE_SELL_VALUE,
                assetOwnersWithAssetCountsToSell,
                TEST_FIXED_RATE_BOND_LAST_ASSET_SELL_DATE,
                1
        );
    }

    public PartialSellFixedRateBondDTO getPartialSellFixedRateBondPackageNotValidTaxResidencyDTO() {
        Map<String, Integer> assetOwnersWithAssetCountsToSell = new LinkedHashMap<>();

        assetOwnersWithAssetCountsToSell.put(String.valueOf(russianAssetsOwnerRepository.findAll().get(0).getId()),
                6);
        assetOwnersWithAssetCountsToSell.put(String.valueOf(russianAssetsOwnerRepository.findAll().get(1).getId()),
                12);

        return new PartialSellFixedRateBondDTO(
                AssetsOwnersCountry.USA,
                TEST_SMALL_PACKAGE_SELL_VALUE,
                assetOwnersWithAssetCountsToSell,
                TEST_FIXED_RATE_BOND_LAST_ASSET_SELL_DATE,
                1
        );
    }

    public SellFixedRateBondDTO getFixedRateBondFullSellDTO() {
        return new SellFixedRateBondDTO(
             AssetsOwnersCountry.RUS,
                TEST_NORMAL_PACKAGE_SELL_VALUE
        );
    }

    public SellFixedRateBondDTO getFixedRateBondFullSellDTODiffWithoutTaxes() {
        return new SellFixedRateBondDTO(
                AssetsOwnersCountry.RUS,
                TEST_SMALL_PACKAGE_SELL_VALUE
        );
    }

    public SellFixedRateBondDTO getNotValidCountryFixedRateBondFullSellDTO() {
        return new SellFixedRateBondDTO(
                AssetsOwnersCountry.USA,
                TEST_NORMAL_PACKAGE_SELL_VALUE
        );
    }

    public AssetsOwnersCountryDTO getAssetsOwnersCountryDTO() {
        return new AssetsOwnersCountryDTO(
                AssetsOwnersCountry.RUS
        );
    }

    public AssetsOwnersCountryDTO getNotValidAssetsOwnersCountryDTO() {
        return new AssetsOwnersCountryDTO(
                AssetsOwnersCountry.USA
        );
    }

    public LoginDto getAdminLoginDto () {
        return adminLoginDto;
    }

    public ResultActions createDefaultAccount() throws Exception {
        return createAccount(accountDTO);
    }

    public ResultActions createDefaultSecondAccount() throws Exception {
        return createAccount(secondAccountDTO);
    }

    public ResultActions createDefaultTurnoverCommissionValue() throws Exception {
        createDefaultAccount();

        TurnoverCommissionValueDTO newTurnoverCommissionValueDTO = new TurnoverCommissionValueDTO(
                accountRepository.findByOrganisationWhereAccountOpened(
                        getAccountDTO().getOrganisationWhereAccountOpened()).getId(),
                TEST_ASSET_TYPE_NAME,
                TEST_COMMISSION_PERCENT_VALUE
        );
        return createTurnoverCommissionValue(newTurnoverCommissionValueDTO);
    }

    public ResultActions createDefaultRussianAssetsOwner() throws Exception {
        return createRussianAssetsOwner(russianAssetsOwnerDTO);
    }

    public ResultActions createSecondRussianAssetsOwner() throws Exception {
        return createRussianAssetsOwner(secondRussianAssetsOwnerDTO);
    }

    public ResultActions createThirdRussianAssetsOwner() throws Exception {
        return createRussianAssetsOwner(thirdRussianAssetsOwnerDTO);
    }

    public ResultActions createDefaultAccountCash() throws Exception {
        createDefaultAccount();
        createDefaultRussianAssetsOwner();

        AccountCashDTO accountCashDTO = new AccountCashDTO(
                accountRepository.findAll().get(0).getId(),
                RUSRUB,
                russianAssetsOwnerRepository.findAll().get(0).getId(),
                0.00F
        );
        return createAccountCash(accountCashDTO);
    }

    public ResultActions createCheapFixedRateBond() throws Exception {
        Map<String, Float> assetOwnersWithAssetCounts
                = prepareEntitiesBeforeFirstBuyFixedRateBondThenGetAssetOwnersWithAssetCounts();

        FirstBuyFixedRateBondDTO firstBuyFixedRateBondDTO = new FirstBuyFixedRateBondDTO(
                AssetsOwnersCountry.RUS,
                30,
                assetOwnersWithAssetCounts,
                TEST_FIXED_RATE_BOND_LAST_ASSET_BUY_DATE,
                90.00F,
                0.00F,
                1,
                RUSRUB,
                "assetTitle",
                accountRepository.findAll().get(0).getId(),
                "RU000A103NZ8",
                "assetIssuerTitle",
                1000,
                100.00F,
                TEST_FIXED_RATE_BOND_MATURITY_DATE
        );
        return createFixedRateBond(firstBuyFixedRateBondDTO);
    }

    public ResultActions createDefaultFixedRateBond() throws Exception {
        Map<String, Float> assetOwnersWithAssetCounts
                = prepareEntitiesBeforeFirstBuyFixedRateBondThenGetAssetOwnersWithAssetCounts();

        FirstBuyFixedRateBondDTO firstBuyFixedRateBondDTO = new FirstBuyFixedRateBondDTO(
                AssetsOwnersCountry.RUS,
                30,
                assetOwnersWithAssetCounts,
                TEST_FIXED_RATE_BOND_LAST_ASSET_BUY_DATE,
                100.00F,
                0.00F,
                1,
                RUSRUB,
                "assetTitle",
                accountRepository.findAll().get(0).getId(),
                "RU000A103NZ8",
                "assetIssuerTitle",
                1000,
                100.00F,
                TEST_FIXED_RATE_BOND_MATURITY_DATE
        );
        return createFixedRateBond(firstBuyFixedRateBondDTO);
    }

    public ResultActions createNotYetMaturedFixedRateBond() throws Exception {
        Map<String, Float> assetOwnersWithAssetCounts
                = prepareEntitiesBeforeFirstBuyFixedRateBondThenGetAssetOwnersWithAssetCounts();

        FirstBuyFixedRateBondDTO firstBuyFixedRateBondDTO = new FirstBuyFixedRateBondDTO(
                AssetsOwnersCountry.RUS,
                30,
                assetOwnersWithAssetCounts,
                TEST_FIXED_RATE_BOND_LAST_ASSET_BUY_DATE_NOT_MATURED,
                100.00F,
                0.00F,
                1,
                RUSRUB,
                "assetTitle",
                accountRepository.findAll().get(0).getId(),
                "RU000A103NZ8",
                "assetIssuerTitle",
                1000,
                100.00F,
                TEST_FIXED_RATE_BOND_MATURITY_DATE_NOT_MATURED
        );
        return createFixedRateBond(firstBuyFixedRateBondDTO);
    }

    public ResultActions createAccount(final AccountDTO accountDTO) throws Exception {
        final var request = post("/data" + ACCOUNT_CONTROLLER_PATH)
                .content(asJson(accountDTO))
                .contentType(APPLICATION_JSON);

        return perform(request, ADMIN_NAME);
    }

    public ResultActions createTurnoverCommissionValue(final TurnoverCommissionValueDTO turnoverCommissionValueDTO)
            throws Exception {
        final var request = post("/data" + TURNOVER_COMMISSION_VALUE_CONTROLLER_PATH)
                .content(asJson(turnoverCommissionValueDTO))
                .contentType(APPLICATION_JSON);

        return perform(request, ADMIN_NAME);
    }

    public ResultActions createRussianAssetsOwner(final NewRussianAssetsOwnerDTO russianAssetsOwnerDTO)
            throws Exception {
        final var request = post("/data" + RUSSIAN_OWNERS_CONTROLLER_PATH)
                .content(asJson(russianAssetsOwnerDTO))
                .contentType(APPLICATION_JSON);

        return perform(request, ADMIN_NAME);
    }

    public ResultActions createAccountCash(final AccountCashDTO accountCashDTO) throws Exception {
        final var request = post("/data" + ACCOUNT_CASH_CONTROLLER_PATH)
                .content(asJson(accountCashDTO))
                .contentType(APPLICATION_JSON);

        return perform(request, ADMIN_NAME);
    }

    public ResultActions createFixedRateBond(final FirstBuyFixedRateBondDTO firstBuyFixedRateBondDTO)
            throws Exception {
        final var request = post("/data" + FIXED_RATE_BOND_CONTROLLER_PATH)
                .content(asJson(firstBuyFixedRateBondDTO))
                .contentType(APPLICATION_JSON);

        return perform(request, ADMIN_NAME);
    }

    public Map<String, Float> prepareEntitiesBeforeFirstBuyFixedRateBondThenGetAssetOwnersWithAssetCounts()
            throws Exception {
        Map<String, Float> assetOwnersWithAssetCounts = new LinkedHashMap<>();

        createDefaultRussianAssetsOwner();
        createSecondRussianAssetsOwner();
        assetOwnersWithAssetCounts.put(String.valueOf(russianAssetsOwnerRepository.findAll().get(0).getId()),
                10.00F);
        assetOwnersWithAssetCounts.put(String.valueOf(russianAssetsOwnerRepository.findAll().get(1).getId()),
                20.00F);
        createDefaultAccount();

        TurnoverCommissionValueDTO newTurnoverCommissionValueDTO = new TurnoverCommissionValueDTO(
                accountRepository.findByOrganisationWhereAccountOpened(
                        getAccountDTO().getOrganisationWhereAccountOpened()).getId(),
                FixedRateBondPackage.class.getSimpleName(),
                TEST_COMMISSION_PERCENT_VALUE
        );
        createTurnoverCommissionValue(newTurnoverCommissionValueDTO);

        AccountCashDTO firstAccountCashDTO = new AccountCashDTO(
                accountRepository.findAll().get(0).getId(),
                RUSRUB,
                russianAssetsOwnerRepository.findAll().get(0).getId(),
                TEST_FIRST_RUSSIAN_OWNER_CASH_AMOUNT
        );
        AccountCashDTO secondAccountCashDTO = new AccountCashDTO(
                accountRepository.findAll().get(0).getId(),
                RUSRUB,
                russianAssetsOwnerRepository.findAll().get(1).getId(),
                TEST_SECOND_RUSSIAN_OWNER_CASH_AMOUNT
        );
        createAccountCash(firstAccountCashDTO);
        createAccountCash(secondAccountCashDTO);

        return assetOwnersWithAssetCounts;
    }

    public void addMoreMoneyToOwnersWhileBuyingMoreBonds() throws Exception {
        AccountCashDTO firstAccountCashDTO = new AccountCashDTO(
                accountRepository.findAll().get(0).getId(),
                RUSRUB,
                russianAssetsOwnerRepository.findAll().get(0).getId(),
                TEST_FIRST_RUSSIAN_OWNER_CASH_AMOUNT
        );
        AccountCashDTO secondAccountCashDTO = new AccountCashDTO(
                accountRepository.findAll().get(0).getId(),
                RUSRUB,
                russianAssetsOwnerRepository.findAll().get(1).getId(),
                TEST_SECOND_RUSSIAN_OWNER_CASH_AMOUNT
        );

        createAccountCash(firstAccountCashDTO);
        createAccountCash(secondAccountCashDTO);
    }

    public void addThirdRussianAssetsOwnerWithMoney() throws Exception {
        createThirdRussianAssetsOwner();

        AccountCashDTO accountCashDTO = new AccountCashDTO(
                accountRepository.findAll().get(0).getId(),
                RUSRUB,
                russianAssetsOwnerRepository.findAll().get(2).getId(),
                TEST_FIRST_RUSSIAN_OWNER_CASH_AMOUNT
        );

        createAccountCash(accountCashDTO);
    }

    public ResultActions login(final LoginDto loginDto) throws Exception {
        return perform(post(LOGIN_PATH).content(asJson(loginDto)).contentType(APPLICATION_JSON));
    }

    public ResultActions perform(final MockHttpServletRequestBuilder request) throws Exception {
        return mockMvc.perform(request);
    }

    public ResultActions perform(final MockHttpServletRequestBuilder request, final String byUser) throws Exception {
        final String token = jwtHelper.expiring(Map.of("username", byUser));

        request.header(AUTHORIZATION, token);
        return perform(request);
    }
}
