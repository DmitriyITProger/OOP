package ru.csu.iit.master2026.oot.hw01bank;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AmountTest {

    @Test
    void отрицательной_суммы_не_бывает() {
        assertThatThrownBy(() -> new Amount(-1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Amount.rubles(-1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void рубли_переводятся_в_копейки() {
        assertThat(Amount.rubles(12).kopecks()).isEqualTo(1200);
        assertThat(Amount.ZERO.kopecks()).isZero();
    }

    @Test
    void равенство_по_значению_и_hashCode() {
        assertThat(new Amount(500)).isEqualTo(Amount.rubles(5));
        assertThat(new Amount(500)).hasSameHashCodeAs(Amount.rubles(5));
        assertThat(new Amount(500)).isNotEqualTo(new Amount(501));

        Set<Amount> set = new HashSet<>();
        set.add(new Amount(500));
        assertThat(set).contains(Amount.rubles(5));
    }

    @Test
    void сложение_не_меняет_слагаемые() {
        Amount a = Amount.rubles(2);
        Amount b = new Amount(30);

        assertThat(a.plus(b)).isEqualTo(new Amount(230));
        assertThat(a).isEqualTo(Amount.rubles(2));
        assertThat(b).isEqualTo(new Amount(30));
    }

    @Test
    void вычитание() {
        assertThat(Amount.rubles(5).minus(Amount.rubles(2))).isEqualTo(Amount.rubles(3));
        assertThat(Amount.rubles(2).minus(Amount.rubles(2))).isEqualTo(Amount.ZERO);
    }

    @Test
    void вычитание_в_минус_запрещено() {
        assertThatThrownBy(() -> Amount.rubles(1).minus(Amount.rubles(2)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void сравнение_isAtLeast() {
        assertThat(Amount.rubles(2).isAtLeast(Amount.rubles(2))).isTrue();
        assertThat(Amount.rubles(3).isAtLeast(Amount.rubles(2))).isTrue();
        assertThat(Amount.rubles(1).isAtLeast(Amount.rubles(2))).isFalse();
    }

    @Test
    void текстовый_вид() {
        assertThat(new Amount(123_405).toString()).isEqualTo("1234.05 руб.");
        assertThat(new Amount(5).toString()).isEqualTo("0.05 руб.");
        assertThat(Amount.ZERO.toString()).isEqualTo("0.00 руб.");
    }
}
