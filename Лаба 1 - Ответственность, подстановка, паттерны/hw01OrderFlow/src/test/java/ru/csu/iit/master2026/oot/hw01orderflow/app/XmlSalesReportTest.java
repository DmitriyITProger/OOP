package ru.csu.iit.master2026.oot.hw01orderflow.app;

import org.junit.jupiter.api.Test;
import ru.csu.iit.master2026.oot.hw01orderflow.domain.CourierDelivery;
import ru.csu.iit.master2026.oot.hw01orderflow.domain.Customer;
import ru.csu.iit.master2026.oot.hw01orderflow.domain.CustomerId;
import ru.csu.iit.master2026.oot.hw01orderflow.domain.Email;
import ru.csu.iit.master2026.oot.hw01orderflow.domain.ExpressDelivery;
import ru.csu.iit.master2026.oot.hw01orderflow.domain.Order;
import ru.csu.iit.master2026.oot.hw01orderflow.domain.Pickup;
import ru.csu.iit.master2026.oot.hw01orderflow.domain.SameDayDelivery;
import ru.csu.iit.master2026.oot.hw01orderflow.infra.InMemoryOrderRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.csu.iit.master2026.oot.hw01orderflow.domain.Money.rubles;

class XmlSalesReportTest {

    private final InMemoryOrderRepository repository = new InMemoryOrderRepository();
    private final Customer anna = new Customer(new CustomerId("c-1"), "Анна",
            new Email("anna@example.ru"), "+79000000001");
    private final PlaceOrder placeOrder = new PlaceOrder(repository, new RecordingNotifier());

    private void threeOrders() {
        Order paid = placeOrder.place(Order.builder()
                .customer(anna).item("Ноутбук", 1, rubles(64990)).delivery(new ExpressDelivery()));
        paid.pay();
        placeOrder.place(Order.builder()
                .customer(anna).item("Кружка", 1, rubles(350)).delivery(new CourierDelivery()));
        Order shipped = placeOrder.place(Order.builder()
                .customer(anna).item("Кружка", 2, rubles(350)).delivery(new Pickup()));
        shipped.pay();
        shipped.ship();
    }

    @Test
    void три_заказа() {
        threeOrders();

        assertThat(new XmlSalesReport(repository).render()).isEqualTo("""
                <?xml version="1.0" encoding="UTF-8"?>
                <report>
                  <order id="ORD-1" status="PAID" totalKopecks="6549000"/>
                  <order id="ORD-2" status="NEW" totalKopecks="65000"/>
                  <order id="ORD-3" status="SHIPPED" totalKopecks="70000"/>
                  <totals count="3" express="1" revenueKopecks="6619000" largest="ORD-1"/>
                </report>
                """);
    }

    @Test
    void пустое_хранилище_без_заказов_и_без_largest() {
        assertThat(new XmlSalesReport(repository).render()).isEqualTo("""
                <?xml version="1.0" encoding="UTF-8"?>
                <report>
                  <totals count="0" express="0" revenueKopecks="0"/>
                </report>
                """);
    }

    @Test
    void экспресс_в_день_заказа_попадает_в_итоги() {
        threeOrders();
        placeOrder.place(Order.builder()
                .customer(anna).item("Кружка", 1, rubles(350)).delivery(new SameDayDelivery()));

        assertThat(new XmlSalesReport(repository).render())
                .contains("<totals count=\"4\" express=\"2\" revenueKopecks=\"6619000\" largest=\"ORD-1\"/>");
    }

    @Test
    void повторный_вызов_даёт_тот_же_результат() {
        threeOrders();
        var report = new XmlSalesReport(repository);

        assertThat(report.render()).isEqualTo(report.render());
    }

    @Test
    void текстовый_и_csv_отчёты_не_изменились() {
        threeOrders();

        assertThat(new TextSalesReport(repository).render()).isEqualTo("""
                ORD-1  PAID      65490.00 руб.
                ORD-2  NEW       650.00 руб.
                ORD-3  SHIPPED   700.00 руб.
                Заказов: 3, экспресс: 1, выручка: 66190.00 руб., крупнейший: ORD-1""");
        assertThat(new CsvSalesReport(repository).render()).isEqualTo("""
                id;status;total_kopecks
                ORD-1;PAID;6549000
                ORD-2;NEW;65000
                ORD-3;SHIPPED;70000
                TOTAL;3;6619000
                """);
    }
}
