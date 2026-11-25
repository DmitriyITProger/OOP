package ru.csu.iit.master2026.oot.hw01orderflow.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.csu.iit.master2026.oot.hw01orderflow.domain.Money.rubles;

class DeliveryTariffTest {

    @Test
    void курьер_платный_до_5000() {
        assertThat(new CourierDelivery().cost(rubles(4999))).isEqualTo(rubles(300));
    }

    @Test
    void курьер_бесплатный_от_5000() {
        assertThat(new CourierDelivery().cost(rubles(5000))).isEqualTo(Money.ZERO);
    }

    @Test
    void экспресс_500_на_следующий_день() {
        var express = new ExpressDelivery();

        assertThat(express.cost(rubles(64990))).isEqualTo(rubles(500));
        assertThat(express.days()).isEqualTo(1);
    }

    @Test
    void в_день_заказа_900_при_любой_сумме_срок_0() {
        var sameDay = new SameDayDelivery();

        assertThat(sameDay.cost(rubles(350))).isEqualTo(rubles(900));
        assertThat(sameDay.cost(rubles(64990))).isEqualTo(rubles(900));
        assertThat(sameDay.days()).isEqualTo(0);
    }

    @Test
    void экспрессами_себя_называют_только_экспресс_и_в_день_заказа() {
        assertThat(new ExpressDelivery().express()).isTrue();
        assertThat(new SameDayDelivery().express()).isTrue();
        assertThat(new CourierDelivery().express()).isFalse();
        assertThat(new Pickup().express()).isFalse();
    }
}
