package ru.csu.iit.master2026.oot.hw01orderflow.app;

import org.junit.jupiter.api.Test;
import ru.csu.iit.master2026.oot.hw01orderflow.domain.CourierDelivery;
import ru.csu.iit.master2026.oot.hw01orderflow.domain.Customer;
import ru.csu.iit.master2026.oot.hw01orderflow.domain.CustomerId;
import ru.csu.iit.master2026.oot.hw01orderflow.domain.Email;
import ru.csu.iit.master2026.oot.hw01orderflow.domain.Order;
import ru.csu.iit.master2026.oot.hw01orderflow.domain.OrderId;
import ru.csu.iit.master2026.oot.hw01orderflow.domain.OrderStatus;
import ru.csu.iit.master2026.oot.hw01orderflow.infra.InMemoryOrderRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.csu.iit.master2026.oot.hw01orderflow.domain.Money.rubles;

class ReturnOrderTest {

    private final Customer anna = new Customer(new CustomerId("c-1"), "Анна",
            new Email("anna@example.ru"), "+79000000001");

    private final InMemoryOrderRepository repository = new InMemoryOrderRepository();
    private final RecordingNotifier notifier = new RecordingNotifier();
    private final PlaceOrder placeOrder = new PlaceOrder(repository, notifier);
    private final ReturnOrder returnOrder = new ReturnOrder(repository, notifier);

    private Order place() {
        notifier.events.clear();
        return placeOrder.place(Order.builder().customer(anna)
                .item("Кружка", 1, rubles(350)).delivery(new CourierDelivery()));
    }

    @Test
    void возврат_отправленного_заказа_с_полной_суммой() {
        Order order = place();
        order.pay();
        order.ship();
        notifier.events.clear();

        returnOrder.returnOrder(order.id());

        assertThat(repository.find(order.id()).orElseThrow().status()).isEqualTo(OrderStatus.RETURNED);
        assertThat(notifier.events).containsExactly("cancelled ORD-1 refund 650.00 руб.");
    }

    @Test
    void возврат_неотправленного_заказа_запрещён_и_без_уведомления() {
        Order order = place();
        order.pay();
        notifier.events.clear();

        assertThatThrownBy(() -> returnOrder.returnOrder(order.id()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(order.status()).isEqualTo(OrderStatus.PAID);
        assertThat(notifier.events).isEmpty();
    }

    @Test
    void повторный_возврат_запрещён() {
        Order order = place();
        order.pay();
        order.ship();
        returnOrder.returnOrder(order.id());
        notifier.events.clear();

        assertThatThrownBy(() -> returnOrder.returnOrder(order.id()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(notifier.events).isEmpty();
    }

    @Test
    void несуществующий_заказ() {
        assertThatThrownBy(() -> returnOrder.returnOrder(new OrderId("ORD-99")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(notifier.events).isEmpty();
    }

    @Test
    void возвращённый_заказ_не_входит_в_выручку_отчёта() {
        Order order = place();
        order.pay();
        order.ship();
        returnOrder.returnOrder(order.id());

        assertThat(new TextSalesReport(repository).render()).isEqualTo("""
                ORD-1  RETURNED  650.00 руб.
                Заказов: 1, экспресс: 0, выручка: 0.00 руб., крупнейший: ORD-1""");
    }
}
