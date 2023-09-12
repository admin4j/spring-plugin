package com.admin4j.spring.plugin.provider.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author andanyang
 * @since 2023/9/12 15:03
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChargeQuery {
    private BigDecimal money;
    private String channel;
}
