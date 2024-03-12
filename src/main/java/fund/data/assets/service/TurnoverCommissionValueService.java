package fund.data.assets.service;

import fund.data.assets.dto.TurnoverCommissionValueDTO;
import fund.data.assets.model.financial_entities.TurnoverCommissionValue;

import java.util.List;

/**
 * Сервис для обслуживания размера комиссии с оборота для типа актива на счёте.
 * Обслуживаемая сущность - {@link fund.data.assets.model.financial_entities.TurnoverCommissionValue}.
 * @version 0.0.1-alpha
 * @author MarkDementev a.k.a JavaMarkDem
 */
public interface TurnoverCommissionValueService {
    TurnoverCommissionValue getTurnoverCommissionValue(Long id);
    List<TurnoverCommissionValue> getTurnoverCommissionValues();
    TurnoverCommissionValue createTurnoverCommissionValue(TurnoverCommissionValueDTO TurnoverCommissionValueDTO);
    TurnoverCommissionValue updateTurnoverCommissionValue(Long id,
                                                          TurnoverCommissionValueDTO TurnoverCommissionValueDTO);
    void deleteTurnoverCommissionValue(Long id);
}
