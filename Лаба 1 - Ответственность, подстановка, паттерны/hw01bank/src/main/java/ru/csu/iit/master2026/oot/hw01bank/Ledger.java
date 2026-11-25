package ru.csu.iit.master2026.oot.hw01bank;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

/** Журнал операций: выполняет команды и отменяет последнюю выполненную. */
public final class Ledger {

    private final Deque<Command> done = new ArrayDeque<>();

    /** Выполняет команду и запоминает её. Если команда бросила исключение — она не запоминается. */
    public void run(Command command) {
        Objects.requireNonNull(command).execute();
        done.push(command);
    }

    /**
     * Отменяет последнюю выполненную команду и забывает её.
     * Если отмена бросила исключение — команда остаётся в журнале.
     *
     * @throws IllegalStateException если отменять нечего
     */
    public void undoLast() {
        Command last = done.peek();
        if (last == null) {
            throw new IllegalStateException("Нечего отменять");
        }
        last.undo();
        done.pop();
    }

    /** Сколько команд можно отменить. */
    public int size() {
        return done.size();
    }
}
