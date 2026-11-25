package ru.csu.iit.master2026.oot.hw01bank;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommandsTest {

    private final Account anna = Account.debit(new AccountId("A"));
    private final Account boris = Account.debit(new AccountId("B"));
    private final Account credit = Account.credit(new AccountId("C"), Amount.rubles(1000));

    // --- Deposit ---

    @Test
    void пополнение_и_отмена() {
        Command deposit = new Deposit(anna, Amount.rubles(100));

        deposit.execute();
        assertThat(anna.balance()).isEqualTo(10_000);

        deposit.undo();
        assertThat(anna.balance()).isZero();
    }

    @Test
    void отмена_пополнения_когда_денег_уже_нет_бросает_и_ничего_не_меняет() {
        Command deposit = new Deposit(anna, Amount.rubles(100));
        deposit.execute();
        anna.withdraw(Amount.rubles(60));

        assertThatThrownBy(deposit::undo).isInstanceOf(IllegalStateException.class);
        assertThat(anna.balance()).isEqualTo(4_000);
    }

    // --- Withdraw ---

    @Test
    void снятие_и_отмена() {
        anna.deposit(Amount.rubles(100));
        Command withdraw = new Withdraw(anna, Amount.rubles(30));

        withdraw.execute();
        assertThat(anna.balance()).isEqualTo(7_000);

        withdraw.undo();
        assertThat(anna.balance()).isEqualTo(10_000);
    }

    @Test
    void снятие_больше_доступного_бросает_и_баланс_не_меняется() {
        anna.deposit(Amount.rubles(10));

        assertThatThrownBy(() -> new Withdraw(anna, Amount.rubles(11)).execute())
                .isInstanceOf(IllegalStateException.class);
        assertThat(anna.balance()).isEqualTo(1_000);
    }

    @Test
    void снятие_с_кредитного_до_лимита_и_отмена() {
        Command withdraw = new Withdraw(credit, Amount.rubles(1000));

        withdraw.execute();
        assertThat(credit.balance()).isEqualTo(-100_000);

        withdraw.undo();
        assertThat(credit.balance()).isZero();
    }

    // --- Transfer ---

    @Test
    void перевод_меняет_оба_счёта_и_отменяется() {
        anna.deposit(Amount.rubles(500));
        Command transfer = new Transfer(anna, boris, Amount.rubles(200));

        transfer.execute();
        assertThat(anna.balance()).isEqualTo(30_000);
        assertThat(boris.balance()).isEqualTo(20_000);

        transfer.undo();
        assertThat(anna.balance()).isEqualTo(50_000);
        assertThat(boris.balance()).isZero();
    }

    @Test
    void перевод_без_денег_не_меняет_ни_один_счёт() {
        anna.deposit(Amount.rubles(100));
        boris.deposit(Amount.rubles(7));

        assertThatThrownBy(() -> new Transfer(anna, boris, Amount.rubles(101)).execute())
                .isInstanceOf(IllegalStateException.class);

        assertThat(anna.balance()).isEqualTo(10_000);
        assertThat(boris.balance()).isEqualTo(700);
    }

    @Test
    void отмена_перевода_когда_у_получателя_денег_уже_нет_не_меняет_ни_один_счёт() {
        anna.deposit(Amount.rubles(100));
        Command transfer = new Transfer(anna, boris, Amount.rubles(100));
        transfer.execute();
        boris.withdraw(Amount.rubles(40));

        assertThatThrownBy(transfer::undo).isInstanceOf(IllegalStateException.class);

        assertThat(anna.balance()).isZero();
        assertThat(boris.balance()).isEqualTo(6_000);
    }

    @Test
    void перевод_с_кредитного_счёта_в_пределах_лимита() {
        Command transfer = new Transfer(credit, anna, Amount.rubles(1000));

        transfer.execute();

        assertThat(credit.balance()).isEqualTo(-100_000);
        assertThat(anna.balance()).isEqualTo(100_000);
    }

    @Test
    void перевод_на_тот_же_счёт_запрещён_в_конструкторе() {
        assertThatThrownBy(() -> new Transfer(anna, anna, Amount.rubles(1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void тот_же_счёт_определяется_по_номеру() {
        Account sameNumber = Account.debit(new AccountId("A"));

        assertThatThrownBy(() -> new Transfer(anna, sameNumber, Amount.rubles(1)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
