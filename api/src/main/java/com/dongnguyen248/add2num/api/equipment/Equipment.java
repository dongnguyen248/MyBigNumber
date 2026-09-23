package com.dongnguyen248.add2num.api.equipment;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "equipments")
public class Equipment {

    @Id
    private String equipmentId;

    private String name;

    @Enumerated(EnumType.STRING)
    private EquipmentStatus status;

    protected Equipment() {
    }

    public Equipment(String equipmentId, String name, EquipmentStatus status) {
        this.equipmentId = equipmentId;
        this.name = name;
        this.status = status;
    }

    public String getEquipmentId() {
        return equipmentId;
    }

    public String getName() {
        return name;
    }

    public EquipmentStatus getStatus() {
        return status;
    }
}
