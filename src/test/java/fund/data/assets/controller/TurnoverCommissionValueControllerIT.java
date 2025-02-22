package fund.data.assets.controller;

import com.fasterxml.jackson.core.type.TypeReference;

import fund.data.assets.TestUtils;
import fund.data.assets.config.SpringConfigForTests;
import fund.data.assets.dto.financial_entities.TurnoverCommissionValueDTO;
import fund.data.assets.dto.common.PercentFloatValueDTO;
import fund.data.assets.exception.EntityWithIDNotFoundException;
import fund.data.assets.exception.NotValidPercentValueInputFormatException;
import fund.data.assets.model.financial_entities.TurnoverCommissionValue;
import fund.data.assets.repository.AccountRepository;
import fund.data.assets.repository.TurnoverCommissionValueRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static fund.data.assets.TestUtils.fromJson;
import static fund.data.assets.TestUtils.asJson;
import static fund.data.assets.TestUtils.TEST_ASSET_TYPE_NAME;
import static fund.data.assets.TestUtils.TEST_COMMISSION_PERCENT_VALUE;
import static fund.data.assets.TestUtils.TEST_COMMISSION_PERCENT_VALUE_FLOAT;
import static fund.data.assets.TestUtils.TEST_STRING_FORMAT_PERCENT_VALUE;
import static fund.data.assets.TestUtils.TEST_FORMATTED_PERCENT_VALUE_FLOAT;
import static fund.data.assets.config.SecurityConfig.ADMIN_NAME;
import static fund.data.assets.config.SpringConfigForTests.TEST_PROFILE;
import static fund.data.assets.controller.AccountController.ID_PATH;
import static fund.data.assets.controller.TurnoverCommissionValueController.TURNOVER_COMMISSION_VALUE_CONTROLLER_PATH;

import static org.assertj.core.api.Assertions.assertThat;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @version 0.6-a
 * @author MarkDementev a.k.a JavaMarkDem
 */
@SpringBootTest(classes = SpringConfigForTests.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles(TEST_PROFILE)
@AutoConfigureMockMvc
public class TurnoverCommissionValueControllerIT {
    @Autowired
    private TestUtils testUtils;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private TurnoverCommissionValueRepository turnoverCommissionValueRepository;

    @BeforeEach
    public void loginBeforeTest() throws Exception {
        testUtils.login(testUtils.getAdminLoginDto());
    }

    @AfterEach
    public void clearRepositories() {
        testUtils.tearDown();
    }

    @Test
    public void getTurnoverCommissionValueIT() throws Exception {
        testUtils.createDefaultTurnoverCommissionValue();

        TurnoverCommissionValue expectedTurnoverCommissionValue = turnoverCommissionValueRepository.findAll().get(0);
        var response = testUtils.perform(
                get("/data" + TURNOVER_COMMISSION_VALUE_CONTROLLER_PATH + ID_PATH,
                        expectedTurnoverCommissionValue.getId()),
                        ADMIN_NAME
                ).andExpect(status().isOk())
                .andReturn()
                .getResponse();
        TurnoverCommissionValue turnoverCommissionValueFromResponse = fromJson(response.getContentAsString(),
                new TypeReference<>() {});

        assertEquals(expectedTurnoverCommissionValue.getId(),
                turnoverCommissionValueFromResponse.getId());
        assertEquals(expectedTurnoverCommissionValue.getAccount().getId(),
                turnoverCommissionValueFromResponse.getAccount().getId());
        assertEquals(expectedTurnoverCommissionValue.getAssetTypeName(),
                turnoverCommissionValueFromResponse.getAssetTypeName());
        assertEquals(expectedTurnoverCommissionValue.getCommissionPercentValue(),
                turnoverCommissionValueFromResponse.getCommissionPercentValue());
        assertNotNull(turnoverCommissionValueFromResponse.getCreatedAt());
    }

    @Test
    public void getNotExistsTurnoverCommissionValueIT() throws Exception {
        testUtils.createDefaultTurnoverCommissionValue();

        Long notExistsTurnoverCommissionValueID = turnoverCommissionValueRepository.findAll().get(0).getId();
        notExistsTurnoverCommissionValueID++;

        Exception exception = testUtils.perform(
                    get("/data" + TURNOVER_COMMISSION_VALUE_CONTROLLER_PATH + ID_PATH,
                            notExistsTurnoverCommissionValueID),
                        ADMIN_NAME)
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();

        assert exception != null;
        assertEquals(EntityWithIDNotFoundException.class, exception.getClass());
    }

    @Test
    public void getTurnoverCommissionValuesIT() throws Exception {
        testUtils.createDefaultTurnoverCommissionValue();

        var response = testUtils.perform(
                    get("/data" + TURNOVER_COMMISSION_VALUE_CONTROLLER_PATH),
                        ADMIN_NAME
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        final List<TurnoverCommissionValue> allTurnoverCommissionValues = fromJson(response.getContentAsString(),
                new TypeReference<>() {});

        assertThat(allTurnoverCommissionValues).hasSize(1);
    }

    @Test
    public void createTurnoverCommissionValueIT() throws Exception {
        testUtils.createDefaultAccount();

        TurnoverCommissionValueDTO validTurnoverCommissionValueDTO = new TurnoverCommissionValueDTO(
                accountRepository.findByOrganisationWhereAccountOpened(
                        testUtils.getAccountDTO().getOrganisationWhereAccountOpened()).getId(),
                TEST_ASSET_TYPE_NAME,
                TEST_COMMISSION_PERCENT_VALUE
        );
        var response = testUtils.perform(
                post("/data" + TURNOVER_COMMISSION_VALUE_CONTROLLER_PATH)
                        .content(asJson(validTurnoverCommissionValueDTO))
                        .contentType(APPLICATION_JSON),
                        ADMIN_NAME
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse();
        TurnoverCommissionValue turnoverCommissionValueFromResponse = fromJson(response.getContentAsString(),
                new TypeReference<>() {});

        assertNotNull(turnoverCommissionValueFromResponse.getId());
        assertEquals(validTurnoverCommissionValueDTO.getAccountID(),
                turnoverCommissionValueFromResponse.getAccount().getId());
        assertEquals(validTurnoverCommissionValueDTO.getAssetTypeName(),
                turnoverCommissionValueFromResponse.getAssetTypeName());
        assertEquals(TEST_COMMISSION_PERCENT_VALUE_FLOAT,
                turnoverCommissionValueFromResponse.getCommissionPercentValue());
        assertNotNull(turnoverCommissionValueFromResponse.getCreatedAt());
        assertNotNull(turnoverCommissionValueFromResponse.getUpdatedAt());
    }

    @Test
    public void createNotValidTurnoverCommissionValueIT() throws Exception {
        testUtils.perform(post("/data" + TURNOVER_COMMISSION_VALUE_CONTROLLER_PATH)
                .content(asJson(testUtils.getNotValidTurnoverCommissionValueDTO()))
                .contentType(APPLICATION_JSON),
                ADMIN_NAME);
        assertThat(turnoverCommissionValueRepository.findAll()).hasSize(0);

        testUtils.createDefaultTurnoverCommissionValue();
        assertThat(turnoverCommissionValueRepository.findAll()).hasSize(1);

        TurnoverCommissionValueDTO turnoverCommissionValueDTOBothNotUniqueAccountAssetTypeName
                = new TurnoverCommissionValueDTO(
                accountRepository.findByOrganisationWhereAccountOpened(
                        testUtils.getAccountDTO().getOrganisationWhereAccountOpened()).getId(),
                TEST_ASSET_TYPE_NAME,
                TEST_COMMISSION_PERCENT_VALUE + TEST_COMMISSION_PERCENT_VALUE
        );
        Exception exception = testUtils.perform(post("/data" + TURNOVER_COMMISSION_VALUE_CONTROLLER_PATH)
                        .content(asJson(turnoverCommissionValueDTOBothNotUniqueAccountAssetTypeName))
                        .contentType(APPLICATION_JSON),
                        ADMIN_NAME)
                .andExpect(status().isBadRequest())
                .andReturn().getResolvedException();

        assert exception != null;
        assertEquals(DataIntegrityViolationException.class, exception.getClass());
        assertThat(turnoverCommissionValueRepository.findAll()).hasSize(1);
    }

    @Test
    public void updateTurnoverCommissionValueIT() throws Exception {
        testUtils.createDefaultTurnoverCommissionValue();

        Long createdTurnoverCommissionId = turnoverCommissionValueRepository.findAll().get(0).getId();
        PercentFloatValueDTO percentFloatValueDTO = new PercentFloatValueDTO(TEST_STRING_FORMAT_PERCENT_VALUE);
        var response = testUtils.perform(put("/data" + TURNOVER_COMMISSION_VALUE_CONTROLLER_PATH
                        + ID_PATH, createdTurnoverCommissionId)
                        .content(asJson(percentFloatValueDTO)).contentType(APPLICATION_JSON),
                        ADMIN_NAME)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        TurnoverCommissionValue turnoverCommissionValueFromResponse = fromJson(response.getContentAsString(),
                new TypeReference<>() {});

        assertNotNull(turnoverCommissionValueFromResponse.getId());
        assertEquals(accountRepository.findByOrganisationWhereAccountOpened(
                        testUtils.getAccountDTO().getOrganisationWhereAccountOpened()).getId(),
                turnoverCommissionValueFromResponse.getAccount().getId());
        assertEquals(testUtils.getTurnoverCommissionValueDTO().getAssetTypeName(),
                turnoverCommissionValueFromResponse.getAssetTypeName());
        assertEquals(TEST_FORMATTED_PERCENT_VALUE_FLOAT,
                turnoverCommissionValueFromResponse.getCommissionPercentValue());
        assertNotNull(turnoverCommissionValueFromResponse.getCreatedAt());
        assertNotNull(turnoverCommissionValueFromResponse.getUpdatedAt());
        assertNotEquals(turnoverCommissionValueFromResponse.getCreatedAt(),
                turnoverCommissionValueFromResponse.getUpdatedAt());
    }

    @Test
    public void notValidUpdateTurnoverCommissionValueIT() throws Exception {
        testUtils.createDefaultTurnoverCommissionValue();

        PercentFloatValueDTO notValidPercentFloatValueDTO = testUtils.getPercentFloatValueDTO();

        notValidPercentFloatValueDTO.setPercentValue(TEST_STRING_FORMAT_PERCENT_VALUE + "111");

        Long createdTurnoverCommissionId = turnoverCommissionValueRepository.findAll().get(0).getId();
        Exception exception = testUtils.perform(put("/data" + TURNOVER_COMMISSION_VALUE_CONTROLLER_PATH
                        + ID_PATH, createdTurnoverCommissionId)
                        .content(asJson(notValidPercentFloatValueDTO))
                        .contentType(APPLICATION_JSON),
                        ADMIN_NAME)
                .andExpect(status().isBadRequest())
                .andReturn().getResolvedException();

        assert exception != null;
        assertEquals(NotValidPercentValueInputFormatException.class, exception.getClass());
        assertEquals(TEST_COMMISSION_PERCENT_VALUE_FLOAT,
                turnoverCommissionValueRepository.findAll().get(0).getCommissionPercentValue());
    }

    @Test
    public void deleteTurnoverCommissionValueIT() throws Exception {
        testUtils.createDefaultTurnoverCommissionValue();

        Long createdTurnoverCommissionValueId = turnoverCommissionValueRepository.findByAccountAndAssetTypeName(
                accountRepository.findAll().get(0), TEST_ASSET_TYPE_NAME).getId();

        testUtils.perform(delete("/data" + TURNOVER_COMMISSION_VALUE_CONTROLLER_PATH + ID_PATH,
                        createdTurnoverCommissionValueId),
                        ADMIN_NAME)
                .andExpect(status().isOk());

        assertNull(turnoverCommissionValueRepository.findByAccountAndAssetTypeName(
                accountRepository.findAll().get(0), TEST_ASSET_TYPE_NAME));
    }
}
