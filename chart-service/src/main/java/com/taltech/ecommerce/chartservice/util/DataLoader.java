package com.taltech.ecommerce.chartservice.util;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.taltech.ecommerce.chartservice.model.Chart;
import com.taltech.ecommerce.chartservice.model.ChartItem;
import com.taltech.ecommerce.chartservice.repository.ChartRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {
    private final ChartRepository chartRepository;
    @Override
    public void run(String... args) {
        if (chartRepository.count() < 2) {
            // Chart 1
            ChartItem chartItem = new ChartItem();
            chartItem.setInventoryCode("iphone_13");
            chartItem.setPrice(new BigDecimal(1000));
            chartItem.setQuantity(1);
            chartItem.setActive(true);
            chartItem.setInsertDate(LocalDateTime.now());
            chartItem.setUpdateDate(LocalDateTime.now());

            ChartItem chartItem2 = new ChartItem();
            chartItem2.setInventoryCode("samsung_a12");
            chartItem2.setPrice(new BigDecimal(800));
            chartItem2.setQuantity(1);
            chartItem2.setActive(true);
            chartItem2.setInsertDate(LocalDateTime.now());
            chartItem2.setUpdateDate(LocalDateTime.now());

            Chart chart = new Chart();
            chart.setUserId(12345L);
            chart.setChartItems(List.of(chartItem, chartItem2));
            chart.setTotalPrice(new BigDecimal(1800));
            chart.setActive(true);
            chart.setInsertDate(LocalDateTime.now());
            chart.setUpdateDate(LocalDateTime.now());
            chartRepository.save(chart);

            // Chart 2
            ChartItem chartItem3 = new ChartItem();
            chartItem3.setInventoryCode("iphone_13");
            chartItem3.setPrice(new BigDecimal(1000));
            chartItem3.setQuantity(2);
            chartItem3.setActive(true);
            chartItem3.setInsertDate(LocalDateTime.now());
            chartItem3.setUpdateDate(LocalDateTime.now());

            ChartItem chartItem4 = new ChartItem();
            chartItem4.setInventoryCode("samsung_a12");
            chartItem4.setPrice(new BigDecimal(800));
            chartItem4.setQuantity(2);
            chartItem4.setActive(true);
            chartItem4.setInsertDate(LocalDateTime.now());
            chartItem4.setUpdateDate(LocalDateTime.now());

            Chart chart2 = new Chart();
            chart2.setUserId(99999L);
            chart2.setChartItems(List.of(chartItem3, chartItem4));
            chart2.setTotalPrice(new BigDecimal(3600));
            chart2.setActive(true);
            chart2.setInsertDate(LocalDateTime.now());
            chart2.setUpdateDate(LocalDateTime.now());

            chartRepository.saveAll(List.of(chart, chart2));
        }
    }
}
