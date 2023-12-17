package com.platformapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/* TODO
*   TestService
*       Добавление нового теста
*       Удаление теста
*       Изменение заданий теста
*       Соответственно получение тестов(а)
*
*  TODO Endpoints
*       Создать тест
*       Обновить тест
*       Удалить тест
*       Начать тест
*       Завершить тест (Kafka)
*       Автор может псмотреть результаты теста + модерация
*
*
* TODO Checking service (Kafka)
*       проверка теста + баллы
*
* Todo валидация
*
*
* Todo доделать аунтификацию + подумать о ролях
*  */
@SpringBootApplication
public class PlatformApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlatformApiApplication.class, args);
    }

}
