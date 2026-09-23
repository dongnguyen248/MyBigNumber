package com.dongnguyen248.add2num.api.equipment;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipmentRepository extends JpaRepository<Equipment, String> {

    List<Equipment> findByStatusOrderByEquipmentId(EquipmentStatus status);

    boolean existsByEquipmentIdAndStatus(String equipmentId, EquipmentStatus status);
}
