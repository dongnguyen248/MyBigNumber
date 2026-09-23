package com.dongnguyen248.add2num.api;

import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dongnguyen248.add2num.api.equipment.Equipment;
import com.dongnguyen248.add2num.api.equipment.EquipmentRepository;
import com.dongnguyen248.add2num.api.equipment.EquipmentStatus;
import com.dongnguyen248.add2num.api.workorder.WorkOrderRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class WorkOrderApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private WorkOrderRepository workOrderRepository;

    @BeforeEach
    void setUp() {
        workOrderRepository.deleteAll();
        equipmentRepository.deleteAll();
        equipmentRepository.save(new Equipment("EQ-001", "Hydraulic Pump", EquipmentStatus.ACTIVE));
        equipmentRepository.save(new Equipment("EQ-002", "Inactive Motor", EquipmentStatus.INACTIVE));
    }

    @Test
    @WithMockUser(username = "technician01", roles = "TECHNICIAN")
    void technicianCanCreateWorkOrderForActiveEquipment() throws Exception {
        mockMvc.perform(post("/api/work-orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "equipmentId": "EQ-001",
                                  "description": "Inspect hydraulic pump",
                                  "priority": "HIGH"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.equipmentId", is("EQ-001")))
                .andExpect(jsonPath("$.priority", is("HIGH")))
                .andExpect(jsonPath("$.status", is("NEW")))
                .andExpect(jsonPath("$.createdBy", is("technician01")));
    }

    @Test
    @WithMockUser(username = "technician01", roles = "TECHNICIAN")
    void inactiveEquipmentIsRejectedWithProblemDetails() throws Exception {
        mockMvc.perform(post("/api/work-orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "equipmentId": "EQ-002",
                                  "description": "Inspect motor",
                                  "priority": "LOW"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type", is("https://example.invalid/problems/bad-request")))
                .andExpect(jsonPath("$.title", is("Bad Request")))
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    @WithMockUser(username = "supervisor01", roles = "SUPERVISOR")
    void supervisorCannotCreateWorkOrder() throws Exception {
        mockMvc.perform(post("/api/work-orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "equipmentId": "EQ-001",
                                  "description": "Inspect hydraulic pump",
                                  "priority": "HIGH"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "technician01", roles = "TECHNICIAN")
    void blankDescriptionIsRejectedWithValidationProblem() throws Exception {
        mockMvc.perform(post("/api/work-orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "equipmentId": "EQ-001",
                                  "description": "",
                                  "priority": "HIGH"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("Validation Failed")))
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    @WithMockUser(username = "supervisor01", roles = "SUPERVISOR")
    void supervisorCanMoveWorkOrderToInProgressButCannotSkipState() throws Exception {
        createWorkOrder();
      Long workOrderId = workOrderRepository.findAll().get(0).getId();

      mockMvc.perform(patch("/api/work-orders/{id}/status", workOrderId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")));

        mockMvc.perform(patch("/api/work-orders/{id}/status", workOrderId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("Invalid State Transition")));
    }

    private void createWorkOrder() throws Exception {
        mockMvc.perform(post("/api/work-orders")
                        .with(csrf())
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("technician01").roles("TECHNICIAN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"equipmentId\":\"EQ-001\",\"description\":\"Inspect pump\",\"priority\":\"HIGH\"}"))
                .andExpect(status().isCreated());
    }
}
