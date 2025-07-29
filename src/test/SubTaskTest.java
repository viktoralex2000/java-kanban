package test;

import com.yandex.app.model.SubTask;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SubTaskTest {
    // Проверка проверка на равенство по айди у подзадачи (2)
    @Test
    void shouldReturnTrueWhenIdsAreEqual() {
        SubTask sub1 = new SubTask("a", "b", 2);
        SubTask sub2 = new SubTask("c", "d", 2);
        sub1.setId(10);
        sub2.setId(10);

        assertEquals(sub1, sub2);
    }
}
