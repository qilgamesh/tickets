package model;

/**
 * Перечисление возможных состояний задания
 *
 * Created by Andrey Semenyuk on 2017.
 */
public enum JobState {
    ACTIVE("active", "Активно"),
    EMPTY("", ""),
    COMPLETED("completed", "Завершено"),
    NEW("new", "Новое"),
    ERROR("error", "Ошибка");

    private String id;
    private String description;

    JobState(String id, String description) {
        this.id = id;
        this.description = description;
    }

    public JobState getById(String id) {
        for (JobState value: JobState.values()) {
            if (value.id.equals(id)) return value;
        }
        return EMPTY;
    }
}
