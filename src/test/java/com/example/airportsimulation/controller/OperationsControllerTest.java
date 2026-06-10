package com.example.airportsimulation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.airportsimulation.service.OperationsInsightService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OperationsController.class)
class OperationsControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OperationsInsightService operationsInsightService;

    @Test
    void exportedRecordsCsvStartsWithUtf8BomForSpreadsheetCompatibility() throws Exception {
        when(operationsInsightService.exportRecordsCsv()).thenReturn("时间,说明\n2026-06-10,航班延误\n");

        byte[] body = mockMvc.perform(get("/operations/export/records.csv"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "text/csv;charset=UTF-8"))
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        assertThat(body).startsWith((byte) 0xEF, (byte) 0xBB, (byte) 0xBF);
    }

    @Test
    void exportedStatisticsCsvStartsWithUtf8BomForSpreadsheetCompatibility() throws Exception {
        when(operationsInsightService.exportStatisticsCsv()).thenReturn("指标,数值\n航班总数,12\n");

        byte[] body = mockMvc.perform(get("/operations/export/statistics.csv"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "text/csv;charset=UTF-8"))
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        assertThat(body).startsWith((byte) 0xEF, (byte) 0xBB, (byte) 0xBF);
    }
}
