package com.dongnguyen248.add2num.api.equipment;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/equipments")
public class EquipmentController {

    private final EquipmentService equipmentService;

    public EquipmentController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'SUPERVISOR')")
    public EquipmentListResponse findActive() {
        return equipmentService.findActive();
    }
}
