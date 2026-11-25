package ru.csu.iit.master2026.oot.hw01orderflow.domain;

/** Экспресс в день заказа: 900 руб. при любой сумме, срок 0 дней. */
public record SameDayDelivery() implements DeliveryTariff {

    private static final Money PRICE = Money.rubles(900);

    @Override
    public Money cost(Money goods) {
        return PRICE;
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
        return "привезём сегодня.";
    }
}
