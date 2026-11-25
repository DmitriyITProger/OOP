package ru.csu.iit.master2026.oot.hw01bank;

import java.util.Objects;

/** Пополнение счёта. Отмена снимает те же деньги — если они ещё на счёте. */
public final class Deposit implements Command {

    private final Account account;
    private final Amount amount;

    public Deposit(Account account, Amount amount) {
        this.account = Objects.requireNonNull(account);
        this.amount = Objects.requireNonNull(amount);
    }

    @Override
    public void execute() {
        account.deposit(amount);
    }

    /** Снимает ту же сумму; если денег уже нет, {@code withdraw} бросит {@link IllegalStateException}. */
    @Override
    public void undo() {
        account.withdraw(amount);
    }
}
