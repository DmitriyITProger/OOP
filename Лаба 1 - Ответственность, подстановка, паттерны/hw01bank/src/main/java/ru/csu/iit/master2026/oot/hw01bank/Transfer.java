package ru.csu.iit.master2026.oot.hw01bank;

import java.util.Objects;

/**
 * Перевод между счетами. Атомарен: и выполнение, и отмена либо меняют оба счёта, либо ни одного.
 */
public final class Transfer implements Command {

    private final Account from;
    private final Account to;
    private final Amount amount;

    /** @throws IllegalArgumentException если {@code from} и {@code to} — один и тот же счёт */
    public Transfer(Account from, Account to, Amount amount) {
        this.from = Objects.requireNonNull(from);
        this.to = Objects.requireNonNull(to);
        this.amount = Objects.requireNonNull(amount);
        if (from.equals(to)) {
            throw new IllegalArgumentException("Перевод на тот же счёт: " + from.id());
        }
    }

    @Override
    public void execute() {
        move(from, to);
    }

    @Override
    public void undo() {
        move(to, from);
    }

    /**
     * Сначала снимаем: если денег нет, {@code withdraw} бросит исключение, и ничего не изменилось.
     * Если не удалось пополнить (переполнение суммы), возвращаем снятое обратно.
     */
    private void move(Account source, Account target) {
        source.withdraw(amount);
        try {
            target.deposit(amount);
        } catch (RuntimeException e) {
            source.deposit(amount);
            throw e;
        }
    }
}
