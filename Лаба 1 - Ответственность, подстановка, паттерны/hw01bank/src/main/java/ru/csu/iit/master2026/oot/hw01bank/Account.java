package ru.csu.iit.master2026.oot.hw01bank;

import java.util.Objects;

/**
 * Счёт клиента — сущность: равенство по номеру, баланс меняется.
 *
 * <p>Два вида счетов:
 * <ul>
 *   <li>дебетовый — баланс никогда не меньше нуля;</li>
 *   <li>кредитный с лимитом — баланс никогда не меньше {@code -limit}.</li>
 * </ul>
 *
 * <p>Как устроить два вида внутри — решать вам; снаружи доступны только фабрики ниже.
 * Добавлять поля, классы и интерфейсы можно, менять публичные сигнатуры — нельзя.
 *
 * <p>Вид счёта — это не подкласс, а одно число: нижняя граница баланса {@code floor}
 * (0 у дебетового, {@code -limit} у кредитного). Поэтому у {@code withdraw} один контракт
 * для всех счетов: снять можно не больше {@link #available()}.
 */
public final class Account {

    private final AccountId id;
    /** Ниже этого баланс опускаться не может. */
    private final long floor;
    private long balance;

    private Account(AccountId id, long floor) {
        this.id = Objects.requireNonNull(id);
        this.floor = floor;
    }

    /** Дебетовый счёт с нулевым балансом. */
    public static Account debit(AccountId id) {
        return new Account(id, 0);
    }

    /** Кредитный счёт с нулевым балансом и лимитом {@code limit}. */
    public static Account credit(AccountId id, Amount limit) {
        Objects.requireNonNull(limit);
        return new Account(id, -limit.kopecks());
    }

    public AccountId id() {
        return id;
    }

    /** Баланс в копейках; отрицательный — только у кредитного счёта. */
    public long balance() {
        return balance;
    }

    /** Сколько можно снять прямо сейчас, не нарушив инвариант счёта. */
    public Amount available() {
        return new Amount(balance - floor);
    }

    public void deposit(Amount amount) {
        balance = Math.addExact(balance, amount.kopecks());
    }

    /**
     * @throws IllegalStateException если {@code amount} больше {@link #available()};
     *                               баланс при этом не меняется
     */
    public void withdraw(Amount amount) {
        if (!available().isAtLeast(amount)) {
            throw new IllegalStateException(
                    "Недостаточно средств на счёте " + id + ": доступно " + available() + ", нужно " + amount);
        }
        balance -= amount.kopecks();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Account other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Account " + id;
    }
}
