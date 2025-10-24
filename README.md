
<!-- PROJECT LOGO -->
<br />
<div align="center">

<h3 align="center">Social Network API</h3>

  <p align="center">
    Бэкенд для социальной сети на Spring Boot
    <br />
    <br />
    <br />
    <a href="https://github.com/sphinx46/social-network/issues/new?labels=bug">Сообщить об ошибке</a>
    &nbsp;&nbsp;&nbsp;&nbsp;·&nbsp;&nbsp;&nbsp;&nbsp;
    <a href="https://github.com/sphinx46/social-network/issues/new?labels=enhancement">Запросить функцию</a>
  </p>
</div>

<!-- SKILL ICONS -->
<p align="center">
  <img src="https://skillicons.dev/icons?i=java,spring,maven,hibernate" />
</p>

## О проекте

Social Network API - это учебный проект, бэкенд для социальной сети, разработанный на Spring Boot. 
Проект представляет собой монолитную версию с cтруктурированной архитектурой и готов к переходу на микросервисную архитектуру.

### Текущий функционал

- **🔐 Аутентификация и авторизация** - JWT-based аутентификация с Spring Security
- **👤 Управление профилями** - Создание и редактирование профилей, загрузка аватарок
- **📝 Система постов** - Создание, редактирование, удаление постов с возможностью загрузки изображений
- **💬 Комментарии** - Полнофункциональная система комментариев к постам
- **❤️ Лайки** - Лайки для постов и комментариев
- **👥 Система друзей** - Запросы на дружбу, принятие/отклонение, черный список
- **📨 Личные сообщения** - Система обмена сообщениями между пользователями
- **🔔 Уведомления** - Система уведомлений с отметками о прочтении
- **📰 Лента новостей** - Персонализированная лента постов друзей
- **📚 Документация API** - Полная документация с Swagger/OpenAPI

### Технологии:

* [![Java][Java]][Java-url]
* [![Spring Boot][Spring]][Spring-url]
* [![Maven][Maven]][Maven-url]
* [![Hibernate][Hibernate]][Hibernate-url]

## Начало работы

Для запуска проекта локально выполните следующие шаги.

### Предварительные требования

* Java 17 или выше
* Maven 3.6+

### Установка

1. Клонируйте репозиторий:
   ```sh
   git clone https://github.com/sphinx46/social-network.git
   ```
2. Перейдите в директорию проекта:
   ```sh
   cd social-network
   ```
3. Соберите проект с помощью Maven:
   ```sh
   mvn clean install
   ```
4. Запустите приложение:
   ```sh
   mvn spring-boot:run
   ```

### 🌐 Доступ к интерфейсам:

- **📚 Swagger UI** - `http://localhost:8080/swagger-ui.html`
- **🗄️ H2 Console** - `http://localhost:8080/h2-console`
  - **JDBC URL:** `jdbc:h2:mem:security-security`
  - **Username:** `root`
  - **Password:** `root`


 ## 🚀 Планы по развитию:
    
- ⚡ **Redis** - Кэширование ленты новостей
- 🚀 **Apache Kafka** - Асинхронная обработка
- 🏗️ **Микросервисы** - Масштабируемая архитектура
- 🔐 **Keycloak интеграция** - Единая система аутентификации и управления пользователям
- 🐳 **Docker контейнеризация** - Упаковка приложения в контейнеры для простого развертывания
- 🎯 **Алгоритмы рекомендаций друзей и постов**
- 🔍 **Полнотекстовый поиск**
- 📊 **Мониторинг и логирование**
- ⚡ **Оптимизация производительности**

[Java]: https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=java&logoColor=white
[Java-url]: https://www.java.com/
[Spring]: https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white
[Spring-url]: https://spring.io/projects/spring-boot
[Maven]: https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white
[Maven-url]: https://maven.apache.org
[Hibernate]: https://img.shields.io/badge/Hibernate-59666C?style=for-the-badge&logo=Hibernate&logoColor=white
[Hibernate-url]: https://hibernate.org/
