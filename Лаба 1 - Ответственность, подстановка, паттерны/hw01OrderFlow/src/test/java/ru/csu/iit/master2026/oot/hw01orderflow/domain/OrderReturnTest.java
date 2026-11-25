package ru.csu.iit.master2026.oot.hw01orderflow.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.csu.iit.master2026.oot.hw01orderflow.domain.Money.rubles;

class OrderReturnTest {

    private final Customer customer = new Customer(new CustomerId("c-1"), "Анна",
            new Email("anna@example.ru"), "+79000000001");

    private Order newOrder() {
        return Order.builder().customer(customer).item("Кружка", 1, rubles(350))
                .delivery(new CourierDelivery()).build(new OrderId("ORD-1"));
    }

    private Order shippedOrder() {
        Order order = newOrder();
        order.pay();
        order.ship();
        return order;
    }

    @Test
    void отправленный_заказ_можно_вернуть() {
        Order order = shippedOrder();

        order.returnOrder();

        assertThat(order.status()).isEqualTo(OrderStatus.RETURNED);
        assertThat(String.valueOf(order.status())).isEqualTo("RETURNED");
    }

    @Test
    void возвращённый_заказ_не_оплачен() {
        Order order = shippedOrder();
        assertThat(order.status().paid()).isTrue();

        order.returnOrder();

        assertThat(order.status().paid()).isFalse();
    }

    @Test
    void из_NEW_PAID_и_CANCELLED_вернуть_нельзя_статус_не_меняется() {
        Order fresh = newOrder();
        Order paid = newOrder();
        paid.pay();
        Order cancelled = newOrder();
        cancelled.cancel();

        for (Order order : new Order[]{fresh, paid, cancelled}) {
            OrderStatus before = order.status();

            assertThatThrownBy(order::returnOrder).isInstanceOf(IllegalStateException.class);
            assertThat(order.status()).isEqualTo(before);
        }
    }

    @Test
    void из_RETURNED_ничего_нельзя() {
        Order order = shippedOrder();
        order.returnOrder();

        assertThatThrownBy(order::pay).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(order::ship).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(order::cancel).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(order::returnOrder).isInstanceOf(IllegalStateException.class);
        assertThat(order.status()).isEqualTo(OrderStatus.RETURNED);
    }

    @Test
    void старые_переходы_не_изменились() {
        Order shipped = shippedOrder();
        assertThatThrownBy(shipped::cancel).isInstanceOf(IllegalStateException.class);
        assertThat(shipped.status()).isEqualTo(OrderStatus.SHIPPED);

        Order paid = newOrder();
        paid.pay();
        paid.cancel();
        assertThat(paid.status()).isEqualTo(OrderStatus.CANCELLED);
    }
}
