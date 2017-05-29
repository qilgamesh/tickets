package model;

/**
 * Перечисление возможных состояний задания
 * <p>
 * Created by Andrey Semenyuk on 2017.
 */
public enum JobState {

    ACTIVE("Активно"),
    ARCHIVED("Архивный"),
    EDITABLE("Редактируется"),
    ERROR("Ошибка"),
    CHECKIN("Регистрация"),
    COMPLETED("Завершено"),
    READY("Готов к регистрации"),
    NEW("Новое");

    private String description;

    public String getDescription() {
        return description;
    }

    JobState(String description) {
        this.description = description;
    }
}
