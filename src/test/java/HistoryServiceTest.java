import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.gb.chat.client.HistoryService;

import java.io.File;
import java.lang.reflect.Field;

public class HistoryServiceTest {

    private HistoryService historyService;


    @Test
    public void testLoad() throws NoSuchFieldException, IllegalAccessException {

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("histotyLoadTest.txt").getFile());

        Field field = HistoryService.class.getDeclaredField("HISTORY_FILE");
        field.setAccessible(true);
        field.set(null, file.getAbsolutePath());


        Assertions.assertEquals("Тест\n", HistoryService.load());
    }




}