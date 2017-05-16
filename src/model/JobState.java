package model;

/**
 * Перечисление возможных состояний задания
 * <p>
 * Created by Andrey Semenyuk on 2017.
 */
public enum JobState {
    ACTIVE("Активно"),
    EDITABLE("Редактируемое"),
    EMPTY(""),
    COMPLETED("Завершено"),
    NEW("Новое"),
    CHECKIN("Регистрация"),
    ERROR("Ошибка");

    private String description;

    public String getDescription() {
        return description;
    }

    JobState(String description) {
        this.description = description;
    }
}
