package ru.csu.iit.master2026.oot.hw01orderflow.infra;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.csu.iit.master2026.oot.hw01orderflow.app.PlaceOrder;
import ru.csu.iit.master2026.oot.hw01orderflow.domain.CourierDelivery;
import ru.csu.iit.master2026.oot.hw01orderflow.domain.Customer;
import ru.csu.iit.master2026.oot.hw01orderflow.domain.CustomerId;
import ru.csu.iit.master2026.oot.hw01orderflow.domain.DeliveryTariff;
import ru.csu.iit.master2026.oot.hw01orderflow.domain.Email;
import ru.csu.iit.master2026.oot.hw01orderflow.domain.ExpressDelivery;
import ru.csu.iit.master2026.oot.hw01orderflow.domain.Money;
import ru.csu.iit.master2026.oot.hw01orderflow.domain.Order;
import ru.csu.iit.master2026.oot.hw01orderflow.domain.Pickup;
import ru.csu.iit.master2026.oot.hw01orderflow.domain.SameDayDelivery;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.csu.iit.master2026.oot.hw01orderflow.domain.Money.rubles;

class ConsoleNotifierTest {

    private final PrintStream originalOut = System.out;
    private final ByteArrayOutputStream captured = new ByteArrayOutputStream();

    @BeforeEach
    void captureOutput() {
        System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void restoreOutput() {
        System.setOut(originalOut);
    }

    private String placeOrderWith(DeliveryTariff delivery) {
        Customer anna = new Customer(new CustomerId("c-1"), "Анна",
                new Email("anna@example.ru"), "+79000000001");
        new PlaceOrder(new InMemoryOrderRepository(), new ConsoleNotifier()).place(Order.builder()
                .customer(anna)
                .item("Кружка", 1, rubles(350))
                .delivery(delivery));
        return captured.toString(StandardCharsets.UTF_8).strip();
    }

    @Test
    void в_день_заказа_обещает_сегодня() {
        assertThat(placeOrderWith(new SameDayDelivery())).isEqualTo(
                "[EMAIL anna@example.ru] Анна, заказ ORD-1 принят. Сумма: 1250.00 руб."
                        + " Экспресс-доставка: привезём сегодня.");
    }

    @Test
    void экспресс_обещает_завтра() {
        assertThat(placeOrderWith(new ExpressDelivery())).isEqualTo(
                "[EMAIL anna@example.ru] Анна, заказ ORD-1 принят. Сумма: 850.00 руб."
                        + " Экспресс-доставка: привезём завтра.");
    }

    @Test
    void курьер_без_обещания() {
        assertThat(placeOrderWith(new CourierDelivery())).isEqualTo(
                "[EMAIL anna@example.ru] Анна, заказ ORD-1 принят. Сумма: 650.00 руб.");
    }

    @Test
    void самовывоз_без_обещания() {
        assertThat(placeOrderWith(new Pickup())).isEqualTo(
                "[EMAIL anna@example.ru] Анна, заказ ORD-1 принят. Сумма: 350.00 руб.");
    }

    @Test
    void новый_экспресс_с_собственным_текстом_работает_без_правок_уведомителя() {
        DeliveryTariff expressPickup = new DeliveryTariff() {
            @Override
            public Money cost(Money goods) {
                return Money.ZERO;
            }

            @Override
            public int days() {
                return 0;
            }

            @Override
            public boolean express() {
                return true;
            }

            @Override
            public String promise() {
                return "ждём на выдаче через час.";
            }
        };

        assertThat(placeOrderWith(expressPickup)).endsWith("Экспресс-доставка: ждём на выдаче через час.");
    }
}
