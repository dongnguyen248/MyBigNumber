package com.dongnguyen248.add2num.api.equipment;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;

    public EquipmentService(EquipmentRepository equipmentRepository) {
        this.equipmentRepository = equipmentRepository;
    }

    public EquipmentListResponse findActive() {
        List<EquipmentResponse> items = equipmentRepository.findByStatusOrderByEquipmentId(EquipmentStatus.ACTIVE)
                .stream()
                .map(EquipmentResponse::from)
                .toList();
        return new EquipmentListResponse(items);
    }
}
