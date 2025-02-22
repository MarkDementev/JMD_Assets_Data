package fund.data.assets.model.asset.exchange;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import fund.data.assets.model.asset.Asset;
import fund.data.assets.model.financial_entities.Account;
import fund.data.assets.utils.AutoSelector;
import fund.data.assets.utils.enums.AssetCurrency;
import fund.data.assets.utils.enums.CommissionSystem;
import fund.data.assets.utils.enums.TaxSystem;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Table;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

import java.util.Map;

/**
 * Биржевой актив - сущность для начала конкретизации сути актива.
 * Абстрактный класс - наследник абстрактного Asset.
 * @version 0.1-b
 * @author MarkDementev a.k.a JavaMarkDem
 */
@Entity
@Table(name = "exchange_assets")
@Inheritance(strategy = InheritanceType.JOINED)
@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({@JsonSubTypes.Type(value = FixedRateBondPackage.class)})
@NoArgsConstructor
@Getter
@Setter
public abstract class ExchangeAsset extends Asset {
    /**
     * Условия валидации поля соответствуют международному стандарту для ISIN.
     * Источник о стандарте - https://ru.wikipedia.org/wiki/Международный_идентификационный_код_ценной_бумаги
     */
    @NotNull
    @Size(min = 12, max = 12)
    @Pattern(regexp = "^[A-Z]{2}[A-Z0-9]{9}[0-9]$")
    private String iSIN;

    /**
     * Любой биржевой актив имеет эмитента - организацию, его выпустившую. Данное поле содержит его наименование
     * в свободной форме.
     */
    @NotBlank
    private String assetIssuerTitle;

    /**
     * В определении состояния актива на учёте фонда важна дата его последнего приобретения или продажи.
     * Её достаточно знать в формате ГГГГ-ММ-ДД.
     * Она должна существовать помимо полей createdAt и updatedAt, т.к. даты создания и обновления записи в БД
     * могут отличаться от даты фактического совершения сделки на бирже.
     */
    @NotNull
    private LocalDate lastAssetBuyOrSellDate;

    /**
     * Тип системы сбора комиссии за брокерское и иное обслуживание по активу. Не содержит в себе числовые значения,
     * они находятся в отдельных сущностях. Пока что тип выбирается и инициализируется в конструкторе при создании.
     */
    @Enumerated(EnumType.STRING)
    private CommissionSystem assetCommissionSystem;

    public ExchangeAsset(AssetCurrency assetCurrency, String assetTypeName, String assetTitle, Integer assetCount,
                         TaxSystem assetTaxSystem, Map<String, Float> assetOwnersWithAssetCounts, Account account,
                         String iSIN, String assetIssuerTitle, LocalDate lastAssetBuyOrSellDate) {
        super(assetCurrency, assetTypeName, assetTitle, assetCount, assetTaxSystem, assetOwnersWithAssetCounts,
                account);

        this.iSIN = iSIN;
        this.assetIssuerTitle = assetIssuerTitle;
        this.lastAssetBuyOrSellDate = lastAssetBuyOrSellDate;
        this.assetCommissionSystem = (CommissionSystem) AutoSelector.selectAssetOperationsCostSystem(assetCurrency,
                assetTypeName, AutoSelector.COMMISSION_SYSTEM_CHOOSE);
    }
}
