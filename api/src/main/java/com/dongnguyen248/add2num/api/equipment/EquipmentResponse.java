package com.dongnguyen248.add2num.api.equipment;

public record EquipmentResponse(String equipmentId, String name, EquipmentStatus status) {

    public static EquipmentResponse from(Equipment equipment) {
        return new EquipmentResponse(equipment.getEquipmentId(), equipment.getName(), equipment.getStatus());
    }
}
