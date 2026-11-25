package ru.csu.iit.master2026.oot.hw01bank;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountTest {

    private static final AccountId ID = new AccountId("40817-001");

    @Test
    void новый_счёт_с_нулевым_балансом() {
        Account debit = Account.debit(ID);
        Account credit = Account.credit(ID, Amount.rubles(1000));

        assertThat(debit.balance()).isZero();
        assertThat(credit.balance()).isZero();
        assertThat(debit.id()).isEqualTo(ID);
    }

    @Test
    void доступно_у_дебетового_равно_балансу() {
        Account debit = Account.debit(ID);
        debit.deposit(Amount.rubles(500));

        assertThat(debit.available()).isEqualTo(Amount.rubles(500));
    }

    @Test
    void доступно_у_кредитного_это_баланс_плюс_лимит() {
        Account credit = Account.credit(ID, Amount.rubles(1000));
        assertThat(credit.available()).isEqualTo(Amount.rubles(1000));

        credit.deposit(Amount.rubles(200));
        assertThat(credit.available()).isEqualTo(Amount.rubles(1200));

        credit.withdraw(Amount.rubles(1100));
        assertThat(credit.balance()).isEqualTo(-90_000);
        assertThat(credit.available()).isEqualTo(Amount.rubles(100));
    }

    @Test
    void дебетовый_можно_опустошить_ровно_в_ноль() {
        Account debit = Account.debit(ID);
        debit.deposit(Amount.rubles(100));

        debit.withdraw(Amount.rubles(100));

        assertThat(debit.balance()).isZero();
        assertThat(debit.available()).isEqualTo(Amount.ZERO);
    }

    @Test
    void дебетовый_не_уходит_в_минус_и_баланс_не_меняется() {
        Account debit = Account.debit(ID);
        debit.deposit(Amount.rubles(100));

        assertThatThrownBy(() -> debit.withdraw(new Amount(10_001)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(debit.balance()).isEqualTo(10_000);
    }

    @Test
    void кредитный_уходит_в_минус_ровно_до_лимита() {
        Account credit = Account.credit(ID, Amount.rubles(1000));

        credit.withdraw(Amount.rubles(1000));

        assertThat(credit.balance()).isEqualTo(-100_000);
        assertThat(credit.available()).isEqualTo(Amount.ZERO);
    }

    @Test
    void кредитный_не_выходит_за_лимит_и_баланс_не_меняется() {
        Account credit = Account.credit(ID, Amount.rubles(1000));
        credit.withdraw(Amount.rubles(400));

        assertThatThrownBy(() -> credit.withdraw(new Amount(60_001)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(credit.balance()).isEqualTo(-40_000);
    }

    @Test
    void кредитный_с_нулевым_лимитом_ведёт_себя_как_дебетовый() {
        Account credit = Account.credit(ID, Amount.ZERO);

        assertThatThrownBy(() -> credit.withdraw(new Amount(1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void счета_равны_по_номеру_а_не_по_балансу_и_не_по_виду() {
        Account a = Account.debit(ID);
        Account b = Account.credit(ID, Amount.rubles(5));
        b.deposit(Amount.rubles(100));

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo(Account.debit(new AccountId("40817-002")));
    }

    @Test
    void счета_с_разными_номерами_и_одинаковым_балансом_не_равны() {
        Account a = Account.debit(new AccountId("1"));
        Account b = Account.debit(new AccountId("2"));
        a.deposit(Amount.rubles(1));
        b.deposit(Amount.rubles(1));

        assertThat(a).isNotEqualTo(b);
    }

    /** Контракт базового типа: снять можно ровно available() и ни копейкой больше — для любого вида счёта. */
    @Test
    void контракт_withdraw_одинаков_для_обоих_видов() {
        for (Account account : new Account[]{
                Account.debit(ID), Account.credit(ID, Amount.rubles(300))}) {
            account.deposit(Amount.rubles(50));
            Amount available = account.available();

            assertThatThrownBy(() -> account.withdraw(available.plus(new Amount(1))))
                    .isInstanceOf(IllegalStateException.class);
            account.withdraw(available);

            assertThat(account.available()).isEqualTo(Amount.ZERO);
        }
    }
}
