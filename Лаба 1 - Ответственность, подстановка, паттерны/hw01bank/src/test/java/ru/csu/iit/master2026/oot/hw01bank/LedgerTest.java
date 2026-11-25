package ru.csu.iit.master2026.oot.hw01bank;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LedgerTest {

    private final Account anna = Account.debit(new AccountId("A"));
    private final Account boris = Account.debit(new AccountId("B"));
    private final Ledger ledger = new Ledger();

    @Test
    void пустой_журнал_отменять_нечего() {
        assertThat(ledger.size()).isZero();
        assertThatThrownBy(ledger::undoLast).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void run_выполняет_и_запоминает() {
        ledger.run(new Deposit(anna, Amount.rubles(100)));

        assertThat(anna.balance()).isEqualTo(10_000);
        assertThat(ledger.size()).isEqualTo(1);
    }

    @Test
    void упавшая_команда_не_запоминается() {
        assertThatThrownBy(() -> ledger.run(new Withdraw(anna, Amount.rubles(1))))
                .isInstanceOf(IllegalStateException.class);

        assertThat(ledger.size()).isZero();
        assertThat(anna.balance()).isZero();
    }

    @Test
    void отменяются_команды_в_обратном_порядке() {
        ledger.run(new Deposit(anna, Amount.rubles(100)));
        ledger.run(new Transfer(anna, boris, Amount.rubles(40)));
        ledger.run(new Withdraw(boris, Amount.rubles(10)));
        assertThat(ledger.size()).isEqualTo(3);

        ledger.undoLast();
        assertThat(boris.balance()).isEqualTo(4_000);
        assertThat(ledger.size()).isEqualTo(2);

        ledger.undoLast();
        assertThat(anna.balance()).isEqualTo(10_000);
        assertThat(boris.balance()).isZero();

        ledger.undoLast();
        assertThat(anna.balance()).isZero();
        assertThat(ledger.size()).isZero();
    }

    @Test
    void упавшая_отмена_оставляет_команду_в_журнале() {
        ledger.run(new Deposit(anna, Amount.rubles(100)));
        anna.withdraw(Amount.rubles(100));

        assertThatThrownBy(ledger::undoLast).isInstanceOf(IllegalStateException.class);

        assertThat(ledger.size()).isEqualTo(1);
        assertThat(anna.balance()).isZero();
    }

    @Test
    void после_исправления_причины_отмену_можно_повторить() {
        ledger.run(new Deposit(anna, Amount.rubles(100)));
        anna.withdraw(Amount.rubles(100));
        assertThatThrownBy(ledger::undoLast).isInstanceOf(IllegalStateException.class);

        anna.deposit(Amount.rubles(100));
        ledger.undoLast();

        assertThat(ledger.size()).isZero();
        assertThat(anna.balance()).isZero();
    }
}
