package ru.csu.iit.master2026.oot.hw01orderflow.app;

import ru.csu.iit.master2026.oot.hw01orderflow.domain.Order;

/** Отчёт в XML: суммы в копейках, итоги одним элементом {@code <totals>}. */
public final class XmlSalesReport extends SalesReport {

    public XmlSalesReport(OrderRepository orders) {
        super(orders);
    }

    @Override
    protected void header(StringBuilder out) {
        out.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                .append("<report>\n");
    }

    @Override
    protected void row(StringBuilder out, Order order) {
        out.append("  <order id=\"").append(order.id())
                .append("\" status=\"").append(order.status())
                .append("\" totalKopecks=\"").append(order.total().kopecks())
                .append("\"/>\n");
    }

    @Override
    protected void footer(StringBuilder out, Totals totals) {
        out.append("  <totals count=\"").append(totals.count())
                .append("\" express=\"").append(totals.express())
                .append("\" revenueKopecks=\"").append(totals.revenue().kopecks()).append('"');
        totals.largest().ifPresent(id -> out.append(" largest=\"").append(id).append('"'));
        out.append("/>\n")
                .append("</report>\n");
    }
}
