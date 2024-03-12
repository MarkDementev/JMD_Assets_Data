package fund.data.assets.model.asset.relationship;

import fund.data.assets.model.asset.Asset;
import fund.data.assets.model.financial_entities.Account;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class FinancialAssetRelationship extends AssetRelationship {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    public FinancialAssetRelationship(Asset asset, Account account) {
        super(asset);

        this.account = account;
    }
}